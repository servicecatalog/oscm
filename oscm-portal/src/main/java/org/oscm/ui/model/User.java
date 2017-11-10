/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;

import org.apache.commons.validator.GenericValidator;

import org.oscm.string.Strings;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Wrapper Class for VOUserDetails which holds additional view attributes.
 * 
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean selected;
    private VOUserDetails voUserDetails;
    private long roleKey = 0;
    private VOUsageLicense voUsageLicense; // for subscriptionBean dialog
    private String ownerChecked;
    private boolean ownerSelected;
    private static final String RADIO_SELECTED = "true";
    private List<POUserGroup> userGroup;
    private String groupsToDisplay = "";

    public User(VOUserDetails voUserDetails) {
        this.voUserDetails = voUserDetails;
    }

    public VOUserDetails getVOUserDetails() {
        return voUserDetails;
    }

    public boolean isLockedFailedLoginAttemps() {
        return getStatus() == UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isLockedFailedLoginAttempts() {
        return voUserDetails.getStatus() == UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS;
    }

    public String getEmail() {
        return voUserDetails.getEMail();
    }

    public void setEmail(String mail) {
        voUserDetails.setEMail(mail);
    }

    /*
     * Delegate Methods
     */

    public String getAdditionalName() {
        return voUserDetails.getAdditionalName();
    }

    public String getAddress() {
        return voUserDetails.getAddress();
    }

    public String getEMail() {
        return voUserDetails.getEMail();
    }

    public String getFirstName() {
        return voUserDetails.getFirstName();
    }

    public long getKey() {
        return voUserDetails.getKey();
    }

    public String getLastName() {
        return voUserDetails.getLastName();
    }

    public String getLocale() {
        return voUserDetails.getLocale();
    }

    public String getOrganizationId() {
        return voUserDetails.getOrganizationId();
    }
    public String getOrganizationName() {
        return voUserDetails.getOrganizationName();
    }

    public String getPhone() {
        return voUserDetails.getPhone();
    }

    public List<SettingType> getRemoteLdapAttributes() {
        return voUserDetails.getRemoteLdapAttributes();
    }

    public Salutation getSalutation() {
        return voUserDetails.getSalutation();
    }

    public UserAccountStatus getStatus() {
        return voUserDetails.getStatus();
    }

    public String getUserId() {
        return voUserDetails.getUserId();
    }

    public int getVersion() {
        return voUserDetails.getVersion();
    }

    public boolean isAdditionalNameDisabled() {
        return isRemoteLdapAttributeActive(SettingType.LDAP_ATTR_ADDITIONAL_NAME);
    }

    public boolean isCustomer() {
        return voUserDetails.getOrganizationRoles().contains(
                OrganizationRoleType.CUSTOMER);
    }

    public boolean isEmailDisabled() {
        return isRemoteLdapAttributeActive(SettingType.LDAP_ATTR_EMAIL)
                && !GenericValidator.isBlankOrNull(getEMail());
    }

    public boolean isFirstNameDisabled() {
        return isRemoteLdapAttributeActive(SettingType.LDAP_ATTR_FIRST_NAME);
    }

    public boolean isLastNameDisabled() {
        return isRemoteLdapAttributeActive(SettingType.LDAP_ATTR_LAST_NAME);
    }

    public boolean isLocaleDisabled() {
        return isRemoteLdapAttributeActive(SettingType.LDAP_ATTR_LOCALE)
                && !GenericValidator.isBlankOrNull(getLocale());
    }

    public boolean isOrganizationAdmin() {
        return voUserDetails.hasAdminRole();
    }

    public boolean isRemoteLdapActive() {
        return voUserDetails.isRemoteLdapActive();
    }

    public boolean isPlatformOperator() {
        return voUserDetails.getUserRoles().contains(
                UserRoleType.PLATFORM_OPERATOR);
    }

    public boolean isSupplier() {
        return voUserDetails.getOrganizationRoles().contains(
                OrganizationRoleType.SUPPLIER);
    }

    public boolean isTechnologyProvider() {
        return voUserDetails.getOrganizationRoles().contains(
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
    }

    public boolean isTechnologyManager() {
        return voUserDetails.getUserRoles().contains(
                UserRoleType.TECHNOLOGY_MANAGER);
    }

    public boolean isServiceManager() {
        return voUserDetails.getUserRoles().contains(
                UserRoleType.SERVICE_MANAGER);
    }

    public boolean isBrokerManager() {
        return voUserDetails.getUserRoles().contains(
                UserRoleType.BROKER_MANAGER);
    }

    public boolean isBroker() {
        return voUserDetails.getOrganizationRoles().contains(
                OrganizationRoleType.BROKER);
    }

    public boolean isResellerManager() {
        return voUserDetails.getUserRoles().contains(
                UserRoleType.RESELLER_MANAGER);
    }

    public boolean isReseller() {
        return voUserDetails.getOrganizationRoles().contains(
                OrganizationRoleType.RESELLER);
    }

    public boolean isMarketplaceOwner() {
        return (voUserDetails.getUserRoles()
                .contains(UserRoleType.MARKETPLACE_OWNER));
    }

    public boolean isSubscriptionManager() {
        return (voUserDetails.getUserRoles()
                .contains(UserRoleType.SUBSCRIPTION_MANAGER));
    }

    public boolean isUnitAdministrator() {
        return (voUserDetails.getUserRoles()
                .contains(UserRoleType.UNIT_ADMINISTRATOR));
    }

    public boolean isUserIdDisabled() {
        return voUserDetails.isRemoteLdapActive();
    }

    public void setAdditionalName(String additionalName) {
        voUserDetails.setAdditionalName(additionalName);
    }

    public void setAddress(String address) {
        voUserDetails.setAddress(address);
    }

    public void setEMail(String mail) {
        voUserDetails.setEMail(mail);
    }

    public void setFirstName(String firstName) {
        voUserDetails.setFirstName(firstName);
    }

    public void setLastName(String lastName) {
        voUserDetails.setLastName(lastName);
    }

    public void setLocale(String locale) {
        voUserDetails.setLocale(locale);
    }

    public void setOrganizationAdmin(boolean isOrganizationAdmin) {
        if (isOrganizationAdmin) {
            voUserDetails.addUserRole(UserRoleType.ORGANIZATION_ADMIN);
        } else {
            voUserDetails.removeUserRole(UserRoleType.ORGANIZATION_ADMIN);
        }
    }

    public void setOrganizationId(String organizationId) {
        voUserDetails.setOrganizationId(organizationId);
    }

    public void setPhone(String phone) {
        voUserDetails.setPhone(phone);
    }

    public void setRemoteLdapActive(boolean remoteLdapActive) {
        voUserDetails.setRemoteLdapActive(remoteLdapActive);
    }

    public void setRemoteLdapAttributes(List<SettingType> remoteLdapAttributes) {
        voUserDetails.setRemoteLdapAttributes(remoteLdapAttributes);
    }

    public void setSalutation(Salutation salutation) {
        voUserDetails.setSalutation(salutation);
    }

    public void setStatus(UserAccountStatus status) {
        voUserDetails.setStatus(status);
    }

    public void setUserId(String userId) {
        voUserDetails.setUserId(userId);
    }

    @Override
    public String toString() {
        return voUserDetails.toString();
    }

    private boolean isRemoteLdapAttributeActive(SettingType setting) {
        return voUserDetails.getRemoteLdapAttributes() != null
                && voUserDetails.getRemoteLdapAttributes().contains(setting);
    }

    public void setRoleKey(long roleKey) {
        this.roleKey = roleKey;
    }

    public long getRoleKey() {
        return roleKey;
    }

    public void setRealmUserId(String realmUserId) {
        voUserDetails.setRealmUserId(realmUserId);
    }

    public String getRealmUserId() {
        return voUserDetails.getRealmUserId();
    }

    public Set<UserRoleType> getUserRoles() {
        return voUserDetails.getUserRoles();
    }

    public VOUsageLicense getVoUsageLicense() {
        return voUsageLicense;
    }

    public void setVoUsageLicense(VOUsageLicense voUsageLicense) {
        this.voUsageLicense = voUsageLicense;
    }

    public boolean isCheckBoxRendered() {
        return getKey() == 0;
    }

    public boolean isImageRendered() {
        return !isCheckBoxRendered();
    }

    public boolean isEmailLabelRendered() {
        return !isEmailInputRendered();
    }

    public boolean isEmailInputRendered() {
        return Strings.isEmpty(getEmail());
    }

    public void roleKeyChanged(ValueChangeEvent event) {
        Long roleKey = (Long) event.getNewValue();
        this.setRoleKey(roleKey.longValue());
    }

    public String getOwnerChecked() {
        ownerChecked = null;
        if (this.isOwnerSelected()) {
            ownerChecked = RADIO_SELECTED;
        }
        return ownerChecked;
    }

    public void setOwnerChecked(String ownerChecked) {
        this.ownerChecked = ownerChecked;
    }

    public boolean isOwnerSelected() {
        return ownerSelected;
    }

    public void setOwnerSelected(boolean ownerSelected) {
        this.ownerSelected = ownerSelected;
    }

    public List<POUserGroup> getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(List<POUserGroup> userGroup) {
        this.userGroup = userGroup;
    }

    public String getGroupsToDisplay() {
        return groupsToDisplay;
    }

    public void setGroupsToDisplay(String groupsToDisplay) {
        this.groupsToDisplay = groupsToDisplay;
    }

    public long getTenantKey() {
        try {
            return Long.parseLong(voUserDetails.getTenantKey());
        } catch(Exception exc) {
            //Do nothing. Current user is in default tenant.
            return 0;
        }
    }
}
