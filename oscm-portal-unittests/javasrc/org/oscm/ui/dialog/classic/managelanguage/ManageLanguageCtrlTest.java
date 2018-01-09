/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-11-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.managelanguage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.types.enumtypes.LocalizedDataType;
import org.oscm.types.enumtypes.StandardLanguage;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.stubs.ApplicationStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.ResourceBundleStub;
import org.oscm.internal.operatorservice.LocalizedDataService;
import org.oscm.internal.operatorservice.ManageLanguageService;
import org.oscm.internal.operatorservice.POLocalizedData;
import org.oscm.internal.operatorservice.POSupportedLanguage;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.PropertiesImportException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.TranslationImportException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;

/**
 * @author zou
 * 
 */

public class ManageLanguageCtrlTest {

    private ManageLanguageCtrl ctrl;
    private ManageLanguageModel model;
    private final String DE = "de";
    private final String EN = "en";
    private final String JA = "ja";
    private final String KEY = "key";
    private final String VALUE = "value";
    private FacesContextStub context;
    ManageLanguageService manageLanguageService;
    LocalizedDataService localizedDataService;
    UiDelegate ui = new UiDelegate();
    private String message;
    private Part uploadedFile;
    @Captor
    ArgumentCaptor<SaaSApplicationException> exceptionToStore;
    
    private Part excelFile = mock(Part.class);

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        message = null;
        model = spy(new ManageLanguageModel());
        List<POSupportedLanguage> languages = new ArrayList<POSupportedLanguage>();
        languages.add(preparePOSupportedLanguage(DE, true, false));
        languages.add(preparePOSupportedLanguage(EN, true, true));
        model.setLanguages(languages);
        model.setDefaultLanguageCode();

        model.setLocalizedData(preparePOLocalizedData(EN, null, DE));

        model.setInitialized(false);
        manageLanguageService = mock(ManageLanguageService.class);
        localizedDataService = mock(LocalizedDataService.class);

