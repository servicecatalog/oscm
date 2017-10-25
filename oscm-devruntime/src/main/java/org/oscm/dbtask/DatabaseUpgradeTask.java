/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: Jul 21, 2011                                                      
 *                                                                              
 *  Completion Time: Jul 21, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tokoda
 * 
 */
public abstract class DatabaseUpgradeTask {

    public static final String COMMAND_IDENTIFIER = "run:";
    public static final String PACKAGE_PATH = "org.oscm.dbtask.";

    protected static final String TABLE_BILLINGRESULT = "billingresult";
    protected static final String TABLE_BILLINGSHARESRESULT = "billingsharesresult";
    protected static final String COLUMN_TKEY = "tkey";
    protected static final String COLUMN_RESULTXML = "resultxml";
    protected static final String COLUMN_RESULTTYPE = "resulttype";

    private Connection connection;
    private List<Statement> statements = new ArrayList<Statement>();

    /**
     * Executes the database upgrade task defined as command parameter.
     * 
     * @param command
     *            Form is 'run:(ClassName)', and the class has to inherit this
     *            Class and belong to the package of this Class.
     * @param connection
     *            database connection
     * @throws Exception
     */
    public static void invoke(String command, Connection connection)
            throws Exception {
        DatabaseUpgradeTask taskObj = null;
        try {
            taskObj = createTaskObject(command);
            taskObj.setConnection(connection);
            taskObj.execute();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (taskObj != null) {
                taskObj.closeStatements();
            }
        }
    }

    /**
     * Returns true if the command is appropriate for invocation the database
     * upgrade task as a Java code. Command form is 'run:(ClassName)'.
     * 
     * @param command
     *            The command string of database upgrade script
     * @return true if the command is appropriate for invocation the database
     */
    @SuppressWarnings("unchecked")
    public static boolean isExecutableCommand(String command) {
        boolean executable = command.trim().matches(
                COMMAND_IDENTIFIER + "[a-zA-Z]*");

        if (executable) {
            try {
                String classPath = extractClassPath(command);
                Class<DatabaseUpgradeTask> taskClass = (Class<DatabaseUpgradeTask>) Class
                        .forName(classPath);
                Method taskMethod = taskClass.getMethod("execute");
                if (taskMethod == null) {
                    executable = false;
                }
            } catch (Exception e) {
                executable = false;
            }
        }

        return executable;
    }

    /**
     * Execute the specific database upgrade task which is override in sub
     * classes.
     */
    public abstract void execute() throws Exception;

    @SuppressWarnings("unchecked")
    private static DatabaseUpgradeTask createTaskObject(String command)
            throws Exception {
        String classPath = extractClassPath(command);
        Class<DatabaseUpgradeTask> taskClass = (Class<DatabaseUpgradeTask>) Class
                .forName(classPath);
        DatabaseUpgradeTask taskObj = taskClass.getConstructor().newInstance();
        return taskObj;
    }

    private static String extractClassPath(String command) {
        String className = PACKAGE_PATH
                + command.trim().replaceFirst(COMMAND_IDENTIFIER, "");
        return className;
    }

    private ResultSet executeQuery(String sql) throws Exception {
        ResultSet result = getStatement().executeQuery(sql);
        return result;
    }

    /**
     * Returns all records of the table designated.
     * 
     * @param table
     * @return all records of the table
     * @throws Exception
     */
    protected ResultSet getRecordsByTable(String table) throws Exception {
        ResultSet result = executeQuery("SELECT * FROM " + table + ";");
        return result;
    }

    /**
     * Returns the value of the configuration key designated.
     * 
     * @param key
     *            configuration key
     * @return the value of the configuration key
     * @throws Exception
     */
    protected String getConfigSettingValue(String key) throws Exception {
        String value = null;
        String sql = "SELECT env_value FROM configurationsetting WHERE information_id='"
                + key + "';";
        ResultSet result = executeQuery(sql);
        if (result.next()) {
            value = result.getString("env_value");
        }
        return value;
    }

    protected Connection getConnection() {
        return connection;
    }

    protected void setConnection(Connection connection) throws Exception {
        this.connection = connection;
    }

    protected Statement getStatement() throws Exception {
        Statement statement = connection.createStatement();
        statements.add(statement);
        return statement;
    }

    protected PreparedStatement getPreparedStatement(String sql)
            throws Exception {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        statements.add(preparedStatement);
        return preparedStatement;
    }

    private void closeStatements() throws Exception {
        for (Statement statement : statements) {
            if (statement != null) {
                statement.close();
                statement = null;
            }
        }
    }

    protected void updateBillingResultTable(String tkey, String migratedXml)
            throws Exception {
        String sql = String.format("UPDATE %s SET %s=? WHERE tkey=?;",
                TABLE_BILLINGRESULT, COLUMN_RESULTXML);
        PreparedStatement stmt = getPreparedStatement(sql);
        stmt.setString(1, migratedXml);
        stmt.setLong(2, Long.parseLong(tkey));
        stmt.executeUpdate();
        stmt.close();
    }

    protected void updateBillingSharesResultTable(String tkey,
            String migratedXml) throws Exception {
        String sql = String.format("UPDATE %s SET %s=? WHERE tkey=?;",
                TABLE_BILLINGSHARESRESULT, COLUMN_RESULTXML);
        PreparedStatement stmt = getPreparedStatement(sql);
        stmt.setString(1, migratedXml);
        stmt.setLong(2, Long.parseLong(tkey));
        stmt.executeUpdate();
        stmt.close();
    }

}
