/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Mockito.mock;

import org.oscm.subscriptionservice.bean.ManageSubscriptionBean;
import org.oscm.subscriptionservice.bean.SubscriptionServiceBean;
import org.oscm.subscriptionservice.bean.TerminateSubscriptionBean;
import org.oscm.test.NullArgumentTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.intf.SubscriptionService;

public class SubscriptionServiceBeanNullArgumentTest extends
        NullArgumentTestBase<SubscriptionService> {

    public SubscriptionServiceBeanNullArgumentTest() {
        super(SubscriptionService.class);
        addNullAllowed("subscribeToService", "users");
        addNullAllowed("subscribeToService", "paymentInfo");
        addNullAllowed("subscribeToService", "billingContact");
        addNullAllowed("addRevokeUser", "usersToBeAdded");
        addNullAllowed("addRevokeUser", "usersToBeRevoked");
        addNullAllowed("modifySubscription", "parameters");
        addNullAllowed("abortAsyncSubscription", "reason");
        addNullAllowed("abortAsyncModifySubscription", "reason");
        addNullAllowed("abortAsyncUpgradeSubscription", "reason");
        addNullAllowed("updateAsyncSubscriptionProgress", "progress");
        addNullAllowed("terminateSubscription", "reason");
        addNullAllowed("upgradeSubscription", "paymentInfo");
        addNullAllowed("upgradeSubscription", "billingContact");
        addNullAllowed("modifySubscriptionPaymentData", "paymentInfo");
        addNullAllowed("modifySubscriptionPaymentData", "billingContact");
        addNullAllowed("getSubscriptionsForOrganizationWithFilter",
                "requiredStatus");
        addNullAllowed("modifySubscription", "udas");
        addNullAllowed("subscribeToService", "udas");
        addNullAllowed("upgradeSubscription", "udas");
        addNullAllowed("updateAsyncOperationProgress", "progress");
        addNullAllowed("updateAsyncSubscriptionStatus","instanceInfo");
        addNullAllowed("notifySubscriptionAboutVmsNumber","instanceInfo");
    }

    @Override
    protected SubscriptionService createInstance(TestContainer container)
            throws Exception {
        container.enableInterfaceMocking(true);
        final org.oscm.intf.SubscriptionService service = new SubscriptionServiceWS();
        ((SubscriptionServiceWS) service).WS_LOGGER = mock(WebServiceLogger.class);
        SubscriptionServiceBean delegate = new SubscriptionServiceBean();
        ((SubscriptionServiceWS) service).delegate = delegate;
        delegate.terminateBean = new TerminateSubscriptionBean();
        delegate.manageBean = new ManageSubscriptionBean();
        container.addBean(service);
        return service;
    }

}
