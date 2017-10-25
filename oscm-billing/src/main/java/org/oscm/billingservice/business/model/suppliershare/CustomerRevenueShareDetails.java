/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.oscm.billingservice.business.BigDecimalAdapter;
import org.oscm.billingservice.business.calculation.BigDecimals;
import org.oscm.converter.PriceConverter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "CustomerRevenueShareDetails")
public class CustomerRevenueShareDetails {
    @XmlAttribute(name = "customerName", required = true)
    protected String customerName;

    @XmlAttribute(name = "customerId", required = true)
    protected String customerId;

    @XmlAttribute(name = "serviceRevenue", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal serviceRevenue;

    @XmlAttribute(name = "marketplaceRevenue", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal marketplaceRevenue;

    @XmlAttribute(name = "operatorRevenue", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal operatorRevenue;

    @XmlAttribute(name = "amountForSupplier", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal amountForSupplier;

    @XmlAttribute(name = "resellerRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal resellerRevenue;

    @XmlAttribute(name = "brokerRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal brokerRevenue;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

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

    public BigDecimal getAmountForSupplier() {
        return amountForSupplier;
    }

    public void setAmountForSupplier(BigDecimal amountForSupplier) {
        this.amountForSupplier = amountForSupplier;
    }

    public BigDecimal getResellerRevenue() {
        return resellerRevenue;
    }

    public void setResellerRevenue(BigDecimal resellerRevenue) {
        this.resellerRevenue = resellerRevenue;
    }

    public BigDecimal getBrokerRevenue() {
        return brokerRevenue;
    }

    public void setBrokerRevenue(BigDecimal brokerRevenue) {
        this.brokerRevenue = brokerRevenue;
    }

    public void calculate(BigDecimal marketplaceRevenueSharePercentage,
            BigDecimal operatorRevenueSharePercentage,
            BigDecimal brokerRevenueSharePercentage,
            BigDecimal resellerRevenueSharePercentage) {
        serviceRevenue = serviceRevenue.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);

        if (marketplaceRevenueSharePercentage == null) {
            marketplaceRevenueSharePercentage = BigDecimal.ZERO;
        }
        marketplaceRevenueSharePercentage = marketplaceRevenueSharePercentage
                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                        PriceConverter.ROUNDING_MODE);
        marketplaceRevenue = BigDecimals.calculatePercent(
                marketplaceRevenueSharePercentage, serviceRevenue);
        setAmountForSupplier(serviceRevenue.subtract(marketplaceRevenue));

        operatorRevenueSharePercentage = operatorRevenueSharePercentage
                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                        PriceConverter.ROUNDING_MODE);
        operatorRevenue = BigDecimals.calculatePercent(
                operatorRevenueSharePercentage, serviceRevenue);
        setAmountForSupplier(amountForSupplier.subtract(operatorRevenue));

        if (brokerRevenueSharePercentage != null) {
            brokerRevenueSharePercentage = brokerRevenueSharePercentage
                    .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                            PriceConverter.ROUNDING_MODE);
            brokerRevenue = BigDecimals.calculatePercent(
                    brokerRevenueSharePercentage, serviceRevenue);
            setAmountForSupplier(amountForSupplier.subtract(brokerRevenue));
        }

        if (resellerRevenueSharePercentage != null) {
            resellerRevenueSharePercentage = resellerRevenueSharePercentage
                    .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                            PriceConverter.ROUNDING_MODE);
            resellerRevenue = BigDecimals.calculatePercent(
                    resellerRevenueSharePercentage, serviceRevenue);
            setAmountForSupplier(amountForSupplier.subtract(resellerRevenue));
        }
    }

    public void updateServiceRevenue(CustomerRevenueShareDetails cus,
            BigDecimal marketplaceRevenueSharePercentage,
            BigDecimal operatorRevenueSharePercentage,
            BigDecimal brokerRevenueSharePercentage,
            BigDecimal resellerRevenueSharePercentage) {
        this.serviceRevenue = serviceRevenue.add(cus.getServiceRevenue());
        calculate(marketplaceRevenueSharePercentage,
                operatorRevenueSharePercentage, brokerRevenueSharePercentage,
                resellerRevenueSharePercentage);
    }

}
