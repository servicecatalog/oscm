/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 12.10.2011                                                      
 *                                                                              
 *  Completion Time: 12.10.2011                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;

public class PaymentTypeIT extends DomainObjectTestBase {

    private PSP psp;
    private PaymentType pt;

    public void initPsp() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // read the existing psp as created by the domain objects test
                // setup
                psp = new PSP();
                psp.setIdentifier("pspIdentifier");
                psp = (PSP) mgr.getReferenceByBusinessKey(psp);
                return null;
            }
        });
    }

    @Test
    public void add() throws Exception {
        initPsp();
        doCreatePT();
        assertEquals("credit_card", pt.getPaymentTypeId());
        assertEquals(0, pt.getVersion());
        assertTrue(pt.getKey() > 0);
        assertEquals(psp.getKey(), pt.getPsp().getKey());
        validateHistory(pt, 1, 0, ModificationType.ADD);
    }

    @Test
    public void modify() throws Exception {
        initPsp();
        doCreatePT();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                pt = mgr.getReference(PaymentType.class, pt.getKey());
                pt.setPaymentTypeId("anotherId");
                return null;
            }
        });
        assertEquals("anotherId", pt.getPaymentTypeId());
        assertEquals(1, pt.getVersion());
        validateHistory(pt, 2, 1, ModificationType.MODIFY);
    }

    @Test
    public void delete() throws Exception {
        initPsp();
        doCreatePT();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                pt = mgr.getReference(PaymentType.class, pt.getKey());
                mgr.remove(pt);
                return null;
            }
        });
        validateHistory(pt, 2, 1, ModificationType.DELETE);
    }

    private void doCreatePT() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                pt = new PaymentType();
                pt.setPaymentTypeId("credit_card");
                pt.setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
                pt.setPsp(mgr.getReference(PSP.class, psp.getKey()));
                mgr.persist(pt);
                return null;
            }
        });
    }

    /**
     * Validates the historized data for the payment type.
     * 
     * @param pt
     *            The payment type to validate the history for.
     * @param count
     *            The expected amount of history entries.
     * @param version
     *            The expected version of the latest history entry.
     * @param modType
     *            The expected modification type of the latest history entry.
     * @throws Exception
     */
    private void validateHistory(final PaymentType pt, final int count,
            final int version, final ModificationType modType) throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                List<DomainHistoryObject<?>> hist = mgr.findHistory(pt);
                assertEquals(count, hist.size());
                DomainHistoryObject<?> latestEntry = hist.get(count - 1);
                assertEquals(version, latestEntry.getObjVersion());
                assertEquals(modType, latestEntry.getModtype());
                return null;
            }
        });
    }
}
