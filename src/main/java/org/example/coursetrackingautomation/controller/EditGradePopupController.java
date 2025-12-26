package org.example.coursetrackingautomation.controller;

import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.service.GradeService;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class EditGradePopupController {

    @FXML private Label lblStudent;
    @FXML private TextField txtMidterm;
    @FXML private TextField txtFinal;
    @FXML private Label lblAverage;
    @FXML private Label lblLetter;
    @FXML private Label lblStatus;

    private final GradeService gradeService;
    private final AlertUtil alertUtil;

    private GradeDTO row;
    private Consumer<GradeDTO> onSave;

    public void setContext(GradeDTO row, Consumer<GradeDTO> onSave) {
        this.row = row;
        this.onSave = onSave;
        fillFormFromRow();
        refreshPreview();
    }

    @FXML
    public void handleSave() {
        if (row == null) {
            handleClose();
            return;
        }

        Double midterm = parseScoreOrNull(txtMidterm == null ? null : txtMidterm.getText());
        Double fin = parseScoreOrNull(txtFinal == null ? null : txtFinal.getText());

        if (!isValidScore(midterm) || !isValidScore(fin)) {
            alertUtil.showErrorAlert(UiConstants.ALERT_TITLE_VALIDATION_ERROR, "Not 0 ile 100 arasında olmalıdır.");
            return;
        }

        row.setMidtermScore(midterm);
        row.setFinalScore(fin);

        Double average = gradeService.calculateAverage(midterm, fin);
        boolean graded = midterm != null && fin != null;
        String letter = graded ? gradeService.determineLetterGrade(average) : null;
        boolean passed = graded && gradeService.isPassed(letter);

        row.setAverageScore(average);
        row.setLetterGrade(letter);
        if (!graded) {
            row.setStatus(UiConstants.UI_STATUS_NOT_GRADED);
        } else {
            row.setStatus(passed ? UiConstants.UI_STATUS_PASSED : UiConstants.UI_STATUS_FAILED);
        }

        refreshPreview();

        if (onSave != null) {
            onSave.accept(row);
        }

        handleClose();
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) (lblStudent != null && lblStudent.getScene() != null ? lblStudent.getScene().getWindow() : null);
        if (stage != null) {
            stage.close();
        }
    }

    private void fillFormFromRow() {
        if (row == null) {
            return;
        }

        if (lblStudent != null) {
            lblStudent.setText(row.getStudentName() == null ? "-" : row.getStudentName());
        }

        if (txtMidterm != null) {
            txtMidterm.setText(row.getMidtermScore() == null ? "" : stripTrailingZeros(row.getMidtermScore()));
        }

        if (txtFinal != null) {
            txtFinal.setText(row.getFinalScore() == null ? "" : stripTrailingZeros(row.getFinalScore()));
        }
    }

    private void refreshPreview() {
        if (row == null) {
            return;
        }

        if (lblAverage != null) {
            lblAverage.setText(row.getAverageScore() == null ? "-" : stripTrailingZeros(row.getAverageScore()));
        }
        if (lblLetter != null) {
            lblLetter.setText(row.getLetterGrade() == null ? "-" : row.getLetterGrade());
        }
        if (lblStatus != null) {
            lblStatus.setText(row.getStatus() == null ? "-" : row.getStatus());
        }
    }

    private boolean isValidScore(Double score) {
        if (score == null) {
            return true;
        }
        return score >= 0.0 && score <= 100.0;
    }

    private Double parseScoreOrNull(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        normalized = normalized.replace(',', '.');
        return Double.parseDouble(normalized);
    }

    private String stripTrailingZeros(Double value) {
        if (value == null) {
            return "";
        }
        if (value % 1.0 == 0.0) {
            return String.valueOf(value.intValue());
        }
        return String.valueOf(value);
    }
}
