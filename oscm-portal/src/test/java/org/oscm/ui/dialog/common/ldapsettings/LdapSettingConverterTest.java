/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.common.ldapsettings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.dialog.common.ldapsettings.LdapSetting;
import org.oscm.ui.dialog.common.ldapsettings.LdapSettingConverter;
import org.oscm.internal.usermanagement.POLdapSetting;

public class LdapSettingConverterTest {

    private Set<LdapSetting> settings = new HashSet<LdapSetting>();
    private List<POLdapSetting> serverSettings = new ArrayList<POLdapSetting>();
    private LdapSettingConverter converter;

    @Before
    public void setup() {
        converter = new LdapSettingConverter();
    }

    @Test
    public void toProperties_Empty() {
        Properties result = converter.toProperties(settings, false);
        assertTrue(result.keySet().isEmpty());
    }

    @Test
    public void toProperties_OneEntry() {
        settings.add(new LdapSetting("key1", "value1", false));
        Properties result = converter.toProperties(settings, false);
        assertEquals("value1", result.getProperty("key1"));
    }

    @Test
    public void toProperties_OneEntryPlatformDefault() {
        settings.add(new LdapSetting("key1", "value1", true));
        Properties result = converter.toProperties(settings, false);
        assertEquals("", result.getProperty("key1"));
    }

    @Test
    public void toProperties_OneEntryPlatformDefaultKeepSettingValues() {
        settings.add(new LdapSetting("key1", "value1", true));
        Properties result = converter.toProperties(settings, true);
        assertEquals("value1", result.getProperty("key1"));
    }

    @Test
    public void addToModel_NoEntry() {
        converter.addToModel(settings, serverSettings);
        assertTrue(settings.isEmpty());
    }

    @Test
    public void addToModel_OneEntry() {
        serverSettings.add(new POLdapSetting("key1", "value1", false));
        converter.addToModel(settings, serverSettings);
        assertEquals(1, settings.size());
        LdapSetting element = settings.iterator().next();
        assertEquals("key1", element.getSettingKey());
        assertEquals("value1", element.getSettingValue());
        assertFalse(element.isPlatformDefault());
    }

}
