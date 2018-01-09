/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 27.03.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;

import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUda;

/**
 * @author weiser
 * 
 */
public class SubscriptionSetOwnerTask extends WebtestTask {

    private String subId;
    private String ownerId;

    @Override
    public void executeInternal() throws Exception {
        if (!isEmpty(ownerId)) {
            SubscriptionService ss = getServiceInterface(SubscriptionService.class);
            VOSubscriptionDetails sub = ss.getSubscriptionDetails(subId);
            sub.setOwnerId(ownerId);
            ss.modifySubscription(sub, null, new ArrayList<VOUda>());
        } else {
            log("No parameter set - noting modified.", 0);
        }
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
