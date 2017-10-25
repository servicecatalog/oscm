/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: farmaki                                                     
 *                                                                              
 *  Creation Date: 16.07.2012                                                    
 *                                                                              
 *  Completion Time: 16.07.2012                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Entity;

import org.oscm.domobjects.enums.RevenueShareModelType;

@Entity
public class RevenueShareModel extends
        DomainObjectWithHistory<RevenueShareModelData> {

    public static final BigDecimal MAX_REVENUE_SHARE = new BigDecimal("100");

    public static final BigDecimal MIN_REVENUE_SHARE = BigDecimal.ZERO;

    private static final long serialVersionUID = 7802515891824949612L;

    public RevenueShareModel() {
        setDataContainer(new RevenueShareModelData());
    }

    public void setRevenueShare(BigDecimal revenueShare) {
        dataContainer.setRevenueShare(revenueShare);
    }

    public BigDecimal getRevenueShare() {
        return dataContainer.getRevenueShare();
    }

    public RevenueShareModelType getRevenueShareModelType() {
        return dataContainer.getRevenueShareModelType();
    }

    public void setRevenueShareModelType(RevenueShareModelType type) {
        dataContainer.setRevenueShareModelType(type);
    }

    /**
     * Copies this revenue share model. The reference to the parent element has
     * to be set after copying.
     * 
     * @return the copy of this
     */
    public RevenueShareModel copy() {
        RevenueShareModel rs = new RevenueShareModel();
        rs.setRevenueShare(getRevenueShare());
        rs.setRevenueShareModelType(getRevenueShareModelType());
        return rs;
    }

}
