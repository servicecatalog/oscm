/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-8                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.oscm.internal.intf.OperatorService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import org.oscm.types.enumtypes.StandardLanguage;
import org.oscm.ui.beans.ApplicationBean.DatePatternEnum;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.stubs.ApplicationStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.ResourceBundleStub;
import org.oscm.internal.types.exception.TranslationImportException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;

/**
 * @author Yuyin
 * 
 */
public class ExcelHandlerTest {

    private static final String DE = "de";
    private static final String EN = "en";
    private static final String JA = "ja";
    private static final String ZH = "zh";
    private static final String VALUE = "value";
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final String SHEET_NAME = BaseBean.LABEL_USERINTERFACE_TRANSLARIONS;

    @Test
    public void validateDatePattern_DATE_PATTERN() throws Exception {
        String pattern = "yyyy-MM-dd z";
        String result = ExcelHandler.validateDatePattern(
                DatePatternEnum.DATE_PATTERN.getMessageKey(), pattern);
        assertEquals(pattern, result);

        result = ExcelHandler.validateDatePattern(
                DatePatternEnum.DATE_PATTERN.getMessageKey(), " " + pattern
                        + " ");
        assertEquals(pattern, result);
    }

    @Test
    public void validateDatePattern_DATE_INPUT_PATTERN() throws Exception {
        String pattern = "yyyy-MM-dd z";
        String result = ExcelHandler.validateDatePattern(
                DatePatternEnum.DATE_INPUT_PATTERN.getMessageKey(), pattern);
        assertEquals(pattern, result);

        result = ExcelHandler.validateDatePattern(
                DatePatternEnum.DATE_INPUT_PATTERN.getMessageKey(), " "
                        + pattern + " ");
        assertEquals(pattern, result);
    }

    @Test
    public void validateDatePattern_DATE_TIME_PATTERN() throws Exception {
        String pattern = "yyyy-MM-dd z";
        String result = ExcelHandler.validateDatePattern(
                DatePatternEnum.DATE_TIME_PATTERN.getMessageKey(), pattern);
        assertEquals(pattern, result);

        result = ExcelHandler.validateDatePattern(
                DatePatternEnum.DATE_TIME_PATTERN.getMessageKey(), " "
                        + pattern + " ");
        assertEquals(pattern, result);
    }

