/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
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
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserGroup;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.UserGroups;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class SubscriptionDaoUnitsIT extends EJBTestBase {

    private DataService ds;
    private SubscriptionDao dao;
    private Organization supplier;
    private Organization supplierCustomer;
    private Subscription subscription1Unit1;
    private Subscription subscription1Unit2;
    private Subscription subscription2Unit2;
    private Subscription subscription1Unit3;
    private UserGroup unit1;
    private UserGroup unit2;
    private UserGroup unit3;

    @Override
    protected void setup(TestContainer container) throws Exception {

        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new SubscriptionDao(ds);

        supplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        supplierCustomer = registerCustomer("supplierCustomer", supplier);

        Product product = createProduct("serviceB", "techServiceB",
                supplier.getOrganizationId(), ServiceAccessType.LOGIN);

        subscription1Unit1 = createSubscription(
                supplierCustomer.getOrganizationId(), product.getProductId(),
                "sub1", supplier);

        subscription1Unit2 = createSubscription(
                supplierCustomer.getOrganizationId(), product.getProductId(),
                "sub2", supplier);

        subscription2Unit2 = createSubscription(
                supplierCustomer.getOrganizationId(), product.getProductId(),
                "sub3", supplier);

        subscription1Unit3 = createSubscription(
                supplierCustomer.getOrganizationId(), product.getProductId(),
                "sub4", supplier);

        Subscription subscriptionNoUnit = createSubscription(
                supplierCustomer.getOrganizationId(), product.getProductId(),
                "sub5", supplier);

        unit1 = createUnit("Unit1", supplierCustomer, null);
        unit2 = createUnit("Unit2", supplierCustomer, null);
        unit3 = createUnit("Unit3", supplierCustomer, null);

        PlatformUser user1 = createUser("User1", supplierCustomer);
        createUsageLicense(user1, subscription1Unit1);
        createUsageLicense(user1, subscription1Unit2);
        createUsageLicense(user1, subscription2Unit2);
        createUsageLicense(user1, subscription1Unit3);
        createUsageLicense(user1, subscriptionNoUnit);

        // last unit assignment is important
        subscription1Unit1 = assignSubscriptionToUnit(subscription1Unit1,
                unit3);
        subscription1Unit1 = assignSubscriptionToUnit(subscription1Unit1,
                unit2);
        subscription1Unit1 = assignSubscriptionToUnit(subscription1Unit1,
                unit1);

        subscription1Unit2 = assignSubscriptionToUnit(subscription1Unit2,
                unit1);
        subscription1Unit2 = assignSubscriptionToUnit(subscription1Unit2,
                unit3);
        subscription1Unit2 = assignSubscriptionToUnit(subscription1Unit2,
                unit2);

        subscription2Unit2 = assignSubscriptionToUnit(subscription2Unit2,
                unit1);
        subscription2Unit2 = assignSubscriptionToUnit(subscription2Unit2,
                unit3);
        subscription2Unit2 = assignSubscriptionToUnit(subscription2Unit2,
                unit2);

        subscription1Unit3 = assignSubscriptionToUnit(subscription1Unit3,
                unit1);
        subscription1Unit3 = assignSubscriptionToUnit(subscription1Unit3,
                unit2);
        subscription1Unit3 = assignSubscriptionToUnit(subscription1Unit3,
                unit3);
    }

    @Test
    public void retrieveSubscriptionReportDataUnitListEmpty() throws Exception {
        // given setup
        // when
        List<ReportResultData> result = retrieveSubscriptionReportData(
                supplierCustomer.getOrganizationId(),
                Collections.<Long> emptyList());

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void retrieveSubscriptionReportDataUnitListNull() throws Exception {
        // given setup
        // when
        List<ReportResultData> result = retrieveSubscriptionReportData(
                supplierCustomer.getOrganizationId(), null);

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void retrieveSubscriptionReportDataUnit1Unit2() throws Exception {
        // given setup
        // when
        List<ReportResultData> result = retrieveSubscriptionReportData(
                supplierCustomer.getOrganizationId(),
                Arrays.asList(Long.valueOf(unit1.getKey()),
                        Long.valueOf(unit2.getKey())));

        // then
        assertEquals(3, result.size());
        verifySubscriptions(result, subscription1Unit1, subscription1Unit2,
                subscription2Unit2);
    }

    @Test
    public void retrieveSubscriptionReportDataUnit1Unit2Unit3()
            throws Exception {
        // given setup
        // when
        List<ReportResultData> result = retrieveSubscriptionReportData(
                supplierCustomer.getOrganizationId(),
                Arrays.asList(Long.valueOf(unit1.getKey()),
                        Long.valueOf(unit2.getKey()),
                        Long.valueOf(unit3.getKey())));

        // then
        assertEquals(4, result.size());
        verifySubscriptions(result, subscription1Unit1, subscription1Unit2,
                subscription2Unit2, subscription1Unit3);
    }

    @Test
    public void retrieveSubscriptionReportDataUnit1() throws Exception {
        // given setup
        // when
        List<ReportResultData> result = retrieveSubscriptionReportData(
                supplierCustomer.getOrganizationId(),
                Arrays.asList(Long.valueOf(unit1.getKey())));

        // then
        assertEquals(1, result.size());
        verifySubscriptions(result, subscription1Unit1);
    }

    @Test
    public void retrieveSubscriptionReportDataUnit2() throws Exception {
        // given setup
        // when
        List<ReportResultData> result = retrieveSubscriptionReportData(
                supplierCustomer.getOrganizationId(),
                Arrays.asList(Long.valueOf(unit2.getKey())));

        // then
        assertEquals(2, result.size());
        verifySubscriptions(result, subscription1Unit2, subscription2Unit2);
    }

    @Test
    public void retrieveSubscriptionReportDataUnit3() throws Exception {
        // given setup
        // when
        List<ReportResultData> result = retrieveSubscriptionReportData(
                supplierCustomer.getOrganizationId(),
                Arrays.asList(Long.valueOf(unit3.getKey())));

        // then
        assertEquals(1, result.size());
        verifySubscriptions(result, subscription1Unit3);
    }

    private Set<String> convertToSet(List<ReportResultData> list) {
        Set<String> subscriptionIds = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            List<Object> columnValues = list.get(i).getColumnValue();
            subscriptionIds.add((String) columnValues.get(5));
        }
        return subscriptionIds;
    }

    private void verifySubscriptions(List<ReportResultData> result,
            Subscription... subscriptions) {
        Set<String> subscriptionIds = convertToSet(result);
        for (Subscription subscription : subscriptions) {
            assertTrue(
                    subscriptionIds.contains(subscription.getSubscriptionId()));
        }
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

    private UsageLicense createUsageLicense(final PlatformUser user,
            final Subscription subscription) throws Exception {
        return runTX(new Callable<UsageLicense>() {
            @Override
            public UsageLicense call() throws Exception {
                return Subscriptions.createUsageLicense(ds, user, subscription);
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

    private PlatformUser createUser(final String userId, final Organization org)
            throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return PlatformUsers.createUser(ds, userId, org);
            }
        });
    }

    private List<ReportResultData> retrieveSubscriptionReportData(
            final String organizationId, final List<Long> unitKeys)
            throws Exception {
        return runTX(new Callable<List<ReportResultData>>() {
            @Override
            public List<ReportResultData> call() throws Exception {
                return dao.retrieveSubscriptionReportData(organizationId,
                        unitKeys);
            }
        });
    }

}
