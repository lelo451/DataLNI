package com.lni.datalni.repository;

import com.lni.datalni.domain.Graph;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real DB2 integration test (SPEC §13). Disabled by default: the IBM DB2 image is large
 * and slow to start. Enable manually (remove {@code @Disabled}, ensure Docker is running)
 * to validate DB2 dialect behaviour and the {@link com.lni.datalni.config.MaxIdGenerator}
 * insert path end to end.
 */
@Disabled("opt-in: requires Docker and pulls the large IBM DB2 image")
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GraphRepositoryDb2IT {

    @Container
    @SuppressWarnings("resource")
    static final Db2Container DB2 = new Db2Container("icr.io/db2_community/db2:11.5.9.0")
            .acceptLicense();

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DB2::getJdbcUrl);
        registry.add("spring.datasource.username", DB2::getUsername);
        registry.add("spring.datasource.password", DB2::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "PLD");
    }

    @Autowired private GraphRepository graphRepository;

    @Test
    void savesGraphWithGeneratedId() {
        Graph graph = new Graph();
        graph.setTitle("Generated");
        Graph saved = graphRepository.save(graph);

        assertThat(saved.getId()).isNotNull();
        assertThat(graphRepository.findById(saved.getId())).isPresent();
    }
}
