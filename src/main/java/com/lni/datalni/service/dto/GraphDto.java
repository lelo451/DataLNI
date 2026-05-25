package com.lni.datalni.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** UI-facing transfer object for {@link com.lni.datalni.domain.Graph}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphDto {

    private Integer id;

    @NotBlank
    @Size(max = 150)
    private String title;

    @Size(max = 150)
    private String description;
}
