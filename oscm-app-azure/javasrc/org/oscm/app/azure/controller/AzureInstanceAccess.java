package org.oscm.app.azure.controller;

import org.oscm.app.azure.i18n.Messages;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.app.v2_0.intf.InstanceAccess;
import org.oscm.app.v2_0.intf.ServerInformation;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

public class AzureInstanceAccess implements InstanceAccess {
    // Reference to an APPlatformService instance
    private APPlatformService platformService;

    private static final long serialVersionUID = 120266701249164759L;

    /**
     * Retrieves an <code>APPlatformService</code> instance.
     * <p>
     * The <code>APPlatformService</code> provides helper methods by which the
     * service controller can access common APP utilities, for example, send
     * emails or lock application instances.
     */
    @PostConstruct
    public void initialize() {
        platformService = APPlatformServiceFactory.getInstance();
    }

    @Override
    public List<? extends ServerInformation> getServerDetails(String instanceId, String subscriptionId, String organizationId) throws AuthenticationException, ConfigurationException, APPlatformException {
        return Collections.emptyList();
    }

    @Override
    public String getAccessInfo(String instanceId, String subscriptionId, String organizationId) throws AuthenticationException, ConfigurationException, APPlatformException {
        ProvisioningSettings settings = platformService.getServiceInstanceDetails(
                AzureController.ID, instanceId, subscriptionId, organizationId);
        return settings.getServiceAccessInfo();
    }

    @Override
    public String getMessage(String locale, String key, Object... arguments) {
        return Messages.get(locale, key, arguments);
    }
}
