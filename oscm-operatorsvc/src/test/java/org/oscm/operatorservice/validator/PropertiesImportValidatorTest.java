/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-8                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.SupportedLanguage;
import org.oscm.internal.types.exception.PropertiesImportException;

/**
 * Unit test for PropertiesImportValidator
 * 
 * @author Gao
 * 
 */
public class PropertiesImportValidatorTest {

    private PropertiesImportValidator validator;
    private DataService ds;
    private Query query;
    private List<SupportedLanguage> languageList;

    @Before
    public void setUp() {
        ds = mock(DataService.class);
        query = mock(Query.class);
        doReturn(query).when(ds).createNamedQuery("SupportedLanguage.findAll");
        validator = spy(new PropertiesImportValidator(ds));
    }

    @Test
    public void checkLanguageCodeNotNull_OK() throws Exception {
        // given
        try {
            // when
            validator.checkLanguageCodeNotNull("en");
        } catch (PropertiesImportException e) {
            // then
            fail();
            throw e;
        }
    }

    @Test(expected = PropertiesImportException.class)
    public void checkLanguageCodeNotNull_NULL() throws Exception {
        // given
        try {
            // when
            validator.checkLanguageCodeNotNull(null);
            // then
            fail();
        } catch (PropertiesImportException e) {
            assertEquals(Boolean.TRUE, Boolean.valueOf(e.getMessage().contains(
                    PropertiesImportException.Reason.NONE_LANGUAGE_CODE
                            .toString())));
            throw e;
        }
    }

    @Test(expected = PropertiesImportException.class)
    public void checkLanguageCodeNotNull_BLANK() throws Exception {
        // given
        try {
            // when
            validator.checkLanguageCodeNotNull("");
            // then
            fail();
        } catch (PropertiesImportException e) {
            assertEquals(Boolean.TRUE, Boolean.valueOf(e.getMessage().contains(
                    PropertiesImportException.Reason.NONE_LANGUAGE_CODE
                            .toString())));
            throw e;
        }
    }

    @Test
    public void checkLanguageSupported_OK() throws Exception {
        // given
        prepareLanguages();
        // when
        try {
            validator.checkLanguageSupported("en");
        } catch (PropertiesImportException e) {
            // then
            fail();
            throw e;
        }
    }

    @Test(expected = PropertiesImportException.class)
    public void checkLanguageSupported_NOK() throws Exception {
        // given
        prepareLanguages();
        // when
        try {
            validator.checkLanguageSupported("aa");
            // then
            fail();
        } catch (PropertiesImportException e) {
            assertEquals(Boolean.TRUE, Boolean.valueOf(e.getMessage().contains(
                    PropertiesImportException.Reason.LANGUAGE_NOT_SUPPORTED
                            .toString())));
            throw e;
        }
    }

    private void prepareLanguages() {
        languageList = new ArrayList<SupportedLanguage>();
        SupportedLanguage enLanguage = new SupportedLanguage();
        enLanguage.setLanguageISOCode("en");
        SupportedLanguage deLanguage = new SupportedLanguage();
        deLanguage.setLanguageISOCode("de");
        SupportedLanguage jaLanguage = new SupportedLanguage();
        jaLanguage.setLanguageISOCode("ja");
        languageList.add(enLanguage);
        languageList.add(deLanguage);
        languageList.add(jaLanguage);
        doReturn(languageList).when(query).getResultList();
    }
}
