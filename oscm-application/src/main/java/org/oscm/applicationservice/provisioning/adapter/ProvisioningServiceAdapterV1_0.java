/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.03.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.provisioning.adapter;

import java.net.URL;
import java.util.List;

import org.oscm.applicationservice.adapter.ProvisioningServiceAdapter;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.provisioning.data.ServiceParameter;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.data.UserResult;
import org.oscm.provisioning.intf.ProvisioningService;

/**
 * @author goebel
 * 
 */
public class ProvisioningServiceAdapterV1_0
        implements ProvisioningServiceAdapter {

    private ProvisioningService service;

    @Override
    public URL getLocalWSDL() {
        return this.getClass()
                .getResource("/wsdl/provisioning/ProvisioningService.wsdl");
    }

    @Override
    public void setProvisioningService(Object provServ) {
        this.service = ProvisioningService.class.cast(provServ);
    }

    @Override
    public BaseResult asyncCreateInstance(InstanceRequest request,
            User requestingUser) {
        return service.asyncCreateInstance(request, requestingUser);
    }

    @Override
    public InstanceResult createInstance(InstanceRequest request,
            User requestingUser) {
        return service.createInstance(request, requestingUser);
    }

    @Override
    public UserResult createUsers(String instanceId, List<User> users,
            User requestingUser) {
        return service.createUsers(instanceId, users, requestingUser);
    }

    @Override
    public BaseResult deleteInstance(String instanceId, String organizationId,
            String subscriptionId, User requestingUser) {
        return service.deleteInstance(instanceId, organizationId,
                subscriptionId, requestingUser);
    }

    @Override
    public BaseResult deleteUsers(String instanceId, List<User> users,
            User requestingUser) {
        return service.deleteUsers(instanceId, users, requestingUser);
    }

    @Override
    public String sendPing(String arg) {
        return service.sendPing(arg);
    }

    @Override
    public BaseResult modifySubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        return service.modifySubscription(instanceId, subscriptionId,
                referenceId, parameterValues, attributeValues, requestingUser);
    }

    @Override
    public BaseResult updateUsers(String instanceId, List<User> users,
            User requestingUser) {
        return service.updateUsers(instanceId, users, requestingUser);
    }

    @Override
    public BaseResult activateInstance(String instanceId, User requestingUser) {
        return service.activateInstance(instanceId, requestingUser);
    }

    @Override
    public BaseResult deactivateInstance(String instanceId,
            User requestingUser) {
        return service.deactivateInstance(instanceId, requestingUser);
    }

    @Override
    public BaseResult asyncModifySubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        return service.asyncModifySubscription(instanceId, subscriptionId,
                referenceId, parameterValues, attributeValues, requestingUser);
    }

    @Override
    public BaseResult asyncUpgradeSubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        return service.asyncUpgradeSubscription(instanceId, subscriptionId,
                referenceId, parameterValues, attributeValues, requestingUser);
    }

    @Override
    public BaseResult upgradeSubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        return service.upgradeSubscription(instanceId, subscriptionId,
                referenceId, parameterValues, attributeValues, requestingUser);
    }

    @Override
    public BaseResult saveAttributes(String organizationId,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        BaseResult br = new BaseResult();
        br.setRc(0);
        br.setDesc("saveAttributes");
        return br;
    }

}
