/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 13.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;

/**
 * @author weiser
 * 
 */
public class SteppedPriceIT extends DomainObjectTestBase {

    @Test
    public void testAdd() throws Exception {
        final SteppedPrice read = doAdd();
        verifyHistory(read, 1, ModificationType.ADD);
    }

    @Test
    public void testModify() throws Exception {
        final SteppedPrice read = doModify(doAdd());
        verifyHistory(read, 2, ModificationType.MODIFY);
    }

    @Test
    public void testDelete() throws Exception {
        final SteppedPrice read = doModify(doAdd());
        runTX(new Callable<Void>() {

            public Void call() throws Exception {
                SteppedPrice steppedPrice = mgr.getReference(
                        SteppedPrice.class, read.getKey());
                mgr.remove(steppedPrice);
                return null;
            }
        });
        verifyHistory(read, 3, ModificationType.DELETE);
    }

    private void verifyHistory(final SteppedPrice read, int historyEntries,
            ModificationType lastModType) throws Exception {
        List<DomainHistoryObject<?>> history = runTX(new Callable<List<DomainHistoryObject<?>>>() {

            public List<DomainHistoryObject<?>> call() throws Exception {
                return mgr.findHistory(read);
            }
        });
        Assert.assertNotNull(history);
        Assert.assertEquals(historyEntries, history.size());
        SteppedPriceHistory sph = (SteppedPriceHistory) history.get(history
                .size() - 1);
        SteppedPriceData dc = read.getDataContainer();
        Assert.assertEquals(dc.getAdditionalPrice(), sph.getAdditionalPrice());
        Assert.assertEquals(dc.getFreeEntityCount(), sph.getFreeEntityCount());
        Assert.assertEquals(dc.getLimit(), sph.getLimit());
        Assert.assertEquals(dc.getPrice(), sph.getPrice());
        Assert.assertEquals(read.getKey(), sph.getObjKey());
        Assert.assertEquals(lastModType, sph.getModtype());

        Assert.assertNull(sph.getPricedEventObjKey());
        Assert.assertNull(sph.getPricedParameterObjKey());
        Assert.assertNull(sph.getPriceModelObjKey());
    }

    private SteppedPrice doModify(final SteppedPrice sp) throws Exception {

        final SteppedPrice read = runTX(new Callable<SteppedPrice>() {

            public SteppedPrice call() throws Exception {
                SteppedPrice steppedPrice = mgr.getReference(
                        SteppedPrice.class, sp.getKey());
                steppedPrice.setAdditionalPrice(new BigDecimal(888));
                steppedPrice.setFreeEntityCount(888);
                steppedPrice.setLimit(new Long(888L));
                steppedPrice.setPrice(new BigDecimal(888));
                return mgr.getReference(SteppedPrice.class,
                        steppedPrice.getKey());
            }
        });
        Assert.assertEquals(new BigDecimal(888), read.getAdditionalPrice());
        Assert.assertEquals(888, read.getFreeEntityCount());
        Assert.assertEquals(new Long(888), read.getLimit());
        Assert.assertEquals(new BigDecimal(888), read.getPrice());
        return read;
    }

    private SteppedPrice doAdd() throws Exception {
        final SteppedPrice sp = new SteppedPrice();
        sp.setAdditionalPrice(new BigDecimal(123));
        sp.setFreeEntityCount(234);
        sp.setLimit(new Long(345L));
        sp.setPrice(new BigDecimal(456));
        final SteppedPrice read = runTX(new Callable<SteppedPrice>() {

            public SteppedPrice call() throws Exception {
                mgr.persist(sp);
                return mgr.getReference(SteppedPrice.class, sp.getKey());
            }
        });

        Assert.assertEquals(new BigDecimal(123), read.getAdditionalPrice());
        Assert.assertEquals(234, read.getFreeEntityCount());
        Assert.assertEquals(new Long(345), read.getLimit());
        Assert.assertEquals(new BigDecimal(456), read.getPrice());
        return read;
    }

    @Test
    public void testCopy() throws Exception {
        SteppedPrice source = doAdd();
        SteppedPrice copy = source.copy();
        Assert.assertEquals(source.getAdditionalPrice(),
                copy.getAdditionalPrice());
        Assert.assertEquals(source.getFreeEntityCount(),
                copy.getFreeEntityCount());
        Assert.assertEquals(source.getLimit(), copy.getLimit());
        Assert.assertEquals(source.getPrice(), copy.getPrice());
        Assert.assertEquals(0, copy.getKey());
        Assert.assertNull(copy.getPricedEvent());
        Assert.assertNull(copy.getPricedParameter());
        Assert.assertNull(copy.getPriceModel());
    }

    @Test
    public void testAddWithNullLimit() throws Exception {
        final SteppedPrice read = doAddWithNullLimit();
        verifyHistory(read, 1, ModificationType.ADD);
    }

    private SteppedPrice doAddWithNullLimit() throws Exception {
        final SteppedPrice sp = new SteppedPrice();
        sp.setAdditionalPrice(new BigDecimal(123));
        sp.setFreeEntityCount(234);
        sp.setLimit(null);
        sp.setPrice(new BigDecimal(456));
        final SteppedPrice read = runTX(new Callable<SteppedPrice>() {

            public SteppedPrice call() throws Exception {
                mgr.persist(sp);
                return mgr.getReference(SteppedPrice.class, sp.getKey());
            }
        });

        Assert.assertEquals(new BigDecimal(123), read.getAdditionalPrice());
        Assert.assertEquals(234, read.getFreeEntityCount());
        Assert.assertEquals(null, read.getLimit());
        Assert.assertEquals(new BigDecimal(456), read.getPrice());
        return read;
    }
}
