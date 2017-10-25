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

import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.ProductReview;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOServiceReview;

/**
 * Assembler to convert the review value objects to the according domain object
 * and vice versa.
 * 
 * @author cheld
 * 
 */
public class ProductReviewAssembler extends BaseAssembler {

    /**
     * Constructs a new transfer object and copies all values from the given
     * domain object.
     * 
     * @param domainObject
     *            the domain object to copy values from
     * @return VOServiceReview
     */
    public static VOServiceReview toVOServiceReview(ProductReview domainObject) {
        VOServiceReview valueObject = new VOServiceReview();
        valueObject.setTitle(domainObject.getTitle());
        valueObject.setComment(domainObject.getComment());
        valueObject.setRating(domainObject.getRating());
        valueObject.setModificationDate(domainObject.getModificationDate());
        valueObject.setKey(domainObject.getKey());
        valueObject.setVersion(domainObject.getVersion());
        valueObject.setProductKey(domainObject.getProductFeedback()
                .getProduct().getKey());
        valueObject.setUserId(domainObject.getPlatformUser().getUserId());
        mapUserName(domainObject, valueObject);
        return valueObject;
    }

    static void mapUserName(ProductReview domainObject,
            VOServiceReview valueObject) {
        StringBuffer buff = new StringBuffer();
        PlatformUser user = domainObject.getPlatformUser();
        if (user.getFirstName() != null
                && user.getFirstName().trim().length() > 0) {
            buff.append(user.getFirstName().trim());
            valueObject.setFirstName(user.getFirstName().trim());
        }
        if (user.getLastName() != null
                && user.getLastName().trim().length() > 0) {
            if (buff.length() > 0) {
                buff.append(" ");
            }
            buff.append(user.getLastName().trim());
            valueObject.setLastName(user.getLastName().trim());
        }
        if (buff.length() > 0) {
            valueObject.setUserName(buff.toString());
        } else {
            valueObject.setUserName(null);
        }
    }

    /**
     * Update the given domain object with the given value object.
     * 
     * @param domainObjectToUpdate
     *            values to be copied to
     * @param valueObject
     *            values to be copied from
     * @throws ValidationException
     *             thrown if the values to not conform to constraints
     * @throws ConcurrentModificationException
     *             thorn if the object in the database is new than the transfer
     *             object
     */
    public static void updateProductReview(ProductReview domainObjectToUpdate,
            VOServiceReview valueObject) throws ValidationException,
            ConcurrentModificationException {

        // get values
        String title = trim(valueObject.getTitle());
        String comment = trim(valueObject.getComment());
        int rating = valueObject.getRating();

        // validate
        BLValidator.isName("title", title, true);
        BLValidator.isComment("comment", comment, true);
        BLValidator.isRating("rating", rating);

        // check for concurrent modification
        verifyVersionAndKey(domainObjectToUpdate, valueObject);

        // store in domain object
        updateValueObject(valueObject, domainObjectToUpdate);
        domainObjectToUpdate.setTitle(title);
        domainObjectToUpdate.setComment(comment);
        domainObjectToUpdate.setRating(rating);
    }

}
