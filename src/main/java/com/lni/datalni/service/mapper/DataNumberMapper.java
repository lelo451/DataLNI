package com.lni.datalni.service.mapper;

import com.lni.datalni.domain.DataNumber;
import com.lni.datalni.service.dto.DataNumberDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper
public interface DataNumberMapper {

    DataNumberDto toDto(DataNumber entity);

    List<DataNumberDto> toDtoList(List<DataNumber> entities);

    DataNumber toEntity(DataNumberDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(DataNumberDto dto, @MappingTarget DataNumber entity);
}
