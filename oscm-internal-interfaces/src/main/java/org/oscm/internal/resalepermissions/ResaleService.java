/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 22.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.resalepermissions;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;

/**
 * Contains all functionality related to resale permissions
 * 
 * @author baumann
 */
@Remote
public interface ResaleService {

    /**
     * Retrieves all the services for which the calling organization has a
     * resale permission
     * <p>
     * Required roles: broker manager, reseller manager
     * 
     * if successful return the List of services(List<VOService>) in Result
     * 
     * @return Response object containing the VOService
     */
    public Response getServicesForVendor();

}
