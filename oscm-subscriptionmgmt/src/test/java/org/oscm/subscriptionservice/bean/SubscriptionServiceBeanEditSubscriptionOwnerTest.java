/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 09.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UserRole;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;

/**
 * @author gao
 * 
 */
public class SubscriptionServiceBeanEditSubscriptionOwnerTest {

    private SubscriptionServiceBean bean;
    private IdentityServiceLocal idManager;

    private PlatformUser user;
    private PlatformUser admin;
    private PlatformUser subscriptionManager;
    private final String userId = "userId";
    private final String adminId = "adminId";
    private final String subscriptionManagerId = "subscriptionManagerId";
    private static final String OWNER = "ownerId";
    private static final String NEW_OWNER = "newownerId";

    @Before
    public void setup() throws Exception {
        user = createUser(false, false);
        admin = createUser(true, false);
        subscriptionManager = createUser(false, true);

        bean = spy(new SubscriptionServiceBean());
        bean.audit = mock(SubscriptionAuditLogCollector.class);

        idManager = mock(IdentityServiceLocal.class);
        bean.idManager = idManager;
    }

    @Test
    public void checkRolesForSubscriptionOwner_Admin() throws Exception {
        // given
        doReturn(admin).when(idManager).getPlatformUser(adminId, null, true);
        // when
        bean.checkRolesForSubscriptionOwner(adminId, null);
    }

    @Test
    public void checkRolesForSubscriptionOwner_SubscriptionManager()
            throws Exception {
        // given
        doReturn(subscriptionManager).when(idManager).getPlatformUser(
                eq(subscriptionManagerId), anyString(), eq(true));
        // when
        bean.checkRolesForSubscriptionOwner(subscriptionManagerId, null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void checkRolesForSubscriptionOwner_ObjectNotFoundException()
            throws Exception {
        // given
        String noneExsitUserId = "noneExsitUserId";
        doThrow(new ObjectNotFoundException()).when(idManager).getPlatformUser(
                eq(noneExsitUserId), anyString(), eq(true));
        // when
        try {
            bean.checkRolesForSubscriptionOwner(noneExsitUserId, null);
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkRolesForSubscriptionOwner_OperationNotPermittedException()
            throws Exception {
        // given
        doReturn(user).when(idManager).getPlatformUser(eq(userId), anyString(),
                eq(true));
        // when
        try {
            bean.checkRolesForSubscriptionOwner(userId, null);
            // then
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage()
                    .contains("Add subscription owner failed."));
            throw e;
        }
    }

    @Test
    public void logSubscriptionOwner() {
        // given
        Subscription sub = givenSubscription(NEW_OWNER);
        PlatformUser oldOwner = givenOldOwner(OWNER);

        // when
        bean.logSubscriptionOwner(sub, oldOwner);

        // then
        verify(bean.audit, times(1)).editSubscriptionOwner(
                any(DataService.class), eq(sub), eq(oldOwner));
    }

    private Subscription givenSubscription(String ownerId) {
        Subscription sub = new Subscription();
        sub.setProduct(new Product());
        if (ownerId != null) {
            PlatformUser owner = new PlatformUser();
            owner.setUserId(ownerId);
            sub.setOwner(owner);
        }
        return sub;
    }

    private PlatformUser givenOldOwner(String ownerId) {
        PlatformUser owner = null;
        if (ownerId != null) {
            owner = new PlatformUser();
            owner.setUserId(ownerId);
        }
        return owner;
    }

    /**
     * create platformUser
     * 
     * @param userIsAdmin
     * @param userIsSubscriptionManager
     * @return
     */
    private PlatformUser createUser(boolean userIsAdmin,
            boolean userIsSubscriptionManager) {
        PlatformUser user = new PlatformUser();
        if (userIsAdmin) {
            user.setUserId(adminId);
            user.setAssignedRoles(createRoleAssignment(user,
                    UserRoleType.ORGANIZATION_ADMIN));
            return user;
        }
        if (userIsSubscriptionManager) {
            user.setUserId(subscriptionManagerId);
            user.setAssignedRoles(createRoleAssignment(user,
                    UserRoleType.SUBSCRIPTION_MANAGER));
            return user;
        }
        user.setUserId(userId);
        return user;
    }

    /**
     * create roles for user
     * 
     * @param user
     * @param roleType
     * @return
     */
    private Set<RoleAssignment> createRoleAssignment(PlatformUser user,
            UserRoleType roleType) {
        Set<RoleAssignment> roles = new HashSet<RoleAssignment>();
        RoleAssignment ra = new RoleAssignment();
        ra.setKey(1L);
        ra.setUser(user);
        ra.setRole(new UserRole(roleType));
        roles.add(ra);
        return roles;
    }
}
