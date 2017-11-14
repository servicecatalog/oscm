/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Mockito.mock;

import org.oscm.billingservice.service.BillingServiceBean;
import org.oscm.test.NullArgumentTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.intf.BillingService;

public class BillingServiceBeanNullArgumentTest extends
        NullArgumentTestBase<BillingService> {

    public BillingServiceBeanNullArgumentTest() {
        super(BillingService.class);
        addNullAllowed("getCustomerBillingData", "from");
        addNullAllowed("getCustomerBillingData", "to");
        addNullAllowed("getCustomerBillingData", "organizationIds");
        addNullAllowed("getRevenueShareData", "from");
        addNullAllowed("getRevenueShareData", "to");
        addNullAllowed("getRevenueShareData", "resultType");
    }

    @Override
    protected BillingService createInstance(TestContainer container)
            throws Exception {
        container.enableInterfaceMocking(true);
        final org.oscm.intf.BillingService service = new BillingServiceWS();
        ((BillingServiceWS) service).WS_LOGGER = mock(WebServiceLogger.class);
        ((BillingServiceWS) service).delegate = new BillingServiceBean();
        container.addBean(service);
        return service;
    }
}
