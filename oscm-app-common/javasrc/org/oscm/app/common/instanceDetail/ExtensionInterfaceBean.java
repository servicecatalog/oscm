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
package org.oscm.app.common.instanceDetail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.oscm.app.v2_0.data.ControllerConfigurationKey;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.InstanceAccess;
import org.oscm.app.v2_0.intf.ServerInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean for showing server information.
 */
@ManagedBean(name = "extensionInterfaceBean")
@ViewScoped
public class ExtensionInterfaceBean implements Serializable {

    private static final long serialVersionUID = -2153894219559699861L;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ExtensionInterfaceBean.class);

    public static final String[] ACCESS_PARAMETERS = new String[] {
            ControllerConfigurationKey.BSS_ORGANIZATION_ID.name(),
            ControllerConfigurationKey.BSS_USER_ID.name(),
            ControllerConfigurationKey.BSS_USER_KEY.name(),
            ControllerConfigurationKey.BSS_USER_PWD.name() };

    private String subscriptionId;
    private String organizationId;
    private String instanceId;

    @PostConstruct
    public void init() {
        String lang = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("lang");
        if (lang != null) {
            FacesContext.getCurrentInstance().getViewRoot()
                    .setLocale(Locale.forLanguageTag(lang));
        }
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = new String(Base64.decodeBase64(subscriptionId));
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = new String(Base64.decodeBase64(organizationId));
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = new String(Base64.decodeBase64(instanceId));
    }

    @Inject
    private InstanceAccess instanceAccess;

    public void setInstanceAccess(InstanceAccess instanceAccess) {
        this.instanceAccess = instanceAccess;
    }

    public List<? extends ServerInformation> getInstanceDetails() {
        try {
            List<? extends ServerInformation> servers = instanceAccess
                    .getServerDetails(instanceId, subscriptionId,
                            organizationId);
            return servers != null ? servers : new ArrayList<>();
        } catch (APPlatformException e) {
            LOGGER.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public String getAccessInfo() {
        try {
            String accessInfo = instanceAccess.getAccessInfo(instanceId,
                    subscriptionId, organizationId);

            return accessInfo != null ? accessInfo : "";

        } catch (APPlatformException e) {
            LOGGER.error(e.getMessage(), e);
            return "";
        }
    }
}
