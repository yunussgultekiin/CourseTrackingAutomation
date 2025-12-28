package org.example.coursetrackingautomation;

/**
 * Thin Java launcher entry point.
 *
 * <p>This indirection is commonly used for packaging where the JavaFX {@link javafx.application.Application}
 * subclass should not be the direct main class.</p>
 */
public class CourseTrackingLauncher {
    /**
     * Delegates to {@link CourseTrackingAutomationApplication}.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        CourseTrackingAutomationApplication.main(args);
    }
}