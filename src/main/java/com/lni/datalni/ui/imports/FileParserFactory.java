package com.lni.datalni.ui.imports;

import java.util.Locale;

/** Picks a parser by file extension. */
public final class FileParserFactory {

    private FileParserFactory() {
    }

    public static TabularFileParser forFile(String fileName) {
        String name = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".csv")) {
            return new CsvFileParser();
        }
        if (name.endsWith(".json")) {
            return new JsonFileParser();
        }
        if (name.endsWith(".xlsx")) {
            return new XlsxFileParser();
        }
        throw new IllegalArgumentException("Formato não suportado: " + fileName
                + ". Use .csv, .json ou .xlsx.");
    }
}
