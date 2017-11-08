/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-24                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.review;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.ProductReview;
import org.oscm.reviewservice.bean.ReviewServiceLocalBean;
import org.oscm.internal.assembler.POServiceReviewAssembler;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * Unit test for ReviewInternalServiceBean
 * 
 * @author Gao
 * 
 */
public class ReviewInternalServiceBeanTest {

    private ReviewInternalServiceBean internalServiceBean;
    private ReviewServiceLocalBean localBean;
    private ProductReview domainObject;
    private ProductReview result;
    private POServiceReview poReview;
    private ProductFeedback productFeedback;
    private Product product;
    private PlatformUser platformUser;

    @Before
    public void setup() throws Exception {
        poReview = new POServiceReview();
        initPOReview();
        domainObject = new ProductReview();
        result = new ProductReview();
        productFeedback = new ProductFeedback();
        product = new Product();
        productFeedback.setProduct(product);
        result.setProductFeedback(productFeedback);
        platformUser = new PlatformUser();
        result.setPlatformUser(platformUser);
        POServiceReviewAssembler.updateProductReview(result, poReview);
        internalServiceBean = new ReviewInternalServiceBean();
        localBean = mock(ReviewServiceLocalBean.class);
        doReturn(domainObject).when(localBean).createOrFindDomainObject(0l);
        internalServiceBean.reviewService = localBean;
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeReview_Null() throws Exception {
        // given
        // when
        internalServiceBean.writeReview(null);
    }

    @Test
    public void writeReview_OK() throws Exception {
        // given
        doReturn(result).when(localBean).writeReview(domainObject,
                Long.valueOf(poReview.getProductKey()));
        // when
        POServiceReview poResult = internalServiceBean.writeReview(poReview);
        // then
        verify(localBean, times(1)).writeReview(eq(domainObject),
                eq(Long.valueOf(poReview.getProductKey())));
        verifyPO(poResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteReview_Null() throws Exception {
        // given
        // when
        internalServiceBean.deleteReview(null);
    }

    @Test
    public void deleteReview_OK() throws Exception {
        // given
        // when
        internalServiceBean.deleteReview(poReview);
        // then
        verify(localBean, times(1)).deleteReview(
                eq(Long.valueOf(poReview.getKey())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteReviewByMarketplaceOwner_ReviewIsNull() throws Exception {
        // given
        // when
        internalServiceBean.deleteReviewByMarketplaceOwner(null, "Reason");
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteReviewByMarketplaceOwner_ReasonIsNull() throws Exception {
        // given
        // when
        internalServiceBean.deleteReviewByMarketplaceOwner(poReview, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteReviewByMarketplaceOwner_ReasonIsEmpty() throws Exception {
        // given
        // when
        internalServiceBean.deleteReviewByMarketplaceOwner(poReview, "");
    }

    @Test
    public void deleteReviewByMarketplaceOwner_OK() throws Exception {
        // given
        // when
        internalServiceBean.deleteReviewByMarketplaceOwner(poReview, "Reason");
        // then
        verify(localBean, times(1)).deleteReviewByMarketplaceOwner(
                eq(Long.valueOf(domainObject.getKey())), eq("Reason"));
    }

    /**
     * verify the result's fields is correct
     * 
     * @param target
     *            PO need to be verified
     */
    private void verifyPO(POServiceReview target) {
        assertEquals(poReview.getComment(), target.getComment());
        assertEquals(poReview.getKey(), target.getKey());
        assertEquals(poReview.getModificationDate(),
                target.getModificationDate());
        assertEquals(poReview.getProductKey(), target.getProductKey());
        assertEquals(poReview.getRating(), target.getRating());
        assertEquals(poReview.getTitle(), target.getTitle());
        assertEquals(poReview.getUserId(), target.getUserId());
        assertEquals(poReview.getUserName(), target.getUserName());
        assertEquals(poReview.getVersion(), target.getVersion());
    }

    /**
     * initialize POReview
     */
    private void initPOReview() {
        poReview.setTitle("title");
        poReview.setComment("comment");
        poReview.setRating(1);
    }
}
