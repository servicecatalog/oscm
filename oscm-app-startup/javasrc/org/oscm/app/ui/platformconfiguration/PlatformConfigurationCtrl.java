/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.app.ui.platformconfiguration;

import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.ui.BaseCtrl;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.util.*;

/**
 * Created by PLGrubskiM on 2017-04-24.
 */
@ViewScoped
@ManagedBean
public class PlatformConfigurationCtrl extends BaseCtrl {

    private static final String PWD_SUFFIX = "_PWD";

    @ManagedProperty(value = "#{platformConfigurationModel}")
    private PlatformConfigurationModel model;

    private APPConfigurationServiceBean appConfigService;

    @PostConstruct
    public void getInitialize() {
        try {
            model.setItems(initPlatformSettings());
            model.setKeys(initItemKeys());
            setModel(model);
        } catch (Exception e) {
            addError(e);
        }
    }

    public String save() {
        try {
            HashMap<String, String> store = new HashMap<>(
                    model.getItems());
            getAppConfigService().storeAppConfigurationSettings(store);
            addMessage(SUCCESS_SAVED);
        } catch (Exception e) {
            addError(e);
        }
        return OUTCOME_SUCCESS;
    }

    public boolean encrypted(String key) {
        if (key.endsWith(PWD_SUFFIX)) {
            return true;
        }
        return false;
    }

    private TreeMap<String, String> initPlatformSettings() throws ConfigurationException {
        HashMap<String, Setting> map = getAppConfigService()
                .getProxyConfigurationSettings();

        TreeMap<String, String> result = new TreeMap<>();

        for (Map.Entry<String, Setting> entry : map.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue().getValue();
            if (isSettingIncluded(key)) {
                result.put(key, value);
            }
        }
        return result;
    }

    private boolean isSettingIncluded(String key) {
        List<String> visibleSettingsList = new ArrayList<>();
        visibleSettingsList.add(PlatformConfigurationKey.APP_ADMIN_MAIL_ADDRESS.toString());
        visibleSettingsList.add(PlatformConfigurationKey.APP_BASE_URL.toString());
        visibleSettingsList.add(PlatformConfigurationKey.BSS_USER_KEY.toString());
        visibleSettingsList.add(PlatformConfigurationKey.BSS_USER_ID.toString());
        visibleSettingsList.add(PlatformConfigurationKey.BSS_USER_PWD.toString());
        if (visibleSettingsList.contains(key)) {
            return true;
        } else {
            return false;
        }
    }

    private List<String> initItemKeys() {
        List<String> keys = new ArrayList<>();
        TreeMap<String, String> items = model.getItems();
        if (items != null) {
            keys.addAll(items.keySet());
        }
        return keys;
    }

    public void setModel(PlatformConfigurationModel model) {
        this.model = model;
    }

    public PlatformConfigurationModel getModel() {
        return model;
    }

    public APPConfigurationServiceBean getAppConfigService() {
        if (appConfigService == null) {
            appConfigService = lookup(APPConfigurationServiceBean.class);
        }
        return appConfigService;
    }
}
