/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.math.BigDecimal;

/**
 * Represents an organization and provides data related to it.
 */
public class VOOrganization extends BaseVO {

    private static final long serialVersionUID = 4479757863394747330L;

    /**
     * The short name that uniquely identifies the organization in the platform
     * (business key).
     */
    private String organizationId;

    /**
     * The organization's address as free text (city, street, country, ZIP code
     * etc.)
     */
    private String address;

    /**
     * The organization's email address.
     */
    private String email;

    /**
     * The default language.
     */
    private String locale;

    /**
     * The organization name.
     */
    private String name;

    /**
     * The organization's phone number.
     */
    private String phone;

    /**
     * The organization's website.
     */
    private String url;

    /**
     * The organization description.
     */
    private String description;

    /**
     * The discount granted to the organization.
     */
    private VODiscount discount;

    /**
     * The distinguished name to identify the organization in certificate based
     * service calls.
     */
    private String distinguishedName;

    /**
     * The home country of the organization
     */
    private String domicileCountry;

    /** The name space. Used for defining the version of the value object. */
    private String nameSpace;

    /**
     * The existence of an image; used in the front-end to display the logo.
     */
    private boolean imageDefined;

    /**
     * The support email address of a supplier organization.
     */
    private String supportEmail;

    private BigDecimal operatorRevenueShare;

    private boolean hasGrantedAccessToMarketplace;

    private boolean hasSubscriptions;

    private boolean hasPublishedServices;

    private long tenantKey;

    /**
     * Retrieves the identifier of the organization.
     * 
     * @return the organization ID
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the identifier of the organization.
     * 
     * @param organizationId
     *            the organization ID
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Retrieves the postal address of the organization.
     * 
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the postal address for the organization.
     * 
     * @param address
     *            the address as free text
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Retrieves the email address of the organization.
     * 
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address for the organization.
     * 
     * @param email
     *            the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retrieves the default language set for the organization.
     * 
     * @return the language code
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the default language for the organization.
     * 
     * @param locale
     *            the language. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     */

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Retrieves the name of the organization.
     * 
     * @return the organization name
     */

    public String getName() {
        return name;
    }

    /**
     * Sets the name for the organization.
     * 
     * @param name
     *            the organization name
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the phone number of the organization.
     * 
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number for the organization.
     * 
     * @param phone
     *            the phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Retrieves the organization's website.
     * 
     * @return the URL of the website
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the organization's website.
     * 
     * @param url
     *            the URL of the website
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Retrieves the description of the organization.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for the organization.
     * 
     * @param description
     *            the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the distinguished name used to identify the organization in
     * certificate based service calls.
     * 
     * @param distinguishedName
     *            the distinguished name
     */
    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    /**
     * Retrieves the discount granted to the organization.
     * 
     * @return a <code>VODiscount</code> object specifying the discount value as
     *         well as the start and end of the discount period
     */
    public VODiscount getDiscount() {
        return discount;
    }

    /**
     * Sets a discount for the organization.
     * 
     * @param discount
     *            a <code>VODiscount</code> object specifying the discount value
     *            as well as the start and end of the discount period
     */
    public void setDiscount(VODiscount discount) {
        this.discount = discount;
    }

    /**
     * Retrieves the distinguished name used to identify the organization in
     * certificate based service calls.
     * 
     * @return the distinguished name
     */
    public String getDistinguishedName() {
        return distinguishedName;
    }

    /**
     * Sets the home country for the organization. This can be any country
     * supported by the platform.
     * 
     * @param countryCode
     *            the country code in ISO 3166 format
     */
    public void setDomicileCountry(String countryCode) {
        this.domicileCountry = countryCode;
    }

    /**
     * Returns the home country of the organization.
     * 
     * @return the country code in ISO 3166 format
     */
    public String getDomicileCountry() {
        return domicileCountry;
    }

    /**
     * Sets the namespace for the organization object.
     * 
     * @param nameSpace
     *            the namespace
     */
    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    /**
     * Returns the namespace set for the organization object.
     * 
     * @return the namespace
     */
    public String getNameSpace() {
        return nameSpace;
    }

    /**
     * Checks whether an image has been defined for this organization.
     * 
     * @return <code>true</code> if an image has been defined,
     *         <code>false</code> otherwise
     */
    public boolean isImageDefined() {
        return imageDefined;
    }

    /**
     * Specifies whether an image is to be used for this organization. An image
     * can be associated with organizations having one of the following roles:
     * technology provider, supplier, broker, reseller.
     * 
     * @param imageDefined
     *            <code>true</code> if an image is to be used,
     *            <code>false</code> otherwise
     */
    public void setImageDefined(boolean imageDefined) {
        this.imageDefined = imageDefined;
    }

    /**
     * Retrieves the support email address of this supplier or reseller
     * organization.
     * 
     * @return the email address
     */
    public String getSupportEmail() {
        return supportEmail;
    }

    /**
     * Sets the support email address of this supplier or reseller organization.
     * 
     * @param supportEmail
     *            the email address
     */
    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    /**
     * @return the operatorRevenueShare
     */
    public BigDecimal getOperatorRevenueShare() {
        return operatorRevenueShare;
    }

    /**
     * @param operatorRevenueShare
     *            the operatorRevenueShare to set
     */
    public void setOperatorRevenueShare(BigDecimal operatorRevenueShare) {
        this.operatorRevenueShare = operatorRevenueShare;
    }

    public boolean isHasGrantedAccessToMarketplace() {
        return hasGrantedAccessToMarketplace;
    }

    public void setHasGrantedAccessToMarketplace(
            boolean hasGrantedAccessToMarketplace) {
        this.hasGrantedAccessToMarketplace = hasGrantedAccessToMarketplace;
    }

    public boolean isHasSubscriptions() {
        return hasSubscriptions;
    }

    public void setHasSubscriptions(boolean hasSubscriptions) {
        this.hasSubscriptions = hasSubscriptions;
    }

    public boolean isHasPublishedServices() {
        return hasPublishedServices;
    }

    public void setHasPublishedServices(boolean hasPublishedServices) {
        this.hasPublishedServices = hasPublishedServices;
    }

    public long getTenantKey() {
        return tenantKey;
    }

    public void setTenantKey(long tenantKey) {
        this.tenantKey = tenantKey;
    }
}
