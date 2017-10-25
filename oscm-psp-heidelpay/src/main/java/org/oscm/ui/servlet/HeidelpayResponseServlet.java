/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.jws.WebService;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.paymentservice.constants.HeidelpayPostParameter;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.psp.data.RegistrationData;
import org.oscm.psp.data.RegistrationData.Status;
import org.oscm.psp.intf.PaymentRegistrationService;
import org.oscm.types.enumtypes.PaymentInfoType;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.PaymentDataException;

/**
 * Servlet implementation class PSPResponse
 */
public class HeidelpayResponseServlet extends HttpServlet {
    protected static final String FRONTEND_REQUEST_CANCELLED = "FRONTEND.REQUEST.CANCELLED";
    protected static final String CRITERION_BES_BASE_URL = "CRITERION.BES_BASE_URL";
    protected static final String IDENTIFICATION_UNIQUEID = "IDENTIFICATION.UNIQUEID";
    protected static final String SUCCESS_RESULT = "ACK";
    protected static final String FAILURE_RESULT = "NOK";
    protected static final String PAYMENT_CODE = "PAYMENT.CODE";
    protected static final String CRITERION_USER_LOCALE = "CRITERION.USER_LOCALE";
    protected static final String CRITERION_BES_PAYMENT_INFO_KEY = "CRITERION.BES_PAYMENT_INFO_KEY";
    protected static final String CRITERION_BES_PAYMENT_INFO_ID = "CRITERION.BES_PAYMENT_INFO_ID";
    protected static final String CRITERION_BES_ORGANIZATION_KEY = "CRITERION.BES_ORGANIZATION_KEY";
    protected static final String CRITERION_BES_PAYMENT_TYPE_KEY = "CRITERION.BES_PAYMENT_TYPE_KEY";
    protected static final String PROCESSING_TIMESTAMP = "PROCESSING.TIMESTAMP";
    protected static final String PROCESSING_REASON = "PROCESSING.REASON";
    protected static final String PROCESSING_RETURN_CODE = "PROCESSING.RETURN.CODE";
    protected static final String PROCESSING_RETURN = "PROCESSING.RETURN";
    protected static final String PROCESSING_CODE = "PROCESSING.CODE";
    protected static final String PROCESSING_RESULT = "PROCESSING.RESULT";

    /**
     * credit card or bank account number
     */
    protected static final String ACCOUNT_NUMBER = "ACCOUNT.NUMBER";

    /**
     * Credit card related data: brand name e.g. VISA; expiration month and year
     */
    protected static final String ACCOUNT_BRAND = "ACCOUNT.BRAND";
    protected static final String ACCOUNT_EXPIRY_MONTH = "ACCOUNT.EXPIRY_MONTH";
    protected static final String ACCOUNT_EXPIRY_YEAR = "ACCOUNT.EXPIRY_YEAR";

    /**
     * Bank related data: identifier ('Bankleitzahl') and name
     */
    protected static final String ACCOUNT_BANK = "ACCOUNT.BANK";
    protected static final String ACCOUNT_BANKNAME = "ACCOUNT.BANKNAME";

    private static final long serialVersionUID = 1L;
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(HeidelpayResponseServlet.class);

    public HeidelpayResponseServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // not required
        return;
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {

        boolean success = true;

        // extract request properties
        Properties pspParameters = new Properties();
        try {
            pspParameters = extractPSPParameters(request);
        } catch (IOException e) {
            // if the request information cannot be read, we cannot determine
            // whether the registration worked or not. Hence we assume it
            // failed, log a warning and return the failure-URL to the PSP.
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_HEIDELPAY_INPUT_PROCESS_FAILED);
            success = false;
        }

        // if the input could be validated, read the properties
        StringBuffer missingPropKeysCSV = new StringBuffer();
        String processingResult = getProperty(PROCESSING_RESULT, pspParameters,
                missingPropKeysCSV);
        String processingCode = getProperty(PROCESSING_CODE, pspParameters,
                missingPropKeysCSV);
        String processingReturn = getProperty(PROCESSING_RETURN, pspParameters,
                missingPropKeysCSV);
        String processingReturnCode = getProperty(PROCESSING_RETURN_CODE,
                pspParameters, missingPropKeysCSV);
        String processingReason = getProperty(PROCESSING_REASON, pspParameters,
                missingPropKeysCSV);
        String processingTime = getProperty(PROCESSING_TIMESTAMP,
                pspParameters, missingPropKeysCSV);
        String paymentCode = getProperty(PAYMENT_CODE, pspParameters,
                missingPropKeysCSV);
        String paymentInfoKey = getProperty(CRITERION_BES_PAYMENT_INFO_KEY,
                pspParameters, missingPropKeysCSV);
        String paymentInfoId = getProperty(CRITERION_BES_PAYMENT_INFO_ID,
                pspParameters, missingPropKeysCSV);
        String organizationKey = getProperty(CRITERION_BES_ORGANIZATION_KEY,
                pspParameters, missingPropKeysCSV);
        String paymentTypeKey = getProperty(CRITERION_BES_PAYMENT_TYPE_KEY,
                pspParameters, missingPropKeysCSV);
        String baseURL = getProperty(CRITERION_BES_BASE_URL, pspParameters,
                missingPropKeysCSV);
        String wsdlUrl = getProperty(
                HeidelpayPostParameter.BES_PAYMENT_REGISTRATION_WSDL,
                pspParameters, missingPropKeysCSV);
        String wsUrl = getProperty(
                HeidelpayPostParameter.BES_PAYMENT_REGISTRATION_ENDPOINT,
                pspParameters, missingPropKeysCSV);

