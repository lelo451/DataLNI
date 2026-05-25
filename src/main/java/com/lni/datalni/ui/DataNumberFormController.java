package com.lni.datalni.ui;

import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.dto.GraphDto;
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
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

/** Create/edit form for a {@link DataNumberDto}; the parent graph can be reassigned. */
@Component
@Scope("prototype")
public class DataNumberFormController implements DialogAware {

    private final DataNumberService dataNumberService;
    private final AsyncRunner async;

    @FXML private ComboBox<GraphDto> graphCombo;
    @FXML private ComboBox<Integer> monthCombo;
    @FXML private Spinner<Integer> yearSpinner;
    @FXML private TextField valueField;
    @FXML private TextField clazzField;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private Runnable onSaved;
    private DataNumberDto model;

    public DataNumberFormController(DataNumberService dataNumberService, AsyncRunner async) {
        this.dataNumberService = dataNumberService;
        this.async = async;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        graphCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(GraphDto graph) {
                if (graph == null) {
                    return "";
                }
                return graph.getTitle() == null || graph.getTitle().isBlank()
                        ? "#" + graph.getId()
                        : "#" + graph.getId() + " - " + graph.getTitle();
            }

            @Override
            public GraphDto fromString(String text) {
                return null;
            }
        });
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

    /** The graphs the number may belong to (loaded by the caller). Call before setGraphId/setModel. */
    public void setGraphs(List<GraphDto> graphs) {
        graphCombo.setItems(FXCollections.observableArrayList(graphs));
    }

    /** Pre-selects the owning graph for a new row. */
    public void setGraphId(Integer graphId) {
        selectGraph(graphId);
    }

    /** @param model the row to edit, or {@code null} to create. */
    public void setModel(DataNumberDto model) {
        this.model = model;
        if (model != null) {
            selectGraph(model.getGraphId());
            if (model.getMonth() != null) {
                monthCombo.setValue(model.getMonth());
            }
            if (model.getYear() != null) {
                yearSpinner.getValueFactory().setValue(model.getYear());
            }
            valueField.setText(Formats.number(model.getValue()));
            clazzField.setText(model.getClazz());
        }
    }

    private void selectGraph(Integer graphId) {
        if (graphId == null) {
            return;
        }
        graphCombo.getItems().stream()
                .filter(g -> graphId.equals(g.getId()))
                .findFirst()
                .ifPresent(graphCombo::setValue);
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
        GraphDto selectedGraph = graphCombo.getValue();
        DataNumberDto dto = DataNumberDto.builder()
                .id(model == null ? null : model.getId())
                .graphId(selectedGraph == null ? null : selectedGraph.getId())
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
