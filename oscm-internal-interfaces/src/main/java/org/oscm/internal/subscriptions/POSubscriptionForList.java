/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Nov 12, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptions;

import java.io.Serializable;
import java.util.Date;

/**
 * @author tokoda
 * 
 */
public class POSubscriptionForList implements Serializable {

    private static final long serialVersionUID = 1L;

    private String subscriptionId;
    
    private long subscriptionKey;

    private long serviceKey;

    private String supplierName;

    private String supplierOrganizationId;

    private String accessUrl;

    private boolean accessViaAccessInfo;

    private String statusText;

    private String statusTextKey;

    private boolean statusActive;

    private boolean statusPending;

    private boolean statusPendingUpd;

    private boolean statusWaitingForApproval;

    private String serviceAccessInfo;

    private int numberOfAssignedUsers;

    private String provisioningProgress;

    private String target;

    private boolean provisioningProgressRendered;
    
    private String purchaseOrderNumber;

    private String unit;
    
    private Date activationDate;
    
    private String serviceName;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    
    public long getSubscriptionKey() {
        return subscriptionKey;
    }
    
    public void setSubscriptionKey(long subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }
    
    public long getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(long serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierOrganizationId() {
        return supplierOrganizationId;
    }

    public void setSupplierOrganizationId(String supplierOrganizationId) {
        this.supplierOrganizationId = supplierOrganizationId;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public boolean isAccessViaAccessInfo() {
        return accessViaAccessInfo;
    }

    public void setAccessViaAccessInfo(boolean accessViaAccessInfo) {
        this.accessViaAccessInfo = accessViaAccessInfo;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getStatusTextKey() {
        return statusTextKey;
    }

    public void setStatusTextKey(String statusTextKey) {
        this.statusTextKey = statusTextKey;
    }

    public boolean isStatusActive() {
        return statusActive;
    }

    public void setStatusActive(boolean statusActive) {
        this.statusActive = statusActive;
    }

    public boolean isStatusPending() {
        return statusPending;
    }

    public void setStatusPending(boolean statusPending) {
        this.statusPending = statusPending;
    }

    public boolean isStatusPendingUpd() {
        return statusPendingUpd;
    }

    public void setStatusPendingUpd(boolean statusPendingUpd) {
        this.statusPendingUpd = statusPendingUpd;
    }

    public String getServiceAccessInfo() {
        return serviceAccessInfo;
    }

    public void setServiceAccessInfo(String serviceAccessInfo) {
        this.serviceAccessInfo = serviceAccessInfo;
    }

    public int getNumberOfAssignedUsers() {
        return numberOfAssignedUsers;
    }

    public void setNumberOfAssignedUsers(int numberOfAssignedUsers) {
        this.numberOfAssignedUsers = numberOfAssignedUsers;
    }

    public boolean isProvisioningProgressRendered() {
        return provisioningProgressRendered;
    }

    public void setProvisioningProgressRendered(
            boolean provisioningProgressRendered) {
        this.provisioningProgressRendered = provisioningProgressRendered;
    }

    public String getProvisioningProgress() {
        return provisioningProgress;
    }

    public void setProvisioningProgress(String provisioningProgress) {
        this.provisioningProgress = provisioningProgress;
    }

    public boolean isStatusWaitingForApproval() {
        return statusWaitingForApproval;
    }

    public void setStatusWaitingForApproval(boolean statusWaitingForApproval) {
        this.statusWaitingForApproval = statusWaitingForApproval;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

	public String getPurchaseOrderNumber() {
		return purchaseOrderNumber;
	}

	public void setPurchaseOrderNumber(String purchaseOrderNumber) {
		this.purchaseOrderNumber = purchaseOrderNumber;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
