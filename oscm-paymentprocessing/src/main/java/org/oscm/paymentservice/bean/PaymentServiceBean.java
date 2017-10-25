/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.paymentservice.bean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.Query;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.PSP;
import org.oscm.domobjects.PSPSetting;
import org.oscm.domobjects.PSPSettingHistory;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentInfoHistory;
import org.oscm.domobjects.PaymentResult;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PaymentTypeHistory;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.DateFactory;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.paymentservice.data.PaymentHistoryData;
import org.oscm.paymentservice.local.PaymentServiceLocal;
import org.oscm.paymentservice.local.PortLocatorLocal;
import org.oscm.paymentservice.retrieval.PaymentHistoryReader;
import org.oscm.permission.PermissionCheck;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.PaymentProcessingStatus;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validation.PaymentDataValidator;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.internal.intf.PaymentService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PSPCommunicationException;
import org.oscm.internal.types.exception.PSPCommunicationException.Reason;
import org.oscm.internal.types.exception.PSPIdentifierForSellerException;
import org.oscm.internal.types.exception.PSPProcessingException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentDeregistrationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.vo.VOPaymentData;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.psp.data.ChargingData;
import org.oscm.psp.data.ChargingResult;
import org.oscm.psp.data.PriceModelData;
import org.oscm.psp.data.Property;
import org.oscm.psp.data.RegistrationLink;
import org.oscm.psp.data.RequestData;

/**
 * Session Bean implementation class PaymentProcessServices
 */
