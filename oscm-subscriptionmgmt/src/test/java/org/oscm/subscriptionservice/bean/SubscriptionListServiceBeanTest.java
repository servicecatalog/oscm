/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserRole;
import org.oscm.subscriptionservice.converter.SubscriptionListConverter;
import org.oscm.subscriptionservice.dao.SubscriptionDao;
import org.oscm.subscriptionservice.dao.UsageLicenseDao;
import org.oscm.subscriptionservice.local.SubscriptionWithRoles;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * @author weiser
 * 
 */
public class SubscriptionListServiceBeanTest {

    private SubscriptionListServiceBean bean;
    private Set<SubscriptionStatus> states;
    private SubscriptionDao subscriptionDao;
    private UsageLicenseDao usageLicenseDao;

    @Before
    public void setup() {
        bean = spy(new SubscriptionListServiceBean());
        subscriptionDao = mock(SubscriptionDao.class);

        bean.ds = mock(DataService.class);
        bean.slc = mock(SubscriptionListConverter.class);

        usageLicenseDao = mock(UsageLicenseDao.class);
        states = EnumSet.allOf(SubscriptionStatus.class);
        when(
                subscriptionDao.getSubscriptionsWithRoles(
                        any(Organization.class), eq(states))).thenReturn(
                new ArrayList<Object[]>());
        when(subscriptionDao.getSubscriptionsForOwner(any(PlatformUser.class)))
                .thenReturn(givenOwnedSubscriptions());
        when(
                usageLicenseDao.getSubscriptionAssignments(
                        any(PlatformUser.class), eq(states))).thenReturn(
                new ArrayList<UsageLicense>());

        when(bean.slc.convert(anyListOf(Object[].class))).thenReturn(
                new ArrayList<SubscriptionWithRoles>());
        when(bean.getSubscriptionDao()).thenReturn(subscriptionDao);
        when(bean.getUsageLicenseDao()).thenReturn(usageLicenseDao);
    }

    @Test
    public void getSubcsriptionsWithRoles() {
        // given
        Organization owner = new Organization();
        owner.setKey(9876);
        Set<SubscriptionStatus> states = EnumSet
                .allOf(SubscriptionStatus.class);

        // when
        List<SubscriptionWithRoles> list = bean.getSubcsriptionsWithRoles(
                owner, states);

        // then
        verify(subscriptionDao, times(1)).getSubscriptionsWithRoles(
                any(Organization.class), eq(states));
        verify(bean.slc, times(1)).convert(anyListOf(Object[].class));
        assertNotNull(list);
    }

    @Test
    public void getSubscriptionAssignments() {
        // given
        PlatformUser user = new PlatformUser();
        user.setKey(1234);

        // when
        List<UsageLicense> list = bean.getSubscriptionAssignments(user, states);

        // then
        verify(bean.getUsageLicenseDao(), times(1)).getSubscriptionAssignments(
                any(PlatformUser.class), eq(states));
        assertNotNull(list);
    }

    @Test
    public void getSubscriptionsForOrganization_NoStatesFilter() {
        // given
        when(bean.ds.getCurrentUser())
                .thenReturn(
                        createCurrentUserWithSubscriptions(UserRoleType.ORGANIZATION_ADMIN));

        // when
        List<Subscription> list = bean.getSubscriptionsForOrganization(null);

        // then
        assertEquals(6, list.size());
        assertEquals(SubscriptionStatus.ACTIVE, list.get(0).getStatus());
        assertEquals(SubscriptionStatus.DEACTIVATED, list.get(1).getStatus());
        assertEquals(SubscriptionStatus.EXPIRED, list.get(2).getStatus());
        assertEquals(SubscriptionStatus.INVALID, list.get(3).getStatus());
        assertEquals(SubscriptionStatus.PENDING, list.get(4).getStatus());
        assertEquals(SubscriptionStatus.SUSPENDED, list.get(5).getStatus());
    }

    @Test
    public void getSubscriptionsForOrganization() {
        // given
        when(bean.ds.getCurrentUser())
                .thenReturn(
                        createCurrentUserWithSubscriptions(UserRoleType.ORGANIZATION_ADMIN));

        // when
        Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.EXPIRED, SubscriptionStatus.PENDING,
                SubscriptionStatus.SUSPENDED);
        List<Subscription> list = bean.getSubscriptionsForOrganization(states);

