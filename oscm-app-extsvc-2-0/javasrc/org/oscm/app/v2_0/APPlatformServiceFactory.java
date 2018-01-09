/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2012-09-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.oscm.app.v2_0.intf.APPlatformService.JNDI_NAME;

/**
 * Factory for creating <code>APPlatformService</code> instances.
 */
public class APPlatformServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(APPlatformServiceFactory.class);

    /**
     * Retrieves an <code>APPlatformService</code> instance.
     * 
     * @return the service instance
     */
    public static APPlatformService getInstance() {
        try {

            Properties p = new Properties();
            p.setProperty (Context.INITIAL_CONTEXT_FACTORY,"org.apache.openejb.client.LocalInitialContextFactory");

            InitialContext context = new InitialContext(p);
            Object lookup = context.lookup(JNDI_NAME);
            if (!APPlatformService.class.isAssignableFrom(lookup.getClass())) {
                throw new IllegalStateException(
                        "Failed to look up APPlatformService. The returned service is not implementing correct interface");
            }
            return (APPlatformService) lookup;
        } catch (NamingException e) {
            logger.error("Service lookup failed: " + JNDI_NAME);
            throw new IllegalStateException(
                    "No valid platform service available", e);
        }
    }
}
