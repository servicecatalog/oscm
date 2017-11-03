/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *                                                                                                                                 
 *  Creation Date: 19.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.util.HashSet;
import java.util.Set;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.UnitRoleAssignment;
import org.oscm.domobjects.UnitUserRole;
import org.oscm.domobjects.UserGroupToUser;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

public class UnitUserRoles {

    public static void createSetupRoles(DataService dm)
            throws NonUniqueBusinessKeyException {
        UnitUserRole role = new UnitUserRole();
        role.setRoleName(UnitRoleType.ADMINISTRATOR);
        dm.persist(role);
        role = new UnitUserRole();
        role.setRoleName(UnitRoleType.USER);
        dm.persist(role);
        dm.flush();
    }

    public static Set<UnitRoleAssignment> createRoleAssignments(DataService dm,
            UserGroupToUser unitAssignment, UnitRoleType... roleTypes)
            throws NonUniqueBusinessKeyException {
        Set<UnitRoleAssignment> assignments = new HashSet<UnitRoleAssignment>();
        for (UnitRoleType roleType : roleTypes) {
            UnitUserRole role = new UnitUserRole();
            role.setRoleName(roleType);
            role = (UnitUserRole) dm.find(role);
            UnitRoleAssignment assignment = new UnitRoleAssignment();
            assignment.setUnitUserRole(role);
            assignment.setUserGroupToUser(unitAssignment);
            dm.persist(assignment);
            assignments.add(assignment);
            dm.flush();
        }
        return assignments;
    }
}
