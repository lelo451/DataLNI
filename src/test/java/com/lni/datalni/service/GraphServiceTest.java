package com.lni.datalni.service;

import com.lni.datalni.domain.Graph;
import com.lni.datalni.exception.NotFoundException;
import com.lni.datalni.repository.DataNumberRepository;
import com.lni.datalni.repository.GraphRepository;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.service.mapper.DataNumberMapper;
import com.lni.datalni.service.mapper.GraphMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void delete_missingId_throwsAndDoesNotDelete() {
        when(graphRepository.existsById(7)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(7)).isInstanceOf(NotFoundException.class);
        verify(graphRepository, never()).deleteById(any());
    }
}
