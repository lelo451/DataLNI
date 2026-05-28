package com.lni.datalni.service;

import com.lni.datalni.domain.Sustainability;
import com.lni.datalni.exception.NotFoundException;
import com.lni.datalni.repository.SustainabilityRepository;
import com.lni.datalni.repository.spec.SustainabilitySpecifications;
import com.lni.datalni.security.SecurityRoles;
import com.lni.datalni.service.dto.SustainabilityCriteria;
import com.lni.datalni.service.dto.SustainabilityDto;
import com.lni.datalni.service.mapper.SustainabilityMapper;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class SustainabilityService {

    private final SustainabilityRepository repository;
    private final SustainabilityMapper mapper;

    public SustainabilityService(SustainabilityRepository repository, SustainabilityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<SustainabilityDto> list() {
        return mapper.toDtoList(repository.findAll(Sort.by("id")));
    }

    public List<SustainabilityDto> search(SustainabilityCriteria criteria) {
        return mapper.toDtoList(repository.findAll(
                SustainabilitySpecifications.matches(criteria), Sort.by("id")));
    }

    public Page<SustainabilityDto> search(SustainabilityCriteria criteria, Pageable pageable) {
        return repository.findAll(SustainabilitySpecifications.matches(criteria), pageable)
                .map(mapper::toDto);
    }

    public SustainabilityDto get(Integer id) {
        return mapper.toDto(find(id));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public SustainabilityDto create(@Valid SustainabilityDto dto) {
        Sustainability entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public SustainabilityDto update(@Valid SustainabilityDto dto) {
        Sustainability entity = find(dto.getId());
        mapper.updateEntity(dto, entity);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Sustainability", id);
        }
        repository.deleteById(id);
    }

    private Sustainability find(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sustainability", id));
    }
}
