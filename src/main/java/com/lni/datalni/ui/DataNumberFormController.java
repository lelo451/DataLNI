package com.lni.datalni.ui;

import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.dto.DataNumberDto;
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

import java.math.BigDecimal;

/** Create/edit form for a {@link DataNumberDto}, scoped to a parent graph. */
@Component
@Scope("prototype")
public class DataNumberFormController implements DialogAware {

    private final DataNumberService dataNumberService;
    private final AsyncRunner async;

    @FXML private Spinner<Integer> monthSpinner;
    @FXML private Spinner<Integer> yearSpinner;
    @FXML private TextField valueField;
    @FXML private TextField clazzField;
    @FXML private Label graphLabel;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private Runnable onSaved;
    private DataNumberDto model;
    private Integer graphId;

    public DataNumberFormController(DataNumberService dataNumberService, AsyncRunner async) {
        this.dataNumberService = dataNumberService;
        this.async = async;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        int currentYear = java.time.Year.now().getValue();
        monthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 1));
        yearSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2999, currentYear));
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    /** Required: the owning graph; its id pre-fills {@code CD_GRAFICO} on new rows. */
    public void setGraphId(Integer graphId) {
        this.graphId = graphId;
        graphLabel.setText("#" + graphId);
    }

    /** @param model the row to edit, or {@code null} to create. */
    public void setModel(DataNumberDto model) {
        this.model = model;
        if (model != null) {
            monthSpinner.getValueFactory().setValue(model.getMonth());
            yearSpinner.getValueFactory().setValue(model.getYear());
            valueField.setText(model.getValue() == null ? "" : model.getValue().toPlainString());
            clazzField.setText(model.getClazz());
            this.graphId = model.getGraphId();
        }
    }

    @FXML
    private void onSave() {
        errorLabel.setVisible(false);
        BigDecimal value;
        try {
            value = new BigDecimal(valueField.getText().trim());
        } catch (NumberFormatException | NullPointerException ex) {
            showError("Value must be a number (e.g. 1234.56).");
            return;
        }
        DataNumberDto dto = DataNumberDto.builder()
                .id(model == null ? null : model.getId())
                .graphId(graphId)
                .month(monthSpinner.getValue())
                .year(yearSpinner.getValue())
                .value(value)
                .clazz(emptyToNull(clazzField.getText()))
                .build();
        async.run(
                () -> model == null ? dataNumberService.create(dto) : dataNumberService.update(dto),
                saved -> {
                    if (onSaved != null) {
                        onSaved.run();
                    }
                    dialogStage.close();
                },
                error -> showError(ErrorTranslator.toMessage(error)));
    }

    @FXML
    private void onCancel() {
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
