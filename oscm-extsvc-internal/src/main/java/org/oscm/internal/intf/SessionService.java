/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-05                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ServiceParameterException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Remote interface for storing, retrieving, and deleting session data.
 * 
 */
@Remote
public interface SessionService {

    /**
     * Creates an entry for a service session with the specified data in the
     * database. The entry can be used later for direct access to the underlying
     * application without a previous login to the platform.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param subscriptionKey
     *            the numeric key of the subscription used in the session
     * @param sessionId
     *            the identifier of the session
     * @param userToken
     *            the user token for the session; used for later 'resolve user
     *            token' calls
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws ServiceParameterException
     *             if the session count for the subscription is greater than the
     *             value of the <code>CONCURRENT_USER</code> parameter of the
     *             underlying technical service, or if the subscription is
     *             expired
     * @throws OperationNotPermittedException
     *             if the subscription has an invalid status or belongs to
     *             another organization
     * @throws ValidationException
     *             if input values are invalid (e.g. the session ID is too long
     *             or not set)
     */

    public void createServiceSession(long subscriptionKey, String sessionId,
            String userToken) throws ObjectNotFoundException,
            ServiceParameterException, OperationNotPermittedException,
            ValidationException;

    /**
     * Deletes the service session entry identified by the given parameters from
     * the database. If the entry is not found, no action is executed.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param subscriptionKey
     *            the numeric key of the subscription related to the session
     * @param sessionId
     *            the session identifier
     * @return the URL of the logout page of the underlying service. This can be
     *         used for redirections.
     */

    public String deleteServiceSession(long subscriptionKey, String sessionId);

    /**
     * Deletes all service session entries for the given subscription from the
     * database. If no entry is found, no action is executed.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscriptionKey
     *            the numeric key of the subscription related to the sessions
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             subscription
     */

    public void deleteServiceSessionsForSubscription(long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Returns the number of service session entries for the given subscription.
     * 
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscriptionKey
     *            the numeric key of the subscription related to the sessions
     * @return the number of session entries
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             subscription
     */

    public int getNumberOfServiceSessions(long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Checks if a service session entry with the given parameters exists in the
     * database.
     * <p>
     * If an entry is found, a valid session exists. If this is the case and the
     * specified user token can be resolved, the method triggers the recording
     * of a corresponding event.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param subscriptionKey
     *            the numeric key of the subscription related to the session
     * @param sessionId
     *            the session identifier
     * @param userToken
     *            the user token to be resolved
     * 
     * @return the ID of the user related to the session, or <code>null</code>
     *         if no session entry is found
     */

    public String resolveUserToken(long subscriptionKey, String sessionId,
            String userToken);

    /**
     * Deletes all session entries with the given session ID from the database.
     * <p>
     * The user of a specific session can work with several services or the
     * platform as such. Therefore, several entries for one session ID may exist
     * in the database. In specific situations, for example, a timeout, all
     * these entries must be deleted.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param sessionId
     *            the session identifier
     */

    public void deleteSessionsForSessionId(String sessionId);

    /**
     * Returns the keys of the subscriptions which are currently used in the
     * session with the specified ID.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param sessionId
     *            the session identifier
     * @return a list of the numeric subscriptions keys. If no subscription is
     *         found, an empty list is returned.
     */

    public List<Long> getSubscriptionKeysForSessionId(String sessionId);

    /**
     * Creates an entry for a platform session in the database.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param sessionId
     *            the identifier of the session
     * @throws ValidationException
     *             if input values are invalid (e.g. the session ID is too long
     *             or not set)
     */

    public void createPlatformSession(String sessionId)
            throws ValidationException;

    /**
     * Deletes the platform session entries with the given session ID from the
     * database.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param sessionId
     *            the session identifier
     * @return the number of deleted session entries
     */

    public int deletePlatformSession(String sessionId);

}
