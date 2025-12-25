package org.example.coursetrackingautomation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.CreateCourseRequest;
import org.example.coursetrackingautomation.entity.Role;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AddCourseFormController {

    @FXML
    private TextField courseCodeField;

    @FXML
    private TextField courseNameField;

    @FXML
    private ComboBox<User> instructorCombo;

    @FXML
    private TextArea descriptionField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField creditsField;

    @FXML
    private TextField capacityField;

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
            Integer credit = parsePositiveInt(creditsField.getText(), "Kredi");
            Integer quota = parsePositiveInt(capacityField.getText(), "Kapasite");

            String term = startDatePicker.getValue() == null
                ? ""
                : String.valueOf(startDatePicker.getValue().getYear());

            if (instructorCombo.getValue() == null) {
                throw new IllegalArgumentException("Akademisyen seçimi zorunludur");
            }
            Long instructorId = instructorCombo.getValue().getId();

            CreateCourseRequest request = new CreateCourseRequest(
                safeTrim(courseCodeField.getText()),
                safeTrim(courseNameField.getText()),
                credit,
                quota,
                term,
                instructorId
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
            throw new IllegalArgumentException(fieldName + " boş bırakılamaz");
        }
        int value;
        try {
            value = Integer.parseInt(safe);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " sayısal olmalıdır");
        }
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " 0'dan büyük olmalıdır");
        }
        return value;
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
