package com.lni.datalni.ui.support;

import com.lni.datalni.service.dto.DataNumberDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public final class DataNumberXlsx {

    private DataNumberXlsx() {
    }

    public static byte[] write(List<DataNumberDto> numeros) throws IOException {
        ByteArrayOutputStream conteudo = new ByteArrayOutputStream();
        try (XSSFWorkbook planilha = new XSSFWorkbook()) {
            Sheet aba = planilha.createSheet("Números");
            Row cabecalho = aba.createRow(0);
            String[] colunas = {"graphId", "year", "month", "value", "clazz"};
            for (int coluna = 0; coluna < colunas.length; coluna++) {
                cabecalho.createCell(coluna).setCellValue(colunas[coluna]);
            }
            int indiceLinha = 1;
            for (DataNumberDto numero : numeros) {
                Row linha = aba.createRow(indiceLinha++);
                writeNumber(linha, 0, numero.getGraphId());
                writeNumber(linha, 1, numero.getYear());
                writeNumber(linha, 2, numero.getMonth());
                writeNumber(linha, 3, numero.getValue());
                Cell classe = linha.createCell(4);
                if (numero.getClazz() != null) {
                    classe.setCellValue(numero.getClazz());
                }
            }
            planilha.write(conteudo);
        }
        return conteudo.toByteArray();
    }

    private static void writeNumber(Row linha, int coluna, Number valor) {
        Cell celula = linha.createCell(coluna);
        if (valor != null) {
            celula.setCellValue(valor.doubleValue());
        }
    }
}
