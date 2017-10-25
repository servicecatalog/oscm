/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 25.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.local;

import java.util.List;

import org.oscm.domobjects.Product;

public class ProductSearchResult {

    private List<Product> services;
    private int resultSize;

    public List<Product> getServices() {
        return services;
    }

    public int getResultSize() {
        return resultSize;
    }

    public void setServices(List<Product> services) {
        this.services = services;
    }

    public void setResultSize(int resultSize) {
        this.resultSize = resultSize;
    }

}
