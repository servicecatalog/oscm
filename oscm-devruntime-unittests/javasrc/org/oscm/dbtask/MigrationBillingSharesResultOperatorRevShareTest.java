/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 13.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.xml.sax.SAXException;

import org.oscm.stream.Streams;
import org.oscm.string.Strings;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;

/**
 * @author baumann
 */
public class MigrationBillingSharesResultOperatorRevShareTest {

    private static final String BILLING_SHARES_RESULT_SUPPLIER_TKEY = "4711";
    private static final String BILLING_SHARES_RESULT_DUMMY = "<SupplierRevenueShareResult/>";
    private static String BILLING_SHARES_RESULT_SETUP;
    private static String BILLING_SHARES_RESULT_EXPECTED;
    private MigrationBillingSharesResultOperatorRevShare migrationTask;

    @BeforeClass
    public static void beforeClass() throws Exception {
        BILLING_SHARES_RESULT_SETUP = readBillingSharesResultFromFile(new File(
                "javares/BillingSharesResult_withoutOpRevShare.xml"));
        BILLING_SHARES_RESULT_EXPECTED = readBillingSharesResultFromFile(new File(
                "javares/BillingSharesResult_withOpRevShare.xml"));
    }

    @Before
    public void setUp() throws Exception {
        migrationTask = spy(new MigrationBillingSharesResultOperatorRevShare());
    }

    private static String readBillingSharesResultFromFile(File testFile)
            throws FileNotFoundException, InterruptedException, IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(testFile);
            String billingSharesResult = Strings.toString(Streams.readFrom(is));
            return billingSharesResult.replaceAll("\r\n", "\n");
        } finally {
            if (is != null) {
                Streams.close(is);
            }
        }

    }

    @Test(expected = SAXException.class)
    public void migrateBillingSharesResultXml_invalidXml() throws Exception {
        // when, then
        migrationTask.migrateBillingSharesResultXml("invalid xml file");
    }

    @Test
    public void execute() throws Exception {
        // given
        mockGetRecordsByTable();

        // when
        migrationTask.execute();

        // then
        ArgumentCaptor<String> tKeyArg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> migratedXmlArg = ArgumentCaptor
                .forClass(String.class);
        verify(migrationTask).updateBillingSharesResultTable(tKeyArg.capture(),
                migratedXmlArg.capture());

        assertEquals("Wrong billing shares result key",
                BILLING_SHARES_RESULT_SUPPLIER_TKEY, tKeyArg.getValue());
        assertEquals(BILLING_SHARES_RESULT_EXPECTED.replaceAll("[ ,\n]", ""),
                migratedXmlArg.getValue().replaceAll("[ ,\n,\r]", ""));
    }

    private void mockGetRecordsByTable() throws Exception {
        ResultSet resultSetMock = mock(ResultSet.class);

        when(Boolean.valueOf(resultSetMock.next())).thenReturn(Boolean.TRUE,
                Boolean.TRUE, Boolean.FALSE);

        when(resultSetMock.getString(DatabaseUpgradeTask.COLUMN_RESULTTYPE))
                .thenReturn(BillingSharesResultType.BROKER.name(),
                        BillingSharesResultType.SUPPLIER.name());

        when(resultSetMock.getString(DatabaseUpgradeTask.COLUMN_RESULTXML))
                .thenReturn(BILLING_SHARES_RESULT_DUMMY,
                        BILLING_SHARES_RESULT_SETUP);

        when(resultSetMock.getString(DatabaseUpgradeTask.COLUMN_TKEY))
                .thenReturn(BILLING_SHARES_RESULT_SUPPLIER_TKEY);

        doReturn(resultSetMock).when(migrationTask).getRecordsByTable(
                DatabaseUpgradeTask.TABLE_BILLINGSHARESRESULT);

        doNothing().when(migrationTask).updateBillingSharesResultTable(
                anyString(), anyString());
    }
}
