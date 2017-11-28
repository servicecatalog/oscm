/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Sep 22, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.converter.PriceConverter;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOPriceModel;

/**
 * Unit tests for {@link PriceModelAssembler}.
 * 
 * @author barzu
 */
public class PriceModelAssemblerTest {

    /**
     * A chargeable PriceModel requires the Period.
     */
    @Test
    public void validatePriceModelSettings_NoPeriod() throws Exception {
        // given
        VOPriceModel priceModel = givenPriceModel();

        try {
            // when
            PriceModelAssembler.validatePriceModelSettings(priceModel);
            fail("ValidationException(reason REQUIRED (parameters=[period])) expected.");
        } catch (ValidationException e) {

            // then
            assertEquals(ReasonEnum.REQUIRED, e.getReason());
            assertEquals(PriceModelAssembler.FIELD_NAME_PERIOD, e.getMember());
        }
    }

    private VOPriceModel givenPriceModel() {
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        return priceModel;
    }

    private VOPriceModel givenPriceModel(BigDecimal oneTimeFee,
            BigDecimal pricePerPeriod, BigDecimal pricePerUserAssignment) {
        VOPriceModel priceModel = givenPriceModel();
        priceModel.setOneTimeFee(oneTimeFee);
        priceModel.setPricePerPeriod(pricePerPeriod);
        priceModel.setPricePerUserAssignment(pricePerUserAssignment);
        return priceModel;
    }

    /**
     * Validate the price model settings in another case.
     */
    @Test
    public void validatePriceModelSettings() throws Exception {
        // given
        VOPriceModel priceModel = givenPriceModel();
        priceModel.setPeriod(PricingPeriod.WEEK);

        // when
        PriceModelAssembler.validatePriceModelSettings(priceModel);
    }

    /**
     * A chargeable PriceModel requires the Period.
     */
    @Test
    public void validatePriceModelSettings_Negative_FreePeriod()
            throws Exception {
        // given
        VOPriceModel priceModel = givenPriceModel();
        priceModel.setFreePeriod(-1);

        try {
            // when
            PriceModelAssembler.validatePriceModelSettings(priceModel);
            fail("ValidationException(reason POSITIVE_NUMBER (parameters=[freePeriod])) expected.");
        } catch (ValidationException e) {

            // then
            assertEquals(ReasonEnum.POSITIVE_NUMBER, e.getReason());
            assertEquals(PriceModelAssembler.FIELD_NAME_FREE_PERIOD,
                    e.getMember());
        }
    }

    @Test
    public void getCurrency_EurIsoCode() throws Exception {
        // given
        VOPriceModel priceModel = new VOPriceModel();

        // when
        priceModel.setCurrencyISOCode("EUR");

        // then
        assertEquals("[EUR]", priceModel.getCurrency());
    }

    @Test
    public void getCurrency_EmptyIsoCode() throws Exception {
        VOPriceModel priceModel = new VOPriceModel();
        assertEquals("", priceModel.getCurrency());
    }

    @Test
    public void validatePriceModelSettings_oneTimeFeeNotInScale() {
        // given
        VOPriceModel priceModel = givenPriceModel(
                BigDecimal.ONE
                        .setScale(PriceConverter.NUMBER_OF_DECIMAL_PLACES + 1),
                BigDecimal.TEN, BigDecimal.ZERO);

        try {
            // when
            PriceModelAssembler.validatePriceModelSettings(priceModel);
            fail("scale of 'One Time Fee' not in range");
        } catch (ValidationException e) {

            // then
            assertEquals(ReasonEnum.SCALE_TO_LONG, e.getReason());
            assertEquals(PriceModelAssembler.FIELD_NAME_ONE_TIME_FEE,
                    e.getMember());
        }
    }

    @Test
    public void validatePriceModelSettings_pricePerPeriodNotInScale() {
        // given
        VOPriceModel priceModel = givenPriceModel(BigDecimal.ONE,
                BigDecimal.TEN
                        .setScale(PriceConverter.NUMBER_OF_DECIMAL_PLACES + 1),
                BigDecimal.ZERO);

        try {
            // when
            PriceModelAssembler.validatePriceModelSettings(priceModel);
            fail("scale of 'Price Per Period' not in range");
        } catch (ValidationException e) {

            // then
            assertEquals(ReasonEnum.SCALE_TO_LONG, e.getReason());
            assertEquals(PriceModelAssembler.FIELD_NAME_PRICE_PERIOD,
                    e.getMember());
        }
    }

    @Test
    public void validatePriceModelSettings_pricePerUserAssignmentNotInScale() {
        // given
        VOPriceModel priceModel = givenPriceModel(BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.ZERO
                        .setScale(PriceConverter.NUMBER_OF_DECIMAL_PLACES + 1));

        try {
            // when
            PriceModelAssembler.validatePriceModelSettings(priceModel);
            fail("scale of 'Price Per User Assignment' not in range");
        } catch (ValidationException e) {

            // then
            assertEquals(ReasonEnum.SCALE_TO_LONG, e.getReason());
            assertEquals(PriceModelAssembler.FIELD_NAME_PRICE_USERASSIGNMENT,
                    e.getMember());
        }
    }
}
