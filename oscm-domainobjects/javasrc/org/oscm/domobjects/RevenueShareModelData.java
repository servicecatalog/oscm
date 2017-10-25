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

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.*;

import org.oscm.domobjects.converters.RSMDConverter;
import org.oscm.domobjects.enums.RevenueShareModelType;

/**
 * The data object for a revenue share model.
 * 
 * @author farmaki
 * 
 */

@Embeddable
public class RevenueShareModelData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = -3450635877726821864L;

    @Column(nullable = false)
    private BigDecimal revenueShare = BigDecimal.ZERO;

    public BigDecimal getRevenueShare() {
        return revenueShare;
    }

    public void setRevenueShare(BigDecimal revenueShare) {
        this.revenueShare = revenueShare;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RevenueShareModelType revenueShareModelType;

    public RevenueShareModelType getRevenueShareModelType() {
        return revenueShareModelType;
    }

    public void setRevenueShareModelType(
            RevenueShareModelType revenueShareModelType) {
        this.revenueShareModelType = revenueShareModelType;
    }

}
