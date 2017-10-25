/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-4                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import java.util.List;

import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Product;
import org.oscm.interceptor.ExceptionMapper;

/**
 * @author Mao
 * 
 */
@Interceptors({ ExceptionMapper.class })
public class MarketplaceDao {

    private DataService dataManager;

    public MarketplaceDao(DataService ds) {
        this.dataManager = ds;
    }

    public List<Marketplace> getMarketplaceByService(Product publishedService) {
        Query query = dataManager.createNamedQuery("Marketplace.findByService");
        query.setParameter("service", publishedService);
        List<Marketplace> mps = ParameterizedTypes.list(query.getResultList(),
                Marketplace.class);
        return mps;
    }

}
