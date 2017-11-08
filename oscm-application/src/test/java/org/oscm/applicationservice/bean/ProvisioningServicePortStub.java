/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.applicationservice.bean;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.oscm.applicationservice.adapter.ProvisioningServiceAdapter;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.PlatformUser;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceInfo;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.provisioning.data.ServiceParameter;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.data.UserResult;

public class ProvisioningServicePortStub implements ProvisioningServiceAdapter {

    public final static int RC_OK = 0;
    public final static int RC_ERROR = 100;
    public final static int RC_EXCEPTION = 200;
    public final static int RC_NULL = 300;

    private int returnCode = 0;

    private final BaseResult resultOk = new BaseResult();

    private InstanceRequest instanceRequest;

    private String instanceId;

    private String organizationId;

    private String subscriptionId;

    private List<Parameter> parameters;

    private List<PlatformUser> users;

    private boolean throwError;

    private InstanceInfo returnedInstanceInfo = null;

    private User requestingUser;

    private String applicationUserId = null;
    private boolean useApplicationUserId = false;

    private <T extends BaseResult> T setRcAndDesc(T result)
            throws WebServiceException {
        result.setRc(returnCode);
        if (returnCode == RC_OK) {
            result.setDesc("Ok");
        } else if (returnCode == RC_NULL) {
            return null;
        } else if (returnCode == RC_EXCEPTION) {
            throw new WebServiceException("Test");
        } else {
            result.setDesc("Error");
        }
        return result;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public InstanceRequest getInstanceRequest() {
        return instanceRequest;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<PlatformUser> getUsers() {
        return users;
    }

    private void setParameters(List<ServiceParameter> serviceParams) {
        parameters = new ArrayList<>();
        for (ServiceParameter serviceParam : serviceParams) {
            ParameterDefinition paramDef = new ParameterDefinition();
            paramDef.setParameterId(serviceParam.getParameterId());
            Parameter param = new Parameter();
            param.setParameterDefinition(paramDef);
            param.setValue(serviceParam.getValue());
            parameters.add(param);
        }
    }

    private void setUsers(List<User> serviceUsers) {
        users = new ArrayList<>();
        for (User serviceUser : serviceUsers) {
            PlatformUser user = new PlatformUser();
            user.setUserId(serviceUser.getApplicationUserId());
            user.setEmail(serviceUser.getEmail());
            users.add(user);
        }
    }

    @Override
    public BaseResult asyncCreateInstance(InstanceRequest request,
            User requestingUser) {
        instanceRequest = request;
        checkThrowError();
        setParameters(request.getParameterValue());
        this.requestingUser = requestingUser;
        return setRcAndDesc(resultOk);
    }

    @Override
    public InstanceResult createInstance(InstanceRequest request,
            User requestingUser) {
        checkThrowError();
        InstanceResult result = new InstanceResult();
        if (request.getOrganizationId() != null) {
            if (returnedInstanceInfo == null) {
                InstanceInfo info = new InstanceInfo();
                info.setAccessInfo(request.getDefaultLocale());
                info.setBaseUrl(ApplicationServiceBeanTest.BASE_URL);
                info.setInstanceId(request.getOrganizationId()
                        + request.getSubscriptionId());
                info.setLoginPath(request.getLoginUrl());
                result.setInstance(info);
            } else {
                result.setInstance(returnedInstanceInfo);
            }
        }
        setParameters(request.getParameterValue());
        this.requestingUser = requestingUser;

        return setRcAndDesc(result);
    }

    @Override
    public UserResult createUsers(String instanceId, List<User> users,
            User requestingUser) {
        this.instanceId = instanceId;
        checkThrowError();
        UserResult result = new UserResult();
        for (User user : users) {
            user.setApplicationUserId(user.getUserId());
            if (useApplicationUserId) {
                user.setApplicationUserId(applicationUserId);
            } else {
                user.setUserLastName(user.getUserId());
            }
        }
        result.setUsers(users);
        this.requestingUser = requestingUser;

        return setRcAndDesc(result);
    }

    @Override
    public BaseResult deleteInstance(String instanceId, String organizationId,
            String subscriptionId, User requestingUser) {
        this.instanceId = instanceId;
        this.organizationId = organizationId;
        this.subscriptionId = subscriptionId;
        checkThrowError();
        this.requestingUser = requestingUser;
        return setRcAndDesc(resultOk);
    }

    @Override
    public BaseResult deleteUsers(String instanceId, List<User> users,
            User requestingUser) {
        this.instanceId = instanceId;
        checkThrowError();
        setUsers(users);
        this.requestingUser = requestingUser;
        return setRcAndDesc(resultOk);
    }

    @Override
    public String sendPing(String arg) {
        checkThrowError();
        return arg;
    }

    public String getVersion() {
        return "1.0";
    }

    @Override
    public BaseResult modifySubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        this.instanceId = instanceId;
        checkThrowError();
        setParameters(parameterValues);
        this.requestingUser = requestingUser;
        return setRcAndDesc(resultOk);
    }

    @Override
    public BaseResult updateUsers(String instanceId, List<User> users,
            User requestingUser) {
        this.instanceId = instanceId;
        checkThrowError();
        setUsers(users);
        this.requestingUser = requestingUser;
        return setRcAndDesc(resultOk);
    }

    @Override
    public URL getLocalWSDL() {

        return null;
    }

    @Override
    public void setProvisioningService(Object provServ) {

    }

    public void setThrowError(boolean throwError) {
        this.throwError = throwError;
    }

    private void checkThrowError() {
        if (throwError) {
            throw new Error("error");
        }
    }

    @Override
    public BaseResult activateInstance(String instanceId, User requestingUser) {
        this.instanceId = instanceId;
        checkThrowError();
        this.requestingUser = requestingUser;
        return setRcAndDesc(resultOk);
    }

    public User getRequestingUser() {
        return requestingUser;
    }

    @Override
    public BaseResult deactivateInstance(String instanceId,
            User requestingUser) {
        this.instanceId = instanceId;
        checkThrowError();
        this.requestingUser = requestingUser;
        return setRcAndDesc(resultOk);
    }

    public void setReturnedInstanceInfo(InstanceInfo returnedInstanceInfo) {
        this.returnedInstanceInfo = returnedInstanceInfo;
    }

    public void setApplicationUserId(String applicationUserId) {
        useApplicationUserId = true;
        this.applicationUserId = applicationUserId;
    }

    @Override
    public BaseResult asyncModifySubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        this.instanceId = instanceId;
        checkThrowError();
        setParameters(parameterValues);
        this.requestingUser = requestingUser;
        return setRcAndDesc(resultOk);
    }

    @Override
    public BaseResult asyncUpgradeSubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        this.instanceId = instanceId;
        checkThrowError();
        setParameters(parameterValues);
        this.requestingUser = requestingUser;
        return setRcAndDesc(resultOk);
    }

    @Override
    public BaseResult upgradeSubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        this.instanceId = instanceId;
        checkThrowError();
        setParameters(parameterValues);
        this.requestingUser = requestingUser;
        return setRcAndDesc(resultOk);
    }

    /**
     * @return the organizationId
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * @return the subscriptionId
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public BaseResult saveAttributes(String organizationId,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        return setRcAndDesc(resultOk);
    }

}
