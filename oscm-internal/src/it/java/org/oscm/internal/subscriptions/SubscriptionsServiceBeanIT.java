/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015年4月30日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptions;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.paginator.Filter;
import org.oscm.paginator.Pagination;
import org.oscm.paginator.PaginationFullTextFilter;
import org.oscm.paginator.TableColumns;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.subscriptionservice.bean.SubscriptionListServiceBean;
import org.oscm.subscriptionservice.bean.SubscriptionServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.LocalizedResources;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.ejb.TestContainer;

/**
 * @author qiu
 * 
 */

public class SubscriptionsServiceBeanIT extends EJBTestBase {
    private DataService ds;
    private SubscriptionsService service;
    private Organization tpAndSupplier;
    private Organization customer;
    private PlatformUser admin;
    private Product product;

    @Override
    protected void setup(TestContainer container) throws Exception {
        ds = new DataServiceBean() {
            @Override
            public PlatformUser getCurrentUser() {
                return givenUser(1, "userId", tpAndSupplier, UserRoleType.ORGANIZATION_ADMIN);
            }
        };
        container.addBean(ds);
        container.enableInterfaceMocking(true);
        container.addBean(new LocalizerServiceBean());
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(mock(ApplicationServiceLocal.class));
        container.addBean(mock(SessionServiceLocal.class));
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new SubscriptionsServiceBean());

        tpAndSupplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        customer = registerCustomer("customer", tpAndSupplier);
        admin = createUser(customer, true, "unitAdmin");

        product = createProduct("serviceB", "techServiceB",
                tpAndSupplier.getOrganizationId(), ServiceAccessType.LOGIN);
        LocalizedResources.localizeProduct(ds, product.getKey(), "en", "ja");
        service = container.get(SubscriptionsService.class);
        createSubscription(customer.getOrganizationId(),
                product.getProductId(), "sub1", tpAndSupplier, null, admin);

    }

    private Organization createOrg(final String organizationId,
                                    final OrganizationRoleType... roles) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                createOrganizationRoles(ds);
                return Organizations.createOrganization(ds, organizationId,
                        roles);
            }
        });
    }

    private PlatformUser createUser(final Organization organization,
                                    final boolean isAdmin, final String userId) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(ds, organization,
                        isAdmin, userId);
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

    @Test(expected = EJBAccessException.class)
    public void getSubscriptionsForOrg_NotAuthorized() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(String.valueOf(admin.getKey()));
                try {
                    service.getSubscriptionsForOrg(null);
                } catch (EJBException e) {
                    throw e.getCausedByException();
                }
                return null;
            }
        });
    }

    @Test
    public void getSubscriptionsForOrgWithFiltering() throws Exception {
        final PaginationFullTextFilter pagination = new PaginationFullTextFilter();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(String.valueOf(admin.getKey()),
                        UserRoleType.SUBSCRIPTION_MANAGER.name());
                try {
                    Set<SubscriptionStatus> subscriptionStatuses = new HashSet<>();
                    subscriptionStatuses.add(SubscriptionStatus.ACTIVE);
                    Set<Filter> set = new HashSet<>();
                    set.add(new Filter(TableColumns.SERVICE_NAME, "Product"));
                    pagination.setFilterSet(set);
                    Response subscriptionsForOrgWithFiltering = service.getSubscriptionsForOrgWithFiltering(subscriptionStatuses,
                            pagination);
                    List<POSubscriptionForList> resultList = subscriptionsForOrgWithFiltering.getResultList(POSubscriptionForList.class);
                    for (POSubscriptionForList poSubscriptionForList : resultList) {
                        assertTrue(poSubscriptionForList.getServiceName().equals("Product 1 (PRODUCT_MARKETING_NAME)"));
                    }
                } catch (EJBException e) {
                    throw e.getCausedByException();
                }
                return null;
            }
        });
    }

    @Test(expected = EJBAccessException.class)
    public void getSubscriptionsForOrgSize_NotAuthorized() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(String.valueOf(admin.getKey()));
                try {
                    service.getSubscriptionsForOrgSize(
                            new HashSet<SubscriptionStatus>(), new Pagination());
                } catch (EJBException e) {
                    throw e.getCausedByException();
                }
                return null;
            }
        });
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
        user.setLocale("en");
        return user;
    }
}
