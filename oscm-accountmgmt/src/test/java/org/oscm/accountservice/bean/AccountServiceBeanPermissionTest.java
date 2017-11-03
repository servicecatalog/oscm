/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import javax.annotation.security.RolesAllowed;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.UserRoleType;

public class AccountServiceBeanPermissionTest {

    private Class<?> beanClass;

    @Before
    public void setup() throws Exception {
        beanClass = Class
                .forName("org.oscm.accountservice.bean.AccountServiceBean");
    }

    @Test
    public void getBillingContacts_NotAuthorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getBillingContacts");

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.BROKER_MANAGER);

        // then
        assertFalse(isRoleAllowed);
    }

    @Test
    public void getBillingContacts_Admin_Authorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getBillingContacts");

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.ORGANIZATION_ADMIN);

        // then
        assertTrue(isRoleAllowed);
    }

    @Test
    public void getBillingContacts_SubManager_Authorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getBillingContacts");

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isRoleAllowed);
    }

    @Test
    public void getPaymentInfos_NotAuthorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getPaymentInfos");

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.BROKER_MANAGER);

        // then
        assertFalse(isRoleAllowed);
    }

    @Test
    public void getPaymentInfos_Admin_Authorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getPaymentInfos");

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.ORGANIZATION_ADMIN);

        // then
        assertTrue(isRoleAllowed);
    }

    @Test
    public void getPaymentInfos_SubManager_Authorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getPaymentInfos");

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isRoleAllowed);
    }

    @Test
    public void getAvailablePaymentTypes_NotAuthorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getAvailablePaymentTypes");

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.BROKER_MANAGER);

        // then
        assertFalse(isRoleAllowed);
    }

    @Test
    public void getAvailablePaymentTypes_SubManager_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getAvailablePaymentTypes");

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isRoleAllowed);
    }

    @Test
    public void getAvailablePaymentTypes_OrganizationAdmin_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getAvailablePaymentTypes");

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.ORGANIZATION_ADMIN);

        // then
        assertTrue(isRoleAllowed);
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
