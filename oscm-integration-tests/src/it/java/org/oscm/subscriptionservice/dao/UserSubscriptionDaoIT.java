/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.12.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.paginator.Filter;
import org.oscm.paginator.PaginationSubForUser;
import org.oscm.paginator.SortOrder;
import org.oscm.paginator.Sorting;
import org.oscm.paginator.TableColumns;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

import junit.framework.Assert;

public class UserSubscriptionDaoIT extends EJBTestBase {

    private DataService ds;
    private UserSubscriptionDao dao;

    Organization organization;
    PlatformUser supplierUser;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new UserSubscriptionDao(ds);

        organization = createOrg("SupplierOrg", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        supplierUser = createUser(organization, false, "SupplierOrg_User");

        TechnicalProduct technicalProduct = TechnicalProducts
                .createTechnicalProduct(ds, organization, "TechProdWithRoles",
                        false, ServiceAccessType.LOGIN);
        TechnicalProducts.addRoleDefinition("USER", technicalProduct, ds);
        TechnicalProducts.addRoleDefinition("GUEST", technicalProduct, ds);
        TechnicalProducts.addRoleDefinition("ADMIN", technicalProduct, ds);

        Product product = Products.createProduct("SupplierOrg", "Product1",
                "TechProdWithRoles", ds);

        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub1", organization);
        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub2", organization);
        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub3", organization);
        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub4", organization);
        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub5", organization);
        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub6", organization);
        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub7", organization);
        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub8", organization);
        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub9", organization);
        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub10", organization);
        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub11", organization);
        Subscriptions.createSubscription(ds, organization.getOrganizationId(),
                product.getProductId(), "sub12", organization);
    }

    @Test
    public void testQueryGetAssignableSubscriptions() throws Exception {

        // do
        final PaginationSubForUser pagination = new PaginationSubForUser(0, 10);

        // when
        List<Object[]> result = runTX(new Callable<List<Object[]>>() {
            @Override
            public List<Object[]> call() throws Exception {
                return dao.getUserAssignableSubscriptions(pagination,
                        organization, supplierUser.getKey(),
                        Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS);
            }
        });

        // then
        Assert.assertEquals(10, result.size());

    }

    @Test
    public void testQueryGetAssignableSubscriptionsNumber() throws Exception {

        // do
        final PaginationSubForUser pagination = new PaginationSubForUser();

        // when
        List<Object[]> result = runTX(new Callable<List<Object[]>>() {
            @Override
            public List<Object[]> call() throws Exception {
                return dao.getUserAssignableSubscriptions(pagination,
                        organization, supplierUser.getKey(),
                        Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS);
            }
        });

        // then
        Assert.assertEquals(12, result.size());

    }

    @Test
    public void testQueryGetAssignableSubscriptionsWithFilter()
            throws Exception {

        // do
        Set<Filter> filterSet = new HashSet<>();

        // filter: sub1, sub10, sub11, sub12
        Filter filter = new Filter(TableColumns.SUBSCRIPTION_ID, "sub1");
        filterSet.add(filter);

        final PaginationSubForUser pagination = new PaginationSubForUser(0, 10);
        pagination.setFilterSet(filterSet);

        // when
        List<Object[]> result = runTX(new Callable<List<Object[]>>() {
            @Override
            public List<Object[]> call() throws Exception {
                return dao.getUserAssignableSubscriptions(pagination,
                        organization, supplierUser.getKey(),
                        Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS);
            }
        });

        // then
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void testQueryGetAssignableSubscriptionsWithSort() throws Exception {

        // do
        Sorting sorting = new Sorting(TableColumns.SUBSCRIPTION_ID,
                SortOrder.DESC);

        // sort descending on the first page: sub9, sub8, sub7 ...
        final PaginationSubForUser pagination = new PaginationSubForUser(0, 10);
        pagination.setSorting(sorting);

        // when
        List<Object[]> result = runTX(new Callable<List<Object[]>>() {
            @Override
            public List<Object[]> call() throws Exception {
                return dao.getUserAssignableSubscriptions(pagination,
                        organization, supplierUser.getKey(),
                        Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS);
            }
        });

        // then
        Object[] subscription = result.get(0);
        Assert.assertEquals("sub9", subscription[0]);
    }

    @Test
    public void testQueryGetAssignableSubscriptionsWithFilterAndSort()
            throws Exception {

        // do
        Sorting sorting = new Sorting(TableColumns.SUBSCRIPTION_ID,
                SortOrder.DESC);

        Set<Filter> filterSet = new HashSet<>();
        Filter filter = new Filter(TableColumns.SUBSCRIPTION_ID, "sub1");
        filterSet.add(filter);

        // sort descending and filter: sub12, sub11, sub10, sub1
        final PaginationSubForUser pagination = new PaginationSubForUser(0, 10);
        pagination.setSorting(sorting);
        pagination.setFilterSet(filterSet);

        // when
        List<Object[]> result = runTX(new Callable<List<Object[]>>() {
            @Override
            public List<Object[]> call() throws Exception {
                return dao.getUserAssignableSubscriptions(pagination,
                        organization, supplierUser.getKey(),
                        Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS);
            }
        });

        // then
        Object[] subscription = result.get(0);
        Assert.assertEquals("sub12", subscription[0]);
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void testQueryGetAssignableSubscriptionsWithFilterAndSort_Bug12420()
            throws Exception {

        // do
        Sorting sorting = new Sorting(TableColumns.SUBSCRIPTION_ID,
                SortOrder.DESC);

        Set<Filter> filterSet = new HashSet<>();
        Filter filter = new Filter(TableColumns.ROLE_IN_SUB, "W");
        filterSet.add(filter);

        // sort descending and filter: sub12, sub11, sub10, sub1
        final PaginationSubForUser pagination = new PaginationSubForUser(0, 10);
        pagination.setSorting(sorting);
        pagination.setFilterSet(filterSet);

        Map<String, Boolean> changedSelectedSubs = new HashMap<>();
        changedSelectedSubs.put("sub1", true);
        pagination.setSelectedUsersIds(changedSelectedSubs);

        // when
        List<Object[]> result = runTX(new Callable<List<Object[]>>() {
            @Override
            public List<Object[]> call() throws Exception {
                return dao.getUserAssignableSubscriptions(pagination,
                        organization, supplierUser.getKey(),
                        Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS);
            }
        });

        // then
        Assert.assertEquals(0, result.size());
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

    private PlatformUser createUser(final Organization organization,
            final boolean isAdmin, final String userId) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(ds, organization, isAdmin,
                        userId);
            }
        });
    }

}
