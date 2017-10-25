/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.mpownershare;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.oscm.billingservice.business.BigDecimalAdapter;
import org.oscm.billingservice.business.calculation.BigDecimals;
import org.oscm.validation.Invariants;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "RevenueShareDetails")
public class RevenueShareDetails {

    @XmlAttribute(name = "serviceRevenue", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal serviceRevenue;

    @XmlAttribute(name = "marketplaceRevenueSharePercentage", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal marketplaceRevenueSharePercentage;

    @XmlAttribute(name = "operatorRevenueSharePercentage")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal operatorRevenueSharePercentage;

    @XmlAttribute(name = "resellerRevenueSharePercentage", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal resellerRevenueSharePercentage;

    @XmlAttribute(name = "brokerRevenueSharePercentage")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal brokerRevenueSharePercentage;

    @XmlAttribute(name = "marketplaceRevenue", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal marketplaceRevenue;

    @XmlAttribute(name = "operatorRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal operatorRevenue;

    @XmlAttribute(name = "brokerRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal brokerRevenue;

    @XmlAttribute(name = "resellerRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal resellerRevenue;

    @XmlAttribute(name = "amountForSupplier", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal amountForSupplier;

    public BigDecimal getServiceRevenue() {
        return serviceRevenue;
    }

    public void setServiceRevenue(BigDecimal serviceRevenue) {
        this.serviceRevenue = serviceRevenue;
    }

    public BigDecimal getMarketplaceRevenueSharePercentage() {
        return marketplaceRevenueSharePercentage;
    }

    public void setMarketplaceRevenueSharePercentage(
            BigDecimal marketplaceRevenueSharePercentage) {
        this.marketplaceRevenueSharePercentage = marketplaceRevenueSharePercentage;
    }

    public BigDecimal getOperatorRevenueSharePercentage() {
        return operatorRevenueSharePercentage;
    }

    public void setOperatorRevenueSharePercentage(BigDecimal value) {
        this.operatorRevenueSharePercentage = value;
    }

    public BigDecimal getOperatorRevenue() {
        return operatorRevenue;
    }

    public void setOperatorRevenue(BigDecimal value) {
        this.operatorRevenue = value;
    }

    public BigDecimal getResellerRevenueSharePercentage() {
        return resellerRevenueSharePercentage;
    }

    public void setResellerRevenueSharePercentage(
            BigDecimal resellerRevenueSharePercentage) {
        this.resellerRevenueSharePercentage = resellerRevenueSharePercentage;
    }

    public BigDecimal getBrokerRevenueSharePercentage() {
        return brokerRevenueSharePercentage;
    }

    public void setBrokerRevenueSharePercentage(
            BigDecimal brokerRevenueSharePercentage) {
        this.brokerRevenueSharePercentage = brokerRevenueSharePercentage;
    }

    public BigDecimal getMarketplaceRevenue() {
        return marketplaceRevenue;
    }

    public void setMarketplaceRevenue(BigDecimal marketplaceRevenue) {
        this.marketplaceRevenue = marketplaceRevenue;
    }

    public BigDecimal getBrokerRevenue() {
        return brokerRevenue;
    }

    public void setBrokerRevenue(BigDecimal brokerRevenue) {
        this.brokerRevenue = brokerRevenue;
    }

    public BigDecimal getResellerRevenue() {
        return resellerRevenue;
    }

    public void setResellerRevenue(BigDecimal resellerRevenue) {
        this.resellerRevenue = resellerRevenue;
    }

    public BigDecimal getAmountForSupplier() {
        return amountForSupplier;
    }

    public void setAmountForSupplier(BigDecimal amountForSupplier) {
        this.amountForSupplier = amountForSupplier;
    }

    public void calculate(OfferingType model) {
        plausibilityChecks();

        marketplaceRevenue = BigDecimals.calculatePercent(
                marketplaceRevenueSharePercentage, serviceRevenue);
        operatorRevenue = BigDecimals.calculatePercent(
                operatorRevenueSharePercentage, serviceRevenue);

        amountForSupplier = serviceRevenue.subtract(marketplaceRevenue)
                .subtract(operatorRevenue);

        switch (model) {
        case BROKER:
            brokerRevenue = BigDecimals.calculatePercent(
                    brokerRevenueSharePercentage, serviceRevenue);
            amountForSupplier = amountForSupplier.subtract(brokerRevenue);
            break;
        case RESELLER:
            resellerRevenue = BigDecimals.calculatePercent(
                    resellerRevenueSharePercentage, serviceRevenue);
            amountForSupplier = amountForSupplier.subtract(resellerRevenue);
            break;
        default:
            break;
        }
    }

    private void plausibilityChecks() {
        Invariants.assertTrue(marketplaceRevenueSharePercentage
                .compareTo(BigDecimal.ZERO) >= 0);
        Invariants.assertTrue(marketplaceRevenueSharePercentage
                .compareTo(new BigDecimal(100)) <= 0);

        if (resellerRevenueSharePercentage != null) {
            Invariants.assertTrue(resellerRevenueSharePercentage
                    .compareTo(BigDecimal.ZERO) >= 0);
            Invariants.assertTrue(resellerRevenueSharePercentage
                    .compareTo(new BigDecimal(100)) <= 0);
        }

        if (brokerRevenueSharePercentage != null) {
            Invariants.assertTrue(brokerRevenueSharePercentage
                    .compareTo(BigDecimal.ZERO) >= 0);
            Invariants.assertTrue(brokerRevenueSharePercentage
                    .compareTo(new BigDecimal(100)) <= 0);
        }
    }
}
