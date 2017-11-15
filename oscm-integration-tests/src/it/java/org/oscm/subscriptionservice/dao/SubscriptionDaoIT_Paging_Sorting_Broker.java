/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.04.2015                                                      
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
public class SubscriptionDaoIT_Paging_Sorting_Broker extends EJBTestBase {
    private DataService ds;
    private SubscriptionDao dao;
    private Organization supplier;
    private PlatformUser supplierUser;
    private final int NUM_SUBSCRIPTIONS = 5;
    private final int NUM_CUSTOMER_SUBSCRIPTIONS = 15;
    Set<SubscriptionStatus> states = Collections
            .unmodifiableSet(EnumSet.of(SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED));
    public final long TIMESTAMP = 1282816800000L;

    private final String TIMESTAMP_STRING = "2010-08-26";

    private final String SUBSCRIPTION_ID_FOR_SUB_ID_CASE = "SUB";
    private final String SUBSCRIPTION_ID_FOR_ACTIVATION_TIME_CASE = "ACT";
    private final String SUBSCRIPTION_ID_FOR_PRODUCT_CASE = "PRD";

    private final String BROKER_CUSTOMER_FOR_SUB_ID_SORTING = "brokerCustomer1";
    private final String BROKER_CUSTOMER_FOR_ACTTIME = "brokerCustomer2";
    private final String BROKER_CUSTOMER_FOR_PRODUCT_ID = "brokerCustomer3";

    private final String SERVICE_ID = "service";

    private final String TECH_SERVICE = "techService";
    private final int NUM_PRODUCTS = 5;
    private final int NUM_ORGS = 3;
    private final int NUM_ACTIVATION_TIMES = 5;
    private final int INITIAL_ELEMENT_ID = 0;
    private final String THREE_LETTER_WORD = "___";

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new SubscriptionDao(ds);

        supplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        supplierUser = createOrgUser("supp_user", supplier, "en");

        final Organization broker = createOrg("broker",
                OrganizationRoleType.BROKER);

        List<Product> products = new ArrayList<>();
        for (int i = 0; i < NUM_PRODUCTS; i++) {
            products.add(createProduct(SERVICE_ID + i, TECH_SERVICE,
                    supplier.getOrganizationId(), ServiceAccessType.LOGIN));
        }

        final Organization brokerCustomerForSubId = registerCustomer(
                BROKER_CUSTOMER_FOR_SUB_ID_SORTING, broker);
        final Organization brokerCustomerForActTime = registerCustomer(
                BROKER_CUSTOMER_FOR_ACTTIME, broker);
        final Organization brokerCustomerForProductId = registerCustomer(
                BROKER_CUSTOMER_FOR_PRODUCT_ID, broker);
        List<Product> partnerProducts = new ArrayList<>();
        for (int i = 0; i < NUM_PRODUCTS; i++)
            partnerProducts.add(createPartnerProduct(products.get(i), broker));

        for (int i = 0; i < NUM_SUBSCRIPTIONS; i++) {
            createPartnerSubscription(
                    brokerCustomerForSubId.getOrganizationId(),
                    partnerProducts.get(INITIAL_ELEMENT_ID),
                    SUBSCRIPTION_ID_FOR_SUB_ID_CASE + i, 0, TIMESTAMP, broker);
        }

        for (int i = 0; i < NUM_ACTIVATION_TIMES; i++) {
            createPartnerSubscription(
                    brokerCustomerForActTime.getOrganizationId(),
                    partnerProducts.get(INITIAL_ELEMENT_ID),
                    SUBSCRIPTION_ID_FOR_ACTIVATION_TIME_CASE + i, 0,
                    TIMESTAMP + i, broker);
        }

