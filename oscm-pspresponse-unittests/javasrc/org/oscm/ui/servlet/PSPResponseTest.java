/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 16.12.2009                                                      
 *                                                                              
 *  Completion Time: 18.07.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.servlet;

import static org.junit.Assert.assertTrue;
import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.ui.stub.PaymentServiceStub;
import org.oscm.ui.stubs.HttpServletRequestStub;
import org.oscm.ui.stubs.HttpServletResponseStub;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.vo.VOPaymentData;

/**
 * Tests for the PSPResponse servlet.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PSPResponseTest {

    private static final String BANKNR = "11122233";
    private static final String BANKNAME = "Test Bank";
    private static final String YEAR = "19";
    private static final String MONTH = "09";
    private static final String CARDNAME = "Test Card";
    private static final String ACCOUNT = "1234567890";

    private PSPResponse objectUnderTest;
    private HttpServletRequestStub requestStub;
    private HttpServletResponseStub responseStub;
    private PaymentServiceStub payStub;
    private ConfigurationServiceStub cfgStub;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new PSPResponse();
        requestStub = new HttpServletRequestStub();
        responseStub = new HttpServletResponseStub();
        requestStub.setUserPrincipal("psp cert dn");
        payStub = new PaymentServiceStub();
        objectUnderTest.cfg = cfgStub;
        objectUnderTest.paymentService = payStub;
    }

    @Test
    public void testDoGet() throws Exception {
        responseStub.setBufferSize(15);
        objectUnderTest.doGet(null, responseStub);
        Assert.assertEquals(
                "Method is not implemented and must return input object", 15,
                responseStub.getBufferSize());
    }

    @Test
    public void testDoPostNoWriter() throws Exception {
        responseStub.enableWriter(false);
        objectUnderTest.doPost(requestStub, responseStub);
    }

    @Test
    public void testDoPostInitializedWriterNoContent() throws Exception {
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong URL written to the response", "",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostInitializedWriterErrorForInputStreamUsage()
            throws Exception {
        requestStub.setErrorOnISUsage(true);
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong URL written to the response", "",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostInitializedWriterWrongPaymentCode() throws Exception {
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong URL written to the response", "",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostCancelledByUserNoBaseURL() throws Exception {
        requestStub.addPropertyToIS(PSPResponse.CRITERION_BES_BASE_URL,
                "baseURL");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong URL written to the response",
                "baseURL/public/pspregistrationresult.jsf?success=false",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostCancelledByUser() throws Exception {
        requestStub.addPropertyToIS(PSPResponse.CRITERION_BES_BASE_URL,
                "baseURL");
        requestStub.addPropertyToIS(PSPResponse.FRONTEND_REQUEST_CANCELLED,
                "true");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong URL written to the response",
                "baseURL/public/pspregistrationresult.jsf?success=cancelled",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostCancelledByUserWithUserLocale() throws Exception {
        requestStub.addPropertyToIS(PSPResponse.CRITERION_BES_BASE_URL,
                "baseURL");
        requestStub.addPropertyToIS(PSPResponse.FRONTEND_REQUEST_CANCELLED,
                "true");
        requestStub.addPropertyToIS(PSPResponse.CRITERION_USER_LOCALE, "de");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong URL written to the response",
                "baseURL/public/pspregistrationresult.jsf?success=cancelled&locale=de",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostWriterUsageFails() throws Exception {
        responseStub.setErrorOnWriterUsage(true);
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertNull("Writer must not be initialized",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostGoodCase() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=true&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostGoodCase_CC() throws Exception {
        setAllRequiredProperties("CC.RG", "ACK", "1");
        setAccountProperties(true, false);
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=true&locale=userLocale",
                responseStub.getWriterContent());
        VOPaymentData data = payStub.getData();
        Assert.assertEquals(CARDNAME + ' ' + MONTH + '/' + YEAR,
                data.getProvider());
        Assert.assertEquals(ACCOUNT, data.getAccountNumber());
    }

    @Test
    public void testDoPostGoodCase_DDOnlyBank() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "1");
        setAccountProperties(false, false);
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=true&locale=userLocale",
                responseStub.getWriterContent());
        VOPaymentData data = payStub.getData();
        Assert.assertEquals(BANKNR, data.getProvider());
        Assert.assertEquals(ACCOUNT, data.getAccountNumber());
    }

    @Test
    public void testDoPostGoodCase_DDBankAndName() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "1");
        setAccountProperties(false, true);
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=true&locale=userLocale",
                responseStub.getWriterContent());
        VOPaymentData data = payStub.getData();
        Assert.assertEquals(BANKNAME + " (" + BANKNR + ')', data.getProvider());
        Assert.assertEquals(ACCOUNT, data.getAccountNumber());
    }

    @Test
    public void testDoPostGoodCaseNoNumericPaymentInfoKey() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "piKey");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=false&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostGoodCaseNumericPaymentInfoKey() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "1234");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=true&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostFailureNoNumericalOrgKey() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "paymentIdKey");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=false&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostFailureOrgNotFound() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "-1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=false&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostFailureSupplierNotFound() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "WrongPaymenInfotId");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=false&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostFailure() throws Exception {
        setAllRequiredProperties("DD.RG", "NOK", "paymentInfoKey");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=false&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostVerifyPaymentTypeDD() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "100");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong payment option stored",
                PaymentInfoType.DIRECT_DEBIT, payStub.getPaymentOption());
    }

    @Test
    public void testDoPostVerifyPaymentTypeCC() throws Exception {
        setAllRequiredProperties("CC.RG", "ACK", "200");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong payment option stored",
                PaymentInfoType.CREDIT_CARD, payStub.getPaymentOption());
    }

    @Test
    public void testDoPostVerifyPaymentTypeInvalidType() throws Exception {
        setAllRequiredProperties("XY.RG", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=false&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPost_MissingPaymentKey() throws Exception {
        requestStub.ignoreProperty(PSPResponse.CRITERION_BES_PAYMENT_INFO_KEY);
        setAllRequiredProperties("CC.RG", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        assertTrue(responseStub.getWriterContent().contains("success=false"));
    }

    private void setAllRequiredProperties(String paymentCode,
            String processingResult, String paymentInfoKey) {
        requestStub.addPropertyToIS(PSPResponse.PROCESSING_RESULT,
                processingResult);
        requestStub.addPropertyToIS(PSPResponse.PROCESSING_CODE, "code");
        requestStub.addPropertyToIS(PSPResponse.PROCESSING_RETURN, "return");
        requestStub.addPropertyToIS(PSPResponse.PROCESSING_RETURN_CODE,
                "returnCode");
        requestStub.addPropertyToIS(PSPResponse.PROCESSING_REASON, "reason");
        requestStub.addPropertyToIS(PSPResponse.PROCESSING_TIMESTAMP,
                "timestamp");
        requestStub.addPropertyToIS(PSPResponse.PAYMENT_CODE, paymentCode);

        requestStub.addPropertyToIS(PSPResponse.CRITERION_BES_PAYMENT_INFO_KEY,
                paymentInfoKey);
        requestStub.addPropertyToIS(PSPResponse.CRITERION_USER_LOCALE,
                "userLocale");
        requestStub.addPropertyToIS(PSPResponse.CRITERION_BES_BASE_URL,
                "baseURL");
    }

    private void setAccountProperties(boolean creditCard,
            boolean provideBankName) {
        requestStub.addPropertyToIS(PSPResponse.ACCOUNT_NUMBER, ACCOUNT);
        if (creditCard) {
            requestStub.addPropertyToIS(PSPResponse.ACCOUNT_BRAND, CARDNAME);
            requestStub
                    .addPropertyToIS(PSPResponse.ACCOUNT_EXPIRY_MONTH, MONTH);
            requestStub.addPropertyToIS(PSPResponse.ACCOUNT_EXPIRY_YEAR, YEAR);
        } else {
            if (provideBankName) {
                requestStub.addPropertyToIS(PSPResponse.ACCOUNT_BANKNAME,
                        BANKNAME);
            }
            requestStub.addPropertyToIS(PSPResponse.ACCOUNT_BANK, BANKNR);
        }
    }

    @Test
    public void testDoPostVerifyPaymentReregistrationCC() throws Exception {
        setAllRequiredProperties("CC.RR", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=true&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostVerifyPaymentReregistrationDD() throws Exception {
        setAllRequiredProperties("DD.RR", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=true&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostVerifyPaymentReregistrationInvalidType()
            throws Exception {
        setAllRequiredProperties("XY.RR", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=false&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostReregistrationFailing() throws Exception {
        setAllRequiredProperties("CC.RR", "NOK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=false&locale=userLocale",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostReregistrationFailingObjectNotFound()
            throws Exception {
        setAllRequiredProperties("CC.RR", "ACK", "-1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "baseURL/public/pspregistrationresult.jsf?success=false&locale=userLocale",
                responseStub.getWriterContent());
    }

}
