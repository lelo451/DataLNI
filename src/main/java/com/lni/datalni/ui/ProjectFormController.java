package com.lni.datalni.ui;

import com.lni.datalni.service.ProjectService;
import com.lni.datalni.service.dto.ProjectDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.Spinners;
import com.lni.datalni.ui.support.StagingSupport;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Create (add several, save all) / edit form for a {@link ProjectDto}. */
@Component
@Scope("prototype")
public class ProjectFormController implements DialogAware {

    private final ProjectService projectService;
    private final AsyncRunner async;

    @FXML private TabPane tabPane;
    @FXML private Tab listTab;
    @FXML private ListView<ProjectDto> pendingList;
    @FXML private Spinner<Integer> odsSpinner;
    @FXML private TextField eprotocolField;
    @FXML private TextField titleField;
    @FXML private TextField coordinatorField;
    @FXML private Label errorLabel;
    @FXML private Button addButton;
    @FXML private Button saveAllButton;
    @FXML private Button saveButton;

    private Stage dialogStage;
    private Runnable onSaved;
    private ProjectDto model;
    private StagingSupport<ProjectDto> staging;

    public ProjectFormController(ProjectService projectService, AsyncRunner async) {
        this.projectService = projectService;
        this.async = async;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        Spinners.integer(odsSpinner, 1, 17, 1);
        staging = new StagingSupport<>(tabPane, listTab, pendingList,
                addButton, saveAllButton, saveButton, async, this::summary);
    }

    private String summary(ProjectDto p) {
        String title = p.getTitle() == null || p.getTitle().isBlank() ? "(sem título)" : p.getTitle();
        return p.getOds() == null ? title : "ODS " + p.getOds() + " · " + title;
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    public void setModel(ProjectDto model) {
        this.model = model;
        staging.setEditing(model != null);
        if (model != null) {
            if (model.getOds() != null) {
                odsSpinner.getValueFactory().setValue(model.getOds());
            }
            eprotocolField.setText(model.getEprotocol());
            titleField.setText(model.getTitle());
            coordinatorField.setText(model.getCoordinator());
        }
    }

    @FXML
    private void onAdd() {
        errorLabel.setVisible(false);
        staging.add(buildDto(false));
        eprotocolField.clear();
        titleField.clear();
        coordinatorField.clear();
        titleField.requestFocus();
    }

    @FXML
    private void onSaveAll() {
        if (staging.isEmpty()) {
            showError(Messages.get("batch.empty"));
            return;
        }
        staging.saveAll(this::persist, this::finish);
    }

    @FXML
    private void onSave() {
        errorLabel.setVisible(false);
        ProjectDto dto = buildDto(true);
        async.run(() -> projectService.update(dto),
                saved -> finish(),
                error -> showError(ErrorTranslator.toMessage(error)));
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }

    private ProjectDto buildDto(boolean keepId) {
        return ProjectDto.builder()
                .id(keepId && model != null ? model.getId() : null)
                .ods(odsSpinner.getValue())
                .eprotocol(emptyToNull(eprotocolField.getText()))
                .title(emptyToNull(titleField.getText()))
                .coordinator(emptyToNull(coordinatorField.getText()))
                .build();
    }

    private Optional<String> persist(ProjectDto dto) {
        try {
            projectService.create(dto);
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(ErrorTranslator.toMessage(e));
        }
    }

    private void finish() {
        if (onSaved != null) {
            onSaved.run();
        }
        dialogStage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
