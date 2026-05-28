package com.lni.datalni.ui;

import com.lni.datalni.service.ProjectService;
import com.lni.datalni.service.dto.ProjectDto;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.SdgCatalog;

import java.util.List;
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

/** Create/edit project form, opened as a modal {@code <window>}. */
@VariableResolver(DelegatingVariableResolver.class)
@Getter
@Setter
public class ProjectFormVM {

    @WireVariable private ProjectService projectService;

    private ProjectDto dto;

    @Init
    public void init(@ExecutionArgParam("dto") ProjectDto dto) {
        this.dto = dto;
    }

    public String getTitle() {
        return dto.getId() == null
                ? Messages.get("project.form.new.title")
                : Messages.get("project.form.edit.title");
    }

    public List<Integer> getOdsOptions() {
        return SdgCatalog.ids();
    }

    public String odsLabel(Integer ods) {
        return SdgCatalog.label(ods);
    }

    @Command
    public void save(@ContextParam(ContextType.VIEW) Component view) {
        try {
            if (dto.getId() == null) {
                projectService.create(dto);
            } else {
                projectService.update(dto);
            }
            BindUtils.postGlobalCommand(null, null, "refreshProjects", null);
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
