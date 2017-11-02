/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.DateConverter;
import org.oscm.converter.PropertiesLoader;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.LocaleUtils;
import org.oscm.ui.common.ServiceAccess;
import org.oscm.ui.common.UiDelegate;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.operatorservice.ManageLanguageService;
import org.oscm.internal.operatorservice.POSupportedLanguage;
import org.oscm.internal.subscriptions.POSubscriptionAndCustomer;
import org.oscm.internal.types.enumtypes.AuthenticationMode;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Managed bean which provides some field settings to the view elements
 * 
 */
@ManagedBean(name="appBean")
@SessionScoped
public class ApplicationBean implements Serializable {

    public static final String FCIP_BRANDING_PACKAGE = "fcip-branding";

    public static final String FCIP_CONTEXT_PATH_ROOT = "/"
            + FCIP_BRANDING_PACKAGE;

    public static final String APPLICATIONS_ROOT_FOLDER = "applications";

    UiDelegate ui = new UiDelegate();

    private static final long serialVersionUID = -4465290515626464652L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ApplicationBean.class);

    private String buildId = null;
    private String buildDate = null;
    private String timeZoneId = null;
    private String oldUserLocale = "";
    private boolean errorPanelForLocaleShow = false;
    private static final String SIMPLE_DATE_PATTERN = "yyyy-MM-dd z";
    private String requestContextPath = "";

    /** Configuration service instance. As member used for JUint for stubbing. */
    private transient ConfigurationService configurationService = null;

    private transient ManageLanguageService manageLanguageService = null;

    transient IdentityService identityService = null;

    /**
     * List of menus and groups of fields in dialogs, which are hidden.
     */
    private Map<String, Boolean> hiddenUIElements = null;

    /**
     * Cached boolean flag if reporting is available or not
     */
    private Boolean reportingAvailable = null;

    /**
     * Cached boolean flag if auth mode is internal
     */
    private Boolean internalAuthMode = null;

    /**
     * The server base URL to be used for building the service access URL.
     */
    private String serverBaseUrl = null;

    /**
     * The https server base URL to be used for building the service access URL.
     */
    private String serverBaseUrlHttps = null;

    /**
     * The interval in milliseconds between the previous response and the next
     * request of <a4j:poll> component.
     */
    private Long interval = null;

    private Boolean samlSpAuthMode = null;

    /**
     * Read the build id and date from the ear manifest.
     */
    private void initBuildIdAndDate() {
        if (buildId != null) {
            return;
        }
        buildId = "-1";
        buildDate = "";

        // read the implementation version property from the war manifest
        final InputStream in = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResourceAsStream("/META-INF/MANIFEST.MF");
        String str = null;
        if (in != null) {
            final Properties prop = PropertiesLoader.loadProperties(in);
            str = prop.getProperty("Implementation-Version");
        }

        if (str == null) {
            return;
        }

        // parse the implementation version
        final int sep = str.lastIndexOf("-");
        buildId = str.substring(0, sep);

        SimpleDateFormat inFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            buildDate = outFormat
                    .format(inFormat.parse(str.substring(sep + 1)));
        } catch (ParseException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_FORMATTING_BUILD_DATE);
        }

    }

    public void setSamlSpAuthMode(Boolean samlSpAuthMode) {
        this.samlSpAuthMode = samlSpAuthMode;
    }

    /**
     * Enum for date patterns in the resource bundle message files
     */
    public static enum DatePatternEnum {
        DATE_PATTERN("datePattern"), DATE_INPUT_PATTERN("dateInputPattern"), DATE_TIME_PATTERN(
                "dateTimePattern");

        private final String messageKey;

        private DatePatternEnum(String messageKey) {
            this.messageKey = messageKey;
        }

        /**
         * This is the key used for the resource bundle message files.
         */
        public String getMessageKey() {
            return messageKey;
        }
    }

    public String getDatePattern() {
        return DatePatternEnum.DATE_PATTERN.getMessageKey();
    }

    public String getDateInputPattern() {
        return DatePatternEnum.DATE_INPUT_PATTERN.getMessageKey();
    }

    public String getDateTimePattern() {
        return DatePatternEnum.DATE_TIME_PATTERN.getMessageKey();
    }

    /**
     * Get the length for a field which contains an id.
     * 
     * @return the length for a field which contains an id.
     */
    public int getIdLen() {
        return ADMValidator.LENGTH_ID;
    }

    /**
     * Get the length for a field which contains an user id.
     * 
     * @return the length for a field which contains an user id.
     */
    public int getUserIdLen() {
        return ADMValidator.LENGTH_USERID;
    }

    /**
     * Get the length for a field which contains a name.
     * 
     * @return the length for a field which contains a name.
     */
    public int getNameLen() {
        return ADMValidator.LENGTH_NAME;
    }

    public int getTenantFieldLen() {
        return ADMValidator.LENGTH_TENANT_FIELD;
    }

    /**
     * Get the length for a field which contains a description.
     * 
     * @return the length for a field which contains a description.
     */
    public int getDescriptionLen() {
        return ADMValidator.LENGTH_DESCRIPTION;
    }

    /**
     * Get the length for a field which contains a reference ID.
     *
     * @return the length for a field which contains a reference ID.
     */
    public int getReferenceIdLen() {
        return ADMValidator.LENGTH_REFERENCE_ID;
    }

    /**
     * Get the length for a field which contains a group name.
     * 
     * @return the length for a field which contains a group name.
     */
    public int getGroupNameLen() {
        return ADMValidator.LENGTH_USER_GROUP_NAME;
    }

    /**
     * Get the length for a field which contains a percent value.
     * 
     * @return the length for a field which contains a percent value.
     */
    public int getPercentValueLen() {
        return ADMValidator.LENGTH_PERCENT_VALUE;
    }

    public int getDNLen() {
        return ADMValidator.LENGTH_DN;
    }

    public int getIntLen() {
        return ADMValidator.LENGTH_INT;
    }

    public int getLongLen() {
        return ADMValidator.LENGTH_LONG;
    }

    /**
     * Get the length for a field which contains a discount period.
     * 
     * @return the length for a field which contains a description.
     */
    public int getDiscountPeriodLen() {
        return ADMValidator.LENGTH_DISCOUNT_PERIOD;
    }

    public List<String> getActiveLocales() {

        List<String> list = LocaleUtils.getSupportedLocales();
        List<String> activeLanguages = getActiveLanguageCode();
        activeLanguages.retainAll(list);

        return activeLanguages;
    }

    public List<SelectItem> getAvailableLanguageItems() {
        List<SelectItem> availableLanguageItems = new ArrayList<SelectItem>();
        for (String isoCode : getActiveLocales()) {
            SelectItem selectItem = new SelectItem();
            Locale languageLocale = new Locale(isoCode);
            String translatedLocale = languageLocale.getDisplayLanguage(ui
                    .getViewLocale());
            selectItem.setLabel(translatedLocale);
            selectItem.setValue(isoCode);
            availableLanguageItems.add(selectItem);
        }
        return availableLanguageItems;
    }

    public boolean checkLocaleValidation(String locale) {
        List<String> locales = getActiveLocales();
        boolean isValid = true;
        if (locales != null && locale != null) {
            if (!locales.contains(locale)) {
                isValid = false;
                addMessage(null, FacesMessage.SEVERITY_WARN,
                        BaseBean.WARNING_SUPPORTEDLANGUAGE_LOCALE_INVALID,
                        new Locale(locale).getDisplayLanguage(ui
                                .getViewLocale()));
            }
        }

        return isValid;
    }

    public String getUserLocaleUpdated() {
        loadIdentityService();
        VOUserDetails voUser = identityService.getCurrentUserDetailsIfPresent();
        VOUserDetails voUserInSession = this
                .getUserFromSessionWithoutException();
        this.errorPanelForLocaleShow = false;
        if (voUser != null && voUser.getLocale() != null
                && voUserInSession != null
                && voUserInSession.getLocale() != null) {
            List<String> locales = getActiveLocales();
            if (locales != null && !locales.isEmpty()) {
                this.oldUserLocale = voUser.getLocale();
                if (!locales.contains(voUser.getLocale())) {
                    voUser.setLocale(getDefaultLocale().getLanguage());
                    setUserInSession(voUser);
                    this.errorPanelForLocaleShow = true;
                } else {
                    if (!oldUserLocale.equalsIgnoreCase(voUserInSession
                            .getLocale())) {
                        setUserInSession(voUser);
                    }
                }
            }
        }
        return "";
    }

    public String getOldUserLocale() {
        return new Locale(this.oldUserLocale).getDisplayLanguage(ui
                .getViewLocale());
    }

    public boolean getErrorPanelForLocaleShow() {
        return errorPanelForLocaleShow;
    }

    public Iterator<Locale> getSupportedLocalesIterator() {
        return getSupportedLocaleList().iterator();
    }

    public List<Locale> getSupportedLocaleList() {
        List<String> languageIds = getActiveLanguageCode();
        List<Locale> list = new ArrayList<Locale>();
        Iterator<Locale> it = getFacesContext().getApplication()
                .getSupportedLocales();
        while (it.hasNext()) {
            Locale locale = it.next();
            if (languageIds.contains(locale.getLanguage())) {
                list.add(locale);
            }
        }
        return list;
    }

    private List<String> getActiveLanguageCode() {
        getManageLanguageService();
        List<String> languageIds = new ArrayList<String>();

        List<POSupportedLanguage> languages = manageLanguageService
                .getLanguages(true);
        if (languages != null && languages.size() > 0) {
            for (POSupportedLanguage lanugage : languages) {
                languageIds.add(lanugage.getLanguageISOCode());
            }
        }
        return languageIds;
    }

    public Locale getDefaultLocale() {
        getManageLanguageService();

        String defaultLanguageISOCode;
        try {
            defaultLanguageISOCode = manageLanguageService.getDefaultLanguage();
        } catch (ObjectNotFoundException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_DEFAULT_LANGUAGE_NOT_FOUND,
                    new String[] {});
            return Locale.ENGLISH;
        }
        if (defaultLanguageISOCode != null) {
            return new Locale(defaultLanguageISOCode);
        }

        return Locale.ENGLISH;
    }

    public String getServiceBaseUri() {
        return Constants.SERVICE_BASE_URI;
    }

    public String getBuildId() {
        initBuildIdAndDate();
        return buildId;
    }

    public String getBuildDate() {
        initBuildIdAndDate();
        return buildDate;
    }

    public void setInternalAuthMode(Boolean internalAuthMode) {
        this.internalAuthMode = internalAuthMode;
    }

    /**
     * Creates an identifier based on the current time.
     * 
     * @return the current time in milliseconds converted to a hex string
     */
    public String getRandomId() {
        return Long.toHexString(System.currentTimeMillis());
    }

    /**
     * Tests whether the UI element with the given ID is generally hidden in
     * this installation.
     * 
     * @param id
     *            id of the UI element
     * @return <code>true</code>, if the element should be hidden
     */
    public boolean isUIElementHidden(String id) {
        final Map<String, Boolean> tmpSet = getHiddenUIElements();

        return tmpSet.containsKey(id);
    }

    /**
     * Getter for hidden UI elements. Initialize the set only the first
     * invocation time.
     * 
     * @return Map of hidden UI elements.
     */
    public Map<String, Boolean> getHiddenUIElements() {
        // initialize only the first invocation time
        if (hiddenUIElements == null) {
            hiddenUIElements = new HashMap<String, Boolean>();
            lookupConfigurationService();
            VOConfigurationSetting hiddenUIElementsConf = configurationService
                    .getVOConfigurationSetting(
                            ConfigurationKey.HIDDEN_UI_ELEMENTS,
                            Configuration.GLOBAL_CONTEXT);
            if (hiddenUIElementsConf != null) {
                String strHiddenUIElementsConf = hiddenUIElementsConf
                        .getValue();
                if (strHiddenUIElementsConf != null) {
                    String[] results = strHiddenUIElementsConf.split(",");
                    for (String str : results) {
                        String trimmedStr = str.trim();
                        if (!trimmedStr.equals("")) {
                            hiddenUIElements.put(trimmedStr, Boolean.FALSE);
                        }
                    }
                }
            }
        }
        return hiddenUIElements;
    }

    /**
     * Initialize the {@link ConfigurationService} if not already done.
     */
    private void lookupConfigurationService() {
        if (configurationService == null) {
            configurationService = ServiceAccess.getServiceAcccessFor(
                    JSFUtils.getRequest().getSession()).getService(
                    ConfigurationService.class);
        }
    }

    /**
     * Determines if payment info should be visible in the marketplace
     *
     * @return true - if payment info should be visible, false - otherwise
     */
    public boolean isPaymentInfoAvailable() {
        lookupConfigurationService();
        return configurationService.isPaymentInfoAvailable();
    }

    /**
     * Checks if the reporting is available. This is the case if the
     * {@link ConfigurationKey#REPORT_ENGINEURL} is set to a non empty value.
     * 
     * @return <code>true</code> if reporting is available otherwise
     *         <code>false</code>.
     */
    public boolean isReportingAvailable() {
        if (reportingAvailable == null) {
            lookupConfigurationService();
            VOConfigurationSetting reportEngineUrl = configurationService
                    .getVOConfigurationSetting(
                            ConfigurationKey.REPORT_ENGINEURL,
                            Configuration.GLOBAL_CONTEXT);
            reportingAvailable = Boolean.valueOf(reportEngineUrl != null
                    && reportEngineUrl.getValue() != null
                    && reportEngineUrl.getValue().trim().length() > 0);
        }
        return reportingAvailable.booleanValue();
    }

    /**
     * Checks if the {@link ConfigurationKey#AUTH_MODE} is set to INTERNAL.
     * 
     * @return <code>true</code> if AUTH_MODE is set to INTERNAL otherwise
     *         <code>false</code>.
     */
    public boolean isInternalAuthMode() {
        if (internalAuthMode == null) {
            lookupConfigurationService();
            VOConfigurationSetting authMode = configurationService
                    .getVOConfigurationSetting(ConfigurationKey.AUTH_MODE,
                            Configuration.GLOBAL_CONTEXT);
            internalAuthMode = Boolean.valueOf(authMode.getValue().equals(
                    AuthenticationMode.INTERNAL.name()));
        }
        return internalAuthMode.booleanValue();
    }

    /**
     * Checks if the {@link ConfigurationKey#AUTH_MODE} is set to INTERNAL.
     *
     * @return <code>true</code> if AUTH_MODE is set to INTERNAL otherwise
     *         <code>false</code>.
     */
    public boolean isSamlSpAuthMode() {
        if (samlSpAuthMode == null) {
            lookupConfigurationService();
            VOConfigurationSetting authMode = configurationService
                .getVOConfigurationSetting(ConfigurationKey.AUTH_MODE,
                    Configuration.GLOBAL_CONTEXT);
            samlSpAuthMode = Boolean.valueOf(authMode.getValue().equals(
                AuthenticationMode.SAML_SP.name()));
        }
        return samlSpAuthMode.booleanValue();
    }

    /**
     * Setter for configuration service. Use it for JUnit for stubbing the EJB.
     * 
     * @param configurationService
     *            Configuration
     */
    protected void setConfigurationService(
            ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Reads the configured time zone from the server. If no information is
     * available, the default time zone will be used.
     * 
     * @return the time zone id (see {@link TimeZone#getAvailableIDs()}
     */
    public String getTimeZoneId() {
        if (timeZoneId == null) {
            lookupConfigurationService();
            VOConfigurationSetting setting = configurationService
                    .getVOConfigurationSetting(ConfigurationKey.TIME_ZONE_ID,
                            Configuration.GLOBAL_CONTEXT);
            if (setting != null && setting.getValue() != null) {
                timeZoneId = TimeZone.getTimeZone(setting.getValue()).getID();
            } else {
                timeZoneId = "GMT";
            }
        }
        return timeZoneId;
    }

    /**
     * Returns the base URL configured on the server excluding the '/' at the
     * end.
     * 
     * @return the server base URL
     */
    public String getServerBaseUrl() {
        if (serverBaseUrl == null) {
            lookupConfigurationService();
            VOConfigurationSetting setting = configurationService
                    .getVOConfigurationSetting(ConfigurationKey.BASE_URL,
                            Configuration.GLOBAL_CONTEXT);
            if (setting == null || setting.getValue() == null
                    || setting.getValue().length() == 0) {
                setting = configurationService.getVOConfigurationSetting(
                        ConfigurationKey.BASE_URL_HTTPS,
                        Configuration.GLOBAL_CONTEXT);
            }
            if (setting != null) {
                serverBaseUrl = getTailoredUrl(setting);
            }
        }
        return serverBaseUrl;
    }

    /**
     * Returns the base URL for https configured on the server excluding the '/'
     * at the end.
     * 
     * @return the server base URL
     */
    public String getServerBaseUrlHttps() {
        if (serverBaseUrlHttps == null) {
            lookupConfigurationService();
            VOConfigurationSetting setting = configurationService
                    .getVOConfigurationSetting(ConfigurationKey.BASE_URL_HTTPS,
                            Configuration.GLOBAL_CONTEXT);
            if (setting != null) {
                serverBaseUrlHttps = getTailoredUrl(setting);
            }
        }
        return serverBaseUrlHttps;
    }

    /**
     * Removes a possible trailing '/' from the config setting URL.
     */
    private String getTailoredUrl(VOConfigurationSetting setting) {
        String url = setting.getValue();
        if (url != null && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * Resets values defined by configuration of BES - to be used after
     * modifying the configuration.
     */
    public void reset() {
        timeZoneId = null;
        serverBaseUrl = null;
        reportingAvailable = null;
        hiddenUIElements = null;
    }

    /**
     * @return the interval of keepAlive tag
     */
    public Long getInterval() {
        if (interval == null) {
            FacesContext ctx = getFacesContext();
            HttpSession httpSession = (HttpSession) ctx.getExternalContext()
                    .getSession(false);
            int maxInactiveInterval = httpSession.getMaxInactiveInterval();
            // To keep session alive, the interval value is 1 minute less than
            // session timeout.
            long intervalValue = (long) maxInactiveInterval * 1000 - 60000L;
            interval = Long.valueOf(intervalValue);
        }
        return interval;
    }

    /**
     * Setter for manage language service. Use it for JUnit for stubbing the
     * EJB.
     * 
     * @param ManageLanguageService
     * 
     */
    protected void setManageLanguageService(
            ManageLanguageService manageLanguageService) {
        this.manageLanguageService = manageLanguageService;
    }

    // allow stubbing
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * Initialize the {@link ManageLanguageService} if not already done.
     */
    private void getManageLanguageService() {
        if (manageLanguageService == null) {
            manageLanguageService = ServiceAccess.getServiceAcccessFor(
                    JSFUtils.getRequest().getSession()).getService(
                    ManageLanguageService.class);
        }
    }

    protected void addMessage(String clientId, FacesMessage.Severity severity,
            String key, String param) {
        JSFUtils.addMessage(clientId, severity, key, new Object[] { param });
    }

    private void loadIdentityService() {
        if (identityService == null) {
            identityService = ServiceAccess.getServiceAcccessFor(
                    JSFUtils.getRequest().getSession()).getService(
                    IdentityService.class);
        }
    }

    protected VOUserDetails getUserFromSessionWithoutException() {
        HttpServletRequest request = (HttpServletRequest) getFacesContext()
                .getExternalContext().getRequest();
        VOUserDetails voUserDetails = (VOUserDetails) request.getSession()
                .getAttribute(Constants.SESS_ATTR_USER);
        return voUserDetails;
    }

    protected void setUserInSession(VOUserDetails voUserDetails) {
        if (voUserDetails == null) {
            throw new SaaSSystemException("voUSerDetails must not be null!");
        }
        HttpServletRequest request = (HttpServletRequest) getFacesContext()
                .getExternalContext().getRequest();
        request.getSession().setAttribute(Constants.SESS_ATTR_USER,
                voUserDetails);
        JSFUtils.verifyViewLocale();
    }

    private boolean isBlank(final String str) {
        if (str == null) {
            return true;
        }
        return str.trim().length() == 0;
    }

    public String getMarketplaceId() {
        HttpServletRequest request = (HttpServletRequest) getFacesContext()
                .getExternalContext().getRequest();
        String marketplaceId = (String) request
                .getAttribute(Constants.REQ_PARAM_MARKETPLACE_ID);
        if (isBlank(marketplaceId)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                marketplaceId = (String) session
                        .getAttribute(Constants.REQ_PARAM_MARKETPLACE_ID);
            }
        }
        return marketplaceId;
    }

    public void convertActivationTimes(
            List<POSubscriptionAndCustomer> poSubscriptionAndCustomers) {
        for (POSubscriptionAndCustomer poSubscriptionAndCustomer : poSubscriptionAndCustomers) {
            convertActivationTime(poSubscriptionAndCustomer);
        }
    }

    private void convertActivationTime(POSubscriptionAndCustomer poSubscriptionAndCustomer) {
        String time = poSubscriptionAndCustomer.getActivation();
        if (time == null) {
            poSubscriptionAndCustomer.setActivation("");
        } else if (isLongValue(time)) {
            poSubscriptionAndCustomer.setActivation(DateConverter
                    .convertLongToDateTimeFormat(Long.valueOf(time)
                                    .longValue(), TimeZone
                                    .getTimeZone(getTimeZoneId()),
                            SIMPLE_DATE_PATTERN));

        }
    }

    private boolean isLongValue(String value) {
        try {
            return (Long.parseLong(value) > 0);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public String getBrandingURL() {
        if (requestContextPath.isEmpty()) {
            initRequestContextPath();
        }

        return requestContextPath;
    }

    private void initRequestContextPath() {
        if (isFCIPBrandingPackageAvailable()) {
            // custom branding
            requestContextPath = FCIP_CONTEXT_PATH_ROOT;
        } else {
            // default branding
            requestContextPath = getFacesContext().getExternalContext()
                    .getRequestContextPath();
        }
    }

    boolean isFCIPBrandingPackageAvailable() {
        String glassfishRoot = ui.getSystemProperty("catalina.base");
        String brandingPackageFolder = glassfishRoot + File.separator
                + APPLICATIONS_ROOT_FOLDER + File.separator
                + FCIP_BRANDING_PACKAGE;

        return fileExists(brandingPackageFolder);
    }

    boolean fileExists(String pathname) {
        return new File(pathname).exists();
    }

    public String getJSMessageByKey(String msgKey) {
        return convertText(ui.getText(msgKey, (Object[]) null));
    }

    private String convertText(String text) {
        String str = text;
        if (null == str || str.trim().length() == 0) {
            return "";
        }
        if (str.indexOf("\"") > -1) {
            str = str.replaceAll("\"", "\\\\\"");
        }

        return str;
    }
}
