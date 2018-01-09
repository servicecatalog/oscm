/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 02.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.business;

import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.provisioning.data.User;

/**
 * Converts between OSCM provisioning users and APP service instance users
 * 
 * @author kulle
 */
public class UserMapper {

    /**
     * Transforms a OSCM provisioning user entity into an APP service user
     * entity.
     */
    public static ServiceUser toServiceUser(User fromUser) {
        if (fromUser == null) {
            return null;
        }
        ServiceUser toUser = new ServiceUser();
        toUser.setApplicationUserId(fromUser.getApplicationUserId());
        toUser.setEmail(fromUser.getEmail());
        toUser.setLocale(fromUser.getLocale());
        toUser.setRoleIdentifier(fromUser.getRoleIdentifier());
        toUser.setUserId(fromUser.getUserId());
        toUser.setLastName(fromUser.getUserLastName());
        toUser.setFirstName(fromUser.getUserFirstName());
        return toUser;
    }

    /**
     * Transforms a OSCM provisioning user entity into an APP service user
     * entity.
     */
    public static User toProvisioningUser(ServiceUser fromUser) {
        if (fromUser == null) {
            return null;
        }
        User toUser = new User();
        toUser.setApplicationUserId(fromUser.getApplicationUserId());
        toUser.setEmail(fromUser.getEmail());
        toUser.setLocale(fromUser.getLocale());
        toUser.setRoleIdentifier(fromUser.getRoleIdentifier());
        toUser.setUserId(fromUser.getUserId());
        toUser.setUserLastName(fromUser.getLastName());
        toUser.setUserFirstName(fromUser.getFirstName());
        return toUser;
    }

}
