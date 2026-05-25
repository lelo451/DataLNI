package com.lni.datalni.service;

import com.lni.datalni.domain.Graph;
import com.lni.datalni.exception.NotFoundException;
import com.lni.datalni.repository.DataNumberRepository;
import com.lni.datalni.repository.GraphRepository;
import com.lni.datalni.repository.spec.GraphSpecifications;
import com.lni.datalni.security.SecurityRoles;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.dto.GraphCriteria;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.service.mapper.DataNumberMapper;
import com.lni.datalni.service.mapper.GraphMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class GraphService {

    private final GraphRepository graphRepository;
    private final DataNumberRepository dataNumberRepository;
    private final GraphMapper graphMapper;
    private final DataNumberMapper dataNumberMapper;

    public GraphService(GraphRepository graphRepository, DataNumberRepository dataNumberRepository,
                        GraphMapper graphMapper, DataNumberMapper dataNumberMapper) {
        this.graphRepository = graphRepository;
        this.dataNumberRepository = dataNumberRepository;
        this.graphMapper = graphMapper;
        this.dataNumberMapper = dataNumberMapper;
    }

    public List<GraphDto> list() {
        return graphMapper.toDtoList(graphRepository.findAll());
    }

    public List<GraphDto> search(GraphCriteria criteria) {
        return graphMapper.toDtoList(graphRepository.findAll(GraphSpecifications.matches(criteria)));
    }

    public GraphDto get(Integer id) {
        return graphMapper.toDto(find(id));
    }

    public List<DataNumberDto> listDataNumbers(Integer graphId) {
        return dataNumberMapper.toDtoList(
                dataNumberRepository.findByGraphIdOrderByYearDescMonthDesc(graphId));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public GraphDto create(@Valid GraphDto dto) {
        Graph entity = graphMapper.toEntity(dto);
        entity.setId(null);
        return graphMapper.toDto(graphRepository.save(entity));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public GraphDto update(@Valid GraphDto dto) {
        Graph entity = find(dto.getId());
        graphMapper.updateEntity(dto, entity);
        return graphMapper.toDto(graphRepository.save(entity));
    }

    @Transactional
    @PreAuthorize(SecurityRoles.CAN_EDIT)
    public void delete(Integer id) {
        if (!graphRepository.existsById(id)) {
            throw new NotFoundException("Graph", id);
        }
        graphRepository.deleteById(id);
    }

    private Graph find(Integer id) {
        return graphRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Graph", id));
    }
}
