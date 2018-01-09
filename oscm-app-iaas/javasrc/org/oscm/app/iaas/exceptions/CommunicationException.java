/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Apr 16, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.exceptions;

import org.oscm.app.iaas.i18n.Messages;
import org.oscm.app.v2_0.exceptions.SuspendException;

/**
 * @author zankov
 * 
 */
public class CommunicationException extends IaasException {

    private static final long serialVersionUID = -218011491239233714L;
    private String hostName;

    public CommunicationException(String message, String hostName) {
        super(message);
        this.hostName = hostName;
    }

    @Override
    public boolean isBusyMessage() {
        return false;
    }

    @Override
    public boolean isIllegalState() {
        return false;
    }

    /**
     * Creates a SuspendException related to this exception.
     * 
     * @return the SuspendException
     */
    public SuspendException getSuspendException() {
        return new SuspendException(Messages.getAll(
                "error_communication_failed", new Object[] { getHostName() }));
    }

    /**
     * Returns the host name
     * 
     * @return the resource type
     */
    public String getHostName() {
        return this.hostName;
    }
}
