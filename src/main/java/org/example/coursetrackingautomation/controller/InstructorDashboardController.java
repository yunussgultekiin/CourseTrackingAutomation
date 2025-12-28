package org.example.coursetrackingautomation.controller;

import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.controller.instructor.InstructorDashboardCoordinator;
import org.example.coursetrackingautomation.controller.instructor.InstructorGradesTableConfigurer;
import org.example.coursetrackingautomation.controller.instructor.InstructorRosterRowFactory;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.service.AttendanceService;
import org.example.coursetrackingautomation.service.InstructorWorkflowService;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
/**
 * JavaFX controller for the instructor dashboard.
 *
 * <p>This controller is intentionally thin: it wires FXML controls and delegates the screen
 * workflow/state management to {@link InstructorDashboardCoordinator}. Table column configuration
 * and row behavior are installed via {@link InstructorGradesTableConfigurer} and
 * {@link InstructorRosterRowFactory}.</p>
 */
public class InstructorDashboardController {

    private static final String STYLE_CRITICAL_ATTENDANCE = "critical-attendance";
    private static final String STYLE_WARNING_ATTENDANCE = "warning-attendance";

    @FXML private ComboBox<String> comboCourses;
    @FXML private ComboBox<String> comboWeeks;
    @FXML private Label lblCourseHours;
    @FXML private TableView<GradeDTO> tableStudents;
    @FXML private TableColumn<GradeDTO, Long> colStudentNumber;
    @FXML private TableColumn<GradeDTO, String> colFullName;
    @FXML private TableColumn<GradeDTO, Double> colMidterm;
    @FXML private TableColumn<GradeDTO, Double> colFinal;
    @FXML private TableColumn<GradeDTO, Integer> colAttendance;
    @FXML private TableColumn<GradeDTO, Boolean> colPresent;
    @FXML private TableColumn<GradeDTO, Double> colAverage;
    @FXML private TableColumn<GradeDTO, String> colLetterGrade;
    @FXML private TableColumn<GradeDTO, String> colStatus;
    @FXML private Button btnLogOut;
    @FXML private Button btnProfile;

    private final UserSession userSession;
    private final InstructorWorkflowService instructorWorkflowService;
    private final AttendanceService attendanceService;
    private final SceneNavigator sceneNavigator;
    private final UiExceptionHandler uiExceptionHandler;
    private final AlertUtil alertUtil;

    private InstructorDashboardCoordinator coordinator;

    @FXML
    /**
     * Initializes the instructor dashboard.
     *
     * <p>Creates the coordinator, configures the grades table, and loads initial data for the
     * currently logged-in instructor.</p>
     */
    public void initialize() {
        coordinator = new InstructorDashboardCoordinator(
            comboCourses,
            comboWeeks,
            lblCourseHours,
            tableStudents,
            userSession,
            instructorWorkflowService,
            sceneNavigator,
            uiExceptionHandler,
            alertUtil
        );

        InstructorGradesTableConfigurer.configure(
            tableStudents,
            colStudentNumber,
            colFullName,
            colMidterm,
            colFinal,
            colAttendance,
            colPresent,
            colAverage,
            colLetterGrade,
            colStatus,
            coordinator::getMidtermProp,
            coordinator::getFinalProp,
            coordinator::getAverageProp,
            coordinator::getLetterProp,
            coordinator::getStatusProp,
            coordinator::getSelectedCourse,
            attendanceService,
            alertUtil,
            coordinator::markDirtyIfChanged
        );
        InstructorRosterRowFactory.configure(
            tableStudents,
            coordinator::getSelectedCourse,
            attendanceService,
            STYLE_CRITICAL_ATTENDANCE,
            STYLE_WARNING_ATTENDANCE,
            coordinator::openEditGradePopup
        );
        tableStudents.setEditable(true);

        coordinator.initialize();
    }

    @FXML
    /**
     * Attempts to log out.
     *
     * <p>If there are unsaved changes, the user is prompted to save before exiting.</p>
     */
    public void handleLogout() {
        Stage stage = (Stage) btnLogOut.getScene().getWindow();
        coordinator.attemptExit(stage);
    }

    @FXML
    /**
     * Opens the profile modal for the current instructor.
     */
    public void handleProfile() {
        sceneNavigator.openModal(
            UiConstants.FXML_PROFILE_POPUP,
            UiConstants.WINDOW_TITLE_PROFILE,
            btnProfile.getScene().getWindow()
        );
    }

    @FXML
    /**
     * Persists grade/attendance edits for the currently selected course and week.
     */
    public void handleSave() {
        coordinator.performSave(Optional.empty());
    }

}
