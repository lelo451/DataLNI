package com.lni.datalni.ui;

import com.lni.datalni.security.CurrentUser;
import com.lni.datalni.service.SustainabilityService;
import com.lni.datalni.service.dto.SustainabilityCriteria;
import com.lni.datalni.service.dto.SustainabilityDto;
import com.lni.datalni.ui.support.ErrorTranslator;
import com.lni.datalni.ui.support.Formats;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@VariableResolver(DelegatingVariableResolver.class)
@Getter
@Setter
public class SustainabilityVM {

    @WireVariable private SustainabilityService sustainabilityService;
    @WireVariable private CurrentUser currentUser;

    private String search = "";
    private Integer yearFilter;
    private Integer odsFilter;
    private int activePage = 0;
    private int pageSize = 50;
    private long totalSize;
    private final ListModelList<SustainabilityDto> items = new ListModelList<>();
    private SustainabilityDto selected;
    private Set<SustainabilityDto> selectedItems = new HashSet<>();
    private String sortColumn = "id";
    private boolean sortAscending = true;

    @Init
    public void init() {
        items.setMultiple(true);
        reload();
    }

    private void reload() {
        SustainabilityCriteria criteria = new SustainabilityCriteria(
                StringUtils.hasText(search) ? search : null, yearFilter, odsFilter);
        Sort sort = sortAscending ? Sort.by(sortColumn).ascending() : Sort.by(sortColumn).descending();
        Pageable pageable = PageRequest.of(activePage, pageSize, sort);
        Page<SustainabilityDto> page = sustainabilityService.search(criteria, pageable);
        items.clear();
        items.addAll(page.getContent());
        totalSize = page.getTotalElements();
    }

    @Command
    @NotifyChange({"items", "totalSize", "activePage", "selected", "selectedItems"})
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
        args.put("dto", new SustainabilityDto());
        Executions.createComponents("/sustainability-form.zul", null, args);
    }

    @Command
    public void doEdit() {
        if (selected == null) {
            return;
        }
        Map<String, Object> args = new HashMap<>();
        args.put("dto", SustainabilityDto.builder()
                .id(selected.getId()).year(selected.getYear()).ods(selected.getOds())
                .title(selected.getTitle()).link(selected.getLink())
                .author(selected.getAuthor()).published(selected.getPublished()).build());
        Executions.createComponents("/sustainability-form.zul", null, args);
    }

    @Command
    @NotifyChange({"items", "totalSize", "selected", "selectedItems"})
    public void doDelete() {
        if (selectedItems.isEmpty()) {
            return;
        }
        Set<SustainabilityDto> toDelete = new HashSet<>(selectedItems);
        String msg = toDelete.size() == 1
                ? Messages.get("sustainability.delete.message", toDelete.iterator().next().getId())
                : Messages.get("delete.confirmMany", toDelete.size());
        Messagebox.show(msg, Messages.get("sustainability.delete.title"),
                new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},
                Messagebox.QUESTION, e -> {
                    if (e.getButton() == Messagebox.Button.OK) {
                        try {
                            for (SustainabilityDto dto : toDelete) {
                                sustainabilityService.delete(dto.getId());
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
    public void refreshSustainability() {
        reload();
    }

    @GlobalCommand
    @NotifyChange({"selected", "selectedItems"})
    public void resetSelection() {
        selected = null;
        items.clearSelection();
        selectedItems.clear();
    }

    public boolean isCanEdit() {
        return currentUser.canEdit();
    }

    public String odsLabel(Integer ods) {
        return SdgCatalog.label(ods);
    }

    public String formatDate(LocalDate date) {
        return Formats.date(date);
    }
}
