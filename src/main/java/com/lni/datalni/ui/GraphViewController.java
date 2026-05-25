package com.lni.datalni.ui;

import com.lni.datalni.security.CurrentUser;
import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.GraphService;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.dto.GraphCriteria;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.ui.imports.ImportField;
import com.lni.datalni.ui.imports.ImportValues;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.Cells;
import com.lni.datalni.ui.support.Dialogs;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Formats;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.RowSelection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Graphs module: master {@link GraphDto} table with a detail {@link DataNumberDto} table. */
@Component
@Scope("prototype")
public class GraphViewController {

    private final GraphService graphService;
    private final DataNumberService dataNumberService;
    private final CurrentUser currentUser;
    private final StageManager stageManager;
    private final AsyncRunner async;

    @FXML private TextField searchField;
    @FXML private TableView<GraphDto> graphTable;
    @FXML private TableColumn<GraphDto, Integer> graphIdColumn;
    @FXML private TableColumn<GraphDto, String> graphTitleColumn;
    @FXML private TableColumn<GraphDto, String> graphDescriptionColumn;
    @FXML private Button newGraphButton;
    @FXML private Button editGraphButton;
    @FXML private Button deleteGraphButton;
    @FXML private Button importGraphButton;

    @FXML private TableView<DataNumberDto> dataNumberTable;
    @FXML private TableColumn<DataNumberDto, String> dnMonthColumn;
    @FXML private TableColumn<DataNumberDto, Integer> dnYearColumn;
    @FXML private TableColumn<DataNumberDto, String> dnValueColumn;
    @FXML private TableColumn<DataNumberDto, String> dnClazzColumn;
    @FXML private Button newDataNumberButton;
    @FXML private Button editDataNumberButton;
    @FXML private Button deleteDataNumberButton;
    @FXML private Button importDataNumberButton;

    private RowSelection<GraphDto> graphSelection;
    private RowSelection<DataNumberDto> dataNumberSelection;

    public GraphViewController(GraphService graphService, DataNumberService dataNumberService,
                               CurrentUser currentUser, StageManager stageManager, AsyncRunner async) {
        this.graphService = graphService;
        this.dataNumberService = dataNumberService;
        this.currentUser = currentUser;
        this.stageManager = stageManager;
        this.async = async;
    }

    @FXML
    private void initialize() {
        graphIdColumn.setCellValueFactory(Cells.of(GraphDto::getId));
        graphTitleColumn.setCellValueFactory(Cells.of(GraphDto::getTitle));
        graphDescriptionColumn.setCellValueFactory(Cells.of(GraphDto::getDescription));

        dnMonthColumn.setCellValueFactory(Cells.of(dn -> Formats.monthName(dn.getMonth())));
        dnYearColumn.setCellValueFactory(Cells.of(DataNumberDto::getYear));
        dnValueColumn.setCellValueFactory(Cells.of(dn -> Formats.number(dn.getValue())));
        dnClazzColumn.setCellValueFactory(Cells.of(DataNumberDto::getClazz));

        // Stretch columns to fill the full table width (prefWidth acts as the share).
        graphTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        dataNumberTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        boolean canEdit = currentUser.canEdit();
        newGraphButton.setVisible(canEdit);
        editGraphButton.setVisible(canEdit);
        deleteGraphButton.setVisible(canEdit);
        importGraphButton.setVisible(canEdit);
        newDataNumberButton.setVisible(canEdit);
        editDataNumberButton.setVisible(canEdit);
        deleteDataNumberButton.setVisible(canEdit);
        importDataNumberButton.setVisible(canEdit);

        graphTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> loadDataNumbers(selected));

        graphSelection = RowSelection.install(graphTable);
        graphSelection.setOnChange(this::updateGraphButtons);
        dataNumberSelection = RowSelection.install(dataNumberTable);
        dataNumberSelection.setOnChange(this::updateDataNumberButtons);
        updateGraphButtons();
        updateDataNumberButtons();

