package com.lni.datalni.ui.support;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

/**
 * Drives the "add to a list, then Save all" flow shared by the create forms: a pending
 * {@link ListView} (double-click to remove), a tab whose title shows the count, and the
 * New (add / save-all) vs Edit (single save) button toggle.
 *
 * @param <D> the staged DTO type
 */
public final class StagingSupport<D> {

    private final ObservableList<D> pending = FXCollections.observableArrayList();
    private final TabPane tabPane;
    private final Tab listTab;
    private final Button addButton;
    private final Button saveAllButton;
    private final Button saveButton;
    private final AsyncRunner async;

    public StagingSupport(TabPane tabPane, Tab listTab, ListView<D> list,
                          Button addButton, Button saveAllButton, Button saveButton,
                          AsyncRunner async, Function<D, String> summary) {
        this.tabPane = tabPane;
        this.listTab = listTab;
        this.addButton = addButton;
        this.saveAllButton = saveAllButton;
        this.saveButton = saveButton;
        this.async = async;

        list.setItems(pending);
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(D item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : summary.apply(item));
            }
        });
        list.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                D selected = list.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    pending.remove(selected);
                }
            }
        });
        pending.addListener((ListChangeListener<D>) c -> updateTabTitle());
        updateTabTitle();
    }

    /**
     * New mode shows add/save-all; edit mode shows a single save and drops the list tab.
     * In new mode Enter triggers "add to list" (fast entry); save-all stays a deliberate click.
     */
    public void setEditing(boolean editing) {
        addButton.setVisible(!editing);
        addButton.setManaged(!editing);
        addButton.setDefaultButton(!editing);
        saveAllButton.setVisible(!editing);
        saveAllButton.setManaged(!editing);
        saveAllButton.setDefaultButton(false);
        saveButton.setVisible(editing);
        saveButton.setManaged(editing);
        saveButton.setDefaultButton(editing);
        if (editing) {
            tabPane.getTabs().remove(listTab);
        }
    }

    public void add(D item) {
        pending.add(item);
    }

    public boolean isEmpty() {
        return pending.isEmpty();
    }

    public void saveAll(Function<D, Optional<String>> persister, Runnable onComplete) {
        Batch.saveAll(async, new ArrayList<>(pending), persister, onComplete);
    }

    private void updateTabTitle() {
        listTab.setText(Messages.get("batch.listTab", String.valueOf(pending.size())));
    }
}
