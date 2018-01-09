/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.Difference;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import org.oscm.setup.DatabaseVersionInfo;
import org.oscm.test.db.ITestDB;
import org.oscm.test.ejb.TestDataSources;

/**
 * Base class for schema and data migration test. Subclasses may use this class
 * to migrate a given data setup (specified as a DBUnit FlatXmlDataSet) to
 * another given version. The result is verified with comparison against another
 * FlatXmlDataSet.
 * 
 * @author hoffmann
 */
public abstract class SchemaUpgradeTestBase {

    private static final ITestDB TESTDB = TestDataSources
            .get("oscm-domainobjects");

    private final DatabaseVersionInfo fromVersion;
    private final DatabaseVersionInfo toVersion;

    protected SchemaUpgradeTestBase(final DatabaseVersionInfo fromVersion,
            final DatabaseVersionInfo toVersion) {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
    }

    @Test
    public void testUpgrade() throws Exception {
        TESTDB.purgeSchema();
        TESTDB.loadSchema(fromVersion);
        TESTDB.clearBusinessData();// get rid of data inserted by schema except
        // schema version
        final DatabaseConnection connection = new DatabaseConnection(TESTDB
                .getDataSource().getConnection());
        insertDataBeforeMigration(connection);
        TESTDB.loadSchema(toVersion);
        assertDataAfterMigration(connection);
    }

    protected void insertDataBeforeMigration(DatabaseConnection connection)
            throws Exception {
        final IDataSet dataSet = loadDataSet(getSetupDataset());
        DatabaseOperation.INSERT.execute(connection, dataSet);
    }

    protected abstract URL getSetupDataset();

    protected void assertDataAfterMigration(DatabaseConnection connection)
            throws Exception {
        final IDataSet expectedSet = loadDataSet(getExpectedDataset());
        for (final String tableName : expectedSet.getTableNames()) {
            assertTable(connection, expectedSet, tableName);
        }
    }

    protected abstract URL getExpectedDataset();

    protected IDataSet loadDataSet(URL source) throws Exception {
        final FlatXmlDataSetBuilder xmlData = new FlatXmlDataSetBuilder();
        xmlData.setColumnSensing(true);
        final ReplacementDataSet set = new ReplacementDataSet(
                xmlData.build(source));
        set.addReplacementObject("[NULL]", null);
        return set;
    }

    protected final void assertTable(final DatabaseConnection connection,
            final IDataSet expectedSet, final String tableName)
            throws Exception {
        final ITable expected = new SortedTable(
                expectedSet.getTable(tableName),
                expectedSet.getTableMetaData(tableName));
        final ITable actual = new SortedTable(
                connection.createTable(tableName),
                expectedSet.getTableMetaData(tableName));

        final ITable filteredActual = DefaultColumnFilter
                .includedColumnsTable(actual, filterColumns(expected
                        .getTableMetaData().getColumns()));
        Assertion.assertEquals(expected, filteredActual, new FailureHandler() {
            public void handle(Difference diff) {
                if (diff.getColumnName().equals("resultxml")) {
                    org.junit.Assert.assertEquals(
                            sanitize(diff.getActualValue()),
                            sanitize(diff.getExpectedValue()));
                } else {
                    try {
                        Assertion.assertEquals(expected, filteredActual);
                    } catch (DatabaseUnitException e) {
                        createFailure(e.getMessage());
                    }
                }
            }

            /**
             * @param obj
             * @return
             */
            private String sanitize(Object obj) {
                String s = obj.toString();
                s = s.replaceAll("\r", "");
                return s;
            }

            public Error createFailure(String arg0) {
                return new Error(arg0);
            }

            public Error createFailure(String arg0, String arg1, String arg2) {
                return new Error(arg0 + " * expected: " + arg1 + " * actual: "
                        + arg2);
            }

            public String getAdditionalInfo(ITable table1, ITable table2,
                    int column, String add) {
                return "table 1: " + table1.getTableMetaData().getTableName()
                        + " * table 2: " + table2.getTableMetaData()
                        + " * column: " + column + " * add: " + add;
            }

        });
    }

    private Column[] filterColumns(Column[] allColumns) {
        List<Column> result = new ArrayList<Column>();
        for (Column column : allColumns) {
            if (!column.getColumnName().equals("moddate")) {
                result.add(column);
            }
        }
        return result.toArray(new Column[result.size()]);
    }
}
