package org.example.coursetrackingautomation.controller;

import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.SessionUser;
import org.example.coursetrackingautomation.service.AuthService;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Component
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;
    private final SceneNavigator sceneNavigator;
    private final UiExceptionHandler uiExceptionHandler;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void handleLogin() {
        try {
            errorLabel.setVisible(false);
            errorLabel.setText("");

            SessionUser user = authService.login(usernameField.getText(), passwordField.getText());
            Stage stage = (Stage) usernameField.getScene().getWindow();

            switch (user.role()) {
                case ADMIN -> sceneNavigator.showAdminDashboard(stage);
                case INSTRUCTOR -> sceneNavigator.showInstructorDashboard(stage);
                case STUDENT -> sceneNavigator.showStudentDashboard(stage);
            }
        } catch (Exception e) {
            errorLabel.setText("Login failed");
            errorLabel.setVisible(true);
            uiExceptionHandler.handle(e);
        }
    }
}