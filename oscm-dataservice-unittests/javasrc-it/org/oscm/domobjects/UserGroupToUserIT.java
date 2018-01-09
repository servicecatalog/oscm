/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-6-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Tests for the domain object relationship user group to platform user
 * 
 * @author Fang
 * 
 */
public class UserGroupToUserIT extends DomainObjectTestBase {

    /**
     * <b>Test case:</b> Add user group to platform user<br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>The dependent object can be retrieved from DB via userGroup and via
     * platform user</li>
     * <li>Cascading delete works for deletion of userGroup</li>
     * <li>Cascading delete works for deletion of platform user</li>
     * </ul>
     * 
     * @throws Exception
     */

    private Organization org;
    private UserGroup userGroup;
    private PlatformUser user;
    private PlatformUser olduser;
    private UserGroupToUser oldUserGroupToUser;

    @Before
    public void setupData() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                setupUserGroup();
                setupPlatformUser();
                return null;
            }
        });
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

    private void setupUserGroup() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {

        org = Organizations.createOrganization(mgr,
                OrganizationRoleType.PLATFORM_OPERATOR);
        assertNotNull("organization expected", org);

        userGroup = new UserGroup();
        userGroup.setName("group1");
        userGroup.setDescription("group1 description");
        userGroup.setReferenceId("group1 reference Id");
        userGroup.setIsDefault(true);
        userGroup.setOrganization(org);

        mgr.persist(userGroup);
    }

    private void setupPlatformUser() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {
        user = PlatformUsers.createUser(mgr, "userId");
        assertNotNull("platform user expected", user);

        mgr.persist(user);
    }

    private void doTestAdd() throws Exception {
        userGroup = mgr.find(UserGroup.class, userGroup.getKey());
        user = mgr.find(PlatformUser.class, user.getKey());

        UserGroupToUser userGroupToUser = new UserGroupToUser();
        userGroupToUser.setUserGroup(userGroup);
        userGroupToUser.setPlatformuser(user);

        mgr.persist(userGroupToUser);
    }

    private void doTestAddCheck() {
        PlatformUser u = mgr.find(PlatformUser.class, user.getKey());
        assertNotNull("PlatformUser expected", u);

        assertNotNull(u.getUserGroupToUsers());
        List<UserGroupToUser> uguList = u.getUserGroupToUsers();
        Assert.assertEquals(1, uguList.size());

        UserGroupToUser ugu = uguList.get(0);
        Assert.assertEquals(userGroup, ugu.getUserGroup());
        Assert.assertEquals(user, ugu.getPlatformuser());
    }

    @Test
    public void testDeleteByPlatformUser() throws Throwable {
        try {
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
                    doTestDeleteByPlatformUser();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteCheckByPlatformUser();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestDeleteByPlatformUser() {
        olduser = mgr.find(PlatformUser.class, user.getKey());
        assertNotNull("Old PlatformUser expected", olduser);

        List<UserGroupToUser> list = olduser.getUserGroupToUsers();
        assertNotNull(list);
        Assert.assertEquals(1, list.size());

        oldUserGroupToUser = mgr.find(UserGroupToUser.class, list.get(0)
                .getKey());
        assertNotNull(oldUserGroupToUser);

        mgr.remove(olduser);
    }

    private void doTestDeleteCheckByPlatformUser() {
        // platform user must be deleted
        PlatformUser pu = mgr.find(PlatformUser.class, olduser.getKey());
        Assert.assertNull("PlatformUser still available", pu);

        // UserGroupToUser must be deleted
        UserGroupToUser userGroupToUser = mgr.find(UserGroupToUser.class,
                oldUserGroupToUser.getKey());
        Assert.assertNull("UserGroupToUser still available", userGroupToUser);
    }

    @Test
    public void testDeleteByUserGroup() throws Throwable {
        try {
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
                    doTestDeleteByUserGroup();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteCheckByUserGroup();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestDeleteByUserGroup() {
        userGroup = mgr.find(UserGroup.class, userGroup.getKey());
        assertNotNull("UserGroup expected", userGroup);

        List<UserGroupToUser> list = userGroup.getUserGroupToUsers();
        assertNotNull(list);
        Assert.assertEquals(1, list.size());

        oldUserGroupToUser = mgr.find(UserGroupToUser.class, list.get(0)
                .getKey());
        assertNotNull(oldUserGroupToUser);

        mgr.remove(userGroup);
    }

    private void doTestDeleteCheckByUserGroup() {
        // userGroup must be deleted
        UserGroup ug = mgr.find(UserGroup.class, userGroup.getKey());
        Assert.assertNull("UserGroup still available", ug);

        // UserGroupToUser must be deleted
        UserGroupToUser userGroupToUser = mgr.find(UserGroupToUser.class,
                oldUserGroupToUser.getKey());
        Assert.assertNull("UserGroupToUser still available", userGroupToUser);
    }
}
