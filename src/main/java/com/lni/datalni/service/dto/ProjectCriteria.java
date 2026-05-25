package com.lni.datalni.service.dto;

/**
 * Filter criteria for searching projects. {@code text} matches title, coordinator
 * or eprotocol (free-text LIKE); {@code ods} filters by SDG code. Null fields ignored.
 */
public record ProjectCriteria(String text, Integer ods) {

    public static ProjectCriteria empty() {
        return new ProjectCriteria(null, null);
    }
}
