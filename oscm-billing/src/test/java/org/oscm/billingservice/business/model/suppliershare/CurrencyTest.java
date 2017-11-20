/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 31, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.model.suppliershare.Currency;
import org.oscm.billingservice.business.model.suppliershare.Marketplace;

public class CurrencyTest {

    private Currency currency;

    @Before
    public void setup() {
        currency = new Currency();
    }

    @Test
    public void calculate_zeroMarketplaces() {
        // given
        currency.getMarketplace().clear();

        // when
        currency.calculate();

        // then no result and exception expected
    }

    @Test
    public void calculate_singleMarketplace() {
        // given
        Marketplace m = mock(Marketplace.class);
        currency.addMarketplace(m);

        // when
        currency.calculate();

        // then
        verify(m).calculate();
    }

    @Test
    public void calculate_multipleMarketplaces() {
        // given
        Marketplace m1 = mock(Marketplace.class);
        currency.addMarketplace(m1);
        Marketplace m2 = mock(Marketplace.class);
        currency.addMarketplace(m2);
        Marketplace m3 = mock(Marketplace.class);
        currency.addMarketplace(m3);

        // when
        currency.calculate();

        // then
        verify(m1).calculate();
        verify(m2).calculate();
        verify(m3).calculate();
    }

}
