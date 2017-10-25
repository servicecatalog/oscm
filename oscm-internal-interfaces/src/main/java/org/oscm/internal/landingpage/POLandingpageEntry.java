/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.landingpage;

import org.oscm.internal.base.BasePO;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * @author zankov
 * 
 */
public class POLandingpageEntry extends BasePO {

    private static final long serialVersionUID = 6686565083965484428L;

    // Service attributes
    private String serviceId;
    private String serviceAccessURL;
    private String shortDescription;
    private String name;
    private ServiceStatus serviceStatus;

    // Subscription attributes
    private boolean subscribed = false;
    private String subscriptionId;
    private SubscriptionStatus subscriptionStatus;
    private String sellerName;

    private ServiceAccessType serviceAccessType;

    private long serviceKey;

    private int serviceVersion;

    private long subscriptionKey;

    private int subscriptionVersion;

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setServiceAccessURL(String baseURL) {
        this.serviceAccessURL = baseURL;
    }

    public String getServiceAccessURL() {
        return serviceAccessURL;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setServiceStatus(ServiceStatus status) {
        this.serviceStatus = status;
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;

    }

    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setServiceAccessType(ServiceAccessType accessType) {
        this.serviceAccessType = accessType;
    }

    public ServiceAccessType getServiceAccessType() {
        return serviceAccessType;
    }

    public void setServiceKey(long key) {
        this.serviceKey = key;
    }

    public long getServiceKey() {
        return serviceKey;
    }

    public void setServiceVersion(int version) {
        this.serviceVersion = version;
    }

    public int getServiceVersion() {
        return serviceVersion;
    }

    public void setSubscriptionKey(long key) {
        this.subscriptionKey = key;
    }

    public long getSubscriptionKey() {
        return subscriptionKey;
    }

    public void setSubscriptionVersion(int version) {
        this.subscriptionVersion = version;
    }

    public int getSubscriptionVersion() {
        return subscriptionVersion;
    }

}
