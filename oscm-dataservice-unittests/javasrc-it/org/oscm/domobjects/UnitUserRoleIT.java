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

import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.UnitRoleType;

public class UnitUserRoleIT extends DomainObjectTestBase {

    private static UnitUserRole unitUserRole;

    @BeforeClass
    public static void setupRole() {
        unitUserRole = new UnitUserRole();
        unitUserRole.setRoleName(UnitRoleType.ADMINISTRATOR);
    }

    @Test
    public void testAdd() throws Exception {

        // create unit role
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                mgr.persist(unitUserRole);
                return null;
            }
        });

        // search unit role
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                UnitUserRole psersistedRole = (UnitUserRole) mgr
                        .find(unitUserRole);
                assertNotNull(psersistedRole);
                return null;
            }
        });
    }
}
