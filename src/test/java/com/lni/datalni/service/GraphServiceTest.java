package com.lni.datalni.service;

import com.lni.datalni.domain.Graph;
import com.lni.datalni.exception.NotFoundException;
import com.lni.datalni.repository.DataNumberRepository;
import com.lni.datalni.repository.GraphRepository;
import com.lni.datalni.service.dto.GraphCriteria;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.service.mapper.DataNumberMapper;
import com.lni.datalni.service.mapper.GraphMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphServiceTest {

    @Mock private GraphRepository graphRepository;
    @Mock private DataNumberRepository dataNumberRepository;
    @Mock private GraphMapper graphMapper;
    @Mock private DataNumberMapper dataNumberMapper;

    @InjectMocks private GraphService service;

    @Test
    void create_clearsClientSuppliedIdBeforeSaving() {
        GraphDto dto = GraphDto.builder().id(999).title("T").build();
        Graph entity = new Graph();
        entity.setId(999);
        when(graphMapper.toEntity(dto)).thenReturn(entity);
        when(graphRepository.save(any(Graph.class))).thenAnswer(inv -> inv.getArgument(0));
        when(graphMapper.toDto(any(Graph.class))).thenReturn(dto);

        service.create(dto);

        ArgumentCaptor<Graph> saved = ArgumentCaptor.forClass(Graph.class);
        verify(graphRepository).save(saved.capture());
        assertThat(saved.getValue().getId()).isNull();
    }

    @Test
    void get_missingId_throwsNotFound() {
        when(graphRepository.findById(42)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void pagedSearch_mapsEntitiesToDtosPreservingPaging() {
        Graph entity = new Graph();
        entity.setId(1);
        GraphDto dto = GraphDto.builder().id(1).title("T").build();
        Pageable pageable = PageRequest.of(0, 50);
        Page<Graph> page = new PageImpl<>(List.of(entity), pageable, 1);
        when(graphRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(graphMapper.toDto(entity)).thenReturn(dto);

        Page<GraphDto> result = service.search(GraphCriteria.empty(), pageable);

        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void delete_missingId_throwsAndDoesNotDelete() {
        when(graphRepository.existsById(7)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(7)).isInstanceOf(NotFoundException.class);
        verify(graphRepository, never()).deleteById(any());
    }
}
