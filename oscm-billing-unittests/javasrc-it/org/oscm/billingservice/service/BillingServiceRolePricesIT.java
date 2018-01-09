/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Aleh Khomich                                                      
 *                                                                              
 *  Creation Date: 30.07.2010                                                      
 *                                                                              
 *  Completion Time: 30.07.2010                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.billingservice.service;

import static org.mockito.Mockito.mock;
import static org.oscm.test.BigDecimalAsserts.checkEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;
import org.oscm.accountservice.dao.UserLicenseDao;
import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.calculation.share.SharesCalculatorBean;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceBean;
import org.oscm.communicationservice.bean.CommunicationServiceBean;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UsageLicense;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.interceptor.DateFactory;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.paymentservice.bean.PaymentServiceStub;
import org.oscm.test.EJBTestBase;
import org.oscm.test.Numbers;
import org.oscm.test.TestDateFactory;
import org.oscm.test.XMLTestValidator;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.w3c.dom.Document;

/**
 * JUnit test.
 * 
 * @author Aleh Khomich.
 * 
 */
public class BillingServiceRolePricesIT extends EJBTestBase {

    private DataService mgr;
    private BillingServiceLocal billingService;
    private Organization supplierAndProvider;
    private Organization customer;
    private TechnicalProduct technicalProduct;
    private Product product;
    private Subscription subscription;
    private XMLTestValidator xmlValidator;

