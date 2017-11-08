/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.04.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingSubscriptionStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

public class BillingSubscriptionStates {

    public static BillingSubscriptionStatus createBillingSubscriptionStatus(
            DataService mgr, long subscriptionKey, long endOfLastBilledPeriod)
            throws NonUniqueBusinessKeyException {
        BillingSubscriptionStatus billingSubStatus = new BillingSubscriptionStatus();
        billingSubStatus.setSubscriptionKey(subscriptionKey);
        billingSubStatus.setEndOfLastBilledPeriod(endOfLastBilledPeriod);

        mgr.persist(billingSubStatus);
        mgr.flush();

        return billingSubStatus;
    }
}
