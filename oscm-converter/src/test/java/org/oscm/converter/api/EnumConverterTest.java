/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.oscm.types.enumtypes.SettingType;
import org.oscm.types.enumtypes.UserRoleType;

/**
 * @author kulle
 * 
 */
public class EnumConverterTest {

    enum TestEnum {
        VALUE;
    }

    @Test
    public void convert_null() {
        assertNull(EnumConverter.convert(null, UserRoleType.class));
    }

    @Test
    public void convert() {
        assertEquals(
                org.oscm.internal.types.enumtypes.UserRoleType.MARKETPLACE_OWNER,
                EnumConverter
                        .convert(
                                UserRoleType.MARKETPLACE_OWNER,
                                org.oscm.internal.types.enumtypes.UserRoleType.class));
    }

    @Test
    public void convert_noMapping() {
        assertNull(EnumConverter.convert(TestEnum.VALUE,
                org.oscm.internal.types.enumtypes.UserRoleType.class));
    }

    @Test
    public void convert_list_null() {
        assertNull(EnumConverter.convertList(null,
                org.oscm.internal.types.enumtypes.SettingType.class));
    }

    @Test
    public void convert_list() {
        // given
        List<SettingType> src = new ArrayList<SettingType>();
        src.add(SettingType.LDAP_ATTR_ADDITIONAL_NAME);
        src.add(SettingType.LDAP_ATTR_EMAIL);

        // when
        List<org.oscm.internal.types.enumtypes.SettingType> result = EnumConverter
                .convertList(
                        src,
                        org.oscm.internal.types.enumtypes.SettingType.class);

        // then
        assertEquals(2, result.size());
        assertTrue(result
                .contains(org.oscm.internal.types.enumtypes.SettingType.LDAP_ATTR_ADDITIONAL_NAME));
        assertTrue(result
                .contains(org.oscm.internal.types.enumtypes.SettingType.LDAP_ATTR_EMAIL));
    }

    @Test
    public void convert_list_noMapping() {
        // given
        List<TestEnum> src = new ArrayList<TestEnum>();
        src.add(TestEnum.VALUE);

        // when
        List<org.oscm.internal.types.enumtypes.SettingType> result = EnumConverter
                .convertList(
                        src,
                        org.oscm.internal.types.enumtypes.SettingType.class);

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void convert_set_null() {
        assertNull(EnumConverter.convertSet(null,
                org.oscm.internal.types.enumtypes.SettingType.class));
    }

    @Test
    public void convert_set() {
        // given
        Set<SettingType> src = new HashSet<SettingType>();
        src.add(SettingType.LDAP_ATTR_ADDITIONAL_NAME);
        src.add(SettingType.LDAP_ATTR_EMAIL);

        // when
        Set<org.oscm.internal.types.enumtypes.SettingType> result = EnumConverter
                .convertSet(
                        src,
                        org.oscm.internal.types.enumtypes.SettingType.class);

        // then
        assertEquals(2, result.size());
        assertTrue(result
                .contains(org.oscm.internal.types.enumtypes.SettingType.LDAP_ATTR_ADDITIONAL_NAME));
        assertTrue(result
                .contains(org.oscm.internal.types.enumtypes.SettingType.LDAP_ATTR_EMAIL));
    }

    @Test
    public void convert_set_noMapping() {
        // given
        Set<TestEnum> src = new HashSet<TestEnum>();
        src.add(TestEnum.VALUE);

        // when
        Set<org.oscm.internal.types.enumtypes.SettingType> result = EnumConverter
                .convertSet(
                        src,
                        org.oscm.internal.types.enumtypes.SettingType.class);

        // then
        assertEquals(0, result.size());
    }
}
