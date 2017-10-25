/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 06.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package internal;

import java.util.List;

import javax.jws.WebService;

import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.provisioning.data.ServiceParameter;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.data.UserResult;
import org.oscm.provisioning.intf.ProvisioningService;

/**
 * This is a stub implementation of the {@link ProvisioningService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Mike J&auml;ger
 * 
 */
@WebService(serviceName = "ProvisioningService", targetNamespace = "http://oscm.org/xsd", portName = "ProvisioningServicePort", endpointInterface = "org.oscm.provisioning.intf.ProvisioningService")
public class ProvisioningServiceImpl implements ProvisioningService {

    @Override
    public BaseResult asyncCreateInstance(InstanceRequest request,
            User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InstanceResult createInstance(InstanceRequest request,
            User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserResult createUsers(String instanceId, List<User> users,
            User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseResult deleteInstance(String instanceId, String organizationId,
            String subscriptionId, User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseResult deleteUsers(String instanceId, List<User> users,
            User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sendPing(String arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseResult modifySubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseResult updateUsers(String instanceId, List<User> users,
            User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseResult activateInstance(String instanceId, User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseResult deactivateInstance(String instanceId,
            User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseResult asyncModifySubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseResult asyncUpgradeSubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseResult upgradeSubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseResult saveAttributes(String organizationId,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        throw new UnsupportedOperationException();
    }

}
