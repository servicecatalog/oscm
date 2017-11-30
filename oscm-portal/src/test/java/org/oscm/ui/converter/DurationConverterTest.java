/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.converter;

import static org.oscm.test.Numbers.L5;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.convert.ConverterException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.Constants;
import org.oscm.ui.common.DurationValidation;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIComponentStub;

public class DurationConverterTest {

    private static final String DATA_TYPE = "dataType";
    private static final String REQUIRED = "required";
    private static final String MIN_VALUE = "minValue";
    private static final String MAX_VALUE = "maxValue";

    private FacesContextStub context;
    private DurationConverter converter;

    private static final UIComponentStub getComponent(boolean mandatory,
            Long min, Long max) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DATA_TYPE, "duration");
        map.put(REQUIRED, Boolean.valueOf(mandatory));
        map.put(MIN_VALUE, min);
        map.put(MAX_VALUE, max);
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        return new UIComponentStub(map);
    }

    @Before
    public void setup() {
        converter = new DurationConverter();
        context = new FacesContextStub(Locale.ENGLISH);
    }

    @Test
    public void testGetAsObject() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        Object result = converter.getAsObject(context, component, "1");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals(
                String.valueOf(DurationValidation.MILLISECONDS_PER_DAY), result);
    }

    @Test
    public void testGetAsObjectDecimalSeparator() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        Object result = converter.getAsObject(context, component, "1,00");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals(
                String.valueOf(DurationValidation.MILLISECONDS_PER_DAY * 100),
                result);
    }

    @Test
    public void testGetAsObjectZero() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        Object result = converter.getAsObject(context, component, "0");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals("0", result);
    }

    @Test
    public void testGetAsObjectNull() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        Object result = converter.getAsObject(context, component, null);
        Assert.assertNull(result);
    }

    @Test
    public void testGetAsObjectEmpty() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        Object result = converter.getAsObject(context, component, "");
        Assert.assertNull(result);
    }

    @Test(expected = ConverterException.class)
    public void testGetAsObjectNullMandatory() throws ConverterException {
        UIComponent component = getComponent(true, null, null);
        converter.getAsObject(context, component, null);
    }

    @Test(expected = ConverterException.class)
    public void testGetAsObjectEmptyMandatory() throws ConverterException {
        UIComponent component = getComponent(true, null, null);
        converter.getAsObject(context, component, "");
    }

    @Test(expected = ConverterException.class)
    public void testGetAsObjectThreeFractionDigits() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        converter.getAsObject(context, component, "2.345");
    }

    @Test(expected = ConverterException.class)
    public void testGetAsObjectNegative() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        converter.getAsObject(context, component, "-1.5");
    }

    @Test(expected = ConverterException.class)
    public void testGetAsObjectTwoDecimalSeparators() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        converter.getAsObject(context, component, "1.57.56");
    }

    @Test
    public void testGetAsObjectLowerRange() throws ConverterException {
        UIComponent component = getComponent(false, L5, null);
        Object result = converter.getAsObject(context, component, "10");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals(
                String.valueOf(10 * DurationValidation.MILLISECONDS_PER_DAY),
                result);
    }

    @Test
    public void testGetAsObjectLowerRangeValue() throws ConverterException {
        UIComponent component = getComponent(false, L5, null);
        Object result = converter.getAsObject(context, component, "5");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals(
                String.valueOf(5 * DurationValidation.MILLISECONDS_PER_DAY),
                result);
    }

    @Test(expected = ConverterException.class)
    public void testGetAsObjectLowerRangeBelow() throws ConverterException {
        UIComponent component = getComponent(false, L5, null);
        converter.getAsObject(context, component, "3.00");
    }

    @Test
    public void testGetAsObjectUpperRange() throws ConverterException {
        UIComponent component = getComponent(false, null, L5);
        Object result = converter.getAsObject(context, component, "3");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals(
                String.valueOf(3 * DurationValidation.MILLISECONDS_PER_DAY),
                result);
    }

    @Test
    public void testGetAsObjectUpperRangeValue() throws ConverterException {
        UIComponent component = getComponent(false, null, L5);
        Object result = converter.getAsObject(context, component, "5");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals(
                String.valueOf(5 * DurationValidation.MILLISECONDS_PER_DAY),
                result);
    }

    @Test(expected = ConverterException.class)
    public void testGetAsObjectUpperRangeAbove() throws ConverterException {
        UIComponent component = getComponent(false, null, L5);
        converter.getAsObject(context, component, "10.00");
    }

    @Test(expected = ConverterException.class)
    public void testGetAsObjectLongMax() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        converter.getAsObject(context, component,
                String.valueOf(Long.MAX_VALUE));
    }

    @Test
    public void testGetAsObjectGerman() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        context.setLocale(Locale.GERMAN);
        Object result = converter.getAsObject(context, component, "1");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals(
                String.valueOf(DurationValidation.MILLISECONDS_PER_DAY), result);
    }

    @Test
    public void testGetAsObjectGermanDecimalSeparator()
            throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        context.setLocale(Locale.GERMAN);
        Object result = converter.getAsObject(context, component, "1.00");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals(
                String.valueOf(DurationValidation.MILLISECONDS_PER_DAY * 100),
                result);
    }

    @Test
    public void testGetAsObjectZeroGerman() throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        context.setLocale(Locale.GERMAN);
        Object result = converter.getAsObject(context, component, "0");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals("0", result);
    }

    @Test(expected = ConverterException.class)
    public void testGetAsObjectTwoDecimalSeparatorsGerman()
            throws ConverterException {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        context.setLocale(Locale.GERMAN);
        converter.getAsObject(context, component, "1,57,56");
    }

    @Test
    public void testGetAsStringNull() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        String result = converter.getAsString(context, component, null);
        Assert.assertNull(result);
    }

    @Test
    public void testGetAsStringEmpty() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        String result = converter.getAsString(context, component, "");
        Assert.assertNull(result);
    }

    @Test
    public void testGetAsStringWhitespaces() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        String result = converter.getAsString(context, component, "   ");
        Assert.assertNull(result);
    }

    @Test
    public void testGetAsString() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        String result = converter.getAsString(context, component,
                String.valueOf(DurationValidation.MILLISECONDS_PER_DAY));
        Assert.assertEquals("1", result);
    }

    @Test
    public void testGetAsString_ThreeDays() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        String result = converter.getAsString(context, component,
                String.valueOf(3 * DurationValidation.MILLISECONDS_PER_DAY));
        Assert.assertEquals("3", result);
    }

    @Test
    public void testGetAsStringGerman() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        context.setLocale(Locale.GERMAN);
        String result = converter.getAsString(context, component,
                String.valueOf(DurationValidation.MILLISECONDS_PER_DAY));
        Assert.assertEquals("1", result);
    }

    @Test
    public void testGetAsStringWithFraction() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        long longValue = (long) (DurationValidation.MILLISECONDS_PER_DAY * 1.5);
        String result = converter.getAsString(context, component,
                String.valueOf(longValue));
        Assert.assertEquals("1", result);
    }

    @Test
    public void testGetAsStringWithLongInput() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        String result = converter.getAsString(context, component,
                Long.valueOf(DurationValidation.MILLISECONDS_PER_DAY));
        Assert.assertEquals("1", result);
    }

    @Test
    public void testGetAsStringGermanWithLongInput() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        context.setLocale(Locale.GERMAN);
        String result = converter.getAsString(context, component,
                Long.valueOf(DurationValidation.MILLISECONDS_PER_DAY));
        Assert.assertEquals("1", result);
    }

    @Test(expected = ConverterException.class)
    public void testGetAsStringWithDoubleInput() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        Double value = Double
                .valueOf(DurationValidation.MILLISECONDS_PER_DAY * 1.5);
        converter.getAsString(context, component, value);
    }

    @Test(expected = ConverterException.class)
    public void testGetAsStringGermanWithDoubleInput() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        context.setLocale(Locale.GERMAN);
        Double value = Double
                .valueOf(DurationValidation.MILLISECONDS_PER_DAY * 1.5);
        converter.getAsString(context, component, value);
    }

    @Test(expected = ConverterException.class)
    public void testGetAsStringInvalidInput() {
        UIComponent component = ConverterTestHelper.getComponent(false, null,
                null, "duration");
        converter.getAsString(context, component, "�lkjhgfd");
    }
}
