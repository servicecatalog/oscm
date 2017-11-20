/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 18, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.techproductoperation.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.SessionContext;

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
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.OperationStatus;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.techproductoperation.dao.OperationRecordDao;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author sun
 * 
 */
public class OperationRecordServiceLocalBeanIT extends EJBTestBase {

    private DataService ds;
    private OperationRecordServiceLocalBean orslb;
    private OperationRecordDao operationRecordDao;
    private LocalizerServiceLocal localizer;
    private Organization org;
    private PlatformUser subManager;
    private PlatformUser user;
    private Subscription sub1;
    private Subscription sub2;
    private Product pro1;
    private TechnicalProduct tp;
    private TechnicalProductOperation op;
    public static final long TIMESTAMP = 1282816800000L;

    private OperationRecord recordForUserSub1;
    private OperationRecord recordForUserSub2;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        container.addBean(new OperationRecordDao());
        container.addBean(new LocalizerServiceBean());
        operationRecordDao = container.get(OperationRecordDao.class);
        localizer = container.get(LocalizerServiceLocal.class);
        orslb = new OperationRecordServiceLocalBean();
        orslb.dm = ds;
        orslb.operationRecordDao = operationRecordDao;
        orslb.localizer = localizer;
        orslb.sessionCtx = mock(SessionContext.class);

        org = createOrg("org1", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        createUser(org, true, "orgAdmin");
        subManager = createUser(org, false, "subManager");
        grantSubscriptionManagerRole(subManager,
                UserRoleType.SUBSCRIPTION_MANAGER);
        user = createUser(org, false, "user");

        tp = createTechnicalProduct();
        op = createOperation(tp);
        pro1 = createProduct(org, tp.getTechnicalProductId(), false,
                "productId1", "priceModelId1");
        sub1 = createSubscription(org.getOrganizationId(), pro1.getProductId(),
                "subscriptionId1", org, null);
        sub2 = createSubscription(org.getOrganizationId(), pro1.getProductId(),
                "subscriptionId2", org, subManager);
        recordForUserSub1 = createOperationRecord(user, sub1, op,
                "transactionId1");
        recordForUserSub2 = createOperationRecord(user, sub2, op,
                "transactionId2");
    }

