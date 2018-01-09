/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Jul 22, 2011                                                      
 *                                                                              
 *  Completion Time: Jul 22, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.oscm.stream.Streams;
import org.oscm.string.Strings;

/**
 * @author tokoda
 * 
 */
public class MigrationBillingResultDecimalPlaceTest {

    private final String TKEY_BILLING_RESULT1 = "10001";
    private final String TKEY_BILLING_RESULT2 = "10002";

    private static String billingResultSetup;
    private static String billingResultExpectedDecimal2;
    private static String billingResultExpectedDecimal3;
    private static String billingResultExpectedDecimal4;
    private static String billingResultExpectedDecimal5;
    private static String billingResultExpectedDecimal6;

    private static String billingResult2Setup;
    private static String billingResult2ExpectedDecimal2;
    private static String billingResult2ExpectedDecimal3;
    private static String billingResult2ExpectedDecimal4;
    private static String billingResult2ExpectedDecimal5;
    private static String billingResult2ExpectedDecimal6;

    private static String billingResultXml;

    private PreparedStatement preparedStatement2;
    private MigrationBillingResultDecimalPlace migrationTaskMockDecimalSetting2;
    private PreparedStatement preparedStatement3;
    private MigrationBillingResultDecimalPlace migrationTaskMockDecimalSetting3;
    private PreparedStatement preparedStatement4;
    private MigrationBillingResultDecimalPlace migrationTaskMockDecimalSetting4;
    private PreparedStatement preparedStatement5;
    private MigrationBillingResultDecimalPlace migrationTaskMockDecimalSetting5;
    private PreparedStatement preparedStatement6;
    private MigrationBillingResultDecimalPlace migrationTaskMockDecimalSetting6;

    private PreparedStatement preparedStatement1;
    private MigrationBillingResultDecimalPlace migrationTaskMockDecimalSetting1;
    private PreparedStatement preparedStatement7;
    private MigrationBillingResultDecimalPlace migrationTaskMockDecimalSetting7;
    private PreparedStatement preparedStatementNotIntDecimalSetting;
    private MigrationBillingResultDecimalPlace migrationTaskMockNotIntDecimalSetting;
    private PreparedStatement preparedStatementNoDecimalSetting;
    private MigrationBillingResultDecimalPlace migrationTaskMockNoDecimalSetting;

    private MigrationBillingResultDecimalPlace migrationTaskMockEmptyXml;
    private MigrationBillingResultDecimalPlace migrationTaskMockWrongXml;

    MigrationBillingResultDecimalPlace migrationTaskMockNoBillingResultData;

    @BeforeClass
    public static void beforeClass() throws Exception {

        billingResultSetup = readBillingResultFromFile(new File(
                "javares/BillingResult_Setup.xml"));
        billingResultExpectedDecimal2 = readBillingResultFromFile(new File(
                "javares/BillingResult_ExpectedDecimal2.xml"));
        billingResultExpectedDecimal3 = readBillingResultFromFile(new File(
                "javares/BillingResult_ExpectedDecimal3.xml"));
        billingResultExpectedDecimal4 = readBillingResultFromFile(new File(
                "javares/BillingResult_ExpectedDecimal4.xml"));
        billingResultExpectedDecimal5 = readBillingResultFromFile(new File(
                "javares/BillingResult_ExpectedDecimal5.xml"));
        billingResultExpectedDecimal6 = readBillingResultFromFile(new File(
                "javares/BillingResult_ExpectedDecimal6.xml"));

        billingResult2Setup = readBillingResultFromFile(new File(
                "javares/BillingResult2_Setup.xml"));
        billingResult2ExpectedDecimal2 = readBillingResultFromFile(new File(
                "javares/BillingResult2_ExpectedDecimal2.xml"));
        billingResult2ExpectedDecimal3 = readBillingResultFromFile(new File(
                "javares/BillingResult2_ExpectedDecimal3.xml"));
        billingResult2ExpectedDecimal4 = readBillingResultFromFile(new File(
                "javares/BillingResult2_ExpectedDecimal4.xml"));
        billingResult2ExpectedDecimal5 = readBillingResultFromFile(new File(
                "javares/BillingResult2_ExpectedDecimal5.xml"));
        billingResult2ExpectedDecimal6 = readBillingResultFromFile(new File(
                "javares/BillingResult2_ExpectedDecimal6.xml"));

        billingResultXml = "wrong";
    }

