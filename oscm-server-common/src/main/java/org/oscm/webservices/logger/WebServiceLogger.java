/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda
 *                                                                              
 *  Creation Date: Sep 8, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 8, 2011 
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices.logger;

import javax.xml.ws.WebServiceContext;

import org.oscm.logging.Log4jLogger;
import org.oscm.dataservice.local.DataService;
import org.oscm.resolver.IPResolver;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * @author tokoda
 */
public class WebServiceLogger {

    private final Log4jLogger logger;

    public WebServiceLogger(Log4jLogger logger) {
        this.logger = logger;
    }

    public void logAccess(WebServiceContext wsContext, DataService dataService) {
        String ipAddress = IPResolver.resolveIpAddress(wsContext);
        String userId = dataService.getCurrentUser().getUserId();
        logger.logInfo(Log4jLogger.ACCESS_LOG,
                LogMessageIdentifier.INFO_USER_ACCESS_WEBSERVICE, userId, ipAddress);
    }
}
