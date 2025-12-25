package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.CreateUserRequest;
import org.example.coursetrackingautomation.entity.Role;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AddUserFormController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<Role> roleComboBox;

    @FXML
    private TextField phoneField;

    @FXML
    private DatePicker birthDatePicker;

    private final UserService userService;
    private final UiExceptionHandler uiExceptionHandler;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
    }

    @FXML
    public void handleSave() {
        try {
            CreateUserRequest request = new CreateUserRequest(
                safeTrim(usernameField.getText()),
                safeString(passwordField.getText()),
                safeTrim(firstNameField.getText()),
                safeTrim(lastNameField.getText()),
                roleComboBox.getValue(),
                null,
                safeTrim(emailField.getText()),
                safeTrim(phoneField.getText()),
                true
            );

            userService.createUser(request);
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
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }
}
