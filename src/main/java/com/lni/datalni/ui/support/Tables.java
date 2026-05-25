package com.lni.datalni.ui.support;

import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;

import java.util.function.Consumer;

/** Small conveniences shared by the module tables: placeholder, double-click and Delete key. */
public final class Tables {

    private Tables() {
    }

    public static void placeholder(TableView<?> table, String text) {
        table.setPlaceholder(new Label(text));
    }

    /** Runs {@code action} with the row that was double-clicked (if any). */
    public static <T> void onDoubleClick(TableView<T> table, Consumer<T> action) {
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                T selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    action.accept(selected);
                }
            }
        });
    }

    /** Runs {@code action} when Delete is pressed while the table has focus. */
    public static void onDeleteKey(TableView<?> table, Runnable action) {
        table.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                action.run();
            }
        });
    }
}
