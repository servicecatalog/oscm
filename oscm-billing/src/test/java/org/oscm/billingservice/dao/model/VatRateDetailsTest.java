/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 19.11.2010                                                      
 *                                                                              
 *  Completion Time: 19.11.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.billingservice.dao.model.VatRateDetails;

/**
 * Test for the VAT rate details object.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class VatRateDetailsTest {

    @Test
    public void testGetEffectiveVatRateForCustomer_NullInput() {
        VatRateDetails vrd = createVRD(null, null, null);
        BigDecimal result = vrd.getEffectiveVatRateForCustomer();
        assertNull(result);
    }

    @Test
    public void testGetEffectiveVatRateForCustomer_OnlyDefault() {
        VatRateDetails vrd = createVRD(BigDecimal.valueOf(12), null, null);
        BigDecimal result = vrd.getEffectiveVatRateForCustomer();
        assertEquals(BigDecimal.valueOf(12), result);
    }

    @Test
    public void testGetEffectiveVatRateForCustomer_OnlyCountry() {
        VatRateDetails vrd = createVRD(null, BigDecimal.valueOf(13), null);
        BigDecimal result = vrd.getEffectiveVatRateForCustomer();
        assertEquals(BigDecimal.valueOf(13), result);
    }

    @Test
    public void testGetEffectiveVatRateForCustomer_OnlyCustomer() {
        VatRateDetails vrd = createVRD(null, null, BigDecimal.valueOf(14));
        BigDecimal result = vrd.getEffectiveVatRateForCustomer();
        assertEquals(BigDecimal.valueOf(14), result);
    }

    @Test
    public void testGetEffectiveVatRateForCustomer_DefAndCountry() {
        VatRateDetails vrd = createVRD(BigDecimal.valueOf(14),
                BigDecimal.valueOf(15), null);
        BigDecimal result = vrd.getEffectiveVatRateForCustomer();
        assertEquals(BigDecimal.valueOf(15), result);
    }

    @Test
    public void testGetEffectiveVatRateForCustomer_DefAndCust() {
        VatRateDetails vrd = createVRD(BigDecimal.valueOf(14), null,
                BigDecimal.valueOf(15));
        BigDecimal result = vrd.getEffectiveVatRateForCustomer();
        assertEquals(BigDecimal.valueOf(15), result);
    }

    @Test
    public void testGetEffectiveVatRateForCustomer_CountryAndCust() {
        VatRateDetails vrd = createVRD(null, BigDecimal.valueOf(14),
                BigDecimal.valueOf(15));
        BigDecimal result = vrd.getEffectiveVatRateForCustomer();
        assertEquals(BigDecimal.valueOf(15), result);
    }

    @Test
    public void testGetEffectiveVatRateForCustomer_AllSet() {
        VatRateDetails vrd = createVRD(BigDecimal.valueOf(13),
                BigDecimal.valueOf(14), BigDecimal.valueOf(15));
        BigDecimal result = vrd.getEffectiveVatRateForCustomer();
        assertEquals(BigDecimal.valueOf(15), result);
    }

    @Test
    public void testGetVatAmount_NullNetCosts() throws Exception {
        VatRateDetails vrd = createVRD(null, null, null);
        vrd.setTotalCosts(new BigDecimal(5));
        BigDecimal result = vrd.getVatAmount();
        assertEquals(new BigDecimal(5), result);
    }

    @Test
    public void testGetVatAmount_NullTotalCosts() throws Exception {
        VatRateDetails vrd = createVRD(null, null, null);
        vrd.setNetCosts(new BigDecimal(5));
        BigDecimal result = vrd.getVatAmount();
        assertEquals(new BigDecimal(0), result);
    }

    @Test
    public void testGetVatAmount_CheckDiff() throws Exception {
        VatRateDetails vrd = createVRD(null, null, null);
        vrd.setNetCosts(new BigDecimal(5));
        vrd.setTotalCosts(new BigDecimal(9));
        BigDecimal result = vrd.getVatAmount();
        assertEquals(new BigDecimal(4), result);
    }

    // ------------------------------------------------------------------------
    // internal methods

    /**
     * Creates a vat rate details object with the given settings.
     */
    private VatRateDetails createVRD(BigDecimal defaultVAT,
            BigDecimal countryVAT, BigDecimal customerVAT) {
        VatRateDetails result = new VatRateDetails();
        result.setDefaultVatRate(defaultVAT);
        result.setCountryVatRate(countryVAT);
        result.setCustomerVatRate(customerVAT);
        return result;
    }

}
