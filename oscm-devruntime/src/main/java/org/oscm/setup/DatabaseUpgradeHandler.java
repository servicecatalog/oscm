/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 26.11.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.setup;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.oscm.dbtask.DatabaseUpgradeTask;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.stream.Streams;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * This class is used to either create the database schema for the product from
 * scratch or to migrate an already existing version.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class DatabaseUpgradeHandler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(DatabaseUpgradeHandler.class);

    Log4jLogger getLogger() {
        return logger;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out
                    .println("Usage: java org.oscm.setup.DatabaseUpgradeHandler <db-properties-file-path> <script-file-path>");
            return;
        }

        final String dbPropertiesFile = args[0];
        final String pathToScriptFiles = args[1];

        DatabaseUpgradeHandler handler = new DatabaseUpgradeHandler();
        Properties dbProperties = HandlerUtils.readProperties(dbPropertiesFile);
        for (int i = 2; i < args.length; i++) {
            String key = null;
            if (args[i].startsWith(HandlerUtils.DB_DRIVER_CLASS)) {
                key = HandlerUtils.DB_DRIVER_CLASS;
            } else if (args[i].startsWith(HandlerUtils.DB_PWD)) {
                key = HandlerUtils.DB_PWD;
            } else if (args[i].startsWith(HandlerUtils.DB_USER)) {
                key = HandlerUtils.DB_USER;
            } else if (args[i].startsWith(HandlerUtils.DB_TYPE)) {
                key = HandlerUtils.DB_TYPE;
            } else if (args[i].startsWith(HandlerUtils.DB_HOST)) {
                key = HandlerUtils.DB_HOST;
            } else if (args[i].startsWith(HandlerUtils.DB_PORT)) {
                key = HandlerUtils.DB_PORT;
            } else if (args[i].startsWith(HandlerUtils.DB_NAME)) {
                key = HandlerUtils.DB_NAME;
            }
            if (key != null && args[i].length() > key.length()
                    && args[i].charAt(key.length()) == '=') {
                dbProperties.put(key, args[i].substring(key.length() + 1));
            }
        }
        Connection connection = null;
        try {
            connection = HandlerUtils.establishDatabaseConnection(dbProperties);
            handler.performUpgrade(connection, pathToScriptFiles,
                    DatabaseVersionInfo.MAX,
                    dbProperties.getProperty(HandlerUtils.DB_TYPE), System.out);
            System.out.println("Migration completed");
        } catch (Throwable ex) {
            logger.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_SQL);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignore) {
                    logger.logError(Log4jLogger.SYSTEM_LOG, ignore,
                            LogMessageIdentifier.ERROR_CLOSE_RESOURCE_FAILED);
                }
            }
        }
    }

    /**
     * Determines the current version from the database and calls all necessary
     * update scripts.
     * 
     * @param conn
     *            The database connection.
     * @param pathToScriptFiles
     *            The path to the database update script files.
     * @param toVersion
     *            The version (including) to which the update should be
     *            performed
     * @param dbType
     *            The database type.
     * @param out
     *            The output print stream for logs
     * @throws IOException
     *             Thrown in case the execution of the script file fails.
     * @throws SQLException
     */
    public void performUpgrade(Connection conn, String pathToScriptFiles,
            DatabaseVersionInfo toVersion, String dbType, PrintStream out)
            throws Exception {
        DatabaseVersionInfo currentSchemaVersion = getCurrentSchemaVersion(conn);
        out.println("Current database schema version is: "
                + currentSchemaVersion.toString());
        List<File> scriptFilesFromDirectory = getScriptFilesFromDirectory(
                pathToScriptFiles, dbType);
        List<File> fileExecutionOrder = getFileExecutionOrder(
                scriptFilesFromDirectory, currentSchemaVersion, toVersion);
        executeScriptFiles(conn, fileExecutionOrder, out);
    }

    /**
     * Calls the interpreter and passes the script files to it to execute them.
     * 
     * @param conn
     *            The database connection.
     * @param executionList
     *            The files to be executed in ordered list.
     * @param out
     *            The output print stream for logs
     * @throws IOException
     *             Thrown in case the execution of the script file fails.
     * @throws SQLException
     * @throws InterruptedException
     */
    private void executeScriptFiles(Connection conn, List<File> executionList,
            PrintStream out) throws IOException, SQLException {

        if (executionList.isEmpty()) {
            out.println("Schema is up to date, no changes necessary.");
            return;
        }

        for (File scriptFile : executionList) {
            out.println("\nProcessing file '" + scriptFile.getName() + "':\n");
            // check database connection
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            try {
                List<String> commands = getSQLCommandsFromFile(scriptFile);
                for (String command : commands) {
                    try {
                        if (DatabaseUpgradeTask.isExecutableCommand(command)) {
                            DatabaseUpgradeTask.invoke(command, conn);
                        } else {
                            stmt.execute(command);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Processing command '"
                                + command + "' failed, because\n"
                                + e.getMessage(), e);
                    }
                }
            } finally {
                closeStatement(stmt);
            }
            setSchemaVersion(conn, scriptFile);
            conn.commit();
        }
    }

    /**
     * Reads the specified source file and returns all SQL commands contained in
     * it. Comments will be ignored.
     * 
     * @param srcFile
     *            The file to be read.
     * @return The list of commands as string.
     * @throws IOException
     */
    protected List<String> getSQLCommandsFromFile(File srcFile)
            throws IOException {
        final String delimiter = ";";
        List<String> result = new ArrayList<String>();
        BufferedReader reader = null;
        InputStreamReader in = null;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(srcFile);
            in = new InputStreamReader(fin, "UTF-8");
            reader = new BufferedReader(in);
            StringBuffer currentLine = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (!line.startsWith("--")) {
                    boolean isEndOfStament = line.endsWith(delimiter);
                    if (isEndOfStament) {
                        currentLine.append(line.substring(0,
                                line.lastIndexOf(delimiter)));
                        result.add(currentLine.toString());
                        currentLine.setLength(0);
                    } else {
                        currentLine.append(line + "\r\n");
                    }
                }
                line = reader.readLine();
            }
        } finally {
            closeStream(fin);
            closeStream(in);
            closeStream(reader);
        }
        return result;
    }

    void closeStream(Closeable stream) {
        Streams.close(stream);
    }

    /**
     * Accesses the database and retrieves the current version information is
     * stored in the version table.
     * 
     * @param conn
     *            The database connection.
     * @return The version information of the currently present database schema.
     */
    private DatabaseVersionInfo getCurrentSchemaVersion(Connection conn) {
        // read the version information
        DatabaseVersionInfo result = DatabaseVersionInfo.MIN;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt
                    .executeQuery("SELECT productMajorVersion, productMinorVersion, schemaVersion FROM VERSION");
            while (rs.next()) {
                int majorVersion = rs.getInt(1);
                int minorVersion = rs.getInt(2);
                int schemaVersion = rs.getInt(3);
                result = new DatabaseVersionInfo(majorVersion, minorVersion,
                        schemaVersion);
            }
        } catch (SQLException e) {
            // do not print anything as per bug 6208
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
        }
        return result;
    }

    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
                logger.logError(Log4jLogger.SYSTEM_LOG, ignore,
                        LogMessageIdentifier.ERROR_CLOSE_RESOURCE_FAILED);
            }
        }
    }

    private void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {
                logger.logError(Log4jLogger.SYSTEM_LOG, ignore,
                        LogMessageIdentifier.ERROR_CLOSE_RESOURCE_FAILED);
            }
        }
    }

    /**
     * Determines the schema version information from the given file name and
     * updates the version table in the database accordingly.
     * 
     * @param conn
     *            The database connection.
     * @param updateFile
     *            The schema update file
     * @throws SQLException
     */
    private void setSchemaVersion(Connection conn, File updateFile)
            throws SQLException {
        DatabaseVersionInfo currentVersion = determineVersionInfoForFile(updateFile);
        setVersionInformation(conn, currentVersion, System.currentTimeMillis());
    }

    /**
     * Sets the specified version information in the version table in the
     * database.
     * 
     * @param conn
     *            The database connection.
     * @param versionInfoToSet
     *            The version information to be set.
     * @throws SQLException
     */
    private void setVersionInformation(Connection conn,
            DatabaseVersionInfo versionInfoToSet, long time)
            throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            stmt.execute("DELETE FROM VERSION");
        } finally {
            closeStatement(stmt);
        }

        PreparedStatement pStmt = conn
                .prepareStatement("INSERT INTO VERSION(productMajorVersion, productMinorVersion, schemaVersion, migrationDate) VALUES(?,?,?,?)");
        try {
            pStmt.setInt(1, versionInfoToSet.getProductMajorVersion());
            pStmt.setInt(2, versionInfoToSet.getProductMinorVersion());
            pStmt.setInt(3, versionInfoToSet.getSchemaVersion());
            pStmt.setTimestamp(4, new Timestamp(time));
            pStmt.execute();
        } finally {
            closeStatement(pStmt);
        }
    }

    /**
     * Returns all .sql script files following the naming pattern
     * "upd_&lt;dbs-name&gt;_&lt;productMajorVersion&gt;_&lt;productMinorVersion&gt;_&ltschemaVersion&gt;.sql"
     * that are stored in the given directory.
     * 
     * @param directoryPath
     *            The directory to search the files in.
     * @param dbType
     *            The database type.
     * @return The list of sql files.
     * @throws IOException
     */
    protected List<File> getScriptFilesFromDirectory(String directoryPath,
            final String dbType) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Specified path '" + directoryPath
                    + "' does not point to a valid directory");
        }

        File[] scriptFiles = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                String fileNameRegex = String.format(
                        "upd_%s+_\\d\\d_\\d\\d_\\d\\d.sql", dbType);
                return Pattern.matches(fileNameRegex, name);
            }
        });

        return Arrays.asList(scriptFiles);
    }

    /**
     * Determines the files that have to be executed (according to the provided
     * version information) and orders them in ascending order.
     * 
     * @param fileList
     *            The list of files to be investigated. The names must have
     *            passed the test in method
     *            {@link #getScriptFilesFromDirectory(String, String)}.
     * @return The files to be executed in the execution order.
     */
    protected List<File> getFileExecutionOrder(List<File> fileList,
            DatabaseVersionInfo currentVersion, DatabaseVersionInfo toVersion) {
        List<File> result = new ArrayList<File>();
        // if the file contains statements for a newer schema, add the file to
        // the list
        for (File file : fileList) {
            DatabaseVersionInfo info = determineVersionInfoForFile(file);
            if (info.compareTo(currentVersion) > 0
                    && info.compareTo(toVersion) <= 0) {
                result.add(file);
            }
        }
        // now sort the list ascending
        Collections.sort(result);

        return result;
    }

    /**
     * Reads the file name and determines the version info the file stands for.
     * 
     * @param file
     *            The file name to parse. The file name must have passed the
     *            test in method {@link #getScriptFilesFromDirectory(String)}.
     * @return The version info according to the information in the file name.
     */
    protected DatabaseVersionInfo determineVersionInfoForFile(File file) {
        String[] fileNameParts = file.getName().split("_");
        int productMajorVersion = Integer.parseInt(fileNameParts[2]);
        int productMinorVersion = Integer.parseInt(fileNameParts[3]);
        int schemaVersion = Integer.parseInt(fileNameParts[4].substring(0,
                fileNameParts[4].indexOf(".")));
        return new DatabaseVersionInfo(productMajorVersion,
                productMinorVersion, schemaVersion);
    }

}
