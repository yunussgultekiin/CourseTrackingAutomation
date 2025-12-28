package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.controller.support.GradeDetailsMessageBuilder;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.service.AttendanceService;
import org.example.coursetrackingautomation.service.TranscriptService;
import org.example.coursetrackingautomation.ui.FxAsync;
import org.example.coursetrackingautomation.ui.GradeStatusUiMapper;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
/**
 * JavaFX controller for the student dashboard.
 *
 * <p>Displays transcript rows and GPA for the authenticated student, and provides navigation to
 * profile and enrollment modals.</p>
 */
public class StudentDashboardController {

    private static final String PROPERTY_COURSE_CODE = "courseCode";
    private static final String PROPERTY_COURSE_NAME = "courseName";
    private static final String PROPERTY_CREDIT = "credit";
    private static final String PROPERTY_WEEKLY_TOTAL_HOURS = "weeklyTotalHours";
    private static final String PROPERTY_WEEKLY_THEORY_HOURS = "weeklyTheoryHours";
    private static final String PROPERTY_WEEKLY_PRACTICE_HOURS = "weeklyPracticeHours";
    private static final String PROPERTY_MIDTERM_SCORE = "midtermScore";
    private static final String PROPERTY_FINAL_SCORE = "finalScore";
    private static final String PROPERTY_AVERAGE_SCORE = "averageScore";
    private static final String PROPERTY_LETTER_GRADE = "letterGrade";
    private static final String PROPERTY_ATTENDANCE_COUNT = "attendanceCount";
    private static final String PROPERTY_STATUS = "status";

    @FXML private Label lblWelcome;
    @FXML private Label lblGpa;
    @FXML private Button btnLogOut;
    @FXML private TableView<GradeDTO> tableStudentCourses;
    @FXML private TableColumn<GradeDTO, String> colCourseCode;
    @FXML private TableColumn<GradeDTO, String> colCourseName;
    @FXML private TableColumn<GradeDTO, Integer> colCredit;
    @FXML private TableColumn<GradeDTO, Integer> colWeeklyTotalHours;
    @FXML private TableColumn<GradeDTO, Integer> colWeeklyTheoryHours;
    @FXML private TableColumn<GradeDTO, Integer> colWeeklyPracticeHours;
    @FXML private TableColumn<GradeDTO, Double> colMidterm;
    @FXML private TableColumn<GradeDTO, Double> colFinal;
    @FXML private TableColumn<GradeDTO, Double> colAverage;
    @FXML private TableColumn<GradeDTO, String> colLetter;
    @FXML private TableColumn<GradeDTO, Integer> colAttendance;
    @FXML private TableColumn<GradeDTO, String> colStatus;

    private final UserSession userSession;
    private final TranscriptService transcriptService;
    private final AttendanceService attendanceService;
    private final SceneNavigator sceneNavigator;
    private final UiExceptionHandler uiExceptionHandler;
    private final AlertUtil alertUtil;

