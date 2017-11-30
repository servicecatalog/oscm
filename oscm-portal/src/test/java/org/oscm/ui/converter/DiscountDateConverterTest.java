/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                         
 *                                                                              
 *  Creation Date: 26.05.2010                                                      
 *                                                                              
 *  Completion Time: 26.05.2010                                    
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.convert.ConverterException;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.stubs.FacesContextStub;

/**
 * JUnit tests for DiscountDateConverte class.
 * 
 * @author Aleh Khomich.
 * 
 */
public class DiscountDateConverterTest {

    private FacesContextStub context;
    private DiscountDateConverter converter;
    private long timestampInTimeZone;

    /**
     * Setup method.
     */
    @Before
    public void setup() {
        converter = new DiscountDateConverter();
        context = new FacesContextStub(Locale.ENGLISH);
    }

    /**
     * Test for getting value.
     * 
     * @throws ParseException
     */
    @Test
    public void testGetAsObject() throws ConverterException, ParseException {
        String expectedString = "05/2010";
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
        Date expectedDate = sdf.parse("05/2010");
        timestampInTimeZone = expectedDate.getTime();
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discountDate");

        Long actual = (Long) converter.getAsObject(context, component,
                expectedString);
        Assert.assertEquals((Long) timestampInTimeZone, actual);
        testGetAsString();
    }

    /**
     * Test for getting string of the value.
     */
    public void testGetAsString() {
        Long expectedLong = timestampInTimeZone; // "05/2010"
        String expected = "05/2010";

        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discountDate");

        String actual = converter.getAsString(context, component, expectedLong);

        Assert.assertEquals(expected, actual);
    }

}
