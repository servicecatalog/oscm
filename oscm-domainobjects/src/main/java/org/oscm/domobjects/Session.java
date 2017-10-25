/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 02.06.2009                                                      
 *                                                                              
 *  Completion Time: 17.09.2006                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.internal.types.enumtypes.SessionType;

/**
 * JPA managed entity representing the session data.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@NamedQueries( {
        @NamedQuery(name = "Session.findEntriesForSessionId", query = "SELECT session FROM Session session WHERE session.dataContainer.sessionId = :sessionId AND session.dataContainer.sessionType = :sessionType"),
        @NamedQuery(name = "Session.findEntriesForSubscription", query = "SELECT session FROM Session session WHERE session.dataContainer.subscriptionTKey = :subscriptionTKey AND session.dataContainer.sessionType = :sessionType"),
        @NamedQuery(name = "Session.deleteAllEntriesForNode", query = "DELETE FROM Session session WHERE session.dataContainer.nodeName = :nodeName"),
        @NamedQuery(name = "Session.getAllEntriesForNode", query = "SELECT session FROM Session session WHERE session.dataContainer.nodeName = :nodeName"),
        @NamedQuery(name = "Session.getActiveSessionsForUser", query = "SELECT session FROM Session session WHERE session.dataContainer.platformUserKey = :userKey"),
        @NamedQuery(name = "Session.getNumOfActiveSessionsForTechProduct", query = "SELECT count(ses) FROM Session ses, Subscription sub, Product p WHERE ses.dataContainer.subscriptionTKey = sub.key AND sub.product.key = p.key AND p.technicalProduct.key = :technicalProductKey"),
        @NamedQuery(name = "Session.deletePlatformSessionsForSessionId", query = "DELETE FROM Session session WHERE session.dataContainer.sessionId = :sessionId AND session.dataContainer.sessionType = :sessionType"),
        @NamedQuery(name = "Session.findByBusinessKey", query = "SELECT session FROM Session session WHERE session.dataContainer.sessionId = :sessionId AND session.dataContainer.subscriptionTKey = :subscriptionTKey AND session.dataContainer.sessionType = :sessionType") })
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "sessionId",
        "subscriptionTKey", "sessionType" }))
@BusinessKey(attributes = { "sessionId", "subscriptionTKey", "sessionType" })
public class Session extends DomainObjectWithVersioning<SessionData> {

    private static final long serialVersionUID = -3098750184139071859L;

    public Session() {
        super();
        dataContainer = new SessionData();
    }

    public String getPlatformUserId() {
        return dataContainer.getPlatformUserId();
    }

    public long getPlatformUserKey() {
        return dataContainer.getPlatformUserKey();
    }

    public String getSessionId() {
        return dataContainer.getSessionId();
    }

    public SessionType getSessionType() {
        return dataContainer.getSessionType();
    }

    public Long getSubscriptionTKey() {
        return dataContainer.getSubscriptionTKey();
    }

    public String getUserToken() {
        return dataContainer.getUserToken();
    }

    public void setPlatformUserId(String platformUserId) {
        dataContainer.setPlatformUserId(platformUserId);
    }

    public void setPlatformUserKey(long platformUserKey) {
        dataContainer.setPlatformUserKey(platformUserKey);
    }

    public void setSessionId(String sessionId) {
        dataContainer.setSessionId(sessionId);
    }

    public void setSessionType(SessionType sessionType) {
        dataContainer.setSessionType(sessionType);
    }

    public void setSubscriptionTKey(Long subscriptionTKey) {
        dataContainer.setSubscriptionTKey(subscriptionTKey);
    }

    public void setUserToken(String userToken) {
        dataContainer.setUserToken(userToken);
    }

    public String getNodeName() {
        return dataContainer.getNodeName();
    }

    public void setNodeName(String nodeName) {
        dataContainer.setNodeName(nodeName);
    }
}
