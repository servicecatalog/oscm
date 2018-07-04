/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Sep 13, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import org.oscm.app.domain.PlatformConfigurationKey;

/**
 * @author Dirk Bernsau
 * 
 */
public class PropertyImportTest {

    private Connection sqlConn;
    private ArrayList<String> sqlStatementes;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private Stack<PreparedStatement> prepStatements;

    private String p_driverClass;
    private String p_driverURL;
    private String p_userName;
    private String p_userPwd;
    private String p_propertyFile;
    private boolean p_overwriteFlag;
    private String p_controllerId;
    private File tempFile;

    @Before
    public void setup() throws Exception {

        sqlConn = Mockito.mock(Connection.class);
        statement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);
        sqlStatementes = new ArrayList<>();
        prepStatements = new Stack<>();
        Mockito.when(sqlConn.prepareStatement(Matchers.anyString()))
                .thenReturn(statement);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);

        p_driverClass = "java.lang.String";
        tempFile = File.createTempFile("temp", ".properties");
        tempFile.deleteOnExit();
        p_propertyFile = tempFile.getAbsolutePath();
    }

    @Test
    public void testMainNull() throws Exception {
        try {
            PropertyImport.main(null);
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(PropertyImport.ERR_USAGE, e.getMessage());
        }
    }

    @Test
    public void testMainLessArgs() throws Exception {
        try {
            PropertyImport.main(new String[3]);
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(PropertyImport.ERR_USAGE, e.getMessage());
        }
    }

    @Test
    public void testMainMoreArgs() throws Exception {
        try {
            PropertyImport.main(new String[8]);
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(PropertyImport.ERR_USAGE, e.getMessage());
        }
    }

    @Test
    public void testMain5Args() throws Exception {
        String[] args = new String[5];
        args[0] = "java.lang.String"; // any class will do for test classloading

        try {
            PropertyImport.main(args);
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(PropertyImport.ERR_DB_CONNECTION, e.getMessage());
        }
    }

    @Test
    public void testMain6Args() throws Exception {
        String[] args = new String[6];
        args[0] = "not.a.class";

        try {
            PropertyImport.main(args);
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(
                    PropertyImport.ERR_DRIVER_CLASS_NOT_FOUND.replaceFirst(
                            PropertyImport.ERR_PARAM_ESC, args[0]),
                    e.getMessage());
        }
    }

    @Test
    public void testMain7Args() throws Exception {
        String[] args = new String[7];
        try {
            PropertyImport.main(args);
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(PropertyImport.ERR_DRIVER_CLASS_NULL, e.getMessage());
        }
    }

    @Test
    public void test_update() throws Exception {
        p_overwriteFlag = true;
        p_controllerId = "PROXY";
        Properties p = getProperties();
        PreparedStatement secondStatement = Mockito
                .mock(PreparedStatement.class);
        PreparedStatement selectConfigSettingsStatement = Mockito
                .mock(PreparedStatement.class);
        ResultSet secondResult = Mockito.mock(ResultSet.class);
        ResultSet thirdResult = Mockito.mock(ResultSet.class);
        prepStatements.push(selectConfigSettingsStatement);
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
        Mockito.when(selectConfigSettingsStatement.executeQuery())
                .thenReturn(thirdResult);

        // finding one entry
        Mockito.when(new Boolean(resultSet.next())).thenReturn(Boolean.TRUE)
                .thenReturn(Boolean.FALSE);
        Mockito.when(new Integer(resultSet.getInt(Matchers.anyInt())))
                .thenReturn(new Integer(0));
        Mockito.when(new Boolean(secondResult.next())).thenReturn(Boolean.TRUE)
                .thenReturn(Boolean.FALSE);
        Mockito.when(new Integer(secondResult.getInt(Matchers.anyInt())))
                .thenReturn(new Integer(4));

        Mockito.doReturn("mm").when(thirdResult).getString(Matchers.anyInt());

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
        String[] expected = new String[] { "SELECT", "INSERT ", "UPDATE ",
                "DELETE " };
        int cnt = 0;
        assertEquals(expected.length, sqlStatementes.size());
        for (String sql : sqlStatementes) {
            assertTrue(sql.startsWith(expected[cnt++]));
        }
    }

    @Test
    public void test_insert() throws Exception {
        Properties p = getProperties();
        PreparedStatement secondStatement = Mockito
                .mock(PreparedStatement.class);
        PreparedStatement selectConfigSettingsStatement = Mockito
                .mock(PreparedStatement.class);
        ResultSet secondResult = Mockito.mock(ResultSet.class);
        ResultSet thirdResult = Mockito.mock(ResultSet.class);

        prepStatements.push(selectConfigSettingsStatement);
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
        Mockito.when(selectConfigSettingsStatement.executeQuery())
                .thenReturn(thirdResult);
        // finding one entry
        Mockito.when(new Boolean(resultSet.next())).thenReturn(Boolean.TRUE)
                .thenReturn(Boolean.FALSE);
        Mockito.when(new Integer(resultSet.getInt(Matchers.anyInt())))
                .thenReturn(new Integer(0));
        Mockito.when(new Boolean(secondResult.next())).thenReturn(Boolean.TRUE)
                .thenReturn(Boolean.FALSE);
        Mockito.when(new Integer(secondResult.getInt(Matchers.anyInt())))
                .thenReturn(new Integer(4));
        Mockito.doReturn("mm").when(thirdResult).getString(Matchers.anyInt());

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
            assertTrue(sql.startsWith(expected[cnt++]));
        }
    }

    @Test
    public void test_noFile() throws Exception {
        p_propertyFile = "/does/not/exist";
        p_controllerId = ""; // for coverage
        PropertyImport importer = createImport();

        try {
            importer.execute();
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(
                    PropertyImport.ERR_FILE_NOT_FOUND.replaceFirst(
                            PropertyImport.ERR_PARAM_ESC, p_propertyFile),
                    e.getMessage());
        }
    }

    @Test
    public void test_closeException() throws Exception {
        Mockito.doThrow(new SQLException()).when(sqlConn).close();
        p_controllerId = null; // for coverage
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

        try {
            importer.execute();
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(PropertyImport.ERR_DB_CLOSE, e.getMessage());
        }
    }

    @Test
    public void test_mandatoryNullRunrimeException() throws Exception {

        p_controllerId = "PROXY";
        Properties p = getProperties();
        p.put(PlatformConfigurationKey.BSS_AUTH_MODE.name(), "   ");

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
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(
                    String.format(PropertyImport.ERR_MANDATORY_ATTRIBUTE,
                            PlatformConfigurationKey.BSS_AUTH_MODE.name()),
                    e.getMessage());
        }
    }

    @Test
    public void test_prepareException() throws Exception {
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

        try {
            importer.execute();
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(PropertyImport.ERR_DB_LOAD_SETTINGS, e.getMessage());
        }
    }

    @Test
    public void execute_importControllerSettings() throws Exception {
        // given
        p_controllerId = "CONTROLLER_ID";
        Properties p = new Properties();
        p.put("CONTROLLER_ID", "ess.vmware");
        p.put("IAAS_API_KEYSTORE_TYPE", "keysore");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            p.store(fos, "No comment");
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        PropertyImport importer = spy(createImport());
        // when
        importer.execute();
        // then
        verify(importer, times(1)).trimValue(anyString());
    }

    @Test
    public void trimValue_notNull() {
        // given
        PropertyImport importer = createImport();
        // when
        String result = importer.trimValue("  dd  ");
        // then
        assertEquals("dd", result);
    }

    @Test
    public void trimValue_Empty() {
        // given
        PropertyImport importer = createImport();
        // when
        String result = importer.trimValue("  ");
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.isEmpty()));
    }

    @Test
    public void trimValue_Null() {
        // given
        PropertyImport importer = spy(createImport());
        // when
        String result = importer.trimValue(null);
        // then
        assertEquals(null, result);
    }

    private PropertyImport createImport() {
        return createImport(p_driverClass, p_driverURL, p_userName, p_userPwd,
                p_propertyFile, p_overwriteFlag, p_controllerId);
    }

    private PropertyImport createImport(String driverClass, String driverURL,
            String userName, String userPwd, String propertyFile,
            boolean overwriteFlag, String controllerId) {
        return new PropertyImport(driverClass, driverURL, userName, userPwd,
                propertyFile, overwriteFlag, controllerId) {
            @Override
            protected Connection getConnetion() throws SQLException {
                return sqlConn;
            }
        };
    }

    private Properties getProperties() {
        Properties p = new Properties();
        p.put(PlatformConfigurationKey.APP_BASE_URL.name(),
                "http://www.fujitsu.com");
        p.put(PlatformConfigurationKey.APP_ADMIN_MAIL_ADDRESS.name(),
                "here@there.com");
        p.put(PlatformConfigurationKey.APP_TIMER_INTERVAL.name(), "15000");
        p.put(PlatformConfigurationKey.APP_TIMER_REFRESH_SUBSCRIPTIONS.name(), "86400000");
        p.put(PlatformConfigurationKey.APP_TIMER_REFRESH_USAGEDATA.name(), "86400000");
        p.put(PlatformConfigurationKey.BSS_WEBSERVICE_URL.name(),
                "http://www.fujitsu.com/{service}/BASIC?wsdl");
        p.put(PlatformConfigurationKey.BSS_USER_KEY.name(), "1000");
        p.put(PlatformConfigurationKey.BSS_USER_PWD.name(), "_crypt:admin123");
        p.put(PlatformConfigurationKey.BSS_AUTH_MODE.name(), "INTERNAL");
        p.put(PlatformConfigurationKey.BSS_WEBSERVICE_WSDL_URL.name(),
                "http://www.fujitsu.com/{service}/oscm/BASIC?wsdl");
        p.put(PlatformConfigurationKey.BSS_STS_WEBSERVICE_WSDL_URL.name(),
                "http://www.fujitsu.com/{service}/oscm/STS?wsdl");
        p.put(PlatformConfigurationKey.APP_KEY_PATH.name(), "./key");
        p.put(PlatformConfigurationKey.APP_TRUSTSTORE.name(), "./cacert.jsk");
        p.put(PlatformConfigurationKey.APP_TRUSTSTORE_PASSWORD.name(),
                "changeit");
        p.put(PlatformConfigurationKey.APP_TRUSTSTORE_BSS_ALIAS.name(),
                "bes-s1as");
        return p;
    }
}
