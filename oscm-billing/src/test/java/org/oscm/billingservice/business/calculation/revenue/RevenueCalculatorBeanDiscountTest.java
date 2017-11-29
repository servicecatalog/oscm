/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.calculation.revenue.model.OverallCosts;
import org.oscm.billingservice.business.model.billingresult.BillingDetailsType;
import org.oscm.billingservice.business.model.billingresult.DiscountType;
import org.oscm.billingservice.business.model.billingresult.OverallCostsType;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.test.BigDecimalAsserts;

/**
 * @author kulle
 * 
 */
public class RevenueCalculatorBeanDiscountTest {

    private RevenueCalculatorBean bean;
    private BillingDataRetrievalServiceLocal bdr;

    private BigDecimal costsBeforeDiscount;
    private BillingInput billingInput;
    private BillingDetailsType billingDetails;
    private long chargingOrgKey;
    private OverallCosts overallCosts;

    @Before
    public void setup() {
        bdr = mock(BillingDataRetrievalServiceLocal.class);
        bean = new RevenueCalculatorBean();
        bean.bdr = bdr;

        costsBeforeDiscount = BigDecimal.ONE;
        billingInput = givenBillingInput("EUR");
        billingDetails = givenBillingDetails();
        chargingOrgKey = 0;
        overallCosts = givenOverallCosts();
    }

    private BillingInput givenBillingInput(String currency) {
        BillingInput.Builder builder = new BillingInput.Builder();
        builder.setCurrencyIsoCode(currency);
        return builder.build();
    }

    private BillingDetailsType givenBillingDetails() {
        OverallCostsType overallCostsType = mock(OverallCostsType.class);
        BillingDetailsType billingDetails = mock(BillingDetailsType.class);
        when(billingDetails.getOverallCosts()).thenReturn(overallCostsType);
        return billingDetails;
    }

    private OverallCosts givenOverallCosts() {
        return OverallCosts.newInstance();
    }

    @Test
    public void allowDiscount_null() {
        // given
        doReturn(null).when(bdr).loadDiscountValue(anyLong(), anyLong(),
                anyLong(), anyLong());

        // when
        OverallCosts discount = bean
                .allowDiscount(billingDetails, costsBeforeDiscount,
                        billingInput, chargingOrgKey, overallCosts);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ONE, discount.get("EUR"));
    }

    @Test
    public void allowDiscount_zero() {
        // given
        doReturn(BigDecimal.ZERO).when(bdr).loadDiscountValue(anyLong(),
                anyLong(), anyLong(), anyLong());

        // when
        OverallCosts discount = bean
                .allowDiscount(billingDetails, costsBeforeDiscount,
                        billingInput, chargingOrgKey, overallCosts);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ONE, discount.get("EUR"));
    }

    @Test
    public void allowDiscount_20percent() {
        // given
        doReturn(new BigDecimal("20")).when(bdr).loadDiscountValue(anyLong(),
                anyLong(), anyLong(), anyLong());

        // when
        OverallCosts discount = bean
                .allowDiscount(billingDetails, costsBeforeDiscount,
                        billingInput, chargingOrgKey, overallCosts);

        // then
        BigDecimalAsserts.checkEquals(new BigDecimal("0.80"),
                discount.get("EUR"));
    }

    /**
     * The internal percent calculation rounds to 2 places, therefore 0.2
     * percent equals zero and the costs are not reduced.
     */
    @Test
    public void allowDiscount_roundingToZero() {
        // given
        doReturn(new BigDecimal("0.2")).when(bdr).loadDiscountValue(anyLong(),
                anyLong(), anyLong(), anyLong());

        // when
        OverallCosts discount = bean
                .allowDiscount(billingDetails, costsBeforeDiscount,
                        billingInput, chargingOrgKey, overallCosts);

        // then
        BigDecimalAsserts.checkEquals(new BigDecimal("1.00"),
                discount.get("EUR"));
    }

    @Test
    public void allowDiscount_overallCosts_netAmount() {
        // given
        doReturn(new BigDecimal("20")).when(bdr).loadDiscountValue(anyLong(),
                anyLong(), anyLong(), anyLong());

        // when
        bean.allowDiscount(billingDetails, costsBeforeDiscount, billingInput,
                chargingOrgKey, overallCosts);

        // then
        verify(billingDetails.getOverallCosts()).setNetAmount(
                new BigDecimal("0.80"));
    }

    @Test
    public void allowDiscount_overallCosts_discountType() {
        // given
        doReturn(new BigDecimal("20")).when(bdr).loadDiscountValue(anyLong(),
                anyLong(), anyLong(), anyLong());

        // when
        bean.allowDiscount(billingDetails, costsBeforeDiscount, billingInput,
                chargingOrgKey, overallCosts);

        // then
        verify(billingDetails.getOverallCosts()).setDiscount(
                argThat(new DiscountArgumentMatcher(newDiscountType(
                        new BigDecimal("0.20"), new BigDecimal("0.80"),
                        BigDecimal.ONE, 20f))));
    }

    DiscountType newDiscountType(BigDecimal netAmount, BigDecimal after,
            BigDecimal before, float percent) {
        DiscountType type = new DiscountType();
        type.setDiscountNetAmount(netAmount);
        type.setNetAmountAfterDiscount(after);
        type.setNetAmountBeforeDiscount(before);
        type.setPercent(percent);
        return type;
    }

    class DiscountArgumentMatcher extends ArgumentMatcher<DiscountType> {

        DiscountType expected;

        public DiscountArgumentMatcher(DiscountType expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object actualObject) {
            DiscountType actual = (DiscountType) actualObject;

            BigDecimalAsserts.checkEquals("DiscountNetAmount is wrong",
                    expected.getDiscountNetAmount(),
                    actual.getDiscountNetAmount());

            BigDecimalAsserts.checkEquals("NetAmountAfterDiscount is wrong",
                    expected.getNetAmountAfterDiscount(),
                    actual.getNetAmountAfterDiscount());

            BigDecimalAsserts.checkEquals("NetAmountBeforeDiscount is wrong",
                    expected.getNetAmountBeforeDiscount(),
                    actual.getNetAmountBeforeDiscount());

            assertEquals("Percent is wrong",
                    Float.valueOf(expected.getPercent()),
                    Float.valueOf(actual.getPercent()));

            return true;
        }
    }

}
