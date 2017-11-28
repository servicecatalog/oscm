/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.11.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UsageLicense;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;

/**
 * @author stavreva
 * 
 */
public class OperatorServiceBeanUsersSqlIT extends EJBTestBase {

    private DataService dm;
    private OperatorService operatorService;
    private Organization org1;
    private Organization org2;
    private PlatformUser user1Org1;
    private PlatformUser user2Org1;
    private PlatformUser user1Org2;
    private PlatformUser user2Org2;
    private Subscription sub1Org1;
    private Subscription sub1Org2;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        container.addBean(new OperatorServiceBean());
        dm = container.get(DataService.class);
        operatorService = container.get(OperatorService.class);

        org1 = createOrganization("Org1");
        user1Org1 = createUser(org1, "user1Org1");
        user2Org1 = createUser(org1, "user2Org1");
        org2 = createOrganization("Org2");
        user1Org2 = createUser(org2, "user1Org2");
        user2Org2 = createUser(org2, "user2Org2");

        Product prodOrg1 = createProduct(org1, "prodOrg1");
        Product prodOrg2 = createProduct(org2, "prodOrg2");

        sub1Org1 = createSubscription(org1, prodOrg1.getProductId(), "sub1Org1");
        createSubscription(org1, prodOrg1.getProductId(), "sub2Org1");
        sub1Org2 = createSubscription(org2, prodOrg2.getProductId(), "sub1Org2");
        createSubscription(org2, prodOrg2.getProductId(), "sub2Org2");
    }

    @Test
    public void getUnassignedUsersByOrg_NoLicenses() throws Exception {
        // given
        container.login("me", ROLE_ORGANIZATION_ADMIN);

        // when
        List<VOUserDetails> users = operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));

        // then
        assertEquals(2, users.size());

        HashSet<Long> expectedUsers = new HashSet<Long>();
        expectedUsers.add(Long.valueOf(user1Org1.getKey()));
        expectedUsers.add(Long.valueOf(user2Org1.getKey()));

        HashSet<Long> actualUsers = new HashSet<Long>();
        actualUsers.add(Long.valueOf(users.get(0).getKey()));
        actualUsers.add(Long.valueOf(users.get(1).getKey()));

        assertEquals(expectedUsers, actualUsers);
    }

    @Test
    public void getUnassignedUsersByOrg_WithLicenses() throws Exception {
        // given
        container.login("me", ROLE_ORGANIZATION_ADMIN);
        createUsage(user1Org1, sub1Org1);

        // when
        List<VOUserDetails> users = operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));

        // then
        assertEquals(1, users.size());
        assertEquals(user2Org1.getKey(), users.get(0).getKey());
    }

    @Test
    public void getUnassignedUsersByOrg_WithAllLicenses() throws Exception {
        // given
        container.login("me", ROLE_ORGANIZATION_ADMIN);
        createUsage(user1Org1, sub1Org1);
        createUsage(user2Org1, sub1Org1);
        // when
        List<VOUserDetails> users = operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));

        // then
        assertEquals(0, users.size());
    }

    @Test
    public void getUnassignedUsersByOrg_WithOtherUserLicenses()
            throws Exception {
        // given
        container.login("me", ROLE_ORGANIZATION_ADMIN);
        createUsage(user1Org2, sub1Org2);
        createUsage(user2Org2, sub1Org2);

        // when
        List<VOUserDetails> users = operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));

        // then
        assertEquals(2, users.size());

        HashSet<Long> expectedUsers = new HashSet<Long>();
        expectedUsers.add(Long.valueOf(user1Org1.getKey()));
        expectedUsers.add(Long.valueOf(user2Org1.getKey()));

        HashSet<Long> actualUsers = new HashSet<Long>();
        actualUsers.add(Long.valueOf(users.get(0).getKey()));
        actualUsers.add(Long.valueOf(users.get(1).getKey()));

        assertEquals(expectedUsers, actualUsers);
    }

    @Test
    public void getUnassignedUsersByOrg_WithSameUserLicenses() throws Exception {
        // given
        container.login("me", ROLE_ORGANIZATION_ADMIN);
        createUsage(user1Org1, sub1Org1);
        createUsage(user1Org1, sub1Org2);

        // when
        List<VOUserDetails> users = operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));

        // then
        assertEquals(1, users.size());
        assertEquals(user2Org1.getKey(), users.get(0).getKey());
    }

    private Organization createOrganization(final String orgId)
            throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(dm, orgId,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.CUSTOMER);
                dm.flush();
                return org;
            }
        });
    }

    private PlatformUser createUser(final Organization org, final String userId)
            throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser user = PlatformUsers.createUser(dm, userId, org);
                dm.flush();
                return user;
            }
        });
    }

    private Product createProduct(final Organization org, final String productId)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                TechnicalProduct techProduct = TechnicalProducts
                        .createTechnicalProduct(dm, org, "tech1" + productId,
                                false, ServiceAccessType.DIRECT);
                Product product = Products.createProduct(
                        org.getOrganizationId(), productId,
                        techProduct.getTechnicalProductId(), dm);
                dm.flush();
                return product;
            }
        });
    }

    private Subscription createSubscription(final Organization org,
            final String productId, final String subscriptionId)
            throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Subscription sub = Subscriptions
                        .createSubscription(dm, org.getOrganizationId(),
                                productId, subscriptionId, org);
                dm.flush();
                return sub;
            }
        });
    }

    private UsageLicense createUsage(final PlatformUser user,
            final Subscription subscription) throws Exception {
        return runTX(new Callable<UsageLicense>() {
            @Override
            public UsageLicense call() throws Exception {
                UsageLicense license = Subscriptions.createUsageLicense(dm,
                        user, subscription);
                dm.flush();
                return license;
            }
        });
    }

    @Test(expected = EJBException.class)
    public void getUnassignedUsersByOrg_MarketplaceOwner() throws Exception {
        container.login("me", ROLE_MARKETPLACE_OWNER);
        operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));
    }

    @Test(expected = EJBException.class)
    public void getUnassignedUsersByOrg_BrokerManager() throws Exception {
        container.login("me", ROLE_BROKER_MANAGER);
        operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));
    }

    @Test(expected = EJBException.class)
    public void getUnassignedUsersByOrg_TechnologyManager() throws Exception {
        container.login("me", ROLE_TECHNOLOGY_MANAGER);
        operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));
    }

    @Test(expected = EJBException.class)
    public void getUnassignedUsersByOrg_ServiceManager() throws Exception {
        container.login("me", ROLE_SERVICE_MANAGER);
        operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));
    }

    @Test
    public void getUnassignedUsersByOrg_OrganizationAdmin() throws Exception {
        container.login("me", ROLE_ORGANIZATION_ADMIN);
        operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));
    }

    @Test
    public void getUnassignedUsersByOrg_SubscriptionManager() throws Exception {
        container.login("me", ROLE_SUBSCRIPTION_MANAGER);
        operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));
    }

    @Test
    public void getUnassignedUsersByOrg_UnitAdministrator() throws Exception {
        container.login("me", ROLE_UNIT_ADMINISTRATOR);
        operatorService.getUnassignedUsersByOrg(
                Long.valueOf(sub1Org1.getKey()), Long.valueOf(org1.getKey()));
    }

}
