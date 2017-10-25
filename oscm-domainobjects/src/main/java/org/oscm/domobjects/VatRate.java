/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 16.11.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * JPA managed entity representing a VAT rate.
 * 
 * @author pock
 * 
 */
@Entity
public class VatRate extends DomainObjectWithHistory<VatRateData> {

    private static final long serialVersionUID = 4964881126238266762L;

    private static final transient Log4jLogger logger = LoggerFactory
            .getLogger(VatRate.class);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Organization owningOrganization;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private SupportedCountry targetCountry;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Organization targetOrganization;

    public VatRate() {
        setDataContainer(new VatRateData());
    }

    public Organization getOwningOrganization() {
        return owningOrganization;
    }

    public void setOwningOrganization(Organization owningOrganization) {
        this.owningOrganization = owningOrganization;
    }

    public SupportedCountry getTargetCountry() {
        return targetCountry;
    }

    public void setTargetCountry(SupportedCountry targetCountry) {
        if (getTargetOrganization() != null && targetCountry != null) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "Cannot set a target country if a target organization exists.");
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.ERROR_SET_TARGET_COUNTRY_FAILED_ORGANIZATION_EXISTS);
            throw e;
        }
        this.targetCountry = targetCountry;
    }

    public Organization getTargetOrganization() {
        return targetOrganization;
    }

    public void setTargetOrganization(Organization targetOrganization) {
        if (getTargetCountry() != null && targetOrganization != null) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "Cannot set a target organization if a target country exists.");
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.ERROR_SET_TARGET_ORGANIZATION_FAILED_COUNTRY_EXISTS);
            throw e;
        }
        this.targetOrganization = targetOrganization;
    }

    public void setRate(BigDecimal rate) {
        dataContainer.setRate(rate);
    }

    public BigDecimal getRate() {
        return dataContainer.getRate();
    }

}
