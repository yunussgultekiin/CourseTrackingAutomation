package org.example.coursetrackingautomation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.UpdateUserRequest;
import org.example.coursetrackingautomation.dto.UserDetailsDTO;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class EditUserFormController {

    @FXML private Label lblUsername;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;

    private final UserService userService;
    private final UiExceptionHandler uiExceptionHandler;

    private Long userId;

    public void setUserId(Long userId) {
        this.userId = userId;
        refresh();
    }

    private void refresh() {
        try {
            if (userId == null) {
                return;
            }

            UserDetailsDTO user = userService.getUserDetailsById(userId);
            lblUsername.setText(user.username() == null ? "-" : user.username());
            txtFirstName.setText(user.firstName());
            txtLastName.setText(user.lastName());
            txtEmail.setText(user.email());
            txtPhone.setText(user.phone());
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleSave() {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("Kullanıcı ID boş olamaz");
            }

            String firstName = requireNotBlank(safeTrim(txtFirstName.getText()), "Ad");
            String lastName = requireNotBlank(safeTrim(txtLastName.getText()), "Soyad");

            UpdateUserRequest request = new UpdateUserRequest(
                firstName,
                lastName,
                safeTrimToNull(txtEmail.getText()),
                safeTrimToNull(txtPhone.getText()),
                null
            );

            userService.updateUser(userId, request);
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
