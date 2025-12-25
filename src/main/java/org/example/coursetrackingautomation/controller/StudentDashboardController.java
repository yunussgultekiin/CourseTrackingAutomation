package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.service.AttendanceService;
import org.example.coursetrackingautomation.service.TranscriptService;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StudentDashboardController {

    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblGpa;

    @FXML
    private Button btnLogOut;

    @FXML
    private Button btnProfile;

    @FXML
    private Button btnTranscript;

    @FXML
    private Button btnEnrollCourse;

    @FXML
    private TableView<GradeDTO> tableStudentCourses;
    @FXML
    private TableColumn<GradeDTO, String> colCourseCode;
    @FXML
    private TableColumn<GradeDTO, String> colCourseName;
    @FXML
    private TableColumn<GradeDTO, Integer> colCredit;
    @FXML
    private TableColumn<GradeDTO, Double> colMidterm;
    @FXML
    private TableColumn<GradeDTO, Double> colFinal;
    @FXML
    private TableColumn<GradeDTO, Double> colAverage;
    @FXML
    private TableColumn<GradeDTO, String> colLetter;
    @FXML
    private TableColumn<GradeDTO, Integer> colAttendance;
    @FXML
    private TableColumn<GradeDTO, String> colStatus;

    private final UserSession userSession;
    private final TranscriptService transcriptService;
    private final AttendanceService attendanceService;
    private final SceneNavigator sceneNavigator;
    private final UiExceptionHandler uiExceptionHandler;
    private final ApplicationContext applicationContext;
    @Autowired
    private GradeService gradeService;

    @FXML
    public void initialize() {
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseCode")); 
        
        colMidterm.setCellValueFactory(new PropertyValueFactory<>("midtermScore"));
        colFinal.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        colAverage.setCellValueFactory(new PropertyValueFactory<>("averageScore"));
        colLetter.setCellValueFactory(new PropertyValueFactory<>("letterGrade"));
        colAttendance.setCellValueFactory(new PropertyValueFactory<>("attendanceCount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

<<<<<<< HEAD
        setupRowColorFactory();
        refresh();
    }

    private void setupRowColorFactory() {
        tableStudentCourses.setRowFactory(tv -> new TableRow<GradeDTO>() {
            @Override
            protected void updateItem(GradeDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                    getStyleClass().removeAll("critical-attendance", "warning-attendance");
                } else {
                    int absentHours = item.getAttendanceCount() == null ? 0 : item.getAttendanceCount();
                    boolean critical = attendanceService.isAttendanceCritical(absentHours);
                    boolean warning = !critical && attendanceService.isAttendanceWarning(absentHours);

                    getStyleClass().removeAll("critical-attendance", "warning-attendance");
                    if (critical) {
                        getStyleClass().add("critical-attendance");
                    } else if (warning) {
                        getStyleClass().add("warning-attendance");
                    }
                }
            }
        });
    }

    private void refresh() {
        try {
            var currentUser = userSession.getCurrentUser().orElseThrow(() -> new IllegalStateException("No active session"));
            lblWelcome.setText("Hoş geldin, " + currentUser.fullName());

            ObservableList<GradeDTO> transcript = FXCollections.observableArrayList(
                transcriptService.getTranscriptGradesForStudent(currentUser.id())
            );
            tableStudentCourses.setItems(transcript);
            lblGpa.setText(transcriptService.calculateGpaText(transcript));
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            sceneNavigator.showLogin(stage);
        }
    }

    @FXML
    public void handleLogout() {
        Stage stage = (Stage) btnLogOut.getScene().getWindow();
        sceneNavigator.performLogout(stage);
    }

    @FXML
    public void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile_popup.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Profilim");
            dialog.initOwner(btnProfile.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.sizeToScene();
            dialog.centerOnScreen();
            dialog.showAndWait();

            refresh();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleShowTranscript() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(UiConstants.FXML_TRANSCRIPT_POPUP));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(UiConstants.WINDOW_TITLE_TRANSCRIPT);
            stage.setScene(new Scene(root));
            stage.initOwner(btnTranscript.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleEnrollCourse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(UiConstants.FXML_ENROLL_COURSE_POPUP));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle(UiConstants.WINDOW_TITLE_ENROLL_COURSE);
            dialog.initOwner(btnEnrollCourse.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.sizeToScene();
            dialog.centerOnScreen();
            dialog.showAndWait();

            refresh();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
        loadDummyData();
    }

    private void loadDummyData() {
        ObservableList<GradeDTO> list = FXCollections.observableArrayList();
        
        Double avg = gradeService.calculateAverage(50.0, 60.0);
        String letter = gradeService.determineLetterGrade(avg);
        String status = gradeService.isPassed(letter) ? "GEÇTİ" : "KALDI";

        list.add(new GradeDTO(1L, "Zeynep", "CSE101", 50.0, 60.0, avg, letter, status, 2, false));
        list.add(new GradeDTO(2L, "Zeynep", "MATH101", 80.0, 90.0, 86.0, "BA", "GECTI", 0, false));
        
        tableStudentCourses.setItems(list);
        
        lblGpa.setText(String.format("%.2f", avg));
    }
}