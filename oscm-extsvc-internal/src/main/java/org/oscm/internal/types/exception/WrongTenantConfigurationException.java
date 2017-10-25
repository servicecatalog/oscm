/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 20.10.16 10:15
 *
 ******************************************************************************/

package org.oscm.internal.types.exception;

/**
 * Authored by dawidch
 */
public class WrongTenantConfigurationException extends SaaSApplicationException {

    public WrongTenantConfigurationException() {
        super();
    }


    public WrongTenantConfigurationException(String message) {
        super(message);
    }

}
