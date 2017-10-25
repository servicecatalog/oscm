/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 18, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dao;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.OrganizationReferenceType;

/**
 * @author zhaoh
 * 
 */
@Stateless
@LocalBean
public class TechnicalProductDao {

    @EJB(beanInterface = DataService.class)
    private DataService dm;

    private static final String QUERY_TECHNICAL_PRODUCT = "SELECT tp FROM TechnicalProduct tp WHERE EXISTS (SELECT mp FROM MarketingPermission mp, OrganizationReference orgRef WHERE mp.technicalProduct = tp AND orgRef = mp.organizationReference AND orgRef.dataContainer.referenceType = :refType AND orgRef.target = :supplier)";

    public TechnicalProductDao() {
    }

    public List<TechnicalProduct> retrieveTechnicalProduct(Organization supplier) {
        Query query = dm.createQuery(QUERY_TECHNICAL_PRODUCT);
        query.setParameter("refType",
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
        query.setParameter("supplier", supplier);
        List<TechnicalProduct> result = ParameterizedTypes.list(
                query.getResultList(), TechnicalProduct.class);

        return result;
    }
}
