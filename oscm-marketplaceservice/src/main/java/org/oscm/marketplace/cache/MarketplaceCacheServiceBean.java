/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 22, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.cache;

import java.util.*;

import javax.ejb.*;

import org.oscm.internal.cache.MarketplaceConfiguration;
import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Singleton bean for caching marketplace configurations.
 * 
 * @author miethaner
 */
@Remote(MarketplaceCacheService.class)
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ)
public class MarketplaceCacheServiceBean implements MarketplaceCacheService {

    /** Caching marketplace configurations */
    private Map<String, MarketplaceConfiguration> configurationCache = new HashMap<>();

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(MarketplaceCacheServiceBean.class);

    @EJB
    private MarketplaceService mp;

    public Log4jLogger getLogger() {
        return logger;
    }

    public MarketplaceService getMarketplaceService() {
        return mp;
    }

    @Override
    public MarketplaceConfiguration getConfiguration(String marketplaceId) {
        MarketplaceConfiguration conf = configurationCache.get(marketplaceId);
        if (conf == null) {
            conf = loadConfiguration(marketplaceId);
        }
        return conf;
    }

    @Override
    @Lock(LockType.WRITE)
    public MarketplaceConfiguration loadConfiguration(String marketplaceId) {
        MarketplaceConfiguration conf = new MarketplaceConfiguration();
        VOMarketplace voMarketPlace;
        List<VOOrganization> allowedOrgs;
        try {
            voMarketPlace = getMarketplaceService().getMarketplaceById(
                    marketplaceId);
            allowedOrgs = getMarketplaceService()
                    .getAllOrganizationsWithAccessToMarketplace(marketplaceId);
        } catch (ObjectNotFoundException e) {
            getLogger().logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_MARKETPLACE_NOT_FOUND,
                    marketplaceId);
            return null;
        }

        Set<String> idSet = new TreeSet<>();
        for (VOOrganization org : allowedOrgs) {
            idSet.add(org.getOrganizationId());
        }

        // Copy related attributes from VOMarketplace to
        // MarketplaceConfiguration
        copyAttribute(voMarketPlace, conf);
        conf.setAllowedOrganizations(idSet);
        configurationCache.put(marketplaceId, conf);

        return conf;
    }

    /**
     * Convenience method for converting from VOMarketplace to
     * MarketplaceConfiguration
     */
    private void copyAttribute(VOMarketplace voMarketPlace,
            MarketplaceConfiguration conf) {
        conf.setReviewEnabled(voMarketPlace.isReviewEnabled());
        conf.setSocialBookmarkEnabled(voMarketPlace.isSocialBookmarkEnabled());
        conf.setTaggingEnabled(voMarketPlace.isTaggingEnabled());
        conf.setCategoriesEnabled(voMarketPlace.isCategoriesEnabled());
        conf.setRestricted(voMarketPlace.isRestricted());
        conf.setLandingPage(voMarketPlace.isHasPublicLandingPage());
        conf.setTenantId(voMarketPlace.getTenantId());
    }

    @Override
    @Lock(LockType.WRITE)
    public void resetConfiguration(String marketplaceId) {
        configurationCache.remove(marketplaceId);
    }

    @Schedule(minute = "*/10", hour = "*", persistent = false)
    @Lock(LockType.WRITE)
    public void scheduledReset() {
        configurationCache.clear();
    }

}
