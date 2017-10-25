/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.app.business;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.oscm.app.adapter.APPlatformControllerAdapter;
import org.oscm.app.i18n.Messages;
import org.oscm.app.v2_0.exceptions.ControllerLookupException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class APPlatformControllerFactory {

    private static final Logger logger = LoggerFactory
            .getLogger(APPlatformControllerFactory.class);

    public static APPlatformController getInstance(String controllerId)
            throws ControllerLookupException {
        String controllerFullId = APPlatformController.JNDI_PREFIX
                + controllerId;
        try {
            Properties p = new Properties();
            p.setProperty (Context.INITIAL_CONTEXT_FACTORY,"org.apache.openejb.client.LocalInitialContextFactory");

            InitialContext context = new InitialContext(p);
            Object lookup = context.lookup(controllerFullId);
            APPlatformController adapter;

            if (APPlatformController.class.isAssignableFrom(lookup.getClass())) {
                adapter = new APPlatformControllerAdapter(
                        (APPlatformController) lookup);
            } else {
                logger.warn("Exception during controller lookup ["
                        + controllerFullId + "]");
                throw new ControllerLookupException(
                        Messages.getAll("error_controller_lookup"));
            }
            return adapter;
        } catch (Exception e) {
            logger.warn("Exception during controller lookup ["
                    + controllerFullId + "]", e);
            throw new ControllerLookupException(
                    Messages.getAll("error_controller_lookup"), e);
        }
    }
}
