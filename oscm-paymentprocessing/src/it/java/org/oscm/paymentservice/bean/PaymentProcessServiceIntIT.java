/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 20.01.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.oscm.test.Numbers.TIMESTAMP;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PSPAccount;
import org.oscm.domobjects.PSPAccountHistory;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentResult;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.DateFactory;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.paymentservice.adapter.PaymentServiceProviderAdapter;
import org.oscm.paymentservice.local.PaymentServiceLocal;
import org.oscm.paymentservice.local.PortLocatorLocal;
import org.oscm.paymentservice.transport.HttpClientFactory;
import org.oscm.paymentservice.transport.HttpMethodFactory;
import org.oscm.payproc.stubs.ConfigurationServiceStub;
import org.oscm.payproc.stubs.HttpClientStub;
import org.oscm.payproc.stubs.PostMethodStub;
import org.oscm.psp.data.ChargingData;
import org.oscm.psp.data.ChargingResult;
import org.oscm.psp.data.RequestData;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.types.enumtypes.PaymentProcessingStatus;
import org.oscm.types.exceptions.PSPCommunicationException;

/**
 * Tests for the payment processing using the open ejb container.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PaymentProcessServiceIntIT extends EJBTestBase {

    private static final String USER_KEY_EXISTING = "1";

    private PaymentServiceLocal pp;
    private DataService mgr;
    private PlatformUser user;

    private PaymentServiceProviderAdapter pspMock;
    private PortLocatorLocal portLocatorMock;

    private static final String COSTS_PLACEHOLDER = "<COSTS_PLACEHOLDER/>";
    private static final String RESULT_XML = "<BillingDetails><Period endDate=\"1262300400000\" startDate=\"1259622000000\"/>"
            + "<OrganizationDetails><Name>Name of organization 1000</Name>"
            + "<Address>Address of organization 1000</Address></OrganizationDetails>"
            + "<Subscriptions><Subscription id=\"sub\" purchaseOrderNumber=\"12345\">"
            + "<PriceModels><PriceModel id=\"5\"><UsagePeriod endDate=\"1262300400000\" startDate=\"1259622000000\"/>"
            + "<GatheredEvents/><PeriodFee basePeriod=\"MONTH\" basePrice=\"1000\" factor=\"1.0\" price=\"1000\"/>"
            + "<UserAssignmentCosts basePeriod=\"MONTH\" basePrice=\"100\" factor=\"0.0\" numberOfUsersTotal=\"0\" price=\"0\"/>"
            + "<PriceModelCosts amount=\"1000\"/></PriceModel></PriceModels>"
            + "<SubscriptionCosts amount=\"1000\"/></Subscription>"
            + "<Subscription id=\"sub2\" purchaseOrderNumber=\"\"><PriceModels>"
            + "<PriceModel id=\"6\"><UsagePeriod endDate=\"1262300400000\" startDate=\"1260428424625\"/>"
            + "<GatheredEvents/><PeriodFee basePeriod=\"WEEK\" basePrice=\"500\" factor=\"3.095197379298942\" price=\"1548\"/>"
            + "<UserAssignmentCosts basePeriod=\"DAY\" basePrice=\"4\" factor=\"0.0\" numberOfUsersTotal=\"0\" price=\"0\"/><PriceModelCosts amount=\"1548\"/></PriceModel></PriceModels>"
            + "<SubscriptionCosts amount=\"1548\"/></Subscription>"
            + "<Subscription id=\"sub2\" purchaseOrderNumber=\"\"><PriceModels><PriceModel id=\"6\">"
            + "<UsagePeriod endDate=\"1260039564578\" startDate=\"1259622000000\"/><GatheredEvents/>"
            + "<PeriodFee basePeriod=\"WEEK\" basePrice=\"500\" factor=\"0.6904176223544973\" price=\"345\"/>"
            + "<UserAssignmentCosts basePeriod=\"DAY\" basePrice=\"4\" factor=\"0.0\" numberOfUsersTotal=\"0\" price=\"0\"/>"
            + "<PriceModelCosts amount=\"345\"/></PriceModel></PriceModels>"
            + "<SubscriptionCosts amount=\"345\"/></Subscription>"
            + "<Subscription id=\"sub3\" purchaseOrderNumber=\"\"><PriceModels><PriceModel id=\"7\">"
            + "<UsagePeriod endDate=\"1261335624781\" startDate=\"1259780424671\"/><GatheredEvents/>"
            + "<PeriodFee basePeriod=\"DAY\" basePrice=\"500\" factor=\"18.000001273148147\" price=\"9000\"/>"
            + "<UserAssignmentCosts basePeriod=\"WEEK\" basePrice=\"4\" factor=\"0.0\" numberOfUsersTotal=\"0\" price=\"0\"/>"
            + "<PriceModelCosts amount=\"9000\"/></PriceModel></PriceModels>"
            + "<SubscriptionCosts amount=\"9000\"/>"
            + "</Subscription></Subscriptions>"
            + "<OverallCosts currency=\"EUR\" grossAmount='" + COSTS_PLACEHOLDER
            + "'/></BillingDetails>";

    private ConfigurationServiceStub cs;

    @Override
    public void setup(TestContainer container) throws Exception {
        container.login(USER_KEY_EXISTING);

        user = new PlatformUser();
        user.setLocale("en");

        DataServiceBean ds = new DataServiceBean();
        DataServiceBean dsSpy = spy(ds);
        container.addBean(new org.oscm.test.stubs.ConfigurationServiceStub());
        container.addBean(dsSpy);
        doReturn(user).when(dsSpy).getCurrentUser();

        cs = new ConfigurationServiceStub();
        container.addBean(cs);
        container.addBean(mock(AccountService.class));
        container.addBean(mock(BillingDataRetrievalServiceLocal.class));
        container.addBean(mock(ApplicationServiceLocal.class));

        pspMock = mock(PaymentServiceProviderAdapter.class);
        when(pspMock.charge(any(RequestData.class), any(ChargingData.class)))
                .thenReturn(new ChargingResult());

        portLocatorMock = mock(PortLocatorLocal.class);
        when(portLocatorMock.getPort(anyString())).thenReturn(pspMock);

        container.addBean(portLocatorMock);
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(new PaymentServiceBean());

        // lookup bean references
        pp = container.get(PaymentServiceLocal.class);
        mgr = container.get(DataService.class);
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                createOrganizationRoles(mgr);
                createPaymentTypes(mgr);
                SupportedCountries.createOneSupportedCountry(mgr);
                return null;
            }
        });

        HttpMethodFactory.setTestMode(true);
        HttpClientFactory.setTestMode(true);
        PostMethodStub
                .setStubReturnValue(PaymentProcessServiceTest.sampleResponse);
        HttpClientStub.reset();
    }

    @Test
    public void testChargeCustomerNoHitForOrg() {
        BillingResult br = initBillingResult();
        boolean chargeCustomer = pp.chargeCustomer(br);

        assertFalse("Operation should have ended with a problem report",
                chargeCustomer);
    }

    /**
     * Creates a plain billing result for the test.
     * 
     * @return The initialized billing result.
     */
    private BillingResult initBillingResult() {
        BillingResult br = new BillingResult();
        br.setOrganizationTKey(1L);
        br.setPeriodStartTime(0L);
        br.setPeriodEndTime(0L);
        return br;
    }

    @Test
    public void testChargeCustomerNoHitForPaymentInfoHistory()
            throws Exception {
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization cust = new Organization();
                cust.setOrganizationId("testOrg");
                cust.setName(
                        "Name of organization " + cust.getOrganizationId());
                cust.setAddress(
                        "Address of organization " + cust.getOrganizationId());
                cust.setEmail(cust.getOrganizationId() + "@organization.com");
                cust.setPhone("012345/678" + cust.getOrganizationId());
                cust.setCutOffDay(1);
                mgr.persist(cust);
                return cust;
            }
        });
        BillingResult br = initBillingResult();
        boolean chargeCustomer = pp.chargeCustomer(br);

        assertFalse("Operation should have ended with a problem report",
                chargeCustomer);
    }

    @Test
    public void testChargeCustomerVerifyTotalCostsOnlySomeCents()
            throws Exception {
        BillingResult br = initBillingResultAndCreateOrg(0.03);

        boolean success = pp.chargeCustomer(br);

        assertTrue("Wrong operation result", success);
        assertNotNull("No payment result was set", br.getPaymentResult());
        PaymentResult pr = br.getPaymentResult();
        assertEquals("wrong status in payment result",
                PaymentProcessingStatus.SUCCESS, pr.getProcessingStatus());
        assertNull(
                "No exception must be stored for the payment result, as the processing succeeded",
                pr.getProcessingException());
    }

    @Test
    public void testChargeCustomerVerifyPaymentResultCreationGoodCase()
            throws Exception {
        BillingResult br = initBillingResultAndCreateOrg(12345);

        boolean success = pp.chargeCustomer(br);

        assertTrue("Wrong operation result", success);
        assertNotNull("No payment result was set", br.getPaymentResult());
        PaymentResult pr = br.getPaymentResult();
        assertEquals("wrong status in payment result",
                PaymentProcessingStatus.SUCCESS, pr.getProcessingStatus());
        assertNull(
                "No exception must be stored for the payment result, as the processing succeeded",
                pr.getProcessingException());
    }

    @Test
    public void testChargeCustomerVerifyPaymentResultCreationDuplicateInvocation()
            throws Exception {
        BillingResult br = initBillingResultAndCreateOrg(12345);

        pp.chargeCustomer(br);

        PaymentResult pr = br.getPaymentResult();
        long processingTime = pr.getProcessingTime();
        int version = pr.getVersion();
        long key = pr.getKey();

        boolean chargeCustomer = pp.chargeCustomer(br);

        pr = br.getPaymentResult();

        assertTrue("Wrong result of re-invocation of payment processing",
                chargeCustomer);
        assertEquals("Processing time must not be changed", processingTime,
                pr.getProcessingTime());
        assertEquals("Wrong payment result key", key, pr.getKey());
        assertEquals("Wrong payment result version", version, pr.getVersion());
    }

    @Test
    public void testChargeCustomerVerifyPaymentResultCreationRetryScenario()
            throws Exception {
        BillingResult br = initBillingResultAndCreateOrg(12345);
        when(pspMock.charge(any(RequestData.class), any(ChargingData.class)))
                .thenThrow(
                        new PSPCommunicationException("Excetion for testing"));

        boolean success = pp.chargeCustomer(br);

        assertFalse("Wrong operation result", success);
        assertNotNull("No payment result was set", br.getPaymentResult());
        PaymentResult pr = br.getPaymentResult();
        assertEquals("wrong status in payment result",
                PaymentProcessingStatus.RETRY, pr.getProcessingStatus());
        assertNotNull(
                "An exception must be stored for the payment result, as the processing failed",
                pr.getProcessingException());
        assertNull("no psp response must be stored", pr.getProcessingResult());
        assertTrue("payment result not persisted", pr.getKey() > 0);
    }

    @Test
    public void testReinvokePaymentProcessing() throws Exception {
        BillingResult br = initBillingResultAndCreateOrg(12345);
        PaymentResult pr = initPaymentResult(br, PaymentProcessingStatus.RETRY);

        PostMethodStub
                .setStubReturnValue(PaymentProcessServiceTest.sampleResponse);
        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Boolean.valueOf(pp.reinvokePaymentProcessing());
            }
        });

        Assert.assertTrue("Processing must not have failed",
                result.booleanValue());

        pr = findDO(PaymentResult.class, pr);
        assertEquals("wrong status in payment result",
                PaymentProcessingStatus.SUCCESS, pr.getProcessingStatus());
        assertNull("No exception must be stored for the payment result",
                pr.getProcessingException());
        assertTrue("payment result not persisted", pr.getKey() > 0);

    }

    @Test
    public void testReinvokePaymentProcessingMultiplePaymentResults()
            throws Exception {
        BillingResult br1 = initBillingResultAndCreateOrg(12345);
        PaymentResult pr1 = initPaymentResult(br1,
                PaymentProcessingStatus.RETRY);

        BillingResult br2 = initBillingResultAndCreateOrg(123);
        PaymentResult pr2 = initPaymentResult(br2,
                PaymentProcessingStatus.SUCCESS);

        BillingResult br3 = initBillingResultAndCreateOrg(1);
        PaymentResult pr3 = initPaymentResult(br3,
                PaymentProcessingStatus.FAILED_INTERNAL);

        PostMethodStub
                .setStubReturnValue(PaymentProcessServiceTest.sampleResponse);
        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Boolean.valueOf(pp.reinvokePaymentProcessing());
            }
        });

        Assert.assertTrue("Processing must not have failed",
                result.booleanValue());

        pr1 = findDO(PaymentResult.class, pr1);
        assertEquals("wrong status in payment result",
                PaymentProcessingStatus.SUCCESS, pr1.getProcessingStatus());
        assertNull("No exception must be stored for the payment result",
                pr1.getProcessingException());
        assertTrue("payment result not persisted", pr1.getKey() > 0);

        // assert that the other payment result objects are not changed
        pr2 = findDO(PaymentResult.class, pr2);
        assertEquals("wrong status in payment result",
                PaymentProcessingStatus.SUCCESS, pr2.getProcessingStatus());

        pr3 = findDO(PaymentResult.class, pr3);
        assertEquals("wrong status in payment result",
                PaymentProcessingStatus.FAILED_INTERNAL,
                pr3.getProcessingStatus());

    }

    @Test
    public void testReinvokePaymentProcessingExternalFailure()
            throws Exception {
        BillingResult br = initBillingResultAndCreateOrg(12345);
        PaymentResult pr = initPaymentResult(br, PaymentProcessingStatus.RETRY);

        when(pspMock.charge(any(RequestData.class), any(ChargingData.class)))
                .thenThrow(new RuntimeException());

        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Boolean.valueOf(pp.reinvokePaymentProcessing());
            }
        });

        Assert.assertFalse("Processing must have failed",
                result.booleanValue());

        pr = findDO(PaymentResult.class, pr);
        assertNotNull(
                "The created exception must be stored for the payment result",
                pr.getProcessingException());
        assertTrue("payment result not persisted", pr.getKey() > 0);

    }

    @Test
    public void testChargeForOutstandingBillsExternalFailure()
            throws Exception {
        final BillingResult br = initBillingResultAndCreateOrg(12345);
        when(pspMock.charge(any(RequestData.class), any(ChargingData.class)))
                .thenThrow(new RuntimeException("Exception for testing"));

        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Boolean.valueOf(pp.chargeForOutstandingBills());

            }
        });

        Assert.assertFalse(
                "Processing must indicate problem as PSP returned NOK",
                result.booleanValue());

        PaymentResult pr = runTX(new Callable<PaymentResult>() {
            @Override
            public PaymentResult call() {
                BillingResult billingResult = mgr.find(BillingResult.class,
                        br.getKey());
                PaymentResult paymentResult = billingResult.getPaymentResult();
                Assert.assertNotNull(paymentResult);
                return paymentResult;
            }
        });

    }

    @Test
    public void testChargeForOutstandingBills_TransactionTime_B10533()
            throws Exception {
        initBillingResultAndCreateOrg(12345);

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                long initialTransactionTime = DateFactory.getInstance()
                        .getTransactionTime();
                pp.chargeForOutstandingBills();
                assertFalse("Transaction time not set",
                        initialTransactionTime == DateFactory.getInstance()
                                .getTransactionTime());
                return null;
            }
        });
    }

    @Test
    public void testChargeForOutstandingBills() throws Exception {
        final BillingResult br = initBillingResultAndCreateOrg(12345);

        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Boolean.valueOf(pp.chargeForOutstandingBills());
            }
        });

        Assert.assertTrue(
                "Processing must indicate no problem as PSP returned NOK",
                result.booleanValue());

        PaymentResult pr = runTX(new Callable<PaymentResult>() {
            @Override
            public PaymentResult call() {
                BillingResult billingResult = mgr.find(BillingResult.class,
                        br.getKey());
                PaymentResult paymentResult = billingResult.getPaymentResult();
                Assert.assertNotNull(paymentResult);
                return paymentResult;
            }
        });

    }

    @Test
    public void testChargeForOutstandingBillsBroker() throws Exception {
        final BillingResult br = initBillingResultAndCreateOrg(12345,
                CREDIT_CARD, true);

        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Boolean.valueOf(pp.chargeForOutstandingBills());
            }
        });

        Assert.assertTrue(
                "Processing must indicate no problem as PSP returned NOK",
                result.booleanValue());

        PaymentResult pr = runTX(new Callable<PaymentResult>() {
            @Override
            public PaymentResult call() {
                BillingResult billingResult = mgr.find(BillingResult.class,
                        br.getKey());
                PaymentResult paymentResult = billingResult.getPaymentResult();
                Assert.assertNotNull(paymentResult);
                return paymentResult;
            }
        });

    }

    @Test
    public void testChargeForOutstandingBillsInvoice() throws Exception {
        final BillingResult br = initBillingResultAndCreateOrg(12345, INVOICE,
                false);

        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Boolean.valueOf(pp.chargeForOutstandingBills());
            }
        });

        Assert.assertTrue(
                "Processing must indicate no problem as PSP returned NOK",
                result.booleanValue());

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                BillingResult billingResult = mgr.find(BillingResult.class,
                        br.getKey());
                Assert.assertNull(
                        "Invoice: There must not be a payment result!",
                        billingResult.getPaymentResult());
                return null;
            }
        });
    }

    @Test
    public void testChargeForOutstandingBillsAlreadyExistingEntries()
            throws Exception {
        BillingResult br = initBillingResultAndCreateOrg(12345);
        final PaymentResult pr = initPaymentResult(br,
                PaymentProcessingStatus.RETRY);

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                pp.reinvokePaymentProcessing();
                return null;
            }
        });
        final BillingResult br2 = initBillingResultAndCreateOrg(12346);
        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Boolean.valueOf(pp.chargeForOutstandingBills());
            }
        });

        Assert.assertTrue("Billing result handling must have succeeded",
                result.booleanValue());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PaymentResult oldPR = mgr.getReference(PaymentResult.class,
                        pr.getKey());

                Assert.assertEquals(PaymentProcessingStatus.SUCCESS,
                        oldPR.getProcessingStatus());

                // Furthermore, a new payment result must exist
                BillingResult billingResult = mgr
                        .getReference(BillingResult.class, br2.getKey());
                PaymentResult secondPR = billingResult.getPaymentResult();

                Assert.assertEquals(PaymentProcessingStatus.SUCCESS,
                        secondPR.getProcessingStatus());

                return null;
            }
        });
    }

    @Test
    public void testChargeForOutstandingBillsNoPSPEnabled() throws Exception {
        initBillingResultAndCreateOrg(12345);
        cs.setPSPUsageEnabled(false);
        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Boolean.valueOf(pp.reinvokePaymentProcessing());
            }
        });
        Assert.assertTrue("If no PSP is enabled, result must always be true",
                result.booleanValue());
    }

    /**
     * Returns the most recent version of the domain object as present in the
     * db.
     * 
     * @param type
     * @param obj
     * @param <T>
     * @return The domain object.
     * @throws Exception
     * 
     */
    private <T extends DomainObject<?>> T findDO(final Class<T> type,
            final T obj) throws Exception {
        return runTX(new Callable<T>() {
            @Override
            public T call() {
                return mgr.find(type, obj.getKey());
            }
        });
    }

    /**
     * Returns the history entries for one domain object.
     * 
     * @param obj
     *            The domain object to find the history for.
     * @return The history.
     * @throws Exception
     */
    private List<?> getHistory(final DomainObject<?> obj) throws Exception {
        return runTX(new Callable<List<?>>() {
            @Override
            public List<?> call() {
                List<DomainHistoryObject<?>> hist = mgr.findHistory(obj);
                return hist;
            }
        });
    }

    /**
     * Creates an initial payment result object for the given billing result
     * object with the given status.
     * 
     * @param br
     *            The billing result object the payment processing was based on.
     * @param status
     *            The status the payment result object should have.
     * @return Returns the initialized payment result.
     * @throws Exception
     */
    private PaymentResult initPaymentResult(final BillingResult br,
            final PaymentProcessingStatus status) throws Exception {
        return runTX(new Callable<PaymentResult>() {
            @Override
            public PaymentResult call() throws Exception {
                PaymentResult pr = new PaymentResult();
                pr.setProcessingStatus(status);
                pr.setProcessingTime(TIMESTAMP);
                pr.setBillingResult(br);

                br.setPaymentResult(pr);

                mgr.persist(pr);
                return pr;
            }
        });
    }

    private BillingResult initBillingResultAndCreateOrg(
            final double overallCosts) throws Exception {
        return initBillingResultAndCreateOrg(overallCosts, CREDIT_CARD, false);
    }

    /**
     * Creates and persists a billing result object with the given price and
     * creates the required corresponding organization.
     * 
     * @param overallCosts
     *            The overall costs to be set.
     * @return The billing result object according to the specified costs.
     * @throws Exception
     */
    private BillingResult initBillingResultAndCreateOrg(
            final double overallCosts, final String paymentType,
            final boolean asBroker) throws Exception {
        return runTX(new Callable<BillingResult>() {
            @Override
            public BillingResult call() throws Exception {
                // SUPPPLIER
                Organization supplier = new Organization();
                supplier.setOrganizationId("supId" + new Random().nextLong());
                OrganizationToRole supplierRelation = new OrganizationToRole();
                OrganizationRole role = new OrganizationRole();
                role.setRoleName(OrganizationRoleType.SUPPLIER);
                supplierRelation.setOrganization(supplier);
                supplierRelation.setOrganizationRole(role);
                Set<OrganizationToRole> roles = new HashSet<>();
                roles.add(supplierRelation);
                supplier.setGrantedRoles(roles);
                supplier.setCutOffDay(1);
                mgr.persist(supplier);

                Organization broker = null;
                if (asBroker) {
                    broker = Organizations.createOrganization(mgr,
                            OrganizationRoleType.BROKER);
                }

                // CUSTOMER
                Organization customer = Organizations.createCustomer(mgr,
                        supplier);

                // BILLING-RESULT
                BillingResult br = new BillingResult();
                br.setCreationTime(TIMESTAMP);
                br.setOrganizationTKey(customer.getKey());
                br.setChargingOrgKey(supplier.getKey());
                br.setResultXML(RESULT_XML.replace(COSTS_PLACEHOLDER,
                        String.valueOf(overallCosts)));
                br.setPeriodEndTime(0L);
                br.setPeriodStartTime(0L);
                br.setNetAmount(BigDecimal.ZERO);
                br.setGrossAmount(BigDecimal.TEN);
                br.setCurrency(SupportedCurrencies.findOrCreate(mgr, "EUR"));
                mgr.persist(br);

                role = new OrganizationRole();
                role.setRoleName(OrganizationRoleType.CUSTOMER);
                role = (OrganizationRole) mgr.getReferenceByBusinessKey(role);

                OrganizationRefToPaymentType apt = new OrganizationRefToPaymentType();
                Organization reloadedOrg = mgr.getReference(Organization.class,
                        customer.getKey());
                apt.setOrganizationReference(reloadedOrg.getSources().get(0));
                apt.setPaymentType(findPaymentType(paymentType, mgr));
                apt.setOrganizationRole(role);
                apt.setUsedAsDefault(false);
                mgr.persist(apt);

                Date d = new Date();

                // PAYMENT INFO
                PaymentInfo pi = new PaymentInfo();
                pi.setOrganization(customer);
                pi.setPaymentType(apt.getPaymentType());
                pi.setPaymentInfoId("name");
                mgr.persist(pi);

                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, supplier, "techProd_" + System.currentTimeMillis(),
                        false, ServiceAccessType.LOGIN);
                Product product = Products.createProductWithoutPriceModel(
                        supplier, tp, "MyProduct");
                product.setStatus(ServiceStatus.ACTIVE);
                mgr.persist(product);
                if (asBroker) {
                    product = Products.createProductResaleCopy(product, broker,
                            mgr);
                }

                Subscription newSub = Subscriptions.createPartnerSubscription(
                        mgr, customer.getOrganizationId(),
                        product.getProductId(), "mySubscriptionId",
                        broker == null ? supplier : broker);
                newSub.setPaymentInfo(pi);
                newSub.setCreationDate(Long.valueOf(100000000));
                newSub.setActivationDate(Long.valueOf(100000000));
                newSub.setCutOffDay(1);
                mgr.persist(newSub);

                br.setSubscriptionKey(Long.valueOf(newSub.getKey()));
                mgr.persist(br);

                // PSP ACCOUNT
                PSPAccount pspa = new PSPAccount();
                pspa.setOrganization(supplier);
                pspa.setPsp(apt.getPaymentType().getPsp());
                pspa.setPspIdentifier(
                        apt.getPaymentType().getPsp().getIdentifier());

                // PSP ACCOUNT HISTORY
                PSPAccountHistory pspah = new PSPAccountHistory();
                pspah.setInvocationDate(d);
                pspah.setModdate(d);
                pspah.setModtype(ModificationType.ADD);
                pspah.setModuser("1");
                pspah.setObjKey(pspa.getKey());
                pspah.setOrganizationObjKey(supplier.getKey());
                pspah.setPspIdentifier(pspa.getPspIdentifier());
                pspah.setPspObjKey(pspa.getPsp().getKey());
                mgr.persist(pspah);

                return br;
            }
        });
    }
}
