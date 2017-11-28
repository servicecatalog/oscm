/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.11.2013      
 *  
 *  author cmin
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ejb.SessionContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.SupportedLanguage;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.types.enumtypes.LocalizedDataType;
import org.oscm.types.enumtypes.StandardLanguage;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.PropertiesImportException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Unit test for OperatorServiceLocalBean
 * 
 * @author cmin
 */

public class OperatorServiceLocalBeanTest {

    private OperatorServiceLocalBean operatorServiceLocalBean;
    private SessionContext sessionCtxMock;
    private DataService ds;

    private Query getLanguages;
    private Query getDefaultLanguages;
    private Query getActiveLanguages;
    private Query getPlatformEvent;
    private Query getPlatformParameter;
    private Query getReportName;
    private Query getPaymentTypeName;

    private SupportedLanguage sl1;
    private SupportedLanguage sl2;
    private SupportedLanguage slNew;

    private List<String> defaultLanguageISOCodeList;
    private List<SupportedLanguage> activeLanguageList;
    private List<SupportedLanguage> languageList;

    private LocalizerServiceLocal localizer;

    @Before
    public void setUp() throws Exception {
        operatorServiceLocalBean = spy(new OperatorServiceLocalBean());
        sessionCtxMock = mock(SessionContext.class);
        operatorServiceLocalBean.sessionCtx = sessionCtxMock;
        ds = mock(DataService.class);
        operatorServiceLocalBean.dm = ds;
        getLanguages = mock(Query.class);
        getDefaultLanguages = mock(Query.class);
        getActiveLanguages = mock(Query.class);
        getPlatformEvent = mock(Query.class);
        getPlatformParameter = mock(Query.class);
        getReportName = mock(Query.class);
        getPaymentTypeName = mock(Query.class);

        sl1 = getSupportedLanguage(1, "en", true, true);
        sl2 = getSupportedLanguage(2, "de", true, false);
        slNew = getSupportedLanguage(0, "te", false, false);

        doReturn(sl1).when(ds).getReferenceByBusinessKey(sl1);
        doReturn(sl2).when(ds).getReferenceByBusinessKey(sl2);

        localizer = mock(LocalizerServiceLocal.class);
        operatorServiceLocalBean.localizer = localizer;
        defaultLanguageISOCodeList = new ArrayList<String>();
    }

    @Test
    public void getLanguages() throws Exception {
        // given
        prepareLanguages();

        // when
        List<SupportedLanguage> list = operatorServiceLocalBean
                .getLanguages(false);
        List<SupportedLanguage> activeList = operatorServiceLocalBean
                .getLanguages(true);

        // then
        assertNotNull(list);
        assertEquals(languageList.size(), list.size());

        assertNotNull(activeList);
        assertEquals(activeLanguageList.size(), activeList.size());
    }

    @Test
    public void getDefaultLanguage() throws Exception {
        // given
        prepareDefaultLanguage();

        // when
        String languageISOCode = operatorServiceLocalBean.getDefaultLanguage();

        // then
        assertEquals(languageISOCode, "en");
    }

    @Test
    public void getDefaultLanguage_none() throws Exception {
        // given
        when(ds.createNamedQuery(eq("SupportedLanguage.findDefault")))
                .thenReturn(getLanguages);
        when(getLanguages.getResultList()).thenReturn(null);

        // when
        try {
            operatorServiceLocalBean.getDefaultLanguage();
            // then
            fail();
        } catch (ObjectNotFoundException e) {
            assertTrue(e.getMessage().contains(
                    "Default supported language cannot be found."));
        }

    }

    @Test
    public void getDefaultLanguage_moreThanOne() throws Exception {
        // given
        prepareDefaultLanguage();
        defaultLanguageISOCodeList.add("de");
        // when
        try {
            operatorServiceLocalBean.getDefaultLanguage();
            // then
            fail();
        } catch (ObjectNotFoundException e) {
            assertTrue(e.getMessage().contains(
                    "Two or more default supported languages have been found."));
        }
    }

    @Test
    public void save_WithNoModification() throws Exception {
        // given
        List<SupportedLanguage> slList = new ArrayList<SupportedLanguage>();
        slList.add(sl1);
        slList.add(sl2);

        // when
        operatorServiceLocalBean.saveLanguages(slList);

        // then
        verify(ds, never()).persist(any(SupportedLanguage.class));
        verify(operatorServiceLocalBean, times(2)).doModify(
                any(SupportedLanguage.class), any(SupportedLanguage.class));
    }

