package com.lni.datalni.ui.support;

import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.ui.imports.CsvFileParser;
import com.lni.datalni.ui.imports.ParsedFile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataNumberCsvTest {

    @Test
    void export_roundTripsImportColumnsWithOptionalFieldsAndEscapedText() throws Exception {
        DataNumberDto numero = DataNumberDto.builder().id(7).graphId(42).year(2026)
                .month(5).value(new BigDecimal("1234.56"))
                .clazz("Educação, \"Pesquisa\"\nExtensão").build();
        DataNumberDto numeroAnual = DataNumberDto.builder().graphId(42).year(2025)
                .value(new BigDecimal("10.00")).build();

        byte[] conteudo = DataNumberCsv.write(List.of(numero, numeroAnual));
        ParsedFile arquivo = new CsvFileParser().parse("numeros-42.csv",
                new ByteArrayInputStream(conteudo));

        assertThat(arquivo.getHeaders()).containsExactly("graphId", "year", "month", "value", "clazz");
        assertThat(arquivo.getRows()).hasSize(2);
        assertThat(arquivo.getRows().get(0)).containsEntry("graphId", "42")
                .containsEntry("year", "2026").containsEntry("month", "5")
                .containsEntry("value", "1234.56").containsEntry("clazz", numero.getClazz());
        assertThat(arquivo.getRows().get(1)).containsEntry("month", "")
                .containsEntry("clazz", "").containsEntry("value", "10.00");
    }
}
