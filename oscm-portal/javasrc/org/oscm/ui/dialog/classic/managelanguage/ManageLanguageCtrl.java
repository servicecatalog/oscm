/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-11-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.managelanguage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import org.oscm.converter.LocaleHandler;
import org.oscm.converter.PropertiesConverter;
import org.oscm.converter.PropertiesLoader;
import org.oscm.types.enumtypes.LocalizedDataType;
import org.oscm.types.enumtypes.StandardLanguage;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.ExcelHandler;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.LocaleUtils;
import org.oscm.ui.resources.DbMessages;
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

@ManagedBean(name="manageLanguageCtrl")
@ViewScoped
public class ManageLanguageCtrl extends BaseBean implements Serializable {

    private static final long serialVersionUID = -5359024161433637230L;
    static final String MANAGE_LANGUAGE_MODEL = "manageLanguageModel";
    private static final int STANDARD_LANGUAGE_NUM = 3;
    private static final int ALLOWED_IMPORTED_LANGUAGE_NUM = 1;
    
    @ManagedProperty(value="#{manageLanguageModel}")
    private ManageLanguageModel model;
    private List<POLocalizedData> excelDatas;
    private long numOfValues = 0;
    /**
     * store the import/export zip file
     */
    private Part excel;

    public Part getExcel() {
        return excel;
    }

    public void setExcel(Part excel) {
        this.excel = excel;
    }

    public String getInitialize() {
        if (!model.isInitialized()) {
            model.setLanguages(getAllSupportedLanguages());
            model.setInitialized(true);
            model.setLocalizedData(new POLocalizedData());
            model.setDefaultLanguageCode();
        }
        return "";
    }

    public void setModel(ManageLanguageModel model) {
        this.model = model;
    }

    List<POSupportedLanguage> getAllSupportedLanguages() {
        List<POSupportedLanguage> supportedLanguages = new ArrayList<POSupportedLanguage>();
        supportedLanguages.addAll(getManageLanguageService()
                .getLanguages(false));
        return supportedLanguages;
    }

    List<POSupportedLanguage> getActivedLanguages() {
        List<POSupportedLanguage> supportedLanguages = new ArrayList<POSupportedLanguage>();
        supportedLanguages
                .addAll(getManageLanguageService().getLanguages(true));
        return supportedLanguages;
    }

    /**
     * save the languages stored in model
     * 
     * @throws SaaSApplicationException
     * 
     */
    public String save() {

        if (model == null) {
            return null;
        }

        if (!model.isTokenValid()) {
            return OUTCOME_SUCCESS;
        }

        try {
            getManageLanguageService().saveLanguages(model.getLanguages());
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            return OUTCOME_ERROR;
        }
        model.resetToken();
        addMessage(null, FacesMessage.SEVERITY_INFO,
                BaseBean.INFO_SUPPORTEDLANGUAGE_SAVED);
        return OUTCOME_SUCCESS;
    }

