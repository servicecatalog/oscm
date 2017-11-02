/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-1-2                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.manageReview;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;

import org.oscm.internal.cache.MarketplaceConfiguration;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.review.POServiceReview;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.dialog.mp.serviceDetails.ServiceDetailsModel;
import org.oscm.ui.model.Service;
import org.oscm.validator.ADMValidator;

/**
 * Controller for manage service's review
 * 
 * @author Gao
 * 
 */
@ManagedBean
@ViewScoped
public class ManageReviewCtrl extends BaseBean implements Serializable {

    private static final long serialVersionUID = -1529604134082868160L;

    @ManagedProperty(value = "#{manageReviewModel}")
    private ManageReviewModel manageReviewModel;

    @ManagedProperty(value = "#{serviceDetailsModel}")
    private ServiceDetailsModel serviceDetailsModel;

    @EJB
    private MarketplaceService marketplaceService;

    private long keyForDeletion;

    /**
     * @return the current service review
     */
    public POServiceReview getServiceReview() {
        if (manageReviewModel.getServiceReview() == null) {
            initServiceReview();
        }
        return manageReviewModel.getServiceReview();
    }

    /**
     * Returns the key of the review which was selected for deletion.
     * 
     * @return the review key
     */
    public long getReviewKeyForDeletion() {
        if (manageReviewModel.getServiceReview() != null) {
            return manageReviewModel.getServiceReview().getKey();
        } else {
            return 0;
        }
    }

    /**
     * Sets the key of the review which should be deleted.
     * 
     * @param key
     *            key of the review
     */
    public void setReviewKeyForDeletion(long key) {
        manageReviewModel.setServiceReview(getServiceReview(key));
    }

    public void setupForDeletion() {
        manageReviewModel.setServiceReview(getServiceReview(keyForDeletion));
    }

    /**
     * Determines the service review for a given key.
     * 
     * @param reviewKey
     *            key of the review to find
     * @return the VO which matches the passed key.
     */
    private POServiceReview getServiceReview(long reviewKey) {
        Service selecteService = serviceDetailsModel.getSelectedService();
        if (reviewKey != 0 && selecteService != null) {
            List<POServiceReview> reviews;

            reviews = serviceDetailsModel.getSelectedServiceFeedback()
                    .getReviews();

            for (POServiceReview poServiceReview : reviews) {
                if (poServiceReview.getKey() == reviewKey) {
                    return poServiceReview;
                }
            }
        }
        return null;
    }

    private void initServiceReview() {
        // if no service is selected return null
        if (serviceDetailsModel.getSelectedService() == null) {
            manageReviewModel.setServiceReview(null);
            return;
        }

        // if service review not loaded yet or selected service has changed...
        if (manageReviewModel.getServiceReview() == null
                || serviceDetailsModel.getSelectedService().getKey() != serviceDetailsModel
                        .getSelectedServiceFeedback().getServiceKey()) {

            // search for an existing review
            for (POServiceReview review : serviceDetailsModel
                    .getSelectedServiceFeedback().getReviews()) {
                if (review.getUserId().equals(getUserFromSession().getUserId())) {
                    manageReviewModel.setServiceReview(review);
                    return;
                }
            }

            // logged in user wrote no review yet, create new service review
            manageReviewModel.setServiceReview(new POServiceReview("", 0, "",
                    getUserFromSession().getUserId(), serviceDetailsModel
                            .getSelectedServiceFeedback().getServiceKey()));
        }
    }

    /**
     * Sets the current service review to null to discard changes on the review.
     */
    public String cancelReview() {
        manageReviewModel.setServiceReview(null);
        setForwardUrl(getRequest());
        return OUTCOME_MARKETPLACE_REDIRECT;
    }

    /**
     * Creates or updates a review.
     */
    public String publishReview() throws SaaSApplicationException {
        if (!getConfig().isReviewEnabled()) {
            return OUTCOME_REVIEW_ENABLEMENT_CHANGED;
        }
        HttpServletRequest httpRequest = getRequest();
        getReviewService().writeReview(manageReviewModel.getServiceReview());

        setForwardUrl(httpRequest);
        return OUTCOME_MARKETPLACE_REDIRECT;
    }

