/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Mockito.mock;

import org.oscm.sessionservice.bean.SessionServiceBean;
import org.oscm.test.NullArgumentTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.intf.SessionService;

public class SessionServiceBeanNullArgumentTest extends
        NullArgumentTestBase<SessionService> {

    public SessionServiceBeanNullArgumentTest() {
        super(SessionService.class);
    }

    @Override
    protected SessionService createInstance(TestContainer container)
            throws Exception {

        container.enableInterfaceMocking(true);
        final org.oscm.intf.SessionService service = new SessionServiceWS();
        ((SessionServiceWS) service).WS_LOGGER = mock(WebServiceLogger.class);
        ((SessionServiceWS) service).delegate = new SessionServiceBean();
        container.addBean(service);
        return service;
    }
}
