package org.example.coursetrackingautomation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.dto.RoleDTO;
import org.example.coursetrackingautomation.dto.SessionUser;
import org.example.coursetrackingautomation.dto.UpdateUserRequest;
import org.example.coursetrackingautomation.dto.UserDetailsDTO;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProfilePopupController {

    @FXML private Label lblUsername;
    @FXML private Separator sepPassword;
    @FXML private Label lblCurrentPassword;
    @FXML private Label lblNewPassword;
    @FXML private Label lblConfirmPassword;
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
            UserDetailsDTO userDetails = userService.getUserDetailsById(currentUser.id());
            txtEmail.setText(userDetails.email());
            txtPhone.setText(userDetails.phone());

            boolean canChangePassword = currentUser.role() == RoleDTO.INSTRUCTOR || currentUser.role() == RoleDTO.STUDENT;
            if (!canChangePassword) {
                setPasswordSectionVisible(false);
            }
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
                throw new IllegalArgumentException("Ad boş bırakılamaz");
            }
            if (lastName.isBlank()) {
                throw new IllegalArgumentException("Soyad boş bırakılamaz");
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

            boolean canChangePassword = currentUser.role() == RoleDTO.INSTRUCTOR || currentUser.role() == RoleDTO.STUDENT;
            if (canChangePassword) {
                String currentPassword = safe(txtCurrentPassword.getText());
                String newPassword = safe(txtNewPassword.getText());
                String confirmPassword = safe(txtConfirmPassword.getText());

                boolean passwordSectionTouched = !currentPassword.isBlank() || !newPassword.isBlank() || !confirmPassword.isBlank();
                if (passwordSectionTouched) {
                    if (currentPassword.isBlank()) {
                        throw new IllegalArgumentException("Mevcut şifre boş bırakılamaz");
                    }
                    if (newPassword.isBlank()) {
                        throw new IllegalArgumentException("Yeni şifre boş bırakılamaz");
                    }
                    if (!newPassword.equals(confirmPassword)) {
                        throw new IllegalArgumentException("Yeni şifre ve doğrulama şifresi eşleşmiyor");
                    }
                    userService.changePassword(currentUser.id(), currentPassword, newPassword);
                }
            }

            alertUtil.showSuccessAlert(UiConstants.ALERT_TITLE_SUCCESS, UiConstants.UI_MESSAGE_PROFILE_UPDATED);
            close();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    private void setPasswordSectionVisible(boolean visible) {
        if (sepPassword != null) {
            sepPassword.setVisible(visible);
            sepPassword.setManaged(visible);
        }
        if (lblCurrentPassword != null) {
            lblCurrentPassword.setVisible(visible);
            lblCurrentPassword.setManaged(visible);
        }
        if (lblNewPassword != null) {
            lblNewPassword.setVisible(visible);
            lblNewPassword.setManaged(visible);
        }
        if (lblConfirmPassword != null) {
            lblConfirmPassword.setVisible(visible);
            lblConfirmPassword.setManaged(visible);
        }
        if (txtCurrentPassword != null) {
            txtCurrentPassword.clear();
            txtCurrentPassword.setVisible(visible);
            txtCurrentPassword.setManaged(visible);
        }
        if (txtNewPassword != null) {
            txtNewPassword.clear();
            txtNewPassword.setVisible(visible);
            txtNewPassword.setManaged(visible);
        }
        if (txtConfirmPassword != null) {
            txtConfirmPassword.clear();
            txtConfirmPassword.setVisible(visible);
            txtConfirmPassword.setManaged(visible);
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