        ctrl = new ManageLanguageCtrl() {
            private static final long serialVersionUID = -5490657213220872346L;

            @Override
            protected ManageLanguageService getManageLanguageService() {
                return manageLanguageService;
            }

            @Override
            protected LocalizedDataService getLocalizedDataService() {
                return localizedDataService;
            }

            @Override
            protected void addMessage(String clientId,
                    FacesMessage.Severity severity, String key) {
                message = key;
            }

            @Override
            protected void addMessage(String clientId,
                    FacesMessage.Severity severity, String key, Object[] params) {
                message = key;
            }

            @Override
            String readExcel(Workbook wb, String sheetName,
                    List<POLocalizedData> excelDatas, List<Locale> locales,
                    LocalizedDataType type) throws ValidationException,
                    TranslationImportException {
                return "de";
            }
        };
        ctrl.setModel(model);
        ctrl.ui = spy(new UiDelegate());
        doReturn(context).when(ctrl.ui).getFacesContext();
        context = new FacesContextStub(Locale.ENGLISH);
        ResourceBundleStub resourceBundleStub = new ResourceBundleStub();
        ((ApplicationStub) context.getApplication())
                .setResourceBundleStub(resourceBundleStub);
        List<Locale> locales = new ArrayList<Locale>();
        locales.add(Locale.ENGLISH);
        ((ApplicationStub) context.getApplication())
                .setSupportedLocales(locales);
        uploadedFile = mock(Part.class);
        doReturn("name").when(uploadedFile).getName();
    }

    @Test
    public void getInitialize() throws Exception {
        // given
        when(manageLanguageService.getLanguages(eq(false))).thenReturn(
                new ArrayList<POSupportedLanguage>());
        // when
        ctrl.getInitialize();

        // then
        boolean initialized = model.isInitialized();
        assertEquals(Boolean.valueOf(true), Boolean.valueOf(initialized));
        assertNotNull(model.getLanguages());
        verify(manageLanguageService, times(1)).getLanguages(eq(false));
    }

    @Test
    public void save() throws Exception {
        // given
        doNothing().when(manageLanguageService).saveLanguages(
                anyListOf(POSupportedLanguage.class));
        doReturn(Boolean.TRUE).when(model).isTokenValid();

        // when
        String result = ctrl.save();

        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        verify(manageLanguageService, times(1)).saveLanguages(
                anyListOf(POSupportedLanguage.class));
    }

    @Test
    public void save_ValidationException() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(model).isTokenValid();
        doThrow(new ValidationException()).when(manageLanguageService)
                .saveLanguages(anyListOf(POSupportedLanguage.class));
        // when
        String result = ctrl.save();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        verify(manageLanguageService, times(1)).saveLanguages(
                anyListOf(POSupportedLanguage.class));
    }

    @Test
    public void save_inValidToken() throws Exception {
        // given
        doReturn(Boolean.FALSE).when(model).isTokenValid();

        // when
        String result = ctrl.save();

        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    @Test
    public void addLanuage() throws Exception {
        // given
        doNothing().when(manageLanguageService).saveLanguages(
                anyListOf(POSupportedLanguage.class));
        model.setLanguages(new ArrayList<POSupportedLanguage>());
        doReturn(Boolean.TRUE).when(model).isTokenValid();

        // when
        String result = ctrl.save();

        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertNotNull(model.getLanguages());
        verify(ctrl.ui, never()).resetDirty();
    }

    @Test
    public void addLanuage_ISOCodeIsNull() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(model).isTokenValid();

        // when
        String result = ctrl.addLanuage();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        verify(manageLanguageService, never()).saveLanguages(
                anyListOf(POSupportedLanguage.class));
        verify(ctrl.ui, times(1)).resetDirty();
    }

    @Test
    public void addLanuage_ISONotSupported() throws SaaSApplicationException {
        // given
        doThrow(new ValidationException()).when(manageLanguageService)
                .saveLanguages(anyListOf(POSupportedLanguage.class));
        model.setNewISOCode("en");
        doReturn(Boolean.TRUE).when(model).isTokenValid();

        // when
        String result = ctrl.addLanuage();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        verify(manageLanguageService, times(1)).saveLanguages(
                anyListOf(POSupportedLanguage.class));
        verify(ctrl.ui, times(1)).resetDirty();
    }

    @Test
    public void addLanuage_inValidToken() throws Exception {
        // given
        doReturn(Boolean.FALSE).when(model).isTokenValid();

        // when
        String result = ctrl.addLanuage();

        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    @Test
    public void createExcel_ExportDE() throws IOException,
            ObjectNotFoundException {
        // given
        POLocalizedData localizedData = preparePOLocalizedData(DE,
                new Properties(), DE);
        localizedData.setType(LocalizedDataType.MailProperties);
        List<POLocalizedData> localizedDatas = new ArrayList<POLocalizedData>();
        localizedDatas.add(localizedData);
        doReturn(localizedDatas).when(localizedDataService).exportProperties(
                anyString());
        // when
        byte[] result = ctrl.createExcel();

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.length > 0));
        verify(localizedDataService, atLeastOnce()).exportProperties(eq("de"));
    }

    @Test
    public void importTranslations_NoImprotFile() {
        // when
        String result = ctrl.importTranslations();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals("error.shop.translations.fileformat", message);
        verify(ctrl.ui, times(1)).resetDirty();
    }

    @Test
    public void importTranslations_fomartError() throws Exception {
        // given
        ctrl.setExcel(excelFile);
        // when
        String result = ctrl.importTranslations();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals("error.shop.translations.fileformat", message);
        verify(ctrl.ui, times(1)).resetDirty();
    }

    @Test
    public void importTranslations_missingStandardLanguage() throws Exception {
        // given
        prepareForImport(TranslationImportException.Reason.MISSING_STANDARD_LANGUAGE);

        // when
        String returnvalue = ctrl.importTranslations();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, returnvalue);
        assertEquals(null, message);
        verify(ctrl, times(1)).handleException(exceptionToStore.capture());
        String messageKey = exceptionToStore.getValue().getMessageKey();
        assertEquals(messageKey,
                "ex.TranslationImportException.MISSING_STANDARD_LANGUAGE");

    }

    @Test
    public void importTranslations_missingSheetName() throws Exception {
        // given
        prepareForImport(TranslationImportException.Reason.SHEET_NAME_NOT_FOUND);

        // when
        String returnvalue = ctrl.importTranslations();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, returnvalue);
        verify(ctrl, times(1)).handleException(exceptionToStore.capture());
        String messageKey = exceptionToStore.getValue().getMessageKey();
        assertEquals("ex.TranslationImportException.SHEET_NAME_NOT_FOUND",
                messageKey);
    }

    @Test
    public void importTranslations_multiLanguageCodeNotSupported()
            throws Exception {
        // given
        prepareForImport(TranslationImportException.Reason.MULTI_LANGUAGE_CODE_NOT_SUPPORTE);

        // when
        String returnvalue = ctrl.importTranslations();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, returnvalue);
        verify(ctrl, times(1)).handleException(exceptionToStore.capture());
        String messageKey = exceptionToStore.getValue().getMessageKey();
        assertEquals(
                "ex.TranslationImportException.MULTI_LANGUAGE_CODE_NOT_SUPPORTE",
                messageKey);
    }

    @Test
    public void importTranslations_Bug10650() throws Exception {
        // given
        ctrl = spy(ctrl);
        when(
                ctrl.readExcel(any(Workbook.class), anyString(),
                        anyListOf(POLocalizedData.class),
                        anyListOf(Locale.class), any(LocalizedDataType.class)))
                .thenThrow(
                        new ValidationException(
                                ReasonEnum.INVALID_LANGUAGE_ISOCODE,
                                "languageISOCode", new Object[] { "ac" }));

        POLocalizedData localizedData = preparePOLocalizedData("ac",
                new Properties(), "ac");
        localizedData.setType(LocalizedDataType.MailProperties);
        List<POLocalizedData> localizedDatas = new ArrayList<POLocalizedData>();
        localizedDatas.add(localizedData);
        doReturn(localizedDatas).when(localizedDataService).exportProperties(
                anyString());
        final byte[] result = ctrl.createExcel();

        doAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation)
                    throws Throwable {
                return new ByteArrayInputStream(result);
            }
        }).when(uploadedFile).getInputStream();
        ctrl.setExcel(uploadedFile);

        // when
        String returnvalue = ctrl.importTranslations();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, returnvalue);
        assertEquals(BaseBean.ERROR_FILE_IMPORT_FAILED_ISOCODE_INVALID, message);
    }

    @Test
    public void importTranslations_missingKeys() throws Exception {
        // given
        prepareForImport(TranslationImportException.Reason.MISSING_KEY);

        // when
        String returnValue = ctrl.importTranslations();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, returnValue);
        verify(ctrl, times(1)).handleException(exceptionToStore.capture());
        String messageKey = exceptionToStore.getValue().getMessageKey();
        assertEquals("ex.TranslationImportException.MISSING_KEY", messageKey);
    }

    @Test
    public void importTranslations_translationMissing() throws Exception {
        ManageLanguageCtrl mlSpy = spy(ctrl);
        // given
        prepareForImportWithException(mlSpy,
                "ex.PropertiesImportException.TRANSLATIONS_MISSING");

        // when
        String returnValue = mlSpy.importTranslations();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, returnValue);
        assertEquals("ex.PropertiesImportException.TRANSLATIONS_MISSING",
                this.message);
    }

    @SuppressWarnings("boxing")
    @Test
    public void importTranslations_translationMissingAndDuplicatKeys()
            throws Exception {
        // given
        ManageLanguageCtrl mlSpy = spy(ctrl);
        final List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();
        final String firstValue = "{0} UI texts for {1} languages have been imported, but translations are multiple.";
        final String finalValue = "0 UI texts for 1 languages have been imported, but translations are multiple.";
        final String key = BaseBean.ERROR_FILE_IMPORT_TRANSLATIONS_MULTIPLEKEY_EXISTING;
        final String messageFortranslationMissing = "languages have been imported, but translations are missing.";

        context = givenContextStub(facesMessages);
        // given
        givenJSFUtilsMessage(context, key, firstValue, null);
        assertEquals(JSFUtils.existMessageInList(context, firstValue), true);
        prepareForImportWithException(mlSpy,
                "ex.PropertiesImportException.TRANSLATIONS_MISSING");

        // when
        String returnValue = mlSpy.importTranslations();

        // then
        verify(mlSpy.ui).resetDirty();
        verify(mlSpy, never()).addMissingTranslationWarning(anyString());

        assertEquals(BaseBean.OUTCOME_ERROR, returnValue);
        assertEquals(true, JSFUtils.existMessageInList(context, finalValue));
        assertFalse(containsMessageInList(context, messageFortranslationMissing));

    }

    private boolean containsMessageInList(FacesContext fc, String msg) {
        for (Iterator<FacesMessage> i = fc.getMessages(); i.hasNext();) {
            if (i.next().getDetail().contains(msg)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void getDefaultKeySet_ui() throws IOException {
        // given
        prepareForDefaultKeys();

        // when
        Set<Object> result = ctrl
                .getDefaultKeySet(BaseBean.LABEL_USERINTERFACE_TRANSLARIONS);

        // then
        assertTrue(result.contains(BaseBean.LABEL_USERINTERFACE_TRANSLARIONS));
        assertTrue(result.contains(BaseBean.LABEL_MAIL_TRANSLARIONS));
        assertTrue(result.contains(BaseBean.LABEL_PLATFORM_TRANSLARIONS));
    }

    @Test
    public void getDefaultKeySet_mail() throws IOException {
        prepareForDefaultKeys();

        // when
        Set<Object> result = ctrl
                .getDefaultKeySet(BaseBean.LABEL_MAIL_TRANSLARIONS);

        // then
        assertTrue(result.contains(BaseBean.LABEL_MAIL_TRANSLARIONS));
        assertEquals(new Integer(result.size()), new Integer(1));
    }

    @Test
    public void getDefaultKeySet_platformObject() throws IOException {
        prepareForDefaultKeys();

        // when
        Set<Object> result = ctrl
                .getDefaultKeySet(BaseBean.LABEL_PLATFORM_TRANSLARIONS);

        // then
        assertTrue(result.contains(BaseBean.LABEL_PLATFORM_TRANSLARIONS));
        assertEquals(new Integer(result.size()), new Integer(1));
    }

    @Test
    public void setNewISOCode() {
        // given
        model.setNewISOCode("EN");

        // then
        assertEquals("en", model.getNewISOCode());

        // given
        model.setNewISOCode("");

        // then
        assertEquals("", model.getNewISOCode());
    }

    @Test
    public void setNewISOCode_NullISOCode() {
        // given
        model.setNewISOCode(null);

        // then
        assertEquals("", model.getNewISOCode());
    }

    @SuppressWarnings("boxing")
    @Test
    public void importTranslations_translationMultiple() throws Exception {
        final ManageLanguageCtrl mlSpy = spy(ctrl);
        final List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();
        final String firstValue = "{0} UI texts for {1} languages have been imported, but translations are multiple.";
        final String finalValue = "0 UI texts for 1 languages have been imported, but translations are multiple.";
        final String key = BaseBean.ERROR_FILE_IMPORT_TRANSLATIONS_MULTIPLEKEY_EXISTING;

        FacesContextStub contextStub = givenContextStub(facesMessages);
        // given
        givenJSFUtilsMessage(contextStub, key, firstValue, null);
        assertEquals(JSFUtils.existMessageInList(contextStub, firstValue), true);
        prepareForImportWithException(mlSpy, null);

        // when
        String returnValue = mlSpy.importTranslations();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, returnValue);
        assertEquals(JSFUtils.existMessageInList(contextStub, finalValue), true);

    }

    /**
     * @param facesMessages
     * @return
     */
    private FacesContextStub givenContextStub(
            final List<FacesMessage> facesMessages) {
        FacesContextStub contextStub = new FacesContextStub(Locale.ENGLISH) {
            @Override
            public void addMessage(String arg0, FacesMessage arg1) {
                facesMessages.add(arg1);
            }

            @Override
            public Iterator<FacesMessage> getMessages() {
                return facesMessages.iterator();
            }
        };
        return contextStub;
    }

    /**
     * @param contextStub
     */
    @SuppressWarnings("unused")
    private void givenJSFUtilsMessage(FacesContextStub contextStub, String key,
            String value, Object[] params) {
        setResourceBundle(contextStub, key, value);

        FacesMessage firstFM = new FacesMessage(FacesMessage.SEVERITY_WARN,
                value, null);
        firstFM.setDetail(value);
        firstFM.setSummary(value);
        contextStub.addMessage(null, firstFM);
    }

    private void setResourceBundle(FacesContextStub contextStub, String key,
            String value) {
        ResourceBundleStub resourceBundleStub = new ResourceBundleStub();
        ((ApplicationStub) contextStub.getApplication())
                .setResourceBundleStub(resourceBundleStub);
        resourceBundleStub.addResource(key, value);
    }

    private POLocalizedData preparePOLocalizedData(String language,
            Properties properties, String selectedLanguage) {
        POLocalizedData po = new POLocalizedData();
        Map<String, Properties> localizedProperties = new HashMap<String, Properties>();
        localizedProperties.put(language, properties);
        Properties enProperties = new Properties();
        enProperties.put(KEY, VALUE);
        localizedProperties.put(EN, enProperties);
        localizedProperties.put(DE, enProperties);
        localizedProperties.put(JA, enProperties);
        localizedProperties.put(EN + StandardLanguage.COLUMN_HEADING_SUFFIX,
                enProperties);
        localizedProperties.put(DE + StandardLanguage.COLUMN_HEADING_SUFFIX,
                enProperties);
        localizedProperties.put(JA + StandardLanguage.COLUMN_HEADING_SUFFIX,
                enProperties);
        po.setPropertiesMap(localizedProperties);
        model.setSelectedLanguageCode(selectedLanguage);
        return po;
    }

    private POSupportedLanguage preparePOSupportedLanguage(String language,
            boolean active, boolean isDefault) {
        POSupportedLanguage po = new POSupportedLanguage();
        po.setLanguageISOCode(language);
        po.setActive(active);
        po.setDefaultLanguageStatus(isDefault);
        return po;
    }

    private void prepareForDefaultKeys() {
        Properties mailProperties = new Properties();
        mailProperties.put(BaseBean.LABEL_MAIL_TRANSLARIONS,
                BaseBean.LABEL_MAIL_TRANSLARIONS);
        Properties platformProperties = new Properties();
        platformProperties.put(BaseBean.LABEL_PLATFORM_TRANSLARIONS,
                BaseBean.LABEL_PLATFORM_TRANSLARIONS);
        when(localizedDataService.loadMailPropertiesFromFile(anyString()))
                .thenReturn(mailProperties);
        when(localizedDataService.loadPlatformObjectsFromFile(anyString()))
                .thenReturn(platformProperties);
    }

    private void prepareForImport(TranslationImportException.Reason reason)
            throws Exception, TranslationImportException,
            PropertiesImportException {
        ctrl = spy(ctrl);
        when(
                ctrl.readExcel(any(Workbook.class), anyString(),
                        anyListOf(POLocalizedData.class),
                        anyListOf(Locale.class), any(LocalizedDataType.class)))
                .thenThrow(new TranslationImportException(reason));

        POLocalizedData localizedData = preparePOLocalizedData(DE,
                new Properties(), DE);
        localizedData.setType(LocalizedDataType.MailProperties);
        List<POLocalizedData> localizedDatas = new ArrayList<POLocalizedData>();
        localizedDatas.add(localizedData);
        doReturn(localizedDatas).when(localizedDataService).exportProperties(
                anyString());
        final byte[] result = ctrl.createExcel();

        doAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation)
                    throws Throwable {
                return new ByteArrayInputStream(result);
            }
        }).when(uploadedFile).getInputStream();
        ctrl.setExcel(uploadedFile);
    }

    private void prepareForImportWithException(ManageLanguageCtrl ctrlSpy,
            String outcomeException) throws Exception,
            TranslationImportException, PropertiesImportException {

        when(
                ctrlSpy.readExcel(any(Workbook.class), anyString(),
                        anyListOf(POLocalizedData.class),
                        anyListOf(Locale.class), any(LocalizedDataType.class)))
                .thenReturn(DE);
        when(
                localizedDataService.importProperties(
                        anyListOf(POLocalizedData.class), anyString()))
                .thenReturn(outcomeException);

        POLocalizedData localizedData = preparePOLocalizedData(DE,
                new Properties(), DE);
        localizedData.setType(LocalizedDataType.MailProperties);
        List<POLocalizedData> localizedDatas = new ArrayList<POLocalizedData>();
        localizedDatas.add(localizedData);
        doReturn(localizedDatas).when(localizedDataService).exportProperties(
                anyString());
        final byte[] result = ctrlSpy.createExcel();

        doAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation)
                    throws Throwable {
                return new ByteArrayInputStream(result);
            }
        }).when(uploadedFile).getInputStream();
        ctrlSpy.setExcel(uploadedFile);
    }

}
