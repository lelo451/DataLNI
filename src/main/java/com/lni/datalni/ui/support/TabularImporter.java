package com.lni.datalni.ui.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.lni.datalni.ui.imports.ParsedFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reads a CSV (header row) or JSON (array of objects) file into a {@link ParsedFile}. */
public final class TabularImporter {

    private TabularImporter() {
    }

    public static ParsedFile parse(File file) throws IOException {
        return file.getName().toLowerCase().endsWith(".json") ? parseJson(file) : parseCsv(file);
    }

    private static ParsedFile parseCsv(File file) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        List<Map<String, String>> rows = new ArrayList<>();
        try (MappingIterator<Map<String, String>> it =
                     mapper.readerForMapOf(String.class).with(schema).readValues(file)) {
            while (it.hasNext()) {
                rows.add(it.next());
            }
        }
        return new ParsedFile(orderedColumns(rows), rows);
    }

    private static ParsedFile parseJson(File file) throws IOException {
        List<Map<String, Object>> raw = new ObjectMapper()
                .readValue(file, new TypeReference<List<Map<String, Object>>>() { });
        List<Map<String, String>> rows = new ArrayList<>(raw.size());
        for (Map<String, Object> object : raw) {
            Map<String, String> row = new LinkedHashMap<>();
            object.forEach((k, v) -> row.put(k, v == null ? "" : String.valueOf(v)));
            rows.add(row);
        }
        return new ParsedFile(orderedColumns(rows), rows);
    }

    /** Union of column names across all rows, preserving first-seen order. */
    private static List<String> orderedColumns(List<Map<String, String>> rows) {
        Set<String> columns = new LinkedHashSet<>();
        rows.forEach(row -> columns.addAll(row.keySet()));
        return new ArrayList<>(columns);
    }
}
