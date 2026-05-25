package com.lni.datalni.ui;

import com.lni.datalni.service.ProjectService;
import com.lni.datalni.service.dto.ProjectDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.ErrorTranslator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Create/edit form for a {@link ProjectDto}. */
@Component
@Scope("prototype")
public class ProjectFormController implements DialogAware {

    private final ProjectService projectService;
    private final AsyncRunner async;

    @FXML private Spinner<Integer> odsSpinner;
    @FXML private TextField eprotocolField;
    @FXML private TextField titleField;
    @FXML private TextField coordinatorField;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private Runnable onSaved;
    private ProjectDto model;

    public ProjectFormController(ProjectService projectService, AsyncRunner async) {
        this.projectService = projectService;
        this.async = async;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        odsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 17, 1));
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
    private void onSave() {
        errorLabel.setVisible(false);
        ProjectDto dto = ProjectDto.builder()
                .id(model == null ? null : model.getId())
                .ods(odsSpinner.getValue())
                .eprotocol(emptyToNull(eprotocolField.getText()))
                .title(emptyToNull(titleField.getText()))
                .coordinator(emptyToNull(coordinatorField.getText()))
                .build();
        async.run(
                () -> model == null ? projectService.create(dto) : projectService.update(dto),
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
