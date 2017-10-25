/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 28, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 28, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOService;

/**
 * Remote interface of the marketplace management service. This interface
 * contains methods that provide performance optimized access. It is intended
 * only as a temporary optimization and will be replaced by other means. Only
 * for internal usage.
 * 
 * @author tokoda
 */

public interface MarketplaceServiceInternal {

    /**
     * Retrieves the catalog entries from all marketplaces to which the
     * specified service has been published.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service
     * 
     * @param service
     *            the service for which the catalog entries are to be retrieved
     * @param performanceHint
     *            a <code>performanceHint</code> constant specifying the data to
     *            include in the result. This can be used to increase the search
     *            performance.
     * @return the catalog entries of the marketplaces the given service has
     *         been published to
     * @throws ObjectNotFoundException
     *             if the given service is not found by its key
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             service
     */
    List<VOCatalogEntry> getMarketplacesForService(VOService service,
                                                   PerformanceHint performanceHint) throws ObjectNotFoundException,
            OperationNotPermittedException;

}
