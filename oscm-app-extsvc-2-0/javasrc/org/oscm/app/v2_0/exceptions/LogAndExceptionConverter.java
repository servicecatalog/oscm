/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 04.06.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.exceptions;

import javax.ejb.EJBException;

import org.oscm.app.v2_0.data.Context;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogAndExceptionConverter {

    /**
     * Convert given exception into platform exception.
     * 
     * @param t
     *            the exception to handle
     * @param context
     *            an optional action context
     * @return the newly generated platform exception
     */
    public static APPlatformException createAndLogPlatformException(Throwable t,
            Context context) {

        // Get real error cause
        if (t instanceof EJBException) {
            if (t.getCause() != null) {
                t = t.getCause();
            }
        }
        APPlatformException result = null;
        Logger logger = null;
        String method = "";
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // There is always one caller => stack trace will always contain two
        // elements
        logger = LoggerFactory.getLogger(stackTrace[1].getClassName());
        method = stackTrace[1].getMethodName() + "() failed: ";
        if (t instanceof APPlatformException) {
            result = (APPlatformException) t;
        } else {
            logger.error("Wrapping APPlatformException for: ", t);
            // Get real error cause
            if (t.getClass().getName().endsWith("OViSSException")
                    && t.getCause() != null) {
                t = t.getCause();
            }

            // Map to platform exception
            String causeMessage = (t.getMessage() != null) ? t.getMessage()
                    : t.getClass().getName();
            String id = context != null ? context.name() : "undefined";
            result = new APPlatformException(
                    Messages.getAll("error_overall_" + id, causeMessage));
        }
        logger.error(
                method + result.getLocalizedMessage(Messages.DEFAULT_LOCALE));
        return result;
    }

    /**
     * Generate a meaningful log text from provisioning settings and instance
     * ID.
     * 
     * @param instanceId
     * @param settings
     * @return the log text
     */
    public static String getLogText(String instanceId,
            ProvisioningSettings settings) {
        StringBuffer sb = new StringBuffer();
        if (instanceId != null) {
            sb.append("InstanceID: ");
            sb.append(instanceId);
        }
        if (settings != null) {
            sb.append(" | OrganizationID: ");
            sb.append(settings.getOrganizationId());
            sb.append(" | SubscriptionID: ");
            sb.append(settings.getSubscriptionId());
            if (settings.getRequestingUser() != null) {
                sb.append(" | RequestingUser: ");
                sb.append(settings.getRequestingUser().getUserId());
            }
        }
        return sb.toString();
    }
}
