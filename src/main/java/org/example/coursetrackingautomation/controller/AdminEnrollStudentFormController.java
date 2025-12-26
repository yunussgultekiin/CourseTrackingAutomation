package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.entity.Role;
import org.example.coursetrackingautomation.entity.User;
import org.example.coursetrackingautomation.service.AdminDashboardService;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AdminEnrollStudentFormController {

    @FXML private ComboBox<UserOption> comboStudent;
    @FXML private ComboBox<CourseOption> comboCourse;

    private final UserService userService;
    private final CourseService courseService;
    private final AdminDashboardService adminDashboardService;
    private final UiExceptionHandler uiExceptionHandler;

    @FXML
    public void initialize() {
        try {
            var students = userService.getActiveUsersByRole(Role.STUDENT).stream()
                .map(AdminEnrollStudentFormController::toUserOption)
                .toList();

            var courses = courseService.getAllActiveCourseDTOs().stream()
                .map(AdminEnrollStudentFormController::toCourseOption)
                .toList();

            comboStudent.setItems(FXCollections.observableArrayList(students));
            comboCourse.setItems(FXCollections.observableArrayList(courses));
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleEnroll() {
        try {
            UserOption student = comboStudent.getValue();
            CourseOption course = comboCourse.getValue();

            if (student == null || student.id() == null) {
                throw new IllegalArgumentException(UiConstants.ERROR_KEY_STUDENT_SELECTION_REQUIRED);
            }
            if (course == null || course.id() == null) {
                throw new IllegalArgumentException(UiConstants.ERROR_KEY_COURSE_SELECTION_REQUIRED);
            }

            adminDashboardService.enrollStudent(student.id(), course.id());
            close();
        } catch (Exception e) {
            uiExceptionHandler.handle(e);
        }
    }

    @FXML
    public void handleClose() {
        close();
    }

    private void close() {
        Stage stage = (Stage) comboStudent.getScene().getWindow();
        stage.close();
    }

    private static UserOption toUserOption(User user) {
        String display = user.getUsername() + " - " + user.getFirstName() + " " + user.getLastName();
        return new UserOption(user.getId(), display);
    }

    private static CourseOption toCourseOption(CourseDTO dto) {
        String display = dto.getCode() + " - " + dto.getName();
        return new CourseOption(dto.getId(), display);
    }

    public record UserOption(Long id, String display) {
        @Override
        public String toString() {
            return display;
        }
    }

    public record CourseOption(Long id, String display) {
        @Override
        public String toString() {
            return display;
        }
    }
}
