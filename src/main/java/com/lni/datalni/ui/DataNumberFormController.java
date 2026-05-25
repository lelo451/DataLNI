package com.lni.datalni.ui;

import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Formats;
import com.lni.datalni.ui.support.FormValidation;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.Spinners;
import com.lni.datalni.ui.support.StagingSupport;
import jakarta.validation.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/** Create (add several, save all) / edit form for a {@link DataNumberDto}. */
@Component
@Scope("prototype")
public class DataNumberFormController implements DialogAware {

    private final DataNumberService dataNumberService;
    private final AsyncRunner async;
    private final Validator validator;

    @FXML private TabPane tabPane;
    @FXML private Tab listTab;
    @FXML private ListView<DataNumberDto> pendingList;
    @FXML private ComboBox<GraphDto> graphCombo;
    @FXML private ComboBox<Integer> monthCombo;
    @FXML private Spinner<Integer> yearSpinner;
    @FXML private TextField valueField;
    @FXML private TextField clazzField;
    @FXML private Label graphIdError;
    @FXML private Label monthError;
    @FXML private Label yearError;
    @FXML private Label valueError;
    @FXML private Label clazzError;
    @FXML private Label errorLabel;
    @FXML private Button addButton;
    @FXML private Button saveAllButton;
    @FXML private Button saveButton;

    private Stage dialogStage;
    private Runnable onSaved;
    private DataNumberDto model;
    private StagingSupport<DataNumberDto> staging;
    private FormValidation<DataNumberDto> validation;

    public DataNumberFormController(DataNumberService dataNumberService, AsyncRunner async,
                                    Validator validator) {
        this.dataNumberService = dataNumberService;
        this.async = async;
        this.validator = validator;
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
        validation = new FormValidation<>(validator);
        validation.field("graphId", graphIdError, graphCombo.valueProperty());
        validation.field("month", monthError, monthCombo.valueProperty());
        validation.field("year", yearError, yearSpinner.valueProperty());
        validation.field("value", valueError, valueField.textProperty());
        validation.field("clazz", clazzError, clazzField.textProperty());
        staging = new StagingSupport<>(tabPane, listTab, pendingList,
                addButton, saveAllButton, saveButton, async, this::summary);
    }

    private String summary(DataNumberDto dn) {
        String graph = dn.getGraphId() == null ? "?" : "#" + dn.getGraphId();
        String year = dn.getYear() == null ? "" : String.valueOf(dn.getYear());
        String clazz = dn.getClazz() == null ? "" : " · " + dn.getClazz();
        return graph + " · " + year + " · " + Formats.number(dn.getValue()) + clazz;
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

    /** Pre-selects the owning graph for new rows. */
    public void setGraphId(Integer graphId) {
        selectGraph(graphId);
    }

    /** @param model the row to edit, or {@code null} to create (add several). */
    public void setModel(DataNumberDto model) {
        this.model = model;
        staging.setEditing(model != null);
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
    private void onAdd() {
        errorLabel.setVisible(false);
        DataNumberDto dto = buildDto(false);
        if (!validateForm(dto)) {
            return;
        }
        staging.add(dto);
        valueField.clear();   // keep graph/month/year/class for the next entry
        valueField.requestFocus();
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
        DataNumberDto dto = buildDto(true);
        if (!validateForm(dto)) {
            return;
        }
        async.run(() -> dataNumberService.update(dto),
                saved -> finish(),
                error -> showError(ErrorTranslator.toMessage(error)));
    }

    /**
     * Validates the DTO against its bean constraints, then refines the {@code value} message:
     * a non-blank but unparseable amount reads "invalid number" rather than "required".
     */
    private boolean validateForm(DataNumberDto dto) {
        boolean valid = validation.validate(dto);
        String text = valueField.getText();
        if (text != null && !text.isBlank() && dto.getValue() == null) {
            validation.show("value", Messages.get("datanumber.value.invalid"));
            valid = false;
        }
        return valid;
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }

    private DataNumberDto buildDto(boolean keepId) {
        GraphDto selectedGraph = graphCombo.getValue();
        return DataNumberDto.builder()
                .id(keepId && model != null ? model.getId() : null)
                .graphId(selectedGraph == null ? null : selectedGraph.getId())
                .month(monthCombo.getValue())
                .year(yearSpinner.getValue())
                .value(parseValueOrNull())
                .clazz(emptyToNull(clazzField.getText()))
                .build();
    }

    /** Parses the amount, returning {@code null} when blank or unparseable (flagged later). */
    private BigDecimal parseValueOrNull() {
        String text = valueField.getText();
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Formats.parseNumber(text);
        } catch (Exception ex) {
            return null;
        }
    }

    private Optional<String> persist(DataNumberDto dto) {
        try {
            dataNumberService.create(dto);
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