    @Before
    public void setUp() throws Exception {

        preparedStatement2 = createPreparedStatementMock();
        migrationTaskMockDecimalSetting2 = createMock("2", preparedStatement2);
        preparedStatement3 = createPreparedStatementMock();
        migrationTaskMockDecimalSetting3 = createMock("3", preparedStatement3);
        preparedStatement4 = createPreparedStatementMock();
        migrationTaskMockDecimalSetting4 = createMock("4", preparedStatement4);
        preparedStatement5 = createPreparedStatementMock();
        migrationTaskMockDecimalSetting5 = createMock("5", preparedStatement5);
        preparedStatement6 = createPreparedStatementMock();
        migrationTaskMockDecimalSetting6 = createMock("6", preparedStatement6);

        preparedStatement1 = createPreparedStatementMock();
        migrationTaskMockDecimalSetting1 = createMock("1", preparedStatement1);
        preparedStatement7 = createPreparedStatementMock();
        migrationTaskMockDecimalSetting7 = createMock("7", preparedStatement7);
        preparedStatementNotIntDecimalSetting = createPreparedStatementMock();
        migrationTaskMockNotIntDecimalSetting = createMock("a",
                preparedStatementNotIntDecimalSetting);
        preparedStatementNoDecimalSetting = createPreparedStatementMock();
        migrationTaskMockNoDecimalSetting = createMock(null,
                preparedStatementNoDecimalSetting);

        migrationTaskMockEmptyXml = createAbnormalXmlMock("");

        migrationTaskMockWrongXml = createAbnormalXmlMock(billingResultXml);

        migrationTaskMockNoBillingResultData = createNodataMock();
    }

