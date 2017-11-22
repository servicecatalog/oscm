/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOSubscriptionIdAndOrganizations;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class SubscriptionServiceBeanCustomerSubscriptionsIT
        extends EJBTestBase {

    private static final String SUPPLIER_SUB_ID = "supplierSubscription";
    private static final String SUPPLIER_CUST_SUB_ID = "supplierCustomerSubscription";
    private static final String SUPPLIER_BROKER_CUST_SUB_ID = "supplierBrokerCustomerSubscription";
    private static final String SUPPLIER_BROKER_CUST_SUB_ID_2 = "supplierBrokerCustomerSubscription2";
    private static final String SUPPLIER_RESELLER_CUST_SUB_ID = "supplierResellerCustomerSubscription";
    private static final String BROKER_SUB_ID = "brokerSubscription";
    private static final String BROKER_CUST_SUB_ID = "brokerCustomerSubscription";
    private static final String RESELLER_SUB_ID = "resellerSubscription";
    private static final String RESELLER_CUST_SUB_ID = "resellerCustomerSubscription";
    private static final String COMMON_SUB_ID = "commonSubscription";

    private DataService mgr;
    private SubscriptionService subscriptionSvc;

    private Organization tpSupOrg;
    private long tpSupUserKey;
    private Organization supplierCustomerOrg;
    private Organization brokerOrg;
    private Organization secondBrokerOrg;
    private Organization thirdBrokerOrg;
    private long brokerUserKey;
    private Organization brokerCustomerOrg;
    private Organization secondBrokerCustomerOrg;
    private Organization resellerOrg;
    private long resellerUserKey;
    private Organization resellerCustomerOrg;
    protected SubscriptionServiceLocal subMgmtLocal;

    @Override
    public void setup(TestContainer container) throws Exception {

        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new TerminateSubscriptionBean());

        mgr = container.get(DataService.class);

        createOrganizations();
        createMarketplaces();
        subscriptionSvc = container.get(SubscriptionService.class);
        subMgmtLocal = container.get(SubscriptionServiceLocal.class);
    }

    private void createOrganizations() throws Exception {
        tpSupOrg = Organizations.createOrganization(mgr, "supplier",
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        tpSupUserKey = Organizations
                .createUserForOrg(mgr, tpSupOrg, true, "tpSup").getKey();
        supplierCustomerOrg = Organizations.createCustomer(mgr, tpSupOrg);

        brokerOrg = Organizations.createOrganization(mgr, "broker",
                OrganizationRoleType.BROKER);

        brokerUserKey = Organizations
                .createUserForOrg(mgr, brokerOrg, true, "broker").getKey();
        brokerCustomerOrg = Organizations.createCustomer(mgr, brokerOrg);
        secondBrokerCustomerOrg = Organizations.createCustomer(mgr, brokerOrg);

        secondBrokerOrg = Organizations.createOrganization(mgr, "broker2",
                OrganizationRoleType.BROKER);

        thirdBrokerOrg = Organizations.createOrganization(mgr, "broker3",
                OrganizationRoleType.BROKER);

        resellerOrg = Organizations.createOrganization(mgr, "reseller",
                OrganizationRoleType.RESELLER);
        resellerUserKey = Organizations
                .createUserForOrg(mgr, resellerOrg, true, "reseller").getKey();
        resellerCustomerOrg = Organizations.createCustomer(mgr, resellerOrg);
    }

    private void createMarketplaces() throws Exception {
        container.login(resellerUserKey, ROLE_SERVICE_MANAGER);
        Marketplaces.createMarketplace(tpSupOrg, "supplierMarketplace", true,
                mgr);
        container.login(resellerUserKey, ROLE_BROKER_MANAGER);
        Marketplaces.createMarketplace(brokerOrg, "brokerMarketplace", true,
                mgr);
        container.login(resellerUserKey, ROLE_RESELLER_MANAGER);
        Marketplaces.createMarketplace(resellerOrg, "resellerMarketplace", true,
                mgr);
    }

    @Test
    public void getSubscriptionIdentifiers_NotAuthorized() throws Exception {
        // given
        container.login(brokerUserKey, ROLE_TECHNOLOGY_MANAGER);
        try {
            // when
            subscriptionSvc.getSubscriptionIdentifiers();
            fail();
        } catch (EJBException ex) {
            // then
            assertTrue(ex.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void getSubscriptionIdentifiers_Supplier() throws Exception {
        // given
        createSubscriptions();
        container.login(tpSupUserKey, ROLE_SERVICE_MANAGER);
        final Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                return getSubscription(SUPPLIER_SUB_ID, tpSupOrg.getKey());
            }
        });
        // Expire the subscription
        subMgmtLocal.expireSubscription(sub);
        // when
        List<VOSubscriptionIdAndOrganizations> result = subscriptionSvc
                .getCustomerSubscriptions();

        // then
        assertEquals(2, result.size());
        assertEquals(COMMON_SUB_ID, result.get(0).getSubscriptionId());
        assertEquals(SUPPLIER_CUST_SUB_ID, result.get(1).getSubscriptionId());

    }

    @Test
    public void getSubscriptionsForTerminate_Supplier_WithExpired()
            throws Exception {
        // given
        createSubscriptions();
        container.login(tpSupUserKey, ROLE_SERVICE_MANAGER);
        final Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                return getSubscription(SUPPLIER_SUB_ID, tpSupOrg.getKey());
            }
        });
        // Expire the subscription
        subMgmtLocal.expireSubscription(sub);
        // when
        List<VOSubscriptionIdAndOrganizations> result = subscriptionSvc
                .getSubscriptionsForTerminate();

        // then
        assertEquals(3, result.size());
        assertEquals(COMMON_SUB_ID, result.get(0).getSubscriptionId());
        assertEquals(SUPPLIER_CUST_SUB_ID, result.get(1).getSubscriptionId());
        assertEquals(SUPPLIER_SUB_ID, result.get(2).getSubscriptionId());
    }

    private Subscription getSubscription(String subscriptionId,
            long organizationKey) {
        Subscription template = new Subscription();
        template.setOrganizationKey(organizationKey);
        template.setSubscriptionId(subscriptionId);
        return (Subscription) mgr.find(template);
    }

    @Test
    public void getSubscriptionIdentifiers_Broker() throws Exception {
        // given
        createSubscriptions();
        container.login(brokerUserKey, ROLE_BROKER_MANAGER);
        // when
        List<String> result = subscriptionSvc.getSubscriptionIdentifiers();
        // then
        assertEquals(3, result.size());
        assertEquals(BROKER_CUST_SUB_ID, result.get(0));
        assertEquals(BROKER_SUB_ID, result.get(1));
        assertEquals(COMMON_SUB_ID, result.get(2));
    }

    @Test
    public void getSubscriptionIdentifiers_Reseller() throws Exception {
        // given
        createSubscriptions();
        container.login(resellerUserKey, ROLE_RESELLER_MANAGER);
        // when
        List<String> result = subscriptionSvc.getSubscriptionIdentifiers();
        // then
        assertEquals(3, result.size());
        assertEquals(COMMON_SUB_ID, result.get(0));
        assertEquals(RESELLER_CUST_SUB_ID, result.get(1));
        assertEquals(RESELLER_SUB_ID, result.get(2));
    }

    @Test
    public void getCustomerSubscriptions_NotAuthorized() throws Exception {
        // given
        container.login(brokerUserKey, ROLE_TECHNOLOGY_MANAGER);
        try {
            // when
            subscriptionSvc.getCustomerSubscriptions();
            fail();
        } catch (EJBException ex) {
            // then
            assertTrue(ex.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void getCustomerSubscriptions_Supplier() throws Exception {
        // given
        createSubscriptions();
        container.login(tpSupUserKey, ROLE_SERVICE_MANAGER);
        // when
        List<VOSubscriptionIdAndOrganizations> result = subscriptionSvc
                .getCustomerSubscriptions();

        // then
        assertEquals(3, result.size());
        assertEquals(COMMON_SUB_ID, result.get(0).getSubscriptionId());
        assertEquals(SUPPLIER_CUST_SUB_ID, result.get(1).getSubscriptionId());
        assertEquals(SUPPLIER_SUB_ID, result.get(2).getSubscriptionId());

    }

    @Test
    public void getCustomerSubscriptions_Broker() throws Exception {
        // given
        createSubscriptions();
        container.login(brokerUserKey, ROLE_BROKER_MANAGER);
        // when
        List<VOSubscriptionIdAndOrganizations> result = subscriptionSvc
                .getCustomerSubscriptions();
        // then
        assertEquals(3, result.size());
        assertEquals(BROKER_CUST_SUB_ID, result.get(0).getSubscriptionId());
        assertEquals(BROKER_SUB_ID, result.get(1).getSubscriptionId());
        assertEquals(COMMON_SUB_ID, result.get(2).getSubscriptionId());
    }

    @Test
    public void getCustomerSubscriptions_Reseller() throws Exception {
        // given
        createSubscriptions();
        container.login(resellerUserKey, ROLE_RESELLER_MANAGER);
        // when
        List<VOSubscriptionIdAndOrganizations> result = subscriptionSvc
                .getCustomerSubscriptions();
        // then
        assertEquals(3, result.size());
        assertEquals(COMMON_SUB_ID, result.get(0).getSubscriptionId());
        assertEquals(RESELLER_CUST_SUB_ID, result.get(1).getSubscriptionId());
        assertEquals(RESELLER_SUB_ID, result.get(2).getSubscriptionId());
    }

    @Test
    public void getCustomerSubscriptions_ForMyBrokerCustomers()
            throws Exception {
        // given
        createSubscriptionsForBrokers();
        container.login(tpSupUserKey, ROLE_SERVICE_MANAGER);
        // when
        List<VOSubscriptionIdAndOrganizations> result = subscriptionSvc
                .getCustomerSubscriptions();
        // then
        assertEquals(2, result.size());

        VOSubscriptionIdAndOrganizations firstSubIdAndOrg = result.get(0);
        VOSubscriptionIdAndOrganizations secondSubIdAndOrg = result.get(1);

        assertEquals(SUPPLIER_BROKER_CUST_SUB_ID,
                firstSubIdAndOrg.getSubscriptionId());
        assertEquals(SUPPLIER_BROKER_CUST_SUB_ID_2,
                secondSubIdAndOrg.getSubscriptionId());

        // Verify customers for the two subscriptions
        List<VOOrganization> firstBrokerCustomerOrgs = firstSubIdAndOrg
                .getOrganizations();
        List<VOOrganization> secondBrokerCustomerOrgs = secondSubIdAndOrg
                .getOrganizations();
        assertEquals(supplierCustomerOrg.getKey(),
                firstBrokerCustomerOrgs.get(0).getKey());

        assertEquals(supplierCustomerOrg.getKey(),
                secondBrokerCustomerOrgs.get(0).getKey());
    }

    @Test
    public void getCustomerSubscriptions_ForMyBrokerCustomers_SameSubId()
            throws Exception {
        // given two partner-specific subscriptions with the same subscription
        // id
        TechnicalProduct tProduct = createTechnicalProduct("serviceId",
                ServiceAccessType.LOGIN);

        createSubscriptionForGrantee(tProduct, tpSupOrg, brokerCustomerOrg,
                brokerOrg, "supplierProduct", SUPPLIER_BROKER_CUST_SUB_ID);

        createSubscriptionForGrantee(tProduct, tpSupOrg,
                secondBrokerCustomerOrg, brokerOrg, "supplierProduct2",
                SUPPLIER_BROKER_CUST_SUB_ID);

        container.login(tpSupUserKey, ROLE_SERVICE_MANAGER);
        // when
        List<VOSubscriptionIdAndOrganizations> result = subscriptionSvc
                .getCustomerSubscriptions();
        // then
        assertEquals(1, result.size());

        VOSubscriptionIdAndOrganizations subIdAndOrg = result.get(0);

        assertEquals(SUPPLIER_BROKER_CUST_SUB_ID,
                subIdAndOrg.getSubscriptionId());

        // Verify customers for the same sub id
        List<VOOrganization> brokerCustomerOrgs = subIdAndOrg
                .getOrganizations();

        assertEquals(brokerCustomerOrg.getKey(),
                brokerCustomerOrgs.get(0).getKey());
        assertEquals(secondBrokerCustomerOrg.getKey(),
                brokerCustomerOrgs.get(1).getKey());
    }

    @Test
    public void getCustomerSubscriptions_ForMyBrokerCustomers_SameProduct()
            throws Exception {
        // given two partner-specific subscriptions with the same subscription
        // id and for the same product.
        TechnicalProduct tProduct = createTechnicalProduct("serviceId",
                ServiceAccessType.LOGIN);

        Product productTemplate = createProductTemplate(tProduct, tpSupOrg,
                "productId");
        createSubscriptionForGranteeAndProduct(brokerCustomerOrg, brokerOrg,
                productTemplate, SUPPLIER_BROKER_CUST_SUB_ID);

        createSubscriptionForGranteeAndProduct(secondBrokerCustomerOrg,
                brokerOrg, productTemplate, SUPPLIER_BROKER_CUST_SUB_ID);

        container.login(tpSupUserKey, ROLE_SERVICE_MANAGER);
        // when
        List<VOSubscriptionIdAndOrganizations> result = subscriptionSvc
                .getCustomerSubscriptions();
        // then
        assertEquals(1, result.size());

        VOSubscriptionIdAndOrganizations subIdAndOrg = result.get(0);
        assertEquals(SUPPLIER_BROKER_CUST_SUB_ID,
                subIdAndOrg.getSubscriptionId());

        // Verify customers for the same sub id and products
        List<VOOrganization> brokerCustomerOrgs = subIdAndOrg
                .getOrganizations();

        assertEquals(brokerCustomerOrg.getKey(),
                brokerCustomerOrgs.get(0).getKey());
        assertEquals(secondBrokerCustomerOrg.getKey(),
                brokerCustomerOrgs.get(1).getKey());
    }

    @Test
    public void getCustomerSubscriptions_NoBrokers() throws Exception {
        // given a supplier with no brokers
        container.login(tpSupUserKey, ROLE_SERVICE_MANAGER);
        // when
        List<VOSubscriptionIdAndOrganizations> result = subscriptionSvc
                .getCustomerSubscriptions();
        // then
        assertEquals(0, result.size());
    }

    @Test
    public void getCustomersForSubscriptionId_NotAuthorized() throws Exception {
        // given
        container.login(brokerUserKey, ROLE_TECHNOLOGY_MANAGER);
        try {
            // when
            subscriptionSvc.getCustomersForSubscriptionId(null);
            fail();
        } catch (EJBException ex) {
            // then
            assertTrue(ex.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void getCustomersForSubscriptionId_Supplier() throws Exception {
        // given
        createSubscriptions();

        // when
        container.login(tpSupUserKey, ROLE_SERVICE_MANAGER);
        List<VOOrganization> result = subscriptionSvc
                .getCustomersForSubscriptionId(COMMON_SUB_ID);

        // then
        assertEquals(1, result.size());
        assertEquals(supplierCustomerOrg.getKey(), result.get(0).getKey());
    }

    @Test
    public void getCustomersForSubscriptionId_Broker() throws Exception {
        // given
        createSubscriptions();
        container.login(brokerUserKey, ROLE_BROKER_MANAGER);
        // when
        List<VOOrganization> result = subscriptionSvc
                .getCustomersForSubscriptionId(COMMON_SUB_ID);
        // then
        assertEquals(1, result.size());
        assertEquals(brokerCustomerOrg.getKey(), result.get(0).getKey());
    }

    @Test
    public void getCustomersForSubscriptionId_Reseller() throws Exception {
        // given
        createSubscriptions();
        container.login(resellerUserKey, ROLE_RESELLER_MANAGER);
        // when
        List<VOOrganization> result = subscriptionSvc
                .getCustomersForSubscriptionId(COMMON_SUB_ID);
        // then
        assertEquals(1, result.size());
        assertEquals(resellerCustomerOrg.getKey(), result.get(0).getKey());
    }

    private void createSubscriptions() throws Exception {
        TechnicalProduct tProduct = createTechnicalProduct("serviceId",
                ServiceAccessType.LOGIN);

        createSubscriptionForOfferer(tProduct, tpSupOrg, tpSupOrg,
                "supplierProduct", SUPPLIER_SUB_ID);
        createSubscriptionForOfferer(tProduct, tpSupOrg, supplierCustomerOrg,
                "supplierProduct2", SUPPLIER_CUST_SUB_ID);
        createSubscriptionForOfferer(tProduct, tpSupOrg, supplierCustomerOrg,
                "supplierCommonProduct", COMMON_SUB_ID);

        createSubscriptionForOfferer(tProduct, brokerOrg, brokerOrg,
                "brokerProduct", BROKER_SUB_ID);
        createSubscriptionForOfferer(tProduct, brokerOrg, brokerCustomerOrg,
                "brokerProduct2", BROKER_CUST_SUB_ID);
        createSubscriptionForOfferer(tProduct, brokerOrg, brokerCustomerOrg,
                "brokerCommonProduct", COMMON_SUB_ID);

        createSubscriptionForOfferer(tProduct, resellerOrg, resellerOrg,
                "resellerProduct", RESELLER_SUB_ID);
        createSubscriptionForOfferer(tProduct, resellerOrg, resellerCustomerOrg,
                "resellerProduct2", RESELLER_CUST_SUB_ID);
        createSubscriptionForOfferer(tProduct, resellerOrg, resellerCustomerOrg,
                "resellerCommonProduct", COMMON_SUB_ID);
    }

    private void createSubscriptionsForBrokers() throws Exception {
        TechnicalProduct tProduct = createTechnicalProduct("serviceId",
                ServiceAccessType.LOGIN);
        // Create two partner-specific subscriptions for two brokers of the
        // supplier.
        createSubscriptionForGrantee(tProduct, tpSupOrg, supplierCustomerOrg,
                brokerOrg, "supplierProduct", SUPPLIER_BROKER_CUST_SUB_ID);

        createSubscriptionForGrantee(tProduct, tpSupOrg, supplierCustomerOrg,
                secondBrokerOrg, "supplierProduct2",
                SUPPLIER_BROKER_CUST_SUB_ID_2);

        // Create a customer subscription for a third broker (must not be
        // returned in the query result).
        createSubscriptionForOfferer(tProduct, thirdBrokerOrg,
                brokerCustomerOrg, "brokerProduct", BROKER_CUST_SUB_ID);

        // create a partner-subscription for a reseller (must not be returned in
        // the query result)
        createSubscriptionForGrantee(tProduct, tpSupOrg, supplierCustomerOrg,
                resellerOrg, "supplierProduct3", SUPPLIER_RESELLER_CUST_SUB_ID);

    }

    private TechnicalProduct createTechnicalProduct(final String serviceId,
            final ServiceAccessType accessType) throws Exception {
        return runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                return TechnicalProducts.createTechnicalProduct(mgr, tpSupOrg,
                        serviceId, false, accessType);
            }
        });
    }

    private void createSubscriptionForOfferer(final TechnicalProduct tProduct,
            final Organization offerer, final Organization customer,
            final String productId, final String subscriptionId)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = Products.createProduct(offerer, tProduct,
                        false, productId, null, mgr);
                Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), product.getProductId(),
                        subscriptionId, offerer);
                return null;
            }
        });
    }

    private void createSubscriptionForGrantee(final TechnicalProduct tProduct,
            final Organization supplier, final Organization customer,
            final Organization grantee, final String productId,
            final String subscriptionId) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Product productTemplate = Products.createProduct(supplier,
                        tProduct, false, productId, null, mgr);

                Product resaleCopy = productTemplate.copyForResale(grantee);
                mgr.persist(resaleCopy);

                Subscriptions.createPartnerSubscription(mgr,
                        customer.getOrganizationId(), resaleCopy.getProductId(),
                        subscriptionId, grantee);
                return null;
            }
        });
    }

    private Product createProductTemplate(final TechnicalProduct tProduct,
            final Organization supplier, final String productId)
            throws Exception {
        Product product = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product productTemplate = Products.createProduct(supplier,
                        tProduct, false, productId, null, mgr);

                return productTemplate;
            }
        });
        return product;
    }

    private void createSubscriptionForGranteeAndProduct(
            final Organization customer, final Organization grantee,
            final Product productTemplate, final String subscriptionId)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Product resaleCopy = productTemplate.copyForResale(grantee);
                mgr.persist(resaleCopy);

                Subscriptions.createPartnerSubscription(mgr,
                        customer.getOrganizationId(), resaleCopy.getProductId(),
                        subscriptionId, grantee);
                return null;
            }
        });
    }

}
