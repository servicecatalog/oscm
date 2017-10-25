/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-05                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.ServiceParameterException;
import org.oscm.types.exceptions.ValidationException;

/**
 * Remote interface for storing, retrieving, and deleting session data.
 * 
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
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
    @WebMethod
    public void createServiceSession(
            @WebParam(name = "subscriptionKey") long subscriptionKey,
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "userToken") String userToken)
            throws ObjectNotFoundException, ServiceParameterException,
            OperationNotPermittedException, ValidationException;

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
    @WebMethod
    public String deleteServiceSession(
            @WebParam(name = "subscriptionKey") long subscriptionKey,
            @WebParam(name = "sessionId") String sessionId);

    /**
     * Deletes all service session entries for the given subscription from the
     * database. If no entry is found, no action is executed.
     * <p>
     * Required role: administrator of the organization the subscription belongs
     * to, or subscription manager who owns the subscription
     * 
     * @param subscriptionKey
     *            the numeric key of the subscription related to the sessions
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     *             if the subscription does not belong to the calling user's
     *             organization, or if the calling user is a subscription
     *             manager but not the owner of the subscription
     */
    @WebMethod
    public void deleteServiceSessionsForSubscription(
            @WebParam(name = "subscriptionKey") long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Returns the number of service session entries for the given subscription.
     * 
     * Required role: administrator of the organization the subscription belongs
     * to, or subscription manager who owns the subscription
     * 
     * @param subscriptionKey
     *            the numeric key of the subscription related to the sessions
     * @return the number of session entries
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     *             if the subscription does not belong to the calling user's
     *             organization, or if the calling user is a subscription
     *             manager but not the owner of the subscription
     */
    @WebMethod
    public int getNumberOfServiceSessions(
            @WebParam(name = "subscriptionKey") long subscriptionKey)
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
    @WebMethod
    public String resolveUserToken(
            @WebParam(name = "subscriptionKey") long subscriptionKey,
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "userToken") String userToken);

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
    @WebMethod
    public void deleteSessionsForSessionId(
            @WebParam(name = "sessionId") String sessionId);

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
    @WebMethod
    public List<Long> getSubscriptionKeysForSessionId(
            @WebParam(name = "sessionId") String sessionId);

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
    @WebMethod
    public void createPlatformSession(
            @WebParam(name = "sessionId") String sessionId)
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
    @WebMethod
    public int deletePlatformSession(
            @WebParam(name = "sessionId") String sessionId);

}
