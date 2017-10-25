/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.ProductReview;
import org.oscm.validator.BLValidator;
import org.oscm.internal.review.POServiceReview;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Assembler to convert the POServiceReview to the according domain object and
 * vice versa.
 * 
 * @author Gao
 * 
 */
public class POServiceReviewAssembler extends BasePOAssembler {

    /**
     * convert domain object to presentation object
     * 
     * @param domainObject
     * @return presentation object
     */
    public static POServiceReview toPOServiceReview(ProductReview domainObject) {
        POServiceReview presentationObject = new POServiceReview();
        presentationObject.setTitle(domainObject.getTitle());
        presentationObject.setComment(domainObject.getComment());
        presentationObject.setRating(domainObject.getRating());
        presentationObject.setModificationDate(domainObject
                .getModificationDate());
        presentationObject.setKey(domainObject.getKey());
        presentationObject.setVersion(domainObject.getVersion());
        presentationObject.setProductKey(domainObject.getProductFeedback()
                .getProduct().getKey());
        presentationObject
                .setUserId(domainObject.getPlatformUser().getUserId());
        mapUserName(domainObject, presentationObject);
        return presentationObject;
    }

    /**
     * update the value from PO to DO
     * 
     * @param domainObjectToUpdate
     * @param po
     * @throws ValidationException
     * @throws ConcurrentModificationException
     */
    public static void updateProductReview(ProductReview domainObjectToUpdate,
            POServiceReview po) throws ValidationException,
            ConcurrentModificationException {
        String title = trim(po.getTitle());
        String comment = trim(po.getComment());
        int rating = po.getRating();

        BLValidator.isName("title", title, true);
        BLValidator.isComment("comment", comment, true);
        BLValidator.isRating("rating", rating);

        verifyVersionAndKey(domainObjectToUpdate, po);

        updatePresentationObject(po, domainObjectToUpdate);
        domainObjectToUpdate.setTitle(title);
        domainObjectToUpdate.setComment(comment);
        domainObjectToUpdate.setRating(rating);
        domainObjectToUpdate.setModificationDate(po.getModificationDate());
    }

    private static void mapUserName(ProductReview domainObject,
            POServiceReview presentationObject) {
        StringBuffer buff = new StringBuffer();
        PlatformUser user = domainObject.getPlatformUser();
        if (user.getFirstName() != null
                && user.getFirstName().trim().length() > 0) {
            buff.append(user.getFirstName().trim());
            presentationObject.setFirstName(user.getFirstName().trim());
        }
        if (user.getLastName() != null
                && user.getLastName().trim().length() > 0) {
            if (buff.length() > 0) {
                buff.append(" ");
            }
            buff.append(user.getLastName().trim());
            presentationObject.setLastName(user.getLastName().trim());
        }
        if (buff.length() > 0) {
            presentationObject.setUserName(buff.toString());
        } else {
            presentationObject.setUserName(null);
        }
    }

}
