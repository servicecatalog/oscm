/********************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                             
 *  Author: schmid                                              
 *                                                                             
 *  Creation Date: 20.01.2009                                                      
 *                                                                              
 *  Completion Time:                                               
 *                                                                              
 ********************************************************************************/
package org.oscm.domobjects;

import static org.oscm.test.Numbers.TIMESTAMP;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PaymentInfos;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * Tests of the organization-related domain objects (incl. auditing
 * functionality)
 * 
 * @author schmid
 */
public class SubscriptionIT extends DomainObjectTestBase {

    private ArrayList<Subscription> subscriptions = new ArrayList<Subscription>();
    private ArrayList<PlatformUser> users = new ArrayList<PlatformUser>();
    private ArrayList<Product> products = new ArrayList<Product>();
    private Organization supplier;
    private Organization broker;
    private Organization customer;
    private Organization platformOperator;
    private PlatformUser admin;
    private PriceModel priceModel;
    private Marketplace marketplace;
    private Product product;

    @Override
    protected void dataSetup() throws Exception {
        SupportedCountries.createSomeSupportedCountries(mgr);
        SupportedCurrency sc = new SupportedCurrency();
        sc.setCurrency(Currency.getInstance("EUR"));
        mgr.persist(sc);
    }

    private void prepareSomeObjects(String testname)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        platformOperator = Organizations.createOrganization(mgr,
                OrganizationRoleType.PLATFORM_OPERATOR);
        supplier = Organizations.createOrganization(mgr,
                OrganizationRoleType.SUPPLIER);
        Marketplaces.ensureMarketplace(supplier, null, mgr);
        customer = Organizations.createCustomer(mgr, supplier);

