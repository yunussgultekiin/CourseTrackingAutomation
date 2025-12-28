package org.example.coursetrackingautomation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

@SpringBootApplication
@Slf4j
/**
 * JavaFX + Spring Boot entry point for the Course Tracking Automation desktop application.
 *
 * <p>This class boots the Spring application context during {@link #init()}, then loads the initial
 * JavaFX scene (login) in {@link #start(Stage)} using Spring-managed controllers.</p>
 */
public class CourseTrackingAutomationApplication extends Application {

    private static ConfigurableApplicationContext springContext;
    private static volatile Throwable initFailure;
    private static final String LOGIN_FXML_PATH = "/fxml/login.fxml";
    private static final String APPLICATION_TITLE = "Ders Takip Otomasyonu";
    private static final double WINDOW_WIDTH = 800.0;
    private static final double WINDOW_HEIGHT = 600.0;

    @Override
    /**
     * Initializes the application and starts the Spring Boot context.
     */
    public void init() {
        try {
            springContext = SpringApplication.run(CourseTrackingAutomationApplication.class);
        } catch (Exception e) {
            log.error("Fatal error while starting Spring context", e);
            initFailure = e;
        }
    }

    @Override
    /**
     * Starts the JavaFX application and renders the initial login view.
     *
     * @param primaryStage the primary JavaFX stage
     */
    public void start(Stage primaryStage) {
        try {
            if (initFailure != null) {
                showFatalErrorAndExit(initFailure);
                return;
            }
            if (springContext == null) {
                showFatalErrorAndExit(new IllegalStateException("Spring context başlatılamadı"));
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LOGIN_FXML_PATH));
            fxmlLoader.setControllerFactory(clazz -> springContext.getAutowireCapableBeanFactory().createBean(clazz));
            
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            primaryStage.setTitle(APPLICATION_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            log.error("Error loading login screen", e);
            showFatalErrorAndExit(e);
        }
    }

    private void showFatalErrorAndExit(Throwable throwable) {
        try {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Uygulama başlatılamadı");
            alert.setContentText("Giriş ekranı yüklenemedi veya uygulama başlatılamadı.\n\n" +
                "Lütfen uygulamayı yeniden başlatın. Sorun devam ederse sistem yöneticinize başvurun.");

            alert.setResizable(true);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setMinHeight(Region.USE_PREF_SIZE);
            dialogPane.setPrefWidth(520);

            Label contentLabel = (Label) dialogPane.lookup(".content.label");
            if (contentLabel != null) {
                contentLabel.setWrapText(true);
                contentLabel.setMinHeight(Region.USE_PREF_SIZE);
            }
            Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
            if (headerLabel != null) {
                headerLabel.setWrapText(true);
                headerLabel.setMinHeight(Region.USE_PREF_SIZE);
            }

            alert.showAndWait();
        } catch (Exception ignored) {
            // If JavaFX alert rendering fails, still exit.
        } finally {
            try {
                if (springContext != null) {
                    springContext.close();
                }
            } catch (Exception ignored) {
            }

            Platform.exit();
            System.exit(1);
        }
    }

    @Override
    /**
     * Shuts down the Spring application context.
     */
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }

    /**
     * Standard Java entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}