package com.lni.datalni.ui;

import com.lni.datalni.security.CurrentUser;
import com.lni.datalni.service.ProjectService;
import com.lni.datalni.service.dto.ProjectCriteria;
import com.lni.datalni.service.dto.ProjectDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.Cells;
import com.lni.datalni.ui.support.Dialogs;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.SdgCatalog;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/** Projects module: searchable table with CRUD; ODS shown as a label. */
@Component
@Scope("prototype")
public class ProjectViewController {

    private final ProjectService projectService;
    private final CurrentUser currentUser;
    private final StageManager stageManager;
    private final AsyncRunner async;

    @FXML private TextField searchField;
    @FXML private TableView<ProjectDto> table;
    @FXML private TableColumn<ProjectDto, Integer> idColumn;
    @FXML private TableColumn<ProjectDto, String> titleColumn;
    @FXML private TableColumn<ProjectDto, String> coordinatorColumn;
    @FXML private TableColumn<ProjectDto, String> eprotocolColumn;
    @FXML private TableColumn<ProjectDto, String> odsColumn;
    @FXML private Button newButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    public ProjectViewController(ProjectService projectService, CurrentUser currentUser,
                                 StageManager stageManager, AsyncRunner async) {
        this.projectService = projectService;
        this.currentUser = currentUser;
        this.stageManager = stageManager;
        this.async = async;
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

        boolean canEdit = currentUser.canEdit();
        newButton.setVisible(canEdit);
        editButton.setVisible(canEdit);
        deleteButton.setVisible(canEdit);

        load();
    }

    @FXML
    private void onSearch() {
        async.run(() -> projectService.search(new ProjectCriteria(searchField.getText(), null)),
                this::populate);
    }

    @FXML
    private void onRefresh() {
        searchField.clear();
        load();
    }

    private void load() {
        async.run(projectService::list, this::populate);
    }

    private void populate(List<ProjectDto> rows) {
        table.setItems(FXCollections.observableArrayList(rows));
    }

    @FXML
    private void onNew() {
        stageManager.<ProjectFormController>openModal("project-form.fxml", "New project", form -> {
            form.setModel(null);
            form.setOnSaved(this::load);
        });
    }

    @FXML
    private void onEdit() {
        ProjectDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        stageManager.<ProjectFormController>openModal("project-form.fxml", "Edit project", form -> {
            form.setModel(selected);
            form.setOnSaved(this::load);
        });
    }

    @FXML
    private void onDelete() {
        ProjectDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (Dialogs.confirm(Messages.get("project.delete.title"),
                Messages.get("project.delete.message", String.valueOf(selected.getId())))) {
            async.run(() -> {
                projectService.delete(selected.getId());
                return null;
            }, ok -> load());
        }
    }
}
