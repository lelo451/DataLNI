package com.lni.datalni.config;

import com.lni.datalni.domain.Graph;
import com.lni.datalni.repository.GraphRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link DB2v97Dialect} paginates without the ANSI {@code OFFSET} clause that
 * DB2 9.7 rejects. Runs against H2 with the custom dialect and inspects the emitted SQL.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:datalnipage;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS PLD",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=PLD",
        "spring.jpa.properties.hibernate.dialect=com.lni.datalni.config.DB2v97Dialect",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector="
                + "com.lni.datalni.config.CapturingStatementInspector"
})
@Sql(statements = {
        "INSERT INTO PLD.LNI_GRAFICO (CD_GRAFICO, DE_TITULO, DE_DESCRICAO) VALUES (1, 'A', 'd')",
        "INSERT INTO PLD.LNI_GRAFICO (CD_GRAFICO, DE_TITULO, DE_DESCRICAO) VALUES (2, 'B', 'd')",
        "INSERT INTO PLD.LNI_GRAFICO (CD_GRAFICO, DE_TITULO, DE_DESCRICAO) VALUES (3, 'C', 'd')"
})
class DB2v97PaginationIT {

    @Autowired
    private GraphRepository graphRepository;

    @Test
    void offsetPageEmulatesWithRowNumberAndNoOffsetClause() {
        CapturingStatementInspector.clear();

        Page<Graph> page = graphRepository.findAll(PageRequest.of(1, 1, Sort.by("id")));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(1);

        // The paged data query selects the columns (de_titulo); the count query does not.
        String pagingSql = CapturingStatementInspector.SQL.stream()
                .map(String::toLowerCase)
                .filter(sql -> sql.contains("de_titulo"))
                .reduce((first, second) -> second)
                .orElseThrow();
        assertThat(pagingSql).doesNotContain(" offset ");
        assertThat(pagingSql).contains("row_number");
    }
}
