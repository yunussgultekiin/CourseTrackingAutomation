package org.example.coursetrackingautomation.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.dto.*;
import org.example.coursetrackingautomation.service.AdminDashboardService;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDashboardController {

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
    private String currentViewMode = VIEW_USERS;
    private String tableConfiguredForViewMode;
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
    public void initialize() {
        try {
            setupUserInfo();
            setupSearchListener();
            setupEnrollmentFilterPanel();
            updateStatisticsPanel();

            handleUsersManagement();
        } catch (Exception e) {
            log.error("Error initializing Admin Dashboard", e);
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleLogout() {
        attemptOperation(() -> {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            sceneNavigator.performLogout(stage);
        });
    }

    @FXML
    public void handleProfile() {
        openModalWindow(UiConstants.FXML_PROFILE_POPUP, UiConstants.WINDOW_TITLE_PROFILE);
    }

    @FXML
    public void handleAddUser() {
        openModalWindow(UiConstants.FXML_ADD_USER_FORM, UiConstants.WINDOW_TITLE_ADD_USER);
        refreshCurrentView();
    }

    @FXML
    public void handleAddCourse() {
        openModalWindow(UiConstants.FXML_ADD_COURSE_FORM, UiConstants.WINDOW_TITLE_ADD_COURSE);
        refreshCurrentView();
    }

    @FXML
    public void handleOpenEnrollStudent() {
        openModalWindow(UiConstants.FXML_ADMIN_ENROLL_STUDENT_FORM, UiConstants.WINDOW_TITLE_ADMIN_ENROLL_STUDENT);
        refreshCurrentView();
    }

    @FXML
    public void handleRefresh() {
        refreshCurrentView();
        updateStatisticsPanel();
    }

    @FXML
    public void handleUsersManagement() {
        switchView(VIEW_USERS, "Kullanıcı Yönetimi", this::loadUsersIntoTable);
    }

    @FXML
    public void handleCoursesManagement() {
        switchView(VIEW_COURSES, "Ders Yönetimi", this::loadCoursesIntoTable);
    }

    @FXML
    public void handleEnrollments() {
        switchView(VIEW_ENROLLMENTS, "Kayıtlar", this::loadEnrollmentsIntoTable);
    }

    @FXML
    public void handleAttendanceReports() {
        switchView(VIEW_ATTENDANCE, "Yoklama Raporları", this::loadAttendanceIntoTable);
    }

    @FXML
    public void handleSearch() {
        attemptOperation(() -> {
            String query = normalizeString(searchField.getText());
            if (query.isBlank()) {
                refreshCurrentView();
                return;
            }
            performSearch(query);
        });
    }

    @FXML
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
        userSession.getCurrentUser().ifPresentOrElse(
                user -> welcomeLabel.setText("Hoş geldiniz, " + user.fullName()),
                () -> welcomeLabel.setText("Hoş geldiniz, Admin")
        );
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> handleSearch());
    }

    private void setupEnrollmentFilterPanel() {
        if (enrollmentsFilterBox != null) {
            enrollmentsFilterBox.setVisible(false);
            enrollmentsFilterBox.setManaged(false);
        }

        if (enrollmentCourseCombo != null) {
            enrollmentCourseCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(CourseDTO course) {
                    if (course == null) return "";
                    String code = course.getCode() == null ? "" : course.getCode();
                    String name = course.getName() == null ? "" : course.getName();
                    return (code + " - " + name).trim();
                }

                @Override
                public CourseDTO fromString(String string) {
                    return null;
                }
            });

            enrollmentCourseCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(CourseDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(enrollmentCourseCombo.getPromptText());
                    } else {
                        setText(enrollmentCourseCombo.getConverter().toString(item));
                    }
                }
            });

            enrollmentCourseCombo.setItems(FXCollections.observableArrayList(courseService.getAllCourseDTOs()));
        }

        if (enrollmentStatusCombo != null) {
            enrollmentStatusCombo.setItems(FXCollections.observableArrayList(ENROLLMENT_STATUS_CODES));
            enrollmentStatusCombo.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : translateEnrollmentStatus(item));
                }
            });
            enrollmentStatusCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(enrollmentStatusCombo.getPromptText());
                    } else {
                        setText(translateEnrollmentStatus(item));
                    }
                }
            });
        }

        if (enrollmentStudentNameField != null) {
            enrollmentStudentNameField.textProperty().addListener((obs, o, n) -> applyEnrollmentFilters());
        }
        if (enrollmentCourseCombo != null) {
            enrollmentCourseCombo.valueProperty().addListener((obs, o, n) -> applyEnrollmentFilters());
        }
        if (enrollmentStatusCombo != null) {
            enrollmentStatusCombo.valueProperty().addListener((obs, o, n) -> applyEnrollmentFilters());
        }
    }

    private void switchView(String viewMode, String title, Runnable loader) {
        this.currentViewMode = viewMode;
        contentTitleLabel.setText(title);

        boolean isEnrollments = VIEW_ENROLLMENTS.equals(viewMode);
        setEnrollmentsFilterVisible(isEnrollments);
        setGlobalSearchVisible(!isEnrollments);

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
        try {
            var stats = adminDashboardService.getStatistics();
            totalUsersLabel.setText(String.valueOf(stats.totalUsers()));
            totalCoursesLabel.setText(String.valueOf(stats.totalCourses()));
            activeEnrollmentsLabel.setText(String.valueOf(stats.activeEnrollments()));
        } catch (Exception e) {
            log.error("Failed to update statistics", e);
        }
    }

    private void performSearch(String query) {
        switch (currentViewMode) {
            case VIEW_USERS -> {
                List<AdminUserRowDTO> filtered = adminDashboardService.getAllUserRows().stream()
                        .filter(u -> containsIgnoreCase(u.username(), query) ||
                                containsIgnoreCase(u.firstName(), query) ||
                                containsIgnoreCase(u.lastName(), query) ||
                                containsIgnoreCase(u.email(), query))
                        .toList();
                populateTable(filtered, this::configureUserColumns);
            }
            case VIEW_COURSES -> {
                List<CourseDTO> filtered = courseService.getAllCourseDTOs().stream()
                        .filter(c -> containsIgnoreCase(c.getCode(), query) ||
                                containsIgnoreCase(c.getName(), query) ||
                                containsIgnoreCase(c.getInstructorName(), query))
                        .toList();
                populateTable(filtered, this::configureCourseColumns);
            }
            default -> refreshCurrentView();
        }
    }

    private void loadUsersIntoTable() {
        populateTable(adminDashboardService.getAllUserRows(), this::configureUserColumns);
    }

    private void loadCoursesIntoTable() {
        populateTable(courseService.getAllCourseDTOs(), this::configureCourseColumns);
    }

    private void loadEnrollmentsIntoTable() {
        populateTable(List.of(), this::configureEnrollmentColumns);
        applyEnrollmentFilters();
    }

    private void loadAttendanceIntoTable() {
        populateTable(adminDashboardService.getAllAttendanceRows(), this::configureAttendanceColumns);
    }

    private <T> void populateTable(List<T> data, Consumer<TableView<T>> columnConfigurator) {
        dataTableView.getItems().clear();
        dataTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        @SuppressWarnings("unchecked")
        TableView<T> specificTable = (TableView<T>) dataTableView;

        if (tableConfiguredForViewMode == null || !tableConfiguredForViewMode.equals(currentViewMode)) {
            specificTable.getColumns().clear();
            columnConfigurator.accept(specificTable);
            tableConfiguredForViewMode = currentViewMode;
        }
        specificTable.setItems(FXCollections.observableArrayList(data));
    }

    private void configureUserColumns(TableView<AdminUserRowDTO> table) {
        table.getColumns().add(createColumn("ID", AdminUserRowDTO::id));
        table.getColumns().add(createColumn("Kullanıcı Adı", AdminUserRowDTO::username));
        table.getColumns().add(createColumn("Ad", AdminUserRowDTO::firstName));
        table.getColumns().add(createColumn("Soyad", AdminUserRowDTO::lastName));

        TableColumn<AdminUserRowDTO, String> roleCol = new TableColumn<>("Rol");
        roleCol.setCellValueFactory(cell -> new SimpleStringProperty(translateRole(cell.getValue().role())));
        roleCol.setMinWidth(140);
        roleCol.setPrefWidth(170);
        table.getColumns().add(roleCol);

        table.getColumns().add(createColumn("E-posta", AdminUserRowDTO::email));
        table.getColumns().add(createActionColumn(ACTION_TYPE_USER));
    }

    private void configureCourseColumns(TableView<CourseDTO> table) {
        table.getColumns().add(createColumn("ID", CourseDTO::getId));
        table.getColumns().add(createColumn("Ders Kodu", CourseDTO::getCode));
        table.getColumns().add(createColumn("Ders Adı", CourseDTO::getName));
        table.getColumns().add(createColumn("Kredi", CourseDTO::getCredit));
        table.getColumns().add(createColumn("Kota", CourseDTO::getQuota));
        table.getColumns().add(createColumn("Akademisyen", CourseDTO::getInstructorName));

        TableColumn<CourseDTO, String> activeCol = new TableColumn<>("Durum");
        activeCol.setCellValueFactory(cell ->
            new SimpleStringProperty(Boolean.TRUE.equals(cell.getValue().getActive()) ? "Aktif" : "Pasif"));
        activeCol.setMinWidth(120);
        activeCol.setPrefWidth(140);
        table.getColumns().add(activeCol);

        table.getColumns().add(createActionColumn(ACTION_TYPE_COURSE));
    }

    private void configureEnrollmentColumns(TableView<AdminEnrollmentRowDTO> table) {
        table.getColumns().add(createColumn("ID", AdminEnrollmentRowDTO::id));
        table.getColumns().add(createColumn("Öğrenci", AdminEnrollmentRowDTO::studentName));
        table.getColumns().add(createColumn("Ders", AdminEnrollmentRowDTO::courseDisplay));
        TableColumn<AdminEnrollmentRowDTO, String> statusCol = new TableColumn<>("Durum");
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(translateEnrollmentStatus(cell.getValue().status())));
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

        table.getColumns().add(createActionColumn(ACTION_TYPE_ENROLLMENT));
    }

    private void configureAttendanceColumns(TableView<AdminAttendanceRowDTO> table) {
        table.getColumns().add(createColumn("ID", AdminAttendanceRowDTO::id));
        table.getColumns().add(createColumn("Öğrenci", AdminAttendanceRowDTO::studentName));
        table.getColumns().add(createColumn("Ders", AdminAttendanceRowDTO::courseDisplay));
        table.getColumns().add(createColumn("Hafta", AdminAttendanceRowDTO::weekNumber));

        TableColumn<AdminAttendanceRowDTO, String> presentCol = new TableColumn<>("Katılım");
        presentCol.setCellValueFactory(cell -> {
            Boolean p = cell.getValue().present();
            return new SimpleStringProperty(p == null ? "-" : (p ? "Var" : "Yok"));
        });
        presentCol.setMinWidth(140);
        presentCol.setPrefWidth(160);
        table.getColumns().add(presentCol);

        table.getColumns().add(createColumn("Tarih", AdminAttendanceRowDTO::date));
    }

    private <S, T> TableColumn<S, T> createColumn(String title, java.util.function.Function<S, T> mapper) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(mapper.apply(cellData.getValue())));

        if ("ID".equals(title)) {
            column.setMinWidth(60);
            column.setPrefWidth(80);
            column.setMaxWidth(100);
        } else {
            column.setMinWidth(110);
            column.setPrefWidth(180);
        }
        return column;
    }

    private <T> TableColumn<T, String> createActionColumn(String actionType) {
        TableColumn<T, String> col = new TableColumn<>("İşlemler");
        col.setCellFactory(createActionCellFactory(actionType));
        col.setMinWidth(170);
        col.setPrefWidth(190);
        col.setMaxWidth(220);
        col.setResizable(false);
        return col;
    }

    private <T> Callback<TableColumn<T, String>, TableCell<T, String>> createActionCellFactory(String type) {
        return column -> new TableCell<>() {
            private final Button editButton = new Button("Düzenle");
            private final Button deleteButton = new Button("Sil");
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("btn-action-edit");
                deleteButton.getStyleClass().add("btn-action-delete");

                editButton.setOnAction(event -> {
                    T item = getTableRow() == null ? null : getTableRow().getItem();
                    if (item != null) {
                        handleEditAction(item, type);
                    }
                });
                deleteButton.setOnAction(event -> {
                    T item = getTableRow() == null ? null : getTableRow().getItem();
                    if (item != null) {
                        handleDeleteAction(item, type);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
    }

    private <T> void handleEditAction(T item, String type) {
        attemptOperation(() -> {
            if (item == null) {
                return;
            }

            switch (type) {
                case ACTION_TYPE_USER -> {
                    Long id = ((AdminUserRowDTO) item).id();
                    if (id == null) {
                        return;
                    }
                    openEditDialog(UiConstants.FXML_EDIT_USER_FORM, UiConstants.WINDOW_TITLE_EDIT_USER,
                            (EditUserFormController c) -> c.setUserId(id));
                }

                case ACTION_TYPE_COURSE -> {
                    Long id = ((CourseDTO) item).getId();
                    if (id == null) {
                        return;
                    }
                    openEditDialog(UiConstants.FXML_EDIT_COURSE_FORM, UiConstants.WINDOW_TITLE_EDIT_COURSE,
                            (EditCourseFormController c) -> c.setCourseId(id));
                }

                case ACTION_TYPE_ENROLLMENT -> {
                    Long id = ((AdminEnrollmentRowDTO) item).id();
                    if (id == null) {
                        return;
                    }
                    openEditDialog(UiConstants.FXML_EDIT_ENROLLMENT_FORM, UiConstants.WINDOW_TITLE_EDIT_ENROLLMENT,
                            (EditEnrollmentFormController c) -> c.setEnrollmentId(id));
                }
            }
            Platform.runLater(this::refreshCurrentView);
        });
    }

    private <T> void handleDeleteAction(T item, String type) {
        attemptOperation(() -> {
            String confirmMsg = getDeleteConfirmationMessage(item, type);
            if (alertUtil.showConfirmationAlert("Emin misiniz?", confirmMsg)) {
                performDelete(item, type);
            }
        });
    }

    private <T> void performDelete(T item, String type) {
        switch (type) {
            case ACTION_TYPE_USER -> {
                adminDashboardService.deleteUser(((AdminUserRowDTO) item).id());
                alertUtil.showInformationAlert("Başarılı", "Kullanıcı silindi.");
            }
            case ACTION_TYPE_COURSE -> {
                courseService.deactivateCourse(((CourseDTO) item).getId());
                alertUtil.showInformationAlert("Başarılı", "Ders pasif yapıldı.");
            }
            case ACTION_TYPE_ENROLLMENT -> {
                var enrollment = (AdminEnrollmentRowDTO) item;
                adminDashboardService.dropEnrollment(enrollment.studentId(), enrollment.courseId());
                alertUtil.showInformationAlert("Başarılı", "Kayıt silindi.");
            }
        }
    }

    private String getDeleteConfirmationMessage(Object item, String type) {
        return switch (type) {
            case ACTION_TYPE_USER -> "'" + ((AdminUserRowDTO) item).username() + "' kullanıcısı silinecek.";
            case ACTION_TYPE_COURSE -> "Bu dersi Pasif yapmak üzeresiniz. Pasif hale getirilen ders yeni öğrenci kabul etmez ve öğrenciler tarafından seçilemez. Devam etmek istiyor musunuz?";
            case ACTION_TYPE_ENROLLMENT -> "Kayıt silinecek.";
            default -> "Bu öğe silinecek.";
        };
    }

    private void applyEnrollmentFilters() {
        if (!VIEW_ENROLLMENTS.equals(currentViewMode)) {
            return;
        }

        String studentQuery = normalizeString(enrollmentStudentNameField == null ? null : enrollmentStudentNameField.getText());
        CourseDTO selectedCourse = enrollmentCourseCombo == null ? null : enrollmentCourseCombo.getValue();
        String statusCode = enrollmentStatusCombo == null ? null : enrollmentStatusCombo.getValue();

        boolean hasAnyFilter = (studentQuery != null && !studentQuery.isBlank())
            || (selectedCourse != null && selectedCourse.getId() != null)
            || (statusCode != null && !statusCode.isBlank());

        if (enrollmentsFilterHintLabel != null) {
            enrollmentsFilterHintLabel.setVisible(!hasAnyFilter);
            enrollmentsFilterHintLabel.setManaged(!hasAnyFilter);
        }

        if (!hasAnyFilter) {
            populateTable(List.of(), this::configureEnrollmentColumns);
            configureRowDoubleClickForCurrentView();
            return;
        }

        List<AdminEnrollmentRowDTO> filtered = adminDashboardService.getAllEnrollmentRows().stream()
            .filter(e -> studentQuery == null || studentQuery.isBlank() || containsIgnoreCase(e.studentName(), studentQuery))
            .filter(e -> selectedCourse == null || selectedCourse.getId() == null || selectedCourse.getId().equals(e.courseId()))
            .filter(e -> statusCode == null || statusCode.isBlank() || normalizeString(e.status()).equals(normalizeString(statusCode)))
            .toList();

        populateTable(filtered, this::configureEnrollmentColumns);
        configureRowDoubleClickForCurrentView();
    }

    private void configureRowDoubleClickForCurrentView() {
        switch (currentViewMode) {
            case VIEW_USERS -> configureRowDoubleClick(ACTION_TYPE_USER);
            case VIEW_COURSES -> configureRowDoubleClick(ACTION_TYPE_COURSE);
            case VIEW_ENROLLMENTS -> configureRowDoubleClick(ACTION_TYPE_ENROLLMENT);
            case VIEW_ATTENDANCE -> configureRowDetailsPopup();
            default -> dataTableView.setRowFactory(null);
        }
    }

    private void configureRowDoubleClick(String actionType) {
        dataTableView.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Object item = row.getItem();
                    if (ACTION_TYPE_USER.equals(actionType) && item instanceof AdminUserRowDTO userRow) {
                        handleEditAction(userRow, ACTION_TYPE_USER);
                    } else if (ACTION_TYPE_COURSE.equals(actionType) && item instanceof CourseDTO courseRow) {
                        handleEditAction(courseRow, ACTION_TYPE_COURSE);
                    } else if (ACTION_TYPE_ENROLLMENT.equals(actionType) && item instanceof AdminEnrollmentRowDTO enrollmentRow) {
                        handleEditAction(enrollmentRow, ACTION_TYPE_ENROLLMENT);
                    }
                }
            });
            return row;
        });
    }

    private void configureRowDetailsPopup() {
        dataTableView.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Object item = row.getItem();
                    if (item instanceof AdminAttendanceRowDTO attendanceRow) {
                        alertUtil.showInformationAlert("Detay", "Öğrenci: " + attendanceRow.studentName() + "\nDers: " + attendanceRow.courseDisplay());
                    }
                }
            });
            return row;
        });
    }

    private String translateRole(RoleDTO role) {
        if (role == null) return "";
        return switch (role) {
            case ADMIN -> "Yönetici";
            case INSTRUCTOR -> "Akademisyen";
            case STUDENT -> "Öğrenci";
            default -> role.name();
        };
    }

    private String normalizeString(String val) {
        return val == null ? "" : val.trim().toLowerCase();
    }

    private boolean containsIgnoreCase(String source, String target) {
        return normalizeString(source).contains(target);
    }

    private void openModalWindow(String fxmlPath, String title) {
        sceneNavigator.openModal(fxmlPath, title, dataTableView.getScene().getWindow());
    }

    private <C> void openEditDialog(String fxmlPath, String title, Consumer<C> controllerConfigurator) {
        sceneNavigator.openModalWithController(
            fxmlPath,
            title,
            dataTableView.getScene().getWindow(),
            controllerConfigurator
        );
    }

    private void attemptOperation(Runnable operation) {
        try {
            operation.run();
            configureRowDoubleClickForCurrentView();
        } catch (Exception e) {
            log.error("Operation failed", e);
            uiExceptionHandler.handle(e);
        }
    }

    private String translateEnrollmentStatus(String statusCode) {
        if (statusCode == null) return "";
        return switch (statusCode.trim().toUpperCase()) {
            case "ACTIVE" -> "Aktif";
            case "ENROLLED" -> "Kayıtlı";
            case "REGISTERED" -> "Kesin Kayıt";
            case "DROPPED" -> "Bıraktı";
            case "CANCELLED" -> "İptal";
            default -> statusCode;
        };
    }
}