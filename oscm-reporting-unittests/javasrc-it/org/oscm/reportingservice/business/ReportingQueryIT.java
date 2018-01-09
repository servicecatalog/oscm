/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Vitaliy Ryumshyn                                                      
 *                                                                              
 *  Creation Date: 9.03.2011                                                      
 *                                                                              
 *  Completion Time: 10.03.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.oscm.test.Numbers.L123;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.accountservice.assembler.BillingContactAssembler;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.service.BillingServiceBean;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.GatheredEvent;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.MarketingPermission;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.PaymentResult;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductReference;
import org.oscm.domobjects.Report;
import org.oscm.domobjects.ReportData;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.eventservice.bean.EventServiceBean;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.ReportingService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ReportType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOReport;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUser;
import org.oscm.reportingservice.bean.ReportingServiceBean;
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.reportingservice.dao.BillingDao.ReportBillingData;
import org.oscm.reportingservice.dao.PlatformRevenueDao;
import org.oscm.reportingservice.dao.PlatformRevenueDao.RowData;
import org.oscm.reportingservice.dao.UnitDao;
import org.oscm.reportingservice.service.stubs.ApplicationServiceStub;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.subscriptionservice.assembler.SubscriptionAssembler;
import org.oscm.subscriptionservice.bean.ManageSubscriptionBean;
import org.oscm.subscriptionservice.bean.SubscriptionServiceBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.techproductoperation.bean.OperationRecordServiceLocalBean;
import org.oscm.techproductoperation.dao.OperationRecordDao;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PaymentInfos;
import org.oscm.test.data.PaymentTypes;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.types.enumtypes.PaymentProcessingStatus;
import org.oscm.types.enumtypes.PlatformEventIdentifier;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.w3c.dom.Document;

/**
 * The tests for reporting queries
 * 
 * @author Vitaliy Ryumshyn
 * 
 */
public class ReportingQueryIT extends EJBTestBase {

    private static final String REPORT_WSDLURL = "http://localhost:8080/Report/ReportingServiceBean?wsdl";
    private static final String REPORT_SOAP_ENDPOINT = "http://localhost:8080/Report/ReportingServiceBean";
    private static final String REPORT_ENGINEURL = "http://localhost:8080/birt/frameset?__report=${reportname}.rptdesign&SessionId=${sessionid}&__locale=${locale}&WSDLURL=${wsdlurl}&SOAPEndPoint=${soapendpoint}";

    private static final File TEST_XML_FILE = new File(
            "javares/BillingResult.xml");

    public static final long TIMESTAMP = 1282816800000L;

    private static final String[] ENABLED_PAYMENTTYPES = new String[] {
            PaymentType.CREDIT_CARD, PaymentType.DIRECT_DEBIT,
            PaymentType.INVOICE };

    private static final String VALID_SESSION_ID = "valid_session";
    private static final String VALID_SESSION_ID2 = "valid_session2";
    private static final int MAX_CUSTOMERS_COUNT = 2;
    // minimum 4!!!
    private static final int MAX_PRODUCT_COUNT = 5;

    private ConfigurationServiceStub configurationStub;

    private final Map<Organization, ArrayList<PlatformUser>> testUsers = new HashMap<>();
    private final Map<Organization, Long> billingResultsKeys = new HashMap<>();
    private final Map<String, List<Organization>> testOrganizations = new HashMap<>();
    private final Map<String, List<Product>> testProducts = new HashMap<>();
    private List<Session> sessions = new ArrayList<>();
    private List<PaymentType> paymentTypes;

