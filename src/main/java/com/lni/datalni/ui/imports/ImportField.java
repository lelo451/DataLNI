package com.lni.datalni.ui.imports;

/** A destination column the user maps a source column to, in the import dialog. */
public record ImportField(String key, String label, boolean required) {

    public static ImportField required(String key, String label) {
        return new ImportField(key, label, true);
    }

    public static ImportField optional(String key, String label) {
        return new ImportField(key, label, false);
    }
}
