/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2016/11/11                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.vmware.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.app.v2_0.intf.InstanceAccess;
import org.oscm.app.v2_0.intf.ServerInformation;
import org.oscm.app.vmware.business.Controller;
import org.oscm.app.vmware.business.Server;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareProcessor;
import org.oscm.app.vmware.i18n.Messages;

public class VMwareInstanceAccess implements InstanceAccess {

    /**
     * 
     */
    private static final long serialVersionUID = 1778190026321422269L;

    // Reference to an APPlatformService instance
    private APPlatformService platformService;

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
    public List<? extends ServerInformation> getServerDetails(String instanceId,
            String subscriptionId, String organizationId)
            throws AuthenticationException, ConfigurationException,
            APPlatformException {

        ProvisioningSettings settings = platformService
                .getServiceInstanceDetails(Controller.ID, instanceId,
                        subscriptionId, organizationId);
        VMPropertyHandler ph = new VMPropertyHandler(settings);
        List<Server> servers = new ArrayList<>();
        VMwareProcessor vmp = new VMwareProcessor();
        servers = vmp.getServersDetails(ph);
        return servers;
    }

    @Override
    public String getAccessInfo(String instanceId, String subscriptionId,
            String organizationId) throws AuthenticationException,
            ConfigurationException, APPlatformException {
        ProvisioningSettings settings = platformService
                .getServiceInstanceDetails(Controller.ID, instanceId,
                        subscriptionId, organizationId);

        String accessInfo = "";
        accessInfo = settings.getServiceAccessInfo();
        VMPropertyHandler ph = new VMPropertyHandler(settings);
        if (ph.getVsphereConsolePort() != null
                && !ph.getVsphereConsolePort().isEmpty()) {
            VMwareProcessor vmp = new VMwareProcessor();
            String aceessUrl = vmp.getVmAccessUrl(ph);
            String htmlLink = "<a href=\"" + aceessUrl + "\">Console</a>";
            accessInfo = accessInfo + " " + htmlLink;
        }
        return accessInfo;
    }

    @Override
    public String getMessage(String locale, String key, Object... arguments) {
        return Messages.get(locale, key, arguments);
    }

}
