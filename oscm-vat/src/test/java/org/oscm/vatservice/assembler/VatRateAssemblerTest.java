/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Oct 1, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vatservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOVatRate;

/**
 * @author kulle
 * 
 */
public class VatRateAssemblerTest {

    @Test
    public void validate() throws Exception {
        // given
        VOVatRate vatRate = givenVatRate(BigDecimal.ONE.setScale(1));

        // when
        VatRateAssembler.validate(vatRate);
    }

    private VOVatRate givenVatRate(BigDecimal rate) {
        VOVatRate vatRate = new VOVatRate();
        vatRate.setKey(1L);
        vatRate.setVersion(0);
        vatRate.setRate(rate);
        return vatRate;
    }

    @Test
    public void validate_nullRate() throws Exception {
        // given
        VOVatRate vatRate = givenVatRate(null);

        // when
        try {
            VatRateAssembler.validate(vatRate);
            fail();
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.VAT, e.getReason());
        }
    }

    @Test
    public void validate_maxAllowedScale() throws Exception {
        // given
        VOVatRate vatRate = givenVatRate(BigDecimal.ONE.setScale(2));

        // when
        VatRateAssembler.validate(vatRate);
    }

    @Test
    public void validate_scaleToLong() throws Exception {
        // given
        VOVatRate vatRate = givenVatRate(BigDecimal.ONE.setScale(3));

        // when
        try {
            VatRateAssembler.validate(vatRate);
            fail();
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.VAT, e.getReason());
        }
    }

    @Test
    public void validate_zeroValue() throws Exception {
        // given
        VOVatRate vatRate = givenVatRate(BigDecimal.ZERO.setScale(2));

        // when
        VatRateAssembler.validate(vatRate);
    }

    @Test
    public void validate_negativeValue() throws Exception {
        // given
        VOVatRate vatRate = givenVatRate(BigDecimal.ONE.negate().setScale(2));

        // when
        try {
            VatRateAssembler.validate(vatRate);
            fail();
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.VAT, e.getReason());
        }
    }

}
