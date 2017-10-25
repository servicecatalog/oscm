/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                            
 *                                                                              
 *  Creation Date: 16.11.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The data container for a VAT rate instance - holds the VAT rate rate.
 * 
 * @author pock
 * 
 */
@Embeddable
public class VatRateData extends DomainDataContainer {

    private static final long serialVersionUID = 7709845594870908299L;

    @Column(nullable = false)
    private BigDecimal rate = BigDecimal.ZERO;

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

}
