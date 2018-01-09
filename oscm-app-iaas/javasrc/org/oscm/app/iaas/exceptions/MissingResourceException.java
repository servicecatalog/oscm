/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 26.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.exceptions;

import org.oscm.app.iaas.data.ResourceType;
import org.oscm.app.iaas.i18n.Messages;
import org.oscm.app.v2_0.exceptions.SuspendException;

/**
 * This exception is thrown when the IaaS system signals that a referenced
 * resource is no longer available.
 */
public class MissingResourceException extends IaasException {

    private static final long serialVersionUID = 6700736857310191919L;

    private ResourceType type;
    private String id;

    public MissingResourceException(String message, ResourceType type, String id) {
        super(message);
        this.type = type;
        this.id = id;
    }

    @Override
    public boolean isBusyMessage() {
        return false;
    }

    /**
     * Returns the type of resource that is missing.
     * 
     * @return the resource type
     */
    public ResourceType getResouceType() {
        return type;
    }

    /**
     * Returns the id of the missing resource.
     * 
     * @return the id
     */
    public String getResouceId() {
        return id;
    }

    /**
     * Creates a SuspendException related to this exception.
     * 
     * @return the SuspendException
     */
    public SuspendException getSuspendException() {
        if (ResourceType.UNKNOWN.equals(getResouceType())) {
            return new SuspendException(Messages.getAll(
                    "error_resource_missing_unknown_type", new Object[] {
                            getResouceType(), getResouceId(), getMessage() }));
        } else {
            return new SuspendException(Messages.getAll(
                    "error_resource_missing", new Object[] { getResouceType(),
                            getResouceId() }));
        }
    }

    @Override
    public boolean isIllegalState() {
        return false;
    }
}
