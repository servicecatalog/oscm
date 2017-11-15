/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2014-02-07      
 *  
 *  author goebel
 *                                                                              
 *******************************************************************************/
package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Subscription;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.SubscriptionStateException;

/**
 * @author goebel
 */
public class ValidateSubscriptionStateBeanTest {

    private ValidateSubscriptionStateBean bean = new ValidateSubscriptionStateBean();

    @Before
    public void setup() throws Exception {
        bean.sessionCtx = mock(SessionContext.class);
    }

    @Test
    public void checkAbortAllowedForModifying_ACTIVE() {
        // given
        Subscription sb = new Subscription();
        sb.setStatus(SubscriptionStatus.ACTIVE);

        try {
            bean.checkAbortAllowedForModifying(sb);
            fail();
        } catch (SubscriptionStateException ex) {
            // then
            assertEquals(
                    "ex.SubscriptionStateException.SUBSCRIPTION_INVALID_STATE",
                    ex.getMessageKey());
        }
    }

    @Test
    public void checkUnsubscribingAllowed_SUSPENDED() throws Exception {
        // given
        Subscription sb = mock(Subscription.class);
        sb.setStatus(SubscriptionStatus.SUSPENDED);

        // when
        bean.checkUnsubscribingAllowed(sb);
    }

    @Test
    public void checkUnsubscribingAllowed_SUSPENDED_UPD() throws Exception {
        // given
        Subscription sb = mock(Subscription.class);
        sb.setStatus(SubscriptionStatus.SUSPENDED_UPD);

        // when
        bean.checkUnsubscribingAllowed(sb);
    }

    @Test
    public void checkUnsubscribingAllowed_PENDING() throws Exception {
        // given
        Subscription sb = mock(Subscription.class);
        sb.setStatus(SubscriptionStatus.PENDING);

        // when
        bean.checkUnsubscribingAllowed(sb);
    }

    @Test
    public void checkUnsubscribingAllowed_PENDING_UPD() throws Exception {
        // given
        Subscription sb = mock(Subscription.class);
        sb.setStatus(SubscriptionStatus.PENDING_UPD);

        // when
        bean.checkUnsubscribingAllowed(sb);
    }

}
