package org.example.coursetrackingautomation.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
/**
 * Convenience utility for displaying JavaFX alert dialogs.
 *
 * <p>Wraps the standard {@link Alert} API and applies consistent dialog styling.
 * Intended for UI-level feedback (validation errors, confirmations, and informational messages).</p>
 */
public class AlertUtil {

    private static final Logger logger = LoggerFactory.getLogger(AlertUtil.class);

    private static final String ERROR_TITLE = "Hata";
    private static final String SUCCESS_TITLE = "Başarılı";
    private static final String WARNING_TITLE = "Uyarı";
    private static final String INFORMATION_TITLE = "Bilgi";
    private static final String CONFIRMATION_TITLE = "Onay";

    /**
     * Shows an error alert with a default title.
     *
     * @param message message to display
     */
    public void showErrorAlert(String message) {
        showErrorAlert(ERROR_TITLE, message);
    }

    /**
     * Shows an error alert.
     *
     * @param title header title
     * @param message message to display
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
     * Shows a success alert with a default title.
     *
     * @param message message to display
     */
    public void showSuccessAlert(String message) {
        showSuccessAlert(SUCCESS_TITLE, message);
    }

    /**
     * Shows a success alert.
     *
     * @param title header title
     * @param message message to display
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
     * Shows a warning alert with a default title.
     *
     * @param message message to display
     */
    public void showWarningAlert(String message) {
        showWarningAlert(WARNING_TITLE, message);
    }

    /**
     * Shows a warning alert.
     *
     * @param title header title
     * @param message message to display
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
     * Shows an informational alert with a default title.
     *
     * @param message message to display
     */
    public void showInformationAlert(String message) {
        showInformationAlert(INFORMATION_TITLE, message);
    }

    /**
     * Shows an informational alert.
     *
     * @param title header title
     * @param message message to display
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
     * Shows a confirmation alert with a default title.
     *
     * @param message message to display
     * @return {@code true} if the user confirms
     */
    public boolean showConfirmationAlert(String message) {
        return showConfirmationAlert(CONFIRMATION_TITLE, message);
    }

    /**
     * Shows a confirmation alert.
     *
     * @param title header title
     * @param message message to display
     * @return {@code true} if the user confirms
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

    private void styleAlert(Alert alert) {
        applyModernDialogIcon(alert);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
    }

    private static void applyModernDialogIcon(Alert alert) {
        if (alert == null) {
            return;
        }

        String glyph;
        String color;

        switch (alert.getAlertType()) {
            case ERROR -> {
                glyph = "✕";
                color = "#e74c3c";
            }
            case WARNING -> {
                glyph = "!";
                color = "#7f8c8d";
            }
            case CONFIRMATION -> {
                glyph = "?";
                color = "#7f8c8d";
            }
            case INFORMATION -> {
                glyph = "ℹ";
                color = "#7f8c8d";
            }
            default -> {
                glyph = "";
                color = "#7f8c8d";
            }
        }

        Label graphic = new Label(glyph);
        graphic.setFont(Font.font("System", 26));
        graphic.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
        alert.setGraphic(graphic);
    }
}

