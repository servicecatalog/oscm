/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.integrationtests.mockproduct;

import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.oscm.integrationtests.mockproduct.RequestLogEntry.RequestDirection;
import org.oscm.integrationtests.mockproduct.i18n.Messages;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceInfo;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.provisioning.data.ServiceParameter;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.data.UserResult;
import org.oscm.provisioning.intf.ProvisioningService;

/**
 * This is a stub implementation of the {@link ProvisioningService}
 * 
 * @author pock
 */
@WebService(serviceName = "ProvisioningService", targetNamespace = "http://oscm.org/xsd", portName =
    "ProvisioningServicePort", endpointInterface = "org.oscm.provisioning.intf.ProvisioningService", wsdlLocation =
    "WEB-INF/wsdl/ProvisioningService.wsdl")
public class ProvisioningServiceBean implements ProvisioningService {

    @Resource
    private WebServiceContext context;

    private static final int RETURN_CODE_OK = 0;

    private <T extends BaseResult> T setOk(T result) {
        result.setRc(RETURN_CODE_OK);
        result.setDesc("Ok");
        return result;
    }

    private <T extends BaseResult> T setOk(T result, String message) {
        result.setRc(RETURN_CODE_OK);
        result.setDesc(message);
        return result;
    }

    private BaseResult getBaseResultOk() {
        return setOk(new BaseResult());
    }

    private BaseResult getBaseResultOk(String message) {
        return setOk(new BaseResult(), message);
    }

    private RequestLogEntry createLogEntry(String title) {
        final ServletContext servletContext = (ServletContext) context
                .getMessageContext().get(MessageContext.SERVLET_CONTEXT);
        final RequestLog log = (RequestLog) servletContext
                .getAttribute(InitServlet.REQUESTLOG);
        final RequestLogEntry entry = log.createEntry(
                ProvisioningService.class.getSimpleName() + "." + title,
                RequestDirection.INBOUND);
        ServletRequest request = (ServletRequest) context.getMessageContext()
                .get(MessageContext.SERVLET_REQUEST);
        entry.setHost(request.getRemoteHost());
        return entry;
    }

    @Override
    public BaseResult asyncCreateInstance(InstanceRequest request,
            User requestingUser) {
        final RequestLogEntry entry = createLogEntry("asyncCreateInstance");
        entry.addParameter("request", request);
        entry.addParameter("requestingUser", requestingUser);

        final QuickLink link1 = entry.addQuickLink("abort",
                "SubscriptionService.abortAsyncSubscription");
        link1.addParameter("subscriptionId", request.getSubscriptionId());
        link1.addParameter("organizationId", request.getOrganizationId());

        final QuickLink link2 = entry.addQuickLink("progress",
                "SubscriptionService.updateAsyncSubscriptionProgress");
        link2.addParameter("subscriptionId", request.getSubscriptionId());
        link2.addParameter("organizationId", request.getOrganizationId());

        final QuickLink link3 = entry.addQuickLink("complete",
                "SubscriptionService.completeAsyncSubscription");
        link3.addParameter("subscriptionId", request.getSubscriptionId());
        link3.addParameter("organizationId", request.getOrganizationId());

        String message = Messages.get(requestingUser.getLocale(),
                "info.subscription.async.created");
        return getBaseResultOk(message);
    }

    @Override
    public InstanceResult createInstance(InstanceRequest request,
            User requestingUser) {
        final RequestLogEntry entry = createLogEntry("createInstance");
        entry.addParameter("request", request);
        entry.addParameter("requestingUser", requestingUser);

        InstanceInfo instance = new InstanceInfo();
        instance.setInstanceId(request.getSubscriptionId());
        instance.setAccessInfo(null);

        InstanceResult result = new InstanceResult();
        result.setInstance(instance);

        String message = Messages.get(requestingUser.getLocale(),
                "info.subscription.created");
        setOk(result, message);

        return result;
    }

    @Override
    public UserResult createUsers(String instanceId, List<User> users,
            User requestingUser) {
        final RequestLogEntry entry = createLogEntry("createUsers");
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("users", users);
        entry.addParameter("requestingUser", requestingUser);

        UserResult result = new UserResult();
        for (User user : users) {
            user.setApplicationUserId(user.getUserId());
        }
        result.setUsers(users);
        setOk(result);

        return result;
    }

