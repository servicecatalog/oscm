/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.internal.types.exception;

/**
 * Created by PLGrubskiM on 2017-07-18.
 */
public class InvalidSigningAlgorithmException extends SaaSApplicationException {

    public InvalidSigningAlgorithmException() {
    }

    public InvalidSigningAlgorithmException(String message) {
        super(message);
    }
}
