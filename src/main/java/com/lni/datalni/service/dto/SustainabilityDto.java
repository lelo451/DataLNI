package com.lni.datalni.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** UI-facing transfer object for {@link com.lni.datalni.domain.Sustainability}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SustainabilityDto {

    private Integer id;

    @NotNull
    @Min(1900)
    @Max(2999)
    private Integer year;

    /** UN SDG code 1-17 (SPEC Q6). */
    @Min(1)
    @Max(17)
    private Integer ods;

    @NotBlank
    @Size(max = 250)
    private String title;

    @Size(max = 550)
    private String link;

    @Size(max = 200)
    private String author;

    private LocalDate published;
}
