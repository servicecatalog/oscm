/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.domain.PlatformConfigurationKey;

public class PropertyImportControllerTest {

    private final static String DRIVER_CLASS = "org.oscm.app.setup.PropertyImport";
    private final static String DRIVER_CLASS_DUMMY = "class";
    private final static String DRIVER_URL = "url";
    private final static String USER_NAME = "user";
    private final static String USER_PASSWORD = "password";
    private final static String PROPERTY_FILE = "file";
    private final static String CONTROLLER_ID = "controller";
    private final static String CONTROLLER_ID_PROXY = "PROXY";
    private final static boolean OVERWRITE_FLAG = true;
    private final static String TEST_PLATFORM_KEY = PlatformConfigurationKey.APP_BASE_URL
            .name();
    private final static String TEST_CONTROLLER_KEY = "KEY";
    private final static String TEST_VALUE = "value";
    private final static String APP_BASE_URL = "http://www.fujitsu.com";
    private static Connection mockConn;
    private static PreparedStatement mockStmt;
    private static ResultSet mockResult;
    private static PropertyImport propImportProxy;
    private static PropertyImport propImportController;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setup() throws SQLException, FileNotFoundException {
        mockConn = Mockito.mock(Connection.class);
        mockStmt = Mockito.mock(PreparedStatement.class);
        mockResult = Mockito.mock(ResultSet.class);
        doReturn(mockStmt).when(mockConn).prepareStatement(anyString());
        doReturn(mockResult).when(mockStmt).executeQuery();

        propImportProxy = spy(new PropertyImport(DRIVER_CLASS, DRIVER_URL,
                USER_NAME, USER_PASSWORD, PROPERTY_FILE, OVERWRITE_FLAG, ""));
        doReturn(mockConn).when(propImportProxy).getConnetion();
        doReturn(null).when(propImportProxy).getInputStreamForProperties();
        doNothing().when(propImportProxy).createEntries(any(Connection.class),
                anyMap());
        doNothing().when(propImportProxy).updateEntries(any(Connection.class),
                anyMap());
        doNothing().when(propImportProxy).deleteEntries(any(Connection.class),
                anyMap());

        propImportController = spy(new PropertyImport(DRIVER_CLASS, DRIVER_URL,
                USER_NAME, USER_PASSWORD, PROPERTY_FILE, OVERWRITE_FLAG,
                CONTROLLER_ID));
        doReturn(mockConn).when(propImportController).getConnetion();
        doReturn(null).when(propImportController).getInputStreamForProperties();
        doNothing().when(propImportController)
                .createEntries(any(Connection.class), anyMap());
        doNothing().when(propImportController)
                .updateEntries(any(Connection.class), anyMap());
        doNothing().when(propImportController)
                .deleteEntries(any(Connection.class), anyMap());
    }

    enum ControllerId {
        EMPTY_VALUE, NOT_PRESENT, WITH_VALUE, RESERVED_APP_VALUE
    }

    @Test
    public void constructor() {
        // when
        PropertyImport propImport = new PropertyImport(DRIVER_CLASS, DRIVER_URL,
                USER_NAME, USER_PASSWORD, PROPERTY_FILE, OVERWRITE_FLAG,
                CONTROLLER_ID);

        // then
        assertEquals(DRIVER_URL, propImport.getDriverURL());
        assertEquals(USER_NAME, propImport.getUserName());
        assertEquals(USER_PASSWORD, propImport.getUserPwd());
        assertEquals(PROPERTY_FILE, propImport.getPropertyFile());
        assertEquals(CONTROLLER_ID, propImport.getControllerId());
        assertEquals(Boolean.valueOf(OVERWRITE_FLAG),
                Boolean.valueOf(propImport.isOverwriteFlag()));
    }

    @Test
    public void constructor_classNotExist() {
        try {
            new PropertyImport(DRIVER_CLASS_DUMMY, DRIVER_URL, USER_NAME,
                    USER_PASSWORD, PROPERTY_FILE, OVERWRITE_FLAG,
                    CONTROLLER_ID);
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(
                    PropertyImport.ERR_DRIVER_CLASS_NOT_FOUND.replaceFirst(
                            PropertyImport.ERR_PARAM_ESC, DRIVER_CLASS_DUMMY),
                    e.getMessage());
        }
    }

