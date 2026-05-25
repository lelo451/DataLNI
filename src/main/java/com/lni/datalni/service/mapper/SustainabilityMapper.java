package com.lni.datalni.service.mapper;

import com.lni.datalni.domain.Sustainability;
import com.lni.datalni.service.dto.SustainabilityDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper
public interface SustainabilityMapper {

    SustainabilityDto toDto(Sustainability entity);

    List<SustainabilityDto> toDtoList(List<Sustainability> entities);

    Sustainability toEntity(SustainabilityDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(SustainabilityDto dto, @MappingTarget Sustainability entity);
}
