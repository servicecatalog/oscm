/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 02.05.2011                                                      
 *                                                                              
 *  Completion Time: 02.05.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.util.ArrayList;
import java.util.List;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Setup class for test data related to platform users.
 * 
 * @author cheld
 */
public class PlatformUsers {

    /**
     * Creates a new platform user with the given user id. The required
     * organization is created as well.
     * 
     * @return PlatformUser
     * @throws NonUniqueBusinessKeyException
     * @throws ObjectNotFoundException
     */
    public static PlatformUser createUser(DataService mgr, String userId)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Organization org = Organizations.createOrganization(mgr);
        return createUser(mgr, userId, org);
    }

    /**
     * Creates a new platform user with the given user id.
     */
    public static PlatformUser createUser(DataService mgr, String userId,
            Organization org) throws NonUniqueBusinessKeyException {
        PlatformUser user = Organizations.createUserForOrg(mgr, org, false,
                userId);
        return user;
    }

    /**
     * Creates a new platform user with the given user id. The user is assigned
     * the administrator role. The required organization is created as well.
     * 
     * @return PlatformUser
     * @throws NonUniqueBusinessKeyException
     * @throws ObjectNotFoundException
     */
    public static PlatformUser createAdmin(DataService mgr, String userId)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Organization org = Organizations.createOrganization(mgr);
        return createAdmin(mgr, userId, org);
    }

    /**
     * Creates a new platform user with the given user id. The user is assigned
     * the administrator role.
     */
    public static PlatformUser createAdmin(DataService mgr, String userId,
            Organization org) throws NonUniqueBusinessKeyException {
        PlatformUser user = Organizations.createUserForOrg(mgr, org, true,
                userId);
        return user;
    }

    /**
     * Grant the given user the role administrator.
     */
    public static void grantAdminRole(DataService mgr, PlatformUser user)
            throws NonUniqueBusinessKeyException {
        grantRoles(mgr, user, UserRoleType.ORGANIZATION_ADMIN);
    }

    /**
     * Grant the given role to the given user.
     */
    public static void grantRoles(DataService mgr, PlatformUser user,
            UserRoleType... rolesToBeGranted)
            throws NonUniqueBusinessKeyException {
        for (UserRoleType roleToBeGranted : rolesToBeGranted) {
            if (!user.hasRole(roleToBeGranted)) {
                UserRole role = findOrCreateRole(mgr, roleToBeGranted);
                RoleAssignment assignment = new RoleAssignment();
                assignment.setRole(role);
                assignment.setUser(user);
                mgr.persist(assignment);
            }
        }
    }

    private static UserRole findOrCreateRole(DataService mgr,
            UserRoleType roleType) throws NonUniqueBusinessKeyException {
        UserRole role = (UserRole) mgr.find(new UserRole(roleType));
        if (role == null) {
            role = new UserRole();
            role.setRoleName(roleType);
            mgr.persist(role);
        }
        return role;
    }

    /**
     * Returns the user with the given ID.
     * 
     */
    public static PlatformUser findUser(DataService mgr, String userId) {
        PlatformUser sample = new PlatformUser();
        sample.setUserId(userId);
        PlatformUser user = (PlatformUser) mgr.find(sample);
        return user;
    }

    /**
     * Returns the user with the given ID prefix. The full user id is
     * constructed with the organization id, the same way the user is created.
     */
    public static PlatformUser findUser(DataService mgr, String userIdPrefix,
            Organization org) {
        String fullUserID = userIdPrefix + "_" + org.getOrganizationId();
        return findUser(mgr, fullUserID);
    }

    /**
     * Creates the given number of users as test data.
     */
    public static List<PlatformUser> createTestData(DataService mgr,
            Organization org, int numberOfUsers)
            throws NonUniqueBusinessKeyException {
        List<PlatformUser> users = new ArrayList<PlatformUser>();
        for (int i = 0; i < numberOfUsers; i++) {
            String userId = "TestUser_" + i;
            PlatformUser user = findUser(mgr, userId, org);
            if (user == null) {
                user = createUser(mgr, userId, org);
            }
            users.add(user);
        }
        return users;
    }
}