    /**
     * add a new supported language for the platform
     * 
     * @throws SaaSApplicationException
     * 
     */
    public String addLanuage() {
        if (!model.isTokenValid()) {
            return OUTCOME_SUCCESS;
        }

        if (model.getNewISOCode() == null || model.getNewISOCode().isEmpty()) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    BaseBean.ERROR_ISOCODE_ISEMPTY);
            ui.resetDirty();
            return OUTCOME_ERROR;
        }

        List<POSupportedLanguage> languages = new ArrayList<POSupportedLanguage>();
        POSupportedLanguage newLanguage = new POSupportedLanguage();
        newLanguage.setLanguageISOCode(model.getNewISOCode());
        languages.add(newLanguage);

        try {
            getManageLanguageService().saveLanguages(languages);
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            ui.resetDirty();
            return OUTCOME_ERROR;
        }
        model.resetToken();
        addMessage(null, FacesMessage.SEVERITY_INFO,
                BaseBean.INFO_SUPPORTEDLANGUAGE_ADDED, model.getNewISOCode());
        return OUTCOME_SUCCESS;
    }

    /**
     * set the select language's DefaultStatus to true
     */
    public String setDefault() {
        if (!model.isTokenValid()) {
            return OUTCOME_SUCCESS;
        }
        model.resetToken();
        return null;
    }

    /**
     * Export the translations into an excel file.
     * 
     * @return the logical outcome.
     * @throws IOException
     *             Thrown in case the access to the uploaded file failed.
     * @throws ObjectNotFoundException
     */
    public String exportTranslations() throws IOException,
            ObjectNotFoundException {
        byte[] buf = createExcel();
        if (buf == null) {
            return OUTCOME_ERROR;
        }
        createResponse(buf);
        return null;
    }

    private void createResponse(byte[] buf) throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) fc
                .getExternalContext().getResponse();

        String filename = generateFileName();

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "attachement; filename=\""
                + filename + "\"");
        response.setContentLength(buf.length);
        OutputStream out;
        out = response.getOutputStream();
        out.write(buf);
        out.flush();
        out.close();
        fc.responseComplete();
    }

    private String generateFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        String filename = sdf.format(Calendar.getInstance().getTime())
                + "_LocalizedLanguage.xls";
        return filename;
    }

    public byte[] createExcel() throws IOException, ObjectNotFoundException {
        String selectedLanguage = model.getSelectedLanguageCode();
        Map<String, Properties> localizedProperties = new HashMap<String, Properties>();
        List<POLocalizedData> poLocalizedDatas = getLocalizedDataService()
                .exportProperties(selectedLanguage);
        Workbook wb = new HSSFWorkbook();
        List<Locale> locales = generateLocaleList();
        for (POLocalizedData data : poLocalizedDatas) {
            localizedProperties = data.getPropertiesMap();
            if (data.getType().equals(LocalizedDataType.MessageProperties)) {
                createSheet(localizedProperties, wb,
                        BaseBean.LABEL_USERINTERFACE_TRANSLARIONS, locales);
            }
            if (data.getType().equals(LocalizedDataType.MailProperties)) {
                createSheet(localizedProperties, wb,
                        BaseBean.LABEL_MAIL_TRANSLARIONS, locales);
            }
            if (data.getType().equals(LocalizedDataType.PlatformObjects)) {
                createSheet(localizedProperties, wb,
                        BaseBean.LABEL_PLATFORM_TRANSLARIONS, locales);
            }
        }

        return createByteArrayForWorkbook(wb);
    }

    private byte[] createByteArrayForWorkbook(Workbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }

    void createSheet(Map<String, Properties> localizedProperties, Workbook wb,
            String sheetName, List<Locale> locales) throws IOException {

        Map<String, ResourceBundle> defaultMessageProperties = loadMessagePropertiesFromFile(
                locales, sheetName);

        List<String> keyList = generateKeyList(localizedProperties,
                defaultMessageProperties, sheetName);

        wb = ExcelHandler.createExcel(keyList, defaultMessageProperties,
                localizedProperties, wb, sheetName, locales);
    }

    private List<Locale> generateLocaleList() {
        String selectedLanguage = model.getSelectedLanguageCode();
        List<Locale> locales = loadStandardLocales();
        if (selectedLanguage != null) {
            locales.add(new Locale(selectedLanguage));
        }
        return locales;
    }

    private List<String> generateKeyList(
            Map<String, Properties> localizedProperties,
            Map<String, ResourceBundle> defaultMessageProperties,
            String sheetName) {
        String defaultLocale = model.getDefaultLanguageCode();
        Set<String> keySet = getKeysFromFile(defaultMessageProperties,
                localizedProperties, defaultLocale, sheetName);
        List<String> keyList = new ArrayList<String>();
        keyList.addAll(keySet);
        Collections.sort(keyList);
        return keyList;
    }

    private Set<String> loadKeySetFromProperties(
            Map<String, Properties> localizedProperties, String defaultLocale) {
        Set<String> keySet = new HashSet<String>();
        Properties properties = localizedProperties.get(defaultLocale);
        if (properties != null) {
            List<Object> propertyKeys = Collections.list(properties.keys());
            for (Object key : propertyKeys) {
                if (key instanceof String) {
                    keySet.add((String) key);
                }
            }
        }
        return keySet;
    }

    private Set<String> getKeysFromFile(
            Map<String, ResourceBundle> defaultMessageProperties,
            Map<String, Properties> localizedProperties, String locale,
            String sheetName) {
        if (BaseBean.LABEL_USERINTERFACE_TRANSLARIONS.equals(sheetName)) {
            ResourceBundle bundle = defaultMessageProperties.get(locale);
            if (bundle != null) {
                return bundle.keySet();
            }
        } else {
            return loadKeySetFromProperties(localizedProperties, locale
                    + StandardLanguage.COLUMN_HEADING_SUFFIX);
        }
        return new HashSet<String>();
    }

    private Map<String, ResourceBundle> loadMessagePropertiesFromFile(
            List<Locale> locales, String sheetName) throws IOException {
        Map<String, ResourceBundle> defaultProperties = new HashMap<String, ResourceBundle>();
        if (sheetName.equals(BaseBean.LABEL_USERINTERFACE_TRANSLARIONS)) {
            for (Locale locale : locales) {
                if (LocaleHandler.isStandardLanguage(locale)) {
                    defaultProperties.put(locale.getLanguage(),
                            loadMessagePropertiesFromFile(locale, sheetName));
                }
            }
        }
        return defaultProperties;
    }

    ResourceBundle loadMessagePropertiesFromFile(Locale locale, String sheetName)
            throws IOException {
        if (sheetName.equals(BaseBean.LABEL_USERINTERFACE_TRANSLARIONS)) {
            if (LocaleHandler.isStandardLanguage(locale)) {
                return PropertiesLoader.loadToBundle(
                        DbMessages.class,
                        DbMessages.class.getPackage().getName()
                                .replaceAll("\\.", "/")
                                + "/Messages_"
                                + locale.toString()
                                + ".properties");
            }
        }
        return null;
    }

    private List<Locale> loadStandardLocales() {
        List<Locale> locales = new ArrayList<Locale>();
        locales.addAll(StandardLanguage.getStandardLocales());
        return locales;
    }

    private boolean checkIfDuplicateKeysExist() {
        if (JSFUtils
                .replaceMessageInListIfExisting(
                        ERROR_FILE_IMPORT_TRANSLATIONS_MULTIPLEKEY_EXISTING,
                        null,
                        ERROR_FILE_IMPORT_TRANSLATIONS_MULTIPLEKEY_EXISTING,
                        new Object[] { String.valueOf(numOfValues),
                                String.valueOf(1) })) {
            ui.resetDirty();
            return true;
        }
        return false;
    }

    public String importTranslations() {

        if (excel == null || excel.getName() == null) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_SHOP_TRANSLATIONS_FILEFORMAT);
            ui.resetDirty();
            return OUTCOME_ERROR;
        }

        try {

            // The stream is always closed according to POI documentation
            InputStream excelStream = excel.getInputStream();
            excelDatas = new ArrayList<POLocalizedData>();
            List<Locale> locales = new ArrayList<Locale>();
            List<String> supportedLocales = LocaleUtils.getSupportedLocales();
            for (String language : supportedLocales) {
                locales.add(new Locale(language));
            }
            Workbook wb = new HSSFWorkbook(excelStream);
            String interfaceLanguageCode = readExcel(wb,
                    BaseBean.LABEL_USERINTERFACE_TRANSLARIONS, excelDatas,
                    locales, LocalizedDataType.MessageProperties);
            String mailLanguage = readExcel(wb,
                    BaseBean.LABEL_MAIL_TRANSLARIONS, excelDatas, locales,
                    LocalizedDataType.MailProperties);
            String platformObjectLanguage = readExcel(wb,
                    BaseBean.LABEL_PLATFORM_TRANSLARIONS, excelDatas, locales,
                    LocalizedDataType.PlatformObjects);
            if (!interfaceLanguageCode.equals(mailLanguage)
                    || !interfaceLanguageCode.equals(platformObjectLanguage)) {
                addMessage(null, FacesMessage.SEVERITY_ERROR,
                        ERROR_TRANSLATIONS_ONELANGUAGE);
                return OUTCOME_ERROR;
            }

            final String missingTranslations = getLocalizedDataService()
                    .importProperties(excelDatas, interfaceLanguageCode);

            model.setLocalizedData(null);
            resetBundles();

            if (checkIfDuplicateKeysExist())
                return OUTCOME_ERROR;

            if (missingTranslations != null) {
                addMissingTranslationWarning(missingTranslations);
                return OUTCOME_ERROR;
            }

            addSuccessMessage();

        } catch (TranslationImportException | PropertiesImportException ex) {
            handleException(ex);
            ui.resetDirty();
            return OUTCOME_ERROR;
        } catch (ValidationException ex) {
            if (ReasonEnum.INVALID_LANGUAGE_ISOCODE == ex.getReason()) {
                addMessage(null, FacesMessage.SEVERITY_ERROR,
                        BaseBean.ERROR_FILE_IMPORT_FAILED_ISOCODE_INVALID,
                        ex.getMessageParams());
            } else if (ReasonEnum.LANGUAGE_ISOCODE_NOT_SUPPORTED == ex
                    .getReason()) {
                addMessage(
                        null,
                        FacesMessage.SEVERITY_ERROR,
                        BaseBean.ERROR_FILE_IMPORT_FAILED_ISOCODE_NOT_SUPPORTED,
                        ex.getMessageParams());
            } else {
                addMessage(null, FacesMessage.SEVERITY_ERROR,
                        ERROR_FILE_IMPORT_FAILED,
                        new Object[] { ex.getLocalizedMessage() });
            }
            ui.resetDirty();
            return OUTCOME_ERROR;
        } catch (Exception e) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_TRANSLATIONS_FILEFORMAT);
            ui.resetDirty();
            return OUTCOME_ERROR;
        }

        return OUTCOME_SUCCESS;
    }

    /**
     * 
     */
    private void addSuccessMessage() {
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_TRANSLATIONS_SAVED,
                new Object[] { String.valueOf(numOfValues), String.valueOf(1) });
    }

    /**
     * @param outcome
     */
    void addMissingTranslationWarning(final String outcome) {
        if (!ui.hasWarnings() && !ui.hasErrors()) {
            addMessage(null, FacesMessage.SEVERITY_WARN, outcome, new Object[] {
                    String.valueOf(numOfValues), String.valueOf(1) });
        }
        ui.resetDirty();
    }

    protected void handleException(SaaSApplicationException e) {
        ExceptionHandler.execute(e);
    }

    String readExcel(Workbook wb, String sheetName,
            List<POLocalizedData> excelDatas, List<Locale> locales,
            LocalizedDataType type) throws ValidationException,
            TranslationImportException, PropertiesImportException, IOException {
        String importedLanguageCode = null;

        Map<String, Properties> propertiesMap = ExcelHandler.readExcel(wb,
                locales.iterator(), sheetName, getDefaultKeySet(sheetName));

        validateLanguageColumns(propertiesMap);
        importedLanguageCode = getImportedLanguageCode(importedLanguageCode,
                propertiesMap);

        Properties importProps = null;
        if (type == LocalizedDataType.PlatformObjects) {
            importProps = propertiesMap.get(importedLanguageCode);
            numOfValues += PropertiesConverter.countNonEmptyValue(importProps);
        } else {
            importProps = PropertiesConverter.removeEmptyValue(propertiesMap
                    .get(importedLanguageCode));
            if (null != importProps) {
                numOfValues += importProps.size();
            }
        }

        createExcelDatas(excelDatas, type, propertiesMap);
        return importedLanguageCode;
    }

    private void validateLanguageColumns(Map<String, Properties> propertiesMap)
            throws PropertiesImportException, TranslationImportException {
        for (StandardLanguage standardLanguage : StandardLanguage.values()) {
            if (!propertiesMap.containsKey(standardLanguage
                    + StandardLanguage.COLUMN_HEADING_SUFFIX)) {
                throw new TranslationImportException(
                        TranslationImportException.Reason.MISSING_STANDARD_LANGUAGE);
            }
        }
        int keysetSize = propertiesMap.keySet().size();
        if (keysetSize == STANDARD_LANGUAGE_NUM) {
            throw new PropertiesImportException(
                    PropertiesImportException.Reason.NONE_LANGUAGE_CODE);
        }
        if (keysetSize != STANDARD_LANGUAGE_NUM
                + ALLOWED_IMPORTED_LANGUAGE_NUM) {
            throw new TranslationImportException(
                    TranslationImportException.Reason.MULTI_LANGUAGE_CODE_NOT_SUPPORTE);
        }
    }

    Set<Object> getDefaultKeySet(String sheetName) throws IOException {
        Set<Object> defaultKeySet = new HashSet<Object>();
        if (BaseBean.LABEL_USERINTERFACE_TRANSLARIONS
                .equalsIgnoreCase(sheetName)) {
            ResourceBundle bundle = loadMessagePropertiesFromFile(
                    Locale.ENGLISH, sheetName);
            defaultKeySet.addAll(bundle.keySet());
        } else if (BaseBean.LABEL_MAIL_TRANSLARIONS.equalsIgnoreCase(sheetName)) {
            Properties defaultProperties = getLocalizedDataService()
                    .loadMailPropertiesFromFile(Locale.ENGLISH.getLanguage());
            defaultKeySet = defaultProperties == null ? new HashSet<Object>()
                    : defaultProperties.keySet();
        } else if (BaseBean.LABEL_PLATFORM_TRANSLARIONS
                .equalsIgnoreCase(sheetName)) {
            Properties defaultProperties = getLocalizedDataService()
                    .loadPlatformObjectsFromFile(Locale.ENGLISH.getLanguage());
            defaultKeySet = defaultProperties == null ? new HashSet<Object>()
                    : defaultProperties.keySet();
        }
        return defaultKeySet;
    }

    private String getImportedLanguageCode(String importedLanguageCode,
            Map<String, Properties> propertiesMap) {
        for (String language : propertiesMap.keySet()) {
            if (!StandardLanguage.isStandardLanguage(language,
                    StandardLanguage.COLUMN_HEADING_SUFFIX)) {
                importedLanguageCode = language;
            }
        }
        return importedLanguageCode;
    }

    private void createExcelDatas(List<POLocalizedData> excelDatas,
            LocalizedDataType type, Map<String, Properties> propertiesMap) {
        POLocalizedData poLocalizedData = new POLocalizedData();
        poLocalizedData.setPropertiesMap(propertiesMap);
        poLocalizedData.setType(type);
        excelDatas.add(poLocalizedData);
    }

}
