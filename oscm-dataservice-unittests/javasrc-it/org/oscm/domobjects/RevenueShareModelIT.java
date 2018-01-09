/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Tests for the domain object representing the revenue share model.
 * 
 * @author farmaki
 * 
 */

public class RevenueShareModelIT extends DomainObjectTestBase {

    private List<DomainObjectWithHistory<?>> domObjects = new ArrayList<DomainObjectWithHistory<?>>();

    /**
     * Tests the creation of a RevenueShareModel object and compare the
     * persisted object with the original and the history.
     * 
     * @throws Throwable
     */
    @Test
    public void testAdd() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestAddCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testModify() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestModify();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestModifyCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testDelete() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestDelete();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestDeleteCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestAdd() throws NonUniqueBusinessKeyException {
        domObjects.clear();

        RevenueShareModel revenueShareModel = new RevenueShareModel();
        revenueShareModel.setRevenueShare(new BigDecimal("100.00"));
        revenueShareModel
                .setRevenueShareModelType(RevenueShareModelType.BROKER_REVENUE_SHARE);

        mgr.persist(revenueShareModel);
        domObjects.add(revenueShareModel);
    }

    private void doTestAddCheck() {
        final BigDecimal ZERO = new BigDecimal("100.00");

        RevenueShareModel oldRevenueShareModel = (RevenueShareModel) domObjects
                .get(0);
        assertNotNull("Old RevenueShareModel expected", oldRevenueShareModel);
        RevenueShareModel savedRevenueShareModel = mgr.find(
                RevenueShareModel.class, oldRevenueShareModel.getKey());

        assertNotNull("RevenueShareModel expected", savedRevenueShareModel);
        assertEquals(RevenueShareModelType.BROKER_REVENUE_SHARE,
                savedRevenueShareModel.getRevenueShareModelType());
        assertEquals(ZERO, savedRevenueShareModel.getRevenueShare());

        List<DomainHistoryObject<?>> histObjs = mgr
                .findHistory(savedRevenueShareModel);
        assertNotNull("History entry 'null' for revenue share model", histObjs);
        assertFalse("History entry empty for revenue share model",
                histObjs.isEmpty());
        assertTrue("One history entry expected for revenue share model",
                histObjs.size() == 1);
        DomainHistoryObject<?> hist = histObjs.get(0);
        assertEquals(ModificationType.ADD, hist.getModtype());
        assertEquals("modUser", "guest", hist.getModuser());
    }

    protected void doTestModifyCheck() {
        final BigDecimal ONE = new BigDecimal("0.00");

        RevenueShareModel oldRevenueShareModel = (RevenueShareModel) domObjects
                .get(0);
        assertNotNull("Old RevenueShareModel expected", oldRevenueShareModel);

        RevenueShareModel savedRevenueShareModel = mgr.find(
                RevenueShareModel.class, oldRevenueShareModel.getKey());
        assertEquals(RevenueShareModelType.RESELLER_REVENUE_SHARE,
                savedRevenueShareModel.getRevenueShareModelType());
        assertEquals(ONE, savedRevenueShareModel.getRevenueShare());

        List<DomainHistoryObject<?>> histObjs = mgr
                .findHistory(savedRevenueShareModel);
        assertNotNull("History 'null' for revenue share model", histObjs);
        assertTrue("Two history entries expected for revenue share model",
                histObjs.size() == 2);
        RevenueShareModelHistory hist = (RevenueShareModelHistory) histObjs
                .get(1);
        assertEquals(ModificationType.MODIFY, hist.getModtype());
    }

    private void doTestModify() throws NonUniqueBusinessKeyException {
        RevenueShareModel oldRevenueShareModel = (RevenueShareModel) domObjects
                .get(0);

        RevenueShareModel savedRevenueShareModel = mgr.find(
                RevenueShareModel.class, oldRevenueShareModel.getKey());
        savedRevenueShareModel.setRevenueShare(new BigDecimal("0.00"));
        savedRevenueShareModel
                .setRevenueShareModelType(RevenueShareModelType.RESELLER_REVENUE_SHARE);
        mgr.persist(savedRevenueShareModel);
        domObjects.clear();
        domObjects.add(savedRevenueShareModel);
    }

    private void doTestDelete() {
        RevenueShareModel oldRevenueShareModel = (RevenueShareModel) domObjects
                .get(0);
        assertNotNull("Old Revenue share model expected", oldRevenueShareModel);
        RevenueShareModel revenueShareModel = mgr.find(RevenueShareModel.class,
                oldRevenueShareModel.getKey());
        assertNotNull("Revenue share model expected", revenueShareModel);
        domObjects.clear();
        domObjects.add(revenueShareModel);
        mgr.remove(revenueShareModel);
    }

    private void doTestDeleteCheck() {
        RevenueShareModel oldRevenueShareModel = (RevenueShareModel) domObjects
                .get(0);
        assertNotNull("Old Revenue share model expected", oldRevenueShareModel);

        RevenueShareModel revenueShareModel = mgr.find(RevenueShareModel.class,
                oldRevenueShareModel.getKey());
        assertNull("Revenue share model still available", revenueShareModel);

        List<DomainHistoryObject<?>> histObjs = mgr
                .findHistory(oldRevenueShareModel);
        Assert.assertNotNull("History entry 'null' for revenue share model",
                histObjs);
        Assert.assertFalse("History entry empty for revenue share model",
                histObjs.isEmpty());
        Assert.assertTrue(
                "Two history entries expected for revenue share model",
                histObjs.size() == 2);
        DomainHistoryObject<?> hist = histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());

        hist = histObjs.get(1);
        Assert.assertEquals(ModificationType.DELETE, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());

    }

}
