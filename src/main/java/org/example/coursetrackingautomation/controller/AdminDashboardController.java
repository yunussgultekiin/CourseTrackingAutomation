package org.example.coursetrackingautomation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.controller.admin.AdminDashboardCoordinator;
import org.example.coursetrackingautomation.dto.*;
import org.example.coursetrackingautomation.service.AdminDashboardService;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * JavaFX controller for the admin dashboard.
 *
 * <p>Provides navigation between admin views (users/courses/enrollments/attendance), supports
 * search/filter interactions, and delegates business operations to the service layer.</p>
 */
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;
    private final CourseService courseService;
    private final AlertUtil alertUtil;
    private final SceneNavigator sceneNavigator;
    private final UserSession userSession;
    private final UiExceptionHandler uiExceptionHandler;

    private AdminDashboardCoordinator coordinator;
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private Button profileButton;
    @FXML private Button addUserButton;
    @FXML private Button addCourseButton;
    @FXML private Button enrollStudentButton;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Label contentTitleLabel;
    @FXML private TableView<Object> dataTableView;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label activeEnrollmentsLabel;
    @FXML private VBox enrollmentsFilterBox;
    @FXML private Label enrollmentsFilterHintLabel;
    @FXML private TextField enrollmentStudentNameField;
    @FXML private ComboBox<CourseDTO> enrollmentCourseCombo;
    @FXML private ComboBox<String> enrollmentStatusCombo;

    @FXML
    /**
     * Initializes the admin dashboard view.
     *
     * <p>Configures UI bindings, loads initial statistics, and defaults to the user management view.</p>
     */
    public void initialize() {
        coordinator = new AdminDashboardCoordinator(
            adminDashboardService,
            courseService,
            alertUtil,
            sceneNavigator,
            userSession,
            uiExceptionHandler,
            welcomeLabel,
            searchField,
            searchButton,
            contentTitleLabel,
            dataTableView,
            totalUsersLabel,
            totalCoursesLabel,
            activeEnrollmentsLabel,
            enrollmentsFilterBox,
            enrollmentsFilterHintLabel,
            enrollmentStudentNameField,
            enrollmentCourseCombo,
            enrollmentStatusCombo
        );
        coordinator.initialize();
    }

    @FXML
    /**
     * Logs out the current user and navigates back to the login scene.
     */
    public void handleLogout() {
        coordinator.handleLogout();
    }

    @FXML
    /**
     * Opens the profile modal for the current user.
     */
    public void handleProfile() {
        coordinator.handleProfile();
    }

    @FXML
    /**
     * Opens the "Add User" modal and refreshes the current table view after closing.
     */
    public void handleAddUser() {
        coordinator.handleAddUser();
    }

    @FXML
    /**
     * Opens the "Add Course" modal and refreshes the current table view after closing.
     */
    public void handleAddCourse() {
        coordinator.handleAddCourse();
    }

    @FXML
    /**
     * Opens the "Enroll Student" modal and refreshes the current table view after closing.
     */
    public void handleOpenEnrollStudent() {
        coordinator.handleOpenEnrollStudent();
    }

    @FXML
    /**
     * Refreshes the currently visible admin view and updates statistics.
     */
    public void handleRefresh() {
        coordinator.handleRefresh();
    }

    @FXML
    /**
     * Switches to the user management view.
     */
    public void handleUsersManagement() {
        coordinator.handleUsersManagement();
    }

    @FXML
    /**
     * Switches to the course management view.
     */
    public void handleCoursesManagement() {
        coordinator.handleCoursesManagement();
    }

    @FXML
    /**
     * Switches to the enrollments view.
     */
    public void handleEnrollments() {
        coordinator.handleEnrollments();
    }

    @FXML
    /**
     * Switches to the attendance reports view.
     */
    public void handleAttendanceReports() {
        coordinator.handleAttendanceReports();
    }

    @FXML
    /**
     * Executes a search for the active view.
     *
     * <p>When the query is blank, the view is reset to its default data.</p>
     */
    public void handleSearch() {
        coordinator.handleSearch();
    }

    @FXML
    /**
     * Clears enrollment filter inputs and re-applies filters.
     */
    public void handleClearEnrollmentFilters() {
        coordinator.handleClearEnrollmentFilters();
    }
}