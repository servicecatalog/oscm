/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: kulle                                          
 *                                                                              
 *  Creation Date: 19.10.2011                                                      
 *                                                                              
 *  Completion Time: 19.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.servlet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.paymentservice.constants.HeidelpayPostParameter;
import org.oscm.ui.stub.PaymentRegistrationServiceStub;
import org.oscm.ui.stubs.HttpServletRequestStub;
import org.oscm.ui.stubs.HttpServletResponseStub;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.psp.data.RegistrationData;
import org.oscm.psp.data.RegistrationData.Status;

/**
 * @author kulle
 * 
 */
public class HeidelpayResponseServletTest {

    private static final String BANKNR = "11122233";
    private static final String BANKNAME = "Test Bank";
    private static final String YEAR = "19";
    private static final String MONTH = "09";
    private static final String CARDNAME = "Test Card";
    private static final String ACCOUNT = "1234567890";

    private static final String BASE_URL = "baseURL";

    private HeidelpayResponseServletStub objectUnderTest;
    private HttpServletRequestStub requestStub;
    private HttpServletResponseStub responseStub;
    private PaymentRegistrationServiceStub registrationStub;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new HeidelpayResponseServletStub();
        requestStub = new HttpServletRequestStub();
        responseStub = new HttpServletResponseStub();
        requestStub.setUserPrincipal("psp cert dn");
        registrationStub = objectUnderTest.getRegistrationMock();
        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.CRITERION_BES_BASE_URL, BASE_URL);
    }

    private void setAccountProperties(boolean creditCard,
            boolean provideBankName) {
        requestStub.addPropertyToIS(HeidelpayResponseServlet.ACCOUNT_NUMBER,
                ACCOUNT);
        if (creditCard) {
            requestStub.addPropertyToIS(HeidelpayResponseServlet.ACCOUNT_BRAND,
                    CARDNAME);
            requestStub.addPropertyToIS(
                    HeidelpayResponseServlet.ACCOUNT_EXPIRY_MONTH, MONTH);
            requestStub.addPropertyToIS(
                    HeidelpayResponseServlet.ACCOUNT_EXPIRY_YEAR, YEAR);
        } else {
            if (provideBankName) {
                requestStub.addPropertyToIS(
                        HeidelpayResponseServlet.ACCOUNT_BANKNAME, BANKNAME);
            }
            requestStub.addPropertyToIS(HeidelpayResponseServlet.ACCOUNT_BANK,
                    BANKNR);
        }
    }

    private void setAllRequiredProperties(String paymentCode,
            String processingResult, String paymentInfoKey) {
        requestStub.addPropertyToIS(HeidelpayResponseServlet.PROCESSING_RESULT,
                processingResult);
        requestStub.addPropertyToIS(HeidelpayResponseServlet.PROCESSING_CODE,
                "code");
        requestStub.addPropertyToIS(HeidelpayResponseServlet.PROCESSING_RETURN,
                "return");
        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.PROCESSING_RETURN_CODE, "returnCode");
        requestStub.addPropertyToIS(HeidelpayResponseServlet.PROCESSING_REASON,
                "reason");
        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.PROCESSING_TIMESTAMP, "timestamp");
        requestStub.addPropertyToIS(HeidelpayResponseServlet.PAYMENT_CODE,
                paymentCode);

        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.CRITERION_BES_PAYMENT_INFO_KEY,
                paymentInfoKey);
        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.CRITERION_USER_LOCALE, "en");
        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.CRITERION_BES_BASE_URL, BASE_URL);

        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.CRITERION_BES_ORGANIZATION_KEY, "1");
        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.CRITERION_BES_PAYMENT_INFO_ID,
                "123qwertz");
        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.CRITERION_BES_PAYMENT_TYPE_KEY, "1");
        requestStub.addPropertyToIS(
                HeidelpayPostParameter.BES_PAYMENT_REGISTRATION_WSDL, "1");
        requestStub.addPropertyToIS(
                HeidelpayPostParameter.BES_PAYMENT_REGISTRATION_ENDPOINT, "1");
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
        Assert.assertEquals("Wrong URL written to the response", BASE_URL
                + Status.Failure, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostInitializedWriterErrorForInputStreamUsage()
            throws Exception {
        requestStub.setErrorOnISUsage(true);
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong URL written to the response", "null"
                + Status.Failure, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostInitializedWriterWrongPaymentCode() throws Exception {
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong URL written to the response", BASE_URL
                + Status.Failure, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostCancelledByUserNoBaseURL() throws Exception {
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong URL written to the response", BASE_URL
                + Status.Failure, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostCancelledByUser() throws Exception {
        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.FRONTEND_REQUEST_CANCELLED, "true");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong URL written to the response", BASE_URL
                + Status.Canceled, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostCancelledByUserWithUserLocale() throws Exception {
        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.FRONTEND_REQUEST_CANCELLED, "true");
        requestStub.addPropertyToIS(
                HeidelpayResponseServlet.CRITERION_USER_LOCALE, "de");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong URL written to the response", BASE_URL
                + Status.Canceled, responseStub.getWriterContent());
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
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Success, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostGoodCase_CC() throws Exception {
        setAllRequiredProperties("CC.RG", "ACK", "1");
        setAccountProperties(true, false);
        objectUnderTest.doPost(requestStub, responseStub);

        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Success, responseStub.getWriterContent());

        RegistrationData data = registrationStub.getRegistrationData();
        Assert.assertEquals(CARDNAME + ' ' + MONTH + '/' + YEAR,
                data.getProvider());
        Assert.assertEquals(ACCOUNT, data.getAccountNumber());
    }

    @Test
    public void testDoPostGoodCase_DDOnlyBank() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "1");
        setAccountProperties(false, false);
        objectUnderTest.doPost(requestStub, responseStub);

        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Success, responseStub.getWriterContent());
        RegistrationData data = registrationStub.getRegistrationData();
        Assert.assertEquals(BANKNR, data.getProvider());
        Assert.assertEquals(ACCOUNT, data.getAccountNumber());
    }

    @Test
    public void testDoPostGoodCase_DDBankAndName() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "1");
        setAccountProperties(false, true);
        objectUnderTest.doPost(requestStub, responseStub);

        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Success, responseStub.getWriterContent());
        RegistrationData data = registrationStub.getRegistrationData();
        Assert.assertEquals(BANKNAME + " (" + BANKNR + ')', data.getProvider());
        Assert.assertEquals(ACCOUNT, data.getAccountNumber());
    }

    @Test
    public void testDoPostGoodCaseNoNumericPaymentInfoKey() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Success, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostGoodCaseNumericPaymentInfoKey() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "1234");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Success, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostFailureNoNumericalOrgKey() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "p");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + "/public/pspregistrationresult.jsf",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostFailureOrgNotFound() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "-1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + "/public/pspregistrationresult.jsf",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostFailureSupplierNotFound() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "-1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + "/public/pspregistrationresult.jsf",
                responseStub.getWriterContent());
    }

    @Test
    public void testDoPostFailure() throws Exception {
        setAllRequiredProperties("DD.RG", "NOK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Failure, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostVerifyPaymentTypeDD() throws Exception {
        setAllRequiredProperties("DD.RG", "ACK", "100");
        objectUnderTest.doPost(requestStub, responseStub);

        Assert.assertEquals("Wrong payment option stored",
                PaymentInfoType.DIRECT_DEBIT,
                registrationStub.getPaymentOption());
    }

    @Test
    public void testDoPostVerifyPaymentTypeCC() throws Exception {
        setAllRequiredProperties("CC.RG", "ACK", "200");
        objectUnderTest.doPost(requestStub, responseStub);

        Assert.assertEquals("Wrong payment option stored",
                PaymentInfoType.CREDIT_CARD,
                registrationStub.getPaymentOption());
    }

    @Test
    public void testDoPostVerifyPaymentTypeInvalidType() throws Exception {
        setAllRequiredProperties("XY.RG", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Failure, responseStub.getWriterContent());
    }

    @Test
    public void testDoPost_MissingPaymentKey() throws Exception {
        requestStub
                .ignoreProperty(HeidelpayResponseServlet.CRITERION_BES_PAYMENT_INFO_KEY);
        setAllRequiredProperties("CC.RG", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Failure, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostVerifyPaymentReregistrationCC() throws Exception {
        setAllRequiredProperties("CC.RR", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Success, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostVerifyPaymentReregistrationDD() throws Exception {
        setAllRequiredProperties("DD.RR", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Success, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostVerifyPaymentReregistrationInvalidType()
            throws Exception {
        setAllRequiredProperties("XY.RR", "ACK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Failure, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostReregistrationFailing() throws Exception {
        setAllRequiredProperties("CC.RR", "NOK", "1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + Status.Failure, responseStub.getWriterContent());
    }

    @Test
    public void testDoPostReregistrationFailingObjectNotFound()
            throws Exception {
        setAllRequiredProperties("CC.RR", "ACK", "-1");
        objectUnderTest.doPost(requestStub, responseStub);
        Assert.assertEquals("Wrong redirect URL returned", BASE_URL
                + "/public/pspregistrationresult.jsf",
                responseStub.getWriterContent());
    }

}
