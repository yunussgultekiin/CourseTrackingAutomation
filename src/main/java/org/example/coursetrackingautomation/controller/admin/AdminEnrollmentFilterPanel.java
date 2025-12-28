package org.example.coursetrackingautomation.controller.admin;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.example.coursetrackingautomation.dto.CourseDTO;

public final class AdminEnrollmentFilterPanel {

    private AdminEnrollmentFilterPanel() {
    }

    public static void initialize(
        VBox enrollmentsFilterBox,
        ComboBox<CourseDTO> enrollmentCourseCombo,
        ComboBox<String> enrollmentStatusCombo,
        TextField enrollmentStudentNameField,
        List<CourseDTO> allCourses,
        List<String> statusCodes,
        java.util.function.Function<String, String> translateStatus,
        Runnable onFiltersChanged
    ) {
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

            enrollmentCourseCombo.setItems(FXCollections.observableArrayList(allCourses));
        }

        if (enrollmentStatusCombo != null) {
            enrollmentStatusCombo.setItems(FXCollections.observableArrayList(statusCodes));
            enrollmentStatusCombo.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : translateStatus.apply(item));
                }
            });
            enrollmentStatusCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(enrollmentStatusCombo.getPromptText());
                    } else {
                        setText(translateStatus.apply(item));
                    }
                }
            });
        }

        if (enrollmentStudentNameField != null) {
            enrollmentStudentNameField.textProperty().addListener((obs, o, n) -> onFiltersChanged.run());
        }
        if (enrollmentCourseCombo != null) {
            enrollmentCourseCombo.valueProperty().addListener((obs, o, n) -> onFiltersChanged.run());
        }
        if (enrollmentStatusCombo != null) {
            enrollmentStatusCombo.valueProperty().addListener((obs, o, n) -> onFiltersChanged.run());
        }
    }

    public static boolean hasAnyFilter(String studentQuery, CourseDTO selectedCourse, String statusCode) {
        return (studentQuery != null && !studentQuery.isBlank())
            || (selectedCourse != null && selectedCourse.getId() != null)
            || (statusCode != null && !statusCode.isBlank());
    }
}
