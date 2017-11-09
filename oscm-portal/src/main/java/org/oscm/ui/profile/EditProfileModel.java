/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.profile;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.common.ImageUploader;

@ViewScoped
@ManagedBean(name="editProfileModel")
public class EditProfileModel {

    private FieldData<Long> userKey;
    private FieldData<Integer> userVersion;
    private FieldData<String> userTitle;
    private FieldData<String> userFirstName;
    private FieldData<String> userLastName;
    private FieldData<String> userMail;
    private FieldData<String> userLocale;

    private long organizationKey;
    private int organizationVersion;
    private FieldData<String> organizationIdentifier;
    private FieldData<String> organizationName;
    private FieldData<String> organizationMail;
    private FieldData<String> organizationSupportEmail;
    private FieldData<String> organizationWebsiteUrl;
    private FieldData<String> organizationAddress;
    private FieldData<String> organizationCountryISOCode;
    private FieldData<String> organizationDescription;
    private FieldData<String> organizationPhone;
    private FieldData<ImageUploader> organizationImage;
    private boolean organizationImageDefined;
    private boolean renderOrganizationSection;

    private boolean initialized;

    public FieldData<Long> getUserKey() {
        return userKey;
    }

    public void setUserKey(FieldData<Long> userKey) {
        this.userKey = userKey;
    }

    public FieldData<Integer> getUserVersion() {
        return userVersion;
    }

    public void setUserVersion(FieldData<Integer> userVersion) {
        this.userVersion = userVersion;
    }

    public FieldData<String> getUserTitle() {
        return userTitle;
    }

    public void setUserTitle(FieldData<String> userTitle) {
        this.userTitle = userTitle;
    }

    public FieldData<String> getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(FieldData<String> userFirstName) {
        this.userFirstName = userFirstName;
    }

    public FieldData<String> getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(FieldData<String> userLastName) {
        this.userLastName = userLastName;
    }

    public FieldData<String> getUserMail() {
        return userMail;
    }

    public void setUserMail(FieldData<String> userMail) {
        this.userMail = userMail;
    }

    public FieldData<String> getUserLocale() {
        return userLocale;
    }

    public void setUserLocale(FieldData<String> userLocale) {
        this.userLocale = userLocale;
    }

    public long getOrganizationKey() {
        return organizationKey;
    }

    public void setOrganizationKey(long organizationKey) {
        this.organizationKey = organizationKey;
    }

    public int getOrganizationVersion() {
        return organizationVersion;
    }

    public void setOrganizationVersion(int organizationVersion) {
        this.organizationVersion = organizationVersion;
    }

    public FieldData<String> getOrganizationIdentifier() {
        return organizationIdentifier;
    }

    public void setOrganizationIdentifier(
            FieldData<String> organizationIdentifier) {
        this.organizationIdentifier = organizationIdentifier;
    }

    public FieldData<String> getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(FieldData<String> organizationName) {
        this.organizationName = organizationName;
    }

    public FieldData<String> getOrganizationMail() {
        return organizationMail;
    }

    public void setOrganizationMail(FieldData<String> organizationMail) {
        this.organizationMail = organizationMail;
    }

    public FieldData<String> getOrganizationSupportEmail() {
        return organizationSupportEmail;
    }

    public void setOrganizationSupportEmail(
            FieldData<String> organizationSupportEmail) {
        this.organizationSupportEmail = organizationSupportEmail;
    }

    public FieldData<String> getOrganizationWebsiteUrl() {
        return organizationWebsiteUrl;
    }

    public void setOrganizationWebsiteUrl(
            FieldData<String> organizationWebsiteUrl) {
        this.organizationWebsiteUrl = organizationWebsiteUrl;
    }

    public FieldData<String> getOrganizationAddress() {
        return organizationAddress;
    }

    public void setOrganizationAddress(FieldData<String> organizationAddress) {
        this.organizationAddress = organizationAddress;
    }

    public FieldData<String> getOrganizationCountryISOCode() {
        return organizationCountryISOCode;
    }

    public void setOrganizationCountryISOCode(
            FieldData<String> organizationCountryISOCode) {
        this.organizationCountryISOCode = organizationCountryISOCode;
    }

    public FieldData<String> getOrganizationDescription() {
        return organizationDescription;
    }

    public void setOrganizationDescription(
            FieldData<String> organizationDescription) {
        this.organizationDescription = organizationDescription;
    }

    public FieldData<String> getOrganizationPhone() {
        return organizationPhone;
    }

    public void setOrganizationPhone(FieldData<String> organizationPhone) {
        this.organizationPhone = organizationPhone;
    }

    public boolean isRenderOrganizationSection() {
        return renderOrganizationSection;
    }

    public void setRenderOrganizationSection(boolean renderOrganizationSection) {
        this.renderOrganizationSection = renderOrganizationSection;
    }

    public FieldData<ImageUploader> getOrganizationImage() {
        return organizationImage;
    }

    public void setOrganizationImage(FieldData<ImageUploader> organizationImage) {
        this.organizationImage = organizationImage;
    }

    public boolean isOrganizationImageDefined() {
        return organizationImageDefined;
    }

    public void setOrganizationImageDefined(boolean organizationImageDefined) {
        this.organizationImageDefined = organizationImageDefined;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

}
