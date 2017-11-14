/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.OnBehalfUserReference;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UserRole;
import org.oscm.subscriptionservice.dao.OrganizationDao;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * @author stavreva
 * 
 */
public class SubscriptionServiceBeanEmailReceiversTest {

    private final static SubscriptionServiceBean subBean = spy(new SubscriptionServiceBean());
    private final static TerminateSubscriptionBean terminateBean = spy(new TerminateSubscriptionBean());
    private final ManageSubscriptionBean manageBean = spy(new ManageSubscriptionBean());
    private final OrganizationDao orgDao = mock(OrganizationDao.class);
    private final List<PlatformUser> givenUsers = new ArrayList<PlatformUser>();

    @Before
    public void setup() throws Exception {
        subBean.terminateBean = terminateBean;
        subBean.manageBean = manageBean;
        doReturn(orgDao).when(subBean.terminateBean).getOrganizationDao();

    }

    @Test
    public void getReceiversForAbortAsyncSubscription() {

        // given
        Organization techProvider = givenOrganization("TP");
        Organization customer = givenOrganization("CUSTOMER");
        PlatformUser owner = givenSubscriptionOwner(customer);
        Subscription sub = givenSubscription(techProvider, customer, owner);
        givenUsers.add(customer.getPlatformUsers().get(0));
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());

        // when
        List<PlatformUser> users = subBean.terminateBean
                .getCustomerAndTechnicalProductAdminForSubscription(sub);

