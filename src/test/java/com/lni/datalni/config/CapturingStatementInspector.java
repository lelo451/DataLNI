package com.lni.datalni.config;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Test helper: records every SQL statement Hibernate runs so tests can assert on it. */
public class CapturingStatementInspector implements StatementInspector {

    public static final List<String> SQL = new CopyOnWriteArrayList<>();

    public static void clear() {
        SQL.clear();
    }

    @Override
    public String inspect(String sql) {
        SQL.add(sql);
        return sql;
    }
}
