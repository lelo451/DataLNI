package com.lni.datalni.service.mapper;

import com.lni.datalni.domain.Project;
import com.lni.datalni.service.dto.ProjectDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper
public interface ProjectMapper {

    ProjectDto toDto(Project entity);

    List<ProjectDto> toDtoList(List<Project> entities);

    Project toEntity(ProjectDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(ProjectDto dto, @MappingTarget Project entity);
}