        // then
        assertEquals(3, users.size());
        verifyAdminOrSubManager(users);

    }

    @Test
    public void getReceiversForAbortAsyncSubscription_OwnerAdmin() {

        // given
        Organization techProvider = givenOrganization("TP");
        Organization customer = givenOrganization("CUSTOMER");
        PlatformUser owner = customer.getOrganizationAdmins().get(0);
        Subscription sub = givenSubscription(techProvider, customer, owner);
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());

        // when
        List<PlatformUser> users = subBean.terminateBean
                .getCustomerAndTechnicalProductAdminForSubscription(sub);

        // then
        assertEquals(2, users.size());
        verifyOnlyAdmin(users);

    }

    @Test
    public void getReceiversForAbortAsyncSubscription_OwnerTechProdAdmin() {

        // given
        Organization techProvider = givenOrganization("TP");
        PlatformUser owner = techProvider.getOrganizationAdmins().get(0);
        Subscription sub = givenSubscription(techProvider, techProvider, owner);
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());

        // when
        List<PlatformUser> users = subBean.terminateBean
                .getCustomerAndTechnicalProductAdminForSubscription(sub);

        // then
        assertEquals(1, users.size());
        verifyOnlyAdmin(users);

    }

    @Test
    public void getCustomerAdminsAndSubscriptionOwner() {

        // given
        Organization techProvider = givenOrganization("TP");
        Organization customer = givenOrganization("CUSTOMER");
        PlatformUser owner = givenSubscriptionOwner(customer);
        Subscription sub = givenSubscription(techProvider, customer, owner);
        givenUsers.add(customer.getPlatformUsers().get(0));
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());

        // when
        List<PlatformUser> users = subBean.terminateBean
                .getCustomerAdminsAndSubscriptionOwner(sub);

        // then
        assertEquals(2, users.size());
        verifyAdminOrSubManager(users);

    }

    @Test
    public void getCustomerAdminsAndSubscriptionOwner_OwnerAdmin() {

        // given
        Organization techProvider = givenOrganization("TP");
        Organization customer = givenOrganization("CUSTOMER");
        PlatformUser owner = customer.getOrganizationAdmins().get(0);
        Subscription sub = givenSubscription(techProvider, customer, owner);
        givenUsers.add(customer.getPlatformUsers().get(0));
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());

        // when
        List<PlatformUser> users = subBean.terminateBean
                .getCustomerAdminsAndSubscriptionOwner(sub);

        // then
        assertEquals(1, users.size());
        verifyOnlyAdmin(users);

    }

    @Test
    public void getCustomerAdminsAndSubscriptionOwner_exceptAdminOnbehalf() {

        // given
        Organization techProvider = givenOrganization("TP");
        Organization customer = givenOrganization("CUSTOMER");
        PlatformUser owner = givenSubscriptionOwner(customer);
        Subscription sub = givenSubscription(techProvider, customer, owner);
        PlatformUser user = customer.getPlatformUsers().get(0);
        user.setMaster(new OnBehalfUserReference());
        givenUsers.add(user);
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());

        // when
        List<PlatformUser> users = subBean.terminateBean
                .getCustomerAdminsAndSubscriptionOwner(sub);

        // then
        assertEquals(1, users.size());
        verifyAdminOrSubManager(users);

    }

    @Test
    public void getCustomerAdminsAndSubscriptionOwner_exceptOwnerOnbehalf() {

        // given
        Organization techProvider = givenOrganization("TP");
        Organization customer = givenOrganization("CUSTOMER");
        PlatformUser owner = givenSubscriptionOwner(customer);
        owner.setMaster(new OnBehalfUserReference());
        Subscription sub = givenSubscription(techProvider, customer, owner);
        givenUsers.add(customer.getPlatformUsers().get(0));
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());

        // when
        List<PlatformUser> users = subBean.terminateBean
                .getCustomerAdminsAndSubscriptionOwner(sub);

        // then
        assertEquals(1, users.size());
        verifyAdminOrSubManager(users);
    }

    @Test
    public void getCustomerAdminsAndSubscriptionOwner_exceptOnbehalf() {

        // given
        Organization techProvider = givenOrganization("TP");
        Organization customer = givenOrganization("CUSTOMER");
        PlatformUser owner = givenSubscriptionOwner(customer);
        owner.setMaster(new OnBehalfUserReference());
        Subscription sub = givenSubscription(techProvider, customer, owner);
        PlatformUser user = customer.getPlatformUsers().get(0);
        user.setMaster(new OnBehalfUserReference());
        givenUsers.add(user);
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());

        // when
        List<PlatformUser> users = subBean.terminateBean
                .getCustomerAdminsAndSubscriptionOwner(sub);

        // then
        assertEquals(0, users.size());
        verifyAdminOrSubManager(users);

    }

    private Subscription givenSubscription(Organization techProvider,
            Organization customer, PlatformUser subOwner) {

        TechnicalProduct tp = new TechnicalProduct();
        tp.setOrganization(techProvider);
        Product prod = new Product();
        prod.setTechnicalProduct(tp);
        Subscription sub = new Subscription();
        sub.setSubscriptionId("id");
        sub.setProduct(prod);
        sub.setOrganization(customer);
        sub.setOwner(subOwner);
        return sub;
    }

    private Organization givenOrganization(String orgId) {
        Organization org = new Organization();
        org.setOrganizationId(orgId);

        PlatformUser user = new PlatformUser();
        user.setUserId(orgId + "_" + UserRoleType.ORGANIZATION_ADMIN.name());
        user.setOrganization(org);
        RoleAssignment roleAssign = new RoleAssignment();
        roleAssign.setUser(user);
        roleAssign.setRole(new UserRole(UserRoleType.ORGANIZATION_ADMIN));
        user.getAssignedRoles().add(roleAssign);
        org.getPlatformUsers().add(user);

        user = new PlatformUser();
        user.setUserId(orgId + "_" + UserRoleType.MARKETPLACE_OWNER.name());
        user.setOrganization(org);
        roleAssign = new RoleAssignment();
        roleAssign.setUser(user);
        roleAssign.setRole(new UserRole(UserRoleType.MARKETPLACE_OWNER));
        user.getAssignedRoles().add(roleAssign);
        org.getPlatformUsers().add(user);

        return org;
    }

    private PlatformUser givenSubscriptionOwner(Organization org) {
        PlatformUser user = new PlatformUser();
        user.setUserId(org.getOrganizationId() + "_"
                + UserRoleType.SUBSCRIPTION_MANAGER.name());
        user.setOrganization(org);
        RoleAssignment roleAssign = new RoleAssignment();
        roleAssign.setUser(user);
        roleAssign.setRole(new UserRole(UserRoleType.SUBSCRIPTION_MANAGER));
        user.getAssignedRoles().add(roleAssign);
        org.getPlatformUsers().add(user);
        return user;
    }

    private void verifyAdminOrSubManager(List<PlatformUser> users) {

        for (PlatformUser user : users) {
            for (RoleAssignment r : user.getAssignedRoles()) {
                UserRoleType role = r.getRole().getRoleName();
                if (!(UserRoleType.ORGANIZATION_ADMIN.equals(role) || (UserRoleType.SUBSCRIPTION_MANAGER
                        .equals(role)))) {
                    fail("User without ORGANIZATION_ADMIN or SUBSCRIPTION_MANAGER role found.");
                }
            }

        }
    }

    private void verifyOnlyAdmin(List<PlatformUser> users) {

        for (PlatformUser user : users) {
            for (RoleAssignment r : user.getAssignedRoles()) {
                UserRoleType role = r.getRole().getRoleName();
                if (!(UserRoleType.ORGANIZATION_ADMIN.equals(role))) {
                    fail("User without ORGANIZATION_ADMIN role found.");
                }
            }

        }
    }
}
