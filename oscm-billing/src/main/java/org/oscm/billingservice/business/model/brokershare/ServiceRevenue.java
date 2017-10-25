/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.brokershare;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.oscm.billingservice.business.BigDecimalAdapter;
import org.oscm.billingservice.business.calculation.BigDecimals;
import org.oscm.converter.PriceConverter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "serviceCustomerRevenues" })
@XmlRootElement(name = "ServiceRevenue")
public class ServiceRevenue {

    @XmlElement(name = "ServiceCustomerRevenue")
    protected List<ServiceCustomerRevenue> serviceCustomerRevenues;

    @XmlAttribute(name = "totalAmount", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal totalAmount = BigDecimal.valueOf(0);

    @XmlAttribute(name = "brokerRevenueSharePercentage", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal brokerRevenueSharePercentage;

    @XmlAttribute(name = "brokerRevenue", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal brokerRevenue;

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal value) {
        this.totalAmount = value;
    }

    public BigDecimal getBrokerRevenueSharePercentage() {
        return brokerRevenueSharePercentage;
    }

    public void setBrokerRevenueSharePercentage(BigDecimal value) {
        this.brokerRevenueSharePercentage = value;
    }

    public BigDecimal getBrokerRevenue() {
        return brokerRevenue;
    }

    public List<ServiceCustomerRevenue> getServiceCustomerRevenues() {
        if (serviceCustomerRevenues == null) {
            serviceCustomerRevenues = new ArrayList<ServiceCustomerRevenue>();
        }
        return this.serviceCustomerRevenues;
    }

    public void addServiceCustomerRevenue(
            ServiceCustomerRevenue serviceCustomerRevenue) {
        boolean isEmptyOrNew = true;
        for (ServiceCustomerRevenue customerRevenue : getServiceCustomerRevenues()) {
            if (customerRevenue.getCustomerId().equals(
                    serviceCustomerRevenue.getCustomerId())) {
                isEmptyOrNew = false;
                customerRevenue.setBrokerRevenue(customerRevenue
                        .getBrokerRevenue().add(
                                serviceCustomerRevenue.getBrokerRevenue()));
                customerRevenue.setTotalAmount(customerRevenue.getTotalAmount()
                        .add(serviceCustomerRevenue.getTotalAmount()));
            }
        }
        if (isEmptyOrNew) {
            getServiceCustomerRevenues().add(serviceCustomerRevenue);
        }
    }

    public void calculate() {
        // serviceCustomerRevenue
        BigDecimal scrTotalAmount = BigDecimal.ZERO;
        BigDecimal scrBrokerRevenue = BigDecimal.ZERO;
        for (ServiceCustomerRevenue revenue : getServiceCustomerRevenues()) {
            revenue.calculate();
            scrTotalAmount = scrTotalAmount.add(revenue.getTotalAmount());
            scrBrokerRevenue = scrBrokerRevenue.add(revenue.getBrokerRevenue());
        }
        this.totalAmount = scrTotalAmount;
        this.brokerRevenue = scrBrokerRevenue;
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
