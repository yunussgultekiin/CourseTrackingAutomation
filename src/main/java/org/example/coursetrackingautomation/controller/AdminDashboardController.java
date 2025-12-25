package org.example.coursetrackingautomation.controller;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.config.UserSession;
import org.example.coursetrackingautomation.dto.AdminAttendanceRowDTO;
import org.example.coursetrackingautomation.dto.AdminEnrollmentRowDTO;
import org.example.coursetrackingautomation.dto.AdminKeyValueRowDTO;
import org.example.coursetrackingautomation.dto.AdminUserRowDTO;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.entity.Role;
import org.example.coursetrackingautomation.service.AdminDashboardService;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.coursetrackingautomation.ui.UiConstants;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDashboardController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AdminDashboardService adminDashboardService;
    private final CourseService courseService;
    private final AlertUtil alertUtil;
    private final ApplicationContext applicationContext;
    private final SceneNavigator sceneNavigator;
    private final UserSession userSession;
    private final UiExceptionHandler uiExceptionHandler;

    private String currentView = "users"; 

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button coursesButton;

    @FXML
    private Button enrollmentsButton;

    @FXML
    private Button attendanceButton;

    @FXML
    private Button statisticsButton;

    @FXML
    private Button addUserButton;

    @FXML
    private Button addCourseButton;

    @FXML
    private Button enrollStudentButton;

    @FXML
    private Button refreshButton;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Label contentTitleLabel;

    @FXML
    @SuppressWarnings("rawtypes")
    private TableView dataTableView;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label totalCoursesLabel;

    @FXML
    private Label activeEnrollmentsLabel;

    @FXML
    public void initialize() {
        try {
            userSession.getCurrentUser().ifPresent(u -> welcomeLabel.setText("Hoş geldiniz, " + u.fullName()));
            updateStatistics();

            // Live search: filter as user types; reset view automatically when cleared.
            searchField.textProperty().addListener((obs, oldValue, newValue) -> handleSearch());

            handleUsersManagement();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            sceneNavigator.performLogout(stage);
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile_popup.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Profilim");
            dialog.initOwner(profileButton.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.sizeToScene();
            dialog.centerOnScreen();
            dialog.showAndWait();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(UiConstants.FXML_ADD_USER_FORM));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle(UiConstants.WINDOW_TITLE_ADD_USER);
            dialog.initOwner(addUserButton.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.sizeToScene();
            dialog.centerOnScreen();
            dialog.showAndWait();

            refreshCurrentView();
            updateStatistics();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleAddCourse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(UiConstants.FXML_ADD_COURSE_FORM));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle(UiConstants.WINDOW_TITLE_ADD_COURSE);
            dialog.initOwner(addCourseButton.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.sizeToScene();
            dialog.centerOnScreen();
            dialog.showAndWait();

            refreshCurrentView();
            updateStatistics();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleUsersManagement() {
        currentView = "users";
        contentTitleLabel.setText("Kullanıcı Yönetimi");
        loadUsersIntoTable();
    }

    @FXML
    public void handleCoursesManagement() {
        currentView = "courses";
        contentTitleLabel.setText("Ders Yönetimi");
        loadCoursesIntoTable();
    }

    @FXML
    public void handleEnrollments() {
        currentView = "enrollments";
        contentTitleLabel.setText("Kayıtlar");
        loadEnrollmentsIntoTable();
    }

    @FXML
    public void handleAttendanceReports() {
        currentView = "attendance";
        contentTitleLabel.setText("Yoklama Raporları");
        loadAttendanceIntoTable();
    }

    @FXML
    public void handleStatistics() {
        currentView = "statistics";
        contentTitleLabel.setText("İstatistikler");
        loadStatisticsIntoTable();
    }

    @FXML
    public void handleOpenEnrollStudent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(UiConstants.FXML_ADMIN_ENROLL_STUDENT_FORM));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle(UiConstants.WINDOW_TITLE_ADMIN_ENROLL_STUDENT);
            dialog.initOwner(enrollStudentButton.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.sizeToScene();
            dialog.centerOnScreen();
            dialog.showAndWait();

            refreshCurrentView();
            updateStatistics();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleRefresh() {
        refreshCurrentView();
        updateStatistics();
    }

    @FXML
    public void handleSearch() {
        try {
            String needle = normalize(searchField.getText());
            if (needle.isBlank()) {
                refreshCurrentView();
                return;
            }

            switch (currentView) {
                case "users" -> {
                    List<AdminUserRowDTO> users = adminDashboardService.getAllUserRows();
                    List<AdminUserRowDTO> filtered = users.stream()
                        .filter(u -> normalize(u.username()).contains(needle)
                            || normalize(u.firstName()).contains(needle)
                            || normalize(u.lastName()).contains(needle)
                            || normalize(u.email()).contains(needle)
                            || normalize(u.role() == null ? "" : u.role().name()).contains(needle))
                        .toList();
                    loadUsersIntoTable(filtered);
                }
                case "courses" -> {
                    List<CourseDTO> courses = courseService.getAllCourseDTOs();
                    List<CourseDTO> filtered = courses.stream()
                        .filter(c -> normalize(c.getCode()).contains(needle)
                            || normalize(c.getName()).contains(needle)
                            || normalize(c.getInstructorName()).contains(needle))
                        .toList();
                    loadCoursesIntoTable(filtered);
                }
                case "enrollments" -> {
                    List<AdminEnrollmentRowDTO> enrollments = adminDashboardService.getAllEnrollmentRows();
                    List<AdminEnrollmentRowDTO> filtered = enrollments.stream()
                        .filter(e -> normalize(e.studentName()).contains(needle)
                            || normalize(e.courseDisplay()).contains(needle)
                            || normalize(e.status()).contains(needle)
                            || normalize(e.enrollmentDate() == null ? "" : e.enrollmentDate().toString()).contains(needle))
                        .toList();
                    loadEnrollmentsIntoTable(filtered);
                }
                case "attendance" -> {
                    List<AdminAttendanceRowDTO> rows = adminDashboardService.getAllAttendanceRows();
                    List<AdminAttendanceRowDTO> filtered = rows.stream()
                        .filter(r -> normalize(r.studentName()).contains(needle)
                            || normalize(r.courseDisplay()).contains(needle)
                            || normalize(r.date() == null ? "" : r.date().toString()).contains(needle)
                            || normalize(r.weekNumber() == null ? "" : String.valueOf(r.weekNumber())).contains(needle)
                            || normalize(r.present() == null ? "" : String.valueOf(r.present())).contains(needle))
                        .toList();
                    loadAttendanceIntoTable(filtered);
                }
                case "statistics" -> loadStatisticsIntoTable();
                default -> refreshCurrentView();
            }
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private void updateStatistics() {
        try {
            var stats = adminDashboardService.getStatistics();
            totalUsersLabel.setText(String.valueOf(stats.totalUsers()));
            totalCoursesLabel.setText(String.valueOf(stats.totalCourses()));
            activeEnrollmentsLabel.setText(String.valueOf(stats.activeEnrollments()));
        } catch (Exception e) {
            log.error("Failed to update statistics", e);
            alertUtil.showErrorAlert("İstatistik güncellenemedi", e.getMessage());
        }
    }

    private void loadUsersIntoTable() {
        loadUsersIntoTable(adminDashboardService.getAllUserRows());
    }

    private void loadUsersIntoTable(List<AdminUserRowDTO> users) {
        try {
            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();

            ObservableList<AdminUserRowDTO> userList = FXCollections.observableArrayList(
                users == null ? List.of() : users
            );

            TableColumn<AdminUserRowDTO, Long> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().id()));

            TableColumn<AdminUserRowDTO, String> usernameCol = new TableColumn<>("Kullanıcı Adı");
            usernameCol.setCellValueFactory(cellData -> new SimpleStringProperty(nullToEmpty(cellData.getValue().username())));

            TableColumn<AdminUserRowDTO, String> firstNameCol = new TableColumn<>("Ad");
            firstNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(nullToEmpty(cellData.getValue().firstName())));

            TableColumn<AdminUserRowDTO, String> lastNameCol = new TableColumn<>("Soyad");
            lastNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(nullToEmpty(cellData.getValue().lastName())));

            TableColumn<AdminUserRowDTO, String> roleCol = new TableColumn<>("Rol");
            roleCol.setCellValueFactory(cellData -> {
                Role role = cellData.getValue().role();
                if (role == null) {
                    return new SimpleStringProperty("");
                }
                String roleText = switch (role) {
                    case ADMIN -> "Yönetici";
                    case INSTRUCTOR -> "Akademisyen";
                    case STUDENT -> "Öğrenci";
                };
                return new SimpleStringProperty(roleText);
            });

            TableColumn<AdminUserRowDTO, String> emailCol = new TableColumn<>("E-posta");
            emailCol.setCellValueFactory(cellData -> new SimpleStringProperty(nullToEmpty(cellData.getValue().email())));

            TableColumn<AdminUserRowDTO, String> actionsCol = new TableColumn<>("İşlemler");
            actionsCol.setCellFactory(createActionCellFactory("user"));

            @SuppressWarnings("unchecked")
            TableView<AdminUserRowDTO> userTableView = (TableView<AdminUserRowDTO>) dataTableView;
            userTableView.getColumns().addAll(idCol, usernameCol, firstNameCol, lastNameCol, roleCol, emailCol, actionsCol);
            userTableView.setItems(userList);
        } catch (Exception e) {
            alertUtil.showErrorAlert("Kullanıcılar yüklenemedi", e.getMessage());
        }
    }

    private void loadCoursesIntoTable() {
        loadCoursesIntoTable(courseService.getAllCourseDTOs());
    }

    private void loadCoursesIntoTable(List<CourseDTO> courses) {
        try {
            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();

            ObservableList<CourseDTO> courseList = FXCollections.observableArrayList(courses == null ? List.of() : courses);

            TableColumn<CourseDTO, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<CourseDTO, String> codeCol = new TableColumn<>("Ders Kodu");
            codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

            TableColumn<CourseDTO, String> nameCol = new TableColumn<>("Ders Adı");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

            TableColumn<CourseDTO, String> creditCol = new TableColumn<>("Kredi");
            creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));

            TableColumn<CourseDTO, String> quotaCol = new TableColumn<>("Kota");
            quotaCol.setCellValueFactory(new PropertyValueFactory<>("quota"));

            TableColumn<CourseDTO, String> instructorCol = new TableColumn<>("Akademisyen");
            instructorCol.setCellValueFactory(new PropertyValueFactory<>("instructorName"));

            TableColumn<CourseDTO, String> activeCol = new TableColumn<>("Aktif");
            activeCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getActive() != null && cellData.getValue().getActive() ? "Evet" : "Hayır"));

            TableColumn<CourseDTO, String> actionsCol = new TableColumn<>("İşlemler");
            actionsCol.setCellFactory(createActionCellFactory("course"));

            @SuppressWarnings("unchecked")
            TableView<CourseDTO> courseTableView = (TableView<CourseDTO>) dataTableView;
            courseTableView.getColumns().addAll(idCol, codeCol, nameCol, creditCol, quotaCol, instructorCol, activeCol, actionsCol);
            courseTableView.setItems(courseList);
        } catch (Exception e) {
            alertUtil.showErrorAlert("Dersler yüklenemedi", e.getMessage());
        }
    }

    private void loadEnrollmentsIntoTable() {
        loadEnrollmentsIntoTable(adminDashboardService.getAllEnrollmentRows());
    }

    private void loadEnrollmentsIntoTable(List<AdminEnrollmentRowDTO> enrollments) {
        try {
            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();

            ObservableList<AdminEnrollmentRowDTO> enrollmentList = FXCollections.observableArrayList(
                enrollments == null ? List.of() : enrollments
            );

            TableColumn<AdminEnrollmentRowDTO, Long> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().id()));

            TableColumn<AdminEnrollmentRowDTO, String> studentCol = new TableColumn<>("Öğrenci");
            studentCol.setCellValueFactory(cellData -> new SimpleStringProperty(nullToEmpty(cellData.getValue().studentName())));

            TableColumn<AdminEnrollmentRowDTO, String> courseCol = new TableColumn<>("Ders");
            courseCol.setCellValueFactory(cellData -> new SimpleStringProperty(nullToEmpty(cellData.getValue().courseDisplay())));

            TableColumn<AdminEnrollmentRowDTO, String> statusCol = new TableColumn<>("Durum");
            statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(nullToEmpty(cellData.getValue().status())));

            TableColumn<AdminEnrollmentRowDTO, String> dateCol = new TableColumn<>("Kayıt Tarihi");
            dateCol.setCellValueFactory(cellData -> {
                var dateTime = cellData.getValue().enrollmentDate();
                return new SimpleStringProperty(dateTime == null ? "" : DATE_TIME_FORMATTER.format(dateTime));
            });

            TableColumn<AdminEnrollmentRowDTO, String> actionsCol = new TableColumn<>("İşlemler");
            actionsCol.setCellFactory(createActionCellFactory("enrollment"));

            @SuppressWarnings("unchecked")
            TableView<AdminEnrollmentRowDTO> enrollmentTableView = (TableView<AdminEnrollmentRowDTO>) dataTableView;
            enrollmentTableView.getColumns().addAll(idCol, studentCol, courseCol, statusCol, dateCol, actionsCol);
            enrollmentTableView.setItems(enrollmentList);
        } catch (Exception e) {
            alertUtil.showErrorAlert("Kayıtlar yüklenemedi", e.getMessage());
        }
    }

    private void loadAttendanceIntoTable() {
        loadAttendanceIntoTable(adminDashboardService.getAllAttendanceRows());
    }

    private void loadAttendanceIntoTable(List<AdminAttendanceRowDTO> rows) {
        try {
            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();

            ObservableList<AdminAttendanceRowDTO> list = FXCollections.observableArrayList(rows == null ? List.of() : rows);

            TableColumn<AdminAttendanceRowDTO, Long> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().id()));

            TableColumn<AdminAttendanceRowDTO, String> studentCol = new TableColumn<>("Öğrenci");
            studentCol.setCellValueFactory(cellData -> new SimpleStringProperty(nullToEmpty(cellData.getValue().studentName())));

            TableColumn<AdminAttendanceRowDTO, String> courseCol = new TableColumn<>("Ders");
            courseCol.setCellValueFactory(cellData -> new SimpleStringProperty(nullToEmpty(cellData.getValue().courseDisplay())));

            TableColumn<AdminAttendanceRowDTO, Integer> weekCol = new TableColumn<>("Hafta");
            weekCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().weekNumber()));

            TableColumn<AdminAttendanceRowDTO, String> presentCol = new TableColumn<>("Katılım");
            presentCol.setCellValueFactory(cellData -> {
                Boolean present = cellData.getValue().present();
                return new SimpleStringProperty(present == null ? "" : (present ? "Var" : "Yok"));
            });

            TableColumn<AdminAttendanceRowDTO, String> dateCol = new TableColumn<>("Tarih");
            dateCol.setCellValueFactory(cellData -> {
                var date = cellData.getValue().date();
                return new SimpleStringProperty(date == null ? "" : date.toString());
            });

            @SuppressWarnings("unchecked")
            TableView<AdminAttendanceRowDTO> tableView = (TableView<AdminAttendanceRowDTO>) dataTableView;
            tableView.getColumns().addAll(idCol, studentCol, courseCol, weekCol, presentCol, dateCol);
            tableView.setItems(list);
        } catch (Exception e) {
            alertUtil.showErrorAlert("Yoklama raporları yüklenemedi", e.getMessage());
        }
    }

    private void loadStatisticsIntoTable() {
        try {
            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();

            var stats = adminDashboardService.getStatistics();
            ObservableList<AdminKeyValueRowDTO> list = FXCollections.observableArrayList(
                new AdminKeyValueRowDTO("Toplam kullanıcı", String.valueOf(stats.totalUsers())),
                new AdminKeyValueRowDTO("Toplam ders", String.valueOf(stats.totalCourses())),
                new AdminKeyValueRowDTO("Aktif kayıt", String.valueOf(stats.activeEnrollments()))
            );

            TableColumn<AdminKeyValueRowDTO, String> keyCol = new TableColumn<>("Metrik");
            keyCol.setCellValueFactory(cellData -> new SimpleStringProperty(nullToEmpty(cellData.getValue().key())));

            TableColumn<AdminKeyValueRowDTO, String> valueCol = new TableColumn<>("Değer");
            valueCol.setCellValueFactory(cellData -> new SimpleStringProperty(nullToEmpty(cellData.getValue().value())));

            @SuppressWarnings("unchecked")
            TableView<AdminKeyValueRowDTO> tableView = (TableView<AdminKeyValueRowDTO>) dataTableView;
            tableView.getColumns().addAll(keyCol, valueCol);
            tableView.setItems(list);
        } catch (Exception e) {
            alertUtil.showErrorAlert("İstatistikler yüklenemedi", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Callback<TableColumn<T, String>, TableCell<T, String>> createActionCellFactory(String type) {
        return column -> new TableCell<T, String>() {
            private final Button editButton = new Button("Düzenle");
            private final Button deleteButton = new Button("Sil");

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 5px 10px;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 5px 10px;");

                editButton.setOnAction(event -> {
                    T item = getTableView().getItems().get(getIndex());
                    handleEdit(item, type);
                });

                deleteButton.setOnAction(event -> {
                    T item = getTableView().getItems().get(getIndex());
                    handleDelete(item, type);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);
                    hbox.getChildren().addAll(editButton, deleteButton);
                    setGraphic(hbox);
                }
            }
        };
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @SuppressWarnings("unchecked")
    private <T> void handleEdit(T item, String type) {
        try {
            if ("user".equals(type)) {
                AdminUserRowDTO user = (AdminUserRowDTO) item;
                FXMLLoader loader = new FXMLLoader(getClass().getResource(UiConstants.FXML_EDIT_USER_FORM));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                EditUserFormController controller = loader.getController();
                controller.setUserId(user.id());

                Stage dialog = new Stage();
                dialog.setTitle(UiConstants.WINDOW_TITLE_EDIT_USER);
                dialog.initOwner(dataTableView.getScene().getWindow());
                dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
                dialog.setScene(new Scene(root));
                dialog.setResizable(false);
                dialog.sizeToScene();
                dialog.centerOnScreen();
                dialog.showAndWait();

                refreshCurrentView();
                updateStatistics();
            } else if ("course".equals(type)) {
                CourseDTO course = (CourseDTO) item;
                FXMLLoader loader = new FXMLLoader(getClass().getResource(UiConstants.FXML_EDIT_COURSE_FORM));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                EditCourseFormController controller = loader.getController();
                controller.setCourseId(course.getId());

                Stage dialog = new Stage();
                dialog.setTitle(UiConstants.WINDOW_TITLE_EDIT_COURSE);
                dialog.initOwner(dataTableView.getScene().getWindow());
                dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
                dialog.setScene(new Scene(root));
                dialog.setResizable(false);
                dialog.sizeToScene();
                dialog.centerOnScreen();
                dialog.showAndWait();

                refreshCurrentView();
                updateStatistics();
            } else if ("enrollment".equals(type)) {
                AdminEnrollmentRowDTO enrollment = (AdminEnrollmentRowDTO) item;
                FXMLLoader loader = new FXMLLoader(getClass().getResource(UiConstants.FXML_EDIT_ENROLLMENT_FORM));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                EditEnrollmentFormController controller = loader.getController();
                controller.setEnrollmentId(enrollment.id());

                Stage dialog = new Stage();
                dialog.setTitle(UiConstants.WINDOW_TITLE_EDIT_ENROLLMENT);
                dialog.initOwner(dataTableView.getScene().getWindow());
                dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
                dialog.setScene(new Scene(root));
                dialog.setResizable(false);
                dialog.sizeToScene();
                dialog.centerOnScreen();
                dialog.showAndWait();

                refreshCurrentView();
                updateStatistics();
            }
        } catch (Exception e) {
            alertUtil.showErrorAlert("Düzenleme başarısız", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void handleDelete(T item, String type) {
        try {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Silme Onayı");
            confirmAlert.setHeaderText("Emin misiniz?");
            
            String contentText = "";
            if ("user".equals(type)) {
                AdminUserRowDTO user = (AdminUserRowDTO) item;
                contentText = "'" + user.username() + "' kullanıcısı silinecek. Devam edilsin mi?";
            } else if ("course".equals(type)) {
                CourseDTO course = (CourseDTO) item;
                contentText = "'" + course.getCode() + " - " + course.getName() + "' dersi pasif yapılacak. Devam edilsin mi?";
            } else if ("enrollment".equals(type)) {
                AdminEnrollmentRowDTO enrollment = (AdminEnrollmentRowDTO) item;
                contentText = "Kayıt (ID: " + enrollment.id() + ") silinecek. Devam edilsin mi?";
            }
            
            confirmAlert.setContentText(contentText);
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if ("user".equals(type)) {
                    AdminUserRowDTO user = (AdminUserRowDTO) item;
                    adminDashboardService.deleteUser(user.id());
                    alertUtil.showInformationAlert("Başarılı", "Kullanıcı silindi: " + user.username());
                } else if ("course".equals(type)) {
                    CourseDTO course = (CourseDTO) item;
                    courseService.deactivateCourse(course.getId());
                    alertUtil.showInformationAlert("Başarılı", "Ders pasif yapıldı: " + course.getCode());
                } else if ("enrollment".equals(type)) {
                    AdminEnrollmentRowDTO enrollment = (AdminEnrollmentRowDTO) item;
                    if (enrollment.studentId() == null || enrollment.courseId() == null) {
                        throw new IllegalArgumentException("Enrollment is missing student/course reference");
                    }
                    adminDashboardService.dropEnrollment(enrollment.studentId(), enrollment.courseId());
                    alertUtil.showInformationAlert("Başarılı", "Kayıt silindi: ID " + enrollment.id());
                }
                
                refreshCurrentView();
                updateStatistics();
            }
        } catch (Exception e) {
            alertUtil.showErrorAlert("Silme başarısız", e.getMessage());
        }
    }

    private void refreshCurrentView() {
        switch (currentView) {
            case "users":
                loadUsersIntoTable();
                break;
            case "courses":
                loadCoursesIntoTable();
                break;
            case "enrollments":
                loadEnrollmentsIntoTable();
                break;
            case "attendance":
                loadAttendanceIntoTable();
                break;
            case "statistics":
                loadStatisticsIntoTable();
                break;
        }
    }

    @FXML
    public void handleEnrollStudent(Long studentId, Long courseId) {
        try {
            adminDashboardService.enrollStudent(studentId, courseId);
            alertUtil.showInformationAlert("Başarılı", "Öğrenci derse başarıyla kaydedildi.");
            refreshCurrentView();
            updateStatistics();
        } catch (IllegalArgumentException e) {
            alertUtil.showErrorAlert("Kayıt hatası", e.getMessage());
        } catch (RuntimeException e) {
            alertUtil.showErrorAlert("Kayıt hatası", e.getMessage());
        } catch (Exception e) {
            alertUtil.showErrorAlert("Beklenmeyen hata", e.getMessage());
        }
    }
}