package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.dto.RoleDTO;
import org.example.coursetrackingautomation.dto.SelectOptionDTO;
import org.example.coursetrackingautomation.service.AdminDashboardService;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.service.UserService;
import org.example.coursetrackingautomation.ui.FxAsync;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
/**
 * JavaFX controller for the admin "Enroll Student" form.
 *
 * <p>Allows an admin to select an active student and an active course and create an enrollment
 * through the admin workflow service.</p>
 */
public class AdminEnrollStudentFormController {

    @FXML private ComboBox<SelectOptionDTO> comboStudent;
    @FXML private ComboBox<SelectOptionDTO> comboCourse;

    private final UserService userService;
    private final CourseService courseService;
    private final AdminDashboardService adminDashboardService;
    private final UiExceptionHandler uiExceptionHandler;

    @FXML
    /**
     * Initializes the selection lists for students and courses.
     */
    public void initialize() {
        FxAsync.runAsync(
            () -> userService.getActiveUserOptionsByRole(RoleDTO.STUDENT),
            students -> comboStudent.setItems(FXCollections.observableArrayList(students)),
            uiExceptionHandler::handle
        );

        FxAsync.runAsync(
            () -> courseService.getAllActiveCourseDTOs().stream()
                .map(AdminEnrollStudentFormController::toCourseOption)
                .toList(),
            courses -> comboCourse.setItems(FXCollections.observableArrayList(courses)),
            uiExceptionHandler::handle
        );
    }

    @FXML
    /**
     * Creates the selected enrollment and closes the window.
     */
    public void handleEnroll() {
        SelectOptionDTO student = comboStudent.getValue();
        SelectOptionDTO course = comboCourse.getValue();

        if (student == null || student.id() == null) {
            uiExceptionHandler.handle(new IllegalArgumentException(UiConstants.ERROR_KEY_STUDENT_SELECTION_REQUIRED));
            return;
        }
        if (course == null || course.id() == null) {
            uiExceptionHandler.handle(new IllegalArgumentException(UiConstants.ERROR_KEY_COURSE_SELECTION_REQUIRED));
            return;
        }

        FxAsync.runAsync(
            () -> {
                adminDashboardService.enrollStudent(student.id(), course.id());
            },
            this::close,
            uiExceptionHandler::handle
        );
    }

    @FXML
    /**
     * Closes the window without saving.
     */
    public void handleClose() {
        close();
    }

    private void close() {
        Stage stage = (Stage) comboStudent.getScene().getWindow();
        stage.close();
    }

    private static SelectOptionDTO toCourseOption(CourseDTO dto) {
        String display = dto.getCode() + " - " + dto.getName();
        return new SelectOptionDTO(dto.getId(), display);
    }
}
