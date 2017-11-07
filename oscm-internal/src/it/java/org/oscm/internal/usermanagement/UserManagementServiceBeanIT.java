/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.identityservice.local.LdapConnector;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;

public class UserManagementServiceBeanIT extends EJBTestBase {

    private UserManagementService ums;
    private LdapSettingsManagementServiceLocal ldapServ;
    private DataService ds;
    private Properties props = new Properties();
    private UserManagementServiceBean umsBean;
    private Set<POLdapSetting> settings = new HashSet<POLdapSetting>();
    private Organization returnOrg = new Organization();

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        ldapServ = mock(LdapSettingsManagementServiceLocal.class);

        doAnswer(new Answer<Properties>() {
            public Properties answer(InvocationOnMock invocation)
                    throws Throwable {
                return props;
            }
        }).when(ldapServ).getOrganizationSettings(anyString());
        doAnswer(new Answer<Properties>() {
            public Properties answer(InvocationOnMock invocation)
                    throws Throwable {
                return props;
            }
        }).when(ldapServ).getOrganizationSettingsResolved(anyString());
        doAnswer(new Answer<Properties>() {
            public Properties answer(InvocationOnMock invocation)
                    throws Throwable {
                return props;
            }
        }).when(ldapServ).getPlatformSettings();

        container.addBean(ldapServ);
        ds = mock(DataService.class);
        container.addBean(ds);
        umsBean = spy(new UserManagementServiceBean());
        container.addBean(umsBean);

        props = new Properties();
        ums = container.get(UserManagementService.class);
        doReturn("dsOrgId").when(umsBean).getOrganizationId();
        doAnswer(new Answer<Organization>() {
            public Organization answer(InvocationOnMock invocation)
                    throws Throwable {
                return returnOrg;
            }
        }).when(ds).getReferenceByBusinessKey(any(Organization.class));

