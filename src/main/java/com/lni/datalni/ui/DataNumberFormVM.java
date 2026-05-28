package com.lni.datalni.ui;

import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.GraphService;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Messages;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Messagebox;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@VariableResolver(DelegatingVariableResolver.class)
@Getter
@Setter
public class DataNumberFormVM {

    @WireVariable private DataNumberService dataNumberService;
    @WireVariable private GraphService graphService;

    private DataNumberDto dto;
    /** Custom setter: when the user picks a graph from the combobox, push the id
     *  into the DTO so the save command sees the new association. */
    @Setter(AccessLevel.NONE)
    private GraphDto selectedGraph;

    @Init
    public void init(@ExecutionArgParam("dto") DataNumberDto dto) {
        this.dto = dto;
        // Pre-select the current graph in the dropdown so the user sees what they
        // are editing. On create the parent screen already stamped graphId.
        if (dto.getGraphId() != null) {
            for (GraphDto g : getGraphOptions()) {
                if (g.getId().equals(dto.getGraphId())) {
                    this.selectedGraph = g;
                    break;
                }
            }
        }
    }

    public void setSelectedGraph(GraphDto graph) {
        this.selectedGraph = graph;
        if (dto != null && graph != null) {
            dto.setGraphId(graph.getId());
        }
    }

    public List<GraphDto> getGraphOptions() {
        return graphService.list();
    }

    public String graphLabel(GraphDto g) {
        return g == null ? "" : g.getId() + " - " + g.getTitle();
    }

    public List<Integer> getMonthOptions() {
        return IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList());
    }

    /** Renders e.g. {@code "Janeiro (1)"} for the combobox label. */
    public String monthLabel(Integer m) {
        if (m == null) {
            return "";
        }
        // Locale.getDefault() is pt-BR (set in DataLniApplication.applyDefaults()).
        String name = Month.of(m).getDisplayName(TextStyle.FULL, Locale.getDefault());
        if (!name.isEmpty()) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name + " (" + m + ")";
    }

    public String getTitle() {
        return dto.getId() == null
                ? Messages.get("datanumber.form.new.title")
                : Messages.get("datanumber.form.edit.title");
    }

    @Command
    public void save(@ContextParam(ContextType.VIEW) Component view) {
        try {
            if (dto.getId() == null) {
                dataNumberService.create(dto);
            } else {
                dataNumberService.update(dto);
            }
            BindUtils.postGlobalCommand(null, null, "refreshDataNumbers", null);
            view.detach();
        } catch (Exception e) {
            Messagebox.show(ErrorTranslator.translate(e),
                    Messages.get("dialog.error.title"),
                    Messagebox.OK, Messagebox.ERROR);
        }
    }

    @Command
    public void cancel(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
    }
}
