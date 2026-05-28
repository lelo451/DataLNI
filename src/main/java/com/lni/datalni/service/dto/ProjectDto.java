package com.lni.datalni.service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

/** UI-facing transfer object for {@link com.lni.datalni.domain.Project}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

    private Integer id;

    /** UN SDG code 1-17 (SPEC Q6). */
    @Range(min = 1, max = 17, message = "{project.ods.range}")
    private Integer ods;

    @Size(max = 50, message = "{project.eprotocol.size}")
    private String eprotocol;

    @NotBlank(message = "{project.title.required}")
    @Size(max = 250, message = "{project.title.size}")
    private String title;

    @Size(max = 200, message = "{project.coordinator.size}")
    private String coordinator;
}
