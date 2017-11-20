/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2016-05-18                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.math.BigInteger;
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
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserRole;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.paginator.Filter;
import org.oscm.paginator.Pagination;
import org.oscm.subscriptionservice.bean.SubscriptionListServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.Udas;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.ServiceProvisioningServiceStub;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.usergroupservice.auditlog.UserGroupAuditLogCollector;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.usergroupservice.dao.UserGroupDao;
import org.oscm.usergroupservice.dao.UserGroupUsersDao;

/**
 * Unit tests for {@link SubscriptionDao} using the test EJB container.
 * 
 * @author stavreva
 */
public class SubscriptionDaoCustomerIT extends EJBTestBase {

    private static final String INSERT_UNIT_ROLE_SQL = "INSERT INTO unituserrole (tkey, version, rolename) VALUES (?, ?, ?)";
    private DataService ds;
    private UserGroupServiceLocalBean userGroupService;
    private SubscriptionDao dao;
    private Product product;
    private Organization supplier1;
    private Organization customer1;
    private Organization customer2;
    private PlatformUser supplierAdmin1;
    private PlatformUser customerAdmin1;

    Set<SubscriptionStatus> SUB_STATES = Collections
            .unmodifiableSet(EnumSet.of(SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED));

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

        ds = new DataServiceBean() {
            @Override
            public PlatformUser getCurrentUser() {
                return givenUserAdmin(1, "userId", customer1);
            }
        };
        container.addBean(ds);
        dao = new SubscriptionDao(ds);
        userGroupService = container.get(UserGroupServiceLocalBean.class);
        userGroupService.setDm(ds);
        initUnitRoles();

