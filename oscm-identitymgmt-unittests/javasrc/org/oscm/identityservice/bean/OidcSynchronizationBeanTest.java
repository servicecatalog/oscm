package org.oscm.identityservice.bean;

import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;


import org.junit.Before;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Tenant;
import org.oscm.identityservice.model.AccessGroupModel;
import org.oscm.identityservice.model.UserImportModel;
import org.oscm.internal.vo.VOUserDetails;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

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
        String token = "";
        List<AccessGroupModel> accessGroupModels = new ArrayList<AccessGroupModel>();
        accessGroupModels.add(createAccessGroupModel());
        doReturn(accessGroupModels).when(oidcSyncBean)
        .getAllOrganizations(tenantId, token);
        doReturn(createOrganization()).when(oidcSyncBean)
        .synchronizeOIDCGroupsWithOrganizations(createAccessGroupModel());

        List<Organization> expected = new ArrayList<Organization>();
        expected.add(createOrganization());

        // when
        List<Organization> result = oidcSyncBean.synchronizeGroups(tenantId,
                token);

        // then
        assertEquals(expected.get(0).getGroupId(), result.get(0).getGroupId());
        assertEquals(expected.get(0).getName(), result.get(0).getName());
    }

    public AccessGroupModel createAccessGroupModel() {
        AccessGroupModel model = new AccessGroupModel();
        model.setId("1");
        model.setDescription("test");
        model.setName("test");
        return model;
    }

    @Test
    public void testSycnchronizeOIDCGroupsWithOrganizationsNull()
            throws Exception {
        // given
        Organization organization = null;
        // organization.setName("test");
        doReturn(organization).when(oidcSyncBean).getOrganizationByGroupId("1");
        doReturn(organization).when(oidcSyncBean).getOrganizationByName("test");
        AccessGroupModel model = createAccessGroupModel();

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
        AccessGroupModel model = createAccessGroupModel();

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
    public void testGetUsersToSynchronizeFromOidcProviderWithExistingUser() throws Exception {
        // given
        String tenantId = "default";
        String token = "";
        Organization organization = createOrganization();
        VOUserDetails userInGroup = new VOUserDetails();
        userInGroup.setUserId("test");
        doReturn(userInGroup).when(oidcSyncBean)
                .getUserinfoFromIdentityService(tenantId, token, userInGroup);

        // when

        UserImportModel result = oidcSyncBean
                .getUsersToSynchronizeFromOidcProvider(tenantId, token,
                        organization, userInGroup, true);

        // then
        assertEquals(null, result);
    }
    
    @Test
    public void testGetUsersToSynchronizeFromOidcProviderWithoutExistingUser() throws Exception {
        // given
        String tenantId = "default";
        String token = "";
        Organization organization = createOrganization();
        VOUserDetails userInGroup = new VOUserDetails();
        userInGroup.setUserId("test");
        doReturn(userInGroup).when(oidcSyncBean)
                .getUserinfoFromIdentityService(tenantId, token, userInGroup);

        // when

        UserImportModel result = oidcSyncBean
                .getUsersToSynchronizeFromOidcProvider(tenantId, token,
                        organization, userInGroup, false);

        // then
        assertEquals(null, result.getMarketplace());
        assertEquals(userInGroup, result.getUser());
        assertEquals(organization, result.getOrganization());
    }

}