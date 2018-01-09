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
public class SubscriptionRenameTask extends WebtestTask {

    private String subId;
    private String newSubId;
    private String newPon;

    @Override
    public void executeInternal() throws Exception {
        SubscriptionService ss = getServiceInterface(SubscriptionService.class);
        VOSubscriptionDetails sub = ss.getSubscriptionDetails(subId);
        sub.setSubscriptionId(newSubId);
        sub.setPurchaseOrderNumber(newPon);
        ss.modifySubscription(sub, null, new ArrayList<VOUda>());
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public void setNewSubId(String newSubId) {
        this.newSubId = newSubId;
    }

    public void setNewPon(String newPon) {
        this.newPon = newPon;
    }

}
