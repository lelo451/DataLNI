package com.lni.datalni.ui.support;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.lni.datalni.service.dto.DataNumberDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DataNumberCsv {

    private DataNumberCsv() {
    }

    public static byte[] write(List<DataNumberDto> numeros) throws IOException {
        CsvSchema esquema = CsvSchema.builder()
                .addColumn("graphId").addColumn("year").addColumn("month")
                .addColumn("value").addColumn("clazz").setUseHeader(true).build();
        ByteArrayOutputStream conteudo = new ByteArrayOutputStream();
        try (SequenceWriter escritor = new CsvMapper().writer(esquema).writeValues(conteudo)) {
            for (DataNumberDto numero : numeros) {
                Map<String, Object> valores = new LinkedHashMap<>();
                valores.put("graphId", numero.getGraphId());
                valores.put("year", numero.getYear());
                valores.put("month", numero.getMonth());
                valores.put("value", numero.getValue() == null ? null : numero.getValue().toPlainString());
                valores.put("clazz", numero.getClazz());
                escritor.write(valores);
            }
        }
        return conteudo.toByteArray();
    }
}
