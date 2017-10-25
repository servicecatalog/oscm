/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.landingpageconfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.domobjects.PublicLandingpage;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.landingpageService.local.VOLandingpageService;
import org.oscm.landingpageService.local.VOPublicLandingpage;
import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.internal.components.MarketplaceSelector;
import org.oscm.internal.components.POMarketplace;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.LandingpageType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exceptions.FillinOptionNotSupportedException;
import org.oscm.internal.vo.VOService;

@Stateless
@Remote(LandingpageConfigurationService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class LandingpageConfigurationServiceBean implements
        LandingpageConfigurationService {

    private static final int MIN_RANGE = 0;
    private static final int MAX_RANGE = 20;

    @EJB
    MarketplaceSelector marketplaceSelector;

    @EJB
    LandingpageServiceLocal landingpageService;

    @Resource
    SessionContext sessionCtx;

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public List<POMarketplace> getMarketplaceSelections() {
        return marketplaceSelector.getMarketplaces();
    }

    @RolesAllowed("MARKETPLACE_OWNER")
    public List<POService> availableServices(String marketplaceId) {
        if (marketplaceId == null || marketplaceId.length() < 1) {
            return Collections.emptyList();
        }
        List<VOService> availableServices = null;
        try {
            availableServices = landingpageService
                    .availableServices(marketplaceId);
        } catch (SaaSApplicationException e) {
            availableServices = Collections.emptyList();
        }
        return assemblePOService(availableServices);
    }

    POService assemblePOService(VOService voService) {
        POService po = new POService(voService.getKey(), voService.getVersion());
        po.setPictureUrl("/image?type=SERVICE_IMAGE&amp;serviceKey="
                + voService.getKey());
        po.setProviderName(voService.getSellerName());
        po.setServiceName(voService.getNameToDisplay());
        if (voService.getStatus().equals(ServiceStatus.ACTIVE)) {
            po.setStatusSymbol("status_ACTIVE");
        } else {
            po.setStatusSymbol("status_NOT_ACTIVE");
        }
        return po;
    }

    List<POService> assemblePOService(List<VOService> voServices) {
        List<POService> pos = new ArrayList<POService>();
        for (VOService prod : voServices) {
            POService po = assemblePOService(prod);
            pos.add(po);
        }
        return pos;
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public Response savePublicLandingpageConfig(
            POPublicLandingpageConfig landingpageConfig)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, ConcurrentModificationException,
            OperationNotPermittedException, FillinOptionNotSupportedException {
        VOPublicLandingpage voLandingpage = buildVOLandingpage(landingpageConfig);
        Response r = new Response();
        try {
            landingpageService.savePublicLandingpageConfig(voLandingpage);
            POPublicLandingpageConfig po = loadLandingpageConfigInt(landingpageConfig
                    .getMarketplaceId());
            r.getResults().add(po);
            // get refreshed list of available services
            List<POService> availables = availableServices(landingpageConfig
                    .getMarketplaceId());
            r.getResults().add(availables);
        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (ValidationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (FillinOptionNotSupportedException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return r;
    }

    VOPublicLandingpage buildVOLandingpage(
            POPublicLandingpageConfig landingpageConfig) {
        VOPublicLandingpage vo = new VOPublicLandingpage();
        vo.setMarketplaceId(landingpageConfig.getMarketplaceId());
        vo.setKey(landingpageConfig.getKey());
        vo.setVersion(landingpageConfig.getVersion());
        vo.setFillinCriterion(landingpageConfig.getFillinCriterion());
        vo.setNumberServices(landingpageConfig.getNumberOfServicesOnLp());
        vo.setLandingpageServices(buildVOLandingpageServices(landingpageConfig.getFeaturedServices()));
        return vo;
    }

    List<VOLandingpageService> buildVOLandingpageServices(
            List<POService> featuredServices) {
        if (featuredServices == null || featuredServices.isEmpty()) {
            return Collections.emptyList();
        }
        List<VOLandingpageService> lps = new ArrayList<VOLandingpageService>();
        for (POService po : featuredServices) {
            VOLandingpageService vo = new VOLandingpageService();
            vo.setKey(po.getKey());
            vo.setVersion(po.getVersion());

            VOService voServiceWithKey = new VOService();
            voServiceWithKey.setKey(po.getKey());
            vo.setService(voServiceWithKey);
            lps.add(vo);
        }
        return lps;
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public Response resetLandingPage(String selectedMarketplace)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            OperationNotPermittedException {
        try {
            landingpageService.resetLandingpage(selectedMarketplace);
            POPublicLandingpageConfig po = loadLandingpageConfigInt(selectedMarketplace);
            List<POService> availables = availableServices(selectedMarketplace);
            return new Response(po, availables);
        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    private POPublicLandingpageConfig loadLandingpageConfigInt(
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException {
        VOPublicLandingpage landingpageConfig = null;
        if (marketplaceId != null) {
            landingpageConfig = landingpageService
                    .loadPublicLandingpageConfig(marketplaceId);
        }
        if (landingpageConfig == null) {
            landingpageConfig = createDefaultPublicLandingpage(marketplaceId);
        }
        POPublicLandingpageConfig po = assemblePOLandingpageConfig(landingpageConfig);
        return po;
    }

    /**
     * @param marketplaceId
     * @return
     */
    VOPublicLandingpage createDefaultPublicLandingpage(String marketplaceId) {
        VOPublicLandingpage landingpageConfig;
        landingpageConfig = new VOPublicLandingpage();
        landingpageConfig.setMarketplaceId(marketplaceId);
        landingpageConfig
                .setNumberServices(PublicLandingpage.DEFAULT_NUMBERSERVICES);
        landingpageConfig
                .setFillinCriterion(PublicLandingpage.DEFAULT_FILLINCRITERION);
        return landingpageConfig;
    }

    @Override
    public Response loadPublicLandingpageConfig(String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException {
        POPublicLandingpageConfig po;
        Response r = new Response();
        po = loadLandingpageConfigInt(marketplaceId);
        r.getResults().add(po);
        List<POService> availables = availableServices(marketplaceId);
        r.getResults().add(availables);
        List<FillinCriterion> fillins = getFillinOptions(marketplaceId);
        r.getResults().add(fillins);
        return r;
    }

    POPublicLandingpageConfig assemblePOLandingpageConfig(VOPublicLandingpage vo) {
        if (vo == null) {
            return null;
        }
        POPublicLandingpageConfig po = new POPublicLandingpageConfig();
        po.setKey(vo.getKey());
        po.setVersion(vo.getVersion());
        po.setMarketplaceId(vo.getMarketplaceId());
        if (vo.getFillinCriterion() == null) {
            po.setFillinCriterion(FillinCriterion.getDefault());
        } else {
            po.setFillinCriterion(vo.getFillinCriterion());
        }
        po.setNumberOfServicesOnLp(vo.getNumberServices());
        List<POService> featuredServices = assemblePOServicesFromLandingpage(vo
                .getLandingpageServices());
        po.setFeaturedServices(featuredServices);
        return po;
    }

    List<POService> assemblePOServicesFromLandingpage(
            List<VOLandingpageService> landingpageServices) {
        List<POService> pos = new ArrayList<POService>();
        for (VOLandingpageService prod : landingpageServices) {
            POService po = assemblePOService(prod.getService());
            pos.add(po);
        }
        return pos;
    }

    @Override
    public List<Integer> getNumOfServicesRange() {
        List<Integer> numOfServicesRange = new ArrayList<Integer>();
        for (int i = MIN_RANGE; i <= MAX_RANGE; i++) {
            numOfServicesRange.add(new Integer(i));
        }
        return numOfServicesRange;
    }

    @Override
    public List<FillinCriterion> getFillinOptions(String marketplaceId) {
        List<FillinCriterion> fillinOptions = new ArrayList<FillinCriterion>();
        fillinOptions.add(FillinCriterion.ACTIVATION_DESCENDING);
        fillinOptions.add(FillinCriterion.NAME_ASCENDING);
        fillinOptions.add(FillinCriterion.RATING_DESCENDING);
        fillinOptions.add(FillinCriterion.NO_FILLIN);
        return fillinOptions;
    }

    @Override
    public LandingpageType loadLandingpageType(String marketplaceId)
            throws ObjectNotFoundException {
        try {
            return LandingpageType.valueOf(landingpageService
                    .loadLandingpageType(marketplaceId).toString());
        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public Response saveEnterpriseLandingpageConfig(String marketplaceId)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ConcurrentModificationException, OperationNotPermittedException {
        Response r = new Response();
        try {
            landingpageService.saveEnterpriseLandingpageConfig(marketplaceId);
        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        return r;
    }

}
