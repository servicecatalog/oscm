/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                   
 *                                                                              
 *  Creation Date: 17.11.2011                                                      
 *                                                                              
 *  Completion Time: 17.11.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserSubscription;

/**
 * Remote interface of the subscription management service. This interface
 * contains methods that provide performance optimized access. It is intended
 * only as a temporary optimization and will be replaced by other means. Only
 * for internal usage.
 * 
 */
@Remote
public interface SubscriptionServiceInternal {

    /**
     * Retrieves all subscriptions of the calling user's organization,
     * regardless of their status.
     * <p>
     * Required role: administrator of the organization
     * 
     * @param performanceHint
     *            a <code>performanceHint</code> constant specifying the data to
     *            include in the result. This can be used to increase the search
     *            performance.
     * 
     * @return the list of subscriptions
     */
    public List<VOSubscription> getSubscriptionsForOrganization(
            PerformanceHint performanceHint);

    /**
     * Retrieves all subscriptions of the calling user's organization that match
     * the given set of subscription status.
     * <p>
     * 
     * @param requiredStatus
     *            only subscriptions that match one of these status will be
     *            included in the result
     * @param performanceHint
     *            a <code>performanceHint</code> constant specifying the data to
     *            include in the result. This can be used to increase the search
     *            performance.
     * @return the list of subscriptions
     */
    public List<VOSubscription> getSubscriptionsForOrganizationWithFilter(
            Set<SubscriptionStatus> requiredStatus,
            PerformanceHint performanceHint);

    /**
     * Retrieves the subscriptions the specified user is assigned to. The list
     * includes subscriptions whose status is <code>ACTIVE</code>,
     * <code>PENDING</code>, <code>SUSPENDED</code>, or <code>EXPIRED</code>.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param user
     *            the user for whom the list of subscriptions is to be retrieved
     * @return the list of subscriptions
     * @throws ObjectNotFoundException
     *             if the user is not found
     * @throws OperationNotPermittedException
     *             if the given user's organization differs from that of the
     *             caller
     */
    public List<VOUserSubscription> getSubscriptionsForUser(VOUser user,
            PerformanceHint performanceHint) throws ObjectNotFoundException,
            OperationNotPermittedException;

    /**
     * Validate this subscription id whether has existed in the calling user's
     * organization, regardless of their status.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param subscriptionId
     *            the subscription identifier
     * @return <code>true</code> if the subscription id has already been used,
     *         <code>false</code> if the subscription id doesn't exist in
     *         calling user's organization
     */
    public boolean validateSubscriptionIdForOrganization(String subscriptionId);

    /**
     * Retrieves all subscriptions of the calling user's organization.
     * <p>
     * Required role: administrator of the organization, subscription manager or unit administrator
     * 
     * @param performanceHint
     *            a <code>performanceHint</code> constant specifying the data to
     *            include in the result. This can be used to increase the search
     *            performance.
     * 
     * @return the list of subscriptions
     */
    public List<VOSubscription> getAllSubscriptionsForOrganization(
            PerformanceHint performanceHint);

}