@Stateless
@Remote(PaymentService.class)
@Local(PaymentServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class PaymentServiceBean implements PaymentService, PaymentServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(PaymentServiceBean.class);

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    protected ConfigurationServiceLocal ic;

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @EJB(beanInterface = ApplicationServiceLocal.class)
    private ApplicationServiceLocal appManager;

    @EJB(beanInterface = PortLocatorLocal.class)
    PortLocatorLocal portLocator;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizer;

    @Resource
    protected SessionContext ctx;

    @Override
    public String determineRegistrationLink(VOPaymentInfo paymentInfo)
            throws PSPCommunicationException, ObjectNotFoundException,
            PaymentDataException, OperationNotPermittedException {

        // check preconditions
        ArgumentValidator.notNull("paymentInfo", paymentInfo);
        PaymentDataValidator
                .validateVOPaymentType(paymentInfo.getPaymentType());
        getValidatedPaymentType(paymentInfo.getPaymentType().getPaymentTypeId());
        PlatformUser currentUser = dm.getCurrentUser();
        PaymentInfo storedPi = dm.find(PaymentInfo.class, paymentInfo.getKey());
        Organization org = currentUser.getOrganization();
        if (storedPi != null) {
            PermissionCheck.owns(storedPi, currentUser.getOrganization(),
                    logger);
        }

        PaymentType paymentType = dm.getReference(PaymentType.class,
                paymentInfo.getPaymentType().getKey());
        RegistrationLink link = null;
        String wsdlUrl = paymentType.getPsp().getWsdlUrl();
        try {
            link = portLocator.getPort(wsdlUrl).determineRegistrationLink(
                    getRequestData(paymentInfo, org, paymentType));
        } catch (Exception ex) {
            PSPCommunicationException pce = new PSPCommunicationException(
                    "determineRegistrationLink failed to invoke implementation",
                    Reason.WEB_SERVICE_CALL_FAILED, ex);
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    pce,
                    LogMessageIdentifier.WARN_PAYMENT_PROCESS_COMMUNICATION_FAILED,
                    wsdlUrl);
            throw pce;
        }

        return link.getUrl();
    }

    private RequestData getRequestData(VOPaymentInfo paymentInfo,
            Organization customerOrg, PaymentType paymentType)
            throws ObjectNotFoundException {
        final RequestData data = new RequestData();

        final PlatformUser currentUser = dm.getCurrentUser();
        data.setCurrentUserLocale(currentUser.getLocale());

        // supplier information
        data.setOrganizationName(customerOrg.getName());
        String email = customerOrg.getEmail();
        if (email == null || email.trim().length() == 0) {
            email = currentUser.getEmail();
        }
        data.setOrganizationEmail(email);
        data.setOrganizationKey(Long.valueOf(customerOrg.getKey()));
        data.setOrganizationId(customerOrg.getOrganizationId());

        // payment info information
        if (paymentInfo.getKey() > 0) {
            final PaymentInfo pi = dm.getReference(PaymentInfo.class,
                    paymentInfo.getKey());
            data.setExternalIdentifier(pi.getExternalIdentifier());
        }
        data.setPaymentInfoKey(Long.valueOf(paymentInfo.getKey()));
        if (paymentInfo.getId() != null) {
            try {
                data.setPaymentInfoId(Base64.encodeBase64String(paymentInfo
                        .getId().getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                throw new SaaSSystemException(
                        "Unexpected base64 encode exception for paymentInfoId "
                                + paymentInfo.getId(), e);
            }
        }
        data.setPaymentTypeKey(Long.valueOf(paymentType.getKey()));

        // payment type information
        data.setPaymentTypeId(paymentType.getPaymentTypeId());

        // psp information
        PSP psp = paymentType.getPsp();
        data.setPspIdentifier(psp.getIdentifier());
        List<Property> settings = new ArrayList<Property>();
        for (PSPSetting setting : psp.getSettings()) {
            settings.add(new Property(setting.getSettingKey(), setting
                    .getSettingValue()));
        }
        data.setProperties(settings);
        return data;
    }

    @Override
    public String determineReregistrationLink(VOPaymentInfo paymentInfo)
            throws PSPCommunicationException, ObjectNotFoundException,
            OperationNotPermittedException, PaymentDataException {

        // check preconditions
        ArgumentValidator.notNull("paymentInfo", paymentInfo);
        PaymentDataValidator
                .validateVOPaymentType(paymentInfo.getPaymentType());
        getValidatedPaymentType(paymentInfo.getPaymentType().getPaymentTypeId());
        PlatformUser currentUser = dm.getCurrentUser();
        PaymentInfo storedPi = dm.getReference(PaymentInfo.class,
                paymentInfo.getKey());
        PermissionCheck.owns(storedPi, currentUser.getOrganization(), logger);

        RegistrationLink link = null;
        String wsdlUrl = storedPi.getPaymentType().getPsp().getWsdlUrl();
        try {
            link = portLocator.getPort(wsdlUrl).determineReregistrationLink(
                    getRequestData(paymentInfo, storedPi.getOrganization(),
                            storedPi.getPaymentType()));
        } catch (Exception ex) {
            PSPCommunicationException pce = new PSPCommunicationException(
                    "determineReregistrationLink failed to invoke implementation",
                    Reason.WEB_SERVICE_CALL_FAILED, ex);
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    pce,
                    LogMessageIdentifier.WARN_PAYMENT_PROCESS_COMMUNICATION_FAILED,
                    wsdlUrl);
            throw pce;
        }

        return link.getUrl();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deregisterPaymentInPSPSystem(PaymentInfo payment)
            throws PaymentDeregistrationException,
            OperationNotPermittedException {

        // check pre-conditions
        if (payment == null
                || payment.getPaymentType().getCollectionType() != PaymentCollectionType.PAYMENT_SERVICE_PROVIDER) {
            return;
        }
        PlatformUser currentUser = dm.getCurrentUser();
        PermissionCheck.owns(payment, currentUser.getOrganization(), logger);

        RequestData data = createRequestData(currentUser.getLocale(), payment);

        deregisterPaymentInfo(payment.getPaymentType().getPsp().getWsdlUrl(),
                data);

    }

    public void deregisterPaymentInfo(String wsdlUrl, RequestData data)
            throws PaymentDeregistrationException {
        Exception occurredException = null;
        LogMessageIdentifier failureMessageId = null;
        String[] failureMessageParam = null;
        try {
            portLocator.getPort(wsdlUrl).deregisterPaymentInformation(data);
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

    /**
     * Retrieves the payment type with the given payment type identifier and
     * validates that this one is marked as supported by the PSP as well as
     * supported by the current customer's supplier.
     * 
     * @param paymentTypeId
     *            Payment type identifier
     * @return The validated payment type.
     * @throws ObjectNotFoundException
     *             Thrown in case the corresponding payment type is not found.
     * @throws PaymentDataException
     *             Thrown in case the payment data could not be validated.
     */
    private PaymentType getValidatedPaymentType(String paymentTypeId)
            throws ObjectNotFoundException, PaymentDataException {

        PaymentType pt = new PaymentType();
        if (paymentTypeId != null) {
            pt.setPaymentTypeId(paymentTypeId);
            pt = (PaymentType) dm.getReferenceByBusinessKey(pt);
        }
        PaymentDataValidator.validatePaymentTypeHandledByPSP(pt);

        return pt;
    }

    // ****************************************************************************************
    // Methods defined in the local interface
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean chargeForOutstandingBills() {

        boolean isPSPUsageEnabled = Boolean.parseBoolean(ic
                .getConfigurationSetting(ConfigurationKey.PSP_USAGE_ENABLED,
                        Configuration.GLOBAL_CONTEXT).getValue());
        if (isPSPUsageEnabled) {
            boolean result = true;
            // determine billing results without existing payment result
            Query query = dm
                    .createNamedQuery("BillingResult.getOutstandingBillingResults");
            List<BillingResult> brs = ParameterizedTypes.list(
                    query.getResultList(), BillingResult.class);
            for (BillingResult br : brs) {
                if (!prepareForNewTransaction().chargeCustomer(br)) {
                    result = false;
                }
            }
            return result;
        }

        return true;
    }

    private PaymentServiceLocal prepareForNewTransaction() {
        DateFactory.getInstance().takeCurrentTime();
        return ctx.getBusinessObject(PaymentServiceLocal.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean chargeCustomer(BillingResult billingResult) {

        boolean isPSPUsageEnabled = Boolean.parseBoolean(ic
                .getConfigurationSetting(ConfigurationKey.PSP_USAGE_ENABLED,
                        Configuration.GLOBAL_CONTEXT).getValue());
        boolean paymentResultUpdateNeeded = false;
        if (isPSPUsageEnabled) {
            try {
                paymentResultUpdateNeeded = chargeCustomerInternal(billingResult);
            } catch (Throwable e) {
                // no exception must be passed to the caller, as this would
                // cause a rollback of the timers
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_PROCESS_PAYMENT_OPERATION);
                // in case of error, store the new payment result containing the
                // exception
                if (!isTransactionRolledBack(e)) {
                    paymentResultUpdateNeeded = true;
                }
                return false;
            } finally {
                try {
                    PaymentResult pr = billingResult.getPaymentResult();
                    if (paymentResultUpdateNeeded) {
                        // as the billing result was not modified, JPA will not
                        // store the payment result due to any cascade setting.
                        // So we store it if a payment result object exists.
                        if (pr != null) {
                            if (!(dm.find(PaymentResult.class, pr.getKey()) != null)) {
                                dm.persist(pr);
                            }
                        }
                    } else if (pr != null && pr.getKey() == 0) {
                        // reset the payment result reference to null, if a new
                        // object was created
                        billingResult.setPaymentResult(null);
                    }
                } catch (Throwable e) {
                    logger.logError(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.ERROR_STORE_PAYMENT_RESULT_FAILED);
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isTransactionRolledBack(Throwable th) {
        Throwable cause = th.getCause();
        while (cause != null && !cause.equals(th)) {
            if (cause instanceof EJBTransactionRolledbackException) {
                return true;
            }
            return isTransactionRolledBack(cause);
        }
        return false;
    }

    /**
     * Charges the customer by sending the appropriate charging request to the
     * PSP system.
     * 
     * @param billingResult
     *            The billing result object containing the details for the debit
     *            operation.
     * @return <code>true</code> in case the billing result was completely
     *         handled and the payment result object related to it was updated,
     *         <code>false</code> otherwise.
     * @throws PSPCommunicationException
     * @throws PSPProcessingException
     */
    private boolean chargeCustomerInternal(BillingResult billingResult)
            throws PSPCommunicationException, PSPProcessingException {

        // if there is no billing result data at all (nothing to charge the
        // customer for), return doing nothing
        if (billingResult.getResultXML().trim().equals("")) {
            return false;
        }

        PaymentResult paymentResult = billingResult.getPaymentResult();
        boolean paymentResultChanged = true;
        OrganizationHistory customer = null;
        long customerKey = 0;
        ChargingResult response = null;

        try {
            if (paymentResult == null) {
                // if there is no payment result for the billing object, create
                // a new one
                paymentResult = new PaymentResult();
                paymentResult.setBillingResult(billingResult);
            } else {
                // if the billing result object was already successfully
                // processed, no further handling is required
                if (paymentResult.getProcessingStatus() == PaymentProcessingStatus.SUCCESS) {
                    paymentResultChanged = false;
                    return false;
                }
                // otherwise clear the settings on exception and response
                paymentResult.setProcessingResult((String) null);
                paymentResult.setProcessingException((String) null);
            }
            paymentResult.setProcessingTime(System.currentTimeMillis());

            // validate subscription
            Long subscriptionKey = billingResult.getSubscriptionKey();
            if (subscriptionKey == null) {
                PSPProcessingException e = new PSPProcessingException(
                        "Payment processing for billing result "
                                + billingResult.getKey()
                                + " aborted, as no subscription key is available");
                throw e;
            }

            // determine supplier history data
            Long supplierKey = Long.valueOf(billingResult.getChargingOrgKey());
            OrganizationHistory supplierHistory = getSupplierHistoryForObjectKey(supplierKey);

            // determine customer history data
            customer = getCustomerHistory(billingResult.getOrganizationTKey());
            customerKey = customer.getKey();

            // determine payment history data
            PaymentHistoryData phd = new PaymentHistoryReader(dm)
                    .getPaymentHistory(billingResult.getSubscriptionKey()
                            .longValue());

            // don't perform payment tasks if payment type is not set
            if (phd.getPaymentTypeHistory() == null
                    || phd.getPaymentTypeHistory().getDataContainer()
                            .getCollectionType() != PaymentCollectionType.PAYMENT_SERVICE_PROVIDER) {
                paymentResultChanged = false;
                return false;
            }

            // don't perform payment tasks if no costs have been generated.
            BigDecimal overallCosts = billingResult.getGrossAmount();
            if (overallCosts == null || overallCosts.doubleValue() <= 0.0) {
                paymentResultChanged = false;
                return false;
            }

            // CHARGE the customer
            RequestData requestData = getRequestDataForCharging(phd,
                    supplierHistory);
            ChargingData chargingData = getChargingData(billingResult,
                    phd.getPaymentInfoHistory());
            final String wsdl = phd.getPspHistory().getWsdlUrl();
            response = portLocator.getPort(wsdl).charge(requestData,
                    chargingData);

            if (response != null) {
                paymentResult.setProcessingResult(response
                        .getProcessingResult());
            } else {
                paymentResult
                        .setProcessingResult("NO RESPONSE RETRIEVED FROM PSP "
                                + wsdl);
            }
        } catch (IOException e) {
            handleExceptionAndMarkForRetry(paymentResult, e);
        } catch (org.oscm.types.exceptions.PSPCommunicationException e) {
            paymentResult.setProcessingException(e);
            paymentResult.setProcessingStatus(PaymentProcessingStatus.RETRY);
            throw ExceptionConverter.convertToUp(e);
        } catch (PSPProcessingException e) {
            paymentResult.setProcessingException(e);
            paymentResult
                    .setProcessingStatus(PaymentProcessingStatus.FAILED_INTERNAL);
            throw e;
        } catch (Exception e) {
            PSPProcessingException ppe = new PSPProcessingException(
                    "Debiting the customer '" + customerKey + "' failed.", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, ppe,
                    LogMessageIdentifier.ERROR_PROCESS_CHARGING_FAILED);

            paymentResult.setProcessingException(ppe);
            paymentResult
                    .setProcessingStatus(PaymentProcessingStatus.FAILED_INTERNAL);
            throw ppe;
        } finally {
            if (paymentResultChanged) {
                billingResult.setPaymentResult(paymentResult);
            }
        }

        // if there was no exception, check the response from the PSP
        if (response == null || "error".equals(response.getProcessingResult())) {
            PSPProcessingException ppe = new PSPProcessingException(
                    "Processing failed as PSP returned failure during processing. Debiting customer '"
                            + customerKey + "' failed.");
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ppe,
                    LogMessageIdentifier.WARN_CHARGING_CUSTOMER_FAILED,
                    Long.toString(customerKey));
            paymentResult.setProcessingException(ppe);
            paymentResult
                    .setProcessingStatus(PaymentProcessingStatus.FAILED_EXTERNAL);
            throw ppe;
        }

        // as there was no problem, set the status to success
        paymentResult.setProcessingStatus(PaymentProcessingStatus.SUCCESS);

        return true;
    }

    private RequestData getRequestDataForCharging(PaymentHistoryData phd,
            OrganizationHistory supplier)
            throws PSPIdentifierForSellerException {

        final RequestData requestData = new RequestData();
        requestData.setCurrentUserLocale(Locale.ENGLISH.getLanguage());

        // supplier information
        requestData.setOrganizationName(supplier.getOrganizationName());
        requestData.setOrganizationEmail(supplier.getEmail());
        requestData.setOrganizationKey(Long.valueOf(supplier.getObjKey()));
        requestData.setOrganizationId(supplier.getOrganizationId());

        // payment info
        PaymentInfoHistory paymentInfoHistory = phd.getPaymentInfoHistory();
        requestData.setExternalIdentifier(paymentInfoHistory
                .getExternalIdentifier());
        requestData.setPaymentInfoKey(Long.valueOf(paymentInfoHistory
                .getObjKey()));
        requestData.setPaymentTypeKey(Long.valueOf(paymentInfoHistory
                .getPaymentTypeObjKey()));
        requestData.setPaymentInfoId(paymentInfoHistory.getPaymentInfoId());

        // payment type
        PaymentTypeHistory paymentTypeHistory = phd.getPaymentTypeHistory();
        requestData.setPaymentTypeId(paymentTypeHistory.getDataContainer()
                .getPaymentTypeId());

        // psp settings
        requestData.setPspIdentifier(getPspIdentifier(phd));
        List<Property> settings = new ArrayList<Property>();
        for (PSPSettingHistory setting : phd.getPspSettingsHistory()) {
            settings.add(new Property(setting.getSettingKey(), setting
                    .getSettingValue()));
        }
        requestData.setProperties(settings);

        return requestData;
    }

    private String getPspIdentifier(PaymentHistoryData phd)
            throws PSPIdentifierForSellerException {
        if (phd.getPspAccountHistory() == null
                || phd.getPspAccountHistory().getPspIdentifier() == null) {
            // charging cannot succeed, so mark the operation as failed by
            // throwing an exception
            PSPIdentifierForSellerException mpi = new PSPIdentifierForSellerException(
                    "PSP with key=" + phd.getPspHistory().getObjKey()
                            + " is missing the psp identifier");
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    mpi,
                    LogMessageIdentifier.WARN_DEBIT_FAILED_NO_PSPIDENTIFIER_SETTING);
            throw mpi;
        }
        return phd.getPspAccountHistory().getPspIdentifier();
    }

    private ChargingData getChargingData(BillingResult billingResult,
            PaymentInfoHistory paymentInfoHistory)
            throws XPathExpressionException, ParserConfigurationException,
            SAXException, IOException, ParseException, ObjectNotFoundException {

        // create document from billing result xml
        Document brDocument = XMLConverter.convertToDocument(
                billingResult.getResultXML(), true);

        // create the charging data
        long customerKey = billingResult.getOrganizationTKey();
        ChargingData chargingData = new ChargingData();
        chargingData.setTransactionId(billingResult.getKey());
        chargingData.setAddress(getAddress(brDocument));
        chargingData.setCurrency(billingResult.getCurrencyCode());
        chargingData.setCustomerKey(Long.valueOf(customerKey));
        chargingData.setEmail(getCustomersCurrentEmailAddress(customerKey));
        chargingData.setExternalIdentifier(paymentInfoHistory
                .getExternalIdentifier());
        chargingData.setNetAmount(getNetAmount(brDocument));
        chargingData.setNetDiscount(billingResult.getNetDiscount());
        chargingData.setGrossAmount(billingResult.getGrossAmount());
        chargingData
                .setPeriodEndTime(new Date(billingResult.getPeriodEndTime()));
        chargingData.setPeriodStartTime(new Date(billingResult
                .getPeriodStartTime()));
        chargingData.setSellerKey(new Long(billingResult.getChargingOrgKey()));
        chargingData.setVatAmount(billingResult.getVATAmount());
        chargingData.setVat(billingResult.getVAT());
        chargingData.setSubscriptionId(getSubscriptionId(brDocument));
        chargingData.setPon(getPurchaseOrderNumber(brDocument));

        NodeList nl = XMLConverter
                .getNodeListByXPath(brDocument,
                        "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel");
        for (int i = 0; i < nl.getLength(); i++) {
            Node priceModel = nl.item(i);
            PriceModelData pmd = new PriceModelData();
            pmd.setPosition(i);
            String pmId = XMLConverter.getStringAttValue(priceModel, "id");

            // start- and end-date
            Node usagePeriod = XMLConverter.getNodeByXPath(brDocument,
                    "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel[@id='"
                            + pmId + "']/UsagePeriod");
            pmd.setStartDate(Long.valueOf(
                    XMLConverter.getStringAttValue(usagePeriod, "startDate"))
                    .longValue());
            pmd.setEndDate(Long.valueOf(
                    XMLConverter.getStringAttValue(usagePeriod, "endDate"))
                    .longValue());

            // gross amount
            Node node = XMLConverter.getNodeByXPath(brDocument,
                    "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel[@id='"
                            + pmId + "']/PriceModelCosts");
            BigDecimal netAmount = XMLConverter.getBigDecimalAttValue(node,
                    "amount");
            pmd.setNetAmount(netAmount);

            chargingData.getPriceModelData().add(pmd);
        }

        return chargingData;
    }

    private String getCustomersCurrentEmailAddress(long customerKey)
            throws ObjectNotFoundException {
        Organization customerOrg = dm.getReference(Organization.class,
                customerKey);
        return customerOrg.getEmail();
    }

    private BigDecimal getNetAmount(Document brDocument)
            throws XPathExpressionException, ParseException {
        PriceConverter parser = new PriceConverter(Locale.ENGLISH);
        String netAmount = XMLConverter.getNodeTextContentByXPath(brDocument,
                "/BillingDetails/OverallCosts/netAmount");
        return parser.parse(netAmount);
    }

    /**
     * Reads the customer's billing address from the billing result xml.
     * 
     * @param brDocument
     * @return the customer's billing address
     * @throws XPathExpressionException
     */
    private String getAddress(Document brDocument)
            throws XPathExpressionException {
        return XMLConverter.getNodeTextContentByXPath(brDocument,
                "/BillingDetails/OrganizationDetails/Address");
    }

    /**
     * Reads the subscription's id from the billing result xml.
     * 
     * @param brDocument
     * @return
     * @throws XPathExpressionException
     */
    private String getSubscriptionId(Document brDocument)
            throws XPathExpressionException {
        return XMLConverter.getNodeTextContentByXPath(brDocument,
                "/BillingDetails/Subscriptions/Subscription/@id");
    }

    /**
     * Reads the subscription's purchase order number from the billing result
     * xml.
     * 
     * @param brDocument
     * @return
     * @throws XPathExpressionException
     */
    private String getPurchaseOrderNumber(Document brDocument)
            throws XPathExpressionException {
        return XMLConverter
                .getNodeTextContentByXPath(brDocument,
                        "/BillingDetails/Subscriptions/Subscription/@purchaseOrderNumber");
    }

    /**
     * Logs the specified exception that is meant to require a later retry of
     * the payment processing. The payment result object will be updated
     * accordingly, finally a communication exception wrapping the specified
     * exception will be thrown.
     * 
     * @param pr
     *            The payment result to be updated.
     * @param e
     *            The caught exception.
     * @throws PSPCommunicationException
     */
    private void handleExceptionAndMarkForRetry(PaymentResult pr, IOException e)
            throws PSPCommunicationException {
        PSPCommunicationException pce = new PSPCommunicationException(
                "Debit request could not be sent to the payment service provider successfully",
                Reason.DEBIT_INVOCATION_FAILED, e);
        logger.logWarn(Log4jLogger.SYSTEM_LOG, pce,
                LogMessageIdentifier.WARN_CHARGING_PROCESS_FAILED);
        pr.setProcessingException(pce);
        pr.setProcessingStatus(PaymentProcessingStatus.RETRY);
        throw pce;
    }

    /**
     * Reads the organization key information from the billing result and
     * determines the latest history entry for the related customer. Finally it
     * returns this entry.
     * 
     * @param organizationTKey
     *            The key value of the organization.
     * @return The history of the customer affected by the billing result.
     * @throws PSPProcessingException
     *             Thrown in case no data is found for the organization refered
     *             in the billing result document.
     */
    private OrganizationHistory getCustomerHistory(long organizationTKey)
            throws PSPProcessingException {

        Query query = dm.createNamedQuery("OrganizationHistory.getByTKeyDesc");
        query.setParameter("objKey", Long.valueOf(organizationTKey));
        List<OrganizationHistory> historyEntriesForCustomer = ParameterizedTypes
                .list(query.getResultList(), OrganizationHistory.class);
        if (historyEntriesForCustomer == null
                || historyEntriesForCustomer.isEmpty()) {
            PSPProcessingException ppe = new PSPProcessingException(
                    "No organization history data found for object key '"
                            + organizationTKey + "'!");
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    ppe,
                    LogMessageIdentifier.WARN_INVALID_PAYMENT_PROCESSING_NO_RELATED_ORGANIZATION_DATA);
            throw ppe;
        }
        OrganizationHistory customer = historyEntriesForCustomer.get(0);

        return customer;
    }

    /**
     * Retrieves the latest history entry for the organization with the given
     * object key. If the corresponding supplier cannot be found, an exception
     * is thrown.
     * 
     * @param supplierObjKey
     *            The key for the supplier organization history entry to be
     *            returned.
     * @return The latest history entry for the supplier.
     */
    private OrganizationHistory getSupplierHistoryForObjectKey(
            Long supplierObjKey) {
        Query query = dm.createNamedQuery("OrganizationHistory.getByTKeyDesc");
        query.setParameter("objKey", supplierObjKey);
        List<OrganizationHistory> supHists = ParameterizedTypes.list(
                query.getResultList(), OrganizationHistory.class);
        if (supHists.isEmpty()) {
            SaaSSystemException sse = new SaaSSystemException(
                    "History data for supplier '"
                            + supplierObjKey
                            + "' not found. Payment processing cannot continue!");
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sse,
                    LogMessageIdentifier.ERROR_DATA_MISSING_TO_COMPLETE_PAYMENT_PROCESS);
            throw sse;
        }
        return supHists.get(0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean reinvokePaymentProcessing() {
        List<PaymentResult> forRetry = getPaymentResultsForRetry();
        boolean result = true;
        for (PaymentResult retryPR : forRetry) {
            result = result && chargeCustomer(retryPR.getBillingResult());
        }
        return result;
    }

    /**
     * Reads all payment result objects from the database that are marked to be
     * retried and returns them.
     * 
     * @return The payment results indicating that the related payment process
     *         failed and has to be retried.
     */
    private List<PaymentResult> getPaymentResultsForRetry() {

        Query query = dm.createNamedQuery("PaymentResult.getAllByStatus");
        query.setParameter("status", PaymentProcessingStatus.RETRY);
        List<PaymentResult> result = ParameterizedTypes.list(
                query.getResultList(), PaymentResult.class);

        return result;
    }

    @Override
    public void savePaymentIdentificationForOrganization(
            VOPaymentData paymentData) throws ObjectNotFoundException,
            PaymentDataException {

        ArgumentValidator.notNull("paymentData", paymentData);
        String identification = paymentData.getIdentification();
        ArgumentValidator.notNull("paymentData.identification", identification);
        PaymentInfo usedPayment = paymentData.getPaymentInfoKey() > 0 ? dm
                .getReference(PaymentInfo.class,
                        paymentData.getPaymentInfoKey()) : new PaymentInfo(
                DateFactory.getInstance().getTransactionTime());

        usedPayment.setPaymentType(dm.getReference(PaymentType.class,
                paymentData.getPaymentTypeKey()));
        usedPayment.setOrganization_tkey(paymentData.getOrganizationKey());
        usedPayment.setPaymentInfoId(paymentData.getPaymentInfoId());

        PaymentDataValidator.validatePaymentTypeHandledByPSP(usedPayment
                .getPaymentType());

        Organization organization = dm.getReference(Organization.class,
                paymentData.getOrganizationKey());

        usedPayment.setExternalIdentifier(identification);
        usedPayment.setAccountNumber(paymentData.getAccountNumber());
        usedPayment.setProviderName(paymentData.getProvider());
        usedPayment.setOrganization(organization);

        for (int i = 1; i <= 10; i++) {
            try {
                dm.persist(usedPayment);
                break;
            } catch (NonUniqueBusinessKeyException e) {
                if (i == 10) {
                    SaaSSystemException sse = new SaaSSystemException(
                            "Caught NonUniqueBusinessKeyException although there is no business key",
                            e);
                    logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                            LogMessageIdentifier.ERROR_UNEXPECTED_BK_VIOLATION);
                    throw sse;
                }
                // try until we have an unique key
                usedPayment.setPaymentInfoId(paymentData.getPaymentInfoId()
                        + '_' + i);
            }
        }
        if (paymentData.getPaymentInfoKey() > 0) {
            activateSuspendedSubscriptions(organization, usedPayment);
        }

    }

    /**
     * Activates suspended subscriptions of the provided customer. Must be
     * called in Case the customer enters a new valid payment info or the
     * customer has a payment info that will be enabled by the supplier (e. g.
     * after disabling it).
     * 
     * @param cust
     *            the organization to get the subscriptions for
     */
    private void activateSuspendedSubscriptions(Organization cust,
            PaymentInfo usedPayment) {
        List<Subscription> subscriptions = cust.getSubscriptions();
        for (Subscription subscription : subscriptions) {
            final SubscriptionStatus current = subscription.getStatus();
            if (current.isSuspendedOrSuspendedUpd()) {
                if (subscription.getPaymentInfo() != null) {

                    if (subscription.getPaymentInfo().getKey() == usedPayment
                            .getKey()) {
                       
                        subscription.setStatus(current.getNextForPaymentTypeRevoked());
                        // call service to activate instance
                        try {
                            appManager.activateInstance(subscription);
                        } catch (TechnicalServiceNotAliveException e) {
                            logger.logError(
                                    Log4jLogger.SYSTEM_LOG,
                                    e,
                                    LogMessageIdentifier.ERROR_ACTIVATE_INSTANCE);
                        } catch (TechnicalServiceOperationException e) {
                            logger.logError(
                                    Log4jLogger.SYSTEM_LOG,
                                    e,
                                    LogMessageIdentifier.ERROR_ACTIVATE_INSTANCE);
                        }
                    }
                }                
            }
        }
    }

    public RequestData createRequestData(String locale, PaymentInfo pi) {
        final RequestData data = new RequestData();
        data.setCurrentUserLocale(locale);
        data.setOrganizationName(pi.getOrganization().getName());
        data.setOrganizationEmail(pi.getOrganization().getEmail());
        data.setOrganizationKey(Long.valueOf(pi.getOrganization().getKey()));
        data.setOrganizationId(pi.getOrganization().getOrganizationId());
        data.setExternalIdentifier(pi.getExternalIdentifier());
        data.setPaymentInfoKey(Long.valueOf(pi.getKey()));
        try {
            data.setPaymentInfoId(Base64.encodeBase64String(pi
                    .getPaymentInfoId().getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new SaaSSystemException(
                    "Unexpected base64 encode exception for paymentInfoId "
                            + pi.getPaymentInfoId(), e);
        }
        // payment type information
        data.setPaymentTypeKey(Long.valueOf(pi.getPaymentType().getKey()));
        data.setPaymentTypeId(pi.getPaymentType().getPaymentTypeId());

        // psp information
        PSP psp = pi.getPaymentType().getPsp();
        data.setPspIdentifier(psp.getIdentifier());
        List<Property> settings = new ArrayList<Property>();
        for (PSPSetting setting : psp.getSettings()) {
            settings.add(new Property(setting.getSettingKey(), setting
                    .getSettingValue()));
        }
        data.setProperties(settings);
        return data;
    }

}
