/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Nov 12, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptions;

import java.util.Set;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.paginator.Pagination;
import org.oscm.paginator.PaginationFullTextFilter;

import javax.ejb.Remote;

/**
 * @author tokoda
 * 
 */
@Remote
public interface SubscriptionsService {

    /**
     * Retrieves all the subscriptions for which the calling organization is
     * subscribing.
     * 
     * @param states
     *            the set of subscription status in which subscriptions should
     *            be returned. If status is null, all subscriptions owned by the
     *            organization are returned.
     * @return A <code>Response</code> object containing the subscription data
     *         as <code>POSubscriptionForList</code>.
     */
    public Response getSubscriptionsForOrg(Set<SubscriptionStatus> states);

    /**
     * Retrieves the subscriptions for the user who is currently logged in. The
     * result is wrapped into a response object.
     */
    public Response getMySubscriptions();

    /**
     * Retrieves the subscriptions and customers for the user who is currently
     * logged in. The result is wrapped into a response object.
     * 
     * @exception OrganizationAuthoritiesException
     */
    public Response getSubscriptionsAndCustomersForManagers()
            throws OrganizationAuthoritiesException;

    /**
     * Retrieves the subscriptions and customers for the user who is currently
     * logged in. The result contains a list which is in a subscriptions range
     * specified with the pagination parameter. The result is wrapped into a
     * response object.
     * 
     * @exception OrganizationAuthoritiesException
     */
    public Response getSubscriptionsAndCustomersForManagers(
            Pagination pagination) throws OrganizationAuthoritiesException;


    Response getSubscriptionsForOrg(Set<SubscriptionStatus> states, Pagination pagination)
            throws OrganizationAuthoritiesException;

    Integer getSubscriptionsForOrgSizeWithFiltering(Set<SubscriptionStatus> states,
                                                    PaginationFullTextFilter pagination) throws OrganizationAuthoritiesException;

    Integer getSubscriptionsAndCustomersForManagersSize(Pagination pagination) throws OrganizationAuthoritiesException;

    Response getSubscriptionsForOrgWithFiltering(Set<SubscriptionStatus> states, PaginationFullTextFilter pagination)
            throws OrganizationAuthoritiesException;

    Integer getSubscriptionsForOrgSize(Set<SubscriptionStatus> states, Pagination pagination)
            throws OrganizationAuthoritiesException;

    Response getMySubscriptionsWithFiltering(PaginationFullTextFilter pagination) throws OrganizationAuthoritiesException;

    Integer getMySubscriptionsSizeWithFiltering(PaginationFullTextFilter pagination) throws OrganizationAuthoritiesException;

    VOSubscriptionDetails getSubscriptionDetails(long subscriptionKey) throws ObjectNotFoundException;

    POSubscription getMySubscriptionDetails(long key);
}