    @FXML
    /**
     * Initializes the student dashboard and loads transcript data.
     */
    public void initialize() {
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_COURSE_CODE));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_COURSE_NAME));
        colCredit.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_CREDIT));
        colWeeklyTotalHours.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_WEEKLY_TOTAL_HOURS));
        colWeeklyTheoryHours.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_WEEKLY_THEORY_HOURS));
        colWeeklyPracticeHours.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_WEEKLY_PRACTICE_HOURS));
        colMidterm.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_MIDTERM_SCORE));
        colFinal.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_FINAL_SCORE));
        colAverage.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_AVERAGE_SCORE));
        colLetter.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_LETTER_GRADE));
        colAttendance.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_ATTENDANCE_COUNT));
        colAttendance.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                GradeDTO row = getTableRow() == null ? null : getTableRow().getItem();
                Integer absentHours = row == null ? null : row.getAttendanceCount();
                int hours = absentHours == null ? 0 : absentHours;

                int totalCourseHours = attendanceService == null
                    ? 0
                    : attendanceService.getTotalCourseHoursForTerm(row == null ? null : row.getWeeklyTotalHours());
                boolean critical = attendanceService != null && attendanceService.isAttendanceCritical(totalCourseHours, hours);
                boolean warning = !critical && attendanceService != null && attendanceService.isAttendanceWarning(totalCourseHours, hours);

                String badgeClass = critical ? "badge-danger" : (warning ? "badge-warning" : "badge-neutral");
                Label badge = new Label(String.valueOf(hours));
                badge.getStyleClass().addAll("badge", badgeClass);

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });
        colStatus.setCellValueFactory(cell -> {
            GradeDTO row = cell.getValue();
            String text = row == null || row.getStatus() == null ? "-" : GradeStatusUiMapper.toTurkish(row.getStatus());
            return new SimpleStringProperty(text);
        });
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || value.isBlank() || "-".equals(value)) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String badgeClass;
                if (UiConstants.UI_STATUS_PASSED.equalsIgnoreCase(value)) {
                    badgeClass = "badge-success";
                } else if (UiConstants.UI_STATUS_FAILED.equalsIgnoreCase(value)) {
                    badgeClass = "badge-danger";
                } else {
                    badgeClass = "badge-neutral";
                }

                Label badge = new Label(value);
                badge.getStyleClass().addAll("badge", badgeClass);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });
        setupRowFactory();
        refresh();
    }

    private void setupRowFactory() {
        tableStudentCourses.setRowFactory(tv -> {
            TableRow<GradeDTO> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showCourseDetails(row.getItem());
                }
            });

            return row;
        });
    }

    private void showCourseDetails(GradeDTO item) {
        if (item == null) {
            return;
        }

        alertUtil.showInformationAlert("Ders Detayı", GradeDetailsMessageBuilder.buildStudentDashboardMessage(item));
    }

    private void refresh() {
        try {
            var currentUser = userSession.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException(UiConstants.ERROR_KEY_NO_ACTIVE_SESSION));

            lblWelcome.setText(UiConstants.UI_WELCOME_PREFIX + currentUser.fullName());

            FxAsync.runAsync(
                () -> transcriptService.getTranscriptGradesForStudent(currentUser.id()),
                transcriptRows -> {
                    ObservableList<GradeDTO> transcript = FXCollections.observableArrayList(transcriptRows);
                    tableStudentCourses.setItems(transcript);
                    lblGpa.setText(transcriptService.calculateGpaText(transcript));
                },
                failure -> {
                    uiExceptionHandler.handle(failure);
                    if (lblWelcome.getScene() != null) {
                        Stage stage = (Stage) lblWelcome.getScene().getWindow();
                        sceneNavigator.showLogin(stage);
                    }
                }
            );
        } catch (IllegalStateException e) {
            uiExceptionHandler.handle(e);
            if (lblWelcome.getScene() != null) {
                Stage stage = (Stage) lblWelcome.getScene().getWindow();
                sceneNavigator.showLogin(stage);
            }
        }
    }

    @FXML
    /**
     * Logs out the current user and navigates back to the login view.
     */
    public void handleLogout() {
        Stage stage = (Stage) btnLogOut.getScene().getWindow();
        sceneNavigator.performLogout(stage);
    }

    @FXML
    /**
     * Opens the profile modal and refreshes the dashboard after closing.
     */
    public void handleProfile() {
        sceneNavigator.openModal(
                UiConstants.FXML_PROFILE_POPUP,
                UiConstants.WINDOW_TITLE_PROFILE,
                lblWelcome.getScene().getWindow()
        );
        refresh();
    }

    @FXML
    /**
     * Opens the transcript modal.
     */
    public void handleShowTranscript() {
        sceneNavigator.openModal(
                UiConstants.FXML_TRANSCRIPT_POPUP,
                UiConstants.WINDOW_TITLE_TRANSCRIPT,
                lblWelcome.getScene().getWindow()
        );
    }

    @FXML
    /**
     * Opens the enroll-course modal and refreshes the dashboard after closing.
     */
    public void handleEnrollCourse() {
        sceneNavigator.openModal(
                UiConstants.FXML_ENROLL_COURSE_POPUP,
                UiConstants.WINDOW_TITLE_ENROLL_COURSE,
                lblWelcome.getScene().getWindow()
        );
        refresh();
    }
}