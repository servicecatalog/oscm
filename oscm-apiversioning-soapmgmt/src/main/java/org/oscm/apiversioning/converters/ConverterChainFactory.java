/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 30, 2015                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.converters;

import org.oscm.apiversioning.enums.ConverterType;

/**
 * @author qiu
 * 
 */
public class ConverterChainFactory {

    public static ConverterChain getConverter(ConverterType type) {
        ConverterChain chain = new ConverterChain();
        if (ConverterType.REQUEST.equals(type)) {
            chain.addConverter(new RequestConverter());
            chain.addConverter(new VOConverter());
        } else if (ConverterType.RESPONSE.equals(type)) {
            chain.addConverter(new ResponseConverter());
            chain.addConverter(new VOConverter());
        } else if (ConverterType.EXCEPTION.equals(type)) {
            chain.addConverter(new ExceptionConverter());
        } else {
            throw new RuntimeException("No coverter is found");
        }
        return chain;
    }
}
