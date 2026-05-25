package com.lni.datalni.service.dto;

/**
 * Filter criteria for searching sustainability records. {@code text} matches title or
 * author; {@code year}/{@code ods} filter exactly. Null/blank fields ignored.
 */
public record SustainabilityCriteria(String text, Integer year, Integer ods) {

    public static SustainabilityCriteria empty() {
        return new SustainabilityCriteria(null, null, null);
    }
}
