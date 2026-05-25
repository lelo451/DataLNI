package com.lni.datalni.ui.imports;

/** A row that failed to import: its 1-based line number and the error message. */
public record RowError(int line, String message) {
}
