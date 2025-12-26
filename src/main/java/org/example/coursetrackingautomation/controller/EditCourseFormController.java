package org.example.coursetrackingautomation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.UpdateCourseRequest;
import org.example.coursetrackingautomation.entity.Course;
import org.example.coursetrackingautomation.entity.Role;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;

import javafx.collections.FXCollections;
import javafx.util.StringConverter;

@Controller
@RequiredArgsConstructor
public class EditCourseFormController {

    @FXML private Label lblCourseCode;
    @FXML private TextField txtName;
    @FXML private TextField txtCredit;
    @FXML private TextField txtQuota;
    @FXML private TextField txtTerm;
    @FXML private TextField txtWeeklyTotalHours;
    @FXML private TextField txtWeeklyTheoryHours;
    @FXML private TextField txtWeeklyPracticeHours;
    @FXML private ComboBox<User> cmbInstructor;
    @FXML private RadioButton rbActive;
    @FXML private RadioButton rbPassive;

    private final ToggleGroup statusGroup = new ToggleGroup();

    private final CourseService courseService;
    private final UserService userService;
    private final UiExceptionHandler uiExceptionHandler;
    private final AlertUtil alertUtil;
    private Long courseId;
    private boolean originalActive;

    @FXML
    public void initialize() {
        try {
            rbActive.setToggleGroup(statusGroup);
            rbPassive.setToggleGroup(statusGroup);

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
            txtWeeklyTotalHours.setText(course.getWeeklyTotalHours() == null ? "" : String.valueOf(course.getWeeklyTotalHours()));
            txtWeeklyTheoryHours.setText(course.getWeeklyTheoryHours() == null ? "" : String.valueOf(course.getWeeklyTheoryHours()));
            txtWeeklyPracticeHours.setText(course.getWeeklyPracticeHours() == null ? "" : String.valueOf(course.getWeeklyPracticeHours()));

            originalActive = course.isActive();
            if (course.isActive()) {
                rbActive.setSelected(true);
            } else {
                rbPassive.setSelected(true);
            }

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
                throw new IllegalArgumentException("Course id must not be blank");
            }

            String name = requireNotBlank(safeTrim(txtName.getText()), "Name");
            String term = requireNotBlank(safeTrim(txtTerm.getText()), "Term");

            Integer credit = parseNullablePositiveInt(txtCredit.getText(), "Credit");
            Integer quota = parseNullablePositiveInt(txtQuota.getText(), "Quota");

            Integer weeklyTotalHours = parsePositiveInt(txtWeeklyTotalHours.getText(), "Haftalık toplam saat");
            Integer weeklyTheoryHours = parseNonNegativeInt(txtWeeklyTheoryHours.getText(), "Haftalık teori saati");
            Integer weeklyPracticeHours = parseNonNegativeInt(txtWeeklyPracticeHours.getText(), "Haftalık uygulama saati");
            if ((weeklyTheoryHours + weeklyPracticeHours) != weeklyTotalHours) {
                throw new IllegalArgumentException("Haftalık toplam saat, teori + uygulama toplamına eşit olmalıdır");
            }

            Long instructorId = cmbInstructor.getValue() == null ? null : cmbInstructor.getValue().getId();

            boolean newActive = rbActive.isSelected();
            if (originalActive && !newActive) {
                boolean confirmed = alertUtil.showConfirmationAlert(
                    "Onay",
                    "Bu dersi Pasif yapmak üzeresiniz.\n\n" +
                        "Bu işlemden sonra:\n" +
                        "- Ders artık yeni öğrenci kabul etmeyecek.\n" +
                        "- Öğrenciler bu dersi seçemeyecek.\n\n" +
                        "Devam etmek istiyor musunuz?"
                );
                if (!confirmed) {
                    // keep UI consistent
                    rbActive.setSelected(true);
                    return;
                }
            }

            UpdateCourseRequest request = new UpdateCourseRequest(
                name,
                credit,
                quota,
                term,
                newActive,
                instructorId,
                weeklyTotalHours,
                weeklyTheoryHours,
                weeklyPracticeHours
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
                throw new IllegalArgumentException(label + " must be greater than 0");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a number");
        }
    }

    private static Integer parsePositiveInt(String raw, String label) {
        if (raw == null || raw.trim().isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(label + " must be greater than 0");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a number");
        }
    }

    private static Integer parseNonNegativeInt(String raw, String label) {
        if (raw == null || raw.trim().isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            if (parsed < 0) {
                throw new IllegalArgumentException(label + " 0 veya daha büyük olmalıdır");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a number");
        }
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
