/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: kulle                                                     
 *                                                                              
 *  Creation Date: 16.12.2011                                                      
 *                                                                              
 *  Completion Time: 16.12.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.oscm.logging.Log4jLogger;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.paymentservice.constants.HeidelpayConfigurationKey;
import org.oscm.paymentservice.constants.HeidelpayPostParameter;
import org.oscm.paymentservice.constants.HeidelpayXMLTags;
import org.oscm.paymentservice.data.HeidelpayResponse;
import org.oscm.paymentservice.transport.HttpMethodFactory;
import org.oscm.payproc.stubs.PostMethodStub;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.psp.data.ChargingData;
import org.oscm.psp.data.ChargingResult;
import org.oscm.psp.data.PriceModelData;
import org.oscm.psp.data.Property;
import org.oscm.psp.data.RegistrationLink;
import org.oscm.psp.data.RequestData;
import org.oscm.types.exceptions.PSPCommunicationException;
import org.oscm.types.exceptions.PSPProcessingException;
import org.oscm.types.exceptions.PaymentDeregistrationException;

/**
 * @author kulle
 * 
 */
public class PaymentServiceProviderBeanTest {

    private static final String ROOTPATH_SCRIPTS_SCRIPT_JS = "rootpath/scripts/script.js";
    private static final String CREDIT_CARD = "CREDIT_CARD";
    private static final String DIRECT_DEBIT = "DIRECT_DEBIT";
    private static final String INVOICE = "INVOICE";

    private PaymentServiceProviderBean psp;
    private HttpClient httpClientMock;

    private final String XML_URL = "xmlURL";
    public static String sampleResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Response version=\"1.0\">"
            + "<Transaction mode=\"LIVE\" response=\"SYNC\" channel=\"678a456b789c123d456e789f012g432\">"
            + "<Identification>"
            + "<TransactionID>MerchantAssignedID</TransactionID>"
            + "<UniqueID>h987i654j321k098l765m432n210o987</UniqueID>"
            + "<ShortID>1234.5678.9876</ShortID>"
            + "</Identification>"
            + "<Processing code=\"DD.DB.90.00\">"
            + "<Timestamp>2003-02-12 14:58:07</Timestamp>"
            + "<Result>ACK</Result>"
            + "<Status code=\"90\">NEW</Status>"
            + "<Reason code=\"00\">Successful Processing</Reason>"
            + "<Return code=\"000.000.000\">Transaction succeeded</Return>"
            + "</Processing>"
            + "<Payment code=\"DD.DB\">"
            + "<Clearing>"
            + "<Amount>1.00</Amount>"
            + "<Currency>EUR</Currency>"
            + "<Descriptor>shop.de 1234.1234.1234 +49 (89) 12345 678 Order Number 1234</Descriptor>"
            + "<Date>2003-02-13</Date>"
            + "<Support>+49 (89) 1234 567</Support>"
            + "</Clearing>"
            + "</Payment>" + "</Transaction>" + "</Response>";

    @Before
    public void setup() {
        psp = new PaymentServiceProviderBean();

        HttpMethodFactory.setTestMode(true);
        PostMethodStub.reset();

        httpClientMock = mock(HttpClient.class);
        HostConfiguration hostConfigurationMock = mock(HostConfiguration.class);
        when(httpClientMock.getHostConfiguration()).thenReturn(
                hostConfigurationMock);

        psp.client = httpClientMock;

    }

    private RequestData createRequestData(String paymentTypeId) {
        RequestData requestData = new RequestData();
        requestData.setCurrentUserLocale("en");
        requestData.setExternalIdentifier("externalId");

        requestData.setOrganizationEmail("email");
        requestData.setOrganizationId("orgId");
        requestData.setOrganizationKey(Long.valueOf(1L));
        requestData.setOrganizationName("Firma Elektroinstallation Meier");

        requestData.setPaymentInfoId("payment info id");
        requestData.setPaymentInfoKey(Long.valueOf(2L));
        requestData.setPaymentTypeId(paymentTypeId);
        requestData.setPaymentTypeKey(Long.valueOf(3L));

        requestData.setPspIdentifier("pspIdentifier");

        List<Property> propertyList = new ArrayList<Property>();
        propertyList.add(new Property(
                HeidelpayConfigurationKey.BASE_URL.name(), "baseURL"));

        propertyList.add(new Property(HeidelpayConfigurationKey.PSP_XML_URL
                .name(), XML_URL));
        propertyList.add(new Property(
                HeidelpayConfigurationKey.PSP_SECURITY_SENDER.name(),
                "securitySender"));
        propertyList.add(new Property(
                HeidelpayConfigurationKey.PSP_TRANSACTION_CHANNEL.name(),
                "txnChannel"));
        propertyList.add(new Property(HeidelpayConfigurationKey.PSP_TXN_MODE
                .name(), "INTEGRATOR_TEST"));
        propertyList.add(new Property(HeidelpayConfigurationKey.PSP_USER_LOGIN
                .name(), "userLogin"));
        propertyList.add(new Property(HeidelpayConfigurationKey.PSP_USER_PWD
                .name(), "userPwd"));
        propertyList.add(new Property(
                HeidelpayConfigurationKey.PSP_RESPONSE_SERVLET_URL.name(),
                "PSP_RESPONSE_SERVLET/PSPResponse"));
        propertyList.add(new Property(
                HeidelpayConfigurationKey.PSP_SUPPORTED_CC_BRANDS.name(),
                "Visa, Master"));
        propertyList.add(new Property(
                HeidelpayConfigurationKey.PSP_SUPPORTED_DD_COUNTRIES.name(),
                "DE,AT,ES,NL"));
        propertyList.add(new Property(HeidelpayConfigurationKey.PSP_FRONTEND_JS_PATH
                .name(), ROOTPATH_SCRIPTS_SCRIPT_JS));

        requestData.setProperties(propertyList);

        return requestData;
    }

