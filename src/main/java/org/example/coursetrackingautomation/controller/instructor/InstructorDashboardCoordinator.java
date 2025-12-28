package org.example.coursetrackingautomation.controller.instructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.controller.EditGradePopupController;
import org.example.coursetrackingautomation.controller.support.CourseHoursLabelFormatter;
import org.example.coursetrackingautomation.controller.support.WeeksListFactory;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.dto.GradeStatus;
import org.example.coursetrackingautomation.dto.InstructorCourseRosterDTO;
import org.example.coursetrackingautomation.service.InstructorWorkflowService;
import org.example.coursetrackingautomation.ui.FxAsync;
import org.example.coursetrackingautomation.ui.GradeStatusUiMapper;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;

public class InstructorDashboardCoordinator {

    /**
     * Encapsulates the instructor dashboard workflow and state.
     *
     * <p>Handles course/week selection, roster loading, attendance application, grade editing,
     * saving changes and guarding navigation/close when there are unsaved edits. The owning FXML
     * controller delegates to this class to keep controllers small and modular.</p>
     */

    private final ComboBox<String> comboCourses;
    private final ComboBox<String> comboWeeks;
    private final Label lblCourseHours;
    private final TableView<GradeDTO> tableStudents;

    private final UserSession userSession;
    private final InstructorWorkflowService instructorWorkflowService;
    private final SceneNavigator sceneNavigator;
    private final UiExceptionHandler uiExceptionHandler;
    private final AlertUtil alertUtil;

    private CourseDTO selectedCourse;
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

    /**
     * Creates a coordinator instance.
     *
     * @param comboCourses course selector
     * @param comboWeeks week selector
     * @param lblCourseHours label showing the selected course hour breakdown
     * @param tableStudents table containing roster/grade rows
     * @param userSession current user session
     * @param instructorWorkflowService workflow service used to load/save instructor data
     * @param sceneNavigator navigation helper for modals/scenes
     * @param uiExceptionHandler centralized UI exception handling
     * @param alertUtil UI alert helper
     */
    public InstructorDashboardCoordinator(
        ComboBox<String> comboCourses,
        ComboBox<String> comboWeeks,
        Label lblCourseHours,
        TableView<GradeDTO> tableStudents,
        UserSession userSession,
        InstructorWorkflowService instructorWorkflowService,
        SceneNavigator sceneNavigator,
        UiExceptionHandler uiExceptionHandler,
        AlertUtil alertUtil
    ) {
        this.comboCourses = comboCourses;
        this.comboWeeks = comboWeeks;
        this.lblCourseHours = lblCourseHours;
        this.tableStudents = tableStudents;
        this.userSession = userSession;
        this.instructorWorkflowService = instructorWorkflowService;
        this.sceneNavigator = sceneNavigator;
        this.uiExceptionHandler = uiExceptionHandler;
        this.alertUtil = alertUtil;
    }

    /**
     * Loads initial data and wires selection listeners.
     */
    public void initialize() {
        clearCourseHoursLabel();

        try {
            var currentUser = userSession.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException(UiConstants.ERROR_KEY_NO_ACTIVE_SESSION));

            FxAsync.runAsync(
                () -> instructorWorkflowService.getActiveCourseCodesForInstructor(currentUser.id()),
                courseCodes -> {
                    configureCourseCombo(courseCodes);
                    configureWeekCombo();
                },
                uiExceptionHandler::handle
            );
        } catch (IllegalStateException e) {
            uiExceptionHandler.handle(e);
        }

