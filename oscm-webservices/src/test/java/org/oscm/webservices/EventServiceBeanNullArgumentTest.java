/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Mockito.mock;

import org.oscm.eventservice.bean.EventServiceBean;
import org.oscm.test.NullArgumentTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.intf.EventService;

public class EventServiceBeanNullArgumentTest extends
        NullArgumentTestBase<EventService> {

    public EventServiceBeanNullArgumentTest() {
        super(EventService.class);
    }

    @Override
    protected EventService createInstance(TestContainer container)
            throws Exception {
        container.enableInterfaceMocking(true);
        final org.oscm.intf.EventService service = new EventServiceWS();
        ((EventServiceWS) service).WS_LOGGER = mock(WebServiceLogger.class);
        ((EventServiceWS) service).delegate = new EventServiceBean();
        container.addBean(service);
        return service;
    }

}