    // private PricedOption pricedOption;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new PaymentServiceStub());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public void sendAllNonSuspendingMessages(
                    List<TriggerMessage> messageData) {
                // empty block for stub
            }
        });
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(mock(SharesDataRetrievalServiceBean.class));
        container.addBean(new RevenueCalculatorBean());
        container.addBean(mock(SharesCalculatorBean.class));
        container.addBean(new CommunicationServiceBean());
        container.addBean(new UserLicenseDao());
        container.addBean(new BillingServiceBean());

        mgr = container.get(DataService.class);
        billingService = container.get(BillingServiceLocal.class);
        xmlValidator = new XMLTestValidator();
        xmlValidator.setup();

        // billing offset set to 0
        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);

        setBaseDataCreationDate();

        // create commons objects for tests
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOrganizationRoles(mgr);
                createSupportedCurrencies(mgr);
                createPaymentTypes(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                supplierAndProvider = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                technicalProduct = TechnicalProducts.createTechnicalProduct(mgr,
                        supplierAndProvider, "techProdId", false,
                        ServiceAccessType.LOGIN);

                product = Products.createProduct(supplierAndProvider,
                        technicalProduct, true, "productId", null, mgr);

                customer = Organizations.createCustomer(mgr,
                        supplierAndProvider);

                return null;
            }
        });

    }

    protected void setBaseDataCreationDate() {
        final Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        DateFactory.setInstance(new TestDateFactory(cal.getTime()));
    }

    /**
     * Billing test for parameters and options price model with priced roles..
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithRolesForParameters() throws Exception {

        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear, testMonth,
                testDay);

        long creationTime = getTimeInMillisForBilling(testYear, testMonth - 2,
                testDay);

        int userNumber = 1;
        BigDecimal expectedPrice = new BigDecimal(49203)
                .setScale(Numbers.BIGDECIMAL_SCALE);

        initDataParameters(userNumber, creationTime, creationTime);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billingService.startBillingRun(billingTime);
                return null;
            }
        });
        Document doc = getBillingDocument();

        String netCosts = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/@grossAmount");
        checkEquals("Wrong net costs found", expectedPrice.toPlainString(),
                netCosts);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for parameters and options price model with priced roles..
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithRolesForParametersAndOptions() throws Exception {

        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear, testMonth,
                testDay);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                testMonth - 2, testDay);
        long subscriptionActivationTime = getTimeInMillisForBilling(testYear,
                testMonth - 2, testDay);

        int userNumber = 1;
        BigDecimal expectedPrice = new BigDecimal(1867)
                .setScale(Numbers.BIGDECIMAL_SCALE);

        initDataParametersAndOptions(userNumber, subscriptionCreationTime,
                subscriptionActivationTime);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billingService.startBillingRun(billingTime);
                return null;
            }
        });
        Document doc = getBillingDocument();

        String netCosts = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/@grossAmount");
        checkEquals("Wrong net costs found", expectedPrice.toPlainString(),
                netCosts);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for price model with priced roles.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithPricedRolesForPriceModel() throws Exception {
        int numUser = 10;
        BigDecimal expectedPrice = new BigDecimal(1021)
                .setScale(Numbers.BIGDECIMAL_SCALE);

        testBillingWithRolesForUserAssignment(numUser, expectedPrice);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for price model with stepped price.
     * 
     * @throws Exception
     */
    private void testBillingWithRolesForUserAssignment(int numUser,
            BigDecimal expectedPrice) throws Exception {

        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear, testMonth,
                testDay);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                testMonth - 2, testDay);
        long subscriptionActivationTime = getTimeInMillisForBilling(testYear,
                testMonth - 2, testDay);

        int userNumber = numUser;

        initDataPriceModel(userNumber, subscriptionCreationTime,
                subscriptionActivationTime);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billingService.startBillingRun(billingTime);
                return null;
            }
        });
        Document doc = getBillingDocument();

        String costs = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/@grossAmount");
        checkEquals(expectedPrice.toPlainString(), costs);
    }

    /**
     * Returns the time matching the specified parameters for year, month and
     * day.
     * 
     * @param paramTestYear
     *            The year.
     * @param paramTestMonth
     *            The month.
     * @param paramTestDay
     *            The day.
     * @return The time matching the specified parameters in milliseconds.
     */
    private long getTimeInMillisForBilling(final int paramTestYear,
            final int paramTestMonth, final int paramTestDay) {
        final Calendar billingCalendar = Calendar.getInstance();
        billingCalendar.set(Calendar.YEAR, paramTestYear);
        billingCalendar.set(Calendar.MONTH, paramTestMonth);
        billingCalendar.set(Calendar.DAY_OF_MONTH, paramTestDay);
        billingCalendar.set(Calendar.HOUR_OF_DAY, 0);
        billingCalendar.set(Calendar.MINUTE, 0);
        billingCalendar.set(Calendar.SECOND, 0);
        billingCalendar.set(Calendar.MILLISECOND, 0);

        return billingCalendar.getTimeInMillis();
    }

    /**
     * Update the modification date of the last history record for the given
     * subscription.
     * 
     * @param obj
     *            The domain object, the last history entry of which has to be
     *            updated.
     * 
     * @param date
     *            the modification date to set.
     */
    private void updateHistoryModDate(DomainObject<?> obj, Date date) {
        obj.setHistoryModificationTime(Long.valueOf(date.getTime()));
    }

    /**
     * Helper method for getting xml billing result.
     * 
     * @return Billing xml document.
     * 
     * @throws Exception
     *             On error.
     */
    private Document getBillingDocument() throws Exception {
        return runTX(new Callable<Document>() {
            @Override
            public Document call() throws Exception {

                Query query = mgr.createQuery(
                        "SELECT br FROM BillingResult br WHERE br.dataContainer.organizationTKey = :organizationTKey ORDER BY br.dataContainer.periodEndTime DESC");
                query.setParameter("organizationTKey",
                        Long.valueOf(customer.getKey()));
                query.setMaxResults(1);
                BillingResult billingResult = (BillingResult) query
                        .getSingleResult();

                System.out.println(billingResult.getResultXML());

                Document doc = XMLConverter
                        .convertToDocument(billingResult.getResultXML(), true);

                return doc;
            }
        });
    }

    /**
     * Creates priced roles for price model.
     * 
     * @param userNumber
     *            Number of users.
     * @param subscriptionCreationTime
     *            Subscription creation time.
     * @param subscriptionActivationTime
     *            Subscription activation time.
     * @param stepNum
     *            Number of steps.
     * @param limitArray
     *            Input data for step values.
     * @param priceArray
     *            Input data for step prices.
     * @param freeAmountArray
     *            Input data for free amount. Limit will be reduced for this
     *            value.
     * @param additionalPriceArray
     *            Input data for additional price for steps. * @throws Exception
     */
    private void initDataPriceModel(final int userNumber,
            final long subscriptionCreationTime,
            final long subscriptionActivationTime) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                subscription = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), product.getProductId(),
                        "subscriptionId", subscriptionCreationTime,
                        subscriptionActivationTime, product.getVendor(), 1);

                subscription.getProduct().setHistoryModificationTime(
                        Long.valueOf(subscriptionCreationTime));

                Date creationDate = new Date(subscriptionCreationTime);

                for (int i = 0; i < 3; i++) {
                    TechnicalProducts.addRoleDefinition("roleDefinition" + i,
                            technicalProduct, mgr,
                            Long.valueOf(subscriptionCreationTime));
                }

                for (int i = 0; i < userNumber; i++) {
                    String userName = "userName" + String.valueOf(i);
                    createUserAndLicense(userName, subscriptionCreationTime,
                            creationDate);
                }

                PriceModel pm = subscription.getPriceModel();
                pm.setPeriod(PricingPeriod.MONTH);
                pm.setPricePerUserAssignment(new BigDecimal(2L));
                updateHistoryModDate(pm, creationDate);
                mgr.flush();

                createPricedProductRoles(pm, null, null, Numbers.BD100,
                        creationDate);

                return null;
            }
        });
    }

    /**
     * Creates priced roles for price model.
     * 
     * @param userNumber
     *            Number of users.
     * @param subscriptionCreationTime
     *            Subscription creation time.
     * @param subscriptionActivationTime
     *            Subscription activation time.
     * @param stepNum
     *            Number of steps.
     * @param limitArray
     *            Input data for step values.
     * @param priceArray
     *            Input data for step prices.
     * @param freeAmountArray
     *            Input data for free amount. Limit will be reduced for this
     *            value.
     * @param additionalPriceArray
     *            Input data for additional price for steps. * @throws Exception
     */
    private void initDataParametersAndOptions(final int userNumber,
            final long subscriptionCreationTime,
            final long subscriptionActivationTime) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                subscription = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), product.getProductId(),
                        "subscriptionId", subscriptionCreationTime,
                        subscriptionActivationTime, product.getVendor(), 1);

                subscription.getProduct().setHistoryModificationTime(
                        Long.valueOf(subscriptionCreationTime));

                Date creationDate = new Date(subscriptionCreationTime);

                for (int i = 0; i < 3; i++) {
                    TechnicalProducts.addRoleDefinition("roleDefinition" + i,
                            technicalProduct, mgr,
                            Long.valueOf(subscriptionCreationTime));
                }

                for (int i = 0; i < userNumber; i++) {
                    String userName = "userName" + String.valueOf(i);
                    createUserAndLicense(userName, subscriptionCreationTime,
                            creationDate);
                }

                PriceModel pm = subscription.getPriceModel();
                pm.setPeriod(PricingPeriod.MONTH);
                pm.setPricePerUserAssignment(new BigDecimal(2l));
                updateHistoryModDate(pm, creationDate);
                mgr.flush();

                ParameterDefinition enumParamDef = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.ENUMERATION,
                                "enumParam", ParameterType.SERVICE_PARAMETER,
                                technicalProduct, mgr, null, null, true);
                updateHistoryModDate(enumParamDef, creationDate);
                enumParamDef.setDefaultValue("x");
                mgr.persist(enumParamDef);

                ParameterOption option = new ParameterOption();
                option.setOptionId("optionId1");
                option.setParameterDefinition(enumParamDef);
                updateHistoryModDate(option, creationDate);
                mgr.persist(option);

                ParameterOption option2 = new ParameterOption();
                option2.setOptionId("optionId2");
                option2.setParameterDefinition(enumParamDef);
                updateHistoryModDate(option2, creationDate);
                mgr.persist(option2);

                List<ParameterOption> paramOptions = new ArrayList<>();
                paramOptions.add(option);
                paramOptions.add(option2);
                enumParamDef.setOptionList(paramOptions);

                Parameter enumParam = Products.createParameter(enumParamDef,
                        product, mgr);
                enumParam.setValue("optionId2");
                updateHistoryModDate(enumParam, creationDate);

                PricedParameter pricedEnumParam = new PricedParameter();
                pricedEnumParam.setParameter(enumParam);
                pricedEnumParam.setPriceModel(pm);
                pm.getSelectedParameters().add(pricedEnumParam);

                PricedOption pricedOption = new PricedOption();
                pricedOption.setParameterOptionKey(option.getKey());
                pricedOption.setPricedParameter(pricedEnumParam);
                pricedOption.setPricePerSubscription(new BigDecimal(777));
                pricedOption.setPricePerUser(new BigDecimal(765));
                List<PricedOption> pricedOptions = new ArrayList<>();
                pricedOptions.add(pricedOption);
                updateHistoryModDate(pricedOption, creationDate);
                mgr.persist(pricedOption);

                PricedOption pricedOption2 = new PricedOption();
                pricedOption2.setParameterOptionKey(option2.getKey());
                pricedOption2.setPricedParameter(pricedEnumParam);
                pricedOption2.setPricePerSubscription(new BigDecimal(888));
                pricedOption2.setPricePerUser(new BigDecimal(876));
                pricedOptions.add(pricedOption2);
                updateHistoryModDate(pricedOption2, creationDate);
                mgr.persist(pricedOption2);

                pricedEnumParam.setPricedOptionList(pricedOptions);
                updateHistoryModDate(pricedEnumParam, creationDate);
                mgr.persist(pricedEnumParam);

                createPricedProductRoles(null, null, pricedOption2,
                        Numbers.BD100, creationDate);

                return null;
            }
        });
    }

    /**
     * Creates priced roles for price model.
     * 
     * @param userNumber
     *            Number of users.
     * @param subscriptionCreationTime
     *            Subscription creation time.
     * @param subscriptionActivationTime
     *            Subscription activation time.
     * @param stepNum
     *            Number of steps.
     * @param limitArray
     *            Input data for step values.
     * @param priceArray
     *            Input data for step prices.
     * @param freeAmountArray
     *            Input data for free amount. Limit will be reduced for this
     *            value.
     * @param additionalPriceArray
     *            Input data for additional price for steps. * @throws Exception
     */
    private void initDataParameters(final int userNumber,
            final long subscriptionCreationTime,
            final long subscriptionActivationTime) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                subscription = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), product.getProductId(),
                        "subscriptionId", subscriptionCreationTime,
                        subscriptionActivationTime, product.getVendor(), 1);

                subscription.getProduct().setHistoryModificationTime(
                        Long.valueOf(subscriptionCreationTime));

                Date creationDate = new Date(subscriptionCreationTime);

                for (int i = 0; i < 3; i++) {
                    TechnicalProducts.addRoleDefinition("roleDefinition" + i,
                            technicalProduct, mgr,
                            Long.valueOf(subscriptionCreationTime));
                }

                for (int i = 0; i < userNumber; i++) {
                    String userName = "userName" + String.valueOf(i);
                    createUserAndLicense(userName, subscriptionCreationTime,
                            creationDate);
                }

                PriceModel pm = subscription.getPriceModel();
                pm.setPeriod(PricingPeriod.MONTH);
                pm.setPricePerUserAssignment(new BigDecimal(2L));
                updateHistoryModDate(pm, creationDate);
                mgr.flush();

                ParameterDefinition paramDef = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.INTEGER,
                                "intParam", ParameterType.SERVICE_PARAMETER,
                                technicalProduct, mgr, null, null, true);

                Query query = mgr.createQuery(
                        "UPDATE ParameterDefinitionHistory pdh SET pdh.modDate = :date");
                query.setParameter("date", creationDate);
                query.executeUpdate();

                Parameter param = Products.createParameter(paramDef, product,
                        mgr);
                query = mgr.createQuery(
                        "UPDATE ParameterHistory ph SET ph.modDate = :date");
                query.setParameter("date", creationDate);
                query.executeUpdate();

                PricedParameter pricedParam = new PricedParameter();
                pricedParam.setParameter(param);
                pricedParam.setPriceModel(pm);
                pricedParam.setPricePerSubscription(new BigDecimal(100));
                pricedParam.setPricePerUser(new BigDecimal(200));
                pm.setSelectedParameters(new ArrayList<PricedParameter>());
                pm.getSelectedParameters().add(pricedParam);
                updateHistoryModDate(pricedParam, creationDate);
                mgr.persist(pricedParam);

                createPricedProductRoles(null, pricedParam, null, Numbers.BD100,
                        creationDate);

                return null;
            }
        });
    }

    /**
     * Create prices for roles for user assignment in price model.
     * 
     * @param priceModel
     * @param pricedOption
     * @param pricedParam
     * @param creationDate
     * @param roleDefinition
     * @throws NonUniqueBusinessKeyException
     * 
     */
    private void createPricedProductRoles(PriceModel priceModel,
            PricedParameter pricedParameter, PricedOption pricedOption,
            BigDecimal pricePerUser, Date creationDate)
            throws NonUniqueBusinessKeyException {

        List<RoleDefinition> roleDefinitionLists = technicalProduct
                .getRoleDefinitions();

        for (RoleDefinition roleDefinition : roleDefinitionLists) {
            PricedProductRole pricedProductRole = new PricedProductRole();
            pricedProductRole.setPriceModel(priceModel);
            pricedProductRole.setPricedParameter(pricedParameter);
            pricedProductRole.setPricedOption(pricedOption);
            pricedProductRole.setRoleDefinition(roleDefinition);
            pricedProductRole.setPricePerUser(pricePerUser);
            updateHistoryModDate(pricedProductRole, creationDate);

            mgr.persist(pricedProductRole);
            mgr.flush();

        }

    }

    /**
     * Create user and license for him.
     * 
     * @param userName
     *            User name.
     * 
     * @param subscriptionCreationTime
     *            Subscription creation time.
     * @param creationDate
     *            Subscription creation date
     * @throws Exception
     *             On error.
     */
    private void createUserAndLicense(String userName,
            final long subscriptionCreationTime, final Date creationDate)
            throws Exception {

        List<RoleDefinition> roleDefinitionLists = technicalProduct
                .getRoleDefinitions();

        PlatformUser user = new PlatformUser();
        user.setUserId(userName);
        user.setOrganization(supplierAndProvider);
        user.setEmail("user_1@user_1.com");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setLocale("en");
        mgr.persist(user);
        user = mgr.find(user);

        UsageLicense license = new UsageLicense();
        license.setAssignmentDate(subscriptionCreationTime);
        license.setSubscription(subscription);
        license.setUser(user);
        license.setRoleDefinition(roleDefinitionLists.get(0));
        updateHistoryModDate(license, creationDate);

        mgr.flush();
        mgr.persist(license);
    }

}
