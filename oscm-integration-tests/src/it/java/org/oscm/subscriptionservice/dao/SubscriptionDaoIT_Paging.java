/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.03.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.paginator.Pagination;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author ono
 * 
 */
public class SubscriptionDaoIT_Paging extends EJBTestBase {
    private DataService ds;
    private SubscriptionDao dao;
    private Organization supplier;
    private PlatformUser supplierUser;
    private final int NUM_SUPP_SUBSCRIPTIONS = 20;
    private final int NUM_BROKER_SUBSCRIPTIONS = 30;

    Set<SubscriptionStatus> states = Collections
            .unmodifiableSet(EnumSet.of(SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED));

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new SubscriptionDao(ds);

        supplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        supplierUser = createOrgUser("supp_user", supplier, "en");

        final Organization supplierCustomer = registerCustomer(
                "supplierCustomer", supplier);

        Product product = createProduct("serviceB", "techServiceB",
                supplier.getOrganizationId(), ServiceAccessType.LOGIN);
        for (int i = 0; i < NUM_SUPP_SUBSCRIPTIONS; i++)
            createSubscription(supplierCustomer.getOrganizationId(),
                    product.getProductId(), "sub1" + i, supplier);

        final Organization broker = createOrg("broker",
                OrganizationRoleType.BROKER);
        final Organization brokerCustomer1 = registerCustomer("brokerCustomer1",
                broker);
        registerCustomer("brokerCustomer2", broker);
        Product partnerProduct = createPartnerProduct(product, broker);
        for (int i = 0; i < NUM_BROKER_SUBSCRIPTIONS; i++)
            createPartnerSubscription(brokerCustomer1.getOrganizationId(),
                    partnerProduct, "brokercustomer1Sub" + i, broker);
    }

    private PlatformUser createOrgUser(final String userId,
            final Organization organization, final String locale)
            throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(ds, organization, true,
                        userId, locale);
            }
        });
    }

    private Organization createOrg(final String organizationId,
            final OrganizationRoleType... roles) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds, organizationId,
                        roles);
            }
        });
    }

    private Organization registerCustomer(final String customerId,
            final Organization vendor) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createCustomer(ds, vendor, customerId,
                        false);
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

    private Product createPartnerProduct(final Product productTemplate,
            final Organization partner) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product resaleCopy = productTemplate.copyForResale(partner);
                ds.persist(resaleCopy);
                return resaleCopy;
            }
        });
    }

    private Subscription createPartnerSubscription(final String customerId,
            final Product product, final String subscriptionId,
            final Organization partner) throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.createPartnerSubscription(ds, customerId,
                        product.getProductId(), subscriptionId, partner);
            }
        });
    }

    @Test
    public void getSubscriptionsForMyCustomersPaginationNormal()
            throws Exception {
        // given
        final Pagination pagination = new Pagination();
        pagination.setOffset(0);
        pagination.setLimit(10);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(pagination.getLimit(), result.size());
    }

    @Test
    public void getSubscriptionsForMyCustomersPaginationGreaterLimit()
            throws Exception {
        // given
        final int limit_greater_listsize = 30;
        final Pagination pagination = new Pagination();
        pagination.setOffset(0);
        pagination.setLimit(limit_greater_listsize);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(NUM_SUPP_SUBSCRIPTIONS, result.size());

    }

    @Test
    public void getSubscriptionsForMyCustomersPaginatiopnPositiveOffset()
            throws Exception {
        // given
        final Pagination pagination = new Pagination();
        final int limit_less_listsize = 10;
        pagination.setOffset(5);
        pagination.setLimit(limit_less_listsize);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(limit_less_listsize, result.size());
    }

    @Test
    public void getSubscriptionsForMyCustomersPaginationNoSubscriptionReturned()
            throws Exception {
        // given
        final int offset_greater_listsize = 21;
        final Pagination pagination = new Pagination();
        pagination.setOffset(offset_greater_listsize);
        pagination.setLimit(10);
        final int expected = 0;

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(expected, result.size());
    }

    @Test
    public void getSubscriptionsForMyCustomersPaginationZeroLimit()
            throws Exception {
        // given
        final int unlimited = 0;
        final Pagination pagination = new Pagination();
        pagination.setOffset(0);
        pagination.setLimit(unlimited);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(NUM_SUPP_SUBSCRIPTIONS, result.size());
    }

    @Test
    public void getSubscriptionsForMyBrokerCustomersPaginationNormal()
            throws Exception {
        // given
        final int limit_less_listsize = 20;
        final Pagination pagination = new Pagination();
        pagination.setOffset(5);
        pagination.setLimit(limit_less_listsize);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(limit_less_listsize, result.size());
    }

    @Test
    public void getSubscriptionsForMyBrokerCustomersPaginationGreaterLimit()
            throws Exception {
        // given
        final int limit_greater_listsize = 35;
        final Pagination pagination = new Pagination();
        pagination.setOffset(5);
        pagination.setLimit(limit_greater_listsize);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(limit_greater_listsize, result.size());
    }

    @Test
    public void getSubscriptionsForMyBrokerCustomersPaginationGreaterOffset()
            throws Exception {
        // given
        final int offset_greater_listsize = 35;
        final Pagination pagination = new Pagination();
        pagination.setOffset(offset_greater_listsize);
        pagination.setLimit(10);
        final int expected = 0;

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(10, result.size());
    }

}
