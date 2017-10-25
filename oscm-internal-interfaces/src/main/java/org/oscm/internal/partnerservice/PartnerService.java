/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.partnerservice;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.vo.VOServiceDetails;

@Remote
public interface PartnerService {

    public Response getServiceDetails(long serviceKey)
            throws ObjectNotFoundException, ServiceStateException,
            OperationNotPermittedException;

    public Response updatePartnerServiceDetails(POPartnerServiceDetails details)
            throws ObjectNotFoundException, ServiceStateException,
            OperationNotPermittedException, ConcurrentModificationException;

    public Response getPriceModelLocalization(VOServiceDetails voServiceDetails)
            throws ObjectNotFoundException, OperationNotPermittedException;

    public Response getServiceForMarketplace(long serviceKey, String locale)
            throws ObjectNotFoundException;

    /**
     * returns all data required to display ServiceDetails view for all tabs
     */
    public Response getAllServiceDetailsForMarketplace(long serviceKey,
            String locale, String marketplaceId) throws ObjectNotFoundException, OperationNotPermittedException;

}