        loadGraphs();
    }

    /** New/Edit act on a single row; only Delete stays enabled when several are selected. */
    private void updateGraphButtons() {
        int count = graphSelection.count();
        newGraphButton.setDisable(count > 1);
        editGraphButton.setDisable(count != 1);
        deleteGraphButton.setDisable(count == 0);
    }

    private void updateDataNumberButtons() {
        int count = dataNumberSelection.count();
        newDataNumberButton.setDisable(count > 1);
        editDataNumberButton.setDisable(count != 1);
        deleteDataNumberButton.setDisable(count == 0);
    }

    @FXML
    private void onSearch() {
        async.run(() -> graphService.search(new GraphCriteria(searchField.getText())),
                this::populateGraphs);
    }

    @FXML
    private void onRefresh() {
        searchField.clear();
        loadGraphs();
    }

    private void loadGraphs() {
        async.run(graphService::list, this::populateGraphs);
    }

    private void populateGraphs(List<GraphDto> graphs) {
        graphTable.setItems(FXCollections.observableArrayList(graphs));
        dataNumberTable.setItems(FXCollections.observableArrayList());
    }

    private void loadDataNumbers(GraphDto graph) {
        if (graph == null) {
            dataNumberTable.setItems(FXCollections.observableArrayList());
            return;
        }
        async.run(() -> dataNumberService.listByGraph(graph.getId()),
                rows -> dataNumberTable.setItems(FXCollections.observableArrayList(rows)));
    }

    // ---- Graph CRUD ----

    @FXML
    private void onNewGraph() {
        stageManager.<GraphFormController>openModal("graph-form.fxml", Messages.get("graph.form.new.title"), form -> {
            form.setModel(null);
            form.setOnSaved(this::loadGraphs);
        });
    }

    @FXML
    private void onEditGraph() {
        List<GraphDto> chosen = graphSelection.getSelected();
        if (chosen.size() != 1) {
            return;
        }
        stageManager.<GraphFormController>openModal("graph-form.fxml", Messages.get("graph.form.edit.title"), form -> {
            form.setModel(chosen.get(0));
            form.setOnSaved(this::loadGraphs);
        });
    }

    @FXML
    private void onDeleteGraph() {
        List<GraphDto> chosen = graphSelection.getSelected();
        if (chosen.isEmpty()) {
            return;
        }
        String message = chosen.size() == 1
                ? Messages.get("graph.delete.message", String.valueOf(chosen.get(0).getId()))
                : Messages.get("delete.confirmMany", String.valueOf(chosen.size()));
        if (Dialogs.confirm(Messages.get("graph.delete.title"), message)) {
            async.run(() -> {
                for (GraphDto graph : chosen) {
                    graphService.delete(graph.getId());
                }
                return null;
            }, ok -> loadGraphs());
        }
    }

    @FXML
    private void onImportGraphs() {
        stageManager.openImport(Messages.get("import.graphs.title"), List.of(
                ImportField.required("title", Messages.get("field.title")),
                ImportField.optional("description", Messages.get("field.description"))),
                this::importGraphRow, this::loadGraphs);
    }

    private Optional<String> importGraphRow(Map<String, String> values) {
        try {
            graphService.create(GraphDto.builder()
                    .title(ImportValues.text(values.get("title")))
                    .description(ImportValues.text(values.get("description")))
                    .build());
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(ErrorTranslator.toMessage(e));
        }
    }

    // ---- DataNumber CRUD ----

    @FXML
    private void onNewDataNumber() {
        GraphDto graph = graphTable.getSelectionModel().getSelectedItem();
        if (graph == null) {
            Dialogs.info(Messages.get("graph.selectFirst.title"),
                    Messages.get("graph.selectFirst.message"));
            return;
        }
        stageManager.<DataNumberFormController>openModal(
                "datanumber-form.fxml", Messages.get("datanumber.form.new.title"), form -> {
                    form.setGraphs(graphTable.getItems());
                    form.setGraphId(graph.getId());
                    form.setModel(null);
                    form.setOnSaved(() -> loadDataNumbers(graph));
                });
    }

    @FXML
    private void onEditDataNumber() {
        GraphDto graph = graphTable.getSelectionModel().getSelectedItem();
        List<DataNumberDto> chosen = dataNumberSelection.getSelected();
        if (graph == null || chosen.size() != 1) {
            return;
        }
        stageManager.<DataNumberFormController>openModal(
                "datanumber-form.fxml", Messages.get("datanumber.form.edit.title"), form -> {
                    form.setGraphs(graphTable.getItems());
                    form.setGraphId(graph.getId());
                    form.setModel(chosen.get(0));
                    form.setOnSaved(() -> loadDataNumbers(graph));
                });
    }

    @FXML
    private void onDeleteDataNumber() {
        GraphDto graph = graphTable.getSelectionModel().getSelectedItem();
        List<DataNumberDto> chosen = dataNumberSelection.getSelected();
        if (chosen.isEmpty()) {
            return;
        }
        String message = chosen.size() == 1
                ? Messages.get("datanumber.delete.message", String.valueOf(chosen.get(0).getId()))
                : Messages.get("delete.confirmMany", String.valueOf(chosen.size()));
        if (Dialogs.confirm(Messages.get("datanumber.delete.title"), message)) {
            async.run(() -> {
                for (DataNumberDto number : chosen) {
                    dataNumberService.delete(number.getId());
                }
                return null;
            }, ok -> loadDataNumbers(graph));
        }
    }

    @FXML
    private void onImportDataNumbers() {
        GraphDto graph = graphTable.getSelectionModel().getSelectedItem();
        stageManager.openImport(Messages.get("import.datanumbers.title"), List.of(
                ImportField.required("graphId", Messages.get("field.graph")),
                ImportField.required("year", Messages.get("field.year")),
                ImportField.required("value", Messages.get("field.value")),
                ImportField.optional("month", Messages.get("field.month")),
                ImportField.optional("clazz", Messages.get("field.clazz"))),
                this::importDataNumberRow, () -> loadDataNumbers(graph));
    }

    private Optional<String> importDataNumberRow(Map<String, String> values) {
        try {
            dataNumberService.create(DataNumberDto.builder()
                    .graphId(ImportValues.integer(values.get("graphId")))
                    .year(ImportValues.integer(values.get("year")))
                    .value(ImportValues.decimal(values.get("value")))
                    .month(ImportValues.integer(values.get("month")))
                    .clazz(ImportValues.text(values.get("clazz")))
                    .build());
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(ErrorTranslator.toMessage(e));
        }
    }
}
