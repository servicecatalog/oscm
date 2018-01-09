/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.ssopropertyimport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Qiu
 * 
 */
public class SSOPropertyImportTest {

    private SSOPropertyImport importer;
    private Connection sqlConn;
    private Statement statement;

    private String driverClass;
    private String driverURL;
    private String userName;
    private String userPwd;
    private static final String configPropertyFile = "configPropertyFile";
    private static final String ssoPropertyFile = "ssoPropertyFile";
    private static final String AUTH_MODE_INTERNAL = "INTERNAL";
    private static final String AUTH_MODE_SAML_SP = "SAML_SP";
    private static final String AUTH_MODE = "AUTH_MODE";
    private static final String ADMIN_USER_ID = "ADMIN_USER_ID";

    @Before
    public void setup() throws Exception {
        driverClass = "java.lang.String";
        importer = spy(createImport());
        sqlConn = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        doReturn(statement).when(sqlConn).createStatement();

    }

    @Test(expected = RuntimeException.class)
    public void Main_Null() throws Exception {
        SSOPropertyImport.main(null);
    }

    @Test(expected = RuntimeException.class)
    public void Main_LessArgs() throws Exception {
        SSOPropertyImport.main(new String[5]);
    }

    @Test(expected = RuntimeException.class)
    public void Main_MoreArgs() throws Exception {
        SSOPropertyImport.main(new String[7]);
    }

    @Test
    public void execute_NoImport() throws Exception {
        // given
        doReturn(AUTH_MODE_INTERNAL).when(importer).getProperty(
                eq(configPropertyFile), eq(AUTH_MODE));
        // when
        importer.execute();
        // then
        verify(importer, never()).updateUserId();

    }

    @Test
    public void execute_Import() throws Exception {
        // given
        doReturn(AUTH_MODE_SAML_SP).when(importer).getProperty(
                eq(configPropertyFile), eq(AUTH_MODE));
        doReturn(ADMIN_USER_ID).when(importer).getProperty(eq(ssoPropertyFile),
                eq(ADMIN_USER_ID));
        // when
        importer.execute();
        // then
        verify(importer, times(1)).updateUserId();

    }

    @Test(expected = java.io.FileNotFoundException.class)
    public void getProperty_noFile() throws Exception {
        // when
        importer.getProperty("not_existed_file", AUTH_MODE);
    }

    @Test
    public void getProperty() throws Exception {
        // when
        String type = importer.getProperty("javares/db.properties", "db.type");
        String host = importer.getProperty("javares/db.properties", "db.host");
        String port = importer.getProperty("javares/db.properties", "db.port");
        String name = importer.getProperty("javares/db.properties", "db.name");

        // then
        assertEquals("postgresql", type);
        assertEquals("localhost", host);
        assertEquals("5432", port);
        assertEquals("bssunittests", name);
    }

    @Test
    public void updateUserId() throws Exception {
        // given
        doReturn(ADMIN_USER_ID).when(importer).getProperty(eq(ssoPropertyFile),
                eq(ADMIN_USER_ID));
        // when
        importer.updateUserId();
        // then
        verify(statement, times(1))
                .executeUpdate(
                        eq("UPDATE platformuser SET userid = 'ADMIN_USER_ID' WHERE tkey = 1000"));
    }

    @Test(expected = RuntimeException.class)
    public void updateUserId_closeException() throws Exception {
        // given
        doThrow(new SQLException()).when(sqlConn).close();
        doReturn(ADMIN_USER_ID).when(importer).getProperty(eq(ssoPropertyFile),
                eq(ADMIN_USER_ID));
        // when
        importer.updateUserId();
    }

    private SSOPropertyImport createImport() {
        return createImport(driverClass, driverURL, userName, userPwd,
                configPropertyFile, ssoPropertyFile);
    }

    private SSOPropertyImport createImport(String driverClass,
            String driverURL, String userName, String userPwd,
            String configPropertyFile, String ssoPropertyFile) {
        return new SSOPropertyImport(driverClass, driverURL, userName, userPwd,
                configPropertyFile, ssoPropertyFile) {
            @Override
            protected Connection getConnetion() throws SQLException {
                return sqlConn;
            }
        };
    }
}
