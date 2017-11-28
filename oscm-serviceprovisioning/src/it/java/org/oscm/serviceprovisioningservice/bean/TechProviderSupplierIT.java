/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Aug 3, 2011                                                      
 *                                                                              
 *  Completion Time: Aug 4, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.StaticEJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.stubs.*;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Dirk Bernsau
 * 
 */
public class TechProviderSupplierIT extends StaticEJBTestBase {

    private static final String PROVIDER_ADMIN = "providerAdmin";

    private static final String EUR = "EUR";

    private static ServiceProvisioningService sps;
    private static DataService mgr;
    private static AccountService acc;

    protected static int NUMBER_TECHNOLOGY_PROVIDER = 4;
    protected static PlatformUser[] technologyProviderAdmins = new PlatformUser[NUMBER_TECHNOLOGY_PROVIDER];;
    protected static Organization[] technologyProviderOrgs = new Organization[NUMBER_TECHNOLOGY_PROVIDER];
    protected static String[] technologyProviderIds = new String[NUMBER_TECHNOLOGY_PROVIDER];

    protected static int NUMBER_SUPPLIERS = 6;
    protected static Organization supplierOrgs[] = new Organization[NUMBER_SUPPLIERS];
    protected static String[] supplierOrgIds = new String[NUMBER_SUPPLIERS];
    protected static String[] supplierAdmins = new String[NUMBER_SUPPLIERS];

    protected static PlatformUser[] supplierUsers = new PlatformUser[NUMBER_SUPPLIERS];

    protected static int NUMBER_CUSTOMERS = 6;
    protected static Organization customerOrgs[] = new Organization[NUMBER_CUSTOMERS];
    protected static String[] customerOrgIds = new String[NUMBER_CUSTOMERS];
    protected static PlatformUser[] customerUsers = new PlatformUser[NUMBER_CUSTOMERS];
    protected static String[] customerAdmins = new String[NUMBER_CUSTOMERS];

    protected static List<String> storedInstanceIds = new ArrayList<String>();
    protected static List<String> storedSubIds = new ArrayList<String>();

    private static SubscriptionStatus subStatusNew;

    private static TechnicalProduct techProd1;

    @BeforeClass
    public static void setupOnce() throws Exception {
        container.enableInterfaceMocking(true);
        container.login("1");
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(new ApplicationServiceStub());
        container.addBean(new ImageResourceServiceStub());
        container.addBean(new SessionServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LdapAccessServiceStub());
        container.addBean(new TaskQueueServiceStub());
        container.addBean(mock(IdentityServiceLocal.class));
        container.addBean(new PaymentServiceStub());
        container.addBean(mock(AccountServiceLocal.class));
        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new TagServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());

