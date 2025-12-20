package org.example.coursetrackingautomation.controller;

import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

}