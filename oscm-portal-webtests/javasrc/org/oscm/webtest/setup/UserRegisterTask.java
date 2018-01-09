/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 30.03.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;

import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author kulle
 * 
 */
public class UserRegisterTask extends WebtestTask {

    private String userId;
    private String address;
    private String organizationId;
    private String email;
    private String firstName;
    private String lastName;
    private String locale;
    private String phone;
    private String additionalName;
    private boolean uniqueId = true;

    private String marketplaceId;
    private String roles;

    @Override
    public void executeInternal() throws Exception {
        IdentityService idSvc = getServiceInterface(IdentityService.class);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(getUserId());
        user.setAdditionalName(getAdditionalName());
        user.setAddress(getAddress());
        user.setOrganizationId(getOrganizationId());
        user.setEMail(getEmail());
        user.setFirstName(getFirstName());
        user.setLastName(getLastName());
        user.setLocale(getLocale());
        user.setPhone(getPhone());

        idSvc.createUser(user, getUserRoleTypes(), getMarketplaceId());
    }

    private ArrayList<UserRoleType> getUserRoleTypes() {
        ArrayList<UserRoleType> roleList = new ArrayList<UserRoleType>();

        String[] splittedRoles = roles.split(",");
        for (int i = 0; i < splittedRoles.length; i++) {
            try {
                roleList.add(UserRoleType.valueOf(splittedRoles[i].trim()));
            } catch (IllegalArgumentException e) {
                String msg = "";
                for (int j = 0; j < UserRoleType.values().length; j++) {
                    msg += (j == 0 ? "" : ", ")
                            + UserRoleType.values()[j].toString();
                }
                throwBuildException("Invalid role '" + splittedRoles[i]
                        + "' - valid roles are " + msg);
            }
        }

        return roleList;
    }

    public String getUserId() {
        if (isUniqueId()) {
            userId = userId + String.valueOf(System.currentTimeMillis());
        }
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String addess) {
        this.address = addess;
    }

    public String getEmail() {
        if (email == null) {
            email = getProject().getProperty("common.email");
        }
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        if (firstName == null) {
            firstName = "First Name";
        }
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        if (lastName == null) {
            lastName = "Last Name";
        }
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLocale() {
        if (locale == null) {
            locale = getProject().getProperty("common.locale");
        }
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAdditionalName() {
        return additionalName;
    }

    public void setAdditionalName(String additionalName) {
        this.additionalName = additionalName;
    }

    public boolean isUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(boolean uniqueId) {
        this.uniqueId = uniqueId;
    }

}
