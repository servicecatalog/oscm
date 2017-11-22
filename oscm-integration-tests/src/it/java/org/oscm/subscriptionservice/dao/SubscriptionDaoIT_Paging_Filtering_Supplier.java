/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.04.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
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
import org.oscm.paginator.Filter;
import org.oscm.paginator.Pagination;
import org.oscm.paginator.SortOrder;
import org.oscm.paginator.Sorting;
import org.oscm.paginator.TableColumns;
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
public class SubscriptionDaoIT_Paging_Filtering_Supplier extends EJBTestBase {
    private DataService ds;
    private SubscriptionDao dao;
    private Organization supplier;
    private PlatformUser supplierUser;
    private final int NUM_SUPP_PRODUCTS = 2;
    private final int NUM_SUBSCRIPTIONS_PER_PRODUCT = 20;
    private final int NUM_CUSTOMER_SUBSCRIPTIONS = 80;
    Set<SubscriptionStatus> states = Collections
            .unmodifiableSet(EnumSet.of(SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED));
    public final long TIMESTAMP1 = 1282816800000L;
    public final long TIMESTAMP2 = 1282903200000L;

    private final String TIMESTAMP_STRING1 = "2010-08-26";
    private final String TIMESTAMP_STRING2 = "2010-08-27";

    private final String SUBSCRIPTION_ID1 = "subscription1";
    private final String SUBSCRIPTION_ID2 = "subscription2";

    private final String SUPPLIER_CUSTOMER1 = "supplierCustomer1";
    private final String SUPPLIER_CUSTOMER2 = "supplierCustomer2";

    private final String CUSTOMER_NAME_PREFIX = "Name of organization ";

    private final String SERVICE_ID1 = "service1";
    private final String SERVICE_ID2 = "service2";

    private final String TECH_SERVICE = "techService";

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new SubscriptionDao(ds);

        supplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        supplierUser = createOrgUser("supp_user", supplier, "en");