        Platform.runLater(this::installStageCloseHandler);
    }

    /**
     * @return the currently selected course, or {@code null} if none is selected
     */
    public CourseDTO getSelectedCourse() {
        return selectedCourse;
    }

    /**
     * Returns (and lazily creates) the midterm score property for a table row.
     *
     * @param row grade row
     * @return observable property backing the table cell
     */
    public SimpleObjectProperty<Double> getMidtermProp(GradeDTO row) {
        if (row == null || row.getStudentId() == null) {
            return new SimpleObjectProperty<>(null);
        }
        return midtermPropByStudentId.computeIfAbsent(row.getStudentId(), k -> new SimpleObjectProperty<>(row.getMidtermScore()));
    }

    /**
     * Returns (and lazily creates) the final score property for a table row.
     *
     * @param row grade row
     * @return observable property backing the table cell
     */
    public SimpleObjectProperty<Double> getFinalProp(GradeDTO row) {
        if (row == null || row.getStudentId() == null) {
            return new SimpleObjectProperty<>(null);
        }
        return finalPropByStudentId.computeIfAbsent(row.getStudentId(), k -> new SimpleObjectProperty<>(row.getFinalScore()));
    }

    /**
     * Returns (and lazily creates) the average score property for a table row.
     *
     * @param row grade row
     * @return observable property backing the table cell
     */
    public SimpleObjectProperty<Double> getAverageProp(GradeDTO row) {
        if (row == null || row.getStudentId() == null) {
            return new SimpleObjectProperty<>(null);
        }
        return averagePropByStudentId.computeIfAbsent(row.getStudentId(), k -> new SimpleObjectProperty<>(row.getAverageScore()));
    }

    /**
     * Returns (and lazily creates) the letter grade property for a table row.
     *
     * @param row grade row
     * @return observable property backing the table cell
     */
    public SimpleObjectProperty<String> getLetterProp(GradeDTO row) {
        if (row == null || row.getStudentId() == null) {
            return new SimpleObjectProperty<>(null);
        }
        return letterPropByStudentId.computeIfAbsent(row.getStudentId(), k -> new SimpleObjectProperty<>(row.getLetterGrade()));
    }

    /**
     * Returns (and lazily creates) the status property for a table row.
     *
     * @param row grade row
     * @return observable property backing the table cell
     */
    public SimpleObjectProperty<String> getStatusProp(GradeDTO row) {
        if (row == null || row.getStudentId() == null) {
            return new SimpleObjectProperty<>(null);
        }
        return statusPropByStudentId.computeIfAbsent(row.getStudentId(), k -> new SimpleObjectProperty<>(toTurkishStatus(row.getStatus())));
    }

    private String toTurkishStatus(GradeStatus status) {
        return status == null ? "" : GradeStatusUiMapper.toTurkish(status);
    }

    /**
     * Marks the screen as dirty when the row differs from the last known saved state.
     *
     * @param row grade row that has been edited
     */
    public void markDirtyIfChanged(GradeDTO row) {
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

    /**
     * Handles course selection changes.
     *
     * <p>Loads the roster for the selected course, initializes week defaults and updates UI labels.</p>
     */
    public void handleCourseSelection() {
        String courseCode = comboCourses == null ? null : comboCourses.getValue();
        if (courseCode == null || courseCode.isBlank()) {
            resetForNoCourseSelection();
            return;
        }

        FxAsync.runAsync(
            () -> instructorWorkflowService.getCourseRoster(courseCode),
            this::applyLoadedRoster,
            uiExceptionHandler::handle
        );
    }

    /**
     * Handles week selection changes and applies attendance for the selected week.
     */
    public void handleWeekSelection() {
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
            applyAttendanceForSelectedWeekAsync();
        } catch (NumberFormatException e) {
            uiExceptionHandler.handle(e);
        }
    }

    /**
     * Saves changes to grades/attendance for the current course/week.
     *
     * @param afterSuccessfulSave optional callback executed after a successful save
     * @return {@code true} when the save completes successfully
     */
    public void performSave(Optional<Runnable> afterSuccessfulSave) {
        String courseCode = comboCourses == null ? null : comboCourses.getValue();
        if (courseCode == null || courseCode.isBlank()) {
            alertUtil.showWarningAlert(UiConstants.ALERT_TITLE_WARNING, UiConstants.UI_MESSAGE_SELECT_COURSE_FIRST);
            return;
        }
        if (selectedWeekNumber == null) {
            alertUtil.showWarningAlert(UiConstants.ALERT_TITLE_WARNING, UiConstants.UI_MESSAGE_SELECT_WEEK_FIRST);
            return;
        }

        // Preserve current presence UI state in case persistence fails.
        Map<Long, Boolean> presentSnapshot = new HashMap<>();
        if (tableStudents.getItems() != null) {
            for (GradeDTO row : tableStudents.getItems()) {
                if (row != null && row.getStudentId() != null) {
                    presentSnapshot.put(row.getStudentId(), row.getPresent());
                }
            }
        }

        nullOutUnchangedPresenceFlags(tableStudents.getItems());

        FxAsync.runAsync(
            () -> {
                instructorWorkflowService.saveCourseStudentUpdates(courseCode, selectedWeekNumber, tableStudents.getItems());
            },
            () -> {
                syncUiPropertiesFromRows();
                tableStudents.refresh();
                alertUtil.showSuccessAlert(UiConstants.ALERT_TITLE_SUCCESS, UiConstants.UI_MESSAGE_CHANGES_SAVED);

                applyAttendanceForSelectedWeekAsync();
                resetDirtyTrackingFromCurrentRows();
                hasUnsavedChanges = false;

                afterSuccessfulSave.ifPresent(Runnable::run);
            },
            failure -> {
                // Restore presence UI state so the table doesn't end up with null values on failure.
                if (tableStudents.getItems() != null) {
                    for (GradeDTO row : tableStudents.getItems()) {
                        if (row != null && row.getStudentId() != null) {
                            row.setPresent(presentSnapshot.get(row.getStudentId()));
                        }
                    }
                    tableStudents.refresh();
                }
                uiExceptionHandler.handle(failure);
            }
        );
    }

    /**
     * Performs a logout attempt.
     *
     * <p>If there are unsaved changes, prompts the user to save or discard before exiting.</p>
     *
     * @param stage current stage
     */
    public void attemptExit(Stage stage) {
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

    /**
     * Opens the grade edit popup and synchronizes the edited values back to the table.
     *
     * @param item the row to edit
     */
    public void openEditGradePopup(GradeDTO item) {
        sceneNavigator.openModalWithController(
            UiConstants.FXML_EDIT_GRADE_POPUP,
            UiConstants.WINDOW_TITLE_EDIT_GRADE,
            tableStudents.getScene().getWindow(),
            (EditGradePopupController c) -> c.setContext(item, updated -> {
                getMidtermProp(updated).set(updated.getMidtermScore());
                getFinalProp(updated).set(updated.getFinalScore());
                getAverageProp(updated).set(updated.getAverageScore());
                getLetterProp(updated).set(updated.getLetterGrade());
                getStatusProp(updated).set(toTurkishStatus(updated.getStatus()));
                markDirtyIfChanged(updated);
                tableStudents.refresh();
            })
        );
    }

    private void configureCourseCombo(java.util.List<String> courseCodes) {
        if (comboCourses == null) {
            return;
        }
        comboCourses.setItems(FXCollections.observableArrayList(courseCodes));
        comboCourses.setOnAction(e -> handleCourseSelection());
    }

    private void configureWeekCombo() {
        if (comboWeeks == null) {
            return;
        }
        comboWeeks.setItems(FXCollections.observableArrayList());
        comboWeeks.setOnAction(e -> handleWeekSelection());
    }

    private void clearCourseHoursLabel() {
        if (lblCourseHours != null) {
            lblCourseHours.setText("");
        }
    }

    private void resetForNoCourseSelection() {
        tableStudents.setItems(FXCollections.observableArrayList());
        selectedCourse = null;
        selectedWeekNumber = null;
        enrollmentIdByStudentId.clear();
        originalPresentByStudentId.clear();
        clearDirtyTracking();
        clearCourseHoursLabel();

        if (comboWeeks != null) {
            comboWeeks.setItems(FXCollections.observableArrayList());
            comboWeeks.setValue(null);
        }
    }

    private void applyLoadedRoster(InstructorCourseRosterDTO roster) {
        if (roster == null) {
            resetForNoCourseSelection();
            return;
        }

        selectedCourse = roster.course();
        originalPresentByStudentId.clear();

        setupWeeksForSelectedCourse();
        updateCourseHoursLabel(selectedCourse);

        ObservableList<GradeDTO> rows = FXCollections.observableArrayList(roster.rows());
        enrollmentIdByStudentId.clear();
        enrollmentIdByStudentId.putAll(roster.enrollmentIdByStudentId());
        originalPresentByStudentId.clear();
        clearDirtyTracking();

        tableStudents.setItems(rows);

        initializeUiPropertiesForRows(rows);
        resetDirtyTrackingFromCurrentRows();
        hasUnsavedChanges = false;

        FxAsync.runAsync(
            () -> instructorWorkflowService.getNextWeekNumber(selectedCourse == null ? null : selectedCourse.getId()),
            defaultWeek -> {
                selectedWeekNumber = defaultWeek;
                if (comboWeeks != null) {
                    comboWeeks.setValue(String.valueOf(defaultWeek));
                }
                applyAttendanceForSelectedWeekAsync();
            },
            uiExceptionHandler::handle
        );
    }

    private void updateCourseHoursLabel(CourseDTO course) {
        if (lblCourseHours == null || course == null) {
            return;
        }

        lblCourseHours.setText(CourseHoursLabelFormatter.format(course));
    }

    private void setupWeeksForSelectedCourse() {
        if (comboWeeks == null || selectedCourse == null) {
            selectedWeekNumber = null;
            return;
        }

        comboWeeks.setItems(WeeksListFactory.buildWeeks(1, 14));

        selectedWeekNumber = null;
        comboWeeks.setValue(null);
    }

    private void applyAttendanceForSelectedWeekAsync() {
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

        Integer week = selectedWeekNumber;
        FxAsync.runAsync(
            () -> instructorWorkflowService.getPresentByEnrollmentIdsAndWeekNumber(enrollmentIds, week),
            presentByEnrollmentId -> {
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
            },
            uiExceptionHandler::handle
        );
    }

    private void nullOutUnchangedPresenceFlags(java.util.List<GradeDTO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (GradeDTO row : items) {
            if (row == null || row.getStudentId() == null) {
                continue;
            }
            Boolean original = originalPresentByStudentId.get(row.getStudentId());
            Boolean current = row.getPresent();
            if (original != null && current != null && original.equals(current)) {
                row.setPresent(null);
            }
        }
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
            midtermPropByStudentId.put(row.getStudentId(), new SimpleObjectProperty<>(row.getMidtermScore()));
            finalPropByStudentId.put(row.getStudentId(), new SimpleObjectProperty<>(row.getFinalScore()));
            averagePropByStudentId.put(row.getStudentId(), new SimpleObjectProperty<>(row.getAverageScore()));
            letterPropByStudentId.put(row.getStudentId(), new SimpleObjectProperty<>(row.getLetterGrade()));
            statusPropByStudentId.put(row.getStudentId(), new SimpleObjectProperty<>(toTurkishStatus(row.getStatus())));
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
            getMidtermProp(row).set(row.getMidtermScore());
            getFinalProp(row).set(row.getFinalScore());
            getAverageProp(row).set(row.getAverageScore());
            getLetterProp(row).set(row.getLetterGrade());
            getStatusProp(row).set(toTurkishStatus(row.getStatus()));
        }
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
        }
    }
}
