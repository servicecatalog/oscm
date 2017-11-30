/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-1-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.manageReview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.cache.MarketplaceConfiguration;
import org.oscm.internal.review.POServiceFeedback;
import org.oscm.internal.review.POServiceReview;
import org.oscm.internal.review.ReviewInternalService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOService;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.mp.serviceDetails.ServiceDetailsModel;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.User;

/**
 * Unit test for ManageReviewCtrl
 * 
 * @author Gao
 * 
 */
public class ManageReviewCtrlTest {
    private ManageReviewCtrl manageReviewCtrl;
    private ManageReviewModel manageReviewModel;
    private ServiceDetailsModel serviceDetailsModel;
    private MarketplaceConfiguration currentMarketplaceConfig;
    private User user;
    private POServiceReview serviceReview;
    private Service service;
    private POServiceFeedback feedback;
    private ReviewInternalService reviewService;

    @Before
    public void before() {
        manageReviewCtrl = createReviewBean();
        serviceDetailsModel = new ServiceDetailsModel();
        manageReviewCtrl.setServiceDetailsModel(serviceDetailsModel);
        manageReviewModel = new ManageReviewModel();
        manageReviewCtrl.setManageReviewModel(manageReviewModel);
        manageReviewCtrl.ui = mock(UiDelegate.class);
        SessionBean sb = mock(SessionBean.class);
        when(manageReviewCtrl.ui.findSessionBean()).thenReturn(sb);

        currentMarketplaceConfig = new MarketplaceConfiguration();
        doReturn(currentMarketplaceConfig).when(manageReviewCtrl).getConfig();
        user = mock(User.class);
        serviceReview = new POServiceReview();
        serviceReview.setKey(1l);
        VOService voService = new VOService();
        voService.setKey(1l);
        service = new Service(voService);
        feedback = new POServiceFeedback();
        reviewService = mock(ReviewInternalService.class);
    }

    @Test
    public void getServiceReview_SelectedServiceIsNull() {
        // given
        manageReviewModel.setServiceReview(null);
        serviceDetailsModel.setSelectedService(null);
        // when
        POServiceReview result = manageReviewCtrl.getServiceReview();
        // then
        assertNull(result);
    }

