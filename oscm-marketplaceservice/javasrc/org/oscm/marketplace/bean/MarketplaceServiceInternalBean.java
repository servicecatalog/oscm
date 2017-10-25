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

package org.oscm.marketplace.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.MarketplaceServiceInternal;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOService;

/**
 * Implementation of performance optimized search service functionality. This
 * class contains methods that provide performance optimized access. It is
 * intended only as a temporary optimization and will be replaced by other
 * means. Only for internal usage.
 * 
 * @author tokoda
 */
@Stateless
@Remote(MarketplaceServiceInternal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class MarketplaceServiceInternalBean
        implements MarketplaceServiceInternal {

    @EJB
    private MarketplaceService marketplaceServiceBean;

    //TODO: fix performance hints
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public List<VOCatalogEntry> getMarketplacesForService(VOService service,
            PerformanceHint performanceHint) throws ObjectNotFoundException,
            OperationNotPermittedException {
        return marketplaceServiceBean.getMarketplacesForService(service);
    }

}
