package org.example.coursetrackingautomation.ui;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.service.AuthService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Slf4j
@Component
@RequiredArgsConstructor
public class SceneNavigator {

    private static final double ADMIN_WINDOW_WIDTH = 1200.0;
    private static final double ADMIN_WINDOW_HEIGHT = 700.0;
    private static final double DASHBOARD_WINDOW_WIDTH = 900.0;
    private static final double DASHBOARD_WINDOW_HEIGHT = 600.0;

    private final ApplicationContext applicationContext;
    private final AuthService authService;

    public void showLogin(Stage stage) {
        setScene(stage, UiConstants.FXML_LOGIN, UiConstants.DEFAULT_WINDOW_WIDTH, UiConstants.DEFAULT_WINDOW_HEIGHT);
    }

    public void showAdminDashboard(Stage stage) {
        setScene(stage, UiConstants.FXML_ADMIN_DASHBOARD, ADMIN_WINDOW_WIDTH, ADMIN_WINDOW_HEIGHT);
    }

    public void showStudentDashboard(Stage stage) {
        setScene(stage, UiConstants.FXML_STUDENT_DASHBOARD, DASHBOARD_WINDOW_WIDTH, DASHBOARD_WINDOW_HEIGHT);
    }

    public void showInstructorDashboard(Stage stage) {
        setScene(stage, UiConstants.FXML_INSTRUCTOR_DASHBOARD, DASHBOARD_WINDOW_WIDTH, DASHBOARD_WINDOW_HEIGHT);
    }

    public void logoutToLogin(Stage stage) {
        performLogout(stage);
    }

    public void performLogout(Stage stage) {
        authService.logout();
        showLogin(stage);
    }

    private void setScene(Stage stage, String fxmlPath, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            log.error("Failed to load FXML: {}", fxmlPath, e);
            throw new IllegalStateException("Failed to load UI", e);
        }
    }
}
