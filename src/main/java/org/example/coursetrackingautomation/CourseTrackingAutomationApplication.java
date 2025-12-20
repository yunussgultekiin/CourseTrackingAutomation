package org.example.coursetrackingautomation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application class that integrates with Spring Boot.
 * This class handles the JavaFX lifecycle and Spring context initialization.
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.example.coursetrackingautomation")
public class CourseTrackingAutomationApplication extends Application {

    private static ConfigurableApplicationContext springContext;
    private static final String LOGIN_FXML_PATH = "/fxml/login.fxml"; // Hangi fxml isteniyorsa o yazılacak.
    private static final String APPLICATION_TITLE = "Course Tracking Automation";
    private static final double WINDOW_WIDTH = 800.0;
    private static final double WINDOW_HEIGHT = 600.0;

    /**
     * Initializes Spring context before JavaFX starts.
     */
    @Override
    public void init() {
        springContext = SpringApplication.run(SpringEntry.class);
    }

    /**
     * Starts the JavaFX application and loads the login screen.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LOGIN_FXML_PATH));
            fxmlLoader.setControllerFactory(springContext::getBean);
            
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            primaryStage.setTitle(APPLICATION_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Closes Spring context when JavaFX application stops.
     */
    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }

    /**
     * Main entry point for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}