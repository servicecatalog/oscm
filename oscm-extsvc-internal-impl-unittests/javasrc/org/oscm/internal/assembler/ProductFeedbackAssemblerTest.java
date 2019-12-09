/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld                                             
 *                                                                              
 *  Creation Date: 17.05.2011                                                      
 *                                                                              
 *  Completion Time: 17.05.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.ProductReview;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.assembler.ProductFeedbackAssembler;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.vo.VOServiceFeedback;
import org.oscm.internal.vo.VOServiceReview;

/**
 * Test classes for class <code>ProductFeedbackAssembler</code>.
 * 
 * @author cheld
 * 
 */
public class ProductFeedbackAssemblerTest {

    /**
     * domain object -> value object
     */
//    @Test
//    public void toVOServiceFeedback() {
//
//        // given a domain object
//        Product product = createDomainObject();
//        PlatformUser currentUser = createLoggedInUser();
//
//        // when converted to value object
//        VOServiceFeedback valueObject = ProductFeedbackAssembler
//                .toVOServiceFeedback(product, currentUser);
//
//        // then attribute and child reviews must be set
//        assertEquals(product.getProductFeedback().getAverageRating(),
//                valueObject.getAverageRating());
//        assertEquals(product.getProductFeedback().getProductReviews().size(),
//                valueObject.getReviews().size());
//        assertEquals(product.getProductFeedback().getProduct().getKey(),
//                valueObject.getServiceKey());
//    }
//
//    private Product createDomainObject() {
//
//        // TechnicalProduct
//        TechnicalProduct technicalProduct = new TechnicalProduct();
//        technicalProduct.setAccessType(ServiceAccessType.LOGIN);
//
//        // product
//        Product product = new Product();
//        product.setKey(123);
//        product.setTechnicalProduct(technicalProduct);
//
//        // feedback
//        ProductFeedback feedback = new ProductFeedback();
//        product.setProductFeedback(feedback);
//        feedback.setAverageRating(new BigDecimal("2.5"));
//        feedback.setProduct(product);
//
//        // review
//        ProductReview review = new ProductReview();
//        review.setProductFeedback(feedback);
//        feedback.getProductReviews().add(review);
//
//        // user
//        PlatformUser user = new PlatformUser();
//        review.setPlatformUser(user);
//
//        return product;
//    }
//
//    private PlatformUser createLoggedInUser() {
//        PlatformUser user = new PlatformUser();
//        Organization org = new Organization();
//        user.setOrganization(org);
//        return user;
//    }
//
//    /**
//     * Test conversion to VO object if no reviews exist. (Average must be 0 -
//     * not null)
//     */
//    @Test
//    public void toVOServiceFeedback_noReviews() {
//
//        // given a domain object
//        TechnicalProduct technicalProduct = new TechnicalProduct();
//        technicalProduct.setAccessType(ServiceAccessType.LOGIN);
//        Product product = new Product();
//        product.setKey(123);
//        product.setTechnicalProduct(technicalProduct);
//        ProductFeedback feedback = new ProductFeedback();
//        product.setProductFeedback(feedback);
//        feedback.setProduct(product);
//
//        // when converted to value object
//        VOServiceFeedback valueObject = ProductFeedbackAssembler
//                .toVOServiceFeedback(product, createLoggedInUser());
//
//        // then attribute and child reviews must be set to 0 (not null)
//        assertEquals(new BigDecimal(0), valueObject.getAverageRating());
//        assertEquals(0, valueObject.getReviews().size());
//
//        assertEquals(product.getKey(), valueObject.getServiceKey());
//    }
//
//    /**
//     * An empty feedback value object must be constructed if no feedback is
//     * given.
//     */
//    @Test
//    public void toVOServiceFeedback_noFeedback() {
//
//        // given no domain object
//        TechnicalProduct technicalProduct = new TechnicalProduct();
//        technicalProduct.setAccessType(ServiceAccessType.LOGIN);
//        Product product = new Product();
//        product.setKey(123);
//        product.setTechnicalProduct(technicalProduct);
//
//        // when converted to value object
//        VOServiceFeedback valueObject = ProductFeedbackAssembler
//                .toVOServiceFeedback(product, createLoggedInUser());
//
//        // then attribute and child reviews must be set to 0 (not null)
//        assertEquals(new BigDecimal(0), valueObject.getAverageRating());
//        assertEquals(0, valueObject.getReviews().size());
//        assertEquals(product.getKey(), valueObject.getServiceKey());
//    }
//
//    @Test
//    public void toVOServiceFeedback_logedOutUser() {
//
//        // given no domain object
//        Product product = new Product();
//
//        // when converted to value object
//        VOServiceFeedback valueObject = ProductFeedbackAssembler
//                .toVOServiceFeedback(product, null);
//
//        // then attribute and child reviews must be set to 0 (not null)
//        assertFalse(valueObject.isAllowedToWriteReview());
//    }
//
//    @Test
//    public void testReviewList() throws Exception {
//        // test obvious get/set for to ensure bean compliance
//        // though setter is never actually used
//        VOServiceFeedback vo = new VOServiceFeedback();
//        vo.setReviews(new ArrayList<VOServiceReview>());
//        assertNotNull(vo.getReviews());
//        assertTrue(vo.getReviews().isEmpty());
//    }
}
