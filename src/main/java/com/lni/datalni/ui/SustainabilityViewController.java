package com.lni.datalni.ui;

import com.lni.datalni.security.CurrentUser;
import com.lni.datalni.service.SustainabilityService;
import com.lni.datalni.service.dto.SustainabilityCriteria;
import com.lni.datalni.service.dto.SustainabilityDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.Dialogs;
import com.lni.datalni.ui.support.LinkOpener;
import com.lni.datalni.ui.support.SdgCatalog;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/** Sustainability module: searchable table, ODS label, clickable link, date column. */
@Component
@Scope("prototype")
public class SustainabilityViewController {

    private final SustainabilityService sustainabilityService;
    private final CurrentUser currentUser;
    private final StageManager stageManager;
    private final AsyncRunner async;

    @FXML private TextField searchField;
    @FXML private TextField yearField;
    @FXML private TableView<SustainabilityDto> table;
    @FXML private TableColumn<SustainabilityDto, Integer> idColumn;
    @FXML private TableColumn<SustainabilityDto, Integer> yearColumn;
    @FXML private TableColumn<SustainabilityDto, String> titleColumn;
    @FXML private TableColumn<SustainabilityDto, String> authorColumn;
    @FXML private TableColumn<SustainabilityDto, String> odsColumn;
    @FXML private TableColumn<SustainabilityDto, String> publishedColumn;
    @FXML private TableColumn<SustainabilityDto, SustainabilityDto> linkColumn;
    @FXML private Button newButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    public SustainabilityViewController(SustainabilityService sustainabilityService,
                                        CurrentUser currentUser, StageManager stageManager,
                                        AsyncRunner async) {
        this.sustainabilityService = sustainabilityService;
        this.currentUser = currentUser;
        this.stageManager = stageManager;
        this.async = async;
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        yearColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getYear()));
        titleColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTitle()));
        authorColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getAuthor()));
        odsColumn.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(SdgCatalog.label(c.getValue().getOds())));
        publishedColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getPublished() == null ? "" : c.getValue().getPublished().toString()));

        linkColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        linkColumn.setCellFactory(col -> new LinkCell());

        boolean canEdit = currentUser.canEdit();
        newButton.setVisible(canEdit);
        editButton.setVisible(canEdit);
        deleteButton.setVisible(canEdit);

        load();
    }

    @FXML
    private void onSearch() {
        Integer year = parseYear(yearField.getText());
        async.run(() -> sustainabilityService.search(
                        new SustainabilityCriteria(searchField.getText(), year, null)),
                this::populate);
    }

    @FXML
    private void onRefresh() {
        searchField.clear();
        yearField.clear();
        load();
    }

    private void load() {
        async.run(sustainabilityService::list, this::populate);
    }

    private void populate(List<SustainabilityDto> rows) {
        table.setItems(FXCollections.observableArrayList(rows));
    }

    private static Integer parseYear(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @FXML
    private void onNew() {
        stageManager.<SustainabilityFormController>openModal(
                "sustainability-form.fxml", "New publication", form -> {
                    form.setModel(null);
                    form.setOnSaved(this::load);
                });
    }

    @FXML
    private void onEdit() {
        SustainabilityDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        stageManager.<SustainabilityFormController>openModal(
                "sustainability-form.fxml", "Edit publication", form -> {
                    form.setModel(selected);
                    form.setOnSaved(this::load);
                });
    }

    @FXML
    private void onDelete() {
        SustainabilityDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (Dialogs.confirm("Delete publication", "Delete publication #" + selected.getId() + "?")) {
            async.run(() -> {
                sustainabilityService.delete(selected.getId());
                return null;
            }, ok -> load());
        }
    }

    /** Renders the link as a hyperlink that opens in the system browser. */
    private static final class LinkCell extends TableCell<SustainabilityDto, SustainabilityDto> {
        private final Hyperlink hyperlink = new Hyperlink();

        @Override
        protected void updateItem(SustainabilityDto item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.getLink() == null || item.getLink().isBlank()) {
                setGraphic(null);
                return;
            }
            hyperlink.setText(item.getLink());
            hyperlink.setOnAction(e -> LinkOpener.open(item.getLink()));
            setGraphic(hyperlink);
        }
    }
}
