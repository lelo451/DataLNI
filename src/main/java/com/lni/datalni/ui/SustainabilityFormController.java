package com.lni.datalni.ui;

import com.lni.datalni.service.SustainabilityService;
import com.lni.datalni.service.dto.SustainabilityDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.ErrorTranslator;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Create/edit form for a {@link SustainabilityDto}. */
@Component
@Scope("prototype")
public class SustainabilityFormController implements DialogAware {

    private final SustainabilityService sustainabilityService;
    private final AsyncRunner async;

    @FXML private Spinner<Integer> yearSpinner;
    @FXML private Spinner<Integer> odsSpinner;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField linkField;
    @FXML private DatePicker publishedPicker;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private Runnable onSaved;
    private SustainabilityDto model;

    public SustainabilityFormController(SustainabilityService sustainabilityService, AsyncRunner async) {
        this.sustainabilityService = sustainabilityService;
        this.async = async;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        int currentYear = java.time.Year.now().getValue();
        yearSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2999, currentYear));
        odsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 17, 1));
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
    private void onSave() {
        errorLabel.setVisible(false);
        SustainabilityDto dto = SustainabilityDto.builder()
                .id(model == null ? null : model.getId())
                .year(yearSpinner.getValue())
                .ods(odsSpinner.getValue())
                .title(emptyToNull(titleField.getText()))
                .author(emptyToNull(authorField.getText()))
                .link(emptyToNull(linkField.getText()))
                .published(publishedPicker.getValue())
                .build();
        async.run(
                () -> model == null
                        ? sustainabilityService.create(dto)
                        : sustainabilityService.update(dto),
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
