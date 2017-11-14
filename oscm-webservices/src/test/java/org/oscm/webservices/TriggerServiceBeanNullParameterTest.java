/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Mockito.mock;

import org.oscm.test.NullArgumentTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.triggerservice.bean.TriggerServiceBean;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.intf.TriggerService;

public class TriggerServiceBeanNullParameterTest extends
        NullArgumentTestBase<TriggerService> {

    public TriggerServiceBeanNullParameterTest() {
        super(TriggerService.class);
        addNullAllowed("cancelActions", "actionKeys");
        addNullAllowed("cancelActions", "reason");
        addNullAllowed("deleteActions", "actionKeys");
        addNullAllowed("rejectAction", "reason");
    }

    @Override
    protected TriggerService createInstance(TestContainer container)
            throws Exception {
        container.enableInterfaceMocking(true);
        final org.oscm.intf.TriggerService service = new TriggerServiceWS();
        ((TriggerServiceWS) service).delegate = new TriggerServiceBean();
        ((TriggerServiceWS) service).WS_LOGGER = mock(WebServiceLogger.class);
        container.addBean(service);
        return service;
    }

}
