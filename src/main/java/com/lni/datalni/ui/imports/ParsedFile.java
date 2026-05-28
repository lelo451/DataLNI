package com.lni.datalni.ui.imports;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Tabular file already parsed into a header row + a list of column-keyed rows.
 * Each {@code rows} entry maps the source header → its raw String value for one
 * record. Parsers normalise all values to String here; field-specific coercion
 * (Integer, BigDecimal, LocalDate, ...) happens per-row at import time.
 */
@RequiredArgsConstructor
@Getter
public class ParsedFile {

    private final String fileName;
    private final List<String> headers;
    private final List<Map<String, String>> rows;

    public boolean isEmpty() {
        return rows == null || rows.isEmpty();
    }
}
