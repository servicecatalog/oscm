/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2012-09-06
 *
 *******************************************************************************/

package org.oscm.app.azure.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

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
@ManagedBean(name = "configurationBean")
@ViewScoped
public class ConfigurationBean implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationBean.class);

    // Reference to an APPlatformService instance
    private APPlatformService platformService;

    // The configuration settings
    private HashMap<String, Setting> items;

    // test
    private String val;

    // Status of the most recent operation
    private String status;

    // Credentials of the controller administrator
    private final String username = "administrator";
    private final String password = "admin123";

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * Constructor.
     */
    public ConfigurationBean() {
        this.platformService = APPlatformServiceFactory.getInstance();
    }

    public String getLoggedInUserId() {
        FacesContext facesContext = getFacesContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);
        if (session != null) {
            return "" + session.getAttribute("loggedInUserId");
        }
        return null;
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

    public void setItems(HashMap<String, Setting> items) {
        this.items = items;
    }

    /**
     * Returns the keys of all controller configuration settings.
     *
     * @return the list of keys
     */
    public List<String> getItemKeys() {
        return new ArrayList<>(getItems().keySet());
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

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}

