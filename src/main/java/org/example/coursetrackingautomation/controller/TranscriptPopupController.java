package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.service.TranscriptService;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TranscriptPopupController {

    @FXML
    private TableView<GradeDTO> tableTranscript;

    @FXML
    private TableColumn<GradeDTO, String> colCourseCode;
    @FXML
    private TableColumn<GradeDTO, Double> colMidterm;
    @FXML
    private TableColumn<GradeDTO, Double> colFinal;
    @FXML
    private TableColumn<GradeDTO, Double> colAverage;
    @FXML
    private TableColumn<GradeDTO, String> colLetter;
    @FXML
    private TableColumn<GradeDTO, Integer> colAttendance;
    @FXML
    private TableColumn<GradeDTO, String> colStatus;

    @FXML
    private Label lblGpa;

    private final UserSession userSession;
    private final TranscriptService transcriptService;
    private final UiExceptionHandler uiExceptionHandler;

    @FXML
    public void initialize() {
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colMidterm.setCellValueFactory(new PropertyValueFactory<>("midtermScore"));
        colFinal.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        colAverage.setCellValueFactory(new PropertyValueFactory<>("averageScore"));
        colLetter.setCellValueFactory(new PropertyValueFactory<>("letterGrade"));
        colAttendance.setCellValueFactory(new PropertyValueFactory<>("attendanceCount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        refresh();
    }

    private void refresh() {
        try {
            var currentUser = userSession.getCurrentUser().orElseThrow(() -> new IllegalStateException("No active session"));
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
