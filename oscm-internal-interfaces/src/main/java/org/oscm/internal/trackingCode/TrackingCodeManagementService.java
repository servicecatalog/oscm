/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-8-31                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.trackingCode;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.components.POMarketplace;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Contains all functionality related to tracking codes
 * 
 * @author Zou
 * 
 */
@Remote
public interface TrackingCodeManagementService {

    /**
     * returns a list of selectable marketplaces
     */
    List<POMarketplace> getMarketplaceSelections();

    /**
     * save the tracking code//
     * 
     * if save is successful:
     * 
     * - return updated POTrackingCode in Result
     * 
     * @param POTrackingCode
     * @return response
     * @throws ObjectNotFoundException
     *             if no marketplace is found with the given identifier
     * @throws ConcurrentModificationException
     *             if the given marketplace was modified by another user between
     *             the moment of loading and savig it
     */
    Response saveTrackingCode(POTrackingCode trackingCode)
            throws ObjectNotFoundException, ConcurrentModificationException;

    /**
     * returns the POTrackingCode for the selected marketplaceId//
     * 
     * if save is successful:
     * 
     * - return POTrackingCode in Response
     * 
     * @param marketplaceId
     * @throws ObjectNotFoundException
     *             if no marketplace is found with the given identifier
     * 
     */
    Response loadTrackingCodeForMarketplace(String marketplaceId)
            throws ObjectNotFoundException;
}
