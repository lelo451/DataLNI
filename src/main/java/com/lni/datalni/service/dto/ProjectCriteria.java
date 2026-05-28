package com.lni.datalni.service.dto;

/**
 * Filter criteria for searching projects. {@code text} matches title, coordinator
 * or eprotocol (free-text LIKE); {@code ods} filters by SDG code. Null fields ignored.
 */
public final class ProjectCriteria {

    private final String text;
    private final Integer ods;

    public ProjectCriteria(String text, Integer ods) {
        this.text = text;
        this.ods = ods;
    }

    public String text() {
        return text;
    }

    public Integer ods() {
        return ods;
    }

    public static ProjectCriteria empty() {
        return new ProjectCriteria(null, null);
    }
}
