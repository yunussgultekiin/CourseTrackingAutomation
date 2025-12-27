package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.CreateUserRequest;
import org.example.coursetrackingautomation.dto.RoleDTO;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AddUserFormController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<RoleDTO> roleComboBox;
    @FXML private TextField phoneField;

    private final UserService userService;
    private final UiExceptionHandler uiExceptionHandler;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(RoleDTO.values()));

        roleComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(RoleDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : toTurkishRole(item));
            }
        });
        roleComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(RoleDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : toTurkishRole(item));
            }
        });
    }

    @FXML
    public void handleSave() {
        try {
            String username = requireNotBlank(safeTrim(usernameField.getText()), "Kullanıcı adı");
            String password = requireNotBlank(safe(passwordField.getText()), "Şifre");
            String firstName = requireNotBlank(safeTrim(firstNameField.getText()), "Ad");
            String lastName = requireNotBlank(safeTrim(lastNameField.getText()), "Soyad");
            String email = requireNotBlank(safeTrim(emailField.getText()), "E-posta");

            RoleDTO role = roleComboBox.getValue();
            if (role == null) {
                throw new IllegalArgumentException(UiConstants.ERROR_KEY_ROLE_SELECTION_REQUIRED);
            }

            CreateUserRequest request = new CreateUserRequest(
                username,
                password,
                firstName,
                lastName,
                role,
                null,
                email,
                safeTrimToNull(phoneField.getText()),
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeTrimToNull(String value) {
        String trimmed = safeTrim(value);
        return trimmed.isBlank() ? null : trimmed;
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " boş bırakılamaz");
        }
        return value;
    }

    private static String toTurkishRole(RoleDTO role) {
        if (role == null) {
            return "";
        }
        return switch (role) {
            case ADMIN -> "Yönetici";
            case INSTRUCTOR -> "Akademisyen";
            case STUDENT -> "Öğrenci";
        };
    }
}
