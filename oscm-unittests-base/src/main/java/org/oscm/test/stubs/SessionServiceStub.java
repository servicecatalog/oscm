/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.List;

import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.internal.intf.SessionService;

public class SessionServiceStub implements SessionServiceLocal, SessionService {

    @Override
    public void createPlatformSession(String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createServiceSession(long subscriptionKey, String sessionId,
            String userToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int deletePlatformSession(String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String deleteServiceSession(long subscriptionTKey, String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteSessionsForSessionId(String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getSubscriptionKeysForSessionId(String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String resolveUserToken(long subscriptionKey, String sessionId,
            String userToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllSessions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Session getPlatformSessionForSessionId(String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Session> getProductSessionsForSubscriptionTKey(
            long subscriptionTKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Session> getSessionsForUserKey(long platformUserKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasTechnicalProductActiveSessions(long technicalProductKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteServiceSessionsForSubscription(long subscriptionKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNumberOfServiceSessions(long subscriptionKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UsageLicense findUsageLicense(Subscription subscription,
            String userId) {
        throw new UnsupportedOperationException();
    }

}
