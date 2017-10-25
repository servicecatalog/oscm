/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 4, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 4, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.payloads;

/**
 * @author tokoda
 * 
 */
public class UpdateUserPayload implements TaskPayload {

    private static final long serialVersionUID = -2057477014295201251L;

    private final long subscriptionKey;

    private final long usageLicenseKey;

    public UpdateUserPayload(long subscriptionKey, long usageLicenseKey) {
        this.subscriptionKey = subscriptionKey;
        this.usageLicenseKey = usageLicenseKey;
    }

    public long getSubscriptionKey() {
        return subscriptionKey;
    }

    public long getUsageLicenseKey() {
        return usageLicenseKey;
    }

    @Override
    public String getInfo() {
        return "SubscriptionKey: " + subscriptionKey + ", UsageLicenseKey: "
                + usageLicenseKey;
    }

}
