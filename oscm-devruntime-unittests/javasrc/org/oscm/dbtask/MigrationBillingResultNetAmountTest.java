/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.dbtask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.converter.XMLConverter;
import org.oscm.stream.Streams;
import org.oscm.string.Strings;

public class MigrationBillingResultNetAmountTest {

    private MigrationBillingResultNetAmount migrationTask;
    private static final Long billingResultKey = Long.valueOf(423414);
    private static String billingResultXml;
    private static final BigDecimal NET_AMOUNT = BigDecimal.valueOf(14681);

    @BeforeClass
    public static void beforeClass() throws Exception {
        billingResultXml = readBillingResultFromFile(new File(
                "javares/BillingResult_Setup.xml"));
    }

    private static String readBillingResultFromFile(File file) throws Exception {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            String billingResult = Strings.toString(Streams.readFrom(is));
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                return billingResult;
            }
            billingResult = billingResult.replaceAll("\r\n", "\n");
            return billingResult;

        } finally {
            Streams.close(is);
        }
    }

    @Before
    public void setup() throws Exception {
        migrationTask = spy(new MigrationBillingResultNetAmount());
        mockGetRecordsByTable();
    }

    private void mockGetRecordsByTable() throws Exception {
        ResultSet resultSetMock = mock(ResultSet.class);

        when(Boolean.valueOf(resultSetMock.next())).thenReturn(Boolean.TRUE,
                Boolean.FALSE);

        doReturn(billingResultKey).when(resultSetMock).getLong(
                eq(DatabaseUpgradeTask.COLUMN_TKEY));

        doReturn(billingResultXml).when(resultSetMock).getString(
                eq(DatabaseUpgradeTask.COLUMN_RESULTXML));

        doReturn(resultSetMock).when(migrationTask).getRecordsByTable(
                DatabaseUpgradeTask.TABLE_BILLINGRESULT);
    }

    @Test
    public void execute() throws Exception {
        // given
        mockConnection();
        mockUpdateBillingResultByTkey();

        // when
        migrationTask.execute();

        // then
        verify(migrationTask, times(1)).getNetAmount(
                any(org.w3c.dom.Document.class));
        verify(migrationTask, times(1)).updateBillingResult(billingResultKey,
                NET_AMOUNT);
    }

    private void mockConnection() throws Exception {
        Connection connection = mock(Connection.class);
        migrationTask.setConnection(connection);

        PreparedStatement stmt = mock(PreparedStatement.class);
        doReturn(stmt).when(connection).prepareStatement(anyString());
    }

    private void mockUpdateBillingResultByTkey() throws Exception {
        doReturn(Integer.valueOf(1)).when(migrationTask)
                .updateBillingResultByTkey(billingResultKey, NET_AMOUNT);
    }

    @Test
    public void getNetAmount() throws Exception {
        // when
        BigDecimal netAmount = migrationTask
                .getNetAmount(XMLConverter
                        .convertToDocument(
                                "<BillingDetails><OverallCosts netAmount=\"14681\" /></BillingDetails>",
                                false));

        // then
        assertNotNull(netAmount);
        assertEquals(0, netAmount.compareTo(NET_AMOUNT));
    }
}
