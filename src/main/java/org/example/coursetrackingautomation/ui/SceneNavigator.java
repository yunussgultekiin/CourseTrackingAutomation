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
/**
 * Centralizes scene transitions and modal dialog navigation for the JavaFX UI.
 *
 * <p>This component loads FXML views using Spring's {@link ApplicationContext} so that controllers
 * can be dependency-injected. Exceptions encountered during navigation are delegated to
 * {@link UiExceptionHandler}.</p>
 */
public class SceneNavigator {

    private static final double ADMIN_WINDOW_WIDTH = 1200.0;
    private static final double ADMIN_WINDOW_HEIGHT = 700.0;
    private static final double DASHBOARD_WINDOW_WIDTH = 900.0;
    private static final double DASHBOARD_WINDOW_HEIGHT = 600.0;

    private final ApplicationContext applicationContext;
    private final AuthService authService;
    private final UiExceptionHandler uiExceptionHandler;

    /**
     * Displays the login view on the given stage.
     *
     * @param stage the primary stage to update
     */
    public void showLogin(Stage stage) {
        setScene(stage, UiConstants.FXML_LOGIN, UiConstants.DEFAULT_WINDOW_WIDTH, UiConstants.DEFAULT_WINDOW_HEIGHT);
    }

    /**
     * Displays the admin dashboard view and enables resizing/maximization.
     *
     * @param stage the primary stage to update
     */
    public void showAdminDashboard(Stage stage) {
        setScene(stage, UiConstants.FXML_ADMIN_DASHBOARD, ADMIN_WINDOW_WIDTH, ADMIN_WINDOW_HEIGHT);
        stage.setResizable(true);
        stage.setMaximized(true);
    }

    /**
     * Displays the student dashboard view and enables resizing/maximization.
     *
     * @param stage the primary stage to update
     */
    public void showStudentDashboard(Stage stage) {
        setScene(stage, UiConstants.FXML_STUDENT_DASHBOARD, DASHBOARD_WINDOW_WIDTH, DASHBOARD_WINDOW_HEIGHT);
        stage.setResizable(true);
        stage.setMaximized(true);
    }

    /**
     * Displays the instructor dashboard view and enables resizing/maximization.
     *
     * @param stage the primary stage to update
     */
    public void showInstructorDashboard(Stage stage) {
        setScene(stage, UiConstants.FXML_INSTRUCTOR_DASHBOARD, DASHBOARD_WINDOW_WIDTH, DASHBOARD_WINDOW_HEIGHT);
        stage.setResizable(true);
        stage.setMaximized(true);
    }

    /**
     * Logs out the current user and returns to the login view.
     *
     * @param stage the primary stage to update
     */
    public void logoutToLogin(Stage stage) {
        performLogout(stage);
    }

    /**
     * Performs logout and navigates to login.
     *
     * @param stage the primary stage to update
     */
    public void performLogout(Stage stage) {
        authService.logout();
        showLogin(stage);
    }

    /**
     * Opens an FXML view as a modal dialog.
     *
     * @param fxmlPath the classpath-relative FXML resource path
     * @param title the window title
     * @param ownerWindow the owner window; may be {@code null}
     */
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

    /**
     * Opens an FXML view as a modal dialog and allows the caller to configure the controller before
     * showing the dialog.
     *
     * @param fxmlPath the classpath-relative FXML resource path
     * @param title the window title
     * @param ownerWindow the owner window; may be {@code null}
     * @param controllerConfigurator a callback invoked with the resolved controller; may be {@code null}
     * @param <T> expected controller type
     */
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