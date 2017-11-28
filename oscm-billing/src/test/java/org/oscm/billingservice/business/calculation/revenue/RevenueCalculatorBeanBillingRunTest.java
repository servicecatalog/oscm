/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 3, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import javax.ejb.SessionContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.test.DateTimeHandling;

/**
 * Tests the revenue calculator's billing run for subscription
 */
@RunWith(MockitoJUnitRunner.class)
public class RevenueCalculatorBeanBillingRunTest {

    @Mock
    BillingDataRetrievalServiceLocal bdr;

    @Mock
    SessionContext sessionCtx;

    @InjectMocks
    RevenueCalculatorBean calculator = spy(new RevenueCalculatorBean());

    private static final String EMPTY_XML = "";
    private static final String CURRENCY_CODE = "EUR";
    private static final BigDecimal ZERO_NORMALIZED = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

    @Test
    public void testPerformBillingRun_withFreePriceModel() throws Exception {

        // given
        BillingInput billingInput = getSimpleBillingInput();

        // when
        BillingResult result = calculator
                .performBillingRunForSubscription(billingInput);

        // then
        // no billing result is persisted
        verify(bdr, times(0)).persistBillingResult(any(BillingResult.class));

        // billing subscription status is updated
        verify(bdr, times(1)).updateBillingSubscriptionStatus(
                eq(billingInput.getSubscriptionKey()),
                eq(billingInput.getBillingPeriodEnd()));

        assertEquals(result.getResultXML(), EMPTY_XML);
        assertEquals(result.getNetAmount(), ZERO_NORMALIZED);
        assertEquals(result.getGrossAmount(), ZERO_NORMALIZED);

    }

    @Test
    public void testPerformBillingRun_withNoSubscriptionHistories()
            throws Exception {

        // given
        BillingInput billingInput = getSimpleBillingInputWithPriceModel();

        // when
        calculator.performBillingRunForSubscription(billingInput);

        // then
        verify(bdr, times(1)).persistBillingResult(any(BillingResult.class));

        // billing subscription status is updated
        verify(bdr, times(1)).updateBillingSubscriptionStatus(
                eq(billingInput.getSubscriptionKey()),
                eq(billingInput.getBillingPeriodEnd()));

        // billing result is removed as no subscription entries exist
        verify(bdr, times(1)).removeBillingResult(any(BillingResult.class));

    }

    private BillingInput getSimpleBillingInput() {

        BillingInput billingInput = new BillingInput.Builder()
                .setStoreBillingResult(true)
                .setSubscriptionKey(12)
                .setBillingPeriodEnd(
                        DateTimeHandling.calculateMillis("2015-07-01 00:00:00"))
                .setSubscriptionHistoryEntries(
                        new ArrayList<SubscriptionHistory>()).build();

        return billingInput;
    }

    private BillingInput getSimpleBillingInputWithPriceModel() {

        BillingInput billingInput = new BillingInput.Builder()
                .setStoreBillingResult(true)
                .setSubscriptionKey(12)
                .setBillingPeriodEnd(
                        DateTimeHandling.calculateMillis("2015-07-01 00:00:00"))
                .setSubscriptionHistoryEntries(
                        new ArrayList<SubscriptionHistory>())
                .setCurrencyIsoCode(CURRENCY_CODE).build();

        return billingInput;
    }
}
