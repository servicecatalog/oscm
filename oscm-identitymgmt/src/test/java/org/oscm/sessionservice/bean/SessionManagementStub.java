/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.sessionservice.bean;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.sessionservice.local.SessionServiceLocal;

@Stateless
public class SessionManagementStub implements SessionServiceLocal {

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deleteAllSessions() {

    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Session> getProductSessionsForSubscriptionTKey(
            long subscriptionTKey) {

        return null;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Session> getSessionsForUserKey(long platformUserKey) {

        return new ArrayList<Session>();
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean hasTechnicalProductActiveSessions(long technicalProductKey) {

        return false;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Session getPlatformSessionForSessionId(String sessionId) {

        return null;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public UsageLicense findUsageLicense(Subscription subscription,
            String userId) {

        return null;
    }

}
