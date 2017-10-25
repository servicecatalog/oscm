/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 02.07.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.setup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Provides some basic functionality required to handle database related tasks.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class DatabaseTaskHandler {

    private final static String ROOT_PATH = "../../../../../../";
    private static DatabaseConnection dbConn;
    private static Connection conn;

    public static void tearDown() {
        try {
            dbConn.close();
            conn.close();
        } catch (Exception e) {

        }
    }

    public static void init(String dbDriverClassName, String databaseDriverURL,
            String databaseUserName, String databaseUserPwd)
            throws ClassNotFoundException, SQLException, DatabaseUnitException {
        Class.forName(dbDriverClassName);
        conn = DriverManager.getConnection(databaseDriverURL, databaseUserName,
                databaseUserPwd);
        dbConn = new DatabaseConnection(conn);
    }

    public static List<String> getOrganizationsFromDB() throws SQLException {
        List<String> organizations = new ArrayList<String>();
        // initiate the directory server
        // as the database assigns the id values to the user and organization
        // entries, those have to be fetched from the database first - pretty
        // ugly
        // so current restriction is: user xml file will be ignored!
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT TKEY FROM ORGANIZATION");
        while (rs.next()) {
            organizations.add(String.valueOf(rs.getLong(1)));
        }
        rs.close();
        return organizations;
    }

    public static List<UserData> getUsers() throws SQLException {
        List<UserData> users = new ArrayList<UserData>();

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt
                .executeQuery("SELECT TKEY, ORGANIZATIONKEY, USERID, ORGANIZATIONADMIN FROM PLATFORMUSER");
        while (rs.next()) {
            String key = String.valueOf(rs.getLong(1));
            String ouKey = String.valueOf(rs.getLong(2));
            String userId = rs.getString(3);
            boolean isOrganizationAdmin = rs.getBoolean(4);
            UserData userdata = new UserData();
            userdata.setOrganizationAdmin(isOrganizationAdmin);
            userdata.setUserKey(key);
            userdata.setUserId(userId);
            userdata.setOrganizationKey(ouKey);
            users.add(userdata);
        }
        rs.close();
        return users;
    }

    public static void cleanDatabase() throws IOException, DataSetException,
            DatabaseUnitException, SQLException {
        String filePath = ROOT_PATH + "javares/devscripts/deleteDbContent.xml";
        String tableList = DatabaseTaskHandler.class.getResource(".").getFile()
                + filePath;
        IDataSet dataToDelete = new FlatXmlDataSetBuilder().build(new File(
                tableList));
        DatabaseOperation.DELETE_ALL.execute(dbConn, dataToDelete);
    }

    /**
     * Inserts the file of the dbUnit compliant flat xml file into the database
     * used by the system.
     * 
     * @param dbSrcFileName
     *            The name of the file to be imported.
     * @throws IOException
     * @throws DataSetException
     * @throws DatabaseUnitException
     * @throws SQLException
     */
    public static void insertData(String dbSourceFileLocation)
            throws IOException, DataSetException, DatabaseUnitException,
            SQLException {
        if (dbSourceFileLocation == null) {
            return;
        }
        ReplacementDataSet dataSet = new ReplacementDataSet(
                new FlatXmlDataSetBuilder()
                        .build(new File(dbSourceFileLocation)));
        dataSet.addReplacementObject("[NULL]", null);
        Date now = new Date();
        dataSet.addReplacementObject("[SYSDATE]", Long.valueOf(now.getTime()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        dataSet.addReplacementObject("[SYSTIMESTAMP]", sdf.format(now));

        DatabaseOperation.INSERT.execute(dbConn, dataSet);
    }

    public void insertData(IDataSet dataToBeInserted)
            throws DatabaseUnitException, SQLException {
        DatabaseOperation.INSERT.execute(dbConn, dataToBeInserted);
    }

    public static void dropTables(String filePath) throws DataSetException,
            IOException, SQLException {
        IDataSet tablesToDelete = new FlatXmlDataSetBuilder().build(new File(
                filePath));
        String[] tableNames = tablesToDelete.getTableNames();
        Statement stmt = conn.createStatement();
        String queryString = "DROP TABLE %s CASCADE";
        for (int i = tableNames.length - 1; i >= 0; i--) {
            // first drop constraints to the table
            deleteConstraints(tableNames[i]);

            // now drop the table itself
            String tableName = tableNames[i];
            try {
                String query = String.format(queryString, tableName);
                stmt.executeUpdate(query);
                System.out.println(query);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        stmt.close();
    }

    /**
     * Removes all referential constraints for the table with the given name
     * found via the metadata.
     * 
     * @param tableName
     *            The name of the table.
     * @throws SQLException
     */
    private static void deleteConstraints(String tableName) throws SQLException {
        ResultSet rs = conn.getMetaData().getExportedKeys(null, null,
                tableName.toUpperCase());
        Statement stmt = conn.createStatement();
        while (rs.next()) {
            String constraintName = rs.getString("FK_NAME");
            String sourceTableName = rs.getString("FKTABLE_NAME");
            String queryString = "ALTER TABLE " + sourceTableName
                    + " DROP FOREIGN KEY " + constraintName;
            stmt.executeUpdate(queryString);
            System.out.println(queryString);
        }
    }

    public static void insertConfigSettings(Properties props)
            throws IOException, DatabaseUnitException, SQLException {
        // create a temporary file representing the configuration settings in
        // dbunit xml format

        File tempFile = File.createTempFile("configsettings", "xml");
        PrintWriter pw = new PrintWriter(new FileOutputStream(tempFile));
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        ConfigurationKey[] allKeys = ConfigurationKey.values();
        pw.println("<dataset>");
        int count = 1;
        for (ConfigurationKey key : allKeys) {
            String keyName = key.getKeyName();
            String propValue = props.getProperty(keyName);
            if (propValue != null) {
                pw.println("<ConfigurationSetting TKEY=\""
                        + count
                        + "\" VERSION=\"0\" CONTEXT_ID=\"global\" INFORMATION_ID=\""
                        + keyName + "\" ENV_VALUE=\"" + propValue + "\"/>");
                count++;
            }
        }
        pw.println("</dataset>");
        pw.close();

        // insert the data to the database
        DatabaseTaskHandler.insertData(tempFile.getAbsolutePath());
    }

}