        supplier1 = createOrg("supplier1", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        supplierAdmin1 = createUser(supplier1, true, "supplierAdmin1");

        customer1 = registerCustomer("customer1", supplier1);
        customerAdmin1 = createUser(customer1, true, "customerAdmin1");

        customer2 = registerCustomer("customer2", supplier1);

        product = createProduct("serviceB", "techServiceB",
                supplier1.getOrganizationId(), ServiceAccessType.LOGIN);

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
    public void getSubscriptionsCustomerSupplier() throws Exception {
        // given
        createSubscription(customer1.getOrganizationId(),
                product.getProductId(), "sub1", supplier1, null, null);
        createSubscription(customer1.getOrganizationId(),
                product.getProductId(), "sub2", supplier1, null, null);
        createSubscription(supplier1.getOrganizationId(),
                product.getProductId(), "sub3", supplier1, null, null);

        final Pagination pagination = new Pagination();
        pagination.setFilterSet(new HashSet<Filter>());

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForOrg(supplierAdmin1, pagination,
                        SUB_STATES);
            }
        });

        // then
        assertEquals(1, result.size());
        assertEquals("sub3", result.get(0).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsWithFilteringCustomerSupplier()
            throws Exception {
        // given
        final long key1 = createSubscription(customer1.getOrganizationId(),
                product.getProductId(), "sub1", supplier1, null, null).getKey();
        createSubscription(customer1.getOrganizationId(),
                product.getProductId(), "sub2", supplier1, null, null).getKey();
        createSubscription(supplier1.getOrganizationId(),
                product.getProductId(), "sub3", supplier1, null, null).getKey();
        final long key4 = createSubscription(supplier1.getOrganizationId(),
                product.getProductId(), "sub4", supplier1, null, null).getKey();

        final Pagination pagination = new Pagination();
        pagination.setFilterSet(new HashSet<Filter>());

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForOrgWithFiltering(supplierAdmin1,
                        pagination, SUB_STATES,
                        Arrays.asList(Long.valueOf(key1), Long.valueOf(key4)));
            }
        });

        // then
        assertEquals(1, result.size());
        assertEquals(key4, result.get(0).getKey());

    }

    @Test
    public void getSubscriptionsWithDefaultUdaValuesAndVendor()
            throws Exception {
        // given
        final UdaDefinition udaWithoutDefaultValButWithAfterSubscribing = runTX(
                new Callable<UdaDefinition>() {
                    @Override
                    public UdaDefinition call() throws Exception {
                        return Udas.createUdaDefinition(ds, supplier1,
                                UdaTargetType.CUSTOMER_SUBSCRIPTION,
                                "CUSTOMER_SUBSCRIPTION", "",
                                UdaConfigurationType.USER_OPTION_OPTIONAL);
                    }
                });
        final UdaDefinition udaDefinitionWithValue = runTX(
                new Callable<UdaDefinition>() {
                    @Override
                    public UdaDefinition call() throws Exception {
                        return Udas.createUdaDefinition(ds, supplier1,
                                UdaTargetType.CUSTOMER_SUBSCRIPTION,
                                "CUSTOMER_SUBSCRIPTION2", "defaultValue",
                                UdaConfigurationType.USER_OPTION_OPTIONAL);
                    }
                });
        final Subscription sub1 = createSubscription(
                customer1.getOrganizationId(), product.getProductId(), "sub1",
                supplier1, null, null);
        final Subscription sub4 = createSubscription(
                supplier1.getOrganizationId(), product.getProductId(), "sub4",
                supplier1, null, null);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Udas.createUda(ds, sub1,
                        udaWithoutDefaultValButWithAfterSubscribing, "");
                Udas.createUda(ds, sub1, udaDefinitionWithValue,
                        "assignedValue");
                Udas.createUda(ds, sub4, udaDefinitionWithValue,
                        "assignedValue");
                UdaDefinition udaDefinition = ds.find(UdaDefinition.class,
                        udaWithoutDefaultValButWithAfterSubscribing.getKey());
                udaDefinition.setDefaultValue("defaultValue");
                ds.persist(udaDefinition);
                return null;
            }
        });

        final Set<Long> set = new HashSet<>();
        set.addAll(Arrays.asList(Long.valueOf(udaDefinitionWithValue.getKey()),
                Long.valueOf(
                        udaWithoutDefaultValButWithAfterSubscribing.getKey())));

        // when
        List<BigInteger> result = runTX(new Callable<List<BigInteger>>() {
            @Override
            public List<BigInteger> call() throws Exception {
                return dao.getSubscriptionsWithDefaultUdaValuesAndVendor(
                        customerAdmin1, SUB_STATES, set);
            }
        });

        final UdaDefinition udaDefinitionWithValueAfterSubscr = runTX(
                new Callable<UdaDefinition>() {
                    @Override
                    public UdaDefinition call() throws Exception {
                        return Udas.createUdaDefinition(ds, supplier1,
                                UdaTargetType.CUSTOMER_SUBSCRIPTION,
                                "CUSTOMER_SUBSCRIPTION3", "value",
                                UdaConfigurationType.USER_OPTION_OPTIONAL);
                    }
                });

        final Set<Long> setAfterSubscr = new HashSet<>();
        set.addAll(Arrays.asList(
                Long.valueOf(udaDefinitionWithValueAfterSubscr.getKey())));

        List<BigInteger> result1 = runTX(new Callable<List<BigInteger>>() {
            @Override
            public List<BigInteger> call() throws Exception {
                return dao.getSubscriptionsWithDefaultUdaValuesAndVendor(
                        customerAdmin1, SUB_STATES, set);
            }
        });

        // then
        assertEquals(1, result.size());
        assertEquals(1, result1.size());

        assertEquals(sub1.getKey(), result.get(0).longValue());

    }

    @Test
    public void getSubscriptionsCustomer() throws Exception {
        // given
        createSubscription(customer1.getOrganizationId(),
                product.getProductId(), "sub1", supplier1, null, null);
        createSubscription(customer2.getOrganizationId(),
                product.getProductId(), "sub2", supplier1, null, null);
        createSubscription(supplier1.getOrganizationId(),
                product.getProductId(), "sub3", supplier1, null, null);

        final Pagination pagination = new Pagination();
        pagination.setFilterSet(new HashSet<Filter>());

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForOrg(customerAdmin1, pagination,
                        SUB_STATES);
            }
        });

        // then
        assertEquals(1, result.size());
        assertEquals("sub1", result.get(0).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsWithFilteringCustomer() throws Exception {
        // given
        final long key1 = createSubscription(customer1.getOrganizationId(),
                product.getProductId(), "sub1_1", supplier1, null, null)
                        .getKey();
        createSubscription(customer1.getOrganizationId(),
                product.getProductId(), "sub1_2", supplier1, null, null);
        createSubscription(customer2.getOrganizationId(),
                product.getProductId(), "sub2", supplier1, null, null);
        final long key4 = createSubscription(supplier1.getOrganizationId(),
                product.getProductId(), "sub3", supplier1, null, null).getKey();

        final Pagination pagination = new Pagination();
        pagination.setFilterSet(new HashSet<Filter>());

        // when
        List<Subscription> result = runTX(new Callable<List<Subscription>>() {
            @Override
            public List<Subscription> call() throws Exception {
                return dao.getSubscriptionsForOrgWithFiltering(customerAdmin1,
                        pagination, SUB_STATES,
                        Arrays.asList(Long.valueOf(key1), Long.valueOf(key4)));
            }
        });

        // then
        assertEquals(1, result.size());
        assertEquals(key1, result.get(0).getKey());
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
        user.setLocale("en");

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
}
