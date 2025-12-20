package org.example.coursetrackingautomation.controller;

import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for Admin Dashboard UI.
 * Handles user interactions and delegates business logic to Service layer.
 */
@Component
public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logoutButton;

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
    private void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.close();
            // TODO: Login ekranına geri dön
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
        }
    }

    @FXML
    private void handleUsersManagement() {
        if (contentTitleLabel != null) {
            contentTitleLabel.setText("Users Management");
        }
        // TODO: Kullanıcıları tabloya yükle
    }

    @FXML
    private void handleCoursesManagement() {
        if (contentTitleLabel != null) {
            contentTitleLabel.setText("Courses Management");
        }
        // TODO: Kursları tabloya yükle
    }

    @FXML
    private void handleEnrollments() {
        if (contentTitleLabel != null) {
            contentTitleLabel.setText("Enrollments");
        }
        // TODO: Kayıtları tabloya yükle
    }

    @FXML
    private void handleAttendanceReports() {
        if (contentTitleLabel != null) {
            contentTitleLabel.setText("Attendance Reports");
        }
        // TODO: Devam raporlarını göster
    }

    @FXML
    private void handleStatistics() {
        if (contentTitleLabel != null) {
            contentTitleLabel.setText("Statistics");
        }
        // TODO: İstatistikleri göster
    }

    @FXML
    private void handleAddUser() {
        // TODO: Kullanıcı ekleme dialogu aç
        System.out.println("Add User clicked");
    }

    @FXML
    private void handleAddCourse() {
        // TODO: Kurs ekleme dialogu aç
        System.out.println("Add Course clicked");
    }

    @FXML
    private void handleRefresh() {
        // TODO: Mevcut verileri yenile
        System.out.println("Refresh clicked");
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField != null ? searchField.getText() : "";
        // TODO: Arama işlemini gerçekleştir
        System.out.println("Search: " + searchText);
    }
}