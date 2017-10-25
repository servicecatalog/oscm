/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 ******************************************************************************/
package org.oscm.internal.types.exception;

/**
 * Created by PLGrubskiM on 2017-07-21.
 */
public class MarketplaceValidationException extends SaaSApplicationException{

    public MarketplaceValidationException() {

    }

    public MarketplaceValidationException(String message) {
        super(message);
    }
}
