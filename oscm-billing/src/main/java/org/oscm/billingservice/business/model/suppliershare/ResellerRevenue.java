/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.oscm.billingservice.business.BigDecimalAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResellerRevenue")
public class ResellerRevenue {

    @XmlAttribute(name = "serviceRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal serviceRevenue;

    @XmlAttribute(name = "marketplaceRevenue", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal marketplaceRevenue;

    @XmlAttribute(name = "operatorRevenue", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal operatorRevenue;

    @XmlAttribute(name = "resellerRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal resellerRevenue;

    @XmlAttribute(name = "overallRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal overallRevenue;

    public BigDecimal getServiceRevenue() {
        return serviceRevenue;
    }

    public void setServiceRevenue(BigDecimal serviceRevenue) {
        this.serviceRevenue = serviceRevenue;
    }

    public BigDecimal getMarketplaceRevenue() {
        return marketplaceRevenue;
    }

    public void setMarketplaceRevenue(BigDecimal marketplaceRevenue) {
        this.marketplaceRevenue = marketplaceRevenue;
    }

    public BigDecimal getOperatorRevenue() {
        return operatorRevenue;
    }

    public void setOperatorRevenue(BigDecimal value) {
        this.operatorRevenue = value;
    }

    public BigDecimal getResellerRevenue() {
        return resellerRevenue;
    }

    public void setResellerRevenue(BigDecimal resellerRevenue) {
        this.resellerRevenue = resellerRevenue;
    }

    public BigDecimal getOverallRevenue() {
        return overallRevenue;
    }

    public void setOverallRevenue(BigDecimal overallRevenue) {
        this.overallRevenue = overallRevenue;
    }

    public void sumServiceRevenue(BigDecimal valueToAdd) {
        if (serviceRevenue == null) {
            serviceRevenue = BigDecimal.ZERO;
        }
        this.serviceRevenue = serviceRevenue.add(valueToAdd);
    }

    public void sumMarketplaceRevenue(BigDecimal valueToAdd) {
        if (marketplaceRevenue == null) {
            marketplaceRevenue = BigDecimal.ZERO;
        }
        this.marketplaceRevenue = marketplaceRevenue.add(valueToAdd);
    }

    public void sumOperatorRevenue(BigDecimal valueToAdd) {
        if (operatorRevenue == null) {
            operatorRevenue = BigDecimal.ZERO;
        }
        this.operatorRevenue = operatorRevenue.add(valueToAdd);
    }

    public void sumResellerRevenue(BigDecimal valueToAdd) {
        if (resellerRevenue == null) {
            resellerRevenue = BigDecimal.ZERO;
        }
        this.resellerRevenue = resellerRevenue.add(valueToAdd);
    }

    public void calculate() {
        overallRevenue = serviceRevenue.subtract(marketplaceRevenue)
                .subtract(resellerRevenue).subtract(operatorRevenue);
    }

}
