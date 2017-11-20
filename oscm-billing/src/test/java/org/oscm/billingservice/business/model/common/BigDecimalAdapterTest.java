/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                               
 *                                                                              
 *  Creation Date: 12.09.2012                                                      
 *                                                                                                         
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.billingservice.business.BigDecimalAdapter;

/**
 * @author cheld
 * 
 */
public class BigDecimalAdapterTest {

    BigDecimalAdapter adapter = new BigDecimalAdapter();

    @Test
    public void marshal_formatDecimal() {

        // given
        BigDecimal givenValue = new BigDecimal("20.127123");

        // when
        String valueAsStr = adapter.marshal(givenValue);

        // then
        assertEquals("20.13", valueAsStr);
    }

    @Test
    public void marshal_formatInteger() {

        // given
        BigDecimal givenValue = new BigDecimal("20");

        // when
        String valueAsStr = adapter.marshal(givenValue);

        // then
        assertEquals("20.00", valueAsStr);
    }

    @Test
    public void marshal_nullValue() {
        assertNull(adapter.marshal(null));
    }

    @Test
    public void marshalAndUnmarshal() {

        // given BigDecial value
        BigDecimal givenValue = new BigDecimal("20.34");

        // when marshaling and unmarshaling again
        String valueAsStr = adapter.marshal(givenValue);
        BigDecimal convertedValue = adapter.unmarshal(valueAsStr);

        // then
        assertThat(givenValue, is(convertedValue));
    }

    @Test
    public void marshalAndUnmarshal_smallValue() {

        // given BigDecial value
        BigDecimal givenValue = new BigDecimal("0.34");

        // when marshaling and unmarshaling again
        String valueAsStr = adapter.marshal(givenValue);
        BigDecimal convertedValue = adapter.unmarshal(valueAsStr);

        // then
        assertThat(givenValue, is(convertedValue));
    }

    @Test
    public void marshalAndUnmarshal_withZeros() {

        // given BigDecial value
        BigDecimal givenValue = new BigDecimal("5.00");

        // when marshaling and unmarshaling again
        String valueAsStr = adapter.marshal(givenValue);
        BigDecimal convertedValue = adapter.unmarshal(valueAsStr);

        // then
        assertThat(givenValue, is(convertedValue));
    }

}
