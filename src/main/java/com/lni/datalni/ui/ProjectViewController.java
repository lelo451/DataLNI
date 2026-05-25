package com.lni.datalni.ui;

import com.lni.datalni.security.CurrentUser;
import com.lni.datalni.service.ProjectService;
import com.lni.datalni.service.dto.ProjectCriteria;
import com.lni.datalni.service.dto.ProjectDto;
import com.lni.datalni.ui.imports.ImportField;
import com.lni.datalni.ui.imports.ImportValues;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.Cells;
import com.lni.datalni.ui.support.Dialogs;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.Pagers;
import com.lni.datalni.ui.support.RowSelection;
import com.lni.datalni.ui.support.SdgCatalog;
import com.lni.datalni.ui.support.StatusService;
import com.lni.datalni.ui.support.Tables;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Projects module: searchable table with CRUD; ODS shown as a label. */
@Component
@Scope("prototype")
public class ProjectViewController {

    private final ProjectService projectService;
    private final CurrentUser currentUser;
    private final StageManager stageManager;
    private final AsyncRunner async;
    private final StatusService status;

    @FXML private TextField searchField;
    @FXML private TableView<ProjectDto> table;
    @FXML private TableColumn<ProjectDto, Integer> idColumn;
    @FXML private TableColumn<ProjectDto, String> titleColumn;
    @FXML private TableColumn<ProjectDto, String> coordinatorColumn;
    @FXML private TableColumn<ProjectDto, String> eprotocolColumn;
    @FXML private TableColumn<ProjectDto, String> odsColumn;
    @FXML private Label countLabel;
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
    private RowSelection<ProjectDto> selection;
    private ProjectCriteria criteria = ProjectCriteria.empty();
    private int page = 0;
    private int totalPages = 0;

    public ProjectViewController(ProjectService projectService, CurrentUser currentUser,
                                 StageManager stageManager, AsyncRunner async, StatusService status) {
        this.projectService = projectService;
        this.currentUser = currentUser;
        this.stageManager = stageManager;
        this.async = async;
        this.status = status;
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(Cells.of(ProjectDto::getId));
        titleColumn.setCellValueFactory(Cells.of(ProjectDto::getTitle));
        coordinatorColumn.setCellValueFactory(Cells.of(ProjectDto::getCoordinator));
        eprotocolColumn.setCellValueFactory(Cells.of(ProjectDto::getEprotocol));
        odsColumn.setCellValueFactory(Cells.of(p -> SdgCatalog.label(p.getOds())));

        // Stretch columns to fill the full table width (prefWidth acts as the share).
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Tables.placeholder(table, Messages.get("table.empty"));
        searchField.setOnAction(e -> onSearch());

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
        criteria = new ProjectCriteria(searchField.getText(), null);
        page = 0;
        load();
    }

    @FXML
    private void onRefresh() {
        searchField.clear();
        criteria = ProjectCriteria.empty();
        page = 0;
        load();
    }

    private void load() {
        async.run(() -> projectService.search(criteria, PageRequest.of(page, PAGE_SIZE, Sort.by("id"))),
                this::populate);
    }

    private void populate(Page<ProjectDto> result) {
        totalPages = result.getTotalPages();
        // After deletes the current page may no longer exist; step back and reload.
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

    @FXML
    private void onNew() {
        stageManager.<ProjectFormController>openModal("project-form.fxml", Messages.get("project.form.new.title"), form -> {
            form.setModel(null);
            form.setOnSaved(this::load);
        });
    }

    @FXML
    private void onEdit() {
        List<ProjectDto> chosen = selection.getSelected();
        if (chosen.size() == 1) {
            editRow(chosen.get(0));
        }
    }

    private void editRow(ProjectDto project) {
        stageManager.<ProjectFormController>openModal("project-form.fxml", Messages.get("project.form.edit.title"), form -> {
            form.setModel(project);
            form.setOnSaved(this::load);
        });
    }

    @FXML
    private void onImport() {
        stageManager.openImport(Messages.get("import.projects.title"), List.of(
                ImportField.required("title", Messages.get("field.title")),
                ImportField.optional("ods", Messages.get("field.ods")),
                ImportField.optional("eprotocol", Messages.get("field.eprotocol")),
                ImportField.optional("coordinator", Messages.get("field.coordinator"))),
                this::importRow, this::load);
    }

    private Optional<String> importRow(Map<String, String> values) {
        try {
            projectService.create(ProjectDto.builder()
                    .title(ImportValues.text(values.get("title")))
                    .ods(ImportValues.integer(values.get("ods")))
                    .eprotocol(ImportValues.text(values.get("eprotocol")))
                    .coordinator(ImportValues.text(values.get("coordinator")))
                    .build());
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(ErrorTranslator.toMessage(e));
        }
    }

    @FXML
    private void onDelete() {
        List<ProjectDto> chosen = selection.getSelected();
        if (chosen.isEmpty()) {
            return;
        }
        String message = chosen.size() == 1
                ? Messages.get("project.delete.message", String.valueOf(chosen.get(0).getId()))
                : Messages.get("delete.confirmMany", String.valueOf(chosen.size()));
        if (Dialogs.confirm(Messages.get("project.delete.title"), message)) {
            int count = chosen.size();
            async.run(() -> {
                for (ProjectDto project : chosen) {
                    projectService.delete(project.getId());
                }
                return null;
            }, ok -> {
                load();
                status.info(Messages.get("status.deleted", String.valueOf(count)));
            });
        }
    }
}
