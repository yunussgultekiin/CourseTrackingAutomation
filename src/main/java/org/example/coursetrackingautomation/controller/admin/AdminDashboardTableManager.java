package org.example.coursetrackingautomation.controller.admin;

import java.util.List;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.scene.control.TableView;

/**
 * Handles generic {@link TableView} (re)configuration and data population for Admin Dashboard.
 *
 * <p>This component manages type casting for the shared table and reconfigures columns when the
 * current view mode changes.</p>
 */
public final class AdminDashboardTableManager {

    private final TableView<Object> dataTableView;
    private String tableConfiguredForViewMode;

    public AdminDashboardTableManager(TableView<Object> dataTableView) {
        this.dataTableView = dataTableView;
    }

    /**
     * Populates the shared table with the given dataset and installs the appropriate columns.
     *
     * @param data rows to render
     * @param columnConfigurator callback that adds columns for the given row type
     * @param currentViewMode active view identifier
     * @param <T> row type
     */
    public <T> void populateTable(List<T> data, Consumer<TableView<T>> columnConfigurator, String currentViewMode) {
        dataTableView.getItems().clear();
        dataTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        @SuppressWarnings("unchecked")
        TableView<T> specificTable = (TableView<T>) dataTableView;

        if (tableConfiguredForViewMode == null || !tableConfiguredForViewMode.equals(currentViewMode)) {
            specificTable.getColumns().clear();
            columnConfigurator.accept(specificTable);
            tableConfiguredForViewMode = currentViewMode;
        }

        specificTable.setItems(FXCollections.observableArrayList(data));
    }

    /**
     * Clears any row factory previously installed for the table.
     */
    public void clearRowFactory() {
        dataTableView.setRowFactory(null);
    }

    /**
     * @return the shared dashboard table view
     */
    public TableView<Object> getTableView() {
        return dataTableView;
    }
}
