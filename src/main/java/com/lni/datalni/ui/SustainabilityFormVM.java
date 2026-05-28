package com.lni.datalni.ui;

import com.lni.datalni.service.SustainabilityService;
import com.lni.datalni.service.dto.SustainabilityDto;
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

@VariableResolver(DelegatingVariableResolver.class)
@Getter
@Setter
public class SustainabilityFormVM {

    @WireVariable private SustainabilityService sustainabilityService;

    private SustainabilityDto dto;

    @Init
    public void init(@ExecutionArgParam("dto") SustainabilityDto dto) {
        this.dto = dto;
    }

    public String getTitle() {
        return dto.getId() == null
                ? Messages.get("sustainability.form.new.title")
                : Messages.get("sustainability.form.edit.title");
    }

    public List<Integer> getOdsOptions() {
        return SdgCatalog.ids();
    }

    public String odsLabel(Integer ods) {
        return SdgCatalog.label(ods);
    }

    @Command
    public void save(@ContextParam(ContextType.VIEW) Component view) {
        // Year is derived from the publication date — the form no longer takes
        // a year input. If the user left the date empty, the existing @NotNull
        // constraint on year will surface a clear validation error.
        if (dto.getPublished() != null) {
            dto.setYear(dto.getPublished().getYear());
        }
        try {
            if (dto.getId() == null) {
                sustainabilityService.create(dto);
            } else {
                sustainabilityService.update(dto);
            }
            BindUtils.postGlobalCommand(null, null, "refreshSustainability", null);
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
