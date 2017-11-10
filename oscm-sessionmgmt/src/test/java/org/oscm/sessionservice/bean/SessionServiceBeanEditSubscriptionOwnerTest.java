/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 09.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.sessionservice.bean;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UserRole;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.OperationNotPermittedException;

/**
 * @author stavreva
 * 
 */
public class SessionServiceBeanEditSubscriptionOwnerTest {

    private static final long SUB_KEY = 1L;
    private SessionServiceBean bean;
    private DataService dsMock;
    private SubscriptionServiceLocal subServiceMock;

    @Before
    public void setup() throws Exception {

        bean = spy(new SessionServiceBean());
        dsMock = mock(DataService.class);
        subServiceMock = mock(SubscriptionServiceLocal.class);
        bean.dm = dsMock;
        bean.subMgmt = subServiceMock;
        bean.userGroupService = mock(UserGroupServiceLocalBean.class);
        doReturn(Collections.EMPTY_LIST).when(bean.userGroupService)
                .getUserGroupsForUserWithRole(anyLong(), anyLong());
        doReturn(Collections.EMPTY_LIST).when(bean)
                .getProductSessionsForSubscriptionTKey(SUB_KEY);
    }

    @Test
    public void getNumberOfServiceSessions_AdminOwner() throws Exception {
        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        Subscription sub = givenSubscription(customer, owner);
        doReturn(owner).when(bean.dm).getCurrentUser();
        doReturn(sub).when(bean.subMgmt).loadSubscription(SUB_KEY);

        // when
        bean.getNumberOfServiceSessions(SUB_KEY);

        // then
        verify(bean, times(1)).getProductSessionsForSubscriptionTKey(SUB_KEY);
    }

    @Test
    public void getNumberOfServiceSessions_AdminNotOwner() throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        PlatformUser notOwner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        Subscription sub = givenSubscription(customer, owner);
        doReturn(notOwner).when(bean.dm).getCurrentUser();
        doReturn(sub).when(bean.subMgmt).loadSubscription(SUB_KEY);

        // when
        bean.getNumberOfServiceSessions(SUB_KEY);

        // then
        verify(bean, times(1)).getProductSessionsForSubscriptionTKey(SUB_KEY);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getNumberOfServiceSessions_AdminOtherOrg() throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        Organization otherOrg = givenOrganization("otherOrg");
        PlatformUser adminOtherOrg = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                otherOrg);
        Subscription sub = givenSubscription(customer, owner);
        doReturn(adminOtherOrg).when(bean.dm).getCurrentUser();
        doReturn(sub).when(bean.subMgmt).loadSubscription(SUB_KEY);

        // when
        bean.getNumberOfServiceSessions(SUB_KEY);

        // then
        verify(bean, times(1)).getProductSessionsForSubscriptionTKey(SUB_KEY);
    }

    @Test
    public void getNumberOfServiceSessions_ManagerOwner() throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.SUBSCRIPTION_MANAGER,
                customer);
        Subscription sub = givenSubscription(customer, owner);
        doReturn(owner).when(bean.dm).getCurrentUser();
        doReturn(sub).when(bean.subMgmt).loadSubscription(SUB_KEY);

        // when
        bean.getNumberOfServiceSessions(SUB_KEY);

        // then
        verify(bean, times(1)).getProductSessionsForSubscriptionTKey(SUB_KEY);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getNumberOfServiceSessions_ManagerNotOwner() throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.SUBSCRIPTION_MANAGER,
                customer);
        PlatformUser notOwner = givenUser(UserRoleType.SUBSCRIPTION_MANAGER,
                customer);
        Subscription sub = givenSubscription(customer, owner);
        doReturn(notOwner).when(bean.dm).getCurrentUser();
        doReturn(sub).when(bean.subMgmt).loadSubscription(SUB_KEY);

        // when
        bean.getNumberOfServiceSessions(SUB_KEY);
    }

    @Test
    public void deleteServiceSessionsForSubscription_AdminOwner()
            throws Exception {
        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        Subscription sub = givenSubscription(customer, owner);
        doReturn(owner).when(bean.dm).getCurrentUser();
        doReturn(sub).when(bean.subMgmt).loadSubscription(SUB_KEY);

        // when
        bean.deleteServiceSessionsForSubscription(SUB_KEY);

        // then
        verify(bean, times(1)).getProductSessionsForSubscriptionTKey(SUB_KEY);
    }

    @Test
    public void deleteServiceSessionsForSubscription_AdminNotOwner()
            throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        PlatformUser notOwner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        Subscription sub = givenSubscription(customer, owner);
        doReturn(notOwner).when(bean.dm).getCurrentUser();
        doReturn(sub).when(bean.subMgmt).loadSubscription(SUB_KEY);

        // when
        bean.deleteServiceSessionsForSubscription(SUB_KEY);

        // then
        verify(bean, times(1)).getProductSessionsForSubscriptionTKey(SUB_KEY);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteServiceSessionsForSubscription_AdminOtherOrg()
            throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        Organization otherOrg = givenOrganization("otherOrg");
        PlatformUser adminOtherOrg = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                otherOrg);
        Subscription sub = givenSubscription(customer, owner);
        doReturn(adminOtherOrg).when(bean.dm).getCurrentUser();
        doReturn(sub).when(bean.subMgmt).loadSubscription(SUB_KEY);

        // when
        bean.deleteServiceSessionsForSubscription(SUB_KEY);

        // then
        verify(bean, times(1)).getProductSessionsForSubscriptionTKey(SUB_KEY);
    }

    @Test
    public void deleteServiceSessionsForSubscription_ManagerOwner()
            throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.SUBSCRIPTION_MANAGER,
                customer);
        Subscription sub = givenSubscription(customer, owner);
        doReturn(owner).when(bean.dm).getCurrentUser();
        doReturn(sub).when(bean.subMgmt).loadSubscription(SUB_KEY);

        // when
        bean.deleteServiceSessionsForSubscription(SUB_KEY);

        // then
        verify(bean, times(1)).getProductSessionsForSubscriptionTKey(SUB_KEY);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteServiceSessionsForSubscription_ManagerNotOwner()
            throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.SUBSCRIPTION_MANAGER,
                customer);
        PlatformUser notOwner = givenUser(UserRoleType.SUBSCRIPTION_MANAGER,
                customer);
        Subscription sub = givenSubscription(customer, owner);
        doReturn(notOwner).when(bean.dm).getCurrentUser();
        doReturn(sub).when(bean.subMgmt).loadSubscription(SUB_KEY);

        // when
        bean.deleteServiceSessionsForSubscription(SUB_KEY);
    }

    private PlatformUser givenUser(UserRoleType roleType, Organization org) {
        PlatformUser user = new PlatformUser();
        user.setUserId("");
        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setUser(user);
        roleAssignment.setRole(new UserRole(roleType));
        user.getAssignedRoles().add(roleAssignment);
        user.setOrganization(org);
        org.getPlatformUsers().add(user);
        return user;
    }

    private Organization givenOrganization(String orgId) {
        Organization org = new Organization();
        org.setOrganizationId(orgId);
        return org;
    }

    private Subscription givenSubscription(Organization customer,
            PlatformUser owner) {
        Subscription sub = new Subscription();
        sub.setKey(SUB_KEY);
        sub.setOrganization(customer);
        sub.setOwner(owner);
        return sub;
    }

}
