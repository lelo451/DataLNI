package com.lni.datalni.service.dto;

/** Filter criteria for data numbers, always scoped to one graph. {@code year} optional. */
public record DataNumberCriteria(Integer graphId, Integer year) {
}
