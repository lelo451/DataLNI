package com.lni.datalni.repository;

import com.lni.datalni.domain.DataNumber;
import com.lni.datalni.domain.Graph;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the column mappings and the Graph &harr; DataNumber relationship against an
 * H2 database. Data is seeded with plain SQL and only read back, so the DB2-specific
 * {@code MaxIdGenerator} is never invoked.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:datalni;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS PLD",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=PLD",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Sql(statements = {
        "INSERT INTO PLD.LNI_GRAFICO (CD_GRAFICO, DE_TITULO, DE_DESCRICAO) VALUES (1, 'Chart A', 'desc')",
        "INSERT INTO PLD.LNI_NUMERO_UEM (CD_NUMERO, ME_MES, AN_ANO, VL_VALOR, CD_GRAFICO, DE_CLASSE) "
                + "VALUES (10, 3, 2024, 1234.56, 1, 'classe')",
        "INSERT INTO PLD.LNI_NUMERO_UEM (CD_NUMERO, ME_MES, AN_ANO, VL_VALOR, CD_GRAFICO, DE_CLASSE) "
                + "VALUES (11, 4, 2023, 9.99, 1, 'classe')"
})
class MappingSliceTest {

    @Autowired private GraphRepository graphRepository;
    @Autowired private DataNumberRepository dataNumberRepository;

    @Test
    void graphMapsAndExposesItsDataNumbers() {
        Graph graph = graphRepository.findById(1).orElseThrow();
        assertThat(graph.getTitle()).isEqualTo("Chart A");
        assertThat(graph.getDataNumbers()).hasSize(2);
    }

    @Test
    void dataNumberPreservesDecimalAndForeignKey() {
        DataNumber number = dataNumberRepository.findById(10).orElseThrow();
        assertThat(number.getValue()).isEqualByComparingTo(new BigDecimal("1234.56"));
        assertThat(number.getGraphId()).isEqualTo(1);
        assertThat(number.getClazz()).isEqualTo("classe");
    }

    @Test
    void derivedQueryFiltersByGraphAndYear() {
        List<DataNumber> rows = dataNumberRepository.findByGraphIdAndYear(1, 2024);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getId()).isEqualTo(10);
    }
}
