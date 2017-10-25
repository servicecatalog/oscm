/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.profile;

import java.util.HashSet;
import java.util.Set;

import org.oscm.internal.base.BasePO;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.vo.VOOrganization;

/**
 * Represents organization data as needed for the profile data.
 * 
 * @author jaeger
 * 
 */
public class POOrganization extends BasePO {

    private static final long serialVersionUID = 1L;

    private String identifier;
    private String name;
    private String mail;
    private String supportEmail;
    private String websiteUrl;
    private String address;
    private String countryISOCode;
    private String description;
    private String phone;
    private boolean imageDefined;
    private Set<OrganizationRoleType> organizationRoles = new HashSet<OrganizationRoleType>();

    public POOrganization() {

    }

    public POOrganization(VOOrganization org, Set<OrganizationRoleType> orgRoles) {
        this.key = org.getKey();
        this.version = org.getVersion();
        this.identifier = org.getOrganizationId();
        this.name = org.getName();
        this.mail = org.getEmail();
        this.supportEmail = org.getSupportEmail();
        this.websiteUrl = org.getUrl();
        this.address = org.getAddress();
        this.countryISOCode = org.getDomicileCountry();
        this.description = org.getDescription();
        this.phone = org.getPhone();
        this.imageDefined = org.isImageDefined();
        this.organizationRoles = orgRoles;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String email) {
        this.mail = email;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountryISOCode() {
        return countryISOCode;
    }

    public void setCountryISOCode(String countryISOCode) {
        this.countryISOCode = countryISOCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Set<OrganizationRoleType> getOrganizationRoles() {
        return organizationRoles;
    }

    public void setOrganizationRoles(Set<OrganizationRoleType> organizationRoles) {
        this.organizationRoles = organizationRoles;
    }

    public boolean isImageDefined() {
        return imageDefined;
    }

    public void setImageDefined(boolean imageDefined) {
        this.imageDefined = imageDefined;
    }

}
