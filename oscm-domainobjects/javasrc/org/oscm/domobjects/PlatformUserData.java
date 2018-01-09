/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
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
import java.util.Date;

import javax.persistence.*;

import org.oscm.domobjects.converters.PricingPeriodConverter;
import org.oscm.domobjects.converters.SalutaionConverter;
import org.oscm.domobjects.converters.UserAccountStatusConverter;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.UserAccountStatus;

/**
 * DataContainer for domain object Organization
 * 
 * @see Organization.java
 * 
 * @author schmid
 */
@Embeddable
public class PlatformUserData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * User's userId
     */
    @Column(nullable = false)
    private String userId;

    /**
     * User's address as freetext (city, street, country, zip-code etc.)
     */
    private String address;

    /**
     * User's firstname
     */
    private String firstName;

    /**
     * User's lastname
     */
    private String lastName;

    /**
     * User's additional names (as free text)
     */
    private String additionalName;

    /**
     * User's email-address (TODO email to be validated ?)
     */
    private String email;

    /**
     * User's phone number
     */
    private String phone;

    /**
     * Date of creation of the user account
     */
    @Column(nullable = false)
    private long creationDate;

    /**
     * Current status of the user's account
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserAccountStatus status;

    /**
     * Counts the number of failed login or security answer attempts.
     */
    @Column(nullable = false)
    private int failedLoginCounter;

    /**
     * User's locale.
     */
    @Column(nullable = false)
    private String locale;

    /**
     * User's salutation.
     */
    @Enumerated(EnumType.STRING)
    private Salutation salutation;

    /**
     * Password salt
     */
    private long passwordSalt;

    /**
     * Password hash
     */
    private byte[] passwordHash;

    /**
     * The user id used in a third party realm, such as LDAP.
     */
    private String realmUserId;

    /**
     * Time stamp of last recovery request for the user's account password
     */
    private long passwordRecoveryStartDate;

    public long getPasswordRecoveryStartDate() {
        return passwordRecoveryStartDate;
    }

    public void setPasswordRecoveryStartDate(long passwordRecoveryStartDate) {
        this.passwordRecoveryStartDate = passwordRecoveryStartDate;
    }

    public String getRealmUserId() {
        return realmUserId;
    }

    public void setRealmUserId(String realmUserId) {
        this.realmUserId = realmUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String login) {
        this.userId = login;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAdditionalName() {
        return additionalName;
    }

    public void setAdditionalName(String additionalName) {
        this.additionalName = additionalName;
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

    public Date getCreationDate() {
        if (creationDate != 0) {
            return new Date(creationDate);
        } else {
            return null;
        }
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate.getTime();
    }

    public UserAccountStatus getStatus() {
        return status;
    }

    public void setStatus(UserAccountStatus status) {
        this.status = status;
    }

    public int getFailedLoginCounter() {
        return failedLoginCounter;
    }

    public void setFailedLoginCounter(int failedLoginCounter) {
        this.failedLoginCounter = failedLoginCounter;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Salutation getSalutation() {
        return salutation;
    }

    public void setSalutation(Salutation salutation) {
        this.salutation = salutation;
    }

    public long getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(long passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }
}
