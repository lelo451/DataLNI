package com.lni.datalni.ui;

import com.lni.datalni.service.GraphService;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Messages;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Backs the delete-graph modal: lets the user choose between cascade-deleting the
 * graph's data numbers and re-parenting them to another graph before deletion.
 */
@VariableResolver(DelegatingVariableResolver.class)
@Getter
@Setter
public class DeleteGraphFormVM {

    @WireVariable private GraphService graphService;

    private List<GraphDto> graphsToDelete;
    /** All other graphs available as move targets (excludes the ones being deleted). */
    private List<GraphDto> targetOptions;
    private GraphDto targetGraph;

    @Init
    public void init(@ExecutionArgParam("graphsToDelete") List<GraphDto> graphsToDelete) {
        this.graphsToDelete = graphsToDelete;
        Set<Integer> excluded = graphsToDelete.stream()
                .map(GraphDto::getId)
                .collect(Collectors.toSet());
        this.targetOptions = graphService.list().stream()
                .filter(g -> !excluded.contains(g.getId()))
                .collect(Collectors.toList());
    }

    public String graphLabel(GraphDto g) {
        return g == null ? "" : g.getId() + " - " + g.getTitle();
    }

    public String getSummary() {
        if (graphsToDelete.size() == 1) {
            return graphLabel(graphsToDelete.get(0));
        }
        return graphsToDelete.size() + " gráficos selecionados";
    }

    @Command
    public void deleteAll(@ContextParam(ContextType.VIEW) Component view) {
        try {
            for (GraphDto g : graphsToDelete) {
                graphService.deleteWithNumbers(g.getId());
            }
            BindUtils.postGlobalCommand(null, null, "refreshGraphs", null);
            view.detach();
        } catch (Exception e) {
            Messagebox.show(ErrorTranslator.translate(e),
                    Messages.get("dialog.error.title"),
                    Messagebox.OK, Messagebox.ERROR);
        }
    }

    @Command
    public void moveAndDelete(@ContextParam(ContextType.VIEW) Component view) {
        if (targetGraph == null) {
            Messagebox.show("Selecione um gráfico de destino para os números.",
                    Messages.get("dialog.info.title"),
                    Messagebox.OK, Messagebox.INFORMATION);
            return;
        }
        try {
            Integer targetId = targetGraph.getId();
            for (GraphDto g : graphsToDelete) {
                graphService.deleteAfterMoveNumbers(g.getId(), targetId);
            }
            BindUtils.postGlobalCommand(null, null, "refreshGraphs", null);
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
