package org.example.coursetrackingautomation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.dto.RoleDTO;
import org.example.coursetrackingautomation.dto.SelectOptionDTO;
import org.example.coursetrackingautomation.dto.UpdateCourseRequest;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;

import javafx.collections.FXCollections;

@Controller
@RequiredArgsConstructor
public class EditCourseFormController {

    @FXML private Label courseCodeLabel;
    @FXML private TextField nameField;
    @FXML private TextField creditField;
    @FXML private TextField quotaField;
    @FXML private TextField termField;
    @FXML private TextField weeklyTotalHoursField;
    @FXML private TextField weeklyTheoryHoursField;
    @FXML private TextField weeklyPracticeHoursField;
    @FXML private ComboBox<SelectOptionDTO> instructorComboBox;
    @FXML private RadioButton activeRadio;
    @FXML private RadioButton passiveRadio;

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
            activeRadio.setToggleGroup(statusGroup);
            passiveRadio.setToggleGroup(statusGroup);

            instructorComboBox.setItems(FXCollections.observableArrayList(userService.getActiveUserOptionsByRole(RoleDTO.INSTRUCTOR)));
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
            CourseDTO course = courseService.getCourseDTOById(courseId);
            courseCodeLabel.setText(course.getCode() == null ? "-" : course.getCode());
            nameField.setText(course.getName());
            creditField.setText(course.getCredit() == null ? "" : String.valueOf(course.getCredit()));
            quotaField.setText(course.getQuota() == null ? "" : String.valueOf(course.getQuota()));
            termField.setText(course.getTerm());
            weeklyTotalHoursField.setText(course.getWeeklyTotalHours() == null ? "" : String.valueOf(course.getWeeklyTotalHours()));
            weeklyTheoryHoursField.setText(course.getWeeklyTheoryHours() == null ? "" : String.valueOf(course.getWeeklyTheoryHours()));
            weeklyPracticeHoursField.setText(course.getWeeklyPracticeHours() == null ? "" : String.valueOf(course.getWeeklyPracticeHours()));

            originalActive = Boolean.TRUE.equals(course.getActive());
            if (Boolean.TRUE.equals(course.getActive())) {
                activeRadio.setSelected(true);
            } else {
                passiveRadio.setSelected(true);
            }

            if (course.getInstructorId() != null) {
                Long instructorId = course.getInstructorId();
                instructorComboBox.getItems().stream()
                    .filter(opt -> opt != null && instructorId.equals(opt.id()))
                    .findFirst()
                    .ifPresent(instructorComboBox::setValue);
            }
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleSave() {
        try {
            if (courseId == null) {
                throw new IllegalArgumentException("Ders ID boş olamaz");
            }

            String name = requireNotBlank(safeTrim(nameField.getText()), "Ders adı");
            String term = requireNotBlank(safeTrim(termField.getText()), "Dönem");

            Integer credit = parseNullablePositiveInt(creditField.getText(), "Kredi");
            Integer quota = parseNullablePositiveInt(quotaField.getText(), "Kota");

            Integer weeklyTotalHours = parsePositiveInt(weeklyTotalHoursField.getText(), "Haftalık toplam saat");
            Integer weeklyTheoryHours = parseNonNegativeInt(weeklyTheoryHoursField.getText(), "Haftalık teori saati");
            Integer weeklyPracticeHours = parseNonNegativeInt(weeklyPracticeHoursField.getText(), "Haftalık uygulama saati");
            if ((weeklyTheoryHours + weeklyPracticeHours) != weeklyTotalHours) {
                throw new IllegalArgumentException("Haftalık toplam saat, teori + uygulama toplamına eşit olmalıdır");
            }

            Long instructorId = instructorComboBox.getValue() == null ? null : instructorComboBox.getValue().id();

            boolean newActive = activeRadio.isSelected();
            if (originalActive && !newActive) {
                boolean confirmed = alertUtil.showConfirmationAlert(
                    "Onay",
                    "Bu dersi Pasif yapmak üzeresiniz. Pasif hale getirilen ders yeni öğrenci kabul etmez ve öğrenciler tarafından seçilemez. Devam etmek istiyor musunuz?"
                );
                if (!confirmed) {
                    activeRadio.setSelected(true);
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
        Stage stage = (Stage) courseCodeLabel.getScene().getWindow();
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
            throw new IllegalArgumentException(label + " sayı olmalıdır");
        }
    }

    private static Integer parsePositiveInt(String raw, String label) {
        if (raw == null || raw.trim().isBlank()) {
            throw new IllegalArgumentException(label + " boş bırakılamaz");
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(label + " 0'dan büyük olmalıdır");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " sayı olmalıdır");
        }
    }

    private static Integer parseNonNegativeInt(String raw, String label) {
        if (raw == null || raw.trim().isBlank()) {
            throw new IllegalArgumentException(label + " boş bırakılamaz");
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            if (parsed < 0) {
                throw new IllegalArgumentException(label + " 0 veya daha büyük olmalıdır");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " sayı olmalıdır");
        }
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " boş bırakılamaz");
        }
        return value;
    }
}
