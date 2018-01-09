/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld                                                      
 *                                                                              
 *  Creation Date: 16.05.2011                                                      
 *                                                                              
 *  Completion Time: 16.05.2011                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.ProductReview;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.assembler.ProductReviewAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOServiceReview;

/**
 * Test classes for class <code>ProductReviewAssembler</code>.
 * 
 * @author cheld
 * 
 */
public class ProductReviewAssemblerTest {

    /**
     * domain object -> value object
     */
    @Test
    public void toVOServiceReview() {

        // given a domain object
        ProductReview domainObject = createDomainObject();

        // when converting to VO object
        VOServiceReview valueObject = ProductReviewAssembler
                .toVOServiceReview(domainObject);

        // then all attributes must be set
        assertEquals(domainObject.getTitle(), valueObject.getTitle());
        assertEquals(domainObject.getComment(), valueObject.getComment());
        assertEquals(domainObject.getRating(), valueObject.getRating());
        assertEquals(domainObject.getModificationDate(),
                valueObject.getModificationDate());
        assertEquals(domainObject.getKey(), valueObject.getKey());
        assertEquals(domainObject.getProductFeedback().getProduct().getKey(),
                valueObject.getProductKey());
        assertEquals(domainObject.getPlatformUser().getUserId(),
                valueObject.getUserId());
        assertTrue(valueObject.getUserName().startsWith(
                domainObject.getPlatformUser().getFirstName()));
        assertTrue(valueObject.getUserName().endsWith(
                domainObject.getPlatformUser().getLastName()));
        assertEquals(valueObject.getFirstName(), domainObject.getPlatformUser()
                .getFirstName());
        assertEquals(valueObject.getLastName(), domainObject.getPlatformUser()
                .getLastName());
    }

    ProductReview createDomainObject() {

        // product
        Product product = new Product();
        product.setKey(123);

        // feedback
        ProductFeedback feedback = new ProductFeedback();
        feedback.setProduct(product);

        // review
        ProductReview review = new ProductReview();
        review.setTitle("myTitle");
        review.setComment("myComment");
        review.setRating(4);
        review.setModificationDate(10);
        review.setKey(2);
        review.setProductFeedback(feedback);
        PlatformUser user = new PlatformUser();
        user.setUserId("fred");
        user.setFirstName("Fred");
        user.setLastName("Durst");
        review.setPlatformUser(user);

        return review;
    }

    /**
     * value object -> domain object
     * 
     * @throws Exception
     */
    @Test
    public void toServiceReview() throws Exception {

        // given a value object
        VOServiceReview valueObject = createValueObject();

        // when converting to domain object
        ProductReview domainObject = new ProductReview();
        ProductReviewAssembler.updateProductReview(domainObject, valueObject);

        // then all attributes must be set
        assertEquals(valueObject.getTitle(), domainObject.getTitle());
        assertEquals(valueObject.getComment(), domainObject.getComment());
        assertEquals(valueObject.getRating(), domainObject.getRating());
        assertEquals(0, domainObject.getModificationDate());
        assertEquals(valueObject.getKey(), domainObject.getKey());
    }

    VOServiceReview createValueObject() {
        VOServiceReview valueObject = new VOServiceReview();
        valueObject.setTitle("myTitle");
        valueObject.setComment("myComment");
        valueObject.setRating(4);
        valueObject.setModificationDate(10);
        valueObject.setKey(0);
        return valueObject;
    }

    @Test
    public void updateProductReview() throws Exception {

        // given a domain object and a modified value object
        ProductReview domainObject = createDomainObject();
        VOServiceReview updatedValueObject = createValueObjectForUpdate();

        // when updating domain object
        ProductReviewAssembler.updateProductReview(domainObject,
                updatedValueObject);

        // then all attributes in the domain object must be updated
        assertEquals(updatedValueObject.getTitle(), domainObject.getTitle());
        assertEquals(updatedValueObject.getComment(), domainObject.getComment());
        assertEquals(updatedValueObject.getRating(), domainObject.getRating());
    }

    VOServiceReview createValueObjectForUpdate() {
        VOServiceReview valueObject = new VOServiceReview();
        valueObject.setTitle("myTitle2");
        valueObject.setComment("myComment2");
        valueObject.setRating(5);
        valueObject.setModificationDate(15);
        valueObject.setKey(2);
        return valueObject;
    }

    /**
     * Modification date is read only. Ignore updates.
     * 
     * @throws Exception
     */
    @Test
    public void updateProductReview_modificationDate() throws Exception {

        // given a domain object. modify the last modification date in the value
        // object
        ProductReview domainObject = createDomainObject();
        domainObject.setModificationDate(10);
        VOServiceReview updatedValueObject = ProductReviewAssembler
                .toVOServiceReview(domainObject);
        updatedValueObject.setModificationDate(25);

        // when updating domain object
        ProductReviewAssembler.updateProductReview(domainObject,
                updatedValueObject);

        // then the new value must be ignored and the old value must be stored
        // in the domain object
        assertEquals(10, domainObject.getModificationDate());
    }

