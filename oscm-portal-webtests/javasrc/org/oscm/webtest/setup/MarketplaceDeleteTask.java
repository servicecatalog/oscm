/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Sep 7, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 7, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * Custom ANT task deleting marketplaces using the WS-API.
 * 
 * @author Dirk Bernsau
 * 
 */
public class MarketplaceDeleteTask extends WebtestTask {

    private String mpIds;
    private boolean failonerror = true;

    public void setIds(String value) {
        mpIds = value;
    }

    public void setFailOnError(String value) {
        failonerror = Boolean.parseBoolean(value);
    }

    @Override
    public void executeInternal() throws Exception {

        MarketplaceService mpSvc = getServiceInterface(MarketplaceService.class);
        String[] split = mpIds.split(",");
        String id = "";
        for (int i = 0; i < split.length; i++) {
            if (split[i].trim().length() > 0) {
                try {
                    id = split[i].trim();
                    mpSvc.deleteMarketplace(id);
                    log("Deleted marketplace with ID " + id);
                } catch (SaaSApplicationException e) {
                    if (failonerror) {
                        throw e;
                    } else {
                        if (e instanceof ObjectNotFoundException) {
                            handleErrorOutput("Marketplace with ID " + id
                                    + " does not exist");
                        } else {
                            handleErrorOutput("Problem while deleting marketplace with id "
                                    + id + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
