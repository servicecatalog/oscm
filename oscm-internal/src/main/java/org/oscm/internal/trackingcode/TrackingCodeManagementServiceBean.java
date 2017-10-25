/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-8-31                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.trackingcode;

import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.domobjects.Marketplace;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.components.MarketplaceSelector;
import org.oscm.internal.components.POMarketplace;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.trackingCode.POTrackingCode;
import org.oscm.internal.trackingCode.TrackingCodeManagementService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author Zou
 */

@Stateless
@Remote(TrackingCodeManagementService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class TrackingCodeManagementServiceBean implements
        TrackingCodeManagementService {

    @EJB(beanInterface = MarketplaceServiceLocal.class)
    MarketplaceServiceLocal mpServiceLocal;

    @EJB
    MarketplaceSelector marketplaceSelector;

    @RolesAllowed("MARKETPLACE_OWNER")
    public List<POMarketplace> getMarketplaceSelections() {
        return marketplaceSelector.getMarketplaces();
    }

    @Resource
    SessionContext sessionCtx;

    @RolesAllowed("MARKETPLACE_OWNER")
    public Response saveTrackingCode(POTrackingCode trackingCode)
            throws ObjectNotFoundException, ConcurrentModificationException {

        try {
            ArgumentValidator.notNull("trackingCode", trackingCode);
            mpServiceLocal.updateMarketplaceTrackingCode(
                    trackingCode.getMarketplaceId(), trackingCode.getVersion(),
                    trackingCode.getTrackingCode());
            return loadTrackingCodeForMarketplace(trackingCode
                    .getMarketplaceId());
        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } finally {

        }
    }

    public Response loadTrackingCodeForMarketplace(String marketplaceId)
            throws ObjectNotFoundException {

        try {
            ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
            Marketplace marketplace = mpServiceLocal
                    .getMarketplace(marketplaceId);
            return new Response(assemblePOTrackingCode(marketplace));
        } finally {

        }
    }

    POTrackingCode assemblePOTrackingCode(Marketplace marketplace) {
        POTrackingCode poTrackingCode = new POTrackingCode();
        poTrackingCode.setMarketplaceId(marketplace.getMarketplaceId());
        poTrackingCode.setVersion(marketplace.getVersion());
        poTrackingCode.setTrackingCode(marketplace.getTrackingCode());
        return poTrackingCode;
    }
}
