package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.service.TranscriptService;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
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
    public void initialize() {
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_COURSE_CODE));
        colMidterm.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_MIDTERM_SCORE));
        colFinal.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_FINAL_SCORE));
        colAverage.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_AVERAGE_SCORE));
        colLetter.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_LETTER_GRADE));
        colAttendance.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_ATTENDANCE_COUNT));
        colStatus.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_STATUS));

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

        String message = "Ders: " + item.getCourseCode() + "\n"
                + "Vize: " + (item.getMidtermScore() == null ? "-" : item.getMidtermScore()) + "\n"
                + "Final: " + (item.getFinalScore() == null ? "-" : item.getFinalScore()) + "\n"
                + "Ortalama: " + (item.getAverageScore() == null ? "-" : item.getAverageScore()) + "\n"
                + "Harf: " + (item.getLetterGrade() == null ? "-" : item.getLetterGrade()) + "\n"
                + "Devamsızlık (Saat): " + (item.getAttendanceCount() == null ? 0 : item.getAttendanceCount()) + "\n"
                + "Saat (Haftalık): "
                + (item.getWeeklyTotalHours() == null ? "-" : item.getWeeklyTotalHours())
                + " (Teori " + (item.getWeeklyTheoryHours() == null ? "-" : item.getWeeklyTheoryHours())
                + ", Uygulama " + (item.getWeeklyPracticeHours() == null ? "-" : item.getWeeklyPracticeHours()) + ")\n"
                + "Durum: " + (item.getStatus() == null ? "-" : item.getStatus());

        alertUtil.showInformationAlert("Not Detayı", message);
    }

    private void refresh() {
        try {
            var currentUser = userSession.getCurrentUser().orElseThrow(() -> new IllegalStateException(UiConstants.ERROR_KEY_NO_ACTIVE_SESSION));
            var transcript = transcriptService.getTranscriptGradesForStudent(currentUser.id());
            tableTranscript.setItems(FXCollections.observableArrayList(transcript));
            lblGpa.setText(transcriptService.calculateGpaText(tableTranscript.getItems()));
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) tableTranscript.getScene().getWindow();
        stage.close();
    }
}
