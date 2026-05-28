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

@VariableResolver(DelegatingVariableResolver.class)
@Getter
@Setter
public class GraphFormVM {

    @WireVariable private GraphService graphService;

    private GraphDto dto;

    @Init
    public void init(@ExecutionArgParam("dto") GraphDto dto) {
        this.dto = dto;
    }

    public String getTitle() {
        return dto.getId() == null
                ? Messages.get("graph.form.new.title")
                : Messages.get("graph.form.edit.title");
    }

    @Command
    public void save(@ContextParam(ContextType.VIEW) Component view) {
        try {
            if (dto.getId() == null) {
                graphService.create(dto);
            } else {
                graphService.update(dto);
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