        // then
        assertEquals(4, list.size());
        assertEquals(SubscriptionStatus.ACTIVE, list.get(0).getStatus());
        assertEquals(SubscriptionStatus.EXPIRED, list.get(1).getStatus());
        assertEquals(SubscriptionStatus.PENDING, list.get(2).getStatus());
        assertEquals(SubscriptionStatus.SUSPENDED, list.get(3).getStatus());
    }

    @Test
    public void getAllSubscriptionsForOrganization_SUBSCRIPTION_MANAGER() {
        // given
        when(bean.ds.getCurrentUser())
                .thenReturn(
                        createCurrentUserWithSubscriptions(UserRoleType.SUBSCRIPTION_MANAGER));

        // when
        List<Subscription> list = bean.getAllSubscriptionsForOrganization();

        // then
        assertEquals(6, list.size());
    }

    @Test
    public void getAllSubscriptionsForOrganization_UNIT_ADMIN() {
        // given
        when(bean.ds.getCurrentUser())
                .thenReturn(
                        createCurrentUserWithSubscriptions(UserRoleType.UNIT_ADMINISTRATOR));

        // when
        List<Subscription> list = bean.getAllSubscriptionsForOrganization();

        // then
        assertEquals(6, list.size());
    }

    @Test
    public void getSubscriptionsForOwner() {
        // given
        PlatformUser owner = new PlatformUser();
        owner.setKey(1234);

        // when
        List<Subscription> subscriptions = bean.getSubscriptionsForOwner(owner);

        // then
        verify(subscriptionDao, times(1)).getSubscriptionsForOwner(
                any(PlatformUser.class));
        assertEquals(6, subscriptions.size());
    }

    @Test
    public void getSubscriptionsForOrganization_OwnedSubscriptions() {
        // given
        when(bean.ds.getCurrentUser()).thenReturn(
                givenSubscriptionManagerUser());

        // when
        Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.EXPIRED, SubscriptionStatus.PENDING,
                SubscriptionStatus.SUSPENDED);
        List<Subscription> list = bean.getSubscriptionsForOrganization(states);

        // then
        assertEquals(4, list.size());
        assertEquals(SubscriptionStatus.ACTIVE, list.get(0).getStatus());
        assertEquals(SubscriptionStatus.EXPIRED, list.get(1).getStatus());
        assertEquals(SubscriptionStatus.PENDING, list.get(2).getStatus());
        assertEquals(SubscriptionStatus.SUSPENDED, list.get(3).getStatus());
        verify(subscriptionDao, times(1)).getSubscriptionsForOwner(
                any(PlatformUser.class));
    }

    private PlatformUser createCurrentUserWithSubscriptions(
            UserRoleType roleType) {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        subscriptions.add(createSubscription(1, SubscriptionStatus.ACTIVE));
        subscriptions
                .add(createSubscription(2, SubscriptionStatus.DEACTIVATED));
        subscriptions.add(createSubscription(3, SubscriptionStatus.EXPIRED));
        subscriptions.add(createSubscription(4, SubscriptionStatus.INVALID));
        subscriptions.add(createSubscription(5, SubscriptionStatus.PENDING));
        subscriptions.add(createSubscription(6, SubscriptionStatus.SUSPENDED));

        Organization org = new Organization();
        org.setSubscriptions(subscriptions);
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        RoleAssignment assignment = new RoleAssignment();
        assignment.setUser(user);
        assignment.setRole(new UserRole(roleType));
        user.getAssignedRoles().add(assignment);
        return user;
    }

    PlatformUser givenSubscriptionManagerUser() {
        PlatformUser user = new PlatformUser();
        user.setKey(1234);
        RoleAssignment assignment = new RoleAssignment();
        assignment.setUser(user);
        assignment.setRole(new UserRole(UserRoleType.SUBSCRIPTION_MANAGER));
        user.getAssignedRoles().add(assignment);
        return user;
    }

    private List<Subscription> givenOwnedSubscriptions() {

        List<Subscription> ownedSubscriptions = new ArrayList<Subscription>();
        ownedSubscriptions
                .add(createSubscription(1, SubscriptionStatus.ACTIVE));
        ownedSubscriptions.add(createSubscription(2,
                SubscriptionStatus.DEACTIVATED));
        ownedSubscriptions
                .add(createSubscription(3, SubscriptionStatus.EXPIRED));
        ownedSubscriptions
                .add(createSubscription(4, SubscriptionStatus.INVALID));
        ownedSubscriptions
                .add(createSubscription(5, SubscriptionStatus.PENDING));
        ownedSubscriptions.add(createSubscription(6,
                SubscriptionStatus.SUSPENDED));

        return ownedSubscriptions;
    }

    private Subscription createSubscription(int key, SubscriptionStatus status) {
        Subscription subscription = new Subscription();
        subscription.setKey(key);
        subscription.setStatus(status);
        return subscription;
    }

}
