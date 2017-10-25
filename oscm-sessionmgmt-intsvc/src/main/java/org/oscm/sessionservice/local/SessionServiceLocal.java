/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 02.06.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.sessionservice.local;

import java.util.List;

import javax.ejb.Local;

import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;

/**
 * Provides the functionality to identify if there are any users having active
 * sessions with a certain product.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Local
public interface SessionServiceLocal {

    /**
     * Removes all existing data about sessions from the database. Should only
     * be invoked during startup to avoid inconsistent entries.
     */
    public void deleteAllSessions();

    /**
     * Returns the list of currently active product sessions for one
     * subscription.
     * 
     * @param subscriptionTKey
     *            The technical key for the subscription referred to.
     * @return The list of sessions found. In case none was found, an empty list
     *         will be returned.
     */
    public List<Session> getProductSessionsForSubscriptionTKey(
            long subscriptionTKey);

    /**
     * Returns all sessions for the user having the given user key.
     * 
     * @param platformUserKey
     *            The key of the user.
     * @return The sessions for the user.
     */
    public List<Session> getSessionsForUserKey(long platformUserKey);

    /**
     * Returns true if there is an active session for the given technical
     * product key.
     * 
     * @param technicalProductKey
     *            The key of the technical product.
     * @return true if there is an active session for the given technical
     *         product key.
     */
    public boolean hasTechnicalProductActiveSessions(long technicalProductKey);

    /**
     * Returns the current active platform session for the sessionId.
     * 
     * @param sessionId
     *            The sessionID.
     * @return The current platform session will be returned.
     */
    public Session getPlatformSessionForSessionId(String sessionId);

    /**
     * Loads the usage license for the given subscription and user or null if
     * none exists.
     * 
     * @param subscription
     *            given subscription
     * @param userId
     *            the userid of the usage license for
     * @return usage licenses
     */
    public UsageLicense findUsageLicense(Subscription subscription,
            String userId);

}
