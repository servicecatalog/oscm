/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 10.10.2019
 *
 *******************************************************************************/

package org.oscm.identityservice.bean;

import org.junit.Before;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Tenant;
import org.oscm.identity.model.GroupInfo;
import org.oscm.identityservice.model.UserImportModel;
import org.oscm.internal.vo.VOUserDetails;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class OidcSynchronizationBeanTest {

    private OidcSynchronizationBean oidcSyncBean;
    private DataService dm;

    @Before
    public void setup() throws Exception {
        oidcSyncBean = spy(new OidcSynchronizationBean());
        oidcSyncBean.dm = dm = mock(DataService.class);
    }

    @Test
    public void testSynchronizeGroups() throws Exception {
        // given
        String tenantId = "";
        List<GroupInfo> accessGroupModels = new ArrayList<GroupInfo>();
        accessGroupModels.add(createAccessGroupModel());
        doReturn(accessGroupModels).when(oidcSyncBean)
                .getAllOrganizations(tenantId);
        doReturn(createOrganization()).when(oidcSyncBean)
                .synchronizeOIDCGroupsWithOrganizations(
                        createAccessGroupModel());

        List<Organization> expected = new ArrayList<Organization>();
        expected.add(createOrganization());

        // when
        List<Organization> result = oidcSyncBean.synchronizeGroups(tenantId);

        // then
        assertEquals(expected.get(0).getGroupId(), result.get(0).getGroupId());
        assertEquals(expected.get(0).getName(), result.get(0).getName());
    }

    public GroupInfo createAccessGroupModel() {
        return GroupInfo.of().id("1").description("test").name("test").build();
    }

    @Test
    public void testSycnchronizeOIDCGroupsWithOrganizationsNull()
            throws Exception {
        // given
        Organization organization = null;
        // organization.setName("test");
        doReturn(organization).when(oidcSyncBean).getOrganizationByGroupId("1");
        doReturn(organization).when(oidcSyncBean).getOrganizationByName("test");
        GroupInfo model = createAccessGroupModel();

        // when
        Organization result = oidcSyncBean
                .synchronizeOIDCGroupsWithOrganizations(model);

        assertEquals(null, result);

    }

    @Test
    public void testSycnchronizeOIDCGroupsWithOrganizations() throws Exception {
        // given
        Organization organization = createOrganization();
        doReturn(null).when(oidcSyncBean).getOrganizationByGroupId("1");
        doReturn(organization).when(oidcSyncBean).getOrganizationByName("test");
        GroupInfo model = createAccessGroupModel();

        // when
        Organization result = oidcSyncBean
                .synchronizeOIDCGroupsWithOrganizations(model);

        assertEquals(organization, result);
    }

    private Organization createOrganization() {
        Organization organization = new Organization();
        organization.setName("test");
        organization.setGroupId("test");
        return organization;
    }

    @Test
    public void testGetFirstMarktplaceIdFromOrganization() {
        // given
        Organization organization = createOrganization();
        List<Marketplace> mps = new ArrayList<Marketplace>();
        Marketplace expected = new Marketplace();
        expected.setKey(1);
        mps.add(expected);
        organization.setMarketplaces(mps);

        // when
        Marketplace result = oidcSyncBean
                .getFirstMarktplaceIdFromOrganization(organization);

        // then

        assertEquals(expected, result);
    }

    @Test
    public void testGetFirstMarktplaceIdFromOrganizationNull() {
        // given
        Organization organization = createOrganization();

        // when
        Marketplace result = oidcSyncBean
                .getFirstMarktplaceIdFromOrganization(organization);

        // then
        assertEquals(null, result);
    }

    @Test
    public void testGetAllTenantIdsForSynchronization() throws Exception {
        // given
        List<String> expected = new ArrayList<String>();
        expected.add("default");
        expected.add("test");

        List<Tenant> tenants = new ArrayList<Tenant>();
        Tenant tenant = new Tenant();
        tenant.setTenantId("test");
        tenants.add(tenant);
        doReturn(tenants).when(oidcSyncBean).getAllTenantsFromDb();

        // when
        List<String> result = oidcSyncBean.getAllTenantIdsForSynchronization();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void testGetUsersToSynchronizeFromOidcProviderWithExistingUser()
            throws Exception {
        // given
        String tenantId = "default";
        Organization organization = createOrganization();
        VOUserDetails userInGroup = new VOUserDetails();
        userInGroup.setUserId("test");

        // when
        UserImportModel result = oidcSyncBean
                .getUsersToSynchronizeFromOidcProvider(tenantId, organization,
                        userInGroup, true);

        // then
        assertEquals(null, result);
    }

    @Test
    public void testGetUsersToSynchronizeFromOidcProviderWithoutExistingUser()
            throws Exception {
        // given
        String tenantId = "default";
        Organization organization = createOrganization();
        VOUserDetails userInGroup = new VOUserDetails();
        userInGroup.setUserId("test");

        // when
        UserImportModel result = oidcSyncBean
                .getUsersToSynchronizeFromOidcProvider(tenantId, organization,
                        userInGroup, false);

        // then
        assertEquals(null, result.getMarketplace());
        assertEquals(userInGroup, result.getUser());
        assertEquals(organization, result.getOrganization());
    }

}