    @Test(expected = ValidationException.class)
    public void validateDatePattern_InvalidPattern() throws Exception {
        String pattern = "234567tzhuj";
        try {
            ExcelHandler.validateDatePattern(
                    DatePatternEnum.DATE_TIME_PATTERN.getMessageKey(), pattern);
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.FILE_IMPORT_FAILED, e.getReason());
            assertEquals(DatePatternEnum.DATE_TIME_PATTERN.getMessageKey(),
                    e.getMember());
            String[] params = e.getMessageParams();
            assertEquals(1, params.length);
            assertEquals(pattern, params[0]);
            throw e;
        }
    }

    @Test
    public void getCellValue() throws Exception {
        String value = ExcelHandler.getCellValue(prepareRow(true, " key "), 0,
                false);
        assertEquals(" key ", value);
    }

    @Test
    public void getCellValue_Trim() throws Exception {
        String value = ExcelHandler.getCellValue(prepareRow(true, " key "), 0,
                true);
        assertEquals("key", value);
    }

    @Test
    public void getCellValue_Empty() throws Exception {
        String value = ExcelHandler.getCellValue(prepareRow(true, "   "), 0,
                true);
        assertEquals("", value);
    }

    @Test
    public void getCellValue_NullValue() throws Exception {
        String value = ExcelHandler.getCellValue(
                prepareRow(true, new String[] { null }), 0, true);
        assertEquals(null, value);
    }

    @Test
    public void getCellValue_NullCell() throws Exception {
        String value = ExcelHandler.getCellValue(
                prepareRow(false, new String[] { null }), 0, true);
        assertEquals(null, value);
    }

    @Test
    public void readSheet_ManageLanguage() throws Exception {
        // given

        Sheet sheet = prepareSheet(null, true, KEY1, "key2");
        FacesContext fc = prepareContext();
        HashSet<Object> defaultKeySet = new HashSet<Object>();
        defaultKeySet.add(KEY1);
        defaultKeySet.add("key2");

        // when
        Map<String, Properties> map = ExcelHandler.readSheet(sheet, fc
                .getApplication().getSupportedLocales(), SHEET_NAME,
                defaultKeySet);

        // then
        Properties props = map.get(DE);
        assertTrue(props.containsKey(KEY1));
        assertEquals(props.get(KEY1), "key1de ");
        props = map.get("de system");
        assertTrue(props.containsKey(KEY1));
        assertEquals(props.get(KEY1), "\nkey1de");

    }

    @Test
    public void readSheet_TranslationBean() throws Exception {

        Sheet sheet = prepareSheet(null, false, KEY1, " key2 ");
        FacesContext fc = prepareContext();
        Map<String, Properties> map = ExcelHandler.readSheet(sheet, fc
                .getApplication().getSupportedLocales(), SHEET_NAME, null);
        verifyReadExcel(map, KEY1, "\nkey1en", "key1de ", "\nkey1ja");
        verifyReadExcel(map, "key2", "\n key2 en", " key2 de ", "\n key2 ja");
    }

    @Test
    public void readSheet_Bug10650() throws Exception {

        Sheet sheet = prepareSheet("ac", true, KEY1, " key2 ");
        FacesContext fc = prepareContext();
        try {
            ExcelHandler.readSheet(sheet, fc.getApplication()
                    .getSupportedLocales(), SHEET_NAME, null);
            fail();
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.INVALID_LANGUAGE_ISOCODE, e.getReason());
            assertArrayEquals(new String[] { "ac" }, e.getMessageParams());
        }
    }

    @Test
    public void readSheet_KeyNotFound() throws Exception {
        // given

        Sheet sheet = prepareSheet("en", true, KEY1, " key2 ");
        FacesContext fc = prepareContext();
        // when
        try {
            ExcelHandler.readSheet(sheet, fc.getApplication()
                    .getSupportedLocales(), SHEET_NAME, new HashSet<Object>());
            // then
            fail();
        } catch (TranslationImportException e) {
            assertTrue(e.getMessageKey().contains(
                    TranslationImportException.Reason.KEY_NOT_FOUND.toString()));
            assertEquals(KEY1, e.getMessageParams()[0]);
            assertEquals(SHEET_NAME, e.getMessageParams()[1]);
        }
    }

    @Test
    public void validateLocale_InvalidISOCode() throws Exception {
        prepareContext();
        // when
        try {
            ExcelHandler.validateLocale("tta");
            fail();
        } catch (ValidationException e) {
            // then
            assertEquals(ReasonEnum.INVALID_LANGUAGE_ISOCODE, e.getReason());
        }
    }

    @Test
    public void validateLocale_NotSupportedISOCode() throws Exception {
        // given
        prepareContext();
        // when
        try {
            ExcelHandler.validateLocale("aa");
        } catch (ValidationException e) {
            // then
            assertEquals(ReasonEnum.LANGUAGE_ISOCODE_NOT_SUPPORTED,
                    e.getReason());
        }
    }

    @Test
    public void validateLocale() throws Exception {
        // given
        prepareContext();

        // when
        ExcelHandler.validateLocale("en");
    }

    @Test
    public void createExcel_keyFoundInDefaultProperty() throws Exception {
        // given

        List<String> keyList = new ArrayList<String>();
        keyList.add(KEY1);
        Map<String, Properties> localizedProperties = prepareProperties(null,
                KEY2, VALUE + "2");
        Map<String, ResourceBundle> defaultProperties = prepareDefaultProperties(
                null, KEY1, VALUE + "1");
        List<Locale> locales = prepareLocaleList(null);
        prepareFacesContextStub(Locale.GERMAN, Locale.ENGLISH);
        // when
        Workbook result = ExcelHandler.createExcel(keyList, defaultProperties,
                localizedProperties, null,
                BaseBean.LABEL_USERINTERFACE_TRANSLARIONS, locales);
        // then
        verifyCreatedResult(result, "User interface",
                "Add your language code here");
        assertEquals(VALUE + "1", result.getSheetAt(0).getRow(1).getCell(1)
                .getStringCellValue());
    }

    @Test
    public void createExcel_keyNotFoundInDefaultProperty() throws Exception {
        // given

        List<String> keyList = new ArrayList<String>();
        keyList.add(KEY1);
        Map<String, Properties> localizedProperties = prepareProperties(null,
                KEY2, VALUE + "2");
        Map<String, ResourceBundle> defaultProperties = prepareDefaultProperties(
                null, KEY3, VALUE + "3");
        List<Locale> locales = prepareLocaleList(null);
        prepareFacesContextStub(Locale.GERMAN, Locale.ENGLISH);
        // when
        Workbook result = ExcelHandler.createExcel(keyList, defaultProperties,
                localizedProperties, null,
                BaseBean.LABEL_USERINTERFACE_TRANSLARIONS, locales);
        // then
        verifyCreatedResult(result, "User interface",
                "Add your language code here");
        assertEquals("", result.getSheetAt(0).getRow(1).getCell(1)
                .getStringCellValue());
    }

    @Test
    public void createExcel_onlyStandardLanguages() throws Exception {
        // given

        List<String> keyList = new ArrayList<String>();
        keyList.add(KEY1);
        Map<String, ResourceBundle> defaultProperties = prepareDefaultProperties(
                null, KEY1, VALUE + "1");
        List<Locale> locales = prepareLocaleList(null);
        prepareFacesContextStub(Locale.GERMAN, Locale.ENGLISH);
        // when
        Workbook result = ExcelHandler.createExcel(keyList, defaultProperties,
                null, null, BaseBean.LABEL_USERINTERFACE_TRANSLARIONS, locales);
        // then
        verifyCreatedResult(result, "User interface",
                "Add your language code here");
    }

    @Test
    public void createExcel_onlyStandardLanguages_Mail() throws Exception {
        // given

        List<String> keyList = new ArrayList<String>();
        keyList.add(KEY1);
        Map<String, Properties> localizedProperties = prepareFileProperties(
                KEY1, VALUE + "1");
        List<Locale> locales = prepareLocaleList(null);
        prepareFacesContextStub(Locale.GERMAN, Locale.ENGLISH);
        // when
        Workbook result = ExcelHandler.createExcel(keyList, null,
                localizedProperties, null, BaseBean.LABEL_MAIL_TRANSLARIONS,
                locales);
        // then
        verifyCreatedResult(result, "Email", "Add your language code here");
    }

    @Test
    public void createExcel_withOneMoreStandardLanguage() throws Exception {
        // given

        List<String> keyList = new ArrayList<String>();
        keyList.add(KEY1);
        Map<String, Properties> localizedProperties = prepareProperties(DE,
                KEY1, VALUE + "1");
        Map<String, ResourceBundle> defaultProperties = prepareDefaultProperties(
                null, KEY1, VALUE + "1");
        List<Locale> locales = prepareLocaleList(DE);
        prepareFacesContextStub(Locale.GERMAN, Locale.ENGLISH);
        // when
        Workbook result = ExcelHandler.createExcel(keyList, defaultProperties,
                localizedProperties, null,
                BaseBean.LABEL_USERINTERFACE_TRANSLARIONS, locales);
        // then
        verifyCreatedResult(result, "User interface", DE);
    }

    @Test
    public void createExcel_withOneMoreStandardLanguage_NewValueImported()
            throws Exception {
        // given

        List<String> keyList = new ArrayList<String>();
        keyList.add(KEY1);
        Map<String, Properties> localizedProperties = prepareProperties(DE,
                KEY1, VALUE + "2");
        Map<String, ResourceBundle> defaultProperties = prepareDefaultProperties(
                null, KEY1, VALUE + "1");
        List<Locale> locales = prepareLocaleList(DE);
        prepareFacesContextStub(Locale.GERMAN, Locale.ENGLISH);
        // when
        Workbook result = ExcelHandler.createExcel(keyList, defaultProperties,
                localizedProperties, null,
                BaseBean.LABEL_USERINTERFACE_TRANSLARIONS, locales);
        // then
        verifyCreatedResult(result, "User interface", DE);
        assertEquals("value1", result.getSheetAt(0).getRow(1).getCell(1)
                .getStringCellValue());
        assertEquals("value2", result.getSheetAt(0).getRow(1).getCell(4)
                .getStringCellValue());
    }

    @Test
    public void createExcel_withOneMoreStandardLanguage_NoValueInDB()
            throws Exception {
        // given

        List<String> keyList = new ArrayList<String>();
        keyList.add(KEY1);
        Map<String, Properties> localizedProperties = prepareProperties(DE,
                null, null);
        Map<String, ResourceBundle> defaultProperties = prepareDefaultProperties(
                null, KEY1, VALUE + "1");
        List<Locale> locales = prepareLocaleList(DE);
        prepareFacesContextStub(Locale.GERMAN, Locale.ENGLISH);
        // when
        Workbook result = ExcelHandler.createExcel(keyList, defaultProperties,
                localizedProperties, null,
                BaseBean.LABEL_USERINTERFACE_TRANSLARIONS, locales);
        // then
        verifyCreatedResult(result, "User interface", DE);
        assertEquals("value1", result.getSheetAt(0).getRow(1).getCell(1)
                .getStringCellValue());
        assertEquals("", result.getSheetAt(0).getRow(1).getCell(4)
                .getStringCellValue());
    }

    @Test
    public void createExcel_withOneNotStandardLanguage() throws Exception {
        // given

        List<String> keyList = new ArrayList<String>();
        keyList.add(KEY1);
        Map<String, Properties> localizedProperties = prepareProperties(ZH,
                KEY1, VALUE + "1");
        Map<String, ResourceBundle> defaultProperties = prepareDefaultProperties(
                null, KEY1, VALUE + "1");
        List<Locale> locales = prepareLocaleList(ZH);
        prepareFacesContextStub(Locale.GERMAN, Locale.ENGLISH);
        // when
        Workbook result = ExcelHandler.createExcel(keyList, defaultProperties,
                localizedProperties, null,
                BaseBean.LABEL_USERINTERFACE_TRANSLARIONS, locales);
        // then
        verifyCreatedResult(result, "User interface", ZH);
    }

    private void verifyCreatedResult(Workbook result, String sheetName,
            String LastCellValue) {
        assertNotNull(null, result);
        assertEquals(sheetName, result.getSheetAt(0).getSheetName());
        assertEquals(5, result.getSheetAt(0).getRow(0).getLastCellNum());
        assertEquals("Key ", result.getSheetAt(0).getRow(0).getCell(0)
                .getStringCellValue());
        assertEquals("de system", result.getSheetAt(0).getRow(0).getCell(1)
                .getStringCellValue());
        assertEquals("en system", result.getSheetAt(0).getRow(0).getCell(2)
                .getStringCellValue());
        assertEquals("ja system", result.getSheetAt(0).getRow(0).getCell(3)
                .getStringCellValue());
        assertEquals(LastCellValue, result.getSheetAt(0).getRow(0).getCell(4)
                .getStringCellValue());
    }

    @Test
    public void createExcel_TranslationBean_onlyStandardLanguages()
            throws Exception {
        // given

        List<String> keyList = new ArrayList<String>();
        keyList.add(KEY1);
        Map<String, Properties> localizedProperties = prepareProperties(null,
                KEY1, VALUE + "1");
        List<Locale> locales = prepareLocaleList(null);
        prepareFacesContextStub(Locale.GERMAN, Locale.ENGLISH);
        // when
        Workbook result = ExcelHandler.createExcel(keyList, null,
                localizedProperties, null, BaseBean.LABEL_SHOP_TRANSLARIONS,
                locales);
        // then
        verifyCreatedResultForTranslationBean(result, "Customize texts", 4, JA);
    }

    @Test
    public void createExcel_TranslationBean_withOneNotStandardLanguage()
            throws Exception {
        // given

        List<String> keyList = new ArrayList<String>();
        keyList.add(KEY1);
        Map<String, Properties> localizedProperties = prepareProperties(ZH,
                KEY1, VALUE + "1");
        List<Locale> locales = prepareLocaleList(ZH);
        prepareFacesContextStub(Locale.GERMAN, Locale.ENGLISH);
        // when
        Workbook result = ExcelHandler.createExcel(keyList, null,
                localizedProperties, null, BaseBean.LABEL_SHOP_TRANSLARIONS,
                locales);
        // then
        verifyCreatedResultForTranslationBean(result, "Customize texts", 5, ZH);
    }

    @Test
    public void createExcel_TranslationBean_KeyNotFoundInDB() throws Exception {
        // given
        List<String> keyList = new ArrayList<String>();
        keyList.add(KEY1);
        Map<String, ResourceBundle> defaultProperties = prepareDefaultProperties(
                null, KEY1, VALUE + "1");
        Map<String, Properties> localizedProperties = prepareProperties(null,
                KEY2, VALUE + "2");
        List<Locale> locales = prepareLocaleList(null);
        prepareFacesContextStub(Locale.GERMAN, Locale.ENGLISH);
        // when
        Workbook result = ExcelHandler.createExcel(keyList, defaultProperties,
                localizedProperties, null, BaseBean.LABEL_SHOP_TRANSLARIONS,
                locales);
        // then
        verifyCreatedResultForTranslationBean(result, "Customize texts", 4, JA);
        assertEquals(VALUE + "1", result.getSheetAt(0).getRow(1).getCell(1)
                .getStringCellValue());

    }

    private void verifyCreatedResultForTranslationBean(Workbook result,
            String sheetName, int columnNum, String LastCellValue) {
        assertNotNull(null, result);
        assertEquals(sheetName, result.getSheetAt(0).getSheetName());
        assertEquals(columnNum, result.getSheetAt(0).getRow(0).getLastCellNum());
        assertEquals("Key ", result.getSheetAt(0).getRow(0).getCell(0)
                .getStringCellValue());
        assertEquals("de", result.getSheetAt(0).getRow(0).getCell(1)
                .getStringCellValue());
        assertEquals("en", result.getSheetAt(0).getRow(0).getCell(2)
                .getStringCellValue());
        assertEquals("ja", result.getSheetAt(0).getRow(0).getCell(3)
                .getStringCellValue());
        assertEquals(LastCellValue,
                result.getSheetAt(0).getRow(0).getCell(columnNum - 1)
                        .getStringCellValue());
    }

    private List<Locale> prepareLocaleList(String languageCode) {
        List<Locale> locales = addStandardLocales();
        if (languageCode != null) {
            locales.add(new Locale(languageCode));
        }
        return locales;
    }

    private List<Locale> addStandardLocales() {
        List<Locale> locales = new ArrayList<Locale>();
        locales.add(new Locale(DE));
        locales.add(new Locale(EN));
        locales.add(new Locale(JA));
        return locales;
    }

    private FacesContext prepareFacesContextStub(Locale userLocale,
            final Locale defaultLocale) {
        FacesContext fc = new FacesContextStub(userLocale) {

            ExternalContext ec = mock(ExternalContext.class);

            @Override
            public ExternalContext getExternalContext() {
                return ec;
            }

            @Override
            public Application getApplication() {

                return new ApplicationStub() {
                    @Override
                    public ResourceBundle getResourceBundle(FacesContext ctx,
                            String name) {
                        ResourceBundleStub resourceBundleStub = new ResourceBundleStub();
                        resourceBundleStub.addResource(
                                BaseBean.LABEL_USERINTERFACE_TRANSLARIONS,
                                "User interface");
                        resourceBundleStub.addResource(
                                BaseBean.LABEL_SHOP_TRANSLARIONS,
                                "Customize texts");

                        return resourceBundleStub;
                    }

                    @Override
                    public Locale getDefaultLocale() {
                        return defaultLocale;
                    }

                    @Override
                    public Iterator<Locale> getSupportedLocales() {
                        List<Locale> locales = new ArrayList<Locale>();
                        locales.add(Locale.ENGLISH);
                        locales.add(Locale.GERMAN);
                        locales.add(Locale.JAPANESE);
                        locales.add(new Locale("in"));
                        return locales.iterator();
                    }
                };
            }
        };
        return fc;
    }

    private FacesContext prepareContext() throws Exception {
        FacesContext fc = prepareFacesContextStub(Locale.ENGLISH,
                Locale.ENGLISH);
        return fc;
    }

    /**
     * Prepares a {@link Row} object that returns the provided values accessible
     * with its index (0-based).
     * 
     * @param returnCell
     *            <code>true</code> if <code>null</code> should be returned as
     *            {@link Cell} from the {@link Row}.
     * @param values
     *            the values to be returned by the cell
     * @return the {@link Row}
     */
    private Row prepareRow(boolean returnCell, String... values) {
        Row row = mock(Row.class);
        if (returnCell) {
            for (int i = 0; i < values.length; i++) {
                Cell cell = mock(Cell.class);
                when(cell.getStringCellValue()).thenReturn(values[i]);
                when(row.getCell(eq(i))).thenReturn(cell);
            }
        } else {
            when(row.getCell(anyInt())).thenReturn(null);
        }
        return row;
    }

    /**
     * Prepares a {@link Sheet} that contains the first row with the headers
     * ('key', 'de system','en system','ja system' and 'isoCode') and beginning
     * from the second row (index 1) a row per provided key - the first column
     * contains the key, the second one '\n' + key + 'de' and then '\n' + key +
     * 'en','\n' + key +'ja',the last one key + 'isoCode '.
     * 
     * @param keys
     *            the keys to be contained
     * @return the {@link Sheet}
     */
    private Sheet prepareSheet(String isoCode, boolean isManageLanguage,
            String... keys) {
        Sheet sheet = mock(Sheet.class);
        doReturn(SHEET_NAME).when(sheet).getSheetName();
        // column headers
        Row row = null;
        if (isoCode == null) {
            isoCode = "de";
        }

        if (isManageLanguage) {
            row = prepareRow(true, "key", "de system", "en system",
                    "ja system", isoCode);
        } else {
            row = prepareRow(true, "key", "de", "en", "ja", isoCode);

        }

        when(sheet.getRow(eq(0))).thenReturn(row);
        // data
        for (int i = 0; i < keys.length; i++) {
            row = prepareRow(true, keys[i], "\n" + keys[i] + "de", "\n"
                    + keys[i] + "en", "\n" + keys[i] + "ja", keys[i] + isoCode
                    + " ");
            when(sheet.getRow(eq(i + 1))).thenReturn(row);
        }
        when(Integer.valueOf(sheet.getLastRowNum())).thenReturn(
                Integer.valueOf(keys.length));
        return sheet;
    }

    private void verifyReadExcel(Map<String, Properties> map, String key,
            String valueEn, String valueDe, String valueJa) {
        Properties props = map.get(EN);
        assertTrue(props.containsKey(key));
        assertEquals(props.get(key), valueEn);

        props = map.get(DE);
        assertTrue(props.containsKey(key));
        assertEquals(props.get(key), valueDe);

        props = map.get(JA);
        assertTrue(props.containsKey(key));
        assertEquals(props.get(key), valueJa);
    }

    private Map<String, Properties> prepareProperties(String language,
            String key, String value) {
        Map<String, Properties> localizedProperties = new HashMap<String, Properties>();
        Properties properties = new Properties();
        if (key != null) {
            properties.put(key, value);
        }
        if (language != null) {
            localizedProperties.put(language, properties);
        }
        localizedProperties.put(DE, properties);
        localizedProperties.put(EN, properties);
        localizedProperties.put(JA, properties);
        return localizedProperties;
    }

    private Map<String, Properties> prepareFileProperties(String key,
            String value) {
        Map<String, Properties> localizedProperties = new HashMap<String, Properties>();
        Properties properties = new Properties();
        properties.put(key, value);
        localizedProperties.put(DE + StandardLanguage.COLUMN_HEADING_SUFFIX,
                properties);
        localizedProperties.put(EN + StandardLanguage.COLUMN_HEADING_SUFFIX,
                properties);
        localizedProperties.put(JA + StandardLanguage.COLUMN_HEADING_SUFFIX,
                properties);
        return localizedProperties;
    }

    private Map<String, ResourceBundle> prepareDefaultProperties(
            String language, String key, String value) {
        Map<String, ResourceBundle> defaultProperties = new HashMap<String, ResourceBundle>();
        PropertyResourceBundle bundle = mock(PropertyResourceBundle.class);
        doReturn(value).when(bundle).handleGetObject(key);
        if (language != null) {
            defaultProperties.put(language, bundle);
        }
        defaultProperties.put(DE, bundle);
        defaultProperties.put(EN, bundle);
        defaultProperties.put(JA, bundle);
        return defaultProperties;
    }
}
