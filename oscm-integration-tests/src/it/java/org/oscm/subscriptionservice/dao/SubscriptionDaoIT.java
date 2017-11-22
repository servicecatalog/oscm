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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
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
import org.oscm.paginator.Pagination;
import org.oscm.subscriptionservice.bean.SubscriptionListServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.Numbers;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
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
public class SubscriptionDaoIT extends EJBTestBase {

    private static final String COMMON_SUB_ID = "commonSubscription";
    private static final String INSERT_UNIT_ROLE_SQL = "INSERT INTO unituserrole (tkey, version, rolename) VALUES (?, ?, ?)";
    private DataService ds;
    private UserGroupServiceLocalBean userGroupService;
    private SubscriptionDao dao;
    private Product product;
    private Organization supplier;
    private Organization supplierCustomer;
    private Subscription subscription;
    private Subscription commonSubscription;

    Set<SubscriptionStatus> states = Collections
            .unmodifiableSet(EnumSet.of(SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED));
    private static final List<SubscriptionStatus> VISIBLE_SUBSCRIPTION_STATUS = Arrays
            .asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED,
                    SubscriptionStatus.PENDING, SubscriptionStatus.PENDING_UPD,
                    SubscriptionStatus.SUSPENDED,
                    SubscriptionStatus.SUSPENDED_UPD);

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new CommunicationServiceStub());
        container.addBean(new AccountServiceStub());
        container.addBean(new ServiceProvisioningServiceStub());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new UserGroupDao());
        container.addBean(new UserGroupUsersDao());
        container.addBean(new UserGroupAuditLogCollector());
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(new UserGroupServiceLocalBean());
        container.addBean(mock(IdentityService.class));

        final Organization org = new Organization();
        org.setKey(0);

        ds = new DataServiceBean() {
            @Override
            public PlatformUser getCurrentUser() {
                return givenUserAdmin(1, "userId", supplierCustomer);
            }
        };
        container.addBean(ds);
        dao = new SubscriptionDao(ds);
        userGroupService = container.get(UserGroupServiceLocalBean.class);
        userGroupService.setDm(ds);
        supplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        supplierCustomer = registerCustomer("supplierCustomer", supplier);

        product = createProduct("serviceB", "techServiceB",
                supplier.getOrganizationId(), ServiceAccessType.LOGIN);

        subscription = createSubscription(supplierCustomer.getOrganizationId(),
                product.getProductId(), "sub1", supplier);

        final Organization broker = createOrg("broker",
                OrganizationRoleType.BROKER);
        final Organization brokerCustomer1 = registerCustomer("brokerCustomer1",
                broker);
        registerCustomer("brokerCustomer2", broker);
        Product partnerProduct = createPartnerProduct(product, broker);
        createPartnerSubscription(brokerCustomer1.getOrganizationId(),
                partnerProduct, "brokercustomer1Sub", broker);

        final Organization reseller = createOrg("reseller",
                OrganizationRoleType.RESELLER);
        final Organization resellerCustomer1 = registerCustomer(
                "resellerCustomer1", reseller);
        registerCustomer("resellerCustomer2", reseller);
        Product partnerProductReseller = createPartnerProduct(product,
                reseller);
        createPartnerSubscription(resellerCustomer1.getOrganizationId(),
                partnerProductReseller, "resellercustomer1Sub", reseller);
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
    public void getActiveSubscriptions() throws Exception {
        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getActiveSubscriptions();
            }
        });

        // then
        assertEquals(3, result.size());
    }

    @Test
    public void getSubscriptionsWithRoles() throws Exception {

        // when
        List<Object[]> result = runTX(new Callable<List<Object[]>>() {
            @Override
            public List<Object[]> call() throws Exception {
                return dao.getSubscriptionsWithRoles(supplierCustomer, states);
            }
        });

        // then
        assertEquals(1, result.size());
    }

    @Test(expected = NoResultException.class)
    public void findSubscriptionForAsyncCallBack() throws Exception {
        // when
        Long result = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return dao.findSubscriptionForAsyncCallBack(
                        subscription.getSubscriptionId(),
                        supplierCustomer.getOrganizationId());
            }
        });

        // then
        assertEquals(0, result.longValue());
    }

    @Test
    public void getNumberOfVisibleSubscriptions() throws Exception {

        // when
        Long result = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return dao.getNumberOfVisibleSubscriptions(
                        product.getTechnicalProduct(), supplierCustomer);
            }
        });

        // then
        assertEquals(1, result.longValue());
    }

    @Test
    public void getSubscriptionIdsForMyCustomers() throws Exception {
        // when
        List<String> result = runTX(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return dao.getSubscriptionIdsForMyCustomers(supplier, states);
            }
        });

        // then
        assertEquals(1, result.size());
    }

    @Test
    public void getSubscriptionsForMyCustomers() throws Exception {
        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyCustomers(supplier, states);
            }
        });

        // then
        assertEquals(1, result.size());
    }

    @Test
    public void getSubscriptionsForMyBrokerCustomers() throws Exception {
        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForMyBrokerCustomers(supplier);
            }
        });

        // then
        assertEquals(1, result.size());
    }

    @Test
    public void getSubscriptionsByStatus() throws Exception {
        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsByStatus(SubscriptionStatus.ACTIVE);
            }
        });

        // then
        assertEquals(3, result.size());
    }

    @Test
    public void hasCurrentUserSubscriptions() throws Exception {
        // when
        Long result = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return dao.hasCurrentUserSubscriptions(Long.valueOf(0),
                        VISIBLE_SUBSCRIPTION_STATUS);
            }
        });

        // then
        assertEquals(0, result.longValue());
    }

    @Test
    public void getSubscriptionsForUser() throws Exception {
        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForUser(ds.getCurrentUser());
            }
        });

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void getSubscriptionsForUserWithKeysEmptySet() throws Exception {
        // when
        final Set<Long> keys = Collections.emptySet();
        final PlatformUser user = createUser(randomString(), supplierCustomer);
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForUser(user,
                        mock(org.oscm.paginator.Pagination.class));
            }
        });

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void getSubscriptionsForUserWithKeys() throws Exception {
        final Product subscription = createSubscription();
        final PlatformUser user = createUser(randomString(), supplierCustomer);
        final Subscription[] subscription1 = new Subscription[1];
        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subscription1[0] = Subscriptions.createSubscription(ds,
                        supplierCustomer.getOrganizationId(), subscription);
                Subscriptions.createUsageLicense(ds, user, subscription1[0]);
                return null;
            }
        });
        final Set<Long> keys = Collections.singleton(subscription1[0].getKey());
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForUserWithSubscriptionKeys(user,
                        mock(org.oscm.paginator.Pagination.class), keys);
            }
        });

        // then
        assertEquals(1, result.size());
    }

    @Test
    public void hasSubscriptionsBasedOnOnBehalfServicesForTp()
            throws Exception {
        createSubscription();
        // when
        Long result = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return dao.hasSubscriptionsBasedOnOnBehalfServicesForTp(
                        commonSubscription);
            }
        });

        // then
        assertEquals(0, result.longValue());
    }

    @Test
    public void getSubscriptionsForOwner() throws Exception {
        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForOwner(ds.getCurrentUser());
            }
        });

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void checkIfProductInstanceIdExists_false() throws Exception {
        // given
        final String productInstanceId = "-125";
        final TechnicalProduct techProduct = createTechnicalProduct("-123",
                ServiceAccessType.LOGIN);
        // when
        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(dao.checkIfProductInstanceIdExists(
                        productInstanceId, techProduct));
            }
        });
        // then
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void checkIfProductInstanceIdExists_true() throws Exception {
        // when
        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(dao.checkIfProductInstanceIdExists(
                        subscription.getProductInstanceId(),
                        subscription.getProduct().getTechnicalProduct()));
            }
        });
        // then
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void getSubscriptionsForUserWithRoles_isEmpty() throws Exception {
        // given
        final PlatformUser user = createUser(randomString(), supplierCustomer);
        final UserGroup unit = createUnit(randomString(), "", "",
                supplierCustomer);
        assignUserToUnit(user, unit, UnitRoleType.ADMINISTRATOR);
        final Set<UserRoleType> userRoleTypes = new HashSet<>();
        userRoleTypes.add(UserRoleType.UNIT_ADMINISTRATOR);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForUserWithRoles(userRoleTypes, user,
                        givenPagination(),
                        Collections.singleton(SubscriptionStatus.ACTIVE));
            }
        });

        // then
        assertEquals(result, Collections.EMPTY_LIST);
    }

    @Test
    public void getSubscriptionsForUserWithRoles_SubscriptionAsUnitAdmin()
            throws Exception {
        // given
        final PlatformUser user = createUser(randomString(), supplierCustomer);
        final UserGroup unit = createUnit(randomString(), "", "",
                supplierCustomer);
        assignUserToUnit(user, unit, UnitRoleType.ADMINISTRATOR);
        final Set<UserRoleType> userRoleTypes = new HashSet<>();
        userRoleTypes.add(UserRoleType.SUBSCRIPTION_MANAGER);
        userRoleTypes.add(UserRoleType.UNIT_ADMINISTRATOR);
        assignUnitToSubscription(unit);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForUserWithRoles(userRoleTypes, user,
                        givenPagination(),
                        Collections.singleton(SubscriptionStatus.ACTIVE));
            }
        });

        // then
        assertEquals(result, Collections.singletonList(subscription));
    }

    @Test
    public void getSubscriptionsForUserWithRolesUnitAdminAndOwner()
            throws Exception {
        // given
        final PlatformUser user = createUser(randomString(), supplierCustomer);
        Subscription sub4UnitAdmin = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.createSubscriptionWithOwner(ds,
                        supplierCustomer.getOrganizationId(),
                        product.getProductId(), "sub4UnitAdmin", null,
                        Numbers.TIMESTAMP, Numbers.TIMESTAMP, supplier, null, 1,
                        user);
            }
        });

        final UserGroup unit = createUnit(randomString(), "", "",
                supplierCustomer);
        assignUserToUnit(user, unit, UnitRoleType.ADMINISTRATOR);
        final Set<UserRoleType> userRoleTypes = new HashSet<>();
        userRoleTypes.add(UserRoleType.UNIT_ADMINISTRATOR);

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForUserWithRoles(userRoleTypes, user,
                        givenPagination(),
                        Collections.singleton(SubscriptionStatus.ACTIVE));
            }
        });

        // then
        assertEquals(result, Collections.singletonList(sub4UnitAdmin));
    }

    private void assignUnitToSubscription(final UserGroup unit)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subscription.setUserGroup(unit);
                ds.merge(subscription);
                return null;
            }
        });
    }

    private Pagination givenPagination() {
        Pagination pagination = new Pagination();
        pagination.setFilterSet(Collections.EMPTY_SET);
        return pagination;
    }

    private org.oscm.paginator.Pagination givenNewPagination() {
        org.oscm.paginator.Pagination pagination = new org.oscm.paginator.Pagination();
        pagination.setFilterSet(Collections.EMPTY_SET);
        return pagination;
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

    private PlatformUser createUser(final String id, final Organization org)
            throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser user = Organizations.createUserForOrg(ds, org,
                        true, id);
                ds.persist(user);
                return user;
            }
        });
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

    private String randomString() {
        return new BigInteger(130, new SecureRandom()).toString(32);
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

    private Product createPartnerProduct(final Product productTemplate,
            final Organization partner) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product resaleCopy = productTemplate.copyForResale(partner);
                ds.persist(resaleCopy);
                return resaleCopy;
            }
        });
    }

    private Subscription createSubscription(final String customerId,
            final String productId, final String subscriptionId,
            final Organization supplier) throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.createSubscription(ds, customerId,
                        productId, subscriptionId, supplier);
            }
        });
    }

    private Subscription createPartnerSubscription(final String customerId,
            final Product product, final String subscriptionId,
            final Organization partner) throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.createPartnerSubscription(ds, customerId,
                        product.getProductId(), subscriptionId, partner);
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

    private TechnicalProduct createTechnicalProduct(final String serviceId,
            final ServiceAccessType accessType) throws Exception {
        return runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                return TechnicalProducts.createTechnicalProduct(ds, supplier,
                        serviceId, false, accessType);
            }
        });
    }

    private Product createSubscription() throws Exception {
        TechnicalProduct tProduct = createTechnicalProduct("serviceId",
                ServiceAccessType.LOGIN);
        return createSubscriptionForOfferer(tProduct, supplier,
                supplierCustomer, "supplierCommonProduct", COMMON_SUB_ID);
    }

    private Product createSubscriptionForOfferer(
            final TechnicalProduct tProduct, final Organization offerer,
            final Organization customer, final String productId,
            final String subscriptionId) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product product = Products.createProduct(offerer, tProduct,
                        false, productId, null, ds);
                commonSubscription = Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), product.getProductId(),
                        subscriptionId, offerer);
                return product;
            }
        });
    }

    Set<String> getSubscriptionStatesAsString(Set<SubscriptionStatus> states) {
        Set<String> statesAsString = new HashSet<>();
        for (SubscriptionStatus s : states) {
            statesAsString.add(s.name());
        }
        return statesAsString;
    }
}
