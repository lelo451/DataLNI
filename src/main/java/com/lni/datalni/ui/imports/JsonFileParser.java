package com.lni.datalni.ui.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reads a JSON array of objects ({@code [{"col":"v"},{...}]}). */
public class JsonFileParser implements TabularFileParser {

    @Override
    public ParsedFile parse(String fileName, InputStream content) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        MapType mapType = TypeFactory.defaultInstance()
                .constructMapType(LinkedHashMap.class, String.class, Object.class);
        List<Map<String, Object>> raw = mapper.readValue(
                content,
                TypeFactory.defaultInstance().constructCollectionType(List.class, mapType));

        // Headers are the union of all keys across rows (preserves first-seen order).
        Set<String> headerSet = new LinkedHashSet<>();
        List<Map<String, String>> rows = new ArrayList<>(raw.size());
        for (Map<String, Object> r : raw) {
            Map<String, String> stringified = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : r.entrySet()) {
                headerSet.add(e.getKey());
                stringified.put(e.getKey(), e.getValue() == null ? "" : String.valueOf(e.getValue()));
            }
            rows.add(stringified);
        }
        return new ParsedFile(fileName, new ArrayList<>(headerSet), rows);
    }
}
