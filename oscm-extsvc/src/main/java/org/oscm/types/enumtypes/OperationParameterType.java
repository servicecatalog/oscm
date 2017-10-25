/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2014-01-29                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the supported types for the values of service operation parameters.
 */
public enum OperationParameterType {

    /**
     * The parameter values are strings.
     */
    INPUT_STRING,

    /**
     * The parameter values are selected from a predefined list of values.
     */
    REQUEST_SELECT;

}