        final Organization supplierCustomer1 = registerCustomer(
                SUPPLIER_CUSTOMER1, supplier);
        final Organization supplierCustomer2 = registerCustomer(
                SUPPLIER_CUSTOMER2, supplier);
        List<Product> products = new ArrayList<>();
        products.add(createProduct(SERVICE_ID1, TECH_SERVICE,
                supplier.getOrganizationId(), ServiceAccessType.LOGIN));
        products.add(createProduct(SERVICE_ID2, TECH_SERVICE,
                supplier.getOrganizationId(), ServiceAccessType.LOGIN));
        for (int i = 0; i < NUM_SUBSCRIPTIONS_PER_PRODUCT; i++) {
            for (int j = 0; j < NUM_SUPP_PRODUCTS; j++) {
                createSubscription(supplierCustomer1.getOrganizationId(),
                        products.get(j).getProductId(),
                        SUBSCRIPTION_ID1 + i + SERVICE_ID1 + j, TIMESTAMP1,
                        supplier);
                createSubscription(supplierCustomer2.getOrganizationId(),
                        products.get(j).getProductId(),
                        SUBSCRIPTION_ID2 + i + SERVICE_ID2 + j, TIMESTAMP2,
                        supplier);
            }
        }
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
            final long activationDate, final Organization supplier)
            throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.createSubscription(ds, customerId,
                        productId, subscriptionId, 0, activationDate, supplier,
                        1);
            }
        });
    }

    private Pagination createPagination(int offset, int limit, Sorting sorting,
            Set<Filter> filterSet) {
        final Pagination pagination = new Pagination();
        pagination.setOffset(offset);
        pagination.setLimit(limit);
        pagination.setSorting(sorting);
        pagination.setFilterSet(filterSet);
        pagination.setDateFormat("yyyy-MM-dd");
        return pagination;
    }

    private Set<Filter> createFilterSet(String subscriptionId,
            String activationTime, String customerId, String customerName,
            String ServiceId) {
        final Filter subscriptionFilter = new Filter(
                TableColumns.SUBSCRIPTION_ID, subscriptionId);
        final Filter actTimeFilter = new Filter(TableColumns.ACTIVATION_TIME,
                activationTime);
        final Filter orgIdFilter = new Filter(TableColumns.CUSTOMER_ID,
                customerId);
        final Filter orgNameFilter = new Filter(TableColumns.CUSTOMER_NAME,
                customerName);
        final Filter productIdFilter = new Filter(TableColumns.SERVICE_ID,
                ServiceId);
        Set<Filter> filterSet = new HashSet<>();
        Collections.addAll(filterSet, subscriptionFilter, actTimeFilter,
                orgIdFilter, orgNameFilter, productIdFilter);
        return filterSet;
    }

    @Test
    public void getSubscriptionsForMyCustomersPaginationNormal()
            throws Exception {
        // given
        Set<Filter> filterSet = createFilterSet(null, null, null, null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, null, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(NUM_CUSTOMER_SUBSCRIPTIONS, result.size());
        for (int i = 0; i < NUM_CUSTOMER_SUBSCRIPTIONS; i++)
            assertEquals(SUBSCRIPTION_ID1.substring(0, 12),
                    result.get(i).getSubscriptionId().substring(0, 12));
    }

    @Test
    public void getSubscriptionsForMyCustomersFilteredBySubscriptionId()
            throws Exception {
        // given
        Set<Filter> filterSet = createFilterSet(SUBSCRIPTION_ID1, null, null,
                null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, null, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(NUM_SUPP_PRODUCTS * NUM_SUBSCRIPTIONS_PER_PRODUCT,
                result.size());
        for (int i = 0; i < NUM_SUPP_PRODUCTS
                * NUM_SUBSCRIPTIONS_PER_PRODUCT; i++)
            assertEquals(SUBSCRIPTION_ID1,
                    result.get(i).getSubscriptionId().substring(0, 13));
    }

    @Test
    public void getSubscriptionsForMyCustomersFilteredBySubscriptionIdNoSubscriptionReturned()
            throws Exception {
        // given
        final String not_existing_subscription = "not existing";
        final int expected = 0;
        Set<Filter> filterSet = createFilterSet(not_existing_subscription, null,
                null, null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, null, filterSet);

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
    public void getSubscriptionsForMyCustomersFilteredByActivation()
            throws Exception {
        // given
        Set<Filter> filterSet = createFilterSet(null, TIMESTAMP_STRING1, null,
                null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, null, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(NUM_SUPP_PRODUCTS * NUM_SUBSCRIPTIONS_PER_PRODUCT,
                result.size());
        for (int i = 0; i < NUM_SUPP_PRODUCTS
                * NUM_SUBSCRIPTIONS_PER_PRODUCT; i++)
            assertEquals(TIMESTAMP1,
                    result.get(i).getActivationDate().longValue());
    }

    @Test
    public void getSubscriptionsForMyCustomersFilteredByActivationNoSubscriptionReturned()
            throws Exception {
        // given
        final String not_existing_activation_time = "not existing";
        final int expected = 0;
        Set<Filter> filterSet = createFilterSet(null,
                not_existing_activation_time, null, null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, null, filterSet);

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
    public void getSubscriptionsForMyCustomersFilteredByCustomerId()
            throws Exception {
        // given
        Set<Filter> filterSet = createFilterSet(null, null, SUPPLIER_CUSTOMER1,
                null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, null, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(NUM_SUPP_PRODUCTS * NUM_SUBSCRIPTIONS_PER_PRODUCT,
                result.size());
        for (int i = 0; i < NUM_SUPP_PRODUCTS
                * NUM_SUBSCRIPTIONS_PER_PRODUCT; i++)
            assertEquals(SUBSCRIPTION_ID1,
                    result.get(i).getSubscriptionId().substring(0, 13));
    }

    @Test
    public void getSubscriptionsForMyCustomersFilteredByCustomerIdNoSubscriptionReturned()
            throws Exception {
        // given
        final String not_existing_customer = "not existing";
        final int expected = 0;
        Set<Filter> filterSet = createFilterSet(null, null,
                not_existing_customer, null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, null, filterSet);

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
    public void getSubscriptionsForMyCustomersFilteredByCustomerName()
            throws Exception {
        // given
        Set<Filter> filterSet = createFilterSet(null, null, null,
                CUSTOMER_NAME_PREFIX + SUPPLIER_CUSTOMER2, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, null, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(NUM_SUPP_PRODUCTS * NUM_SUBSCRIPTIONS_PER_PRODUCT,
                result.size());
        for (int i = 0; i < NUM_SUPP_PRODUCTS
                * NUM_SUBSCRIPTIONS_PER_PRODUCT; i++)
            assertEquals(SUBSCRIPTION_ID2,
                    result.get(i).getSubscriptionId().substring(0, 13));
    }

    @Test
    public void getSubscriptionsForMyCustomersFilteredByCustomerNameNoSubscriptionReturned()
            throws Exception {
        // given
        final String not_existing_customer_name = "not existing";
        final int expected = 0;
        Set<Filter> filterSet = createFilterSet(null, null, null,
                not_existing_customer_name, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, null, filterSet);

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
    public void getSubscriptionsForMyCustomersFilteredByServiceID()
            throws Exception {
        // given
        Set<Filter> filterSet = createFilterSet(null, null, null, null,
                SERVICE_ID1);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, null, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(NUM_SUPP_PRODUCTS * NUM_SUBSCRIPTIONS_PER_PRODUCT,
                result.size());
        for (int i = 0; i < NUM_SUPP_PRODUCTS
                * NUM_SUBSCRIPTIONS_PER_PRODUCT; i++)
            assertEquals(SUBSCRIPTION_ID2.substring(0, 12),
                    result.get(i).getSubscriptionId().substring(0, 12));
    }

    @Test
    public void getSubscriptionsForMyCustomersFilteredByServiceIDNoSubscriptionReturned()
            throws Exception {
        // given
        final String not_existing_service_id = "not existing";
        final int expected = 0;
        Set<Filter> filterSet = createFilterSet(null, null, null, null,
                not_existing_service_id);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, null, filterSet);

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
    public void getSubscriptionsForMyCustomersFilteredByMultiColumn()
            throws Exception {
        // given
        final Sorting sorting = new Sorting(TableColumns.SUBSCRIPTION_ID,
                SortOrder.ASC);
        Set<Filter> filterSet = createFilterSet(SUBSCRIPTION_ID1,
                TIMESTAMP_STRING1, SUPPLIER_CUSTOMER1, CUSTOMER_NAME_PREFIX,
                SERVICE_ID1);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplierUser, states,
                        pagination);
            }
        });

        // then
        assertEquals(NUM_SUBSCRIPTIONS_PER_PRODUCT, result.size());
        assertEquals(SUBSCRIPTION_ID1 + 0 + SERVICE_ID1 + 0,
                result.get(0).getSubscriptionId());
        for (int i = 0; i < 10; i++)
            assertEquals(SUBSCRIPTION_ID1 + (10 + i) + SERVICE_ID1 + 0,
                    result.get(1 + i).getSubscriptionId());
        for (int i = 10; i < 19; i++)
            assertEquals(SUBSCRIPTION_ID1 + (i - 9) + SERVICE_ID1 + 0,
                    result.get(1 + i).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsForMyCustomersFilteredByMultiColumnNoSubscriptionReturned()
            throws Exception {
        // given
        final int expected = 0;
        final Sorting sorting = new Sorting(TableColumns.SUBSCRIPTION_ID,
                SortOrder.ASC);
        Set<Filter> filterSet = createFilterSet(SUBSCRIPTION_ID1,
                TIMESTAMP_STRING2, SUPPLIER_CUSTOMER1, CUSTOMER_NAME_PREFIX,
                SERVICE_ID1);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

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

}
