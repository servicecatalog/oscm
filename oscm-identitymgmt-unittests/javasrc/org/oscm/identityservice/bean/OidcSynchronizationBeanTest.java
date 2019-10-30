/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019
 *                                                                              
 *  Creation Date: 10.10.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.Tenant;
import org.oscm.identity.model.GroupInfo;
import org.oscm.identityservice.model.UserImportModel;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUserDetails;


/**
 * Unit test for OidcSynchronizationBean.
 * <p>
 * 
 * @author goebel
 */
public class OidcSynchronizationBeanTest {

    private OidcSynchronizationBean oidcSyncBean;
    private DataService dm;
    private Query queryMock;
    
    final private static String DEFAULT_TENANT ="default";

    @Before
    public void setup() throws Exception {
        oidcSyncBean = spy(new OidcSynchronizationBean());
        oidcSyncBean.dm = dm = mock(DataService.class);
        queryMock = mock(Query.class);
        doReturn(queryMock).when(dm).createNamedQuery(anyString());
    }

    @Test
    public void synchronizeGroups() throws Exception {
        // given
        String tenantId = "anyTenantId";

        mockGroupsForTenant(tenantId);

        // when
        List<Organization> result = oidcSyncBean.synchronizeGroups("anyTenantId");

        // then
        assertOrganizations(result);
    }
    
    @Test
    public void synchronizeGroups_GroupId_NotExisting()
            throws Exception {
        // given
        Organization org = givenOrgWithNotExistingGroupID();
        GroupInfo model = givenGroupInfo(org);

        // when
        List<Organization> result = oidcSyncBean
                .syncOrganizations(model, DEFAULT_TENANT);

        // then
        assertEquals(org, result.get(0));
        verify(oidcSyncBean, times(1)).findOrganizationByName(eq(org.getName()), eq(DEFAULT_TENANT));

    }

    @Test
    public void synchronizeGroups_GroupId_Existing()
            throws Exception {
        // given
        Organization org = givenOrgWithExistingGroupID();
        GroupInfo model = givenGroupInfo(org);

        // when
        List<Organization> result = oidcSyncBean
                .syncOrganizations(model, DEFAULT_TENANT);

        // then
        assertEquals(org, result.get(0));
        verify(oidcSyncBean, times(0)).findOrganizationByName(anyString(), anyString());
    }
    
    @Test
    public void synchronizeGroups_OrgWithRoles()
            throws Exception {
        // given
        Organization org = givenOrgWithBrokerRole();
        VOUserDetails user = givenUserNoRoles();
        GroupInfo model = givenGroupInfo(org);

        // when
        UserImportModel result = oidcSyncBean
                .getUserModel("anyTenantId", org, user);

        // then
        assertOrgAndMarketplace(org, result);
        assertBrokerManagerRole(result.getUser());
    }
   
    @Test
    public void synchronizeGroups_GroupId_Null()
            throws Exception {
        // given
        Organization org = givenOrgWithoutGroupID();
        GroupInfo model = givenGroupInfo(org);

        // when
        List<Organization> result = oidcSyncBean
                .syncOrganizations(model, DEFAULT_TENANT);

        // then
        assertEquals(org, result.get(0));
    }
    
    @Test
    public void synchronizeGroups_GroupId_NotFound() throws Exception {
        // given
        Organization org = givenOrgWithGroupNotFound();
        GroupInfo model = createGroupInfo();

        // when
        List<Organization> result = oidcSyncBean
                .syncOrganizations(model, DEFAULT_TENANT);
        // then
        verify(oidcSyncBean, times(1)).findOrganizationByName(eq(org.getName()), eq(DEFAULT_TENANT));
        assertEquals(org, result.get(0));
        
    }
    
    @Test
    public void synchronizeGroups_NameNotUnique() throws Exception {
        // given
        Organization organization = givenOrganization();
        doThrow(new NonUniqueResultException()).when(oidcSyncBean).findOrganizationByGroupId(anyString());
        GroupInfo model = createGroupInfo();

        // when
        List<Organization> result = oidcSyncBean
                .syncOrganizations(model, "default");
        // then
        assertTrue(result.isEmpty());
    }
   
    @Test
    public void getAllTenantIds() throws Exception {
        // given
       
        givenTenants("test");

        // when
        List<String> result = oidcSyncBean.getAllTenantIds();

        // then
        assertEquals(result, Arrays.asList(DEFAULT_TENANT, "test"));
    }

