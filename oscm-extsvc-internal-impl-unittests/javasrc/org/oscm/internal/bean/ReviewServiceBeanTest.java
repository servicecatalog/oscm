/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-1-15                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.bean;

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
import org.oscm.internal.vo.VOServiceReview;

/**
 * Unit test for ReviewServiceBean
 * 
 * @author Gao
 * 
 */
public class ReviewServiceBeanTest {

    private ReviewServiceBean reviewServiceBean;
    private ReviewServiceLocalBean localBean;
    private ProductReview domainObject;
    private ProductReview result;
    private VOServiceReview voReview;
    private ProductFeedback productFeedback;
    private Product product;
    private PlatformUser platformUser;

    @Before
    public void setup() throws Exception {
        voReview = new VOServiceReview();
        initVOReview();
        domainObject = new ProductReview();
        result = new ProductReview();
        productFeedback = new ProductFeedback();
        product = new Product();
        productFeedback.setProduct(product);
        result.setProductFeedback(productFeedback);
        platformUser = new PlatformUser();
        result.setPlatformUser(platformUser);
        reviewServiceBean = new ReviewServiceBean();
        localBean = mock(ReviewServiceLocalBean.class);
        doReturn(domainObject).when(localBean).createOrFindDomainObject(0l);
        reviewServiceBean.localBean = localBean;
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeReview_Null() throws Exception {
        // given
        // when
        reviewServiceBean.writeReview(null);
    }

    @Test
    public void writeReview_OK() throws Exception {
        // given
        initResultDO();
        doReturn(result).when(localBean).writeReview(domainObject,
                Long.valueOf(voReview.getProductKey()));
        // when
        VOServiceReview voResult = reviewServiceBean.writeReview(voReview);
        // then
        verify(localBean, times(1)).writeReview(eq(domainObject),
                eq(Long.valueOf(voReview.getProductKey())));
        verifyVO(voResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteReview_Null() throws Exception {
        // given
        // when
        reviewServiceBean.deleteReview(null);
    }

    @Test
    public void deleteReview_OK() throws Exception {
        // given
        // when
        reviewServiceBean.deleteReview(voReview);
        // then
        verify(localBean, times(1)).deleteReview(
                eq(Long.valueOf(voReview.getKey())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteReviewByMarketplaceOwner_ReviewIsNull() throws Exception {
        // given
        // when
        reviewServiceBean.deleteReviewByMarketplaceOwner(null, "Reason");
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteReviewByMarketplaceOwner_ReasonIsNull() throws Exception {
        // given
        // when
        reviewServiceBean.deleteReviewByMarketplaceOwner(voReview, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteReviewByMarketplaceOwner_ReasonIsEmpty() throws Exception {
        // given
        // when
        reviewServiceBean.deleteReviewByMarketplaceOwner(voReview, "");
    }

    @Test
    public void deleteReviewByMarketplaceOwner_OK() throws Exception {
        // given
        // when
        reviewServiceBean.deleteReviewByMarketplaceOwner(voReview, "Reason");
        // then
        verify(localBean, times(1)).deleteReviewByMarketplaceOwner(
                eq(Long.valueOf(domainObject.getKey())), eq("Reason"));
    }

    /**
     * verify the result's fields is correct
     * 
     * @param target
     *            VO need to be verified
     */
    private void verifyVO(VOServiceReview target) {
        assertEquals(voReview.getComment(), target.getComment());
        assertEquals(voReview.getKey(), target.getKey());
        assertEquals(voReview.getModificationDate(),
                target.getModificationDate());
        assertEquals(voReview.getProductKey(), target.getProductKey());
        assertEquals(voReview.getRating(), target.getRating());
        assertEquals(voReview.getTitle(), target.getTitle());
        assertEquals(voReview.getUserId(), target.getUserId());
        assertEquals(voReview.getUserName(), target.getUserName());
        assertEquals(voReview.getVersion(), target.getVersion());
    }

    private void initVOReview() {
        voReview.setTitle("title");
        voReview.setComment("comment");
        voReview.setRating(1);
    }

    private void initResultDO() {
        result.setTitle(voReview.getTitle());
        result.setComment(voReview.getComment());
        result.setRating(voReview.getRating());
    }
}
