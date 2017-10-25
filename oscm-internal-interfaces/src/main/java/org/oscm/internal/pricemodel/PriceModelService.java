/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 12.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricemodel;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * @author weiser
 * 
 */
@Remote
public interface PriceModelService {

    List<POCustomer> getCustomers() throws SaaSApplicationException;

    Response deleteCustomerSpecificServices(List<POCustomerService> services)
            throws SaaSApplicationException;

    List<POCustomerService> getCustomerSpecificServices(String customerId)
            throws SaaSApplicationException;
}
