/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.brokershare;

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

    @XmlAttribute(name = "brokerRevenueSharePercentage", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal brokerRevenueSharePercentage;

    @XmlAttribute(name = "brokerRevenue", required = true)
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getBrokerRevenueSharePercentage() {
        return brokerRevenueSharePercentage;
    }

    public void setBrokerRevenueSharePercentage(
            BigDecimal brokerRevenueSharePercentage) {
        this.brokerRevenueSharePercentage = brokerRevenueSharePercentage;
    }

    public BigDecimal getBrokerRevenue() {
        return brokerRevenue;
    }

    public void setBrokerRevenue(BigDecimal brokerRevenue) {
        this.brokerRevenue = brokerRevenue;
    }

    public void calculate() {
        brokerRevenueSharePercentage = brokerRevenueSharePercentage.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        totalAmount = totalAmount.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        brokerRevenue = BigDecimals.calculatePercent(
                brokerRevenueSharePercentage, totalAmount);
    }

}
