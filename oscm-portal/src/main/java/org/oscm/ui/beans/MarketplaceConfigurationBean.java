/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 23, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.oscm.internal.cache.MarketplaceConfiguration;
import org.oscm.internal.intf.MarketplaceService;

/**
 * Delegation managed bean for marketplace configuration
 * 
 * @author miethaner
 */
@ApplicationScoped
@ManagedBean(name = "marketplaceConfigurationBean")
public class MarketplaceConfigurationBean extends BaseBean implements
        Serializable {

    private static final long serialVersionUID = 2745904981052531745L;

    @EJB
    private MarketplaceService marketplaceService;

    public MarketplaceConfiguration getCurrentConfiguration() {
        return marketplaceService.getCachedMarketplaceConfiguration(BaseBean
                .getMarketplaceIdStatic());
    }

}
