package org.example.coursetrackingautomation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.dto.SessionUser;
import org.example.coursetrackingautomation.dto.UpdateUserRequest;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProfilePopupController {

    @FXML private Label lblUsername;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;

    private final UserSession userSession;
    private final UserService userService;
    private final UiExceptionHandler uiExceptionHandler;
    private final AlertUtil alertUtil;

    @FXML
    public void initialize() {
        try {
            var currentUser = userSession.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException(UiConstants.ERROR_KEY_NO_ACTIVE_SESSION));
            lblUsername.setText(currentUser.username());
            txtFirstName.setText(currentUser.firstName());
            txtLastName.setText(currentUser.lastName());
            var fullUser = userService.getUserById(currentUser.id());
            txtEmail.setText(fullUser.getEmail());
            txtPhone.setText(fullUser.getPhone());
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleSave() {
        try {
            var currentUser = userSession.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException(UiConstants.ERROR_KEY_NO_ACTIVE_SESSION));

            String firstName = safeTrim(txtFirstName.getText());
            String lastName = safeTrim(txtLastName.getText());
            String email = safeTrimOrNull(txtEmail.getText());
            String phone = safeTrimOrNull(txtPhone.getText());

            if (firstName.isBlank()) {
                throw new IllegalArgumentException("First name must not be blank");
            }
            if (lastName.isBlank()) {
                throw new IllegalArgumentException("Last name must not be blank");
            }

            userService.updateUser(currentUser.id(), new UpdateUserRequest(
                firstName,
                lastName,
                email,
                phone,
                null
            ));

            userSession.setCurrentUser(new SessionUser(
                currentUser.id(),
                currentUser.username(),
                firstName,
                lastName,
                currentUser.role()
            ));

            String currentPassword = safe(txtCurrentPassword.getText());
            String newPassword = safe(txtNewPassword.getText());
            String confirmPassword = safe(txtConfirmPassword.getText());

            boolean passwordSectionTouched = !currentPassword.isBlank() || !newPassword.isBlank() || !confirmPassword.isBlank();
            if (passwordSectionTouched) {
                if (currentPassword.isBlank()) {
                    throw new IllegalArgumentException("Current password must not be blank");
                }
                if (newPassword.isBlank()) {
                    throw new IllegalArgumentException("New password must not be blank");
                }
                if (!newPassword.equals(confirmPassword)) {
                    throw new IllegalArgumentException("New password and confirmation do not match");
                }
                userService.changePassword(currentUser.id(), currentPassword, newPassword);
            }

            alertUtil.showSuccessAlert(UiConstants.ALERT_TITLE_SUCCESS, UiConstants.UI_MESSAGE_PROFILE_UPDATED);
            close();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleClose() {
        close();
    }

    private void close() {
        Stage stage = (Stage) lblUsername.getScene().getWindow();
        stage.close();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safeTrimOrNull(String value) {
        String trimmed = safeTrim(value);
        return trimmed.isBlank() ? null : trimmed;
    }
}
