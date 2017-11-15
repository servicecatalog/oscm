/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 4, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTechnicalServiceOperation;
import org.oscm.internal.vo.VOUser;

public class SubscriptionServiceBeanPermissionTest {

    private Class<?> beanClass;

    @Before
    public void setup() throws Exception {
        beanClass = Class
                .forName("org.oscm.subscriptionservice.bean.SubscriptionServiceBean");
    }

    @Test
    public void terminateSubscription_Broker_NotAuthorized() throws Exception {
        // given
        Method method = beanClass.getMethod("terminateSubscription",
                VOSubscription.class, String.class);

        // when
        boolean isBrokerRoleAllowed = isRoleAllowed(method,
                UserRoleType.BROKER_MANAGER);

        // then
        assertFalse(isBrokerRoleAllowed);
    }

    @Test
    public void subscribeToService_SubscriptionManager_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("subscribeToService",
                VOSubscription.class, VOService.class, List.class,
                VOPaymentInfo.class, VOBillingContact.class, List.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void subscribeToService_OrganizationAdmin_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("subscribeToService",
                VOSubscription.class, VOService.class, List.class,
                VOPaymentInfo.class, VOBillingContact.class, List.class);

        // when
        boolean isOrganizationAdminRoleAllowed = isRoleAllowed(method,
                UserRoleType.ORGANIZATION_ADMIN);

        // then
        assertTrue(isOrganizationAdminRoleAllowed);
    }

    @Test
    public void getServiceRolesforService_SubscriptionManager_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getServiceRolesForService",
                VOService.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void executeServiceOperation_SubscriptionManager_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("executeServiceOperation",
                VOSubscription.class, VOTechnicalServiceOperation.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void hasCurrentUserSubscriptions_SubscriptionManager_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("hasCurrentUserSubscriptions");

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getSubscriptionsForUser_SubscriptionManager_NotAuthorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getSubscriptionsForUser",
                VOUser.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertFalse(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getSubscriptionsForCurrentUser_SubscriptionManager_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getSubscriptionsForCurrentUser");

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void completeAsyncSubscription_SubscriptionManager_NotAuthorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("completeAsyncSubscription",
                String.class, String.class, VOInstanceInfo.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertFalse(isSubscriptionManagerRoleAllowed);

    }

    @Test
    public void updateAsyncSubscriptionProgress_SubscriptionManager_NotAuthorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("updateAsyncSubscriptionProgress",
                String.class, String.class, List.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertFalse(isSubscriptionManagerRoleAllowed);

    }

    @Test
    public void getSubscriptionIdentifiers_SubscriptionManager_NotAuthorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getSubscriptionIdentifiers");

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertFalse(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getCustomersForSubscriptionId_SubscriptionManager_NotAuthorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getCustomersForSubscriptionId",
                String.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertFalse(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getCustomerSubscriptions_SubscriptionManager_NotAuthorized()
            throws Exception {

        // given
        Method method = beanClass.getMethod("getCustomerSubscriptions");

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertFalse(isSubscriptionManagerRoleAllowed);

    }

    @Test
    public void getSubscriptionForCustomer_SubscriptionManager_NotAuthorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getSubscriptionForCustomer",
                String.class, String.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertFalse(isSubscriptionManagerRoleAllowed);

    }

    @Test
    public void terminateSubscription_SubscriptionManager_NotAuthorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("terminateSubscription",
                VOSubscription.class, String.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertFalse(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getSubscriptionsForOrganization_Authorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getSubscriptionsForOrganization");

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getSubscriptionsForOrganization_PerformanceHint_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getSubscriptionsForOrganization",
                PerformanceHint.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getSubscriptionsForOrganizationWithFilter_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod(
                "getSubscriptionsForOrganizationWithFilter", Set.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getSubscriptionsForOrganizationWithFilter_PerformanceHint_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod(
                "getSubscriptionsForOrganizationWithFilter", Set.class,
                PerformanceHint.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getServiceRolesForSubscription_SubscriptionManger_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getServiceRolesForSubscription",
                String.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getSubscriptionDetails_SubscriptionManger_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getSubscriptionDetails",
                String.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void upgradeSubscription_SubscriptionManger_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("upgradeSubscription",
                VOSubscription.class, VOService.class, VOPaymentInfo.class,
                VOBillingContact.class, List.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getUpgradeOptions_SubscriptionManger_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getUpgradeOptions", String.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void addRevokeUser_SubscriptionManger_Authorized() throws Exception {
        // given
        Method method = beanClass.getMethod("addRevokeUser", String.class,
                List.class, List.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void modifySubscription_SubscriptionManger_Authorized()
            throws Exception {

        // given
        Method method = beanClass.getMethod("modifySubscription",
                VOSubscription.class, List.class, List.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void modifySubscriptionPaymentData_SubscriptionManger_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("modifySubscriptionPaymentData",
                VOSubscription.class, VOBillingContact.class,
                VOPaymentInfo.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void reportIssue_SubscriptionManger_Authorized() throws Exception {
        // given
        Method method = beanClass.getMethod("reportIssue", String.class,
                String.class, String.class);
        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void unsubscribeFromService_SubscriptionManger_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("unsubscribeFromService",
                String.class);

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void hasCurrentUserSubscriptions_SubscriptionManger_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("hasCurrentUserSubscriptions");

        // when
        boolean isSubscriptionManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isSubscriptionManagerRoleAllowed);
    }

    @Test
    public void getSubscriptionForManagers_ServiceManger_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getSubscriptionsForManagers");

        // when
        boolean isServiceManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.SERVICE_MANAGER);

        // then
        assertTrue(isServiceManagerRoleAllowed);
    }

    @Test
    public void getSubscriptionForManagers_BrokerManger_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getSubscriptionsForManagers");

        // when
        boolean isBrokerManagerRoleAllowed = isRoleAllowed(method,
                UserRoleType.BROKER_MANAGER);

        // then
        assertTrue(isBrokerManagerRoleAllowed);
    }

    private boolean isRoleAllowed(Method method, UserRoleType roleType) {
        RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            return true;
        }

        for (String role : rolesAllowed.value()) {
            if (role.equals(roleType.name())) {
                return true;
            }
        }

        return false;
    }

}
