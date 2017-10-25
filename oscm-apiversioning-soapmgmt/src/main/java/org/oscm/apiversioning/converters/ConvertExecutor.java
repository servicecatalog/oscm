/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 22, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.converters;

import javax.xml.soap.SOAPException;

/**
 * @author qiu
 * 
 */
public class ConvertExecutor {

    public static void convert(ConverterContext context) throws SOAPException {
        ConverterChain chain = ConverterChainFactory.getConverter(context
                .getConverterType());
        chain.exec(context, chain);
    }
}
