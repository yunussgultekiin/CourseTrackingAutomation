package org.example.coursetrackingautomation.controller;

import java.util.List;
import java.util.Optional;

import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.EnrollmentRepository;
import org.example.coursetrackingautomation.repository.UserRepository;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.service.EnrollmentService;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javafx.beans.property.SimpleStringProperty;
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

@Component
public class AdminDashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private AlertUtil alertUtil;

    @Autowired
    private ApplicationContext applicationContext;

    private String currentView = "users"; // "users", "courses", "enrollments"

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
        // Initialize dashboard data
        updateStatistics();
    }

    /**
     * Yunus'un yazdığı performLogout işlemi
     */
    public void performLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            alertUtil.showErrorAlert("Çıkış yapılırken bir hata oluştu: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        performLogout();
    }

    @FXML
    public void handleProfile() {
        alertUtil.showInformationAlert("Profilim", "Profil bilgileri yakında eklenecek.");
    }

    @FXML
    public void handleAddUser() {
        alertUtil.showInformationAlert("Kullanıcı Ekle", "Kullanıcı ekleme formu yakında eklenecek.");
    }

    @FXML
    public void handleAddCourse() {
        alertUtil.showInformationAlert("Ders Ekle", "Ders ekleme formu yakında eklenecek.");
    }

    @FXML
    public void handleUsersManagement() {
        currentView = "users";
        contentTitleLabel.setText("Users Management");
        loadUsersIntoTable();
    }

    @FXML
    public void handleCoursesManagement() {
        currentView = "courses";
        contentTitleLabel.setText("Courses Management");
        loadCoursesIntoTable();
    }

    @FXML
    public void handleEnrollments() {
        currentView = "enrollments";
        contentTitleLabel.setText("Enrollments");
        loadEnrollmentsIntoTable();
    }

    @FXML
    public void handleAttendanceReports() {
        contentTitleLabel.setText("Attendance Reports");
        // TODO: Load attendance reports
    }

    @FXML
    public void handleStatistics() {
        contentTitleLabel.setText("Statistics");
        // TODO: Show statistics
    }

    @FXML
    public void handleRefresh() {
        updateStatistics();
        alertUtil.showInformationAlert("Yenilendi", "Veriler yenilendi.");
    }

    @FXML
    public void handleSearch() {
        String searchTerm = searchField.getText();
        // TODO: Implement search functionality
        alertUtil.showInformationAlert("Arama", "Arama özelliği yakında eklenecek.");
    }

    private void updateStatistics() {
        try {
            long totalUsers = userRepository.count();
            long totalCourses = courseRepository.count();
            long activeEnrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getStatus() != null && 
                    (e.getStatus().equals("ACTIVE") || 
                     e.getStatus().equals("ENROLLED") || 
                     e.getStatus().equals("REGISTERED")))
                .count();

            totalUsersLabel.setText(String.valueOf(totalUsers));
            totalCoursesLabel.setText(String.valueOf(totalCourses));
            activeEnrollmentsLabel.setText(String.valueOf(activeEnrollments));
        } catch (Exception e) {
            alertUtil.showErrorAlert("İstatistikler güncellenirken hata: " + e.getMessage());
        }
    }

    /**
     * Kullanıcıları tabloya yükler
     */
    private void loadUsersIntoTable() {
        try {
            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();

            // Kullanıcıları getir
            List<User> users = userRepository.findAll();
            ObservableList<User> userList = FXCollections.observableArrayList(users);

            // Kolonları oluştur
            TableColumn<User, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<User, String> usernameCol = new TableColumn<>("Kullanıcı Adı");
            usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

            TableColumn<User, String> firstNameCol = new TableColumn<>("Ad");
            firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

            TableColumn<User, String> lastNameCol = new TableColumn<>("Soyad");
            lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

            TableColumn<User, String> roleCol = new TableColumn<>("Rol");
            roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

            TableColumn<User, String> emailCol = new TableColumn<>("E-posta");
            emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

            // İşlemler kolonu (Düzenle ve Sil)
            TableColumn<User, String> actionsCol = new TableColumn<>("İşlemler");
            actionsCol.setCellFactory(createActionCellFactory("user"));

            @SuppressWarnings("unchecked")
            TableView<User> userTableView = (TableView<User>) dataTableView;
            userTableView.getColumns().addAll(idCol, usernameCol, firstNameCol, lastNameCol, roleCol, emailCol, actionsCol);
            userTableView.setItems(userList);
        } catch (Exception e) {
            alertUtil.showErrorAlert("Kullanıcılar yüklenirken hata: " + e.getMessage());
        }
    }

    /**
     * Dersleri tabloya yükler
     */
    private void loadCoursesIntoTable() {
        try {
            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();

            // Dersleri getir (CourseService kullanarak)
            List<CourseDTO> courses = courseService.getAllCourseDTOs();
            ObservableList<CourseDTO> courseList = FXCollections.observableArrayList(courses);

            // Kolonları oluştur
            TableColumn<CourseDTO, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<CourseDTO, String> codeCol = new TableColumn<>("Ders Kodu");
            codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

            TableColumn<CourseDTO, String> nameCol = new TableColumn<>("Ders Adı");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

            TableColumn<CourseDTO, String> creditCol = new TableColumn<>("Kredi");
            creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));

            TableColumn<CourseDTO, String> quotaCol = new TableColumn<>("Kontenjan");
            quotaCol.setCellValueFactory(new PropertyValueFactory<>("quota"));

            TableColumn<CourseDTO, String> instructorCol = new TableColumn<>("Öğretim Üyesi");
            instructorCol.setCellValueFactory(new PropertyValueFactory<>("instructorName"));

            TableColumn<CourseDTO, String> activeCol = new TableColumn<>("Aktif");
            activeCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getActive() != null && cellData.getValue().getActive() ? "Evet" : "Hayır"));

            // İşlemler kolonu (Düzenle ve Sil)
            TableColumn<CourseDTO, String> actionsCol = new TableColumn<>("İşlemler");
            actionsCol.setCellFactory(createActionCellFactory("course"));

            @SuppressWarnings("unchecked")
            TableView<CourseDTO> courseTableView = (TableView<CourseDTO>) dataTableView;
            courseTableView.getColumns().addAll(idCol, codeCol, nameCol, creditCol, quotaCol, instructorCol, activeCol, actionsCol);
            courseTableView.setItems(courseList);
        } catch (Exception e) {
            alertUtil.showErrorAlert("Dersler yüklenirken hata: " + e.getMessage());
        }
    }

    /**
     * Kayıtları tabloya yükler
     */
    private void loadEnrollmentsIntoTable() {
        try {
            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();

            // Kayıtları getir
            List<org.example.coursetrackingautomation.entity.Enrollment> enrollments = enrollmentRepository.findAll();
            ObservableList<org.example.coursetrackingautomation.entity.Enrollment> enrollmentList = 
                FXCollections.observableArrayList(enrollments);

            // Kolonları oluştur
            TableColumn<org.example.coursetrackingautomation.entity.Enrollment, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<org.example.coursetrackingautomation.entity.Enrollment, String> studentCol = new TableColumn<>("Öğrenci");
            studentCol.setCellValueFactory(cellData -> {
                User student = cellData.getValue().getStudent();
                return new SimpleStringProperty(student != null ? 
                    student.getFirstName() + " " + student.getLastName() : "");
            });

            TableColumn<org.example.coursetrackingautomation.entity.Enrollment, String> courseCol = new TableColumn<>("Ders");
            courseCol.setCellValueFactory(cellData -> {
                org.example.coursetrackingautomation.entity.Course course = cellData.getValue().getCourse();
                return new SimpleStringProperty(course != null ? course.getCode() + " - " + course.getName() : "");
            });

            TableColumn<org.example.coursetrackingautomation.entity.Enrollment, String> statusCol = new TableColumn<>("Durum");
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

            TableColumn<org.example.coursetrackingautomation.entity.Enrollment, String> dateCol = new TableColumn<>("Kayıt Tarihi");
            dateCol.setCellValueFactory(new PropertyValueFactory<>("enrollmentDate"));

            // İşlemler kolonu (Düzenle ve Sil)
            TableColumn<org.example.coursetrackingautomation.entity.Enrollment, String> actionsCol = new TableColumn<>("İşlemler");
            actionsCol.setCellFactory(createActionCellFactory("enrollment"));

            @SuppressWarnings("unchecked")
            TableView<org.example.coursetrackingautomation.entity.Enrollment> enrollmentTableView = 
                (TableView<org.example.coursetrackingautomation.entity.Enrollment>) dataTableView;
            enrollmentTableView.getColumns().addAll(idCol, studentCol, courseCol, statusCol, dateCol, actionsCol);
            enrollmentTableView.setItems(enrollmentList);
        } catch (Exception e) {
            alertUtil.showErrorAlert("Kayıtlar yüklenirken hata: " + e.getMessage());
        }
    }

    /**
     * Düzenle ve Sil butonlarını içeren hücre fabrikası oluşturur
     */
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

    /**
     * Düzenle işlemi
     */
    @SuppressWarnings("unchecked")
    private <T> void handleEdit(T item, String type) {
        try {
            if ("user".equals(type)) {
                User user = (User) item;
                alertUtil.showInformationAlert("Düzenle", 
                    "Kullanıcı düzenleme formu: " + user.getUsername() + "\n(Bu özellik yakında eklenecek)");
            } else if ("course".equals(type)) {
                CourseDTO course = (CourseDTO) item;
                alertUtil.showInformationAlert("Düzenle", 
                    "Ders düzenleme formu: " + course.getCode() + " - " + course.getName() + "\n(Bu özellik yakında eklenecek)");
            } else if ("enrollment".equals(type)) {
                org.example.coursetrackingautomation.entity.Enrollment enrollment = 
                    (org.example.coursetrackingautomation.entity.Enrollment) item;
                alertUtil.showInformationAlert("Düzenle", 
                    "Kayıt düzenleme formu: ID " + enrollment.getId() + "\n(Bu özellik yakında eklenecek)");
            }
        } catch (Exception e) {
            alertUtil.showErrorAlert("Düzenleme işlemi sırasında hata: " + e.getMessage());
        }
    }

    /**
     * Silme işlemi - "Emin misiniz?" uyarısı ile
     */
    @SuppressWarnings("unchecked")
    private <T> void handleDelete(T item, String type) {
        try {
            // Onay dialogu göster
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Silme Onayı");
            confirmAlert.setHeaderText("Emin misiniz?");
            
            String contentText = "";
            if ("user".equals(type)) {
                User user = (User) item;
                contentText = "Kullanıcı '" + user.getUsername() + "' silinecek. Emin misiniz?";
            } else if ("course".equals(type)) {
                CourseDTO course = (CourseDTO) item;
                contentText = "Ders '" + course.getCode() + " - " + course.getName() + "' silinecek. Emin misiniz?";
            } else if ("enrollment".equals(type)) {
                org.example.coursetrackingautomation.entity.Enrollment enrollment = 
                    (org.example.coursetrackingautomation.entity.Enrollment) item;
                contentText = "Kayıt (ID: " + enrollment.getId() + ") silinecek. Emin misiniz?";
            }
            
            confirmAlert.setContentText(contentText);
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Silme işlemi
                if ("user".equals(type)) {
                    User user = (User) item;
                    userRepository.delete(user);
                    alertUtil.showInformationAlert("Başarılı", "Kullanıcı silindi: " + user.getUsername());
                } else if ("course".equals(type)) {
                    CourseDTO course = (CourseDTO) item;
                    courseService.deactivateCourse(course.getId());
                    alertUtil.showInformationAlert("Başarılı", "Ders pasif hale getirildi: " + course.getCode());
                } else if ("enrollment".equals(type)) {
                    org.example.coursetrackingautomation.entity.Enrollment enrollment = 
                        (org.example.coursetrackingautomation.entity.Enrollment) item;
                    // EnrollmentService kullanarak kayıt silme
                    enrollmentService.dropEnrollment(enrollment.getStudent().getId(), enrollment.getCourse().getId());
                    alertUtil.showInformationAlert("Başarılı", "Kayıt silindi: ID " + enrollment.getId());
                }
                
                // Tabloyu yenile
                refreshCurrentView();
                updateStatistics();
            }
        } catch (Exception e) {
            alertUtil.showErrorAlert("Silme işlemi sırasında hata: " + e.getMessage());
        }
    }

    /**
     * Mevcut görünümü yeniler
     */
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
        }
    }

    /**
     * Öğrenci kaydı işlemleri - EnrollmentService'e bağlı ve exception handling ile
     */
    @FXML
    public void handleEnrollStudent(Long studentId, Long courseId) {
        try {
            enrollmentService.enrollStudent(studentId, courseId);
            alertUtil.showInformationAlert("Başarılı", "Öğrenci derse başarıyla kaydedildi.");
            refreshCurrentView();
            updateStatistics();
        } catch (IllegalArgumentException e) {
            alertUtil.showErrorAlert("Kayıt Hatası: " + e.getMessage());
        } catch (RuntimeException e) {
            alertUtil.showErrorAlert("Kayıt Hatası: " + e.getMessage());
        } catch (Exception e) {
            alertUtil.showErrorAlert("Beklenmeyen hata: " + e.getMessage());
        }
    }
}