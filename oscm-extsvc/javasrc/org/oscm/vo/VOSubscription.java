/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2009-09-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.util.ArrayList;
import java.util.List;

import org.oscm.types.enumtypes.ServiceAccessType;
import org.oscm.types.enumtypes.SubscriptionStatus;

/**
 * Represents a subscription to a service.
 * 
 */
public class VOSubscription extends BaseVO  {

    private static final long serialVersionUID = 8010904760458989105L;

    private String serviceId;
    private long serviceKey;
    private Long activationDate;
    private Long creationDate;
    private Long deactivationDate;
    private String serviceAccessInfo;
    private ServiceAccessType serviceAccessType;
    private String serviceBaseURL;
    private String serviceLoginPath;
    private SubscriptionStatus status;
    private String serviceInstanceId;
    private boolean timeoutMailSent;
    private String purchaseOrderNumber;
    private String subscriptionId;
    private String provisioningProgress;
    private String provisioningError;
    private int numberOfAssignedUsers;
    private String sellerName;
    private String ownerId;
    private long unitKey;
    private String unitName;

    /**
     * The technical service operations.
     */
    private List<VOTechnicalServiceOperation> technicalServiceOperations = new ArrayList<VOTechnicalServiceOperation>();

    /**
     * Retrieves the identifier of the subscription's underlying marketable
     * service.
     * 
     * @return the service ID
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the identifier of the subscription's underlying marketable service.
     * 
     * @param serviceId
     *            the service ID
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Retrieves the date and time the subscription will be activated.
     * 
     * @return the activation date and time
     */
    public Long getActivationDate() {
        return activationDate;
    }

    /**
     * Sets the date and time the subscription is to be activated.
     * 
     * @param activationDate
     *            the activation date and time
     */
    public void setActivationDate(Long activationDate) {
        this.activationDate = activationDate;
    }

    /**
     * Retrieves the date and time the subscription was created.
     * 
     * @return the creation date and time
     */
    public Long getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the date and time the subscription is created.
     * 
     * @param creationDate
     *            the creation date and time
     */
    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Retrieves the date and time the subscription is to be deactivated.
     * 
     * @return the deactivation date and time
     */
    public Long getDeactivationDate() {
        return deactivationDate;
    }

    /**
     * Sets the date and time the subscription is to be deactivated.
     * 
     * @param deactivationDate
     *            the deactivation date and time
     */
    public void setDeactivationDate(Long deactivationDate) {
        this.deactivationDate = deactivationDate;
    }

    /**
     * Retrieves the text describing how to access the underlying application.
     * 
     * @return the access information
     */
    public String getServiceAccessInfo() {
        return serviceAccessInfo;
    }

    /**
     * Sets the text describing how to access the underlying application.
     * 
     * @param serviceAccessInfo
     *            the access information
     */
    public void setServiceAccessInfo(String serviceAccessInfo) {
        this.serviceAccessInfo = serviceAccessInfo;
    }

    /**
     * Retrieves the access type of the subscription's service. The access type
     * specifies how users access the underlying application.
     * 
     * @return the service access type
     */
    public ServiceAccessType getServiceAccessType() {
        return serviceAccessType;
    }

    /**
     * Sets the access type of the subscription's service. The access type
     * specifies how users access the underlying application.
     * 
     * @param serviceAccessType
     *            the service access type
     */
    public void setServiceAccessType(ServiceAccessType serviceAccessType) {
        this.serviceAccessType = serviceAccessType;
    }

    /**
     * Retrieves the URL of the remote interface of the application underlying
     * to the subscription's service.
     * 
     * @return the URL
     */
    public String getServiceBaseURL() {
        return serviceBaseURL;
    }

    /**
     * Sets the URL of the remote interface of the application underlying to the
     * subscription's service.
     * 
     * @param serviceBaseURL
     *            the URL of the application underlying to the subscription's
     *            service<br>
     *            Be aware that internet domain names must follow the following
     *            rules: <br>
     *            They must start with a letter and end with a letter or number.<br>
     *            They may contain letters, numbers, or hyphens only. Special
     *            characters are not allowed.<br>
     *            They may consist of a maximum of 63 characters.
     */
    public void setServiceBaseURL(String serviceBaseURL) {
        this.serviceBaseURL = serviceBaseURL;
    }

    /**
     * Retrieves the path for logging in to the underlying application. This is
     * the path of the application's token handler for login requests with user
     * tokens.
     * 
     * @return the login path
     */
    public String getServiceLoginPath() {
        return serviceLoginPath;
    }

    /**
     * Sets the path for logging in to the underlying application. This is the
     * path of the application's token handler for login requests with user
     * tokens.
     * 
     * @param serviceLoginPath
     *            the login path
     */
    public void setServiceLoginPath(String serviceLoginPath) {
        this.serviceLoginPath = serviceLoginPath;
    }

