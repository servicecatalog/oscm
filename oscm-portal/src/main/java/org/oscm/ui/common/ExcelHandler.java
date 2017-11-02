/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-8                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.oscm.types.enumtypes.StandardLanguage;
import org.oscm.ui.beans.ApplicationBean.DatePatternEnum;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.resources.DefaultMessages;
import org.oscm.internal.types.exception.TranslationImportException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;

/**
 * @author Yuyin
 * 
 */
public class ExcelHandler {
    private final static String ADDLANGUAGE = "Add your language code here";

    /**
     * Create a new excel file which contains the translations
     * 
     * @return a byte array which represents the created excel file.
     * @throws IOException
     *             Thrown in case the serialization of the excel file fails.
     */
    public static Workbook createExcel(List<String> keyList,
            Map<String, ResourceBundle> defaultProperties,
            Map<String, Properties> localizedProperties, Workbook wb,
            String sheetName, List<Locale> locales) {
        if (wb == null) {
            wb = new HSSFWorkbook();
        }

        String sheetNameForDisplay = getDefaultResourceBundle().getString(
                sheetName);

        Sheet sheet = wb.getSheet(sheetNameForDisplay);
        if (sheet == null) {
            sheet = wb.createSheet(sheetNameForDisplay);

            CellStyle styleTitle = initializeSheet(wb, sheet);

            List<String> localeList = createFirstRow(sheetName, locales, sheet,
                    styleTitle);

            createRows(keyList, defaultProperties, localizedProperties, sheet,
                    localeList, sheetName);
        }
        return wb;
    }

    private static List<String> createFirstRow(String sheetName,
            List<Locale> locales, Sheet sheet, CellStyle styleTitle) {
        int colIdx = 0;
        Row titleRow = sheet.createRow(0);
        sheet.setColumnWidth(colIdx, 30 * 256);
        Cell titleCell = titleRow.createCell(colIdx++);
        titleCell.setCellStyle(styleTitle);
        titleCell.setCellValue(getDefaultResourceBundle().getString(
                BaseBean.LABEL_SHOP_TRANSLARIONS_KEY));
        return createColumnHeaders(sheetName, locales, sheet, styleTitle,
                colIdx, titleRow);
    }

    private static List<String> createColumnHeaders(String sheetName,
            List<Locale> locales, Sheet sheet, CellStyle styleTitle,
            int colIdx, Row titleRow) {
        Cell titleCell;
        List<String> localeList = new ArrayList<String>();
        int localesSize = locales.size();
        String cellValue = null;
        for (int i = 0; i < localesSize; i++) {
            sheet.setColumnWidth(colIdx, 40 * 256);
            titleCell = titleRow.createCell(colIdx++);
            titleCell.setCellStyle(styleTitle);
            if (i < 3
                    && !BaseBean.LABEL_SHOP_TRANSLARIONS.equals(sheetName)
                    && StandardLanguage.isStandardLanguage(locales.get(i)
                            .getLanguage())) {
                cellValue = locales.get(i).getLanguage()
                        + StandardLanguage.COLUMN_HEADING_SUFFIX;
                titleCell.setCellValue(cellValue);
                localeList.add(cellValue);

            } else {
                cellValue = locales.get(i).getLanguage();
                titleCell.setCellValue(cellValue);
                localeList.add(cellValue);
            }
        }
        if (locales.size() == 3
                && !BaseBean.LABEL_SHOP_TRANSLARIONS.equals(sheetName)) {
            sheet.setColumnWidth(colIdx, 40 * 256);
            titleCell = titleRow.createCell(colIdx++);
            titleCell.setCellStyle(styleTitle);
            titleCell.setCellValue(ADDLANGUAGE);
        }

        return localeList;
    }

