/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mani Afschar                                                    
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *  Completion Time: 10.10.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.local;

import java.io.IOException;

import javax.ejb.Local;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.oscm.paymentservice.adapter.PaymentServiceProviderAdapter;

/**
 * The local interface for port locator of the payment processing.
 * 
 * @author Mani Afschar
 * 
 */

@Local
public interface PortLocatorLocal {

    /**
     * Determines all currently non-handled BillingResult objects and, if the
     * customer uses a PSP relevant PaymentInfo, invokes the payment process for
     * it.
     * 
     * @return <code>true</code> in case the charging operations succeeded for
     *         all open bills, <code>false</code> otherwise.
     */
    public PaymentServiceProviderAdapter getPort(String wsdl)
            throws IOException, WSDLException, ParserConfigurationException;

}