    private MigrationBillingResultDecimalPlace createMock(
            String decimalSetting, PreparedStatement pstmt) throws Exception {

        // mock object for read DB
        ResultSet billingResultMock = mock(ResultSet.class);
        when(Boolean.valueOf(billingResultMock.next())).thenReturn(
                Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        when(billingResultMock.getString("tkey")).thenReturn(
                TKEY_BILLING_RESULT1, TKEY_BILLING_RESULT2);
        when(billingResultMock.getString("resultxml")).thenReturn(
                billingResultSetup, billingResult2Setup);

        Connection connectionMock = mock(Connection.class);
        when(connectionMock.prepareStatement(anyString())).thenReturn(pstmt);

        // stub methods of the Super Class
        MigrationBillingResultDecimalPlace task = new MigrationBillingResultDecimalPlace();
        task.setConnection(connectionMock);

        MigrationBillingResultDecimalPlace taskSpy = spy(task);
        doReturn(decimalSetting).when(taskSpy).getConfigSettingValue(
                "DECIMAL_PLACES");
        doReturn(billingResultMock).when(taskSpy).getRecordsByTable(
                "billingresult");

        return taskSpy;
    }

    private PreparedStatement createPreparedStatementMock() throws Exception {
        // mock objects for update DB
        PreparedStatement pStatement = mock(PreparedStatement.class);
        when(Integer.valueOf(pStatement.executeUpdate())).thenReturn(
                Integer.valueOf(1));
        return pStatement;
    }

    private MigrationBillingResultDecimalPlace createNodataMock()
            throws Exception {

        // mock object for read DB
        ResultSet noDataResultMock = mock(ResultSet.class);
        when(Boolean.valueOf(noDataResultMock.next()))
                .thenReturn(Boolean.FALSE);

        // stub methods of the Super Class
        MigrationBillingResultDecimalPlace task = new MigrationBillingResultDecimalPlace();
        MigrationBillingResultDecimalPlace taskSpy = spy(task);

        doReturn("2").when(taskSpy).getConfigSettingValue("DECIMAL_PLACES");
        doReturn(noDataResultMock).when(taskSpy).getRecordsByTable(
                "billingresult");

        return taskSpy;
    }

    private MigrationBillingResultDecimalPlace createAbnormalXmlMock(
            String inputXml) throws Exception {

        // mock object for read DB
        ResultSet billingResultMock = mock(ResultSet.class);
        when(Boolean.valueOf(billingResultMock.next())).thenReturn(
                Boolean.TRUE, Boolean.FALSE);
        when(billingResultMock.getString("tkey")).thenReturn(
                TKEY_BILLING_RESULT1);
        when(billingResultMock.getString("resultxml")).thenReturn(inputXml);

        // stub methods of the Super Class
        MigrationBillingResultDecimalPlace task = new MigrationBillingResultDecimalPlace();
        MigrationBillingResultDecimalPlace taskSpy = spy(task);

        doReturn("2").when(taskSpy).getConfigSettingValue("DECIMAL_PLACES");
        doReturn(billingResultMock).when(taskSpy).getRecordsByTable(
                "billingresult");

        return taskSpy;
    }

    private void verifyMethodCall(MigrationBillingResultDecimalPlace task,
            PreparedStatement pstmt, String resultXml1, String resultXml2)
            throws Exception {
        verify(task, atLeastOnce()).getConfigSettingValue("DECIMAL_PLACES");
        verify(task, atLeastOnce()).getRecordsByTable("billingresult");
        verify(pstmt, atLeastOnce()).setString(1, resultXml1);
        verify(pstmt, atLeastOnce()).setString(1, resultXml2);
    }

    @Test
    public void testExecuteDecimalSetting2() throws Exception {
        migrationTaskMockDecimalSetting2.execute();
        verifyMethodCall(migrationTaskMockDecimalSetting2, preparedStatement2,
                billingResultExpectedDecimal2, billingResult2ExpectedDecimal2);
    }

    @Test
    public void testExecuteDecimalSetting3() throws Exception {
        migrationTaskMockDecimalSetting3.execute();
        verifyMethodCall(migrationTaskMockDecimalSetting3, preparedStatement3,
                billingResultExpectedDecimal3, billingResult2ExpectedDecimal3);
    }

    @Test
    public void testExecuteDecimalSetting4() throws Exception {
        migrationTaskMockDecimalSetting4.execute();
        verifyMethodCall(migrationTaskMockDecimalSetting4, preparedStatement4,
                billingResultExpectedDecimal4, billingResult2ExpectedDecimal4);
    }

    @Test
    public void testExecuteDecimalSetting5() throws Exception {
        migrationTaskMockDecimalSetting5.execute();
        verifyMethodCall(migrationTaskMockDecimalSetting5, preparedStatement5,
                billingResultExpectedDecimal5, billingResult2ExpectedDecimal5);
    }

    @Test
    public void testExecuteDecimalSetting6() throws Exception {
        migrationTaskMockDecimalSetting6.execute();
        verifyMethodCall(migrationTaskMockDecimalSetting6, preparedStatement6,
                billingResultExpectedDecimal6, billingResult2ExpectedDecimal6);
    }

    @Test
    public void testExecuteDecimalSettingUnder() throws Exception {
        migrationTaskMockDecimalSetting1.execute();
        verifyMethodCall(migrationTaskMockDecimalSetting1, preparedStatement1,
                billingResultExpectedDecimal2, billingResult2ExpectedDecimal2);
    }

    @Test
    public void testExecuteDecimalSettingOver() throws Exception {
        migrationTaskMockDecimalSetting7.execute();
        verifyMethodCall(migrationTaskMockDecimalSetting7, preparedStatement7,
                billingResultExpectedDecimal2, billingResult2ExpectedDecimal2);
    }

    @Test
    public void testExecuteNotIntDecimalSetting() throws Exception {
        migrationTaskMockNotIntDecimalSetting.execute();
        verifyMethodCall(migrationTaskMockNotIntDecimalSetting,
                preparedStatementNotIntDecimalSetting,
                billingResultExpectedDecimal2, billingResult2ExpectedDecimal2);
    }

    @Test
    public void testExecuteNoDecimalSetting() throws Exception {
        migrationTaskMockNoDecimalSetting.execute();
        verifyMethodCall(migrationTaskMockNoDecimalSetting,
                preparedStatementNoDecimalSetting,
                billingResultExpectedDecimal2, billingResult2ExpectedDecimal2);
    }

    @Test
    public void testExecuteNoBillingResultData() throws Exception {
        migrationTaskMockNoBillingResultData.execute();
        verify(migrationTaskMockNoBillingResultData, atLeastOnce())
                .getConfigSettingValue("DECIMAL_PLACES");
        verify(migrationTaskMockNoBillingResultData, atLeastOnce())
                .getRecordsByTable("billingresult");
    }

    @Test
    public void testExecuteEmptyXml() throws Exception {
        migrationTaskMockEmptyXml.execute();
        verify(migrationTaskMockEmptyXml, atLeastOnce()).getConfigSettingValue(
                "DECIMAL_PLACES");
        verify(migrationTaskMockEmptyXml, atLeastOnce()).getRecordsByTable(
                "billingresult");
    }

    @Test(expected = SAXException.class)
    public void testExecuteWrongXml() throws Exception {
        migrationTaskMockWrongXml.execute();
    }

    private static String readBillingResultFromFile(File testFile)
            throws FileNotFoundException, InterruptedException, IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(testFile);
            String billingResult = Strings.toString(Streams.readFrom(is));
            billingResult = billingResult.substring(billingResult
                    .indexOf("<BillingDetails>"));
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                return billingResult;
            }
            billingResult = billingResult.replaceAll("\r\n", "\n");
            return billingResult;

        } finally {
            Streams.close(is);
        }

    }
}
