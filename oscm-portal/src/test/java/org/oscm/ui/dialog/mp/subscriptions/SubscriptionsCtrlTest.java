/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Nov 6, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.subscriptions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.subscriptiondetails.POSubscriptionDetails;
import org.oscm.internal.subscriptions.POSubscriptionForList;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;

@SuppressWarnings("boxing")
public class SubscriptionsCtrlTest {


    public static final String SUBSCRIPTION_ID = "234";
    private SubscriptionsCtrl ctrl;

    private SubscriptionListsLazyDataModel model;
    private SessionBean session;
    private POSubscriptionDetails subscriptionDetails;

    @Before
    public void setup() {

        session = mock(SessionBean.class);
        subscriptionDetails = mock(POSubscriptionDetails.class);
        model = mock(SubscriptionListsLazyDataModel.class);

        ctrl = new SubscriptionsCtrl();
        ctrl.setModel(model);
        ctrl.setSessionBean(session);
        ctrl.setPOSubscriptionDetails(subscriptionDetails);
        ctrl = spy(ctrl);
        doReturn(Boolean.TRUE).when(ctrl).validateSubscriptionStatus(anyString());
    }

    @Test
    public void showSubscriptionDetails_succeed() throws Exception {
        // given
        mockModel();

        // when
        String result = ctrl.showSubscriptionDetails(SUBSCRIPTION_ID, SUBSCRIPTION_ID);

        // then
        assertEquals(BaseBean.OUTCOME_SHOW_DETAILS, result);
    }


    @Test
    public void showSubscriptionDetails_selectedSubscription() throws Exception {
        // given
        mockModel();

        // when
        String result = ctrl.showSubscriptionDetails(SUBSCRIPTION_ID, SUBSCRIPTION_ID);

        // then
        verify(session).setSelectedSubscriptionId(SUBSCRIPTION_ID);
    }

    private void mockModel() {
        POSubscriptionForList subscription = mock(POSubscriptionForList.class);
        when(model.getSelectedSubscription()).thenReturn(subscription);
        when(subscription.getSubscriptionId()).thenReturn(SUBSCRIPTION_ID);
    }

}
