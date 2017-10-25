/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015-01-26                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.billing.service;



import javax.ejb.Remote;

import org.oscm.billing.external.exception.BillingException;

/**
 * Interface for general tasks a billing adapter needs to
 * perform, such as testing the connection to the external 
 * billing system.
 */
@Remote
public interface BillingPluginService {

    /**
     * Tests the connection to the external billing system.
     * 
     * @throws BillingException
     *             if the connection cannot be established
     */
    public void testConnection() throws BillingException;

}
