/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.common.ldapsettings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.Part;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.usermanagement.POLdapOrganization;
import org.oscm.internal.usermanagement.POLdapSetting;
import org.oscm.internal.usermanagement.UserManagementService;

public class LdapConfigurationCtrlTest {

    private LdapConfigurationCtrl lcc;
    private UserManagementService ums;
    private Set<POLdapSetting> returnSettings = new HashSet<POLdapSetting>();

    @Captor
    ArgumentCaptor<Properties> propertiesToStore;
    private LdapConfigurationModel model;
    private byte[] fileContent;
    private Part uploadedFile;
    @Captor
    ArgumentCaptor<byte[]> outputFileContent;
    private String newOrgId;
    private ValueChangeEvent vce;
    @Captor
    ArgumentCaptor<String> successMessageKey;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        lcc = spy(new LdapConfigurationCtrl());
        model = spy(new LdapConfigurationModel());
        lcc.setModel(model);
        ums = mock(UserManagementService.class);
        lcc.ui = mock(UiDelegate.class);

        doReturn(ums).when(lcc).getUserManagementService();
        doReturn(returnSettings).when(ums).getOrganizationSettingsResolved();
        doReturn(returnSettings).when(ums).getOrganizationSettingsResolved(
                anyString());
        doReturn(returnSettings).when(ums).getPlatformSettings();
        uploadedFile = mock(Part.class);
        model.setFile(uploadedFile);
        doAnswer(new Answer<InputStream>() {
            public InputStream answer(InvocationOnMock invocation)
                    throws Throwable {
                return new ByteArrayInputStream(fileContent);
            }
        }).when(uploadedFile).getInputStream();
        fileContent = String.format("").getBytes("UTF-8");
        doNothing().when(lcc).writeSettings(outputFileContent.capture());

