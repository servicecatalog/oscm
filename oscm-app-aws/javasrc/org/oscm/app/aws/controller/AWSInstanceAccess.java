/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2016/11/11                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.aws.controller;

import java.util.List;

import javax.annotation.PostConstruct;

import org.oscm.app.aws.data.Server;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.app.v2_0.intf.InstanceAccess;
import org.oscm.app.v2_0.intf.ServerInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tateiwamext
 * 
 */
public class AWSInstanceAccess implements InstanceAccess {

    // Reference to an APPlatformService instance
    private APPlatformService platformService;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AWSInstanceAccess.class);
    private static final String AWS_INSTANCE_ID_KEY = "AWS_INSTANCE_ID";

    /**
     * 
     */
    private static final long serialVersionUID = -4847055337273230537L;

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
            throws APPlatformException {
        // TODO Replace the method which don't need authentication after
        // implementation.
        ProvisioningSettings settings = platformService
                .getServiceInstanceDetails(AWSController.ID, instanceId,
                        subscriptionId, organizationId);
        PropertyHandler ph = new PropertyHandler(settings);
        List<Server> servers;
        servers = new EC2Processor(ph, getAwsInstanceId(settings))
                .getServerDetails();
        return servers;
    }

    @Override
    public String getAccessInfo(String instanceId, String subscriptionId,
            String organizationId) throws APPlatformException {
        ProvisioningSettings settings = platformService
                .getServiceInstanceDetails(AWSController.ID, instanceId,
                        subscriptionId, organizationId);
        return settings.getServiceAccessInfo();
    }

    @Override
    public String getMessage(String locale, String key, Object... arguments) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieves AWS instance ID corresponding to the instanceId from APP
     * database.
     *
     * @param settings
     * @return
     */
    private String getAwsInstanceId(ProvisioningSettings settings) {
        return settings.getParameters().get(AWS_INSTANCE_ID_KEY).getValue();
    }

}
