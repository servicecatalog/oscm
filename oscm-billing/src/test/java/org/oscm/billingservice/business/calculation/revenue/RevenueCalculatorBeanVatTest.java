/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.calculation.revenue.model.OverallCosts;
import org.oscm.billingservice.business.model.billingresult.BillingDetailsType;
import org.oscm.billingservice.business.model.billingresult.OverallCostsType;
import org.oscm.billingservice.business.model.billingresult.VATType;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.model.VatRateDetails;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.BillingResult;
import org.oscm.test.BigDecimalAsserts;

/**
 * @author kulle
 * 
 */
public class RevenueCalculatorBeanVatTest {

    private RevenueCalculatorBean bean;
    private BillingDataRetrievalServiceLocal bdr;

    private BillingResult billingResult;
    private BillingInput billingInput;
    private BillingDetailsType billingDetails;
    private OverallCosts overallCosts;
    private VatRateDetails vatForCustomer;

    @Before
    public void setup() {
        bdr = mock(BillingDataRetrievalServiceLocal.class);
        bean = spy(new RevenueCalculatorBean());
        bean.bdr = bdr;

        billingInput = givenBillingInput("EUR");
        billingDetails = givenBillingDetails();
        billingResult = givenBillingResult();
        overallCosts = givenOverallCosts("EUR", BigDecimal.ZERO);

        vatForCustomer = mock(VatRateDetails.class);
        doReturn(vatForCustomer).when(bdr).loadVATForCustomer(anyLong(),
                anyLong(), anyLong());
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

    private OverallCosts givenOverallCosts(String currency, BigDecimal amount) {
        OverallCosts costs = OverallCosts.newInstance();
        return costs.set(currency, amount);
    }

    private BillingResult givenBillingResult() {
        return new BillingResult();
    }

    @Test
    public void includeVat_effectiveVatIsNull() {
        // given
        doReturn(null).when(vatForCustomer).getEffectiveVatRateForCustomer();

        // when
        bean.includeVat(billingDetails, billingInput, billingResult,
                overallCosts);

        // then
        OverallCostsType overallCostsType = billingDetails.getOverallCosts();
        verify(overallCostsType, never()).setVAT(any(VATType.class));
    }

    @Test
    public void includeVat_vat() {
        // given
        overallCosts = givenOverallCosts("EUR", BigDecimal.TEN);
        doReturn(vatForCustomer).when(bean).calculateVatCosts(
                any(VatRateDetails.class));
        doReturn(BigDecimal.ONE).when(vatForCustomer)
                .getEffectiveVatRateForCustomer();

        // when
        bean.includeVat(billingDetails, billingInput, billingResult,
                overallCosts);

        // then
        verify(vatForCustomer).setNetCosts(
                argThat(new ArgumentMatcher<BigDecimal>() {

                    @Override
                    public boolean matches(Object argument) {
                        BigDecimal b = (BigDecimal) argument;
                        BigDecimalAsserts.checkEquals(BigDecimal.TEN, b);
                        return true;
                    }
                }));
        OverallCostsType overallCostsType = billingDetails.getOverallCosts();
        verify(overallCostsType).setVAT(any(VATType.class));
    }

    @Test
    public void includeVat_grossAmount() {
        // given
        doReturn(BigDecimal.ONE).when(vatForCustomer).getTotalCosts();

        // when
        bean.includeVat(billingDetails, billingInput, billingResult,
                overallCosts);

        // then
        OverallCostsType overallCostsType = billingDetails.getOverallCosts();
        verify(overallCostsType).setGrossAmount(
                argThat(new ArgumentMatcher<BigDecimal>() {

                    @Override
                    public boolean matches(Object argument) {
                        BigDecimal b = (BigDecimal) argument;
                        BigDecimalAsserts.checkEquals(BigDecimal.ONE, b);
                        return true;
                    }
                }));
    }

}
