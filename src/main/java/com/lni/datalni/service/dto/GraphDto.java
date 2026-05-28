package com.lni.datalni.service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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

    @NotBlank(message = "{graph.title.required}")
    @Size(max = 150, message = "{graph.title.size}")
    private String title;

    @Size(max = 150, message = "{graph.description.size}")
    private String description;
}
