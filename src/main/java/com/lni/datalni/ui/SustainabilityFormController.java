package com.lni.datalni.ui;

import com.lni.datalni.service.SustainabilityService;
import com.lni.datalni.service.dto.SustainabilityDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Formats;
import com.lni.datalni.ui.support.FormValidation;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.Spinners;
import com.lni.datalni.ui.support.StagingSupport;
import jakarta.validation.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
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

/** Create (add several, save all) / edit form for a {@link SustainabilityDto}. */
@Component
@Scope("prototype")
public class SustainabilityFormController implements DialogAware {

    private final SustainabilityService sustainabilityService;
    private final AsyncRunner async;
    private final Validator validator;

    @FXML private TabPane tabPane;
    @FXML private Tab listTab;
    @FXML private ListView<SustainabilityDto> pendingList;
    @FXML private Spinner<Integer> yearSpinner;
    @FXML private Spinner<Integer> odsSpinner;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField linkField;
    @FXML private DatePicker publishedPicker;
    @FXML private Label yearError;
    @FXML private Label odsError;
    @FXML private Label titleError;
    @FXML private Label authorError;
    @FXML private Label linkError;
    @FXML private Label errorLabel;
    @FXML private Button addButton;
    @FXML private Button saveAllButton;
    @FXML private Button saveButton;

    private Stage dialogStage;
    private Runnable onSaved;
    private SustainabilityDto model;
    private StagingSupport<SustainabilityDto> staging;
    private FormValidation<SustainabilityDto> validation;

    public SustainabilityFormController(SustainabilityService sustainabilityService, AsyncRunner async,
                                        Validator validator) {
        this.sustainabilityService = sustainabilityService;
        this.async = async;
        this.validator = validator;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        Spinners.integer(yearSpinner, 1900, 2999, java.time.Year.now().getValue());
        Spinners.integer(odsSpinner, 1, 17, 1);
        publishedPicker.setConverter(Formats.dateConverter());
        publishedPicker.setPromptText("dd/mm/aaaa");
        validation = new FormValidation<>(validator);
        validation.field("year", yearError, yearSpinner.valueProperty());
        validation.field("ods", odsError, odsSpinner.valueProperty());
        validation.field("title", titleError, titleField.textProperty());
        validation.field("author", authorError, authorField.textProperty());
        validation.field("link", linkError, linkField.textProperty());
        staging = new StagingSupport<>(tabPane, listTab, pendingList,
                addButton, saveAllButton, saveButton, async, this::summary);
    }

    private String summary(SustainabilityDto s) {
        String title = s.getTitle() == null || s.getTitle().isBlank() ? "(sem título)" : s.getTitle();
        return s.getYear() == null ? title : s.getYear() + " · " + title;
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    public void setModel(SustainabilityDto model) {
        this.model = model;
        staging.setEditing(model != null);
        if (model != null) {
            if (model.getYear() != null) {
                yearSpinner.getValueFactory().setValue(model.getYear());
            }
            if (model.getOds() != null) {
                odsSpinner.getValueFactory().setValue(model.getOds());
            }
            titleField.setText(model.getTitle());
            authorField.setText(model.getAuthor());
            linkField.setText(model.getLink());
            publishedPicker.setValue(model.getPublished());
        }
    }

    @FXML
    private void onAdd() {
        errorLabel.setVisible(false);
        SustainabilityDto dto = buildDto(false);
        if (!validation.validate(dto)) {
            return;
        }
        staging.add(dto);
        titleField.clear();
        authorField.clear();
        linkField.clear();
        publishedPicker.setValue(null);
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
        SustainabilityDto dto = buildDto(true);
        if (!validation.validate(dto)) {
            return;
        }
        async.run(() -> sustainabilityService.update(dto),
                saved -> finish(),
                error -> showError(ErrorTranslator.toMessage(error)));
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }

    private SustainabilityDto buildDto(boolean keepId) {
        return SustainabilityDto.builder()
                .id(keepId && model != null ? model.getId() : null)
                .year(yearSpinner.getValue())
                .ods(odsSpinner.getValue())
                .title(emptyToNull(titleField.getText()))
                .author(emptyToNull(authorField.getText()))
                .link(emptyToNull(linkField.getText()))
                .published(publishedPicker.getValue())
                .build();
    }

    private Optional<String> persist(SustainabilityDto dto) {
        try {
            sustainabilityService.create(dto);
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
