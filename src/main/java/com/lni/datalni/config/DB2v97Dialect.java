package com.lni.datalni.config;

import org.hibernate.dialect.DB2Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.spi.StandardSqlAstTranslatorFactory;
import org.hibernate.sql.ast.tree.Statement;
import org.hibernate.sql.exec.spi.JdbcOperation;

/**
 * DB2 dialect for the corporate server, which runs DB2 LUW 9.7.
 *
 * <p>Hibernate 7's stock {@link DB2Dialect} paginates with the ANSI
 * {@code OFFSET n ROWS FETCH FIRST m ROWS ONLY} clause, introduced only in DB2 11.1, so 9.7
 * rejects it with SQLCODE -104. Pagination is rendered by the dialect's SQL AST translator,
 * not its {@code LimitHandler}, so the fix swaps in {@link DB2v97SqlAstTranslator}, which
 * emulates the offset with a {@code ROW_NUMBER()} window and inlines row limits as literals —
 * both supported by 9.7. The emulation needs an {@code ORDER BY} in the query, so every paged
 * query must carry a stable {@code Sort} (the views sort by id).
 */
public class DB2v97Dialect extends DB2Dialect {

    @Override
    public SqlAstTranslatorFactory getSqlAstTranslatorFactory() {
        return new StandardSqlAstTranslatorFactory() {
            @Override
            protected <T extends JdbcOperation> SqlAstTranslator<T> buildTranslator(
                    SessionFactoryImplementor sessionFactory, Statement statement) {
                return new DB2v97SqlAstTranslator<>(sessionFactory, statement);
            }
        };
    }
}