    @Test
    public void getUserModel() throws Exception {
        // given
        Organization org = givenOrganization();
        VOUserDetails user = givenUser();

        // when
        UserImportModel result = oidcSyncBean.getUserModel(DEFAULT_TENANT, org, user);

        // then
        assertEquals(org, result.getOrganization());
        assertEquals(DEFAULT_TENANT, result.getUser().getTenantId());
    }
    
    private Organization givenOrganization() {
        Organization organization = new Organization();
        organization.setName("test");
        organization.setGroupId("test-id");
        return organization;
    }
    
    private List<Organization> givenOrganizations() {
        return Arrays.asList(givenOrganization());
    }

    protected VOUserDetails givenUser() {
        VOUserDetails userInGroup = new VOUserDetails();
        userInGroup.setUserId("test");
        return userInGroup;
    }

    protected void assertOrganizations(List<Organization> result) {
        assertEquals(givenOrganization().getGroupId(), result.get(0).getGroupId());
        assertEquals(givenOrganization().getName(), result.get(0).getName());
    }

    protected void mockGroupsForTenant(String tenantId) {
        List<GroupInfo> accessGroupModels = new ArrayList<GroupInfo>();
        accessGroupModels.add(createGroupInfo());
        doReturn(accessGroupModels).when(oidcSyncBean)
                .getAllGroups(tenantId);
        doReturn(givenOrganizations()).when(oidcSyncBean)
                .syncOrganizations(
                        any(), anyString());
    }

    private GroupInfo createGroupInfo() {
        GroupInfo model = new GroupInfo();
        model.setId("1");
        model.setDescription("test");
        model.setName("test");
        return model;
    }
    
    private GroupInfo createGroupInfo(Organization o) {
        GroupInfo model = new GroupInfo();
        model.setId(o.getGroupId());
        model.setDescription("test");
        model.setName(o.getName());
        return model;
    }

    protected Organization givenOrgWithoutGroupID() {
        doThrow(new EntityNotFoundException()).when(oidcSyncBean).findOrganizationByGroupId(anyString());
        Organization organization = givenOrganization();
        organization.setGroupId(null);
        organization.setName("Group1");
        
        doReturn(Arrays.asList(organization)).when(queryMock)
                .getResultList();
        return organization;
    }
    
    protected Organization givenOrgWithExistingGroupID() {
        Organization organization = givenOrganization();
        organization.setGroupId("test");
        organization.setName("Group1");
        
        doReturn(Arrays.asList(organization)).when(queryMock)
                .getResultList();
        return organization;
    }
    
    protected Organization givenOrgWithNotExistingGroupID() {
        doThrow(new EntityNotFoundException()).when(oidcSyncBean).findOrganizationByGroupId(anyString());
        Organization organization = givenOrganization();
        organization.setGroupId(null);
        organization.setName("Group1");
        
        doReturn(Arrays.asList(organization)).when(queryMock)
                .getResultList();
        return organization;
    }
    
    private Organization givenOrgWithGroupNotFound() {
        Organization organization = givenOrganization();
        doThrow(new EntityNotFoundException()).when(oidcSyncBean).findOrganizationByGroupId(anyString());
        doReturn(organization).when(oidcSyncBean).findOrganizationByName(eq("test"), eq(DEFAULT_TENANT));
        return organization;
    }

    protected GroupInfo givenGroupInfo(Organization o) {
        GroupInfo model = createGroupInfo(o);
        return model;
    }
    
    private void givenTenants(String... tenantIds) throws Exception {
        List<Tenant> tenants = new ArrayList<Tenant>();
        for (String id : tenantIds) {
            Tenant tenant = new Tenant();
            tenant.setTenantId(id);
            tenants.add(tenant);
        }
        doReturn(tenants).when(oidcSyncBean).getAllTenantsFromDb();
    }

    private Organization givenOrgWithBrokerRole() {
        Organization org = givenOrgWithExistingGroupID();
        org.setMarketplaces(Arrays.asList(new Marketplace()));
        addRole(org, OrganizationRoleType.BROKER);
        return org;
    }

    private void addRole(Organization org, OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganizationRole(role);
        org.setGrantedRoles(Collections.singleton(otr));
    }
    
    private void assertBrokerManagerRole(VOUserDetails u) {
        assertTrue(u.getUserRoles().contains(UserRoleType.BROKER_MANAGER));
    }

    private void assertOrgAndMarketplace(Organization org, UserImportModel result) {
        assertEquals(org, result.getOrganization());
        assertNotNull(result.getMarketplace());
    }
    
    private VOUserDetails givenUserNoRoles() {
        return new VOUserDetails();
    }
}