package com.lni.datalni.config;

import org.hibernate.dialect.sql.ast.DB2SqlAstTranslator;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.ast.tree.Statement;
import org.hibernate.sql.exec.spi.JdbcOperation;

/**
 * SQL AST translator for DB2 LUW 9.7. Hibernate 7's {@link DB2SqlAstTranslator} renders
 * pagination with the ANSI {@code OFFSET n ROWS FETCH FIRST m ROWS ONLY} clause (introduced
 * in DB2 11.1), which 9.7 rejects with SQLCODE -104. The pre-11.1 behaviour still exists in
 * Hibernate, gated behind two methods:
 *
 * <ul>
 *   <li>{@code supportsOffsetClause() = false} makes the translator emulate an offset with a
 *       {@code ROW_NUMBER() OVER(...)} window (supported by 9.7) instead of {@code OFFSET};</li>
 *   <li>{@code supportsParameterOffsetFetchExpression() = false} inlines the row limits as
 *       integer literals, because 9.7 does not accept bind parameters in {@code FETCH FIRST}.</li>
 * </ul>
 */
public class DB2v97SqlAstTranslator<T extends JdbcOperation> extends DB2SqlAstTranslator<T> {

    public DB2v97SqlAstTranslator(SessionFactoryImplementor sessionFactory, Statement statement) {
        super(sessionFactory, statement);
    }

    @Override
    protected boolean supportsOffsetClause() {
        return false;
    }

    @Override
    protected boolean supportsParameterOffsetFetchExpression() {
        return false;
    }
}
