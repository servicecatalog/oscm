/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 28.03.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import org.oscm.internal.intf.SubscriptionService;

/**
 * @author weiser
 * 
 */
public class SubscriptionUnsubscribeTask extends WebtestTask {

    private String subId;

    @Override
    public void executeInternal() throws Exception {
        SubscriptionService ss = getServiceInterface(SubscriptionService.class);
        ss.unsubscribeFromService(subId);
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

}
