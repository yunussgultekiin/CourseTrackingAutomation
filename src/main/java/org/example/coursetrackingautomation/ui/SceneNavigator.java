package org.example.coursetrackingautomation.ui;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.stage.Modality;
import javafx.stage.Window;
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
    private final UiExceptionHandler uiExceptionHandler;

    public void showLogin(Stage stage) {
        setScene(stage, UiConstants.FXML_LOGIN, UiConstants.DEFAULT_WINDOW_WIDTH, UiConstants.DEFAULT_WINDOW_HEIGHT);
    }

    public void showAdminDashboard(Stage stage) {
        setScene(stage, UiConstants.FXML_ADMIN_DASHBOARD, ADMIN_WINDOW_WIDTH, ADMIN_WINDOW_HEIGHT);
        stage.setResizable(true);
        stage.setMaximized(true);
    }

    public void showStudentDashboard(Stage stage) {
        setScene(stage, UiConstants.FXML_STUDENT_DASHBOARD, DASHBOARD_WINDOW_WIDTH, DASHBOARD_WINDOW_HEIGHT);
        stage.setResizable(true);
        stage.setMaximized(true);
    }

    public void showInstructorDashboard(Stage stage) {
        setScene(stage, UiConstants.FXML_INSTRUCTOR_DASHBOARD, DASHBOARD_WINDOW_WIDTH, DASHBOARD_WINDOW_HEIGHT);
        stage.setResizable(true);
        stage.setMaximized(true);
    }

    public void logoutToLogin(Stage stage) {
        performLogout(stage);
    }

    public void performLogout(Stage stage) {
        authService.logout();
        showLogin(stage);
    }

    public void openModal(String fxmlPath, String title, Window ownerWindow) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(clazz -> applicationContext.getAutowireCapableBeanFactory().createBean(clazz));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle(title);

            if (ownerWindow != null) {
                dialog.initOwner(ownerWindow);
            }

            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(true);
            dialog.sizeToScene();
            dialog.centerOnScreen();
            dialog.showAndWait();
        } catch (IOException e) {
            log.error("Failed to open modal: {}", fxmlPath, e);
            uiExceptionHandler.handle(e);
        }
    }

    public <T> void openModalWithController(String fxmlPath, String title, Window ownerWindow, Consumer<T> controllerConfigurator) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(clazz -> applicationContext.getAutowireCapableBeanFactory().createBean(clazz));
            Parent root = loader.load();

            T controller = loader.getController();
            if (controllerConfigurator != null) {
                controllerConfigurator.accept(controller);
            }

            Stage dialog = new Stage();
            dialog.setTitle(title);

            if (ownerWindow != null) {
                dialog.initOwner(ownerWindow);
            }

            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(true);
            dialog.sizeToScene();
            dialog.centerOnScreen();
            dialog.showAndWait();
        } catch (IOException e) {
            log.error("Failed to open modal: {}", fxmlPath, e);
            uiExceptionHandler.handle(e);
        }
    }

    private void setScene(Stage stage, String fxmlPath, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(clazz -> applicationContext.getAutowireCapableBeanFactory().createBean(clazz));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            log.error("Failed to load FXML: {}", fxmlPath, e);
            throw new IllegalStateException("Failed to load UI", e);
        }
    }
}