    @Test
    public void constructor_classNull() {
        try {
            new PropertyImport(null, DRIVER_URL, USER_NAME, USER_PASSWORD,
                    PROPERTY_FILE, OVERWRITE_FLAG, CONTROLLER_ID);
            fail("Runtime exception expected");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(PropertyImport.ERR_DRIVER_CLASS_NULL, e.getMessage());
        }
    }

    @Test
    public void constructor_controleIdNull() {
        // when
        PropertyImport propImport = new PropertyImport(DRIVER_CLASS, DRIVER_URL,
                USER_NAME, USER_PASSWORD, PROPERTY_FILE, OVERWRITE_FLAG, null);

        // then
        assertEquals(CONTROLLER_ID_PROXY, propImport.getControllerId());
    }

    @Test
    public void constructor_controleIdEmpty() {
        // when
        PropertyImport propImport = new PropertyImport(DRIVER_CLASS, DRIVER_URL,
                USER_NAME, USER_PASSWORD, PROPERTY_FILE, OVERWRITE_FLAG, "");

        // then
        assertEquals(CONTROLLER_ID_PROXY, propImport.getControllerId());
    }

    @Ignore
    @Test
    public void execute_ProxySettings() {
        // given
        Properties p = givenAppProperties(ControllerId.WITH_VALUE);
        doReturn(p).when(propImportProxy)
                .loadProperties(any(InputStream.class));

        // when
        propImportProxy.execute();

        // then
        assertEquals(CONTROLLER_ID_PROXY, propImportProxy.getControllerId());
    }

    @Test
    public void execute_ControllerSettings() {
        // given
        Properties p = givenAppProperties(ControllerId.WITH_VALUE);
        doReturn(p).when(propImportController)
                .loadProperties(any(InputStream.class));

        // when
        propImportController.execute();

        // then
        assertEquals(CONTROLLER_ID, propImportController.getControllerId());
    }

    @Test
    public void execute_ControllerSettings_EmptyControllerId() {
        // given
        Properties p = givenAppProperties(ControllerId.EMPTY_VALUE);
        doReturn(p).when(propImportController)
                .loadProperties(any(InputStream.class));

        // when
        try {
            propImportController.execute();
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(PropertyImport.ERR_CONTROLLER_ID_EMPTY,
                    e.getMessage());
        }
    }

    @Test
    public void execute_ControllerSettings_ReservedAppControllerId() {
        // given
        Properties p = givenAppProperties(ControllerId.RESERVED_APP_VALUE);
        doReturn(p).when(propImportController)
                .loadProperties(any(InputStream.class));

        // when
        try {
            propImportController.execute();
            fail("Runtime exception expected.");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(PropertyImport.ERR_CONTROLLER_ID_RESERVED,
                    e.getMessage());
        }
    }

    private Properties givenAppProperties(ControllerId controllerId) {
        Properties p = getProperties();
        if (ControllerId.WITH_VALUE.equals(controllerId)) {
            p.setProperty("CONTROLLER_ID", CONTROLLER_ID);
        } else if (ControllerId.EMPTY_VALUE.equals(controllerId)) {
            p.setProperty("CONTROLLER_ID", "");
        } else if (ControllerId.RESERVED_APP_VALUE.equals(controllerId)) {
            p.setProperty("CONTROLLER_ID", CONTROLLER_ID_PROXY);
        }
        p.setProperty(TEST_PLATFORM_KEY, APP_BASE_URL);
        p.setProperty(TEST_CONTROLLER_KEY, TEST_VALUE);
        return p;
    }

    private Properties getProperties() {
        Properties p = new Properties();

        p.put(PlatformConfigurationKey.APP_BASE_URL.name(), APP_BASE_URL);
        p.put(PlatformConfigurationKey.APP_ADMIN_MAIL_ADDRESS.name(),
                "here@there.com");
        p.put(PlatformConfigurationKey.APP_TIMER_INTERVAL.name(), "15000");
        p.put(PlatformConfigurationKey.APP_TIMER_REFRESH_SUBSCRIPTIONS.name(), "86400000");
        p.put(PlatformConfigurationKey.APP_TIMER_REFRESH_USAGEDATA.name(), "86400000");
        p.put(PlatformConfigurationKey.BSS_WEBSERVICE_URL.name(),
                "http://localhost:8680/{service}/BASIC?wsdl");

        p.put(PlatformConfigurationKey.BSS_USER_KEY.name(), "1000");
        p.put(PlatformConfigurationKey.BSS_USER_PWD.name(), "_crypt:admin123");
        p.put(PlatformConfigurationKey.BSS_AUTH_MODE.name(), "INTERNAL");
        return p;
    }
}
