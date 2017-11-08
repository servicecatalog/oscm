/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                   
 *                                                                              
 *  Creation Date: 02.05.2011                                                      
 *                                                                              
 *  Completion Time: 03.05.2011                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.util.HashSet;
import java.util.Set;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Setup class for test data related to roles of platform users.
 * 
 * @author cheld
 */
public class UserRoles {

    public static void createSetupRoles(DataService dm)
            throws NonUniqueBusinessKeyException {
        UserRole role = new UserRole(UserRoleType.ORGANIZATION_ADMIN);
        dm.persist(role);
        role = new UserRole(UserRoleType.PLATFORM_OPERATOR);
        dm.persist(role);
        role = new UserRole(UserRoleType.SERVICE_MANAGER);
        dm.persist(role);
        role = new UserRole(UserRoleType.TECHNOLOGY_MANAGER);
        dm.persist(role);
        role = new UserRole(UserRoleType.MARKETPLACE_OWNER);
        dm.persist(role);
        role = new UserRole(UserRoleType.BROKER_MANAGER);
        dm.persist(role);
        role = new UserRole(UserRoleType.RESELLER_MANAGER);
        dm.persist(role);
        role = new UserRole(UserRoleType.SUBSCRIPTION_MANAGER);
        dm.persist(role);
        role = new UserRole(UserRoleType.UNIT_ADMINISTRATOR);
        dm.persist(role);
    }

    public static Set<RoleAssignment> createRoleAssignments(PlatformUser user,
            UserRoleType... roleTypes) {
        Set<RoleAssignment> assignments = new HashSet<RoleAssignment>();
        for (UserRoleType roleType : roleTypes) {
            RoleAssignment assignment = new RoleAssignment();
            assignment.setUser(user);
            assignment.setRole(new UserRole(roleType));
            assignments.add(assignment);
        }
        return assignments;
    }

}
