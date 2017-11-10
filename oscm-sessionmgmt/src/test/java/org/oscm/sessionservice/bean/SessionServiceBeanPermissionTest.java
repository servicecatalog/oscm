/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.sessionservice.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import javax.annotation.security.RolesAllowed;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.UserRoleType;

public class SessionServiceBeanPermissionTest {

    private Class<?> beanClass;

    @Before
    public void setup() throws Exception {
        beanClass = Class
                .forName("org.oscm.sessionservice.bean.SessionServiceBean");
    }

    @Test
    public void deleteServiceSessionsForSubscription_NotAuthorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod(
                "deleteServiceSessionsForSubscription", long.class);

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.BROKER_MANAGER);

        // then
        assertFalse(isRoleAllowed);
    }

    @Test
    public void deleteServiceSessionsForSubscription_Admin_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod(
                "deleteServiceSessionsForSubscription", long.class);

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.ORGANIZATION_ADMIN);

        // then
        assertTrue(isRoleAllowed);
    }

    @Test
    public void deleteServiceSessionsForSubscription_SubManager_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod(
                "deleteServiceSessionsForSubscription", long.class);

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

        // then
        assertTrue(isRoleAllowed);
    }

    @Test
    public void getNumberOfServiceSessions_NotAuthorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getNumberOfServiceSessions",
                long.class);

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.BROKER_MANAGER);

        // then
        assertFalse(isRoleAllowed);
    }

    @Test
    public void getNumberOfServiceSessions_Admin_Authorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getNumberOfServiceSessions",
                long.class);

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.ORGANIZATION_ADMIN);

        // then
        assertTrue(isRoleAllowed);
    }

    @Test
    public void getNumberOfServiceSessions_SubManager_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getNumberOfServiceSessions",
                long.class);

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.SUBSCRIPTION_MANAGER);

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
