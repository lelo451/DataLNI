package com.lni.datalni.service.dto;

/** Filter criteria for searching graphs. {@code null}/blank fields are ignored. */
public record GraphCriteria(String text) {

    public static GraphCriteria empty() {
        return new GraphCriteria(null);
    }
}
