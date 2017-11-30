/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                         
 *                                                                              
 *  Creation Date: 13.10.2010                                                      
 *                                                                              
 *  Completion Time: 13.10.2010                                    
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.converter;

import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.convert.ConverterException;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.stubs.FacesContextStub;

/**
 * JUnit tests for TrimConverter class.
 */
public class TrimConverterTest {

    private FacesContextStub context;
    private TrimConverter converter;
    private UIComponent component;

    /**
     * Setup test method.
     */
    @Before
    public void setup() {
        converter = new TrimConverter();
        context = new FacesContextStub(Locale.ENGLISH);
        component = ConverterTestHelper.getComponent(false, null, null, "id");
    }

    /**
     * Test for getting value.
     */
    @Test
    public void testGetString() throws ConverterException {

        String value = "abc";
        String expected = "abc";

        String actual = (String) converter.getAsObject(context, component,
                value);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test for getting value.
     */
    @Test
    public void testGetStringWithLeadingBlanks() throws ConverterException {

        String value = "   abc";
        String expected = "abc";

        String actual = (String) converter.getAsObject(context, component,
                value);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test for getting value.
     */
    @Test
    public void testGetStringWithTrailingBlanks() throws ConverterException {

        String value = "abc   ";
        String expected = "abc";

        String actual = (String) converter.getAsObject(context, component,
                value);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test for getting value.
     */
    @Test
    public void testGetStringWithBlanks() throws ConverterException {

        String value = "   abc   ";
        String expected = "abc";

        String actual = (String) converter.getAsObject(context, component,
                value);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test for getting value.
     */
    @Test
    public void testGetStringWithBlanksInMiddle() throws ConverterException {

        String value = "   abc  abc   ";
        String expected = "abc  abc";

        String actual = (String) converter.getAsObject(context, component,
                value);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetAsObjectOneChar() throws ConverterException {
        String in = "x";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals(in, out);
    }

    @Test
    public void testGetAsObjectEmptyString() throws ConverterException {
        String in = "";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsObjectNull() throws ConverterException {
        String in = null;
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsObjectOneSpace() throws ConverterException {
        String in = " ";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsObjectFourSpaces() throws ConverterException {
        String in = "    ";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsObjectOneThreeByteSpace() throws ConverterException {
        String in = "\u3000";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsObjectFourThreeByteSpace() throws ConverterException {
        String in = "\u3000\u3000\u3000\u3000";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsObjectSpaceAndThreeByteSpaceMixture()
            throws ConverterException {
        String in = " " + "\u3000" + " " + "\u3000" + " ";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsObjectSpaceAndThreeByteSpaceMixtureMiddle()
            throws ConverterException {
        String in = "\u3000" + "  " + "\u3000\u3000\u3000" + "  " + "\u3000";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsObjectSpaceAndThreeByteSpaceMixtureLeading()
            throws ConverterException {
        String in = " " + "\u3000" + "  " + "\u3000\u3000" + "  " + "\u3000";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsObjectSpaceAndThreeByteSpaceMixtureTrailing()
            throws ConverterException {
        String in = "\u3000" + "  " + "\u3000\u3000" + "  " + "\u3000" + "  ";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsObjectThreeByteSpace() throws ConverterException {
        String in = "\u3000" + "one" + "\u3000" + "two" + "\u3000";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals("one" + "\u3000" + "two", out);
    }

    @Test
    public void testGetAsObjectThreeByteSpaceMiddle() throws ConverterException {
        String in = "one" + "\u3000" + "two";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals("one" + "\u3000" + "two", out);
    }

    @Test
    public void testGetAsObjectThreeByteSpaceLeading()
            throws ConverterException {
        String in = "\u3000" + "one";
        String out = (String) converter.getAsObject(context, component, in);
        Assert.assertEquals("one", out);
    }

    @Test
    public void testGetAsStringOneChar() throws ConverterException {
        String in = "x";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals(in, out);
    }

    @Test
    public void testGetAsStringEmptyString() throws ConverterException {
        String in = "";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsStringNull() throws ConverterException {
        String in = null;
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsStringOneSpace() throws ConverterException {
        String in = " ";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsStringFourSpaces() throws ConverterException {
        String in = "    ";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsStringOneThreeByteSpace() throws ConverterException {
        String in = "\u3000";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsStringFourThreeByteSpace() throws ConverterException {
        String in = "\u3000\u3000\u3000\u3000";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsStringSpaceAndThreeByteSpaceMixture()
            throws ConverterException {
        String in = " " + "\u3000" + " " + "\u3000" + " ";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsStringSpaceAndThreeByteSpaceMixtureMiddle()
            throws ConverterException {
        String in = "\u3000" + "  " + "\u3000\u3000\u3000" + "  " + "\u3000";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsStringSpaceAndThreeByteSpaceMixtureLeading()
            throws ConverterException {
        String in = " " + "\u3000" + "  " + "\u3000\u3000" + "  " + "\u3000";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsStringSpaceAndThreeByteSpaceMixtureTrailing()
            throws ConverterException {
        String in = "\u3000" + "  " + "\u3000\u3000" + "  " + "\u3000" + "  ";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals(null, out);
    }

    @Test
    public void testGetAsStringThreeByteSpace() throws ConverterException {
        String in = "\u3000" + "one" + "\u3000" + "two" + "\u3000";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals("one" + "\u3000" + "two", out);
    }

    @Test
    public void testGetAsStringThreeByteSpaceMiddle() throws ConverterException {
        String in = "one" + "\u3000" + "two";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals("one" + "\u3000" + "two", out);
    }

    @Test
    public void testGetAsStringThreeByteSpaceLeading()
            throws ConverterException {
        String in = "\u3000" + "one";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals("one", out);
    }

    @Test
    public void testGetAsStringThreeByteSpaceTrailing()
            throws ConverterException {
        String in = "one" + "\u3000";
        String out = converter.getAsString(context, component, in);
        Assert.assertEquals("one", out);
    }
}
