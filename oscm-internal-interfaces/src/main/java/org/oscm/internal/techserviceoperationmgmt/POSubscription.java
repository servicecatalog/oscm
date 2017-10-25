/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Nov 14, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.techserviceoperationmgmt;

import org.oscm.internal.base.BasePO;

/**
 * @author zhaoh.fnst
 * 
 */
public class POSubscription extends BasePO {

    private static final long serialVersionUID = -8588928347022173768L;

    private String subscriptionId;

    /**
     * @return the subscriptionId
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * @param subscriptionId
     *            the subscriptionId to set
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

}
