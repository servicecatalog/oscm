/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 27.07.2015                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.domobjects;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

public class UnitRoleAssignmentIT extends DomainObjectTestBase {

    private UnitUserRole unitUserRole;
    private UserGroupToUser userGroupToUser;
    private UnitRoleAssignment unitRoleAssignment;

    @Before
    public void setupData() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                setupUnitUserRole();
                setupUserGroupToUser();
                return null;
            }
        });
    }

    private void setupUnitUserRole() throws NonUniqueBusinessKeyException {
        unitUserRole = new UnitUserRole();
        unitUserRole.setRoleName(UnitRoleType.ADMINISTRATOR);

        assertNotNull("unit user role expected", unitUserRole);
        mgr.persist(unitUserRole);
    }

    private void setupUserGroupToUser() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {
        PlatformUser user = PlatformUsers.createUser(mgr, "userId");
        assertNotNull("platform user expected", user);
        mgr.persist(user);

        Organization org = Organizations.createOrganization(mgr,
                OrganizationRoleType.PLATFORM_OPERATOR);
        assertNotNull("organization expected", org);
        mgr.persist(org);

        UserGroup userGroup = new UserGroup();
        userGroup.setName("NEW_GROUP");
        userGroup.setOrganization(org);
        mgr.persist(userGroup);
        
        userGroupToUser = new UserGroupToUser();
        userGroupToUser.setPlatformuser(user);
        userGroupToUser.setUserGroup(userGroup);
        
        assertNotNull("platform user expected", user);
        mgr.persist(userGroupToUser);
    }

    @Test
    public void testAdd() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAddCheck();
                return null;
            }
        });
    }

    private void doTestAdd() throws Exception {
        unitUserRole = mgr.find(UnitUserRole.class, unitUserRole.getKey());
        userGroupToUser = mgr.find(UserGroupToUser.class, userGroupToUser.getKey());

        unitRoleAssignment = new UnitRoleAssignment();
        unitRoleAssignment.setUnitUserRole(unitUserRole);
        unitRoleAssignment.setUserGroupToUser(userGroupToUser);

        mgr.persist(unitRoleAssignment);
    }

    private void doTestAddCheck() {
        unitRoleAssignment = mgr.find(UnitRoleAssignment.class,
                unitRoleAssignment.getKey());
        assertNotNull("Unit user role expected", unitRoleAssignment);

        Assert.assertEquals(unitUserRole, unitRoleAssignment.getUnitUserRole());
        Assert.assertEquals(userGroupToUser, unitRoleAssignment.getUserGroupToUser());
    }

    @Test
    public void testDelete() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                doTestDelete();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestDeleteCheck();
                return null;
            }
        });
    }

    private void doTestDelete() {
        unitRoleAssignment = mgr.find(UnitRoleAssignment.class,
                unitRoleAssignment.getKey());
        assertNotNull("Unit role assignment expected", unitRoleAssignment);
        mgr.remove(unitRoleAssignment);
    }

    private void doTestDeleteCheck() {
        unitRoleAssignment = mgr.find(UnitRoleAssignment.class,
                unitRoleAssignment.getKey());
        Assert.assertNull("Unit Role Assignment still available",
                unitRoleAssignment);
    }
}