        // determine if the user cancelled the registration. Do not read this
        // using the helper method, as this parameter might be missing (what
        // indicates that the user did not cancel his operation).
        boolean cancelledByUser = Boolean.parseBoolean(pspParameters
                .getProperty(FRONTEND_REQUEST_CANCELLED));

        // if any value is null, this must be considered as an indicator for an
        // incomplete and unsuccessful registration. Log the problem and return
        // the failure site URL (set success to false)
        if (missingPropKeysCSV.length() > 0) {
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_PROCESS_PSP_REQUEST_FAILED_MISSING_PROPERTIES,
                    missingPropKeysCSV.toString(), pspParameters.toString());
            success = false;
        }

        PaymentInfoType paymentOption = getPaymentOptionFromCode(paymentCode);
        if (paymentCode == null
                || (!paymentCode.contains(".RG") && !paymentCode
                        .contains(".RR")) || paymentOption == null) {
            logger.logInfo(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.INFO_PAYMENT_CODE_UNKNOWN, paymentCode);
            success = false;
        }
        // build registration data
        RegistrationData registrationData = createRegistrationData(
                pspParameters, paymentInfoKey, paymentTypeKey, organizationKey,
                paymentInfoId, paymentOption);

        // handle the scenario where the user cancelled the registration
        if (cancelledByUser) {
            logger.logInfo(
                    Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.INFO_PAYMENT_INFO_REGISTRATION_CANCELLED,
                    paymentInfoKey, processingTime);
            success = false;
            registrationData.setStatus(Status.Canceled);
        } else if (success && FAILURE_RESULT.equals(processingResult)) {
            // if input could be read, evaluate it
            // log a warning, including reason
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_PAYMENT_INFORMATION_WITH_PSP_REGISTRATION_FAILED,
                    paymentInfoKey, processingCode, processingReturn,
                    processingReturnCode, processingReason, processingTime);
            success = false;
            registrationData.setStatus(Status.Failure);
        }

        String url = null;
        try {
            // get ws url from properties
            if (!success && registrationData.getStatus() == Status.Success) {
                registrationData.setStatus(Status.Failure);
            }
            url = register(wsdlUrl, registrationData, wsUrl);
            logger.logInfo(
                    Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.INFO_PAYMENT_INFO_REGISTRATION_SUCCESS,
                    paymentInfoKey, processingCode, processingReturn,
                    processingReturnCode, processingReason, processingTime);
        } catch (Exception e) {
            success = false;
            registrationData.setStatus(Status.Failure);
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_EVALUATE_PSP_RESPONSE_FAILED);
        }

        // redirect to success site
        try {
            PrintWriter writer = response.getWriter();
            if (writer == null) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_RESPONSE_URL_TO_PSP_FAILED_NOT_WRITTEN_RESPONSE_OBJECT,
                        Boolean.toString(success));
            } else {
                if (url != null) {
                    writer.write(baseURL + url);
                } else {
                    writer.print(baseURL + "/public/pspregistrationresult.jsf");
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_RESPONSE_URL_TO_PSP_FAILED_NOT_DETERMINED_RESPONSE_URL,
                            Boolean.toString(success));
                }
            }
        } catch (IOException e) {
            // in case we cannot write to the response, log the problem. The
            // assumption is that the customer now gets to see a heidelpay
            // exception text in the portal iframe.
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_SEND_RESPONSE_URL_TO_PSP_FAILED,
                    Boolean.toString(success));
        }

    }

    String register(String wsdlUrl, RegistrationData registrationData,
            String wsUrl) throws ObjectNotFoundException, PaymentDataException,
            OperationNotPermittedException, MalformedURLException, Exception {
        PaymentRegistrationService paymentRegistrationServicePort = Service
                .create(new URL(wsdlUrl),
                        new QName(PaymentRegistrationService.class
                                .getAnnotation(WebService.class)
                                .targetNamespace(),
                                PaymentRegistrationService.class
                                        .getSimpleName())).getPort(
                        PaymentRegistrationService.class);
        setEndpointInContext(
                ((BindingProvider) paymentRegistrationServicePort), wsUrl);
        return paymentRegistrationServicePort.register(registrationData);
    }

    public <T> void setEndpointInContext(BindingProvider client, String wsUrl) {
        Map<String, Object> clientRequestContext = client.getRequestContext();
        clientRequestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                wsUrl);
    }

    private RegistrationData createRegistrationData(Properties pspParameters,
            String paymentInfoKey, String paymentTypeKey,
            String organizationKey, String paymentInfoId,
            PaymentInfoType paymentOption) {

        RegistrationData registrationData = new RegistrationData();
        registrationData.setIdentification(pspParameters
                .getProperty(IDENTIFICATION_UNIQUEID));
        registrationData.setAccountNumber(pspParameters
                .getProperty(ACCOUNT_NUMBER));
        // Regarding the next 3 try/catch blocks:
        // Here we just convert the string number to a long. Input might
        // be strange, since it comes from external resource.
        // Threrefore, we try to convert and If it failes, we set the
        // key to -1 and let. We don't want to have any business logic
        // here, the next step, calling the psp registration web service
        // of BES will raise an proper error if needed.
        // -1 values might be correct if PaymentInfoKey is set to a
        // proper value.
        if (paymentInfoKey != null && paymentInfoKey.trim().length() > 0) {
            try {
                registrationData.setPaymentInfoKey(Long
                        .parseLong(paymentInfoKey));
            } catch (NumberFormatException ex) {
                registrationData.setPaymentInfoKey(-1);
            }
        }
        registrationData.setPaymentInfoId(paymentInfoId);
        if (paymentTypeKey != null && paymentTypeKey.trim().length() > 0) {
            try {
                registrationData.setPaymentTypeKey(Long
                        .parseLong(paymentTypeKey));
            } catch (NumberFormatException ex) {
                registrationData.setPaymentTypeKey(-1);
            }
        }
        if (organizationKey != null && organizationKey.trim().length() > 0) {
            try {
                registrationData.setOrganizationKey(Long
                        .parseLong(organizationKey));
            } catch (NumberFormatException ex) {
                registrationData.setOrganizationKey(-1);
            }
        }
        registrationData.setProvider(determineProviderName(pspParameters,
                paymentOption));

        return registrationData;
    }

    protected String determineProviderName(Properties params,
            PaymentInfoType paymentOption) {
        String provider = null;
        if (paymentOption == PaymentInfoType.DIRECT_DEBIT) {
            String bankName = params.getProperty(ACCOUNT_BANKNAME);
            String bank = params.getProperty(ACCOUNT_BANK);
            if (isEmpty(bankName)) {
                provider = bank;
            } else {
                provider = bankName;
                if (!isEmpty(bank)) {
                    provider += " (" + bank + ")";
                }
            }

        } else if (paymentOption == PaymentInfoType.CREDIT_CARD) {
            String brand = params.getProperty(ACCOUNT_BRAND);
            String month = params.getProperty(ACCOUNT_EXPIRY_MONTH);
            String year = params.getProperty(ACCOUNT_EXPIRY_YEAR);
            provider = brand;
            if (!isEmpty(month) && !isEmpty(year)) {
                provider += " " + month + "/" + year;
            }
        }
        return provider;
    }

    /**
     * Determines the payment info type according to the payment code returned
     * by heidelpay.
     * 
     * @param paymentCode
     *            The payment code to check.
     * @return The payment info type corresponding to the payment code,
     *         <code>null</code> if the content is not valid.
     */
    private PaymentInfoType getPaymentOptionFromCode(String paymentCode) {
        if (paymentCode == null) {
            return null;
        }

        if (paymentCode.startsWith("CC")) {
            return PaymentInfoType.CREDIT_CARD;
        } else if (paymentCode.startsWith("DD")) {
            return PaymentInfoType.DIRECT_DEBIT;
        }
        return null;
    }

    /**
     * Converts the input given in the request to a properties object.
     * 
     * @param request
     *            The received request.
     * @return The properties contained in the request.
     * @throws IOException
     *             Thrown in case the request information could not be
     *             evaluated.
     */
    private Properties extractPSPParameters(HttpServletRequest request)
            throws IOException {

        Properties props = new Properties();
        ServletInputStream inputStream = request.getInputStream();
        if (inputStream == null) {
            return props;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(
                inputStream, "UTF-8"));
        String line = br.readLine();
        StringBuffer sb = new StringBuffer();
        while (line != null) {
            sb.append(line);
            line = br.readLine();
        }
        String params = sb.toString();
        StringTokenizer st = new StringTokenizer(params, "&");
        while (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            String[] splitResult = nextToken.split("=");
            String key = splitResult[0];
            String value = "";
            if (splitResult.length > 1) {
                value = URLDecoder.decode(splitResult[1], "UTF-8");
            }
            props.setProperty(key, value);
        }

        return props;
    }

    /**
     * Determines the property value from the property object specified as
     * parameter.
     * 
     * @param key
     *            The key to look for.
     * @param sourceProperties
     *            The source properties object.
     * @param missingKeysCSV
     *            The comma separated list of keys indicating missing values.
     * @return The property value for the given key.
     */
    private String getProperty(String key, Properties sourceProperties,
            StringBuffer missingKeysCSV) {
        String result = sourceProperties.getProperty(key);
        if (result == null) {
            if (missingKeysCSV.length() > 0) {
                missingKeysCSV.append(", ");
            }
            missingKeysCSV.append(key);

        }
        return result;
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // not allowed because of security risk
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // not allowed because of security risk
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // not allowed because of security risk
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    private static final boolean isEmpty(String value) {
        return (value == null || value.trim().length() == 0);
    }

}
