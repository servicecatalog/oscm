/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich.                                                      
 *                                                                              
 *  Creation Date: 13.07.2010.                                                      
 *                                                                              
 *  Completion Time: 13.07.2010.                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.serviceprovisioningservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.test.Numbers;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOSteppedPrice;

/**
 * Test class for SteppedPrice assembler.
 * 
 * @author Aleh Khomich.
 * 
 */
public class SteppedPriceAssemblerTest {

    /**
     * Test for converting from list of domain objects to list of value objects
     */
    @Test
    public void toVOSteppedPrices() throws Exception {
        // given
        List<SteppedPrice> steppedPriceList = new ArrayList<SteppedPrice>();
        long[] steppedPriceKeyArray = { 1, 2, 3 };
        long[] pricedEventKeyArray = { 10, 20, 30 };
        long[] pricedParameterKeyArray = { 100, 200, 300 };
        for (int i = 0; i < 3; i++) {
            SteppedPrice steppedPrice = newSteppedPrice(
                    steppedPriceKeyArray[i], pricedEventKeyArray[i],
                    pricedParameterKeyArray[i]);
            steppedPriceList.add(steppedPrice);
        }

        // when
        List<VOSteppedPrice> voList = SteppedPriceAssembler
                .toVOSteppedPrices(steppedPriceList);

        // then
        for (int i = 0; i < 3; i++) {
            assertEquals(steppedPriceKeyArray[i], voList.get(i).getKey());
        }
    }

    /**
     * Test for converting from value object to domain objects.
     */
    @Test
    public void toSteppedPrice() throws Exception {
        // given
        BigDecimal price = BigDecimal.valueOf(100L);
        Long limit = Numbers.L300;
        VOSteppedPrice voSteppedPrice = new VOSteppedPrice();
        voSteppedPrice.setPrice(price);
        voSteppedPrice.setLimit(limit);

        // when
        SteppedPrice steppedPrice = SteppedPriceAssembler
                .toSteppedPrice(voSteppedPrice);

        // then
        assertEquals(price, steppedPrice.getPrice());
        assertEquals(limit, steppedPrice.getLimit());
    }

    /**
     * Test for validation of stepped price. Null input. Exception is expected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void validateSteppedPrice_nullInput() throws Exception {
        // given
        VOSteppedPrice steppedPrice = null;

        // when
        SteppedPriceAssembler.validateSteppedPrice(steppedPrice);
    }

    /**
     * Test for validation of stepped price. Negative price. Exception is
     * expected.
     */
    @Test(expected = ValidationException.class)
    public void validateSteppedPrice_negativePrice() throws Exception {
        // given
        BigDecimal price = BigDecimal.valueOf(-100L);
        VOSteppedPrice voSteppedPrice = new VOSteppedPrice();
        voSteppedPrice.setPrice(price);

        // when
        SteppedPriceAssembler.validateSteppedPrice(voSteppedPrice);
    }

    /**
     * Test for validation of stepped price. Negative limit. Exception is
     * expected.
     */
    @Test(expected = ValidationException.class)
    public void validateSteppedPrice_NegativeLimit() throws Exception {
        // given
        VOSteppedPrice voSteppedPrice = new VOSteppedPrice();
        voSteppedPrice.setLimit(Numbers.Lm100);

        // when
        SteppedPriceAssembler.validateSteppedPrice(voSteppedPrice);
    }

    @Test(expected = ValidationException.class)
    public void validateSteppedPrice_Bug10252_Zero() throws Exception {
        // given
        VOSteppedPrice voSteppedPrice = new VOSteppedPrice();
        voSteppedPrice.setLimit(Numbers.L0);

        // when
        SteppedPriceAssembler.validateSteppedPrice(voSteppedPrice);
    }

    @Test
    public void validateSteppedPrice_Bug10252_OK() throws Exception {
        // given
        VOSteppedPrice voSteppedPrice = new VOSteppedPrice();
        voSteppedPrice.setLimit(Numbers.L1);

        // when
        SteppedPriceAssembler.validateSteppedPrice(voSteppedPrice);
    }

    /**
     * Test for converting from list of domain objects to list of value objects
     */
    @Test
    public void validateSteppedPrice() throws Exception {
        // given
        List<VOSteppedPrice> steppedPriceList = givenSteppedPriceList();

        // when
        SteppedPriceAssembler.validateSteppedPrice(steppedPriceList);
    }

    private List<VOSteppedPrice> givenSteppedPriceList() {
        List<VOSteppedPrice> steppedPriceList = new ArrayList<VOSteppedPrice>();
        BigDecimal[] price = { BigDecimal.valueOf(1L), BigDecimal.valueOf(2L),
                BigDecimal.valueOf(3L), BigDecimal.valueOf(4) };
        Long[] limit = { Numbers.L100, Numbers.L200, Numbers.L300, null };
        for (int i = 0; i < 4; i++) {
            VOSteppedPrice steppedPrice = newVOSteppedPrice(price[i], limit[i]);
            steppedPriceList.add(steppedPrice);
        }
        return steppedPriceList;
    }

    /**
     * Test for converting from list of domain objects to list of value objects.
     * Duplicate value.
     */
    @Test(expected = ValidationException.class)
    public void validateSteppedPrice_duplicateValue() throws Exception {
        List<VOSteppedPrice> steppedPriceList = givenSteppedPriceList();
        steppedPriceList.get(2).setLimit(Numbers.L100);

        // when
        SteppedPriceAssembler.validateSteppedPrice(steppedPriceList);
    }

    private VOSteppedPrice newVOSteppedPrice(BigDecimal price, Long limit) {
        long key = 1L;
        int version = 2;
        VOSteppedPrice voSteppedPrice = new VOSteppedPrice();
        voSteppedPrice.setKey(key);
        voSteppedPrice.setVersion(version);
        voSteppedPrice.setPrice(price);
        voSteppedPrice.setLimit(limit);
        return voSteppedPrice;
    }

    private SteppedPrice newSteppedPrice(long steppedPriceKey,
            long pricedEventKey, long pricedParameterKey) {
        PricedEvent pricedEvent = new PricedEvent();
        pricedEvent.setKey(pricedEventKey);
        Event event = new Event();
        event.setKey(3);
        pricedEvent.setEvent(event);

        PricedParameter pricedParameter = new PricedParameter();
        pricedParameter.setKey(pricedParameterKey);
        ParameterDefinition parameterDefinition = new ParameterDefinition();
        parameterDefinition.setKey(6);
        Parameter parameter = new Parameter();
        parameter.setKey(5);
        parameter.setParameterDefinition(parameterDefinition);
        pricedParameter.setParameter(parameter);

        PriceModel priceModel = new PriceModel();
        priceModel.setKey(7);

        SteppedPrice steppedPrice = new SteppedPrice();
        steppedPrice.setKey(steppedPriceKey);
        steppedPrice.setPricedEvent(pricedEvent);
        steppedPrice.setPricedParameter(pricedParameter);
        steppedPrice.setPriceModel(priceModel);
        return steppedPrice;
    }

    @Test
    public void validateSteppedPrice_price_scaleNotInRange() throws Exception {
        // given
        List<VOSteppedPrice> steppedPriceList = givenSteppedPriceList();
        steppedPriceList.get(0).setPrice(
                BigDecimal.TEN
                        .setScale(PriceConverter.NUMBER_OF_DECIMAL_PLACES + 1));

        // when
        try {
            SteppedPriceAssembler.validateSteppedPrice(steppedPriceList);
            fail();
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.SCALE_TO_LONG, e.getReason());
        }
    }

}
