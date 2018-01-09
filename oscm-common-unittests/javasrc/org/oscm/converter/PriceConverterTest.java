/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Aleh Khomich.                                                      
 *                                                                              
 *  Creation Date: 25.01.2010                                                      
 *                                                                              
 *  Completion Time:                                               
 *                                                                              
 *******************************************************************************/
package org.oscm.converter;

import static org.oscm.test.BigDecimalAsserts.checkEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import org.junit.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.logging.LoggerFactory;

/**
 * Unit tests for PriceConverter class.
 * 
 * @author Aleh Khomich.
 * 
 */
public class PriceConverterTest {

    /** Instance of tested class. */
    private PriceConverter priceConverter;

    char[] array;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LoggerFactory.activateRollingFileAppender("./logs", null, "DEBUG");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // for future using.
    }

    @Before
    public void setUp() throws Exception {
        priceConverter = new PriceConverter(null);
    }

    @After
    public void tearDown() throws Exception {
        priceConverter = null;
    }

    @Test
    public void ctors() throws Exception {
        new PriceConverter();
        new PriceConverter(null);
    }

    @Test
    public void testgetValueToDisplay() throws Exception {
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(
                priceConverter.getActiveLocale());
        StringBuffer strMoneyBuffer = new StringBuffer();
        strMoneyBuffer.append("123");
        strMoneyBuffer.append(dfs.getDecimalSeparator());
        strMoneyBuffer.append("45");

        Assert.assertEquals(
                strMoneyBuffer.toString(),
                priceConverter.getValueToDisplay(
                        new BigDecimal(strMoneyBuffer.toString()), true));

        strMoneyBuffer.delete(0, strMoneyBuffer.length());
        strMoneyBuffer.append("123");
        strMoneyBuffer.append(dfs.getDecimalSeparator());
        strMoneyBuffer.append("00");

        Assert.assertEquals(
                "123.00",
                priceConverter.getValueToDisplay(
                        priceConverter.parse(strMoneyBuffer.toString()), true));

    }

    @Test
    public void testGetValueToDisplay_DefaultLocale() {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        Assert.assertEquals("50.00",
                converter.getValueToDisplay(BigDecimal.valueOf(50L), true));
    }

    @Test
    public void testGetValueToDisplay_DefaultLocaleNull() {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        Assert.assertEquals("0.00", converter.getValueToDisplay(null, true));
    }

    @Test
    public void testGetValueToDisplay_DefaultLocaleNegative() {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        Assert.assertEquals("-50.00",
                converter.getValueToDisplay(BigDecimal.valueOf(-50L), true));
    }

    @Test
    public void testGetValueToDisplay_DefaultLocaleMin() {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        double price = Long.MIN_VALUE;
        Assert.assertEquals(
                price,
                Double.parseDouble(converter.getValueToDisplay(
                        new BigDecimal(Long.MIN_VALUE), true).replace(",", "")),
                0.0);
    }

    @Test
    public void testGetValueToDisplay_DefaultLocaleMax() {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        double price = Long.MAX_VALUE;
        Assert.assertEquals(
                price,
                Double.parseDouble(converter.getValueToDisplay(
                        new BigDecimal(Long.MAX_VALUE), true).replace(",", "")),
                0.0);
    }

    @Test
    public void testGetValueToDisplay_GermanLocale() {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        Assert.assertEquals("50,00",
                converter.getValueToDisplay(new BigDecimal(50L), true));
    }

    @Test
    public void testGetValueToDisplay_GermanLocaleNull() {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        Assert.assertEquals("0,00", converter.getValueToDisplay(null, true));
    }

    @Test
    public void testGetValueToDisplay_GermanLocaleNegative() {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        Assert.assertEquals("-50,00",
                converter.getValueToDisplay(new BigDecimal(-50L), true));
    }

    @Test
    public void testGetFormattedValueToDisplay() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.ENGLISH);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());

        String price = "10987656.123456789123";
        Assert.assertEquals("10,987,656.123456789123",
                converter.getValueToDisplay(converter.parse(price), true));
    }

    @Test
    public void testGetFormattedValueToDisplay_GermanLocaleNoGrouping()
            throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());

        String price = "10987656,123456789123";
        Assert.assertEquals("10987656,123456789123",
                converter.getValueToDisplay(converter.parse(price), false));
    }

    @Test
    public void testGetFormattedValueToDisplay_LargeValue() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.ENGLISH);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());

        String price = "12123355781232321221231233123131213133149.12345456678899";

        Assert.assertEquals(
                "12,123,355,781,232,321,221,231,233,123,131,213,133,149.12345456678899",
                converter.getValueToDisplay(converter.parse(price), true));
    }

    @Test
    public void testGetFormattedValueToDisplay_LargeValueGermanLocale()
            throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());

        String price = "12123355781232321221231233123131213133149,12345456678899";

        Assert.assertEquals(
                "12.123.355.781.232.321.221.231.233.123.131.213.133.149,12345456678899",
                converter.getValueToDisplay(converter.parse(price), true));
    }

    @Test
    public void testGetFormattedValueToDisplay_DefaultLocaleNull() {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        Assert.assertEquals("0.00", converter.getValueToDisplay(null, true));
    }

    @Test
    public void testGetFormattedValueToDisplay_SmallValue()
            throws ParseException {
        PriceConverter converter = new PriceConverter(Locale.ENGLISH);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());

        String price = "123.45";
        Assert.assertEquals(price,
                converter.getValueToDisplay(converter.parse(price), true));

    }

    @Test
    public void testParse() throws Exception {
        checkEquals("123", priceConverter.parse("123"));
    }

    @Test(expected = ParseException.class)
    public void testParse_DefaultLocaleWrongSymbolEnd() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        converter.parse("50.00a");
    }

    @Test(expected = ParseException.class)
    public void testParse_DefaultLocaleWrongSymbolBegin() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        converter.parse("a50.00");
    }

    @Test(expected = ParseException.class)
    public void testParse_DefaultLocaleWrongSymbolMiddle() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        converter.parse("50.a00");
    }

    @Test
    public void testParse_DefaultLocale() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        checkEquals("50", converter.parse("50"));
    }

    @Test
    public void testParse_DefaultLocaleWithSeparator() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        checkEquals("50000.14", converter.parse("50,000.14"));
    }

    @Test
    public void testParse_DefaultLocaleWithSeparators() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        BigDecimal valueToStore = converter.parse(",,,.3");
        checkEquals("0.3", valueToStore);
        String valueToDisplay = converter.getValueToDisplay(valueToStore, true);
        Assert.assertEquals("0.30", valueToDisplay);
    }

    @Test
    public void testParse_GermanLocaleWithSeparators() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        BigDecimal valueToStore = converter.parse("...,3");
        checkEquals("0.3", valueToStore);
        String valueToDisplay = converter.getValueToDisplay(valueToStore, true);
        Assert.assertEquals("0,30", valueToDisplay);
    }

    @Test
    public void testParse_GermanLocaleWithSeparator() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        BigDecimal valueToStore = converter.parse("50.000,14");
        checkEquals("50000.14", valueToStore);
    }

    @Test
    public void testParse_MaximumFractionDigits_DE() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());

        BigDecimal d = converter
                .parse("50.000,11111111111111111111111111111111111111111111111111");
        assertEquals(new BigDecimal(
                "50000.11111111111111111111111111111111111111111111111111"), d);
    }

    @Test(expected = ParseException.class)
    public void testParse_ExceedsMaximumFractionDigits_DE() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());

        converter
                .parse("50.000,111111111111111111111111111111111111111111111111113");
    }

    @Test
    public void testParse_MaximumIntegerDigitsDE() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        BigDecimal d = converter
                .parse("11111111111111111111111111111111111111111111111111,001");
        assertEquals(new BigDecimal(
                "11111111111111111111111111111111111111111111111111.001"), d);
    }

    @Test
    public void testParse_MaximumIntegerDigitsDE_WithGroupingSeparators()
            throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        BigDecimal d = converter
                .parse("111111111111111111111.1.1111111111111111111111.111.111,001");
        assertEquals(new BigDecimal(
                "11111111111111111111111111111111111111111111111111.001"), d);
    }

    @Test(expected = ParseException.class)
    public void testParse_ExceedsMaximumIntegerDigitsDE() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        converter
                .parse("111111111111111111111111111111111111111111111111113,001");
    }

    @Test(expected = ParseException.class)
    public void testParse_ExceedsMaximumIntegerDigitsDE2() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        converter
                .parse("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111113,00");
    }

    @Test(expected = ParseException.class)
    public void testParse_DefaultLocaleNegative() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        converter.parse("-50.00");
    }

    @Test
    public void testParse_DefaultLocaleNegativeAllowed() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        converter.parse("-50.00", true);
    }

    @Test
    public void testParse_DefaultLocaleZero() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        checkEquals("0.00", converter.parse("0.00"));
    }

    @Test
    public void testParse_DefaultLocaleEmpty() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        BigDecimal valueToStore = converter.parse("   ");
        assertTrue(BigDecimalComparator.isZero(valueToStore));
    }

    @Test
    public void testParse_DefaultLocaleNull() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        BigDecimal valueToStore = converter.parse(null);
        assertTrue(BigDecimalComparator.isZero(valueToStore));
    }

    @Test
    public void testParse_DefaultLocaleOwnOutput() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        BigDecimal price = new BigDecimal("5000");
        String valueToDisplay = converter.getValueToDisplay(price, true);
        BigDecimal valueToStore = converter.parse(valueToDisplay);
        checkEquals("5000.00", valueToStore);
    }

    @Test(expected = ParseException.class)
    public void testParse_DefaultLocaleInvalid() throws Exception {
        PriceConverter converter = new PriceConverter(null);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        converter.parse("xyz");
    }

    @Test
    public void testParse_GermanLocale() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        checkEquals("50.00", converter.parse("50,00"));
    }

    @Test(expected = ParseException.class)
    public void testParse_GermanLocaleNegative() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        converter.parse("-50,00");
    }

    @Test
    public void testParse_GermanLocaleNegativeAllowed() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        converter.parse("-50,00", true);
    }

    @Test
    public void testParse_GermanLocaleZero() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        checkEquals("0.00", converter.parse("0,00"));
    }

    @Test
    public void testParse_GermanLocaleOwnOutput() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.GERMAN);
        Assert.assertEquals(Locale.GERMAN, converter.getActiveLocale());
        long price = 50L;
        String valueToDisplay = converter.getValueToDisplay(new BigDecimal(
                price), true);
        BigDecimal valueToStore = converter.parse(valueToDisplay);
        checkEquals("50.00", valueToStore);
    }

    /*
     * Test for bug 7122
     */
    @Test(expected = ParseException.class)
    public void testParse_InvalidFormat() throws Exception {
        PriceConverter converter = new PriceConverter(Locale.ENGLISH);
        Assert.assertEquals(Locale.ENGLISH, converter.getActiveLocale());
        converter.parse("-'1.2.3'");
    }

    /* Tests for Bug#7905 */
    /* GOOD cases */

    @Test
    public void testParse_1GroupingAndDecimalSeparator() throws Exception {
        BigDecimal d = new PriceConverter(Locale.ENGLISH).parse("1,000.1234");
        // compare to ignore the scale:
        assertEquals(0, BigDecimal.valueOf(1000.1234).compareTo(d));
    }

    @Test
    public void testParse_1GroupingAndDecimalSeparatorDE() throws Exception {
        BigDecimal d = new PriceConverter(Locale.GERMAN).parse("1.000,1234");
        // compare to ignore the scale:
        assertEquals(0, BigDecimal.valueOf(1000.1234).compareTo(d));
    }

    @Test
    public void testParse_ValidDecimalChars() throws Exception {
        BigDecimal d = new PriceConverter(Locale.ENGLISH).parse("0.33123");
        // compare to ignore the scale:
        assertEquals(0, BigDecimal.valueOf(0.33123).compareTo(d));
    }

    @Test
    public void testParse_ValidDecimalChars_NoIntegerPlaces() throws Exception {
        BigDecimal d = new PriceConverter(Locale.ENGLISH).parse(".33123");
        // compare to ignore the scale:
        assertEquals(0, BigDecimal.valueOf(0.33123).compareTo(d));
    }

    @Test
    public void testParse_ValidDecimalChars_NoIntegerPlacesDE()
            throws Exception {
        BigDecimal d = new PriceConverter(Locale.GERMAN).parse(",33123");
        // compare to ignore the scale:
        assertEquals(0, BigDecimal.valueOf(0.33123).compareTo(d));
    }

    @Test
    public void testParse_BadlyPlacedGroupingSeparators() throws Exception {
        BigDecimal d = new PriceConverter(Locale.ENGLISH).parse("20,,0056");
        // compare to ignore the scale:
        assertEquals(0, BigDecimal.valueOf(200056).compareTo(d));
    }

    @Test
    public void testParse_BadlyPlacedGroupingSeparatorsDE() throws Exception {
        BigDecimal d = new PriceConverter(Locale.GERMAN).parse("20..0056");
        // compare to ignore the scale:
        assertEquals(0, BigDecimal.valueOf(200056).compareTo(d));
    }

    @Test
    public void testParse_BadlyPlacedGroupingSeparators2() throws Exception {
        BigDecimal d = new PriceConverter(Locale.ENGLISH).parse("20,,.01");
        // compare to ignore the scale:
        assertEquals(0, BigDecimal.valueOf(20.01).compareTo(d));
    }

    @Test
    public void testParse_Trim() throws Exception {
        // contains spaces and tabs left and right
        BigDecimal d = new PriceConverter(Locale.ENGLISH)
                .parse("         20.01         ");
        // compare to ignore the scale:
        assertEquals(0, BigDecimal.valueOf(20.01).compareTo(d));
    }

    @Test
    public void testParse_OnlyBlanks() throws Exception {
        // contains spaces and tabs
        BigDecimal d = new PriceConverter(Locale.ENGLISH)
                .parse("                  ");
        // compare to ignore the scale:
        assertEquals(0, BigDecimal.ZERO.compareTo(d));
    }

    /* BAD cases */

    @Test(expected = ParseException.class)
    public void testParse_2DecimalSeparators() throws Exception {
        new PriceConverter(Locale.ENGLISH).parse("20..0056");
    }

    @Test(expected = ParseException.class)
    public void testParse_2DecimalSeparators_() throws Exception {
        new PriceConverter(Locale.ENGLISH).parse("0.555.123");
    }

    @Test(expected = ParseException.class)
    public void testParse_Parse_3DecimalSeparators() throws Exception {
        new PriceConverter(Locale.ENGLISH).parse("5.123..123");
    }

    @Test(expected = ParseException.class)
    public void testParse_InvalidDecimalChars_NoIntegerPlaces()
            throws Exception {
        new PriceConverter(Locale.ENGLISH).parse(".33..123");
    }

    @Test(expected = ParseException.class)
    public void testParse_2DecimalSeparatorsDE() throws Exception {
        new PriceConverter(Locale.GERMAN).parse("20,,0056");
    }

    @Test(expected = ParseException.class)
    public void testParse_3DecimalSeparatorsDE() throws Exception {
        new PriceConverter(Locale.GERMAN).parse("5,123,,123");
    }

    @Test(expected = ParseException.class)
    public void testParse_GroupingSeparatorAfterComma() throws Exception {
        new PriceConverter(Locale.ENGLISH).parse(".123,493");
    }

    @Test(expected = ParseException.class)
    public void testParse_GroupingSeparatorAfterComma_WrongGrouping()
            throws Exception {
        new PriceConverter(Locale.ENGLISH).parse(".123,493,2878");
    }

    @Test(expected = ParseException.class)
    public void testParse_GroupingSeparatorAfterComma_NothingAfter()
            throws Exception {
        new PriceConverter(Locale.ENGLISH).parse(".123,");
    }

    @Test(expected = ParseException.class)
    public void testParse_CurrencySymbol1() throws Exception {
        new PriceConverter(Locale.GERMAN).parse("€1.000,1234");
    }

    @Test(expected = ParseException.class)
    public void testParse_CurrencySymbol2() throws Exception {
        new PriceConverter(Locale.GERMAN).parse("€ 1.000,1234");
    }

    @Test(expected = ParseException.class)
    public void testParse_CurrencySymbol3() throws Exception {
        new PriceConverter(Locale.GERMAN).parse("1.000,1234€");
    }

    @Test(expected = ParseException.class)
    public void testParse_CurrencySymbol4() throws Exception {
        new PriceConverter(Locale.GERMAN).parse("1.000,1234 €");
    }

    @Test(expected = ParseException.class)
    public void testParse_PlusSymbol1() throws Exception {
        new PriceConverter(Locale.GERMAN).parse("+1.000,1234");
    }

    @Test(expected = ParseException.class)
    public void testParse_PlusSymbol2() throws Exception {
        new PriceConverter(Locale.GERMAN).parse("+ 1.000,1234");
    }

    @Test(expected = ParseException.class)
    public void testParse_MinusSymbol1() throws Exception {
        new PriceConverter(Locale.GERMAN).parse("-1.000,1234");
    }

    @Test(expected = ParseException.class)
    public void testParse_MinusSymbol2() throws Exception {
        new PriceConverter(Locale.GERMAN).parse("- 1.000,1234");
    }

    @Test(expected = ParseException.class)
    public void testParse_Space() throws Exception {
        new PriceConverter(Locale.ENGLISH).parse("20. 01");
    }

    @Test(expected = ParseException.class)
    public void testParse_Tab() throws Exception {
        new PriceConverter(Locale.ENGLISH).parse("20.   01");
    }

}
