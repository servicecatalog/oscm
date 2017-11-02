/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Florian Walker                                                
 *                                                                              
 *  Creation Date: 14.06.2011                                                      
 *                                                                              
 *  Completion Time: 14.06.2011                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import java.util.Date;
import java.util.List;

import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUsageLicense;

/**
 * Wrapper Class for VOSubscriptionDetails which holds additional view
 * attributes.
 */
public class SubscriptionDetails {
    private VOSubscriptionDetails voSubscriptionDetails;
    private Service subscribedService;

    public SubscriptionDetails(VOSubscriptionDetails voSubscriptionDetails) {
        this.voSubscriptionDetails = voSubscriptionDetails;
    }

    public VOSubscriptionDetails getVOSubscriptionDetails() {
        return voSubscriptionDetails;
    }

    public void setVOSubscriptionDetails(
            VOSubscriptionDetails voSubscriptionDetails) {
        this.voSubscriptionDetails = voSubscriptionDetails;
    }

    public VOService getVOSubscribedService() {
        return voSubscriptionDetails != null ? voSubscriptionDetails
                .getSubscribedService() : null;
    }

    public Service getSubscribedService() {
        if (subscribedService == null && voSubscriptionDetails != null
                && voSubscriptionDetails.getSubscribedService() != null) {
            subscribedService = new Service(
                    voSubscriptionDetails.getSubscribedService());
        }
        return subscribedService;
    }

    public long getServiceKey() {
        return voSubscriptionDetails.getServiceKey();
    }

    public String getSupplierName() {
        return voSubscriptionDetails.getSellerName();
    }

    public Date getActivationDate() {
        final Long l = voSubscriptionDetails.getActivationDate();
        return l == null ? null : new Date(l.longValue());
    }

    public List<VOUsageLicense> getUsageLicenses() {
        return voSubscriptionDetails.getUsageLicenses();
    }

}
