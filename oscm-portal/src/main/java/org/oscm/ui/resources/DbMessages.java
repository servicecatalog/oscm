/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ServiceAccess;
import org.oscm.internal.intf.BrandService;
import org.oscm.internal.operatorservice.LocalizedDataService;

public class DbMessages extends DefaultMessages {

    static final int RESET_INTERVAL = 3600000;

    // the request contains the organization id but the translations are
    // marketplace specific. This map is used to store the customer id ->
    // marketplace id mapping
    private final Map<String, String> oId2mId = Collections
            .synchronizedMap(new HashMap<String, String>());

    // caches mId to properties
    private final Map<String, Properties> propertiesMap = Collections
            .synchronizedMap(new HashMap<String, Properties>());

    // caches language code to properties
    private final Map<String, Properties> localizedPropertiesMap = Collections
            .synchronizedMap(new HashMap<String, Properties>());

    long refreshPropertyTime = System.currentTimeMillis();

    private BrandService brandManagement;

    private LocalizedDataService localizedDataService;

    DbMessages(Locale locale) throws IOException {
        super(locale);
    }

    /**
     * Get the brand management service
     * 
     * @return the accounting management service
     */
    BrandService getBrandManagementService(ServiceAccess serviceAccess) {
        if (brandManagement == null) {
            brandManagement = serviceAccess.getService(BrandService.class);
        }
        return brandManagement;
    }

    /**
     * @return an enumeration of the keys.
     */
    @Override
    public Enumeration<String> getKeys() {
        Properties properties = getProperties();
        if (properties != null) {
            ArrayList<String> list = new ArrayList<String>();
            for (Enumeration<String> e = super.getKeys(); e.hasMoreElements();) {
                list.add(e.nextElement());
            }
            for (Enumeration<Object> e = properties.keys(); e.hasMoreElements();) {
                String key = e.nextElement().toString();
                if (!list.contains(key)) {
                    list.add(key);
                }
            }
            return Collections.enumeration(list);
        }
        return super.getKeys();
    }

    /**
     * Gets an object for the given key from this resource bundle (if we have
     * customized translations use them otherwise return the default one).
     * Returns null if this resource bundle does not contain an object for the
     * given key.
     * 
     * @param key
     *            the key for the desired object
     * 
     * @return the object for the given key, or null
     * 
     * @throws NullPointerException
     *             if key is null
     */
    @Override
    public Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        final Properties properties = getProperties();
        if (properties != null) {
            final Object obj = properties.get(key);
            if (obj != null && obj.toString().trim().length() != 0) {
                return obj;
            }
        }
        if (this.getClass().equals(DbMessages.class)
                && key.startsWith("locale.")) {
            return "";
        }
        return super.handleGetObject(key);
    }

    /**
     * Get the properties for the current organization. The identifier of the
     * organization is read from the current request.
     * 
     * @return the properties for the current organization.
     */
    private Properties getProperties() {
        Properties properties = null;
        FacesContext fc = FacesContext.getCurrentInstance();

        if (fc != null) {
            if (isResetRequired(System.currentTimeMillis())) {
                resetProperties();
            }
            HttpServletRequest request = (HttpServletRequest) fc
                    .getExternalContext().getRequest();
            Locale locale = getLocale();
            if (locale.getLanguage().length() == 0) {
                locale = Locale.ENGLISH;
            }

            if (request != null) {
                HttpSession session = request.getSession();
                String marketplaceId = (String) session
                        .getAttribute(Constants.REQ_PARAM_MARKETPLACE_ID);

                // check if the properties are cached for the marketplace id and if
                // not read it by service
                if (marketplaceId != null) {
                    properties = propertiesMap.get(marketplaceId);
                    if (properties == null) {
                        ServiceAccess serviceAccess = ServiceAccess
                                .getServiceAcccessFor(request.getSession());
                        properties = getBrandManagementService(serviceAccess)
                                .loadMessagePropertiesFromDB(marketplaceId,
                                        locale.toString());
                        propertiesMap.put(marketplaceId, properties);
                    }
                    if (!properties.isEmpty()) {
                        return properties;
                    }
                }

                properties = localizedPropertiesMap.get(locale.getLanguage());
                if (properties == null) {
                    ServiceAccess serviceAccess = ServiceAccess
                            .getServiceAcccessFor(request.getSession());
                    properties = getLocalizedDataService(serviceAccess)
                            .loadMessageProperties(locale.getLanguage());
                    if (properties == null) {
                        properties = new Properties();
                    }
                    localizedPropertiesMap.put(locale.getLanguage(), properties);
                }
            }
        }
        return properties;
    }

    LocalizedDataService getLocalizedDataService(ServiceAccess serviceAccess) {
        if (localizedDataService == null)
            localizedDataService = serviceAccess
                    .getService(LocalizedDataService.class);
        return localizedDataService;
    }

    boolean isResetRequired(long time) {
        return refreshPropertyTime < (time - RESET_INTERVAL);
    }

    public void resetProperties() {
        oId2mId.clear();
        propertiesMap.clear();
        refreshPropertyTime = System.currentTimeMillis();
        localizedPropertiesMap.clear();
        if (parent != null) {
            ((DbMessages) parent).resetProperties();
        }
    }
}
