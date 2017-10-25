/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 01.09.16 08:10
 *
 ******************************************************************************/

package org.oscm.internal.types.exception;

/**
 * Authored by dawidch
 */
public class NotExistentTenantException extends SaaSApplicationException {

    public NotExistentTenantException() {
        super();
    }

    public NotExistentTenantException(Reason reason) {
        super(String.valueOf(reason));
    }

    public NotExistentTenantException(String message) {
        super(message);
    }

    public enum Reason {
        TENANT_NOT_FOUND, MISSING_TENANT_PARAMETER, MISSING_TEANT_ID_IN_SAML
    }
}
