/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015年1月30日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.converters;

import javax.xml.soap.SOAPException;

/**
 * @author qiu
 * 
 */
public class ResponseConverter implements IConverter {

    @Override
    public void exec(ConverterContext context, ConverterChain chain)
            throws SOAPException {
        ConverterUtil.convert(context);
    }

}
