/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 27, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.triggerprocess;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;

/**
 * @author zankov
 * 
 */
@Remote
public interface TriggerProcessesService {

    /**
     * Retrieves all trigger processes with the given status and creates a
     * subscription presentation objects for the UI. </br> The number of
     * assigned user is manually set to 1 if the auto-assignment flag is
     * <code>true</code>.
     * 
     * @return list of POSubscriptionForList
     */
    public Response getAllWaitingForApprovalSubscriptions();

    /**
     * Retrieves all trigger processes with the given status and creates a list
     * of subscription presentation objects. The trigger process entries are
     * filtered by current user and auto-assignment flag. </br>The number of
     * assigned user is manually set to 1 if the auto-assignment flag is
     * <code>true</code>.
     * 
     * @return list of POSubscription
     */
    public Response getMyWaitingForApprovalSubscriptions();

    /**
     * Retrieves all waiting for approval trigger processes with the given
     * subscription ID.
     * 
     * @return list of trigger processes
     */
    public Response getAllWaitingForApprovalTriggerProcessesBySubscriptionId(
            String subscriptionId);
}
