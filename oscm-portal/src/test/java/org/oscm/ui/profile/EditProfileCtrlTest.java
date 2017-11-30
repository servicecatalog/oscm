/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.profile;

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

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.ImageUploader;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.profile.POOrganization;
import org.oscm.internal.profile.POProfile;
import org.oscm.internal.profile.POUser;
import org.oscm.internal.profile.ProfileService;
import org.oscm.internal.types.constants.HiddenUIConstants;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.usermanagement.UserManagementService;

public class EditProfileCtrlTest {

    private EditProfileCtrl ctrl;
    private POUser user;
    private POOrganization org;
    private POProfile profile;
    @Captor
    private ArgumentCaptor<POProfile> saveArgument;
    @Captor
    private ArgumentCaptor<String> capturedMid;
    boolean hideOrgPanel = false;
    private Set<SettingType> mappedSettingTypes = new HashSet<SettingType>();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        ctrl = spy(new EditProfileCtrl());
        ctrl.model = new EditProfileModel();
        ApplicationBean appBean = mock(ApplicationBean.class);
        doAnswer(new Answer<Boolean>() {
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return Boolean.valueOf(hideOrgPanel);
            }
        }).when(appBean).isUIElementHidden(
                eq(HiddenUIConstants.PANEL_ORGANIZATION_EDIT_ORGANIZATIONDATA));
        ctrl.appBean = appBean;
        doReturn("marketplaceidentifier").when(ctrl).getMarketplaceId();
        initProfile();
        ctrl.profileSvc = mock(ProfileService.class);
        doNothing().when(ctrl.profileSvc).saveProfile(saveArgument.capture(),
                capturedMid.capture());

        ctrl.identitySvc = mock(IdentityService.class);
        doReturn(Boolean.TRUE).when(ctrl.identitySvc)
                .isCallerOrganizationAdmin();

        ctrl.userMgmtSvc = mock(UserManagementService.class);
        doAnswer(new Answer<Set<SettingType>>() {
            public Set<SettingType> answer(InvocationOnMock invocation)
                    throws Throwable {
                return mappedSettingTypes;
            }
        }).when(ctrl.userMgmtSvc).getMappedAttributes();
        doNothing().when(ctrl).updateUserInSession();
    }

    @Test
    public void initModelData_validateUserData() throws Exception {
        ctrl.initModelData(profile);

        EditProfileModel model = ctrl.model;
        assertEquals(user.getKey(), model.getUserKey().getValue().longValue());
        assertEquals(user.getVersion(), model.getUserVersion().getValue()
                .intValue());
        assertEquals(user.getTitle().name(), model.getUserTitle().getValue());
        assertEquals(user.getFirstName(), model.getUserFirstName().getValue());
        assertEquals(user.getLastName(), model.getUserLastName().getValue());
        assertEquals(user.getMail(), model.getUserMail().getValue());
        assertEquals(user.getLocale(), model.getUserLocale().getValue());
        assertTrue(model.isInitialized());
    }

    @Test
    public void initModelData_validateInitializedFlag() throws Exception {
        ctrl.initModelData(profile);
        assertTrue(ctrl.model.isInitialized());
    }

    @Test
    public void initModelData_validateOrgData() throws Exception {
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        validateOrganizationData(model, false);
    }

    @Test
    public void initModelData_Supplier() throws Exception {
        profile.getOrganization().getOrganizationRoles()
                .add(OrganizationRoleType.SUPPLIER);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        validateOrganizationData(model, true);
    }

    @Test
    public void initModelData_Broker() throws Exception {
        profile.getOrganization().getOrganizationRoles()
                .add(OrganizationRoleType.BROKER);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        validateOrganizationData(model, true);
    }

    @Test
    public void initModelData_Reseller() throws Exception {
        profile.getOrganization().getOrganizationRoles()
                .add(OrganizationRoleType.RESELLER);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        validateOrganizationData(model, true);
    }

    @Test
    public void initModelData_RenderOrgSectionNoAdmin() throws Exception {
        // given
        doReturn(Boolean.FALSE).when(ctrl.identitySvc)
                .isCallerOrganizationAdmin();
        // when
        ctrl.initModelData(profile);
        // then
        EditProfileModel model = ctrl.model;
        assertFalse(model.isRenderOrganizationSection());
    }

    @Test
    public void initModelData_RenderOrgSectionAdmin() throws Exception {
        profile.getUser().getUserRoles().add(UserRoleType.ORGANIZATION_ADMIN);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertTrue(model.isRenderOrganizationSection());
    }

    @Test
    public void initModelData_RenderOrgSectionAdminButHidden() throws Exception {
        profile.getUser().getUserRoles().add(UserRoleType.ORGANIZATION_ADMIN);
        hideOrgPanel = true;
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertFalse(model.isRenderOrganizationSection());
    }

    @Test
    public void initModelData_NoImageDefined() throws Exception {
        profile.getUser().getUserRoles().add(UserRoleType.ORGANIZATION_ADMIN);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertFalse(model.isOrganizationImageDefined());
    }

    @Test
    public void initModelData_ImageDefined() throws Exception {
        profile.getOrganization().setImageDefined(true);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertTrue(model.isOrganizationImageDefined());
    }

    @Test
    public void initModelData_ValidateImageUploaderNoSellingOrg()
            throws Exception {
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertNotNull(model.getOrganizationImage().getValue());
        assertFalse(model.getOrganizationImage().isRendered());
    }

    @Test
    public void initModelData_ValidateImageUploaderSupplier() throws Exception {
        profile.getOrganization().getOrganizationRoles()
                .add(OrganizationRoleType.SUPPLIER);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertNotNull(model.getOrganizationImage().getValue());
        assertTrue(model.getOrganizationImage().isRendered());
    }

    @Test
    public void initModelData_ValidateImageUploaderBroker() throws Exception {
        profile.getOrganization().getOrganizationRoles()
                .add(OrganizationRoleType.BROKER);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertNotNull(model.getOrganizationImage().getValue());
        assertTrue(model.getOrganizationImage().isRendered());
    }

    @Test
    public void initModelData_ValidateImageUploaderReseller() throws Exception {
        profile.getOrganization().getOrganizationRoles()
                .add(OrganizationRoleType.RESELLER);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertNotNull(model.getOrganizationImage().getValue());
        assertTrue(model.getOrganizationImage().isRendered());
    }

    @Test
    public void initModelData_ValidateUserSettingsNoTitle() throws Exception {
        profile.getUser().setTitle(null);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertNotNull(model.getUserTitle());
        assertNull(model.getUserTitle().getValue());
    }

    @Test
    public void initModelData_ValidateDataReadOnlySettings_NoneMapped()
            throws Exception {
        profile.getUser().setTitle(null);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertFalse(model.getUserMail().isReadOnly());
        assertFalse(model.getUserFirstName().isReadOnly());
        assertFalse(model.getUserLastName().isReadOnly());
        assertFalse(model.getUserLocale().isReadOnly());
    }

    @Test
    public void initModelData_ValidateDataReadOnlySettings_MailMapped()
            throws Exception {
        mappedSettingTypes.add(SettingType.LDAP_ATTR_EMAIL);
        profile.getUser().setTitle(null);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getUserMail().isReadOnly()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.getUserFirstName().isReadOnly()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.getUserLastName().isReadOnly()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.getUserLocale().isReadOnly()));
    }

    @Test
    public void initModelData_ValidateDataReadOnlySettings_AllMapped()
            throws Exception {
        mappedSettingTypes.addAll(SettingType.LDAP_ATTRIBUTES);
        profile.getUser().setTitle(null);
        ctrl.initModelData(profile);
        EditProfileModel model = ctrl.model;
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getUserMail().isReadOnly()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getUserFirstName().isReadOnly()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getUserLastName().isReadOnly()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.getUserLocale().isReadOnly()));
    }

    @Test
    public void initModelData__localeCheck() throws Exception {
        // given
        profile.getUser().setLocale("de");

        // when
        ctrl.initModelData(profile);

        // then
        verify(ctrl.appBean, times(1)).checkLocaleValidation("de");
    }

    @Test
    public void save_validateMappingJustUserData() throws Exception {
        // first initialize the model
        profile = new POProfile(user, null);
        ctrl.initModelData(profile);
        ctrl.save();

        // ensure that save operation is called with proper argument
        verify(ctrl.profileSvc, times(1)).saveProfile(any(POProfile.class),
                anyString());
        assertEquals("marketplaceidentifier", capturedMid.getValue());
        assertEquals(1, saveArgument.getAllValues().size());
        POProfile passedProfile = saveArgument.getValue();
        POUser passedUserData = passedProfile.getUser();
        assertEquals(user.getKey(), passedUserData.getKey());
        assertEquals(user.getVersion(), passedUserData.getVersion());
        assertEquals(user.getFirstName(), passedUserData.getFirstName());
        assertEquals(user.getLastName(), passedUserData.getLastName());
        assertEquals(user.getMail(), passedUserData.getMail());
        assertEquals(user.getTitle(), passedUserData.getTitle());
        assertEquals(user.getLocale(), passedUserData.getLocale());
        assertNull(passedProfile.getOrganization());
    }

    @Test
    public void save_validateInitializedFlag() throws Exception {
        // first initialize the model
        profile = new POProfile(user, null);
        ctrl.initModelData(profile);
        ctrl.save();
        assertFalse(ctrl.getModel().isInitialized());
    }

    @Test
    public void save_NoTitle() throws Exception {
        // first initialize the model
        ctrl.initModelData(profile);
        ctrl.model.getUserTitle().setValue(null);
        ctrl.save();
        assertNull(saveArgument.getValue().getUser().getTitle());
    }

    @Test
    public void save_validateMappingOrgData() throws Exception {
        // first initialize the model
        ctrl.initModelData(profile);
        ctrl.save();

        // ensure that save operation is called with proper argument
        verify(ctrl.profileSvc, times(1)).saveProfile(any(POProfile.class),
                anyString());
        assertEquals(1, saveArgument.getAllValues().size());
        assertEquals("marketplaceidentifier", capturedMid.getValue());
        POProfile passedProfile = saveArgument.getValue();
        POOrganization passedOrgData = passedProfile.getOrganization();
        assertEquals(org.getKey(), passedOrgData.getKey());
        assertEquals(org.getVersion(), passedOrgData.getVersion());
        assertEquals(org.getAddress(), passedOrgData.getAddress());
        assertEquals(org.getCountryISOCode(), passedOrgData.getCountryISOCode());
        assertEquals(org.getDescription(), passedOrgData.getDescription());
        assertEquals(org.getIdentifier(), passedOrgData.getIdentifier());
        assertEquals(org.getMail(), passedOrgData.getMail());
        assertEquals(org.getName(), passedOrgData.getName());
        assertEquals(org.getPhone(), passedOrgData.getPhone());
        assertEquals(org.getSupportEmail(), passedOrgData.getSupportEmail());
        assertEquals(org.getWebsiteUrl(), passedOrgData.getWebsiteUrl());
    }

    @Test
    public void save_validateImageData() throws Exception {
        // first initialize the model
        ctrl.initModelData(profile);
        ImageUploader iuMock = mock(ImageUploader.class);
        ctrl.model.getOrganizationImage().setValue(iuMock);
        ctrl.save();
        verify(iuMock, times(1)).getPOImageResource();
    }

    private void initProfile() {
        user = new POUser();
        user.setKey(91);
        user.setVersion(5);
        user.setTitle(Salutation.MS);
        user.setFirstName("Herbert");
        user.setLastName("Kraxlhuber");
        user.setMail("mail@mailhost.xy");
        user.setLocale("en");
        org = new POOrganization();
        org.setKey(84);
        org.setVersion(2);
        org.setIdentifier("identifier");
        org.setName("name");
        org.setMail("mail@mailhost.de");
        org.setSupportEmail("supportmail@mailhost.en");
        org.setWebsiteUrl("http://host.en");
        org.setAddress("address");
        org.setCountryISOCode("DE");
        org.setDescription("description");
        org.setPhone("0281323292");
        profile = new POProfile(user, org);
    }

    private void validateOrganizationData(EditProfileModel model,
            boolean isSellerOrganization) {
        assertEquals(org.getKey(), model.getOrganizationKey());
        assertEquals(org.getVersion(), model.getOrganizationVersion());
        assertEquals(org.getIdentifier(), model.getOrganizationIdentifier()
                .getValue());
        assertEquals(org.getName(), model.getOrganizationName().getValue());
        assertEquals(org.getMail(), model.getOrganizationMail().getValue());
        assertEquals(org.getSupportEmail(), model.getOrganizationSupportEmail()
                .getValue());
        assertEquals(org.getWebsiteUrl(), model.getOrganizationWebsiteUrl()
                .getValue());
        assertEquals(org.getAddress(), model.getOrganizationAddress()
                .getValue());
        assertEquals(org.getCountryISOCode(), model
                .getOrganizationCountryISOCode().getValue());
        assertTrue(model.getOrganizationCountryISOCode().isRequired());
        assertEquals(org.getDescription(), model.getOrganizationDescription()
                .getValue());
        assertEquals(org.getPhone(), model.getOrganizationPhone().getValue());
        // check those attributes which are rendered for selling organizations
        assertEquals(Boolean.valueOf(isSellerOrganization),
                Boolean.valueOf(model.getOrganizationSupportEmail()
                        .isRendered()));
        // check those attributes which are mandatory for selling organizations
        assertEquals(Boolean.valueOf(isSellerOrganization),
                Boolean.valueOf(model.getOrganizationName().isRequired()));
        assertEquals(Boolean.valueOf(isSellerOrganization),
                Boolean.valueOf(model.getOrganizationMail().isRequired()));
        assertEquals(Boolean.valueOf(isSellerOrganization),
                Boolean.valueOf(model.getOrganizationPhone().isRequired()));
        assertEquals(Boolean.valueOf(isSellerOrganization),
                Boolean.valueOf(model.getOrganizationWebsiteUrl().isRequired()));
        assertEquals(Boolean.valueOf(isSellerOrganization),
                Boolean.valueOf(model.getOrganizationAddress().isRequired()));
    }

}
