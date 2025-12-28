package org.example.coursetrackingautomation.controller;

import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.service.AuthService;
import org.example.coursetrackingautomation.ui.FxAsync;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Component
@RequiredArgsConstructor
/**
 * JavaFX controller for the login view.
 *
 * <p>Delegates authentication to {@link AuthService} and navigates to the appropriate dashboard
 * based on the authenticated user's role.</p>
 */
public class LoginController {

    private final AuthService authService;
    private final SceneNavigator sceneNavigator;
    private final UiExceptionHandler uiExceptionHandler;

    @FXML private Button loginButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private volatile boolean loginInProgress;

    @FXML
    /**
     * Handles the login action triggered from the UI.
     *
     * <p>On success, the current stage is replaced with the role-specific dashboard scene.
     * Failures are delegated to {@link UiExceptionHandler}.</p>
     */
    public void handleLogin() {
        if (loginInProgress) {
            return;
        }

        loginInProgress = true;

        if (loginButton != null) {
            loginButton.setDisable(true);
        }

        errorLabel.setVisible(false);
        errorLabel.setText("");

        String username = usernameField.getText();
        String password = passwordField.getText();

        FxAsync.runAsync(
            () -> authService.login(username, password),
            user -> {
                try {
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    try {
                        switch (user.role()) {
                            case ADMIN -> sceneNavigator.showAdminDashboard(stage);
                            case INSTRUCTOR -> sceneNavigator.showInstructorDashboard(stage);
                            case STUDENT -> sceneNavigator.showStudentDashboard(stage);
                        }
                    } catch (Exception navigationError) {
                        // If a dashboard FXML/controller load fails, make it visible to the user.
                        uiExceptionHandler.handle(navigationError);
                        errorLabel.setText("Panel açılamadı. Lütfen tekrar deneyin.");
                        errorLabel.setVisible(true);
                    }
                } finally {
                    loginInProgress = false;
                    if (loginButton != null) {
                        loginButton.setDisable(false);
                    }
                }
            },
            failure -> {
                loginInProgress = false;
                if (loginButton != null) {
                    loginButton.setDisable(false);
                }

                // Fallback inline message in case dialogs are missed.
                errorLabel.setText("Giriş yapılamadı. Bilgilerinizi kontrol edin.");
                errorLabel.setVisible(true);
                uiExceptionHandler.handle(failure);
            }
        );
    }
}