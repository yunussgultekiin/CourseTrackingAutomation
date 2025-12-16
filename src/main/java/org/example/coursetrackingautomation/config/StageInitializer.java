package org.example.coursetrackingautomation.config;

import org.springframework.stereotype.Component;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ApplicationEvent;

@Component
public class StageInitializer {
    public static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) { super(stage); }
    }
}