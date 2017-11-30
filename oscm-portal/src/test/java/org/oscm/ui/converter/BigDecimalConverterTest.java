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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.convert.ConverterException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.stubs.FacesContextStub;

/**
 * JUnit tests for BigDecimalConverter class.
 * 
 * @author Aleh Khomich.
 * 
 */
public class BigDecimalConverterTest {

    private FacesContextStub context;
    private BigDecimalConverter converter;

    /**
     * Setup test method.
     */
    @Before
    public void setup() {
        converter = new BigDecimalConverter();
        context = new FacesContextStub(Locale.ENGLISH);
    }

    /**
     * Test for getting value.
     */
    @Test(expected = ConverterException.class)
    public void getAsObject_NotDigitsMiddle() throws ConverterException {
        String str = "1abc1";
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discount");

        converter.getAsObject(context, component, str);
    }

    /**
     * Test for getting value.
     */
    @Test(expected = ConverterException.class)
    public void getAsObject_NotDigitsAfter() throws ConverterException {
        String str = "100abc";
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discount");

        converter.getAsObject(context, component, str);
    }

    /**
     * Test for getting value.
     */
    @Test(expected = ConverterException.class)
    public void getAsObject_NotDigits() throws ConverterException {
        String str = "abc";
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discount");

        converter.getAsObject(context, component, str);
    }

    /**
     * Test for getting value.
     */
    @Test
    public void getAsObject_Zero() throws ConverterException {
        String str = "0";
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discount");

        BigDecimal expected = new BigDecimal(str);

        BigDecimal actual = (BigDecimal) converter.getAsObject(context,
                component, str);

        assertEquals(expected, actual);
    }

    /**
     * Test for getting value. Integer
     */
    @Test
    public void getAsObject() throws ConverterException {
        String str = "1";
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discount");
        BigDecimal expected = new BigDecimal(str);

        BigDecimal actual = (BigDecimal) converter.getAsObject(context,
                component, str);

        assertEquals(expected, actual);
    }

    /**
     * Test for getting value.
     */
    @Test
    public void getAsObject_4Decimals() throws ConverterException {
        String str = "0.0005";
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discount");
        BigDecimal expected = new BigDecimal(str);

        BigDecimal actual = (BigDecimal) converter.getAsObject(context,
                component, str);

        assertEquals(0, expected.compareTo(actual));
    }

    /**
     * Test for getting value for German local.
     */
    @Test
    public void getAsObject_GermanLocale() throws ConverterException {
        context = new FacesContextStub(Locale.GERMAN);

        String str = "1,23";
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discount");
        BigDecimal expected = new BigDecimal("1.23");

        BigDecimal actual = (BigDecimal) converter.getAsObject(context,
                component, str);

        assertEquals(expected, actual);
        context = new FacesContextStub(Locale.ENGLISH);
    }

    @Test
    public void getAsObject_empty() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discount");
        BigDecimal actual = (BigDecimal) converter.getAsObject(context,
                component, "");
        assertNull(actual);
    }

    /**
     * Test for getting string of the value.
     */
    @Test
    public void getAsString() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discount");
        String expected = "1.00";
        BigDecimal value = new BigDecimal(expected);

        String actual = converter.getAsString(context, component, value);

        assertEquals(expected, actual);
    }

    @Test
    public void getAsString_null() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "discount");

        String actual = converter.getAsString(context, component, null);

        assertNull(actual);
    }

}
