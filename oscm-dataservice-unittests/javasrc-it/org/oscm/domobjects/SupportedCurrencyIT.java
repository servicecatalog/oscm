/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 31.08.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Currency;
import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Tests for the domain object representing supported currencies. Basically only
 * a plain insert and then a find by business-key are tested, as the data for
 * this object belongs to the initial setup-data of the BES product.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class SupportedCurrencyIT extends DomainObjectTestBase {

    @Test
    public void testCreate() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doCreate();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doFindByBK();
                return null;
            }
        });
    }

    private void doCreate() throws NonUniqueBusinessKeyException {
        SupportedCurrency sc = new SupportedCurrency();
        SupportedCurrencyData data = new SupportedCurrencyData();
        data.setCurrency(Currency.getInstance("EUR"));
        sc.setDataContainer(data);
        mgr.persist(sc);
    }

    private void doFindByBK() {
        SupportedCurrency sc = new SupportedCurrency();
        Currency cur = Currency.getInstance("EUR");
        sc.setCurrency(cur);
        sc = (SupportedCurrency) mgr.find(sc);
        SupportedCurrencyData data = sc.getDataContainer();

        Assert.assertNotNull("Object not found", sc);
        Assert.assertEquals("Wrong currency code", "EUR",
                data.getCurrencyISOCode());
        Assert.assertEquals(cur, data.getCurrency());
        Assert.assertEquals("Wrong currency code", "EUR",
                sc.getCurrencyISOCode());
        Assert.assertEquals(cur, sc.getCurrency());
    }

}
