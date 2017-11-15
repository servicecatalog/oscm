/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.sessionservice.bean;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.types.exception.ServiceParameterException;

/**
 * Bean implementation of the product session management component.
 */
@Stateless
@Local(SessionServiceLocal.class)
@Remote(SessionService.class)
@Interceptors(InvocationDateContainer.class)
public class SessionManagementStub2 implements SessionServiceLocal,
        SessionService {

    public void deleteAllSessions() {
    }

    public List<Session> getProductSessionsForSubscriptionTKey(
            long subscriptionTKey) {
        return new ArrayList<Session>();
    }

    public void createServiceSession(long subscriptionKey, String sessionId,
            String userToken) throws ServiceParameterException {
    }

    public String deleteServiceSession(long subscriptionTKey, String sessionId) {
        return null;
    }

    public void deleteSessionsForSessionId(String sessionId) {
    }

    public List<Long> getSubscriptionKeysForSessionId(String sessionId) {
        return new ArrayList<Long>();
    }

    public String resolveUserToken(long subscriptionKey, String sessionId,
            String userToken) {
        return null;
    }

    public void createPlatformSession(String sessionId) {
    }

    public List<Session> getSessionsForUserKey(long platformUserKey) {
        return null;
    }

    public boolean hasTechnicalProductActiveSessions(long technicalProductKey) {
        return false;
    }

    public int deletePlatformSession(String sessionId) {
        return 0;
    }

    public Session getPlatformSessionForSessionId(String sessionId) {
        return null;
    }

    public void deleteServiceSessionsForSubscription(long subscriptionKey) {
    }


    public int getNumberOfServiceSessions(long subscriptionKey) {
        return 0;
    }

    public UsageLicense findUsageLicense(Subscription subscription,
            String userId) {
        return null;
    }

}
