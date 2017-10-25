/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 22, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import javax.ejb.Remote;

import org.oscm.internal.cache.MarketplaceConfiguration;

/**
 * Interface for accessing the cache for marketplace configurations
 * 
 * @author miethaner
 */
@Remote
public interface MarketplaceCacheService {

    /**
     * Gets the corresponding marketplace configuration for the given
     * marketplace id
     * 
     * @param marketplaceId
     *            the marketplace id
     * @return the configuration
     */
    public MarketplaceConfiguration getConfiguration(String marketplaceId);

    /**
     * Loads the marketplace configuration for the given marketplace id into the
     * cache
     * 
     * @param marketplaceId
     *            the marketplace id
     * @return the configuration for the loaded marketplace
     */
    public MarketplaceConfiguration loadConfiguration(String marketplaceId);

    /**
     * Reset configuration with specified Marketplace ID.
     */
    public void resetConfiguration(String marketplaceId);
}
