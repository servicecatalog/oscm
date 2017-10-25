/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-1-3                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import java.math.BigDecimal;

import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.ProductReview;
import org.oscm.internal.review.POServiceFeedback;

/**
 * Assembler to convert the POServiceFeedback to the according domain object and
 * vice versa.
 * 
 * @author Gao
 * 
 */
public class POServiceFeedbackAssembler extends BasePOAssembler {

    /**
     * Constructs a new transfer object and copies all values from the given
     * domain object.
     * 
     * @param domainObject
     *            the domain object to copy values from
     * @return POServiceFeedback
     */
    public static POServiceFeedback toPOServiceFeedback(Product product,
            PlatformUser currentUser, boolean isUsageSubscriptionExist) {
        Product template = product.getTemplateOrSelf();
        ProductFeedback domainObject = template.getProductFeedback();
        POServiceFeedback presentationObject = createEmptyPresentationObject();
        mapProductKey(product, presentationObject);
        if (domainObject != null) {
            mapAverageRating(domainObject, presentationObject);
            mapReviews(domainObject, presentationObject);
        }
        mapAuthorities(template, presentationObject, currentUser,
                isUsageSubscriptionExist);
        return presentationObject;
    }

    private static void mapAuthorities(Product product,
            POServiceFeedback presentationObject, PlatformUser currentUser,
            boolean isUsageSubscriptionExist) {
        if (currentUser != null) {
            if (isUsageSubscriptionExist || product.isExtenalService()) {
                presentationObject.setAllowedToWriteReview(true);
            } else {
                boolean authorized = product
                        .isAllowedToUpdateOwnReview(currentUser);
                presentationObject.setAllowedToWriteReview(authorized);
            }
        } else {
            presentationObject.setAllowedToWriteReview(false);
        }
    }

    private static POServiceFeedback createEmptyPresentationObject() {
        POServiceFeedback presentationObject = new POServiceFeedback();
        presentationObject.setAverageRating(new BigDecimal(0));
        return presentationObject;
    }

    private static void mapAverageRating(ProductFeedback domainObject,
            POServiceFeedback presentationObject) {
        if (domainObject.getAverageRating() != null) {
            presentationObject
                    .setAverageRating(domainObject.getAverageRating());
        }
    }

    private static void mapReviews(ProductFeedback domainObject,
            POServiceFeedback presentationObject) {
        for (ProductReview review : domainObject.getProductReviews()) {
            presentationObject.getReviews().add(
                    POServiceReviewAssembler.toPOServiceReview(review));
        }
    }

    private static void mapProductKey(Product product,
            POServiceFeedback presentationObject) {
        presentationObject.setServiceKey(product.getKey());
    }

}
