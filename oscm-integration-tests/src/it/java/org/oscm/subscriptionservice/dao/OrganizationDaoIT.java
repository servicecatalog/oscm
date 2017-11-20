/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Unit tests for {@link OrganizationDao} using the test EJB container.
 * 
 * @author Mao
 */
public class OrganizationDaoIT extends EJBTestBase {

    protected static final String GLOBAL_MP_ID = "GLOBAL_MP";
    private DataService ds;
    private OrganizationDao dao;
    Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
            SubscriptionStatus.PENDING);

    private static final String SUPPLIER_SUB_ID = "supplierSubscription";
    private static final String SUPPLIER_CUST_SUB_ID = "supplierCustomerSubscription";
    private static final String BROKER_SUB_ID = "brokerSubscription";
    private static final String BROKER_CUST_SUB_ID = "brokerCustomerSubscription";
    private static final String RESELLER_SUB_ID = "resellerSubscription";
    private static final String RESELLER_CUST_SUB_ID = "resellerCustomerSubscription";
    private static final String COMMON_SUB_ID = "commonSubscription";
    private Organization tpSupOrg;
    private long tpSupUserKey;
    private Organization supplierCustomerOrg;
    private Organization brokerOrg;
    private Organization brokerCustomerOrg;
    private Organization resellerOrg;
    private Organization resellerCustomerOrg;
    protected SubscriptionServiceLocal subMgmtLocal;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new OrganizationDao(ds);
        createOrganizations();
    }

    private void createOrganizations() throws Exception {
        tpSupOrg = Organizations.createOrganization(ds, "supplier",
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        tpSupUserKey = Organizations
                .createUserForOrg(ds, tpSupOrg, true, "tpSup").getKey();
        supplierCustomerOrg = Organizations.createCustomer(ds, tpSupOrg);
        brokerOrg = Organizations.createOrganization(ds, "broker",
                OrganizationRoleType.BROKER);
        brokerCustomerOrg = Organizations.createCustomer(ds, brokerOrg);

        resellerOrg = Organizations.createOrganization(ds, "reseller",
                OrganizationRoleType.RESELLER);
        resellerCustomerOrg = Organizations.createCustomer(ds, resellerOrg);
    }

    @Test
    public void getCustomersForSubscriptionId() throws Exception {
        // given
        createSubscriptions();

        // when
        container.login(tpSupUserKey, ROLE_SERVICE_MANAGER);

        // when
        List<Organization> result = runTX(new Callable<List<Organization>>() {
            @Override
            public List<Organization> call() throws Exception {
                return dao.getCustomersForSubscriptionId(tpSupOrg,
                        COMMON_SUB_ID, states);
            }
        });

        // then
        assertEquals(1, result.size());
        assertEquals(supplierCustomerOrg.getKey(), result.get(0).getKey());
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

    private void createSubscriptionForOfferer(final TechnicalProduct tProduct,
            final Organization offerer, final Organization customer,
            final String productId, final String subscriptionId)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = Products.createProduct(offerer, tProduct,
                        false, productId, null, ds);
                Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), product.getProductId(),
                        subscriptionId, offerer);
                return null;
            }
        });
    }

    private TechnicalProduct createTechnicalProduct(final String serviceId,
            final ServiceAccessType accessType) throws Exception {
        return runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                return TechnicalProducts.createTechnicalProduct(ds, tpSupOrg,
                        serviceId, false, accessType);
            }
        });
    }
}
