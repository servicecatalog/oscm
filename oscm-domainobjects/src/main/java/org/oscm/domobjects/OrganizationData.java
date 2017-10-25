/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * DataContainer for domain object Organization
 * 
 * @see Organization
 * 
 * @author schmid
 */
@Embeddable
public class OrganizationData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * Short name uniquely identifying the organization in the whole platform
     * (Business key)
     */
    @Column(nullable = false)
    private String organizationId;

    /**
     * Organization's address as free text (incl. city, street, country,
     * zip-code etc.)
     */
    private String address;

    /**
     * Organization's name
     */
    private String name;

    /**
     * Organization's email-address
     */
    private String email;

    /**
     * Indicates whether the organization is configured to support users from a
     * remote LDAP system or not.
     */
    private boolean remoteLdapActive;

    /**
     * The support email-address for supplier organizations.
     */
    private String supportEmail;

    /**
     * Organization's phone number
     */
    private String phone;

    /**
     * Organization's web site
     */
    private String url;

    /**
     * Date of registration of the organization at the SaaS platform
     */
    @Column(nullable = false)
    private long registrationDate;

    /**
     * Date of deregistration of the organization from the SaaS platform. Will
     * be <code>null</code> as long as the organization's account is active.
     */
    private Long deregistrationDate;

    /**
     * Organization's locale.
     */
    private String locale;

    /**
     * The distinguished name for certificate based web service security.
     */
    private String distinguishedName;

    /**
     * The billing cut-off day for this organization as supplier/reseller.
     */
    //TODO @Column(nullable = false)
    private int cutOffDay;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRegistrationDate(long registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Temporal(TemporalType.DATE)
    public long getRegistrationDate() {
        return registrationDate;
    }

    public void setDeregistrationDate(Long deregistrationDate) {
        this.deregistrationDate = deregistrationDate;
    }

    @Temporal(TemporalType.DATE)
    public Long getDeregistrationDate() {
        return deregistrationDate;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public boolean isRemoteLdapActive() {
        return remoteLdapActive;
    }

    public void setRemoteLdapActive(boolean remoteLdapActive) {
        this.remoteLdapActive = remoteLdapActive;
    }

    public void setCutOffDay(int cutOffDay) {
        this.cutOffDay = cutOffDay;
    }

    public int getCutOffDay() {
        return cutOffDay;
    }

}
