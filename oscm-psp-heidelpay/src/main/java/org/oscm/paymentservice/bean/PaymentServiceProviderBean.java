/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.paymentservice.bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.jws.WebService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.paymentservice.constants.HeidelpayConfigurationKey;
import org.oscm.paymentservice.constants.HeidelpayPostParameter;
import org.oscm.paymentservice.constants.HeidelpayXMLTags;
import org.oscm.paymentservice.data.HeidelpayResponse;
import org.oscm.paymentservice.transport.HttpClientFactory;
import org.oscm.paymentservice.transport.HttpMethodFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.psp.data.ChargingData;
import org.oscm.psp.data.ChargingResult;
import org.oscm.psp.data.PaymentProcessingStatus;
import org.oscm.psp.data.PriceModelData;
import org.oscm.psp.data.RegistrationLink;
import org.oscm.psp.data.RequestData;
import org.oscm.psp.intf.PaymentServiceProvider;
import org.oscm.types.exceptions.PSPCommunicationException;
import org.oscm.types.exceptions.PSPCommunicationException.Reason;
import org.oscm.types.exceptions.PSPIdentifierForSellerException;
import org.oscm.types.exceptions.PSPProcessingException;
import org.oscm.types.exceptions.PaymentDeregistrationException;
import org.oscm.types.exceptions.SaaSSystemException;

/**
 * Session Bean implementation class PaymentProcessServices
 */
@WebService(serviceName = "PaymentServiceProvider", targetNamespace = "http://oscm.org/xsd", portName = "PaymentServiceProviderPort", endpointInterface = "org.oscm.psp.intf.PaymentServiceProvider", wsdlLocation = "PaymentServiceProvider.wsdl")
public class PaymentServiceProviderBean implements PaymentServiceProvider {

    private static final String BILL_DATE_FORMAT = "dd/MM/yyyy";
    private static final String LOAD_PARAMETER_NAME = "load";
    private static final String TRANSACTION_RESPONSE_VALUE = "SYNC";
    private static final int MAX_POS_TEXT_LENGTH = 128;

    protected static Log4jLogger logger = LoggerFactory
            .getLogger(PaymentServiceProviderBean.class);

    protected HttpClient client;

    /**
     * Determines the proxy settings as defined in the systems configuration
     * settings and configures the HTTP client accordingly.
     */
    private void setProxyForHTTPClient(RequestData data) {

        if (client == null) {
            client = HttpClientFactory.getHttpClient();
        }

        String currentProxy = client.getHostConfiguration().getProxyHost();
        String proxyName = data
                .getProperty(HeidelpayConfigurationKey.HTTP_PROXY.name());
        if (proxyName != null && proxyName.length() > 0) {
            int proxyPort = Integer.parseInt(data
                    .getProperty(HeidelpayConfigurationKey.HTTP_PROXY_PORT
                            .name()));
            client.getHostConfiguration().setProxy(proxyName, proxyPort);
        } else if (currentProxy != null && currentProxy.length() > 0) {
            // remove old proxy settings
            client.getHostConfiguration().setProxyHost(null);
        }

    }

