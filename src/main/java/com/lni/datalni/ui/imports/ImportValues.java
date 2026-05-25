package com.lni.datalni.ui.imports;

import com.lni.datalni.ui.support.Formats;
import com.lni.datalni.ui.support.Messages;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Lenient parsers for raw import cell values. Blank -> {@code null}; bad values throw with
 * an i18n message so the row is reported as an error.
 */
public final class ImportValues {

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ImportValues() {
    }

    public static String text(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static Integer integer(String value) {
        String t = text(value);
        if (t == null) {
            return null;
        }
        try {
            return Integer.valueOf(t);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(Messages.get("import.invalidNumber", t));
        }
    }

    /** Accepts plain ({@code 1234.56}) or pt-BR ({@code 1.234,56}) decimals. */
    public static BigDecimal decimal(String value) {
        String t = text(value);
        if (t == null) {
            return null;
        }
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            try {
                return Formats.parseNumber(t);
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.get("import.invalidNumber", t));
            }
        }
    }

    /** Accepts ISO ({@code yyyy-MM-dd}) or pt-BR ({@code dd/MM/yyyy}) dates. */
    public static LocalDate date(String value) {
        String t = text(value);
        if (t == null) {
            return null;
        }
        try {
            return LocalDate.parse(t);
        } catch (Exception e) {
            try {
                return LocalDate.parse(t, BR_DATE);
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.get("import.invalidDate", t));
            }
        }
    }
}
