package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.entity.Enrollment;
import org.example.coursetrackingautomation.service.EnrollmentService;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class EditEnrollmentFormController {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_ENROLLED = "ENROLLED";
    private static final String STATUS_REGISTERED = "REGISTERED";
    private static final String STATUS_DROPPED = "DROPPED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    @FXML
    private Label lblEnrollmentId;

    @FXML
    private Label lblStudent;

    @FXML
    private Label lblCourse;

    @FXML
    private ComboBox<String> comboStatus;

    private final EnrollmentService enrollmentService;
    private final UiExceptionHandler uiExceptionHandler;

    private Long enrollmentId;

    @FXML
    public void initialize() {
        comboStatus.setItems(FXCollections.observableArrayList(
            STATUS_ACTIVE,
            STATUS_ENROLLED,
            STATUS_REGISTERED,
            STATUS_DROPPED,
            STATUS_CANCELLED
        ));
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
        refresh();
    }

    private void refresh() {
        try {
            if (enrollmentId == null) {
                return;
            }

            Enrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId);
            lblEnrollmentId.setText(String.valueOf(enrollment.getId()));

            String studentName = enrollment.getStudent() == null
                ? "-"
                : (enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName());
            String courseDisplay = enrollment.getCourse() == null
                ? "-"
                : (enrollment.getCourse().getCode() + " - " + enrollment.getCourse().getName());

            lblStudent.setText(studentName);
            lblCourse.setText(courseDisplay);

            String status = enrollment.getStatus() == null ? null : enrollment.getStatus().trim().toUpperCase();
            if (status != null && !status.isBlank()) {
                comboStatus.setValue(status);
            }
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleSave() {
        try {
            if (enrollmentId == null) {
                throw new IllegalArgumentException("Kayıt id boş olamaz");
            }

            String status = comboStatus.getValue();
            enrollmentService.updateEnrollmentStatus(enrollmentId, status);
            close();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleClose() {
        close();
    }

    private void close() {
        Stage stage = (Stage) lblEnrollmentId.getScene().getWindow();
        stage.close();
    }
}
