/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.integrationtests.mockproduct;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.oscm.integrationtests.mockproduct.RequestLogEntry.RequestDirection;
import org.oscm.notification.intf.NotificationService;
import org.oscm.notification.vo.VONotification;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationPaymentConfiguration;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServicePaymentConfiguration;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTriggerProcess;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * This is a stub implementation of the {@link NotificationService}
 * 
 * @author pock
 */
@WebService(serviceName = "NotificationService", targetNamespace = "http://oscm.org/xsd", portName = "StubServicePort", endpointInterface = "org.oscm.notification.intf.NotificationService", wsdlLocation = "WEB-INF/wsdl/NotificationService.wsdl")
public class NotificationServiceBean implements NotificationService {

    @Resource
    private WebServiceContext context;

    private String getMethodName() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        (new Throwable()).printStackTrace(pw);
        pw.flush();
        String stackTrace = baos.toString();
        pw.close();

        StringTokenizer tok = new StringTokenizer(stackTrace, "\n");
        String l = tok.nextToken(); // 'java.lang.Throwable'
        l = tok.nextToken(); // 'at ...getCurrentMethodName'
        l = tok.nextToken(); // 'at ...get log method name'
        l = tok.nextToken(); // 'at ...<caller to getCurrentRoutine>'

        // Parse line
        tok = new StringTokenizer(l.trim(), " <(");
        String str = tok.nextToken(); // 'at'
        str = tok.nextToken(); // '...<caller to getCurrentRoutine>'
        int idx = str.lastIndexOf(".");
        if (idx >= 0) {
            str = str.substring(idx + 1);
        }
        return str;
    }

    private RequestLogEntry createLogEntry() {
        final ServletContext servletContext = (ServletContext) context
                .getMessageContext().get(MessageContext.SERVLET_CONTEXT);
        final RequestLog log = (RequestLog) servletContext
                .getAttribute(InitServlet.REQUESTLOG);
        final RequestLogEntry entry = log.createEntry(
                NotificationService.class.getSimpleName() + "."
                        + getMethodName(), RequestDirection.INBOUND);
        ServletRequest request = (ServletRequest) context.getMessageContext()
                .get(MessageContext.SERVLET_REQUEST);
        entry.setHost(request.getRemoteHost());
        return entry;
    }

    private void addQuickLinks(final RequestLogEntry entry,
            VOTriggerProcess triggerProcess) {
        QuickLink link;
        link = entry.addQuickLink("approve", "TriggerService.approveAction");
        link.addParameter("key", String.valueOf(triggerProcess.getKey()));
        link = entry.addQuickLink("reject", "TriggerService.rejectAction");
        link.addParameter("key", String.valueOf(triggerProcess.getKey()));
    }

    @Override
    public void billingPerformed(String xmlBillingData) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("xmlBillingData", xmlBillingData);
    }

    @Override
    public void onActivateProduct(VOTriggerProcess triggerProcess,
            VOService product) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("product", product);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onAddRevokeUser(VOTriggerProcess triggerProcess,
            String subscriptionId, List<VOUsageLicense> usersToBeAdded,
            List<VOUser> usersToBeRevoked) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("subscriptionId", subscriptionId);
        entry.addParameter("usersToBeAdded", usersToBeAdded);
        entry.addParameter("usersToBeRevoked", usersToBeRevoked);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onAddSupplier(VOTriggerProcess triggerProcess, String supplierId) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("supplierId", supplierId);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onDeactivateProduct(VOTriggerProcess triggerProcess,
            VOService product) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("product", product);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onModifySubscription(VOTriggerProcess triggerProcess,
            VOSubscription subscription, List<VOParameter> modifiedParameters) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("subscription", subscription);
        entry.addParameter("modifiedParameters", modifiedParameters);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onRegisterCustomer(VOTriggerProcess triggerProcess,
            VOOrganization organization, VOUserDetails user,
            Properties organizationProperties) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("organization", organization);
        entry.addParameter("user", user);
        entry.addParameter("organizationProperties", organizationProperties);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onRemoveSupplier(VOTriggerProcess triggerProcess,
            String supplierId) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("supplierId", supplierId);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onSaveDefaultPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            Set<VOPaymentType> defaultConfiguration) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("defaultConfiguration", defaultConfiguration);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onSaveCustomerPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            VOOrganizationPaymentConfiguration customerConfiguration) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("customerConfiguration", customerConfiguration);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onSubscribeToProduct(VOTriggerProcess triggerProcess,
            VOSubscription subscription, VOService product,
            List<VOUsageLicense> users) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("subscription", subscription);
        entry.addParameter("users", users);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onUnsubscribeFromProduct(VOTriggerProcess triggerProcess,
            String subId) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("subId", subId);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onUpgradeSubscription(VOTriggerProcess triggerProcess,
            VOSubscription current, VOService newProduct) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("current", current);
        entry.addParameter("newProduct", newProduct);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onSaveServicePaymentConfiguration(
            VOTriggerProcess triggerProcess,
            VOServicePaymentConfiguration serviceConfiguration) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("serviceConfiguration", serviceConfiguration);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onSaveServiceDefaultPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            Set<VOPaymentType> defaultConfiguration) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("defaultConfiguration", defaultConfiguration);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onSubscriptionCreation(VOTriggerProcess triggerProcess,
            VOService service, List<VOUsageLicense> users,
            VONotification notification) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("service", service);
        entry.addParameter("users", users);
        entry.addParameter("notification", notification);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onSubscriptionModification(VOTriggerProcess triggerProcess,
            List<VOParameter> parameters, VONotification notification) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("parameters", parameters);
        entry.addParameter("notification", notification);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onSubscriptionTermination(VOTriggerProcess triggerProcess,
            VONotification notification) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("notification", notification);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onRegisterUserInOwnOrganization(
            VOTriggerProcess triggerProcess, VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("triggerProcess", triggerProcess);
        entry.addParameter("user", user);
        entry.addParameter("roles", roles);
        entry.addParameter("marketplaceId", marketplaceId);
        addQuickLinks(entry, triggerProcess);
    }

    @Override
    public void onCancelAction(@WebParam(name = "actionKey") long actionKey) {
        final RequestLogEntry entry = createLogEntry();
        entry.addParameter("actionKey", new Long(actionKey));
    }
}
