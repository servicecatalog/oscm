/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.oscm.domobjects.converters.SSConverter;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * DataContainer for domain object Subscription
 * 
 * @see Subscription
 * 
 * @author schmid
 */
@Embeddable
public class SubscriptionData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = -4906064557490051396L;

    /**
     * Unique identifier (within organization domain) for the subscription
     */
    @Column(nullable = false)
    private String subscriptionId;

    /**
     * Current status of the subscription (ACTIVE/PENDING/INVALID/DEACTIVATED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    /**
     * Date of creation of the subscription
     */
    @Column(nullable = false)
    private Long creationDate;

    /**
     * Date of activation of the subscription (may lie ahead, as products may be
     * subscribed to a target date).
     */
    private Long activationDate;

    /**
     * Date of deactivation of the subscription (may lie ahead, as products may
     * be unsubscribed for a target date).
     */
    private Long deactivationDate;

    /**
     * Identifier of the product instance that is returned by the concrete
     * product at the time of tenant provisioning.
     */
    private String productInstanceId;

    /**
     * A flag indicating that a mail was already sent to administrators in case
     * a subscription timed out.
     */
    private boolean timeoutMailSent;

    /**
     * The order number the subscription has in the customer's booking system.
     * Has no relevance to the BES at all, but is only stored for customer's
     * convenience.
     */
    private String purchaseOrderNumber;

    /**
     * The base URL to communicate with the instance of the technical product.
     */
    private String baseURL;

    /**
     * The path to the login wrapper of the technical product.
     */
    private String loginPath;

    /**
     * The information how to access an instance of the technical product
     */
    private String accessInfo;

    /**
     * The billing cut-off day for this subscription.
     */
    // @Column(nullable = false)
    private int cutOffDay;

    /**
     * The message from succesfull provisioning result
     */
    @Transient
    private String successMessage;

    @Column(nullable = false)
    private boolean external;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public void setActivationDate(Long activationDate) {
        this.activationDate = activationDate;
    }

    public Long getActivationDate() {
        return activationDate;
    }

    public void setDeactivationDate(Long deactivationDate) {
        this.deactivationDate = deactivationDate;
    }

    public Long getDeactivationDate() {
        return deactivationDate;
    }

    public String getProductInstanceId() {
        return productInstanceId;
    }

    public void setProductInstanceId(String productInstanceId) {
        this.productInstanceId = productInstanceId;
    }

    public void setTimeoutMailSent(boolean timeoutMailSent) {
        this.timeoutMailSent = timeoutMailSent;
    }

    public boolean isTimeoutMailSent() {
        return timeoutMailSent;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    public String getAccessInfo() {
        return accessInfo;
    }

    public void setAccessInfo(String accessInfo) {
        this.accessInfo = accessInfo;
    }

    public void setCutOffDay(int cutOffDay) {
        this.cutOffDay = cutOffDay;
    }

    public int getCutOffDay() {
        return cutOffDay;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public boolean isExternal() {
        return external;
    }

}
