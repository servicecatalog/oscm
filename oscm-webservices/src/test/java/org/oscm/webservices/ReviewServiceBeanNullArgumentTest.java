/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Mockito.mock;

import org.oscm.test.NullArgumentTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.internal.bean.ReviewServiceBean;
import org.oscm.intf.ReviewService;

public class ReviewServiceBeanNullArgumentTest extends
        NullArgumentTestBase<ReviewService> {

    public ReviewServiceBeanNullArgumentTest() {
        super(ReviewService.class);
    }

    @Override
    protected ReviewService createInstance(TestContainer container)
            throws Exception {
        container.enableInterfaceMocking(true);
        final org.oscm.intf.ReviewService service = new ReviewServiceWS();
        ((ReviewServiceWS) service).WS_LOGGER = mock(WebServiceLogger.class);
        ((ReviewServiceWS) service).delegate = new ReviewServiceBean();
        container.addBean(service);
        return service;
    }
}
