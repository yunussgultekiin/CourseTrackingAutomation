package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.service.EnrollmentService;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class EnrollCoursePopupController {

    @FXML
    private TableView<CourseDTO> tableCourses;

    @FXML
    private TableColumn<CourseDTO, String> colCode;

    @FXML
    private TableColumn<CourseDTO, String> colName;

    @FXML
    private TableColumn<CourseDTO, Integer> colCredit;

    @FXML
    private TableColumn<CourseDTO, Long> colAvailable;

    @FXML
    private Label lblHint;

    @FXML
    private Button btnEnroll;

    private final UserSession userSession;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final UiExceptionHandler uiExceptionHandler;

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("availableQuota"));

        refreshCourses();
    }

    @FXML
    public void handleEnroll() {
        try {
            CourseDTO selected = tableCourses.getSelectionModel().getSelectedItem();
            if (selected == null || selected.getId() == null) {
                throw new IllegalArgumentException("Please select a course");
            }

            Long studentId = userSession.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No active session"))
                .id();

            enrollmentService.enrollStudent(studentId, selected.getId());
            close();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleClose() {
        close();
    }

    private void refreshCourses() {
        tableCourses.setItems(FXCollections.observableArrayList(courseService.getAllActiveCourseDTOs()));
    }

    private void close() {
        Stage stage = (Stage) tableCourses.getScene().getWindow();
        stage.close();
    }
}