    private static CellStyle initializeSheet(Workbook wb, Sheet sheet) {
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        CellStyle styleTitle;
        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short) 12);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleFont.setFontName("Arial");
        styleTitle = wb.createCellStyle();
        styleTitle.setFont(titleFont);
        return styleTitle;
    }

    private static void createRows(List<String> keyList,
            Map<String, ResourceBundle> defaultProperties,
            Map<String, Properties> localizedProperties, Sheet sheet,
            List<String> localeList, String sheetName) {
        sheet.createFreezePane(1, 1);
        int rowIdx = 1;
        int colIdx = 0;
        for (String key : keyList) {
            Row row = sheet.createRow(rowIdx++);
            colIdx = 0;
            row.createCell(colIdx++).setCellValue(key);

            for (String locale : localeList) {
                String cellValue = null;
                cellValue = getCellValue(defaultProperties,
                        localizedProperties, key, locale, sheetName);
                row.createCell(colIdx++).setCellValue(cellValue);
            }
        }
    }

    private static String getCellValue(
            Map<String, ResourceBundle> defaultProperties,
            Map<String, Properties> localizedProperties, String key,
            String locale, String sheetName) {
        String cellValue;

        if (BaseBean.LABEL_SHOP_TRANSLARIONS.equals(sheetName)) {
            cellValue = getCellValueForTranslation(defaultProperties,
                    localizedProperties, key, locale);
        } else {
            cellValue = getCellValueForManageLanguage(defaultProperties,
                    localizedProperties, key, locale, sheetName);
        }

        return cellValue;
    }

    private static String getCellValueForTranslation(
            Map<String, ResourceBundle> defaultProperties,
            Map<String, Properties> localizedProperties, String key,
            String locale) {
        String cellValue = localizedProperties.get(locale).getProperty(key);
        if (cellValue == null) {
            cellValue = getCellValueFromBundle(defaultProperties, key, locale);
        }
        return cellValue;
    }

    private static String getCellValueForManageLanguage(
            Map<String, ResourceBundle> defaultProperties,
            Map<String, Properties> localizedProperties, String key,
            String locale, String sheetName) {
        String cellValue = null;
        if (StandardLanguage.isStandardLanguage(locale,
                StandardLanguage.COLUMN_HEADING_SUFFIX)
                && BaseBean.LABEL_USERINTERFACE_TRANSLARIONS.equals(sheetName)) {
            cellValue = getCellValueFromBundle(defaultProperties, key,
                    removeSuffix(locale));
        } else {
            cellValue = localizedProperties.get(locale).getProperty(key);
        }
        return cellValue;
    }

    private static String getCellValueFromBundle(
            Map<String, ResourceBundle> defaultProperties, String key,
            String locale) {
        ResourceBundle rb = defaultProperties.get(locale);
        String cellValue = null;
        // if rb does not exist and also not exist in localized
        // resource, set empty value
        if (rb != null) {
            try {
                cellValue = rb.getString(key);
            } catch (MissingResourceException ex) {
                // The resource can not be found for the current
                // locale - that's ok
            }
        }
        return cellValue;
    }

    private static String removeSuffix(String s) {
        if (s.endsWith(StandardLanguage.COLUMN_HEADING_SUFFIX)) {
            return s.replace(StandardLanguage.COLUMN_HEADING_SUFFIX, "");
        }
        return s;

    }

    /**
     * Read the excel to get the Map of properties for supported locals
     * 
     * @param wb
     *            workbook which is the source
     * @param supportedLocales
     *            supported Locale Iterator
     * @param sheetName
     * @param defaultKeySet
     *            if this parameter is not null: if there is invalid key not
     *            in this set, TranslationImportException.KEY_NOT_FOUND will
     *            throw.
     * @return
     * @throws ValidationException
     * @throws TranslationImportException
     */
    public static Map<String, Properties> readExcel(Workbook wb,
            Iterator<Locale> supportedLocales, String sheetName,
            Set<Object> defaultKeySet) throws ValidationException,
            TranslationImportException {
        Sheet sheet = null;
        try {
            sheet = wb
                    .getSheet(getDefaultResourceBundle().getString(sheetName));
            if (sheet == null) {
                throw new TranslationImportException();
            }
        } catch (Exception e) {
            throw new TranslationImportException(
                    TranslationImportException.Reason.SHEET_NAME_NOT_FOUND);
        }
        return readSheet(sheet, supportedLocales, sheetName, defaultKeySet);
    }

    public static Map<String, Properties> readExcel(Workbook wb,
            Iterator<Locale> supportedLocales, String sheetName)
            throws ValidationException, TranslationImportException {
        return readExcel(wb, supportedLocales, sheetName, null);
    }

    /**
     * Read the sheet to get the Map of properties for supported locals
     * 
     * @param sheet
     * @param supportedLocales
     * @param sheetName
     * @param defaultKeySet
     *            if this parameter is not null: if there is invalid key not
     *            in this set, TranslationImportException.KEY_NOT_FOUND will
     *            throw.
     * @return 
     * @throws ValidationException
     * @throws TranslationImportException
     */
    public static Map<String, Properties> readSheet(Sheet sheet,
            Iterator<Locale> supportedLocales, String sheetName,
            Set<Object> defaultKeySet) throws ValidationException,
            TranslationImportException {

        List<String> localeStringList = readFirstRow(sheet);

        // create a properties object for each supported locale and uploaded
        // locale
        Map<String, Properties> propertiesMap = initializePropertyMap(
                supportedLocales, localeStringList, sheetName);

        readRows(sheet, propertiesMap, localeStringList, defaultKeySet);

        return propertiesMap;
    }

    private static Map<String, Properties> initializePropertyMap(
            Iterator<Locale> supportedLocales, List<String> localeStringList,
            String sheetName) {
        Map<String, Properties> propertiesMap = new HashMap<String, Properties>();

        if (!BaseBean.LABEL_SHOP_TRANSLARIONS.equals(sheetName)) {
            for (String localeString : localeStringList) {
                if (StandardLanguage.isStandardLanguage(localeString,
                        StandardLanguage.COLUMN_HEADING_SUFFIX)) {
                    propertiesMap.put(localeString, new Properties());
                }
            }
        }

        while (supportedLocales.hasNext()) {
            String localeString = supportedLocales.next().toString();
            if (localeStringList.contains(localeString)) {
                propertiesMap.put(localeString, new Properties());
            }
        }
        return propertiesMap;
    }

    private static List<String> readFirstRow(Sheet sheet)
            throws TranslationImportException, ValidationException {
        List<String> localeStringList = new ArrayList<String>();
        Row row = sheet.getRow(0);
        if (row != null) {
            int colIdx = 1; // skip the first col it contains the keys
            String localeString = "";
            while (true) {
                localeString = getCellValue(row, colIdx++, true);
                if (localeString == null) {
                    break;
                }
                if (StandardLanguage.isStandardLanguage(localeString,
                        StandardLanguage.COLUMN_HEADING_SUFFIX)) {
                    localeStringList.add(localeString);
                    continue;
                }
                validateLocale(localeString);
                localeString = localeString.toLowerCase();
                localeStringList.add(localeString);
            }
        }
        return localeStringList;
    }

    static void validateLocale(String languageISOCode)
            throws ValidationException {

        if (LocaleUtils.isLocaleSupported(languageISOCode)) {
            return;
        }
        if (LocaleUtils.isLocaleValid(languageISOCode)) {
            addMessageParam(languageISOCode,
                    ReasonEnum.LANGUAGE_ISOCODE_NOT_SUPPORTED);
        }
        addMessageParam(languageISOCode, ReasonEnum.INVALID_LANGUAGE_ISOCODE);

    }

    private static void addMessageParam(String localeString, ReasonEnum reason)
            throws ValidationException {
        ValidationException ve = new ValidationException(reason,
                "languageISOCode", new Object[] { localeString });
        ve.setMessageParams(new String[] { localeString });
        throw ve;
    }

    private static void readRows(Sheet sheet,
            Map<String, Properties> propertiesMap,
            List<String> localeStringList, Set<Object> defaultKeySet)
            throws TranslationImportException, ValidationException {
        Row row;
        int len = sheet.getLastRowNum() + 1;
        for (int i = 1; i < len; i++) {
            row = sheet.getRow(i);
            if (row != null) {
                String key = getCellValue(row, 0, true);
                if (key == null) {
                    throw new TranslationImportException(
                            TranslationImportException.Reason.MISSING_KEY);
                } else {
                    for (int colIdx = 0; colIdx < localeStringList.size(); colIdx++) {
                        String localeString = localeStringList.get(colIdx);
                        Properties properties = propertiesMap.get(localeString);
                        if (properties != null) {
                            // fist column contains the key
                            String val = null;
                            try {
                                val = getCellValue(row, colIdx + 1, false);
                            } catch (java.lang.IllegalStateException e) {
                                TranslationImportException ex = new TranslationImportException(
                                        TranslationImportException.Reason.CELL_NOT_TEXT);
                                ex.setMessageParams(new String[] {
                                        sheet.getSheetName(), key, localeString });
                                throw ex;
                            }

                            if (defaultKeySet != null
                                    && !defaultKeySet.contains(key)) {
                                TranslationImportException ex = new TranslationImportException(
                                        TranslationImportException.Reason.KEY_NOT_FOUND);
                                ex.setMessageParams(new String[] { key,
                                        sheet.getSheetName() });
                                throw ex;
                            }

                            if (checkIfDuplicateKeysExist(propertiesMap,
                                    localeString, key)) {
                                JSFUtils.addMessage(
                                        null,
                                        FacesMessage.SEVERITY_WARN,
                                        BaseBean.ERROR_FILE_IMPORT_TRANSLATIONS_MULTIPLEKEY_EXISTING,
                                        null);

                            }
                            if (val != null) {
                                propertiesMap.get(localeString).put(key,
                                        validateDatePattern(key, val));
                            } else {
                                propertiesMap.get(localeString).put(key,
                                        validateDatePattern(key, ""));
                            }

                        }
                    }
                }
            }
        }
    }

    private static boolean checkIfDuplicateKeysExist(
            Map<String, Properties> propertiesMap, String localeString,
            String key) {
        if (propertiesMap.get(localeString) != null
                && propertiesMap.get(localeString).get(key) != null) {
            return true;
        }
        return false;
    }

    /**
     * Check for all in DatePatternEnum defined date patterns if the syntax is
     * correct.
     */
    public static String validateDatePattern(String key, String value)
            throws ValidationException {
        for (DatePatternEnum iter : DatePatternEnum.values()) {
            if (key.equals(iter.getMessageKey())) {
                try {
                    new SimpleDateFormat(value);
                    value = value.trim();
                    break;
                } catch (Exception e) {
                    throw new ValidationException(
                            ValidationException.ReasonEnum.FILE_IMPORT_FAILED,
                            key, new Object[] { value });
                }
            }
        }
        return value;
    }

    /**
     * Read a string from the excel cell in the given row with the given index.
     * 
     * @param row
     *            the row containing the cell to read.
     * @param idx
     *            the index of the cell to read.
     * @param trim
     *            <code>true</code> if the read value should be trimmed before
     *            returning.
     * @return the read string or null if the cell doesn't exist or contains a
     *         blank string.
     * @throws TranslationImportException
     */
    public static String getCellValue(Row row, int idx, boolean trim)
            throws TranslationImportException {
        String val = null;
        Cell cell = row.getCell(idx);
        if (cell != null) {
            val = cell.getStringCellValue();
            if (val != null) {
                if (trim) {
                    val = val.trim();
                }
            }
        }
        return val;
    }

    private static ResourceBundle getDefaultResourceBundle() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                DefaultMessages.class.getName(), FacesContext
                        .getCurrentInstance().getApplication()
                        .getDefaultLocale());
        return resourceBundle;
    }
}
