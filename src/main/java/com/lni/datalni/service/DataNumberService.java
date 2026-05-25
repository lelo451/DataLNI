package com.lni.datalni.service;

import com.lni.datalni.domain.DataNumber;
import com.lni.datalni.exception.NotFoundException;
import com.lni.datalni.repository.DataNumberRepository;
import com.lni.datalni.security.SecurityRoles;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.mapper.DataNumberMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class DataNumberService {

    private final DataNumberRepository repository;
    private final DataNumberMapper mapper;

    public DataNumberService(DataNumberRepository repository, DataNumberMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<DataNumberDto> listByGraph(Integer graphId) {
        return mapper.toDtoList(repository.findByGraphIdOrderByYearDescMonthDesc(graphId));
    }

    public List<DataNumberDto> listByGraphAndYear(Integer graphId, Integer year) {
        return mapper.toDtoList(repository.findByGraphIdAndYear(graphId, year));
    }

    public DataNumberDto get(Integer id) {
        return mapper.toDto(find(id));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public DataNumberDto create(@Valid DataNumberDto dto) {
        DataNumber entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public DataNumberDto update(@Valid DataNumberDto dto) {
        DataNumber entity = find(dto.getId());
        mapper.updateEntity(dto, entity);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("DataNumber", id);
        }
        repository.deleteById(id);
    }

    private DataNumber find(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("DataNumber", id));
    }
}
