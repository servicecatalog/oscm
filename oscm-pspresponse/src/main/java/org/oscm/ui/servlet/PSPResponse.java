/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.PaymentService;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOPaymentData;

/**
 * Servlet implementation class PSPResponse
 */
@RunAs("OrganizationAdmin")
public class PSPResponse extends HttpServlet {

    @EJB(beanInterface = ConfigurationService.class)
    protected ConfigurationService cfg;

    protected static final String FRONTEND_REQUEST_CANCELLED = "FRONTEND.REQUEST.CANCELLED";
    protected static final String CRITERION_BES_BASE_URL = "CRITERION.BES_BASE_URL";
    protected static final String IDENTIFICATION_UNIQUEID = "IDENTIFICATION.UNIQUEID";
    protected static final String SUCCESS_RESULT = "ACK";
    protected static final String FAILURE_RESULT = "NOK";
    protected static final String PAYMENT_CODE = "PAYMENT.CODE";
    protected static final String CRITERION_USER_LOCALE = "CRITERION.USER_LOCALE";
    protected static final String CRITERION_BES_PAYMENT_INFO_KEY = "CRITERION.BES_PAYMENT_INFO_KEY";
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

    protected PaymentService paymentService;

    private static final long serialVersionUID = 1L;
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(PSPResponse.class);

