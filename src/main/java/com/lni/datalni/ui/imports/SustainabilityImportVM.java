package com.lni.datalni.ui.imports;

import com.lni.datalni.service.SustainabilityService;
import com.lni.datalni.service.dto.SustainabilityDto;
import com.lni.datalni.ui.support.Messages;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@VariableResolver(DelegatingVariableResolver.class)
public class SustainabilityImportVM extends BaseImportVM {

    @WireVariable private SustainabilityService sustainabilityService;

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public List<ImportFieldSpec> getFieldSpecs() {
        return Arrays.asList(
                new ImportFieldSpec("title", "Título", true, s -> s),
                new ImportFieldSpec("ods", "ODS", false, this::parseInt),
                new ImportFieldSpec("author", "Autor", false, s -> s),
                new ImportFieldSpec("link", "Link", false, s -> s),
                new ImportFieldSpec("published", "Publicação", false, this::parseDate)
        );
    }

    @Override
    protected void persist(Map<String, Object> values) {
        LocalDate published = (LocalDate) values.get("published");
        SustainabilityDto dto = SustainabilityDto.builder()
                .title((String) values.get("title"))
                .ods((Integer) values.get("ods"))
                .author((String) values.get("author"))
                .link((String) values.get("link"))
                .published(published)
                // Year is derived from the publication date, mirroring the form behaviour.
                .year(published == null ? null : published.getYear())
                .build();
        sustainabilityService.create(dto);
    }

    @Override
    protected String refreshGlobalCommand() {
        return "refreshSustainability";
    }

    private Integer parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(Messages.get("import.invalidNumber", s));
        }
    }

    private LocalDate parseDate(String s) {
        String v = s.trim();
        try {
            return LocalDate.parse(v, BR);
        } catch (DateTimeParseException ignored) {
            // Fall through and try ISO.
        }
        try {
            return LocalDate.parse(v, ISO);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(Messages.get("import.invalidDate", s));
        }
    }
}
