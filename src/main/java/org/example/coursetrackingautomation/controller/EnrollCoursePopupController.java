package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.service.EnrollmentService;
import org.example.coursetrackingautomation.ui.FxAsync;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
/**
 * JavaFX controller for the student course enrollment popup.
 *
 * <p>Shows a list of active courses with available quota and allows the authenticated student
 * to enroll via {@link EnrollmentService}.</p>
 */
public class EnrollCoursePopupController {

    private static final String PROPERTY_CODE = "code";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_CREDIT = "credit";
    private static final String PROPERTY_AVAILABLE_QUOTA = "availableQuota";

    @FXML
    private TableView<CourseDTO> tableCourses;

    @FXML
    private TableColumn<CourseDTO, String> colCode;

    @FXML
    private TableColumn<CourseDTO, String> colName;

    @FXML
    private TableColumn<CourseDTO, Integer> colCredit;

    @FXML
    private TableColumn<CourseDTO, Long> colAvailable;

    @FXML
    private Label lblHint;

    @FXML
    private Button btnEnroll;

    private final UserSession userSession;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final UiExceptionHandler uiExceptionHandler;

    @FXML
    /**
     * Initializes the table view and loads courses.
     */
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_CODE));
        colName.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_NAME));
        colCredit.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_CREDIT));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_AVAILABLE_QUOTA));

        tableCourses.setRowFactory(tv -> {
            TableRow<CourseDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    tableCourses.getSelectionModel().select(row.getItem());
                    handleEnroll();
                }
            });
            return row;
        });

        refreshCourses();
    }

    @FXML
    /**
     * Enrolls the current student into the selected course.
     */
    public void handleEnroll() {
        try {
            CourseDTO selected = tableCourses.getSelectionModel().getSelectedItem();
            if (selected == null || selected.getId() == null) {
                throw new IllegalArgumentException(UiConstants.ERROR_KEY_COURSE_SELECTION_REQUIRED);
            }

            Long studentId = userSession.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException(UiConstants.ERROR_KEY_NO_ACTIVE_SESSION))
                .id();

            FxAsync.runAsync(
                () -> enrollmentService.enrollStudent(studentId, selected.getId()),
                savedEnrollment -> close(),
                uiExceptionHandler::handle
            );
        } catch (IllegalStateException | IllegalArgumentException e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    /**
     * Closes the popup without enrolling.
     */
    public void handleClose() {
        close();
    }

    private void refreshCourses() {
        FxAsync.runAsync(
            courseService::getAllActiveCourseDTOs,
            courses -> tableCourses.setItems(FXCollections.observableArrayList(courses)),
            uiExceptionHandler::handle
        );
    }

    private void close() {
        Stage stage = (Stage) tableCourses.getScene().getWindow();
        stage.close();
    }
}
