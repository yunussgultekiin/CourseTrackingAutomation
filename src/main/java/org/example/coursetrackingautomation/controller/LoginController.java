package org.example.coursetrackingautomation.controller;

import org.example.coursetrackingautomation.service.AuthService;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

@Component
public class LoginController {

    private final AuthService authService;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    private void handleLogin() {
        clearError();

        String username = usernameField != null ? usernameField.getText() : "";
        String password = passwordField != null ? passwordField.getText() : "";

        if (isBlank(username) || isBlank(password)) {
            showError("Kullanıcı adı ve şifre gerekli.");
            return;
        }

        boolean authenticated = authService.authenticate(username, password);
        if (!authenticated) {
            showError("Geçersiz kimlik bilgileri.");
        }
        // TODO: Başarılı giriş sonrası doğru dashboard'a yönlendir.
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}