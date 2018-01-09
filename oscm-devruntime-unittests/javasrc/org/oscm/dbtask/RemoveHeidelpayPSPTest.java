/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-4-18                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Qiu
 * 
 */
public class RemoveHeidelpayPSPTest {

    private RemoveHeidelpayPSP removeTask;
    private Connection connection;
    private Statement stmt;

    @Before
    public void setup() throws Exception {
        removeTask = spy(new RemoveHeidelpayPSP());
        mockConnection();

    }

    private void mockConnection() throws Exception {
        connection = mock(Connection.class);
        removeTask.setConnection(connection);
        stmt = mock(Statement.class);
        doReturn(stmt).when(connection).createStatement();
        mockResultSet(null, false);
    }

    private void mockResultSet(String sql, boolean isNext) throws SQLException {
        ResultSet resultSetMock = mock(ResultSet.class);
        if (sql == null) {
            doReturn(resultSetMock).when(stmt).executeQuery(anyString());
        } else {
            doReturn(resultSetMock).when(stmt).executeQuery(sql);
        }
        doReturn(Boolean.valueOf(isNext)).when(resultSetMock).next();
    }

    @Test
    public void execute_NoBlankWsdl() throws Exception {
        // given
        mockResultSet(RemoveHeidelpayPSP.QUERY_PSP_HEIDELPAYWITHBLANKWSDL,
                false);
        // when
        removeTask.execute();
        // then
        verify(stmt, never()).executeUpdate(anyString());
    }

    @Test
    public void execute_() throws Exception {
        // given
        mockResultSet(RemoveHeidelpayPSP.QUERY_PSP_HEIDELPAYWITHBLANKWSDL, true);
        // when
        removeTask.execute();
        // then
        verify(stmt, atLeastOnce()).executeUpdate(anyString());
    }

    @Test
    public void execute_HasPSPAccountRecord() throws Exception {
        // given
        mockResultSet(RemoveHeidelpayPSP.QUERY_PSPACCOUNT, true);
        // when
        removeTask.execute();
        // then
        verify(stmt, never()).executeUpdate(anyString());
    }

    @Test
    public void execute_HasPSPAccountHistoryRecord() throws Exception {
        // given
        mockResultSet(RemoveHeidelpayPSP.QUERY_PSPACCOUNTHISTORY, true);
        // when
        removeTask.execute();
        // then
        verify(stmt, never()).executeUpdate(anyString());
    }

    @Test
    public void execute_HasPaymentInfoRecord() throws Exception {
        // given
        mockResultSet(RemoveHeidelpayPSP.QUERY_PAYMENTINFO_PAYMENTTYPE, true);
        // when
        removeTask.execute();
        // then
        verify(stmt, never()).executeUpdate(anyString());
    }

    @Test
    public void execute_HasPaymentInfoHistoryRecord() throws Exception {
        // given
        mockResultSet(RemoveHeidelpayPSP.QUERY_PAYMENTINFOHISTORY_PAYMENTTYPE,
                true);
        // when
        removeTask.execute();
        // then
        verify(stmt, never()).executeUpdate(anyString());
    }
}
