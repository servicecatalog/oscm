/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.12.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the LocaleHandler class.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class LocaleHandlerTest {

    @Test
    public void ctors() throws Exception {
        new LocaleHandler();
    }

    @Test
    public void testGetLocaleFromStringNull() throws Exception {
        Locale localeFromString = LocaleHandler.getLocaleFromString(null);
        Assert.assertNotNull(localeFromString);
        Assert.assertEquals("Wrong locale returned", "en",
                localeFromString.getLanguage());
    }

    @Test
    public void testGetLocaleFromStringLanguageOnly() throws Exception {
        Locale localeFromString = LocaleHandler.getLocaleFromString("de");
        Assert.assertNotNull(localeFromString);
        Assert.assertEquals("Wrong locale returned", "de",
                localeFromString.getLanguage());
    }

    @Test
    public void testGetLocaleFromStringLanguageAndCountry() throws Exception {
        Locale localeFromString = LocaleHandler.getLocaleFromString("de_DE");
        Assert.assertNotNull(localeFromString);
        Assert.assertEquals("Wrong language returned", "de",
                localeFromString.getLanguage());
        Assert.assertEquals("Wrong country returned", "DE",
                localeFromString.getCountry());
    }

    @Test
    public void testGetLocaleFromStringLanguageAndVersion() throws Exception {
        Locale localeFromString = LocaleHandler.getLocaleFromString("de__xy");
        Assert.assertNotNull(localeFromString);
        Assert.assertEquals("Wrong language returned", "de",
                localeFromString.getLanguage());
        Assert.assertEquals("Wrong country returned", "",
                localeFromString.getCountry());
        Assert.assertEquals("Wrong variant returned", "xy",
                localeFromString.getVariant());
    }

    @Test
    public void testGetLocaleFromStringLanguageCountryAndVariant()
            throws Exception {
        Locale localeFromString = LocaleHandler.getLocaleFromString("de_DE_xy");
        Assert.assertNotNull(localeFromString);
        Assert.assertEquals("Wrong language returned", "de",
                localeFromString.getLanguage());
        Assert.assertEquals("Wrong country returned", "DE",
                localeFromString.getCountry());
        Assert.assertEquals("Wrong variant returned", "xy",
                localeFromString.getVariant());
    }

    @Test
    public void testGetLocaleFromStringNonsenseInput() throws Exception {
        Locale localeFromString = LocaleHandler
                .getLocaleFromString("dedf_DE_xy");
        Assert.assertNotNull(localeFromString);
        Assert.assertEquals("Wrong language returned", "en",
                localeFromString.getLanguage());
    }

    @Test
    public void isStandardLanguage_EN() {
        // given
        Locale locale = new Locale("EN");
        // when
        boolean result = LocaleHandler.isStandardLanguage(locale);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isStandardLanguage_DE() {
        // given
        Locale locale = new Locale("de");
        // when
        boolean result = LocaleHandler.isStandardLanguage(locale);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isStandardLanguage_JA() {
        // given
        Locale locale = Locale.JAPANESE;
        // when
        boolean result = LocaleHandler.isStandardLanguage(locale);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isStandardLanguage_FR() {
        // given
        Locale locale = Locale.FRENCH;
        // when
        boolean result = LocaleHandler.isStandardLanguage(locale);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

}
