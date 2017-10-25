/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 30, 2015                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.converters;

import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.SOAPException;

/**
 * @author qiu
 * 
 */
public class ConverterChain implements IConverter {

    private List<IConverter> converters = new ArrayList<IConverter>();
    private int index = 0;

    public ConverterChain addConverter(IConverter converter) {
        this.converters.add(converter);
        return this;
    }

    @Override
    public void exec(ConverterContext context, ConverterChain chain)
            throws SOAPException {
        if (index == converters.size()) {
            return;
        }
        IConverter converter = converters.get(index);
        index++;
        converter.exec(context, chain);
    }

}
