/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 16.05.2011                                                      
 *                                                                              
 *  Completion Time: 16.05.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import java.math.BigDecimal;

import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.ProductReview;
import org.oscm.internal.vo.VOServiceFeedback;

/**
 * Assembler to convert the feedback value objects to the according domain
 * object and vice versa.
 * 
 * @author cheld
 * 
 */
public class ProductFeedbackAssembler {

    /**
     * Constructs a new transfer object and copies all values from the given
     * domain object.
     * 
     * @param domainObject
     *            the domain object to copy values from
     * @return VOServiceFeedback
     */
    public static VOServiceFeedback toVOServiceFeedback(Product product,
            PlatformUser currentUser) {
        Product template = product.getTemplateOrSelf();
        ProductFeedback domainObject = template.getProductFeedback();
        VOServiceFeedback valueObject = createEmptyValueObject();
        mapProductKey(product, valueObject);
        if (domainObject != null) {
            mapAverageRating(domainObject, valueObject);
            mapReviews(domainObject, valueObject);
        }
        mapAuthorities(template, valueObject, currentUser);
        return valueObject;
    }

    static void mapAuthorities(Product product, VOServiceFeedback valueObject,
            PlatformUser currentUser) {
        boolean userIsLoggedIn = currentUser != null;
        if (userIsLoggedIn) {
            boolean authorized = product.isAllowedToWriteReview(currentUser);
            valueObject.setAllowedToWriteReview(authorized);
        } else {
            valueObject.setAllowedToWriteReview(false);
        }
    }

    static VOServiceFeedback createEmptyValueObject() {
        VOServiceFeedback valueObject = new VOServiceFeedback();
        valueObject.setAverageRating(new BigDecimal(0));
        return valueObject;
    }

    static void mapAverageRating(ProductFeedback domainObject,
            VOServiceFeedback valueObject) {
        if (domainObject.getAverageRating() != null) {
            valueObject.setAverageRating(domainObject.getAverageRating());
        }
    }

    static void mapReviews(ProductFeedback domainObject,
            VOServiceFeedback valueObject) {
        for (ProductReview review : domainObject.getProductReviews()) {
            valueObject.getReviews().add(
                    ProductReviewAssembler.toVOServiceReview(review));
        }
    }

    static void mapProductKey(Product product, VOServiceFeedback valueObject) {
        valueObject.setServiceKey(product.getKey());
    }

}
