/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for class <code>CountryCode</code>
 * 
 * @author cheld
 * 
 */
public class CountryBeanTest {

    private CountryBean bean;

    @Before
    public void before() {
        bean = new CountryBean();
        bean.currentUserLocale = Locale.GERMAN;
    }

    /**
     * Assert that a localized name exists for country code.
     */
    @Test
    public void getDisplayCountries() {
        String displayName = bean.getDisplayCountries().get(
                Locale.GERMANY.getCountry());
        assertEquals("Deutschland", displayName);
    }

    /**
     * The ISO country code is returned in case no localized name exists
     */
    @Test
    public void getDisplayCountries_negative() {
        String name = bean.getDisplayCountries().get("non-existing");
        assertEquals("non-existing", name);
    }

    /**
     * Check if a change of the user locale is detected properly.
     */
    @Test
    public void getDisplayCountries_localeChanged() {

        // given
        String displayName = bean.getDisplayCountries().get(
                Locale.GERMANY.getCountry());
        assertEquals("Deutschland", displayName);

        // execute
        bean.currentUserLocale = Locale.ENGLISH;

        // assert
        displayName = bean.getDisplayCountries().get(
                Locale.GERMANY.getCountry());
        assertEquals("Germany", displayName);
    }
}
