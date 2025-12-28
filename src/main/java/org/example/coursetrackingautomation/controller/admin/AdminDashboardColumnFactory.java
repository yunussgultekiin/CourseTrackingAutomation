package org.example.coursetrackingautomation.controller.admin;

import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;

/**
 * Factory for building Admin Dashboard {@link TableColumn} instances.
 *
 * <p>Creates standard value columns and a reusable action column containing edit/delete buttons.</p>
 */
public final class AdminDashboardColumnFactory {

    private final Function<String, String> enrollmentStatusTranslator;

    /**
     * @param enrollmentStatusTranslator translator used for displaying enrollment status codes
     */
    public AdminDashboardColumnFactory(Function<String, String> enrollmentStatusTranslator) {
        this.enrollmentStatusTranslator = enrollmentStatusTranslator;
    }

    /**
     * Creates a simple value column.
     *
     * @param title column title
     * @param mapper maps a row object to a cell value
     * @param <S> row type
     * @param <T> cell value type
     * @return configured column
     */
    public <S, T> TableColumn<S, T> createColumn(String title, Function<S, T> mapper) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(mapper.apply(cellData.getValue())));

        if ("ID".equals(title)) {
            column.setMinWidth(60);
            column.setPrefWidth(80);
            column.setMaxWidth(100);
        } else {
            int minWidth = 120;
            int prefWidth = 180;

            switch (title) {
                case "Kullanıcı Adı" -> {
                    minWidth = 140;
                    prefWidth = 200;
                }
                case "Ad", "Soyad" -> {
                    minWidth = 130;
                    prefWidth = 170;
                }
                case "E-posta" -> {
                    minWidth = 240;
                    prefWidth = 320;
                }
                case "Ders Kodu" -> {
                    minWidth = 140;
                    prefWidth = 180;
                }
                case "Ders Adı" -> {
                    minWidth = 280;
                    prefWidth = 380;
                }
                case "Akademisyen" -> {
                    minWidth = 220;
                    prefWidth = 280;
                }
                case "Öğrenci" -> {
                    minWidth = 220;
                    prefWidth = 280;
                }
                case "Ders" -> {
                    minWidth = 260;
                    prefWidth = 360;
                }
                case "Hafta" -> {
                    minWidth = 90;
                    prefWidth = 110;
                }
                case "Kredi", "Kota" -> {
                    minWidth = 90;
                    prefWidth = 110;
                }
                case "Tarih" -> {
                    minWidth = 160;
                    prefWidth = 200;
                }
                default -> {
                    // keep defaults
                }
            }

            column.setMinWidth(minWidth);
            column.setPrefWidth(prefWidth);
        }
        return column;
    }

    /**
     * Creates the action column containing edit/delete buttons.
     *
     * @param actionType entity type identifier (e.g. user/course/enrollment)
     * @param onEdit edit callback
     * @param onDelete delete callback
     * @param <T> row type
     * @return action column
     */
    public <T> TableColumn<T, String> createActionColumn(String actionType, Consumer<T> onEdit, Consumer<T> onDelete) {
        TableColumn<T, String> col = new TableColumn<>("İşlemler");
        col.setCellFactory(createActionCellFactory(actionType, onEdit, onDelete));
        col.setMinWidth(110);
        col.setPrefWidth(130);
        col.setMaxWidth(160);
        col.setResizable(false);
        return col;
    }

    public Function<String, String> getEnrollmentStatusTranslator() {
        return enrollmentStatusTranslator;
    }

    private <T> Callback<TableColumn<T, String>, TableCell<T, String>> createActionCellFactory(
        String type,
        Consumer<T> onEdit,
        Consumer<T> onDelete
    ) {
        return column -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().addAll("icon-button", "icon-button-edit");
                deleteButton.getStyleClass().addAll("icon-button", "icon-button-delete");

                editButton.setGraphic(createEditIcon());
                deleteButton.setGraphic(createDeleteIcon());

                editButton.setTooltip(new Tooltip("Düzenle"));
                deleteButton.setTooltip(new Tooltip("Sil"));

                editButton.setOnAction(event -> {
                    T item = getTableRow() == null ? null : getTableRow().getItem();
                    if (item != null) {
                        onEdit.accept(item);
                    }
                });
                deleteButton.setOnAction(event -> {
                    T item = getTableRow() == null ? null : getTableRow().getItem();
                    if (item != null) {
                        onDelete.accept(item);
                    }
                });

                pane.setStyle("-fx-alignment: center; -fx-padding: 0 6px 0 6px;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
    }

    private static SVGPath createEditIcon() {
        SVGPath icon = new SVGPath();
        icon.getStyleClass().add("icon");
        icon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zm2.92 2.83H5v-.92l8.06-8.06.92.92L5.92 20.08zM20.71 7.04a1.003 1.003 0 0 0 0-1.42l-2.34-2.34a1.003 1.003 0 0 0-1.42 0l-1.83 1.83 3.75 3.75 1.84-1.82z");
        icon.setScaleX(0.85);
        icon.setScaleY(0.85);
        return icon;
    }

    private static SVGPath createDeleteIcon() {
        SVGPath icon = new SVGPath();
        icon.getStyleClass().add("icon");
        icon.setContent("M6 19a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V7H6v12zm3.5-9h1v8h-1V10zm4 0h1v8h-1V10zM15.5 4l-1-1h-5l-1 1H5v2h14V4z");
        icon.setScaleX(0.85);
        icon.setScaleY(0.85);
        return icon;
    }
}
