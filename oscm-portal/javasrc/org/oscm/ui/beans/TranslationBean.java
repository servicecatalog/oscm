/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: pock, Enes Sejfi                                                      
 *                                                                              
 *  Creation Date: 12.11.2009                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.ExcelHandler;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.resources.DefaultMessages;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.intf.BrandService;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * Backing bean for translation related actions
 * 
 */
@ViewScoped
@ManagedBean(name="translationBean")
public class TranslationBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -2003114285293661531L;

    private static final String DEFAULT_LOCALE = "en";

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(TranslationBean.class);

    private static final String KEY_TERMS = "public.terms.description";
    private static final String KEY_TERMS_ERROR_MSG = "public.terms.error.msg";
    private static final String KEY_PRIVACY_POLICY = "public.privacypolicy.description";
    private static final String KEY_PRIVACY_POLICY_ERROR_MSG = "public.privacypolicy.error.msg";
    private static final String KEY_IMPRINT = "public.imprint.url";
    private static final String KEY_IMPRINT_DESCRIPTION = "public.imprint.url.description";
    private BrandService brandManagement;

    private Part excel;

    private String locale;

    /**
     * Holds the locale specific properties for the selected marketplace. In
     * case the marketplace or the locale changes, it needs to be refreshed.
     */
    private Properties messageProperties = null;

    /**
     * Holds the default messages bundle for the selected locale. It's values
     * will be used in case the value is not part of the messagerProperties.
     */
    private ResourceBundle defaultMessageBundle = null;

    private String privacypolicy;
    private String terms;
    private String imprint;
    private String imprintDescription;

    private String stageContent;
    private boolean showConfirm;
    protected List<VOLocalizedText> stages = null;

    @ManagedProperty(value = "#{appBean}")
    private ApplicationBean appBean;
    @ManagedProperty(value="#{marketplaceBean}")
    private MarketplaceBean marketplaceBean;

    /**
     * Indicates if the content of the stage field was changed
     */
    private boolean dirtyStage = false;

    /**
     * Indicates if the default was used for the imprint (i.e. no imprint
     * defined for given locale)
     */
    private boolean imprintDefaultLocaleUsed = false;

    /**
     * Gets the marketplace bean.
     * 
     * @return the marketplace bean
     */
    public MarketplaceBean getMarketplaceBean() {
        return marketplaceBean;
    }

    /**
     * Sets the marketplace bean.
     * 
     * @param marketplaceBean
     *            the new marketplace bean
     */
    public void setMarketplaceBean(MarketplaceBean marketplaceBean) {
        this.marketplaceBean = marketplaceBean;
    }

    /**
     * Get the brand management service
     * 
     * @return the accounting management service
     */
    protected BrandService getBrandManagementService() {
        brandManagement = getService(BrandService.class, brandManagement);
        return brandManagement;
    }

    ApplicationBean getApplicationBean() {
        return appBean;
    }

    public Part getExcel() {
        return excel;
    }

    public void setExcel(Part excel) {
        this.excel = excel;
    }

    public String getLocale() {
        if (locale == null) {
            locale = getUserFromSession().getLocale();
        }
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
        resetMembers();
        setShowConfirm(false);
        setDirtyStage(false);
    }

    public String getPrivacypolicy() {
        if (privacypolicy == null
                && getMarketplaceBean().getMarketplaceId() != null) {
            privacypolicy = getProperty(KEY_PRIVACY_POLICY, getLocale());
        }
        return privacypolicy;
    }

    public void setPrivacypolicy(String privacypolicy) {
        this.privacypolicy = privacypolicy;
    }

    public String getTerms() {
        if (terms == null && getMarketplaceBean().getMarketplaceId() != null) {
            terms = getProperty(KEY_TERMS, getLocale());
        }
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    /**
     * Retrieves the imprint in the locale of the currently logged-in user for
     * the marketplace the user is logged in (or in the default locale if not
     * defined for the user-locale). If no imprint is defined neither in the
     * current user locale nor in the default locale, the empty string is
     * returned.
     * <p>
     * This function is used directly by the JSF pages.
     */
    public String getImprintSessionLocale() {
        String imprintInclFallback = "";
        ResourceBundle bundle = getResourceBundle(new Locale(getUserLanguage()));
        if (bundle != null) {
            imprintInclFallback = bundle.getString(KEY_IMPRINT);
            // If the imprint URL is not set, try to read the imprint
            // defined for the default locale
            if (imprintInclFallback.trim().length() == 0
                    && !DEFAULT_LOCALE.equals(getUserLanguage())) {
                ResourceBundle bundleDefaultLocale = getResourceBundle(new Locale(
                        DEFAULT_LOCALE));
                imprintInclFallback = bundleDefaultLocale
                        .getString(KEY_IMPRINT);
            }
        }
        return imprintInclFallback;
    }

    /**
     * Retrieves the imprint in the locale of the currently logged-in user (or
     * in the default locale if not defined for the user-locale). If no imprint
     * is defined neither in the current user locale nor in the default locale,
     * a descriptive text in the current user locale is returned.
     */
    public String getImprint() {
        String displayValue = null;
        if (getMarketplaceBean().getMarketplaceId() != null) {
            if (imprint == null) {
                imprint = getImprintForLocale(getLocale());
            }
            displayValue = imprint;
            if (displayValue != null && displayValue.trim().length() == 0
                    || imprintDefaultLocaleUsed) {
                displayValue = getProperty(KEY_IMPRINT_DESCRIPTION, getLocale());
            }
        }
        return displayValue;
    }

    /**
     * Retrieves the imprint in the given locale (or in the default locale if
     * not defined for the user-locale). If no imprint is defined neither in the
     * current user locale nor in the default locale, the empty string is
     * returned.
     * 
     * @param loc
     *            the locale for the imprint
     */
    private String getImprintForLocale(String loc) {
        String imprintInclFallback = getProperty(KEY_IMPRINT, loc);
        imprintDefaultLocaleUsed = false;
        // If the imprint URL is not set, try to read the imprint
        // defined for the default locale
        if (imprintInclFallback == null
                || imprintInclFallback.trim().length() == 0
                && !DEFAULT_LOCALE.equals(loc)) {
            imprintInclFallback = getProperty(KEY_IMPRINT, DEFAULT_LOCALE);
            imprintDefaultLocaleUsed = imprintInclFallback != null
                    && imprintInclFallback.trim().length() > 0;
        }
        return imprintInclFallback;
    }

    public void setImprint(String imprint) {
        this.imprint = imprint;
    }

    /**
     * Resolves the resource bundle for the current locale and returns the
     * description of the imprint field.
     * 
     * @return the description of the imprint field.
     */
    private String getImprintDescription() {
        if (imprintDescription == null) {
            imprintDescription = getProperty(KEY_IMPRINT_DESCRIPTION,
                    getLocale());
        }
        return imprintDescription;
    }

    public byte[] createExcel() throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();

        // Load default properties from bundle
        Map<String, ResourceBundle> defaultProperties = new HashMap<>();
        Iterator<Locale> it = getApplicationBean()
                .getSupportedLocalesIterator();
        while (it.hasNext()) {
            Locale locale = it.next();
            defaultProperties.put(locale.getLanguage(), ResourceBundle
                    .getBundle(DefaultMessages.class.getName(), locale));
        }

        // load localized properties from server
        List<Locale> locales = new ArrayList<>();
        Map<String, Properties> localizedProperties = new HashMap<>();
        it = getApplicationBean().getSupportedLocalesIterator();
        while (it.hasNext()) {
            Locale locale = it.next();
            locales.add(locale);
            localizedProperties.put(
                    locale.getLanguage(),
                    getBrandManagementService().loadMessageProperties(
                            getMarketplaceBean().getMarketplaceId(),
                            locale.getLanguage()));
        }

        // Collect all message keys(= server side properties keys +
        // local bundles keys)
        ResourceBundle defaultBundle = ResourceBundle.getBundle(
                DefaultMessages.class.getName(), fc.getApplication()
                        .getDefaultLocale());
        Set<String> keySet = new HashSet<>();
        keySet.addAll(Collections.list(defaultBundle.getKeys()));
        Properties properties = getMessageProperties(DEFAULT_LOCALE);
        List<Object> propertyKeys = Collections.list(properties.keys());
        for (Object key : propertyKeys) {
            if (key instanceof String) {
                keySet.add((String) key);
            }
        }
        List<String> keyList = new ArrayList<>();
        keyList.addAll(keySet);
        Collections.sort(keyList);

        Workbook wb = ExcelHandler.createExcel(keyList, defaultProperties,
                localizedProperties, null, BaseBean.LABEL_SHOP_TRANSLARIONS,
                locales);
        // create a byte array of the workbook
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }

    /**
     * Delete all customized translations for the current supplier.
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     */
    public String deleteTranslations() throws SaaSApplicationException {

        try {
            getBrandManagementService().deleteAllMessageProperties(
                    getMarketplaceBean().getMarketplaceId());
        } catch (SaaSApplicationException e) {
            marketplaceBean.checkMarketplaceDropdownAndMenuVisibility(e);
            throw e;
        }
        resetBundles();
        resetInputs();

        addMessage(null, FacesMessage.SEVERITY_INFO,
                INFO_SHOP_TRANSLATIONS_DELETED);
        return null;
    }

    /**
     * Export the translations for the current supplier into an excel file.
     * 
     * @return the logical outcome.
     * @throws IOException
     *             Thrown in case the access to the uploaded file failed.
     */
    public String exportTranslations() throws IOException {

        byte[] buf = createExcel();
        if (buf == null) {
            return OUTCOME_ERROR;
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) fc
                .getExternalContext().getResponse();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        String filename = sdf.format(Calendar.getInstance().getTime())
                + "_Translations.xls";

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

        return null;
    }

    /**
     * Read the translations from the uploaded excel file and store them in the
     * database for the current supplier. Translations for unknown locales will
     * be ignored.
     * 
     * @return the logical outcome.
     */
    public String importTranslations() {

        if (excel == null || excel.getName() == null) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_SHOP_TRANSLATIONS_FILEFORMAT);
            return OUTCOME_ERROR;
        }

        try {
            // The stream is always closed according to POI documentation
            InputStream excelStream = excel.getInputStream();
            Workbook wb = new HSSFWorkbook(excelStream);
            Map<String, Properties> propertiesMap = ExcelHandler.readExcel(wb,
                    getApplicationBean().getSupportedLocalesIterator(),
                    BaseBean.LABEL_SHOP_TRANSLARIONS);

            try {
                getBrandManagementService().saveMessageProperties(
                        propertiesMap, getMarketplaceBean().getMarketplaceId());
            } catch (SaaSApplicationException e) {
                marketplaceBean.checkMarketplaceDropdownAndMenuVisibility(e);
                throw e;
            }
            resetBundles();

            long numOfValues = 0;
            for (Properties properties : propertiesMap.values()) {
                numOfValues += properties.size();
            }

            if (JSFUtils.replaceMessageInListIfExisting(
                    ERROR_FILE_IMPORT_TRANSLATIONS_MULTIPLEKEY_EXISTING,
                    null,
                    ERROR_FILE_IMPORT_TRANSLATIONS_MULTIPLEKEY_EXISTING,
                    new Object[] { String.valueOf(numOfValues),
                            String.valueOf(1) })) {
                ui.resetDirty();
                return OUTCOME_ERROR;
            }

            addMessage(
                    null,
                    FacesMessage.SEVERITY_INFO,
                    INFO_TRANSLATIONS_SAVED,
                    new Object[] { String.valueOf(numOfValues),
                            String.valueOf(propertiesMap.size()) });
        } catch (ValidationException ex) {
            logger.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_PROCESS_TRANSLATION_FILE);
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_FILE_IMPORT_FAILED,
                    new Object[] { ex.getLocalizedMessage() });
            return OUTCOME_ERROR;
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_PROCESS_TRANSLATION_FILE);
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_SHOP_TRANSLATIONS_FILEFORMAT);
            return OUTCOME_ERROR;
        }

        return OUTCOME_SUCCESS;
    }

    /**
     * Check the text length.
     */
    private void validateTextLength(String key, String text)
            throws ValidationException {
        if (text.length() > ADMValidator.LENGTH_TEXT) {
            String title = JSFUtils.getText(key, null);

            throw new ValidationException(ReasonEnum.LENGTH_TEXT, key,
                    new Object[] { title, Integer.toString(text.length()),
                            Integer.toString(ADMValidator.LENGTH_TEXT) });
        }
    }

    /**
     * Save the possibly changed terms, privacy policy and imprint.
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     */
    public String save() throws SaaSApplicationException {

        Properties properties = getBrandManagementService()
                .loadMessageProperties(getMarketplaceBean().getMarketplaceId(),
                        locale);

        if (privacypolicy != null) {
            validateTextLength(KEY_PRIVACY_POLICY_ERROR_MSG, privacypolicy);
        } else {
        	privacypolicy = "";
        }
        properties.put(KEY_PRIVACY_POLICY, privacypolicy);

        if (terms != null) {
            validateTextLength(KEY_TERMS_ERROR_MSG, terms);
        } else {
        	terms = "";
        }
        properties.put(KEY_TERMS, terms);

        if ((imprint == null) || (imprint.equals(getImprintDescription()))) {
            properties.put(KEY_IMPRINT, "");
        } else {
            properties.put(KEY_IMPRINT, imprint);
        }

        Map<String, Properties> propertiesMap;
        propertiesMap = new HashMap<>();
        propertiesMap.put(getLocale(), properties);
        try {
            getBrandManagementService().saveMessageProperties(propertiesMap,
                    getMarketplaceBean().getMarketplaceId());
        } catch (SaaSApplicationException e) {
            marketplaceBean.checkMarketplaceDropdownAndMenuVisibility(e);
            throw e;
        }

        resetBundles();

        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_TRANSLATIONS_SAVED,
                new Object[] { "3", "1" });
        return null;
    }

    /**
     * Clear the terms and the privacy policy so that they will be read again
     * (with the new locale).
     * 
     * @return the logical outcome.
     */
    public void changeLocale() {
        resetInputs();
    }

    /**
     * Reset cached values and all input elements in order that they will be
     * read again.
     */
    private void resetInputs() {
        resetMembers();
        resetUIInputChildren();
        setShowConfirm(false);
        setDirtyStage(false);
    }

    public String getStage() {
        if (stages == null) {
            initStage();
            stageContent = getStageForLocale(locale, stages);
        } else {
            if (stageContent == null) {
                stageContent = getStageForLocale(locale, stages);
            }
        }
        return stageContent;
    }

    /**
     * Returns the stage content for the specified locale if existing from the
     * provided list of localized texts. If no matching locale was found,
     * <code>null</code> is returned.
     * 
     * @param locale
     *            the locale to get the stage for
     * @param stages
     *            the {@link VOLocalizedText}s to get the stage from
     * @return the stage or <code>null</code>
     */
    private static String getStageForLocale(String locale,
            List<VOLocalizedText> stages) {
        if (stages != null) {
            for (VOLocalizedText text : stages) {
                if (text.getLocale().equals(locale)) {
                    return text.getText();
                }
            }
        }
        return null;
    }

    /**
     * Initializes the marketplace id and the localized stages if not already
     * done.
     */
    private void initStage() {
        if (getMarketplaceBean().getMarketplaceId() != null) {
            try {
                stages = getBrandManagementService()
                        .getMarketplaceStageLocalization(
                                getMarketplaceBean().getMarketplaceId());
            } catch (SaaSApplicationException e) {
                marketplaceBean.checkMarketplaceDropdownAndMenuVisibility(e);
                ExceptionHandler.execute(e);
            }
        }
    }

    public void setDirtyStage(boolean dirty) {
        this.dirtyStage = dirty;
    }

    public boolean isDirtyStage() {
        return dirtyStage;
    }

    public void setStage(String stage) {
        stageContent = stage;
    }

    /**
     * Action to enable the preview panel.
     * 
     * @return the logical outcome
     */
    public String preview() {
        setShowConfirm(true);
        return null;
    }

    /**
     * Action to enable the preview panel.
     * 
     * @return the logical outcome
     */
    public String cancelPreview() {
        setShowConfirm(false);
        return null;
    }

    /**
     * Saves the stage for the selected locale.
     * 
     * @return the logical outcome
     * @throws SaaSApplicationException
     */
    public String saveStage() throws SaaSApplicationException {
        try {
            getBrandManagementService().setMarketplaceStage(stageContent,
                    getMarketplaceBean().getMarketplaceId(), locale);
        } catch (SaaSApplicationException e) {
            marketplaceBean.checkMarketplaceDropdownAndMenuVisibility(e);
            throw e;
        } finally {
            setDirtyStage(false);
            stages = null;
            setShowConfirm(false);
        }
        addMessage(null, FacesMessage.SEVERITY_INFO,
                INFO_MARKETPLACE_STAGE_SAVED);
        return null;
    }

    public void setShowConfirm(boolean showConfirm) {
        this.showConfirm = showConfirm;
    }

    public boolean isShowConfirm() {
        return showConfirm;
    }

    /**
     * Returns the stage for the preview - handling is done lime for the real
     * stage - if the current locale's stage is empty, try to get the english
     * one. If this is also empty, return the default image.
     * 
     * @return the stage for the preview
     */
    public String getStagePreview() {
        String stage = stageContent;
        if (isBlank(stage)) {
            if (!DEFAULT_LOCALE.equals(locale)) {
                // get the stage in the default locale if the current locale is
                // a different one
                stage = getStageForLocale(DEFAULT_LOCALE, stages);
            }
            if (isBlank(stage)) {
                // if it is still empty, use the default image
                stage = MessageFormat.format(
                        SkinBean.MARKETPLACE_STAGE_DEFAULT,
                        getRequestContextPath());
            }
        }
        return stage;
    }

    String getRequestContextPath() {
        return getFacesContext().getExternalContext().getRequestContextPath();
    }

    /**
     * This methods is used as a callback for value-changed-event of the select
     * marketplace facelet.
     */
    public void processValueChange(ValueChangeEvent event)
            throws AbortProcessingException {
    	
    	String selectedMarketplaceId = (String) event.getNewValue();
    	
    	if (selectedMarketplaceId.equals("0")) {
    		marketplaceBean.setMarketplaceId(null);
    	} else{
    		marketplaceBean.setMarketplaceId(selectedMarketplaceId);
    	}
    	
        resetMembers();
        resetStage();
        this.marketplaceBean.processValueChange(event);
    }

    /**
     * Resets the stage.
     */
    private void resetStage() {
        stages = null;
    }

    /**
     * Resets all members.
     */
    private void resetMembers() {
        privacypolicy = null;
        terms = null;
        imprint = null;
        imprintDescription = null;
        stageContent = null;
        messageProperties = null;
        defaultMessageBundle = null;

    }

    /**
     * Returns the value for the passed key in the specified locale. If the
     * value exists in the properties set on the server it'll be used. in case
     * the property is not defined, the default value will be loaded from the
     * corresponding resourcebundle.
     */
    String getProperty(String key, String locale) {
        String property = null;
        Properties dbProperties = getMessageProperties(locale);
        if (messageProperties != null) {
            property = dbProperties.getProperty(key);
        }
        if (property == null) {
            ResourceBundle bundle = getDefaultMessageBundle(locale);
            try {
                property = bundle.getString(key);
            } catch (MissingResourceException e) {
                // Ignore; "property" will be returned as null
            }
        }
        return property;
    }

    /**
     * Get the message properties for a given locale.
     */
    private Properties getMessageProperties(String locale) {
        String mid = getMarketplaceBean().getMarketplaceId();
        if (messageProperties == null && mid != null) {
            messageProperties = getMessagePropertiesFromDb(mid, locale);
        }
        return messageProperties;
    }

    private Properties getMessagePropertiesFromDb(String mid, String locale) {
        Properties props = new Properties();
        Properties propsFromLocalizedDataService = getLocalizedDataService()
                .loadMessageProperties(locale);
        if (propsFromLocalizedDataService != null) {
            props.putAll(propsFromLocalizedDataService);
        }
        Properties propsFromBrandMgmtService = getBrandManagementService()
                .loadMessageProperties(mid, locale);
        if (propsFromBrandMgmtService != null) {
            props.putAll(propsFromBrandMgmtService);
        }
        return props;
    }

    /**
     * Returns the default message bundle or the given locale.
     */
    private ResourceBundle getDefaultMessageBundle(String locale) {
        if (defaultMessageBundle == null) {
            defaultMessageBundle = ResourceBundle.getBundle(
                    DefaultMessages.class.getName(), new Locale(locale), Thread
                            .currentThread().getContextClassLoader());
        }
        return defaultMessageBundle;
    }

    /**
     * Check if the given value contains a default text.
     * 
     * @param value
     *            the value of the text input field
     * @return "true" if the value contains a default text else "false"
     */
    private boolean checkIfDefaultText(String value) {
        boolean defaultText = false;
        List<String> defaultTexts = new ArrayList<>();
        String defaultTextLang1 = getProperty(KEY_IMPRINT_DESCRIPTION,
                getLocale());
        defaultTexts.add(defaultTextLang1);
        String defaultTextLang2 = JSFUtils.getText(KEY_IMPRINT_DESCRIPTION,
                null);
        defaultTexts.add(defaultTextLang2);
        if (defaultTexts.contains(value)) {
            defaultText = true;
        }
        return defaultText;
    }

    private ValidatorException constructValidatorException(
            final FacesContext context, final UIComponent component) {
        // create ValidationException
        String label = JSFUtils.getLabel(component);
        ValidationException ex = new ValidationException(
                ValidationException.ReasonEnum.URL, label, null);

        // map to ValidatorException
        Object[] args = null;
        if (label != null) {
            args = new Object[] { label };
        }
        String text = JSFUtils.getText(ex.getMessageKey(), args, context);
        return new ValidatorException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR, text, null));
    }

    public void validateImprint(final FacesContext context,
            final UIComponent component, final Object valueObj) {

        // skip if no value
        if (valueObj == null) {
            return;
        }
        String value = valueObj.toString();
        if (value.length() == 0) {
            return;
        }

        // the imprint value must contain a valid URL or a default text
        if (ADMValidator.isUrl(value)) {
            return;
        }
        if (checkIfDefaultText(value)) {
            return;
        }

        // validation failed
        throw constructValidatorException(context, component);
    }

    public ApplicationBean getAppBean() {
        return appBean;
    }

    public void setAppBean(ApplicationBean appBean) {
        this.appBean = appBean;
    }
}
