package com.lni.datalni.ui;

import com.lni.datalni.security.CurrentUser;
import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.GraphService;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.dto.GraphCriteria;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.Cells;
import com.lni.datalni.ui.support.Dialogs;
import com.lni.datalni.ui.support.Formats;
import com.lni.datalni.ui.support.Messages;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @FXML private TableView<DataNumberDto> dataNumberTable;
    @FXML private TableColumn<DataNumberDto, String> dnMonthColumn;
    @FXML private TableColumn<DataNumberDto, Integer> dnYearColumn;
    @FXML private TableColumn<DataNumberDto, String> dnValueColumn;
    @FXML private TableColumn<DataNumberDto, String> dnClazzColumn;
    @FXML private Button newDataNumberButton;
    @FXML private Button editDataNumberButton;
    @FXML private Button deleteDataNumberButton;

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
        newDataNumberButton.setVisible(canEdit);
        editDataNumberButton.setVisible(canEdit);
        deleteDataNumberButton.setVisible(canEdit);

        graphTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> loadDataNumbers(selected));

        loadGraphs();
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
        stageManager.<GraphFormController>openModal("graph-form.fxml", "New graph", form -> {
            form.setModel(null);
            form.setOnSaved(this::loadGraphs);
        });
    }

    @FXML
    private void onEditGraph() {
        GraphDto selected = graphTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        stageManager.<GraphFormController>openModal("graph-form.fxml", "Edit graph", form -> {
            form.setModel(selected);
            form.setOnSaved(this::loadGraphs);
        });
    }

    @FXML
    private void onDeleteGraph() {
        GraphDto selected = graphTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (Dialogs.confirm(Messages.get("graph.delete.title"),
                Messages.get("graph.delete.message", String.valueOf(selected.getId())))) {
            async.run(() -> {
                graphService.delete(selected.getId());
                return null;
            }, ok -> loadGraphs());
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
                "datanumber-form.fxml", "New data number", form -> {
                    form.setGraphs(graphTable.getItems());
                    form.setGraphId(graph.getId());
                    form.setModel(null);
                    form.setOnSaved(() -> loadDataNumbers(graph));
                });
    }

    @FXML
    private void onEditDataNumber() {
        GraphDto graph = graphTable.getSelectionModel().getSelectedItem();
        DataNumberDto selected = dataNumberTable.getSelectionModel().getSelectedItem();
        if (graph == null || selected == null) {
            return;
        }
        stageManager.<DataNumberFormController>openModal(
                "datanumber-form.fxml", "Edit data number", form -> {
                    form.setGraphs(graphTable.getItems());
                    form.setGraphId(graph.getId());
                    form.setModel(selected);
                    form.setOnSaved(() -> loadDataNumbers(graph));
                });
    }

    @FXML
    private void onDeleteDataNumber() {
        GraphDto graph = graphTable.getSelectionModel().getSelectedItem();
        DataNumberDto selected = dataNumberTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (Dialogs.confirm(Messages.get("datanumber.delete.title"),
                Messages.get("datanumber.delete.message", String.valueOf(selected.getId())))) {
            async.run(() -> {
                dataNumberService.delete(selected.getId());
                return null;
            }, ok -> loadDataNumbers(graph));
        }
    }
}
