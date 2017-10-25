/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-4-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reviewservice.dao;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.ProductReview;

/**
 * @author Mao
 * 
 */
@Stateless
@LocalBean
public class ProductReviewDao {

    @EJB(beanInterface = DataService.class)
    DataService dm;

    public List<ProductReview> getProductReviewsForUser(PlatformUser user) {
        Query query = dm.createNamedQuery("ProductReview.findByUser");
        query.setParameter("platformUser", user);
        List<ProductReview> reviews = ParameterizedTypes.list(
                query.getResultList(), ProductReview.class);
        return reviews;
    }
}
