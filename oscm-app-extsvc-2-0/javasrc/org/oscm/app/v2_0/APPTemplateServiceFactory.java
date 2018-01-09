/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 20.09.17 07:03
 *
 ******************************************************************************/

package org.oscm.app.v2_0;

import org.oscm.app.v2_0.intf.APPTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

import static org.oscm.app.v2_0.intf.APPTemplateService.JNDI_NAME;

/**
 * Created by BadziakP on 2017-09-19.
 */
public class APPTemplateServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(APPlatformServiceFactory.class);

    private APPTemplateServiceFactory() {}

    public static APPTemplateService getInstance() {
        try {
            Properties p = new Properties();
            p.setProperty (Context.INITIAL_CONTEXT_FACTORY,"org.apache.openejb.client.LocalInitialContextFactory");

            InitialContext context = new InitialContext(p);
            Object lookup = context.lookup(JNDI_NAME);
            if (!APPTemplateService.class.isAssignableFrom(lookup.getClass())) {
                throw new IllegalStateException(
                    "Failed to look up APPlatformService. The returned service is not implementing correct interface");
            }
            return (APPTemplateService) lookup;
        } catch (NamingException e) {
            logger.error("Service lookup failed: " + JNDI_NAME);
            throw new IllegalStateException(
                "No valid platform service available", e);
        }
    }
}
