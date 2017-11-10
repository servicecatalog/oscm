/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class SubscriptionDaoSubscriptionsIT extends EJBTestBase {

    private DataService ds;
    private SubscriptionDao dao;
    private Organization supplier;
    private Organization supplierCustomer1;
    private Organization supplierCustomer2;
    private Subscription subCust1NoUsers;
    private Subscription subCust1NoUsersTerminated;
    private Subscription subCust1WithUsers;
    private Subscription subCust1WithUsersTerminated;
    private Subscription subCust2NoUsers;
    private Subscription subCust2NoUsersTerminated;
    private Subscription subCust2WithUsers;
    private Subscription subCust2WithUsersTerminated;

    @Override
    protected void setup(TestContainer container) throws Exception {

        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new SubscriptionDao(ds);

        supplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        registerCustomer("CustomerNoSubscriptions", supplier);

        Product productA = createProduct("serviceA", "techServiceA",
                supplier.getOrganizationId(), ServiceAccessType.LOGIN);

        Product productB = createProduct("serviceB", "techServiceB",
                supplier.getOrganizationId(), ServiceAccessType.LOGIN);

        createProduct("serviceNoSubscription", "techServiceC",
                supplier.getOrganizationId(), ServiceAccessType.LOGIN);

        supplierCustomer1 = registerCustomer("supplierCustomer1", supplier);
        PlatformUser user1Cust1 = createUser("User1Cust1", supplierCustomer1);
        PlatformUser user2Cust1 = createUser("User2Cust1", supplierCustomer1);
        PlatformUser userNotAssignedCust1 = createUser("userNotAssignedCust1",
                supplierCustomer1);

        subCust1NoUsers = createSubscription(
                supplierCustomer1.getOrganizationId(), productA.getProductId(),
                "subCust1NoUsers", supplier);

        subCust1NoUsersTerminated = createSubscription(
                supplierCustomer1.getOrganizationId(), productB.getProductId(),
                "subCust1NoUsersTerminated", supplier);

        terminateSubscription(subCust1NoUsersTerminated);

        subCust1WithUsers = createSubscription(
                supplierCustomer1.getOrganizationId(), productA.getProductId(),
                "subCust1WithUsers", supplier);
        createUsageLicense(user1Cust1, subCust1WithUsers);
        createUsageLicense(user2Cust1, subCust1WithUsers);

        subCust1WithUsersTerminated = createSubscription(
                supplierCustomer1.getOrganizationId(), productB.getProductId(),
                "subCust1WithUsersTerminated", supplier);
        createUsageLicense(user1Cust1, subCust1WithUsersTerminated);
        createUsageLicense(user2Cust1, subCust1WithUsersTerminated);

        terminateSubscription(subCust1WithUsersTerminated);

        supplierCustomer2 = registerCustomer("supplierCustomer2", supplier);
        PlatformUser user1Cust2 = createUser("User1Cust2", supplierCustomer2);
        PlatformUser user2Cust2 = createUser("User2Cust2", supplierCustomer2);
        PlatformUser userNotAssignedCust2 = createUser("userNotAssignedCust2",
                supplierCustomer1);

        subCust2NoUsers = createSubscription(
                supplierCustomer2.getOrganizationId(), productA.getProductId(),
                "subCust2NoUsers", supplier);

        subCust2NoUsersTerminated = createSubscription(
                supplierCustomer2.getOrganizationId(), productB.getProductId(),
                "subCust2NoUsersTerminated", supplier);

        terminateSubscription(subCust2NoUsersTerminated);

        subCust2WithUsers = createSubscription(
                supplierCustomer2.getOrganizationId(), productA.getProductId(),
                "subCust2WithUsers", supplier);
        createUsageLicense(user1Cust2, subCust2WithUsers);
        createUsageLicense(user2Cust2, subCust2WithUsers);

        subCust2WithUsersTerminated = createSubscription(
                supplierCustomer2.getOrganizationId(), productB.getProductId(),
                "subCust2WithUsersTerminated", supplier);
        createUsageLicense(user1Cust2, subCust2WithUsersTerminated);
        createUsageLicense(user2Cust2, subCust2WithUsersTerminated);

        terminateSubscription(subCust2WithUsersTerminated);

    }

    @Test
    public void retrieveSubscriptionReportDataCustomer1() throws Exception {
        // given setup
        // when
        List<ReportResultData> result = retrieveSubscriptionReportData(
                supplierCustomer1.getOrganizationId());

        // then
        assertEquals(3, result.size());
        verifySubscriptions(result, true, subCust1NoUsers, subCust1WithUsers);
        verifySubscriptions(result, false, subCust1NoUsersTerminated,
                subCust1WithUsersTerminated, subCust2NoUsers,
                subCust2NoUsersTerminated, subCust2WithUsers,
                subCust2WithUsersTerminated);
    }

    @Test
    public void retrieveSubscriptionReportDataCustomer2() throws Exception {
        // given setup
        // when
        List<ReportResultData> result = retrieveSubscriptionReportData(
                supplierCustomer2.getOrganizationId());

        // then
        assertEquals(3, result.size());
        verifySubscriptions(result, true, subCust2NoUsers, subCust2WithUsers);
        verifySubscriptions(result, false, subCust2NoUsersTerminated,
                subCust2WithUsersTerminated, subCust1NoUsers,
                subCust1NoUsersTerminated, subCust1WithUsers,
                subCust1WithUsersTerminated);
    }

    private Set<String> convertToSet(List<ReportResultData> list, int column) {
        Set<String> subscriptionIds = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            List<Object> columnValues = list.get(i).getColumnValue();
            subscriptionIds.add((String) columnValues.get(column));
        }
        return subscriptionIds;
    }

    private void verifySubscriptions(List<ReportResultData> result,
            boolean verifyContains, Subscription... subscriptions) {
        Set<String> subscriptionIds = convertToSet(result, 5);
        Set<String> userIds = convertToSet(result, 1);
        for (Subscription subscription : subscriptions) {
            if (verifyContains) {
                assertTrue(subscriptionIds
                        .contains(subscription.getSubscriptionId()));
            } else {
                assertFalse(subscriptionIds
                        .contains(subscription.getSubscriptionId()));
            }
            List<UsageLicense> usageLicenses = subscription.getUsageLicenses();
            for (UsageLicense usageLicense : usageLicenses) {
                if (verifyContains) {
                    assertTrue(userIds
                            .contains(usageLicense.getUser().getUserId()));
                } else {
                    assertFalse(userIds
                            .contains(usageLicense.getUser().getUserId()));
                }
            }
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

    private void terminateSubscription(final Subscription subscription)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscriptions.teminateSubscription(ds, subscription);
                return null;
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
            final String organizationId) throws Exception {
        return runTX(new Callable<List<ReportResultData>>() {
            @Override
            public List<ReportResultData> call() throws Exception {
                return dao.retrieveSubscriptionReportData(organizationId);
            }
        });
    }

}
