/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.profile;

import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.ImageUploader;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.profile.POImageResource;
import org.oscm.internal.profile.POOrganization;
import org.oscm.internal.profile.POProfile;
import org.oscm.internal.profile.POUser;
import org.oscm.internal.profile.ProfileService;
import org.oscm.internal.types.constants.HiddenUIConstants;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.usermanagement.UserManagementService;

@ViewScoped
@ManagedBean(name="editProfileCtrl")
public class EditProfileCtrl extends BaseBean {
	
	@ManagedProperty(value="#{editProfileModel}")
    EditProfileModel model;
    ApplicationBean appBean;

    UserManagementService userMgmtSvc;
    ProfileService profileSvc;
    IdentityService identitySvc;

    public String getInitialize() {
        if (model == null) {
            model = ui.findBean("editProfileModel");
        }
        if (appBean == null) {
            appBean = ui.findBean("appBean");
        }
        initModelData(getProfileSvc().getProfile());
        return "";
    }

    private UserManagementService getUserMgmtSvc() {
        if (userMgmtSvc == null) {
            userMgmtSvc = sl.findService(UserManagementService.class);
        }
        return userMgmtSvc;
    }

    private ProfileService getProfileSvc() {
        if (profileSvc == null) {
            profileSvc = sl.findService(ProfileService.class);
        }
        return profileSvc;
    }

    private IdentityService getIdSvc() {
        if (identitySvc == null) {
            identitySvc = sl.findService(IdentityService.class);
        }
        return identitySvc;
    }

    boolean isOrganizationAdmin() {
        return getIdSvc().isCallerOrganizationAdmin();
    }

    @Override
    protected String getMarketplaceId() {
        return super.getMarketplaceId();
    }

    /**
     * Initializes the model with the profile data from the server.
     * 
     * @param profile
     *            The profile data to store in the model.
     */
    void initModelData(POProfile profile) {
        if (model.isInitialized()) {
            return;
        }
        initUserData(profile);
        initOrganizationData(profile);
        model.setRenderOrganizationSection(isOrganizationAdmin()
                && !appBean
                        .isUIElementHidden(HiddenUIConstants.PANEL_ORGANIZATION_EDIT_ORGANIZATIONDATA));
        model.setInitialized(true);
    }

    /**
     * Sets the details for the user data.
     * 
     * @param profile
     *            The profile data containing the information to use.
     */
    private void initUserData(POProfile profile) {
        POUser user = profile.getUser();

        // mark mapped LDAP attributes as read-only
        Set<SettingType> mappedAttributes = getUserMgmtSvc()
                .getMappedAttributes();

        model.setUserKey(new FieldData<Long>(Long.valueOf(user.getKey())));
        model.setUserVersion(new FieldData<Integer>(Integer.valueOf(user
                .getVersion())));
        String userTitle = user.getTitle() != null ? user.getTitle().name()
                : null;
        model.setUserTitle(new FieldData<String>(userTitle));

        boolean roFirstName = mappedAttributes
                .contains(SettingType.LDAP_ATTR_FIRST_NAME);
        model.setUserFirstName(new FieldData<String>(user.getFirstName(),
                roFirstName));

        boolean roLastName = mappedAttributes
                .contains(SettingType.LDAP_ATTR_LAST_NAME);
        model.setUserLastName(new FieldData<String>(user.getLastName(),
                roLastName));

        boolean roUserMail = mappedAttributes
                .contains(SettingType.LDAP_ATTR_EMAIL);
        model.setUserMail(new FieldData<String>(user.getMail(), roUserMail,
                true));

        model.setUserLocale(new FieldData<String>(user.getLocale(), false));

        appBean.checkLocaleValidation(user.getLocale());

    }

