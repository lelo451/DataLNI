package com.lni.datalni.ui;

import com.lni.datalni.security.CurrentUser;
import com.lni.datalni.service.ProjectService;
import com.lni.datalni.service.dto.ProjectCriteria;
import com.lni.datalni.service.dto.ProjectDto;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Messages;
import com.lni.datalni.ui.support.SdgCatalog;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** MVVM view model for the Projects list page. */
@VariableResolver(DelegatingVariableResolver.class)
@Getter
@Setter
public class ProjectsVM {

    @WireVariable private ProjectService projectService;
    @WireVariable private CurrentUser currentUser;

    private String search = "";
    private Integer odsFilter;
    private int activePage = 0;
    /** Mutable so the page-size selector can two-way bind via @bind. */
    private int pageSize = 50;
    private long totalSize;
    private final ListModelList<ProjectDto> items = new ListModelList<>();
    private ProjectDto selected;
    private Set<ProjectDto> selectedItems = new HashSet<>();
    private String sortColumn = "id";
    private boolean sortAscending = true;

    @Init
    public void init() {
        // The listbox attribute multiple="true" flips the widget, but the underlying
        // ListModelList stays in single-select mode unless we tell it otherwise.
        items.setMultiple(true);
        reload();
    }

    private void reload() {
        ProjectCriteria criteria = new ProjectCriteria(
                StringUtils.hasText(search) ? search : null, odsFilter);
        Sort sort = sortAscending ? Sort.by(sortColumn).ascending() : Sort.by(sortColumn).descending();
        Pageable pageable = PageRequest.of(activePage, pageSize, sort);
        Page<ProjectDto> page = projectService.search(criteria, pageable);
        items.clear();
        items.addAll(page.getContent());
        totalSize = page.getTotalElements();
    }

    @Command
    @NotifyChange({"items", "totalSize", "activePage", "selected", "selectedItems"})
    public void onSort(@org.zkoss.bind.annotation.BindingParam("column") String column) {
        // Same column → flip direction. New column → start ascending.
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

    @Command
    @NotifyChange({"items", "totalSize", "activePage"})
    public void doSearch() {
        activePage = 0;
        reload();
    }

    @Command
    @NotifyChange({"items", "activePage"})
    public void onPage() {
        reload();
    }

    @Command
    @NotifyChange({"items", "totalSize", "activePage", "selected", "selectedItems"})
    public void onChangePageSize() {
        // Drop selection and reset to the first page so the user lands somewhere sane.
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
    public void doNew() {
        Map<String, Object> args = new HashMap<>();
        args.put("dto", new ProjectDto());
        Executions.createComponents("/project-form.zul", null, args);
    }

    @Command
    public void doEdit() {
        if (selected == null) {
            return;
        }
        Map<String, Object> args = new HashMap<>();
        args.put("dto", ProjectDto.builder()
                .id(selected.getId()).ods(selected.getOds())
                .eprotocol(selected.getEprotocol()).title(selected.getTitle())
                .coordinator(selected.getCoordinator()).build());
        Executions.createComponents("/project-form.zul", null, args);
    }

    @Command
    @NotifyChange({"items", "totalSize", "selected", "selectedItems"})
    public void doDelete() {
        if (selectedItems.isEmpty()) {
            return;
        }
        Set<ProjectDto> toDelete = new HashSet<>(selectedItems);
        String msg = toDelete.size() == 1
                ? Messages.get("project.delete.message", toDelete.iterator().next().getId())
                : Messages.get("delete.confirmMany", toDelete.size());
        Messagebox.show(msg, Messages.get("project.delete.title"),
                new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},
                Messagebox.QUESTION, e -> {
                    if (e.getButton() == Messagebox.Button.OK) {
                        try {
                            for (ProjectDto dto : toDelete) {
                                projectService.delete(dto.getId());
                            }
                            selected = null;
                            selectedItems.clear();
                            reload();
                            BindUtils.postNotifyChange(this, "items", "totalSize",
                                    "selected", "selectedItems");
                        } catch (Exception ex) {
                            Messagebox.show(ErrorTranslator.translate(ex),
                                    Messages.get("dialog.error.title"),
                                    Messagebox.OK, Messagebox.ERROR);
                        }
                    }
                });
    }

    @GlobalCommand
    @NotifyChange({"items", "totalSize"})
    public void refreshProjects() {
        reload();
    }

    /** Fired by the tabbox onSelect handler whenever the user switches tab. */
    @GlobalCommand
    @NotifyChange({"selected", "selectedItems"})
    public void resetSelection() {
        selected = null;
        items.clearSelection();
        selectedItems.clear();
    }

    // -- Computed properties (no field backing) ------------------------------

    public boolean isCanEdit() {
        return currentUser.canEdit();
    }

    public String odsLabel(Integer ods) {
        return SdgCatalog.label(ods);
    }
}
