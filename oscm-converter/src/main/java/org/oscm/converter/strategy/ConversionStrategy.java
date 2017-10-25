/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 25.06.15 11:21
 *
 *******************************************************************************/

package org.oscm.converter.strategy;

import org.oscm.converter.api.DataServiceHolder;

/**
 * Interface used for conversion between different objects From -> To
 * 
 * @param <From>
 * @param <To>
 */
public interface ConversionStrategy<From, To> extends DataServiceHolder {

    /**
     * Converts object 'To' expected type object.
     * 
     * @param from
     *            - Object used for conversion
     * @return Target object converted from 'From" object
     */
    To convert(From from);
}