    @Test
    public void getServiceReview_InitNewReview() {
        // given
        manageReviewModel.setServiceReview(null);
        serviceDetailsModel.setSelectedService(service);
        serviceDetailsModel.setSelectedServiceFeedback(feedback);
        // when
        POServiceReview result = manageReviewCtrl.getServiceReview();
        // then
        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUserId());
    }

    @Test
    public void getReviewKeyForDeletion_SelectedReviewIsNull() {
        // given
        manageReviewModel.setServiceReview(null);
        // when
        long result = manageReviewCtrl.getReviewKeyForDeletion();
        // then
        assertEquals(0, result);
    }

    @Test
    public void getReviewKeyForDeletion_SelectedReviewNotNull() {
        // given
        manageReviewModel.setServiceReview(serviceReview);
        // when
        long result = manageReviewCtrl.getReviewKeyForDeletion();
        // then
        assertEquals(1, result);
    }

    @Test
    public void setReviewKeyForDeletion_NotExsitsKey() {
        // given
        long key = 9l;
        serviceDetailsModel.setSelectedService(service);
        List<POServiceReview> ratings = Arrays.asList(serviceReview);
        feedback.setReviews(ratings);
        serviceDetailsModel.setSelectedServiceFeedback(feedback);
        // when
        manageReviewCtrl.setReviewKeyForDeletion(key);
        // then
        assertNull(manageReviewModel.getServiceReview());
    }

    @Test
    public void setReviewKeyForDeletion_ExsitsKey() {
        // given
        long key = 9l;
        serviceDetailsModel.setSelectedService(service);
        serviceReview.setKey(key);
        List<POServiceReview> ratings = Arrays.asList(serviceReview);
        feedback.setReviews(ratings);
        serviceDetailsModel.setSelectedServiceFeedback(feedback);
        // when
        manageReviewCtrl.setReviewKeyForDeletion(key);
        // then
        assertNotNull(manageReviewModel.getServiceReview());
        assertEquals(key, manageReviewModel.getServiceReview().getKey());
    }

    @Test
    public void cancelReview() {
        // given
        // when
        String result = manageReviewCtrl.cancelReview();
        // then
        assertNull(manageReviewModel.getServiceReview());
        assertEquals(BaseBean.OUTCOME_MARKETPLACE_REDIRECT, result);
    }

    @Test
    public void removeReview() throws Exception {
        // given
        manageReviewModel.setServiceReview(serviceReview);
        // when
        String result = manageReviewCtrl.removeReview();
        // then
        verify(reviewService, times(1)).deleteReview(eq(serviceReview));
        assertEquals(BaseBean.OUTCOME_MARKETPLACE_REDIRECT, result);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void removeReview_OperationNotPermittedException() throws Exception {
        // given
        manageReviewModel.setServiceReview(serviceReview);
        doThrow(new OperationNotPermittedException()).when(reviewService)
                .deleteReview(eq(serviceReview));
        // when
        try {
            manageReviewCtrl.removeReview();
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void removeReview_ObjectNotFoundException() throws Exception {
        // given
        manageReviewModel.setServiceReview(serviceReview);
        doThrow(new ObjectNotFoundException()).when(reviewService)
                .deleteReview(eq(serviceReview));
        // when
        try {
            manageReviewCtrl.removeReview();
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void getLeftCharacters() {
        // given
        String comment = "comment";
        serviceReview.setComment(comment);
        manageReviewModel.setServiceReview(serviceReview);
        // when
        String result = manageReviewCtrl.getLeftCharacters();
        // then
        assertEquals(String.valueOf((2000 - comment.length())), result);
    }

    @Test
    public void getTitleLength() {
        // given
        // when
        String result = manageReviewCtrl.getTitleLength();
        // then
        assertEquals("100", result);
    }

    @Test
    public void getIsNewReview_TRUE() {
        // given
        serviceReview.setKey(0);
        manageReviewModel.setServiceReview(serviceReview);
        // when
        boolean result = manageReviewCtrl.getIsNewReview();
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void getIsNewReview_FALSE() {
        // given
        serviceReview.setKey(1);
        manageReviewModel.setServiceReview(serviceReview);
        // when
        boolean result = manageReviewCtrl.getIsNewReview();
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void getCommentLength() {
        // given
        // when
        String result = manageReviewCtrl.getCommentLength();
        // then
        assertEquals("2000", result);
    }

    @Test
    public void getDeletionReason() {
        // given
        manageReviewCtrl.setDeletionReason("Reason");
        // when
        String result = manageReviewCtrl.getDeletionReason();
        // then
        assertEquals("Reason", result);
    }

    @Test
    public void removeReviewByMarketplaceAdmin() throws Exception {
        // given
        manageReviewModel.setServiceReview(serviceReview);
        manageReviewCtrl.setDeletionReason("Reason");
        // when
        String result = manageReviewCtrl.removeReviewByMarketplaceAdmin();
        // then
        verify(reviewService, times(1)).deleteReviewByMarketplaceOwner(
                eq(serviceReview), eq("Reason"));
        assertEquals(BaseBean.OUTCOME_MARKETPLACE_REDIRECT, result);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void removeReviewByMarketplaceAdmin_OperationNotPermittedException()
            throws Exception {
        // given
        manageReviewModel.setServiceReview(serviceReview);
        manageReviewCtrl.setDeletionReason("Reason");
        doThrow(new OperationNotPermittedException())
                .when(reviewService)
                .deleteReviewByMarketplaceOwner(eq(serviceReview), eq("Reason"));
        // when
        try {
            manageReviewCtrl.removeReviewByMarketplaceAdmin();
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void removeReviewByMarketplaceAdmin_ObjectNotFoundException()
            throws Exception {
        // given
        manageReviewModel.setServiceReview(serviceReview);
        manageReviewCtrl.setDeletionReason("Reason");
        doThrow(new ObjectNotFoundException())
                .when(reviewService)
                .deleteReviewByMarketplaceOwner(eq(serviceReview), eq("Reason"));
        // when
        try {
            manageReviewCtrl.removeReviewByMarketplaceAdmin();
            // then
            fail();
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void publishReview_SettingDisabledConcurrently() throws Exception {
        currentMarketplaceConfig.setReviewEnabled(true);
        String outcome = manageReviewCtrl.publishReview();
        assertEquals(BaseBean.OUTCOME_MARKETPLACE_REDIRECT, outcome);

        currentMarketplaceConfig.setReviewEnabled(false);
        outcome = manageReviewCtrl.publishReview();
        assertEquals(BaseBean.OUTCOME_REVIEW_ENABLEMENT_CHANGED, outcome);
    }

    @SuppressWarnings("serial")
    private ManageReviewCtrl createReviewBean() {
        final HttpServletRequest httpServletReqMock = mock(HttpServletRequest.class);
        HttpSession sessionMock = mock(HttpSession.class);
        when(httpServletReqMock.getServletPath()).thenReturn(
                "http://test.com/servlet");
        when(httpServletReqMock.getSession()).thenReturn(sessionMock);

        manageReviewCtrl = spy(new ManageReviewCtrl() {
            @Override
            public HttpServletRequest getRequest() {
                return httpServletReqMock;
            }

            @Override
            public ReviewInternalService getReviewService() {
                return reviewService;
            }

            @Override
            public User getUserFromSession() {
                return user;
            }
        });
        return manageReviewCtrl;
    }

}