        LdapConnector connectorMock = mock(LdapConnector.class);
        doReturn(connectorMock).when(umsBean).createLdapConnector(props);
        doNothing().when(connectorMock)
                .ensureAllMandatoryLdapPropertiesPresent();
        doReturn(Boolean.TRUE).when(connectorMock).canConnect();
    }

    @Test
    public void getMappedAttributes_OrganizationAdmin() throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.getMappedAttributes();
    }

    @Test
    public void getMappedAttributes_PlatformOperator() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.getMappedAttributes();
    }

    @Test
    public void getMappedAttributes_NonPrivilegedUser() throws Exception {
        ums.getMappedAttributes();
    }

    @Test
    public void getMappedAttributes_Delegation() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.getMappedAttributes();
        verify(ldapServ, times(1)).getMappedAttributes();
    }

    @Test(expected = EJBAccessException.class)
    public void getOrganizationSettings_StringParam_OrganizationAdmin()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        try {
            ums.getOrganizationSettings("orgId");
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void getOrganizationSettings_StringParam_PlatformOperator()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.getOrganizationSettings("orgId");
    }

    @Test
    public void getOrganizationSettings_StringParam_Delegation()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        String orgId = "xyz123";
        ums.getOrganizationSettings(orgId);
        verify(ldapServ, times(1)).getOrganizationSettings(eq(orgId));
    }

    @Test
    public void getOrganizationSettings_StringParam_CredentialsMasking()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        props.put("LDAP_CREDENTIALS", "password");
        String orgId = "xyz123";
        Properties result = ums.getOrganizationSettings(orgId);
        assertTrue(result.containsKey(SettingType.LDAP_CREDENTIALS.name()));
        assertEquals("********",
                result.getProperty(SettingType.LDAP_CREDENTIALS.name()));
    }

    @Test(expected = EJBAccessException.class)
    public void getOrganizationSettingsResolved_StringParam_OrganizationAdmin()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        try {
            ums.getOrganizationSettingsResolved("orgId");
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void getOrganizationSettingsResolved_StringParam_PlatformOperator()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.getOrganizationSettingsResolved("orgId");
    }

    @Test
    public void getOrganizationSettingsResolved_StringParam_Delegation()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        doReturn(settings).when(umsBean).mergeSettings(any(Properties.class),
                any(Properties.class));
        String orgId = "xyz123";
        assertSame(settings, ums.getOrganizationSettingsResolved(orgId));
        verify(ldapServ, times(1)).getOrganizationSettings(eq(orgId));
        verify(ldapServ, times(1)).getPlatformSettings();
        verify(umsBean, times(1)).mergeSettings(any(Properties.class),
                any(Properties.class));
    }

    @Test
    public void getOrganizationSettingsResolved_noParam_OrganizationAdmin()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.getOrganizationSettingsResolved();
    }

    @Test(expected = EJBAccessException.class)
    public void getOrganizationSettingsResolved_noParam_PlatformOperator()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        try {
            ums.getOrganizationSettingsResolved();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void getOrganizationSettingsResolved_noParam_Delegation()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        doReturn(settings).when(umsBean).mergeSettings(any(Properties.class),
                any(Properties.class));
        assertSame(settings, ums.getOrganizationSettingsResolved());
        ArgumentCaptor<String> orgId = ArgumentCaptor.forClass(String.class);
        verify(ldapServ, times(1)).getOrganizationSettings(orgId.capture());
        verify(ldapServ, times(1)).getPlatformSettings();
        verify(umsBean, times(1)).mergeSettings(any(Properties.class),
                any(Properties.class));
        assertEquals("dsOrgId", orgId.getValue());
    }

    @Test
    public void setOrganizationSettings_noOrgIdParam_OrganizationAdmin()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.setOrganizationSettings(props);
    }

    @Test(expected = EJBAccessException.class)
    public void setOrganizationSettings_noOrgIdParam_PlatformOperator()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        try {
            ums.setOrganizationSettings(props);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void setOrganizationSettings_noOrgIdParam_Delegation()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.setOrganizationSettings(props);
        ArgumentCaptor<String> orgId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Properties> capturedProps = ArgumentCaptor
                .forClass(Properties.class);
        verify(ldapServ, times(1)).setOrganizationSettings(orgId.capture(),
                capturedProps.capture());
        assertEquals("dsOrgId", orgId.getValue());
        assertSame(props, capturedProps.getValue());
    }

    @Test(expected = EJBAccessException.class)
    public void setOrganizationSettings_orgIdParam_OrganizationAdmin()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        try {
            ums.setOrganizationSettings("orgId", props);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void setOrganizationSettings_orgIdParam_PlatformOperator()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.setOrganizationSettings("orgId", props);
    }

    @Test
    public void setOrganizationSettings_orgIdParam_Delegation()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.setOrganizationSettings("orgId", props);
        ArgumentCaptor<String> orgId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Properties> capturedProps = ArgumentCaptor
                .forClass(Properties.class);
        verify(ldapServ, times(1)).setOrganizationSettings(orgId.capture(),
                capturedProps.capture());
        assertEquals("orgId", orgId.getValue());
        assertSame(props, capturedProps.getValue());
    }

    @Test
    public void canConnect_noOrgIdParam_OrganizationAdmin() throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.canConnect();
    }

    @Test(expected = EJBAccessException.class)
    public void canConnect_noOrgIdParam_PlatformOperator() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        try {
            ums.canConnect();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void canConnect_noOrgIdParam_Delegation() throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.canConnect();
        ArgumentCaptor<String> orgId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Properties> capturedProps = ArgumentCaptor
                .forClass(Properties.class);
        verify(ldapServ, times(1)).getOrganizationSettingsResolved(
                orgId.capture());
        verifyNoMoreInteractions(ldapServ);
        verify(umsBean, times(1)).canConnect(capturedProps.capture());
        assertEquals("dsOrgId", orgId.getValue());
        assertSame(props, capturedProps.getValue());
    }

    @Test(expected = EJBAccessException.class)
    public void canConnect_orgIdParam_OrganizationAdmin() throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        try {
            ums.canConnect("orgId");
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void canConnect_orgIdParam_PlatformOperator() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.canConnect("orgId");
    }

    @Test
    public void canConnect_orgIdParam_Delegation() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.canConnect("orgId");
        ArgumentCaptor<String> orgId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Properties> capturedProps = ArgumentCaptor
                .forClass(Properties.class);
        verify(ldapServ, times(1)).getOrganizationSettingsResolved(
                orgId.capture());
        verifyNoMoreInteractions(ldapServ);
        verify(umsBean, times(1)).canConnect(capturedProps.capture());
        assertEquals("orgId", orgId.getValue());
        assertSame(props, capturedProps.getValue());
    }

    @Test
    public void canConnect_nullOrgIdParam_Delegation() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.canConnect(null);
        ArgumentCaptor<Properties> capturedProps = ArgumentCaptor
                .forClass(Properties.class);
        verify(ldapServ, times(1)).getPlatformSettings();
        verifyNoMoreInteractions(ldapServ);
        verify(umsBean, times(1)).canConnect(capturedProps.capture());
        assertSame(props, capturedProps.getValue());
    }

    @Test
    public void canConnect_emptyStringOrgIdParam_Delegation() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.canConnect("");
        ArgumentCaptor<Properties> capturedProps = ArgumentCaptor
                .forClass(Properties.class);
        verify(ldapServ, times(1)).getPlatformSettings();
        verifyNoMoreInteractions(ldapServ);
        verify(umsBean, times(1)).canConnect(capturedProps.capture());
        assertSame(props, capturedProps.getValue());
    }

    @Test(expected = EJBAccessException.class)
    public void resetOrganizationSettings_orgIdParam_OrganizationAdmin()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        try {
            ums.resetOrganizationSettings("orgId");
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void resetOrganizationSettings_orgIdParam_PlatformOperator()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.resetOrganizationSettings("orgId");
    }

    @Test
    public void resetOrganizationSettings_orgIdParam_Delegation()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.resetOrganizationSettings("orgId");
        ArgumentCaptor<String> orgId = ArgumentCaptor.forClass(String.class);
        verify(ldapServ, times(1)).resetOrganizationSettings(orgId.capture());
        assertEquals("orgId", orgId.getValue());
    }

    @Test
    public void resetOrganizationSettings_noOrgIdParam_OrganizationAdmin()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.resetOrganizationSettings();
    }

    @Test(expected = EJBAccessException.class)
    public void resetOrganizationSettings_noOrgIdParam_PlatformOperator()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        try {
            ums.resetOrganizationSettings();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void resetOrganizationSettings_noOrgIdParam_Delegation()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.resetOrganizationSettings();
        ArgumentCaptor<String> orgId = ArgumentCaptor.forClass(String.class);
        verify(ldapServ, times(1)).resetOrganizationSettings(orgId.capture());
        assertEquals("dsOrgId", orgId.getValue());
    }

    @Test
    public void getPlatformSettings_noParam_PlatformOperator() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.getPlatformSettings();
    }

    @Test
    public void getPlatformSettings_noParam_ServiceManager() throws Exception {
        container.login(1, ROLE_SERVICE_MANAGER);
        ums.getPlatformSettings();
    }

    @Test
    public void getPlatformSettings_noParam_ResellerManager() throws Exception {
        container.login(1, ROLE_RESELLER_MANAGER);
        ums.getPlatformSettings();
    }

    @Test
    public void getPlatformSettings_noParam_BrokerManager() throws Exception {
        container.login(1, ROLE_BROKER_MANAGER);
        ums.getPlatformSettings();
    }

    @Test
    public void getPlatformSettings_noParam_OrganizationAdmin()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.getPlatformSettings();
    }

    @Test
    public void getPlatformSettings_noParam_Delegation() throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.getPlatformSettings();
        verify(ldapServ, times(1)).getPlatformSettings();
    }

    @Test
    public void getPlatformSettings_noParam_Transformation() throws Exception {
        props.setProperty(SettingType.LDAP_ATTR_EMAIL.name(), "bla");
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.getPlatformSettings();
        ArgumentCaptor<Properties> capturedProps = ArgumentCaptor
                .forClass(Properties.class);
        verify(umsBean, times(1)).mergeSettings(capturedProps.capture(),
                same(props));
        assertEquals(1, capturedProps.getValue().keySet().size());
        assertEquals(
                "",
                capturedProps.getValue().getProperty(
                        SettingType.LDAP_ATTR_EMAIL.name()));
    }

    @Test
    public void getPlatformSettings_Result() throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        HashSet<POLdapSetting> mergeResult = new HashSet<POLdapSetting>();
        doReturn(mergeResult).when(umsBean).mergeSettings(
                any(Properties.class), any(Properties.class));
        Set<POLdapSetting> result = ums.getPlatformSettings();
        assertSame(mergeResult, result);
    }

    @Test(expected = EJBAccessException.class)
    public void setPlatformSettings_OrganizationAdmin() throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        try {
            ums.setPlatformSettings(props);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void setPlatformSettings_PlatformOperator() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.setPlatformSettings(props);
    }

    @Test
    public void setPlatformSettings_Delegation() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.setPlatformSettings(props);
        ArgumentCaptor<Properties> capturedProps = ArgumentCaptor
                .forClass(Properties.class);
        verify(ldapServ, times(1)).setPlatformSettings(capturedProps.capture());
        assertSame(props, capturedProps.getValue());
    }

    @Test
    public void mergeSettings_Empty() throws Exception {
        Set<POLdapSetting> result = umsBean.mergeSettings(props, props);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void mergeSettings_OnlyPlatformProps() throws Exception {
        Properties platformSettings = new Properties();
        platformSettings.put("setting2", "value2");
        Set<POLdapSetting> result = umsBean.mergeSettings(props,
                new Properties());
        assertTrue(result.isEmpty());
    }

    @Test
    public void mergeSettings_OnlyOrgProps() throws Exception {
        props.put("setting1", "value1");
        Set<POLdapSetting> result = umsBean.mergeSettings(props,
                new Properties());
        assertEquals(1, result.size());
        POLdapSetting entry = result.iterator().next();
        assertEquals("setting1", entry.getSettingKey());
        assertEquals("value1", entry.getSettingValue());
        assertFalse(entry.isPlatformDefault());
    }

    @Test
    public void mergeSettings_OrgAndPlatformPropsDifferentKey()
            throws Exception {
        props.put("setting1", "value1");
        Properties platformSettings = new Properties();
        platformSettings.put("setting2", "value2");
        Set<POLdapSetting> result = umsBean.mergeSettings(props,
                platformSettings);
        assertEquals(1, result.size());
        POLdapSetting entry = result.iterator().next();
        assertEquals("setting1", entry.getSettingKey());
        assertEquals("value1", entry.getSettingValue());
        assertFalse(entry.isPlatformDefault());
    }

    @Test
    public void mergeSettings_OrgCredentials_Masked() throws Exception {
        props.put(SettingType.LDAP_CREDENTIALS.name(), "password");
        Properties platformSettings = new Properties();
        platformSettings.put("setting2", "value2");
        Set<POLdapSetting> result = umsBean.mergeSettings(props,
                new Properties());
        assertEquals(1, result.size());
        POLdapSetting entry = result.iterator().next();
        assertEquals("LDAP_CREDENTIALS", entry.getSettingKey());
        assertEquals("********", entry.getSettingValue());
    }

    @Test
    public void mergeSettings_OrgAndPlatformPropsEqualKey() throws Exception {
        props.put("setting1", "value1");
        Properties platformSettings = new Properties();
        platformSettings.put("setting1", "value2");
        Set<POLdapSetting> result = umsBean.mergeSettings(props,
                platformSettings);
        assertEquals(1, result.size());
        POLdapSetting entry = result.iterator().next();
        assertEquals("setting1", entry.getSettingKey());
        assertEquals("value1", entry.getSettingValue());
        assertFalse(entry.isPlatformDefault());
    }

    @Test
    public void mergeSettings_OrgSettingLinked() throws Exception {
        props.put("setting1", "");
        Properties platformSettings = new Properties();
        platformSettings.put("setting1", "value2");
        Set<POLdapSetting> result = umsBean.mergeSettings(props,
                platformSettings);
        assertEquals(1, result.size());
        POLdapSetting entry = result.iterator().next();
        assertEquals("setting1", entry.getSettingKey());
        assertEquals("value2", entry.getSettingValue());
        assertTrue(entry.isPlatformDefault());
    }

    @Test
    public void isPlatformOperator_False() {
        PlatformUser user = new PlatformUser();
        when(ds.getCurrentUser()).thenReturn(user);
        assertFalse(umsBean.isPlatformOperator());
    }

    @Test
    public void isPlatformOperator_True() {
        PlatformUser user = spy(new PlatformUser());
        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setRole(new UserRole(UserRoleType.PLATFORM_OPERATOR));
        user.getAssignedRoles().add(roleAssignment);
        when(ds.getCurrentUser()).thenReturn(user);
        assertTrue(umsBean.isPlatformOperator());
    }

    @Test(expected = EJBAccessException.class)
    public void getLdapManagedOrganizations_NonOperator() throws Exception {
        try {
            ums.getLdapManagedOrganizations();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void getLdapManagedOrganizations_verifyDelegation() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.getLdapManagedOrganizations();
        verify(ldapServ, times(1)).getLdapManagedOrganizations();
    }

    @Test
    public void getLdapManagedOrganizations_verifyValueReturn()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        Set<Organization> input = new HashSet<Organization>();
        Organization inputOrg = new Organization();
        inputOrg.setKey(1);
        inputOrg.setName("orgName");
        inputOrg.setOrganizationId("orgId");
        input.add(inputOrg);
        when(ldapServ.getLdapManagedOrganizations()).thenReturn(input);
        Set<POLdapOrganization> result = ums.getLdapManagedOrganizations();
        assertEquals(1, result.size());
        POLdapOrganization entry = result.iterator().next();
        assertEquals(inputOrg.getKey(), entry.getKey());
        assertEquals(inputOrg.getVersion(), entry.getVersion());
        assertEquals(inputOrg.getName(), entry.getName());
        assertEquals(inputOrg.getOrganizationId(), entry.getIdentifier());
    }

    @Test(expected = EJBAccessException.class)
    public void isOrganizationLDAPManaged_orgId_NonOperator() throws Exception {
        try {
            ums.isOrganizationLDAPManaged("orgId");
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void isOrganizationLDAPManaged_orgId_Operator() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.isOrganizationLDAPManaged("orgId");
    }

    @Test(expected = ObjectNotFoundException.class)
    public void isOrganizationLDAPManaged_orgId_NonExistingOrgId()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        doThrow(new ObjectNotFoundException()).when(ds)
                .getReferenceByBusinessKey(any(Organization.class));
        ums.isOrganizationLDAPManaged("nonExistingOrgId");
    }

    @Test
    public void isOrganizationLDAPManaged_orgId_VerifyDelegation()
            throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ArgumentCaptor<Organization> orgParam = ArgumentCaptor
                .forClass(Organization.class);
        boolean result = ums.isOrganizationLDAPManaged("passedOrgId");
        verify(ds, times(1)).getReferenceByBusinessKey(orgParam.capture());
        assertEquals("passedOrgId", orgParam.getValue().getOrganizationId());
        assertFalse(result);
    }

    @Test
    public void isOrganizationLDAPManaged_orgId_Positive() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        returnOrg.setRemoteLdapActive(true);
        assertTrue(ums.isOrganizationLDAPManaged("passedOrgId"));
    }

    @Test(expected = EJBAccessException.class)
    public void isOrganizationLDAPManaged_noOrgId_NonAdmin() throws Exception {
        try {
            ums.isOrganizationLDAPManaged();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void isOrganizationLDAPManaged_noOrgId_OrgAdmin() throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ums.isOrganizationLDAPManaged();
    }

    @Test
    public void isOrganizationLDAPManaged_noOrgId_VerifyDelegation()
            throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        ArgumentCaptor<Organization> orgParam = ArgumentCaptor
                .forClass(Organization.class);
        boolean result = ums.isOrganizationLDAPManaged();
        verify(ds, times(1)).getReferenceByBusinessKey(orgParam.capture());
        assertEquals("dsOrgId", orgParam.getValue().getOrganizationId());
        assertFalse(result);
    }

    @Test
    public void isOrganizationLDAPManaged_noOrgId_Positive() throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        returnOrg.setRemoteLdapActive(true);
        assertTrue(ums.isOrganizationLDAPManaged());
    }

    @Test
    public void clearPlatformSettings_platformOperator() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.clearPlatformSettings();
    }

    @Test(expected = EJBAccessException.class)
    public void clearPlatformSettings_OrganizationAdmin() throws Exception {
        container.login(1, ROLE_ORGANIZATION_ADMIN);
        try {
            ums.clearPlatformSettings();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void clearPlatformSettings_Delegation() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        ums.clearPlatformSettings();
        verify(ldapServ, times(1)).clearPlatformSettings();
    }

}
