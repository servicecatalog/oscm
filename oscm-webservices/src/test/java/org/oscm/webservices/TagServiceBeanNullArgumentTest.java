/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 10.05.2011                                                      
 *                                                                              
 *  Completion Time: 10.05.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Mockito.mock;

import org.oscm.serviceprovisioningservice.bean.TagServiceBean;
import org.oscm.test.NullArgumentTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.intf.TagService;

/**
 * @author weiser
 * 
 */
public class TagServiceBeanNullArgumentTest extends
        NullArgumentTestBase<TagService> {

    public TagServiceBeanNullArgumentTest() {
        super(TagService.class);
    }

    @Override
    protected TagService createInstance(TestContainer container)
            throws Exception {
        container.enableInterfaceMocking(true);
        final org.oscm.intf.TagService service = new TagServiceWS();
        ((TagServiceWS) service).WS_LOGGER = mock(WebServiceLogger.class);
        ((TagServiceWS) service).delegate = new TagServiceBean();
        container.addBean(service);
        return service;
    }

}
