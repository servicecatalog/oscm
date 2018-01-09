/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 02.06.2009                                                      
 *                                                                              
 *  Completion Time: 17.09.2006                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.internal.types.enumtypes.SessionType;

/**
 * JPA managed entity representing the session data.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Embeddable
public class SessionData extends DomainDataContainer {

    private static final long serialVersionUID = -7324727128467372577L;

    /**
     * The technical key of the subscription that this session corresponds to.
     * It is NOT realized as referential constraint!
     */
    private Long subscriptionTKey;

    /**
     * The identifier of the current session.
     */
    private String sessionId;

    /**
     * The user token information for the session.
     */
    private String userToken;

    /**
     * The identifier of the platform user related to the session.
     */
    @Column(nullable = false)
    private String platformUserId;

    /**
     * The technical key of the platform user related to the session.
     */
    @Column(nullable = false)
    private long platformUserKey;

    /**
     * The type of the session.
     */
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    /**
     * The name of the node the session was created on.
     */
    @Column(nullable = false)
    private String nodeName;

    public Long getSubscriptionTKey() {
        return subscriptionTKey;
    }

    public void setSubscriptionTKey(Long subscriptionTKey) {
        this.subscriptionTKey = subscriptionTKey;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getPlatformUserId() {
        return platformUserId;
    }

    public void setPlatformUserId(String platformUserId) {
        this.platformUserId = platformUserId;
    }

    public long getPlatformUserKey() {
        return platformUserKey;
    }

    public void setPlatformUserKey(long platformUserKey) {
        this.platformUserKey = platformUserKey;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

}
