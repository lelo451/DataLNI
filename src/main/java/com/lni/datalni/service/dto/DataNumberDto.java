package com.lni.datalni.service.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** UI-facing transfer object for {@link com.lni.datalni.domain.DataNumber}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataNumberDto {

    private Integer id;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer month;

    @NotNull
    @Min(1900)
    @Max(2999)
    private Integer year;

    @NotNull
    @Digits(integer = 8, fraction = 2)
    private BigDecimal value;

    @NotNull
    private Integer graphId;

    @Size(max = 150)
    private String clazz;
}