    /**
     * Sets the details for the organization data.
     * 
     * @param profile
     *            The profile data containing the information to use.
     */
    private void initOrganizationData(POProfile profile) {
        if (profile.getOrganization() != null) {
            POOrganization org = profile.getOrganization();
            model.setOrganizationKey(org.getKey());
            model.setOrganizationVersion(org.getVersion());
            model.setOrganizationIdentifier(new FieldData<String>(org
                    .getIdentifier()));
            model.setOrganizationName(new FieldData<String>(org.getName(),
                    false, isSellingOrganization(org), true));
            model.setOrganizationMail(new FieldData<String>(org.getMail(),
                    false, isSellingOrganization(org), true));
            model.setOrganizationSupportEmail(new FieldData<String>(org
                    .getSupportEmail(), false, false,
                    isSellingOrganization(org)));
            model.setOrganizationWebsiteUrl(new FieldData<String>(org
                    .getWebsiteUrl(), false, isSellingOrganization(org), true));
            model.setOrganizationAddress(new FieldData<String>(
                    org.getAddress(), false, isSellingOrganization(org), true));
            model.setOrganizationCountryISOCode(new FieldData<String>(org
                    .getCountryISOCode(), false, true, true));
            model.setOrganizationDescription(new FieldData<String>(org
                    .getDescription()));
            model.setOrganizationPhone(new FieldData<String>(org.getPhone(),
                    false, isSellingOrganization(org), true));
            model.setOrganizationImageDefined(org.isImageDefined());
            model.setOrganizationImage(new FieldData<ImageUploader>(
                    new ImageUploader(ImageType.ORGANIZATION_IMAGE), true,
                    false, isSellingOrganization(org)));
        }
    }

    private boolean isSellingOrganization(POOrganization org) {
        Set<OrganizationRoleType> roles = org.getOrganizationRoles();
        boolean result = roles.contains(OrganizationRoleType.SUPPLIER)
                || roles.contains(OrganizationRoleType.BROKER)
                || roles.contains(OrganizationRoleType.RESELLER);
        return result;
    }

    /**
     * Stores the current content of the model.
     * 
     * @return <code>null</code> to stay on the same page.
     */
    public String save() throws SaaSApplicationException {

        POUser user = new POUser();
        user.setKey(model.getUserKey().getValue().longValue());
        user.setVersion(model.getUserVersion().getValue().intValue());
        user.setFirstName(model.getUserFirstName().getValue());
        user.setLastName(model.getUserLastName().getValue());
        user.setMail(model.getUserMail().getValue());
        user.setLocale(model.getUserLocale().getValue());
        if (model.getUserTitle().getValue() != null) {
            user.setTitle(Salutation.valueOf(model.getUserTitle().getValue()));
        }
        POOrganization org = null;
        POImageResource image = null;
        if (model.getOrganizationIdentifier() != null) {
            org = new POOrganization();
            org.setKey(model.getOrganizationKey());
            org.setVersion(model.getOrganizationVersion());
            org.setMail(model.getOrganizationMail().getValue());
            org.setCountryISOCode(model.getOrganizationCountryISOCode()
                    .getValue());
            org.setAddress(model.getOrganizationAddress().getValue());
            org.setWebsiteUrl(model.getOrganizationWebsiteUrl().getValue());
            org.setDescription(model.getOrganizationDescription().getValue());
            org.setIdentifier(model.getOrganizationIdentifier().getValue());
            org.setName(model.getOrganizationName().getValue());
            org.setPhone(model.getOrganizationPhone().getValue());
            org.setSupportEmail(model.getOrganizationSupportEmail().getValue());
            image = model.getOrganizationImage().getValue()
                    .getPOImageResource();
        }
        POProfile profile = new POProfile(user, org, image);
        getProfileSvc().saveProfile(profile, getMarketplaceId());
        updateUserInSession();
        model.setInitialized(false);

        return OUTCOME_SUCCESS;

    }

    public EditProfileModel getModel() {
        return model;
    }

    public void setModel(EditProfileModel model) {
        this.model = model;
    }

    @Override
    public void updateUserInSession() {
        // update value object in session
        super.updateUserInSession();
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_USER_PROFILE_SAVED,
                getUserFromSession().getUserId());
    }

}
