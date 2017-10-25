/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.util.ArrayList;
import java.util.List;

import org.oscm.types.enumtypes.Salutation;
import org.oscm.types.enumtypes.SettingType;
import org.oscm.types.enumtypes.UserRoleType;

/**
 * Represents detailed information on a user registered in the platform.
 * 
 */
public class VOUserDetails extends VOUser {

    private static final long serialVersionUID = -3721638490335982843L;

    private String eMail;

    private String firstName;
    private String additionalName;
    private String lastName;
    private String address;
    private String phone;
    private String locale;
    private Salutation salutation;

    /**
     * The user ID in the context of a third-party realm, such as LDAP. The user
     * ID in another realm may differ from the user ID in the platform,
     * particularly if the realm user ID is not unique in the platform.
     */
    private String realmUserId;

    /**
     * Returns the user's ID for another realm, for example, LDAP. The user ID
     * for another realm may differ from the user ID in the platform,
     * particularly if the realm user ID is not unique in the platform.
     * 
     * @return the external user ID
     */
    public String getRealmUserId() {
        return realmUserId;
    }

    /**
     * Sets the user's ID for another realm, for example, LDAP. The user ID for
     * another realm may differ from the user ID in the platform, particularly
     * if the realm user ID is not unique in the platform.
     * 
     * @param realmUserId
     *            the external user ID
     */
    public void setRealmUserId(String realmUserId) {
        this.realmUserId = realmUserId;
    }

    /**
     * Flag indicating whether the user's organization uses a remote
     * authentication system such as LDAP.
     */
    private boolean remoteLdapActive;

    /**
     * The LDAP attributes for the user's organization which map to user
     * properties in the platform.
     */
    private List<SettingType> remoteLdapAttributes = new ArrayList<SettingType>();

    /**
     * Default constructor.
     */
    public VOUserDetails() {

    }

    /**
     * Retrieves the user's postal address.
     * 
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the user's postal address.
     * 
     * @param address
     *            the address as free text
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Retrieves the user's phone number.
     * 
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the user's phone number.
     * 
     * @param phone
     *            the phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Checks whether the user is an administrator of his organization.
     * 
     * @return <code>true</code> if the user has the administrator role,
     *         <code>false</code> otherwise
     * 
     */
    public boolean hasAdminRole() {
        return getUserRoles().contains(UserRoleType.ORGANIZATION_ADMIN);
    }

    /**
     * Constructs a user details object with the given numeric key and version.
     * 
     * @param id
     *            the key
     * @param version
     *            the version
     */
    public VOUserDetails(long id, int version) {
        super(id, version);
    }

    /**
     * Retrieves the user's email address.
     * 
     * @return the email address
     */
    public String getEMail() {
        return eMail;
    }

    /**
     * Sets the user's email address.
     * 
     * @param mail
     *            the email address
     */
    public void setEMail(String mail) {
        eMail = mail;
    }

    /**
     * Retrieves the user's first name.
     * 
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.
     * 
     * @param firstName
     *            the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Retrieves the user's additional name.
     * 
     * @return the additional name
     */
    public String getAdditionalName() {
        return additionalName;
    }

    /**
     * Sets the user's additional name.
     * 
     * @param additionalName
     *            the additional name
     */
    public void setAdditionalName(String additionalName) {
        this.additionalName = additionalName;
    }

    /**
     * Retrieves the user's last name.
     * 
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.
     * 
     * @param lastName
     *            the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Retrieves the user's default language.
     * 
     * @return the language code
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the user's default language.
     * 
     * @param locale
     *            the language. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Retrieves the title or salutation used to address the user in generated
     * emails.
     * 
     * @return the salutation, for example, <code>Mr.</code> or
     *         <code>Mrs.</code>
     */
    public Salutation getSalutation() {
        return salutation;
    }

    /**
     * Sets the title or salutation to be used to address the user in generated
     * emails.
     * 
     * @param salutation
     *            the salutation, for example, <code>Mr.</code> or
     *            <code>Mrs.</code>
     */
    public void setSalutation(Salutation salutation) {
        this.salutation = salutation;
    }

    /**
     * Checks whether the user's organization uses a remote LDAP system for user
     * authentication.
     * 
     * @return <code>true</code> if the organization uses a remote LDAP system,
     *         <code>false</code> otherwise
     */
    public boolean isRemoteLdapActive() {
        return remoteLdapActive;
    }

    /**
     * Specifies whether the user's organization uses a remote LDAP system for
     * user authentication.
     * 
     * @param remoteLdapActive
     *            <code>true</code> if the organization uses a remote LDAP
     *            system, <code>false</code> otherwise
     */
    public void setRemoteLdapActive(boolean remoteLdapActive) {
        this.remoteLdapActive = remoteLdapActive;
    }

    /**
     * Retrieves the LDAP attributes for the user's organization which map to
     * user properties in the platform.
     * 
     * @return the list of LDAP attributes
     */
    public List<SettingType> getRemoteLdapAttributes() {
        return remoteLdapAttributes;
    }

    /**
     * Sets the LDAP attributes for the user's organization which map to user
     * properties in the platform.
     * 
     * @param remoteLdapAttributes
     *            the list of LDAP attributes
     */
    public void setRemoteLdapAttributes(List<SettingType> remoteLdapAttributes) {
        this.remoteLdapAttributes = remoteLdapAttributes;
    }
}
