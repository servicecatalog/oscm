/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jul 1, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.usergroupservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.UserGroups;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author zhaoh.fnst
 * 
 */
public class UserGroupDaoIT extends EJBTestBase {

    private static final String INSERT_UNIT_ROLE_SQL = "INSERT INTO unituserrole (tkey, version, rolename) VALUES (?, ?, ?)";
    private static final String groupName1 = "name1";
    private static final String groupDescription1 = "description1";
    private static final String groupReferenceId1 = "referenceId1";
    private static final String groupName2 = "name2";
    private static final String groupDescription2 = "description2";
    private static final String groupReferenceId2 = "referenceId2";
    private static final String defaultGroupName = "defaultGroup";
    private static final String defaultGroupDescription = "defaultGroupDescription";
    private static final String defaultGroupReferenceId = "defaultGroupReferenceId";
    private DataService ds;
    private UserGroupDao dao;
    private Organization admin;
    private Organization admin2;
    private PlatformUser user;
    private PlatformUser user2;
    private UserGroup group1;
    private UserGroup group2;
    private UserGroup defaultGroup;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new UserGroupDao();
        dao.dm = ds;
        admin = createOrg("admin", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        admin2 = createOrg("admin2", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        user = createUser(admin, false, "user");
        user2 = createUser(admin, false, "user2");
        group1 = createUserGroup(groupName1, admin, true, groupDescription1,
                groupReferenceId1, user);
        group2 = createUserGroup(groupName2, admin, false, groupDescription2,
                groupReferenceId2, user);
        defaultGroup = createUserGroup(defaultGroupName, admin2, true,
                defaultGroupDescription, defaultGroupReferenceId, user2);
        initUnitRoles();
    }

    private void initUnitRoles() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                insertUnitRole(Long.valueOf(1L), Long.valueOf(0L),
                        "ADMINISTRATOR");
                insertUnitRole(Long.valueOf(2L), Long.valueOf(0L), "USER");
                return null;
            }
        });
    }

    private void insertUnitRole(Long key, Long version, String roleName) {
        Query query = ds.createNativeQuery(INSERT_UNIT_ROLE_SQL);
        query.setParameter(1, key);
        query.setParameter(2, version);
        query.setParameter(3, roleName);
        query.executeUpdate();
    }

    @Test
    public void getUserGroupsForOrganization() throws Exception {
        // when
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        List<UserGroup> result = runTX(new Callable<List<UserGroup>>() {
            @Override
            public List<UserGroup> call() throws Exception {
                return dao.getUserGroupsForOrganization();
            }
        });

        // then
        assertEquals(2, result.size());
    }

    @Test
    public void getUserGroupsForOrganizationWithoutDefault() throws Exception {
        // when
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        List<UserGroup> result = runTX(new Callable<List<UserGroup>>() {
            @Override
            public List<UserGroup> call() throws Exception {
                return dao.getUserGroupsForOrganizationWithoutDefault();
            }
        });

        // then
        assertEquals(1, result.size());
        assertEquals(groupDescription2, result.get(0).getDescription());
        assertEquals(groupReferenceId2, result.get(0).getReferenceId());
        assertEquals(groupName2, result.get(0).getName());
        assertEquals(admin, result.get(0).getOrganization());

    }

    @Test
    public void getUserGroupDetails() throws Exception {
        // when
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        UserGroup result = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws Exception {
                return dao.getUserGroupDetails(group1.getKey());
            }
        });

        // then
        assertEquals(groupDescription1, result.getDescription());
        assertEquals(groupName1, result.getName());
        assertEquals(groupReferenceId1, result.getReferenceId());
        assertEquals(admin, result.getOrganization());

    }

    @Test
    public void getUserGroupsForUserWithoutDefault() throws Exception {
        // when
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        List<UserGroup> result = runTX(new Callable<List<UserGroup>>() {
            @Override
            public List<UserGroup> call() throws Exception {
                List<UserGroup> userGroupsForUserWithoutDefault = dao.getUserGroupsForUserWithoutDefault(user.getKey());
                assertEquals(admin, userGroupsForUserWithoutDefault.get(0).getOrganization());
                return userGroupsForUserWithoutDefault;

            }
        });

        // then
        assertEquals(1, result.size());
        assertEquals(groupDescription2, result.get(0).getDescription());
        assertEquals(groupReferenceId2, result.get(0).getReferenceId());
        assertEquals(groupName2, result.get(0).getName());

    }

    @Test
    public void getInvisibleProductKeysForUser() throws Exception {
        // when
        container.login(user.getKey(), ROLE_TECHNOLOGY_MANAGER);
        prepareInvisibleProducts();

        List<Long> result = runTX(new Callable<List<Long>>() {
            @Override
            public List<Long> call() throws Exception {
                return dao.getInvisibleProductKeysForUser(user.getKey());
            }
        });

        // then
        assertEquals(2, result.size());
    }

    @Test
    public void getInvisibleProductKeysWithUsersFlag() throws Exception {
        // when
        container.login(user.getKey(), ROLE_TECHNOLOGY_MANAGER);
        prepareInvisibleProducts();

        List<UserGroupToInvisibleProduct> result = runTX(new Callable<List<UserGroupToInvisibleProduct>>() {
            @Override
            public List<UserGroupToInvisibleProduct> call() throws Exception {
                return dao.getInvisibleProducts(group1.getKey());
            }
        });

        // then
        assertEquals(3, result.size());
    }

    @Test
    public void getInvisibleProductKeysForGroup() throws Exception {
        // when
        container.login(user2.getKey(), ROLE_TECHNOLOGY_MANAGER);
        prepareInvisibleProductsOnlyWithDefaultGroup();

        List<Long> result = runTX(new Callable<List<Long>>() {
            @Override
            public List<Long> call() throws Exception {
                return dao.getInvisibleProductKeysForGroup(defaultGroup
                        .getKey());
            }
        });

        // then
        assertEquals(5, result.size());
    }

    @Test
    public void getUserCountForGroup() throws Exception {
        // given
        PlatformUser user1_group2 = createUser(admin, false, "user1_group2");
        setUserToUserGroup(group2, user1_group2);
        PlatformUser user2_group2 = createUser(admin, false, "user2_group2");
        setUserToUserGroup(group2, user2_group2);

        // when
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        Long result = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return Long.valueOf(dao.getUserCountForGroup(group2.getKey()));
            }
        });

        // then
        assertEquals(3, result.longValue());
    }

    @Test
    public void getUserGroupsForUserWithRole() throws Exception {
        // given
        final PlatformUser testUser = createUser(admin, false, randomString());
        final UserGroup ug1 = createUserGroup(randomString(), admin, false,
                randomString(), randomString(), user);
        final UserGroup ug2 = createUserGroup(randomString(), admin, false,
                randomString(), randomString(), user);
        setUnitAdmin(testUser, ug1);
        setUnitAdmin(testUser, ug2);

        // when
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        List<UserGroup> result = runTX(new Callable<List<UserGroup>>() {
            @Override
            public List<UserGroup> call() throws Exception {
                return dao.getUserGroupsForUserWithRole(testUser.getKey(),
                        UnitRoleType.ADMINISTRATOR.getKey());
            }
        });

        // then
        assertEquals(2, result.size());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ds.remove(ug1);
                ds.remove(ug2);
                return null;
            }
        });
    }

    private void setUnitAdmin(final PlatformUser user, final UserGroup unit)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UserGroupToUser userGroupToUser = new UserGroupToUser();
                userGroupToUser.setUserGroup(unit);
                userGroupToUser.setPlatformuser(user);
                ds.persist(userGroupToUser);
                ds.flush();
                UnitRoleAssignment roleAssignment = new UnitRoleAssignment();
                UnitUserRole userRole = new UnitUserRole();
                userRole.setKey(UnitRoleType.ADMINISTRATOR.getKey());
                userRole.setRoleName(UnitRoleType.ADMINISTRATOR);
                roleAssignment.setUnitUserRole(userRole);
                roleAssignment.setUserGroupToUser(userGroupToUser);
                ds.persist(roleAssignment);
                ds.flush();
                return null;
            }
        });
    }

    @Test
    public void getUserCountForDefaultGroup() throws Exception {
        // given
        PlatformUser user1_group1 = createUser(admin, false, "user1_group1");
        setUserToUserGroup(group1, user1_group1);
        PlatformUser user2_group1 = createUser(admin, false, "user2_group1");
        setUserToUserGroup(group1, user2_group1);

        // when
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        Long result = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return Long.valueOf(dao.getUserCountForDefaultGroup(user
                        .getOrganization().getOrganizationId()));
            }
        });

        // then
        assertEquals(4, result.longValue());
    }

    @Test
    public void getAssignedUserIdsForGroup() throws Exception {
        // given
        PlatformUser user1_group2 = createUser(admin, false, "user1_group2");
        setUserToUserGroup(group2, user1_group2);
        PlatformUser user2_group2 = createUser(admin, false, "user2_group2");
        setUserToUserGroup(group2, user2_group2);

        // when
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        List<String> result = runTX(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return dao.getAssignedUserIdsForGroup(group2.getKey());
            }
        });

        // then
        assertEquals(3, result.size());
    }

    @Test
    public void getRoleAssignmentByUserAndGroup() throws Exception {
        // given
        final PlatformUser user1_group2 = createUser(admin, false,
                "user1_group2");
        setUnitAdmin(user1_group2, group2);

        // when
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        UnitRoleType result = runTX(new Callable<UnitRoleType>() {
            @Override
            public UnitRoleType call() throws Exception {
                return dao
                        .getRoleAssignmentByUserAndGroup(group2.getKey(),
                                user1_group2.getUserId()).getUnitUserRole()
                        .getRoleName();
            }
        });
        // then
        assertEquals(result, UnitRoleType.ADMINISTRATOR);
    }

    private Organization createOrg(final String organizationId,
            final OrganizationRoleType... roles) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds, organizationId,
                        roles);
            }
        });
    }

    private PlatformUser createUser(final Organization org,
            final boolean isAdmin, final String userId) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(ds, org, isAdmin, userId);
            }
        });
    }

    private UserGroup createUserGroup(final String name,
            final Organization org, final boolean isDefault,
            final String description, final String referenceId,
            final PlatformUser user) throws Exception {
        return runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws Exception {
                return UserGroups.createUserGroup(ds, name, org, isDefault,
                        description, referenceId, user);
            }
        });
    }

    private Product createProduct(final Organization org, final String productId)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                return Products.createProduct(org, "", false, productId,
                        "test", ds);
            }
        });
    }

    private void setInvisbleProductToUserGroup(final UserGroup userGroup,
            final Product product, final boolean invisibleForAllUsers)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UserGroupToInvisibleProduct grpToProd = new UserGroupToInvisibleProduct();
                grpToProd.setProduct(product);
                grpToProd.setUserGroup(userGroup);
                grpToProd.setForallusers(invisibleForAllUsers);
                ds.persist(grpToProd);
                return null;
            }
        });
    }

    private UserGroupToUser setUserToUserGroup(final UserGroup userGroup,
            final PlatformUser user) throws Exception {
        return runTX(new Callable<UserGroupToUser>() {
            @Override
            public UserGroupToUser call() throws Exception {
                UserGroupToUser grpToUser = new UserGroupToUser();
                grpToUser.setPlatformuser(user);
                grpToUser.setUserGroup(userGroup);
                ds.persist(grpToUser);
                return grpToUser;
            }
        });
    }

    private void prepareInvisibleProducts() throws Exception {
        Product prod1 = createProduct(admin, "prod1");
        Product prod2 = createProduct(admin, "prod2");
        Product prod3 = createProduct(admin, "prod3");
        Product prod4 = createProduct(admin, "prod4");
        Product prod5 = createProduct(admin, "prod5");
        Product prod6 = createProduct(admin, "prod6");

        UserGroup group3 = createUserGroup("group3", admin, false, "", "", user);
        UserGroup group4 = createUserGroup("group4", admin, false, "", "", user);
        UserGroup group5 = createUserGroup("group5", admin, false, "", "", user);

        // group1 is default group
        setInvisbleProductToUserGroup(group1, prod1, false);
        setInvisbleProductToUserGroup(group1, prod2, false);
        setInvisbleProductToUserGroup(group1, prod3, false);

        setInvisbleProductToUserGroup(group2, prod2, false);
        setInvisbleProductToUserGroup(group2, prod3, false);
        setInvisbleProductToUserGroup(group2, prod6, false);

        setInvisbleProductToUserGroup(group3, prod2, false);
        setInvisbleProductToUserGroup(group3, prod3, false);
        setInvisbleProductToUserGroup(group3, prod4, false);
        setInvisbleProductToUserGroup(group3, prod5, false);

        setInvisbleProductToUserGroup(group4, prod2, true);
        setInvisbleProductToUserGroup(group4, prod3, true);
        setInvisbleProductToUserGroup(group4, prod4, true);

        setInvisbleProductToUserGroup(group5, prod2, true);
        setInvisbleProductToUserGroup(group5, prod3, true);
        setInvisbleProductToUserGroup(group5, prod5, true);

        PlatformUser user1 = createUser(admin, false, "user1");

        setUserToUserGroup(group1, user1);
    }

    private void prepareInvisibleProductsOnlyWithDefaultGroup()
            throws Exception {
        Product prod1 = createProduct(admin, "prod1");
        Product prod2 = createProduct(admin, "prod2");
        Product prod3 = createProduct(admin, "prod3");
        Product prod4 = createProduct(admin, "prod4");
        Product prod5 = createProduct(admin, "prod5");

        // group1 is default group
        setInvisbleProductToUserGroup(defaultGroup, prod1, false);
        setInvisbleProductToUserGroup(defaultGroup, prod2, false);
        setInvisbleProductToUserGroup(defaultGroup, prod3, false);
        setInvisbleProductToUserGroup(defaultGroup, prod4, false);
        setInvisbleProductToUserGroup(defaultGroup, prod5, false);

    }

    @Test
    public void isNotTerminatedSubscriptionAssignedToUnitWithAssignedSubscription()
            throws Exception {
        // given
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                Product product = Products.createProduct(
                        supplier.getOrganizationId(), "product1",
                        "techProduct1", ds);
                Subscription sub1 = Subscriptions.createSubscription(ds,
                        supplier.getOrganizationId(), product);
                sub1.setUserGroup(group1);
                ds.persist(sub1);
                return null;
            }
        });
        // when
        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(dao
                        .isNotTerminatedSubscriptionAssignedToUnit(group1
                                .getKey()));
            }
        });
        // then
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void isNotTerminatedSubscriptionAssignedToUnitWithAssignedSubscriptions()
            throws Exception {
        // given
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                Product product = Products.createProduct(
                        supplier.getOrganizationId(), "product1",
                        "techProduct1", ds);

                Subscription sub1 = Subscriptions.createSubscription(ds,
                        supplier.getOrganizationId(), product);
                Subscription sub2 = Subscriptions.createSubscription(ds,
                        supplier.getOrganizationId(), product);
                sub1.setUserGroup(group1);
                sub2.setUserGroup(group1);
                ds.persist(sub1);
                ds.persist(sub2);

                return null;
            }
        });
        // when
        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(dao
                        .isNotTerminatedSubscriptionAssignedToUnit(group1
                                .getKey()));
            }
        });
        // then
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void isNotTerminatedSubscriptionAssignedToUnitWithoutAssignedSubscriptions()
            throws Exception {
        // given
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                Product product = Products.createProduct(
                        supplier.getOrganizationId(), "product1",
                        "techProduct1", ds);

                Subscription sub1 = Subscriptions.createSubscription(ds,
                        supplier.getOrganizationId(), product);
                Subscriptions.createSubscription(ds,
                        supplier.getOrganizationId(), product);
                sub1.setUserGroup(group2);
                ds.persist(sub1);

                return null;
            }
        });
        // when
        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(dao
                        .isNotTerminatedSubscriptionAssignedToUnit(group1
                                .getKey()));
            }
        });
        // then
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void getUserGroupAssignment() throws Exception {
        // given
        // when
        UserGroupToUser userGroupToUser = runTX(new Callable<UserGroupToUser>() {
            @Override
            public UserGroupToUser call() throws Exception {
                return dao.getUserGroupAssignment(group1, user);
            }
        });

        // then
        assertNotNull(userGroupToUser);
        assertEquals(user.getKey(), userGroupToUser.getPlatformuser_tkey());
        assertEquals(group1.getKey(), userGroupToUser.getUsergroup_tkey());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUserGroupAssignmentException() throws Exception {
        // given
        final UserGroup group = new UserGroup();
        final PlatformUser pUser = new PlatformUser();

        // when
        runTX(new Callable<UserGroupToUser>() {
            @Override
            public UserGroupToUser call() throws Exception {
                return dao.getUserGroupAssignment(group, pUser);
            }
        });

        // then exception
    }

    private String randomString() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }

}
