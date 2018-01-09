/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                  
 *  Creation Date: 24.07.15 13:52
 *
 *******************************************************************************/

package org.oscm.usergroupservice.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.UnitRoleAssignment;
import org.oscm.domobjects.UnitUserRole;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserGroupToInvisibleProduct;
import org.oscm.domobjects.UserGroupToUser;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.paginator.Pagination;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ServiceProvisioningServiceStub;
import org.oscm.usergroupservice.auditlog.UserGroupAuditLogCollector;
import org.oscm.usergroupservice.dao.UserGroupDao;
import org.oscm.usergroupservice.dao.UserGroupUsersDao;

public class UserGroupServiceLocalBeanIT extends EJBTestBase {

    protected DataService mgr;
    UserGroupServiceLocalBean localService;
    UserGroupDao localDao;

    private String userKey;
    private String userKey_2;

    private static final String INSERT_UNIT_ROLE_SQL = "INSERT INTO unituserrole (tkey, version, rolename) VALUES (?, ?, ?)";
    private Product product;
    private Marketplace marketplaceOne;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new CommunicationServiceStub());
        container.addBean(new AccountServiceStub());
        container.addBean(new ServiceProvisioningServiceStub());
        container.addBean(new DataServiceBean());

        container.addBean(new LocalizerServiceBean());
        container.addBean(new UserGroupDao());
        container.addBean(new UserGroupUsersDao());
        container.addBean(new UserGroupAuditLogCollector());
        container.addBean(mock(SubscriptionListServiceLocal.class));
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(mock(IdentityService.class));
        container.addBean(mock(TaskQueueServiceLocal.class));
        container.addBean(new UserGroupServiceLocalBean());
        container.addBean(Mockito.mock(SubscriptionListServiceLocal.class));

        mgr = container.get(DataService.class);

        localService = container.get(UserGroupServiceLocalBean.class);
        localDao = container.get(UserGroupDao.class);

        container.login("setup", ROLE_ORGANIZATION_ADMIN);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createPaymentTypes(mgr);
                Organization org = Organizations.createOrganization(mgr);
                Organization org2 = Organizations.createOrganization(mgr);
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        false, "user");
                userKey = String.valueOf(user.getKey());

                user = Organizations
                        .createUserForOrg(mgr, org2, false, "user_2");
                userKey_2 = String.valueOf(user.getKey());

                insertUnitRole(Long.valueOf(1L), Long.valueOf(0L),
                        "ADMINISTRATOR");
                insertUnitRole(Long.valueOf(2L), Long.valueOf(0L), "USER");

                product = Products.createProduct(org, "", false, "prodId",
                        "test", mgr);

                marketplaceOne = Marketplaces.createMarketplace(org, "marketplaceOne", true, mgr);
                org.getMarketplaces().add(marketplaceOne);
                return null;
            }
        });
    }

    private void insertUnitRole(Long key, Long version, String roleName) {
        Query query = mgr.createNativeQuery(INSERT_UNIT_ROLE_SQL);
        query.setParameter(1, key);
        query.setParameter(2, version);
        query.setParameter(3, roleName);

        query.executeUpdate();
    }

    @Test
    public void createUserGroup()
            throws Exception {
        // given
        container.login(userKey);

        // when
        UserGroup createdGroup = createGroups(1).get(0);

        // then
        List<UserGroup> expectedGroups = runTX(new Callable<List<UserGroup>>() {
            @Override
            public List<UserGroup> call() throws Exception {
                return localService.getOrganizationalUnits(null);
            }
        });

        Assert.assertEquals(1, expectedGroups.size());

        UserGroup expectedGroup = expectedGroups.get(0);
        Assert.assertEquals(expectedGroup.getName(), createdGroup.getName());
        Assert.assertEquals(expectedGroup.getDescription(),
                createdGroup.getDescription());
        Assert.assertEquals(expectedGroup.getReferenceId(),
                createdGroup.getReferenceId());

    }

    @Test
    public void getOrganizationalUnits()
            throws Exception {
        // given
        container.login(userKey);

        List<UserGroup> createdGroups = createGroups(10);
        // when
        List<UserGroup> groups = runTX(new Callable<List<UserGroup>>() {
            @Override
            public List<UserGroup> call() throws Exception {
                return localService.getOrganizationalUnits(null);
            }
        });

        // then
        Assert.assertEquals(createdGroups.size(), groups.size());
    }

    @Test
    public void getOrganizationalUnitsWithPaging() throws Exception {
        // given
        container.login(userKey);

        createGroups(10);
        final Pagination pagination = new Pagination(0, 5);
        // when
        List<UserGroup> groups = runTX(new Callable<List<UserGroup>>() {
            @Override
            public List<UserGroup> call() throws Exception {
                return localService.getOrganizationalUnits(pagination);
            }
        });

        Assert.assertEquals(5, groups.size());
    }

    @Test
    public void grantUserRolesNew() throws Exception {
        // given
        container.login(userKey);

        final UserGroup userGroup = createGroups(1).get(0);
        final PlatformUser user = findUser(userKey);
        final UnitRoleType roleType = UnitRoleType.ADMINISTRATOR;

        // when
        assignUsersToGroup(userGroup, Collections.singletonList(user));
        grantUserRoles(userGroup, user, Collections.singletonList(roleType));

        List<UnitRoleType> roleTypes = runTX(
                new Callable<List<UnitRoleType>>() {
                    @Override
                    public List<UnitRoleType> call() throws Exception {
                        List<UnitRoleType> assignments = new ArrayList<>();
                        UnitRoleAssignment assignment = (UnitRoleAssignment) mgr
                                .getReferenceByBusinessKey(
                                        getRoleAssignment(roleType,
                                                userGroup, user));

                        assignments.add(assignment.getUnitUserRole()
                                .getRoleName());
                        return assignments;
                    }
                });

        // then
        Assert.assertEquals(1, roleTypes.size());
        Assert.assertEquals(UnitRoleType.ADMINISTRATOR, roleTypes.get(0));
    }

    @Test
    public void grantUserRolesExisting() throws Exception {
        // given
        container.login(userKey);

        final UserGroup userGroup = createGroups(1).get(0);
        final PlatformUser user = findUser(userKey);
        final UnitRoleType roleType = UnitRoleType.ADMINISTRATOR;

        // when
        assignUsersToGroup(userGroup, Collections.singletonList(user));

        // grant twice same role in order to check if there will be no exception
        grantUserRoles(userGroup, user, Collections.singletonList(roleType));
        grantUserRoles(userGroup, user, Collections.singletonList(roleType));

        List<UnitRoleType> roleTypes = getRoleTypes(userGroup, roleType, user);

        // then
        Assert.assertEquals(1, roleTypes.size());
        Assert.assertEquals(UnitRoleType.ADMINISTRATOR, roleTypes.get(0));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void grantUserRolesException() throws Exception {
        // given
        container.login(userKey);

        final UserGroup userGroup = createGroups(1).get(0);
        final PlatformUser user = new PlatformUser();
        final UnitRoleType roleType = UnitRoleType.ADMINISTRATOR;

        user.setKey(0); // some not existing user key

        // when
        assignUsersToGroup(userGroup, Collections.singletonList(user));
        grantUserRoles(userGroup, user, Collections.singletonList(roleType));

        // then exception
    }

    @Test
    public void revokeUserRoles() throws Exception {
        // given
        container.login(userKey);

        final UserGroup userGroup = createGroups(1).get(0);
        final PlatformUser user = findUser(userKey);
        final UnitRoleType roleType = UnitRoleType.ADMINISTRATOR;

        // when
        assignUsersToGroup(userGroup, Collections.singletonList(user));
        grantUserRoles(userGroup, user, Collections.singletonList(roleType));
        revokeUserRoles(userGroup, user, Collections.singletonList(roleType));

        // then
        List<UnitRoleType> roleTypes = getRoleTypes(userGroup, roleType, user);

        Assert.assertEquals(0, roleTypes.size());

    }

    @Test(expected = ObjectNotFoundException.class)
    public void revokeUserRolesException() throws Exception {
        // given
        container.login(userKey);

        final UserGroup userGroup = createGroups(1).get(0);
        final PlatformUser user = new PlatformUser();
        final UnitRoleType roleType = UnitRoleType.ADMINISTRATOR;

        user.setKey(0); // some not existing user key

        // when
        assignUsersToGroup(userGroup, Collections.singletonList(user));
        revokeUserRoles(userGroup, user, Collections.singletonList(roleType));
    }

    @Test
    public void deleteUserGroup() throws Exception {
        // given
        container.login(userKey);

        final UserGroup userGroup = createGroups(1).get(0);

        // when
        deleteGroup(userGroup.getName());

        // then
        List<UserGroup> groups = getOrganizationalUnits(null);
        Assert.assertEquals(0, groups.size());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteUserGroupNotExists() throws Exception {
        // given
        container.login(userKey);

        final UserGroup userGroup = new UserGroup();
        userGroup.setName(randomString());

        // when
        deleteGroup(userGroup.getName());

        // then exception
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteUserGroupWrongOrganization() throws Exception {
        // given
        container.login(userKey);

        final UserGroup userGroup = createGroups(1).get(0);

        // when
        container.login(userKey_2);
        deleteGroup(userGroup.getName());

        // then exception
    }

    private List<UserGroup> createGroups(int number) throws Exception {
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        final List<UserGroup> groups = new ArrayList<>();
        while (number-- > 0) {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    UserGroup userGroup = localService.createUserGroup(
                            randomString(), randomString(),
                            randomString());

                    groups.add(userGroup);
                    return null;
                }
            });
        }

        return groups;
    }

    @Test
    public void createGroupsWithNullRefIdAndDesc() throws Exception {
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        final UserGroup[] group = new UserGroup[1];
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                group[0] = localService.createUserGroup(
                        randomString(), randomString(),
                        randomString());

                return null;
            }
        });
        assertNotNull(group[0]);
        assertFalse(group[0].getKey() == 0);
    }

    private String randomString() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }

    private UnitUserRole getUnitUserRole(UnitRoleType type) {
        UnitUserRole userRole = new UnitUserRole();

        userRole.setRoleName(type);
        userRole.setKey(type.getKey());

        return userRole;
    }

    private UnitRoleAssignment getRoleAssignment(UnitRoleType roleType,
                                                 UserGroup userGroup, PlatformUser user)
            throws ObjectNotFoundException {
        UnitRoleAssignment assignment = new UnitRoleAssignment();
        UserGroupToUser userGroupToUser = localDao.getUserGroupAssignment(
                userGroup, user);

        assignment.setUnitUserRole(getUnitUserRole(roleType));
        assignment.setUserGroupToUser(userGroupToUser);

        return assignment;
    }

    private void assignUsersToGroup(final UserGroup group,
                                    final List<PlatformUser> users) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localService.assignUsersToGroup(group, users);
                return null;
            }
        });
    }

    private void grantUserRoles(final UserGroup userGroup,
                                final PlatformUser user, final List<UnitRoleType> roleTypes)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localService.grantUserRoles(user, roleTypes, userGroup);
                return null;
            }
        });
    }

    private PlatformUser findUser(final String userKey) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getReference(PlatformUser.class, Long.parseLong(userKey));
            }
        });
    }

    private void revokeUserRoles(final UserGroup userGroup, final PlatformUser user,
                                 final List<UnitRoleType> roleTypes) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localService.revokeUserRoles(user, roleTypes, userGroup);
                return null;
            }
        });
    }

    private List<UnitRoleType> getRoleTypes(final UserGroup userGroup,
                                            final UnitRoleType roleType, final PlatformUser user)
            throws Exception {
        return runTX(new Callable<List<UnitRoleType>>() {
            @Override
            public List<UnitRoleType> call() throws Exception {
                List<UnitRoleType> assignments = new ArrayList<>();

                try {
                    UnitRoleAssignment assignment = (UnitRoleAssignment) mgr
                            .getReferenceByBusinessKey(getRoleAssignment(
                                    roleType, userGroup, user));

                    assignments.add(assignment.getUnitUserRole().getRoleName());
                } catch (ObjectNotFoundException ignored) {
                }

                return assignments;
            }
        });
    }

    private List<UserGroup> getOrganizationalUnits(final Pagination pagination) throws Exception {
        return runTX(new Callable<List<UserGroup>>() {
            @Override
            public List<UserGroup> call() throws Exception {
                return localService.getOrganizationalUnits(pagination);
            }
        });
    }

    private void deleteGroup(final String name) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localService.deleteUserGroup(name);
                return null;
            }
        });
    }

    @Test
    public void getUserGroupsForUserWithRole() throws Exception {
        // given
        final UserGroup userGroup = createGroups(1).get(0);
        final PlatformUser user = findUser(userKey);
        final UnitRoleType roleType = UnitRoleType.ADMINISTRATOR;

        assignUsersToGroup(userGroup, Collections.singletonList(user));
        grantUserRoles(userGroup, user, Collections.singletonList(roleType));

        // when
        List<UserGroup> userGroups = runTX(new Callable<List<UserGroup>>() {
            @Override
            public List<UserGroup> call() throws Exception {
                return localDao.getUserGroupsForUserWithRole(user.getKey(),
                        roleType.getKey());
            }
        });

        // then
        Assert.assertEquals(1, userGroups.size());
        Assert.assertEquals(userGroup, userGroups.get(0));

    }

    @Test
    public void getAccessibleServices() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                //given
                String marketplaceId = String.valueOf(marketplaceOne.getKey());
                UserGroup userGroup = createGroups(1).get(0);
                CatalogEntry catalogEntry = new CatalogEntry();
                catalogEntry.setProduct(product);
                catalogEntry.setMarketplace(marketplaceOne);
                mgr.merge(catalogEntry);
                final String unitId = String.valueOf(userGroup.getKey());
                mgr.merge(userGroup);
                //when
                final List<Product> accessibleServices = localDao.getAccessibleServices(unitId, new Pagination(0, 0), marketplaceId);
                //then
                boolean result = false;
                for (Product accessibleService : accessibleServices) {
                    if (accessibleService.getKey() == product.getKey()){
                        result = true;
                    }
                }
                assertTrue(result);
                return null;
            }
        });
    }

    @Test
    public void getVisibleServices() throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                //given
                String marketplaceId = String.valueOf(marketplaceOne.getKey());
                UserGroup userGroup = createGroups(1).get(0);
                CatalogEntry catalogEntry = new CatalogEntry();
                catalogEntry.setProduct(product);
                catalogEntry.setMarketplace(marketplaceOne);
                mgr.merge(catalogEntry);
                final String unitId = String.valueOf(userGroup.getKey());
                UserGroupToInvisibleProduct ug2ip = new UserGroupToInvisibleProduct();
                userGroup = (UserGroup) mgr.merge(userGroup);
                ug2ip.setProduct(product);
                ug2ip.setUserGroup(userGroup);
                ug2ip.setForallusers(false);
                ug2ip = (UserGroupToInvisibleProduct) mgr.merge(ug2ip);
                List<UserGroupToInvisibleProduct> ug2ipList = new ArrayList<>();
                ug2ipList.add(ug2ip);
                userGroup.setUserGroupToInvisibleProducts(ug2ipList);
                mgr.merge(userGroup);
                //when
                final List<Product> visibleServices = localDao.getVisibleServices(unitId, new Pagination(0, 0), marketplaceId);
                //then
                boolean result = false;
                for (Product visibleService : visibleServices) {
                    if (visibleService.getKey() == product.getKey()){
                        result = true;
                    }
                }
                assertTrue(result);
                return null;
            }
        });
    }

    @Test
    public void addAccessibleServices() throws Exception {
        final UserGroup userGroup = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws Exception {
                //given
                UserGroup userGroup = createGroups(1).get(0);
                final String unitId = String.valueOf(userGroup.getKey());
                UserGroupToInvisibleProduct ug2ip = new UserGroupToInvisibleProduct();
                userGroup = (UserGroup) mgr.merge(userGroup);
                ug2ip.setProduct(product);
                ug2ip.setUserGroup(userGroup);
                ug2ip.setForallusers(true);
                ug2ip = (UserGroupToInvisibleProduct) mgr.merge(ug2ip);
                List<UserGroupToInvisibleProduct> ug2ipList = new ArrayList<>();
                ug2ipList.add(ug2ip);
                userGroup.setUserGroupToInvisibleProducts(ug2ipList);
                userGroup = (UserGroup) mgr.merge(userGroup);
                //when
                localService.addAccessibleServices(unitId, Collections.singletonList(Long.toString(product.getKey())));
                return userGroup;
            }
        });
        //then
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UserGroup ugFromDB = mgr.find(UserGroup.class, userGroup.getKey());
                final List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = ugFromDB.getUserGroupToInvisibleProducts();
                boolean result = true;
                for (UserGroupToInvisibleProduct userGroupToInvisibleProduct : userGroupToInvisibleProducts) {
                    if (userGroupToInvisibleProduct.getProduct().getKey() == product.getKey()) {
                        result = false;
                    }
                }
                assertTrue(result);
                return null;
            }
        });
    }

    @Test
    public void revokeAccessibleServices() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                //given
                String marketplaceId = String.valueOf(marketplaceOne.getKey());
                UserGroup userGroup = createGroups(1).get(0);
                CatalogEntry catalogEntry = new CatalogEntry();
                catalogEntry.setProduct(product);
                catalogEntry.setMarketplace(marketplaceOne);
                mgr.merge(catalogEntry);
                final String unitId = String.valueOf(userGroup.getKey());
                mgr.merge(userGroup);
                //when
                List<Product> accessibleServices = localDao.getAccessibleServices(unitId, new Pagination(0, 0), marketplaceId);
                boolean result1 = false;
                for (Product accessibleService : accessibleServices) {
                    if (accessibleService.getKey() == product.getKey()){
                        result1 = true;
                    }
                }
                localService.revokeAccessibleServices(unitId, Arrays.asList(String.valueOf(product.getKey())));
                boolean result2 = true;
                List<Product> accessibleServicesAfter = localDao.getAccessibleServices(unitId, new Pagination(0, 0), marketplaceId);
                for (Product accessibleService : accessibleServicesAfter) {
                    if (accessibleService.getKey() == product.getKey()){
                        result2 = false;
                    }
                }
                //then
                assertTrue(result1);
                assertTrue(result2);
                return null;
            }
        });
    }

    @Test
    public void addVisibleServices() throws Exception {
        final UserGroup userGroup = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws Exception {
                //given
                UserGroup userGroup = createGroups(1).get(0);
                final String unitId = String.valueOf(userGroup.getKey());
                UserGroupToInvisibleProduct ug2ip = new UserGroupToInvisibleProduct();
                userGroup = (UserGroup) mgr.merge(userGroup);
                ug2ip.setProduct(product);
                ug2ip.setUserGroup(userGroup);
                ug2ip.setForallusers(true);
                ug2ip = (UserGroupToInvisibleProduct) mgr.merge(ug2ip);
                List<UserGroupToInvisibleProduct> ug2ipList = new ArrayList<>();
                ug2ipList.add(ug2ip);
                userGroup.setUserGroupToInvisibleProducts(ug2ipList);
                userGroup = (UserGroup) mgr.merge(userGroup);
                //when
                localService.addVisibleServices(unitId, Collections.singletonList(Long.toString(product.getKey())));
                return userGroup;
            }
        });
        //then
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UserGroup ugFromDB = mgr.find(UserGroup.class, userGroup.getKey());
                final List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = ugFromDB.getUserGroupToInvisibleProducts();
                for (UserGroupToInvisibleProduct userGroupToInvisibleProduct : userGroupToInvisibleProducts) {
                    assertFalse(userGroupToInvisibleProduct.isForallusers());
                }
                return null;
            }
        });
    }

    @Test
    public void revokeVisibleServices() throws Exception {
        final UserGroup userGroup = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws Exception {
                //given
                UserGroup userGroup = createGroups(1).get(0);
                final String unitId = String.valueOf(userGroup.getKey());
                UserGroupToInvisibleProduct ug2ip = new UserGroupToInvisibleProduct();
                userGroup = (UserGroup) mgr.merge(userGroup);
                ug2ip.setProduct(product);
                ug2ip.setUserGroup(userGroup);
                ug2ip.setForallusers(false);
                ug2ip = (UserGroupToInvisibleProduct) mgr.merge(ug2ip);
                List<UserGroupToInvisibleProduct> ug2ipList = new ArrayList<>();
                ug2ipList.add(ug2ip);
                userGroup.setUserGroupToInvisibleProducts(ug2ipList);
                userGroup = (UserGroup) mgr.merge(userGroup);
                //when
                localService.revokeVisibleServices(unitId, Collections.singletonList(Long.toString(product.getKey())));
                return userGroup;
            }
        });
        //then
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UserGroup ugFromDB = mgr.find(UserGroup.class, userGroup.getKey());
                final List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = ugFromDB.getUserGroupToInvisibleProducts();
                for (UserGroupToInvisibleProduct userGroupToInvisibleProduct : userGroupToInvisibleProducts) {
                    assertTrue(userGroupToInvisibleProduct.isForallusers());
                }
                return null;
            }
        });
    }

}
