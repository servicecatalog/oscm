/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date:  2013-5-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.reportingservice.dao.BillingDao.ReportBillingData;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.data.UserGroups;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author Wenxin Gao
 * 
 */
@SuppressWarnings("boxing")
public class BillingDaoIT extends EJBTestBase {
    private BillingDao dao;
    private DataService ds;
    private long subscriptionKey = 10L;
    private long billingCreationTime = 1000L;
    private String billingResultXml = "<BillingDetails key=\"10001\"></BillingDetails>";
    private static long NON_EXISTING_BILLING_KEY = 647823L;
    private static final String NON_EXISTING_SUPPLIER_ID = "NonExistingSupplier";
    private static final String SUPPLIER_ORG_ID = "Supplier";
    private static final String CUSTOMER_ORG_ID = "Customer";
    private static final String UNIT_NAME = "test unit";

    private Organization supplierOrg;
    private UserGroup testUnit;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new BillingDao(ds);

        supplierOrg = createOrganization(SUPPLIER_ORG_ID);
        testUnit = createUnit(UNIT_NAME);
    }

    @Test
    public void retrieveBillingDetails_Bug9896() throws Exception {
        // given
        createSupportedCountriesAndCurrencies();
        createSubscriptionHistory(subscriptionKey, supplierOrg.getKey(),
                "2013-01-21 09:00:00", 19, ModificationType.ADD,
                SubscriptionStatus.ACTIVE, 100L, 200L);
        final BillingResult br = createBillingResult(1L, 1L,
                Long.valueOf(subscriptionKey), 1000L, supplierOrg.getKey(), 0L);

        // when
        List<ReportBillingData> result = retrieveBillingDetails(br.getKey(),
                supplierOrg.getKey(), null);

        // then
        assertEquals(1, result.size());
        ReportBillingData data = result.get(0);
        assertEquals(billingResultXml, data.getBillingResult());
        assertEquals(billingCreationTime, data.getDate());
    }

    @Test
    public void retrieveBillingDetailsWithUnit() throws Exception {
        // given
        createSupportedCountriesAndCurrencies();
        createSubscriptionHistory(subscriptionKey, supplierOrg.getKey(),
                "2013-01-21 09:00:00", 19, ModificationType.ADD,
                SubscriptionStatus.ACTIVE, 100L, 200L);
        final BillingResult br = createBillingResult(1L, 1L,
                Long.valueOf(subscriptionKey), 1000L, supplierOrg.getKey(), 0L);

        // when
        List<ReportBillingData> result = retrieveBillingDetails(br.getKey(),
                supplierOrg.getKey(), null);

        // then
        assertEquals(1, result.size());
    }

    private List<ReportBillingData> retrieveBillingDetails(
            final long billingKey, final long supplierKey,
            final List<Long> unitKeys) throws Exception {
        return runTX(new Callable<List<ReportBillingData>>() {
            @Override
            public List<ReportBillingData> call() throws Exception {
                if (unitKeys == null) {
                    return dao.retrieveBillingDetails(billingKey, supplierKey);
                } else {
                    return dao.retrieveBillingDetails(billingKey, supplierKey,
                            unitKeys);
                }
            }
        });
    }

    @Test
    public void retrieveSupplierBillingBySupplierId_nonExistingSupplierId()
            throws Exception {
        // when
        List<ReportResultData> reportData = retrieveSupplierBillingBySupplierId(
                NON_EXISTING_SUPPLIER_ID);

        // then
        assertTrue(reportData.isEmpty());
    }

    private List<ReportResultData> retrieveSupplierBillingBySupplierId(
            final String supplierOrgId) throws Exception {
        return runTX(new Callable<List<ReportResultData>>() {
            @Override
            public List<ReportResultData> call() throws Exception {
                // when
                return dao.retrieveSupplierBillingBySupplierId(supplierOrgId);
            }
        });
    }

    @Test
    public void retrieveSupplierBillingBySupplierId() throws Exception {
        // given
        final Organization customer = createOrganization(CUSTOMER_ORG_ID);
        createSupportedCountriesAndCurrencies();
        createSubscriptionHistory(2L, customer.getKey(), "2013-01-21 09:00:00",
                19, ModificationType.ADD, SubscriptionStatus.ACTIVE, 100L,
                200L);
        BillingResult br = createBillingResult(0L, 1L, 2L, customer.getKey(),
                supplierOrg.getKey(), 0L);

        // when
        List<ReportResultData> reportData = retrieveSupplierBillingBySupplierId(
                supplierOrg.getOrganizationId());

        // then
        assertEquals(1, reportData.size());
        assertEquals(asString(br.getCreationTime()), valueOf(reportData, 0));
        assertEquals(asString(br.getPeriodStartTime()), valueOf(reportData, 1));
        assertEquals(asString(br.getPeriodEndTime()), valueOf(reportData, 2));
        assertEquals(CUSTOMER_ORG_ID, valueOf(reportData, 3));
        assertEquals(br.getResultXML(), valueOf(reportData, 4));
        assertEquals(asString(br.getKey()), valueOf(reportData, 5));
    }

    @Test
    public void retrieveBillingDetailsForUnitOfSubscription() throws Exception {
        // given
        createSupportedCountriesAndCurrencies();
        UserGroup unit = createUnit("unit", supplierOrg, null);
        Product product = createProduct("serviceA", "techServiceA",
                supplierOrg.getOrganizationId(), ServiceAccessType.LOGIN);
        Subscription sub = createSubscription(supplierOrg.getOrganizationId(),
                product.getProductId(), "SubscriptionId", supplierOrg);
        assignSubscriptionToUnit(sub, unit);
        createSubscriptionHistory(sub.getKey(), supplierOrg.getKey(),
                "2013-01-21 09:00:00", 19, ModificationType.ADD,
                SubscriptionStatus.ACTIVE, 100L, 200L);
        final BillingResult br = createBillingResult(1L, 1L,
                Long.valueOf(sub.getKey()), supplierOrg.getKey(),
                supplierOrg.getKey(), 0L);

        // when
        List<ReportBillingData> result = retrieveBillingDetails(br.getKey(),
                supplierOrg.getKey(),
                Collections.singletonList(Long.valueOf(unit.getKey())));

        // then
        assertEquals(1, result.size());
        assertEquals(br.getResultXML(), result.get(0).getBillingResult());
    }

    @Test
    public void retrieveBillingDetailsForUnitOfBillingResult()
            throws Exception {
        // given
        createSupportedCountriesAndCurrencies();
        UserGroup unit = createUnit("unit", supplierOrg, null);
        Product product = createProduct("serviceA", "techServiceA",
                supplierOrg.getOrganizationId(), ServiceAccessType.LOGIN);
        Subscription sub = createSubscription(supplierOrg.getOrganizationId(),
                product.getProductId(), "SubscriptionId", supplierOrg);
        createSubscriptionHistory(sub.getKey(), supplierOrg.getKey(),
                "2013-01-21 09:00:00", 19, ModificationType.ADD,
                SubscriptionStatus.ACTIVE, 100L, 200L);
        final BillingResult br = createBillingResult(1L, 1L,
                Long.valueOf(sub.getKey()), supplierOrg.getKey(),
                supplierOrg.getKey(), unit.getKey());

        // when
        List<ReportBillingData> result = retrieveBillingDetails(br.getKey(),
                supplierOrg.getKey(),
                Collections.singletonList(Long.valueOf(unit.getKey())));

        // then
        assertEquals(1, result.size());
        assertEquals(br.getResultXML(), result.get(0).getBillingResult());
    }

    @Test
    public void retrieveBillingDetailsForUnitNoRights1() throws Exception {
        // given
        createSupportedCountriesAndCurrencies();
        UserGroup unit = createUnit("unit", supplierOrg, null);
        Product product = createProduct("serviceA", "techServiceA",
                supplierOrg.getOrganizationId(), ServiceAccessType.LOGIN);
        Subscription sub = createSubscription(supplierOrg.getOrganizationId(),
                product.getProductId(), "SubscriptionId", supplierOrg);
        createSubscriptionHistory(sub.getKey(), supplierOrg.getKey(),
                "2013-01-21 09:00:00", 19, ModificationType.ADD,
                SubscriptionStatus.ACTIVE, 100L, 200L);
        final BillingResult br = createBillingResult(1L, 1L,
                Long.valueOf(sub.getKey()), supplierOrg.getKey(),
                supplierOrg.getKey(), 0L);

        // when
        List<ReportBillingData> result = retrieveBillingDetails(br.getKey(),
                supplierOrg.getKey(),
                Collections.singletonList(Long.valueOf(unit.getKey())));

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void retrieveBillingDetailsForUnitNoRights2() throws Exception {
        // given
        createSupportedCountriesAndCurrencies();
        UserGroup unit = createUnit("unit", supplierOrg, null);
        Product product = createProduct("serviceA", "techServiceA",
                supplierOrg.getOrganizationId(), ServiceAccessType.LOGIN);
        Subscription sub = createSubscription(supplierOrg.getOrganizationId(),
                product.getProductId(), "SubscriptionId", supplierOrg);
        assignSubscriptionToUnit(sub, testUnit);
        createSubscriptionHistory(sub.getKey(), supplierOrg.getKey(),
                "2013-01-21 09:00:00", 19, ModificationType.ADD,
                SubscriptionStatus.ACTIVE, 100L, 200L);
        final BillingResult br = createBillingResult(1L, 1L,
                Long.valueOf(sub.getKey()), supplierOrg.getKey(),
                supplierOrg.getKey(), testUnit.getKey());

        // when
        List<ReportBillingData> result = retrieveBillingDetails(br.getKey(),
                supplierOrg.getKey(),
                Collections.singletonList(Long.valueOf(unit.getKey())));

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void retrieveBillingDetailsMoreUnits() throws Exception {
        // given
        createSupportedCountriesAndCurrencies();
        UserGroup unit0 = createUnit("unit0", supplierOrg, null);
        UserGroup unit1 = createUnit("unit1", supplierOrg, null);
        UserGroup unit2 = createUnit("unit2", supplierOrg, null);
        UserGroup unit3 = createUnit("unit3", supplierOrg, null);
        UserGroup unit4 = createUnit("unit4", supplierOrg, null);
        Product product = createProduct("serviceA", "techServiceA",
                supplierOrg.getOrganizationId(), ServiceAccessType.LOGIN);
        Subscription sub = createSubscription(supplierOrg.getOrganizationId(),
                product.getProductId(), "SubscriptionId", supplierOrg);
        createSubscriptionHistory(sub.getKey(), supplierOrg.getKey(),
                "2013-01-21 09:00:00", 19, ModificationType.ADD,
                SubscriptionStatus.ACTIVE, 100L, 200L);
        assignSubscriptionToUnit(sub, testUnit);
        final BillingResult br = createBillingResult(1L, 1L,
                Long.valueOf(sub.getKey()), supplierOrg.getKey(),
                supplierOrg.getKey(), unit0.getKey());

        // when
        List<ReportBillingData> result = retrieveBillingDetails(br.getKey(),
                supplierOrg.getKey(),
                Arrays.asList(Long.valueOf(unit0.getKey()),
                        Long.valueOf(unit1.getKey()),
                        Long.valueOf(unit2.getKey()),
                        Long.valueOf(unit3.getKey()),
                        Long.valueOf(unit4.getKey()),
                        Long.valueOf(testUnit.getKey())));

        // then
        assertEquals(1, result.size());
        assertEquals(br.getResultXML(), result.get(0).getBillingResult());
    }

    @Test
    public void retrieveBillingDetailsForUnitEmptyList() throws Exception {
        // given

        // when
        List<ReportBillingData> result = retrieveBillingDetails(1000L, 1000L,
                Collections.<Long> emptyList());

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void retrieveBillingDetailsForUnitNull() throws Exception {
        // given

        // when
        List<ReportBillingData> result = retrieveBillingDetails(1000L, 1000L,
                null);

        // then
        assertEquals(0, result.size());
    }

    private Subscription createSubscription(final String customerId,
            final String productId, final String subscriptionId,
            final Organization supplier) throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.createSubscription(ds, customerId,
                        productId, subscriptionId, supplier);
            }
        });
    }

    private Subscription assignSubscriptionToUnit(
            final Subscription subscription, final UserGroup unit)
            throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.assignToUnit(ds, subscription, unit);
            }
        });
    }

    private Product createProduct(final String productId,
            final String techProductId, final String organizationId,
            final ServiceAccessType type) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                return Products.createProduct(organizationId, productId,
                        techProductId, ds, type);
            }
        });
    }

    private UserGroup createUnit(final String name, final Organization org,
            final PlatformUser user) throws Exception {
        return runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws Exception {
                return UserGroups.createUserGroup(ds, name, org, false,
                        "Description", "refId", user);
            }
        });
    }

    private String valueOf(List<ReportResultData> reportData, int position) {
        return reportData.get(0).getColumnValue().get(position).toString();
    }

    private String asString(long value) {
        return String.valueOf(value);
    }

    /**
     * create an organization in DB for supplier
     * 
     * @return
     * @throws Exception
     */
    private Organization createOrganization(final String organizationId)
            throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds, organizationId);
            }
        });
    }

    private UserGroup createUnit(final String name) throws Exception {
        return runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws Exception {
                return UserGroups.createUserGroup(ds, name, supplierOrg, false,
                        "", "", null);
            }
        });
    }

    /**
     * create supported countries and currencies in DB
     * 
     * @throws Exception
     */
    private void createSupportedCountriesAndCurrencies() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCountries.createOneSupportedCountry(ds);
                SupportedCurrencies.createOneSupportedCurrency(ds);
                return null;
            }
        });
    }

    /**
     * create a SubscriptionHistory in DB
     * 
     * @param subscriptionObjKey
     * @param customerOrganizationKey
     * @param modificationDate
     * @param version
     * @param modificationType
     * @param subscriptionStatus
     * @param productobjKey
     * @param mpObjKey
     * @throws Exception
     */
    private void createSubscriptionHistory(final long subscriptionObjKey,
            final long customerOrganizationKey, final String modificationDate,
            final int version, final ModificationType modificationType,
            final SubscriptionStatus subscriptionStatus,
            final long productobjKey, final long mpObjKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscriptions.createSubscriptionHistory(ds, subscriptionObjKey,
                        customerOrganizationKey, modificationDate, version,
                        modificationType, subscriptionStatus, productobjKey,
                        mpObjKey);
                return null;
            }

        });
    }

    /**
     * create billingResult in DB
     * 
     * @param periodStart
     * @param periodEnd
     * @param subscriptionKey
     * @param customerOrgKey
     * @param sellerKey
     * @throws Exception
     */
    private BillingResult createBillingResult(final long periodStart,
            final long periodEnd, final Long subscriptionKey,
            final long customerOrgKey, final long sellerKey, final long unitKey)
            throws Exception {
        return runTX(new Callable<BillingResult>() {
            @Override
            public BillingResult call() throws Exception {
                BillingResult br = new BillingResult();
                br.setCreationTime(billingCreationTime);
                br.setOrganizationTKey(customerOrgKey);
                br.setPeriodEndTime(periodEnd);
                br.setPeriodStartTime(periodStart);
                br.setResultXML(billingResultXml);
                br.setChargingOrgKey(sellerKey);
                br.setCurrency(SupportedCurrencies.findOrCreate(ds, "EUR"));
                br.setNetAmount(BigDecimal.TEN);
                br.setGrossAmount(BigDecimal.TEN);
                br.setSubscriptionKey(subscriptionKey);
                br.setVendorKey(sellerKey);
                if (unitKey != 0) {
                    br.setUsergroupKey(unitKey);
                }
                ds.persist(br);
                return br;
            }
        });
    }

    @Test
    public void retrieveBillingDetailsByKey_nonExistingKey() throws Exception {
        // when
        List<ReportBillingData> billingDetails = retrieveBillingDetailsByKey(
                NON_EXISTING_BILLING_KEY);

        // then
        assertTrue(billingDetails.isEmpty());
    }

    private List<ReportBillingData> retrieveBillingDetailsByKey(
            final long billingKey) throws Exception {
        return runTX(new Callable<List<ReportBillingData>>() {
            @Override
            public List<ReportBillingData> call() {
                return dao.retrieveBillingDetailsByKey(billingKey);
            }
        });
    }

    @Test
    public void retrieveBillingDetailsByKey() throws Exception {
        // given
        Organization supplier = createOrganization("supplier");
        BillingResult br = createBillingResult(0L, 1L, 2L, 3L,
                supplier.getKey(), 0L);

        // when
        List<ReportBillingData> billingDetails = retrieveBillingDetailsByKey(
                br.getKey());

        // then
        assertEquals(1, billingDetails.size());
        assertEquals(billingDetails.get(0).getDate(), br.getCreationTime());
        assertEquals(billingDetails.get(0).getBillingResult(),
                br.getResultXML());

    }
}
