package com.lni.datalni.ui.imports;

import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.ui.support.Messages;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@VariableResolver(DelegatingVariableResolver.class)
public class DataNumberImportVM extends BaseImportVM {

    @WireVariable private DataNumberService dataNumberService;

    @Override
    public List<ImportFieldSpec> getFieldSpecs() {
        return Arrays.asList(
                new ImportFieldSpec("graphId", "Gráfico (ID)", true, this::parseInt),
                new ImportFieldSpec("year", "Ano", true, this::parseInt),
                new ImportFieldSpec("month", "Mês", false, this::parseInt),
                new ImportFieldSpec("value", "Valor", true, this::parseDecimal),
                new ImportFieldSpec("clazz", "Classe", false, s -> s)
        );
    }

    @Override
    protected void persist(Map<String, Object> values) {
        DataNumberDto dto = DataNumberDto.builder()
                .graphId((Integer) values.get("graphId"))
                .year((Integer) values.get("year"))
                .month((Integer) values.get("month"))
                .value((BigDecimal) values.get("value"))
                .clazz((String) values.get("clazz"))
                .build();
        dataNumberService.create(dto);
    }

    @Override
    protected String refreshGlobalCommand() {
        return "refreshDataNumbers";
    }

    private Integer parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(Messages.get("import.invalidNumber", s));
        }
    }

    private BigDecimal parseDecimal(String s) {
        // Accept either pt-BR ("1.234,56") or dot-decimal ("1234.56") gracefully.
        String normalised = s.trim();
        if (normalised.contains(",") && !normalised.contains(".")) {
            normalised = normalised.replace(",", ".");
        } else if (normalised.matches(".*\\d\\.\\d{3},\\d.*")) {
            normalised = normalised.replace(".", "").replace(",", ".");
        }
        try {
            return new BigDecimal(normalised);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    Messages.get("import.invalidNumber", s).toLowerCase(Locale.ROOT));
        }
    }
}
