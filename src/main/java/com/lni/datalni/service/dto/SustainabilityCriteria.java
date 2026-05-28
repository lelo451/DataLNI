package com.lni.datalni.service.dto;

/**
 * Filter criteria for searching sustainability records. {@code text} matches title or
 * author; {@code year}/{@code ods} filter exactly. Null/blank fields ignored.
 */
public final class SustainabilityCriteria {

    private final String text;
    private final Integer year;
    private final Integer ods;

    public SustainabilityCriteria(String text, Integer year, Integer ods) {
        this.text = text;
        this.year = year;
        this.ods = ods;
    }

    public String text() {
        return text;
    }

    public Integer year() {
        return year;
    }

    public Integer ods() {
        return ods;
    }

    public static SustainabilityCriteria empty() {
        return new SustainabilityCriteria(null, null, null);
    }
}