    /**
     * Validates the registration link related response and returns the link if
     * the response was indicating a successful execution.
     * 
     * @param regLinkDetails
     *            The String the PSP returned for the initial request. It
     *            contains the registration URL.
     * @return The response URL.
     * @throws PSPCommunicationException
     *             Thrown in case the PSP response did not contain a valid
     *             response URL.
     */
    private String validateLinkDetails(String regLinkDetails)
            throws PSPCommunicationException {

        StringTokenizer st = new StringTokenizer(regLinkDetails, "&");
        List<NameValuePair> result = new ArrayList<NameValuePair>();
        while (st.hasMoreElements()) {
            String[] split = st.nextToken().split("=");
            String keyString = split[0];
            String valueString = "";
            if (split.length > 1) {
                try {
                    valueString = URLDecoder.decode(split[1], "UTF-8");
                    valueString = valueString.replaceAll("\r\n", "");
                } catch (UnsupportedEncodingException e) {
                    SaaSSystemException sse = new SaaSSystemException(
                            "The link details for the PSP communication could not be validated",
                            e);
                    logger.logError(
                            Log4jLogger.SYSTEM_LOG,
                            sse,
                            LogMessageIdentifier.ERROR_NOT_SUPPORTED_ENCODING_UTF8);
                    throw sse;
                }
            }
            result.add(new NameValuePair(keyString, valueString));
        }

        for (NameValuePair nvp : result) {
            String keyString = nvp.getName();
            if (keyString.contains("VALIDATION")) {
                if (!nvp.getValue().equals("ACK")) {
                    PSPCommunicationException pce = new PSPCommunicationException(
                            "Accessing the PSP site failed, cannot proceed; "
                                    + keyString + "=" + nvp.getValue(),
                            Reason.MISSING_RESPONSE_URL);
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_RETRIEVE_REDIRECT_URL_INVALID);
                    throw pce;
                }
            }
        }
        String responseURL = getRedirectURL(result);
        if (responseURL == null) {
            PSPCommunicationException pce = new PSPCommunicationException(
                    "PSP response '" + regLinkDetails
                            + "' did not contain valid response URL",
                    Reason.MISSING_RESPONSE_URL);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, pce,
                    LogMessageIdentifier.WARN_DETERMINE_PSP_LINK_FAILED,
                    regLinkDetails);
            throw pce;
        }

        return responseURL;
    }

    /**
     * Checks all present name value pairs for the existence of an entry with
     * name FRONTEND.REDIRECT_URL. The found entry will be returned, null if
     * there is none.
     * 
     * @param result
     *            The list of name value pairs to search in.
     * @return The redirect URL, null if it is not set.
     */
    private String getRedirectURL(List<NameValuePair> result) {

        String redirectURL = null;
        for (NameValuePair nvp : result) {
            if (HeidelpayPostParameter.FRONTEND_REDIRECT_URL.equals(nvp
                    .getName())) {
                redirectURL = nvp.getValue();
                break;
            }
        }

        return redirectURL;
    }

    /**
     * Posts the given parameters to the PSP and retrieves the link the user has
     * to be redirected to.
     * 
     * @param registrationParameters
     *            The parameters to be sent with the call.
     * 
     * @return The response of the POST call to initialize the registration,
     *         which contains the redirection link for the calling user.
     * @throws PSPCommunicationException
     */
    private String retrieveRegistrationLinkDetails(
            List<NameValuePair> registrationParameters, RequestData data2)
            throws PSPCommunicationException {

        StringBuffer response = new StringBuffer();

        String pspPostInterfaceURL = data2
                .getProperty(HeidelpayConfigurationKey.PSP_POST_URL.name());
        PostMethod postMethod = HttpMethodFactory
                .getPostMethod(pspPostInterfaceURL);
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.addAll(registrationParameters);

        NameValuePair[] data = parameters.toArray(new NameValuePair[parameters
                .size()]);
        postMethod.setRequestBody(data);

        Header rqHeader = new Header("Content-Type",
                "application/x-www-form-urlencoded;charset=UTF-8");
        postMethod.setRequestHeader(rqHeader);
        BufferedReader br = null;
        InputStream in = null;
        try {
            client.executeMethod(postMethod);
            in = postMethod.getResponseBodyAsStream();
            br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = br.readLine();
            while (line != null) {
                response.append(line);
                line = br.readLine();
            }

        } catch (Exception e) {
            PSPCommunicationException pce = new PSPCommunicationException(
                    "Failure during communication with the PSP",
                    Reason.MISSING_RESPONSE_URL, e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, pce,
                    LogMessageIdentifier.WARN_RETRIEVE_PSP_LINK_FAILED);
            throw pce;
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                // ignore because it's already closed
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore because it's already closed
            }
            postMethod.releaseConnection();
        }

        return response.toString();
    }

    /**
     * Initializes the parameters required for the POST call to the PSP for
     * initial registration..
     * 
     * @param paymentInfo
     *            The payment type of payment info the iframe should be
     *            pre-configured for.
     * @return The properties required for the connection to the PSP in order to
     *         register the credit card or direct debit.
     */
    private List<NameValuePair> initPostParametersForRegistration(
            RequestData data) {

        List<NameValuePair> regParams = new ArrayList<NameValuePair>();
        setBasicPostParameters(regParams, data);

        // operation code for registration, we use credit card as default.
        regParams.add(new NameValuePair(HeidelpayPostParameter.PAYMENT_CODE,
                getHeidelPayPaymentType(data.getPaymentTypeId()) + ".RG"));
        initGeneralRegistrationPostData(regParams, data);

        return regParams;
    }

    /**
     * Initializes the parameters required for the POST call to the PSP for
     * re-registration..
     * 
     * @param paymentInfo
     *            The current payment information object.
     * @param paymentType
     *            The payment type the iframe should be pre-configured for.
     * @param supplierId
     *            Supplier context
     * @return The properties required for the connection to the PSP in order to
     *         register the credit card or direct debit.
     */
    private List<NameValuePair> initPostParametersForReRegistration(
            RequestData data) {

        List<NameValuePair> regParams = new ArrayList<NameValuePair>();
        setBasicPostParameters(regParams, data);

        // operation code for re-registration, we use credit card as default.
        regParams.add(new NameValuePair(HeidelpayPostParameter.PAYMENT_CODE,
                getHeidelPayPaymentType(data.getPaymentTypeId()) + ".RR"));

        // also set the already stored reference id
        regParams.add(new NameValuePair(
                HeidelpayPostParameter.IDENTIFICATION_REFERENCEID, data
                        .getExternalIdentifier()));

        initGeneralRegistrationPostData(regParams, data);

        return regParams;
    }

    /**
     * Inits the POST data required for every post request related to
     * registration.
     * 
     * @param regParams
     *            The parameters to be updated.
     * @param paymentInfo
     *            The payment type from paymentInfo the iframe should be
     *            pre-configured for.
     */
    private void initGeneralRegistrationPostData(List<NameValuePair> regParams,
            RequestData data) {
        // the user related data
        setUserProperties(data, regParams);

        setAuthenticationPostParameters(regParams, data);

        // the frontend settings
        setWPFControlParameters(data, regParams);

        setCallbackPostParameters(regParams, data);

        // set available payment options
        setChoiceRestrictionPostParameters(regParams, data);

        // transaction id is the customer's key value and the keyword
        // 'registration'
        regParams.add(new NameValuePair(
                HeidelpayPostParameter.IDENTIFICATION_TRANSACTIONID, data
                        .getOrganizationKey() + " registration"));
        regParams.add(new NameValuePair(
                HeidelpayPostParameter.PAYMENT_INFO_KEY, String.valueOf(data
                        .getPaymentInfoKey())));
        regParams.add(new NameValuePair(HeidelpayPostParameter.PAYMENT_INFO_ID,
                data.getPaymentInfoId()));
        regParams.add(new NameValuePair(
                HeidelpayPostParameter.PAYMENT_TYPE_KEY, String.valueOf(data
                        .getPaymentTypeKey())));
        regParams.add(new NameValuePair(
                HeidelpayPostParameter.ORGANIZATION_KEY, String.valueOf(data
                        .getOrganizationKey())));
        regParams.add(new NameValuePair(HeidelpayPostParameter.USER_LOCALE,
                data.getCurrentUserLocale()));
        String baseUrl = data.getProperty(HeidelpayConfigurationKey.BASE_URL
                .name());
        regParams.add(new NameValuePair(HeidelpayPostParameter.BASE_URL,
                baseUrl));

        String jsPath = data
                .getProperty(HeidelpayConfigurationKey.PSP_FRONTEND_JS_PATH
                        .name());
        regParams.add(new NameValuePair(
                HeidelpayPostParameter.FRONTEND_JS_SCRIPT, jsPath));
        regParams
                .add(new NameValuePair(
                        HeidelpayPostParameter.BES_PAYMENT_REGISTRATION_WSDL,
                        data.getProperty(HeidelpayConfigurationKey.PSP_PAYMENT_REGISTRATION_WSDL
                                .name())));
        regParams
                .add(new NameValuePair(
                        HeidelpayPostParameter.BES_PAYMENT_REGISTRATION_ENDPOINT,
                        data.getProperty(HeidelpayConfigurationKey.PSP_PAYMENT_REGISTRATION_ENDPOINT
                                .name())));
    }

    /**
     * Sets the parameters required for a later callback to our system.
     * 
     * @param regParams
     *            The list of name value pairs the callback parameters will be
     *            added to.
     */
    private void setCallbackPostParameters(List<NameValuePair> regParams,
            RequestData data) {

        regParams.add(new NameValuePair(
                HeidelpayPostParameter.FRONTEND_REDIRECT_TIME, "0"));

        // use the base-url to determine the response address
        String servletBaseURL = data
                .getProperty(HeidelpayConfigurationKey.PSP_RESPONSE_SERVLET_URL
                        .name());
        regParams.add(new NameValuePair(
                HeidelpayPostParameter.FRONTEND_RESPONSE_URL, servletBaseURL));

    }

    /**
     * Sets the parameters to restrict the payment method choices a user can
     * make in the WPF.
     * 
     * @param regParams
     *            The list of name value pairs the restriction parameters will
     *            be added to.
     * @param paymentInfo
     *            The payment type from payment info the iframe should be
     *            pre-configured for.
     */
    private void setChoiceRestrictionPostParameters(
            List<NameValuePair> regParams, RequestData data) {

        regParams
                .add(new NameValuePair(
                        HeidelpayPostParameter.FRONTEND_PM_DEFAULT_DISABLE_ALL,
                        "true"));

        String restrictionValue = null;
        if ("DIRECT_DEBIT".equals(data.getPaymentTypeId())) {
            restrictionValue = data
                    .getProperty(HeidelpayConfigurationKey.PSP_SUPPORTED_DD_COUNTRIES
                            .name());
        }
        if ("CREDIT_CARD".equals(data.getPaymentTypeId())) {
            restrictionValue = data
                    .getProperty(HeidelpayConfigurationKey.PSP_SUPPORTED_CC_BRANDS
                            .name());
        }
        if (restrictionValue != null) {
            addPostParamsToRestrictPaymentOptions(
                    getHeidelPayPaymentType(data.getPaymentTypeId()),
                    restrictionValue, regParams);
        }

    }

    /**
     * Adds the parameters to restrict the user's choices in the PSP's WPF
     * frontend.
     * 
     * @param method
     *            The method to be allowed in the WPF.
     * @param subtypes
     *            The subtypes to be set for the method.
     * @param params
     *            The list containing all currently set params. It will be
     *            enhanced by the new settings.
     */
    private void addPostParamsToRestrictPaymentOptions(String method,
            String subtypes, List<NameValuePair> params) {
        String methodKey = HeidelpayPostParameter.FRONTEND_PM_1_METHOD;
        String enablementKey = HeidelpayPostParameter.FRONTEND_PM_1_ENABLED;
        String subtypesKey = HeidelpayPostParameter.FRONTEND_PM_1_SUBTYPES;

        params.add(new NameValuePair(methodKey, method));
        params.add(new NameValuePair(enablementKey, "true"));
        params.add(new NameValuePair(subtypesKey, subtypes));
    }

    /**
     * Sets the parameters required to enable the WPF
     * 
     * @param regParams
     *            The list of name value pairs the WPF control parameters will
     *            be added to.
     */
    private void setWPFControlParameters(RequestData data,
            List<NameValuePair> regParams) {

        regParams.add(new NameValuePair(
                HeidelpayPostParameter.FRONTEND_ENABLED, "true"));
        regParams.add(new NameValuePair(HeidelpayPostParameter.FRONTEND_POPUP,
                "false"));
        regParams.add(new NameValuePair(HeidelpayPostParameter.FRONTEND_MODE,
                "DEFAULT"));
        regParams.add(new NameValuePair(
                HeidelpayPostParameter.FRONTEND_LANGUAGE, data
                        .getCurrentUserLocale()));
        regParams.add(new NameValuePair(
                HeidelpayPostParameter.FRONTEND_ONEPAGE, "true"));
        regParams.add(new NameValuePair(
                HeidelpayPostParameter.FRONTEND_NEXT_TARGET,
                "self.location.href"));

    }

    /**
     * Determines the required authentication parameters and sets them as
     * parameters for th e POST call.
     * 
     * @param regParams
     *            The list of name value paris the authentication parameters
     *            will be added to.
     */
    private void setAuthenticationPostParameters(List<NameValuePair> regParams,
            RequestData data) {

        regParams.add(new NameValuePair(HeidelpayPostParameter.SECURITY_SENDER,
                data.getProperty(HeidelpayConfigurationKey.PSP_SECURITY_SENDER
                        .name())));
        regParams
                .add(new NameValuePair(
                        HeidelpayPostParameter.TRANSACTION_CHANNEL,
                        data.getProperty(HeidelpayConfigurationKey.PSP_TRANSACTION_CHANNEL
                                .name())));
        regParams.add(new NameValuePair(HeidelpayPostParameter.USER_LOGIN, data
                .getProperty(HeidelpayConfigurationKey.PSP_USER_LOGIN.name())));
        regParams.add(new NameValuePair(HeidelpayPostParameter.USER_PWD, data
                .getProperty(HeidelpayConfigurationKey.PSP_USER_PWD.name())));

    }

    /**
     * Sets the basis parameters for the POST call to the PSP POST interface.
     * 
     * @param regParams
     *            The list of name value pairs the basic parameters will be
     *            added to.
     */
    private void setBasicPostParameters(List<NameValuePair> regParams,
            RequestData data) {

        String txnMode = data
                .getProperty(HeidelpayConfigurationKey.PSP_TXN_MODE.name());
        regParams.add(new NameValuePair(HeidelpayPostParameter.REQUEST_VERSION,
                HeidelpayXMLTags.REQUEST_COMPLIANCE_LEVEL));
        regParams.add(new NameValuePair(
                HeidelpayPostParameter.TRANSACTION_MODE, txnMode));

    }

    /**
     * Sets the user related properties as predefined in the PSP WPF. User can
     * still change the values.
     * 
     * @param regParams
     *            The properties object to be updated.
     */
    private void setUserProperties(RequestData data,
            List<NameValuePair> regParams) {

        if (data.getOrganizationName() != null) {
            regParams.add(new NameValuePair(
                    HeidelpayPostParameter.NAME_COMPANY, data
                            .getOrganizationName()));
        }
        if (data.getOrganizationEmail() != null) {
            regParams.add(new NameValuePair(
                    HeidelpayPostParameter.CONTACT_EMAIL, data
                            .getOrganizationEmail()));
        }

        // send dummy address data, empty strings
        regParams.add(new NameValuePair(HeidelpayPostParameter.USER_LAST_NAME,
                "  "));
        regParams.add(new NameValuePair(HeidelpayPostParameter.USER_FIRST_NAME,
                "  "));
        regParams.add(new NameValuePair(HeidelpayPostParameter.ADDRESS_STREET,
                "-"));
        regParams
                .add(new NameValuePair(HeidelpayPostParameter.ADDRESS_ZIP, "-"));
        regParams.add(new NameValuePair(HeidelpayPostParameter.ADDRESS_CITY,
                "-"));
        regParams.add(new NameValuePair(HeidelpayPostParameter.ADDRESS_COUNTRY,
                "DE"));

        // account holder information cannot be sent, as we are not providing
        // all account data..., so the WPF form will display 4 blanks

    }

    private void wrapIoException(IOException e, String info)
            throws PSPCommunicationException {
        PSPCommunicationException pce = new PSPCommunicationException(
                "Debit request could not be sent to the payment service provider successfully.\n[Details] "
                        + info, Reason.DEBIT_INVOCATION_FAILED, e);

        logger.logWarn(Log4jLogger.SYSTEM_LOG, pce,
                LogMessageIdentifier.WARN_CHARGING_PROCESS_FAILED);
        throw pce;
    }

    /**
     * Sets the analysis data in the XML request based on the customer and
     * billing data.
     * 
     * @param doc
     *            The root document.
     * @param transaction
     *            The transaction element to append the analysis data to.
     * @param customerHistory
     *            The customer the bill is generated for.
     * @param billingResult
     *            The detail data for the bill to be generated. This billing
     *            result must be related to a subscription, meaning that
     *            <code>billingResult.getSubscriptionKey()</code> must not
     *            return <code>null</code>.
     * @throws DOMException
     */
    private void setAnalysisXMLParameter(Document doc, Element transaction,
            ChargingData chargingData) throws DOMException {

        Element analysisNode = appendAnalysisElement(doc, transaction);
        appendAddressCriterion(doc, chargingData, analysisNode);
        appendEmailCriterion(doc, chargingData, analysisNode);
        appendCurrencyCriterion(doc, chargingData, analysisNode);
        appendTotalAmountCriterion(doc, chargingData, analysisNode);
        appendNetAmountCriterion(doc, chargingData, analysisNode);
        appendVatAmountCriterion(doc, chargingData, analysisNode);
        appendVatCriterion(doc, chargingData, analysisNode);
        appendNetDiscountCriterion(doc, chargingData, analysisNode);
        appendSubscriptionCriterion(chargingData, analysisNode);

    }

    private Element appendAnalysisElement(Document doc, Element transaction) {
        Element analysisNode = doc
                .createElement(HeidelpayXMLTags.XML_ELEMENT_ANALYSIS);
        transaction.appendChild(analysisNode);
        return analysisNode;
    }

    private void appendNetDiscountCriterion(Document doc,
            ChargingData chargingData, Element analysisNode) {
        Element netDiscountCriterion = doc
                .createElement(HeidelpayXMLTags.XML_ANALYSIS_CRITERION);
        netDiscountCriterion.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_NAME,
                HeidelpayXMLTags.XML_ANALYSIS_AMOUNT_NET_DISCOUNT);
        BigDecimal netDiscount = chargingData.getNetDiscount();
        String netDiscountAsString = netDiscount == null ? "null" : netDiscount
                .toPlainString();
        netDiscountCriterion.setTextContent(netDiscountAsString);
        if (netDiscount != null) {
            analysisNode.appendChild(netDiscountCriterion);
        }
    }

    private void appendVatCriterion(Document doc, ChargingData chargingData,
            Element analysisNode) {
        Element vatCriterion = doc
                .createElement(HeidelpayXMLTags.XML_ANALYSIS_CRITERION);
        vatCriterion.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_NAME,
                HeidelpayXMLTags.XML_ANALYSIS_PERCENT_VAT);
        vatCriterion.setTextContent(chargingData.getVat());
        analysisNode.appendChild(vatCriterion);
    }

    private void appendVatAmountCriterion(Document doc,
            ChargingData chargingData, Element analysisNode) {
        Element vatAmountCriterion = doc
                .createElement(HeidelpayXMLTags.XML_ANALYSIS_CRITERION);
        vatAmountCriterion.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_NAME,
                HeidelpayXMLTags.XML_ANALYSIS_AMOUNT_VAT);
        String vatAmountAsString = chargingData.getVatAmount() == null ? "null"
                : chargingData.getVatAmount().toPlainString();
        vatAmountCriterion.setTextContent(vatAmountAsString);
        analysisNode.appendChild(vatAmountCriterion);
    }

    private void appendNetAmountCriterion(Document doc,
            ChargingData chargingData, Element analysisNode) {
        Element netAmountCriterion = doc
                .createElement(HeidelpayXMLTags.XML_ANALYSIS_CRITERION);
        netAmountCriterion.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_NAME,
                HeidelpayXMLTags.XML_ANALYSIS_AMOUNT_NET);
        String netAmountAsString = chargingData.getNetAmount().toPlainString();
        netAmountCriterion.setTextContent(netAmountAsString);
        analysisNode.appendChild(netAmountCriterion);
    }

    private void appendTotalAmountCriterion(Document doc,
            ChargingData chargingData, Element analysisNode) {
        Element totalAmountCriterion = doc
                .createElement(HeidelpayXMLTags.XML_ANALYSIS_CRITERION);
        totalAmountCriterion.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_NAME,
                HeidelpayXMLTags.XML_ANALYSIS_AMOUNT_TOTAL);
        totalAmountCriterion.setTextContent(String.valueOf(chargingData
                .getGrossAmount()));
        analysisNode.appendChild(totalAmountCriterion);
    }

    private void appendCurrencyCriterion(Document doc,
            ChargingData chargingData, Element analysisNode) {
        Element currencyCriterion = doc
                .createElement(HeidelpayXMLTags.XML_ANALYSIS_CRITERION);
        currencyCriterion.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_NAME,
                HeidelpayXMLTags.XML_ANALYSIS_CURRENCY);
        currencyCriterion.setTextContent(chargingData.getCurrency());
        analysisNode.appendChild(currencyCriterion);
    }

    private void appendEmailCriterion(Document doc, ChargingData chargingData,
            Element analysisNode) {
        Element emailCriterion = doc
                .createElement(HeidelpayXMLTags.XML_ANALYSIS_CRITERION);
        emailCriterion.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_NAME,
                HeidelpayXMLTags.XML_ANALYSIS_EMAIL);
        if (chargingData.getEmail() != null) {
            emailCriterion.setTextContent(chargingData.getEmail());
        }
        analysisNode.appendChild(emailCriterion);
    }

    private void appendAddressCriterion(Document doc,
            ChargingData chargingData, Element analysisNode) {
        Element addressCriterion = doc
                .createElement(HeidelpayXMLTags.XML_ANALYSIS_CRITERION);
        addressCriterion.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_NAME,
                HeidelpayXMLTags.XML_ANALYSIS_ADDRESS_COMPLETE);
        if (chargingData.getAddress() != null) {
            addressCriterion.setTextContent(chargingData.getAddress());
        }
        analysisNode.appendChild(addressCriterion);
    }

    private void appendSubscriptionCriterion(ChargingData chargingData,
            Element analysisNode) {

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);

        for (PriceModelData pmd : chargingData.getPriceModelData()) {
            // POSITION
            String criterionName = HeidelpayXMLTags.XML_ANALYSIS_POSITION_POSITIONNAME
                    .replace(HeidelpayXMLTags.XML_ANALYSIS_NUMBER_PLACEHOLDER,
                            nf.format(pmd.getPosition()));
            Element positionNameCriterion = analysisNode.getOwnerDocument()
                    .createElement(HeidelpayXMLTags.XML_ANALYSIS_CRITERION);
            positionNameCriterion.setAttribute(
                    HeidelpayXMLTags.XML_ATTRIBUTE_NAME, criterionName);
            positionNameCriterion.setTextContent(String.valueOf(pmd
                    .getPosition()));
            analysisNode.appendChild(positionNameCriterion);

            // POISTION AMOUNT
            criterionName = HeidelpayXMLTags.XML_ANALYSIS_POSITION_AMOUNT
                    .replace(HeidelpayXMLTags.XML_ANALYSIS_NUMBER_PLACEHOLDER,
                            nf.format(pmd.getPosition()));
            Element positionAmountCriterion = analysisNode.getOwnerDocument()
                    .createElement(HeidelpayXMLTags.XML_ANALYSIS_CRITERION);
            positionAmountCriterion.setAttribute(
                    HeidelpayXMLTags.XML_ATTRIBUTE_NAME, criterionName);
            positionAmountCriterion.setTextContent(pmd.getNetAmount()
                    .toPlainString());
            analysisNode.appendChild(positionAmountCriterion);

            // PON etc.
            // the text shown on the bill should be <sub-id>, <startDate> -
            // <endDate> if there is no PON for the subscription or
            // <sub-id>/<PON>,
            // <startDate> - <endDate> otherwise
            criterionName = HeidelpayXMLTags.XML_ANALYSIS_POSITION_TEXT
                    .replace(HeidelpayXMLTags.XML_ANALYSIS_NUMBER_PLACEHOLDER,
                            nf.format(pmd.getPosition()));
            StringBuffer billText = new StringBuffer(
                    chargingData.getSubscriptionId());
            if (chargingData.getPon() != null
                    && chargingData.getPon().length() > 0) {
                billText.append("/").append(chargingData.getPon());
            }

            // the following character for the period must be shown and will
            // have a total length of 23
            // == ', xx/yy/zzzz-xx/yy/zzzz'. All in all we hae 128 chars, so 105
            // are left. if the length of sub-id and pon exceeds that, trim them
            final int maxTextLength = MAX_POS_TEXT_LENGTH - 3
                    - (2 * BILL_DATE_FORMAT.length());
            if (billText.length() > maxTextLength) {
                billText.setLength(maxTextLength - 3);
                billText.append("...");
            }

            // finally add the start- and endTime
            DateFormat df = new SimpleDateFormat(BILL_DATE_FORMAT);
            long startTime = pmd.getStartDate();
            billText.append(", ").append(df.format(new Date(startTime)));

            long endTime = pmd.getEndDate() - 1;
            billText.append("-").append(df.format(new Date(endTime)));

            Element positionTextCriterion = analysisNode.getOwnerDocument()
                    .createElement(HeidelpayXMLTags.XML_ANALYSIS_CRITERION);
            positionTextCriterion.setAttribute(
                    HeidelpayXMLTags.XML_ATTRIBUTE_NAME, criterionName);
            positionTextCriterion.setTextContent(billText.toString());
            analysisNode.appendChild(positionTextCriterion);
        }
    }

    private String getStackTrace(Throwable ex) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Sets the header information (request compliance level) in the XML
     * request.
     * 
     * @param doc
     *            The root document.
     * @return The request element representing the top-element in the document
     *         structure.
     */
    private Element setHeaderXMLParameters(Document doc) {

        Element request = doc
                .createElement(HeidelpayXMLTags.XML_ELEMENT_REQUEST);
        request.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_VERSION,
                HeidelpayXMLTags.REQUEST_COMPLIANCE_LEVEL);
        doc.appendChild(request);

        return request;
    }

    /**
     * Sets the identification information in the XML structure.
     * 
     * @param doc
     *            The root document.
     * @param transaction
     *            The transaction element of the document.
     * @param transactionId
     *            The transaction identifier to be set..
     * @param referenceId
     *            The referenceId to be set. If the value is null, it will not
     *            appear in the request.
     */
    private void setIdentificationXMLParameters(Document doc,
            Element transaction, long transactionId, String referenceId) {

        Element identification = doc
                .createElement(HeidelpayXMLTags.XML_ELEMENT_IDENTIFICATION);
        Element transactionIdElement = doc
                .createElement(HeidelpayXMLTags.XML_ELEMENT_TRANSACTIONID);
        transactionIdElement.setTextContent(String.valueOf(transactionId));
        identification.appendChild(transactionIdElement);

        if (referenceId != null) {
            Element referenceIdElement = doc
                    .createElement(HeidelpayXMLTags.XML_ELEMENT_REFERENCEID);
            referenceIdElement.setTextContent(referenceId);
            identification.appendChild(referenceIdElement);
        }

        transaction.appendChild(identification);

    }

    /**
     * Sets the customer identifier in the XML request for the PSP.
     * 
     * @param doc
     *            The root document.
     * @param transaction
     *            The transaction element to append the customer identifier to.
     * @param paymentInformation
     *            The payment information of the user.
     */
    private void setPaymentIdentifierXMLParameter(Document doc,
            Element transaction, String externalIdentifier) {

        Element account = doc
                .createElement(HeidelpayXMLTags.XML_ELEMENT_ACCOUNT);
        account.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_REGISTRATION,
                externalIdentifier);
        transaction.appendChild(account);

    }

    /**
     * Sets the payment related information in the XML structure.
     * 
     * @param doc
     *            The root document.
     * @param transaction
     *            The transaction element to be enhanced by the payment element.
     * @param billingResult
     *            The billing result containing the costs.
     * @param paymentType
     *            The payment type to be used.
     */
    private void setPaymentXMLParameters(Document doc, Element transaction,
            RequestData data, ChargingData chargingData) {

        Element payment = doc
                .createElement(HeidelpayXMLTags.XML_ELEMENT_PAYMENT);

        String paymentPrefix = getHeidelPayPaymentType(data.getPaymentTypeId());

        payment.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_CODE, paymentPrefix
                + ".DB");
        transaction.appendChild(payment);
        Element presentation = doc
                .createElement(HeidelpayXMLTags.XML_ELEMENT_REPRESENTATION);
        Element amount = doc.createElement(HeidelpayXMLTags.XML_ELEMENT_AMOUNT);
        Element currency = doc
                .createElement(HeidelpayXMLTags.XML_ELEMENT_CURRENCY);
        Element usage = doc.createElement(HeidelpayXMLTags.XML_ELEMENT_USAGE);

        // determine costs
        PriceConverter converter = new PriceConverter(Locale.US);
        String costs = converter.getValueToDisplay(
                chargingData.getGrossAmount(), false);
        amount.setTextContent(costs);
        currency.setTextContent(chargingData.getCurrency());

        // Set the text that will appear on user's statement - supplier,
        // period, billing key (128 chars at most)
        String supName = data.getOrganizationName();
        if (supName == null || supName.length() == 0) {
            supName = data.getOrganizationId();
        }
        long billingKey = chargingData.getTransactionId();
        DateFormat df = new SimpleDateFormat(BILL_DATE_FORMAT);
        String periodStart = df.format(chargingData.getPeriodStartTime());
        String periodEnd = df.format(chargingData.getPeriodEndTime());
        usage.setTextContent(String.format("%s, Id: %s, %s - %s", supName,
                String.valueOf(billingKey), periodStart, periodEnd));

        payment.appendChild(presentation);
        presentation.appendChild(amount);
        presentation.appendChild(currency);
        presentation.appendChild(usage);

    }

    /**
     * Sets the transaction related attributes in the given document.
     * 
     * @param doc
     *            The root document, used to generate elements.
     * @param request
     *            The request the information will be added to as sub-elements.
     * @param useGlobalChannel
     *            Indicates whether the supplier's channel id should be used or
     *            the global channel id. Deregistration must use the global one,
     *            as the registration is based on it, too. Not to be used for
     *            debit operations.
     * @return The transaction element for further processing.
     * @throws PSPIdentifierForSellerException
     *             Thrown in case the supplier's psp identifier setting cannot
     *             be evaluated.
     */
    private Element setTransactionXMLAttributes(Document doc, Element request,
            boolean useGlobalChannel, RequestData data)
            throws PSPIdentifierForSellerException {

        String txnMode = data
                .getProperty(HeidelpayConfigurationKey.PSP_TXN_MODE.name());
        String userLogin = data
                .getProperty(HeidelpayConfigurationKey.PSP_USER_LOGIN.name());
        String userPwd = data
                .getProperty(HeidelpayConfigurationKey.PSP_USER_PWD.name());

        Element transaction = doc
                .createElement(HeidelpayXMLTags.XML_ELEMENT_TRANSACTION);
        transaction.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_MODE, txnMode);
        transaction.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_RESPONSE,
                TRANSACTION_RESPONSE_VALUE);

        String channelId = null;
        if (useGlobalChannel) {
            channelId = data
                    .getProperty(HeidelpayConfigurationKey.PSP_TRANSACTION_CHANNEL
                            .name());
        } else {
            channelId = data.getPspIdentifier();
            if (channelId == null) {
                // Charging cannot succeed, so mark the operation as failed by
                // throwing an exception
                PSPIdentifierForSellerException mpi = new PSPIdentifierForSellerException(
                        "Supplier organization '" + data.getOrganizationKey()
                                + "' is missing the pspIdentifier setting");
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        mpi,
                        LogMessageIdentifier.WARN_DEBIT_FAILED_NO_PSPIDENTIFIER_SETTING);
                throw mpi;
            }
        }
        transaction.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_CHANNEL,
                channelId);
        request.appendChild(transaction);

        Element user = doc.createElement(HeidelpayXMLTags.XML_ELEMENT_USER);
        user.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_LOGIN, userLogin);
        user.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_PASSWORD, userPwd);
        transaction.appendChild(user);

        return transaction;
    }

    /**
     * Appends the required security information to the given document.
     * 
     * @param doc
     *            The parent document..
     * @param request
     *            The request object, the information is a sub-element to.
     */
    private void setSecurityHeaderXMLParameters(Document doc, Element request,
            RequestData data) {
        String senderId = data
                .getProperty(HeidelpayConfigurationKey.PSP_SECURITY_SENDER
                        .name());
        Element header = doc.createElement(HeidelpayXMLTags.XML_ELEMENT_HEADER);
        Element security = doc
                .createElement(HeidelpayXMLTags.XML_ELEMENT_SECURITY);
        security.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_SENDER, senderId);
        header.appendChild(security);
        request.appendChild(header);
    }

    @Override
    public RegistrationLink determineRegistrationLink(RequestData data)
            throws PSPCommunicationException {

        if (data == null) {
            throw new IllegalArgumentException("requestData must not be null!");
        }

        setProxyForHTTPClient(data);
        List<NameValuePair> registrationParameters = initPostParametersForRegistration(data);

        String regLinkDetails = retrieveRegistrationLinkDetails(
                registrationParameters, data);

        String result = validateLinkDetails(regLinkDetails);
        RegistrationLink link = new RegistrationLink();
        link.setUrl(result);
        link.setBrowserTarget("");

        return link;
    }

    @Override
    public RegistrationLink determineReregistrationLink(RequestData data)
            throws PSPCommunicationException {

        if (data == null) {
            throw new IllegalArgumentException("requestData must not be null!");
        }

        setProxyForHTTPClient(data);
        List<NameValuePair> registrationParameters = initPostParametersForReRegistration(data);
        String regLinkDetails = retrieveRegistrationLinkDetails(
                registrationParameters, data);
        String result = validateLinkDetails(regLinkDetails);
        RegistrationLink link = new RegistrationLink(result, "");

        return link;
    }

    @Override
    public void deregisterPaymentInformation(RequestData data)
            throws PaymentDeregistrationException {

        if (data == null) {
            throw new IllegalArgumentException("requestData must not be null!");
        }

        String processingResult = null;
        Exception occurredException = null;
        LogMessageIdentifier failureMessageId = null;
        String[] failureMessageParam = null;

        setProxyForHTTPClient(data);

        PostMethod postMethod = HttpMethodFactory.getPostMethod(data
                .getProperty(HeidelpayConfigurationKey.PSP_XML_URL.name()));
        try {
            Document doc = createDeregistrationRequestDocument(data);

            String result = XMLConverter.convertToString(doc, true);
            NameValuePair nvp = new NameValuePair(LOAD_PARAMETER_NAME, result);
            postMethod.setRequestBody(new NameValuePair[] { nvp });

            client.executeMethod(postMethod);

            String response = postMethod.getResponseBodyAsString();
            HeidelpayResponse heidelPayResponse = new HeidelpayResponse(
                    response);
            processingResult = heidelPayResponse.getProcessingResult();

            if (!HeidelpayPostParameter.SUCCESS_RESULT.equals(processingResult)) {
                String returnCode = heidelPayResponse.getProcessingReturnCode();
                failureMessageId = LogMessageIdentifier.ERROR_DEREGISTER_PAYMENT_INFORMATION_IN_PSP_FAILED;
                failureMessageParam = new String[] {
                        String.valueOf(data.getOrganizationId()),
                        String.valueOf(data.getPaymentInfoKey()), returnCode };
            }
        } catch (Exception e) {
            occurredException = e;
            failureMessageId = LogMessageIdentifier.ERROR_DEREGISTER_PAYMENT_INFORMATION__FAILED_ON_PSP_SIDE;
            failureMessageParam = new String[] {
                    String.valueOf(data.getPaymentInfoKey()),
                    data.getOrganizationId() };
        }

        if (failureMessageId != null) {
            PaymentDeregistrationException pdf = new PaymentDeregistrationException(
                    "Deregistration in PSP system failed", occurredException);
            logger.logError(Log4jLogger.SYSTEM_LOG, pdf, failureMessageId,
                    failureMessageParam);
            throw pdf;
        }

    }

    private Document createDeregistrationRequestDocument(RequestData data)
            throws ParserConfigurationException,
            PSPIdentifierForSellerException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element headerElement = setHeaderXMLParameters(doc);
        setSecurityHeaderXMLParameters(doc, headerElement, data);

        Element transactionElement = setTransactionXMLAttributes(doc,
                headerElement, true, data);

        setIdentificationXMLParameters(doc, transactionElement, data
                .getPaymentInfoKey().longValue(), data.getExternalIdentifier());

        // set the payment code to indicate deregistration
        Element paymentElement = doc
                .createElement(HeidelpayXMLTags.XML_ELEMENT_PAYMENT);
        paymentElement.setAttribute(HeidelpayXMLTags.XML_ATTRIBUTE_CODE,
                getHeidelPayPaymentType(data.getPaymentTypeId()) + ".DR");
        transactionElement.appendChild(paymentElement);
        return doc;
    }

    @Override
    public ChargingResult charge(RequestData requestData,
            ChargingData chargingData) throws PSPCommunicationException,
            PSPProcessingException {

        if (requestData == null || chargingData == null) {
            throw new IllegalArgumentException(
                    "requestData and chargingData must not be null!");
        }

        ChargingResult result = new ChargingResult();
        String resultMessage = "";
        String doc = null;
        String url = null;
        String response = null;
        try {
            setProxyForHTTPClient(requestData);

            // don't perform payment tasks if no costs have been generated.
            BigDecimal overallCosts = chargingData.getGrossAmount();
            if (overallCosts == null || overallCosts.doubleValue() <= 0.0) {
                return null;
            }

            // create xml request document
            Document chargingDocument = createChargingXmlDocument(requestData,
                    chargingData);

            url = requestData.getProperty(HeidelpayConfigurationKey.PSP_XML_URL
                    .name());

            // create post method and execute request
            PostMethod postMethod = HttpMethodFactory.getPostMethod(url);

            doc = XMLConverter.convertToString(chargingDocument, true);
            logger.logDebug("charge(RequestData, ChargingData) sending to '"
                    + url + "':\n" + doc);

            NameValuePair nvp = new NameValuePair(LOAD_PARAMETER_NAME, doc);

            postMethod.setRequestBody(new NameValuePair[] { nvp });
            client.executeMethod(postMethod);

            // store the processing result XML
            response = postMethod.getResponseBodyAsString();
            logger.logDebug("charge(RequestData, ChargingData) received:\n"
                    + response);
            HeidelpayResponse heidelpayResponse = getHeidelPayResponse(response);
            resultMessage = heidelpayResponse.getProcessingResult();
            result.setProcessingResult(response);
        } catch (HttpException e) {
            wrapIoException(e, getErrorDetails(url, doc, response));
        } catch (IOException e) {
            wrapIoException(e, getErrorDetails(url, doc, response));
        } catch (Exception e) {
            wrapException(chargingData, e, getErrorDetails(url, doc, response));
        }

        // if there was no exception, check the response from the PSP
        if (!resultMessage.equals(HeidelpayPostParameter.SUCCESS_RESULT)) {
            PSPProcessingException ppe = new PSPProcessingException(
                    "Processing failed as PSP returned '" + resultMessage
                            + "' during processing. Debiting customer '"
                            + chargingData.getCustomerKey()
                            + "' failed.\n[Details] "
                            + getErrorDetails(url, doc, response));
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ppe,
                    LogMessageIdentifier.WARN_CHARGING_CUSTOMER_FAILED,
                    Long.toString(chargingData.getCustomerKey().longValue()));
            result.setProcessingException(getStackTrace(ppe));
            result.setProcessingStatus(PaymentProcessingStatus.FAILED_EXTERNAL);
            throw ppe;
        }

        // as there was no problem, set the status to success
        result.setProcessingStatus(PaymentProcessingStatus.SUCCESS);

        return result;
    }

    protected HeidelpayResponse getHeidelPayResponse(String response)
            throws XPathExpressionException, ParserConfigurationException,
            SAXException, IOException {
        return new HeidelpayResponse(response);
    }

    private String getErrorDetails(String url, String sentData, String response) {
        StringBuffer bf = new StringBuffer();
        if (url != null) {
            bf.append("XML sent to '");
            bf.append(url);
            bf.append("'");
        }
        if (sentData != null) {
            bf.append(":\n'");
            bf.append(sentData);
        }
        if (response != null) {
            bf.append("\nReceived Response:\n");
            bf.append(response);
        }
        return bf.toString();
    }

    private void wrapException(ChargingData chargingData, Exception e,
            String info) throws PSPProcessingException {
        PSPProcessingException ppe = new PSPProcessingException(
                "Debiting the customer ' " + chargingData.getCustomerKey()
                        + "' failed.\n[Details] " + info, e);

        logger.logError(Log4jLogger.SYSTEM_LOG, ppe,
                LogMessageIdentifier.ERROR_PROCESS_CHARGING_FAILED);
        throw ppe;
    }

    private Document createChargingXmlDocument(RequestData data,
            ChargingData chargingData) throws ParserConfigurationException,
            PSPIdentifierForSellerException {
        // create xml document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        // build xml structure
        Element request = setHeaderXMLParameters(doc);
        setSecurityHeaderXMLParameters(doc, request, data);
        Element transaction = setTransactionXMLAttributes(doc, request, false,
                data);
        setIdentificationXMLParameters(doc, transaction,
                chargingData.getTransactionId(), null);
        setPaymentXMLParameters(doc, transaction, data, chargingData);
        setPaymentIdentifierXMLParameter(doc, transaction,
                data.getExternalIdentifier());
        setAnalysisXMLParameter(doc, transaction, chargingData);
        return doc;
    }

    private String getHeidelPayPaymentType(String paymentType) {
        if (paymentType == null) {
            throw new IllegalArgumentException(
                    "Payment type 'null' not allowed for PSP Heidelpay calls");
        }
        paymentType = paymentType.toLowerCase();
        if (paymentType.indexOf("credit") > -1
                && paymentType.indexOf("card") > -1) {
            return HeidelpayPostParameter.PAYMENT_OPTION_CREDIT_CARD;
        }
        if (paymentType.indexOf("direct") > -1
                && paymentType.indexOf("debit") > -1) {
            return HeidelpayPostParameter.PAYMENT_OPTION_DIRECT_DEBIT;
        }
        throw new IllegalArgumentException("Payment type '" + paymentType
                + "' not allowed for PSP Heidelpay calls");
    }
}
