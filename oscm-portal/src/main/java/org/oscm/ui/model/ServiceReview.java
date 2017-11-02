/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: May 20, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import java.math.BigDecimal;

import org.oscm.ui.common.RatingCssMapper;
import org.oscm.internal.review.POServiceReview;

/**
 * Wrapper class for a POServiceReview providing additional convenience methods.
 * 
 * @author barzu
 */
public class ServiceReview {

    private POServiceReview po;
    private boolean belongsToLoggedInUser;

    public ServiceReview(POServiceReview po) {
        this.po = po;
    }

    public POServiceReview getPo() {
        return po;
    }

    public String getRatingCss() {
        if (po != null) {
            return RatingCssMapper
                    .getRatingClass(new BigDecimal(po.getRating()));
        }
        return RatingCssMapper.getRatingClass(BigDecimal.ZERO);
    }

    public boolean isBelongsToLoggedInUser() {
        return belongsToLoggedInUser;
    }

    public void setBelongsToLoggedInUser(boolean belongsToLoggedInUser) {
        this.belongsToLoggedInUser = belongsToLoggedInUser;
    }
}
