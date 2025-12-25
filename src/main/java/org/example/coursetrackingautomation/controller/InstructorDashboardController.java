package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.converter.DoubleStringConverter;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.entity.Course;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.service.AttendanceService;
import org.example.coursetrackingautomation.service.GradeService;
import org.example.coursetrackingautomation.service.InstructorWorkflowService;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.springframework.context.ApplicationContext;

@Controller
@RequiredArgsConstructor
public class InstructorDashboardController {

    @FXML
    private ComboBox<String> comboCourses;
    @FXML
    private TableView<GradeDTO> tableStudents;
    @FXML
    private TableColumn<GradeDTO, Long> colStudentNumber;
    @FXML
    private TableColumn<GradeDTO, String> colFullName;
    @FXML
    private TableColumn<GradeDTO, Double> colMidterm;
    @FXML
    private TableColumn<GradeDTO, Double> colFinal;
    @FXML
    private TableColumn<GradeDTO, Integer> colAttendance;
    @FXML
    private TableColumn<GradeDTO, Boolean> colPresent;
    @FXML
    private TableColumn<GradeDTO, Double> colAverage;
    @FXML
    private TableColumn<GradeDTO, String> colStatus;
    @FXML
    private Button btnSave;

    @FXML
    private Button btnLogOut;

    @FXML
    private Button btnProfile;

    private final UserSession userSession;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final InstructorWorkflowService instructorWorkflowService;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;
    private final SceneNavigator sceneNavigator;
    private final UiExceptionHandler uiExceptionHandler;
    private final AlertUtil alertUtil;
    private final ApplicationContext applicationContext;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupRowColorFactory();
        tableStudents.setEditable(true);

        try {
            var currentUser = userSession.getCurrentUser().orElseThrow(() -> new IllegalStateException("No active session"));
            var courses = courseRepository.findByInstructorIdAndActiveTrue(currentUser.id());
            comboCourses.setItems(FXCollections.observableArrayList(courses.stream().map(Course::getCode).toList()));
            comboCourses.setOnAction(e -> loadCourseStudents());
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
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
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleSave() {
        try {
            String courseCode = comboCourses.getValue();
            if (courseCode == null || courseCode.isBlank()) {
                alertUtil.showWarningAlert("Uyarı", "Lütfen önce bir ders seçiniz.");
                return;
            }
            instructorWorkflowService.saveCourseStudentUpdates(courseCode, tableStudents.getItems());
            tableStudents.refresh();
            alertUtil.showSuccessAlert("Başarılı", "Değişiklikler kaydedildi.");
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    private void loadCourseStudents() {
        String courseCode = comboCourses.getValue();
        if (courseCode == null || courseCode.isBlank()) {
            tableStudents.setItems(FXCollections.observableArrayList());
            return;
        }

        Course course = courseRepository.findByCode(courseCode)
            .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı"));

        String courseName = course.getName();
        Integer credit = course.getCredit();

        ObservableList<GradeDTO> rows = FXCollections.observableArrayList();
        for (Enrollment enrollment : enrollmentRepository.findByCourseId(course.getId())) {
            Double midterm = enrollment.getGrade() == null || enrollment.getGrade().getMidtermScore() == null
                ? null : enrollment.getGrade().getMidtermScore().doubleValue();
            Double finalScore = enrollment.getGrade() == null || enrollment.getGrade().getFinalScore() == null
                ? null : enrollment.getGrade().getFinalScore().doubleValue();

            boolean graded = midterm != null && finalScore != null;
            Double average = gradeService.calculateAverage(midterm, finalScore);
            String letter = graded ? gradeService.determineLetterGrade(average) : null;
            boolean passed = graded && gradeService.isPassed(letter);
            boolean critical = attendanceService.isAttendanceCritical(enrollment.getAbsenteeismCount() == null ? 0 : enrollment.getAbsenteeismCount());

            String status;
            if (!graded) {
                status = "Notlar girilmedi";
            } else {
                status = passed ? "Geçti" : "Kaldı";
            }

            rows.add(new GradeDTO(
                enrollment.getStudent().getId(),
                enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName(),
                courseCode,
                courseName,
                credit,
                midterm,
                finalScore,
                average,
                letter,
                status,
                enrollment.getAbsenteeismCount(),
                critical,
                null
            ));
        }

        tableStudents.setItems(rows);
    }

    private void setupTableColumns() {
        colStudentNumber.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colAverage.setCellValueFactory(new PropertyValueFactory<>("averageScore"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAttendance.setCellValueFactory(new PropertyValueFactory<>("attendanceCount"));

        colAttendance.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        colAttendance.setOnEditCommit(event -> {
            GradeDTO row = event.getRowValue();
            row.setAttendanceCount(event.getNewValue());
            boolean critical = attendanceService.isAttendanceCritical(event.getNewValue() == null ? 0 : event.getNewValue());
            row.setAbsentCritically(critical);
        });

        colPresent.setCellValueFactory(cellData -> {
            GradeDTO row = cellData.getValue();
            boolean initial = row.getPresent() == null ? true : row.getPresent();
            BooleanProperty property = new SimpleBooleanProperty(initial);
            property.addListener((obs, oldValue, newValue) -> row.setPresent(newValue));
            return property;
        });
        colPresent.setCellFactory(CheckBoxTableCell.forTableColumn(colPresent));

        colMidterm.setCellValueFactory(new PropertyValueFactory<>("midtermScore"));
        colMidterm.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colMidterm.setOnEditCommit(event -> event.getRowValue().setMidtermScore(event.getNewValue()));
        
        colFinal.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        colFinal.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colFinal.setOnEditCommit(event -> event.getRowValue().setFinalScore(event.getNewValue()));
    }

    private void setupRowColorFactory() {
        tableStudents.setRowFactory(tv -> new TableRow<GradeDTO>() {
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

}