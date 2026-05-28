package com.lni.datalni.ui;

import com.lni.datalni.security.CurrentUser;
import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.GraphService;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.dto.GraphCriteria;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Formats;
import com.lni.datalni.ui.support.Messages;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** MVVM view model for the Graphs master-detail page. */
@VariableResolver(DelegatingVariableResolver.class)
@Getter
@Setter
public class GraphsVM {

    @WireVariable private GraphService graphService;
    @WireVariable private DataNumberService dataNumberService;
    @WireVariable private CurrentUser currentUser;

    private String search = "";
    private int activePage = 0;
    private int pageSize = 50;
    private long totalSize;
    private final ListModelList<GraphDto> items = new ListModelList<>();
    private GraphDto selected;
    private Set<GraphDto> selectedItems = new HashSet<>();
    private String sortColumn = "id";
    private boolean sortAscending = true;

    private final ListModelList<DataNumberDto> dataNumbers = new ListModelList<>();
    private DataNumberDto selectedNumber;
    private Set<DataNumberDto> selectedNumbers = new HashSet<>();

    @Init
    public void init() {
        items.setMultiple(true);
        dataNumbers.setMultiple(true);
        reload();
    }

    private void reload() {
        GraphCriteria criteria = new GraphCriteria(
                StringUtils.hasText(search) ? search : null);
        Sort sort = sortAscending ? Sort.by(sortColumn).ascending() : Sort.by(sortColumn).descending();
        Pageable pageable = PageRequest.of(activePage, pageSize, sort);
        Page<GraphDto> page = graphService.search(criteria, pageable);
        items.clear();
        items.addAll(page.getContent());
        totalSize = page.getTotalElements();
        loadDetail();
    }

    @Command
    @NotifyChange({"items", "totalSize", "activePage", "selected", "selectedItems",
            "dataNumbers", "detailHeader"})
    public void onSort(@org.zkoss.bind.annotation.BindingParam("column") String column) {
        if (column.equals(sortColumn)) {
            sortAscending = !sortAscending;
        } else {
            sortColumn = column;
            sortAscending = true;
        }
        activePage = 0;
        selected = null;
        selectedItems.clear();
        reload();
    }

    private void loadDetail() {
        dataNumbers.clear();
        if (selected != null) {
            List<DataNumberDto> rows = graphService.listDataNumbers(selected.getId());
            dataNumbers.addAll(rows == null ? Collections.emptyList() : rows);
        }
    }

    @Command
    @NotifyChange({"items", "totalSize", "activePage", "selected", "selectedItems",
            "dataNumbers", "detailHeader"})
    public void doSearch() {
        activePage = 0;
        selected = null;
        selectedItems.clear();
        reload();
    }

    @Command
    @NotifyChange({"items", "activePage", "selected", "selectedItems",
            "dataNumbers", "detailHeader"})
    public void onPage() {
        selected = null;
        selectedItems.clear();
        reload();
    }

    @Command
    @NotifyChange({"items", "totalSize", "activePage", "selected", "selectedItems",
            "dataNumbers", "detailHeader"})
    public void onChangePageSize() {
        activePage = 0;
        selected = null;
        selectedItems.clear();
        reload();
    }

    public java.util.List<Integer> getPageSizeOptions() {
        return java.util.Arrays.asList(25, 50, 100, 200);
    }

    public String getItemsPerPageLabel() {
        return Messages.get("page.itemsPerPage");
    }

    @Command
    @NotifyChange({"selected", "dataNumbers", "detailHeader"})
    public void onSelectGraph(@ContextParam(ContextType.TRIGGER_EVENT) SelectEvent<?, ?> event) {
        // By the time this command runs, the selectedItems binding has already
        // committed the new state — so use it to decide what the reference click
        // actually meant.
        GraphDto ref = null;
        if (event != null && event.getReference() instanceof Listitem) {
            Object v = ((Listitem) event.getReference()).getValue();
            if (v instanceof GraphDto) {
                ref = (GraphDto) v;
            }
        }

        if (ref != null && selectedItems.contains(ref)) {
            // Just checked / clicked into the row → promote it to active.
            selected = ref;
        } else if (selected != null && !selectedItems.contains(selected)) {
            // The active row was just unchecked. Pick another remaining one, or
            // clear so the detail pane goes back to the "select a graph" header.
            selected = selectedItems.isEmpty() ? null : selectedItems.iterator().next();
        }
        // Otherwise: an unchecked row wasn't the active one — keep current active.

        loadDetail();
    }

