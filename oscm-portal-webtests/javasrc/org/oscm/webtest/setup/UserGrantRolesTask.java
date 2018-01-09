/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Sep 22, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 22, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;

import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Custom ANT task granting user roles to a user using the WS-API.
 * 
 * @author Dirk Bernsau
 * 
 */
public class UserGrantRolesTask extends WebtestTask {

    private String userId;
    private String roles;
    protected boolean isRevoke = false; // allows easy subclassing

    public void setUserId(String value) {
        userId = value;
    }

    public void setRoles(String value) {
        roles = value;
    }

    @Override
    public void executeInternal() throws Exception {
        if (roles == null || roles.trim().length() == 0) {
            throwBuildException("No roles specified - use the 'roles' attribute to specify one or more (comma separated) roles");
            return;
        }
        String[] splittedRoles = roles.split(",");
        ArrayList<UserRoleType> roleList = new ArrayList<UserRoleType>();
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
        IdentityService idSvc = getServiceInterface(IdentityService.class);
        if (userId == null || userId.trim().length() == 0) {
            VOUserDetails user = idSvc.getCurrentUserDetailsIfPresent();
            if (user == null) {
                throwBuildException("No userId specified - use the 'userId' attribute to specify a user");
                return;
            }
            userId = user.getUserId();
        }
        VOUser user = new VOUser();
        user.setUserId(userId);
        user = idSvc.getUser(user);
        if (isRevoke) {
            idSvc.revokeUserRoles(user, roleList);
            log("Revoked role(s) " + roleList + " to user with ID '" + userId
                    + "'");
        } else {
            idSvc.grantUserRoles(user, roleList);
            log("Granted role(s) " + roleList + " to user with ID '" + userId
                    + "'");
        }
    }
}