    @Test
    public void save_WithModification() throws Exception {
        // given
        List<SupportedLanguage> slList = new ArrayList<SupportedLanguage>();
        slList.add(sl1);
        slList.add(sl2);

        doReturn(getSupportedLanguage(2, "de", false, false)).when(ds)
                .getReferenceByBusinessKey(sl2);

        // when
        operatorServiceLocalBean.saveLanguages(slList);

        // then
        verify(ds, never()).persist(any(SupportedLanguage.class));
        verify(operatorServiceLocalBean, times(2)).doModify(
                any(SupportedLanguage.class), any(SupportedLanguage.class));
    }

    @Test
    public void save_New() throws Exception {
        // given
        List<SupportedLanguage> slList = new ArrayList<SupportedLanguage>();
        slList.add(slNew);

        // when
        operatorServiceLocalBean.saveLanguages(slList);

        // then
        verify(ds, times(1)).persist(any(SupportedLanguage.class));
        verify(operatorServiceLocalBean, times(0)).doModify(
                any(SupportedLanguage.class), any(SupportedLanguage.class));
    }

    @Test
    public void save_withNullLanguages() throws Exception {
        // given
        // when
        try {
            operatorServiceLocalBean.saveLanguages(null);
            // then
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(
                    "Parameter Language list must not be null."));
        }
    }

    @Test
    public void save_withNullISOCode() throws Exception {
        // given
        List<SupportedLanguage> slList = new ArrayList<SupportedLanguage>();
        slList.add(sl1);
        slList.add(sl2);
        slList.add(getSupportedLanguage(0, null, false, false));

        // when
        try {
            operatorServiceLocalBean.saveLanguages(slList);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(
                    "Parameter Language ISO code must not be null."));
        }
    }

    @Test
    public void save_withNullLanugage() throws Exception {
        // given
        List<SupportedLanguage> slList = new ArrayList<SupportedLanguage>();
        slList.add(sl1);
        slList.add(sl2);
        slList.add(null);

        // when
        try {
            operatorServiceLocalBean.saveLanguages(slList);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(
                    "Parameter Language must not be null."));
        }

    }

    @Test
    public void save_InvalidISOCode() throws Exception {
        // given
        List<SupportedLanguage> slList = new ArrayList<SupportedLanguage>();
        slList.add(getSupportedLanguage(0, "tte", false, false));

        // when
        try {
            operatorServiceLocalBean.saveLanguages(slList);
            // then
            fail();
        } catch (ValidationException e) {
            assertEquals("Parameter Language ISO code is invalid",
                    "languageISOCode", e.getMember());
        }
    }

    @Test
    public void loadMessageProperties_withOneStandardLanguage()
            throws Exception {
        // given
        doReturn(new Properties()).when(localizer)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        any(LocalizedObjectTypes.class), anyString());

        // when
        Map<String, Properties> result = operatorServiceLocalBean
                .loadMessageProperties("de");
        // then
        assertEquals(3, result.size());
        verify(localizer, times(3)).loadLocalizedPropertiesFromDatabase(eq(0L),
                eq(LocalizedObjectTypes.MESSAGE_PROPERTIES), anyString());
    }

    @Test
    public void loadMessageProperties_LanguageCodeIsNull() throws Exception {
        // given
        doReturn(new Properties()).when(localizer)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        any(LocalizedObjectTypes.class), anyString());

        // when
        Map<String, Properties> result = operatorServiceLocalBean
                .loadMessageProperties(null);
        // then
        assertEquals(3, result.size());
        verify(localizer, times(3)).loadLocalizedPropertiesFromDatabase(eq(0L),
                eq(LocalizedObjectTypes.MESSAGE_PROPERTIES), anyString());
    }

    @Test
    public void loadMessageProperties_withOneNotStandardLanguage()
            throws Exception {
        // given
        doReturn(new Properties()).when(localizer)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        any(LocalizedObjectTypes.class), anyString());

        // when
        Map<String, Properties> result = operatorServiceLocalBean
                .loadMessageProperties("zh");
        // then
        assertEquals(4, result.size());
        verify(localizer, times(4)).loadLocalizedPropertiesFromDatabase(eq(0L),
                eq(LocalizedObjectTypes.MESSAGE_PROPERTIES), anyString());
    }

    @Test
    public void loadMailProperties_withOneNotStandardLanguage()
            throws Exception {
        // given
        doReturn(new Properties()).when(localizer)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        any(LocalizedObjectTypes.class), anyString());
        doReturn(new Properties())
                .when(localizer)
                .loadLocalizedPropertiesFromFile(
                        eq(LocalizedObjectTypes.MAIL_CONTENT
                                .getSourceLocation()),
                        anyString());
        // when
        Map<String, Properties> result = operatorServiceLocalBean
                .loadMailProperties("de");
        // then
        verifyResult(result, 6, null);
        verify(localizer, times(3)).loadLocalizedPropertiesFromDatabase(eq(0L),
                eq(LocalizedObjectTypes.MAIL_PROPERTIES), anyString());
    }

    @Test
    public void loadMailProperties_LanguageCodeIsNull() throws Exception {
        // given
        doReturn(new Properties()).when(localizer)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        any(LocalizedObjectTypes.class), anyString());
        doReturn(new Properties())
                .when(localizer)
                .loadLocalizedPropertiesFromFile(
                        eq(LocalizedObjectTypes.MAIL_CONTENT
                                .getSourceLocation()),
                        anyString());
        // when
        Map<String, Properties> result = operatorServiceLocalBean
                .loadMailProperties(null);
        // then
        verifyResult(result, 6, null);
        verify(localizer, times(3)).loadLocalizedPropertiesFromDatabase(eq(0L),
                eq(LocalizedObjectTypes.MAIL_PROPERTIES), anyString());
    }

    @Test
    public void loadMailProperties_WithNotStandardLanguage() throws Exception {
        // given
        doReturn(new Properties()).when(localizer)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        any(LocalizedObjectTypes.class), anyString());
        doReturn(new Properties())
                .when(localizer)
                .loadLocalizedPropertiesFromFile(
                        eq(LocalizedObjectTypes.MAIL_CONTENT
                                .getSourceLocation()),
                        anyString());
        // when
        Map<String, Properties> result = operatorServiceLocalBean
                .loadMailProperties("zh");
        // then
        verifyResult(result, 7, "zh");
        verify(localizer, times(4)).loadLocalizedPropertiesFromDatabase(eq(0L),
                eq(LocalizedObjectTypes.MAIL_PROPERTIES), anyString());
    }

    private void verifyResult(Map<String, Properties> result, int size,
            String languageCode) {
        assertEquals(size, result.size());
        for (StandardLanguage language : StandardLanguage.values()) {
            assertNotNull(result.get(language.toString()));
            assertNotNull(result.get(language.toString()
                    + StandardLanguage.COLUMN_HEADING_SUFFIX));

        }
        if (languageCode != null) {
            assertNotNull(result.get(languageCode));
        }
    }

    @Test
    public void loadPlatformObjects_withOneStandardLanguage() throws Exception {
        // given
        preparePlatformObjects();
        // when
        Map<String, Properties> result = operatorServiceLocalBean
                .loadPlatformObjects("de");
        // then
        verify(getPlatformParameter, times(6)).getResultList();
        verifyResult(result, 6, null);
    }

    @Test
    public void loadPlatformObjects_withOneNotStandardLanguage()
            throws Exception {
        // given
        preparePlatformObjects();
        // when
        Map<String, Properties> result = operatorServiceLocalBean
                .loadPlatformObjects("zh");
        // then
        verify(getPlatformParameter, times(7)).getResultList();
        verifyResult(result, 7, "zh");
    }

    @Test
    public void loadPlatformObjects_LanguageCodeIsNull() throws Exception {
        // given
        preparePlatformObjects();
        // when
        Map<String, Properties> result = operatorServiceLocalBean
                .loadPlatformObjects(null);
        // then
        verify(getPlatformParameter, times(6)).getResultList();
        verifyResult(result, 6, null);
    }

    @Test
    public void loadPropertiesFromDB() {
        // given
        prepareDefaultLanguage();
        doReturn(new Properties()).when(localizer)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        any(LocalizedObjectTypes.class), anyString());
        // when
        operatorServiceLocalBean.loadPropertiesFromDB("de");
        // then
        verify(localizer, times(1)).loadLocalizedPropertiesFromDatabase(eq(0L),
                eq(LocalizedObjectTypes.MESSAGE_PROPERTIES), eq("de"));
        verify(localizer, times(1)).loadLocalizedPropertiesFromDatabase(eq(0L),
                eq(LocalizedObjectTypes.MAIL_PROPERTIES), eq("de"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadPropertiesFromDB_Null() throws Exception {
        // given
        // when
        try {
            operatorServiceLocalBean.loadPropertiesFromDB(null);
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = PropertiesImportException.class)
    public void saveProperties_LanguageCodeIsNull() throws Exception {
        // given
        Map<String, Properties> messgaePropertiesMap = new HashMap<String, Properties>();
        String languageCode = null;
        LocalizedDataType dataType = LocalizedDataType.MessageProperties;
        // when
        try {
            operatorServiceLocalBean.saveProperties(messgaePropertiesMap,
                    languageCode, dataType);
            // then
            fail();
        } catch (Exception e) {
            assertEquals(Boolean.TRUE, Boolean.valueOf(e.getMessage().contains(
                    PropertiesImportException.Reason.NONE_LANGUAGE_CODE
                            .toString())));
            throw e;
        }
    }

    @Test(expected = PropertiesImportException.class)
    public void saveProperties_LanguageCodeIsInvalid() throws Exception {
        // given
        prepareLanguages();
        Map<String, Properties> messgaePropertiesMap = new HashMap<String, Properties>();
        String languageCode = "test";
        LocalizedDataType dataType = LocalizedDataType.MessageProperties;
        // when
        try {
            operatorServiceLocalBean.saveProperties(messgaePropertiesMap,
                    languageCode, dataType);
            // then
            fail();
        } catch (Exception e) {
            assertEquals(Boolean.TRUE, Boolean.valueOf(e.getMessage().contains(
                    PropertiesImportException.Reason.LANGUAGE_NOT_SUPPORTED
                            .toString())));
            throw e;
        }
    }

    @Test
    public void saveProperties_MessageProperties() throws Exception {
        testSaveProperties(LocalizedDataType.MessageProperties,
                LocalizedObjectTypes.MESSAGE_PROPERTIES);
    }

    @Test
    public void saveProperties_MailProperties() throws Exception {
        testSaveProperties(LocalizedDataType.MailProperties,
                LocalizedObjectTypes.MAIL_PROPERTIES);
    }

    @Test
    public void saveProperties_PlatformEventNames() throws Exception {
        testSaveProperties(LocalizedDataType.PlatformObjects,
                LocalizedObjectTypes.EVENT_DESC);
    }

    @Test
    public void saveProperties_PlatformParameterNames() throws Exception {
        testSaveProperties(LocalizedDataType.PlatformObjects,
                LocalizedObjectTypes.PARAMETER_DEF_DESC);
    }

    @Test
    public void saveProperties_PlatformObjects_EmptyLocalizedValue()
            throws Exception {
        // given
        prepareLanguages();
        preparePlatformEventAndParameter();
        Map<String, Properties> messagePropertiesMap = preparePropMap(
                LocalizedDataType.PlatformObjects,
                LocalizedObjectTypes.EVENT_DESC, "");
        String languageCode = "de";
        // when
        operatorServiceLocalBean.saveProperties(messagePropertiesMap,
                languageCode, LocalizedDataType.PlatformObjects);
        // then
        verify(localizer, never()).storeLocalizedResource(eq("de"), eq(0L),
                eq(LocalizedObjectTypes.EVENT_DESC), anyString());
        verify(localizer, times(1)).removeLocalizedValue(anyLong(),
                eq(LocalizedObjectTypes.EVENT_DESC), eq("de"));
    }

    @Test
    public void checkAreAllItemsTranslated() throws Exception {
        // given
        prepareDefaultLanguage();
        List<Map<String, Properties>> maps = preparePropertiesMaps("value1",
                "value2");
        // when
        try {
            String outcome = operatorServiceLocalBean
                    .checkAreAllItemsTranslated(maps, "de");
            assertEquals(null, outcome);
        } catch (Exception e) {
            // then
            fail();
        }
    }

    @Test
    public void checkAreAllItemsTranslated_WARN_EmptyValue() throws Exception {
        // given
        prepareDefaultLanguage();

        List<Map<String, Properties>> maps = preparePropertiesMaps("value1", "");

        // when
        String outcome = operatorServiceLocalBean.checkAreAllItemsTranslated(
                maps, "de");

        // then
        assertTranslationsMissingWarning(outcome);
    }

    @Test
    public void checkAreAllItemsTranslated_WARN_KeyNotFound() throws Exception {
        // given
        prepareDefaultLanguage();

        List<Map<String, Properties>> maps = preparePropertiesMaps("value1", "");

        maps.get(0).get("de").remove("id2");

        // when
        String outcome = operatorServiceLocalBean.checkAreAllItemsTranslated(
                maps, "de");

        // then
        assertTranslationsMissingWarning(outcome);
    }

    private void assertTranslationsMissingWarning(String outcome) {
        assertNotNull(outcome);

        final String expected = new PropertiesImportException(
                PropertiesImportException.Reason.TRANSLATIONS_MISSING)
                .getMessageKey();
        assertEquals(outcome, expected);
    }

    private SupportedLanguage getSupportedLanguage(long key,
            String languageISOCode, boolean activeStatus, boolean defaultStatus) {
        SupportedLanguage sl = new SupportedLanguage();
        sl.setKey(key);
        sl.setLanguageISOCode(languageISOCode);
        sl.setActiveStatus(activeStatus);
        sl.setDefaultStatus(defaultStatus);
        return sl;
    }

    private void prepareDefaultLanguage() {
        defaultLanguageISOCodeList.add("en");
        when(ds.createNamedQuery(eq("SupportedLanguage.findDefault")))
                .thenReturn(getDefaultLanguages);
        when(getDefaultLanguages.getResultList()).thenReturn(
                defaultLanguageISOCodeList);
    }

    private void preparePlatformObjects() {
        doReturn(new Properties()).when(localizer)
                .loadLocalizedPropertiesFromFile(anyString(), anyString());
        preparePaymentTypeNames();
        prepareReportNames();
        preparePlatformEventNames();
        preparePlatformParameterNames();
    }

    private void preparePaymentTypeNames() {
        List<Object[]> result = new ArrayList<Object[]>();
        when(
                ds.createQuery(eq(operatorServiceLocalBean
                        .getQueryStringForDB(LocalizedObjectTypes.PAYMENT_TYPE_NAME))))
                .thenReturn(getPaymentTypeName);
        when(
                ds.createQuery(eq(operatorServiceLocalBean
                        .getQueryStringForFile(LocalizedObjectTypes.PAYMENT_TYPE_NAME))))
                .thenReturn(getPaymentTypeName);
        when(getPaymentTypeName.getResultList()).thenReturn(result);
    }

    private void prepareReportNames() {
        List<Object[]> result = new ArrayList<Object[]>();
        when(
                ds.createQuery(eq(operatorServiceLocalBean
                        .getQueryStringForDB(LocalizedObjectTypes.REPORT_DESC))))
                .thenReturn(getReportName);
        when(
                ds.createQuery(eq(operatorServiceLocalBean
                        .getQueryStringForFile(LocalizedObjectTypes.REPORT_DESC))))
                .thenReturn(getReportName);
        when(getReportName.getResultList()).thenReturn(result);
    }

    private void preparePlatformEventNames() {
        List<Object[]> result = new ArrayList<Object[]>();
        when(
                ds.createQuery(eq(operatorServiceLocalBean
                        .getQueryStringForDB(LocalizedObjectTypes.EVENT_DESC))))
                .thenReturn(getPlatformEvent);
        when(
                ds.createQuery(eq(operatorServiceLocalBean
                        .getQueryStringForFile(LocalizedObjectTypes.EVENT_DESC))))
                .thenReturn(getPlatformEvent);
        when(getPlatformEvent.getResultList()).thenReturn(result);
    }

    private void preparePlatformParameterNames() {
        List<Object[]> result = new ArrayList<Object[]>();
        when(
                ds.createQuery(operatorServiceLocalBean
                        .getQueryStringForDB(LocalizedObjectTypes.PARAMETER_DEF_DESC)))
                .thenReturn(getPlatformParameter);
        when(
                ds.createQuery(operatorServiceLocalBean
                        .getQueryStringForFile(LocalizedObjectTypes.PARAMETER_DEF_DESC)))
                .thenReturn(getPlatformParameter);
        when(getPlatformParameter.getResultList()).thenReturn(result);
    }

    private void preparePlatformEventAndParameter() {
        Event e = new Event();
        e.setKey(0L);
        when(ds.createNamedQuery(eq("Event.getPlatformEvent"))).thenReturn(
                getPlatformEvent);
        when(getPlatformEvent.getSingleResult()).thenReturn(e);
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setKey(0L);
        when(
                ds.createNamedQuery(eq("ParameterDefinition.getPlatformParameterDefinition")))
                .thenReturn(getPlatformParameter);
        when(getPlatformParameter.getSingleResult()).thenReturn(paramDef);

    }

    private void testSaveProperties(LocalizedDataType dataType,
            LocalizedObjectTypes dbType) throws Exception {
        // given
        prepareLanguages();
        preparePlatformEventAndParameter();
        Map<String, Properties> messagePropertiesMap = preparePropMap(dataType,
                dbType, "value");
        String languageCode = "de";
        // when
        operatorServiceLocalBean.saveProperties(messagePropertiesMap,
                languageCode, dataType);
        // then
        verify(localizer, times(1)).storeLocalizedResource(eq("de"), eq(0L),
                eq(dbType), anyString());
        verify(localizer, times(1)).storeLocalizedResource(eq("en"), eq(0L),
                eq(dbType), anyString());
        verify(localizer, never()).storeLocalizedResource(eq("en system"),
                eq(0L), eq(dbType), anyString());
        verify(localizer, never()).removeLocalizedValue(anyLong(),
                any(LocalizedObjectTypes.class), anyString());

    }

    private Map<String, Properties> preparePropMap(LocalizedDataType type,
            LocalizedObjectTypes dbType, String value) {
        Map<String, Properties> messagePropertiesMap = new HashMap<String, Properties>();
        if (LocalizedDataType.PlatformObjects.equals(type)) {

            Properties properties_de = new Properties();
            properties_de.put(dbType.toString() + ".testId", value);
            messagePropertiesMap.put("de", properties_de);
            Properties properties_en = new Properties();
            properties_en.put(dbType.toString() + ".testId", value);
            messagePropertiesMap.put("en", properties_en);
            properties_en.put(dbType.toString() + ".testId", value);
            messagePropertiesMap.put("en system", properties_en);

        } else {
            Properties properties_de = new Properties();
            properties_de.put("testId", value);
            messagePropertiesMap.put("de", properties_de);
            Properties properties_en = new Properties();
            properties_en.put("testId", value);
            messagePropertiesMap.put("en", properties_en);
        }

        return messagePropertiesMap;
    }

    private List<Map<String, Properties>> preparePropertiesMaps(String value1,
            String value2) {
        List<Map<String, Properties>> propertiesMaps = new ArrayList<Map<String, Properties>>();
        Map<String, Properties> propMap = new HashMap<String, Properties>();
        Properties enProp = new Properties();
        enProp.put("id1", "value1");
        enProp.put("id2", value1);
        Properties deProp = new Properties();
        deProp.put("id1", "value_1");
        deProp.put("id2", value2);
        propMap.put("en system", enProp);
        propMap.put("de system", deProp);
        propMap.put("de", deProp);
        propertiesMaps.add(propMap);
        return propertiesMaps;
    }

    private void prepareLanguages() {
        activeLanguageList = new ArrayList<SupportedLanguage>();
        activeLanguageList.add(getSupportedLanguage(1, "en", true, true));
        activeLanguageList.add(getSupportedLanguage(2, "de", true, false));
        activeLanguageList.add(getSupportedLanguage(3, "ja", true, false));

        languageList = new ArrayList<SupportedLanguage>();
        languageList.addAll(activeLanguageList);
        languageList.add(getSupportedLanguage(4, "te", false, false));

        when(ds.createNamedQuery(eq("SupportedLanguage.findAllActive")))
                .thenReturn(getActiveLanguages);
        when(getActiveLanguages.getResultList()).thenReturn(activeLanguageList);

        when(ds.createNamedQuery(eq("SupportedLanguage.findAll"))).thenReturn(
                getLanguages);
        when(getLanguages.getResultList()).thenReturn(languageList);
    }

}
