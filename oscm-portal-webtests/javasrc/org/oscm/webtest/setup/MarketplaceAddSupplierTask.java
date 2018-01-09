/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Ronny Weiser                                                      
 *                                                                              
 *  Creation Date: Sep 16, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 16, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.Arrays;

import org.oscm.internal.intf.MarketplaceService;

/**
 * Custom ANT task granting publishing roles for a supplier to a marketplace
 * using the WS-API.
 * 
 * @author weiser
 */
public class MarketplaceAddSupplierTask extends WebtestTask {

    private String supplierId;
    private String marketplaceId;

    @Override
    public void executeInternal() throws Exception {
        MarketplaceService mpSvc = getServiceInterface(MarketplaceService.class);
        mpSvc.addOrganizationsToMarketplace(
                Arrays.asList(new String[] { supplierId }), marketplaceId);
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }
}
