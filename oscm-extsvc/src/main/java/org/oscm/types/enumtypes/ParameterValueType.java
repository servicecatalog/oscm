/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-06-29                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;


/**
 * Specifies the supported data types for the values of service parameters.
 * 
 * @author pock
 * 
 */
public enum ParameterValueType {
    /**
     * The parameter values are booleans.
     */
    BOOLEAN,

    /**
     * The parameter values are integers.
     */
    INTEGER,

    /**
     * The parameter values are longs.
     */
    LONG,

    /**
     * The parameter values are strings.
     */
    STRING,

    /**
     * The parameter values are durations in days, in a decimal representation
     * of <code>dec(n,2)</code>.
     */
    DURATION,
    /**
     * The parameter values are selected from a given list of options.
     */
    ENUMERATION,

    /**
     * The parameter values are strings with hidden input field and encrypted data storage.
     */
    PWD

}
