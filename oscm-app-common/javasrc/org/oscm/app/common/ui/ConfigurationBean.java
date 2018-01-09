/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  AWS controller implementation for the 
 *  Asynchronous Provisioning Platform (APP)
 *       
 *  Creation Date: 2013-10-17                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.common.ui;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ControllerConfigurationKey;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.i18n.Messages;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean for reading and writing controller configuration settings.
 */
@ManagedBean
@SessionScoped
public class ConfigurationBean implements Serializable {

    private static final long serialVersionUID = -1300403486736808608L;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ConfigurationBean.class);

    public static final String[] ACCESS_PARAMETERS = new String[] {
            ControllerConfigurationKey.BSS_ORGANIZATION_ID.name(),
            ControllerConfigurationKey.BSS_USER_ID.name(),
            ControllerConfigurationKey.BSS_USER_KEY.name(),
            ControllerConfigurationKey.BSS_USER_PWD.name() };

    // Status styles
    private static final String STATUS_CLASS_INFO = "statusInfo";
    private static final String STATUS_CLASS_ERROR = "statusError";

    // Localized parameter info
    public final static String MSG_CONFIG_TITLE = "config_ui_title";
    public final static String MSG_SETTINGS_TITLE = "config_ui_settings_title";
    public final static String MSG_DISPLAYNAME_PREFIX = "param_display_";
    public final static String MSG_TOOLTIP_PREFIX = "param_tooltip_";

    // Reference to an APPlatformService instance
    private APPlatformService platformService;

    @Inject
    private ControllerAccess controllerAccess;

    // The map with all configuration settings
    private Map<String, ConfigurationItem> items;

    // The controller specific settings
    private LinkedList<ConfigurationItem> ctrlItems;

    // The access data (core) settings
    private LinkedList<ConfigurationItem> accessItems;

    // Status of the most recent operation
    private String status;
    private String statusClass;

    // Credentials of the controller technology manager
    private String username;
    private String password;

    // Currently used locale
    private String locale;

    // Remembers whether current values have been modified
    private boolean isDirty;

    private boolean isSaved;
    private boolean isUpdated;
    private String token;
    private String tokenIntern;

    public String getToken() {
        return tokenIntern;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Checks if the internal token matches the one submitted from the form. If
     * the tokens don't match, the action should not be processed.
     * 
     * @return <code>true</code> if internal and submitted token match otherwise
     *         <code>false</code>
     */
    public boolean isTokenValid() {
        return tokenIntern.equals(token);
    }

    public void resetToken() {
        tokenIntern = String.valueOf(Math.random());
    }

    /**
     * Constructor.
     */
    public ConfigurationBean() {
        try {
            platformService = APPlatformServiceFactory.getInstance();
            resetToken();

        } catch (IllegalStateException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    @PostConstruct
    public void init() {
        LOGGER.info("Initialize config bean");
    }

    public void setControllerAccess(final ControllerAccess controllerAccess) {
        this.controllerAccess = controllerAccess;
    }

    public String getInitialize() {

        if (!isSaved) {
            status = null;
        } else {
            isSaved = false;
        }
        if (isUpdated) {
            isDirty = true;
        } else {
            isDirty = false;
        }
        return "";
    }

    /**
     * Returns a map with all the controller configuration settings as key/value
     * pairs.
     * 
     * @return the settings
     */
    public List<ConfigurationItem> getItems() {
        if (ctrlItems == null) {
            readConfiguration();
        } else {
            validateLocale();
        }
        isUpdated = false;
        return ctrlItems;
    }

    /**
     * Returns a map with all the access data specific settings as key/value
     * pairs.
     * 
     * @return the settings
     */
    public List<ConfigurationItem> getAccessItems() {
        if (accessItems == null) {
            readConfiguration();
        } else {
            validateLocale();
        }
        isUpdated = false;
        return accessItems;
    }

    private String changedItemKey;

    public String getChangedItemKey() {
        return changedItemKey;
    }

    public void setChangedItemKey(String changedItemKey) {
        this.changedItemKey = changedItemKey;
    }

    public void updateItems() {
        if (items != null) {
            ConfigurationItem item = items.get(getChangedItemKey());
            item.setDirty(true);
            isUpdated = true;
        }
    }

    /**
     * Reads all controller specific settings into the local cache.
     */
    private void readConfiguration() {
        try {
            // Get logged in credentials
            FacesContext facesContext = getContext();
            HttpSession session = (HttpSession) facesContext
                    .getExternalContext().getSession(false);
            username = "" + session.getAttribute("loggedInUserId");
            password = "" + session.getAttribute("loggedInUserPassword");

            // Get current browser locale (for display names and tooltips)
            Locale currentLocale = facesContext.getViewRoot().getLocale();
            this.locale = currentLocale.getLanguage();

            // Get all settings and map them to configuration items
            HashMap<String, Setting> settings = platformService
                    .getControllerSettings(controllerAccess.getControllerId(),
                            new PasswordAuthentication(username, password));
            items = new HashMap<>();
            for (String key : settings.keySet()) {
                // Add next item to local cache
                Setting setting = settings.get(key);
                addConfigurationItem(key,
                        setting != null ? setting.getValue() : null);
            }

            // Build groups which are displayed to the user
            accessItems = new LinkedList<>();
            List<String> core_ps = Arrays.asList(ACCESS_PARAMETERS);
            for (String key : core_ps) {
                ConfigurationItem item = getConfigurationItem(key);
                if (key.equals(ControllerConfigurationKey.BSS_ORGANIZATION_ID
                        .name())) {
                    item.setReadOnly(true);
                }
                accessItems.add(item);
            }

            // Read controller settings
            ctrlItems = new LinkedList<>();
            for (String key : controllerAccess.getControllerParameterKeys()) {
                ConfigurationItem item = getConfigurationItem(key);
                ctrlItems.add(item);
            }

            // Reset dirty state
            isDirty = false;

        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.error("Failed to load items", e);
            setErrorStatus(e);
        }

    }

    /**
     * Checks whether client has changed browser locale.
     */
    private void validateLocale() {
        // Get logged in credentials
        FacesContext facesContext = getContext();
        // Get current browser locale (for display names and tooltips)
        Locale currentLocale = facesContext.getViewRoot().getLocale();
        if (!this.locale.equals(currentLocale.getLanguage())) {
            // Locale has been changed!
            this.locale = currentLocale.getLanguage();
            for (ConfigurationItem item : items.values()) {
                applyLocale(item);
            }
        }
    }

    /**
     * Returns the configuration item with the given key.
     * <p>
     * If it does not yet exist it will be created and initialized with an empty
     * value.
     */
    private ConfigurationItem getConfigurationItem(String key) {
        if (!items.containsKey(key)) {
            return addConfigurationItem(key, "");
        }
        return items.get(key);
    }

    /**
     * Creates a configuration item with the given key and value.
     * <p>
     * The item will be stored within the internal cached list of settings.
     */
    private ConfigurationItem addConfigurationItem(String key, String value) {
        // Create new entity
        ConfigurationItem item = new ConfigurationItem(key, value);

        // Set locale specific texts
        applyLocale(item);

        // Cache item in local collection
        items.put(key, item);

        return item;
    }

    /**
     * Apply current locale to given configuration item
     */
    private void applyLocale(ConfigurationItem item) {
        String tooltip = controllerAccess.getMessage(locale,
                MSG_TOOLTIP_PREFIX + item.getKey(), new Object[0]);
        if (tooltip != null && !tooltip.startsWith("!")) {
            item.setTooltip(tooltip);
        } else {
            item.setTooltip(null);
        }
        String displayName = controllerAccess.getMessage(locale,
                MSG_DISPLAYNAME_PREFIX + item.getKey(), new Object[0]);
        if (displayName != null && !displayName.startsWith("!")) {
            item.setDisplayName(displayName);
        } else {
            item.setDisplayName(null);
        }
    }

    /**
     * Stores the currently logged-in user as administrator
     */
    public void applyCurrentUser() {
        FacesContext facesContext = getContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);

        // Get logged in user
        String userId = "" + session.getAttribute("loggedInUserId");
        String password = "" + session.getAttribute("loggedInUserPassword");
        // TODO the user key is not yet available
        String userKey = "" + session.getAttribute("loggedInUserKey");

        // Update access settings
        getConfigurationItem(ControllerConfigurationKey.BSS_USER_ID.name())
                .setValue(userId);
        getConfigurationItem(ControllerConfigurationKey.BSS_USER_PWD.name())
                .setValue(password);
        getConfigurationItem(ControllerConfigurationKey.BSS_USER_KEY.name())
                .setValue(userKey);
    }

    /**
     * Saves the controller configuration settings.
     */
    public void save() {

        if (!isTokenValid()) {
            return;
        }

        try {
            // Collect all modified values
            HashMap<String, Setting> map = new HashMap<>();
            if (items != null) {
                for (ConfigurationItem item : items.values()) {
                    if (item.isDirty()) {
                        String value = item.getValue();
                        if (value != null) {
                            value = value.trim();
                        }
                        map.put(item.getKey(),
                                new Setting(item.getKey(), value));
                    }
                }
            }

            // And store them
            platformService.storeControllerSettings(
                    controllerAccess.getControllerId(), map,
                    new PasswordAuthentication(username, password));

            // Update status
            setInfoStatus(Messages.get(locale, "ui.config.status.saved"));

        } catch (Throwable e) {
            LOGGER.error("Failed to save items", e);
            setErrorStatus(e);
        }

        // Reread all items
        readConfiguration();
        isSaved = true;
        resetItems();
        resetToken();
    }

    private void resetItems() {
        if (items != null) {
            for (ConfigurationItem item : items.values()) {
                item.setDirty(false);
            }
        }
        isUpdated = false;
    }

    /**
     * Revert all changes
     */
    public void undo() {
        // Reread all items
        readConfiguration();
    }

    /**
     * Returns whether any value of the page has been changed.
     * 
     * @return the status
     */
    public boolean isDirty() {
        return isDirty;
    }

    /**
     * Returns the status of the most recent operation.
     * 
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the status of the most recent operation.
     * 
     * @return the status
     */
    public String getStatusClass() {
        return statusClass;
    }

    /**
     * Sets an error status which will be displayed to the user
     */
    private void setErrorStatus(Throwable e) {
        status = "*** " + ((e.getMessage() != null) ? e.getMessage()
                : e.getClass().getName());
        statusClass = STATUS_CLASS_ERROR;
    }

    /**
     * Sets an info status which will be displayed to the user
     */
    private void setInfoStatus(String message) {
        status = message;
        statusClass = STATUS_CLASS_INFO;
    }

    /**
     * Returns the title string for the configuration page.
     * <p>
     * The title will be retrieved from the resource bundle of the controler. If
     * not defined some default title (including the ID) will be used.
     * 
     * @return the title
     */
    public String getConfigurationTitle() {
        if (locale == null) {
            this.locale = getContext().getViewRoot().getLocale().getLanguage();
        }
        String displayName = controllerAccess.getMessage(locale,
                MSG_CONFIG_TITLE, new Object[0]);
        if (displayName.equals("!" + MSG_CONFIG_TITLE + "!")) {
            // Not defined => use default
            displayName = Messages.get(locale, "ui.config.title",
                    controllerAccess.getControllerId());
        }

        return displayName;
    }

    public String getSettingsTitle() {
        if (locale == null) {
            this.locale = getContext().getViewRoot().getLocale().getLanguage();
        }
        String controllerSettingsTitle = controllerAccess.getMessage(locale,
                MSG_SETTINGS_TITLE, new Object[0]);
        if (controllerSettingsTitle.equals("!" + MSG_SETTINGS_TITLE + "!")) {
            // Not defined => use default
            controllerSettingsTitle = Messages.get(locale,
                    "ui.config.settings.title",
                    controllerAccess.getControllerId());
        }
        return controllerSettingsTitle;
    }

    public String getLoggedInUserId() {
        FacesContext facesContext = getContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);
        if (session != null) {
            String loggedInUserId = "" + session.getAttribute("loggedInUserId");
            return loggedInUserId;
        }
        return null;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean isSaved) {
        this.isSaved = isSaved;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean isUpdated) {
        this.isUpdated = isUpdated;
    }

    // allow stubbing in unit tests
    protected FacesContext getContext() {
        return FacesContext.getCurrentInstance();
    }
}
