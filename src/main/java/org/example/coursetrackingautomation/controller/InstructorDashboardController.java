package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.StringConverter;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.entity.Course;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.service.AttendanceService;
import org.example.coursetrackingautomation.service.GradeService;
import org.example.coursetrackingautomation.service.InstructorWorkflowService;
import org.example.coursetrackingautomation.repository.AttendanceRecordRepository;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class InstructorDashboardController {

    private static final String PROPERTY_STUDENT_ID = "studentId";
    private static final String PROPERTY_STUDENT_NAME = "studentName";
    private static final String PROPERTY_AVERAGE_SCORE = "averageScore";
    private static final String PROPERTY_STATUS = "status";
    private static final String PROPERTY_ATTENDANCE_COUNT = "attendanceCount";
    private static final String PROPERTY_MIDTERM_SCORE = "midtermScore";
    private static final String PROPERTY_FINAL_SCORE = "finalScore";
    private static final String PROPERTY_LETTER_GRADE = "letterGrade";

    private static final String STYLE_CRITICAL_ATTENDANCE = "critical-attendance";
    private static final String STYLE_WARNING_ATTENDANCE = "warning-attendance";

    @FXML private ComboBox<String> comboCourses;
    @FXML private ComboBox<String> comboWeeks;
    @FXML private Label lblCourseHours;
    @FXML private TableView<GradeDTO> tableStudents;
    @FXML private TableColumn<GradeDTO, Long> colStudentNumber;
    @FXML private TableColumn<GradeDTO, String> colFullName;
    @FXML private TableColumn<GradeDTO, Double> colMidterm;
    @FXML private TableColumn<GradeDTO, Double> colFinal;
    @FXML private TableColumn<GradeDTO, Integer> colAttendance;
    @FXML private TableColumn<GradeDTO, Boolean> colPresent;
    @FXML private TableColumn<GradeDTO, Double> colAverage;
    @FXML private TableColumn<GradeDTO, String> colLetterGrade;
    @FXML private TableColumn<GradeDTO, String> colStatus;
    @FXML private Button btnSave;
    @FXML private Button btnLogOut;
    @FXML private Button btnProfile;

    private final UserSession userSession;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final InstructorWorkflowService instructorWorkflowService;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;
    private final SceneNavigator sceneNavigator;
    private final UiExceptionHandler uiExceptionHandler;
    private final AlertUtil alertUtil;

    private Course selectedCourse;
    private Integer selectedWeekNumber;
    private final Map<Long, Long> enrollmentIdByStudentId = new HashMap<>();
    private final Map<Long, Boolean> originalPresentByStudentId = new HashMap<>();

    private final Map<Long, Double> originalMidtermByStudentId = new HashMap<>();
    private final Map<Long, Double> originalFinalByStudentId = new HashMap<>();
    private final Map<Long, Integer> originalAttendanceHoursByStudentId = new HashMap<>();

    private final Map<Long, SimpleObjectProperty<Double>> midtermPropByStudentId = new HashMap<>();
    private final Map<Long, SimpleObjectProperty<Double>> finalPropByStudentId = new HashMap<>();
    private final Map<Long, SimpleObjectProperty<Double>> averagePropByStudentId = new HashMap<>();
    private final Map<Long, SimpleObjectProperty<String>> letterPropByStudentId = new HashMap<>();
    private final Map<Long, SimpleObjectProperty<String>> statusPropByStudentId = new HashMap<>();

    private boolean hasUnsavedChanges = false;

    private final StringConverter<Double> scoreConverter = new StringConverter<>() {
        @Override
        public String toString(Double value) {
            return value == null ? "" : stripTrailingZeros(value);
        }

        @Override
        public Double fromString(String text) {
            if (text == null) {
                return null;
            }
            String normalized = text.trim();
            if (normalized.isEmpty()) {
                return null;
            }
            normalized = normalized.replace(',', '.');
            return Double.parseDouble(normalized);
        }
    };

    @FXML
    public void initialize() {
        setupTableColumns();
        setupRowColorFactory();
        tableStudents.setEditable(true);

        Platform.runLater(this::installStageCloseHandler);

        if (lblCourseHours != null) {
            lblCourseHours.setText("");
        }

        try {
            var currentUser = userSession.getCurrentUser().orElseThrow(() -> new IllegalStateException(UiConstants.ERROR_KEY_NO_ACTIVE_SESSION));
            var courses = courseRepository.findByInstructorIdAndActiveTrue(currentUser.id());
            comboCourses.setItems(FXCollections.observableArrayList(courses.stream().map(Course::getCode).toList()));
            comboCourses.setOnAction(e -> handleCourseSelection());

            if (comboWeeks != null) {
                comboWeeks.setItems(FXCollections.observableArrayList());
                comboWeeks.setOnAction(e -> handleWeekSelection());
            }
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleLogout() {
        Stage stage = (Stage) btnLogOut.getScene().getWindow();
        attemptExit(stage);
    }

    @FXML
    public void handleProfile() {
        sceneNavigator.openModal(
            UiConstants.FXML_PROFILE_POPUP,
            UiConstants.WINDOW_TITLE_PROFILE,
            btnProfile.getScene().getWindow()
        );
    }

    @FXML
    public void handleSave() {
        performSave(Optional.empty());
    }

    private boolean performSave(Optional<Runnable> afterSuccessfulSave) {
        try {
            String courseCode = comboCourses.getValue();
            if (courseCode == null || courseCode.isBlank()) {
                alertUtil.showWarningAlert(UiConstants.ALERT_TITLE_WARNING, UiConstants.UI_MESSAGE_SELECT_COURSE_FIRST);
                return false;
            }
            if (selectedWeekNumber == null) {
                alertUtil.showWarningAlert(UiConstants.ALERT_TITLE_WARNING, UiConstants.UI_MESSAGE_SELECT_WEEK_FIRST);
                return false;
            }

            for (GradeDTO row : tableStudents.getItems()) {
                if (row == null || row.getStudentId() == null) {
                    continue;
                }
                Boolean original = originalPresentByStudentId.get(row.getStudentId());
                Boolean current = row.getPresent();
                if (original != null && current != null && original.equals(current)) {
                    row.setPresent(null);
                }
            }

            instructorWorkflowService.saveCourseStudentUpdates(courseCode, selectedWeekNumber, tableStudents.getItems());

            syncUiPropertiesFromRows();
            tableStudents.refresh();
            alertUtil.showSuccessAlert(UiConstants.ALERT_TITLE_SUCCESS, UiConstants.UI_MESSAGE_CHANGES_SAVED);

            applyAttendanceForSelectedWeek();
            resetDirtyTrackingFromCurrentRows();
            hasUnsavedChanges = false;

            afterSuccessfulSave.ifPresent(Runnable::run);
            return true;
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
            return false;
        }
    }

    private void handleCourseSelection() {
        try {
            String courseCode = comboCourses.getValue();
            if (courseCode == null || courseCode.isBlank()) {
                tableStudents.setItems(FXCollections.observableArrayList());
                selectedCourse = null;
                selectedWeekNumber = null;
                enrollmentIdByStudentId.clear();
                originalPresentByStudentId.clear();
                clearDirtyTracking();
                if (lblCourseHours != null) {
                    lblCourseHours.setText("");
                }
                if (comboWeeks != null) {
                    comboWeeks.setItems(FXCollections.observableArrayList());
                    comboWeeks.setValue(null);
                }
                return;
            }

            Course course = courseRepository.findByCode(courseCode)
                .orElseThrow(() -> new IllegalArgumentException(UiConstants.ERROR_KEY_COURSE_NOT_FOUND));

            selectedCourse = course;
            originalPresentByStudentId.clear();

            setupWeeksForSelectedCourse();

            if (lblCourseHours != null) {
                Integer weeklyTotal = course.getWeeklyTotalHours();
                Integer weeklyTheory = course.getWeeklyTheoryHours();
                Integer weeklyPractice = course.getWeeklyPracticeHours();

                String totalText = weeklyTotal == null ? "-" : weeklyTotal.toString();
                String theoryText = weeklyTheory == null ? "-" : weeklyTheory.toString();
                String practiceText = weeklyPractice == null ? "-" : weeklyPractice.toString();
                lblCourseHours.setText("Saat: " + totalText + " (Teori " + theoryText + ", Uyg. " + practiceText + ")");
            }

            String courseName = course.getName();
            Integer credit = course.getCredit();

            ObservableList<GradeDTO> rows = FXCollections.observableArrayList();
            enrollmentIdByStudentId.clear();
            originalPresentByStudentId.clear();
            clearDirtyTracking();
            for (Enrollment enrollment : enrollmentRepository.findByCourseIdWithStudentAndGrade(course.getId())) {
                enrollmentIdByStudentId.put(enrollment.getStudent().getId(), enrollment.getId());
                Double midterm = enrollment.getGrade() == null || enrollment.getGrade().getMidtermScore() == null
                    ? null : enrollment.getGrade().getMidtermScore().doubleValue();
                Double finalScore = enrollment.getGrade() == null || enrollment.getGrade().getFinalScore() == null
                    ? null : enrollment.getGrade().getFinalScore().doubleValue();

                boolean graded = midterm != null && finalScore != null;
                Double average = gradeService.calculateAverage(midterm, finalScore);
                String letter = graded ? gradeService.determineLetterGrade(average) : null;
                boolean passed = graded && gradeService.isPassed(letter);
                int absentHoursUi = attendanceService.toAbsentHours(course, enrollment.getAbsenteeismCount());
                boolean critical = attendanceService.isAttendanceCritical(course, enrollment.getAbsenteeismCount());

                String status;
                if (!graded) {
                    status = UiConstants.UI_STATUS_NOT_GRADED;
                } else {
                    status = passed ? UiConstants.UI_STATUS_PASSED : UiConstants.UI_STATUS_FAILED;
                }

                rows.add(new GradeDTO(
                    enrollment.getStudent().getId(),
                    enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName(),
                    courseCode,
                    courseName,
                    credit,
                    course.getWeeklyTotalHours(),
                    course.getWeeklyTheoryHours(),
                    course.getWeeklyPracticeHours(),
                    midterm,
                    finalScore,
                    average,
                    letter,
                    status,
                    absentHoursUi,
                    critical,
                    true
                ));
            }

            tableStudents.setItems(rows);

            initializeUiPropertiesForRows(rows);
            resetDirtyTrackingFromCurrentRows();
            hasUnsavedChanges = false;

            applyAttendanceForSelectedWeek();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    private void setupWeeksForSelectedCourse() {
        if (comboWeeks == null || selectedCourse == null) {
            selectedWeekNumber = null;
            return;
        }

        ObservableList<String> weeks = FXCollections.observableArrayList();
        for (int i = 1; i <= 14; i++) {
            weeks.add(String.valueOf(i));
        }
        comboWeeks.setItems(weeks);

        int defaultWeek = 1;
        try {
            Integer maxWeekNumber = attendanceRecordRepository.findMaxWeekNumberByCourseId(selectedCourse.getId());
            if (maxWeekNumber != null && maxWeekNumber >= 1) {
                defaultWeek = Math.min(14, maxWeekNumber + 1);
            }
        } catch (Exception ignored) {
            defaultWeek = 1;
        }

        selectedWeekNumber = defaultWeek;
        comboWeeks.setValue(String.valueOf(defaultWeek));
    }

    private void handleWeekSelection() {
        try {
            if (selectedCourse == null) {
                return;
            }

            if (comboWeeks == null) {
                return;
            }

            String weekText = comboWeeks.getValue();
            if (weekText == null || weekText.isBlank()) {
                selectedWeekNumber = null;
                originalPresentByStudentId.clear();
                return;
            }

            selectedWeekNumber = Integer.parseInt(weekText);
            originalPresentByStudentId.clear();
            applyAttendanceForSelectedWeek();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    private void applyAttendanceForSelectedWeek() {
        try {
            if (selectedCourse == null || selectedWeekNumber == null) {
                return;
            }

            var items = tableStudents.getItems();
            if (items == null || items.isEmpty()) {
                return;
            }

            var enrollmentIds = enrollmentIdByStudentId.values();
            if (enrollmentIds.isEmpty()) {
                return;
            }

            var records = attendanceRecordRepository.findByEnrollmentIdsAndWeekNumberWithEnrollment(enrollmentIds, selectedWeekNumber);

            Map<Long, Boolean> presentByEnrollmentId = new HashMap<>();
            for (var record : records) {
                if (record.getEnrollment() != null && record.getEnrollment().getId() != null) {
                    presentByEnrollmentId.put(record.getEnrollment().getId(), record.isPresent());
                }
            }

            for (GradeDTO row : items) {
                if (row == null || row.getStudentId() == null) {
                    continue;
                }
                Long enrollmentId = enrollmentIdByStudentId.get(row.getStudentId());
                boolean present = enrollmentId != null && presentByEnrollmentId.containsKey(enrollmentId)
                    ? presentByEnrollmentId.get(enrollmentId)
                    : true;
                row.setPresent(present);
                originalPresentByStudentId.put(row.getStudentId(), present);
            }

            tableStudents.refresh();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    private void setupTableColumns() {
        colStudentNumber.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_STUDENT_ID));
        colFullName.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_STUDENT_NAME));
        colAverage.setCellValueFactory(cell -> getAverageProp(cell.getValue()));
        if (colLetterGrade != null) {
            colLetterGrade.setCellValueFactory(cell -> getLetterProp(cell.getValue()));
        }
        colStatus.setCellValueFactory(cell -> getStatusProp(cell.getValue()));
        colAttendance.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_ATTENDANCE_COUNT));

        colAttendance.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        colAttendance.setOnEditCommit(event -> {
            GradeDTO row = event.getRowValue();
            Integer newHours = event.getNewValue();
            Integer oldHours = event.getOldValue();

            if (newHours == null || newHours < 0) {
                alertUtil.showErrorAlert("Doğrulama Hatası", "Devamsızlık saati 0 veya daha büyük olmalıdır.");
                row.setAttendanceCount(oldHours);
                tableStudents.refresh();
                return;
            }

            if (selectedCourse != null && selectedCourse.getWeeklyTotalHours() != null && selectedCourse.getWeeklyTotalHours() > 0) {
                int weekly = selectedCourse.getWeeklyTotalHours();
                if (newHours % weekly != 0) {
                    alertUtil.showErrorAlert(
                        "Doğrulama Hatası",
                        "Devamsızlık saati, dersin haftalık toplam saatinin katı olmalıdır (" + weekly + ")."
                    );
                    row.setAttendanceCount(oldHours);
                    tableStudents.refresh();
                    return;
                }
            }

            row.setAttendanceCount(newHours);
            boolean critical = attendanceService.isAttendanceCriticalByHours(selectedCourse, newHours);
            row.setAbsentCritically(critical);
            markDirtyIfChanged(row);
        });

        colPresent.setCellValueFactory(cellData -> {
            GradeDTO row = cellData.getValue();
            boolean initial = row.getPresent() == null ? true : row.getPresent();
            BooleanProperty property = new SimpleBooleanProperty(initial);
            property.addListener((obs, oldValue, newValue) -> {
                row.setPresent(newValue);
                markDirtyIfChanged(row);
            });
            return property;
        });
        colPresent.setCellFactory(CheckBoxTableCell.forTableColumn(colPresent));

        colMidterm.setCellValueFactory(cell -> getMidtermProp(cell.getValue()));
        colMidterm.setEditable(false);
        
        colFinal.setCellValueFactory(cell -> getFinalProp(cell.getValue()));
        colFinal.setEditable(false);
    }

    private void initializeUiPropertiesForRows(ObservableList<GradeDTO> rows) {
        midtermPropByStudentId.clear();
        finalPropByStudentId.clear();
        averagePropByStudentId.clear();
        letterPropByStudentId.clear();
        statusPropByStudentId.clear();

        if (rows == null) {
            return;
        }

        for (GradeDTO row : rows) {
            if (row == null || row.getStudentId() == null) {
                continue;
            }
            Long sid = row.getStudentId();
            midtermPropByStudentId.put(sid, new SimpleObjectProperty<>(row.getMidtermScore()));
            finalPropByStudentId.put(sid, new SimpleObjectProperty<>(row.getFinalScore()));
            averagePropByStudentId.put(sid, new SimpleObjectProperty<>(row.getAverageScore()));
            letterPropByStudentId.put(sid, new SimpleObjectProperty<>(row.getLetterGrade()));
            statusPropByStudentId.put(sid, new SimpleObjectProperty<>(row.getStatus()));
        }
    }

    private void syncUiPropertiesFromRows() {
        var rows = tableStudents.getItems();
        if (rows == null) {
            return;
        }
        for (GradeDTO row : rows) {
            if (row == null || row.getStudentId() == null) {
                continue;
            }
            Long sid = row.getStudentId();
            getMidtermProp(row).set(row.getMidtermScore());
            getFinalProp(row).set(row.getFinalScore());
            getAverageProp(row).set(row.getAverageScore());
            getLetterProp(row).set(row.getLetterGrade());
            getStatusProp(row).set(row.getStatus());
        }
    }

    private SimpleObjectProperty<Double> getMidtermProp(GradeDTO row) {
        if (row == null || row.getStudentId() == null) {
            return new SimpleObjectProperty<>(null);
        }
        return midtermPropByStudentId.computeIfAbsent(row.getStudentId(), k -> new SimpleObjectProperty<>(row.getMidtermScore()));
    }

    private SimpleObjectProperty<Double> getFinalProp(GradeDTO row) {
        if (row == null || row.getStudentId() == null) {
            return new SimpleObjectProperty<>(null);
        }
        return finalPropByStudentId.computeIfAbsent(row.getStudentId(), k -> new SimpleObjectProperty<>(row.getFinalScore()));
    }

    private SimpleObjectProperty<Double> getAverageProp(GradeDTO row) {
        if (row == null || row.getStudentId() == null) {
            return new SimpleObjectProperty<>(null);
        }
        return averagePropByStudentId.computeIfAbsent(row.getStudentId(), k -> new SimpleObjectProperty<>(row.getAverageScore()));
    }

    private SimpleObjectProperty<String> getLetterProp(GradeDTO row) {
        if (row == null || row.getStudentId() == null) {
            return new SimpleObjectProperty<>(null);
        }
        return letterPropByStudentId.computeIfAbsent(row.getStudentId(), k -> new SimpleObjectProperty<>(row.getLetterGrade()));
    }

    private SimpleObjectProperty<String> getStatusProp(GradeDTO row) {
        if (row == null || row.getStudentId() == null) {
            return new SimpleObjectProperty<>(null);
        }
        return statusPropByStudentId.computeIfAbsent(row.getStudentId(), k -> new SimpleObjectProperty<>(row.getStatus()));
    }

    private void recomputeDerivedGradeFields(GradeDTO row) {
        if (row == null) {
            return;
        }

        Double midterm = row.getMidtermScore();
        Double fin = row.getFinalScore();
        boolean graded = midterm != null && fin != null;

        Double average = gradeService.calculateAverage(midterm, fin);
        String letter = graded ? gradeService.determineLetterGrade(average) : null;
        boolean passed = graded && gradeService.isPassed(letter);

        row.setAverageScore(average);
        row.setLetterGrade(letter);
        if (!graded) {
            row.setStatus(UiConstants.UI_STATUS_NOT_GRADED);
        } else {
            row.setStatus(passed ? UiConstants.UI_STATUS_PASSED : UiConstants.UI_STATUS_FAILED);
        }

        getAverageProp(row).set(average);
        getLetterProp(row).set(letter);
        getStatusProp(row).set(row.getStatus());
    }

    private boolean isValidScore(Double score) {
        if (score == null) {
            return true;
        }
        return score >= 0.0 && score <= 100.0;
    }

    private void setMidtermValue(GradeDTO row, Double value) {
        if (row == null) {
            return;
        }
        row.setMidtermScore(value);
        getMidtermProp(row).set(value);
        tableStudents.refresh();
    }

    private void setFinalValue(GradeDTO row, Double value) {
        if (row == null) {
            return;
        }
        row.setFinalScore(value);
        getFinalProp(row).set(value);
        tableStudents.refresh();
    }

    private void resetDirtyTrackingFromCurrentRows() {
        originalMidtermByStudentId.clear();
        originalFinalByStudentId.clear();
        originalAttendanceHoursByStudentId.clear();

        var rows = tableStudents.getItems();
        if (rows == null) {
            return;
        }
        for (GradeDTO row : rows) {
            if (row == null || row.getStudentId() == null) {
                continue;
            }
            Long sid = row.getStudentId();
            originalMidtermByStudentId.put(sid, row.getMidtermScore());
            originalFinalByStudentId.put(sid, row.getFinalScore());
            originalAttendanceHoursByStudentId.put(sid, row.getAttendanceCount());
        }
    }

    private void markDirtyIfChanged(GradeDTO row) {
        if (row == null || row.getStudentId() == null) {
            return;
        }

        Long sid = row.getStudentId();
        Double oMidterm = originalMidtermByStudentId.get(sid);
        Double oFinal = originalFinalByStudentId.get(sid);
        Integer oAttendance = originalAttendanceHoursByStudentId.get(sid);
        Boolean oPresent = originalPresentByStudentId.get(sid);

        boolean changed = !equalsNullable(oMidterm, row.getMidtermScore())
            || !equalsNullable(oFinal, row.getFinalScore())
            || !equalsNullable(oAttendance, row.getAttendanceCount())
            || (oPresent != null && row.getPresent() != null && !oPresent.equals(row.getPresent()));

        if (changed) {
            hasUnsavedChanges = true;
        }
    }

    private void clearDirtyTracking() {
        originalMidtermByStudentId.clear();
        originalFinalByStudentId.clear();
        originalAttendanceHoursByStudentId.clear();
        midtermPropByStudentId.clear();
        finalPropByStudentId.clear();
        averagePropByStudentId.clear();
        letterPropByStudentId.clear();
        statusPropByStudentId.clear();
        hasUnsavedChanges = false;
    }

    private <T> boolean equalsNullable(T a, T b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    private String stripTrailingZeros(Double value) {
        if (value == null) {
            return "";
        }
        // Keep it simple: show integer values without .0
        if (value % 1.0 == 0.0) {
            return String.valueOf(value.intValue());
        }
        return String.valueOf(value);
    }

    private void attemptExit(Stage stage) {
        if (!hasUnsavedChanges) {
            sceneNavigator.performLogout(stage);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kaydedilmemiş Değişiklikler");
        alert.setHeaderText("Kaydedilmemiş değişiklikler var.");
        alert.setContentText("Çıkış yapmadan önce kaydetmek ister misiniz?");

        ButtonType btnSaveAndExit = new ButtonType("Kaydet ve Çık", ButtonBar.ButtonData.YES);
        ButtonType btnExitWithoutSave = new ButtonType("Kaydetmeden Çık", ButtonBar.ButtonData.NO);
        ButtonType btnBack = new ButtonType("Geri Dön", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnSaveAndExit, btnExitWithoutSave, btnBack);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == btnBack) {
            return;
        }

        if (result.get() == btnExitWithoutSave) {
            sceneNavigator.performLogout(stage);
            return;
        }

        if (result.get() == btnSaveAndExit) {
            performSave(Optional.of(() -> sceneNavigator.performLogout(stage)));
        }
    }

    private void installStageCloseHandler() {
        try {
            if (tableStudents == null || tableStudents.getScene() == null) {
                return;
            }
            Stage stage = (Stage) tableStudents.getScene().getWindow();
            if (stage == null) {
                return;
            }
            stage.setOnCloseRequest(evt -> {
                if (!hasUnsavedChanges) {
                    return;
                }
                evt.consume();
                attemptExit(stage);
            });
        } catch (Exception e) {
            // don't block UI for close handler issues
        }
    }

    private void setupRowColorFactory() {
        tableStudents.setRowFactory(tv -> {
            TableRow<GradeDTO> row = new TableRow<>() {
                @Override
                protected void updateItem(GradeDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setStyle("");
                        getStyleClass().removeAll(STYLE_CRITICAL_ATTENDANCE, STYLE_WARNING_ATTENDANCE);
                    } else {
                        int absentHours = item.getAttendanceCount() == null ? 0 : item.getAttendanceCount();
                        boolean critical = attendanceService.isAttendanceCriticalByHours(selectedCourse, absentHours);
                        boolean warning = !critical && attendanceService.isAttendanceWarningByHours(selectedCourse, absentHours);

                        getStyleClass().removeAll(STYLE_CRITICAL_ATTENDANCE, STYLE_WARNING_ATTENDANCE);
                        if (critical) {
                            getStyleClass().add(STYLE_CRITICAL_ATTENDANCE);
                        } else if (warning) {
                            getStyleClass().add(STYLE_WARNING_ATTENDANCE);
                        }
                    }
                }
            };

            row.setOnMouseClicked(event -> {
                if (event.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                if (event.getClickCount() == 2 && !row.isEmpty() && tableStudents.getEditingCell() == null) {
                    GradeDTO item = row.getItem();
                    if (item == null) {
                        return;
                    }

                    sceneNavigator.openModalWithController(
                        UiConstants.FXML_EDIT_GRADE_POPUP,
                        UiConstants.WINDOW_TITLE_EDIT_GRADE,
                        tableStudents.getScene().getWindow(),
                        (EditGradePopupController c) -> c.setContext(item, updated -> {
                            // Keep UI properties in sync and mark dirty.
                            getMidtermProp(updated).set(updated.getMidtermScore());
                            getFinalProp(updated).set(updated.getFinalScore());
                            getAverageProp(updated).set(updated.getAverageScore());
                            getLetterProp(updated).set(updated.getLetterGrade());
                            getStatusProp(updated).set(updated.getStatus());
                            markDirtyIfChanged(updated);
                            tableStudents.refresh();
                        })
                    );
                }
            });

            return row;
        });
    }

}