    private void setForwardUrl(HttpServletRequest httpRequest) {
        String relativePath = "";
        if (httpRequest.getServletPath() != null) {
            relativePath += httpRequest.getServletPath();
        }
        if (httpRequest.getPathInfo() != null) {
            relativePath += httpRequest.getPathInfo();
        }
        String queryPart = getServiceDetailsQueryPart(httpRequest,
                ui.findSessionBean());
        if (queryPart != null) {
            relativePath += queryPart;
        }
        httpRequest.getSession().setAttribute(Constants.SESS_ATTR_FORWARD_URL,
                relativePath);
    }

    /**
     * Removes (deletes) the current service review. This should only be called
     * if an existing review is edited.
     * 
     * @return success if no exception occurred
     * @throws OrganizationAuthoritiesException
     */
    public String removeReview() throws OperationNotPermittedException,
            ObjectNotFoundException {
        if (manageReviewModel.getServiceReview().getKey() != 0) {
            getReviewService().deleteReview(
                    manageReviewModel.getServiceReview());
        }
        setForwardUrl(getRequest());
        return OUTCOME_MARKETPLACE_REDIRECT;
    }

    /**
     * @return the count of characters that are left to extend the review
     *         comment
     */
    public String getLeftCharacters() {
        return String.valueOf(ADMValidator.LENGTH_COMMENT
                - getServiceReview().getComment().length());
    }

    /**
     * @return the maximum length a title can have.
     */
    public String getTitleLength() {
        return String.valueOf(ADMValidator.LENGTH_NAME);
    }

    /**
     * @return true if this is a transient review, false if an existing review
     *         is edited.
     */
    public boolean getIsNewReview() {
        return getServiceReview().getKey() == 0;
    }

    /**
     * @return the maximal count of character which can be used to write a
     *         review comment.
     */
    public String getCommentLength() {
        return String.valueOf(ADMValidator.LENGTH_COMMENT);
    }

    /**
     * Returns the reason of the deletion by an marketplace administrator.
     * 
     * @return the reason of the deletion
     */
    public String getDeletionReason() {
        return manageReviewModel.getDeletionReason();
    }

    /**
     * Sets the reason of the deletion by an marketplace administrator.
     * 
     * @param deletionReason
     *            the reason of the deletion
     */
    public void setDeletionReason(String deletionReason) {
        manageReviewModel.setDeletionReason(deletionReason);
    }

    /**
     * Invokes the corresponding service method to delete a service review.
     * 
     * @return a string which triggers the navigation to the same page.
     * 
     * @throws OperationNotPermittedException
     *             thrown in case the caller is not permitted to call the
     *             service method
     * @throws ObjectNotFoundException
     *             thrown in case the passed review was not found
     */
    public String removeReviewByMarketplaceAdmin()
            throws OperationNotPermittedException, ObjectNotFoundException {
        HttpServletRequest httpRequest = getRequest();
        if (manageReviewModel.getServiceReview() != null) {
            getReviewService().deleteReviewByMarketplaceOwner(
                    manageReviewModel.getServiceReview(),
                    manageReviewModel.getDeletionReason());
        }

        setForwardUrl(httpRequest);
        return OUTCOME_MARKETPLACE_REDIRECT;
    }

    public boolean getMarketplaceOwner() {
        return isMarketplaceOwner();
    }

    public MarketplaceConfiguration getConfig() {
        return marketplaceService.getCachedMarketplaceConfiguration(BaseBean
                .getMarketplaceIdStatic());
    }

    public ServiceDetailsModel getServiceDetailsModel() {
        return serviceDetailsModel;
    }

    public void setServiceDetailsModel(ServiceDetailsModel serviceDetailsModel) {
        this.serviceDetailsModel = serviceDetailsModel;
    }

    public ManageReviewModel getManageReviewModel() {
        return manageReviewModel;
    }

    public void setManageReviewModel(ManageReviewModel manageReviewModel) {
        this.manageReviewModel = manageReviewModel;
    }

    public long getKeyForDeletion() {
        return keyForDeletion;
    }

    public void setKeyForDeletion(long keyForDeletion) {
        this.keyForDeletion = keyForDeletion;
    }
}
