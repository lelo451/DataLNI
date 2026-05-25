package com.lni.datalni.ui;

import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Formats;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.Spinners;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.IntStream;

/** Create/edit form for a {@link DataNumberDto}, scoped to a parent graph. */
@Component
@Scope("prototype")
public class DataNumberFormController implements DialogAware {

    private final DataNumberService dataNumberService;
    private final AsyncRunner async;

    @FXML private ComboBox<Integer> monthCombo;
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
        monthCombo.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(1, 12).boxed().toList()));
        monthCombo.setConverter(Formats.monthConverter());
        monthCombo.setValue(java.time.LocalDate.now().getMonthValue());
        Spinners.integer(yearSpinner, 1900, 2999, java.time.Year.now().getValue());
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
            if (model.getMonth() != null) {
                monthCombo.setValue(model.getMonth());
            }
            if (model.getYear() != null) {
                yearSpinner.getValueFactory().setValue(model.getYear());
            }
            valueField.setText(Formats.number(model.getValue()));
            clazzField.setText(model.getClazz());
            this.graphId = model.getGraphId();
        }
    }

    @FXML
    private void onSave() {
        errorLabel.setVisible(false);
        BigDecimal value;
        try {
            value = Formats.parseNumber(valueField.getText());
        } catch (Exception ex) {
            showError(Messages.get("datanumber.value.invalid"));
            return;
        }
        DataNumberDto dto = DataNumberDto.builder()
                .id(model == null ? null : model.getId())
                .graphId(graphId)
                .month(monthCombo.getValue())
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
