/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Florian Walker                                                 
 *                                                                              
 *  Creation Date: 19.05.2011                                                      
 *                                                                              
 *  Completion Time: 19.05.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Mockito.mock;

import org.oscm.marketplace.bean.CategorizationServiceBean;
import org.oscm.test.NullArgumentTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.intf.CategorizationService;

/**
 * Null-Argument check for the service methods of the categorization service
 * bean
 * 
 * @author Mani Afschar
 * 
 */
public class CategorizationServiceBeanNullArgumentTest extends
        NullArgumentTestBase<CategorizationService> {

    public CategorizationServiceBeanNullArgumentTest() {
        super(CategorizationService.class);
        addNullAllowed("saveCategories", "toBeSaved");
        addNullAllowed("saveCategories", "toBeDeleted");
        addNullAllowed("saveCategories", "locale");
    }

    @Override
    protected CategorizationService createInstance(TestContainer container)
            throws Exception {
        container.enableInterfaceMocking(true);
        final org.oscm.intf.CategorizationService service = new CategorizationServiceWS();
        ((CategorizationServiceWS) service).WS_LOGGER = mock(WebServiceLogger.class);
        ((CategorizationServiceWS) service).delegate = new CategorizationServiceBean();
        container.addBean(service);
        return service;

    }

}
