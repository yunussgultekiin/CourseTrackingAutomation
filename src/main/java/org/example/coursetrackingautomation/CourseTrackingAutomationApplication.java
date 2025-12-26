package org.example.coursetrackingautomation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
@Slf4j
public class CourseTrackingAutomationApplication extends Application {

    private static ConfigurableApplicationContext springContext;
    private static final String LOGIN_FXML_PATH = "/fxml/login.fxml";
    private static final String APPLICATION_TITLE = "Ders Takip Otomasyonu";
    private static final double WINDOW_WIDTH = 800.0;
    private static final double WINDOW_HEIGHT = 600.0;

    @Override
    public void init() {
        springContext = SpringApplication.run(CourseTrackingAutomationApplication.class);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
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
        }
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}