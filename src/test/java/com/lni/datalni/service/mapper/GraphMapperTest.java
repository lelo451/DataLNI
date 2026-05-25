package com.lni.datalni.service.mapper;

import com.lni.datalni.domain.Graph;
import com.lni.datalni.service.dto.GraphDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Exercises the MapStruct-generated implementation directly. */
class GraphMapperTest {

    private final GraphMapper mapper = new GraphMapperImpl();

    @Test
    void toDto_copiesScalarFields() {
        Graph graph = new Graph();
        graph.setId(1);
        graph.setTitle("Title");
        graph.setDescription("Desc");

        GraphDto dto = mapper.toDto(graph);

        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getTitle()).isEqualTo("Title");
        assertThat(dto.getDescription()).isEqualTo("Desc");
    }

    @Test
    void updateEntity_keepsIdAndIgnoresNulls() {
        Graph entity = new Graph();
        entity.setId(5);
        entity.setTitle("Old");
        entity.setDescription("Keep");

        GraphDto dto = GraphDto.builder().id(999).title("New").description(null).build();
        mapper.updateEntity(dto, entity);

        assertThat(entity.getId()).isEqualTo(5);          // id never overwritten
        assertThat(entity.getTitle()).isEqualTo("New");   // updated
        assertThat(entity.getDescription()).isEqualTo("Keep"); // null ignored
    }
}
