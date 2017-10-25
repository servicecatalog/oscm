/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 27, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.triggerprocess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.triggerservice.local.TriggerServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.subscriptions.POSubscription;
import org.oscm.internal.subscriptions.POSubscriptionForList;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTriggerProcess;

/**
 * @author zankov
 * 
 */
@Stateless
@Remote(TriggerProcessesService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class TriggerProcessesServiceBean implements TriggerProcessesService {

    @EJB
    TriggerService triggerService;

    @EJB
    TriggerServiceLocal triggerServiceLocal;

    @EJB
    DataService dm;

    @Override
    public Response getAllWaitingForApprovalSubscriptions() {
        PlatformUser currentUser = dm.getCurrentUser();
        List<VOTriggerProcess> allProcesses = triggerService
                .getAllActionsForOrganization();
        List<TriggerProcessStatus> statuses = Arrays
                .asList(TriggerProcessStatus.WAITING_FOR_APPROVAL);
        List<VOTriggerProcess> filteredProcesses = filterByTriggerProcessStatusAndType(
                allProcesses, statuses, TriggerType.SUBSCRIBE_TO_SERVICE);

        List<POSubscriptionForList> triggerSubscriptions = new ArrayList<POSubscriptionForList>();
        if (currentUser.isOrganizationAdmin()) {
            triggerSubscriptions = toPOSubscriptions(filteredProcesses);
        } else if (currentUser.hasRole(UserRoleType.SUBSCRIPTION_MANAGER)) {
            filteredProcesses = filterByCurrentUser(filteredProcesses);
            triggerSubscriptions = toPOSubscriptions(filteredProcesses);
        }

        Response response = new Response();
        response.getResults().add(triggerSubscriptions);
        return response;
    }

    private List<POSubscriptionForList> toPOSubscriptions(
            List<VOTriggerProcess> filteredProcesses) {
        List<POSubscriptionForList> result = new ArrayList<POSubscriptionForList>();
        for (VOTriggerProcess triggerProcess : filteredProcesses) {
            POSubscriptionForList subscription = toPOSubscriptionForList(triggerProcess);
            result.add(subscription);
        }

        return result;
    }

    POSubscriptionForList toPOSubscriptionForList(
            VOTriggerProcess triggerProcess) {
        POSubscriptionForList po = new POSubscriptionForList();

        appendSubscriptionAttributes(po, triggerProcess.getSubscription());

        appendServiceAttributes(po, triggerProcess.getService());

        TriggerProcessStatus status = triggerProcess.getStatus();
        po.setStatusWaitingForApproval(status == TriggerProcessStatus.WAITING_FOR_APPROVAL);

        po.setStatusTextKey(TriggerProcessStatus.class.getSimpleName() + "." + status.name());

        po.setStatusText(status.name().toLowerCase());
        po.setServiceName(triggerProcess.getService().getNameToDisplay());

        // Auto assignment of user. This is necessary because the
        // subscription is still not created and user assignment is still
        // not executed.
        if (triggerProcess.getService().getAutoAssignUserEnabled()
                .booleanValue()) {
            po.setNumberOfAssignedUsers(1);
        }

        return po;
    }

    private void appendServiceAttributes(POSubscriptionForList po,
            VOService service) {
        po.setServiceKey(service.getKey());
        po.setSupplierName(service.getSellerName());
    }

    private void appendSubscriptionAttributes(POSubscriptionForList po,
            VOSubscription subscription) {
        po.setSubscriptionId(subscription.getSubscriptionId());
    }

    private List<VOTriggerProcess> filterByTriggerProcessStatusAndType(
            List<VOTriggerProcess> allProcesses,
            List<TriggerProcessStatus> statuses, TriggerType type) {
        List<VOTriggerProcess> result = new ArrayList<VOTriggerProcess>();

        for (VOTriggerProcess currProcess : allProcesses) {
            if (checkTriggerProcessStatus(currProcess, statuses)
                    && checkTriggerProcessType(currProcess, type)) {
                result.add(currProcess);
            }
        }

        return result;
    }

    private boolean checkTriggerProcessStatus(VOTriggerProcess currProcess,
            List<TriggerProcessStatus> statuses) {
        for (TriggerProcessStatus status : statuses) {
            if (currProcess.getStatus() == status) {
                return true;
            }
        }
        return false;
    }

    private boolean checkTriggerProcessType(VOTriggerProcess currProcess,
            TriggerType type) {
        if (null == type) {
            return true;
        }
        if (currProcess.getTriggerDefinition().getType().equals(type)) {
            return true;
        }
        return false;
    }

    @Override
    public Response getMyWaitingForApprovalSubscriptions() {
        List<VOTriggerProcess> triggerProcesses = triggerService
                .getAllActionsForOrganization();

        List<TriggerProcessStatus> statuses = Arrays
                .asList(TriggerProcessStatus.WAITING_FOR_APPROVAL);
        List<VOTriggerProcess> filteredTriggerProcesses = filterByTriggerProcessStatusAndType(
                triggerProcesses, statuses, TriggerType.SUBSCRIBE_TO_SERVICE);

        filteredTriggerProcesses = filterByCurrentUser(filteredTriggerProcesses);

        filteredTriggerProcesses = filterByAutoAssignment(filteredTriggerProcesses);

        List<POSubscription> result = toPOSubscription(filteredTriggerProcesses);

        Response response = new Response();
        response.getResults().add(result);
        return response;
    }

    @Override
    public Response getAllWaitingForApprovalTriggerProcessesBySubscriptionId(
            String subscriptionId) {
        List<VOTriggerProcess> allTriggerProcesses = triggerServiceLocal
                .getAllActionsForSubscription(subscriptionId);

        List<TriggerProcessStatus> statuses = new ArrayList<TriggerProcessStatus>();
        statuses.add(TriggerProcessStatus.INITIAL);
        statuses.add(TriggerProcessStatus.WAITING_FOR_APPROVAL);
        List<VOTriggerProcess> triggerProcesses = filterByTriggerProcessStatusAndType(
                allTriggerProcesses, statuses, null);

        Response response = new Response();
        response.getResults().add(triggerProcesses);
        return response;
    }

    List<POSubscription> toPOSubscription(
            List<VOTriggerProcess> filteredTriggerProcesses) {
        List<POSubscription> result = new ArrayList<POSubscription>();
        for (VOTriggerProcess triggerProcess : filteredTriggerProcesses) {
            POSubscription poSub = new POSubscription(
                    triggerProcess.getSubscription());

            appendSubscriptionAttributes(poSub, triggerProcess);

            appendServiceAttributes(poSub, triggerProcess);

            poSub.setStatusWaitingForApproval(triggerProcess.getStatus() == TriggerProcessStatus.WAITING_FOR_APPROVAL);

            poSub.setStatusText(triggerProcess.getStatus().name().toLowerCase());
            poSub.setStatusTextKey(TriggerProcessStatus.class.getSimpleName()
                    + "." + triggerProcess.getStatus().name());

            // Dummy assignment of user. This is necessary because the
            // subscription is still not created and user assignment is still
            // not executed.
            if (triggerProcess.getService().isAutoAssignUserEnabled()
                    .booleanValue()) {
                poSub.setNumberOfAssignedUsers(1);
            }

            result.add(poSub);
        }

        return result;
    }

    private void appendServiceAttributes(POSubscription po,
            VOTriggerProcess triggerProcess) {
        po.setServiceKey(triggerProcess.getService().getKey());
        po.setSupplierName(triggerProcess.getService().getSellerName());
        po.setServiceName(triggerProcess.getService().getNameToDisplay());
    }

    private void appendSubscriptionAttributes(POSubscription po,
            VOTriggerProcess triggerProcess) {

        if (triggerProcess.getService().isAutoAssignUserEnabled()
                .booleanValue()) {
            po.setNumberOfAssignedUsers(1);
        }

        po.setSubscriptionId(triggerProcess.getSubscription()
                .getSubscriptionId());
        po.setProvisioningProgress(triggerProcess.getSubscription()
                .getProvisioningProgress());
        po.setNumberOfAssignedUsers(triggerProcess.getSubscription()
                .getNumberOfAssignedUsers());

        SubscriptionStatus status = triggerProcess.getSubscription()
                .getStatus();
        if (status == null) {
            po.setStatusTextKey("");
            po.setStatusText("");
        }
    }

    private List<VOTriggerProcess> filterByCurrentUser(
            List<VOTriggerProcess> triggerProcesses) {
        PlatformUser owner = dm.getCurrentUser();
        List<VOTriggerProcess> result = new ArrayList<VOTriggerProcess>();
        for (VOTriggerProcess triggerProcess : triggerProcesses) {
            if (owner.getKey() == triggerProcess.getUser().getKey()) {
                result.add(triggerProcess);
            }
        }

        return result;
    }

    private List<VOTriggerProcess> filterByAutoAssignment(
            List<VOTriggerProcess> triggerProcesses) {
        List<VOTriggerProcess> result = new ArrayList<VOTriggerProcess>();
        for (VOTriggerProcess triggerProcess : triggerProcesses) {
            if (triggerProcess.getService().isAutoAssignUserEnabled()
                    .booleanValue()) {
                result.add(triggerProcess);
            }
        }

        return result;
    }
}
