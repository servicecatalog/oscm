/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.db;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.oscm.test.db.ITestDB;
import org.oscm.test.ejb.TestDataSources;

public class TestDatabase {
    private static final ITestDB TESTDATABASE = TestDataSources.get("oscm-app");

    public void initDatabase() throws Exception {
        System.out.println("Start database initialization...");
        TESTDATABASE.purgeSchema();
        TESTDATABASE.loadSchema();
        System.out.println("Database initialization finished");
    }

    public void insertData(URL dataSetUrl) throws Exception {
        System.out.println("Insert test data.");
        final IDataSet dataSet = loadDataSet(dataSetUrl);
        final DatabaseConnection connection = new DatabaseConnection(
                getDBconnection());
        DatabaseOperation.INSERT.execute(connection, dataSet);
    }

    public void clean() throws Exception {
        System.out.println("Clearn test data");
        TESTDATABASE.clearBusinessData();
    }

    public void close() throws Exception {
        System.out.println("Close database connection");
        TESTDATABASE.getDataSource().getConnection().close();
    }

    public Connection getDBconnection() throws SQLException {
        return TESTDATABASE.getDataSource().getConnection();
    }

    private IDataSet loadDataSet(URL source) throws Exception {
        final ReplacementDataSet set = new ReplacementDataSet(
                new FlatXmlDataSetBuilder().build(source));
        set.addReplacementObject("[NULL]", null);
        return set;
    }

    public void updateDBSchemaToLatestVersion() throws Exception {
        TESTDATABASE.loadSchema();
    }
}
