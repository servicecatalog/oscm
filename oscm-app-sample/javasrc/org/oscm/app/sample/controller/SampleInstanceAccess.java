/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 25, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.sample.controller;

import java.util.ArrayList;
import java.util.Arrays;
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

/**
 * Data provider for custom tab.
 * 
 * @author miethaner
 */
public class SampleInstanceAccess implements InstanceAccess {

    private static final long serialVersionUID = 4849529207608842289L;

    private APPlatformService platformService;

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
                .getServiceInstanceDetails(SampleController.ID, instanceId,
                        subscriptionId, organizationId);
        PropertyHandler ph = new PropertyHandler(settings);

        List<Server> servers = new ArrayList<>();
        Server s = new Server();
        s.setId(instanceId);
        s.setName(ph.getMessage());
        s.setPrivateIP(Arrays.asList("127.0.0.1"));
        s.setPublicIP(Arrays.asList("127.0.0.1"));
        s.setStatus(ph.getState().name());
        s.setType(ph.getEMail());
        servers.add(s);

        return servers;
    }

    @Override
    public String getAccessInfo(String instanceId, String subscriptionId,
            String organizationId) throws AuthenticationException,
            ConfigurationException, APPlatformException {

        ProvisioningSettings settings = platformService
                .getServiceInstanceDetails(SampleController.ID, instanceId,
                        subscriptionId, organizationId);

        return settings.getServiceAccessInfo();
    }

    @Override
    public String getMessage(String locale, String key, Object... arguments) {
        return null;
    }

}
