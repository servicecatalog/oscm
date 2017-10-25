/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 17, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.techserviceoperationmgmt;

import org.oscm.internal.base.BasePO;
import org.oscm.internal.types.enumtypes.OperationStatus;

/**
 * @author zhaoh.fnst
 * 
 */
public class POOperationRecord extends BasePO {

    private static final long serialVersionUID = 1802499299492814410L;

    private String transactionId;
    private String operationId;
    private POSubscription subscription;
    private long executionDate;
    private POUser user;
    private OperationStatus status;
    private String statusDesc;

    /**
     * @return the subscription
     */
    public POSubscription getSubscription() {
        return subscription;
    }

    /**
     * @param subscription
     *            the subscription to set
     */
    public void setSubscription(POSubscription subscription) {
        this.subscription = subscription;
    }

    /**
     * @return the user
     */
    public POUser getUser() {
        return user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(POUser user) {
        this.user = user;
    }

    /**
     * @return the operationId
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * @param operationId
     *            the operationId to set
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * @return the subscriptionId
     */
    public String getSubscriptionId() {
        return subscription.getSubscriptionId();
    }

    /**
     * @return the executionDate
     */
    public long getExecutionDate() {
        return executionDate;
    }

    /**
     * @param executionDate
     *            the executionDate to set
     */
    public void setExecutionDate(long executionDate) {
        this.executionDate = executionDate;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return user.getUserId();
    }

    /**
     * @return the status
     */
    public OperationStatus getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    /**
     * @return the statusDesc
     */
    public String getStatusDesc() {
        return statusDesc;
    }

    /**
     * @param statusDesc
     *            the statusDesc to set
     */
    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    /**
     * @return the transactionId
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * @param transactionId
     *            the transactionId to set
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
