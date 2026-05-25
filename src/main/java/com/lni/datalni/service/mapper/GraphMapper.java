package com.lni.datalni.service.mapper;

import com.lni.datalni.domain.Graph;
import com.lni.datalni.service.dto.GraphDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper
public interface GraphMapper {

    GraphDto toDto(Graph entity);

    List<GraphDto> toDtoList(List<Graph> entities);

    @Mapping(target = "dataNumbers", ignore = true)
    Graph toEntity(GraphDto dto);

    /** Applies non-null DTO fields onto an existing entity (id and collection untouched). */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataNumbers", ignore = true)
    void updateEntity(GraphDto dto, @MappingTarget Graph entity);
}
