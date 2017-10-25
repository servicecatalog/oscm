/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.resellershare;

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
@XmlRootElement(name = "ServiceCustomerRevenue")
public class ServiceCustomerRevenue {

    @XmlAttribute(name = "customerName", required = true)
    protected String customerName;

    @XmlAttribute(name = "customerId", required = true)
    protected String customerId;

    @XmlAttribute(name = "totalAmount", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal totalAmount = BigDecimal.valueOf(0);

    @XmlAttribute(name = "resellerRevenueSharePercentage", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal resellerRevenueSharePercentage;

    @XmlAttribute(name = "resellerRevenue", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal resellerRevenue;

    @XmlAttribute(name = "purchasePrice", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal purchasePrice;

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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getResellerRevenueSharePercentage() {
        return resellerRevenueSharePercentage;
    }

    public void setResellerRevenueSharePercentage(
            BigDecimal resellerRevenueSharePercentage) {
        this.resellerRevenueSharePercentage = resellerRevenueSharePercentage;
    }

    public BigDecimal getResellerRevenue() {
        return resellerRevenue;
    }

    public void setResellerRevenue(BigDecimal resellerRevenue) {
        this.resellerRevenue = resellerRevenue;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public void calculate() {
        resellerRevenueSharePercentage = resellerRevenueSharePercentage
                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                        PriceConverter.ROUNDING_MODE);
        totalAmount = totalAmount.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        resellerRevenue = BigDecimals.calculatePercent(
                resellerRevenueSharePercentage, totalAmount);
        purchasePrice = totalAmount.subtract(resellerRevenue);
    }
}
