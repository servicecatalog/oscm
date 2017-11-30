/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.common;

import static java.util.Locale.CHINESE;
import static java.util.Locale.ITALIAN;
import static java.util.Locale.JAPANESE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.faces.application.Application;

import org.junit.Test;

import org.oscm.ui.stubs.ApplicationStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.internal.vo.VOLocalizedText;

public class LocaleUtilsTest {

    @Test
    public void testTrimEmpty() {
        final List<VOLocalizedText> values = Collections.emptyList();
        final List<VOLocalizedText> result = LocaleUtils.trim(values, iterator(
                JAPANESE, CHINESE, ITALIAN));

        assertEquals(3, result.size());
        assertEquals("ja", result.get(0).getLocale());
        assertEquals("", result.get(0).getText());
        assertEquals("zh", result.get(1).getLocale());
        assertEquals("", result.get(1).getText());
        assertEquals("it", result.get(2).getLocale());
        assertEquals("", result.get(2).getText());
    }

    @Test
    public void testTrimAdd() {
        final List<VOLocalizedText> values = Arrays.asList(new VOLocalizedText(
                "it", "pizza"));
        final List<VOLocalizedText> result = LocaleUtils.trim(values, iterator(
                JAPANESE, CHINESE, ITALIAN));

        assertEquals(3, result.size());
        assertEquals("ja", result.get(0).getLocale());
        assertEquals("", result.get(0).getText());
        assertEquals("zh", result.get(1).getLocale());
        assertEquals("", result.get(1).getText());
        assertEquals("it", result.get(2).getLocale());
        assertEquals("pizza", result.get(2).getText());
    }

    @Test
    public void testTrimRemove() {
        final List<VOLocalizedText> values = Arrays.asList(new VOLocalizedText(
                "de", "rollbraten"), new VOLocalizedText("ja", "sushi"),
                new VOLocalizedText("it", "pizza"));
        final List<VOLocalizedText> result = LocaleUtils.trim(values, iterator(
                JAPANESE, ITALIAN));

        assertEquals(2, result.size());
        assertEquals("ja", result.get(0).getLocale());
        assertEquals("sushi", result.get(0).getText());
        assertEquals("it", result.get(1).getLocale());
        assertEquals("pizza", result.get(1).getText());
    }

    @Test
    public void testGetNegative() {
        final List<VOLocalizedText> values = Arrays.asList(new VOLocalizedText(
                "ja", "sushi"), new VOLocalizedText("it", "pizza"));

        assertNull(LocaleUtils.get(values, "de"));
    }

    @Test
    public void testGetPositive() {
        final List<VOLocalizedText> values = Arrays.asList(new VOLocalizedText(
                "ja", "sushi"), new VOLocalizedText("it", "pizza"));

        assertEquals("sushi", LocaleUtils.get(values, "ja"));
    }

    @Test
    public void testSetNegative() {
        final List<VOLocalizedText> values = Arrays.asList(new VOLocalizedText(
                "ja", "sushi"), new VOLocalizedText("it", "pizza"));

        LocaleUtils.set(values, "da", "sauerkraut");
        assertEquals("sushi", LocaleUtils.get(values, "ja"));
        assertEquals("pizza", LocaleUtils.get(values, "it"));
    }

    @Test
    public void testSetPositive() {
        final List<VOLocalizedText> values = Arrays.asList(new VOLocalizedText(
                "ja", "sushi"), new VOLocalizedText("it", "pizza"));

        LocaleUtils.set(values, "it", "pasta");
        assertEquals("pasta", LocaleUtils.get(values, "it"));
    }

    @Test
    public void getSupportedLocales_id() {
        initFacesContext();
        List<String> locales = LocaleUtils.getSupportedLocales();
        assertEquals(Boolean.FALSE, Boolean.valueOf(locales.contains("id")));
    }

    @Test
    public void getSupportedLocales_in() {
        initFacesContext();
        List<String> locales = LocaleUtils.getSupportedLocales();
        assertEquals(Boolean.TRUE, Boolean.valueOf(locales.contains("in")));
    }

    private Iterator<Locale> iterator(Locale... locales) {
        return Arrays.asList(locales).iterator();
    }

    private void initFacesContext() {
        new FacesContextStub(Locale.ENGLISH) {
            @Override
            public Application getApplication() {
                Application applicationStub = new ApplicationStub();
                List<Locale> locales = new ArrayList<Locale>();
                locales.add(Locale.ENGLISH);
                locales.add(Locale.GERMAN);
                locales.add(Locale.JAPANESE);
                locales.add(new Locale("in"));
                applicationStub.setSupportedLocales(locales);
                return applicationStub;
            }
        };
    }
}
