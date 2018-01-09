/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Jul 21, 2011                                                      
 *                                                                              
 *  Completion Time: Jul 21, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

/**
 * @author tokoda
 *
 */
public class DatabaseUpgradeTaskTest {

    String identifier;
    String stubClassName;
    DatabaseUpgradeTaskStub task;
    Connection connectionMock;

    @Before
    public void setUp() throws Exception {
        
        identifier = DatabaseUpgradeTask.COMMAND_IDENTIFIER;
        stubClassName = "DatabaseUpgradeTaskStub";

        // mock object for DB access
        ResultSet configSettingResultSet = mock(ResultSet.class);
        when(Boolean.valueOf(configSettingResultSet.next())).thenReturn(
                Boolean.TRUE);
        when(configSettingResultSet.getString("env_value")).thenReturn(
                "25");

        Statement statementMock = mock(Statement.class);
        when(
                statementMock
                        .executeQuery("SELECT env_value FROM configurationsetting WHERE information_id='MAIL_PORT';"))
                .thenReturn(configSettingResultSet);
        when(statementMock.executeQuery("SELECT * FROM configurationsetting;"))
                .thenReturn(configSettingResultSet);
        when(
                Integer.valueOf(statementMock
                        .executeUpdate("UPDATE configurationsetting SET env_value='26' WHERE tkey='2';")))
                .thenReturn(Integer.valueOf(1));

        connectionMock = mock(Connection.class);
        when(connectionMock.createStatement()).thenReturn(statementMock);

        task = new DatabaseUpgradeTaskStub();
        task.setConnection(connectionMock);

    }

    @Test
    public void testInvoke() throws Exception {
        DatabaseUpgradeTask.invoke(createCorrectCommand(), connectionMock);
    }

    @Test
    public void testIsExecutableSuccess() throws Exception {
        String command = "   " + createCorrectCommand() + "    ";
        assertTrue(DatabaseUpgradeTask.isExecutableCommand(command));
    }

    @Test
    public void testIsExecutableWrongClass() throws Exception {
        String command = identifier + "Wrong";
        assertFalse(DatabaseUpgradeTask.isExecutableCommand(command));
    }

    @Test
    public void testIsExecutableWrongIdentifier() throws Exception {
        String command = "wrong:" + stubClassName;
        assertFalse(DatabaseUpgradeTask.isExecutableCommand(command));
    }

    @Test
    public void testIsExecutableWrongExtraBeforeCommand() throws Exception {
        String command = "-" + createCorrectCommand();
        assertFalse(DatabaseUpgradeTask.isExecutableCommand(command));
    }

    @Test
    public void testIsExecutableWrongExtraBackCommand() throws Exception {
        String command = createCorrectCommand() + ";";
        assertFalse(DatabaseUpgradeTask.isExecutableCommand(command));
    }

    @Test
    public void testGetRecordsByTable() throws Exception {
        ResultSet result = task.getRecordsByTable("configurationsetting");
        assertTrue(result.next());
        assertEquals("25", result.getString("env_value"));
    }

    @Test
    public void testGetConfigSettingValue() throws Exception {
        String value = task.getConfigSettingValue("MAIL_PORT");
        assertEquals("25", value);
    }

    private String createCorrectCommand() {
        return identifier + stubClassName;
    }
}