    private ChargingData createChargingData() throws Exception {
        ChargingData chargingData = new ChargingData();
        chargingData.setTransactionId(1L);
        chargingData.setAddress("");
        chargingData.setCurrency("EUR");
        chargingData.setCustomerKey(Long.valueOf(1L));
        chargingData.setEmail("email");
        chargingData.setExternalIdentifier("externalidentifier");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 1970);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 2);
        chargingData.setPeriodEndTime(c.getTime());
        chargingData.setPeriodStartTime(c.getTime());
        chargingData.setSellerKey(Long.valueOf(2L));

        chargingData.setGrossAmount(BigDecimal.valueOf(1226));
        chargingData.setNetAmount(BigDecimal.valueOf(1030));
        chargingData.setNetDiscount(BigDecimal.ZERO);
        chargingData.setVatAmount(BigDecimal.valueOf(196));
        chargingData.setVat("");

        chargingData.setSubscriptionId("sub");
        chargingData.setPon("12345");

        chargingData.getPriceModelData().add(
                new PriceModelData(1, 1259622000000L, 1262300400000L,
                        BigDecimal.valueOf(1000)));
        chargingData.getPriceModelData().add(
                new PriceModelData(2, 1259622000000L, 1262300400000L,
                        BigDecimal.valueOf(2000)));

        return chargingData;
    }

    private void validateRequestDetails(NameValuePair[] requestBodyDetails,
            String paymentOption, String organizationName, String currency,
            String amount) throws Exception {
        Assert.assertEquals(
                "Array must only contain one entry, the load parameter", 1,
                requestBodyDetails.length);
        Assert.assertEquals("Wrong attribute name, only valid name is 'load'",
                "load", requestBodyDetails[0].getName());
        String request = requestBodyDetails[0].getValue();

        Document requestDoc = XMLConverter.convertToDocument(request, true);

        Node requestVersion = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/@version");
        Assert.assertNotNull("Request version not contained in XML request",
                requestVersion);
        Assert.assertEquals("Wrong request version information", "1.0",
                requestVersion.getTextContent());

        Node securitySender = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Header/Security/@sender");
        Assert.assertNotNull("Security sender not contained in XML request",
                securitySender);

        Assert.assertEquals("Wrong security sender", "securitySender",
                securitySender.getTextContent());

        Node transactionMode = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/@mode");
        Assert.assertNotNull("Transaction mode not contained in XML request",
                transactionMode);
        Assert.assertEquals("Wrong transaction mode", "INTEGRATOR_TEST",
                transactionMode.getTextContent());

        Node transactionResponse = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/@response");
        Assert.assertNotNull(
                "Transaction response not contained in XML request",
                transactionResponse);
        Assert.assertEquals("Wrong transaction response", "SYNC",
                transactionResponse.getTextContent());

        Node transactionChannel = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/@channel");
        Assert.assertNotNull(
                "Transaction channel not contained in XML request",
                transactionChannel);
        Assert.assertEquals("Wrong transaction channel", "pspIdentifier",
                transactionChannel.getTextContent());

        Node userLogin = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/User/@login");
        Assert.assertNotNull("User login not contained in XML request",
                userLogin);
        Assert.assertEquals("Wrong user login", "userLogin",
                userLogin.getTextContent());

        Node userPwd = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/User/@pwd");
        Assert.assertNotNull("User pwd not contained in XML request", userPwd);
        Assert.assertEquals("Wrong user pwd", "userPwd",
                userPwd.getTextContent());

        Node paymentCode = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/Payment/@code");
        Assert.assertNotNull("Payment code not contained in XML request",
                paymentCode);
        Assert.assertEquals("Wrong payment code", paymentOption + ".DB",
                paymentCode.getTextContent());

        Node paymentAmount = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/Payment/Presentation/Amount/text()");
        Assert.assertNotNull("Payment amount not contained in XML request",
                paymentAmount);
        Assert.assertEquals("Wrong payment amount", amount,
                paymentAmount.getTextContent());

        Node paymentCurrency = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/Payment/Presentation/Currency/text()");
        Assert.assertNotNull("Payment currency not contained in XML request",
                paymentCurrency);
        Assert.assertEquals("Wrong payment currency", currency,
                paymentCurrency.getTextContent());

        Node accountRegistration = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/Account/@registration");
        Assert.assertNotNull(
                "Account registration not contained in XML request",
                accountRegistration);
        Assert.assertEquals("Wrong account registration", "externalId",
                accountRegistration.getTextContent());

        Node paymentUsage = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/Payment/Presentation/Usage/text()");
        Assert.assertNotNull("Payment usage not contained in XML request",
                paymentUsage);
        Assert.assertEquals("Wrong payment usage", organizationName
                + ", Id: 1, 02/01/1970 - 02/01/1970",
                paymentUsage.getTextContent());

        Node transactionId = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/Identification/TransactionID/text()");
        Assert.assertNotNull(
                "Transaction identifier not contained in XML request",
                transactionId);
        Assert.assertEquals("Wrong transaction identifier", "1",
                transactionId.getTextContent());
    }

    private void validateRequestAnalysisData(
            NameValuePair[] requestBodyDetails, String customerAddress,
            ChargingData chargingData) throws Exception {

        String request = requestBodyDetails[0].getValue();
        Document requestDoc = XMLConverter.convertToDocument(request, true);

        Node analysisNode = XMLConverter.getNodeByXPath(requestDoc,
                "/Request/Transaction/Analysis");
        Assert.assertNotNull("Analysis node not found", analysisNode);

        String address = XMLConverter
                .getNodeTextContentByXPath(
                        requestDoc,
                        "/Request/Transaction/Analysis/Criterion[@name='"
                                + HeidelpayXMLTags.XML_ANALYSIS_ADDRESS_COMPLETE
                                + "']");
        String requiredAddress = customerAddress;
        Assert.assertEquals("Wrong address", requiredAddress, address);

        String currency = XMLConverter.getNodeTextContentByXPath(requestDoc,
                "/Request/Transaction/Analysis/Criterion[@name='"
                        + HeidelpayXMLTags.XML_ANALYSIS_CURRENCY + "']");
        Assert.assertEquals("Wrong currency", chargingData.getCurrency(),
                currency);

        String totalAmount = XMLConverter.getNodeTextContentByXPath(requestDoc,
                "/Request/Transaction/Analysis/Criterion[@name='"
                        + HeidelpayXMLTags.XML_ANALYSIS_AMOUNT_TOTAL + "']");
        Assert.assertEquals("Wrong total amount", "1226", totalAmount);

        String netAmount = XMLConverter.getNodeTextContentByXPath(requestDoc,
                "/Request/Transaction/Analysis/Criterion[@name='"
                        + HeidelpayXMLTags.XML_ANALYSIS_AMOUNT_NET + "']");
        Assert.assertEquals("Wrong net amount", "1030", netAmount);

        String vatAmount = XMLConverter.getNodeTextContentByXPath(requestDoc,
                "/Request/Transaction/Analysis/Criterion[@name='"
                        + HeidelpayXMLTags.XML_ANALYSIS_AMOUNT_VAT + "']");
        Assert.assertEquals("Wrong vat amount", "196", vatAmount);

        String vat = XMLConverter.getNodeTextContentByXPath(requestDoc,
                "/Request/Transaction/Analysis/Criterion[@name='"
                        + HeidelpayXMLTags.XML_ANALYSIS_PERCENT_VAT + "']");
        Assert.assertEquals("Wrong vat percentage",
                String.valueOf(chargingData.getVat()), vat);

        assertPriceModel1(requestDoc);
        assertPriceModel2(requestDoc);
    }

    private void assertPriceModel1(Document requestDoc)
            throws XPathExpressionException {
        String sub1PosName = XMLConverter
                .getNodeTextContentByXPath(
                        requestDoc,
                        "/Request/Transaction/Analysis/Criterion[@name='"
                                + HeidelpayXMLTags.XML_ANALYSIS_POSITION_POSITIONNAME
                                        .replace(
                                                HeidelpayXMLTags.XML_ANALYSIS_NUMBER_PLACEHOLDER,
                                                "01") + "']");
        Assert.assertEquals("Wrong subscription position in request", "1",
                sub1PosName);

        String sub1Amount = XMLConverter
                .getNodeTextContentByXPath(
                        requestDoc,
                        "/Request/Transaction/Analysis/Criterion[@name='"
                                + HeidelpayXMLTags.XML_ANALYSIS_POSITION_AMOUNT
                                        .replace(
                                                HeidelpayXMLTags.XML_ANALYSIS_NUMBER_PLACEHOLDER,
                                                "01") + "']");
        Assert.assertEquals("Wrong subscription costs in request", "1000",
                sub1Amount);

        String sub1Text = XMLConverter
                .getNodeTextContentByXPath(
                        requestDoc,
                        "/Request/Transaction/Analysis/Criterion[@name='"
                                + HeidelpayXMLTags.XML_ANALYSIS_POSITION_TEXT
                                        .replace(
                                                HeidelpayXMLTags.XML_ANALYSIS_NUMBER_PLACEHOLDER,
                                                "01") + "']");
        Assert.assertEquals("Wrong subscription text in request",
                "sub/12345, 01/12/2009-31/12/2009", sub1Text);
    }

    private void assertPriceModel2(Document requestDoc)
            throws XPathExpressionException {
        String sub1PosName = XMLConverter
                .getNodeTextContentByXPath(
                        requestDoc,
                        "/Request/Transaction/Analysis/Criterion[@name='"
                                + HeidelpayXMLTags.XML_ANALYSIS_POSITION_POSITIONNAME
                                        .replace(
                                                HeidelpayXMLTags.XML_ANALYSIS_NUMBER_PLACEHOLDER,
                                                "02") + "']");
        Assert.assertEquals("Wrong subscription position in request", "2",
                sub1PosName);

        String sub1Amount = XMLConverter
                .getNodeTextContentByXPath(
                        requestDoc,
                        "/Request/Transaction/Analysis/Criterion[@name='"
                                + HeidelpayXMLTags.XML_ANALYSIS_POSITION_AMOUNT
                                        .replace(
                                                HeidelpayXMLTags.XML_ANALYSIS_NUMBER_PLACEHOLDER,
                                                "02") + "']");
        Assert.assertEquals("Wrong subscription costs in request", "2000",
                sub1Amount);

        String sub1Text = XMLConverter
                .getNodeTextContentByXPath(
                        requestDoc,
                        "/Request/Transaction/Analysis/Criterion[@name='"
                                + HeidelpayXMLTags.XML_ANALYSIS_POSITION_TEXT
                                        .replace(
                                                HeidelpayXMLTags.XML_ANALYSIS_NUMBER_PLACEHOLDER,
                                                "02") + "']");
        Assert.assertEquals("Wrong subscription text in request",
                "sub/12345, 01/12/2009-31/12/2009", sub1Text);
    }

    private void validateDeregistrationXMLResponse(PaymentInfo pi,
            NameValuePair[] requestBodyDetails, String pspPaymentTypeId)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {
        NameValuePair nameValuePair = requestBodyDetails[0];
        String value = nameValuePair.getValue();
        System.out.println(value);
        Document doc = XMLConverter.convertToDocument(value, true);
        Assert.assertEquals("1.0", XMLConverter.getNodeTextContentByXPath(doc,
                "/Request/@version"));
        Assert.assertEquals("securitySender", XMLConverter
                .getNodeTextContentByXPath(doc,
                        "/Request/Header/Security/@sender"));
        Assert.assertEquals("INTEGRATOR_TEST", XMLConverter
                .getNodeTextContentByXPath(doc, "/Request/Transaction/@mode"));
        Assert.assertEquals("txnChannel",
                XMLConverter.getNodeTextContentByXPath(doc,
                        "/Request/Transaction/@channel"));
        Assert.assertEquals("SYNC", XMLConverter.getNodeTextContentByXPath(doc,
                "/Request/Transaction/@response"));
        Assert.assertEquals("userLogin", XMLConverter
                .getNodeTextContentByXPath(doc,
                        "/Request/Transaction/User/@login"));
        Assert.assertEquals("userPwd", XMLConverter.getNodeTextContentByXPath(
                doc, "/Request/Transaction/User/@pwd"));
        Assert.assertEquals(
                String.valueOf(pi.getKey()),
                XMLConverter
                        .getNodeTextContentByXPath(doc,
                                "/Request/Transaction/Identification/TransactionID/text()"));
        Assert.assertEquals(
                "externalId",
                XMLConverter
                        .getNodeTextContentByXPath(doc,
                                "/Request/Transaction/Identification/ReferenceID/text()"));
        Assert.assertEquals(pspPaymentTypeId + ".DR", XMLConverter
                .getNodeTextContentByXPath(doc,
                        "/Request/Transaction/Payment/@code"));
    }

    private PaymentInfo createPaymentInfo(PaymentCollectionType collectionType,
            String paymentTypeId) {
        PaymentInfo pi = new PaymentInfo();
        PaymentType pt = new PaymentType();
        pt.setCollectionType(collectionType);
        pt.setPaymentTypeId(paymentTypeId);
        if (paymentTypeId.equals(PaymentInfoType.DIRECT_DEBIT.name())) {
            pt.setPaymentTypeId("DD");
        }
        if (paymentTypeId.equals(PaymentInfoType.CREDIT_CARD.name())) {
            pt.setPaymentTypeId("CC");
        }
        pi.setPaymentType(pt);
        return pi;
    }

    private void validateLinkRequestParameters(boolean ddUsed) {
        Map<String, String> requestParams = getRequestParameterMap();

        // check parameters
        assertEqualParams(HeidelpayPostParameter.REQUEST_VERSION, "1.0",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.PAYMENT_CODE, (ddUsed ? "DD"
                : "CC") + ".RG", requestParams);
        assertEqualParams(HeidelpayPostParameter.TRANSACTION_MODE,
                "INTEGRATOR_TEST", requestParams);
        assertEqualParams(HeidelpayPostParameter.NAME_FAMILY, "  ",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.NAME_GIVEN, "  ",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.ADDRESS_STREET, "-",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.ADDRESS_ZIP, "-",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.ADDRESS_CITY, "-",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.ADDRESS_COUNTRY, "DE",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.SECURITY_SENDER,
                "securitySender", requestParams);
        assertEqualParams(HeidelpayPostParameter.TRANSACTION_CHANNEL,
                "txnChannel", requestParams);
        assertEqualParams(HeidelpayPostParameter.USER_LOGIN, "userLogin",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.USER_PWD, "userPwd",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.IDENTIFICATION_TRANSACTIONID,
                "0 registration", requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_JS_SCRIPT,
                ROOTPATH_SCRIPTS_SCRIPT_JS, requestParams);

        assertEqualParams(HeidelpayPostParameter.IDENTIFICATION_REFERENCEID,
                null, requestParams);
        assertEqualParams(HeidelpayPostParameter.ACCOUNT_HOLDER, null,
                requestParams);

        assertEqualParams(HeidelpayPostParameter.USER_LOCALE, "en",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.BASE_URL, "baseURL",
                requestParams);

        assertEqualParams(HeidelpayPostParameter.FRONTEND_ENABLED, "true",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_POPUP, "false",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_MODE, "DEFAULT",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_LANGUAGE, "en",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_ONEPAGE, "true",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_REDIRECT_TIME, "0",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_RESPONSE_URL,
                "PSP_RESPONSE_SERVLET/PSPResponse", requestParams);

        assertEqualParams(
                HeidelpayPostParameter.FRONTEND_PM_DEFAULT_DISABLE_ALL, "true",
                requestParams);

        assertEqualParams(HeidelpayPostParameter.FRONTEND_PM_1_METHOD,
                ddUsed ? "DD" : "CC", requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_PM_1_ENABLED, "true",
                requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_PM_1_SUBTYPES,
                ddUsed ? "DE,AT,ES,NL" : "Visa, Master", requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_PM_2_METHOD, null,
                requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_PM_2_ENABLED, null,
                requestParams);
        assertEqualParams(HeidelpayPostParameter.FRONTEND_PM_2_SUBTYPES, null,
                requestParams);
    }

    private Map<String, String> getRequestParameterMap() {
        NameValuePair[] requestBodyDetails = PostMethodStub
                .getRequestBodyDetails();

        // put the entries in a map
        Map<String, String> requestParams = new HashMap<String, String>();

        for (NameValuePair nvp : requestBodyDetails) {
            requestParams.put(nvp.getName(), nvp.getValue());
        }
        return requestParams;
    }

    private void assertEqualParams(String paramName, String value,
            Map<String, String> requestParams) {
        if (value == null) {
            Assert.assertNull(paramName
                    + " parameter must not be set in request",
                    requestParams.get(paramName));
        } else {
            Assert.assertEquals(paramName + " parameter wrong in request",
                    value, requestParams.get(paramName));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void charge_Null() throws Exception {
        psp.charge(null, null);
    }

    @Test(expected = PSPProcessingException.class)
    public void charge_NoPSPIdentifier() throws Exception {
        // setup
        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(CREDIT_CARD);
        requestData.setPspIdentifier(null);
        PostMethodStub.setStubReturnValue(sampleResponse);

        // execute
        psp.charge(requestData, chargingData);
    }

    @Test
    public void charge_creditCard() throws Exception {
        // setup
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(CREDIT_CARD);
        PostMethodStub.setStubReturnValue(sampleResponse);

        // execute
        ChargingResult chargingResult = psp.charge(requestData, chargingData);

        // assert request xml
        NameValuePair[] requestBodyDetails = PostMethodStub
                .getRequestBodyDetails();
        validateRequestDetails(requestBodyDetails, "CC",
                "Firma Elektroinstallation Meier", "EUR", "1226.00");
        validateRequestAnalysisData(requestBodyDetails, "", chargingData);

        // assert xml response
        assertNotNull(
                "Operation passed, so the processing details must be contained in the billing result!",
                chargingResult.getProcessingResult());
        Document processingResult = XMLConverter.convertToDocument(
                chargingResult.getProcessingResult(), true);
        assertEquals(
                "Wrong result in processing result document",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response version=\"1.0\"><Transaction mode=\"LIVE\" response=\"SYNC\" channel=\"678a456b789c123d456e789f012g432\"><Identification><TransactionID>MerchantAssignedID</TransactionID><UniqueID>h987i654j321k098l765m432n210o987</UniqueID><ShortID>1234.5678.9876</ShortID></Identification><Processing code=\"DD.DB.90.00\"><Timestamp>2003-02-12 14:58:07</Timestamp><Result>ACK</Result><Status code=\"90\">NEW</Status><Reason code=\"00\">Successful Processing</Reason><Return code=\"000.000.000\">Transaction succeeded</Return></Processing><Payment code=\"DD.DB\"><Clearing><Amount>1.00</Amount><Currency>EUR</Currency><Descriptor>shop.de 1234.1234.1234 +49 (89) 12345 678 Order Number 1234</Descriptor><Date>2003-02-13</Date><Support>+49 (89) 1234 567</Support></Clearing></Payment></Transaction></Response>",
                chargingResult.getProcessingResult());
        assertEquals("Wrong result in processing result document", "ACK",
                XMLConverter.getNodeTextContentByXPath(processingResult,
                        "/Response/Transaction/Processing/Result"));
    }

    @Test
    public void charge_creditCardSpecialCharacter() throws Exception {
        // setup
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(CREDIT_CARD);
        requestData
                .setOrganizationName("å¯Œå£«é€š ã‚¨ãƒ�ãƒ–ãƒªãƒ³ã‚° ã‚½ãƒ•ãƒˆã‚¦ã‚§ã‚¢ ãƒ†ã‚¯ãƒŽãƒ­ã‚¸ãƒ¼");
        PostMethodStub.setStubReturnValue(sampleResponse);

        // execute
        ChargingResult chargingResult = psp.charge(requestData, chargingData);

        // assert request xml
        NameValuePair[] requestBodyDetails = PostMethodStub
                .getRequestBodyDetails();
        validateRequestDetails(
                requestBodyDetails,
                "CC",
                "å¯Œå£«é€š ã‚¨ãƒ�ãƒ–ãƒªãƒ³ã‚° ã‚½ãƒ•ãƒˆã‚¦ã‚§ã‚¢ ãƒ†ã‚¯ãƒŽãƒ­ã‚¸ãƒ¼",
                "EUR", "1226.00");
        validateRequestAnalysisData(requestBodyDetails, "", chargingData);

        // assert xml response
        assertNotNull(
                "Operation passed, so the processing details must be contained in the billing result!",
                chargingResult.getProcessingResult());
        Document processingResult = XMLConverter.convertToDocument(
                chargingResult.getProcessingResult(), true);
        assertEquals(
                "Wrong result in processing result document",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response version=\"1.0\"><Transaction mode=\"LIVE\" response=\"SYNC\" channel=\"678a456b789c123d456e789f012g432\"><Identification><TransactionID>MerchantAssignedID</TransactionID><UniqueID>h987i654j321k098l765m432n210o987</UniqueID><ShortID>1234.5678.9876</ShortID></Identification><Processing code=\"DD.DB.90.00\"><Timestamp>2003-02-12 14:58:07</Timestamp><Result>ACK</Result><Status code=\"90\">NEW</Status><Reason code=\"00\">Successful Processing</Reason><Return code=\"000.000.000\">Transaction succeeded</Return></Processing><Payment code=\"DD.DB\"><Clearing><Amount>1.00</Amount><Currency>EUR</Currency><Descriptor>shop.de 1234.1234.1234 +49 (89) 12345 678 Order Number 1234</Descriptor><Date>2003-02-13</Date><Support>+49 (89) 1234 567</Support></Clearing></Payment></Transaction></Response>",
                chargingResult.getProcessingResult());
        assertEquals("Wrong result in processing result document", "ACK",
                XMLConverter.getNodeTextContentByXPath(processingResult,
                        "/Response/Transaction/Processing/Result"));
    }

    @Test
    public void charge_detailedDebugLogging_Bug8712() throws Exception {
        // setup
        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(DIRECT_DEBIT);
        PostMethodStub.setStubReturnValue(sampleResponse);
        Log4jLogger oldLogger = PaymentServiceProviderBean.logger;
        Log4jLogger loggerMock = mock(Log4jLogger.class);

        // Check response is logged with debug!
        try {
            PaymentServiceProviderBean.logger = loggerMock;
            psp.charge(requestData, chargingData);
            verify(loggerMock, atLeastOnce()).logDebug(
                    Matchers.contains(sampleResponse));
            verify(loggerMock, atLeastOnce()).logDebug(
                    Matchers.contains(XML_URL));
        } finally {
            PaymentServiceProviderBean.logger = oldLogger;
        }
    }

    @Test
    public void charge_communicationFailureDetailedInfo_Bug8712()
            throws Exception {
        // setup
        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(DIRECT_DEBIT);
        PostMethodStub.setStubReturnValue(sampleResponse);
        when(
                Integer.valueOf(httpClientMock
                        .executeMethod(any(HttpMethod.class)))).thenThrow(
                new IOException());
        // Check error message provides details in case of communication failure
        try {
            psp.charge(requestData, chargingData);
            Assert.fail(PSPCommunicationException.class.getName() + " expected");
        } catch (PSPCommunicationException pspEx) {
            final String ERROR_MSG = pspEx.getMessage();
            Assert.assertTrue(ERROR_MSG.indexOf("[Details]") > 0);
            Assert.assertTrue(ERROR_MSG.indexOf(XML_URL) > 0);
            Assert.assertTrue(ERROR_MSG
                    .indexOf(HeidelpayXMLTags.XML_ATTRIBUTE_CHANNEL) > 0);
            Assert.assertTrue(ERROR_MSG
                    .indexOf(HeidelpayXMLTags.XML_ATTRIBUTE_LOGIN) > 0);
        }
    }

    @Test
    public void charge_responseNOKDetailedInfo_Bug8712() throws Exception {
        // setup
        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(DIRECT_DEBIT);
        PostMethodStub.setStubReturnValue(sampleResponse);

        HeidelpayResponse responseMock = mock(HeidelpayResponse.class);

        when(responseMock.getProcessingResult()).thenReturn(
                HeidelpayPostParameter.FAILURE_RESULT);
        PaymentServiceProviderBean pspSpy = Mockito.spy(psp);

        when(pspSpy.getHeidelPayResponse(sampleResponse)).thenReturn(
                responseMock);

        // Check error message provides details in case of communication failure
        try {
            pspSpy.charge(requestData, chargingData);
            Assert.fail(PSPProcessingException.class.getName() + " expected");
        } catch (PSPProcessingException pspEx) {
            final String ERROR_MSG = pspEx.getMessage();
            Assert.assertTrue(ERROR_MSG.indexOf("[Details]") > 0);
            Assert.assertTrue(ERROR_MSG.indexOf(XML_URL) > 0);
            Assert.assertTrue(ERROR_MSG
                    .indexOf(HeidelpayXMLTags.XML_ATTRIBUTE_CHANNEL) > 0);
            Assert.assertTrue(ERROR_MSG
                    .indexOf(HeidelpayXMLTags.XML_ATTRIBUTE_LOGIN) > 0);

            Assert.assertTrue(ERROR_MSG.indexOf("Received Response") > 0);
            Assert.assertTrue(ERROR_MSG.indexOf(sampleResponse) > 0);

        }
    }

    @Test
    public void charge_reponseFailureDetailedInfo_Bug8712() throws Exception {
        // setup
        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(DIRECT_DEBIT);
        PostMethodStub.setStubReturnValue(sampleResponse);
        PaymentServiceProviderBean pspSpy = Mockito.spy(psp);
        when(pspSpy.getHeidelPayResponse(sampleResponse)).thenThrow(
                new IOException());

        // Check error message provides details in case of communication failure
        try {
            pspSpy.charge(requestData, chargingData);
            Assert.fail(PSPCommunicationException.class.getName() + " expected");
        } catch (PSPCommunicationException pspEx) {
            final String ERROR_MSG = pspEx.getMessage();
            Assert.assertTrue(ERROR_MSG.indexOf("[Details]") > 0);
            Assert.assertTrue(ERROR_MSG.indexOf(XML_URL) > 0);
            Assert.assertTrue(ERROR_MSG
                    .indexOf(HeidelpayXMLTags.XML_ATTRIBUTE_CHANNEL) > 0);
            Assert.assertTrue(ERROR_MSG
                    .indexOf(HeidelpayXMLTags.XML_ATTRIBUTE_LOGIN) > 0);
            Assert.assertTrue(ERROR_MSG.indexOf("Received Response") > 0);
            Assert.assertTrue(ERROR_MSG.indexOf(sampleResponse) > 0);
        }
    }

    @Test
    public void charge_directDebit() throws Exception {
        // setup
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(DIRECT_DEBIT);
        PostMethodStub.setStubReturnValue(sampleResponse);

        // execute
        ChargingResult chargingResult = psp.charge(requestData, chargingData);

        // assert request xml
        NameValuePair[] requestBodyDetails = PostMethodStub
                .getRequestBodyDetails();
        validateRequestDetails(requestBodyDetails, "DD",
                "Firma Elektroinstallation Meier", "EUR", "1226.00");
        validateRequestAnalysisData(requestBodyDetails, "", chargingData);

        // assert xml response
        assertNotNull(
                "Operation passed, so the processing details must be contained in the billing result!",
                chargingResult.getProcessingResult());
        Document processingResult = XMLConverter.convertToDocument(
                chargingResult.getProcessingResult(), true);
        assertEquals(
                "Wrong result in processing result document",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response version=\"1.0\"><Transaction mode=\"LIVE\" response=\"SYNC\" channel=\"678a456b789c123d456e789f012g432\"><Identification><TransactionID>MerchantAssignedID</TransactionID><UniqueID>h987i654j321k098l765m432n210o987</UniqueID><ShortID>1234.5678.9876</ShortID></Identification><Processing code=\"DD.DB.90.00\"><Timestamp>2003-02-12 14:58:07</Timestamp><Result>ACK</Result><Status code=\"90\">NEW</Status><Reason code=\"00\">Successful Processing</Reason><Return code=\"000.000.000\">Transaction succeeded</Return></Processing><Payment code=\"DD.DB\"><Clearing><Amount>1.00</Amount><Currency>EUR</Currency><Descriptor>shop.de 1234.1234.1234 +49 (89) 12345 678 Order Number 1234</Descriptor><Date>2003-02-13</Date><Support>+49 (89) 1234 567</Support></Clearing></Payment></Transaction></Response>",
                chargingResult.getProcessingResult());
        assertEquals("Wrong result in processing result document", "ACK",
                XMLConverter.getNodeTextContentByXPath(processingResult,
                        "/Response/Transaction/Processing/Result"));
    }

    @Test(expected = PSPProcessingException.class)
    public void charge_invoice() throws Exception {
        // setup
        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(INVOICE);
        PostMethodStub.setStubReturnValue(sampleResponse);

        // execute
        ChargingResult chargingResult = psp.charge(requestData, chargingData);

        // assert request xml
        assertNotNull(chargingResult);
    }

    @Test(expected = PSPCommunicationException.class)
    public void charge_HttpException() throws Exception {
        // setup
        when(
                Integer.valueOf(httpClientMock
                        .executeMethod(any(HttpMethod.class)))).thenThrow(
                new HttpException());
        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(CREDIT_CARD);
        PostMethodStub.setStubReturnValue(sampleResponse);

        // execute
        psp.charge(requestData, chargingData);
    }

    @Test(expected = PSPCommunicationException.class)
    public void charge_IOException() throws Exception {
        // setup
        when(
                Integer.valueOf(httpClientMock
                        .executeMethod(any(HttpMethod.class)))).thenThrow(
                new IOException());
        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(CREDIT_CARD);
        PostMethodStub.setStubReturnValue(sampleResponse);

        // execute
        psp.charge(requestData, chargingData);
    }

    @Test(expected = PSPProcessingException.class)
    public void charge_Exception() throws Exception {
        // setup
        when(
                Integer.valueOf(httpClientMock
                        .executeMethod(any(HttpMethod.class)))).thenThrow(
                new RuntimeException());
        ChargingData chargingData = createChargingData();
        RequestData requestData = createRequestData(CREDIT_CARD);
        PostMethodStub.setStubReturnValue(sampleResponse);

        // execute
        psp.charge(requestData, chargingData);
    }

    @Test
    public void deregisterPaymentInformation_CreditCard()
            throws PaymentDeregistrationException, XPathExpressionException,
            ParserConfigurationException, SAXException, IOException {

        // setup
        RequestData requestData = createRequestData(CREDIT_CARD);
        PostMethodStub.setStubReturnValue(sampleResponse);
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        pi.setKey(2L);

        // execute
        psp.deregisterPaymentInformation(requestData);

        // assert
        NameValuePair[] requestBodyDetails = PostMethodStub
                .getRequestBodyDetails();
        Assert.assertNotNull("A request must have been generated",
                requestBodyDetails);
        Assert.assertEquals("Wrong number of parameters for the request", 1,
                requestBodyDetails.length);
        validateDeregistrationXMLResponse(pi, requestBodyDetails, "CC");
    }

    @Test
    public void deregisterPaymentInformation_DirectDebit()
            throws PaymentDeregistrationException, XPathExpressionException,
            ParserConfigurationException, SAXException, IOException {

        // setup
        RequestData requestData = createRequestData(DIRECT_DEBIT);
        PostMethodStub.setStubReturnValue(sampleResponse);
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        pi.setKey(2L);

        // execute
        psp.deregisterPaymentInformation(requestData);

        // assert
        NameValuePair[] requestBodyDetails = PostMethodStub
                .getRequestBodyDetails();
        Assert.assertNotNull("A request must have been generated",
                requestBodyDetails);
        Assert.assertEquals("Wrong number of parameters for the request", 1,
                requestBodyDetails.length);
        validateDeregistrationXMLResponse(pi, requestBodyDetails, "DD");
    }

    @Test(expected = PaymentDeregistrationException.class)
    public void deregisterPaymentInformation_Negative() throws Exception {
        // setup
        RequestData requestData = createRequestData(DIRECT_DEBIT);
        PostMethodStub.setStubReturnValue(sampleResponse.replace("ACK", "NOK"));

        // execute
        psp.deregisterPaymentInformation(requestData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deregisterPaymentInformation_Null() throws Exception {
        psp.deregisterPaymentInformation(null);
    }

    @Test
    public void determineRegistrationLink_CreditCard() throws Exception {
        // setup
        RequestData requestData = createRequestData(CREDIT_CARD);
        requestData.setOrganizationKey(Long.valueOf(0));
        PostMethodStub
                .setStubReturnValue("POST.VALIDATION=ACK&FRONTEND.REDIRECT_URL=https%3A%2F%2Ftest.ctpe.net%2Ffrontend%2FstartFrontend.prc%3Bjsessionid%3D811C209B1A370839FB118EF1EDE5B050.sapp01&P3.VALIDATION=ACK");

        // execute
        psp.determineRegistrationLink(requestData);

        // assert
        validateLinkRequestParameters(false);
    }

    @Test
    public void determineRegistrationLink_DirectDebit() throws Exception {
        // SETUP
        RequestData requestData = createRequestData(DIRECT_DEBIT);
        requestData.setOrganizationKey(Long.valueOf(0));
        PostMethodStub
                .setStubReturnValue("POST.VALIDATION=ACK&FRONTEND.REDIRECT_URL=https%3A%2F%2Ftest.ctpe.net%2Ffrontend%2FstartFrontend.prc%3Bjsessionid%3D811C209B1A370839FB118EF1EDE5B050.sapp01&P3.VALIDATION=ACK");

        // EXECUTE
        RegistrationLink registrationLink = psp
                .determineRegistrationLink(requestData);

        // ASSERT
        Assert.assertEquals(
                "Wrong redirect URL returned",
                "https://test.ctpe.net/frontend/startFrontend.prc;jsessionid=811C209B1A370839FB118EF1EDE5B050.sapp01",
                registrationLink.getUrl());

        // verify requested operation, expected type is DD.RG
        validateLinkRequestParameters(true);
    }

    @Test(expected = PSPCommunicationException.class)
    public void determineRegistrationLink_IOException() throws Exception {
        // SETUP
        when(
                Integer.valueOf(httpClientMock
                        .executeMethod(any(HttpMethod.class)))).thenThrow(
                new IOException());
        RequestData requestData = createRequestData(DIRECT_DEBIT);

        // EXECUTE
        psp.determineRegistrationLink(requestData);
    }

    @Test(expected = PSPCommunicationException.class)
    public void determineRegistrationLink_HttpException() throws Exception {
        // SETUP
        when(
                Integer.valueOf(httpClientMock
                        .executeMethod(any(HttpMethod.class)))).thenThrow(
                new HttpException());
        RequestData requestData = createRequestData(DIRECT_DEBIT);

        // EXECUTE
        psp.determineRegistrationLink(requestData);
    }

    @Test(expected = PSPCommunicationException.class)
    public void determineRegistrationLink_Exception() throws Exception {
        // SETUP
        when(
                Integer.valueOf(httpClientMock
                        .executeMethod(any(HttpMethod.class)))).thenThrow(
                new RuntimeException());
        RequestData requestData = createRequestData(DIRECT_DEBIT);

        // EXECUTE
        psp.determineRegistrationLink(requestData);
    }

    @Test(expected = PSPCommunicationException.class)
    public void determineRegistrationLink_InvalidResponse() throws Exception {
        // SETUP
        RequestData requestData = createRequestData(DIRECT_DEBIT);
        requestData.setOrganizationKey(Long.valueOf(0));
        PostMethodStub.setStubReturnValue("bla");

        // EXECUTE
        psp.determineRegistrationLink(requestData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void determineRegistrationLink_Invoice() throws Exception {
        // SETUP
        RequestData requestData = createRequestData(INVOICE);
        requestData.setOrganizationKey(Long.valueOf(0));

        // EXECUTE
        psp.determineRegistrationLink(requestData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void determineRegistrationLink_Null() throws Exception {
        psp.determineRegistrationLink(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void determineReRegistrationLink_Null() throws Exception {
        psp.determineReregistrationLink(null);
    }

}
