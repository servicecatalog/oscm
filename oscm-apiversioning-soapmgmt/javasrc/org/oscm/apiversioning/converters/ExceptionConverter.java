/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 30, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.converters;

import javax.xml.soap.SOAPException;

/**
 * @author qiu
 * 
 */
public class ExceptionConverter implements IConverter {

    @Override
    public void exec(ConverterContext context, ConverterChain chain)
            throws SOAPException {
        ConverterUtil.convert(context);
    }

}
