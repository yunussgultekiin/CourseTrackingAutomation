package org.example.coursetrackingautomation.config;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import javafx.stage.Stage;

@Component
public class StageInitializer {

    public static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }
    }
}