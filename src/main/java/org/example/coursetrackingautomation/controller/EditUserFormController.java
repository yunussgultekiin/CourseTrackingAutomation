package org.example.coursetrackingautomation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.UpdateUserRequest;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class EditUserFormController {

    @FXML
    private Label lblUsername;

    @FXML
    private TextField txtFirstName;

    @FXML
    private TextField txtLastName;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhone;

    @FXML
    private PasswordField txtPassword;

    private final UserService userService;
    private final UiExceptionHandler uiExceptionHandler;

    private Long userId;

    public void setUserId(Long userId) {
        this.userId = userId;
        refresh();
    }

    private void refresh() {
        try {
            if (userId == null) {
                return;
            }

            User user = userService.getUserById(userId);
            lblUsername.setText(user.getUsername() == null ? "-" : user.getUsername());
            txtFirstName.setText(user.getFirstName());
            txtLastName.setText(user.getLastName());
            txtEmail.setText(user.getEmail());
            txtPhone.setText(user.getPhone());
            txtPassword.clear();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleSave() {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("Kullanıcı id boş olamaz");
            }

            UpdateUserRequest request = new UpdateUserRequest(
                safeTrim(txtFirstName.getText()),
                safeTrim(txtLastName.getText()),
                safeTrimToNull(txtEmail.getText()),
                safeTrimToNull(txtPhone.getText()),
                txtPassword.getText() == null ? null : txtPassword.getText()
            );

            userService.updateUser(userId, request);
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
        Stage stage = (Stage) lblUsername.getScene().getWindow();
        stage.close();
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safeTrimToNull(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
