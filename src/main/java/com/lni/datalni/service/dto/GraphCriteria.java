package com.lni.datalni.service.dto;

/** Filter criteria for searching graphs. {@code null}/blank fields are ignored. */
public final class GraphCriteria {

    private final String text;

    public GraphCriteria(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    public static GraphCriteria empty() {
        return new GraphCriteria(null);
    }
}
