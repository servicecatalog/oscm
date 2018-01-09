/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld                                                      
 *                                                                              
 *  Creation Date: 02.05.2011                                                      
 *                                                                              
 *  Completion Time: 02.05.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.data.PlatformUsers;

/**
 * Test cases for the domain class <code>RoleAssignment</code>
 * 
 * @author cheld
 */
public class RoleAssignmentIT extends DomainObjectTestBase {

    long key;

    @Test
    public void testAdd() throws Exception {

        // create role assignment
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                RoleAssignment assignment = new RoleAssignment();
                assignment.setRole(createRole());
                assignment.setUser(createUser());
                mgr.persist(assignment);
                key = assignment.getKey();

                return null;
            }

        });

        // search role assignment
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                // check persisted object
                RoleAssignment assignment = mgr.find(RoleAssignment.class, key);
                assertNotNull(assignment.getUser());
                assertNotNull(assignment.getRole());

                // versions of associated objects must not change
                assertEquals(0, assignment.getUser().getVersion());
                assertEquals(0, assignment.getRole().getVersion());

                return null;
            }

        });
    }

    PlatformUser createUser() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {
        PlatformUser user = PlatformUsers.createUser(mgr, "user");
        return user;
    }

    UserRole createRole() throws NonUniqueBusinessKeyException {
        UserRole role = new UserRole(UserRoleType.ORGANIZATION_ADMIN);
        mgr.persist(role);
        return role;
    }

}