    /**
     * Spaces must be trimmed
     */
    @Test
    public void toServiceReview_trim() throws Exception {

        // given a values with tailing spaces
        VOServiceReview valueObject = createValueObject();
        valueObject.setTitle("  title with spaces   ");
        valueObject.setComment("  comment with spaces   ");

        // when converting to domain object
        ProductReview domainObject = new ProductReview();
        ProductReviewAssembler.updateProductReview(domainObject, valueObject);

        // then title and comment must be trimmed
        assertEquals("title with spaces", domainObject.getTitle());
        assertEquals("comment with spaces", domainObject.getComment());
    }

    /**
     * comment is limited to 2000 chars
     */
    @Test
    public void toServiceReview_maxLenghtComments() throws Exception {

        // given a value object with long comment
        VOServiceReview valueObject = createValueObject();
        valueObject.setComment(longText(ADMValidator.LENGTH_COMMENT));

        // when converting
        ProductReview domainObject = new ProductReview();
        ProductReviewAssembler.updateProductReview(domainObject, valueObject);

        // then value is stored
        assertEquals(ADMValidator.LENGTH_COMMENT, domainObject.getComment()
                .length());
    }

    /**
     * comment is limited to 2000 chars
     */
    @Test(expected = ValidationException.class)
    public void toServiceReview_commentTooLong() throws Exception {

        // given a value object with TOO long comment
        VOServiceReview valueObject = createValueObject();
        valueObject.setComment(longText(ADMValidator.LENGTH_COMMENT + 1));

        // when converting an exception is thrown
        ProductReviewAssembler.updateProductReview(new ProductReview(),
                valueObject);
    }

    private String longText(int length) {
        char text[] = new char[length];
        Arrays.fill(text, 'a');
        return new String(text);
    }

    /**
     * Comment is mandatory
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void toServiceReview_noComment() throws Exception {

        // given a value object with no comment
        VOServiceReview valueObject = createValueObject();
        valueObject.setComment("");

        // when converting an exception is thrown
        ProductReviewAssembler.updateProductReview(new ProductReview(),
                valueObject);
    }

    /**
     * Rating must be between 1 and 5
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void toServiceReview_noRating() throws Exception {

        // given a value object with no rating
        VOServiceReview valueObject = createValueObject();
        valueObject.setRating(0);

        // when converting an exception is thrown
        ProductReviewAssembler.updateProductReview(new ProductReview(),
                valueObject);
    }

    /**
     * Rating must be between 1 and 5
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void toServiceReview_wrongRating() throws Exception {

        // given a value object with illegal rating
        VOServiceReview valueObject = createValueObject();
        valueObject.setRating(6);

        // when converting an exception is thrown
        ProductReviewAssembler.updateProductReview(new ProductReview(),
                valueObject);
    }

    /**
     * title is limited to 100 chars
     */
    @Test
    public void toServiceReview_titleMaxLength() throws Exception {

        // given a value object with long title
        VOServiceReview valueObject = createValueObject();
        valueObject.setTitle(longText(ADMValidator.LENGTH_NAME));

        // when converting
        ProductReview domainObject = new ProductReview();
        ProductReviewAssembler.updateProductReview(domainObject, valueObject);

        // then value is stored
        assertEquals(ADMValidator.LENGTH_NAME, domainObject.getTitle().length());
    }

    /**
     * title is limited to 100 chars
     */
    @Test(expected = ValidationException.class)
    public void toServiceReview_titleTooLong() throws Exception {

        // given a value object with TOO long title
        VOServiceReview valueObject = createValueObject();
        valueObject.setTitle(longText(129));

        // when converting an exception is thrown
        ProductReviewAssembler.updateProductReview(new ProductReview(),
                valueObject);
    }

    /**
     * Title is mandatory
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void toServiceReview_noTitle() throws Exception {

        // given a value object with no title
        VOServiceReview valueObject = createValueObject();
        valueObject.setTitle("");

        // when converting an exception is thrown
        ProductReviewAssembler.updateProductReview(new ProductReview(),
                valueObject);
    }

    /**
     * Concurrent modification
     * 
     * @throws Exception
     */
    @Test(expected = ConcurrentModificationException.class)
    public void updateProductReview_concurrentModification() throws Exception {

        // given a domain object and a value object with different versions
        ProductReview domainObject = createDomainObject();
        VOServiceReview valueObject = createValueObject();
        valueObject.setVersion(-1);

        // when updating domain object the version of the domain object must be
        // smaller than value object
        ProductReviewAssembler.updateProductReview(domainObject, valueObject);

    }
}
