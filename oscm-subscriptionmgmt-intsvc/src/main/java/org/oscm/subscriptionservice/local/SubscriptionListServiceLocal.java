/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.local;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.usermanagement.POSubscription;
import org.oscm.paginator.Pagination;
import org.oscm.paginator.PaginationInt;

/**
 * @author weiser
 * 
 */
@Local
public interface SubscriptionListServiceLocal {

    /**
     * Returns a list of Subscriptions with the passed states and owned by the
     * passed organization mapped to the roles defined on the technical service.
     * 
     * @param owner
     *            the owning organization
     * @param states
     *            the set of {@link SubscriptionStatus} in which subscriptions
     *            should be returned
     * @return the list of subscriptions mapped to the roles
     */
    List<SubscriptionWithRoles> getSubcsriptionsWithRoles(Organization owner,
            Set<SubscriptionStatus> states);

    /**
     * Gets the list of {@link UsageLicense}s representing the
     * {@link Subscription}s the passed user is assigned to including the
     * assigned role if available.
     * 
     * @param user
     *            the user to get the subscription assignments for
     * @param states
     *            the set of {@link SubscriptionStatus} in which the
     *            subscriptions must be
     * @return the list of {@link UsageLicense}s
     */
    List<UsageLicense> getSubscriptionAssignments(PlatformUser user,
            Set<SubscriptionStatus> states);

    /**
     * Returns a list of Subscriptions with the passed states and owned by the
     * organization of the current user.
     * 
     * @param states
     *            the set of {@link SubscriptionStatus} in which subscriptions
     *            should be returned. If status is null, all subscriptions owned
     *            by the organization of the current user are returned.
     * @return the list of {@link Subscription}s
     */
    List<Subscription> getSubscriptionsForOrganization(
            Set<SubscriptionStatus> states);

    /**
     * Returns a list of Subscriptions with the passed states and owned by the
     * organization of the current user.
     * 
     * @param states
     *            the set of {@link SubscriptionStatus} in which subscriptions
     *            should be returned. If status is null, all subscriptions owned
     *            by the organization of the current user are returned.
     * @param pagination
     *            the pagination, sorting, and filtering parameters
     * 
     * @return the list of {@link Subscription}s
     */
    List<Subscription> getSubscriptionsForOrganization(Set<SubscriptionStatus> states, Pagination pagination)
            throws OrganizationAuthoritiesException;

    /**
     * Returns a list of Subscriptions which are owned by the defined
     * PlatformUser.
     *
     * @param owner
     *            The PlatformUser whose subscriptions will be retrieved
     * @return the list of {@link Subscription}s owned by the user
     */
    @Deprecated
    public List<Subscription> getSubscriptionsForOwner(PlatformUser owner);

    /**
     * Returns a list of Subscriptions which are owned by the defined
     * PlatformUser. In addition results may be filtered.
     *
     * @param owner
     *            The PlatformUser whose subscriptions will be retrieved
     * @return the list of {@link Subscription}s owned by the user
     */
    List<Subscription> getSubscriptionsForOrganizationWithFiltering(
            Set<SubscriptionStatus> states, org.oscm.paginator.Pagination pagination, Collection<Long> subscriptionKeys)
                    throws OrganizationAuthoritiesException;

    /**
     * Check if usable subscription exist or not for target template product.
     * @param user
     * @param states
     * @param template
     * @return <code>true</code> if the usable subscription for current user
     *         exist
     */
    public boolean isUsableSubscriptionExistForTemplate(PlatformUser user,
            Set<SubscriptionStatus> states, Product template);

    /**
     * Returns a list of Subscriptions owned by the organization of the current user.
     *
     * @return the list of {@link Subscription}s
     */
    public List<Subscription> getAllSubscriptionsForOrganization();

    /**
     * Retrieves the list of user's assignable subscriptions
     *
     * @param pagination
     *          the pagination, sorting, and filtering parameters
     * @param user
     *          user for which subscription will be retrieved
     * @param states
     *          subscription's assignable states
     *
     * @return the list of {@link POSubscription}s which are assignable to user
     */
    public List<POSubscription> getUserAssignableSubscriptions(org.oscm.paginator.Pagination pagination, PlatformUser user, Set<SubscriptionStatus> states);

    /**
     * Retrieves the number of user's assignable subscriptions
     *
     * @param pagination
     *          the pagination, sorting, and filtering parameters
     * @param user
     *          user for which subscription will be retrieved
     * @param states
     *          subscription's assignable states
     * 
     * @return number of subscriptions
     */
    Long getUserAssignableSubscriptionsNumber(PaginationInt pagination,
            PlatformUser user, Set<SubscriptionStatus> states);
}
