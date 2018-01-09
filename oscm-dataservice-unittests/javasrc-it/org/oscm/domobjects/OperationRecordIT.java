/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014年9月17日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.id.IdGenerator;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.internal.types.enumtypes.OperationStatus;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author yuyin
 * 
 */
public class OperationRecordIT extends DomainObjectTestBase {

    private final List<OperationRecord> operationList = new ArrayList<OperationRecord>();
    private Organization organization;
    private TechnicalProduct technicalProduct;
    private static final String transactionid = IdGenerator
            .generateArtificialIdentifier();

    private void verify(final OperationStatus status) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assert.assertTrue("Nothing to verify", operationList.size() > 0);
                for (OperationRecord obj : operationList) {
                    OperationRecord savedObj = null;

                    try {
                        savedObj = mgr.getReference(OperationRecord.class,
                                obj.getKey());
                        Assert.assertTrue(
                                ReflectiveCompare.showDiffs(obj, savedObj),
                                ReflectiveCompare.compare(obj, savedObj));
                    } catch (ObjectNotFoundException e) {
                        throw e;
                    }
                    Assert.assertEquals("status", status, savedObj.getStatus());
                    Assert.assertEquals("transactionid", transactionid,
                            savedObj.getTransactionid());
                }
                return null;
            }
        });
    }

    @Override
    protected void dataSetup() throws Exception {
        createOrganizationRoles(mgr);
        organization = Organizations.createOrganization(mgr,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        technicalProduct = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP", false, ServiceAccessType.DIRECT);
    }

    public OperationRecord createOperationRecord(DataService mgr,
            OperationStatus status) throws Exception {
        organization = mgr.getReference(Organization.class,
                organization.getKey());
        PlatformUser user = Organizations.createUserForOrg(mgr, organization,
                true, "userId");

        Product product = Products.createProduct(
                organization.getOrganizationId(), "product1", "techProduct1",
                mgr);

        Subscription sub = Subscriptions.createSubscription(mgr,
                organization.getOrganizationId(), product);

        TechnicalProductOperation op = createTechnicalProductOperation();

        OperationRecord operationRecord = new OperationRecord();
        operationRecord.setUser(user);
        operationRecord.setStatus(status);
        operationRecord.setSubscription(sub);
        operationRecord.setTechnicalProductOperation(op);
        operationRecord.setTransactionid(transactionid);

        mgr.persist(operationRecord);

        return operationRecord;
    }

    @Test
    public void testAdd() throws Exception {
        operationList.add(createOperationRecord());
        verify(OperationStatus.RUNNING);
    }

    @Test
    public void testModify() throws Exception {
        operationList.add(createOperationRecord());
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                OperationRecord opRecord = mgr.getReference(
                        OperationRecord.class, operationList.get(0).getKey());
                opRecord.setStatus(OperationStatus.ERROR);
                operationList.remove(0);
                operationList.add((OperationRecord) ReflectiveClone
                        .clone(opRecord));
                return null;
            }
        });
        verify(OperationStatus.ERROR);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDelete() throws Exception {
        operationList.add(createOperationRecord());
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                mgr.remove(mgr.getReference(TriggerProcess.class, operationList
                        .get(0).getKey()));
                return null;
            }
        });
        verify(null);
    }

    private OperationRecord createOperationRecord() throws Exception {
        return runTX(new Callable<OperationRecord>() {
            public OperationRecord call() throws Exception {
                OperationRecord operationRecord = createOperationRecord(mgr,
                        OperationStatus.RUNNING);
                return (OperationRecord) ReflectiveClone.clone(operationRecord);
            }
        });
    }

    private TechnicalProductOperation createTechnicalProductOperation()
            throws Exception {
        final TechnicalProductOperation op = new TechnicalProductOperation();
        op.setOperationId("ID");
        op.setActionUrl("actionUrl");

        final TechnicalProductOperation read = runTX(new Callable<TechnicalProductOperation>() {

            public TechnicalProductOperation call() throws Exception {

                TechnicalProduct tp = mgr.getReference(TechnicalProduct.class,
                        technicalProduct.getKey());
                op.setTechnicalProduct(tp);
                op.setTechnicalProduct(technicalProduct);
                mgr.persist(op);
                return mgr.getReference(TechnicalProductOperation.class,
                        op.getKey());
            }
        });
        return read;
    }

}
