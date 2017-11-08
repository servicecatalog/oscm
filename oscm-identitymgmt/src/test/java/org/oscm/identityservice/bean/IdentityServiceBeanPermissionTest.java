/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import javax.annotation.security.RolesAllowed;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.UserRoleType;

public class IdentityServiceBeanPermissionTest {

    private Class<?> beanClass;

    @Before
    public void setup() throws Exception {
        beanClass = Class
                .forName("org.oscm.identityservice.bean.IdentityServiceBean");
    }

    @Test
    public void getUsersForOrganization_NotAuthorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getUsersForOrganization");

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.BROKER_MANAGER);

        // then
        assertFalse(isRoleAllowed);
    }

    @Test
    public void getUsersForOrganization_Admin_Authorized() throws Exception {
        // given
        Method method = beanClass.getMethod("getUsersForOrganization");

        // when
        boolean isRoleAllowed = isRoleAllowed(method,
                UserRoleType.ORGANIZATION_ADMIN);

        // then
        assertTrue(isRoleAllowed);
    }

    @Test
    public void getUsersForOrganization_SubManager_Authorized()
            throws Exception {
        // given
        Method method = beanClass.getMethod("getUsersForOrganization");

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
