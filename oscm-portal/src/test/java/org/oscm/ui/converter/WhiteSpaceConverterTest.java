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

import org.junit.Assert;

import org.junit.Test;

import org.oscm.converter.WhiteSpaceConverter;

/**
 * JUnit tests for WhiteSpaceConverter class. Currently only \u3000 spaces are
 * tested!
 */
public class WhiteSpaceConverterTest {

    @Test
    public void testConvertAllWhiteSpacesOneChar() {
        String in = "x";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals(in, out);
    }

    @Test
    public void testConvertAllWhiteSpacesThreeChar() {
        String in = "one";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals(in, out);
    }

    @Test
    public void testConvertAllWhiteSpacesEmptyString() {
        String in = "";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals("", out);
    }
    
    @Test
    public void testConvertAllWhiteSpacesNull() {
        String in = null;
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals("", out);
    }

    @Test
    public void testConvertAllWhiteSpacesOneSpace() {
        String in = " ";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals(in, out);
    }

    @Test
    public void testConvertAllWhiteSpacesFourSpaces() {
        String in = "    ";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals(in, out);
    }

    @Test
    public void testConvertAllWhiteSpacesOneThreeByteSpace() {
        String in = "\u3000";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals(" ", out);
    }

    @Test
    public void testConvertAllWhiteSpacesFourThreeByteSpace() {
        String in = "\u3000\u3000\u3000\u3000";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals("    ", out);
    }

    @Test
    public void testConvertAllWhiteSpacesSpaceAndThreeByteSpaceMixture() {
        String in = " " + "\u3000" + " " + "\u3000" + " ";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals("     ", out);
    }

    @Test
    public void testConvertAllWhiteSpacesSpaceAndThreeByteSpaceMixtureMiddle() {
        String in = "\u3000" + "  " + "\u3000\u3000\u3000" + "  " + "\u3000";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals("         ", out);
    }

    @Test
    public void testConvertAllWhiteSpacesSpaceAndThreeByteSpaceMixtureLeading() {
        String in = " " + "\u3000" + "  " + "\u3000\u3000" + "  " + "\u3000";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals("         ", out);
    }

    @Test
    public void testConvertAllWhiteSpacesSpaceAndThreeByteSpaceMixtureTrailing() {
        String in = "\u3000" + "  " + "\u3000\u3000" + "  " + "\u3000" + "  ";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals("          ", out);
    }

    @Test
    public void testConvertAllWhiteSpacesThreeByteSpace() {
        String in = "\u3000" + "one" + "\u3000" + "two" + "\u3000";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals(" one" + " " + "two ", out);
    }

    @Test
    public void testConvertAllWhiteSpacesThreeByteSpaceMiddle() {
        String in = "one" + "\u3000" + "two";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals("one" + " " + "two", out);
    }

    @Test
    public void testConvertAllWhiteSpacesThreeByteSpaceLeading() {
        String in = "\u3000" + "one";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals(" one", out);
    }

    @Test
    public void testConvertAllWhiteSpacesThreeByteSpaceTrailing() {
        String in = "one" + "\u3000";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals("one ", out);
    }

    @Test
    public void testConvertAllWhiteSpacesSpace() {
        String in = " " + "one" + " " + "two" + " ";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals(" one two ", out);
    }

    @Test
    public void testConvertAllWhiteSpacesSpaceMiddle() {
        String in = "one" + " " + "two";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals("one two", out);
    }

    @Test
    public void testConvertAllWhiteSpacesSpaceLeading() {
        String in = " " + "one";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals(" one", out);
    }

    @Test
    public void testConvertAllWhiteSpacesTrailing() {
        String in = "one" + " ";
        String out = WhiteSpaceConverter.replace(in);
        Assert.assertEquals("one ", out);
    }
}
