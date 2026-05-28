package com.lni.datalni.service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

/** UI-facing transfer object for {@link com.lni.datalni.domain.Sustainability}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SustainabilityDto {

    private Integer id;

    @NotNull(message = "{sustainability.year.required}")
    @Range(min = 1900, max = 2999, message = "{sustainability.year.range}")
    private Integer year;

    /** UN SDG code 1-17 (SPEC Q6). */
    @Range(min = 1, max = 17, message = "{sustainability.ods.range}")
    private Integer ods;

    @NotBlank(message = "{sustainability.title.required}")
    @Size(max = 250, message = "{sustainability.title.size}")
    private String title;

    @Size(max = 550, message = "{sustainability.link.size}")
    private String link;

    @Size(max = 200, message = "{sustainability.author.size}")
    private String author;

    private LocalDate published;
}
