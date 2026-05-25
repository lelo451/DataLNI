package com.lni.datalni.ui;

import com.lni.datalni.service.GraphService;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.FormValidation;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.StagingSupport;
import jakarta.validation.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Create (add several, save all) / edit form for a {@link GraphDto}. */
@Component
@Scope("prototype")
public class GraphFormController implements DialogAware {

    private final GraphService graphService;
    private final AsyncRunner async;
    private final Validator validator;

    @FXML private TabPane tabPane;
    @FXML private Tab listTab;
    @FXML private ListView<GraphDto> pendingList;
    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private Label titleError;
    @FXML private Label descriptionError;
    @FXML private Label errorLabel;
    @FXML private Button addButton;
    @FXML private Button saveAllButton;
    @FXML private Button saveButton;

    private Stage dialogStage;
    private Runnable onSaved;
    private GraphDto model;
    private StagingSupport<GraphDto> staging;
    private FormValidation<GraphDto> validation;

    public GraphFormController(GraphService graphService, AsyncRunner async, Validator validator) {
        this.graphService = graphService;
        this.async = async;
        this.validator = validator;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        validation = new FormValidation<>(validator);
        validation.field("title", titleError, titleField.textProperty());
        validation.field("description", descriptionError, descriptionField.textProperty());
        staging = new StagingSupport<>(tabPane, listTab, pendingList,
                addButton, saveAllButton, saveButton, async, this::summary);
    }

    private String summary(GraphDto g) {
        return g.getTitle() == null || g.getTitle().isBlank() ? "(sem título)" : g.getTitle();
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    /** @param model the row to edit, or {@code null} to create (add several). */
    public void setModel(GraphDto model) {
        this.model = model;
        staging.setEditing(model != null);
        if (model != null) {
            titleField.setText(model.getTitle());
            descriptionField.setText(model.getDescription());
        }
    }

    @FXML
    private void onAdd() {
        errorLabel.setVisible(false);
        GraphDto dto = buildDto(false);
        if (!validation.validate(dto)) {
            return;
        }
        staging.add(dto);
        titleField.clear();
        descriptionField.clear();
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
        GraphDto dto = buildDto(true);
        if (!validation.validate(dto)) {
            return;
        }
        async.run(() -> graphService.update(dto),
                saved -> finish(),
                error -> showError(ErrorTranslator.toMessage(error)));
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }

    private GraphDto buildDto(boolean keepId) {
        return GraphDto.builder()
                .id(keepId && model != null ? model.getId() : null)
                .title(emptyToNull(titleField.getText()))
                .description(emptyToNull(descriptionField.getText()))
                .build();
    }

    private Optional<String> persist(GraphDto dto) {
        try {
            graphService.create(dto);
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
