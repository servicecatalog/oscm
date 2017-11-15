/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 18, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.techproductoperation.dao;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.OperationRecord;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.internal.types.enumtypes.OperationStatus;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author zhaoh.fnst
 * 
 */
public class OperationRecordDaoIT extends EJBTestBase {

    private DataService ds;
    private OperationRecordDao dao;
    private Organization org1;
    private PlatformUser user1;
    private PlatformUser user2;
    private PlatformUser user3;
    private Subscription sub1;
    private Subscription sub2;
    private Subscription sub3;
    private Product pro1;
    private TechnicalProduct tp;
    private TechnicalProductOperation op;
    public static final long TIMESTAMP = 1282816800000L;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new OperationRecordDao();
        dao.dm = ds;

        org1 = createOrg("org1", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        user1 = createUser(org1, true, "user1");
        user2 = createUser(org1, false, "user2");
        grantSubscriptionManagerRole(user2, UserRoleType.SUBSCRIPTION_MANAGER);
        user3 = createUser(org1, false, "user3");

        tp = createTechnicalProduct();
        op = createOperation(tp);
        pro1 = createProduct(org1, tp.getTechnicalProductId(), false,
                "productId1", "priceModelId1");
        sub1 = createSubscription(org1.getOrganizationId(), pro1.getProductId(),
                "subscriptionId1", org1, null);
        sub2 = createSubscription(org1.getOrganizationId(), pro1.getProductId(),
                "subscriptionId2", org1, user2);
        sub3 = createSubscription(org1.getOrganizationId(), pro1.getProductId(),
                "subscriptionId3", org1, user2);
        createOperationRecord(user1, sub1, op, "transactionId11");
        createOperationRecord(user2, sub1, op, "transactionId12");
        createOperationRecord(user3, sub1, op, "transactionId13");
        createOperationRecord(user1, sub2, op, "transactionId14");
        createOperationRecord(user2, sub2, op, "transactionId15");
        createOperationRecord(user3, sub2, op, "transactionId16");
        createOperationRecord(user1, sub3, op, "transactionId17");
        createOperationRecord(user2, sub3, op, "transactionId18");
        createOperationRecord(user3, sub3, op, "transactionId19");
    }

    @Test
    public void getOperationsForOrgAdmin() throws Exception {
        // when
        container.login(user1.getKey(), ROLE_ORGANIZATION_ADMIN);
        List<OperationRecord> result = runTX(
                new Callable<List<OperationRecord>>() {
                    @Override
                    public List<OperationRecord> call() throws Exception {
                        return dao.getOperationsForOrgAdmin(org1.getKey());
                    }
                });

        // then
        assertEquals(9, result.size());
    }

    @Test
    public void getOperationsForSubManager1() throws Exception {
        // when
        List<OperationRecord> result = runTX(
                new Callable<List<OperationRecord>>() {
                    @Override
                    public List<OperationRecord> call() throws Exception {
                        return dao.getOperationsForSubManager(user1.getKey());
                    }
                });

        // then
        assertEquals(3, result.size());
    }

    @Test
    public void getOperationsForSubManager2() throws Exception {
        // when
        List<OperationRecord> result = runTX(
                new Callable<List<OperationRecord>>() {
                    @Override
                    public List<OperationRecord> call() throws Exception {
                        return dao.getOperationsForSubManager(user2.getKey());
                    }
                });

        // then
        assertEquals(7, result.size());
    }

    @Test
    public void getOperationsForUser1() throws Exception {
        // when
        List<OperationRecord> result = runTX(
                new Callable<List<OperationRecord>>() {
                    @Override
                    public List<OperationRecord> call() throws Exception {
                        return dao.getOperationsForUser(user1.getKey());
                    }
                });

        // then
        assertEquals(3, result.size());
    }

    @Test
    public void getOperationsForUser2() throws Exception {
        // when
        List<OperationRecord> result = runTX(
                new Callable<List<OperationRecord>>() {
                    @Override
                    public List<OperationRecord> call() throws Exception {
                        return dao.getOperationsForUser(user2.getKey());
                    }
                });

        // then
        assertEquals(3, result.size());
    }

    @Test
    public void getOperationsForUser3() throws Exception {
        // when
        List<OperationRecord> result = runTX(
                new Callable<List<OperationRecord>>() {
                    @Override
                    public List<OperationRecord> call() throws Exception {
                        return dao.getOperationsForUser(user3.getKey());
                    }
                });

        // then
        assertEquals(3, result.size());
    }

    private TechnicalProductOperation createOperation(
            final TechnicalProduct technicalProduct) throws Exception {
        return runTX(new Callable<TechnicalProductOperation>() {
            @Override
            public TechnicalProductOperation call() throws Exception {
                TechnicalProductOperation operation = new TechnicalProductOperation();
                operation.setActionUrl("actionUrl");
                operation.setOperationId("operationId");
                operation.setTechnicalProduct(technicalProduct);
                ds.persist(operation);
                ds.flush();
                return operation;
            }
        });
    }

    private void createOperationRecord(final PlatformUser user,
            final Subscription sub,
            final TechnicalProductOperation technicalProductOperation,
            final String transactionId) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                OperationRecord record = new OperationRecord();
                record.setExecutiondate(new Date(10000L));
                record.setStatus(OperationStatus.RUNNING);
                record.setSubscription(sub);
                record.setTechnicalProductOperation(technicalProductOperation);
                record.setTransactionid(transactionId);
                record.setUser(user);
                ds.persist(record);
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

    private PlatformUser createUser(final Organization org,
            final boolean isAdmin, final String userId) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(ds, org, isAdmin, userId);
            }
        });
    }

    private void grantSubscriptionManagerRole(final PlatformUser user,
            final UserRoleType type) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PlatformUsers.grantRoles(ds, user, type);
                return null;
            }
        });
    }

    private TechnicalProduct createTechnicalProduct() throws Exception {
        return runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                return TechnicalProducts.createTechnicalProduct(ds, org1, "id",
                        false, ServiceAccessType.LOGIN);
            }
        });
    }

    private Product createProduct(final Organization supplier,
            final String techProdId, final boolean chargeable,
            final String productId, final String priceModelId)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                return Products.createProduct(supplier, techProdId, chargeable,
                        productId, priceModelId, ds);
            }
        });
    }

    private Subscription createSubscription(final String customerId,
            final String productId, final String subscriptionId,
            final Organization supplier, final PlatformUser owner)
            throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.createSubscriptionWithOwner(ds, customerId,
                        productId, subscriptionId, null, TIMESTAMP, TIMESTAMP,
                        supplier, null, 1, owner);
            }
        });
    }

}
