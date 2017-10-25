/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.subscriptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * Wrapper Class for VOSubscription which holds additional view attributes.
 * 
 */
public class POSubscription implements Serializable {

    private static final long serialVersionUID = -5443859617863345910L;

    private boolean selected;
    private final VOSubscription voSubscription;
    private long roleKey = 0;
    private List<VORoleDefinition> roles;
    private String accessUrl;
    private OperationModel selectedOperation;
    private boolean executeDisabled = true;
    private String selectedOperationId;
    private boolean statusWaitingForApproval;
    private TriggerProcessStatus triggerProcessStatus;

    private SubscriptionStatus status;

    private String statusText;
    private String statusTextKey;

    private int numberOfAssignedUsers;

    private String supplierName;

    private String target = "";
    
    private String serviceName;

    private String subscriptionName;

    public POSubscription(VOSubscription voSubscription) {
        this.voSubscription = voSubscription;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public VOSubscription getVOSubscription() {
        return voSubscription;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getHexKey() {
        return Long.toHexString(voSubscription.getKey());
    }

    public boolean isAccessViaAccessInfo() {
        return voSubscription.getServiceAccessType() == ServiceAccessType.DIRECT
                || voSubscription.getServiceAccessType() == ServiceAccessType.USER;
    }

    public boolean isStatusActive() {
        return getStatus() == SubscriptionStatus.ACTIVE;
    }

    public boolean isStatusPending() {
        return getStatus() == SubscriptionStatus.PENDING;
    }

    public boolean isStatusPendingUpd() {
        return getStatus() == SubscriptionStatus.PENDING_UPD;
    }

    /*
     * Delegate Methods
     */

    public Date getActivationDate() {
        final Long l = voSubscription.getActivationDate();
        return l == null ? null : new Date(l.longValue());
    }

    public Date getCreationDate() {
        final Long l = voSubscription.getCreationDate();
        return l == null ? null : new Date(l.longValue());
    }

    public Date getDeactivationDate() {
        final Long l = voSubscription.getDeactivationDate();
        return l == null ? null : new Date(l.longValue());
    }

    public long getKey() {
        return voSubscription.getKey();
    }

    public String getServiceAccessInfo() {
        return voSubscription.getServiceAccessInfo();
    }

    public String getServiceAccessInfoFormPart() {
        return voSubscription.getServiceAccessInfoFormPart();
    }

    public ServiceAccessType getServiceAccessType() {
        return voSubscription.getServiceAccessType();
    }

    public String getServiceBaseURL() {
        return voSubscription.getServiceBaseURL();
    }

    public String getServiceId() {
        return voSubscription.getServiceId();
    }

    public long getServiceKey() {
        return voSubscription.getServiceKey();
    }

    public String getServiceInstanceId() {
        return voSubscription.getServiceInstanceId();
    }

    public String getServiceLoginPath() {
        return voSubscription.getServiceLoginPath();
    }

    public String getPurchaseOrderNumber() {
        return voSubscription.getPurchaseOrderNumber();
    }

    public SubscriptionStatus getStatus() {
        return this.status;
    }

    public String getSubscriptionId() {
        return voSubscription.getSubscriptionId();
    }

    public String getSupplierName() {
        return this.supplierName;
    }

    public void setSupplierName(String name) {
        this.supplierName = name;
    }

    public int getVersion() {
        return voSubscription.getVersion();
    }

    public boolean isTimeoutMailSent() {
        return voSubscription.isTimeoutMailSent();
    }

    public void setActivationDate(Date activationDate) {
        Long l = activationDate == null ? null : Long.valueOf(activationDate
                .getTime());
        voSubscription.setActivationDate(l);
    }

    public void setCreationDate(Date creationDate) {
        Long l = creationDate == null ? null : Long.valueOf(creationDate
                .getTime());
        voSubscription.setCreationDate(l);
    }

    public void setDeactivationDate(Date deactivationDate) {
        Long l = deactivationDate == null ? null : Long
                .valueOf(deactivationDate.getTime());
        voSubscription.setDeactivationDate(l);
    }

    public void setServiceAccessInfo(String serviceAccessInfo) {
        voSubscription.setServiceAccessInfo(serviceAccessInfo);
    }

    public void setServiceAccessType(ServiceAccessType serviceAccessType) {
        voSubscription.setServiceAccessType(serviceAccessType);
    }

    public void setServiceBaseURL(String serviceBaseURL) {
        voSubscription.setServiceBaseURL(serviceBaseURL);
    }

    public void setServiceId(String serviceId) {
        voSubscription.setServiceId(serviceId);
    }

    public void setServiceKey(long serviceKey) {
        voSubscription.setServiceKey(serviceKey);
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        voSubscription.setServiceInstanceId(serviceInstanceId);
    }

    public void setServiceLoginPath(String serviceLoginPath) {
        voSubscription.setServiceLoginPath(serviceLoginPath);
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        voSubscription.setPurchaseOrderNumber(purchaseOrderNumber);
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
        voSubscription.setStatus(status);
    }

    public void setSubscriptionId(String subscriptionId) {
        voSubscription.setSubscriptionId(subscriptionId);
    }

    public void setTimeoutMailSent(boolean timeoutMailSent) {
        voSubscription.setTimeoutMailSent(timeoutMailSent);
    }

    @Override
    public String toString() {
        return voSubscription.toString();
    }

    public void setRoleKey(long roleKey) {
        this.roleKey = roleKey;
    }

    public long getRoleKey() {
        return roleKey;
    }

    public void setRoles(List<VORoleDefinition> serviceRoles) {
        this.roles = serviceRoles;
    }

    public List<VORoleDefinition> getRoles() {
        return roles;
    }

    public VORoleDefinition getSelectedRole() {
        if (roles == null || roleKey == 0) {
            return null;
        }
        for (VORoleDefinition role : roles) {
            if (role.getKey() == roleKey) {
                return role;
            }
        }
        return null;
    }

    public String getProvisioningProgress() {
        return voSubscription.getProvisioningProgress();
    }

    public void setProvisioningProgress(String provisioningProgress) {
        voSubscription.setProvisioningProgress(provisioningProgress);
    }

    public List<VOTechnicalServiceOperation> getTechnicalServiceOperations() {
        return voSubscription.getTechnicalServiceOperations();
    }

    public void setTechnicalServiceOperations(
            List<VOTechnicalServiceOperation> technicalServiceOperations) {
        voSubscription
                .setTechnicalServiceOperations(technicalServiceOperations);
    }

    public List<SelectItem> getTechnicalServiceOperationItems() {
        List<SelectItem> items = new ArrayList<SelectItem>();
        for (VOTechnicalServiceOperation operation : getTechnicalServiceOperations()) {
            SelectItem item = new SelectItem(operation.getOperationId(),
                    operation.getOperationName());
            items.add(item);
        }
        return items;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getStatusTextKey() {
        return this.statusTextKey;
    }

    public void setStatusTextKey(String statusTextKey) {
        this.statusTextKey = statusTextKey;
    }

    public int getNumberOfAssignedUsers() {
        return this.numberOfAssignedUsers;
    }

    public void setNumberOfAssignedUsers(int nrOfAssignedUsers) {
        this.numberOfAssignedUsers = nrOfAssignedUsers;
    }

    public boolean isProvisioningProgressRendered() {
        return isStatusPending() && getProvisioningProgress() != null;
    }

    public boolean isOperationsRendered() {
        return (isStatusActive() || isStatusPendingUpd())
                && !getTechnicalServiceOperations().isEmpty();
    }

    public void setSelectedOperation(OperationModel op) {
        selectedOperation = op;
    }

    public OperationModel getSelectedOperation() {
        return selectedOperation;
    }

    public List<OperationParameterModel> getOperationParameters() {
        if (selectedOperation == null) {
            return new LinkedList<>();
        }
        return selectedOperation.getParameters();
    }

    public String getOperationDescription() {
        if (selectedOperation == null) {
            return "";
        }
        return selectedOperation.getDescription();
    }

    public boolean isExecuteDisabled() {
        return executeDisabled;
    }

    public void setExecuteDisabled(boolean executeDisabled) {
        this.executeDisabled = executeDisabled;
    }

    public String getSelectedOperationId() {
        return selectedOperationId;
    }

    public void setSelectedOperationId(String selectedOperationId) {
        this.selectedOperationId = selectedOperationId;
    }

    public boolean isShowUseServiceButton() {
        return (isStatusActive() || isStatusPendingUpd())
                && getAccessUrl() != null && !getAccessUrl().isEmpty();
    }

    public boolean isStatusWaitingForApproval() {
        return statusWaitingForApproval;
    }

    public void setStatusWaitingForApproval(boolean statusWaitingForApproval) {
        this.statusWaitingForApproval = statusWaitingForApproval;
    }

    public TriggerProcessStatus getTriggerProcessStatus() {
        return triggerProcessStatus;
    }

    public void setTriggerProcessStatus(
            TriggerProcessStatus triggerProcessStatus) {
        this.triggerProcessStatus = triggerProcessStatus;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

    public String getCustomTabUrl() {
        return voSubscription.getCustomTabUrl();
    }

    public void setCustomTabUrl(String customTabUrl) {
        voSubscription.setCustomTabUrl(customTabUrl);
    }

    public String getCustomTabName() {
        return voSubscription.getCustomTabName();
    }

    public void setCustomTabName(String customTabName) {
        voSubscription.setCustomTabName(customTabName);
    }

    public String getOrganizationId() {
        return voSubscription.getOrganizationId();
    }

    public void setOrganizationId(String orgId) {
        voSubscription.setOrganizationId(orgId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        POSubscription other = (POSubscription) o;
        return getKey() == other.getKey();
    }

    @Override
    public int hashCode() {
        return voSubscription.hashCode();
    }

    public void setSubscriptionName(String subscriptionId) {
        this.subscriptionName = subscriptionId;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }
}
