package com.lni.datalni.ui;

import com.lni.datalni.security.CurrentUser;
import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.GraphService;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.dto.GraphCriteria;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.ui.support.AsyncRunner;
import com.lni.datalni.ui.support.Dialogs;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
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
    @FXML private TableColumn<DataNumberDto, Integer> dnMonthColumn;
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
        graphIdColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        graphTitleColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTitle()));
        graphDescriptionColumn.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(c.getValue().getDescription()));

        dnMonthColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getMonth()));
        dnYearColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getYear()));
        dnValueColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getValue() == null ? "" : c.getValue().getValue().toPlainString()));
        dnClazzColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getClazz()));

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
        if (Dialogs.confirm("Delete graph",
                "Delete graph #" + selected.getId() + " and its data numbers reference?")) {
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
            Dialogs.info("Select a graph", "Pick a graph first to add a data number to it.");
            return;
        }
        stageManager.<DataNumberFormController>openModal(
                "datanumber-form.fxml", "New data number", form -> {
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
        if (Dialogs.confirm("Delete data number", "Delete data number #" + selected.getId() + "?")) {
            async.run(() -> {
                dataNumberService.delete(selected.getId());
                return null;
            }, ok -> loadDataNumbers(graph));
        }
    }
}