        vce = mock(ValueChangeEvent.class);
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return newOrgId;
            }
        }).when(vce).getNewValue();
        doNothing().when(lcc).addSuccessMessage(successMessageKey.capture());
        doNothing().when(lcc).updateUserInSession();
    }

    @Test
    public void initResolvedSettings_noSettings() throws Exception {
        assertFalse(model.isShowIsPlatformSettingColumnVisible());
        lcc.initResolvedSettings();
        assertTrue(lcc.getModel().getSettings().isEmpty());
        assertTrue(model.isShowIsPlatformSettingColumnVisible());
    }

    @Test
    public void initResolvedSettings_withOldSettings() throws Exception {
        model.getSettings().add(new LdapSetting("key9", "value9", false));
        assertFalse(model.isShowIsPlatformSettingColumnVisible());
        lcc.initResolvedSettings();
        assertTrue(lcc.getModel().getSettings().isEmpty());
        assertTrue(model.isShowIsPlatformSettingColumnVisible());
    }

    @SuppressWarnings("boxing")
    @Test
    public void initModelData_platformOperator() throws Exception {
        when(ums.isPlatformOperator()).thenReturn(Boolean.TRUE);
        lcc.initModelData();
        verify(ums, times(1)).isPlatformOperator();
        assertTrue(lcc.getModel().isOrganizationSelectionSupported());
        verify(lcc, times(0)).initResolvedSettings();
        verify(lcc, times(1)).initPlatformSettings();
        verify(lcc, times(1)).initOrgData();
    }

    @Test
    public void initModelData_organizationAdmin() throws Exception {
        lcc.initModelData();
        assertFalse(lcc.getModel().isOrganizationSelectionSupported());
        assertNull(lcc.getModel().getOrganizationIdentifier());
        verify(lcc, times(1)).initResolvedSettings();
        verify(lcc, times(0)).initPlatformSettings();
        verify(lcc, times(0)).initOrgData();
    }

    @Test
    public void initSettingData_VerifyReset() throws Exception {
        model.getSettings().add(new LdapSetting("key", "value", true));
        lcc.initSettingData();
        assertTrue(model.getSettings().isEmpty());
    }

    @Test
    public void initSettingData_PlatformSettingsInit() throws Exception {
        model.setOrganizationIdentifier(null);
        model.setOrganizationSelectionSupported(true);
        lcc.initSettingData();
        verify(lcc, times(0)).initResolvedSettings();
        verify(lcc, times(1)).initPlatformSettings();
    }

    @Test
    public void initSettingData_OrgSettingsInit() throws Exception {
        lcc.initSettingData();
        verify(lcc, times(1)).initResolvedSettings();
        verify(lcc, times(0)).initPlatformSettings();
    }

    @Test
    public void initResolvedSettings_oneSetting() throws Exception {
        returnSettings.add(new POLdapSetting(SettingType.LDAP_BASE_DN.name(),
                "o=fujitsu", false));
        lcc.initResolvedSettings();
        verify(ums, times(1)).getOrganizationSettingsResolved();
        List<LdapSetting> settings = lcc.getModel().getSettings();
        assertEquals(1, settings.size());
        assertEquals("LDAP_BASE_DN", settings.get(0).getSettingKey());
        assertEquals("o=fujitsu", settings.get(0).getSettingValue());
        assertFalse(settings.get(0).isPlatformDefault());
    }

    @Test
    public void initResolvedSettings_oneSetting_OrgIdSet() throws Exception {
        model.setOrganizationIdentifier("new org id");
        returnSettings.add(new POLdapSetting(SettingType.LDAP_BASE_DN.name(),
                "o=fujitsu", false));
        lcc.initResolvedSettings();
        verify(ums, times(1)).getOrganizationSettingsResolved(eq("new org id"));
        List<LdapSetting> settings = lcc.getModel().getSettings();
        assertEquals(1, settings.size());
        assertEquals("LDAP_BASE_DN", settings.get(0).getSettingKey());
        assertEquals("o=fujitsu", settings.get(0).getSettingValue());
        assertFalse(settings.get(0).isPlatformDefault());
    }

    @Test
    public void initResolvedSettings_severalSettingsWithDefaults()
            throws Exception {
        returnSettings.add(new POLdapSetting(SettingType.LDAP_BASE_DN.name(),
                "o=fujitsu", false));
        returnSettings.add(new POLdapSetting(SettingType.LDAP_ATTR_UID.name(),
                "uid", true));
        returnSettings.add(new POLdapSetting(SettingType.LDAP_CONTEXT_FACTORY
                .name(), "just.another.Factory", true));
        returnSettings.add(new POLdapSetting(SettingType.LDAP_URL.name(),
                "ldap://host:389", false));
        lcc.initResolvedSettings();
        List<LdapSetting> settings = lcc.getModel().getSettings();
        assertEquals(4, settings.size());
        Map<String, LdapSetting> settingsMap = new HashMap<String, LdapSetting>();
        for (LdapSetting setting : settings) {
            settingsMap.put(setting.getSettingKey(), setting);
        }
        LdapSetting currentSetting = settingsMap.get("LDAP_BASE_DN");
        assertNotNull(currentSetting);
        assertEquals("o=fujitsu", currentSetting.getSettingValue());
        assertFalse(currentSetting.isPlatformDefault());
        currentSetting = settingsMap.get("LDAP_ATTR_UID");
        assertNotNull(currentSetting);
        assertEquals("uid", currentSetting.getSettingValue());
        assertTrue(currentSetting.isPlatformDefault());
        currentSetting = settingsMap.get("LDAP_CONTEXT_FACTORY");
        assertNotNull(currentSetting);
        assertEquals("just.another.Factory", currentSetting.getSettingValue());
        assertTrue(currentSetting.isPlatformDefault());
        currentSetting = settingsMap.get("LDAP_URL");
        assertNotNull(currentSetting);
        assertEquals("ldap://host:389", currentSetting.getSettingValue());
        assertFalse(currentSetting.isPlatformDefault());
    }

    @Test
    public void initPlatformSettings_oneSetting() throws Exception {
        returnSettings.add(new POLdapSetting(SettingType.LDAP_BASE_DN.name(),
                "o=fujitsu", false));
        lcc.initPlatformSettings();
        verify(ums, times(1)).getPlatformSettings();
        verifyNoMoreInteractions(ums);
        List<LdapSetting> settings = lcc.getModel().getSettings();
        assertEquals(1, settings.size());
        assertEquals("LDAP_BASE_DN", settings.get(0).getSettingKey());
        assertEquals("o=fujitsu", settings.get(0).getSettingValue());
        assertFalse(settings.get(0).isPlatformDefault());
    }

    @Test
    public void initOrgData_existingSettings() throws Exception {
        model.getOrganizations().add(new SelectItem("id", "label"));
        lcc.initOrgData();
        assertTrue(model.getOrganizations().isEmpty());
    }

    @Test
    public void initOrgData_VerifySettings() throws Exception {
        HashSet<POLdapOrganization> orgs = new HashSet<POLdapOrganization>();
        orgs.add(new POLdapOrganization(4, 1, "orgName1", "orgId1"));
        orgs.add(new POLdapOrganization(5, 2, null, "orgId2"));
        when(ums.getLdapManagedOrganizations()).thenReturn(orgs);
        lcc.initOrgData();
        verify(ums, times(1)).getLdapManagedOrganizations();
        List<SelectItem> modelOrgs = model.getOrganizations();
        assertEquals(2, modelOrgs.size());
        Map<String, SelectItem> idOrgMap = new HashMap<String, SelectItem>();
        for (SelectItem currentOrg : modelOrgs) {
            idOrgMap.put((String) currentOrg.getValue(), currentOrg);
        }
        SelectItem entry = idOrgMap.get("orgId1");
        assertEquals("orgId1", entry.getValue());
        assertEquals("orgName1 (orgId1)", entry.getLabel());
        entry = idOrgMap.get("orgId2");
        assertEquals("orgId2", entry.getValue());
        assertEquals("orgId2", entry.getLabel());
    }

    @Test
    public void testConnection_VerifyDelegationOrgAdmin() throws Exception {
        doReturn(Boolean.TRUE).when(ums).canConnect();
        lcc.testConnection();
        verify(ums, times(1)).canConnect();
    }

    @Test
    public void testConnection_VerifyDelegationOperator() throws Exception {
        model.setOrganizationIdentifier("testConnOrgId");
        doReturn(Boolean.TRUE).when(ums).canConnect(anyString());
        lcc.testConnection();
        verify(ums, times(1)).canConnect(eq("testConnOrgId"));
        assertEquals("info.organization.ldapsettings.tested",
                successMessageKey.getValue());
    }

    @Test
    public void testConnection_VerifyDelegationPlatformSettings()
            throws Exception {
        model.setOrganizationIdentifier(null);
        model.setOrganizationSelectionSupported(true);
        doReturn(Boolean.TRUE).when(ums).canConnect(anyString());
        lcc.testConnection();
        verify(ums, times(1)).canConnect(null);
        assertEquals("info.organization.ldapsettings.tested",
                successMessageKey.getValue());
    }

    @Test
    public void testConnection_VerifySuccess() throws Exception {
        doReturn(Boolean.TRUE).when(ums).canConnect();
        String result = lcc.testConnection();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    @Test
    public void testConnection_VerifyFailure() throws Exception {
        doReturn(Boolean.FALSE).when(ums).canConnect();
        ArgumentCaptor<String> clientId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Severity> severity = ArgumentCaptor
                .forClass(Severity.class);
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        doNothing().when(lcc).addMessage(anyString(), any(Severity.class),
                anyString());
        String result = lcc.testConnection();
        verify(lcc, times(1)).addMessage(clientId.capture(),
                severity.capture(), key.capture());
        assertEquals("", result);
        assertNull(clientId.getValue());
        assertEquals(FacesMessage.SEVERITY_ERROR, severity.getValue());
        assertEquals("error.ldap.connectionfailure", key.getValue());
    }

    @Test
    public void reset_VerifyDelegationOrgAdmin() throws Exception {
        lcc.reset();
        verify(ums, times(1)).resetOrganizationSettings();
    }

    @Test
    public void reset_VerifyDelegation_verifyUpdateUserInSession()
            throws Exception {
        lcc.reset();
        verify(lcc, times(1)).updateUserInSession();
    }

    @Test
    public void reset_VerifyDelegationOperator() throws Exception {
        model.setOrganizationIdentifier("resetOrgId");
        lcc.reset();
        verify(ums, times(1)).resetOrganizationSettings(eq("resetOrgId"));
        assertEquals("info.organization.ldapsettings.reset",
                successMessageKey.getValue());
    }

    @Test
    public void reset_ModelReload() throws Exception {
        lcc.getModel().getSettings()
                .add(new LdapSetting("key", "value", false));
        lcc.reset();
        verify(lcc, times(1)).initResolvedSettings();
        assertTrue(lcc.getModel().getSettings().isEmpty());
    }

    @Test
    public void clear_VerifyDelegationOperator() throws Exception {
        lcc.clear();
        verify(ums, times(1)).clearPlatformSettings();
    }

    @Test
    public void clear_ModelReload() throws Exception {
        lcc.getModel().getSettings()
                .add(new LdapSetting("key", "value", false));
        model.setOrganizationIdentifier(null);
        model.setOrganizationSelectionSupported(true);
        lcc.clear();
        verify(lcc, times(1)).initPlatformSettings();
        assertTrue(lcc.getModel().getSettings().isEmpty());
    }

    @Test
    public void importSettings_PropertyEvaluation_NoSettingsOrgAdmin()
            throws Exception {
        lcc.importSettings();
        verify(ums, times(1)).setOrganizationSettings(
                propertiesToStore.capture());
        verify(uploadedFile, times(1)).getInputStream();
        Properties value = propertiesToStore.getValue();
        assertNotNull(value);
        assertTrue(value.keySet().isEmpty());
    }

    @Test
    public void importSettings_PropertyEvaluation_NoSettingsOperator()
            throws Exception {
        model.setOrganizationIdentifier("importOrgId");
        lcc.importSettings();
        verify(ums, times(1)).setOrganizationSettings(eq("importOrgId"),
                propertiesToStore.capture());
        verify(uploadedFile, times(1)).getInputStream();
        Properties value = propertiesToStore.getValue();
        assertNotNull(value);
        assertTrue(value.keySet().isEmpty());
    }

    @Test
    public void importSettings_PropertyEvaluation_EmptyFile() throws Exception {
        fileContent = String.format("").getBytes("UTF-8");
        lcc.importSettings();
        verify(ums, times(1)).setOrganizationSettings(
                propertiesToStore.capture());
        Properties value = propertiesToStore.getValue();
        assertTrue(value.keySet().isEmpty());
    }

    @Test
    public void importSettings_PropertyEvaluation_verifyUpdateUserInSession()
            throws Exception {
        fileContent = String.format("").getBytes("UTF-8");
        lcc.importSettings();
        verify(lcc, times(1)).updateUserInSession();
    }

    @Test
    public void importSettings_PropertyEvaluation_ContainsSettings()
            throws Exception {
        fileContent = String.format("key1=value1%nkey2=value2").getBytes(
                "UTF-8");
        lcc.importSettings();
        verify(ums, times(1)).setOrganizationSettings(
                propertiesToStore.capture());
        Properties value = propertiesToStore.getValue();
        assertEquals("value1", value.getProperty("key1"));
        assertEquals("value2", value.getProperty("key2"));
        assertEquals("info.organization.ldapsettings.imported",
                successMessageKey.getValue());
    }

    @Test
    public void importSettings_PropertyEvaluation_ForPlatformSettings()
            throws Exception {
        model.setOrganizationSelectionSupported(true);
        model.setOrganizationIdentifier(null);
        fileContent = String.format("key1=value1%nkey2=value2").getBytes(
                "UTF-8");
        lcc.importSettings();
        verify(ums, times(1)).setPlatformSettings(propertiesToStore.capture());
        Properties value = propertiesToStore.getValue();
        assertEquals("value1", value.getProperty("key1"));
        assertEquals("value2", value.getProperty("key2"));
        assertEquals("info.organization.ldapsettings.imported",
                successMessageKey.getValue());
    }

    @Test
    public void importSettings_PropertyEvaluation_NonPropFile()
            throws Exception {
        fileContent = String.format("PK").getBytes("UTF-8");
        lcc.importSettings();
        verify(ums, times(1)).setOrganizationSettings(
                propertiesToStore.capture());
        Properties value = propertiesToStore.getValue();
        assertEquals(1, value.keySet().size());
    }

    @Test
    public void importSettings_ModelReinit() throws Exception {
        lcc.importSettings();
        verify(lcc, times(1)).initResolvedSettings();
        assertNull(model.getFile());
    }

    @Test
    public void importSettings_NoFile() throws Exception {
        model.setFile(null);

        String outcome = lcc.importSettings();

        assertEquals(BaseBean.OUTCOME_ERROR, outcome);
        verify(lcc.ui).handleError(eq((String) null),
                eq(LdapConfigurationCtrl.ERROR_NO_IMPORT_FILE));
        verifyZeroInteractions(ums);
    }

    private Properties getBytesAsProps() throws IOException {
        Properties props = new Properties();
        props.load(new ByteArrayInputStream(outputFileContent.getValue()));
        return props;
    }

    @Test
    public void exportSettings_noSettingsInModel() throws Exception {
        String result = lcc.exportSettings();
        verifyNoMoreInteractions(ums);
        verify(lcc, times(1)).writeSettings(any(byte[].class));
        Properties props = getBytesAsProps();
        assertTrue(props.keySet().isEmpty());
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    @Test
    public void exportSettings_oneOrgSettingInModel_settingForOwnOrg()
            throws Exception {
        model.setOrganizationIdentifier("non empty id");
        model.getSettings().add(new LdapSetting("key1", "value1", false));
        String result = lcc.exportSettings();
        verifyNoMoreInteractions(ums);
        verify(lcc, times(1)).writeSettings(any(byte[].class));
        assertEquals("value1", getBytesAsProps().getProperty("key1"));
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        verify(lcc, times(0)).addSuccessMessage(anyString());
    }

    @Test
    public void exportSettings_oneOrgSettingInModel_defaultSettingForOtherOrg()
            throws Exception {
        model.setOrganizationIdentifier("non empty id");
        model.getSettings().add(new LdapSetting("key1", "value1", true));
        String result = lcc.exportSettings();
        verifyNoMoreInteractions(ums);
        verify(lcc, times(1)).writeSettings(any(byte[].class));
        assertEquals("", getBytesAsProps().getProperty("key1"));
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        verify(lcc, times(0)).addSuccessMessage(anyString());
    }

    @Test
    public void exportSettings_oneOrgSettingInModel_defaultSettingAsOperator()
            throws Exception {
        model.setOrganizationIdentifier(null);
        model.getSettings().add(new LdapSetting("key1", "value1", true));
        String result = lcc.exportSettings();
        verifyNoMoreInteractions(ums);
        verify(lcc, times(1)).writeSettings(any(byte[].class));
        assertEquals("value1", getBytesAsProps().getProperty("key1"));
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        verify(lcc, times(0)).addSuccessMessage(anyString());
    }

    @Test
    public void exportSettings_twoSettingsInModel_forOtherOrg()
            throws Exception {
        model.setOrganizationIdentifier("non empty id");
        model.getSettings().add(new LdapSetting("key1", "value1", false));
        model.getSettings().add(new LdapSetting("key2", "value2", true));
        String result = lcc.exportSettings();
        verifyNoMoreInteractions(ums);
        verify(lcc, times(1)).writeSettings(any(byte[].class));
        Properties props = getBytesAsProps();
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("", props.getProperty("key2"));
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    @Test
    public void exportSettings_twoSettingsInModel_asOperator() throws Exception {
        model.setOrganizationIdentifier(null);
        model.getSettings().add(new LdapSetting("key1", "value1", false));
        model.getSettings().add(new LdapSetting("key2", "value2", true));
        String result = lcc.exportSettings();
        verifyNoMoreInteractions(ums);
        verify(lcc, times(1)).writeSettings(any(byte[].class));
        Properties props = getBytesAsProps();
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    @Test
    public void orgChanged_orgSetToOrgUnset() throws Exception {
        model.setShowIsPlatformSettingColumnVisible(true);
        model.setShowClearButtonVisible(false);
        model.setOrganizationIdentifier("non empty id");
        model.getSettings().add(new LdapSetting("key", "value", false));
        lcc.orgChanged(vce);
        assertNull(model.getOrganizationIdentifier());
        assertNull(model.getFile());
        assertFalse(model.isShowIsPlatformSettingColumnVisible());
        assertTrue(model.isShowClearButtonVisible());
        assertTrue(model.getSettings().isEmpty());
        verify(lcc, times(0)).initModelData();
        verify(lcc, times(0)).initResolvedSettings();
        verify(lcc, times(1)).initPlatformSettings();
    }

    @Test
    public void orgChanged_orgSetToOtherOrgSet() throws Exception {
        model.setOrganizationIdentifier("non empty id");
        model.setShowIsPlatformSettingColumnVisible(true);
        model.setShowClearButtonVisible(false);
        newOrgId = "orgId";
        lcc.orgChanged(vce);
        assertEquals("orgId", model.getOrganizationIdentifier());
        assertTrue(model.isShowIsPlatformSettingColumnVisible());
        assertFalse(model.isShowClearButtonVisible());
        verify(lcc, times(0)).initModelData();
        verify(lcc, times(1)).initResolvedSettings();
        verify(lcc, times(0)).initPlatformSettings();
    }

    @Test
    public void orgChanged_orgSetToEqualOrgSet() throws Exception {
        model.setOrganizationIdentifier("orgId");
        model.setShowIsPlatformSettingColumnVisible(false);
        model.setShowClearButtonVisible(true);
        newOrgId = "orgId";
        lcc.orgChanged(vce);
        assertFalse(model.isShowIsPlatformSettingColumnVisible());
        assertTrue(model.isShowClearButtonVisible());
        verify(lcc, times(0)).initModelData();
        verify(lcc, times(0)).initResolvedSettings();
    }

    @Test
    public void orgChanged_NoOrgToOrgSet() throws Exception {
        model.setShowIsPlatformSettingColumnVisible(true);
        model.setShowClearButtonVisible(false);
        newOrgId = "orgId";
        lcc.orgChanged(vce);
        verify(lcc, times(0)).initModelData();
        verify(lcc, times(1)).initResolvedSettings();
        assertTrue(model.isShowIsPlatformSettingColumnVisible());
        assertFalse(model.isShowClearButtonVisible());
    }

    @Test
    public void isNoSettingsDefined_ContentValidation_Empty() {
        assertTrue(lcc.isNoSettingsDefined());
    }

    @Test
    public void isNoSettingsDefined_ContentValidation_SettingsDefined() {
        model.getSettings().add(new LdapSetting("key", "value", false));
        assertFalse(lcc.isNoSettingsDefined());
    }

}
