/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.UnitRoleAssignment;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserGroupToUser;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.UnitUserRoles;
import org.oscm.test.data.UserGroups;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class UnitDaoIT extends EJBTestBase {

    private DataService ds;
    private UnitDao dao;
    private UserGroup unit1;
    private UserGroup unit2;
    private UserGroup unit3;
    private PlatformUser user1;
    private PlatformUser user2;
    private PlatformUser user3;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new UnitDao(ds);

        createUnitUserRoles();

        Organization customerOrg = createOrganization("OrganizationId",
                OrganizationRoleType.CUSTOMER);

        user1 = createUser("User1", customerOrg);
        user2 = createUser("User2", customerOrg);
        user3 = createUser("User3", customerOrg);

        unit1 = createUnit("unit1", customerOrg, null);
        assignUnitRole(assignUserToUnit(user1, unit1),
                UnitRoleType.ADMINISTRATOR);
        assignUnitRole(assignUserToUnit(user2, unit1), UnitRoleType.USER);

        unit2 = createUnit("unit2", customerOrg, null);
        assignUnitRole(assignUserToUnit(user2, unit2),
                UnitRoleType.ADMINISTRATOR);
        assignUnitRole(assignUserToUnit(user3, unit2), UnitRoleType.USER);

        unit3 = createUnit("unit3", customerOrg, null);
        assignUnitRole(assignUserToUnit(user1, unit3),
                UnitRoleType.ADMINISTRATOR);
        assignUnitRole(assignUserToUnit(user2, unit3), UnitRoleType.USER);
    }

    @Test
    public void retrieveUnitForUnitAdmin_user1() throws Exception {
        // given setup
        // when
        List<Long> unitKeys = retrieveUnitForAdmin(user1.getKey());

        // then
        assertEquals(2, unitKeys.size());
        assertTrue(unitKeys.contains(Long.valueOf(unit1.getKey())));
        assertTrue(unitKeys.contains(Long.valueOf(unit3.getKey())));
    }

    @Test
    public void retrieveUnitForUnitAdmin_user2() throws Exception {
        // given setup
        // when
        List<Long> unitKeys = retrieveUnitForAdmin(user2.getKey());

        // then
        assertEquals(1, unitKeys.size());
        assertTrue(unitKeys.contains(Long.valueOf(unit2.getKey())));
    }

    @Test
    public void retrieveUnitForUnitAdmin_user3() throws Exception {
        // given setup
        // when
        List<Long> unitKeys = retrieveUnitForAdmin(user3.getKey());

        // then
        assertEquals(0, unitKeys.size());
    }

    private Organization createOrganization(final String organizationId,
            final OrganizationRoleType... roles) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds, organizationId,
                        roles);
            }
        });
    }

    private PlatformUser createUser(final String userId, final Organization org)
            throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return PlatformUsers.createUser(ds, userId, org);
            }
        });
    }

    private UserGroup createUnit(final String name, final Organization org,
            final PlatformUser user) throws Exception {
        return runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws Exception {
                return UserGroups.createUserGroup(ds, name, org, false,
                        "Description", "refId", user);
            }
        });
    }

    private UserGroupToUser assignUserToUnit(final PlatformUser user,
            final UserGroup group) throws Exception {
        return runTX(new Callable<UserGroupToUser>() {
            @Override
            public UserGroupToUser call() throws Exception {
                return UserGroups.assignUserToGroup(ds, user, group);
            }
        });
    }

    private Set<UnitRoleAssignment> assignUnitRole(
            final UserGroupToUser unitAssignment,
            final UnitRoleType... roleTypes) throws Exception {
        return runTX(new Callable<Set<UnitRoleAssignment>>() {
            @Override
            public Set<UnitRoleAssignment> call() throws Exception {
                return UnitUserRoles.createRoleAssignments(ds, unitAssignment,
                        roleTypes);
            }
        });
    }

    private List<Long> retrieveUnitForAdmin(final long unitAdminKey)
            throws Exception {
        return runTX(new Callable<List<Long>>() {
            @Override
            public List<Long> call() throws Exception {
                return dao.retrieveUnitKeysForUnitAdmin(unitAdminKey);
            }
        });
    }

    private Void createUnitUserRoles() throws Exception {
        return runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UnitUserRoles.createSetupRoles(ds);
                return null;
            }
        });
    }

}
