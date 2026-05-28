package com.lni.datalni.ui.support;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** pt-BR formatters for listcell display (ZK MVVM can't call static FQNs in @load). */
public final class Formats {

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);
    private static final DecimalFormat DECIMAL =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(PT_BR));

    private Formats() {
    }

    public static String date(LocalDate value) {
        return value == null ? "" : DATE.format(value);
    }

    public static String decimal(BigDecimal value) {
        return value == null ? "" : DECIMAL.format(value);
    }
}
