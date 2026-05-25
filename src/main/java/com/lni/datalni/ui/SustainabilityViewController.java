package com.lni.datalni.ui;

import com.lni.datalni.security.CurrentUser;
import com.lni.datalni.service.SustainabilityService;
import com.lni.datalni.service.dto.SustainabilityCriteria;
import com.lni.datalni.service.dto.SustainabilityDto;
import com.lni.datalni.ui.imports.ImportField;
import com.lni.datalni.ui.imports.ImportValues;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.Cells;
import com.lni.datalni.ui.support.Dialogs;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Formats;
import com.lni.datalni.ui.support.LinkOpener;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.Pagers;
import com.lni.datalni.ui.support.RowSelection;
import com.lni.datalni.ui.support.SdgCatalog;
import com.lni.datalni.ui.support.StatusService;
import com.lni.datalni.ui.support.Tables;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Sustainability module: searchable table, ODS label, clickable link, date column. */
@Component
@Scope("prototype")
public class SustainabilityViewController {

    private final SustainabilityService sustainabilityService;
    private final CurrentUser currentUser;
    private final StageManager stageManager;
    private final AsyncRunner async;
    private final StatusService status;

    @FXML private TextField searchField;
    @FXML private TextField yearField;
    @FXML private Label countLabel;
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
    @FXML private Button importButton;
    @FXML private Label pageInfo;
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;

    private static final int PAGE_SIZE = 50;
    private RowSelection<SustainabilityDto> selection;
    private SustainabilityCriteria criteria = SustainabilityCriteria.empty();
    private int page = 0;
    private int totalPages = 0;

    public SustainabilityViewController(SustainabilityService sustainabilityService,
                                        CurrentUser currentUser, StageManager stageManager,
                                        AsyncRunner async, StatusService status) {
        this.sustainabilityService = sustainabilityService;
        this.currentUser = currentUser;
        this.stageManager = stageManager;
        this.async = async;
        this.status = status;
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(Cells.of(SustainabilityDto::getId));
        yearColumn.setCellValueFactory(Cells.of(SustainabilityDto::getYear));
        titleColumn.setCellValueFactory(Cells.of(SustainabilityDto::getTitle));
        authorColumn.setCellValueFactory(Cells.of(SustainabilityDto::getAuthor));
        odsColumn.setCellValueFactory(Cells.of(s -> SdgCatalog.label(s.getOds())));
        publishedColumn.setCellValueFactory(Cells.of(s -> Formats.date(s.getPublished())));

        linkColumn.setCellValueFactory(Cells.of(s -> s));
        linkColumn.setCellFactory(col -> new LinkCell());

        // Stretch columns to fill the full table width (prefWidth acts as the share).
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Tables.placeholder(table, Messages.get("table.empty"));
        searchField.setOnAction(e -> onSearch());
        yearField.setOnAction(e -> onSearch());

        boolean canEdit = currentUser.canEdit();
        newButton.setVisible(canEdit);
        editButton.setVisible(canEdit);
        deleteButton.setVisible(canEdit);
        importButton.setVisible(canEdit);
        if (canEdit) {
            Tables.onDoubleClick(table, this::editRow);
            Tables.onDeleteKey(table, this::onDelete);
        }

        selection = RowSelection.install(table);
        selection.setOnChange(this::updateActionButtons);
        updateActionButtons();

        load();
    }

    /** New/Edit act on a single row; only Delete stays enabled when several are selected. */
    private void updateActionButtons() {
        int count = selection.count();
        newButton.setDisable(count > 1);
        editButton.setDisable(count != 1);
        deleteButton.setDisable(count == 0);
    }

    @FXML
    private void onSearch() {
        Integer year = parseYear(yearField.getText());
        criteria = new SustainabilityCriteria(searchField.getText(), year, null);
        page = 0;
        load();
    }

    @FXML
    private void onRefresh() {
        searchField.clear();
        yearField.clear();
        criteria = SustainabilityCriteria.empty();
        page = 0;
        load();
    }

    private void load() {
        async.run(() -> sustainabilityService.search(criteria, PageRequest.of(page, PAGE_SIZE)),
                this::populate);
    }

    private void populate(Page<SustainabilityDto> result) {
        totalPages = result.getTotalPages();
        if (page > 0 && page >= totalPages) {
            page = Math.max(0, totalPages - 1);
            load();
            return;
        }
        table.setItems(FXCollections.observableArrayList(result.getContent()));
        countLabel.setText(Messages.get("table.count", String.valueOf(result.getTotalElements())));
        Pagers.update(firstPageButton, prevPageButton, nextPageButton, lastPageButton,
                pageInfo, page, totalPages);
    }

    @FXML
    private void onFirstPage() {
        goTo(0);
    }

    @FXML
    private void onPrevPage() {
        goTo(page - 1);
    }

    @FXML
    private void onNextPage() {
        goTo(page + 1);
    }

    @FXML
    private void onLastPage() {
        goTo(totalPages - 1);
    }

    private void goTo(int target) {
        int clamped = Math.max(0, Math.min(target, totalPages - 1));
        if (clamped != page) {
            page = clamped;
            load();
        }
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
                "sustainability-form.fxml", Messages.get("sustainability.form.new.title"), form -> {
                    form.setModel(null);
                    form.setOnSaved(this::load);
                });
    }

    @FXML
    private void onEdit() {
        List<SustainabilityDto> chosen = selection.getSelected();
        if (chosen.size() == 1) {
            editRow(chosen.get(0));
        }
    }

    private void editRow(SustainabilityDto item) {
        stageManager.<SustainabilityFormController>openModal(
                "sustainability-form.fxml", Messages.get("sustainability.form.edit.title"), form -> {
                    form.setModel(item);
                    form.setOnSaved(this::load);
                });
    }

    @FXML
    private void onImport() {
        stageManager.openImport(Messages.get("import.sustainability.title"), List.of(
                ImportField.required("year", Messages.get("field.year")),
                ImportField.required("title", Messages.get("field.title")),
                ImportField.optional("ods", Messages.get("field.ods")),
                ImportField.optional("author", Messages.get("field.author")),
                ImportField.optional("link", Messages.get("field.link")),
                ImportField.optional("published", Messages.get("field.published"))),
                this::importRow, this::load);
    }

    private Optional<String> importRow(Map<String, String> values) {
        try {
            sustainabilityService.create(SustainabilityDto.builder()
                    .year(ImportValues.integer(values.get("year")))
                    .ods(ImportValues.integer(values.get("ods")))
                    .title(ImportValues.text(values.get("title")))
                    .author(ImportValues.text(values.get("author")))
                    .link(ImportValues.text(values.get("link")))
                    .published(ImportValues.date(values.get("published")))
                    .build());
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(ErrorTranslator.toMessage(e));
        }
    }

    @FXML
    private void onDelete() {
        List<SustainabilityDto> chosen = selection.getSelected();
        if (chosen.isEmpty()) {
            return;
        }
        String message = chosen.size() == 1
                ? Messages.get("sustainability.delete.message", String.valueOf(chosen.get(0).getId()))
                : Messages.get("delete.confirmMany", String.valueOf(chosen.size()));
        if (Dialogs.confirm(Messages.get("sustainability.delete.title"), message)) {
            int count = chosen.size();
            async.run(() -> {
                for (SustainabilityDto item : chosen) {
                    sustainabilityService.delete(item.getId());
                }
                return null;
            }, ok -> {
                load();
                status.info(Messages.get("status.deleted", String.valueOf(count)));
            });
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
