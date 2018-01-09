/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-6-24                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Organizations;
import org.oscm.validator.ADMValidator;

/**
 * Test of the UserGroup domain object.
 * 
 * @author Fang
 * 
 */
public class UserGroupIT extends DomainObjectTestBase {

    private final List<DomainObjectWithVersioning<?>> domObjects = new ArrayList<DomainObjectWithVersioning<?>>();
    private Organization org;
    private UserGroup userGroup;

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
        org = Organizations.createOrganization(mgr,
                OrganizationRoleType.PLATFORM_OPERATOR);
        assertNotNull("organisation expected", org);

        userGroup = new UserGroup();
        userGroup.setName("group1");
        userGroup.setDescription("group1 description");
        userGroup.setIsDefault(false);
        userGroup.setOrganization(org);
        userGroup.setReferenceId("a reference id");

        mgr.persist(userGroup);
        domObjects.add(userGroup);
    }

    private void doTestAddCheck() {
        UserGroup oldGroup = (UserGroup) domObjects.get(0);
        assertNotNull("Old UserGroup expected", oldGroup);
        UserGroup userGroup = mgr.find(UserGroup.class, oldGroup.getKey());
        assertNotNull("UserGroup expected", userGroup);

        assertEquals(oldGroup.getName(), userGroup.getName());
        assertEquals(oldGroup.getDescription(), userGroup.getDescription());
        assertFalse(userGroup.isDefault());
        assertEquals(oldGroup.getOrganization().getKey(), userGroup
                .getOrganization().getKey());
        assertEquals(oldGroup.getReferenceId(), userGroup.getReferenceId());
    }

    @Test
    public void testDelete() throws Throwable {
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
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestDelete() {
        UserGroup oldUserGroup = (UserGroup) domObjects.get(0);
        assertNotNull("Old UserGroup expected", oldUserGroup);
        UserGroup userGroup = mgr.find(UserGroup.class, oldUserGroup.getKey());
        assertNotNull("UserGroup expected", userGroup);
        domObjects.clear();
        domObjects.add((UserGroup) ReflectiveClone.clone(userGroup));
        mgr.remove(userGroup);
    }

    private void doTestDeleteCheck() {
        UserGroup oldUserGroup = (UserGroup) domObjects.get(0);
        assertNotNull("Old UserGroup expected", oldUserGroup);
        try {
            mgr.getReference(UserGroup.class, oldUserGroup.getKey());
            throw new IllegalStateException("deleted usergroup "
                    + oldUserGroup.getKey() + " still exists!");
        } catch (ObjectNotFoundException ex) {
            // expected
        }
    }

    @Test
    public void testModify() throws Throwable {
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
                    doTestModify(true);
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testModify_Bug11135() throws Throwable {
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
                    doTestModify(false);
                    return null;
                }
            });
        } catch (SaaSSystemException e) {
            boolean isRestrict255 = e.getCause().getMessage().contains("255");
            assertEquals(Boolean.TRUE, Boolean.valueOf(isRestrict255));
        }
    }

    @Test
    public void testModifyRefId() throws Throwable {
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
                    doTestModifyRefId();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyRefIdCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestModify(boolean isValidDesc) {
        UserGroup oldUserGroup = (UserGroup) domObjects.get(0);
        assertNotNull("Old UserGroup expected", oldUserGroup);
        UserGroup userGroup = mgr.find(UserGroup.class, oldUserGroup.getKey());
        assertNotNull("UserGroup expected", userGroup);

        if (isValidDesc) {
            userGroup.setDescription("1234567890#new#");
        } else {
            String testString = fillString('x', ADMValidator.LENGTH_USERID + 1);
            userGroup.setDescription(testString);
        }

        domObjects.clear();
        domObjects.add((UserGroup) ReflectiveClone.clone(userGroup));
    }

    private void doTestModifyCheck() {
        UserGroup oldUserGroup = (UserGroup) domObjects.get(0);
        assertNotNull("Old Category expected", oldUserGroup);
        UserGroup userGroup = mgr.find(UserGroup.class, oldUserGroup.getKey());
        assertNotNull("UserGroup expected", userGroup);
        assertEquals("Description value", oldUserGroup.getDescription(),
                userGroup.getDescription());
    }

    private void doTestModifyRefId() {
        UserGroup oldUserGroup = (UserGroup) domObjects.get(0);
        assertNotNull("Old UserGroup expected", oldUserGroup);
        UserGroup userGroup = mgr.find(UserGroup.class, oldUserGroup.getKey());
        assertNotNull("UserGroup expected", userGroup);
        userGroup.setReferenceId("The new reference id");
        domObjects.clear();
        domObjects.add((UserGroup) ReflectiveClone.clone(userGroup));
    }

    private void doTestModifyRefIdCheck() {
        UserGroup oldUserGroup = (UserGroup) domObjects.get(0);
        assertNotNull("Old Category expected", oldUserGroup);
        UserGroup userGroup = mgr.find(UserGroup.class, oldUserGroup.getKey());
        assertNotNull("UserGroup expected", userGroup);
        assertEquals("Wrong reference ID", oldUserGroup.getReferenceId(),
                userGroup.getReferenceId());
    }

    /**
     * Cascading delete groups for deletion of organization
     * 
     * @throws Throwable
     */
    @Test
    public void testDeleteByOrg() throws Throwable {
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
                    doTestDeleteByOrg();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteCheckByOrg();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestDeleteByOrg() {
        org = mgr.find(Organization.class, org.getKey());
        assertNotNull("Organization expected", org);

        List<UserGroup> list = org.getUserGroups();
        assertNotNull(list);
        Assert.assertEquals(1, list.size());

        userGroup = mgr.find(UserGroup.class, list.get(0).getKey());
        assertNotNull(userGroup);

        mgr.remove(org);
    }

    private void doTestDeleteCheckByOrg() {
        // Organization must be deleted
        Organization organization = mgr.find(Organization.class, org.getKey());
        Assert.assertNull("Organization still available", organization);

        // UserGroup must be deleted
        UserGroup ug = mgr.find(UserGroup.class, userGroup.getKey());
        Assert.assertNull("UserGroup still available", ug);
    }

    @Test
    public void getInvisibleProducts() {
        UserGroup userGrp = createUserGroupWithProducts();
        assertEquals(2, userGrp.getInvisibleProducts().size());
    }

    @Test
    public void getUsers() {
        UserGroup userGrp = createUserGroupWithUsers();
        assertEquals(2, userGrp.getUsers().size());
    }

    private UserGroup createUserGroupWithProducts() {
        UserGroup userGroup = new UserGroup();
        userGroup
                .setUserGroupToInvisibleProducts(prepareUserGroupToInvisibleProducts());
        return userGroup;
    }

    private UserGroup createUserGroupWithUsers() {
        UserGroup userGroup = new UserGroup();
        userGroup.setUserGroupToUsers(prepareUserGroupToUsers());
        return userGroup;
    }

    private List<UserGroupToInvisibleProduct> prepareUserGroupToInvisibleProducts() {
        List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = new ArrayList<UserGroupToInvisibleProduct>();
        userGroupToInvisibleProducts
                .add(prepareUserGroupToInvisibleProduct("product1"));
        userGroupToInvisibleProducts
                .add(prepareUserGroupToInvisibleProduct("product2"));
        return userGroupToInvisibleProducts;
    }

    private List<UserGroupToUser> prepareUserGroupToUsers() {
        List<UserGroupToUser> userGroupToUsers = new ArrayList<UserGroupToUser>();
        userGroupToUsers.add(prepareUserGroupToUser("user1"));
        userGroupToUsers.add(prepareUserGroupToUser("user2"));
        return userGroupToUsers;
    }

    private UserGroupToInvisibleProduct prepareUserGroupToInvisibleProduct(
            String id) {
        UserGroupToInvisibleProduct userGroupToInvisibleProduct = new UserGroupToInvisibleProduct();
        userGroupToInvisibleProduct.setProduct(prepareProduct(id));
        userGroupToInvisibleProduct.setForallusers(false);
        return userGroupToInvisibleProduct;
    }

    private UserGroupToUser prepareUserGroupToUser(String id) {
        UserGroupToUser userGroupToUser = new UserGroupToUser();
        userGroupToUser.setPlatformuser(prepareUser(id));
        return userGroupToUser;
    }

    private Product prepareProduct(String productId) {
        Product p = new Product();
        p.setProductId(productId);
        return p;
    }

    private PlatformUser prepareUser(String userId) {
        PlatformUser user = new PlatformUser();
        user.setUserId(userId);
        return user;

    }

    private String fillString(char fillChar, int count) {
        char[] chars = new char[count];
        while (count > 0)
            chars[--count] = fillChar;
        return new String(chars);
    }
}
