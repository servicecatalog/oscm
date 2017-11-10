/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *  Completion Time: <date>                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.faces.context.FacesContext;

import org.oscm.converter.PropertiesLoader;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.ServiceAccess;
import org.oscm.internal.intf.BrandService;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Managed bean which holds the active skin
 * 
 */
public class SkinBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -8396573356774096545L;

    public static final String DEFAULT_SKIN_NAME = "interstage";

    public static final String MARKETPLACE_STAGE_DEFAULT = "<img id=\"marketplaceStageDefault\" src=\"{0}"
            + Marketplace.MARKETPLACE_ROOT
            + "/img/flash.png\" style=\"border-style: none;\" />";

    public static final Map<String, String> KEY_TO_DEFAULTKEY;

    static {
        KEY_TO_DEFAULTKEY = new HashMap<String, String>();
        KEY_TO_DEFAULTKEY.put("stepBackgroundColor", "navItemBackgroundColor");
        KEY_TO_DEFAULTKEY.put("stepTextColor", "navItemTextColor");
        KEY_TO_DEFAULTKEY.put("toolbarBackgroundColor",
                "navGroupBackgroundColor");
        KEY_TO_DEFAULTKEY.put("toolbarBorderColor", "navGroupBorderColor");
        KEY_TO_DEFAULTKEY.put("collapsibleBackgroundColor",
                "generalBackgroundColor");
    }

    public static final String KEY_CONTENT_MARGIN = "contentMargin";

    public static final String KEY_GENERAL_PADDING = "generalPadding";

    private BrandService brandManagement;

    private Properties properties = null;

    /**
     * Cache for the stage content
     */
    private String marketplaceStage = null;

    protected BrandService getBrandManagementService() {
        if (brandManagement == null) {
            brandManagement = ServiceAccess.getServiceAcccessFor(
                    JSFUtils.getRequest().getSession()).getService(
                    BrandService.class);
        }
        return brandManagement;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = getProperties(DEFAULT_SKIN_NAME);
            addDefaultProperties(properties);
        }

        return properties;
    }

    /**
     * Read the properties from the property file for the given skin name.
     * 
     * @param skinName
     *            name of the skin for which the properties are read.
     * 
     * @return read properties.
     */
    private Properties getProperties(String skinName) {
        final String resource = skinName + ".skin.properties";
        return addDefaultProperties(PropertiesLoader.load(SkinBean.class,
                resource));
    }

    /**
     * Add default values for the properties with are not mandatory.
     */
    private Properties addDefaultProperties(Properties properties) {
        if (properties.getProperty(KEY_GENERAL_PADDING) == null) {
            properties.put(KEY_GENERAL_PADDING, "0px");
        }
        if (properties.getProperty(KEY_CONTENT_MARGIN) == null) {
            properties.put(KEY_CONTENT_MARGIN, "12px");
        }
        for (String key : KEY_TO_DEFAULTKEY.keySet()) {
            if (properties.getProperty(key) == null) {
                String defaultKey = KEY_TO_DEFAULTKEY.get(key);
                if (defaultKey != null) {
                    String defaultValue = properties.getProperty(defaultKey);
                    if (defaultValue != null) {
                        properties.put(key, properties.get(defaultKey));
                    }
                }
            }
        }
        return properties;
    }

    /**
     * Retrieves the content of the mpl stage from the brand service. If there
     * was no localized resource defined for the stage content, a default will
     * be returned. The locale used to retrieve the content is the locale of the
     * currently login user. If the access happens anonymously the browser
     * locale will be used.
     * 
     * @return returns the stage content for the current marketplace from the
     *         brand service depending on the local of the current user or
     *         browser locale.
     */
    public String getMarketplaceStage() {
        String localeString = null;

        FacesContext fc = getFacesContext();

        VOUserDetails voUserDetails = BaseBean
                .getUserFromSessionWithoutException(fc);
        if (voUserDetails != null) {
            localeString = voUserDetails.getLocale();
        }

        if (localeString == null) {
            Locale locale = fc.getViewRoot().getLocale();
            localeString = locale.getLanguage();
        }

        marketplaceStage = getBrandManagementService().getMarketplaceStage(
                getMarketplaceId(), localeString);

        if (isEmpty(marketplaceStage)) {
            // No localized resource found for the mpl stage
            marketplaceStage = MessageFormat.format(MARKETPLACE_STAGE_DEFAULT,
                    getRequestContextPath());
        }
        return marketplaceStage;
    }

    String getRequestContextPath() {
        return getFacesContext().getExternalContext().getRequestContextPath();
    }

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    private static final boolean isEmpty(String string) {
        return (string == null || string.trim().length() == 0);
    }
}
