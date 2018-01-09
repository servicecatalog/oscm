/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2012-07-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;


/**
 * Specifies the number of times the value of a service parameter can be set and
 * modified.
 * 
 */
public enum ParameterModificationType {
    /**
     * The parameter value can be set and modified without limitation.
     */
    STANDARD,

    /**
     * The parameter value can be set once, but it cannot be modified later.
     */
    ONE_TIME;
}
