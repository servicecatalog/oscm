/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 23.01.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author weiser
 * 
 */
public class UserRegisterMultipleTask extends WebtestTask {

    private static final String[] LAST_NAMES = new String[] { "Mueller",
            "Schmidt", "Bauer", "Schneider", "Wagner", "Huber", "Fischer" };
    private static final String[] FIRST_NAMES = new String[] { "Ralf",
            "Rainer", "Elisabeth", "Rolf", "Monika", "Helga", "Erich",
            "Johann", "Bernhard", "Joachim", "Renate", "Petra" };
    private static final Random RND = new Random();

    private String locale;
    private String userId;
    private String organizationId;
    private String email;
    private String marketplaceId;
    private String roles;
    private int number;

    private int numUserKeysReturned;
    private String userKeyProp = "userKeys";

    @Override
    public void executeInternal() throws Exception {
        IdentityService idSvc = getServiceInterface(IdentityService.class);

        VOUserDetails user = new VOUserDetails();
        user.setOrganizationId(getOrganizationId());
        user.setEMail(getEmail());
        user.setLocale(getLocale());
        ArrayList<UserRoleType> userRoleTypes = getUserRoleTypes();

        ArrayList<String> userKeys = new ArrayList<String>();
        int i = 0;
        while (i < number) {
            user.setFirstName(getRandomFirstName());
            user.setLastName(getRandomLastName());
            user.setUserId(getUniqueUserId());
            try {
                VOUserDetails u = idSvc.createUser(user, userRoleTypes,
                        getMarketplaceId());
                if (userKeys.size() < numUserKeysReturned) {
                    userKeys.add(String.valueOf(u.getKey()));
                }
                i++;
            } catch (NonUniqueBusinessKeyException e) {
                // new try with another random user id
            }
        }
        String string = userKeys.toString();
        getProject().setProperty(userKeyProp,
                string.substring(1, string.length() - 1));
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

    public String getUniqueUserId() {
        return userId + "_" + UUID.randomUUID().toString().split("-")[4];
    }

    public String getUserId() {
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

    public String getEmail() {
        if (email == null) {
            email = getProject().getProperty("common.email");
        }
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRandomFirstName() {
        return random(FIRST_NAMES);
    }

    public String getRandomLastName() {
        return random(LAST_NAMES);
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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    private String random(String[] values) {
        int i = RND.nextInt(values.length);
        return values[i];
    }

    public int getNumUserKeysReturned() {
        return numUserKeysReturned;
    }

    public void setNumUserKeysReturned(int numUserKeysReturned) {
        this.numUserKeysReturned = numUserKeysReturned;
    }

    public String getUserKeyProp() {
        return userKeyProp;
    }

    public void setUserKeyProp(String userKeyProp) {
        this.userKeyProp = userKeyProp;
    }
}