    public PSPResponse() {
        super();
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // not required
        return;
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        

        try {
            final Properties p = new Properties();
            boolean success = determinePSPParams(request, p);
            String redirectUrl = add(p.getProperty(CRITERION_BES_BASE_URL),
                    "/public/pspregistrationresult.jsf?success=");

            if (Boolean.parseBoolean(p.getProperty(FRONTEND_REQUEST_CANCELLED))) {
                logger.logInfo(
                        Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.INFO_PAYMENT_INFO_REGISTRATION_CANCELLED,
                        p.getProperty(CRITERION_BES_PAYMENT_INFO_KEY),
                        p.getProperty(PROCESSING_TIMESTAMP));
                redirectUrl = add(redirectUrl, "cancelled");
                success = false;
            } else {
                if (success) {
                    final PaymentInfoType paymentOption = getPaymentOptionFromCode(p
                            .getProperty(PAYMENT_CODE));
                    if (paymentOption == null) {
                        logger.logInfo(Log4jLogger.SYSTEM_LOG,
                                LogMessageIdentifier.INFO_PAYMENT_CODE_UNKNOWN,
                                p.getProperty(PAYMENT_CODE));
                        success = false;
                    } else {
                        success = handleSuccess(success, p, paymentOption);
                    }
                }
                redirectUrl = add(redirectUrl, String.valueOf(success));
            }

            writeResponse(response, success, redirectUrl,
                    p.getProperty(CRITERION_USER_LOCALE));
        } catch (Throwable ex) {
            logger.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_PROCESS_PAYMENT_OPERATION);
        }

        
    }

    private boolean validateResponse(Properties p) {
        // if the input could be validated, read the properties
        StringBuffer missingPropKeysCSV = new StringBuffer();
        getProperty(PROCESSING_RESULT, p, missingPropKeysCSV);
        getProperty(PROCESSING_CODE, p, missingPropKeysCSV);
        getProperty(PROCESSING_RETURN, p, missingPropKeysCSV);
        getProperty(PROCESSING_RETURN_CODE, p, missingPropKeysCSV);
        getProperty(PROCESSING_REASON, p, missingPropKeysCSV);
        getProperty(PROCESSING_TIMESTAMP, p, missingPropKeysCSV);
        final String paymentCode = getProperty(PAYMENT_CODE, p,
                missingPropKeysCSV);
        getProperty(CRITERION_BES_PAYMENT_INFO_KEY, p, missingPropKeysCSV);
        getProperty(CRITERION_USER_LOCALE, p, missingPropKeysCSV);
        getProperty(CRITERION_BES_BASE_URL, p, missingPropKeysCSV);

        // if any value is null, this must be considered as an indicator for an
        // incomplete and unsuccessful registration. Log the problem and return
        // the failure site URL (set success to false)
        if (missingPropKeysCSV.length() > 0) {
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_PROCESS_PSP_REQUEST_FAILED_MISSING_PROPERTIES,
                    missingPropKeysCSV.toString(), p.toString());
            return false;
        }

        if (paymentCode == null
                || (!paymentCode.contains(".RG") && !paymentCode
                        .contains(".RR"))) {
            logger.logInfo(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.INFO_PAYMENT_CODE_UNKNOWN, paymentCode);
            return false;
        }
        return true;
    }

    private boolean handleSuccess(boolean success, Properties params,
            PaymentInfoType paymentOption) {
        final String processingResult = params.getProperty(PROCESSING_RESULT);
        final String paymentInfoKey = params
                .getProperty(CRITERION_BES_PAYMENT_INFO_KEY);
        final String processingCode = params.getProperty(PROCESSING_CODE);
        final String processingReturn = params.getProperty(PROCESSING_RETURN);
        final String processingReturnCode = params
                .getProperty(PROCESSING_RETURN_CODE);
        final String processingReason = params.getProperty(PROCESSING_REASON);
        final String processingTime = params.getProperty(PROCESSING_TIMESTAMP);
        if (FAILURE_RESULT.equals(processingResult)) {
            // log a warning, including reason
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_PAYMENT_INFORMATION_WITH_PSP_REGISTRATION_FAILED,
                    paymentInfoKey, processingCode, processingReturn,
                    processingCode, processingReason, processingTime);
            success = false;
        } else if (SUCCESS_RESULT.equals(processingResult)) {
            String identificationId = params
                    .getProperty(IDENTIFICATION_UNIQUEID);
            try {
                VOPaymentData pd = new VOPaymentData();
                pd.setIdentification(identificationId);
                pd.setPaymentInfoKey(Long.parseLong(paymentInfoKey));
                pd.setAccountNumber(params.getProperty(ACCOUNT_NUMBER));
                pd.setProvider(determineProviderName(params, paymentOption));
                paymentService.savePaymentIdentificationForOrganization(pd);
                logger.logInfo(
                        Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.INFO_PAYMENT_INFO_REGISTRATION_SUCCESS,
                        paymentInfoKey, processingCode, processingReturn,
                        processingReturnCode, processingReason, processingTime);
            } catch (ObjectNotFoundException e) {
                // Organization has been removed in the meantime (or
                // wrong data was sent), so log a system exception (as
                // the user cannot do anything)
                success = false;
                logException("The payment info key specified by the PSP call-back '"
                        + paymentInfoKey
                        + "' does not refer to an existing organization.");
            } catch (NumberFormatException e) {
                // user can't repair this, the organization key was sent
                // in a wrong way or not sent at all. So throw a system
                // exception.
                success = false;
                logException("The payment info key key specified by the PSP call-back '"
                        + paymentInfoKey + "' is not a valid long value.");
            } catch (PaymentDataException e) {
                // payment type support has been dropped in the
                // meantime. Unlikely, but user cannot do anything about
                // it...
                success = false;
                logException("The payment type is not supported by provided payment info '"
                        + paymentInfoKey + "' anymore.");
            }
        }
        return success;
    }

    private void writeResponse(HttpServletResponse response, boolean success,
            String redirectUrl, String userLocale) {
        // redirect to success site
        try {
            // if the user locale is known, append it to the redirect URL
            if (userLocale != null) {
                redirectUrl = add(redirectUrl, "&locale=" + userLocale);
            }

            PrintWriter writer = response.getWriter();
            if (writer == null) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_RESPONSE_URL_TO_PSP_FAILED_NOT_WRITTEN_RESPONSE_OBJECT,
                        Boolean.toString(success));
            } else {
                if (redirectUrl != null) {
                    writer.write(redirectUrl);
                } else {
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
    private boolean determinePSPParams(HttpServletRequest request, Properties p) {
        

        try {
            ServletInputStream inputStream = request.getInputStream();
            if (inputStream == null) {
                return false;
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
                p.setProperty(key, value);
            }
            return validateResponse(p);
        } catch (IOException e) {
            // if the request information cannot be read, we cannot determine
            // whether the registration worked or not. Hence we assume it
            // failed, log a warning and return the failure-URL to the PSP.
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_HEIDELPAY_INPUT_PROCESS_FAILED);
        }
        
        return false;
    }

    /**
     * Creates a system exception with the given error message, logs an
     * according entry in the log files. The exception is not thrown, as in the
     * calling context the failure URL has to be returned.
     * 
     * @param errorMessage
     *            The error message to be used.
     */
    private void logException(String errorMessage) {
        SaaSSystemException sse = new SaaSSystemException(errorMessage);
        logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                LogMessageIdentifier.ERROR_EVALUATE_PSP_RESPONSE_FAILED);
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

    /**
     * Appends the operation result to the basic redirect URL. If the basic
     * value is null, null will be returned.
     * 
     * @param sourceString
     *            The base URL.
     * @param success
     *            The result of the operation.
     * @return The URL enhanced by the operation result.
     */
    private String add(String sourceString, String success) {
        if (sourceString == null) {
            return null;
        }
        return sourceString + success;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            Context context = new InitialContext();
            paymentService = PaymentService.class.cast(context
                    .lookup(PaymentService.class.getName()));
        } catch (NamingException e) {
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.ERROR_LOOKUP_ACCOUNTING_SERVICE_FAILED_FOR_HANDLEING_PSP);
            throw new SaaSSystemException(e);
        }
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