        for (int i = 0; i < NUM_PRODUCTS; i++) {
            createPartnerSubscription(
                    brokerCustomerForProductId.getOrganizationId(),
                    partnerProducts.get(i),
                    SUBSCRIPTION_ID_FOR_PRODUCT_CASE + i, 0, TIMESTAMP, broker);
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
            final long creationDate, final long activationDate,
            final Organization partner) throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.createPartnerSubscription(ds, customerId,
                        product.getProductId(), subscriptionId, creationDate,
                        activationDate, partner);
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
    public void getSubscriptionsForMyCustomersAscendingSortBySubscriptionId()
            throws Exception {
        // given
        final Sorting sorting = new Sorting(TableColumns.SUBSCRIPTION_ID,
                SortOrder.ASC);
        Set<Filter> filterSet = createFilterSet(null, null,
                BROKER_CUSTOMER_FOR_SUB_ID_SORTING, null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(NUM_SUBSCRIPTIONS, result.size());
        for (int i = 0; i < result.size(); i++)
            assertEquals(SUBSCRIPTION_ID_FOR_SUB_ID_CASE + i,
                    result.get(i).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsForMyCustomersDecendingSortBySubscriptionId()
            throws Exception {
        // given
        final Sorting sorting = new Sorting(TableColumns.SUBSCRIPTION_ID,
                SortOrder.DESC);
        Set<Filter> filterSet = createFilterSet(null, null,
                BROKER_CUSTOMER_FOR_SUB_ID_SORTING, null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(NUM_SUBSCRIPTIONS, result.size());
        for (int i = 0; i < result.size(); i++)
            assertEquals(
                    SUBSCRIPTION_ID_FOR_SUB_ID_CASE
                            + (NUM_SUBSCRIPTIONS - (i + 1)),
                    result.get(i).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsForMyCustomersAscendingSortByActivationTime()
            throws Exception {
        // given
        final Sorting sorting = new Sorting(TableColumns.ACTIVATION_TIME,
                SortOrder.ASC);
        Set<Filter> filterSet = createFilterSet(null, null,
                BROKER_CUSTOMER_FOR_ACTTIME, null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(NUM_ACTIVATION_TIMES, result.size());
        for (int i = 0; i < result.size(); i++)
            assertEquals(SUBSCRIPTION_ID_FOR_ACTIVATION_TIME_CASE + i,
                    result.get(i).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsForMyCustomersDecendingSortByActivationTime()
            throws Exception {
        // given
        final Sorting sorting = new Sorting(TableColumns.ACTIVATION_TIME,
                SortOrder.DESC);
        Set<Filter> filterSet = createFilterSet(null, null,
                BROKER_CUSTOMER_FOR_ACTTIME, null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(NUM_ACTIVATION_TIMES, result.size());
        for (int i = 0; i < result.size(); i++)
            assertEquals(
                    SUBSCRIPTION_ID_FOR_ACTIVATION_TIME_CASE
                            + (NUM_ACTIVATION_TIMES - (i + 1)),
                    result.get(i).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsForMyCustomersAscendingSortByCustomerId()
            throws Exception {
        // given
        final Sorting sorting = new Sorting(TableColumns.CUSTOMER_ID,
                SortOrder.ASC);
        Set<Filter> filterSet = createFilterSet(
                THREE_LETTER_WORD + INITIAL_ELEMENT_ID, TIMESTAMP_STRING, null,
                null, SERVICE_ID + INITIAL_ELEMENT_ID);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(NUM_ORGS, result.size());
        assertEquals(SUBSCRIPTION_ID_FOR_SUB_ID_CASE + INITIAL_ELEMENT_ID,
                result.get(0).getSubscriptionId());
        assertEquals(
                SUBSCRIPTION_ID_FOR_ACTIVATION_TIME_CASE + INITIAL_ELEMENT_ID,
                result.get(1).getSubscriptionId());
        assertEquals(SUBSCRIPTION_ID_FOR_PRODUCT_CASE + INITIAL_ELEMENT_ID,
                result.get(2).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsForMyCustomersDecendingSortByCustomerId()
            throws Exception {
        // given
        final Sorting sorting = new Sorting(TableColumns.CUSTOMER_ID,
                SortOrder.DESC);
        Set<Filter> filterSet = createFilterSet(
                THREE_LETTER_WORD + INITIAL_ELEMENT_ID, TIMESTAMP_STRING, null,
                null, SERVICE_ID + INITIAL_ELEMENT_ID);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(NUM_ORGS, result.size());
        assertEquals(SUBSCRIPTION_ID_FOR_PRODUCT_CASE + INITIAL_ELEMENT_ID,
                result.get(0).getSubscriptionId());
        assertEquals(
                SUBSCRIPTION_ID_FOR_ACTIVATION_TIME_CASE + INITIAL_ELEMENT_ID,
                result.get(1).getSubscriptionId());
        assertEquals(SUBSCRIPTION_ID_FOR_SUB_ID_CASE + INITIAL_ELEMENT_ID,
                result.get(2).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsForMyCustomersAscendingSortByCustomerName()
            throws Exception {
        // given
        final Sorting sorting = new Sorting(TableColumns.CUSTOMER_NAME,
                SortOrder.ASC);
        Set<Filter> filterSet = createFilterSet(
                THREE_LETTER_WORD + INITIAL_ELEMENT_ID, TIMESTAMP_STRING, null,
                null, SERVICE_ID + INITIAL_ELEMENT_ID);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(NUM_ORGS, result.size());
        assertEquals(SUBSCRIPTION_ID_FOR_SUB_ID_CASE + INITIAL_ELEMENT_ID,
                result.get(0).getSubscriptionId());
        assertEquals(
                SUBSCRIPTION_ID_FOR_ACTIVATION_TIME_CASE + INITIAL_ELEMENT_ID,
                result.get(1).getSubscriptionId());
        assertEquals(SUBSCRIPTION_ID_FOR_PRODUCT_CASE + INITIAL_ELEMENT_ID,
                result.get(2).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsForMyCustomersDecendingSortByCustomerName()
            throws Exception {
        // given
        final Sorting sorting = new Sorting(TableColumns.CUSTOMER_NAME,
                SortOrder.DESC);
        Set<Filter> filterSet = createFilterSet(
                THREE_LETTER_WORD + INITIAL_ELEMENT_ID, TIMESTAMP_STRING, null,
                null, SERVICE_ID + INITIAL_ELEMENT_ID);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(NUM_ORGS, result.size());
        assertEquals(SUBSCRIPTION_ID_FOR_PRODUCT_CASE + INITIAL_ELEMENT_ID,
                result.get(0).getSubscriptionId());
        assertEquals(
                SUBSCRIPTION_ID_FOR_ACTIVATION_TIME_CASE + INITIAL_ELEMENT_ID,
                result.get(1).getSubscriptionId());
        assertEquals(SUBSCRIPTION_ID_FOR_SUB_ID_CASE + INITIAL_ELEMENT_ID,
                result.get(2).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsForMyCustomersAscendingSortByServiceId()
            throws Exception {
        // given
        final Sorting sorting = new Sorting(TableColumns.SERVICE_ID,
                SortOrder.ASC);
        Set<Filter> filterSet = createFilterSet(null, null,
                BROKER_CUSTOMER_FOR_PRODUCT_ID, null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(NUM_PRODUCTS, result.size());
        for (int i = 0; i < result.size(); i++)
            assertEquals(SUBSCRIPTION_ID_FOR_PRODUCT_CASE + i,
                    result.get(i).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsForMyCustomersDecendingSortByServiceId()
            throws Exception {
        // given
        final Sorting sorting = new Sorting(TableColumns.SERVICE_ID,
                SortOrder.DESC);
        Set<Filter> filterSet = createFilterSet(null, null,
                BROKER_CUSTOMER_FOR_PRODUCT_ID, null, null);
        final Pagination pagination = createPagination(0,
                NUM_CUSTOMER_SUBSCRIPTIONS, sorting, filterSet);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplierUser,
                        states, pagination);
            }
        });

        // then
        assertEquals(NUM_PRODUCTS, result.size());
        for (int i = 0; i < result.size(); i++)
            assertEquals(
                    SUBSCRIPTION_ID_FOR_PRODUCT_CASE + (NUM_PRODUCTS - (i + 1)),
                    result.get(i).getSubscriptionId());
    }

}
