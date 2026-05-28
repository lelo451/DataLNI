package com.lni.datalni.ui.imports;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads {@code .csv} (header row + records) using jackson-dataformat-csv. */
public class CsvFileParser implements TabularFileParser {

    @Override
    public ParsedFile parse(String fileName, InputStream content) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        List<Map<String, String>> rows = new ArrayList<>();
        List<String> headers = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(content, StandardCharsets.UTF_8);
             MappingIterator<Map<String, String>> it = mapper.readerFor(Map.class).with(schema).readValues(reader)) {
            while (it.hasNext()) {
                Map<String, String> row = it.next();
                rows.add(new LinkedHashMap<>(row));
                if (headers.isEmpty()) {
                    headers.addAll(row.keySet());
                }
            }
        }
        return new ParsedFile(fileName, headers, rows);
    }
}
