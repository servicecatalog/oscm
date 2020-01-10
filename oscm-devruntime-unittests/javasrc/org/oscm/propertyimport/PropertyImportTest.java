/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-8-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.propertyimport;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * @author Qiu
 * 
 */
public class PropertyImportTest {

    private Connection sqlConn;
    private ArrayList<String> sqlStatementes;
    private PreparedStatement statement;
    private PreparedStatement countStatement;
    private ResultSet resultSet;
    private ResultSet countResultSet;
    private Stack<PreparedStatement> prepStatements;

    private String p_driverClass;
    private String p_driverURL;
    private String p_userName;
    private String p_userPwd;
    private String p_propertyFile;
    private boolean p_overwriteFlag;
    private String p_contextId;
    private File tempFile;

    @Before
    public void setup() throws Exception {

        sqlConn = Mockito.mock(Connection.class);
        statement = Mockito.mock(PreparedStatement.class);
        countStatement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);
        countResultSet = Mockito.mock(ResultSet.class);
        sqlStatementes = new ArrayList<>();
        prepStatements = new Stack<>();
        Mockito.when(sqlConn.prepareStatement(Matchers.anyString()))
                .thenReturn(statement);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);

        Mockito.when(sqlConn.createStatement()).thenReturn(countStatement);
        Mockito.when(countStatement.executeQuery(Matchers.anyString()))
                .thenReturn(countResultSet);
        Mockito.when(new Boolean(countResultSet.next()))
                .thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        Mockito.when(new Integer(countResultSet.getInt(Matchers.anyInt())))
                .thenReturn(new Integer(1));

        p_driverClass = "java.lang.String";
        tempFile = File.createTempFile("temp", ".properties");
        tempFile.deleteOnExit();
        p_propertyFile = tempFile.getAbsolutePath();
    }

    @Test(expected = RuntimeException.class)
    public void Main_Null() throws Exception {
        PropertyImport.main(null);
    }

    @Test(expected = RuntimeException.class)
    public void Main_LessArgs() throws Exception {
        PropertyImport.main(new String[3]);
    }

    @Test(expected = RuntimeException.class)
    public void Main_MoreArgs() throws Exception {
        PropertyImport.main(new String[8]);
    }

    @Test(expected = RuntimeException.class)
    public void Main_5Args() throws Exception {
        String[] args = new String[5];
        args[0] = "java.lang.String";
        PropertyImport.main(args);
    }

    @Test(expected = RuntimeException.class)
    public void Main_6Args() throws Exception {
        String[] args = new String[6];
        args[0] = "not.a.class";
        PropertyImport.main(args);
    }

    @Test(expected = RuntimeException.class)
    public void Main_7Args() throws Exception {
        String[] args = new String[7];
        PropertyImport.main(args);
    }

    @Test
    public void execute_update() throws Exception {
        p_overwriteFlag = true;
        p_contextId = "PROXY";
        Properties p = getProperties();
        PreparedStatement secondStatement = Mockito
                .mock(PreparedStatement.class);
        ResultSet secondResult = Mockito.mock(ResultSet.class);
        prepStatements.push(statement);
        prepStatements.push(secondStatement);
        Answer<PreparedStatement> answerStatement = new Answer<PreparedStatement>() {
            @Override
            public PreparedStatement answer(InvocationOnMock invocation)
                    throws Throwable {
                Object[] arguments = invocation.getArguments();
                if (arguments != null && arguments.length > 0) {
                    sqlStatementes.add(arguments[0].toString());
                }
                PreparedStatement firstElement;
                try {
                    firstElement = prepStatements.firstElement();
                } catch (NoSuchElementException e) {
                    return statement;
                }
                if (prepStatements.size() > 1) {
                    prepStatements.remove(0);
                }
                return firstElement;
            }
        };
        Mockito.doAnswer(answerStatement).when(sqlConn)
                .prepareStatement(Matchers.anyString());
        Mockito.when(secondStatement.executeQuery()).thenReturn(secondResult);
        // finding one entry
        Mockito.when(new Boolean(resultSet.next())).thenReturn(Boolean.TRUE)
                .thenReturn(Boolean.FALSE);
        Mockito.when(new Integer(resultSet.getInt(Matchers.anyInt())))
                .thenReturn(new Integer(0));
        Mockito.when(new Boolean(secondResult.next())).thenReturn(Boolean.TRUE)
                .thenReturn(Boolean.FALSE);
        Mockito.when(new Integer(secondResult.getInt(Matchers.anyInt())))
                .thenReturn(new Integer(4));

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            p.store(fos, "No comment");
            PropertyImport importer = createImport();
            importer.execute();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        String[] expected = new String[] { "SELECT ", "INSERT ", "UPDATE ",
                "DELETE " };
        int cnt = 0;
        assertEquals(expected.length, sqlStatementes.size());
        for (String sql : sqlStatementes) {
            assertEquals(Boolean.TRUE,
                    Boolean.valueOf(sql.startsWith(expected[cnt++])));
        }
    }

    @Test(expected = RuntimeException.class)
    public void execute_InvalidAuthMode() throws Exception {
        p_overwriteFlag = true;
        p_contextId = "PROXY";
        Properties p = getProperties();
        p.put(ConfigurationKey.AUTH_MODE.name(), "Invalid Value");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            p.store(fos, "No comment");
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        PropertyImport importer = createImport();
        try {
            importer.execute();
        } catch (RuntimeException e) {
            assertEquals(
                    "Authentication mode has an invalid value - Allowed values are [INTERNAL, OIDC]",
                    e.getMessage());
            throw e;
        }

    }

    @Test(expected = RuntimeException.class)
    public void execute_nullMandatoryValueInInternalMode() throws Exception {
        p_overwriteFlag = true;
        p_contextId = "PROXY";
        Properties p = getProperties();
        p.put(ConfigurationKey.AUTH_MODE.name(), "");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            p.store(fos, "No comment");
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        PropertyImport importer = createImport();
        try {
            importer.execute();
        } catch (RuntimeException e) {
            assertEquals(
                    "Mandatory attribute " + ConfigurationKey.AUTH_MODE.name()
                            + " can not be set a null value",
                    e.getMessage());
            throw e;
        }

    }

    @Test
    public void execute_insert() throws Exception {
        Properties p = getProperties();
        PreparedStatement secondStatement = Mockito
                .mock(PreparedStatement.class);
        ResultSet secondResult = Mockito.mock(ResultSet.class);
        prepStatements.push(statement);
        prepStatements.push(secondStatement);
        Answer<PreparedStatement> answerStatement = new Answer<PreparedStatement>() {
            @Override
            public PreparedStatement answer(InvocationOnMock invocation)
                    throws Throwable {
                Object[] arguments = invocation.getArguments();
                if (arguments != null && arguments.length > 0) {
                    sqlStatementes.add(arguments[0].toString());
                }
                PreparedStatement firstElement;
                try {
                    firstElement = prepStatements.firstElement();
                } catch (NoSuchElementException e) {
                    return statement;
                }
                if (prepStatements.size() > 1) {
                    prepStatements.remove(0);
                }
                return firstElement;
            }
        };
        Mockito.doAnswer(answerStatement).when(sqlConn)
                .prepareStatement(Matchers.anyString());
        Mockito.when(secondStatement.executeQuery()).thenReturn(secondResult);
        // finding one entry
        Mockito.when(new Boolean(resultSet.next())).thenReturn(Boolean.TRUE)
                .thenReturn(Boolean.FALSE);
        Mockito.when(new Integer(resultSet.getInt(Matchers.anyInt())))
                .thenReturn(new Integer(0));
        Mockito.when(new Boolean(secondResult.next())).thenReturn(Boolean.TRUE)
                .thenReturn(Boolean.FALSE);
        Mockito.when(new Integer(secondResult.getInt(Matchers.anyInt())))
                .thenReturn(new Integer(4));

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            p.store(fos, "No comment");
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        PropertyImport importer = createImport();
        importer.execute();
        String[] expected = new String[] { "SELECT ", "INSERT ", "DELETE " };
        int cnt = 0;
        assertEquals(expected.length, sqlStatementes.size());
        for (String sql : sqlStatementes) {
            assertEquals(Boolean.TRUE,
                    Boolean.valueOf(sql.startsWith(expected[cnt++])));
        }
    }

    @Test(expected = RuntimeException.class)
    public void execute_noFile() throws Exception {
        p_propertyFile = "/does/not/exist";
        p_contextId = ""; // for coverage
        PropertyImport importer = createImport();
        importer.execute();
    }

    @Test(expected = RuntimeException.class)
    public void execute_closeException() throws Exception {
        Mockito.doThrow(new SQLException()).when(sqlConn).close();
        p_contextId = null; // for coverage
        PropertyImport importer = createImport();
        importer.execute();
    }

    @Test(expected = RuntimeException.class)
    public void execute_prepareException() throws Exception {
        Mockito.when(sqlConn.prepareStatement(Matchers.anyString()))
                .thenThrow(new SQLException());
        Properties p = getProperties();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            p.store(fos, "No comment");
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        PropertyImport importer = createImport();
        importer.execute();
    }

    @Test(expected = RuntimeException.class)
    public void execute_secondPrepareException() throws Exception {
        Mockito.when(sqlConn.prepareStatement(Matchers.anyString()))
                .thenReturn(statement).thenThrow(new SQLException());
        Properties p = getProperties();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            p.store(fos, "No comment");
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        PropertyImport importer = createImport();
        importer.execute();
    }

    private PropertyImport createImport() {
        return createImport(p_driverClass, p_driverURL, p_userName, p_userPwd,
                p_propertyFile, p_overwriteFlag, p_contextId);
    }

    private PropertyImport createImport(String driverClass, String driverURL,
            String userName, String userPwd, String propertyFile,
            boolean overwriteFlag, String contextId) {
        return new PropertyImport(driverClass, driverURL, userName, userPwd,
                propertyFile, overwriteFlag, contextId) {
            @Override
            protected Connection getConnetion() throws SQLException {
                return sqlConn;
            }
        };
    }

    private Properties getProperties() {
        Properties p = new Properties();
        p.put(ConfigurationKey.AUTH_MODE.name(), "INTERNAL");
        p.put(ConfigurationKey.BASE_URL.name(), "http://localhost:8180");
        p.put(ConfigurationKey.SSO_STS_URL.name(), "http://localhost:8680");

        p.put(ConfigurationKey.BASE_URL_HTTPS.name(), "http://localhost:8180");
        p.put(ConfigurationKey.LOG_FILE_PATH.name(), "../logs");
        p.put(ConfigurationKey.PSP_USAGE_ENABLED.name(), "false");
        p.put(ConfigurationKey.TAGGING_MAX_TAGS.name(), "20");
        p.put(ConfigurationKey.TAGGING_MIN_SCORE.name(), "1");

        p.put(ConfigurationKey.WS_TIMEOUT.name(), "180000");
        p.put(ConfigurationKey.SSO_DEFAULT_TENANT_ID.name(), "8f96dede");
        p.put(ConfigurationKey.SSO_IDP_SAML_ASSERTION_ISSUER_ID.name(),
                "default");
        p.put(ConfigurationKey.HIDDEN_UI_ELEMENTS.name(),
                "operator.manageBillingAdapters,techService.viewBillingAdapters");
        p.put(ConfigurationKey.SSO_SIGNING_KEYSTORE.name(), "./cacerts.jks");
        p.put(ConfigurationKey.SSO_SIGNING_KEYSTORE_PASS.name(), "changeit");
        p.put(ConfigurationKey.SSO_SIGNING_KEY_ALIAS.name(), "s1as");
        return p;
    }
}