    /**
     * Retrieves the status of the subscription.
     * 
     * @return the status
     */
    public SubscriptionStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the subscription.
     * 
     * @param status
     *            the status
     */
    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    /**
     * Retrieves the identifier of the application instance that was created for
     * the subscription.
     * 
     * @return the instance ID
     */
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    /**
     * Sets the identifier of the application instance that was created for the
     * subscription.
     * 
     * @param serviceInstanceId
     *            the instance ID
     */
    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    /**
     * Defines whether an email is to be sent when the subscription expires.
     * 
     * @param timeoutMailSent
     *            <code>true</code> if an email is to be sent,
     *            <code>false</code> otherwise
     */
    public void setTimeoutMailSent(boolean timeoutMailSent) {
        this.timeoutMailSent = timeoutMailSent;
    }

    /**
     * Returns whether an email is sent when the subscription expires.
     * 
     * @return <code>true</code> if an email is sent, <code>false</code>
     *         otherwise
     */
    public boolean isTimeoutMailSent() {
        return timeoutMailSent;
    }

    /**
     * Retrieves the reference number the customer specified when creating the
     * subscription.
     * 
     * @return the reference number
     */
    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    /**
     * Sets the customer-specific reference number for the subscription.
     * 
     * @param purchaseOrderNumber
     *            the reference number
     */
    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    /**
     * Retrieves the identifier of the subscription.
     * 
     * @return the subscription ID
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets the identifier of the subscription.
     * 
     * @param subscriptionId
     *            the subscription ID
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Retrieves information on the progress of the provisioning operation.
     * 
     * @return the progress information
     */
    public String getProvisioningProgress() {
        return provisioningProgress;
    }

    /**
     * Sets information on the progress of the provisioning operation.
     * 
     * @param provisioningProgress
     *            the progress information
     */
    public void setProvisioningProgress(String provisioningProgress) {
        this.provisioningProgress = provisioningProgress;
    }

    /**
     * Sets the operations provided by the subscription's underlying technical
     * service.
     * 
     * @param technicalServiceOperations
     *            the list of technical service operations
     */
    public void setTechnicalServiceOperations(
            List<VOTechnicalServiceOperation> technicalServiceOperations) {
        this.technicalServiceOperations = technicalServiceOperations;
    }

    /**
     * Retrieves the operations provided by the subscription's underlying
     * technical service.
     * 
     * @return the list of technical service operations
     */
    public List<VOTechnicalServiceOperation> getTechnicalServiceOperations() {
        return technicalServiceOperations;
    }

    /**
     * Sets the numeric key of the subscription's underlying service.
     * 
     * @param serviceKey
     *            the key
     */
    public void setServiceKey(long serviceKey) {
        this.serviceKey = serviceKey;
    }

    /**
     * Retrieves the numeric key of the subscription's underlying service.
     * 
     * @return the key
     */
    public long getServiceKey() {
        return serviceKey;
    }

    /**
     * Sets the number of users assigned to the subscription. This value is only
     * set when reading the subscription data, but not evaluated, for example,
     * in write operations.
     * 
     * @param numberOfAssignedUsers
     *            the number of users
     */
    public void setNumberOfAssignedUsers(int numberOfAssignedUsers) {
        this.numberOfAssignedUsers = numberOfAssignedUsers;
    }

    /**
     * Returns the number of users assigned to the subscription.
     * 
     * @return the number of users
     */
    public int getNumberOfAssignedUsers() {
        return numberOfAssignedUsers;
    }

    /**
     * Sets the name of the seller organization (supplier, broker, or reseller)
     * that provides the subscription's underlying service.
     * 
     * @param sellerName
     *            the organization name
     */
    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    /**
     * Retrieves the name of the seller organization (supplier, broker, or
     * reseller) that provides the subscription's underlying service.
     * 
     * @return the organization name
     */
    public String getSellerName() {
        return sellerName;
    }

    /**
     * Retrieves the owner of the subscription.
     * 
     * @return the user ID of the owner
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Sets the owner of the subscription. The specified user must have the
     * administrator or subscription manager role.
     * 
     * @param ownerId
     *            the user ID of the owner
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Retrieves the numeric key of the organizational unit to which the
     * subscription is assigned.
     * 
     * @return the key
     */
    public long getUnitKey() {
        return unitKey;
    }

    /**
     * Sets the numeric key of the organizational unit to which the subscription
     * is to be assigned. If not specified, the name is used to identify the
     * organizational unit.
     * 
     * @param unitKey the key
     */
    public void setUnitKey(long unitKey) {
        this.unitKey = unitKey;
    }

    /**
     * Retrieves the name of the organizational unit to which the subscription
     * is assigned.
     * 
     * @return the name of the organizational unit
     */
    public String getUnitName() {
        return unitName;
    }

    /**
     * Sets the name of the organizational unit, to which the subscription is 
     * to be assigned. If it is <code>null</code> or empty, the subscription is  
     * not assigned to any unit. 
     * 
     * @param unitName the name of the organizational unit
     */
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
    
    /**
     * Retrieves information about the error in case provisioning operation was aborted.
     * 
     * @return the provisioning error
     */
    public String getProvisioningError() {
        return provisioningError;
    }
    
    /**
     * Sets information about the error during provisioning operation.
     * 
     * @param provisioningError
     *            the provisioning error
     */
    public void setProvisioningError(String provisioningError) {
        this.provisioningError = provisioningError;
    }
}
