package org.example.coursetrackingautomation.controller.instructor;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.service.AttendanceService;

public final class InstructorRosterRowFactory {

    private InstructorRosterRowFactory() {
    }

    public static void configure(
        TableView<GradeDTO> table,
        Supplier<CourseDTO> selectedCourseSupplier,
        AttendanceService attendanceService,
        String criticalStyleClass,
        String warningStyleClass,
        Consumer<GradeDTO> onEditGrade
    ) {
        if (table == null) {
            return;
        }

        table.setRowFactory(tv -> {
            TableRow<GradeDTO> row = new TableRow<>() {
                @Override
                protected void updateItem(GradeDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    updateAttendanceStyle(this, item, empty, selectedCourseSupplier, attendanceService, criticalStyleClass, warningStyleClass);
                }
            };

            row.setOnMouseClicked(event -> {
                if (event.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    try {
                        table.edit(-1, null);
                    } catch (Exception ignored) {
                    }

                    GradeDTO item = row.getItem();
                    if (item == null || onEditGrade == null) {
                        return;
                    }

                    onEditGrade.accept(item);
                }
            });

            return row;
        });
    }

    private static void updateAttendanceStyle(
        TableRow<GradeDTO> row,
        GradeDTO item,
        boolean empty,
        Supplier<CourseDTO> selectedCourseSupplier,
        AttendanceService attendanceService,
        String criticalStyleClass,
        String warningStyleClass
    ) {
        if (row == null) {
            return;
        }
        // Row-based styling removed in favor of per-cell badges.
        row.getStyleClass().removeAll(criticalStyleClass, warningStyleClass);
    }
}
