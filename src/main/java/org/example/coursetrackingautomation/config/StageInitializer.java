package org.example.coursetrackingautomation.config;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import javafx.stage.Stage;

/**
 * Stage initializer for JavaFX application.
 * Handles stage ready events for Spring-JavaFX integration.
 */
@Component
public class StageInitializer {
    
    /**
     * Event class for stage ready notification.
     */
    public static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }
    }
}