/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.taskhandling.payloads;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Data container for all users to be imported
 * 
 * @author cheld
 * 
 */
public class ImportUserPayload implements TaskPayload {

    private static final long serialVersionUID = 9084531050184890385L;

    List<UserDefinition> usersToBeImported = new ArrayList<UserDefinition>();

    Long importingUserKey;

    String marketplaceId;

    String organizationId;

    /**
     * @return the marketplaceId
     */
    public String getMarketplaceId() {
        return marketplaceId;
    }

    /**
     * @param marketplaceId
     *            the marketplaceId to set
     */
    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public static class UserDefinition implements Serializable {
        private static final long serialVersionUID = 7231276813243517449L;

        VOUserDetails userDetails;

        List<UserRoleType> roles;

        public UserDefinition(VOUserDetails userDetails,
                List<UserRoleType> roles) {
            this.userDetails = userDetails;
            this.roles = roles;

        }

        public VOUserDetails getUserDetails() {
            return userDetails;
        }

        public List<UserRoleType> getRoles() {
            return roles;
        }
    }

    public void addUser(VOUserDetails userDetails, List<UserRoleType> roles) {
        usersToBeImported.add(new UserDefinition(userDetails, roles));
    }

    public List<UserDefinition> getUsersToBeImported() {
        return usersToBeImported;
    }

    public Long getImportingUserKey() {
        return importingUserKey;
    }

    public void setImportingUserKey(Long key) {
        this.importingUserKey = key;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationId() {
        return organizationId;
    }


    @Override
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Users: ");
        for (UserDefinition user : usersToBeImported) {
            sb.append(user.getUserDetails().getUserId());
            sb.append(", ");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getInfo();
    }

}
