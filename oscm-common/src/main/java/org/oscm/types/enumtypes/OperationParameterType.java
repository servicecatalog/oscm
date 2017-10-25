/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Represents the possible operation parameter types.
 * 
 * @author weiser
 * 
 */
public enum OperationParameterType {

    INPUT_STRING(false),

    REQUEST_SELECT(true);

    private boolean requestValues;

    private OperationParameterType(boolean requestValues) {
        this.requestValues = requestValues;
    }

    /**
     * Check if for this {@link OperationParameterType} a request of the
     * possible values from the external system is necessary.
     * 
     * @return true if values must be requested from the external system
     */
    public boolean isRequestValues() {
        return requestValues;
    }

}
