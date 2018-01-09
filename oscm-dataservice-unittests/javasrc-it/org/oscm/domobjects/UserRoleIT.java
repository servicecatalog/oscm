/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 29.04.2011                                                      
 *                                                                              
 *  Completion Time: 29.04.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * 
 * Test of the UserRole domain object.
 * 
 * @author cheld
 * 
 */
public class UserRoleIT extends DomainObjectTestBase {

    @Test
    public void testAdd() throws Exception {

        // create role
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                UserRole role = new UserRole(UserRoleType.ORGANIZATION_ADMIN);
                mgr.persist(role);
                return null;
            }
        });

        // search role
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                UserRole psersistedRole = (UserRole) mgr.find(new UserRole(
                        UserRoleType.ORGANIZATION_ADMIN));
                assertNotNull(psersistedRole);
                return null;
            }
        });
    }

}
