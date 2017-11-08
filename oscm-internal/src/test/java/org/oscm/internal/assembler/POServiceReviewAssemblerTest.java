/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-25                                                      
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
import org.oscm.internal.review.POServiceReview;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Unit test of POServiceReviewAssembler
 * 
 * @author Gao
 * 
 */
public class POServiceReviewAssemblerTest {

    @Test
    public void toPOServiceReview() {
        // given
        ProductReview domainObject = createDomainObject();
        // when
        POServiceReview valueObject = POServiceReviewAssembler
                .toPOServiceReview(domainObject);
        // then
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

    @Test
    public void updateProductReview() throws Exception {
        // given
        POServiceReview presentationObject = createPresentationObject();
        ProductReview domainObject = new ProductReview();

        // when
        POServiceReviewAssembler.updateProductReview(domainObject,
                presentationObject);

        // then
        assertEquals(presentationObject.getTitle(), domainObject.getTitle());
        assertEquals(presentationObject.getComment(), domainObject.getComment());
        assertEquals(presentationObject.getRating(), domainObject.getRating());
        assertEquals(presentationObject.getModificationDate(),
                domainObject.getModificationDate());
        assertEquals(presentationObject.getKey(), domainObject.getKey());
    }

    @Test
    public void updateProductReview_trim() throws Exception {
        // given
        POServiceReview presentationObject = createPresentationObject();
        presentationObject.setTitle("  title with spaces   ");
        presentationObject.setComment("  comment with spaces   ");
        ProductReview domainObject = new ProductReview();

        // when
        POServiceReviewAssembler.updateProductReview(domainObject,
                presentationObject);

        // then
        assertEquals("title with spaces", domainObject.getTitle());
        assertEquals("comment with spaces", domainObject.getComment());
    }

    @Test
    public void updateProductReview_maxLenghtComments() throws Exception {
        // given
        POServiceReview presentationObject = createPresentationObject();
        presentationObject.setComment(longText(ADMValidator.LENGTH_COMMENT));
        ProductReview domainObject = new ProductReview();

        // when
        POServiceReviewAssembler.updateProductReview(domainObject,
                presentationObject);

        // then
        assertEquals(ADMValidator.LENGTH_COMMENT, domainObject.getComment()
                .length());
    }

    @Test(expected = ValidationException.class)
    public void updateProductReview_commentTooLong() throws Exception {
        // given
        POServiceReview presentationObject = createPresentationObject();
        presentationObject
                .setComment(longText(ADMValidator.LENGTH_COMMENT + 1));
        ProductReview domainObject = new ProductReview();

        // when
        POServiceReviewAssembler.updateProductReview(domainObject,
                presentationObject);
    }

    @Test(expected = ValidationException.class)
    public void updateProductReview_noComment() throws Exception {
        // given
        POServiceReview presentationObject = createPresentationObject();
        presentationObject.setComment("");
        ProductReview domainObject = new ProductReview();

        // when
        POServiceReviewAssembler.updateProductReview(domainObject,
                presentationObject);
    }

    @Test(expected = ValidationException.class)
    public void updateProductReview_noRating() throws Exception {
        // given
        POServiceReview presentationObject = createPresentationObject();
        presentationObject.setRating(0);
        ProductReview domainObject = new ProductReview();

        // when
        POServiceReviewAssembler.updateProductReview(domainObject,
                presentationObject);
    }

    @Test(expected = ValidationException.class)
    public void updateProductReview_wrongRating() throws Exception {
        // given
        POServiceReview presentationObject = createPresentationObject();
        presentationObject.setRating(6);
        ProductReview domainObject = new ProductReview();

        // when
        POServiceReviewAssembler.updateProductReview(domainObject,
                presentationObject);
    }

    @Test
    public void updateProductReview_titleMaxLength() throws Exception {
        // given
        POServiceReview presentationObject = createPresentationObject();
        presentationObject.setTitle(longText(ADMValidator.LENGTH_NAME));
        ProductReview domainObject = new ProductReview();

        // when
        POServiceReviewAssembler.updateProductReview(domainObject,
                presentationObject);

        // then
        assertEquals(ADMValidator.LENGTH_NAME, domainObject.getTitle().length());
    }

    @Test(expected = ValidationException.class)
    public void updateProductReview_titleTooLong() throws Exception {
        // given
        POServiceReview presentationObject = createPresentationObject();
        presentationObject.setTitle(longText(129));
        ProductReview domainObject = new ProductReview();

        // when
        POServiceReviewAssembler.updateProductReview(domainObject,
                presentationObject);
    }

    @Test(expected = ValidationException.class)
    public void toServiceReview_noTitle() throws Exception {
        // given
        POServiceReview presentationObject = createPresentationObject();
        presentationObject.setTitle("");
        ProductReview domainObject = new ProductReview();

        // when
        POServiceReviewAssembler.updateProductReview(domainObject,
                presentationObject);
    }

    private ProductReview createDomainObject() {
        Product product = new Product();
        product.setKey(123);

        ProductFeedback feedback = new ProductFeedback();
        feedback.setProduct(product);

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

    private POServiceReview createPresentationObject() {
        POServiceReview presentationObject = new POServiceReview();
        presentationObject.setTitle("myTitle");
        presentationObject.setComment("myComment");
        presentationObject.setRating(4);
        presentationObject.setModificationDate(10);
        presentationObject.setKey(0);
        return presentationObject;
    }

    private String longText(int length) {
        char text[] = new char[length];
        Arrays.fill(text, 'a');
        return new String(text);
    }
}
