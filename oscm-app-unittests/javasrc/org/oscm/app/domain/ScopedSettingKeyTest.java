/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 20.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.oscm.app.domain.ConfigurationSetting.ScopedSettingKey;

/**
 * @author Dirk Bernsau
 * 
 */
public class ScopedSettingKeyTest {

    @Test
    public void testEqualsSettings() throws Exception {
        ScopedSettingKey key = new ConfigurationSetting.ScopedSettingKey();
        ScopedSettingKey key_a = new ConfigurationSetting.ScopedSettingKey();
        assertFalse(key == null);
        assertTrue(key.equals(key));
        assertTrue(key.equals(key_a));
        assertFalse(key.equals("Hello"));
        assertTrue(key.hashCode() == key.hashCode());

        ScopedSettingKey key_ab1 = new ConfigurationSetting.ScopedSettingKey(
                "a", "b");
        ScopedSettingKey key_ab2 = new ConfigurationSetting.ScopedSettingKey(
                "a", "b");
        ScopedSettingKey key_ba1 = new ConfigurationSetting.ScopedSettingKey(
                "b", "a");
        assertTrue(key_ab1.equals(key_ab2));
        assertTrue(key_ab1.hashCode() == key_ab2.hashCode());

        assertFalse(key_ab1.equals(key_ba1));
        assertFalse(key_ab1.hashCode() == key_ba1.hashCode());

        ScopedSettingKey key_an1 = new ConfigurationSetting.ScopedSettingKey(
                "a", null);
        ScopedSettingKey key_an2 = new ConfigurationSetting.ScopedSettingKey(
                "a", null);
        ScopedSettingKey key_na1 = new ConfigurationSetting.ScopedSettingKey(
                null, "a");
        ScopedSettingKey key_na2 = new ConfigurationSetting.ScopedSettingKey(
                null, "a");
        ScopedSettingKey key_bn1 = new ConfigurationSetting.ScopedSettingKey(
                "b", null);
        ScopedSettingKey key_nb1 = new ConfigurationSetting.ScopedSettingKey(
                null, "b");
        assertEquals(key_an1, key_an2);
        assertEquals(key_na1, key_na2);
        assertFalse(key_an1.equals(key_bn1));
        assertFalse(key_na1.equals(key_nb1));
        assertFalse(key_an1.equals(key_ab1));
        assertFalse(key_nb1.equals(key_ab1));
    }

    @Test
    public void testEqualsAttributes() throws Exception {
        CustomAttribute.ScopedSettingKey key = new CustomAttribute.ScopedSettingKey();
        CustomAttribute.ScopedSettingKey key_a = new CustomAttribute.ScopedSettingKey();
        assertFalse(key == null);
        assertTrue(key.equals(key));
        assertTrue(key.equals(key_a));
        assertFalse(key.equals("Hello"));
        assertTrue(key.hashCode() == key.hashCode());

        CustomAttribute.ScopedSettingKey key_ab1 = new CustomAttribute.ScopedSettingKey(
                "a", "b");
        CustomAttribute.ScopedSettingKey key_ab2 = new CustomAttribute.ScopedSettingKey(
                "a", "b");
        CustomAttribute.ScopedSettingKey key_ba1 = new CustomAttribute.ScopedSettingKey(
                "b", "a");
        assertTrue(key_ab1.equals(key_ab2));
        assertTrue(key_ab1.hashCode() == key_ab2.hashCode());

        assertFalse(key_ab1.equals(key_ba1));
        assertFalse(key_ab1.hashCode() == key_ba1.hashCode());

        CustomAttribute.ScopedSettingKey key_an1 = new CustomAttribute.ScopedSettingKey(
                "a", null);
        CustomAttribute.ScopedSettingKey key_an2 = new CustomAttribute.ScopedSettingKey(
                "a", null);
        CustomAttribute.ScopedSettingKey key_na1 = new CustomAttribute.ScopedSettingKey(
                null, "a");
        CustomAttribute.ScopedSettingKey key_na2 = new CustomAttribute.ScopedSettingKey(
                null, "a");
        CustomAttribute.ScopedSettingKey key_bn1 = new CustomAttribute.ScopedSettingKey(
                "b", null);
        CustomAttribute.ScopedSettingKey key_nb1 = new CustomAttribute.ScopedSettingKey(
                null, "b");
        assertEquals(key_an1, key_an2);
        assertEquals(key_na1, key_na2);
        assertFalse(key_an1.equals(key_bn1));
        assertFalse(key_na1.equals(key_nb1));
        assertFalse(key_an1.equals(key_ab1));
        assertFalse(key_nb1.equals(key_ab1));
    }
}