        // allow invoice payment type for supplier
        OrganizationReference orgRef = new OrganizationReference(
                platformOperator, supplier,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
        mgr.persist(orgRef);

        OrganizationRefToPaymentType ortpt = new OrganizationRefToPaymentType();
        OrganizationRoleType orgRoleType = OrganizationRoleType.SUPPLIER;
        OrganizationRole orgRole = new OrganizationRole(orgRoleType);
        orgRole = (OrganizationRole) mgr.getReferenceByBusinessKey(orgRole);
        ortpt.setOrganizationRole(orgRole);
        ortpt.setPaymentType(getInvoicePaymentType());
        ortpt.setOrganizationReference(orgRef);
        mgr.persist(ortpt);

        // Add 3 Users
        users.clear();
        for (int i = 0; i < 3; i++) {
            PlatformUser usr1 = new PlatformUser();
            usr1.setAdditionalName("AddName1");
            usr1.setAddress("Address1");
            usr1.setCreationDate(GregorianCalendar.getInstance().getTime());
            usr1.setEmail("EMail1");
            usr1.setFirstName("Arnold");
            usr1.setLastName("Schwarzenegger");
            usr1.setUserId("usr" + i);
            usr1.setPhone("111111/111111");
            usr1.setStatus(UserAccountStatus.ACTIVE);
            usr1.setOrganization(supplier);
            usr1.setLocale("en");
            supplier.addPlatformUser(usr1);
            mgr.persist(usr1);
            users.add((PlatformUser) ReflectiveClone.clone(usr1));
        }
        // Add 2 products
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                supplier, testname, false, ServiceAccessType.LOGIN);
        Product prod;
        products.clear();
        for (int i = 1; i < 3; i++) {
            prod = new Product();
            prod.setVendor(supplier);
            prod.setProductId(testname + "Product" + i);
            prod.setTechnicalProduct(tProd);
            prod.setProvisioningDate(TIMESTAMP);
            prod.setStatus(ServiceStatus.ACTIVE);
            prod.setType(ServiceType.TEMPLATE);
            ParameterSet emptyPS = new ParameterSet();
            prod.setParameterSet(emptyPS);
            PriceModel pm = new PriceModel();
            if (i % 2 != 0) {
                pm.setType(PriceModelType.PRO_RATA);
                pm.setPeriod(PricingPeriod.DAY);
                pm.setPricePerPeriod(new BigDecimal(1L));
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("EUR"));
                sc = (SupportedCurrency) mgr.find(sc);
                pm.setCurrency(sc);
                prod.setPriceModel(pm);
            }
            prod.setPriceModel(pm);
            mgr.persist(prod);
            products.add((Product) ReflectiveClone.clone(prod));
        }
    }

    /**
     * <b>Testcase:</b> Add new Subscription objects <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>All objects can be retrieved from DB and are identical to provided
     * Platform user objects</li>
     * <li>Relations to the organization are set (bidirectional)</li>
     * <li>A history object is created for each Subscription stored</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testAdd() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAddCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestAdd() throws SaaSApplicationException {
        String testname = "SubTestAdd";
        prepareSomeObjects(testname);
        // Add a new subscription for product 1
        Subscription sub1 = new Subscription();
        sub1.setCreationDate(TIMESTAMP);
        sub1.setStatus(SubscriptionStatus.ACTIVE);
        sub1.setSubscriptionId(testname + "1");
        sub1.setOrganization(supplier);
        sub1.bindToProduct(products.get(0));
        Assert.assertNotNull("Subscribed product has no supplier", products
                .get(0).getVendor());
        Marketplace mp = Marketplaces.ensureMarketplace(products.get(0)
                .getVendor(), "marketplaceId", mgr);
        Assert.assertNotNull("Supplier has no local marketplace", mp);
        sub1.setMarketplace(mp);
        sub1.addUser(users.get(0), null);
        sub1.setProductInstanceId("prod1");
        sub1.setPurchaseOrderNumber("PON for Testing");
        sub1.setCutOffDay(1);
        sub1.setUuid(UUID.randomUUID());
        mgr.persist(sub1);
        subscriptions.clear();
        subscriptions.add(sub1);
    }

    private void doTestAddCheck() {
        resolveOrganization();
        String testname = "SubTestAdd";
        Subscription qry = new Subscription();
        Subscription saved;
        Subscription orgSub = subscriptions.get(0);
        qry.setSubscriptionId(testname + "1");
        qry.setOrganizationKey(supplier.getKey());
        saved = (Subscription) mgr.find(qry);
        Assert.assertNotNull("Cannot find '" + orgSub.getSubscriptionId()
                + "' in DB", saved);
        Assert.assertTrue(ReflectiveCompare.showDiffs(saved, orgSub),
                ReflectiveCompare.compare(saved, orgSub));

        // Check relation to organization
        boolean condition = ReflectiveCompare.compare(saved.getOrganization(),
                supplier);
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(saved.getOrganization(), supplier),
                condition);

        // Check relation to product
        Assert.assertTrue(ReflectiveCompare.showDiffs(saved.getProduct(),
                products.get(0)), ReflectiveCompare.compare(saved.getProduct(),
                products.get(0)));
        // Check relation to user
        // should be exactly one
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgSub.getUsageLicenses()
                .get(0), saved.getUsageLicenses().get(0)), ReflectiveCompare
                .compare(orgSub.getUsageLicenses().get(0), saved
                        .getUsageLicenses().get(0)));
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(orgSub.getUsageLicenses().get(0)
                        .getUser(), saved.getUsageLicenses().get(0).getUser()),
                ReflectiveCompare.compare(orgSub.getUsageLicenses().get(0)
                        .getUser(), saved.getUsageLicenses().get(0).getUser()));
        Assert.assertEquals("Purchase order number not stored",
                "PON for Testing", saved.getPurchaseOrderNumber());
        // Load history objects and check them
        // H1) Check subscription history object
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        Assert.assertNotNull("History entry 'null' for subscription "
                + orgSub.getSubscriptionId());
        Assert.assertFalse(
                "History entry empty for subscription "
                        + orgSub.getSubscriptionId(), histObjs.isEmpty());
        Assert.assertTrue("Only one history entry expected for subscription "
                + orgSub.getSubscriptionId(), histObjs.size() == 1);
        DomainHistoryObject<?> hist = histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgSub, hist),
                ReflectiveCompare.compare(orgSub, hist));
        Assert.assertEquals("OBJID in history different", orgSub.getKey(),
                hist.getObjKey());
        SubscriptionData subHistData = (SubscriptionData) hist
                .getDataContainer();
        Assert.assertEquals("Wrong purchase order number in history data",
                "PON for Testing", subHistData.getPurchaseOrderNumber());
        Assert.assertTrue(hist instanceof SubscriptionHistory);
        Assert.assertEquals("Wrong marketplace key in history data", saved
                .getMarketplace().getKey(), ((SubscriptionHistory) hist)
                .getMarketplaceObjKey().longValue());
        // H2) Check usageLicense history object
        histObjs = mgr.findHistory(saved.getUsageLicenses().get(0));
        Assert.assertNotNull("History entry 'null' for UsageLicense 0 of subscription "
                + orgSub.getSubscriptionId());
        Assert.assertFalse(
                "History entry empty for UsageLicense 0 of subscription "
                        + orgSub.getSubscriptionId(), histObjs.isEmpty());
        Assert.assertTrue(
                "Only one history entry expected for UsageLicense 0 of subscription "
                        + orgSub.getSubscriptionId(), histObjs.size() == 1);
        hist = histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgSub.getUsageLicenses()
                .get(0), hist), ReflectiveCompare.compare(orgSub
                .getUsageLicenses().get(0), hist));
        Assert.assertEquals("OBJID in history different", orgSub
                .getUsageLicenses().get(0).getKey(), hist.getObjKey());
        // H3) Check PriceModel history object (the one copied into the
        // subscription)
        histObjs = mgr.findHistory(saved.getPriceModel());
        Assert.assertNotNull("History entry 'null' for PriceModel of subscription "
                + orgSub.getSubscriptionId());
        Assert.assertFalse(
                "History entry empty for PriceModel of subscription "
                        + orgSub.getSubscriptionId(), histObjs.isEmpty());
        Assert.assertTrue(
                "Only one history entry expected for PriceModel of subscription "
                        + orgSub.getSubscriptionId(), histObjs.size() == 1);
        hist = histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(orgSub.getPriceModel(), hist),
                ReflectiveCompare.compare(orgSub.getPriceModel(), hist));
        Assert.assertEquals("OBJID in history different", orgSub
                .getPriceModel().getKey(), hist.getObjKey());
        // H4) Check organization (no separate history object should be created)
        histObjs = mgr.findHistory(saved.getOrganization());
        Assert.assertNotNull(
                "History entry 'null' for organization of subscription "
                        + orgSub.getSubscriptionId(), histObjs);
        Assert.assertEquals(
                "2 history entries expected for organization of subscription "
                        + orgSub.getSubscriptionId(), 2, histObjs.size());
        // H5) Check product (no separate history object should be created)
        histObjs = mgr.findHistory(saved.getProduct());
        Assert.assertNotNull("History entry 'null' for product of subscription "
                + orgSub.getSubscriptionId());
        Assert.assertTrue(
                "Only one history entry expected for product of subscription "
                        + orgSub.getSubscriptionId(), histObjs.size() == 1);
        // H6) Check PlatformUser (no separate history object should be created)
        histObjs = mgr.findHistory(users.get(0));
        Assert.assertNotNull("History entry 'null' for user of subscription "
                + orgSub.getSubscriptionId());
        Assert.assertTrue(
                "Only one history entry expected for user of subscription "
                        + orgSub.getSubscriptionId(), histObjs.size() == 1);
    }

    private void resolveOrganization() {
        mgr.find(Organization.class, supplier.getKey());
    }

    /**
     * <b>Testcase:</b> Modify an existing Subscription object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Modification is saved to the DB</li>
     * <li>History object created for the subscription</li>
     * <li>No history object created for the priceModel (unchanged)</li>
     * <li>No new history object for UsageLicense (unchanged)</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testModifySubscription() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifySubPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifySub();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifySubCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestModifySubPrepare() throws SaaSApplicationException {
        String testname = "SubTestModSub";
        prepareSomeObjects(testname);
        // Add a new subscription for product 1
        Subscription sub1 = new Subscription();
        sub1.setCreationDate(TIMESTAMP);
        sub1.setStatus(SubscriptionStatus.ACTIVE);
        sub1.setSubscriptionId(testname + "1");
        sub1.setOrganization(supplier);
        sub1.bindToProduct(products.get(0));
        sub1.addUser(users.get(0), null);
        sub1.setProductInstanceId("prod1");
        sub1.setCutOffDay(1);
        sub1.setUuid(UUID.randomUUID());
        mgr.persist(sub1);
        subscriptions.clear();
        subscriptions.add(sub1);
    }

    private void doTestModifySub() {
        String testname = "SubTestModSub";
        Subscription qry = new Subscription();
        qry.setSubscriptionId(testname + "1");
        qry.setOrganizationKey(supplier.getKey());
        Subscription sub = (Subscription) mgr.find(qry);
        sub.setProductInstanceId("http://thisisanewurl.com");
        subscriptions.clear();
        subscriptions.add((Subscription) ReflectiveClone.clone(sub));
    }

    private void doTestModifySubCheck() {
        String testname = "SubTestModSub";
        Subscription qry = new Subscription();
        Subscription saved;
        Subscription orgSub = subscriptions.get(0);
        orgSub = mgr.find(Subscription.class, orgSub.getKey());
        qry.setSubscriptionId(testname + "1");
        qry.setOrganizationKey(supplier.getKey());
        saved = (Subscription) mgr.find(qry);
        Assert.assertNotNull("Cannot find '" + orgSub.getSubscriptionId()
                + "' in DB", saved);
        Assert.assertTrue(ReflectiveCompare.showDiffs(saved, orgSub),
                ReflectiveCompare.compare(saved, orgSub));
        // Check relation to organization
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(saved.getOrganization(), supplier),
                ReflectiveCompare.compare(saved.getOrganization(), supplier));
        // Check relation to product
        Assert.assertTrue(ReflectiveCompare.showDiffs(saved.getProduct(),
                products.get(0)), ReflectiveCompare.compare(saved.getProduct(),
                products.get(0)));
        // Check relation to user
        // should be exactly one
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgSub.getUsageLicenses()
                .get(0), saved.getUsageLicenses().get(0)), ReflectiveCompare
                .compare(orgSub.getUsageLicenses().get(0), saved
                        .getUsageLicenses().get(0)));
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(orgSub.getUsageLicenses().get(0)
                        .getUser(), saved.getUsageLicenses().get(0).getUser()),
                ReflectiveCompare.compare(orgSub.getUsageLicenses().get(0)
                        .getUser(), saved.getUsageLicenses().get(0).getUser()));
        // Check price model
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(orgSub.getPriceModel(),
                        saved.getPriceModel()),
                ReflectiveCompare.compare(orgSub.getPriceModel(),
                        saved.getPriceModel()));
        // Load history objects and check them
        // H1) Check subscription history object
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        Assert.assertNotNull("History entry 'null' for subscription "
                + orgSub.getSubscriptionId());
        Assert.assertFalse(
                "History entry empty for subscription "
                        + orgSub.getSubscriptionId(), histObjs.isEmpty());
        Assert.assertTrue(
                "Exactly 2 history entries expected for subscription "
                        + orgSub.getSubscriptionId(), histObjs.size() == 2);
        // Check modification entry, i.e. the second one
        DomainHistoryObject<?> hist = histObjs.get(1);
        Assert.assertEquals(ModificationType.MODIFY, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgSub, hist),
                ReflectiveCompare.compare(orgSub, hist));
        Assert.assertEquals("OBJID in history different", orgSub.getKey(),
                hist.getObjKey());
        // H2) Check usageLicense history object (should be unchanged, as this
        // is a collection and none of the entries had a dirty flag set)
        histObjs = mgr.findHistory(saved.getUsageLicenses().get(0));
        Assert.assertNotNull("History entry 'null' for UsageLicense 0 of subscription "
                + orgSub.getSubscriptionId());
        Assert.assertFalse(
                "History entry empty for UsageLicense 0 of subscription "
                        + orgSub.getSubscriptionId(), histObjs.isEmpty());
        Assert.assertTrue(
                "Only one history entry expected for UsageLicense 0 of subscription "
                        + orgSub.getSubscriptionId(), histObjs.size() == 1);
        // H3) Check PriceModel history object (the one copied into the
        // subscription) --> should be unchanged, as no dirtyFlag set
        histObjs = mgr.findHistory(saved.getPriceModel());
        Assert.assertNotNull("History entry 'null' for PriceModel of subscription "
                + orgSub.getSubscriptionId());
        Assert.assertFalse(
                "History entry empty for PriceModel of subscription "
                        + orgSub.getSubscriptionId(), histObjs.isEmpty());
        Assert.assertTrue(
                "Exactly 1 history entry expected for PriceModel of subscription "
                        + orgSub.getSubscriptionId(), histObjs.size() == 1);
    }

    /**
     * <b>Testcase:</b> Modify PriceModel of an existing Subscription object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Modification is saved to the DB</li>
     * <li>No History object created for the subscription</li>
     * <li>History object created for the priceModel</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testModifySubPriceModel() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifySubPriceModelPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifySubPriceModel();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifySubPriceModelCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestModifySubPriceModelPrepare()
            throws SaaSApplicationException {
        String testname = "SubTestModSubPriceModel";
        prepareSomeObjects(testname);
        // Add a new subscription for product 1
        Subscription sub1 = new Subscription();
        sub1.setCreationDate(TIMESTAMP);
        sub1.setStatus(SubscriptionStatus.ACTIVE);
        sub1.setSubscriptionId(testname + "1");
        sub1.setOrganization(supplier);
        sub1.bindToProduct(products.get(0));
        sub1.addUser(users.get(0), null);
        sub1.setProductInstanceId("prod1");
        sub1.setCutOffDay(1);
        sub1.setUuid(UUID.randomUUID());
        mgr.persist(sub1);
        subscriptions.clear();
        subscriptions.add(sub1);
    }

    private void doTestModifySubPriceModel() {
        String testname = "SubTestModSubPriceModel";
        Subscription qry = new Subscription();
        qry.setSubscriptionId(testname + "1");
        qry.setOrganizationKey(supplier.getKey());
        Subscription sub = (Subscription) mgr.find(qry);
        subscriptions.clear();
        subscriptions.add(sub);
        priceModel = sub.getPriceModel();
        load(priceModel);
    }

    private void doTestModifySubPriceModelCheck() {
        String testname = "SubTestModSubPriceModel";
        Subscription qry = new Subscription();
        Subscription saved;
        Subscription orgSub = subscriptions.get(0);
        qry.setSubscriptionId(testname + "1");
        qry.setOrganizationKey(supplier.getKey());
        saved = (Subscription) mgr.find(qry);
        Assert.assertNotNull("Cannot find '" + orgSub.getSubscriptionId()
                + "' in DB", saved);
        Assert.assertTrue(ReflectiveCompare.showDiffs(saved, orgSub),
                ReflectiveCompare.compare(saved, orgSub));
        // Check price model
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(priceModel, saved.getPriceModel()),
                ReflectiveCompare.compare(orgSub.getPriceModel(),
                        saved.getPriceModel()));
        // Load history objects and check them
        // H1) Check subscription history object
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        Assert.assertNotNull("History entry 'null' for subscription "
                + orgSub.getSubscriptionId());
        Assert.assertFalse(
                "History entry empty for subscription "
                        + orgSub.getSubscriptionId(), histObjs.isEmpty());
        Assert.assertTrue("Exactly 1 history entry expected for subscription "
                + orgSub.getSubscriptionId(), histObjs.size() == 1);
        // H2) Check usageLicense history object (should be unchanged, as this
        // is a collection and none of the entries had a dirty flag set)
        histObjs = mgr.findHistory(saved.getUsageLicenses().get(0));
        Assert.assertNotNull("History entry 'null' for UsageLicense 0 of subscription "
                + orgSub.getSubscriptionId());
        Assert.assertEquals(
                "Only one history entry expected for UsageLicense 0 of subscription "
                        + orgSub.getSubscriptionId(), 1, histObjs.size());
        // H3) Check PriceModel history object (the one copied into the
        // subscription) --> should contain a new entry
        histObjs = mgr.findHistory(saved.getPriceModel());
        Assert.assertNotNull("History entry 'null' for PriceModel of subscription "
                + orgSub.getSubscriptionId());
        Assert.assertEquals(
                "Exactly 1 history entries expected for PriceModel of subscription "
                        + orgSub.getSubscriptionId(), 1, histObjs.size());
        // Check modification entry, i.e. the second one
        DomainHistoryObject<?> hist = histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(saved.getPriceModel(), hist),
                ReflectiveCompare.compare(saved.getPriceModel(), hist));
        Assert.assertEquals("OBJID in history different", saved.getPriceModel()
                .getKey(), hist.getObjKey());
    }

    /**
     * <b>Testcase:</b> Delete an existing Subscription <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Subscription is deleted</li>
     * <li>PriceModel is deleted</li>
     * <li>UsageLicenses are deleted</li>
     * <li>User is still present</li>
     * <li>Product is still present</li>
     * <li>Organization is still present</li>
     * <li>History object created for the subscription</li>
     * <li>History object created for the pricemodel</li>
     * <li>History object created for the usagelicenses</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testDeleteSubscription() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteSubPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteSub();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteSubCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestDeleteSubPrepare() throws SaaSApplicationException {
        String testname = "SubTestDelSub";
        prepareSomeObjects(testname);
        // Add a new subscription for product 1
        Subscription sub1 = new Subscription();
        sub1.setCreationDate(TIMESTAMP);
        sub1.setStatus(SubscriptionStatus.ACTIVE);
        sub1.setSubscriptionId(testname + "1");
        sub1.setOrganization(supplier);
        sub1.bindToProduct(products.get(0));
        Assert.assertNotNull("Subscribed product has no supplier", products
                .get(0).getVendor());
        Marketplace mp = Marketplaces.ensureMarketplace(products.get(0)
                .getVendor(), "xyz", mgr);
        Assert.assertNotNull("Supplier has no local marketplace", mp);
        sub1.setMarketplace(mp);
        sub1.addUser(users.get(0), null);
        sub1.setProductInstanceId("prod1");
        sub1.setCutOffDay(1);
        sub1.setUuid(UUID.randomUUID());
        mgr.persist(sub1);
        subscriptions.clear();
        subscriptions.add(sub1);
    }

    private void doTestDeleteSub() {
        String testname = "SubTestDelSub";
        Subscription qry = new Subscription();
        qry.setSubscriptionId(testname + "1");
        qry.setOrganizationKey(supplier.getKey());
        Subscription sub = (Subscription) mgr.find(qry);
        mgr.remove(sub);

    }

    private void doTestDeleteSubCheck() {
        String testname = "SubTestDelSub";
        // Try to load deleted
        Subscription orgSub = subscriptions.get(0);
        Subscription qrySub = new Subscription();
        qrySub.setSubscriptionId(testname + "1");
        qrySub.setOrganizationKey(supplier.getKey());
        Subscription saved = (Subscription) mgr.find(qrySub);
        Assert.assertNull("Deleted Subscription '" + orgSub.getSubscriptionId()
                + "' can still be accessed via DataManager.find", saved);
        // Load history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(orgSub);
        Assert.assertNotNull(
                "History entry 'null' for Subscription "
                        + orgSub.getSubscriptionId(), histObjs);
        Assert.assertFalse(
                "History entry empty for Subscription "
                        + orgSub.getSubscriptionId(), histObjs.isEmpty());
        Assert.assertEquals(
                "Exactly 2 history entries expected for Subscription "
                        + orgSub.getSubscriptionId(), 2, histObjs.size());
        // load modified history object (should be second)
        DomainHistoryObject<?> hist = histObjs.get(1);
        Assert.assertEquals(ModificationType.DELETE, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgSub, hist),
                ReflectiveCompare.compare(orgSub, hist));
        Assert.assertEquals("OBJID in history different", orgSub.getKey(),
                hist.getObjKey());
        Assert.assertTrue(hist instanceof SubscriptionHistory);
        Assert.assertEquals("Wrong marketplace key in history data", orgSub
                .getMarketplace().getKey(), ((SubscriptionHistory) hist)
                .getMarketplaceObjKey().longValue());

        // PriceModel, UsageLicenses and Marketplace have no business key => try
        // to load by
        // id
        PriceModel savedPM = mgr.find(PriceModel.class, orgSub.getPriceModel()
                .getKey());
        Assert.assertNotNull(
                "PriceModel of subscription '" + orgSub.getSubscriptionId()
                        + "' must not have been removed automatically", savedPM);
        UsageLicense savedUL = mgr.find(UsageLicense.class, orgSub
                .getUsageLicenses().get(0).getKey());
        Assert.assertNull(
                "Deleted UsageLicense of subscription '"
                        + orgSub.getSubscriptionId()
                        + "' can still be accessed via DataManager.find",
                savedUL);
        Marketplace mp = mgr.find(Marketplace.class, orgSub.getMarketplace()
                .getKey());
        Assert.assertNotNull(
                "Marketplace related to subscription '"
                        + orgSub.getSubscriptionId()
                        + "' must not have been removed automatically", mp);
        // Load history objects for PriceModel and check them
        histObjs = mgr.findHistory(orgSub.getPriceModel());
        Assert.assertNotNull(
                "History entry 'null' for PriceModel of Subscription "
                        + orgSub.getSubscriptionId(), histObjs);
        Assert.assertFalse(
                "History entry empty for PriceModel of Subscription "
                        + orgSub.getSubscriptionId(), histObjs.isEmpty());
        Assert.assertTrue(
                "Exactly 1 history entry expected for PriceModel of Subscription "
                        + orgSub.getSubscriptionId(), histObjs.size() == 1);
        // Load history object for UsageLicense and check them
        histObjs = mgr.findHistory(orgSub.getUsageLicenses().get(0));
        Assert.assertNotNull(
                "History entry 'null' for License of Subscription "
                        + orgSub.getSubscriptionId(), histObjs);
        Assert.assertFalse("History entry empty for License of Subscription "
                + orgSub.getSubscriptionId(), histObjs.isEmpty());
        Assert.assertTrue(
                "Exactly 2 history entries expected for License of Subscription "
                        + orgSub.getSubscriptionId(), histObjs.size() == 2);
        // load modified history object (should be second)
        hist = histObjs.get(1);
        Assert.assertEquals(ModificationType.DELETE, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgSub.getUsageLicenses()
                .get(0), hist), ReflectiveCompare.compare(orgSub
                .getUsageLicenses().get(0), hist));
        Assert.assertEquals("OBJID in history different", orgSub
                .getUsageLicenses().get(0).getKey(), hist.getObjKey());

        // Organization, Product and Users shall still be here
        Assert.assertNotNull("Organization not found !",
                mgr.find(Organization.class, orgSub.getOrganization().getKey()));
        Product qryProduct = new Product();
        qryProduct.setVendor(supplier);
        qryProduct.setProductId(testname + "Product1");
        Assert.assertNotNull("Product not found !", mgr.find(qryProduct));
        PlatformUser qryUser = new PlatformUser();
        qryUser.setUserId("usr1");
        Assert.assertNotNull("User not found !", mgr.find(qryUser));
    }

    /**
     * <b>Testcase:</b> Try to insert two Subscription with the same
     * subscriptionId<br>
     * <b>ExpectedResult:</b> SaasNonUniqueBusinessKeyException
     * 
     * @throws Throwable
     */
    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testViolateUniqueConstraint() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestViolateUniqueConstraintPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestViolateUniqueConstraint();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestViolateUniqueConstraintPrepare()
            throws SaaSApplicationException {
        String testname = "SubTestNonUnique";
        prepareSomeObjects(testname);
        // Add a new subscription for product 1
        Subscription sub1 = new Subscription();
        sub1.setCreationDate(TIMESTAMP);
        sub1.setStatus(SubscriptionStatus.ACTIVE);
        sub1.setSubscriptionId(testname + "1");
        sub1.setOrganization(supplier);
        sub1.bindToProduct(products.get(0));
        sub1.addUser(users.get(0), null);
        sub1.setProductInstanceId("prod1");
        sub1.setCutOffDay(1);
        sub1.setUuid(UUID.randomUUID());
        mgr.persist(sub1);
    }

    private void doTestViolateUniqueConstraint()
            throws SaaSApplicationException {
        String testname = "SubTestNonUnique";
        Subscription sub1 = new Subscription();
        sub1.setCreationDate(TIMESTAMP);
        sub1.setStatus(SubscriptionStatus.ACTIVE);
        sub1.setSubscriptionId(testname + "1");
        sub1.setOrganization(supplier);
        sub1.bindToProduct(products.get(0));
        sub1.addUser(users.get(0), null);
        sub1.setProductInstanceId("prod1");
        sub1.setCutOffDay(1);
        sub1.setUuid(UUID.randomUUID());
        mgr.persist(sub1);
    }

    /**
     * <b>Testcase:</b> Try to insert Subscription without a related
     * Organization<br>
     * <b>ExpectedResult:</b> PersistenceException
     * 
     * @throws Throwable
     */
    @Test(expected = EJBTransactionRolledbackException.class)
    public void testSubscriptionWithoutOrganization() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestSubscriptionWithoutOrganization();
                return null;
            }
        });
    }

    private void doTestSubscriptionWithoutOrganization()
            throws SaaSApplicationException {
        String testname = "SubTestNoOrganization";
        prepareSomeObjects(testname);
        // Add a new subscription for product 1
        Subscription sub1 = new Subscription();
        sub1.setCreationDate(TIMESTAMP);
        sub1.setStatus(SubscriptionStatus.ACTIVE);
        sub1.setSubscriptionId(testname + "1");
        sub1.bindToProduct(products.get(0));
        sub1.addUser(users.get(0), null);
        sub1.setProductInstanceId("prod1");
        sub1.setCutOffDay(1);
        sub1.setUuid(UUID.randomUUID());
        mgr.persist(sub1);
    }

    /**
     * <b>Testcase:</b> Try to insert Subscription without a related Product<br>
     * <b>ExpectedResult:</b> PersistenceException
     * 
     * @throws Throwable
     */
    @Test(expected = EJBTransactionRolledbackException.class)
    public void testSubscriptionWithoutProduct() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestSubscriptionWithoutProduct();
                return null;
            }
        });
    }

    private void doTestSubscriptionWithoutProduct()
            throws SaaSApplicationException {
        String testname = "SubTestNoProduct";
        prepareSomeObjects(testname);
        // Add a new subscription for product 1
        Subscription sub1 = new Subscription();
        sub1.setCreationDate(TIMESTAMP);
        sub1.setStatus(SubscriptionStatus.ACTIVE);
        sub1.setSubscriptionId(testname + "1");
        sub1.setOrganization(supplier);
        sub1.addUser(users.get(0), null);
        sub1.setProductInstanceId("prod1");
        sub1.setCutOffDay(1);
        sub1.setUuid(UUID.randomUUID());
        mgr.persist(sub1);
    }

    /**
     * <b>Testcase:</b> Modify the usedPayment of an existing organization
     * object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Modification is saved to the DB</li>
     * <li>History object created for the organization</li>
     * <li>History object created for the PaymentInfo</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testModifyPaymentInfo() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyPaymentInfoPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyPaymentInfo();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyPaymentInfoCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestModifyPaymentInfoPrepare()
            throws SaaSApplicationException {

        String testname = "SubTestModPaymentInfo";
        prepareSomeObjects(testname);
        // Add a new subscription for product 1
        Subscription sub1 = new Subscription();
        sub1.setCreationDate(TIMESTAMP);
        sub1.setStatus(SubscriptionStatus.ACTIVE);
        sub1.setSubscriptionId(testname);
        sub1.setOrganization(supplier);
        sub1.bindToProduct(products.get(0));
        sub1.addUser(users.get(0), null);
        sub1.setProductInstanceId("prod1");
        sub1.setCutOffDay(1);

        PaymentInfo pi = PaymentInfos.createPaymentInfo(customer, mgr,
                getInvoicePaymentType());
        BillingContact bc = PaymentInfos.createBillingContact(mgr, customer);

        mgr.persist(pi);
        sub1.setBillingContact(bc);
        sub1.setPaymentInfo(pi);
        sub1.setUuid(UUID.randomUUID());
        mgr.persist(sub1);
        subscriptions.clear();
        subscriptions.add(sub1);
    }

    /**
     * Returns the persisted domain object for the invoice payment type.
     * 
     * @return The invoice payment type.
     * @throws ObjectNotFoundException
     */
    private PaymentType getInvoicePaymentType() throws ObjectNotFoundException {
        PaymentType paymentType = new PaymentType();
        paymentType.setPaymentTypeId("INVOICE");
        paymentType = (PaymentType) mgr.getReferenceByBusinessKey(paymentType);
        return paymentType;
    }

    private void doTestModifyPaymentInfo() throws SaaSApplicationException {
        String testname = "SubTestModPaymentInfo";
        Subscription qry = new Subscription();
        qry.setSubscriptionId(testname);
        qry.setOrganizationKey(supplier.getKey());
        Subscription sub = (Subscription) mgr.find(qry);
        // Modify and persist
        PaymentInfo pi = sub.getPaymentInfo();
        supplier = mgr.getReference(Organization.class, supplier.getKey());
        List<OrganizationRefToPaymentType> paymentTypes = supplier
                .getSourcesForType(
                        OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER)
                .get(0).getPaymentTypes();
        PaymentType pt = null;
        for (OrganizationRefToPaymentType currentType : paymentTypes) {
            if (currentType.getPaymentType().getPaymentTypeId().equals(INVOICE)) {
                pt = currentType.getPaymentType();
            }
        }
        pi.setPaymentType(pt);
        pi.setPaymentInfoId(pi.getPaymentInfoId() + "mod");
        mgr.persist(pi);
        subscriptions.clear();
        subscriptions.add((Subscription) ReflectiveClone.clone(sub));
    }

    private void doTestModifyPaymentInfoCheck() {
        String testname = "SubTestModPaymentInfo";
        Subscription saved;
        Subscription orgSub = subscriptions.get(0);
        orgSub = mgr.find(Subscription.class, orgSub.getKey());
        Subscription qry = new Subscription();
        qry.setSubscriptionId(testname);
        qry.setOrganizationKey(supplier.getKey());
        saved = (Subscription) mgr.find(qry);

        Assert.assertNotNull("Cannot find '" + orgSub.getSubscriptionId()
                + "' in DB", saved);
        Assert.assertTrue(ReflectiveCompare.showDiffs(saved, orgSub),
                ReflectiveCompare.compare(saved, orgSub));
        // Check price model
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(orgSub.getPriceModel(),
                        saved.getPriceModel()),
                ReflectiveCompare.compare(orgSub.getPriceModel(),
                        saved.getPriceModel()));
        // Load history objects and check them
        // H1) Check subscription history object
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        Assert.assertNotNull("History entry 'null' for subscription "
                + orgSub.getSubscriptionId());
        Assert.assertFalse(
                "History entry empty for subscription "
                        + orgSub.getSubscriptionId(), histObjs.isEmpty());
        Assert.assertTrue("Exactly 1 history entry expected for subscription "
                + orgSub.getSubscriptionId(), histObjs.size() == 1);
        // H2) Check usageLicense history object (should be unchanged, as this
        // is a collection and none of the entries had a dirty flag set)
        histObjs = mgr.findHistory(saved.getUsageLicenses().get(0));
        Assert.assertNotNull("History entry 'null' for UsageLicense 0 of subscription "
                + orgSub.getSubscriptionId());
        Assert.assertEquals(
                "Only one history entry expected for UsageLicense 0 of subscription "
                        + orgSub.getSubscriptionId(), 1, histObjs.size());
        // H3) Check PaymentInfo history object --> there should be a new entry
        PaymentInfo orgPI = orgSub.getPaymentInfo();
        PaymentInfo savedPI = saved.getPaymentInfo();
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgPI, savedPI),
                ReflectiveCompare.compare(orgPI, savedPI));
        histObjs = mgr.findHistory(orgPI);
        Assert.assertNotNull(
                "History entry 'null' for PaymentInfo of subscription "
                        + orgSub.getSubscriptionId(), histObjs);
        Assert.assertEquals(
                "Exactly 2 history entries expected for PaymentInfo of subscription "
                        + orgSub.getSubscriptionId(), 2, histObjs.size());
        // load added history object
        PaymentInfoHistory hist = (PaymentInfoHistory) histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertEquals("OBJID in history different", orgPI.getKey(),
                hist.getObjKey());

        // load modified history object
        hist = (PaymentInfoHistory) histObjs.get(1);
        Assert.assertEquals(ModificationType.MODIFY, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertEquals("OBJID in history different", orgPI.getKey(),
                hist.getObjKey());
        Assert.assertEquals("id", orgPI.getPaymentInfoId(), hist
                .getDataContainer().getPaymentInfoId());
    }

    /**
     * <b>Testcase:</b> Delete usedPayment of an existing subscription object
     * and add a new PaymentInfo<br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Old PaymentInfo marked as deleted in the DB</li>
     * <li>History object created for the deleted object</li>
     * <li>New PaymentInfo stored in the DB</li>
     * <li>History object created for the new PaymentInfo</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testNewPaymentInfo() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestNewPaymentInfoPrepare();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestNewPaymentInfo();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestNewPaymentInfoCheck();
                return null;
            }
        });
    }

    private void doTestNewPaymentInfoPrepare() throws SaaSApplicationException {
        String testname = "SubTestNewPaymentInfo";
        prepareSomeObjects(testname);
        // Add a new subscription for product 1
        Subscription sub1 = new Subscription();
        sub1.setCreationDate(TIMESTAMP);
        sub1.setStatus(SubscriptionStatus.ACTIVE);
        sub1.setSubscriptionId(testname);
        sub1.setOrganization(supplier);
        sub1.bindToProduct(products.get(0));
        sub1.addUser(users.get(0), null);
        sub1.setProductInstanceId("prod1");
        sub1.setCutOffDay(1);

        PaymentInfo pi = PaymentInfos.createPaymentInfo(customer, mgr,
                getInvoicePaymentType());

        sub1.setPaymentInfo(pi);
        sub1.setUuid(UUID.randomUUID());
        mgr.persist(sub1);
        subscriptions.clear();
        subscriptions.add(sub1);
    }

    private void doTestNewPaymentInfo() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {

        String testname = "SubTestNewPaymentInfo";
        Subscription qry = new Subscription();
        qry.setSubscriptionId(testname);
        qry.setOrganizationKey(supplier.getKey());
        Subscription sub = (Subscription) mgr.find(qry);
        subscriptions.clear();
        subscriptions.add((Subscription) ReflectiveClone.clone(sub));
        // remove pi
        PaymentInfo pi_old = sub.getPaymentInfo();
        PaymentInfo pi_new = PaymentInfos.createPaymentInfo(customer, mgr,
                getInvoicePaymentType());
        mgr.persist(pi_new);
        sub.setPaymentInfo(pi_new);
        mgr.remove(pi_old);
        subscriptions.add((Subscription) ReflectiveClone.clone(sub));
    }

    private void doTestNewPaymentInfoCheck() {
        String testname = "SubTestNewPaymentInfo";
        Subscription saved;
        Subscription orgSub = subscriptions.get(1);
        orgSub = mgr.find(Subscription.class, orgSub.getKey());
        Subscription qry = new Subscription();
        qry.setSubscriptionId(testname);
        qry.setOrganizationKey(supplier.getKey());
        saved = (Subscription) mgr.find(qry);

        Assert.assertNotNull("Cannot find '" + orgSub.getSubscriptionId()
                + "' in DB", saved);
        Assert.assertTrue(ReflectiveCompare.showDiffs(saved, orgSub),
                ReflectiveCompare.compare(saved, orgSub));
        // Check price model
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(orgSub.getPriceModel(),
                        saved.getPriceModel()),
                ReflectiveCompare.compare(orgSub.getPriceModel(),
                        saved.getPriceModel()));
        // Load history objects and check them
        // H1) Check subscription history object
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        Assert.assertNotNull("History entry 'null' for subscription "
                + orgSub.getSubscriptionId());
        Assert.assertFalse(
                "History entry empty for subscription "
                        + orgSub.getSubscriptionId(), histObjs.isEmpty());
        /*
         * Because of the deletion of the payment type a new history modified
         * entry was created.
         */
        Assert.assertTrue("Exactly 2 history entry expected for subscription "
                + orgSub.getSubscriptionId(), histObjs.size() == 2);
        // H2) Check usageLicense history object (should be unchanged, as this
        // is a collection and none of the entries had a dirty flag set)
        histObjs = mgr.findHistory(saved.getUsageLicenses().get(0));
        Assert.assertNotNull("History entry 'null' for UsageLicense 0 of subscription "
                + orgSub.getSubscriptionId());
        Assert.assertEquals(
                "Only one history entry expected for UsageLicense 0 of subscription "
                        + orgSub.getSubscriptionId(), 1, histObjs.size());
        // H3) Check PaymentInfo history object --> there should be a new entry
        PaymentInfo orgPI = orgSub.getPaymentInfo();
        PaymentInfo savedPI = saved.getPaymentInfo();
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgPI, savedPI),
                ReflectiveCompare.compare(orgPI, savedPI));
        histObjs = mgr.findHistory(orgPI);
        Assert.assertNotNull(
                "History entry 'null' for PaymentInfo of subscription "
                        + orgSub.getSubscriptionId(), histObjs);
        Assert.assertEquals(
                "Exactly 1 history entries expected for PaymentInfo of subscription "
                        + orgSub.getSubscriptionId(), 1, histObjs.size());
        // load added history object
        DomainHistoryObject<?> hist = histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgPI, hist),
                ReflectiveCompare.compare(orgPI, hist));
        Assert.assertEquals("OBJID in history different", orgPI.getKey(),
                hist.getObjKey());

        PaymentInfo oldPI = subscriptions.get(0).getPaymentInfo();
        histObjs = mgr.findHistory(oldPI);

        hist = histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(oldPI, hist),
                ReflectiveCompare.compare(oldPI, hist));
        Assert.assertTrue(orgPI.getKey() != hist.getObjKey());

        // load deleted history object
        hist = histObjs.get(1);
        Assert.assertEquals(ModificationType.DELETE, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(oldPI, hist),
                ReflectiveCompare.compare(oldPI, hist));
        Assert.assertTrue(orgPI.getKey() != hist.getObjKey());
    }

    /**
     * The test checks a named query. The purpose of the query is to find out if
     * there are organizations that have more than one subscription. In this
     * test case the setup creates two subscriptions for one organization.
     * 
     * @throws Throwable
     */
    @Test
    public void testOrganizationsWithMoreThanOneSubscription_twoSubscriptions()
            throws Throwable {

        // given two subscriptions for two different products of the same
        // technical product
        final TechnicalProduct tProd = runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);

                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "service",
                                false, ServiceAccessType.LOGIN);
                Product prod = createProduct(tProd, "prod1");
                Product prod2 = createProduct(tProd, "prod3");
                createSubscription(prod, "sub1", supplier);
                createSubscription(prod2, "sub2", supplier);

                return tProd;
            }

        });

        // then query must return 1
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.organizationsWithMoreThanOneVisibleSubscription");
                query.setParameter("productKey", Long.valueOf(tProd.getKey()));
                Object result = query.getSingleResult();
                assertEquals(Long.valueOf(1), result);

                return null;
            }
        });
    }

    Product createProduct(TechnicalProduct tProd, String productId)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Product prod = Products.createProduct(supplier.getOrganizationId(),
                productId, tProd.getTechnicalProductId(), mgr);
        return prod;
    }

    Subscription createSubscription(Product prod, String subscriptionId,
            Organization org) throws NonUniqueBusinessKeyException {
        return createSubscription(prod, subscriptionId, org, null);
    }

    Subscription createSubscription(Product prod, String subscriptionId,
            Organization org, Marketplace marketplace)
            throws NonUniqueBusinessKeyException {
        return createSubscription(prod, subscriptionId, org, marketplace,
                TIMESTAMP.longValue());
    }

    Subscription createSubscription(Product prod, String subscriptionId,
            Organization org, Marketplace marketplace, long creationDate)
            throws NonUniqueBusinessKeyException {
        Subscription sub = new Subscription();
        sub.setCreationDate(Long.valueOf(creationDate));
        sub.setActivationDate(Long.valueOf(creationDate));
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setSubscriptionId(subscriptionId);
        sub.setOrganization(org);
        sub.bindToProduct(prod);
        sub.setMarketplace(marketplace);
        sub.setCutOffDay(1);
        sub.setUuid(UUID.randomUUID());
        mgr.persist(sub);
        return sub;
    }

    /**
     * The test checks a named query. The purpose of the query is to find out if
     * there are organizations that have more than one subscription. In this
     * test case the setup creates one subscription for each organization.
     * 
     * @throws Throwable
     */
    @Test
    public void testOrganizationsWithMoreThanOneSubscription_oneSubscriptions()
            throws Throwable {

        // given two subscriptions for two different organizations
        final TechnicalProduct tProd = runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                customer = Organizations.createCustomer(mgr, supplier);

                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "service",
                                false, ServiceAccessType.LOGIN);
                Product prod = createProduct(tProd, "prod1");
                createSubscription(prod, "sub1", supplier);
                createSubscription(prod, "sub2", customer);

                return tProd;
            }

        });

        // then query must return 0
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.organizationsWithMoreThanOneVisibleSubscription");
                query.setParameter("productKey", Long.valueOf(tProd.getKey()));
                Object result = query.getSingleResult();
                assertEquals(Long.valueOf(0), result);

                return null;
            }
        });
    }

    @Test
    public void testOrganizationsWithMoreThanOneSubscription_invisibleSubscriptions()
            throws Throwable {

        // given two subscriptions for two different products of the same
        // technical product
        final TechnicalProduct tProd = runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);

                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "service",
                                false, ServiceAccessType.LOGIN);
                Product prod = createProduct(tProd, "prod1");
                createInvisibleSubscription(prod, "sub1", supplier);
                createInvisibleSubscription(prod, "sub2", supplier);
                return tProd;
            }

        });

        // then query must return 0
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.organizationsWithMoreThanOneVisibleSubscription");
                query.setParameter("productKey", Long.valueOf(tProd.getKey()));
                Object result = query.getSingleResult();
                assertEquals(Long.valueOf(0), result);
                return null;
            }
        });
    }

    Subscription createInvisibleSubscription(Product prod,
            String subscriptionId, Organization org)
            throws NonUniqueBusinessKeyException {
        Subscription sub = createSubscription(prod, subscriptionId, org);
        sub.setStatus(SubscriptionStatus.DEACTIVATED);
        mgr.persist(sub);
        return sub;
    }

    /**
     * The test checks a named query. The purpose of the query is to find out
     * how many subscriptions a given organization has for a given technical
     * product.
     * 
     * @throws Exception
     */
    @Test
    public void testNumberOfSubscriptions() throws Exception {
        // given two subscriptions for two different products of the same
        // technical product
        final TechnicalProduct tProd = runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);

                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "service",
                                false, ServiceAccessType.LOGIN);
                Product prod = createProduct(tProd, "prod1");
                Product prod2 = createProduct(tProd, "prod3");
                createSubscription(prod, "sub1", supplier);
                createSubscription(prod2, "sub2", supplier);

                return tProd;
            }
        });

        // then query must return 2
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.numberOfVisibleSubscriptions");
                query.setParameter("productKey", Long.valueOf(tProd.getKey()));
                query.setParameter("orgKey", Long.valueOf(supplier.getKey()));
                Object result = query.getSingleResult();
                assertEquals(Long.valueOf(2), result);

                return null;
            }
        });

    }

    @Test
    public void testNumberOfSubscriptions_invisibleSubscriptions()
            throws Exception {
        // given two subscriptions for two different products of the same
        // technical product
        final TechnicalProduct tProd = runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);

                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "service",
                                false, ServiceAccessType.LOGIN);
                Product prod = createProduct(tProd, "prod1");
                createInvisibleSubscription(prod, "sub1", supplier);
                createInvisibleSubscription(prod, "sub2", supplier);

                return tProd;
            }
        });

        // then query must return 0
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.numberOfVisibleSubscriptions");
                query.setParameter("productKey", Long.valueOf(tProd.getKey()));
                query.setParameter("orgKey", Long.valueOf(supplier.getKey()));
                Object result = query.getSingleResult();
                assertEquals(Long.valueOf(0), result);

                return null;
            }
        });

    }

    /**
     * One subscription, one technical product
     * 
     * @throws Exception
     */
    @Test
    public void testIsLastSubscription_Positive() throws Exception {
        // given one subscription for one technical product
        runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "service",
                                false, ServiceAccessType.LOGIN);
                tProd.setAllowingOnBehalfActing(true);
                Product prod = createProduct(tProd, "prod1");

                createSubscription(prod, "lastSubscription", supplier);
                return tProd;
            }
        });

        // then query must return 1
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.hasSubscriptionsBasedOnOnBehalfServicesForTp");
                query.setParameter("tpOrgKey", Long.valueOf(supplier.getKey()));
                Object result = query.getSingleResult();
                assertEquals(Long.valueOf(1), result);

                return null;
            }
        });
    }

    /**
     * two subscriptions for one technical product
     * 
     * @throws Exception
     */
    @Test
    public void testIsLastSubscription_Negative() throws Exception {
        // given two subscriptions for one technical product
        runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "service",
                                false, ServiceAccessType.LOGIN);
                tProd.setAllowingOnBehalfActing(true);
                Product prod = createProduct(tProd, "prod1");

                createSubscription(prod, "Subscription1", supplier);
                createSubscription(prod, "Subscription2", supplier);
                return tProd;
            }
        });

        // then query must return 2
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.hasSubscriptionsBasedOnOnBehalfServicesForTp");
                query.setParameter("tpOrgKey", Long.valueOf(supplier.getKey()));
                Object result = query.getSingleResult();
                assertEquals(Long.valueOf(2), result);

                return null;
            }
        });
    }

    /**
     * two subscriptions for two technical products
     * 
     * @throws Exception
     */
    @Test
    public void testIsLastSubscription_NegativeTwoTechnicalProducts()
            throws Exception {
        // given two subscriptions for two technical products
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                TechnicalProduct tProd1 = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "tp1", false,
                                ServiceAccessType.LOGIN);
                tProd1.setAllowingOnBehalfActing(true);
                Product prod1 = createProduct(tProd1, "prod1");
                createSubscription(prod1, "Subscription1", supplier);

                TechnicalProduct tProd2 = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "tp2", false,
                                ServiceAccessType.LOGIN);
                tProd2.setAllowingOnBehalfActing(true);
                Product prod2 = createProduct(tProd2, "prod2");
                createSubscription(prod2, "Subscription2", supplier);

                return null;
            }
        });

        // then query must return 2
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.hasSubscriptionsBasedOnOnBehalfServicesForTp");
                query.setParameter("tpOrgKey", Long.valueOf(supplier.getKey()));
                Object result = query.getSingleResult();
                assertEquals(Long.valueOf(2), result);

                return null;
            }
        });
    }

    @Test
    public void testIsLastSubscription_ZeroSubscriptions() throws Exception {
        // given two subscriptions for one technical product
        runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "service",
                                false, ServiceAccessType.LOGIN);
                tProd.setAllowingOnBehalfActing(true);

                return tProd;
            }
        });

        // then query must return 0
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.hasSubscriptionsBasedOnOnBehalfServicesForTp");
                query.setParameter("tpOrgKey", Long.valueOf(supplier.getKey()));
                Object result = query.getSingleResult();
                assertEquals(Long.valueOf(0), result);

                return null;
            }
        });
    }

    /**
     * one subscription is based on a technical service with enabled on behalf.
     * the other subscription is based on a technical service with disabled on
     * behalf.
     * 
     * @throws Exception
     */
    @Test
    public void testIsLastSubscription_TwoTechnicalProducts() throws Exception {
        // given two subscriptions: one with on behalf, second not on behealf
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                TechnicalProduct tProd1 = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "tp1", false,
                                ServiceAccessType.LOGIN);
                tProd1.setAllowingOnBehalfActing(true);
                Product prod1 = createProduct(tProd1, "prod1");
                createSubscription(prod1, "Subscription1", supplier);

                TechnicalProduct tProd2 = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "tp2", false,
                                ServiceAccessType.LOGIN);
                tProd2.setAllowingOnBehalfActing(false);
                Product prod2 = createProduct(tProd2, "prod2");
                createSubscription(prod2, "Subscription2", supplier);

                return null;
            }
        });

        // then query must return 1
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.hasSubscriptionsBasedOnOnBehalfServicesForTp");
                query.setParameter("tpOrgKey", Long.valueOf(supplier.getKey()));
                Object result = query.getSingleResult();
                assertEquals(Long.valueOf(1), result);
                return null;
            }
        });
    }

    /**
     * One subscriptions, two technical services
     * 
     * @throws Exception
     */
    @Test
    public void testIsLastSubscription_OneSubscriptionsTwoTechnicalProducts()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                TechnicalProduct tProd1 = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "tp1", false,
                                ServiceAccessType.LOGIN);
                tProd1.setAllowingOnBehalfActing(true);
                Product prod1 = createProduct(tProd1, "prod1");
                createSubscription(prod1, "Subscription1", supplier);

                TechnicalProduct tProd2 = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "tp2", false,
                                ServiceAccessType.LOGIN);
                tProd2.setAllowingOnBehalfActing(true);

                return null;
            }
        });

        // then query must return 1
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.hasSubscriptionsBasedOnOnBehalfServicesForTp");
                query.setParameter("tpOrgKey", Long.valueOf(supplier.getKey()));
                Object result = query.getSingleResult();
                assertEquals(Long.valueOf(1), result);

                return null;
            }
        });
    }

    /**
     * Test for the named query 'Subscription.getActiveSubscriptionsForUser'.
     * Create two subscriptions and usage licenses accordingly. Query must find
     * both.
     * 
     * @throws Exception
     */
    @Test
    public void testGetActiveSubscriptionsForUser() throws Exception {

        // given two subscriptions and for each a usage license for admin
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                admin = Organizations.createUserForOrg(mgr, supplier, true,
                        "admin");

                Product prod = Products.createProduct(
                        supplier.getOrganizationId(), "prod", "techProd", mgr);
                Subscription sub = createSubscription(prod, "Subscription1",
                        supplier);
                createUsageLicense(sub, admin);
                Subscription sub2 = createSubscription(prod, "Subscription2",
                        supplier);
                createUsageLicense(sub2, admin);
                return null;
            }
        });

        // then the query must find both subscriptions for admin user
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.getCurrentUserSubscriptions");
                query.setParameter("userKey", Long.valueOf(admin.getKey()));
                query.setParameter("status",
                        Subscription.VISIBLE_SUBSCRIPTION_STATUS);
                List<?> result = query.getResultList();
                assertEquals(2, result.size());
                return null;
            }
        });
    }

    void createUsageLicense(Subscription sub, PlatformUser user)
            throws NonUniqueBusinessKeyException {
        UsageLicense license = new UsageLicense();
        license.setSubscription(sub);
        license.setUser(user);
        mgr.persist(license);
    }

    /**
     * Test for the named query 'Subscription.getActiveSubscriptionsForUser'.
     * Create one subscription but no usage license. Query result must be empty.
     * 
     * @throws Exception
     */
    @Test
    public void testGetActiveSubscriptionsForUser_subscriptionWithoutUsageLicense()
            throws Exception {

        // given one subscription, but admin has no usage license
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                admin = Organizations.createUserForOrg(mgr, supplier, true,
                        "admin");

                Product prod = Products.createProduct(
                        supplier.getOrganizationId(), "prod", "techProd", mgr);
                createSubscription(prod, "Subscription1", supplier);
                return null;
            }
        });

        // then the query must have an empty result for admin
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.getCurrentUserSubscriptions");
                query.setParameter("userKey", Long.valueOf(admin.getKey()));
                query.setParameter("status",
                        Subscription.VISIBLE_SUBSCRIPTION_STATUS);
                List<?> result = query.getResultList();
                assertEquals(0, result.size());

                return null;
            }
        });
    }

    /**
     * Test for the named query 'Subscription.getActiveSubscriptionsForUser'.
     * Create one disabled subscription and a usage license for it. The query
     * result must be empty, because the subscription is not active and should
     * not be visible.
     * 
     * @throws Exception
     */
    @Test
    public void testGetActiveSubscriptionsForUser_inactiveSubscription()
            throws Exception {

        // given a subscription that is inactive and a usage license for admin
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                admin = Organizations.createUserForOrg(mgr, supplier, true,
                        "admin");

                Product prod = Products.createProduct(
                        supplier.getOrganizationId(), "prod", "techProd", mgr);
                Subscription sub = createInvisibleSubscription(prod,
                        "Subscription1", supplier);
                createUsageLicense(sub, admin);
                return null;
            }
        });

        // then the query result must be empty, because inactive subscriptions
        // are not listed
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.getCurrentUserSubscriptions");
                query.setParameter("userKey", Long.valueOf(admin.getKey()));
                query.setParameter("status",
                        Subscription.VISIBLE_SUBSCRIPTION_STATUS);
                List<?> result = query.getResultList();
                assertEquals(0, result.size());
                return null;
            }
        });
    }

    /**
     * Test for the named query 'Subscription.getActiveSubscriptionsForUser'.
     * Create one subscription but the usage license is for a different user.
     * Query result must be empty.
     * 
     * @throws Exception
     */
    @Test
    public void testGetActiveSubscriptionsForUser_differentUser()
            throws Exception {

        // given one subscription. However, the usage license is not for admin,
        // but a different user
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                admin = Organizations.createUserForOrg(mgr, supplier, true,
                        "admin");
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        supplier, true, "user");

                Product prod = Products.createProduct(
                        supplier.getOrganizationId(), "prod", "techProd", mgr);
                Subscription sub = createSubscription(prod, "Subscription1",
                        supplier);
                createUsageLicense(sub, user);
                return null;
            }
        });

        // then the query must have no result, because admin has no usage
        // license
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.getCurrentUserSubscriptions");
                query.setParameter("userKey", Long.valueOf(admin.getKey()));
                query.setParameter("status",
                        Subscription.VISIBLE_SUBSCRIPTION_STATUS);
                List<?> result = query.getResultList();
                assertEquals(0, result.size());
                return null;
            }
        });
    }

    /**
     * Test for the named query 'Subscription.getForProduct'. Create
     * subscriptions with product1,product2.The template of product1 is product.
     * Query must return only subscriptions with product as the template which
     * is set as a parameter.
     * 
     * @throws Exception
     */

    @Test
    public void getForProduct() throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                product = Products.createProduct(supplier.getOrganizationId(),
                        "product", "techProduct", mgr);
                Product product1 = Products.createProduct(
                        supplier.getOrganizationId(), "product1",
                        "techProduct", mgr);
                product1.setTemplate(product);
                Product product2 = Products.createProduct(
                        supplier.getOrganizationId(), "product2",
                        "techProduct", mgr);
                createSubscription(product1, "subscription1", supplier);
                createSubscription(product2, "subscription2", supplier);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.getForProduct");
                query.setParameter("product", product);
                query.setParameter("status", EnumSet.of(
                        SubscriptionStatus.ACTIVE,
                        SubscriptionStatus.SUSPENDED,
                        SubscriptionStatus.PENDING_UPD,
                        SubscriptionStatus.SUSPENDED_UPD));
                List<Subscription> result = ParameterizedTypes.list(
                        query.getResultList(), Subscription.class);
                assertEquals(1, result.size());
                assertEquals("subscription1", result.get(0).getSubscriptionId());
                return null;
            }
        });

    }

    /**
     * Test for the named query 'Subscription.getForMarketplace'. Create
     * subscription with marketplace1 and with marketplace2 and without
     * marketplace. Query must return only subscription with marketplace which
     * is set as a parameter.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSubscriptionsForMarketplace() throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                marketplace = Marketplaces.createGlobalMarketplace(supplier,
                        "marketplace1", mgr);
                Marketplace marketplace2 = Marketplaces.ensureMarketplace(
                        supplier, "marketplace2", mgr);

                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "service",
                                false, ServiceAccessType.LOGIN);
                Product product = createProduct(tProd, "product1");

                createSubscription(product, "subscription1", supplier,
                        marketplace);
                createSubscription(product, "subscription2", supplier,
                        marketplace2);
                createSubscription(product, "subscription3", supplier);

                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.getForMarketplace");
                query.setParameter("marketplace", marketplace);
                List<Subscription> result = ParameterizedTypes.list(
                        query.getResultList(), Subscription.class);
                assertEquals(2, result.size());
                return null;
            }
        });
    }

    /**
     * Test for the named query 'Subscription.findUsageLicense'. Create one
     * usage license to be found. Create one additional usage license for
     * another user and one additional usage license for another subscription.
     * 
     * @throws Exception
     */
    @Test
    public void testFindUsageLicense() throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                Product product = Products.createProduct(
                        supplier.getOrganizationId(), "product1",
                        "techProduct1", mgr);

                Subscription sub1 = Subscriptions.createSubscription(mgr,
                        supplier.getOrganizationId(), product);
                Subscription sub2 = Subscriptions.createSubscription(mgr,
                        supplier.getOrganizationId(), product);
                subscriptions.add(sub1);

                PlatformUser user1 = PlatformUsers.createUser(mgr, "user1",
                        supplier);
                PlatformUser user2 = PlatformUsers.createUser(mgr, "user2",
                        supplier);
                users.add(user1);

                Subscriptions.createUsageLicense(mgr, user1, sub1);
                Subscriptions.createUsageLicense(mgr, user2, sub1);
                Subscriptions.createUsageLicense(mgr, user1, sub2);

                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.findUsageLicense");
                query.setParameter("userId", users.get(0).getUserId());
                query.setParameter("subscriptionKey",
                        Long.valueOf(subscriptions.get(0).getKey()));

                @SuppressWarnings("unchecked")
                List<UsageLicense> result = query.getResultList();
                assertEquals(1, result.size());
                UsageLicense loadedLicense = result.get(0);
                assertEquals(users.get(0), loadedLicense.getUser());
                assertEquals(subscriptions.get(0),
                        loadedLicense.getSubscription());
                return null;
            }
        });
    }

    @Test
    public void getForOrgFetchRoles() throws Exception {
        final Long orgKey = runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, org, "tp", false, ServiceAccessType.LOGIN);

                TechnicalProduct tp_role = TechnicalProducts
                        .createTechnicalProduct(mgr, org, "tp_role", false,
                                ServiceAccessType.LOGIN);
                TechnicalProducts.addRoleDefinition("roleA", tp_role, mgr);

                TechnicalProduct tp_roles = TechnicalProducts
                        .createTechnicalProduct(mgr, org, "tp_roles", false,
                                ServiceAccessType.LOGIN);
                TechnicalProducts.addRoleDefinition("role1", tp_roles, mgr);
                TechnicalProducts.addRoleDefinition("role2", tp_roles, mgr);

                Product p = Products.createProduct(org, tp, false, "p1", "pm1",
                        mgr);
                Product p_role = Products.createProduct(org, tp_role, false,
                        "p2", "pm2", mgr);
                Product p_roles = Products.createProduct(org, tp_roles, false,
                        "p3", "pm3", mgr);

                Subscriptions.createSubscription(mgr, org.getOrganizationId(),
                        p.getProductId(), "s1", org);
                Subscriptions.createSubscription(mgr, org.getOrganizationId(),
                        p.getProductId(), "s2", org);

                Subscriptions.createSubscription(mgr, org.getOrganizationId(),
                        p_role.getProductId(), "s3_role", org);

                Subscriptions.createSubscription(mgr, org.getOrganizationId(),
                        p_roles.getProductId(), "s4_roles", org);
                Subscriptions.createSubscription(mgr, org.getOrganizationId(),
                        p_roles.getProductId(), "s5_roles", org);

                return Long.valueOf(org.getKey());
            }
        });

        List<Object[]> list = runTX(new Callable<List<Object[]>>() {

            @Override
            public List<Object[]> call() throws Exception {
                Query q = mgr
                        .createNamedQuery("Subscription.getForOrgFetchRoles");
                q.setParameter("status",
                        EnumSet.allOf(SubscriptionStatus.class));
                q.setParameter("orgKey", orgKey);
                @SuppressWarnings("unchecked")
                List<Object[]> list = q.getResultList();
                return list;
            }
        });

        assertEquals(7, list.size());

        assertEquals("s1", ((Subscription) list.get(0)[0]).getSubscriptionId());
        assertEquals(null, list.get(0)[1]);

        assertEquals("s2", ((Subscription) list.get(1)[0]).getSubscriptionId());
        assertEquals(null, list.get(1)[1]);

        assertEquals("s3_role",
                ((Subscription) list.get(2)[0]).getSubscriptionId());
        assertEquals("roleA", ((RoleDefinition) list.get(2)[1]).getRoleId());

        assertEquals("s4_roles",
                ((Subscription) list.get(3)[0]).getSubscriptionId());
        assertEquals("role1", ((RoleDefinition) list.get(3)[1]).getRoleId());
        assertEquals("s4_roles",
                ((Subscription) list.get(4)[0]).getSubscriptionId());
        assertEquals("role2", ((RoleDefinition) list.get(4)[1]).getRoleId());

        assertEquals("s5_roles",
                ((Subscription) list.get(5)[0]).getSubscriptionId());
        assertEquals("role1", ((RoleDefinition) list.get(5)[1]).getRoleId());
        assertEquals("s5_roles",
                ((Subscription) list.get(6)[0]).getSubscriptionId());
        assertEquals("role2", ((RoleDefinition) list.get(6)[1]).getRoleId());
    }

    /**
     * Test for the named query 'Subscription.getSubscriptionsForMyCustomers'.
     * Create two subscriptions for my customer.
     * 
     * @throws Exception
     */
    @Test
    public void getSubscriptionsForMyCustomer() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                customer = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);

                Product productTemplate = Products.createProduct(
                        supplier.getOrganizationId(), "product1",
                        "techProduct1", mgr);

                Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), productTemplate);
                Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), productTemplate);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SubscriptionStatus> states = EnumSet.of(
                        SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING,
                        SubscriptionStatus.EXPIRED);
                Query query = mgr
                        .createNamedQuery("Subscription.getSubscriptionsForMyCustomers");
                query.setParameter("offerer", supplier);
                query.setParameter("states", states);
                List<Subscription> result = ParameterizedTypes.list(
                        query.getResultList(), Subscription.class);
                assertEquals(2, result.size());
                assertEquals(customer.getOrganizationId(), result.get(0)
                        .getOrganization().getOrganizationId());
                return null;
            }
        });
    }

    /**
     * Test for the named query
     * 'Subscription.getSubscriptionsForMyBrokerCustomers'. Create one partner
     * subscription for broker customer.
     * 
     * @throws Exception
     */
    @Test
    public void getSubscriptionsForMyBrokerCustomer() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                broker = Organizations.createOrganization(mgr,
                        OrganizationRoleType.BROKER);
                customer = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);

                Product productTemplate = Products.createProduct(
                        supplier.getOrganizationId(), "product1",
                        "techProduct1", mgr);

                Product resaleCopy = productTemplate.copyForResale(broker);
                mgr.persist(resaleCopy);

                Subscriptions.createPartnerSubscription(mgr,
                        customer.getOrganizationId(),
                        resaleCopy.getProductId(),
                        "supplierBrokerCustomerSubscription", broker);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createNamedQuery("Subscription.getSubscriptionsForMyBrokerCustomers");
                query.setParameter("offerer", supplier);
                List<Subscription> result = ParameterizedTypes.list(
                        query.getResultList(), Subscription.class);
                assertEquals(1, result.size());
                assertEquals(customer.getOrganizationId(), result.get(0)
                        .getOrganization().getOrganizationId());
                return null;
            }
        });
    }

}
