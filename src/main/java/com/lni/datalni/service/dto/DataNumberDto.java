package com.lni.datalni.service.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;

/** UI-facing transfer object for {@link com.lni.datalni.domain.DataNumber}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataNumberDto {

    private Integer id;

    // Optional: some data sets are annual (no month). The form always supplies one.
    @Range(min = 1, max = 12, message = "{datanumber.month.range}")
    private Integer month;

    @NotNull(message = "{datanumber.year.required}")
    @Range(min = 1900, max = 2999, message = "{datanumber.year.range}")
    private Integer year;

    @NotNull(message = "{datanumber.value.required}")
    @Digits(integer = 8, fraction = 2, message = "{datanumber.value.digits}")
    private BigDecimal value;

    @NotNull(message = "{datanumber.graph.required}")
    private Integer graphId;

    /** {@code class} is reserved in Java, so the field is named {@code clazz}. */
    @Size(max = 150, message = "{datanumber.clazz.size}")
    private String clazz;
}
