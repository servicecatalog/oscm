/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.data;

import com.microsoft.azure.management.network.models.ProvisioningState;

public class AzureState {

    /**
     * Azure
     */
    private String provisioningState;

    /**
     * Azure
     */
    private String statusCode = "";

    /**
     * Azure
     */
    public AzureState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    /**
     * Azure
     */
    public String getProvisioningState() {
        return provisioningState;
    }

    /**
     * Azure
     */
    public void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    /**
     * Azure
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * Azure
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Azure
     */
    public boolean isSucceeded() {
        return ProvisioningState.SUCCEEDED.equals(provisioningState);
    }

    /**
     * Azure
     */
    public boolean isDeleted() {
        return ProvisioningState.DELETING.equals(provisioningState);
    }

    /**
     * Azure
     */
    public boolean isFailed() {
        return ProvisioningState.FAILED.equals(provisioningState);
    }
}
