package com.lni.datalni.service;

import com.lni.datalni.domain.Project;
import com.lni.datalni.exception.NotFoundException;
import com.lni.datalni.repository.ProjectRepository;
import com.lni.datalni.repository.spec.ProjectSpecifications;
import com.lni.datalni.security.SecurityRoles;
import com.lni.datalni.service.dto.ProjectCriteria;
import com.lni.datalni.service.dto.ProjectDto;
import com.lni.datalni.service.mapper.ProjectMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository repository;
    private final ProjectMapper mapper;

    public ProjectService(ProjectRepository repository, ProjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ProjectDto> list() {
        return mapper.toDtoList(repository.findAll());
    }

    public List<ProjectDto> search(ProjectCriteria criteria) {
        return mapper.toDtoList(repository.findAll(ProjectSpecifications.matches(criteria)));
    }

    public List<ProjectDto> searchByOds(Integer ods) {
        return search(new ProjectCriteria(null, ods));
    }

    public ProjectDto get(Integer id) {
        return mapper.toDto(find(id));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public ProjectDto create(@Valid ProjectDto dto) {
        Project entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public ProjectDto update(@Valid ProjectDto dto) {
        Project entity = find(dto.getId());
        mapper.updateEntity(dto, entity);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Project", id);
        }
        repository.deleteById(id);
    }

    private Project find(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project", id));
    }
}
