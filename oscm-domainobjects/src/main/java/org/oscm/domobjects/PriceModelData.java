/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time: 04.08.2009                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.*;

import org.oscm.domobjects.converters.PriceModelTConverter;
import org.oscm.domobjects.converters.PricingPeriodConverter;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * DataContainer for domain object PriceModel
 * 
 * @see PriceModel
 * 
 * @author schmid
 */
@Embeddable
public class PriceModelData extends DomainDataContainer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PriceModelType type = PriceModelType.FREE_OF_CHARGE;

    /**
     * Period to be settled (YEAR, MONTH, WEEK, DAY)
     */
    @Enumerated(EnumType.STRING)
    private PricingPeriod period;

    /**
     * Free trial period in days.
     */
    @Column(nullable = false)
    private int freePeriod;

    /**
     * Price of service period (in platform standard currency)
     */
    @Column(nullable = false)
    private BigDecimal pricePerPeriod = BigDecimal.ZERO;

    /**
     * Price of an assignment of one user for the entire user related period.
     */
    @Column(nullable = false)
    private BigDecimal pricePerUserAssignment = BigDecimal.ZERO;

    /**
     * Flag indicating that provisioning process has finished. The pricemodel is
     * ready for beeing charged.
     */
    @Column(nullable = false)
    private boolean provisioningCompleted = true;

    /**
     * A pricing option for one-time fee.
     */
    @Column(nullable = false)
    private BigDecimal oneTimeFee = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private boolean external;
    
    public PriceModelData() {
        super();
    }

    public int getFreePeriod() {
        return freePeriod;
    }

    public void setFreePeriod(int freePeriod) {
        this.freePeriod = freePeriod;
    }

    public PricingPeriod getPeriod() {
        return period;
    }

    public void setPeriod(PricingPeriod period) {
        this.period = period;
    }

    public BigDecimal getPricePerPeriod() {
        return pricePerPeriod;
    }

    public void setPricePerPeriod(BigDecimal pricePerPeriod) {
        this.pricePerPeriod = pricePerPeriod;
    }

    public BigDecimal getPricePerUserAssignment() {
        return pricePerUserAssignment;
    }

    public void setPricePerUserAssignment(BigDecimal pricePerUser) {
        this.pricePerUserAssignment = pricePerUser;
    }

    public BigDecimal getOneTimeFee() {
        return oneTimeFee;
    }

    public void setOneTimeFee(BigDecimal oneTimeFee) {
        this.oneTimeFee = oneTimeFee;
    }

    public PriceModelType getType() {
        return type;
    }

    public void setType(PriceModelType type) {
        this.type = type;
    }

    public void setProvisioningCompleted(boolean completed) {
        this.provisioningCompleted = completed;
    }

    public boolean isProvisioningCompleted() {
        return provisioningCompleted;
    }
    
    public void setExternal(boolean external) {
        this.external = external;
    }

    public boolean isExternal() {
        return external;
    }
}
