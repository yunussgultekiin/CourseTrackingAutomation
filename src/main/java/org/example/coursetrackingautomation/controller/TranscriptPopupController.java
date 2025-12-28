package org.example.coursetrackingautomation.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.controller.support.GradeDetailsMessageBuilder;
import org.example.coursetrackingautomation.service.TranscriptService;
import org.example.coursetrackingautomation.ui.FxAsync;
import org.example.coursetrackingautomation.ui.GradeStatusUiMapper;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
/**
 * JavaFX controller for the transcript popup.
 *
 * <p>Displays transcript rows and GPA for the authenticated student.</p>
 */
public class TranscriptPopupController {

    private static final String PROPERTY_COURSE_CODE = "courseCode";
    private static final String PROPERTY_MIDTERM_SCORE = "midtermScore";
    private static final String PROPERTY_FINAL_SCORE = "finalScore";
    private static final String PROPERTY_AVERAGE_SCORE = "averageScore";
    private static final String PROPERTY_LETTER_GRADE = "letterGrade";
    private static final String PROPERTY_ATTENDANCE_COUNT = "attendanceCount";
    private static final String PROPERTY_STATUS = "status";

    @FXML private TableView<GradeDTO> tableTranscript;
    @FXML private TableColumn<GradeDTO, String> colCourseCode;
    @FXML private TableColumn<GradeDTO, Double> colMidterm;
    @FXML private TableColumn<GradeDTO, Double> colFinal;
    @FXML private TableColumn<GradeDTO, Double> colAverage;
    @FXML private TableColumn<GradeDTO, String> colLetter;
    @FXML private TableColumn<GradeDTO, Integer> colAttendance;
    @FXML private TableColumn<GradeDTO, String> colStatus;
    @FXML private Label lblGpa;

    private final UserSession userSession;
    private final TranscriptService transcriptService;
    private final UiExceptionHandler uiExceptionHandler;
    private final AlertUtil alertUtil;

    @FXML
    /**
     * Initializes the transcript table and loads data for the current student.
     */
    public void initialize() {
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_COURSE_CODE));
        colMidterm.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_MIDTERM_SCORE));
        colFinal.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_FINAL_SCORE));
        colAverage.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_AVERAGE_SCORE));
        colLetter.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_LETTER_GRADE));
        colAttendance.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_ATTENDANCE_COUNT));
        colAttendance.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                int hours = value == null ? 0 : value;
                Label badge = new Label(String.valueOf(hours));
                badge.getStyleClass().addAll("badge", "badge-neutral");
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });
        colStatus.setCellValueFactory(cell -> {
            GradeDTO row = cell.getValue();
            String text = row == null || row.getStatus() == null ? "-" : GradeStatusUiMapper.toTurkish(row.getStatus());
            return new SimpleStringProperty(text);
        });
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || value.isBlank() || "-".equals(value)) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String badgeClass;
                if (UiConstants.UI_STATUS_PASSED.equalsIgnoreCase(value)) {
                    badgeClass = "badge-success";
                } else if (UiConstants.UI_STATUS_FAILED.equalsIgnoreCase(value)) {
                    badgeClass = "badge-danger";
                } else {
                    badgeClass = "badge-neutral";
                }

                Label badge = new Label(value);
                badge.getStyleClass().addAll("badge", badgeClass);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        tableTranscript.setRowFactory(tv -> {
            TableRow<GradeDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showGradeDetails(row.getItem());
                }
            });
            return row;
        });

        refresh();
    }

    private void showGradeDetails(GradeDTO item) {
        if (item == null) {
            return;
        }

        alertUtil.showInformationAlert("Not Detayı", GradeDetailsMessageBuilder.buildTranscriptPopupMessage(item));
    }

    private void refresh() {
        try {
            var currentUser = userSession.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException(UiConstants.ERROR_KEY_NO_ACTIVE_SESSION));

            FxAsync.runAsync(
                () -> transcriptService.getTranscriptGradesForStudent(currentUser.id()),
                transcript -> {
                    tableTranscript.setItems(FXCollections.observableArrayList(transcript));
                    lblGpa.setText(transcriptService.calculateGpaText(tableTranscript.getItems()));
                },
                uiExceptionHandler::handle
            );
        } catch (IllegalStateException e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    /**
     * Closes the popup.
     */
    public void handleClose() {
        Stage stage = (Stage) tableTranscript.getScene().getWindow();
        stage.close();
    }
}