    // -- Graph CRUD ----------------------------------------------------------

    @Command
    public void doNew() {
        Map<String, Object> args = new HashMap<>();
        args.put("dto", new GraphDto());
        Executions.createComponents("/graph-form.zul", null, args);
    }

    @Command
    public void doEdit() {
        if (selected == null) {
            return;
        }
        Map<String, Object> args = new HashMap<>();
        args.put("dto", GraphDto.builder()
                .id(selected.getId()).title(selected.getTitle())
                .description(selected.getDescription()).build());
        Executions.createComponents("/graph-form.zul", null, args);
    }

    @Command
    public void doDelete() {
        if (selectedItems.isEmpty()) {
            return;
        }
        // Delegate to the delete-graph modal: it owns the cascade-vs-move decision
        // and the data-number FK is handled inside one transaction per graph.
        Map<String, Object> args = new HashMap<>();
        args.put("graphsToDelete", new java.util.ArrayList<>(selectedItems));
        Executions.createComponents("/delete-graph.zul", null, args);
    }

    // -- DataNumber (detail) CRUD --------------------------------------------

    @Command
    public void doNewNumber() {
        // Pre-fill the graphId if the user has a graph in focus; otherwise let the
        // form's combobox prompt them to pick one.
        Map<String, Object> args = new HashMap<>();
        DataNumberDto fresh = new DataNumberDto();
        if (selected != null) {
            fresh.setGraphId(selected.getId());
        }
        args.put("dto", fresh);
        Executions.createComponents("/datanumber-form.zul", null, args);
    }

    @Command
    public void doEditNumber() {
        if (selectedNumber == null) {
            return;
        }
        Map<String, Object> args = new HashMap<>();
        args.put("dto", DataNumberDto.builder()
                .id(selectedNumber.getId()).month(selectedNumber.getMonth())
                .year(selectedNumber.getYear()).value(selectedNumber.getValue())
                .graphId(selectedNumber.getGraphId()).clazz(selectedNumber.getClazz()).build());
        Executions.createComponents("/datanumber-form.zul", null, args);
    }

    @Command
    public void doDeleteNumber() {
        if (selectedNumbers.isEmpty()) {
            return;
        }
        Set<DataNumberDto> toDelete = new HashSet<>(selectedNumbers);
        String msg = toDelete.size() == 1
                ? Messages.get("datanumber.delete.message", toDelete.iterator().next().getId())
                : Messages.get("delete.confirmMany", toDelete.size());
        Messagebox.show(msg, Messages.get("datanumber.delete.title"),
                new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},
                Messagebox.QUESTION, e -> {
                    if (e.getButton() == Messagebox.Button.OK) {
                        try {
                            for (DataNumberDto dto : toDelete) {
                                dataNumberService.delete(dto.getId());
                            }
                            selectedNumber = null;
                            selectedNumbers.clear();
                            loadDetail();
                            BindUtils.postNotifyChange(this, "dataNumbers",
                                    "selectedNumber", "selectedNumbers");
                        } catch (Exception ex) {
                            Messagebox.show(ErrorTranslator.translate(ex),
                                    Messages.get("dialog.error.title"),
                                    Messagebox.OK, Messagebox.ERROR);
                        }
                    }
                });
    }

    @GlobalCommand
    @NotifyChange({"items", "totalSize", "dataNumbers", "detailHeader"})
    public void refreshGraphs() {
        reload();
    }

    @GlobalCommand
    @NotifyChange({"dataNumbers"})
    public void refreshDataNumbers() {
        loadDetail();
    }

    @GlobalCommand
    @NotifyChange({"selected", "selectedItems", "selectedNumber", "selectedNumbers",
            "dataNumbers", "detailHeader"})
    public void resetSelection() {
        selected = null;
        items.clearSelection();
        selectedItems.clear();
        selectedNumber = null;
        dataNumbers.clearSelection();
        selectedNumbers.clear();
        // Detail panel reflects whichever graph is selected — with selected cleared
        // it should empty out.
        loadDetail();
    }

    // -- Computed properties -------------------------------------------------

    public String getDetailHeader() {
        if (selected == null) {
            return Messages.get("datanumber.headerNone");
        }
        return Messages.get("datanumber.header", selected.getTitle());
    }

    public boolean isCanEdit() {
        return currentUser.canEdit();
    }

    public String formatValue(BigDecimal value) {
        return Formats.decimal(value);
    }
}
