/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 03.07.15 11:22
 *
 *******************************************************************************/

package org.oscm.billingservice.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.accountservice.dao.UserLicenseDao;
import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorLocal;
import org.oscm.billingservice.business.calculation.share.SharesCalculatorLocal;
import org.oscm.billingservice.business.model.brokershare.BrokerRevenueShareResult;
import org.oscm.billingservice.business.model.mpownershare.MarketplaceOwnerRevenueShareResult;
import org.oscm.billingservice.business.model.resellershare.ResellerRevenueShareResult;
import org.oscm.billingservice.business.model.suppliershare.SupplierRevenueShareResult;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceLocal;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.billingservice.service.model.BillingPeriodData;
import org.oscm.billingservice.service.model.BillingRun;
import org.oscm.billingservice.service.model.BillingSubscriptionChunk;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.BillingSharesResult;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.interceptor.DateFactory;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.string.Strings;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validation.Invariants;
import org.oscm.validator.BLValidator;
import org.oscm.internal.intf.BillingService;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Session Bean implementation class BillingServiceBean
 */
@Stateless
@Remote(BillingService.class)
@Local(BillingServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class BillingServiceBean implements BillingService, BillingServiceLocal {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(BillingServiceBean.class);

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    protected ConfigurationServiceLocal cfgMgmt;

    @EJB(beanInterface = TriggerQueueServiceLocal.class)
    protected TriggerQueueServiceLocal triggerQS;

    @EJB(beanInterface = RevenueCalculatorLocal.class)
    protected RevenueCalculatorLocal revenueCalculator;

    @EJB(beanInterface = SharesCalculatorLocal.class)
    protected SharesCalculatorLocal sharesCalculator;

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = BillingDataRetrievalServiceLocal.class)
    BillingDataRetrievalServiceLocal bdr;

    @EJB(beanInterface = SharesDataRetrievalServiceLocal.class)
    SharesDataRetrievalServiceLocal sdr;

    @EJB
    private CommunicationServiceLocal commService;

    @EJB(beanInterface = UserLicenseDao.class)
    private UserLicenseDao userLicenseDao;

    @Override
    public boolean startBillingRun(long currentTime) {
        LOGGER.logInfo(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.INFO_USER_BILLING_RUN_STARTED);

        boolean success = true;
        Set<Long> failedSubscriptions = new HashSet<>();

        DataProviderTimerBased billingRunProvider = getBillingRunProvider(currentTime);
        List<BillingPeriodData> billingPeriodList = billingRunProvider
                .loadBillingData();

        for (BillingPeriodData billingPeriodData : billingPeriodList) {
            BillingSubscriptionChunk billingSubscriptionChunk = billingRunProvider
                    .getSubscriptionHistories(billingPeriodData, dm);

            BillingRun result = executeBilling(billingSubscriptionChunk,
                    failedSubscriptions);
            success = success && result.isSuccessful();

            List<TriggerMessage> messages = createTriggerMessagesForAllCustomers(result);
            triggerQS.sendAllNonSuspendingMessages(messages,
                    dm.getCurrentUserIfPresent());
        }

        if (!failedSubscriptions.isEmpty()) {
            sendEmailAboutFailures(currentTime);
        }
        success = success && performShareCalculationRun(billingRunProvider);
        LOGGER.logInfo(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.INFO_USER_BILLING_RUN_FINISHED);
        return success;
    }

    DataProviderTimerBased getBillingRunProvider(long currentTime) {
        return new DataProviderTimerBased(currentTime,
                cfgMgmt.getBillingRunOffsetInMs(), bdr);
    }

    private void sendEmailAboutFailures(long billingStartTime) {
        Object[] params = new Object[4];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        Date date = new Date(billingStartTime);
        params[0] = simpleDateFormat.format(date);
        for (PlatformUser platformUser : userLicenseDao.getPlatformOperators()) {
            try {
                params[3] = platformUser.getLastName();
                commService.sendMail(platformUser, EmailType.BILLING_FAILED,
                        params, null);
            } catch (MailOperationException exc) {
                LOGGER.logError(Log4jLogger.SYSTEM_LOG, exc,
                        LogMessageIdentifier.ERROR);
            }
        }
    }

    boolean performShareCalculationRun(DataProviderTimerBased billingRunProvider) {
        boolean success = sharesCalculator.performBrokerSharesCalculationRun(
                billingRunProvider.getPeriodRevenueSharesStart(),
                billingRunProvider.getPeriodRevenueSharesEnd());
        success = success
                && sharesCalculator.performMarketplacesSharesCalculationRun(
                        billingRunProvider.getPeriodRevenueSharesStart(),
                        billingRunProvider.getPeriodRevenueSharesEnd());
        success = success
                && sharesCalculator.performResellerSharesCalculationRun(
                        billingRunProvider.getPeriodRevenueSharesStart(),
                        billingRunProvider.getPeriodRevenueSharesEnd());
        success = success
                && sharesCalculator.performSupplierSharesCalculationRun(
                        billingRunProvider.getPeriodRevenueSharesStart(),
                        billingRunProvider.getPeriodRevenueSharesEnd());
        return success;
    }

    List<TriggerMessage> createTriggerMessagesForAllCustomers(
            BillingRun resultBillingRun) {

        List<TriggerMessage> messages = new ArrayList<>();

        if (resultBillingRun != null
                && resultBillingRun.getBillingResultList() != null) {
            for (BillingResult resultForCustomer : resultBillingRun
                    .getBillingResultList()) {
                List<TriggerProcessParameter> list = new ArrayList<>();
                TriggerProcessParameter tpp = new TriggerProcessParameter();
                tpp.setName(TriggerProcessParameterName.XML_BILLING_DATA);
                tpp.setSerializedValue(resultForCustomer.getResultXML());
                list.add(tpp);

                Organization customer = dm.find(Organization.class,
                        resultForCustomer.getOrganizationTKey());
                List<Organization> receiverOrgs = new ArrayList<>();
                if (customer != null) {
                    receiverOrgs.add(customer);
                    List<Organization> suppliers = customer
                            .getSuppliersOfCustomer();
                    if (suppliers != null && !suppliers.isEmpty()) {
                        receiverOrgs.addAll(suppliers);
                    }
                }

                messages.add(new TriggerMessage(TriggerType.START_BILLING_RUN,
                        list, receiverOrgs));
            }
        }

        return messages;
    }

    /**
     * Execute the billing calculation for a chunk of subscriptions in a given
     * billing period. Dont't perform the calculation for subscriptions, where a
     * billing calculation failed.
     * 
     * @param billingSubscriptionChunk
     *            a chunk of subscriptions to be billed
     * @return an object with the calculated billing results
     */
    BillingRun executeBilling(
            BillingSubscriptionChunk billingSubscriptionChunk,
            Set<Long> failedSubscriptions) {
        BillingRun result = new BillingRun(
                billingSubscriptionChunk.getBillingPeriodStart(),
                billingSubscriptionChunk.getBillingPeriodEnd());

        for (BillingInput billingInput : billingSubscriptionChunk
                .getBillingInputList()) {

            Long subscriptionKey = Long.valueOf(billingInput
                    .getSubscriptionKey());

            if (!failedSubscriptions.contains(subscriptionKey)) {
                try {
                    BillingResult bill = revenueCalculator
                            .performBillingRunForSubscription(billingInput);

                    if (!Strings.isEmpty(bill.getResultXML())) {
                        result.addBillingResult(bill);
                    }
                } catch (Exception e) {
                    failedSubscriptions.add(subscriptionKey);
                    result.setSuccessful(false);
                    logBillingRunFailed(e, billingInput);
                }
            }
        }

        return result;
    }

    /**
     * Execute the billing calculation for the payment preview report or for the
     * export of billing data.
     */
    BillingRun executeBilling(DataProvider dataProvider) {
        BillingRun result = new BillingRun(dataProvider.getPeriodStart(),
                dataProvider.getPeriodEnd());

        for (BillingInput billingInput : dataProvider.getBillingInput()) {

            try {
                BillingResult bill = revenueCalculator
                        .performBillingRunForSubscription(billingInput);

                if (!Strings.isEmpty(bill.getResultXML())) {
                    result.addBillingResult(bill);
                }
            } catch (Exception e) {
                logBillingRunFailed(e, billingInput);
                result.clearBillingResults();
                result.setSuccessful(false);
                return result;
            }
        }

        return result;
    }

    private void logBillingRunFailed(Exception e, BillingInput billingInput) {
        long billingPeriodStart = billingInput.getBillingPeriodStart();
        long billingPeriodEnd = billingInput.getBillingPeriodEnd();
        Date startDate = new Date(billingPeriodStart);
        Date endDate = new Date(billingPeriodEnd);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        LOGGER.logError(
                Log4jLogger.SYSTEM_LOG,
                e,
                LogMessageIdentifier.ERROR_BILLING_RUN_FAILED_NO_BILL_GENERATED,
                String.valueOf(billingInput.getSubscriptionKey()),
                String.valueOf(billingInput.getOrganizationKey()),
                String.valueOf(simpleDateFormat.format(startDate)),
                String.valueOf(simpleDateFormat.format(endDate)));
    }

    @Override
    public List<BillingResult> generateBillingForAnyPeriod(long start,
            long end, long organizationKey) throws BillingRunFailed {
        DataProviderAnyPeriod billingAnyPeriodProvider = new DataProviderAnyPeriod(
                bdr, start, end, organizationKey, null, false, dm);
        BillingRun billingRun = executeBilling(billingAnyPeriodProvider);
        return billingRun != null ? billingRun.getBillingResultList()
                : new ArrayList<BillingResult>();
    }

    @Override
    public BillingRun generatePaymentPreviewReport(long organizationKey)
            throws BillingRunFailed {
        final long periodEnd = DateFactory.getInstance().getTransactionTime();
        final long periodStart = determinePeriodStartForPaymentPreview(periodEnd);

        return executeBilling(new DataProviderAnyPeriod(bdr, periodStart,
                periodEnd, organizationKey, null, true, dm));
    }

    @Override
    public BillingRun generatePaymentPreviewReport(long organizationKey,
            List<Long> unitKeys) throws BillingRunFailed {
        final long periodEnd = DateFactory.getInstance().getTransactionTime();
        final long periodStart = determinePeriodStartForPaymentPreview(periodEnd);

        return executeBilling(new DataProviderAnyPeriod(bdr, periodStart,
                periodEnd, organizationKey, unitKeys, true, dm));
    }

    private long determinePeriodStartForPaymentPreview(final long periodEnd) {
        Calendar periodStart = Calendar.getInstance();
        periodStart.setTimeInMillis(periodEnd);
        periodStart.set(Calendar.DAY_OF_MONTH, 1);
        periodStart.set(Calendar.HOUR_OF_DAY, 0);
        periodStart.set(Calendar.MINUTE, 0);
        periodStart.set(Calendar.SECOND, 0);
        periodStart.set(Calendar.MILLISECOND, 0);
        return periodStart.getTimeInMillis();
    }

    boolean orgHasChargeableSubscriptions(Long organizationKey) {
        Query priceModelQuery = dm
                .createNamedQuery("PriceModelHistory.getPriceModelForOrganizationKey");
        priceModelQuery.setParameter("organizationKey", organizationKey);
        return ((Long) priceModelQuery.getSingleResult()).longValue() != 0;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public byte[] getCustomerBillingData(Long pFrom, Long pTo,
            List<String> customerIds) throws OrganizationAuthoritiesException {
        Organization sellerOrg = dm.getCurrentUser().getOrganization();
        Set<OrganizationRoleType> orgRoles = sellerOrg.getGrantedRoleTypes();

        Query query;
        if (customerIds == null || customerIds.isEmpty()) {
            query = dm.createNamedQuery("BillingResult.getForAllCustomers");
        } else {
            query = dm.createNamedQuery("BillingResult.getForCustomers");
            query.setParameter("customerIdList", customerIds);
        }
        query.setParameter("seller", sellerOrg);
        if (orgRoles.contains(OrganizationRoleType.RESELLER)) {
            query.setParameter("orgreftype",
                    OrganizationReferenceType.RESELLER_TO_CUSTOMER);
        } else if (orgRoles.contains(OrganizationRoleType.SUPPLIER)) {
            query.setParameter("orgreftype",
                    OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        } else {
            OrganizationAuthoritiesException e = new OrganizationAuthoritiesException();
            LOGGER.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORGANIZATION_ROLE_MISSING,
                    OrganizationRoleType.SUPPLIER.toString() + " or "
                            + OrganizationRoleType.RESELLER.toString());
            throw e;
        }

        query.setParameter("fromDate", pFrom == null ? Long.valueOf(0) : pFrom);
        query.setParameter("toDate", pTo == null ? Long.valueOf(Long.MAX_VALUE)
                : pTo);

        final List<String> fragments = new ArrayList<>();
        for (BillingResult billingResult : ParameterizedTypes.iterable(
                query.getResultList(), BillingResult.class)) {
            fragments.add(billingResult.getResultXML());
        }
        return XMLConverter.combine("Billingdata", fragments);
    }

    @Override
    @SuppressWarnings("boxing")
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER",
            "PLATFORM_OPERATOR", "MARKETPLACE_OWNER" })
    public byte[] getRevenueShareData(Long pFrom, Long pTo,
            BillingSharesResultType resultType)
            throws OrganizationAuthoritiesException,
            OperationNotPermittedException, ValidationException {

        validateInputParameters(pFrom, pTo, resultType);

        byte[] empty = {};

        Organization callingOrganization = dm.getCurrentUser()
                .getOrganization();

        checkRevenueShareDataPermission(callingOrganization, resultType);
        List<BillingSharesResult> billingSharesResult = loadBillingSharesResult(
                pFrom, pTo, resultType, callingOrganization.getKey());
        if (billingSharesResult.isEmpty()) {
            return empty;
        }

        final List<String> fragments = new ArrayList<>();
        for (BillingSharesResult billingResult : billingSharesResult) {
            fragments.add(billingResult.getResultXML());
        }

        return XMLConverter.combine("RevenueSharesResults", fragments,
                schemaHeader(resultType));
    }

    private void validateInputParameters(Long pFrom, Long pTo,
            BillingSharesResultType resultType) throws ValidationException {

        ArgumentValidator.notNull("from", pFrom);
        ArgumentValidator.notNull("to", pTo);
        ArgumentValidator.notNull("resultType", resultType);

        Date fromDate = new Date(pFrom.longValue());
        Date toDate = new Date(pTo.longValue());
        BLValidator.isValidDateRange(fromDate, toDate);
    }

    List<BillingSharesResult> loadBillingSharesResult(Long fromDate,
            Long toDate, BillingSharesResultType resultType,
            Long organizationKey) {
        List<BillingSharesResult> result;
        if (isUserPlatformOperator()) {
            result = sdr.loadBillingSharesResult(resultType, fromDate, toDate);
        } else {
            result = sdr.loadBillingSharesResultForOrganization(
                    organizationKey, resultType, fromDate, toDate);
        }
        return result;
    }

    private boolean isUserPlatformOperator() {
        Set<UserRoleType> userRoles = dm.getCurrentUser()
                .getAssignedRoleTypes();
        return userRoles.contains(UserRoleType.PLATFORM_OPERATOR);
    }

    private String schemaHeader(BillingSharesResultType resultType) {
        String revenueShareResultSchema = null;
        switch (resultType) {
        case BROKER:
            revenueShareResultSchema = BrokerRevenueShareResult.SCHEMA;
            break;
        case RESELLER:
            revenueShareResultSchema = ResellerRevenueShareResult.SCHEMA;
            break;
        case MARKETPLACE_OWNER:
            revenueShareResultSchema = MarketplaceOwnerRevenueShareResult.SCHEMA;
            break;
        case SUPPLIER:
            revenueShareResultSchema = SupplierRevenueShareResult.SCHEMA;
            break;
        default:
            Invariants.assertTrue(false,
                    "ERROR: unkown value of BillingSharesResultType");
            break;
        }
        return revenueShareResultSchema;
    }

    private void checkRevenueShareDataPermission(
            Organization callingOrganization, BillingSharesResultType resultType)
            throws OrganizationAuthoritiesException,
            OperationNotPermittedException {
        PlatformUser callingUser = dm.getCurrentUser();

        // PLATFORM_OPERATOR may call any resultType
        if (callingOrganization.getGrantedRoleTypes().contains(
                OrganizationRoleType.PLATFORM_OPERATOR)
                || callingUser.getAssignedRoleTypes().contains(
                        UserRoleType.PLATFORM_OPERATOR)) {
            return;
        }

        switch (resultType) {
        case BROKER:
            verifyOrganizationHasRole(callingOrganization,
                    OrganizationRoleType.BROKER);
            verifyUserHasRoles(callingUser, UserRoleType.BROKER_MANAGER);
            break;
        case RESELLER:
            verifyOrganizationHasRole(callingOrganization,
                    OrganizationRoleType.RESELLER);
            verifyUserHasRoles(callingUser, UserRoleType.RESELLER_MANAGER);
            break;
        case MARKETPLACE_OWNER:
            verifyOrganizationHasRole(callingOrganization,
                    OrganizationRoleType.MARKETPLACE_OWNER);
            verifyUserHasRoles(callingUser, UserRoleType.MARKETPLACE_OWNER);
            break;
        case SUPPLIER:
            verifyOrganizationHasRole(callingOrganization,
                    OrganizationRoleType.SUPPLIER);
            verifyUserHasRoles(callingUser, UserRoleType.SERVICE_MANAGER);
            break;
        default:
            Invariants.assertTrue(false,
                    "ERROR: unkown value of BillingSharesResultType");
            break;
        }
    }

    private void verifyOrganizationHasRole(Organization callingOrg,
            OrganizationRoleType expectedRole)
            throws OrganizationAuthoritiesException {
        Set<OrganizationRoleType> orgRoles = callingOrg.getGrantedRoleTypes();
        if (!orgRoles.contains(expectedRole)) {
            OrganizationAuthoritiesException e = new OrganizationAuthoritiesException();
            LOGGER.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORGANIZATION_ROLE_MISSING,
                    expectedRole.toString());
            throw e;
        }
    }

    private void verifyUserHasRoles(PlatformUser callingUser,
            UserRoleType expectedRole) throws OperationNotPermittedException {
        Set<UserRoleType> userRoles = callingUser.getAssignedRoleTypes();
        if (!userRoles.contains(expectedRole)) {
            OperationNotPermittedException e = new OperationNotPermittedException();
            LOGGER.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_USER_ROLE_MISSING,
                    expectedRole.toString());
            throw e;
        }
    }

}
