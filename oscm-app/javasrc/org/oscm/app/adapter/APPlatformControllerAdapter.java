/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 29.10.15 10:01
 *
 ******************************************************************************/

package org.oscm.app.adapter;

import java.util.List;
import java.util.Properties;

import org.oscm.app.v2_0.data.ControllerSettings;
import org.oscm.app.v2_0.data.InstanceDescription;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.InstanceStatusUsers;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.OperationParameter;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.exceptions.ServiceNotReachableException;
import org.oscm.app.v2_0.intf.APPlatformController;

public class APPlatformControllerAdapter implements APPlatformController {

    private APPlatformController delegate;

    private APPlatformControllerAdapter() {
    }

    public APPlatformControllerAdapter(APPlatformController delegate) {
        this.delegate = delegate;
    }

    public Object getDelegate() {
        return delegate;
    }

    public void setDelegate(APPlatformController delegate) {
        this.delegate = delegate;
    }

    @Override
    public InstanceDescription createInstance(ProvisioningSettings settings)
            throws APPlatformException {
        return delegate.createInstance(settings);
    }

    @Override
    public InstanceStatus modifyInstance(String instanceId,
            ProvisioningSettings currentSettings,
            ProvisioningSettings newSettings) throws APPlatformException {
        return delegate
                .modifyInstance(instanceId, currentSettings, newSettings);
    }

    @Override
    public InstanceStatus deleteInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {
        return delegate.deleteInstance(instanceId, settings);
    }

    @Override
    public InstanceStatus getInstanceStatus(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {
        return delegate.getInstanceStatus(instanceId, settings);
    }

    @Override
    public InstanceStatus notifyInstance(String instanceId,
            ProvisioningSettings settings, Properties properties)
            throws APPlatformException {
        return delegate.notifyInstance(instanceId, settings, properties);
    }

    @Override
    public InstanceStatus activateInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {
        return delegate.activateInstance(instanceId, settings);
    }

    @Override
    public InstanceStatus deactivateInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {
        return delegate.deactivateInstance(instanceId, settings);
    }

    @Override
    public InstanceStatusUsers createUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {
        return delegate.createUsers(instanceId, settings, users);
    }

    @Override
    public InstanceStatus deleteUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {
        return delegate.deleteUsers(instanceId, settings, users);
    }

    @Override
    public InstanceStatus updateUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {
        return delegate.updateUsers(instanceId, settings, users);
    }

    @Override
    public List<LocalizedText> getControllerStatus(ControllerSettings settings)
            throws APPlatformException {
        return delegate.getControllerStatus(settings);
    }

    @Override
    public List<OperationParameter> getOperationParameters(String userId,
            String instanceId, String operationId, ProvisioningSettings settings)
            throws APPlatformException {
        return delegate.getOperationParameters(userId, instanceId, operationId,
                settings);
    }

    @Override
    public InstanceStatus executeServiceOperation(String userId,
            String instanceId, String transactionId, String operationId,
            List<OperationParameter> parameters, ProvisioningSettings settings)
            throws APPlatformException {
        return delegate.executeServiceOperation(userId, instanceId,
                transactionId, operationId, parameters, settings);
    }

    @Override
    public void setControllerSettings(ControllerSettings settings) {
        delegate.setControllerSettings(settings);
    }

    @Override
    public int getServersNumber(String instanceId, String subscriptionId, String organizationId)
        throws APPlatformException {
        return delegate.getServersNumber(instanceId,subscriptionId, organizationId);
    }
    
    @Override
    public boolean gatherUsageData(String controllerId,
            String instanceId, String startTime, String endTime,
            ProvisioningSettings settings) throws APPlatformException {
        return delegate.gatherUsageData(controllerId, instanceId, startTime, endTime, settings);
    }

    @Override
    public boolean ping(String controllerId) throws ServiceNotReachableException {
        return delegate.ping(controllerId);
    }

    @Override
    public boolean canPing() throws ConfigurationException {
        return delegate.canPing();
    }
}
