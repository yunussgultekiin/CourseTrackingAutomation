package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.service.AttendanceService;
import org.example.coursetrackingautomation.service.TranscriptService;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
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

    private static final String STYLE_CRITICAL_ATTENDANCE = "critical-attendance";
    private static final String STYLE_WARNING_ATTENDANCE = "warning-attendance";

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
        colStatus.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_STATUS));
        setupRowColorFactory();
        refresh();
    }

    private void setupRowColorFactory() {
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

            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                row.getStyleClass().removeAll(STYLE_CRITICAL_ATTENDANCE, STYLE_WARNING_ATTENDANCE);

                if (newItem == null) {
                    return;
                }

                int absentHours = newItem.getAttendanceCount() == null ? 0 : newItem.getAttendanceCount();
                int totalCourseHours = attendanceService.getTotalCourseHoursForTerm(newItem.getWeeklyTotalHours());
                boolean critical = attendanceService.isAttendanceCritical(totalCourseHours, absentHours);
                boolean warning = !critical && attendanceService.isAttendanceWarning(totalCourseHours, absentHours);

                if (critical) {
                    row.getStyleClass().add(STYLE_CRITICAL_ATTENDANCE);
                } else if (warning) {
                    row.getStyleClass().add(STYLE_WARNING_ATTENDANCE);
                }
            });

            return row;
        });
    }

    private void showCourseDetails(GradeDTO item) {
        if (item == null) {
            return;
        }

        String midtermText = item.getMidtermScore() == null ? "-" : String.valueOf(item.getMidtermScore());
        String finalText = item.getFinalScore() == null ? "-" : String.valueOf(item.getFinalScore());
        String averageText = item.getAverageScore() == null ? "-" : String.valueOf(item.getAverageScore());
        String letterText = item.getLetterGrade() == null ? "-" : item.getLetterGrade();
        String statusText = item.getStatus() == null ? "-" : item.getStatus();

        String weeklyTotal = item.getWeeklyTotalHours() == null ? "-" : item.getWeeklyTotalHours().toString();
        String weeklyTheory = item.getWeeklyTheoryHours() == null ? "-" : item.getWeeklyTheoryHours().toString();
        String weeklyPractice = item.getWeeklyPracticeHours() == null ? "-" : item.getWeeklyPracticeHours().toString();

        String message = "Ders: " + item.getCourseCode() + " - " + item.getCourseName() + "\n"
                + "Kredi: " + (item.getCredit() == null ? "-" : item.getCredit()) + "\n"
                + "Saat (Haftalık): " + weeklyTotal + " (Teori " + weeklyTheory + ", Uygulama " + weeklyPractice + ")\n"
                + "Vize: " + midtermText + "\n"
                + "Final: " + finalText + "\n"
                + "Ortalama: " + averageText + "\n"
                + "Harf Notu: " + letterText + "\n"
                + "Devamsızlık (Saat): " + (item.getAttendanceCount() == null ? 0 : item.getAttendanceCount()) + "\n"
                + "Durum: " + statusText;

        alertUtil.showInformationAlert("Ders Detayı", message);
    }

    private void refresh() {
        try {
            var currentUser = userSession.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException(UiConstants.ERROR_KEY_NO_ACTIVE_SESSION));

                lblWelcome.setText(UiConstants.UI_WELCOME_PREFIX + currentUser.fullName());

            ObservableList<GradeDTO> transcript = FXCollections.observableArrayList(
                    transcriptService.getTranscriptGradesForStudent(currentUser.id())
            );
            tableStudentCourses.setItems(transcript);
            lblGpa.setText(transcriptService.calculateGpaText(transcript));
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
            if (lblWelcome.getScene() != null) {
                Stage stage = (Stage) lblWelcome.getScene().getWindow();
                sceneNavigator.showLogin(stage);
            }
        }
    }

    @FXML
    public void handleLogout() {
        Stage stage = (Stage) btnLogOut.getScene().getWindow();
        sceneNavigator.performLogout(stage);
    }

    @FXML
    public void handleProfile() {
        sceneNavigator.openModal(
                UiConstants.FXML_PROFILE_POPUP,
                UiConstants.WINDOW_TITLE_PROFILE,
                lblWelcome.getScene().getWindow()
        );
        refresh();
    }

    @FXML
    public void handleShowTranscript() {
        sceneNavigator.openModal(
                UiConstants.FXML_TRANSCRIPT_POPUP,
                UiConstants.WINDOW_TITLE_TRANSCRIPT,
                lblWelcome.getScene().getWindow()
        );
    }

    @FXML
    public void handleEnrollCourse() {
        sceneNavigator.openModal(
                UiConstants.FXML_ENROLL_COURSE_POPUP,
                UiConstants.WINDOW_TITLE_ENROLL_COURSE,
                lblWelcome.getScene().getWindow()
        );
        refresh();
    }
}