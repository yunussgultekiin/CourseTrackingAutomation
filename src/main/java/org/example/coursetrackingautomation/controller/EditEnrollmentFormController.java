package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.entity.EnrollmentStatus;
import org.example.coursetrackingautomation.service.EnrollmentService;
import org.example.coursetrackingautomation.ui.FxAsync;
import org.example.coursetrackingautomation.ui.EnrollmentStatusUiMapper;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
/**
 * JavaFX controller for editing an enrollment.
 *
 * <p>Loads enrollment details and allows updating its status through {@link EnrollmentService}.</p>
 */
public class EditEnrollmentFormController {

    @FXML
    private Label lblEnrollmentId;

    @FXML
    private Label lblStudent;

    @FXML
    private Label lblCourse;

    @FXML
    private ComboBox<String> comboStatus;

    private final EnrollmentService enrollmentService;
    private final UiExceptionHandler uiExceptionHandler;

    private Long enrollmentId;

    @FXML
    /**
     * Initializes the status selection control.
     */
    public void initialize() {
        comboStatus.setItems(FXCollections.observableArrayList(
            EnrollmentStatus.ACTIVE.name(),
            EnrollmentStatus.ENROLLED.name(),
            EnrollmentStatus.REGISTERED.name(),
            EnrollmentStatus.DROPPED.name(),
            EnrollmentStatus.CANCELLED.name()
        ));

        comboStatus.setEditable(false);
        comboStatus.setConverter(new StringConverter<>() {
            @Override
            public String toString(String object) {
                return EnrollmentStatusUiMapper.toTurkish(object);
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        });

        comboStatus.setCellFactory(cb -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : EnrollmentStatusUiMapper.toTurkish(item));
            }
        });
        comboStatus.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(comboStatus.getPromptText());
                } else {
                    setText(EnrollmentStatusUiMapper.toTurkish(item));
                }
            }
        });
    }

    /**
     * Sets the enrollment id to edit and loads the current values.
     *
     * @param enrollmentId enrollment identifier
     */
    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
        refresh();
    }

    private void refresh() {
        if (enrollmentId == null) {
            return;
        }

        Long id = enrollmentId;
        FxAsync.runAsync(
            () -> enrollmentService.getEnrollmentDetailsById(id),
            details -> {
                lblEnrollmentId.setText(String.valueOf(details.id()));
                lblStudent.setText(details.studentDisplay());
                lblCourse.setText(details.courseDisplay());

                EnrollmentStatus status = details.status();
                if (status != null) {
                    comboStatus.setValue(status.name());
                }
            },
            uiExceptionHandler::handle
        );
    }

    @FXML
    /**
     * Persists the selected enrollment status.
     */
    public void handleSave() {
        if (enrollmentId == null) {
            uiExceptionHandler.handle(new IllegalArgumentException("Kayıt ID boş olamaz"));
            return;
        }

        Long id = enrollmentId;
        String status = comboStatus.getValue();
        FxAsync.runAsync(
            () -> {
                enrollmentService.updateEnrollmentStatus(id, status);
            },
            this::close,
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
        Stage stage = (Stage) lblEnrollmentId.getScene().getWindow();
        stage.close();
    }
}
