package com.lni.datalni.service.dto;

/** Filter criteria for data numbers, always scoped to one graph. {@code year} optional. */
public final class DataNumberCriteria {

    private final Integer graphId;
    private final Integer year;

    public DataNumberCriteria(Integer graphId, Integer year) {
        this.graphId = graphId;
        this.year = year;
    }

    public Integer graphId() {
        return graphId;
    }

    public Integer year() {
        return year;
    }
}
