/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-5                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ModifiedEntity;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UserRole;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Unit tests for {@link ModifiedEntityDao} using the test EJB container.
 * 
 * @author Mao
 */
public class ModifiedEntityDaoIT extends EJBTestBase {

    private DataService ds;
    private ModifiedEntityDao dao;
    private Product product;
    private Organization supplier;
    private Organization supplierCustomer;
    private Subscription subscription;
    private final static String SUBSCRIPTION_ID = "subscriptionID";

    @Override
    protected void setup(TestContainer container) throws Exception {
        final Organization org = new Organization();
        org.setKey(0);
        ds = new DataServiceBean() {
            @Override
            public PlatformUser getCurrentUser() {
                return givenUserAdmin(1, "userId", org);
            }
        };
        container.addBean(new ConfigurationServiceStub());
        container.addBean(ds);
        dao = new ModifiedEntityDao(ds);
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
    }

    @Test
    public void countSubscriptionOfOrganizationAndSubscription()
            throws Exception {
        // when
        Long result = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return dao.countSubscriptionOfOrganizationAndSubscription(
                        subscription, SUBSCRIPTION_ID);
            }
        });

        // then
        assertEquals(0, result.longValue());
    }

    @Test
    public void retrieveModifiedEntities() throws Exception {

        // when
        List<ModifiedEntity> result = runTX(
                new Callable<List<ModifiedEntity>>() {
                    @Override
                    public List<ModifiedEntity> call() throws Exception {
                        return dao.retrieveModifiedEntities(subscription);
                    }
                });

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void deleteModifiedEntityForSubscription() throws Exception {
        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                dao.deleteModifiedEntityForSubscription(subscription);
                return null;
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
}
