package org.example.coursetrackingautomation.controller.admin;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.dto.AdminAttendanceRowDTO;
import org.example.coursetrackingautomation.dto.AdminEnrollmentRowDTO;
import org.example.coursetrackingautomation.dto.AdminUserRowDTO;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.dto.RoleDTO;
import org.example.coursetrackingautomation.entity.EnrollmentStatus;
import org.example.coursetrackingautomation.service.AdminDashboardService;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.ui.FxAsync;
import org.example.coursetrackingautomation.ui.EnrollmentStatusUiMapper;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;

@Slf4j
/**
 * Coordinates the Admin Dashboard UI workflow.
 *
 * <p>This class owns the state for the currently selected view (users/courses/enrollments/attendance)
 * and implements the UI interactions: navigation, search, filtering, statistics refresh and table
 * configuration. The FXML controller delegates to this coordinator to keep controllers small and
 * focused on wiring.</p>
 */
public class AdminDashboardCoordinator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String VIEW_USERS = "users";
    private static final String VIEW_COURSES = "courses";
    private static final String VIEW_ENROLLMENTS = "enrollments";
    private static final String VIEW_ATTENDANCE = "attendance";
    private static final String ACTION_TYPE_USER = "user";
    private static final String ACTION_TYPE_COURSE = "course";
    private static final String ACTION_TYPE_ENROLLMENT = "enrollment";

    private static final List<String> ENROLLMENT_STATUS_CODES = List.of(
        "ACTIVE",
        "ENROLLED",
        "REGISTERED",
        "DROPPED",
        "CANCELLED"
    );

    private final AdminDashboardService adminDashboardService;
    private final CourseService courseService;
    private final AlertUtil alertUtil;
    private final SceneNavigator sceneNavigator;
    private final UserSession userSession;
    private final UiExceptionHandler uiExceptionHandler;

    private final Label welcomeLabel;
    private final Button searchButton;
    private final TextField searchField;
    private final Label contentTitleLabel;
    private final TableView<Object> dataTableView;

    private final Label totalUsersLabel;
    private final Label totalCoursesLabel;
    private final Label activeEnrollmentsLabel;

    private final VBox enrollmentsFilterBox;
    private final Label enrollmentsFilterHintLabel;
    private final TextField enrollmentStudentNameField;
    private final ComboBox<CourseDTO> enrollmentCourseCombo;
    private final ComboBox<String> enrollmentStatusCombo;

    private String currentViewMode = VIEW_USERS;

    private boolean suppressSearchEvents;

    private long tableRequestSequence;
    private long activeTableRequestToken;
    private String activeTableRequestViewMode = VIEW_USERS;

    private AdminDashboardTableManager tableManager;
    private AdminDashboardActions dashboardActions;
    private AdminDashboardColumnFactory columnFactory;

    /**
     * Creates a coordinator instance.
     *
     * @param adminDashboardService service providing admin dashboard data
     * @param courseService service providing course data
     * @param alertUtil UI alert helper
     * @param sceneNavigator navigation helper for scenes/modals
     * @param userSession current session holder
     * @param uiExceptionHandler centralized UI exception handling
     * @param welcomeLabel label used for greeting text
     * @param searchField global search input
     * @param searchButton global search trigger
     * @param contentTitleLabel label showing the current view title
     * @param dataTableView shared table for displaying the active view
     * @param totalUsersLabel statistics label
     * @param totalCoursesLabel statistics label
     * @param activeEnrollmentsLabel statistics label
     * @param enrollmentsFilterBox enrollments-only filter panel
     * @param enrollmentsFilterHintLabel hint shown when no filter is applied
     * @param enrollmentStudentNameField enrollment filter input
     * @param enrollmentCourseCombo enrollment filter input
     * @param enrollmentStatusCombo enrollment filter input
     */
    public AdminDashboardCoordinator(
        AdminDashboardService adminDashboardService,
        CourseService courseService,
        AlertUtil alertUtil,
        SceneNavigator sceneNavigator,
        UserSession userSession,
        UiExceptionHandler uiExceptionHandler,
        Label welcomeLabel,
        TextField searchField,
        Button searchButton,
        Label contentTitleLabel,
        TableView<Object> dataTableView,
        Label totalUsersLabel,
        Label totalCoursesLabel,
        Label activeEnrollmentsLabel,
        VBox enrollmentsFilterBox,
        Label enrollmentsFilterHintLabel,
        TextField enrollmentStudentNameField,
        ComboBox<CourseDTO> enrollmentCourseCombo,
        ComboBox<String> enrollmentStatusCombo
    ) {
        this.adminDashboardService = adminDashboardService;
        this.courseService = courseService;
        this.alertUtil = alertUtil;
        this.sceneNavigator = sceneNavigator;
        this.userSession = userSession;
        this.uiExceptionHandler = uiExceptionHandler;
        this.welcomeLabel = welcomeLabel;
        this.searchField = searchField;
        this.searchButton = searchButton;
        this.contentTitleLabel = contentTitleLabel;
        this.dataTableView = dataTableView;
        this.totalUsersLabel = totalUsersLabel;
        this.totalCoursesLabel = totalCoursesLabel;
        this.activeEnrollmentsLabel = activeEnrollmentsLabel;
        this.enrollmentsFilterBox = enrollmentsFilterBox;
        this.enrollmentsFilterHintLabel = enrollmentsFilterHintLabel;
        this.enrollmentStudentNameField = enrollmentStudentNameField;
        this.enrollmentCourseCombo = enrollmentCourseCombo;
        this.enrollmentStatusCombo = enrollmentStatusCombo;
    }

    /**
     * Initializes UI bindings and loads the default view.
     */
    public void initialize() {
        try {
            tableManager = new AdminDashboardTableManager(dataTableView);
            dashboardActions = new AdminDashboardActions(
                adminDashboardService,
                courseService,
                alertUtil,
                sceneNavigator,
                uiExceptionHandler,
                this::getStage,
                this::refreshCurrentView
            );
            columnFactory = new AdminDashboardColumnFactory(this::translateEnrollmentStatus);

            setupUserInfo();
            setupSearchListener();
            setupEnrollmentFilterPanel();
            updateStatisticsPanel();

            handleUsersManagement();
        } catch (RuntimeException e) {
            log.error("Error initializing Admin Dashboard", e);
            uiExceptionHandler.handle(e);
        }
    }

    /**
     * Logs out the current user.
     */
    public void handleLogout() {
        attemptOperation(() -> sceneNavigator.performLogout(getStage()));
    }

    /**
     * Opens the profile modal for the current user.
     */
    public void handleProfile() {
        dashboardActions.openModal(UiConstants.FXML_PROFILE_POPUP, UiConstants.WINDOW_TITLE_PROFILE);
    }

    /**
     * Opens the "Add User" modal and refreshes the current view.
     */
    public void handleAddUser() {
        dashboardActions.openModal(UiConstants.FXML_ADD_USER_FORM, UiConstants.WINDOW_TITLE_ADD_USER);
        refreshCurrentView();
    }

    /**
     * Opens the "Add Course" modal and refreshes the current view.
     */
    public void handleAddCourse() {
        dashboardActions.openModal(UiConstants.FXML_ADD_COURSE_FORM, UiConstants.WINDOW_TITLE_ADD_COURSE);
        refreshCurrentView();
    }

    /**
     * Opens the "Enroll Student" modal and refreshes the current view.
     */
    public void handleOpenEnrollStudent() {
        dashboardActions.openModal(UiConstants.FXML_ADMIN_ENROLL_STUDENT_FORM, UiConstants.WINDOW_TITLE_ADMIN_ENROLL_STUDENT);
        refreshCurrentView();
    }

    /**
     * Refreshes the active view and statistics.
     */
    public void handleRefresh() {
        refreshCurrentView();
        updateStatisticsPanel();
    }

    /**
     * Switches to the user management view.
     */
    public void handleUsersManagement() {
        switchView(VIEW_USERS, "Kullanıcı Yönetimi", this::loadUsersIntoTable);
    }

    /**
     * Switches to the course management view.
     */
    public void handleCoursesManagement() {
        switchView(VIEW_COURSES, "Ders Yönetimi", this::loadCoursesIntoTable);
    }

    /**
     * Switches to the enrollments view.
     */
    public void handleEnrollments() {
        switchView(VIEW_ENROLLMENTS, "Kayıtlar", this::loadEnrollmentsIntoTable);
    }

    /**
     * Switches to the attendance reports view.
     */
    public void handleAttendanceReports() {
        switchView(VIEW_ATTENDANCE, "Yoklama Raporları", this::loadAttendanceIntoTable);
    }

    /**
     * Executes the global search for the active view.
     *
     * <p>When the search query is blank, the view resets to its default dataset.</p>
     */
    public void handleSearch() {
        attemptOperation(() -> {
            if (searchField == null || !searchField.isVisible() || suppressSearchEvents) {
                return;
            }
            String query = normalizeString(searchField == null ? null : searchField.getText());
            if (query.isBlank()) {
                refreshCurrentView();
                return;
            }
            performSearch(query);
        });
    }

    /**
     * Clears all enrollment filter inputs and re-applies the enrollments filter.
     */
    public void handleClearEnrollmentFilters() {
        if (!VIEW_ENROLLMENTS.equals(currentViewMode)) {
            return;
        }

        if (enrollmentStudentNameField != null) {
            enrollmentStudentNameField.clear();
        }
        if (enrollmentCourseCombo != null) {
            enrollmentCourseCombo.setValue(null);
        }
        if (enrollmentStatusCombo != null) {
            enrollmentStatusCombo.setValue(null);
        }

        applyEnrollmentFilters();
    }

    private void setupUserInfo() {
        if (welcomeLabel == null) {
            return;
        }

        userSession.getCurrentUser().ifPresentOrElse(
            user -> welcomeLabel.setText("Hoş geldiniz, " + user.fullName()),
            () -> welcomeLabel.setText("Hoş geldiniz, Admin")
        );
    }

    private void setupSearchListener() {
        if (searchField == null) {
            return;
        }
        searchField.textProperty().addListener((obs, oldValue, newValue) -> handleSearch());
    }

    private void setupEnrollmentFilterPanel() {
        AdminEnrollmentFilterPanel.initialize(
            enrollmentsFilterBox,
            enrollmentCourseCombo,
            enrollmentStatusCombo,
            enrollmentStudentNameField,
            List.of(),
            ENROLLMENT_STATUS_CODES,
            this::translateEnrollmentStatus,
            this::applyEnrollmentFilters
        );

        if (enrollmentCourseCombo != null) {
            FxAsync.runAsync(
                courseService::getAllCourseDTOs,
                courses -> enrollmentCourseCombo.setItems(javafx.collections.FXCollections.observableArrayList(courses)),
                uiExceptionHandler::handle
            );
        }
    }

    private void switchView(String viewMode, String title, Runnable loader) {
        this.currentViewMode = viewMode;
        if (contentTitleLabel != null) {
            contentTitleLabel.setText(title);
        }

        boolean isEnrollments = VIEW_ENROLLMENTS.equals(viewMode);
        setEnrollmentsFilterVisible(isEnrollments);

        boolean showGlobalSearch = VIEW_USERS.equals(viewMode)
            || VIEW_COURSES.equals(viewMode)
            || VIEW_ATTENDANCE.equals(viewMode);
        setGlobalSearchVisible(showGlobalSearch);

        if (!showGlobalSearch && searchField != null) {
            suppressSearchEvents = true;
            try {
                searchField.clear();
            } finally {
                suppressSearchEvents = false;
            }
        }

        attemptOperation(loader);
    }

    private void setGlobalSearchVisible(boolean visible) {
        if (searchField != null) {
            searchField.setVisible(visible);
            searchField.setManaged(visible);
        }
        if (searchButton != null) {
            searchButton.setVisible(visible);
            searchButton.setManaged(visible);
        }
    }

    private void setEnrollmentsFilterVisible(boolean visible) {
        if (enrollmentsFilterBox == null) {
            return;
        }
        enrollmentsFilterBox.setVisible(visible);
        enrollmentsFilterBox.setManaged(visible);
    }

    private void refreshCurrentView() {
        switch (currentViewMode) {
            case VIEW_USERS -> loadUsersIntoTable();
            case VIEW_COURSES -> loadCoursesIntoTable();
            case VIEW_ENROLLMENTS -> loadEnrollmentsIntoTable();
            case VIEW_ATTENDANCE -> loadAttendanceIntoTable();
            default -> log.warn("Unknown view mode: {}", currentViewMode);
        }
        updateStatisticsPanel();
    }

    private void updateStatisticsPanel() {
        FxAsync.runAsync(
            adminDashboardService::getStatistics,
            stats -> {
                if (totalUsersLabel != null) {
                    totalUsersLabel.setText(String.valueOf(stats.totalUsers()));
                }
                if (totalCoursesLabel != null) {
                    totalCoursesLabel.setText(String.valueOf(stats.totalCourses()));
                }
                if (activeEnrollmentsLabel != null) {
                    activeEnrollmentsLabel.setText(String.valueOf(stats.activeEnrollments()));
                }
            },
            failure -> log.error("Failed to update statistics", failure)
        );
    }

    private void performSearch(String query) {
        switch (currentViewMode) {
            case VIEW_USERS -> {
                long token = beginTableRequest(VIEW_USERS);
                showTableLoading();
                FxAsync.runAsync(
                    adminDashboardService::getAllUserRows,
                    users -> {
                        if (!isActiveTableRequest(VIEW_USERS, token)) {
                            return;
                        }
                        List<AdminUserRowDTO> filtered = users.stream()
                            .filter(u -> containsIgnoreCase(u.username(), query)
                                || containsIgnoreCase(u.firstName(), query)
                                || containsIgnoreCase(u.lastName(), query)
                                || containsIgnoreCase(u.email(), query))
                            .toList();
                        populateTable(filtered, this::configureUserColumns);
                        configureRowDoubleClickForCurrentView();
                    },
                    failure -> {
                        if (!isActiveTableRequest(VIEW_USERS, token)) {
                            return;
                        }
                        showTableError("Veriler yüklenemedi.");
                        uiExceptionHandler.handle(failure);
                    }
                );
            }
            case VIEW_COURSES -> {
                long token = beginTableRequest(VIEW_COURSES);
                showTableLoading();
                FxAsync.runAsync(
                    courseService::getAllCourseDTOs,
                    courses -> {
                        if (!isActiveTableRequest(VIEW_COURSES, token)) {
                            return;
                        }
                        List<CourseDTO> filtered = courses.stream()
                            .filter(c -> containsIgnoreCase(c.getCode(), query)
                                || containsIgnoreCase(c.getName(), query)
                                || containsIgnoreCase(c.getInstructorName(), query))
                            .toList();
                        populateTable(filtered, this::configureCourseColumns);
                        configureRowDoubleClickForCurrentView();
                    },
                    failure -> {
                        if (!isActiveTableRequest(VIEW_COURSES, token)) {
                            return;
                        }
                        showTableError("Veriler yüklenemedi.");
                        uiExceptionHandler.handle(failure);
                    }
                );
            }
            case VIEW_ATTENDANCE -> {
                long token = beginTableRequest(VIEW_ATTENDANCE);
                showTableLoading();
                FxAsync.runAsync(
                    adminDashboardService::getAllAttendanceRows,
                    rows -> {
                        if (!isActiveTableRequest(VIEW_ATTENDANCE, token)) {
                            return;
                        }

                        List<AdminAttendanceRowDTO> filtered = (rows == null ? List.<AdminAttendanceRowDTO>of() : rows).stream()
                            .filter(r -> containsIgnoreCase(r.studentName(), query)
                                || containsIgnoreCase(r.courseDisplay(), query)
                                || containsIgnoreCase(r.weekNumber() == null ? null : String.valueOf(r.weekNumber()), query)
                                || containsIgnoreCase(r.date() == null ? null : String.valueOf(r.date()), query)
                                || containsIgnoreCase(r.present() == null ? null : (Boolean.TRUE.equals(r.present()) ? "var" : "yok"), query))
                            .toList();

                        if (filtered.isEmpty()) {
                            showTableEmpty("Kayıt bulunamadı.");
                        } else {
                            showTableEmpty("");
                        }

                        populateTable(filtered, this::configureAttendanceColumns);
                        configureRowDoubleClickForCurrentView();
                    },
                    failure -> {
                        if (!isActiveTableRequest(VIEW_ATTENDANCE, token)) {
                            return;
                        }
                        showTableError("Veriler yüklenemedi.");
                        uiExceptionHandler.handle(failure);
                    }
                );
            }
            default -> refreshCurrentView();
        }
    }

    private void loadUsersIntoTable() {
        long token = beginTableRequest(VIEW_USERS);
        showTableLoading();
        FxAsync.runAsync(
            adminDashboardService::getAllUserRows,
            rows -> {
                if (!isActiveTableRequest(VIEW_USERS, token)) {
                    return;
                }
                populateTable(rows, this::configureUserColumns);
                configureRowDoubleClickForCurrentView();
            },
            failure -> {
                if (!isActiveTableRequest(VIEW_USERS, token)) {
                    return;
                }
                showTableError("Veriler yüklenemedi.");
                uiExceptionHandler.handle(failure);
            }
        );
    }

    private void loadCoursesIntoTable() {
        long token = beginTableRequest(VIEW_COURSES);
        showTableLoading();
        FxAsync.runAsync(
            courseService::getAllCourseDTOs,
            rows -> {
                if (!isActiveTableRequest(VIEW_COURSES, token)) {
                    return;
                }
                populateTable(rows, this::configureCourseColumns);
                configureRowDoubleClickForCurrentView();
            },
            failure -> {
                if (!isActiveTableRequest(VIEW_COURSES, token)) {
                    return;
                }
                showTableError("Veriler yüklenemedi.");
                uiExceptionHandler.handle(failure);
            }
        );
    }

    private void loadEnrollmentsIntoTable() {
        beginTableRequest(VIEW_ENROLLMENTS);
        populateTable(List.of(), this::configureEnrollmentColumns);
        applyEnrollmentFilters();
    }

    private void loadAttendanceIntoTable() {
        long token = beginTableRequest(VIEW_ATTENDANCE);
        showTableLoading();
        FxAsync.runAsync(
            adminDashboardService::getAllAttendanceRows,
            rows -> {
                if (!isActiveTableRequest(VIEW_ATTENDANCE, token)) {
                    return;
                }
                populateTable(rows, this::configureAttendanceColumns);
                configureRowDoubleClickForCurrentView();
            },
            failure -> {
                if (!isActiveTableRequest(VIEW_ATTENDANCE, token)) {
                    return;
                }
                showTableError("Veriler yüklenemedi.");
                uiExceptionHandler.handle(failure);
            }
        );
    }

    private <T> void populateTable(List<T> data, Consumer<TableView<T>> columnConfigurator) {
        if (tableManager == null) {
            return;
        }
        tableManager.populateTable(data, columnConfigurator, currentViewMode);
    }

    private void configureUserColumns(TableView<AdminUserRowDTO> table) {
        table.getColumns().add(columnFactory.createColumn("ID", AdminUserRowDTO::id));
        table.getColumns().add(columnFactory.createColumn("Kullanıcı Adı", AdminUserRowDTO::username));
        table.getColumns().add(columnFactory.createColumn("Ad", AdminUserRowDTO::firstName));
        table.getColumns().add(columnFactory.createColumn("Soyad", AdminUserRowDTO::lastName));

        TableColumn<AdminUserRowDTO, String> roleCol = new TableColumn<>("Rol");
        roleCol.setCellValueFactory(cell -> new SimpleStringProperty(translateRole(cell.getValue().role())));
        roleCol.setMinWidth(140);
        roleCol.setPrefWidth(170);
        table.getColumns().add(roleCol);

        table.getColumns().add(columnFactory.createColumn("E-posta", AdminUserRowDTO::email));
        table.getColumns().add(columnFactory.createActionColumn(
            ACTION_TYPE_USER,
            user -> dashboardActions.handleEdit(user, ACTION_TYPE_USER),
            user -> dashboardActions.handleDelete(user, ACTION_TYPE_USER)
        ));
    }

    private void configureCourseColumns(TableView<CourseDTO> table) {
        table.getColumns().add(columnFactory.createColumn("ID", CourseDTO::getId));
        table.getColumns().add(columnFactory.createColumn("Ders Kodu", CourseDTO::getCode));
        table.getColumns().add(columnFactory.createColumn("Ders Adı", CourseDTO::getName));
        table.getColumns().add(columnFactory.createColumn("Kredi", CourseDTO::getCredit));
        table.getColumns().add(columnFactory.createColumn("Kota", CourseDTO::getQuota));
        table.getColumns().add(columnFactory.createColumn("Akademisyen", CourseDTO::getInstructorName));

        TableColumn<CourseDTO, String> activeCol = new TableColumn<>("Durum");
        activeCol.setCellValueFactory(cell ->
            new SimpleStringProperty(Boolean.TRUE.equals(cell.getValue().getActive()) ? "Aktif" : "Pasif"));
        activeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || value.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(value);
                badge.getStyleClass().addAll("badge", "Aktif".equalsIgnoreCase(value) ? "badge-success" : "badge-neutral");
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });
        activeCol.setMinWidth(120);
        activeCol.setPrefWidth(140);
        table.getColumns().add(activeCol);

        table.getColumns().add(columnFactory.createActionColumn(
            ACTION_TYPE_COURSE,
            course -> dashboardActions.handleEdit(course, ACTION_TYPE_COURSE),
            course -> dashboardActions.handleDelete(course, ACTION_TYPE_COURSE)
        ));
    }

    private void configureEnrollmentColumns(TableView<AdminEnrollmentRowDTO> table) {
        table.getColumns().add(columnFactory.createColumn("ID", AdminEnrollmentRowDTO::id));
        table.getColumns().add(columnFactory.createColumn("Öğrenci", AdminEnrollmentRowDTO::studentName));
        table.getColumns().add(columnFactory.createColumn("Ders", AdminEnrollmentRowDTO::courseDisplay));
        TableColumn<AdminEnrollmentRowDTO, String> statusCol = new TableColumn<>("Durum");
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(translateEnrollmentStatus(cell.getValue().status())));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || value.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String badgeClass;
                EnrollmentStatus status = getTableRow() == null || getTableRow().getItem() == null
                    ? null
                    : getTableRow().getItem().status();

                if (status == EnrollmentStatus.ACTIVE || status == EnrollmentStatus.REGISTERED) {
                    badgeClass = "badge-success";
                } else if (status == EnrollmentStatus.ENROLLED) {
                    badgeClass = "badge-info";
                } else if (status == EnrollmentStatus.DROPPED) {
                    badgeClass = "badge-warning";
                } else if (status == EnrollmentStatus.CANCELLED) {
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
        statusCol.setMinWidth(140);
        statusCol.setPrefWidth(160);
        table.getColumns().add(statusCol);

        TableColumn<AdminEnrollmentRowDTO, String> dateCol = new TableColumn<>("Kayıt Tarihi");
        dateCol.setCellValueFactory(cell -> {
            var date = cell.getValue().enrollmentDate();
            return new SimpleStringProperty(date != null ? DATE_TIME_FORMATTER.format(date) : "-");
        });
        dateCol.setMinWidth(180);
        dateCol.setPrefWidth(210);
        table.getColumns().add(dateCol);

        table.getColumns().add(columnFactory.createActionColumn(
            ACTION_TYPE_ENROLLMENT,
            enrollment -> dashboardActions.handleEdit(enrollment, ACTION_TYPE_ENROLLMENT),
            enrollment -> dashboardActions.handleDelete(enrollment, ACTION_TYPE_ENROLLMENT)
        ));
    }

    private void configureAttendanceColumns(TableView<AdminAttendanceRowDTO> table) {
        table.getColumns().add(columnFactory.createColumn("ID", AdminAttendanceRowDTO::id));
        table.getColumns().add(columnFactory.createColumn("Öğrenci", AdminAttendanceRowDTO::studentName));
        table.getColumns().add(columnFactory.createColumn("Ders", AdminAttendanceRowDTO::courseDisplay));
        table.getColumns().add(columnFactory.createColumn("Hafta", AdminAttendanceRowDTO::weekNumber));

        TableColumn<AdminAttendanceRowDTO, String> presentCol = new TableColumn<>("Katılım");
        presentCol.setCellValueFactory(cell -> {
            Boolean p = cell.getValue().present();
            return new SimpleStringProperty(p == null ? "-" : (p ? "Var" : "Yok"));
        });
        presentCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || value.isBlank() || "-".equals(value)) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                String badgeClass = "Var".equalsIgnoreCase(value) ? "badge-success" : "badge-danger";
                Label badge = new Label(value);
                badge.getStyleClass().addAll("badge", badgeClass);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });
        presentCol.setMinWidth(140);
        presentCol.setPrefWidth(160);
        table.getColumns().add(presentCol);

        table.getColumns().add(columnFactory.createColumn("Tarih", AdminAttendanceRowDTO::date));
    }

    private void applyEnrollmentFilters() {
        if (!VIEW_ENROLLMENTS.equals(currentViewMode)) {
            return;
        }

        long token = beginTableRequest(VIEW_ENROLLMENTS);

        String studentQueryRaw = enrollmentStudentNameField == null ? null : enrollmentStudentNameField.getText();
        String studentQuery = normalizeString(studentQueryRaw);
        CourseDTO selectedCourse = enrollmentCourseCombo == null ? null : enrollmentCourseCombo.getValue();
        Long selectedCourseId = selectedCourse == null ? null : selectedCourse.getId();
        String statusCode = enrollmentStatusCombo == null ? null : enrollmentStatusCombo.getValue();
        EnrollmentStatus selectedStatus = parseEnrollmentStatus(statusCode);

        boolean hasAnyFilter = AdminEnrollmentFilterPanel.hasAnyFilter(studentQuery, selectedCourse, statusCode);

        if (enrollmentsFilterHintLabel != null) {
            enrollmentsFilterHintLabel.setVisible(!hasAnyFilter);
            enrollmentsFilterHintLabel.setManaged(!hasAnyFilter);
        }

        if (!hasAnyFilter) {
            showTableEmpty("");
            populateTable(List.of(), this::configureEnrollmentColumns);
            configureRowDoubleClickForCurrentView();
            return;
        }

        showTableLoading();
        populateTable(List.of(), this::configureEnrollmentColumns);

        FxAsync.runAsync(
            () -> adminDashboardService.searchEnrollmentRows(studentQuery, selectedCourseId, selectedStatus),
            rows -> {
                if (!isActiveTableRequest(VIEW_ENROLLMENTS, token)) {
                    return;
                }
                if (rows == null || rows.isEmpty()) {
                    showTableEmpty("Kayıt bulunamadı.");
                } else {
                    showTableEmpty("");
                }
                populateTable(rows, this::configureEnrollmentColumns);
                configureRowDoubleClickForCurrentView();
            },
            failure -> {
                if (!isActiveTableRequest(VIEW_ENROLLMENTS, token)) {
                    return;
                }
                showTableError("Veriler yüklenemedi.");
                uiExceptionHandler.handle(failure);
            }
        );
    }

    private void showTableEmpty(String message) {
        setTablePlaceholder(message == null ? "" : message);
        if (tableManager != null) {
            tableManager.clearRowFactory();
        }
    }

    private EnrollmentStatus parseEnrollmentStatus(String statusCode) {
        if (statusCode == null || statusCode.isBlank()) {
            return null;
        }
        try {
            return EnrollmentStatus.valueOf(statusCode.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private long beginTableRequest(String expectedViewMode) {
        activeTableRequestViewMode = expectedViewMode;
        activeTableRequestToken = ++tableRequestSequence;
        return activeTableRequestToken;
    }

    private boolean isActiveTableRequest(String expectedViewMode, long token) {
        return token == activeTableRequestToken
            && expectedViewMode != null
            && expectedViewMode.equals(activeTableRequestViewMode)
            && expectedViewMode.equals(currentViewMode);
    }

    private void showTableLoading() {
        setTablePlaceholder("Yükleniyor...");
        if (tableManager != null) {
            tableManager.clearRowFactory();
        }
    }

    private void showTableError(String message) {
        setTablePlaceholder(message == null || message.isBlank() ? "Veriler yüklenemedi." : message);
    }

    private void setTablePlaceholder(String text) {
        if (dataTableView == null) {
            return;
        }
        Label label = new Label(text == null ? "" : text);
        label.setWrapText(true);
        dataTableView.setPlaceholder(label);
    }

    private void configureRowDoubleClickForCurrentView() {
        switch (currentViewMode) {
            case VIEW_USERS -> AdminDashboardRowHandlers.configureEditOnDoubleClick(dataTableView, ACTION_TYPE_USER, (item, type) -> dashboardActions.handleEdit(item, type));
            case VIEW_COURSES -> AdminDashboardRowHandlers.configureEditOnDoubleClick(dataTableView, ACTION_TYPE_COURSE, (item, type) -> dashboardActions.handleEdit(item, type));
            case VIEW_ENROLLMENTS -> AdminDashboardRowHandlers.configureEditOnDoubleClick(dataTableView, ACTION_TYPE_ENROLLMENT, (item, type) -> dashboardActions.handleEdit(item, type));
            case VIEW_ATTENDANCE -> AdminDashboardRowHandlers.configureAttendanceDetailsPopup(dataTableView, this::showAttendanceDetails);
            default -> dataTableView.setRowFactory(null);
        }
    }

    private void showAttendanceDetails(AdminAttendanceRowDTO attendanceRow) {
        if (attendanceRow == null) {
            return;
        }
        alertUtil.showInformationAlert("Detay", "Öğrenci: " + attendanceRow.studentName() + "\nDers: " + attendanceRow.courseDisplay());
    }

    private String translateRole(RoleDTO role) {
        if (role == null) return "";
        return switch (role) {
            case ADMIN -> "Yönetici";
            case INSTRUCTOR -> "Akademisyen";
            case STUDENT -> "Öğrenci";
            default -> "Diğer";
        };
    }

    private String normalizeString(String val) {
        return val == null ? "" : val.trim().toLowerCase();
    }

    private boolean containsIgnoreCase(String source, String target) {
        return normalizeString(source).contains(target);
    }

    private Stage getStage() {
        return dataTableView == null || dataTableView.getScene() == null
            ? null
            : (Stage) dataTableView.getScene().getWindow();
    }

    private void attemptOperation(Runnable operation) {
        try {
            operation.run();
            configureRowDoubleClickForCurrentView();
        } catch (RuntimeException e) {
            log.error("Operation failed", e);
            uiExceptionHandler.handle(e);
        }
    }

    private String translateEnrollmentStatus(EnrollmentStatus status) {
        return EnrollmentStatusUiMapper.toTurkish(status);
    }

    private String translateEnrollmentStatus(String statusCode) {
        return EnrollmentStatusUiMapper.toTurkish(statusCode);
    }
}
