package com.lni.datalni.ui.support;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds a leading checkbox column to a {@link TableView} for multi-row selection, with a
 * header checkbox that selects/clears all rows. Selection is tracked per row instance
 * (identity), reset whenever the table's items are replaced, and reported via a callback
 * so the toolbar can enable/disable actions by selection count.
 */
public final class RowSelection<T> {

    private final TableView<T> table;
    private final Map<T, BooleanProperty> selected = new IdentityHashMap<>();
    private final CheckBox headerBox = new CheckBox();
    private Runnable onChange = () -> { };
    private boolean bulk;

    private RowSelection(TableView<T> table) {
        this.table = table;
        // Required for the per-row CheckBoxTableCell checkboxes to be clickable (so the user
        // can check individual rows, not only all/none via the header). The data columns have
        // no editable cell factory, so they stay read-only.
        table.setEditable(true);

        TableColumn<T, Boolean> column = new TableColumn<>();
        column.setSortable(false);
        column.setReorderable(false);
        column.setResizable(false);
        column.setEditable(true);
        column.setMinWidth(40);
        column.setMaxWidth(40);
        column.setCellValueFactory(cd -> propertyFor(cd.getValue()));
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        column.setGraphic(headerBox);
        table.getColumns().add(0, column);

        headerBox.setOnAction(e -> setAll(headerBox.isSelected()));
        // Replacing the items list (reload) clears the selection.
        table.itemsProperty().addListener((obs, old, items) -> {
            selected.clear();
            fire();
        });
    }

    public static <T> RowSelection<T> install(TableView<T> table) {
        return new RowSelection<>(table);
    }

    /** Called whenever the selection (or item count) changes. */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    public List<T> getSelected() {
        List<T> result = new ArrayList<>();
        for (T item : table.getItems()) {
            BooleanProperty property = selected.get(item);
            if (property != null && property.get()) {
                result.add(item);
            }
        }
        return result;
    }

    public int count() {
        return getSelected().size();
    }

    private BooleanProperty propertyFor(T item) {
        return selected.computeIfAbsent(item, k -> {
            SimpleBooleanProperty property = new SimpleBooleanProperty(false);
            property.addListener((obs, was, is) -> fire());
            return property;
        });
    }

    private void setAll(boolean value) {
        bulk = true;
        try {
            for (T item : table.getItems()) {
                propertyFor(item).set(value);
            }
        } finally {
            bulk = false;
        }
        fire();
    }

    private void fire() {
        if (bulk) {
            return;
        }
        syncHeader();
        onChange.run();
    }

    private void syncHeader() {
        int total = table.getItems().size();
        int chosen = count();
        headerBox.setSelected(total > 0 && chosen == total);
        headerBox.setIndeterminate(chosen > 0 && chosen < total);
    }
}
