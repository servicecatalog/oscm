/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Tenant;
import org.oscm.identityservice.local.IdentityServiceLocal;

/**
 *
 * @author PLGrubskiM
 *
 */
public class SubscriptionUtilBeanTest {

    private SubscriptionUtilBean bean;

    @Before
    public void setUp() {
        bean = new SubscriptionUtilBean();
        bean.idManager = mock(IdentityServiceLocal.class);
    }

    @Test
    public void setSubscriptionOwnerTest() throws Exception {
        // given
        final String ownerId = "someOwnerId";
        final String tenantId = "someTenantId";

        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);

        Organization organization = new Organization();
        organization.setKey(11111L);
        organization.setTenant(tenant);

        Subscription subscriptionToModify = new Subscription();
        subscriptionToModify.setKey(22222L);
        subscriptionToModify.setOrganization(organization);

        PlatformUser platformUser = mock(PlatformUser.class);
        // when
        final IdentityServiceLocal mockIdManager = bean.idManager;
        when(mockIdManager.getPlatformUser(ownerId, tenantId, false))
                .thenReturn(platformUser);
        bean.setSubscriptionOwner(subscriptionToModify, ownerId, false);
        // then
        verify(mockIdManager, times(1)).getPlatformUser(ownerId, tenantId,
                false);
        Assert.assertTrue(subscriptionToModify.getOwner().equals(platformUser));
    }

    @Test
    public void setSubscriptionOwnerTest_nullOwner() throws Exception {
        // given
        final String ownerId = null;
        final String tenantId = "someTenantId";

        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);

        Organization organization = new Organization();
        organization.setKey(11111L);
        organization.setTenant(tenant);

        Subscription subscriptionToModify = new Subscription();
        subscriptionToModify.setKey(22222L);
        subscriptionToModify.setOrganization(organization);

        PlatformUser platformUser = mock(PlatformUser.class);
        // when
        final IdentityServiceLocal mockIdManager = bean.idManager;
        when(mockIdManager.getPlatformUser(ownerId, tenantId, false))
                .thenReturn(platformUser);
        bean.setSubscriptionOwner(subscriptionToModify, ownerId, false);
        // then
        verify(mockIdManager, times(0)).getPlatformUser(ownerId, tenantId,
                false);
        Assert.assertTrue(subscriptionToModify.getOwner() == null);
    }

    @Test
    public void setSubscriptionOwnerTest_emptyOwner() throws Exception {
        // given
        final String ownerId = "";
        final String tenantId = "someTenantId";

        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);

        Organization organization = new Organization();
        organization.setKey(11111L);
        organization.setTenant(tenant);

        Subscription subscriptionToModify = new Subscription();
        subscriptionToModify.setKey(22222L);
        subscriptionToModify.setOrganization(organization);

        PlatformUser platformUser = mock(PlatformUser.class);
        // when
        final IdentityServiceLocal mockIdManager = bean.idManager;
        when(mockIdManager.getPlatformUser(ownerId, tenantId, false))
                .thenReturn(platformUser);
        bean.setSubscriptionOwner(subscriptionToModify, ownerId, false);
        // then
        verify(mockIdManager, times(0)).getPlatformUser(ownerId, tenantId,
                false);
        Assert.assertTrue(subscriptionToModify.getOwner() == null);
    }
}