    protected LocalizerServiceLocal localizer;
    protected SubscriptionService subMgmt;
    protected SubscriptionServiceLocal subMgmtLocal;
    protected IdentityService idMgmt;
    private ReportingServiceBean reporting;
    private DataService mgr;
    private long MULTIPLIER = 2L;
    private PlatformUser[] customerAdminsA;
    private PlatformUser[] customerAdminsB;
    private PlatformUser supplierUserA;
    @SuppressWarnings("unused")
    private PlatformUser supplierUserB;
    private Organization tpAndSupplierA;
    private Organization tpAndSupplierB;
    private Organization platformOperator;
    private Map<OrganizationRoleType, List<Report>> roleToReports;
    private Product product;
    private List<VOSubscription> subscriptionsA = new ArrayList<>();
    private ReportingService reportService;
    private UserGroupServiceLocalBean userGroupService;

    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        roleToReports = new HashMap<>();
        roleToReports.put(OrganizationRoleType.CUSTOMER,
                getReportList("Event", "Subscription"));
        roleToReports.put(OrganizationRoleType.SUPPLIER,
                getReportList("Supplier_Product", "Supplier_Customer",
                        "Supplier_Billing", "Supplier_PaymentResultStatus"));
        roleToReports.put(OrganizationRoleType.TECHNOLOGY_PROVIDER,
                getReportList("Provider_Event", "Provider_Supplier",
                        "Provider_Subscription", "Provider_Instance"));

        testProducts.put("A", new ArrayList<Product>());
        testProducts.put("B", new ArrayList<Product>());
        testOrganizations.put("A", new ArrayList<Organization>());
        testOrganizations.put("B", new ArrayList<Organization>());

        setupContainer(container);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                paymentTypes = createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                initReports();
                SupportedCountries.createSomeSupportedCountries(mgr);

