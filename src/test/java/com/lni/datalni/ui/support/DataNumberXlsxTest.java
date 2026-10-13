package com.lni.datalni.ui.support;

import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.ui.imports.ParsedFile;
import com.lni.datalni.ui.imports.XlsxFileParser;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataNumberXlsxTest {

    @Test
    void export_formulaLikeClass_isStoredAsLiteralText() throws Exception {
        DataNumberDto numero = DataNumberDto.builder().graphId(42).year(2026)
                .value(new BigDecimal("99999999.99")).clazz("=SUM(1,2)").build();
        byte[] conteudo = DataNumberXlsx.write(List.of(numero));

        try (XSSFWorkbook planilha = new XSSFWorkbook(new ByteArrayInputStream(conteudo))) {
            Sheet aba = planilha.getSheetAt(0);
            assertThat(aba.getRow(1).getCell(4).getCellType()).isEqualTo(CellType.STRING);
            assertThat(aba.getRow(1).getCell(4).getStringCellValue()).isEqualTo("=SUM(1,2)");
        }
        ParsedFile arquivo = new XlsxFileParser().parse("numeros.xlsx", new ByteArrayInputStream(conteudo));
        assertThat(new BigDecimal(arquivo.getRows().get(0).get("value")))
                .isEqualByComparingTo(numero.getValue());
    }

    @Test
    void export_roundTripsImportColumnsWithNumericCellsAndOptionalFields() throws Exception {
        DataNumberDto numero = DataNumberDto.builder().id(7).graphId(42).year(2026)
                .month(5).value(new BigDecimal("1234.56"))
                .clazz("Educação, \"Pesquisa\"\nExtensão").build();
        DataNumberDto numeroAnual = DataNumberDto.builder().graphId(42).year(2025)
                .value(new BigDecimal("10.00")).build();

        byte[] conteudo = DataNumberXlsx.write(List.of(numero, numeroAnual));
        ParsedFile arquivo = new XlsxFileParser().parse("numeros-Gráfico.xlsx",
                new ByteArrayInputStream(conteudo));

        assertThat(arquivo.getHeaders()).containsExactly("graphId", "year", "month", "value", "clazz");
        assertThat(arquivo.getRows()).hasSize(2);
        assertThat(arquivo.getRows().get(0)).containsEntry("graphId", "42")
                .containsEntry("year", "2026").containsEntry("month", "5")
                .containsEntry("value", "1234.56").containsEntry("clazz", numero.getClazz());
        assertThat(arquivo.getRows().get(1)).containsEntry("month", "").containsEntry("clazz", "");
        assertThat(new BigDecimal(arquivo.getRows().get(1).get("value"))).isEqualByComparingTo("10.00");
        try (XSSFWorkbook planilha = new XSSFWorkbook(new ByteArrayInputStream(conteudo))) {
            assertThat(planilha.getNumberOfSheets()).isEqualTo(1);
            Sheet aba = planilha.getSheetAt(0);
            assertThat(aba.getLastRowNum()).isEqualTo(2);
            for (int coluna = 0; coluna < 4; coluna++) {
                assertThat(aba.getRow(1).getCell(coluna).getCellType()).isEqualTo(CellType.NUMERIC);
            }
            assertThat(aba.getRow(2).getCell(2).getCellType()).isEqualTo(CellType.BLANK);
            assertThat(aba.getRow(2).getCell(4).getCellType()).isEqualTo(CellType.BLANK);
        }
    }
}
