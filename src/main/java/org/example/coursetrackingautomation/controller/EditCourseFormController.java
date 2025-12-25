package org.example.coursetrackingautomation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.UpdateCourseRequest;
import org.example.coursetrackingautomation.entity.Course;
import org.example.coursetrackingautomation.entity.Role;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

import javafx.collections.FXCollections;
import javafx.util.StringConverter;

@Controller
@RequiredArgsConstructor
public class EditCourseFormController {

    @FXML
    private Label lblCourseCode;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtCredit;

    @FXML
    private TextField txtQuota;

    @FXML
    private TextField txtTerm;

    @FXML
    private ComboBox<User> cmbInstructor;

    @FXML
    private CheckBox chkActive;

    private final CourseService courseService;
    private final UserService userService;
    private final UiExceptionHandler uiExceptionHandler;

    private Long courseId;

    @FXML
    public void initialize() {
        try {
            cmbInstructor.setConverter(new StringConverter<>() {
                @Override
                public String toString(User user) {
                    if (user == null) {
                        return "";
                    }
                    String fullName = (user.getFirstName() == null ? "" : user.getFirstName())
                        + " "
                        + (user.getLastName() == null ? "" : user.getLastName());
                    return fullName.trim() + " (" + user.getUsername() + ")";
                }

                @Override
                public User fromString(String string) {
                    return null;
                }
            });

            cmbInstructor.setItems(FXCollections.observableArrayList(userService.getActiveUsersByRole(Role.INSTRUCTOR)));
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
        refresh();
    }

    private void refresh() {
        try {
            if (courseId == null) {
                return;
            }
            Course course = courseService.getCourseById(courseId);
            lblCourseCode.setText(course.getCode() == null ? "-" : course.getCode());
            txtName.setText(course.getName());
            txtCredit.setText(course.getCredit() == null ? "" : String.valueOf(course.getCredit()));
            txtQuota.setText(course.getQuota() == null ? "" : String.valueOf(course.getQuota()));
            txtTerm.setText(course.getTerm());
            chkActive.setSelected(course.isActive());

            if (course.getInstructor() != null) {
                Long instructorId = course.getInstructor().getId();
                cmbInstructor.getItems().stream()
                    .filter(u -> u != null && instructorId != null && instructorId.equals(u.getId()))
                    .findFirst()
                    .ifPresent(cmbInstructor::setValue);
            }
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleSave() {
        try {
            if (courseId == null) {
                throw new IllegalArgumentException("Ders id boş olamaz");
            }

            Integer credit = parseNullablePositiveInt(txtCredit.getText(), "Kredi");
            Integer quota = parseNullablePositiveInt(txtQuota.getText(), "Kontenjan");

            Long instructorId = cmbInstructor.getValue() == null ? null : cmbInstructor.getValue().getId();

            UpdateCourseRequest request = new UpdateCourseRequest(
                safeTrim(txtName.getText()),
                credit,
                quota,
                safeTrim(txtTerm.getText()),
                chkActive.isSelected(),
                instructorId
            );

            courseService.updateCourse(courseId, request);
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
        Stage stage = (Stage) lblCourseCode.getScene().getWindow();
        stage.close();
    }

    private static String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private static Integer parseNullablePositiveInt(String raw, String label) {
        if (raw == null || raw.trim().isBlank()) {
            return null;
        }

        try {
            int parsed = Integer.parseInt(raw.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(label + " 0'dan büyük olmalıdır");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " geçerli bir tam sayı olmalıdır");
        }
    }
}
