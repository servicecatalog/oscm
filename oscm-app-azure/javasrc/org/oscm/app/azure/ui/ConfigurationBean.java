/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2012-09-06
 *
 *******************************************************************************/

package org.oscm.app.azure.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.oscm.app.azure.i18n.Messages;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.intf.APPlatformService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean for reading and writing controller configuration settings.
 */
public class ConfigurationBean {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationBean.class);

    // Reference to an APPlatformService instance
    private APPlatformService platformService;

    // The configuration settings
    private HashMap<String, Setting> items;

    // Status of the most recent operation
    private String status;

    // Credentials of the controller administrator
    private final String username = "manager_A";
    private final String password = "secret";

    /**
     * Constructor.
     */
    public ConfigurationBean(APPlatformService platformService) {
        try {
            if (platformService == null) {
                this.platformService = APPlatformServiceFactory.getInstance();
            } else {
                this.platformService = platformService;
            }
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Returns a map with all the controller configuration settings as key/value
     * pairs.
     *
     * @return the settings
     */
    public HashMap<String, Setting> getItems() {
        if (items == null) {
            try {
                // Read settings once
                PasswordAuthentication pwAuth = new PasswordAuthentication(username, password);
                items = platformService.getControllerSettings("ess.azureARM", pwAuth);

            } catch (Exception e) {
                // Fail until correct credentials are set
                items = new HashMap<>();
                items.put("key A", new Setting("value A", "value A"));
                items.put("key B", new Setting("value B", "value B"));
                items.put("key C", new Setting("value C", "value C"));
                items.put("key D", new Setting("value D", "value D"));
            }
        }
        return items;
    }

    /**
     * Returns the keys of all controller configuration settings.
     *
     * @return the list of keys
     */
    public List<String> getItemKeys() {
        List<String> keys = new ArrayList<String>();
        keys.addAll(getItems().keySet());
        return keys;
    }

    /**
     * Saves the controller configuration settings.
     */
    public void save() {
        try {
            PasswordAuthentication pwAuth = new PasswordAuthentication(username, password);
            platformService.storeControllerSettings("ess.azureARM", items, pwAuth);

            // Update status
            Locale currentLocale = new Locale("en");
            final FacesContext facesInstance = FacesContext.getCurrentInstance();
            if (facesInstance != null && facesInstance.getApplication() != null) {
                currentLocale = facesInstance.getApplication().getDefaultLocale();
            }
            status = Messages.get(currentLocale.getLanguage(), "ui.config.status.saved");

        } catch (Exception e) {
            status = "*** " + e.getMessage();
        }
    }

    /**
     * Returns the status of the most recent operation.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }
}
