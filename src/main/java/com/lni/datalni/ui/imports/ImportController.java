package com.lni.datalni.ui.imports;

import com.lni.datalni.ui.DialogAware;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.Cells;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Messages;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Column-mapping import dialog: maps each destination field to a source column
 * (auto-matched by name, editable, or ignored), previews the file, then imports the rows
 * off the FX thread, skipping invalid rows and listing them on a separate "Falhas" tab.
 */
@Component
@Scope("prototype")
public class ImportController implements DialogAware {

    private static final int PREVIEW_ROWS = 8;

    private final AsyncRunner async;

    @FXML private TabPane tabPane;
    @FXML private GridPane mappingGrid;
    @FXML private TableView<Map<String, String>> previewTable;
    @FXML private Tab failuresTab;
    @FXML private TableView<RowError> failuresTable;
    @FXML private TableColumn<RowError, Integer> failureLineColumn;
    @FXML private TableColumn<RowError, String> failureMessageColumn;
    @FXML private Button importButton;
    @FXML private Label errorLabel;
    @FXML private Label summaryLabel;

    private Stage dialogStage;
    private List<ImportField> fields;
    private ParsedFile source;
    private RowImporter importer;
    private Runnable onImported;
    private String ignoreLabel;
    private final Map<String, ComboBox<String>> combos = new LinkedHashMap<>();

    public ImportController(AsyncRunner async) {
        this.async = async;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        summaryLabel.setVisible(false);
        failureLineColumn.setCellValueFactory(Cells.of(RowError::line));
        failureMessageColumn.setCellValueFactory(Cells.of(RowError::message));
        failuresTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        failuresTab.setDisable(true);
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void configure(List<ImportField> fields, ParsedFile source,
                          RowImporter importer, Runnable onImported) {
        this.fields = fields;
        this.source = source;
        this.importer = importer;
        this.onImported = onImported;
        this.ignoreLabel = Messages.get("import.ignore");
        buildMapping();
        buildPreview();
    }

    private void buildMapping() {
        List<String> options = new ArrayList<>();
        options.add(ignoreLabel);
        options.addAll(source.columns());
        int row = 0;
        for (ImportField field : fields) {
            Label label = new Label(field.label() + (field.required() ? " *" : ""));
            ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(options));
            combo.setValue(autoMatch(field));
            combo.setPrefWidth(280);
            mappingGrid.add(label, 0, row);
            mappingGrid.add(combo, 1, row);
            combos.put(field.key(), combo);
            row++;
        }
    }

    /** Pre-selects the source column whose name matches the field's key or label. */
    private String autoMatch(ImportField field) {
        String key = normalize(field.key());
        String label = normalize(field.label());
        for (String column : source.columns()) {
            String normalized = normalize(column);
            if (normalized.equals(key) || normalized.equals(label)) {
                return column;
            }
        }
        return ignoreLabel;
    }

    private void buildPreview() {
        for (String column : source.columns()) {
            TableColumn<Map<String, String>, String> tableColumn = new TableColumn<>(column);
            tableColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get(column)));
            previewTable.getColumns().add(tableColumn);
        }
        previewTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        List<Map<String, String>> preview = source.rows()
                .subList(0, Math.min(PREVIEW_ROWS, source.rows().size()));
        previewTable.setItems(FXCollections.observableArrayList(preview));
    }

    @FXML
    private void onImport() {
        errorLabel.setVisible(false);

        Map<String, String> mapping = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        for (ImportField field : fields) {
            String column = combos.get(field.key()).getValue();
            if (column == null || column.equals(ignoreLabel)) {
                if (field.required()) {
                    missing.add(field.label());
                }
            } else {
                mapping.put(field.key(), column);
            }
        }
        if (!missing.isEmpty()) {
            showError(Messages.get("import.requiredMissing", String.join(", ", missing)));
            return;
        }

        importButton.setDisable(true);
        async.run(() -> runImport(mapping), this::onImportFinished, error -> {
            importButton.setDisable(false);
            showError(ErrorTranslator.toMessage(error));
        });
    }

    private void onImportFinished(ImportResult result) {
        if (onImported != null) {
            onImported.run();
        }
        summaryLabel.setText(Messages.get("import.summary",
                String.valueOf(result.ok()), String.valueOf(result.failures().size())));
        summaryLabel.setVisible(true);

        if (result.failures().isEmpty()) {
            dialogStage.close();
            return;
        }
        // Keep the dialog open to show the failed rows; disable re-import to avoid duplicates.
        failuresTable.setItems(FXCollections.observableArrayList(result.failures()));
        failuresTab.setText(Messages.get("import.failuresTab",
                String.valueOf(result.failures().size())));
        failuresTab.setDisable(false);
        tabPane.getSelectionModel().select(failuresTab);
    }

    private ImportResult runImport(Map<String, String> mapping) {
        int ok = 0;
        List<RowError> failures = new ArrayList<>();
        int line = 0;
        for (Map<String, String> sourceRow : source.rows()) {
            line++;
            Map<String, String> mapped = new LinkedHashMap<>();
            mapping.forEach((fieldKey, column) -> mapped.put(fieldKey, sourceRow.get(column)));
            Optional<String> error = importer.importRow(mapped);
            if (error.isEmpty()) {
                ok++;
            } else {
                failures.add(new RowError(line, error.get()));
            }
        }
        return new ImportResult(ok, failures);
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /** Lower-cases and strips accents/punctuation for lenient name matching. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }

    private record ImportResult(int ok, List<RowError> failures) {
    }
}
