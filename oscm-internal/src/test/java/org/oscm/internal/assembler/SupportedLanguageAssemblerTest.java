/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cmin                                                     
 *                                                                              
 *  Creation Date: 11 11, 2013                                                      
 *                                                                              
 *  Completion Time: 11 11, 2013                        
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.SupportedLanguage;
import org.oscm.internal.operatorservice.POSupportedLanguage;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * Unit Test class for SupportedLanguageAssembler
 * 
 * @author cmin
 * 
 */
@SuppressWarnings("boxing")
public class SupportedLanguageAssemblerTest {

    private SupportedLanguage supportedLanguage;
    private SupportedLanguage supportedLanguage2;
    private POSupportedLanguage poSupportedLanguage;
    private POSupportedLanguage poSupportedLanguage2;

    private Locale currentUserLocale;

    @Before
    public void setUp() {
        supportedLanguage = new SupportedLanguage();
        supportedLanguage.setKey(1);
        supportedLanguage.setLanguageISOCode("en");
        supportedLanguage.setActiveStatus(true);
        supportedLanguage.setDefaultStatus(true);

        supportedLanguage2 = new SupportedLanguage();
        supportedLanguage2.setKey(2);
        supportedLanguage2.setLanguageISOCode("de");

        // incoming entries
        poSupportedLanguage = new POSupportedLanguage();
        poSupportedLanguage.setKey(1);
        poSupportedLanguage.setLanguageISOCode("en");
        poSupportedLanguage.setActive(true);
        poSupportedLanguage.setDefaultLanguageStatus(true);

        poSupportedLanguage2 = new POSupportedLanguage();
        poSupportedLanguage2.setKey(2);
        poSupportedLanguage2.setLanguageISOCode("de");

        currentUserLocale = new Locale("en");
    }

    @Test
    public void toPOSupportedLanguage_NullInput() {
        assertNull(SupportedLanguageAssembler.toPOLanguage(null,
                currentUserLocale));
    }

    @Test
    public void toPOSupportedLanguages_NullInput() {
        assertNull(SupportedLanguageAssembler.toPOLanguages(null,
                currentUserLocale));
    }

    /**
     * domain object -> value object
     */
    @Test
    public void toPOSupportedLanguage() {

        // when
        POSupportedLanguage poSupportLanguage = SupportedLanguageAssembler
                .toPOLanguage(supportedLanguage, currentUserLocale);

        // then
        assertNotNull(poSupportLanguage);
        verify(supportedLanguage, poSupportLanguage);
        assertEquals(poSupportLanguage.getLanguageName(), "English");
    }

    /**
     * domain object -> value object
     */
    @Test
    public void toPOSupportedLanguages() {
        // given
        List<SupportedLanguage> supportedLanguageList = new ArrayList<SupportedLanguage>();
        supportedLanguageList.add(supportedLanguage);
        supportedLanguageList.add(supportedLanguage2);

        // when
        List<POSupportedLanguage> poSupportLanguages = SupportedLanguageAssembler
                .toPOLanguages(supportedLanguageList, currentUserLocale);

        // then
        verify(supportedLanguage, poSupportLanguages.get(0));
        assertEquals(poSupportLanguages.get(0).getLanguageName(), "English");
        verify(supportedLanguage2, poSupportLanguages.get(1));
        assertEquals(poSupportLanguages.get(1).getLanguageName(), "German");

    }

    @Test(expected = IllegalArgumentException.class)
    public void toSupportedLanguage_NullArgument() {
        SupportedLanguageAssembler.toLanguage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toSupportedLanguages_NullArgument() {
        SupportedLanguageAssembler.toLanguages(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toSupportedLanguage_NullISOCode() {
        SupportedLanguageAssembler.toLanguage(new POSupportedLanguage());
    }

    @Test
    public void toSupportedLanguage() {
        // when
        SupportedLanguage supportedLanguage = SupportedLanguageAssembler
                .toLanguage(poSupportedLanguage);

        // then
        verify(supportedLanguage, poSupportedLanguage);
    }

    @Test
    public void toSupportedLanguages() {
        // given
        List<POSupportedLanguage> poList = new ArrayList<POSupportedLanguage>();
        poList.add(poSupportedLanguage);
        poList.add(poSupportedLanguage2);

        // when
        List<SupportedLanguage> supportedLanguageList = SupportedLanguageAssembler
                .toLanguages(poList);

        // then
        assertNotNull(supportedLanguageList);
        verify(supportedLanguageList.get(0), poSupportedLanguage);
        verify(supportedLanguageList.get(1), poSupportedLanguage2);
    }

    private void verify(SupportedLanguage supportedLanguage,
            POSupportedLanguage poSupportLanguage) {
        assertEquals(supportedLanguage.getKey(), poSupportLanguage.getKey());
        assertEquals(supportedLanguage.getLanguageISOCode(),
                poSupportLanguage.getLanguageISOCode());
        assertEquals(supportedLanguage.getActiveStatus(),
                poSupportLanguage.isActive());
        assertEquals(supportedLanguage.getDefaultStatus(),
                poSupportLanguage.isDefaultLanguageStatus());
    }
}