    @Test
    public void createOperationRecord() throws Exception {
        // when
        container.login(user.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                OperationRecord record = new OperationRecord();
                record.setExecutiondate(new Date(10000L));
                record.setStatus(OperationStatus.RUNNING);
                record.setSubscription(sub1);
                record.setTechnicalProductOperation(op);
                record.setTransactionid("test");
                record.setUser(user);
                orslb.createOperationRecord(record);
                return null;
            }
        });

        List<OperationRecord> result = runTX(
                new Callable<List<OperationRecord>>() {
                    @Override
                    public List<OperationRecord> call() throws Exception {
                        return operationRecordDao
                                .getOperationsForUser(user.getKey());
                    }
                });
        // then
        assertEquals(3, result.size());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void createOperationRecord_NonUniqueBusinessKeyException()
            throws Exception {
        // when
        container.login(user.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                OperationRecord record = new OperationRecord();
                record.setExecutiondate(new Date(10000L));
                record.setStatus(OperationStatus.RUNNING);
                record.setSubscription(sub1);
                record.setTechnicalProductOperation(op);
                record.setTransactionid("transactionId1");
                record.setUser(user);
                orslb.createOperationRecord(record);
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void createOperationRecord_IllegalArgumentException()
            throws Exception {
        // when
        container.login(user.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                orslb.createOperationRecord(null);
                return null;
            }
        });
    }

    @Test
    public void deleteOperationRecords() throws Exception {
        // when
        container.login(user.getKey());
        final List<Long> recordKeys = new ArrayList<>();
        recordKeys.add(Long.valueOf(recordForUserSub1.getKey()));
        recordKeys.add(Long.valueOf(recordForUserSub2.getKey()));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                orslb.deleteOperationRecords(recordKeys);
                return null;
            }
        });

        List<OperationRecord> result = runTX(
                new Callable<List<OperationRecord>>() {
                    @Override
                    public List<OperationRecord> call() throws Exception {
                        return operationRecordDao
                                .getOperationsForUser(user.getKey());
                    }
                });
        // then
        assertEquals(0, result.size());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteOperationRecords_ObjectNotFoundException()
            throws Exception {
        // when
        container.login(user.getKey());
        final List<Long> recordKeys = new ArrayList<>();
        recordKeys.add(Long.valueOf(recordForUserSub1.getKey()));
        recordKeys.add(Long.valueOf(1L));
        recordKeys.add(Long.valueOf(recordForUserSub2.getKey()));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                orslb.deleteOperationRecords(recordKeys);
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteOperationRecords_IllegalArgumentException()
            throws Exception {
        // when
        container.login(user.getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                orslb.deleteOperationRecords(null);
                return null;
            }
        });
    }

    @Test
    public void changeOperationStatus_ProgressIsNotEmpty() throws Exception {
        // when
        container.login(user.getKey());
        final List<VOLocalizedText> progress = new ArrayList<>();
        VOLocalizedText text = new VOLocalizedText();
        text.setLocale("en");
        text.setText("text");
        text.setVersion(0);
        progress.add(text);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                orslb.updateOperationStatus("transactionId1",
                        OperationStatus.ERROR, progress);
                return null;
            }
        });

        final OperationRecord record = runTX(new Callable<OperationRecord>() {
            @Override
            public OperationRecord call() throws Exception {
                OperationRecord r = new OperationRecord();
                r.setTransactionid("transactionId1");
                return (OperationRecord) ds.getReferenceByBusinessKey(r);
            }
        });

        String localizedTest = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                OperationRecord r = new OperationRecord();
                r.setTransactionid("transactionId1");
                return localizer.getLocalizedTextFromDatabase("en",
                        record.getKey(),
                        LocalizedObjectTypes.OPERATION_STATUS_DESCRIPTION);
            }
        });
        // then
        assertEquals(OperationStatus.ERROR, record.getStatus());
        assertEquals("text", localizedTest);
    }

    @Test
    public void changeOperationStatus_ProgressIsEmpty() throws Exception {
        // when
        container.login(user.getKey());
        final List<VOLocalizedText> progress = new ArrayList<>();
        VOLocalizedText text = new VOLocalizedText();
        text.setLocale("en");
        text.setText("text");
        text.setVersion(0);
        progress.add(text);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                orslb.updateOperationStatus("transactionId1",
                        OperationStatus.ERROR, progress);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                orslb.updateOperationStatus("transactionId1",
                        OperationStatus.ERROR,
                        new ArrayList<VOLocalizedText>());
                return null;
            }
        });

        final OperationRecord record = runTX(new Callable<OperationRecord>() {
            @Override
            public OperationRecord call() throws Exception {
                OperationRecord r = new OperationRecord();
                r.setTransactionid("transactionId1");
                return (OperationRecord) ds.getReferenceByBusinessKey(r);
            }
        });

        String localizedTest = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                OperationRecord r = new OperationRecord();
                r.setTransactionid("transactionId1");
                return localizer.getLocalizedTextFromDatabase("en",
                        record.getKey(),
                        LocalizedObjectTypes.OPERATION_STATUS_DESCRIPTION);
            }
        });
        // then
        assertEquals(OperationStatus.ERROR, record.getStatus());
        assertEquals("", localizedTest);
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

    private OperationRecord createOperationRecord(final PlatformUser user,
            final Subscription sub,
            final TechnicalProductOperation technicalProductOperation,
            final String transactionId) throws Exception {
        return runTX(new Callable<OperationRecord>() {
            @Override
            public OperationRecord call() throws Exception {
                OperationRecord record = new OperationRecord();
                record.setExecutiondate(new Date(10000L));
                record.setStatus(OperationStatus.RUNNING);
                record.setSubscription(sub);
                record.setTechnicalProductOperation(technicalProductOperation);
                record.setTransactionid(transactionId);
                record.setUser(user);
                ds.persist(record);
                return record;
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
                return TechnicalProducts.createTechnicalProduct(ds, org, "id",
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
