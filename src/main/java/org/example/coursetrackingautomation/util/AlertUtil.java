package org.example.coursetrackingautomation.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for displaying alert dialogs.
 * Follows DRY principle and provides standardized alert dialogs.
 */
@Component
public class AlertUtil {

    private static final Logger logger = LoggerFactory.getLogger(AlertUtil.class);

    private static final String ERROR_TITLE = "Error";
    private static final String SUCCESS_TITLE = "Success";
    private static final String WARNING_TITLE = "Warning";
    private static final String INFORMATION_TITLE = "Information";
    private static final String CONFIRMATION_TITLE = "Confirmation";

    /**
     * Shows an error alert dialog.
     *
     * @param message The error message to display
     */
    public void showErrorAlert(String message) {
        showErrorAlert(ERROR_TITLE, message);
    }

    /**
     * Shows an error alert dialog with custom title.
     *
     * @param title   The title of the alert
     * @param message The error message to display
     */
    public void showErrorAlert(String title, String message) {
        logger.error("Error Alert: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(ERROR_TITLE);
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Shows a success alert dialog.
     *
     * @param message The success message to display
     */
    public void showSuccessAlert(String message) {
        showSuccessAlert(SUCCESS_TITLE, message);
    }

    /**
     * Shows a success alert dialog with custom title.
     *
     * @param title   The title of the alert
     * @param message The success message to display
     */
    public void showSuccessAlert(String title, String message) {
        logger.info("Success Alert: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(SUCCESS_TITLE);
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert dialog.
     *
     * @param message The warning message to display
     */
    public void showWarningAlert(String message) {
        showWarningAlert(WARNING_TITLE, message);
    }

    /**
     * Shows a warning alert dialog with custom title.
     *
     * @param title   The title of the alert
     * @param message The warning message to display
     */
    public void showWarningAlert(String title, String message) {
        logger.warn("Warning Alert: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(WARNING_TITLE);
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Shows an information alert dialog.
     *
     * @param message The information message to display
     */
    public void showInformationAlert(String message) {
        showInformationAlert(INFORMATION_TITLE, message);
    }

    /**
     * Shows an information alert dialog with custom title.
     *
     * @param title   The title of the alert
     * @param message The information message to display
     */
    public void showInformationAlert(String title, String message) {
        logger.info("Information Alert: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(INFORMATION_TITLE);
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog and returns the user's choice.
     *
     * @param message The confirmation message to display
     * @return true if user confirmed, false otherwise
     */
    public boolean showConfirmationAlert(String message) {
        return showConfirmationAlert(CONFIRMATION_TITLE, message);
    }

    /**
     * Shows a confirmation dialog with custom title and returns the user's choice.
     *
     * @param title   The title of the alert
     * @param message The confirmation message to display
     * @return true if user confirmed, false otherwise
     */
    public boolean showConfirmationAlert(String title, String message) {
        logger.info("Confirmation Alert: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(CONFIRMATION_TITLE);
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Styles the alert dialog for better appearance.
     *
     * @param alert The alert to style
     */
    private void styleAlert(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
    }
}

