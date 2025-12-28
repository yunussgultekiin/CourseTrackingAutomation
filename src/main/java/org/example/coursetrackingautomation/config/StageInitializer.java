package org.example.coursetrackingautomation.config;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import javafx.stage.Stage;

@Component
/**
 * Publishes a Spring application event when the primary JavaFX {@link Stage} is ready.
 *
 * <p>This pattern enables Spring-managed listeners to perform initial scene setup after JavaFX
 * has created the primary stage.</p>
 */
public class StageInitializer {

    /**
     * Spring application event carrying the JavaFX {@link Stage} instance.
     */
    public static class StageReadyEvent extends ApplicationEvent {
        /**
         * Creates a new event for the provided stage.
         *
         * @param stage the primary JavaFX stage
         */
        public StageReadyEvent(Stage stage) {
            super(stage);
        }
    }
}