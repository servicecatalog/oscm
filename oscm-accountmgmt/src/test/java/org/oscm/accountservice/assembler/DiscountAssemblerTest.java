/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 21.11.2011                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.assembler;

import static org.oscm.test.Numbers.L100;
import static org.oscm.test.Numbers.L200;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.Discount;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VODiscount;

/**
 * Test for the discount assembler.
 * 
 * @author Enes Sejfi
 * 
 */
public class DiscountAssemblerTest {

    @Test
    public void constructor() throws Exception {
        new DiscountAssembler();
    }

    @Test
    public void toVODiscount() throws Exception {
        // given
        Discount discount = givenDiscount(L100, L200);

        // when
        VODiscount voDiscount = DiscountAssembler.toVODiscount(discount);

        // then
        assertEquals(discount.getKey(), voDiscount.getKey());
        assertEquals(0, voDiscount.getVersion());
        assertEquals(discount.getValue(), voDiscount.getValue());
        assertEquals(L100, voDiscount.getStartTime());
        assertEquals(L200, voDiscount.getEndTime());
    }

    private Discount givenDiscount(Long actualTime, Long endTime) {
        Discount discount = new Discount();
        discount.setKey(643);
        discount.setValue(BigDecimal.valueOf(32));
        discount.setStartTime(actualTime);
        discount.setEndTime(endTime);
        return discount;
    }

    @Test
    public void toDiscount() throws Exception {
        // given
        VODiscount voDiscount = givenVoDiscount(L100, L200);

        // when
        Discount discount = DiscountAssembler.toDiscount(voDiscount);

        // then
        assertEquals(voDiscount.getKey(), discount.getKey());
        assertEquals(voDiscount.getVersion(), 0);
        assertEquals(voDiscount.getValue(), discount.getValue());
        assertEquals(L100, discount.getStartTime());
        assertEquals(L200, discount.getEndTime());
    }

    private VODiscount givenVoDiscount(Long startTime, Long endTime) {
        return givenVoDiscount(startTime, endTime, BigDecimal.valueOf(32));
    }

    private VODiscount givenVoDiscount(Long startTime, Long endTime,
            BigDecimal value) {
        VODiscount voDiscount = new VODiscount();
        voDiscount.setKey(643);
        voDiscount.setValue(value);
        voDiscount.setStartTime(startTime);
        voDiscount.setEndTime(endTime);
        return voDiscount;
    }

    @Test
    public void updateDiscount() throws Exception {
        // given
        VODiscount voDiscount = givenVoDiscount(L100, L200);
        Discount oldDiscount = givenDiscount(L100, L200);

        // when
        Discount discount = DiscountAssembler.updateDiscount(voDiscount,
                oldDiscount);

        // then
        assertEquals(voDiscount.getKey(), discount.getKey());
        assertEquals(voDiscount.getVersion(), 0);
        assertEquals(voDiscount.getValue(), discount.getValue());
        assertEquals(L100, discount.getStartTime());
        assertEquals(L200, discount.getEndTime());
    }

    @Test(expected = ValidationException.class)
    public void validate_discountValueIsNotSet() throws ValidationException {
        // given
        VODiscount discount = givenVoDiscount(L100, L200, null);

        // when
        DiscountAssembler.validate(discount);
    }

    @Test(expected = ValidationException.class)
    public void validate_discountIsLessThanMin() throws ValidationException {
        // given
        VODiscount discount = givenVoDiscount(L100, L200,
                BigDecimal.valueOf(-1));

        // when
        DiscountAssembler.validate(discount);
    }

    @Test(expected = ValidationException.class)
    public void validate_discountIsInMinValue() throws ValidationException {
        // given
        VODiscount discount = givenVoDiscount(L100, L200, BigDecimal.ZERO);

        // when
        DiscountAssembler.validate(discount);
    }

    @Test
    public void validate_discountIsInRange() throws ValidationException {
        // given
        VODiscount discount = givenVoDiscount(L100, L200,
                BigDecimal.valueOf(50));

        // when
        DiscountAssembler.validate(discount);
    }

    @Test
    public void validate_discountIsInMaxRange() throws ValidationException {
        // given
        VODiscount discount = givenVoDiscount(L100, L200,
                BigDecimal.valueOf(100));

        // when
        DiscountAssembler.validate(discount);
    }

    @Test(expected = ValidationException.class)
    public void validate_discountGreaterThanMaxValue()
            throws ValidationException {
        // given
        VODiscount discount = givenVoDiscount(L100, L200,
                BigDecimal.valueOf(100.01));

        // when
        DiscountAssembler.validate(discount);
    }

    @Test(expected = ValidationException.class)
    public void validate_discountStartTimeGreaterThanEndTime()
            throws ValidationException {
        // given
        VODiscount discount = givenVoDiscount(L200, L100,
                BigDecimal.valueOf(50));

        // when
        DiscountAssembler.validate(discount);
    }

    @Test(expected = ValidationException.class)
    public void validate_discountNotInScale() throws Exception {
        // given
        VODiscount discount = givenVoDiscount(
                L200,
                L100,
                BigDecimal.valueOf(50).setScale(
                        PriceConverter.NUMBER_OF_DECIMAL_PLACES + 1));

        // when
        DiscountAssembler.validate(discount);
    }

    @Test
    public void toVODiscount_NullParameter() throws Exception {
        // given
        Discount d = null;

        // when
        VODiscount voDiscount = DiscountAssembler.toVODiscount(d);

        // then
        assertNull(voDiscount);
    }
}
