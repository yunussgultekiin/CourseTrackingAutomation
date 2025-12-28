package org.example.coursetrackingautomation.controller.admin;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.example.coursetrackingautomation.dto.AdminAttendanceRowDTO;

/**
 * Utility methods for configuring Admin Dashboard table row interactions.
 *
 * <p>Currently used to attach double-click handlers for editing rows and viewing attendance
 * details. This class is UI-only and does not perform any business logic.</p>
 */
public final class AdminDashboardRowHandlers {

    private AdminDashboardRowHandlers() {
    }

    /**
     * Installs a row factory that invokes the provided edit callback on double-click.
     *
     * @param table the table to configure
     * @param actionType a string describing the entity type (e.g. user/course/enrollment)
     * @param onEdit callback invoked with the row item and action type
     */
    public static void configureEditOnDoubleClick(TableView<Object> table, String actionType, BiConsumer<Object, String> onEdit) {
        if (table == null) {
            return;
        }

        table.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Object item = row.getItem();
                    if (item != null) {
                        onEdit.accept(item, actionType);
                    }
                }
            });
            return row;
        });
    }

    /**
     * Installs a row factory that invokes the provided details callback for attendance rows on
     * double-click.
     *
     * @param table the table to configure
     * @param onShowDetails callback invoked with the selected {@link AdminAttendanceRowDTO}
     */
    public static void configureAttendanceDetailsPopup(TableView<Object> table, Consumer<AdminAttendanceRowDTO> onShowDetails) {
        if (table == null) {
            return;
        }

        table.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Object item = row.getItem();
                    if (item instanceof AdminAttendanceRowDTO attendanceRow) {
                        onShowDetails.accept(attendanceRow);
                    }
                }
            });
            return row;
        });
    }
}