                platformOperator = Organizations.createOrganization(mgr,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                platformOperator.setOrganizationId(
                        OrganizationRoleType.PLATFORM_OPERATOR.name());
                Marketplaces.createGlobalMarketplace(platformOperator,
                        GLOBAL_MARKETPLACE_NAME, mgr);
                mgr.persist(platformOperator);

                product = Products.createProduct("orgID", "SomeService",
                        "techProductId", mgr);

                Organization organization = mgr.find(Organization.class,
                        product.getVendorKey());
                OrganizationReference orgRef = new OrganizationReference(
                        organization, organization,
                        OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
                mgr.persist(orgRef);
                organization.getTargets().add(orgRef);
                organization.getSources().add(orgRef);

                MarketingPermission mp = new MarketingPermission();
                mp.setTechnicalProduct(product.getTechnicalProduct());
                mp.setOrganizationReference(orgRef);
                mgr.persist(mp);

                mgr.flush();
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("EUR"));
                mgr.persist(sc);
                initMasterData("A");
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                initMasterData("B");
                return null;
            }
        });

        enablePaymentTypes(mgr, tpAndSupplierA.getOrganizationId(),
                OrganizationRoleType.SUPPLIER);

        enablePaymentTypes(mgr, tpAndSupplierB.getOrganizationId(),
                OrganizationRoleType.SUPPLIER);

        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);

        for (int i = 0; i < MAX_CUSTOMERS_COUNT; i++) {
            container.login(customerAdminsA[i].getKey(),
                    ROLE_ORGANIZATION_ADMIN);
            Session session = new Session();
            session.setPlatformUserKey(customerAdminsA[i].getKey());
            sessions.clear();
            sessions.add(session);

            enablePaymentTypes(mgr,
                    testOrganizations.get("A").get(i).getOrganizationId(),
                    OrganizationRoleType.CUSTOMER);
            subscriptionsA.add(subscribeToProductAndUse("A", 0, i));

            runBillingForCustomer("A", i);
        }

        for (int i = 0; i < MAX_CUSTOMERS_COUNT; i++) {
            container.login(customerAdminsB[i].getKey(),
                    ROLE_ORGANIZATION_ADMIN);
            Session session = new Session();
            session.setPlatformUserKey(customerAdminsA[i].getKey());
            sessions.clear();
            sessions.add(session);

            enablePaymentTypes(mgr,
                    testOrganizations.get("B").get(i).getOrganizationId(),
                    OrganizationRoleType.CUSTOMER);
            subscribeToProductAndUse("B", 0, i);
            runBillingForCustomer("B", i);
        }

    }

    private void setupContainer(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());

        container.addBean(new SessionServiceStub() {
            @Override
            public Session getPlatformSessionForSessionId(String sessionId) {
                for (Session session : sessions) {

                    if (VALID_SESSION_ID.equals(sessionId)) {
                        return session;
                    }
                    if (VALID_SESSION_ID2.equals(sessionId)) {
                        return session;
                    }

                }
                throw new NoResultException("Invalid session id");
            }

            @Override
            public List<Session> getProductSessionsForSubscriptionTKey(
                    long subscriptionTKey) {
                return sessions;
            }
        });

        container.addBean(new EventServiceBean());
        container.addBean(new ApplicationServiceStub());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                List<TriggerProcessMessageData> result = new ArrayList<>();
                for (TriggerMessage m : messageData) {
                    TriggerProcess tp = new TriggerProcess();
                    tp.setUser(supplierUserA);
                    TriggerProcessMessageData data = new TriggerProcessMessageData(
                            tp, m);
                    result.add(data);
                }

                return result;
            }

        });
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(new BillingServiceBean());
        IdentityServiceLocal mock = mock(IdentityServiceLocal.class);
        doAnswer(new Answer() {
            @Override
            public PlatformUser answer(InvocationOnMock invocation)
                    throws Throwable {
                PlatformUser user = new PlatformUser();
                user.setUserId((String) invocation.getArguments()[0]);
                return (PlatformUser) mgr.getReferenceByBusinessKey(user);
            }
        }).when(mock).getPlatformUser(anyString(), anyBoolean());
        container.addBean(mock);
        container.addBean(new TenantProvisioningServiceBean());
        container.addBean(new CommunicationServiceStub());

        configurationStub = new ConfigurationServiceStub();
        configurationStub.setConfigurationSetting(
                ConfigurationKey.REPORT_ENGINEURL, REPORT_ENGINEURL);
        configurationStub.setConfigurationSetting(
                ConfigurationKey.REPORT_SOAP_ENDPOINT, REPORT_SOAP_ENDPOINT);
        configurationStub.setConfigurationSetting(
                ConfigurationKey.REPORT_WSDLURL, REPORT_WSDLURL);
        container.addBean(configurationStub);
        container.addBean(new ManageSubscriptionBean());
        container.addBean(new OperationRecordDao());
        container.addBean(new OperationRecordServiceLocalBean());
        container.addBean(new SubscriptionServiceBean());
        reporting = new ReportingServiceBean();
        container.addBean(reporting);

        mgr = container.get(DataService.class);
        localizer = container.get(LocalizerServiceLocal.class);
        subMgmt = container.get(SubscriptionService.class);
        subMgmtLocal = container.get(SubscriptionServiceLocal.class);
        idMgmt = container.get(IdentityService.class);

        // enforce injection of referenced beans
        reportService = container.get(ReportingService.class);
    }

    /**
     * 
     * assign allowed payment types to organization
     * 
     * @param mgr
     * @param orgId
     * @param roleType
     * @throws Exception
     */
    protected void enablePaymentTypes(final DataService mgr, final String orgId,
            final OrganizationRoleType roleType) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organizations.addPaymentTypesToOrganizationRef(mgr, orgId,
                        roleType);
                return null;
            }
        });
    }

    /**
     * Initialize test database with master data (products and price models)
     * 
     * @return The key of the created admin user.
     * @throws Exception
     */
    private void initMasterData(String custAbr) throws Exception {

        // create organization (technical provider, supplier)
        Organization tpAndSupplier = Organizations.createOrganization(mgr,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        OrganizationReference ref = new OrganizationReference(platformOperator,
                tpAndSupplier,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
        mgr.persist(ref);
        mgr.persist(tpAndSupplier);

        OrganizationReference orgRef = new OrganizationReference(tpAndSupplier,
                tpAndSupplier,
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
        mgr.persist(orgRef);
        tpAndSupplier.getTargets().add(orgRef);
        tpAndSupplier.getSources().add(orgRef);

        // insert TECHNICAL PRODUCT
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                tpAndSupplier, "TP_ID_" + custAbr, false,
                ServiceAccessType.LOGIN);
        prepareTechnicalProduct(tProd);

        MarketingPermission mp = new MarketingPermission();
        mp.setTechnicalProduct(tProd);
        mp.setOrganizationReference(orgRef);
        mgr.persist(mp);

        // create EVENTS
        long tpKey = tProd.getKey();
        TechnicalProduct technicalProduct = mgr
                .getReference(TechnicalProduct.class, tpKey);
        Event event = new Event();
        event.setTechnicalProduct(technicalProduct);
        event.setEventIdentifier(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
        event.setEventType(EventType.SERVICE_EVENT);
        mgr.persist(event);

        LocalizedResource lr = new LocalizedResource();
        lr.setLocale("en");
        lr.setObjectKey(event.getKey());
        lr.setObjectType(LocalizedObjectTypes.EVENT_DESC);
        lr.setValue("en login event");
        mgr.persist(lr);

        LocalizedResource lr1 = new LocalizedResource();
        lr1.setLocale("de");
        lr1.setObjectKey(event.getKey());
        lr1.setObjectType(LocalizedObjectTypes.EVENT_DESC);
        lr1.setValue("de login event");
        mgr.persist(lr1);

        // insert PRODUCTS
        addProducts(custAbr, tpAndSupplier, tProd, MAX_PRODUCT_COUNT + 1,
                testProducts.get(custAbr));
        ProductReference pref;
        pref = new ProductReference(testProducts.get(custAbr).get(0),
                testProducts.get(custAbr).get(MAX_PRODUCT_COUNT - 3));
        mgr.persist(pref);
        pref = new ProductReference(testProducts.get(custAbr).get(0),
                testProducts.get(custAbr).get(MAX_PRODUCT_COUNT - 2));
        mgr.persist(pref);

        // organization admin of the technical product's owner for notification
        PlatformUser supplierUser = Organizations.createUserForOrg(mgr,
                tpAndSupplier, true, "admin");
        if (custAbr.equalsIgnoreCase("A")) {
            tpAndSupplierA = tpAndSupplier;
            supplierUserA = supplierUser;
            customerAdminsA = new PlatformUser[MAX_CUSTOMERS_COUNT];
        } else {
            tpAndSupplierB = tpAndSupplier;
            supplierUserB = supplierUser;
            customerAdminsB = new PlatformUser[MAX_CUSTOMERS_COUNT];
        }

        // insert some organizations with users
        Organization cust = null;
        for (int i = 1; i <= MAX_CUSTOMERS_COUNT; i++) {
            cust = Organizations.createOrganization(mgr,
                    OrganizationRoleType.CUSTOMER);
            ref = new OrganizationReference(tpAndSupplier, cust,
                    OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
            mgr.persist(ref);

            mgr.persist(cust);
            mgr.flush();

            testOrganizations.get(custAbr).add(cust);
            ArrayList<PlatformUser> userlist = new ArrayList<>();
            testUsers.put(cust, userlist);
            // add a organization admin
            PlatformUser admin = Organizations.createUserForOrg(mgr, cust, true,
                    "admin");
            if (custAbr.equalsIgnoreCase("A")) {
                customerAdminsA[i - 1] = admin;
            } else {
                customerAdminsB[i - 1] = admin;
            }
            userlist.add((PlatformUser) ReflectiveClone.clone(admin));
            // add some users for the organization
            for (int j = 1; j <= 5; j++) {
                PlatformUser user = Organizations.createUserForOrg(mgr, cust,
                        false, "user" + j);
                userlist.add((PlatformUser) ReflectiveClone.clone(user));
            }
        }

        mgr.flush();
    }

    private void initReports()
            throws ObjectNotFoundException, NonUniqueBusinessKeyException {

        for (OrganizationRoleType orgType : roleToReports.keySet()) {
            List<Report> custReports = roleToReports.get(orgType);
            for (Report report : custReports) {
                OrganizationRole role = new OrganizationRole();
                role.setRoleName(orgType);
                role = (OrganizationRole) mgr.getReferenceByBusinessKey(role);
                ReportData data = new ReportData();
                data.setReportName(report.getReportName());
                report.setDataContainer(data);
                report.setOrganizationRole(role);
                mgr.persist(report);
            }
        }
        mgr.flush();

    }

    private static String getTestFileAsString(File file) {
        FileInputStream inputStream = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            inputStream = new FileInputStream(file);
            Document doc = builder.parse(inputStream);
            return XMLConverter.convertToString(doc, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore, wanted to close anyway
                }
            }
        }
    }

    private List<Report> getReportList(String... reportNames) {
        List<Report> result = new ArrayList<>();
        for (String string : reportNames) {
            Report report = new Report();
            report.setReportName(string);
            result.add(report);
        }
        return result;
    }

    /**
     * Creates a parameter definition for the technical product and also an
     * option for it. Furthermore an event definition for the technical product
     * is created.
     * 
     * @param tProd
     *            The technical product to be updated.
     * @throws NonUniqueBusinessKeyException
     */
    private void prepareTechnicalProduct(TechnicalProduct tProd)
            throws NonUniqueBusinessKeyException {
        ParameterDefinition pd = TechnicalProducts.addParameterDefinition(
                ParameterValueType.INTEGER, "intParam",
                ParameterType.SERVICE_PARAMETER, tProd, mgr, null, null, true);
        ParameterOption option = new ParameterOption();
        option.setOptionId("OPT");
        option.setParameterDefinition(pd);
        List<ParameterOption> list = new ArrayList<>();
        list.add(option);
        pd.setOptionList(list);
        mgr.persist(option);
        Event event = TechnicalProducts.addEvent("eventId",
                EventType.SERVICE_EVENT, tProd, mgr);
        mgr.persist(event);

        LocalizedResource lr = new LocalizedResource();
        lr.setLocale("en");
        lr.setObjectKey(event.getKey());
        lr.setObjectType(LocalizedObjectTypes.EVENT_DESC);
        lr.setValue("en_event");
        mgr.persist(lr);

        LocalizedResource lr2 = new LocalizedResource();
        lr2.setLocale("de");
        lr2.setObjectKey(event.getKey());
        lr2.setObjectType(LocalizedObjectTypes.EVENT_DESC);
        lr2.setValue("de_event");
        mgr.persist(lr2);
    }

    private void addProducts(String custAbrv, Organization supplier,
            TechnicalProduct tProd, int max, List<Product> products)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Product prod;
        ParameterDefinition paramDef = tProd.getParameterDefinitions().get(0);
        for (int i = 1; i < max; i++) {
            prod = Products.createProduct(supplier, tProd, (i % 2 != 1),
                    "Product_" + custAbrv + "_" + i, null, mgr);
            prod.setAutoAssignUserEnabled(Boolean.FALSE);
            Parameter param = new Parameter();
            param.setParameterDefinition(paramDef);
            param.setParameterSet(prod.getParameterSet());
            param.setValue("1");
            mgr.persist(param);

            PricedParameter pricedParam = new PricedParameter();
            pricedParam.setParameter(param);
            pricedParam.setPricePerUser(new BigDecimal(1));
            PriceModel priceModel = prod.getPriceModel();

            SteppedPrice sp = new SteppedPrice();
            sp.setAdditionalPrice(new BigDecimal(123));
            sp.setFreeEntityCount(123);
            sp.setLimit(L123);
            sp.setPrice(new BigDecimal(123));
            sp.setPriceModel(priceModel);
            priceModel.setSteppedPrices(Collections.singletonList(sp));
            SupportedCurrency sc = new SupportedCurrency();
            sc.setCurrency(Currency.getInstance("EUR"));
            sc = (SupportedCurrency) mgr.find(sc);
            priceModel.setCurrency(sc);
            pricedParam.setPriceModel(priceModel);

            priceModel.setOneTimeFee(new BigDecimal(12345L));
            priceModel.setPricePerPeriod(new BigDecimal(67890L));
            priceModel.setPricePerUserAssignment(new BigDecimal(34567L));
            priceModel.setType(PriceModelType.PRO_RATA);

            mgr.persist(priceModel);

            PricedOption option = new PricedOption();
            option.setPricedParameter(pricedParam);
            option.setPricePerUser(new BigDecimal(2));
            option.setParameterOptionKey(
                    paramDef.getOptionList().get(0).getKey());

            PricedEvent pEvent = new PricedEvent();
            pEvent.setEvent(tProd.getEvents().get(0));
            pEvent.setEventPrice(new BigDecimal(50));
            pEvent.setPriceModel(priceModel);

            priceModel.getConsideredEvents().add(pEvent);

            mgr.persist(pricedParam);
            mgr.persist(option);
            mgr.persist(pEvent);

            PaymentTypes.enableForProduct(prod, mgr, ENABLED_PAYMENTTYPES);
            products.add((Product) ReflectiveClone.clone(prod));
        }
    }

    private VOSubscription subscribeToProductAndUse(final String custAbrv,
            int ProductIdx, final int custIdx) throws Exception {
        VOService product = getProductByKey(
                testProducts.get(custAbrv).get(ProductIdx).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler.toVOUser(testUsers
                .get(testOrganizations.get(custAbrv).get(custIdx)).get(1));
        users[0] = UserDataAssembler.toVOUser(testUsers
                .get(testOrganizations.get(custAbrv).get(custIdx)).get(2));
        users[1] = UserDataAssembler.toVOUser(testUsers
                .get(testOrganizations.get(custAbrv).get(custIdx)).get(3));

        final VOPaymentInfo voPaymentInfo = runTX(
                new Callable<VOPaymentInfo>() {
                    @Override
                    public VOPaymentInfo call() throws Exception {
                        return PaymentInfos.createVOPaymentInfo(
                                testOrganizations.get(custAbrv).get(custIdx),
                                mgr, paymentTypes.get(0));
                    }
                });
        final VOBillingContact bc = runTX(new Callable<VOBillingContact>() {

            @Override
            public VOBillingContact call() throws Exception {
                return PaymentInfos.createBillingContact(
                        testOrganizations.get(custAbrv).get(custIdx), mgr);
            }
        });
        VOSubscription newSub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(
                        "subscribeToProductIDX_" + ProductIdx + "_SUP_"
                                + custAbrv + "_CUSIDX_" + custIdx),
                product, getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        eventForCustomers(newSub.getKey(), admins[0].getUserId());
        eventForCustomers(newSub.getKey(), users[0].getUserId());
        eventForCustomers(newSub.getKey(), users[1].getUserId());

        return newSub;
    }

    private void eventForCustomers(final long subscriptionId,
            final String actor) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                // create GATHEREDEVENT
                GatheredEvent gatheredEvent = new GatheredEvent();
                gatheredEvent.setActor(actor);
                gatheredEvent.setOccurrenceTime(TIMESTAMP);
                gatheredEvent.setType(EventType.SERVICE_EVENT);
                gatheredEvent.setEventId(
                        PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
                gatheredEvent.setMultiplier(MULTIPLIER);
                gatheredEvent.setSubscriptionTKey(subscriptionId);
                mgr.persist(gatheredEvent);
                return null;
            }
        });

    }

    /**
     * 
     * mock by saving billing result in DB
     * 
     * @param custAbrv
     * @param custIdx
     * @throws Exception
     */
    private void runBillingForCustomer(final String custAbrv, final int custIdx)
            throws Exception {

        final Organization cust = testOrganizations.get(custAbrv).get(custIdx);
        final long organizationKey = cust.getKey();

        long chargingOrgKey = 0;
        if (custAbrv.equalsIgnoreCase("A")) {
            chargingOrgKey = tpAndSupplierA.getKey();
        } else {
            chargingOrgKey = tpAndSupplierB.getKey();
        }
        final long fchargingOrgKey = chargingOrgKey;
        runTX(new Callable<Void>() {

            @Override
            @SuppressWarnings("boxing")
            public Void call() throws Exception {

                Subscription subscription = Subscriptions.createSubscription(
                        mgr, cust.getOrganizationId(), product);

                BillingResult br = new BillingResult();
                br.setCreationTime(123L);
                br.setOrganizationTKey(organizationKey);
                br.setChargingOrgKey(fchargingOrgKey);
                br.setPeriodStartTime(234L);
                br.setPeriodEndTime(345L);
                br.setResultXML(getTestFileAsString(TEST_XML_FILE));
                br.setNetAmount(BigDecimal.ZERO);
                br.setGrossAmount(BigDecimal.ZERO);
                br.setCurrency(SupportedCurrencies.findOrCreate(mgr, "EUR"));
                br.setSubscriptionKey(subscription.getKey());

                mgr.persist(br);

                PaymentResult pr = new PaymentResult();
                pr.setProcessingStatus(PaymentProcessingStatus.SUCCESS);
                pr.setProcessingTime(TIMESTAMP);
                pr.setBillingResult(br);
                mgr.persist(pr);

                br.setPaymentResult(pr);

                mgr.persist(br);

                billingResultsKeys.put(
                        testOrganizations.get(custAbrv).get(custIdx),
                        br.getKey());

                return null;
            }
        });

    }

    private VOService getProductByKey(final long key) throws Exception {
        return runTX(new Callable<VOService>() {

            @Override
            public VOService call() throws Exception {
                Product product = mgr.getReference(Product.class, key);
                return ProductAssembler.toVOProduct(product,
                        new LocalizerFacade(localizer, "en"));
            }
        });
    }

    @Test
    public void testGetServiceName() throws Exception {
        final Subscription subscription = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Product p = testProducts.get("A").get(0);
                return Subscriptions
                        .createSubscription(mgr,
                                String.valueOf(customerAdminsA[0]
                                        .getOrganization().getOrganizationId()),
                                p);
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BillingResultParser parser = new BillingResultParser(
                        new BillingDao(mgr));
                String serviceName = parser.getServiceName(
                        String.valueOf(subscription.getPriceModel().getKey()));
                assertEquals(
                        subscription.getProduct().getProductId().split("#")[0],
                        serviceName);
                return null;
            }
        });
    }

    @Test
    public void testGetServiceNameWithUpgrade() throws Exception {
        final Subscription subscription = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Product p = testProducts.get("A").get(0);
                p = (Product) mgr.find(p);
                Subscription s = Subscriptions
                        .createSubscription(mgr,
                                String.valueOf(customerAdminsA[0]
                                        .getOrganization().getOrganizationId()),
                                p);

                ProductReference pr = new ProductReference(p,
                        testProducts.get("A").get(1));
                mgr.persist(pr);
                p.getCompatibleProducts().addAll(Collections.singletonList(pr));
                mgr.flush();
                load(s.getProduct().getTemplate().getVendor());
                load(s.getProduct().getTechnicalProduct());
                return s;
            }
        });

        runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                container.login(customerAdminsA[0].getKey(),
                        UserRoleType.ORGANIZATION_ADMIN.toString());

                VOPaymentInfo voPaymentInfo = PaymentInfos.createVOPaymentInfo(
                        testOrganizations.get("A").get(0), mgr,
                        paymentTypes.get(0));
                Product p = testProducts.get("A").get(1);
                p = (Product) mgr.find(p);
                subscription.getProduct().getTechnicalProduct()
                        .setTechnicalProductOperations(null);
                VOSubscription vosub = SubscriptionAssembler.toVOSubscription(
                        subscription, new LocalizerFacade(localizer, "en"));
                VOService voProduct = ProductAssembler.toVOProduct(p,
                        new LocalizerFacade(localizer, "en"));
                VOSubscription upgradeSubscription = subMgmt
                        .upgradeSubscription(vosub, voProduct, voPaymentInfo,
                                BillingContactAssembler.toVOBillingContact(
                                        subscription.getBillingContact()),
                                new ArrayList<VOUda>());
                return String.valueOf(upgradeSubscription.getServiceKey());
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BillingResultParser parser = new BillingResultParser(
                        new BillingDao(mgr));
                Subscription temp = (Subscription) mgr.find(subscription);
                String newServiceName = parser.getServiceName(
                        String.valueOf(temp.getPriceModel().getKey()));
                assertEquals(temp.getProduct().getProductId().split("#")[0],
                        newServiceName);
                return null;
            }
        });
    }

    @Test
    public void detailedBillingReport_Query_SubscriptionTerminated()
            throws Exception {
        container.login(supplierUserA.getKey(), ROLE_SERVICE_MANAGER);
        Map<RDOSummary, Document> result = runTX(
                new Callable<Map<RDOSummary, Document>>() {
                    @Override
                    public Map<RDOSummary, Document> call() throws Exception {
                        for (VOSubscription s : subscriptionsA) {
                            subMgmt.terminateSubscription(s, "");
                        }
                        BillingDao billingDao = new BillingDao(mgr);
                        BillingDetailsReport billingReport = new BillingDetailsReport(
                                billingDao, new UnitDao(mgr), userGroupService);
                        List<ReportBillingData> billingData = billingDao
                                .retrieveBillingDetails(
                                        billingResultsKeys.get(testOrganizations
                                                .get("A").get(0)).longValue(),
                                        tpAndSupplierA.getKey());
                        return billingReport.getBillingData(billingData);
                    }
                });

        assertFalse(result.values().isEmpty());
    }

    /**
     * The setup creates four billing results. The SQL result must contain four
     * rows.
     */
    @Test
    public void platformRevenue_Query() throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                PlatformRevenueDao result = new PlatformRevenueDao(mgr,
                        new Locale(createPlatformOperator().getLocale())
                                .getLanguage());
                result.executeQuery(new Date(0), new Date());

                List<RowData> data = result.getRowData();
                assertEquals(4, data.size());
                assertNotNull(data.get(0).getSupplierName());
                assertNotNull(data.get(0).getSupplierID());
                assertEquals("EUR", data.get(0).getCurrency());
                assertEquals("14681", data.get(0).getAmount().toString());
                return null;
            };
        });
    }

    private PlatformUser createPlatformOperator() {
        final PlatformUser admin = new PlatformUser();
        admin.setLocale("en");
        return admin;
    }

    /**
     * Define a date range that excludes any data.
     */
    @Test
    public void testPlatformRevenueQuery_emptyDateRange() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                PlatformRevenueDao result = new PlatformRevenueDao(mgr,
                        new Locale(createPlatformOperator().getLocale())
                                .getLanguage());
                result.executeQuery(new Date(0), new Date(10));
                List<RowData> data = result.getRowData();
                assertEquals(0, data.size());
                return null;
            }
        });
    }

    @Test
    public void getAvailableReportsForOrgAdmin() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(String.valueOf(supplierUserA.getKey()),
                        UserRoleType.ORGANIZATION_ADMIN.name());
                try {
                    List<VOReport> reports = reportService
                            .getAvailableReportsForOrgAdmin(ReportType.ALL);
                    assertEquals(10, reports.size());
                } catch (EJBException e) {
                    throw e.getCausedByException();
                }
                return null;
            }
        });
    }

    @Test(expected = EJBAccessException.class)
    public void getAvailableReportsForOrgAdmin_NotAuthorized()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(String.valueOf(supplierUserA.getKey()),
                        UserRoleType.SUBSCRIPTION_MANAGER.name());
                try {
                    reportService
                            .getAvailableReportsForOrgAdmin(ReportType.ALL);
                } catch (EJBException e) {
                    throw e.getCausedByException();
                }
                return null;
            }
        });
    }
}
