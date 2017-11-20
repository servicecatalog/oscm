/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-5                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserGroupToUser;
import org.oscm.domobjects.UserRole;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.paginator.Filter;
import org.oscm.paginator.Pagination;
import org.oscm.subscriptionservice.bean.SubscriptionListServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.ServiceProvisioningServiceStub;
import org.oscm.usergroupservice.auditlog.UserGroupAuditLogCollector;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.usergroupservice.dao.UserGroupDao;
import org.oscm.usergroupservice.dao.UserGroupUsersDao;

/**
 * Unit tests for {@link SubscriptionDao} using the test EJB container.
 * 
 * @author Mao
 */
public class SubscriptionDaoIT_KeyFiltering extends EJBTestBase {

    private static final String INSERT_UNIT_ROLE_SQL = "INSERT INTO unituserrole (tkey, version, rolename) VALUES (?, ?, ?)";
    private DataService ds;
    private UserGroupServiceLocalBean userGroupService;
    private SubscriptionDao dao;
    private Product product;
    private Organization supplier;
    private Organization customer;
    private Subscription subscription;
    private PlatformUser unitAdmin;
    private UserGroup unit;

    Set<SubscriptionStatus> SUB_STATES = Collections
            .unmodifiableSet(EnumSet.of(SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED));

    private static final Set<UserRoleType> UNIT_ADMINISTRATOR_ROLE = new HashSet<>(
            Arrays.asList(UserRoleType.UNIT_ADMINISTRATOR));

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new CommunicationServiceStub());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new AccountServiceStub());
        container.addBean(new ServiceProvisioningServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new UserGroupDao());
        container.addBean(new UserGroupUsersDao());
        container.addBean(new UserGroupAuditLogCollector());
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(new UserGroupServiceLocalBean());
        container.addBean(mock(IdentityService.class));
        ds = new DataServiceBean() {
            @Override
            public PlatformUser getCurrentUser() {
                return givenUserAdmin(1, "userId", customer);
            }
        };
        container.addBean(ds);
        dao = new SubscriptionDao(ds);
        userGroupService = container.get(UserGroupServiceLocalBean.class);
        userGroupService.setDm(ds);
        initUnitRoles();
        supplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        customer = registerCustomer("customer", supplier);
        unitAdmin = createUser(customer, false, "unitAdmin");
        unit = createUnit("Unit1", "", "", customer);
        assignUserToUnit(unitAdmin, unit, UnitRoleType.ADMINISTRATOR);
        product = createProduct("serviceB", "techServiceB",
                supplier.getOrganizationId(), ServiceAccessType.LOGIN);

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
    public void getSubscriptionsForUserWithRolesWithFiltering()
            throws Exception {
        // given
        subscription = createSubscription(customer.getOrganizationId(),
                product.getProductId(), "sub1", supplier, unit, unitAdmin);
        createSubscription(customer.getOrganizationId(), product.getProductId(),
                "sub2", supplier, unit, unitAdmin);

        final Pagination pagination = new Pagination();
        pagination.setFilterSet(new HashSet<Filter>());

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForUserWithRolesWithFiltering(
                        UNIT_ADMINISTRATOR_ROLE, unitAdmin, pagination,
                        SUB_STATES,
                        Arrays.asList(Long.valueOf(subscription.getKey())));
            }
        });

        // then
        assertEquals(1, result.size());
        assertEquals(subscription.getKey(), result.get(0).getKey());
    }

    private PlatformUser assignUserToUnit(final PlatformUser user,
            final UserGroup userGroup, final UnitRoleType... roleTypes)
            throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                UserGroupToUser userGroupToUser = new UserGroupToUser();
                userGroupToUser.setUserGroup(userGroup);
                userGroupToUser.setPlatformuser(user);
                ds.persist(userGroupToUser);
                ds.flush();
                userGroupService.grantUserRoles(user, Arrays.asList(roleTypes),
                        userGroup);
                return user;
            }
        });
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

    private Organization registerCustomer(final String customerId,
            final Organization vendor) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createCustomer(ds, vendor, customerId,
                        false);
            }
        });
    }

    private Product createProduct(final String productId,
            final String techProductId, final String organizationId,
            final ServiceAccessType type) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                return Products.createProduct(organizationId, productId,
                        techProductId, ds, type);
            }
        });
    }

    private Subscription createSubscription(final String customerId,
            final String productId, final String subscriptionId,
            final Organization supplier, final UserGroup unit,
            final PlatformUser owner) throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Subscription sub = Subscriptions.createSubscription(ds,
                        customerId, productId, subscriptionId, supplier);
                if (unit != null) {
                    sub = Subscriptions.assignToUnit(ds, sub, unit);
                }
                if (owner != null) {
                    sub.setOwner(owner);
                    ds.persist(sub);
                    ds.flush();
                }
                return sub;
            }
        });
    }

    private PlatformUser givenUserAdmin(long key, String id, Organization org) {
        return givenUser(key, id, org, UserRoleType.ORGANIZATION_ADMIN);
    }

    private PlatformUser givenUser(long key, String id, Organization org,
            UserRoleType roleType) {
        PlatformUser user = new PlatformUser();
        user.setKey(key);
        user.setUserId(id);
        user.setOrganization(org);
        RoleAssignment roleAssign = new RoleAssignment();
        roleAssign.setUser(user);
        roleAssign.setRole(new UserRole(roleType));
        user.getAssignedRoles().add(roleAssign);
        return user;
    }

    private PlatformUser createUser(final Organization organization,
            final boolean isAdmin, final String userId) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(ds, organization, isAdmin,
                        userId);
            }
        });
    }

    private UserGroup createUnit(final String name, final String description,
            final String referenceId, final Organization org) throws Exception {
        return runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws Exception {
                UserGroup userGroup = new UserGroup();
                userGroup.setName(name);
                userGroup.setDescription(description);
                userGroup.setReferenceId(referenceId);
                userGroup.setOrganization(org);
                ds.persist(userGroup);
                return userGroup;
            }
        });
    }
}
