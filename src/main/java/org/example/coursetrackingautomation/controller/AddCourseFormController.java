package org.example.coursetrackingautomation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.CreateCourseRequest;
import org.example.coursetrackingautomation.entity.Role;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AddCourseFormController {

    @FXML private TextField courseCodeField;
    @FXML private TextField courseNameField;
    @FXML private TextField termField;
    @FXML private ComboBox<User> instructorCombo;
    @FXML private TextField creditsField;
    @FXML private TextField capacityField;
    @FXML private TextField weeklyTotalHoursField;
    @FXML private TextField weeklyTheoryHoursField;
    @FXML private TextField weeklyPracticeHoursField;

    private final CourseService courseService;
    private final UserService userService;
    private final UiExceptionHandler uiExceptionHandler;

    @FXML
    public void initialize() {
        try {
            instructorCombo.setConverter(new StringConverter<>() {
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

            instructorCombo.setItems(FXCollections.observableArrayList(userService.getActiveUsersByRole(Role.INSTRUCTOR)));
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleSave() {
        try {
            String code = requireNotBlank(safeTrim(courseCodeField.getText()), "Code");
            String name = requireNotBlank(safeTrim(courseNameField.getText()), "Name");
            String term = requireNotBlank(safeTrim(termField.getText()), "Term");

            Integer credit = parsePositiveInt(creditsField.getText(), "Credit");
            Integer quota = parsePositiveInt(capacityField.getText(), "Capacity");

            Integer weeklyTotalHours = parsePositiveInt(weeklyTotalHoursField.getText(), "Haftalık toplam saat");
            Integer weeklyTheoryHours = parseNonNegativeInt(weeklyTheoryHoursField.getText(), "Haftalık teori saati");
            Integer weeklyPracticeHours = parseNonNegativeInt(weeklyPracticeHoursField.getText(), "Haftalık uygulama saati");
            if ((weeklyTheoryHours + weeklyPracticeHours) != weeklyTotalHours) {
                throw new IllegalArgumentException("Haftalık toplam saat, teori + uygulama toplamına eşit olmalıdır");
            }

            if (instructorCombo.getValue() == null) {
                throw new IllegalArgumentException(UiConstants.ERROR_KEY_INSTRUCTOR_SELECTION_REQUIRED);
            }
            Long instructorId = instructorCombo.getValue().getId();

            CreateCourseRequest request = new CreateCourseRequest(
                code,
                name,
                credit,
                quota,
                term,
                instructorId,
                weeklyTotalHours,
                weeklyTheoryHours,
                weeklyPracticeHours
            );

            courseService.createCourse(request);
            close();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) courseCodeField.getScene().getWindow();
        stage.close();
    }

    private static Integer parsePositiveInt(String raw, String fieldName) {
        String safe = raw == null ? "" : raw.trim();
        if (safe.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        int value;
        try {
            value = Integer.parseInt(safe);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a number");
        }
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private static Integer parseNonNegativeInt(String raw, String fieldName) {
        String safe = raw == null ? "" : raw.trim();
        if (safe.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        int value;
        try {
            value = Integer.parseInt(safe);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a number");
        }
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " 0 veya daha büyük olmalıdır");
        }
        return value;
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
