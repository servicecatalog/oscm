/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.db;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.oscm.setup.DatabaseUpgradeHandler;
import org.oscm.setup.DatabaseVersionInfo;

/**
 * Internal utilities shared by the test database implementations.
 * 
 * @author hoffmann
 */
class TestDBSetup {

    static void loadSchema(final DataSource datasource, String scriptDir,
            final DatabaseVersionInfo toVersion, String dbType)
            throws Exception {
        Connection connection = datasource.getConnection();
        DatabaseUpgradeHandler handler = new DatabaseUpgradeHandler();
        handler.performUpgrade(connection, scriptDir, toVersion, dbType,
                new PrintStream(new DevNul()));
        connection.close();
    }

    static void purgeInstanceData(final DataSource datasource,
            final String user, final boolean clearVersion) throws Exception {
        final Connection conn = datasource.getConnection();
        final ResultSet tables = conn.getMetaData().getTables(null, user, null,
                null);
        Statement stmt = conn.createStatement();
        List<String> tableNames = new ArrayList<String>();
        while (tables.next()) {
            String name = tables.getString("TABLE_NAME");
            if (tables.getString("TABLE_TYPE").equals("TABLE")
                    && (clearVersion || !name.equalsIgnoreCase("version"))) {
                tableNames.add(name);
            }
        }
        // Exclude sequence counters:
        tableNames.remove("hibernate_sequences");
        tableNames.remove("version");
        while (tableNames.size() > 0) {
            // at least one table will be cleaned per run
            String tableName = tableNames.remove(0);
            try {
                stmt.execute("DELETE FROM " + tableName);
            } catch (SQLException e) {
                tableNames.add(tableName);
                setFKColumnsNull(conn, user, tableName);
            }
        }
        stmt.close();
        conn.close();
    }

    static void purgeSchema(final DataSource datasource, final String user)
            throws Exception {
        final Connection conn = datasource.getConnection();
        final ResultSet tables = conn.getMetaData().getTables(null, user, null,
                new String[] { "TABLE" });
        final Statement stmt = conn.createStatement();
        final List<String> tableNames = new ArrayList<String>();
        while (tables.next()) {
            tableNames.add(tables.getString("TABLE_NAME"));
        }
        while (tableNames.size() > 0) {
            // at least one table will be removed per run
            String tableName = tableNames.remove(0);
            try {
                deleteConstraints(conn, tableName);
                stmt.execute("DROP TABLE " + tableName);
            } catch (SQLException e) {
                tableNames.add(tableName);
            }
        }
        stmt.close();
        conn.close();
    }

    private static void setFKColumnsNull(Connection conn, String user,
            String tableName) throws SQLException {
        ResultSet rs = conn.getMetaData()
                .getExportedKeys(null, null, tableName);
        Statement stmt = conn.createStatement();
        while (rs.next()) {
            String sourceTableName = rs.getString("FKTABLE_NAME");
            String columnName = rs.getString("FKCOLUMN_NAME");
            ResultSet columns = conn.getMetaData().getColumns(null, user,
                    sourceTableName, columnName);
            while (columns.next()) {
                if (columns.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls) {
                    String queryString = String.format(
                            "UPDATE %s SET %s = NULL", sourceTableName,
                            columnName);
                    stmt.executeUpdate(queryString);
                }
            }
        }
    }

    private static void deleteConstraints(final Connection conn,
            String tableName) throws SQLException {
        ResultSet rs = conn.getMetaData()
                .getExportedKeys(null, null, tableName);
        Statement stmt = conn.createStatement();
        while (rs.next()) {
            String constraintName = rs.getString("FK_NAME");
            String sourceTableName = rs.getString("FKTABLE_NAME");
            String queryString = "ALTER TABLE " + sourceTableName
                    + " DROP CONSTRAINT " + constraintName;
            stmt.executeUpdate(queryString);
        }
        stmt.close();
    }

    private static class DevNul extends OutputStream {
        @Override
        public void write(int arg0) throws IOException {
        }
    }

}
