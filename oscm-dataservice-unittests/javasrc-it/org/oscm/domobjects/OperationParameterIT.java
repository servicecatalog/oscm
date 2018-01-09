/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 22.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.types.enumtypes.OperationParameterType;

/**
 * @author weiser
 * 
 */
public class OperationParameterIT extends DomainObjectTestBase {

    private Organization organization;
    private TechnicalProduct technicalProduct;
    private TechnicalProductOperation operation1;
    private TechnicalProductOperation operation2;

    @Override
    protected void dataSetup() throws Exception {
        createOrganizationRoles(mgr);
        organization = Organizations.createOrganization(mgr,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        technicalProduct = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP", false, ServiceAccessType.DIRECT);
        operation1 = TechnicalProducts.addTechnicalProductOperation(mgr,
                technicalProduct, "OP1", "actionURL1");
        operation2 = TechnicalProducts.addTechnicalProductOperation(mgr,
                technicalProduct, "OP2", "actionURL2");
    }

    @Test
    public void add() throws Exception {
        doAdd("PARAM1", operation1);

    }

    @Test
    public void add_SameIdInDifferentOperations() throws Exception {
        doAdd("PARAM1", operation1);
        doAdd("PARAM1", operation2);
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void add_DuplicateId() throws Exception {
        doAdd("PARAM1", operation1);
        doAdd("PARAM1", operation1);
    }

    @Test
    public void modify() throws Exception {
        doModify(doAdd("PARAM1", operation1), "PARAM47");
    }

    @Test
    public void delete() throws Exception {
        final OperationParameter op = doAdd("PARAM1", operation1);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                OperationParameter op_read = mgr.getReference(
                        OperationParameter.class, op.getKey());
                mgr.remove(op_read);
                return null;
            }
        });
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                assertNull(mgr.find(OperationParameter.class, op.getKey()));
                return null;
            }
        });

    }

    @Test
    public void delete_CascadeFromOperation() throws Exception {
        doAdd("PARAM1", operation1);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                TechnicalProductOperation tpo = mgr.getReference(
                        TechnicalProductOperation.class, operation1.getKey());
                mgr.remove(tpo);
                return null;
            }
        });

    }

    private OperationParameter doAdd(final String id,
            final TechnicalProductOperation tpo) throws Exception {

        final Long key = runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                TechnicalProductOperation tpo_read = mgr.getReference(
                        TechnicalProductOperation.class, tpo.getKey());
                OperationParameter op = new OperationParameter();
                op.setId(id);
                op.setMandatory(false);
                op.setType(OperationParameterType.INPUT_STRING);
                op.setTechnicalProductOperation(tpo_read);
                mgr.persist(op);
                return Long.valueOf(op.getKey());
            }
        });
        final OperationParameter read = runTX(new Callable<OperationParameter>() {

            @Override
            public OperationParameter call() throws Exception {
                return mgr.getReference(OperationParameter.class,
                        key.longValue());
            }
        });

        assertEquals(id, read.getId());
        assertEquals(OperationParameterType.INPUT_STRING, read.getType());
        assertFalse(read.isMandatory());
        return read;
    }

    private OperationParameter doModify(final OperationParameter op,
            final String id) throws Exception {

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                OperationParameter op_read = mgr.getReference(
                        OperationParameter.class, op.getKey());
                op_read.setId(id);
                op_read.setMandatory(true);
                op_read.setType(OperationParameterType.REQUEST_SELECT);
                return null;
            }
        });
        final OperationParameter read = runTX(new Callable<OperationParameter>() {

            @Override
            public OperationParameter call() throws Exception {
                return mgr.getReference(OperationParameter.class, op.getKey());
            }
        });

        assertEquals(id, read.getId());
        assertEquals(OperationParameterType.REQUEST_SELECT, read.getType());
        assertTrue(read.isMandatory());
        return read;
    }

}
