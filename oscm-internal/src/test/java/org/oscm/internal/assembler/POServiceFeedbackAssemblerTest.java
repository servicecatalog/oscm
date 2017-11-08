/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-1-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.ProductReview;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.review.POServiceFeedback;
import org.oscm.internal.types.enumtypes.ServiceAccessType;

/**
 * Unit test for POServiceFeedbackAssembler
 * 
 * @author Gao
 * 
 */
public class POServiceFeedbackAssemblerTest {

    private Product product;
    private ProductFeedback feedback;
    private PlatformUser currentUser;

    @Before
    public void setup() {
        createLoggedInUser();
    }

    /**
     * domain object to presentation object
     */
    @Test
    public void toPOServiceFeedback() {
        // given
        createProduct();
        createReviews();
        feedback.setAverageRating(new BigDecimal("2.5"));
        // when
        POServiceFeedback presentationObject = POServiceFeedbackAssembler
                .toPOServiceFeedback(product, currentUser,false);
        // then
        assertEquals(product.getProductFeedback().getAverageRating(),
                presentationObject.getAverageRating());
        assertEquals(product.getProductFeedback().getProductReviews().size(),
                presentationObject.getReviews().size());
        assertEquals(product.getProductFeedback().getProduct().getKey(),
                presentationObject.getServiceKey());
    }

    /**
     * Test conversion to PO object if no reviews exist. (Average must be 0 -
     * not null)
     */
    @Test
    public void toPOServiceFeedback_noReviews() {
        // given
        createProduct();
        // when
        POServiceFeedback presentationObject = POServiceFeedbackAssembler
                .toPOServiceFeedback(product, currentUser,false);
        // then
        assertEquals(new BigDecimal(0), presentationObject.getAverageRating());
        assertEquals(0, presentationObject.getReviews().size());

        assertEquals(product.getKey(), presentationObject.getServiceKey());
    }

    /**
     * An empty feedback value object must be constructed if no feedback is
     * given.
     */
    @Test
    public void toPOServiceFeedback_noFeedback() {
        // given
        createProduct(false);
        // when
        POServiceFeedback presentationObject = POServiceFeedbackAssembler
                .toPOServiceFeedback(product, currentUser,false);
        // then
        assertEquals(new BigDecimal(0), presentationObject.getAverageRating());
        assertEquals(0, presentationObject.getReviews().size());
        assertEquals(product.getKey(), presentationObject.getServiceKey());
    }

    @Test
    public void toPOServiceFeedback_logedOutUser() {
        // given
        createProduct();
        // when
        POServiceFeedback presentationObject = POServiceFeedbackAssembler
                .toPOServiceFeedback(product, null,false);
        // then
        assertFalse(presentationObject.isAllowedToWriteReview());
    }

    private void createProduct() {
        createProduct(true);
    }

    private void createProduct(boolean withFeedBack) {
        // TechnicalProduct
        TechnicalProduct technicalProduct = new TechnicalProduct();
        technicalProduct.setAccessType(ServiceAccessType.LOGIN);

        // product
        product = new Product();
        product.setKey(123);
        product.setTechnicalProduct(technicalProduct);

        if (withFeedBack) {
            // feedback
            feedback = new ProductFeedback();
            product.setProductFeedback(feedback);
            feedback.setProduct(product);
        }

    }

    private void createReviews() {
        // review
        ProductReview review = new ProductReview();
        review.setProductFeedback(feedback);
        feedback.getProductReviews().add(review);

        // user
        PlatformUser user = new PlatformUser();
        review.setPlatformUser(user);
    }

    private void createLoggedInUser() {
        currentUser = new PlatformUser();
        Organization org = new Organization();
        currentUser.setOrganization(org);
    }
}
