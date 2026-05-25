package com.lni.datalni.ui.imports;

import java.util.List;
import java.util.Map;

/** A parsed CSV/JSON file: the ordered source column names and the rows (column -> value). */
public record ParsedFile(List<String> columns, List<Map<String, String>> rows) {
}
