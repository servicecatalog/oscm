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
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.interceptor.ExceptionMapper;

/**
 * @author Mao
 * 
 */
@Interceptors({ ExceptionMapper.class })
public class ProductDao {

    public ProductDao(DataService ds) {
        this.dataManager = ds;
    }

    private DataService dataManager;

    public List<Product> getCopyForCustomer(Product productTemplate,
            Organization organization) {
        Query query = dataManager
                .createNamedQuery("Product.getCopyForCustomer");
        query.setParameter("template", productTemplate);
        query.setParameter("customer", organization);
        List<Product> resultList = ParameterizedTypes.list(
                query.getResultList(), Product.class);
        return resultList;
    }

    public List<Product> getProductForCustomerOnly(long supplierKey,
            Organization organization) {
        Query query = dataManager
                .createNamedQuery("Product.getForCustomerOnly");
        query.setParameter("vendorKey", Long.valueOf(supplierKey));
        query.setParameter("customer", organization);
        return ParameterizedTypes.list(query.getResultList(), Product.class);
    }

}
