package com.lni.datalni.ui.support;

import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** pt-BR display/parse formatting for dates ({@code dd/MM/yyyy}) and decimals ({@code 1.234,56}). */
public final class Formats {

    public static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formats() {
    }

    // ---- Dates ----

    public static String date(LocalDate date) {
        return date == null ? "" : DATE.format(date);
    }

    /** Null-safe {@code dd/MM/yyyy} converter for a {@code DatePicker}. */
    public static StringConverter<LocalDate> dateConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DATE.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(text.trim(), DATE);
                } catch (Exception e) {
                    return null;
                }
            }
        };
    }

    // ---- Months (value 1-12 <-> pt-BR name) ----

    public static String monthName(Integer month) {
        if (month == null) {
            return "";
        }
        String name = Month.of(month).getDisplayName(TextStyle.FULL, PT_BR);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /** Converter for a {@code ComboBox<Integer>} of months 1-12, displaying pt-BR names. */
    public static StringConverter<Integer> monthConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Integer month) {
                return monthName(month);
            }

            @Override
            public Integer fromString(String text) {
                if (text == null || text.isBlank()) {
                    return null;
                }
                for (int m = 1; m <= 12; m++) {
                    if (monthName(m).equalsIgnoreCase(text.trim())) {
                        return m;
                    }
                }
                return null;
            }
        };
    }

    // ---- Decimals ----

    private static DecimalFormat decimalFormat() {
        DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(PT_BR));
        df.setParseBigDecimal(true);
        return df;
    }

    public static String number(BigDecimal value) {
        return value == null ? "" : decimalFormat().format(value);
    }

    /** Parses a pt-BR decimal (e.g. {@code 1.234,56}). */
    public static BigDecimal parseNumber(String text) throws ParseException {
        return (BigDecimal) decimalFormat().parse(text.trim());
    }
}
