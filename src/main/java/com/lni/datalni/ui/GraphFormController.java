package com.lni.datalni.ui;

import com.lni.datalni.service.GraphService;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.ErrorTranslator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Create/edit form for a {@link GraphDto}. */
@Component
@Scope("prototype")
public class GraphFormController implements DialogAware {

    private final GraphService graphService;
    private final AsyncRunner async;

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private Runnable onSaved;
    private GraphDto model;

    public GraphFormController(GraphService graphService, AsyncRunner async) {
        this.graphService = graphService;
        this.async = async;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    /** @param model the row to edit, or {@code null} to create. */
    public void setModel(GraphDto model) {
        this.model = model;
        if (model != null) {
            titleField.setText(model.getTitle());
            descriptionField.setText(model.getDescription());
        }
    }

    @FXML
    private void onSave() {
        GraphDto dto = GraphDto.builder()
                .id(model == null ? null : model.getId())
                .title(emptyToNull(titleField.getText()))
                .description(emptyToNull(descriptionField.getText()))
                .build();
        errorLabel.setVisible(false);
        async.run(
                () -> model == null ? graphService.create(dto) : graphService.update(dto),
                saved -> {
                    if (onSaved != null) {
                        onSaved.run();
                    }
                    dialogStage.close();
                },
                error -> {
                    errorLabel.setText(ErrorTranslator.toMessage(error));
                    errorLabel.setVisible(true);
                });
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