    @Override
    public BaseResult deleteInstance(String instanceId, String organizationId,
            String subscriptionId, User requestingUser) {
        final RequestLogEntry entry = createLogEntry("deleteInstance");
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("organizationId", organizationId);
        entry.addParameter("subscriptionId", subscriptionId);
        entry.addParameter("requestingUser", requestingUser);

        return getBaseResultOk();
    }

    @Override
    public BaseResult deleteUsers(String instanceId, List<User> users,
            User requestingUser) {
        final RequestLogEntry entry = createLogEntry("deleteUsers");
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("users", users);
        entry.addParameter("requestingUser", requestingUser);

        return getBaseResultOk();
    }

    @Override
    public String sendPing(String arg) {
        final RequestLogEntry entry = createLogEntry("sendPing");
        entry.addParameter("arg", arg);

        return arg;
    }

    @Override
    public BaseResult modifySubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        final RequestLogEntry entry = createLogEntry("modifySubscription");
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("subscriptionId", subscriptionId);
        entry.addParameter("referenceId", referenceId);
        entry.addParameter("parameterValues", parameterValues);
        entry.addParameter("attributeValues", attributeValues);
        entry.addParameter("requestingUser", requestingUser);

        return getBaseResultOk();
    }

    @Override
    public BaseResult updateUsers(String instanceId, List<User> users,
            User requestingUser) {
        final RequestLogEntry entry = createLogEntry("updateUsers");
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("users", users);
        entry.addParameter("requestingUser", requestingUser);

        return getBaseResultOk();
    }

    @Override
    public BaseResult activateInstance(String instanceId, User requestingUser) {
        final RequestLogEntry entry = createLogEntry("activateInstance");
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("requestingUser", requestingUser);
        return getBaseResultOk();
    }

    @Override
    public BaseResult deactivateInstance(String instanceId,
            User requestingUser) {
        final RequestLogEntry entry = createLogEntry("deactivateInstance");
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("requestingUser", requestingUser);
        return getBaseResultOk();
    }

    @Override
    public BaseResult asyncModifySubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        final RequestLogEntry entry = createLogEntry("asyncModifySubscription");
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("subscriptionId", subscriptionId);
        entry.addParameter("referenceId", referenceId);
        entry.addParameter("parameterValues", parameterValues);
        entry.addParameter("attributeValues", attributeValues);
        entry.addParameter("requestingUser", requestingUser);
        final QuickLink link1 = entry.addQuickLink("abort",
                "SubscriptionService.abortAsyncModifySubscription");
        link1.addParameter("subscriptionId", subscriptionId);
        link1.addParameter("instanceId", instanceId);

        final QuickLink link2 = entry.addQuickLink("complete",
                "SubscriptionService.completeAsyncModifySubscription");
        link2.addParameter("subscriptionId", subscriptionId);
        link2.addParameter("instanceId", instanceId);
        return getBaseResultOk();
    }

    @Override
    public BaseResult asyncUpgradeSubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        final RequestLogEntry entry = createLogEntry(
                "asyncUpgradeSubscription");
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("subscriptionId", subscriptionId);
        entry.addParameter("referenceId", referenceId);
        entry.addParameter("parameterValues", parameterValues);
        entry.addParameter("attributeValues", attributeValues);
        entry.addParameter("requestingUser", requestingUser);
        final QuickLink link1 = entry.addQuickLink("abort",
                "SubscriptionService.abortAsyncUpgradeSubscription");
        link1.addParameter("subscriptionId", subscriptionId);
        link1.addParameter("instanceId", instanceId);

        final QuickLink link2 = entry.addQuickLink("complete",
                "SubscriptionService.completeAsyncUpgradeSubscription");
        link2.addParameter("subscriptionId", subscriptionId);
        link2.addParameter("instanceId", instanceId);
        return getBaseResultOk();
    }

    @Override
    public BaseResult upgradeSubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        final RequestLogEntry entry = createLogEntry("upgradeSubscription");
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("subscriptionId", subscriptionId);
        entry.addParameter("referenceId", referenceId);
        entry.addParameter("parameterValues", parameterValues);
        entry.addParameter("attributeValues", attributeValues);
        entry.addParameter("requestingUser", requestingUser);
        return getBaseResultOk();
    }

    @Override
    public BaseResult saveAttributes(String organizationId,
            List<ServiceAttribute> attributeValues, User requestingUser) {
        final RequestLogEntry entry = createLogEntry("saveAttributes");
        entry.addParameter("organizationId", organizationId);
        entry.addParameter("attributeValues", attributeValues);
        entry.addParameter("requestingUser", requestingUser);
        return getBaseResultOk();
    }

}
