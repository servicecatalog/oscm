/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.billingservice.business.BigDecimalJaxbCustomBinder;

public class BigDecimalJaxbCustomBinderTest {
    @Test
    public void parseBigDecimal_int() {
        // given
        String numberAsStr = "5";

        // when
        BigDecimal number = BigDecimalJaxbCustomBinder
                .parseBigDecimal(numberAsStr);

        // then
        assertNotNull(number);
        assertEquals(new Integer(numberAsStr).intValue(), number.intValue());
    }

    @Test
    public void parseBigDecimal_double() {
        // given
        String numberAsStr = "51.98";

        // when
        BigDecimal number = BigDecimalJaxbCustomBinder
                .parseBigDecimal(numberAsStr);

        // then
        assertNotNull(number);
        assertEquals(0, new Double(numberAsStr).compareTo(new Double(number
                .doubleValue())));
    }
}
