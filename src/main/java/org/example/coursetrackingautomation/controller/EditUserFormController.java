package org.example.coursetrackingautomation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.UpdateUserRequest;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.FxAsync;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
/**
 * JavaFX controller for the "Edit User" form.
 *
 * <p>Loads user details by id, allows editing of profile fields, and persists updates via
 * {@link UserService}.</p>
 */
public class EditUserFormController {

    @FXML private Label lblUsername;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;

    private final UserService userService;
    private final UiExceptionHandler uiExceptionHandler;

    private Long userId;

    /**
     * Sets the user id to edit and loads the current values into the form.
     *
     * @param userId user identifier
     */
    public void setUserId(Long userId) {
        this.userId = userId;
        refresh();
    }

    private void refresh() {
        if (userId == null) {
            return;
        }

        FxAsync.runAsync(
            () -> userService.getUserDetailsById(userId),
            user -> {
                lblUsername.setText(user.username() == null ? "-" : user.username());
                txtFirstName.setText(user.firstName());
                txtLastName.setText(user.lastName());
                txtEmail.setText(user.email());
                txtPhone.setText(user.phone());
            },
            uiExceptionHandler::handle
        );
    }

    @FXML
    /**
     * Validates input and persists user updates.
     */
    public void handleSave() {
        final UpdateUserRequest request;
        final Long id = userId;
        try {
            if (id == null) {
                throw new IllegalArgumentException("Kullanıcı ID boş olamaz");
            }

            String firstName = requireNotBlank(safeTrim(txtFirstName.getText()), "Ad");
            String lastName = requireNotBlank(safeTrim(txtLastName.getText()), "Soyad");

            request = new UpdateUserRequest(
                firstName,
                lastName,
                safeTrimToNull(txtEmail.getText()),
                safeTrimToNull(txtPhone.getText()),
                null
            );
        } catch (IllegalArgumentException e) {
            uiExceptionHandler.handle(e);
            return;
        }

        FxAsync.runAsync(
            () -> {
                userService.updateUser(id, request);
                return Boolean.TRUE;
            },
            ignored -> close(),
            uiExceptionHandler::handle
        );
    }

    @FXML
    /**
     * Closes the window without saving.
     */
    public void handleClose() {
        close();
    }

    private void close() {
        Stage stage = (Stage) lblUsername.getScene().getWindow();
        stage.close();
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safeTrimToNull(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " boş bırakılamaz");
        }
        return value;
    }
}
