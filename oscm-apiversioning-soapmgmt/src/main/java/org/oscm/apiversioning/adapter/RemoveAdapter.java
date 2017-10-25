/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 30, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.adapter;

import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.oscm.apiversioning.upgrade.info.ModificationDetail;

/**
 * @author qiu
 * 
 */
public class RemoveAdapter implements IAdapter {

    @Override
    public void exec(SOAPMessageContext soapMessageContext,
            ModificationDetail detail) throws SOAPException {
        // TODO Auto-generated method stub

    }

}
