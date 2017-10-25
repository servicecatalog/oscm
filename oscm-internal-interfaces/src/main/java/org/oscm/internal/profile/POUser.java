/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.profile;

import java.util.HashSet;
import java.util.Set;

import org.oscm.internal.base.BasePO;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Represents user data as needed for the profile data.
 * 
 * @author jaeger
 * 
 */
public class POUser extends BasePO {

    private static final long serialVersionUID = 1L;

    private Salutation title;
    private String firstName;
    private String lastName;
    private String mail;
    private String locale;
    private Set<UserRoleType> userRoles = new HashSet<UserRoleType>();

    public POUser() {

    }

    public POUser(VOUserDetails details) {
        this.key = details.getKey();
        this.version = details.getVersion();
        this.firstName = details.getFirstName();
        this.lastName = details.getLastName();
        this.title = details.getSalutation();
        this.mail = details.getEMail();
        this.locale = details.getLocale();
        this.userRoles = details.getUserRoles();
    }

    public Salutation getTitle() {
        return title;
    }

    public void setTitle(Salutation title) {
        this.title = title;
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

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Set<UserRoleType> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRoleType> userRoles) {
        this.userRoles = userRoles;
    }

}
