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

import org.oscm.marketplace.bean.MarketplaceServiceBean;
import org.oscm.test.NullArgumentTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.intf.MarketplaceService;

/**
 * Null-Argument check for the service methods of the MPL service bean
 * 
 * @author Florian Walker
 * 
 */
public class MarketplaceServiceBeanNullArgumentTest extends
        NullArgumentTestBase<MarketplaceService> {

    public MarketplaceServiceBeanNullArgumentTest() {

        super(MarketplaceService.class);
        addNullAllowed("getMarketplaceForSubscription", "locale");
        addNullAllowed("saveBrandingUrl", "brandingUrl");
    }

    @Override
    protected MarketplaceService createInstance(TestContainer container)
            throws Exception {
        container.enableInterfaceMocking(true);
        final org.oscm.intf.MarketplaceService service = new MarketplaceServiceWS();
        ((MarketplaceServiceWS) service).WS_LOGGER = mock(WebServiceLogger.class);
        ((MarketplaceServiceWS) service).delegate = new MarketplaceServiceBean();
        container.addBean(service);
        return service;

    }

}
