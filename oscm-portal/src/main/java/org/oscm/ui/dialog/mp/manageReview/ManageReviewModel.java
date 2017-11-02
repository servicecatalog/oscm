/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-1-2                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.manageReview;

import org.oscm.ui.beans.BaseModel;
import org.oscm.internal.review.POServiceReview;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Model for manage service's review
 * 
 * @author Gao
 * 
 */
@ManagedBean
@ViewScoped
public class ManageReviewModel extends BaseModel {

    private static final long serialVersionUID = 7702100381399626924L;

    private String deletionReason;

    private POServiceReview serviceReview;

    public String getDeletionReason() {
        return deletionReason;
    }

    public void setDeletionReason(String deletionReason) {
        this.deletionReason = deletionReason;
    }

    public POServiceReview getServiceReview() {
        return serviceReview;
    }

    public void setServiceReview(POServiceReview serviceReview) {
        this.serviceReview = serviceReview;
    }
}
