package org.example.coursetrackingautomation.controller;

import org.example.coursetrackingautomation.repository.CourseRepository;
import org.example.coursetrackingautomation.repository.UserRepository;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Component
public class AdminDashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AlertUtil alertUtil;

    @Autowired
    private ApplicationContext applicationContext;

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
    private TableView<?> dataTableView;

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

    @FXML
    public void handleLogout() {
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
        contentTitleLabel.setText("Users Management");
        // TODO: Load users into table
    }

    @FXML
    public void handleCoursesManagement() {
        contentTitleLabel.setText("Courses Management");
        // TODO: Load courses into table
    }

    @FXML
    public void handleEnrollments() {
        contentTitleLabel.setText("Enrollments");
        // TODO: Load enrollments into table
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
            // TODO: Calculate active enrollments
            long activeEnrollments = 0;

            totalUsersLabel.setText(String.valueOf(totalUsers));
            totalCoursesLabel.setText(String.valueOf(totalCourses));
            activeEnrollmentsLabel.setText(String.valueOf(activeEnrollments));
        } catch (Exception e) {
            // Handle error silently or log it
        }
    }
}