        sps = container.get(ServiceProvisioningService.class);
        mgr = container.get(DataService.class);
        acc = container.get(AccountService.class);

        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance(EUR));
                mgr.persist(sc);
                createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);

                for (int i = 0; i < NUMBER_TECHNOLOGY_PROVIDER; i++) {
                    technologyProviderOrgs[i] = Organizations
                            .createOrganization(mgr,
                                    OrganizationRoleType.TECHNOLOGY_PROVIDER);
                    technologyProviderAdmins[i] = Organizations
                            .createUserForOrg(mgr, technologyProviderOrgs[i],
                                    true, PROVIDER_ADMIN);
                    technologyProviderIds[i] = technologyProviderOrgs[i]
                            .getOrganizationId();
                }

                for (int i = 0; i < NUMBER_SUPPLIERS; i++) {
                    supplierOrgs[i] = Organizations.createOrganization(mgr,
                            OrganizationRoleType.SUPPLIER);
                    supplierAdmins[i] = "supplier" + i + "Admin";
                    supplierUsers[i] = Organizations.createUserForOrg(mgr,
                            supplierOrgs[i], true, supplierAdmins[i]);
                    supplierOrgIds[i] = supplierOrgs[i].getOrganizationId();
                }

                for (int i = 0; i < NUMBER_CUSTOMERS; i++) {
                    customerOrgs[i] = Organizations.createOrganization(mgr,
                            OrganizationRoleType.CUSTOMER);
                    customerAdmins[i] = "customer" + i + "Admin";
                    customerUsers[i] = Organizations.createUserForOrg(mgr,
                            customerOrgs[i], true, customerAdmins[i]);
                    customerOrgIds[i] = customerOrgs[i].getOrganizationId();
                }

                // TechnicalProducts and products for
                // technologyProviderOrganization0
                TechnicalProduct techProd0 = TechnicalProducts
                        .createTechnicalProduct(mgr, technologyProviderOrgs[0],
                                "technicalProductId0", false,
                                ServiceAccessType.DIRECT);
                Products.createProduct(supplierOrgs[1], techProd0, false,
                        "productId1", null, mgr);
                Products.createProduct(supplierOrgs[2], techProd0, false,
                        "productId2", null, mgr);

                // TechnicalProducts and products for
                // technologyProviderOrganization1
                container.login(
                        String.valueOf(technologyProviderAdmins[1].getKey()),
                        ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
                techProd1 = TechnicalProducts.createTechnicalProduct(mgr,
                        technologyProviderOrgs[1], "technicalProductId1",
                        false, ServiceAccessType.DIRECT);
                VOTechnicalService voTechnicalSrv = new VOTechnicalService();
                voTechnicalSrv.setKey(techProd1.getKey());
                acc.addSuppliersForTechnicalService(voTechnicalSrv,
                        Arrays.asList(supplierOrgIds));
                Products.createProduct(supplierOrgs[0], techProd1, false,
                        "productId3", null, mgr);
                Products.createProduct(supplierOrgs[4], techProd1, false,
                        "productId4", null, mgr);

                // Subscriptions for technologyProviderOrganization0
                Subscription sub = Subscriptions.createSubscription(mgr,
                        customerOrgs[1].getOrganizationId(), "productId1",
                        "1_1", supplierOrgs[1]);
                storedSubIds.add(sub.getSubscriptionId());
                storedInstanceIds.add(sub.getProductInstanceId());

                sub = Subscriptions.createSubscription(mgr,
                        customerOrgs[2].getOrganizationId(), "productId1",
                        "2_1", supplierOrgs[1]);
                storedSubIds.add(sub.getSubscriptionId());
                storedInstanceIds.add(sub.getProductInstanceId());

                sub = Subscriptions.createSubscription(mgr,
                        customerOrgs[1].getOrganizationId(), "productId2",
                        "1_2", supplierOrgs[2]);
                storedSubIds.add(sub.getSubscriptionId());
                storedInstanceIds.add(sub.getProductInstanceId());

                sub = Subscriptions.createSubscription(mgr,
                        customerOrgs[2].getOrganizationId(), "productId2",
                        "2_2", supplierOrgs[2]);
                storedSubIds.add(sub.getSubscriptionId());
                storedInstanceIds.add(sub.getProductInstanceId());

                // Subscriptions for technologyProviderOrganization1
                sub = Subscriptions.createSubscription(mgr,
                        customerOrgs[0].getOrganizationId(), "productId3",
                        "0_0", supplierOrgs[0]);
                storedSubIds.add(sub.getSubscriptionId());
                storedInstanceIds.add(sub.getProductInstanceId());

                sub = Subscriptions.createSubscription(mgr,
                        customerOrgs[4].getOrganizationId(), "productId4",
                        "4_4", supplierOrgs[4]);
                storedSubIds.add(sub.getSubscriptionId());
                storedInstanceIds.add(sub.getProductInstanceId());
                return null;
            }
        });

        container.logout();
        loginTechnologyProviderManager(0);

        List<String> supps = new ArrayList<String>();
        supps.add(supplierOrgIds[1]);
        supps.add(supplierOrgIds[2]);

        container.logout();

        loginTechnologyProviderManager(1);

        supps = new ArrayList<String>();
        supps.add(supplierOrgIds[3]);
        supps.add(supplierOrgIds[4]);

        container.logout();
    }

    /**
     * Login as TechnologyProvider. null instead of list. Exception expected.
     * 
     * @throws Exception
     */
    @Test(expected = org.oscm.internal.types.exception.IllegalArgumentException.class)
    public void testNullParameter() throws Exception {
        loginTechnologyProviderManager(0);
        try {
            sps.getInstanceIdsForSellers(null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    /**
     * Login as TechnologyProvider. No SupplierIds. Empty list expected.
     * 
     * @throws Exception
     */
    @Test
    public void testEmptyIds() throws Exception {
        loginTechnologyProviderManager(0);
        List<String> instanceIds = sps
                .getInstanceIdsForSellers(new ArrayList<String>());
        assertNotNull("List expected", instanceIds);
        assertEquals("Empty list expected", 0, instanceIds.size());
    }

    /**
     * Login with wrong role. SupplierAdmin3. Exception expected.
     * 
     * @throws Exception
     */
    @Test(expected = EJBAccessException.class)
    public void testWrongRoleSupplierAdmin() throws Exception {
        loginSupplierAdmin(3);
        try {
            sps.getInstanceIdsForSellers(new ArrayList<String>());
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    /**
     * Login with wrong role. Platform operator. Exception expected.
     * 
     * @throws Exception
     */
    @Test(expected = EJBAccessException.class)
    public void testWrongRolePlatformOperator() throws Exception {
        loginPlatformOperator();
        try {
            sps.getInstanceIdsForSellers(new ArrayList<String>());
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    /**
     * Login with wrong role. TechnologyProviderAdmin. Exception expected.
     * 
     * @throws Exception
     */
    @Test(expected = EJBAccessException.class)
    public void testWrongRoleTechnologyProviderAdmin() throws Exception {
        loginTechnologyProviderAdmin(0);
        try {
            sps.getInstanceIdsForSellers(new ArrayList<String>());
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    /**
     * Login with wrong role. SupplierServiceManger3. Exception expected.
     * 
     * @throws Exception
     */
    @Test(expected = EJBAccessException.class)
    public void testWrongRoleSupplierServiceManger() throws Exception {
        loginSupplierServiceManger(3);
        try {
            sps.getInstanceIdsForSellers(new ArrayList<String>());
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    /**
     * Login as as TechnologyProvider. Supplier0 has no Products published from
     * TechnologyProvider0. Expected empty list.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgId0() throws Exception {
        loginTechnologyProviderManager(0);
        ArrayList<String> list = new ArrayList<String>();
        list.add(supplierOrgIds[0]);
        List<String> instanceIds = sps.getInstanceIdsForSellers(list);
        assertNotNull("List expected", instanceIds);
        assertEquals("Empty list expected", 0, instanceIds.size());
    }

    /**
     * Login as as TechnologyProvider. Supplier1 has published 1 Product.
     * Consumer1 and 2 are subscribed to this product. The list must contain 2
     * subscriptions. Check the correctness of the returned (product)
     * instanceIDs of the subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgId1() throws Exception {
        loginTechnologyProviderManager(0);
        ArrayList<String> list = new ArrayList<String>();
        list.add(supplierOrgIds[1]);
        List<String> instanceIds = sps.getInstanceIdsForSellers(list);

        assertNotNull("List expected", instanceIds);
        assertEquals("Instance ids - ", 2, instanceIds.size());

        List<String> expectedInstanceIds = new ArrayList<String>();
        expectedInstanceIds.add(storedInstanceIds.get(0));
        expectedInstanceIds.add(storedInstanceIds.get(1));
        for (String element : instanceIds) {
            assertTrue(expectedInstanceIds.contains(element));
        }
    }

    /**
     * Login as as TechnologyProvider. Supplier0 has no Products published from
     * TechnologyProvider0. Supplier1 has published 1 Product. Consumer1 and 2
     * are subscribed to this product. The list must contain 2 subscriptions.
     * Check the correctness of the returned (product) instanceIDs of the
     * subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgId0_1() throws Exception {
        loginTechnologyProviderManager(0);
        ArrayList<String> list = new ArrayList<String>();
        list.add(supplierOrgIds[0]);
        list.add(supplierOrgIds[1]);
        List<String> instanceIds = sps.getInstanceIdsForSellers(list);

        assertNotNull("List expected", instanceIds);
        assertEquals("Instance ids - ", 2, instanceIds.size());

        List<String> expectedInstanceIds = new ArrayList<String>();
        expectedInstanceIds.add(storedInstanceIds.get(0));
        expectedInstanceIds.add(storedInstanceIds.get(1));
        for (String element : instanceIds) {
            assertTrue(expectedInstanceIds.contains(element));
        }
    }

    /**
     * Login as as TechnologyProvider. Supplier2 has published 1 Product.
     * Consumer1 and 2 are subscribed to this product. The list must contain 2
     * subscriptions. Check the correctness of the returned (product)
     * instanceIDs of the subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgId2() throws Exception {
        loginTechnologyProviderManager(0);
        ArrayList<String> list = new ArrayList<String>();
        list.add(supplierOrgIds[2]);
        List<String> instanceIds = sps.getInstanceIdsForSellers(list);

        assertNotNull("List expected", instanceIds);
        assertEquals("Instance ids - ", 2, instanceIds.size());

        List<String> expectedInstanceIds = new ArrayList<String>();
        expectedInstanceIds.add(storedInstanceIds.get(2));
        expectedInstanceIds.add(storedInstanceIds.get(3));
        for (String element : instanceIds) {
            assertTrue(expectedInstanceIds.contains(element));
        }
    }

    /**
     * Login as as TechnologyProvider. Supplier3 has no Products published.
     * Supplier2 has published 1 Product. Consumer1 and 2 are subscribed to this
     * product. The list must contain 2 subscriptions. Check the correctness of
     * the returned (product) instanceIDs of the subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgId2_3() throws Exception {
        loginTechnologyProviderManager(0);
        ArrayList<String> list = new ArrayList<String>();
        list.add(supplierOrgIds[2]);
        list.add(supplierOrgIds[3]);
        List<String> instanceIds = sps.getInstanceIdsForSellers(list);

        assertNotNull("List expected", instanceIds);
        assertEquals("Instance ids - ", 2, instanceIds.size());

        List<String> expectedInstanceIds = new ArrayList<String>();
        expectedInstanceIds.add(storedInstanceIds.get(2));
        expectedInstanceIds.add(storedInstanceIds.get(3));
        for (String element : instanceIds)
            assertTrue(expectedInstanceIds.contains(element));
    }

    /**
     * Login as as TechnologyProvider. Supplier1 and 2 have each published 1
     * Product. Consumer1 and 2 are subscribed to both products. The list must
     * contain 4 subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgId1_2() throws Exception {
        loginTechnologyProviderManager(0);

        ArrayList<String> list = new ArrayList<String>();
        list.add(supplierOrgIds[1]);
        list.add(supplierOrgIds[2]);
        List<String> instanceIds = sps.getInstanceIdsForSellers(list);

        assertNotNull("List expected", instanceIds);
        assertEquals("Instance ids - ", 4, instanceIds.size());
    }

    /**
     * Login as as TechnologyProvider. Supplier3 has no Products published.
     * Expected empty list.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgId3() throws Exception {
        loginTechnologyProviderManager(0);
        ArrayList<String> list = new ArrayList<String>();
        list.add(supplierOrgIds[3]);
        List<String> instanceIds = sps.getInstanceIdsForSellers(list);
        assertNotNull("List expected", instanceIds);
        assertEquals("Empty list expected", 0, instanceIds.size());
    }

    /**
     * Login as as TechnologyProvider. Supplier1 and 2 have each published 1
     * Product. Consumer1 and 2 are subscribed to both products. The list must
     * contain 4 subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgIdAll() throws Exception {
        loginTechnologyProviderManager(0);

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < NUMBER_SUPPLIERS; i++)
            list.add(supplierOrgIds[i]);

        List<String> instanceIds = sps.getInstanceIdsForSellers(list);

        assertNotNull("List expected", instanceIds);
        assertEquals("Instance ids - ", 4, instanceIds.size());
    }

    /**
     * Login as as TechnologyProvider. Activate one subscription. The list for
     * all suppliers must contain all 4 subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgActivateSubscription() throws Exception {
        checkQueryForSubscriptionStatus(SubscriptionStatus.ACTIVE, true);
    }

    /**
     * Login as as TechnologyProvider. Deactivate one subscription. The list for
     * all suppliers must contain only 3 subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgDeactivateSubscription() throws Exception {
        checkQueryForSubscriptionStatus(SubscriptionStatus.DEACTIVATED, false);
    }

    /**
     * Login as as TechnologyProvider. Set one subscription to expired. The list
     * for all suppliers must contain only 3 subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgExpiredSubscription() throws Exception {
        checkQueryForSubscriptionStatus(SubscriptionStatus.EXPIRED, false);
    }

    /**
     * Login as as TechnologyProvider. Invalidate one subscription. The list for
     * all suppliers must contain only 3 subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgInvalidSubscription() throws Exception {
        checkQueryForSubscriptionStatus(SubscriptionStatus.INVALID, false);
    }

    /**
     * Login as as TechnologyProvider. Set one subscription to pending. The list
     * for all suppliers must contain only 3 subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgPendingSubscription() throws Exception {
        checkQueryForSubscriptionStatus(SubscriptionStatus.PENDING, false);
    }

    /**
     * Login as as TechnologyProvider. Suspend one subscription. The list for
     * all suppliers must contain all 4 subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgSuspendedSubscription() throws Exception {
        checkQueryForSubscriptionStatus(SubscriptionStatus.SUSPENDED, true);
    }

    /**
     * helper method to check the "subscription" query
     * 
     * @throws Exception
     */
    private void checkQueryForSubscriptionStatus(SubscriptionStatus status,
            boolean findAll) throws Exception {
        loginTechnologyProviderManager(0);

        setSubStatus(status);

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < NUMBER_SUPPLIERS; i++)
            list.add(supplierOrgIds[i]);

        List<String> instanceIds = sps.getInstanceIdsForSellers(list);

        assertNotNull("List expected", instanceIds);

        if (findAll)
            assertEquals("Instance ids - ", 4, instanceIds.size());
        else {
            assertEquals("Instance ids - ", 3, instanceIds.size());
            Assert.assertTrue(!instanceIds.contains(storedInstanceIds.get(0)));
        }

        // reset status
        setSubStatus(SubscriptionStatus.ACTIVE);
    }

    /**
     * @throws Exception
     */
    private void setSubStatus(SubscriptionStatus status) throws Exception {

        subStatusNew = status;

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = new Subscription();
                sub.setSubscriptionId(storedSubIds.get(0));
                sub.setOrganization(mgr.getReference(Organization.class,
                        customerOrgs[1].getKey()));
                sub = Subscription.class.cast(mgr
                        .getReferenceByBusinessKey(sub));

                sub.setStatus(subStatusNew);
                return null;
            }
        });
    }

    /**
     * Login as as TechnologyProvider1. Supplier0 has one Product published from
     * TechnologyProvider1. The list must contain 1 subscription. Check the
     * correctness of the returned (product) instanceID of the subscription.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgId0_techProv1() throws Exception {
        loginTechnologyProviderManager(1);
        ArrayList<String> list = new ArrayList<String>();
        list.add(supplierOrgIds[0]);
        List<String> instanceIds = sps.getInstanceIdsForSellers(list);
        assertNotNull("List expected", instanceIds);
        assertEquals("Instance ids - ", 1, instanceIds.size());
        assertEquals(storedInstanceIds.get(4), instanceIds.get(0));
    }

    /**
     * Login as as TechnologyProvider1. Supplier4 has one Product published from
     * TechnologyProvider1. The list must contain 1 subscription. Check the
     * correctness of the returned (product) instanceID of the subscription.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgId4_techProv1() throws Exception {
        loginTechnologyProviderManager(1);
        ArrayList<String> list = new ArrayList<String>();
        list.add(supplierOrgIds[4]);
        List<String> instanceIds = sps.getInstanceIdsForSellers(list);
        assertNotNull("List expected", instanceIds);
        assertEquals("Instance ids - ", 1, instanceIds.size());
        assertEquals(storedInstanceIds.get(5), instanceIds.get(0));
    }

    /**
     * Login as as TechnologyProvider1. Supplier0 and 4 have each published 1
     * Product. Consumer0 and 4 are subscribed each to one product. The list
     * must contain 2 subscriptions.
     * 
     * @throws Exception
     */
    @Test
    public void testSupplierOrgIdAll_techProv1() throws Exception {
        loginTechnologyProviderManager(1);

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < NUMBER_SUPPLIERS; i++)
            list.add(supplierOrgIds[i]);

        List<String> instanceIds = sps.getInstanceIdsForSellers(list);

        assertNotNull("List expected", instanceIds);
        assertEquals("Instance ids - ", 2, instanceIds.size());

        List<String> expectedInstanceIds = new ArrayList<String>();
        expectedInstanceIds.add(storedInstanceIds.get(4));
        expectedInstanceIds.add(storedInstanceIds.get(5));
        for (String element : instanceIds)
            assertTrue(expectedInstanceIds.contains(element));
    }

    /**
     * Login as as TechnologyProvider1. Supplier4 has published 1 Product.
     * Consumer4 is subscribed to this product. The list must contain 1
     * subscription.
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveSupplierOrg_techProv1() throws Exception {
        loginTechnologyProviderManager(1);

        // remove supplier4
        ArrayList<String> list = new ArrayList<String>();
        list.add(supplierOrgIds[4]);
        VOTechnicalService voTechnicalSrv = new VOTechnicalService();
        voTechnicalSrv.setKey(techProd1.getKey());
        acc.removeSuppliersFromTechnicalService(voTechnicalSrv,
                Arrays.asList(supplierOrgIds));
        List<String> instanceIds = sps.getInstanceIdsForSellers(list);

        assertNotNull("List expected", instanceIds);
        assertEquals("Instance ids - ", 1, instanceIds.size());

        List<String> expectedInstanceIds = new ArrayList<String>();
        expectedInstanceIds.add(storedInstanceIds.get(4));
        expectedInstanceIds.add(storedInstanceIds.get(5));
        for (String element : instanceIds)
            assertTrue(expectedInstanceIds.contains(element));

        instanceIds = sps.getInstanceIdsForSellers(list);

        assertNotNull("List expected", instanceIds);
        assertEquals("Instance ids - ", 1, instanceIds.size());

        expectedInstanceIds = new ArrayList<String>();
        expectedInstanceIds.add(storedInstanceIds.get(4));
        expectedInstanceIds.add(storedInstanceIds.get(5));
        for (String element : instanceIds)
            assertTrue(expectedInstanceIds.contains(element));
    }

    @After
    public void logout() {
        try {
            container.logout();
        } catch (Exception e) {
            // don't care
        }
    }

    private static void loginPlatformOperator() {
        container.login(1, ROLE_PLATFORM_OPERATOR);
    }

    private static void loginTechnologyProviderAdmin(int index) {
        container.login(
                String.valueOf(technologyProviderAdmins[index].getKey()),
                ROLE_ORGANIZATION_ADMIN);
    }

    private static void loginTechnologyProviderManager(int index) {
        container.login(
                String.valueOf(technologyProviderAdmins[index].getKey()),
                ROLE_TECHNOLOGY_MANAGER);
    }

    private static void loginSupplierAdmin(int index) {
        container.login(String.valueOf(supplierAdmins[index]),
                ROLE_ORGANIZATION_ADMIN);
    }

    private static void loginSupplierServiceManger(int index) {
        container.login(String.valueOf(supplierUsers[index].getKey()),
                ROLE_SERVICE_MANAGER);
    }

}
