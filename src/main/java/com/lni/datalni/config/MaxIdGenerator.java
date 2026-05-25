package com.lni.datalni.config;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application-assigned primary keys via {@code MAX(column) + 1}.
 *
 * <p>The PLD tables have no DB2 identity/sequence backing their primary keys, so new
 * rows take the next free id. Wire it onto an {@code @Id} field with:
 * <pre>{@code
 * @GeneratedValue(generator = "gen_xxx")
 * @GenericGenerator(name = "gen_xxx", strategy = "com.lni.datalni.config.MaxIdGenerator",
 *     parameters = {
 *         @org.hibernate.annotations.Parameter(name = "table",  value = "LNI_XXX"),
 *         @org.hibernate.annotations.Parameter(name = "column", value = "CD_XXX")
 *     })
 * }</pre>
 */
public class MaxIdGenerator implements IdentifierGenerator {

    private String table;
    private String column;
    private String schema;
    private Class<?> returnType;

    /**
     * Tracks the last generated ID per qualified table name (schema.table).
     * This prevents duplicate IDs when multiple entities are persisted in the
     * same transaction without flushing between them (e.g., batch inserts).
     */
    private static final Map<String, Long> lastGenerated = new ConcurrentHashMap<>();

    @Override
    public void configure(org.hibernate.type.Type type, java.util.Properties params,
                          org.hibernate.service.ServiceRegistry serviceRegistry) {
        this.table = params.getProperty("table");
        this.column = params.getProperty("column");
        this.schema = params.getProperty("schema", "PLD");
        this.returnType = type.getReturnedClass();
    }

    @Override
    public synchronized Serializable generate(SharedSessionContractImplementor session, Object object)
            throws HibernateException {
        String qualifiedTable = schema + "." + table;
        String sql = "SELECT COALESCE(MAX(" + column + "), 0) FROM " + qualifiedTable
                + " WITH RS USE AND KEEP EXCLUSIVE LOCKS";
        try {
            long dbMax = 0;
            Connection connection = session.getJdbcConnectionAccess().obtainConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dbMax = rs.getLong(1);
                }
            } finally {
                session.getJdbcConnectionAccess().releaseConnection(connection);
            }

            long cachedMax = lastGenerated.getOrDefault(qualifiedTable, 0L);
            long nextId = Math.max(dbMax, cachedMax) + 1;
            lastGenerated.put(qualifiedTable, nextId);

            if (Integer.class.equals(returnType) || int.class.equals(returnType)) {
                return (int) nextId;
            }
            return nextId;
        } catch (Exception e) {
            throw new HibernateException("Failed to generate ID from " + qualifiedTable, e);
        }
    }
}
