package org.example.coursetrackingautomation.controller.instructor;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.geometry.Pos;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.TextField;
import org.example.coursetrackingautomation.controller.support.AttendanceHoursValidator;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.service.AttendanceService;
import org.example.coursetrackingautomation.util.AlertUtil;

public final class InstructorGradesTableConfigurer {

    private static final String PROPERTY_STUDENT_ID = "studentId";
    private static final String PROPERTY_STUDENT_NAME = "studentName";
    private static final String PROPERTY_ATTENDANCE_COUNT = "attendanceCount";

    private InstructorGradesTableConfigurer() {
    }

    public static void configure(
        TableView<GradeDTO> table,
        TableColumn<GradeDTO, Long> colStudentNumber,
        TableColumn<GradeDTO, String> colFullName,
        TableColumn<GradeDTO, Double> colMidterm,
        TableColumn<GradeDTO, Double> colFinal,
        TableColumn<GradeDTO, Integer> colAttendance,
        TableColumn<GradeDTO, Boolean> colPresent,
        TableColumn<GradeDTO, Double> colAverage,
        TableColumn<GradeDTO, String> colLetterGrade,
        TableColumn<GradeDTO, String> colStatus,
        Function<GradeDTO, SimpleObjectProperty<Double>> midtermProp,
        Function<GradeDTO, SimpleObjectProperty<Double>> finalProp,
        Function<GradeDTO, SimpleObjectProperty<Double>> averageProp,
        Function<GradeDTO, SimpleObjectProperty<String>> letterProp,
        Function<GradeDTO, SimpleObjectProperty<String>> statusProp,
        Supplier<CourseDTO> selectedCourseSupplier,
        AttendanceService attendanceService,
        AlertUtil alertUtil,
        Consumer<GradeDTO> onRowChanged
    ) {
        if (table == null) {
            return;
        }

        if (colStudentNumber != null) {
            colStudentNumber.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_STUDENT_ID));
        }
        if (colFullName != null) {
            colFullName.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_STUDENT_NAME));
        }
        if (colAverage != null) {
            colAverage.setCellValueFactory(cell -> averageProp.apply(cell.getValue()));
        }
        if (colLetterGrade != null) {
            colLetterGrade.setCellValueFactory(cell -> letterProp.apply(cell.getValue()));
        }
        if (colStatus != null) {
            colStatus.setCellValueFactory(cell -> statusProp.apply(cell.getValue()));
        }

        if (colAttendance != null) {
            colAttendance.setCellValueFactory(new PropertyValueFactory<>(PROPERTY_ATTENDANCE_COUNT));
            colAttendance.setCellFactory(col -> new BadgeEditingIntegerCell(selectedCourseSupplier, attendanceService));
            colAttendance.setOnEditCommit(event -> {
                GradeDTO row = event == null ? null : event.getRowValue();
                if (row == null) {
                    return;
                }

                Integer newHours = event.getNewValue();
                Integer oldHours = event.getOldValue();

                CourseDTO selectedCourse = selectedCourseSupplier == null ? null : selectedCourseSupplier.get();
                var validationError = AttendanceHoursValidator.validate(newHours, selectedCourse);
                if (validationError.isPresent()) {
                    if (alertUtil != null) {
                        alertUtil.showErrorAlert("Doğrulama Hatası", validationError.get());
                    }
                    row.setAttendanceCount(oldHours);
                    table.refresh();
                    return;
                }

                row.setAttendanceCount(newHours);
                boolean critical = attendanceService != null
                    && attendanceService.isAttendanceCriticalByHours(selectedCourse == null ? null : selectedCourse.getWeeklyTotalHours(), newHours);
                row.setAbsentCritically(critical);

                if (onRowChanged != null) {
                    onRowChanged.accept(row);
                }
            });
        }

        if (colPresent != null) {
            colPresent.setCellValueFactory(cellData -> buildPresentProperty(cellData.getValue(), onRowChanged));
            colPresent.setCellFactory(CheckBoxTableCell.forTableColumn(colPresent));
        }

        if (colMidterm != null) {
            colMidterm.setCellValueFactory(cell -> midtermProp.apply(cell.getValue()));
            colMidterm.setEditable(false);
        }

        if (colFinal != null) {
            colFinal.setCellValueFactory(cell -> finalProp.apply(cell.getValue()));
            colFinal.setEditable(false);
        }
    }

    private static BooleanProperty buildPresentProperty(GradeDTO row, Consumer<GradeDTO> onRowChanged) {
        boolean initial = row != null && row.getPresent() != null ? row.getPresent() : true;
        BooleanProperty property = new SimpleBooleanProperty(initial);
        property.addListener((obs, oldValue, newValue) -> {
            if (row == null) {
                return;
            }
            row.setPresent(newValue);
            if (onRowChanged != null) {
                onRowChanged.accept(row);
            }
        });
        return property;
    }

    private static final class BadgeEditingIntegerCell extends TableCell<GradeDTO, Integer> {
        private final Supplier<CourseDTO> selectedCourseSupplier;
        private final AttendanceService attendanceService;
        private final TextField editor = new TextField();

        private BadgeEditingIntegerCell(Supplier<CourseDTO> selectedCourseSupplier, AttendanceService attendanceService) {
            this.selectedCourseSupplier = selectedCourseSupplier;
            this.attendanceService = attendanceService;
            editor.getStyleClass().add("input-compact");
            editor.setOnAction(e -> commitEdit(parse(editor.getText())));
            editor.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (Boolean.FALSE.equals(isFocused) && isEditing()) {
                    commitEdit(parse(editor.getText()));
                }
            });
        }

        @Override
        public void startEdit() {
            super.startEdit();
            Integer value = getItem();
            editor.setText(value == null ? "" : String.valueOf(value));
            setText(null);
            setGraphic(editor);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            editor.requestFocus();
            editor.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            renderBadge(getItem(), isEmpty());
        }

        @Override
        protected void updateItem(Integer value, boolean empty) {
            super.updateItem(value, empty);
            if (isEditing()) {
                setText(null);
                setGraphic(editor);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                return;
            }
            renderBadge(value, empty);
        }

        private void renderBadge(Integer value, boolean empty) {
            if (empty) {
                setText(null);
                setGraphic(null);
                return;
            }
            int hours = value == null ? 0 : value;

            CourseDTO selectedCourse = selectedCourseSupplier == null ? null : selectedCourseSupplier.get();
            Integer weeklyTotalHours = selectedCourse == null ? null : selectedCourse.getWeeklyTotalHours();
            boolean critical = attendanceService != null && attendanceService.isAttendanceCriticalByHours(weeklyTotalHours, hours);
            boolean warning = !critical && attendanceService != null && attendanceService.isAttendanceWarningByHours(weeklyTotalHours, hours);

            String badgeClass = critical ? "badge-danger" : (warning ? "badge-warning" : "badge-neutral");
            Label badge = new Label(String.valueOf(hours));
            badge.getStyleClass().addAll("badge", badgeClass);

            setText(null);
            setGraphic(badge);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);
        }

        private static Integer parse(String text) {
            String trimmed = text == null ? "" : text.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            try {
                return Integer.valueOf(trimmed);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }
}
