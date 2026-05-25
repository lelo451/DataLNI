package com.lni.datalni.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** UI-facing transfer object for {@link com.lni.datalni.domain.Project}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

    private Integer id;

    /** UN SDG code 1-17 (SPEC Q6). */
    @Min(1)
    @Max(17)
    private Integer ods;

    @Size(max = 50)
    private String eprotocol;

    @NotBlank
    @Size(max = 250)
    private String title;

    @Size(max = 200)
    private String coordinator;
}
