/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
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

    @XmlAttribute(name = "marketplaceRevenue", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal marketplaceRevenue;

    @XmlAttribute(name = "operatorRevenueSharePercentage", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal operatorRevenueSharePercentage;

    @XmlAttribute(name = "operatorRevenue", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal operatorRevenue;

    @XmlAttribute(name = "resellerRevenueSharePercentage")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal resellerRevenueSharePercentage;

    @XmlAttribute(name = "resellerRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal resellerRevenue;

    @XmlAttribute(name = "brokerRevenueSharePercentage")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal brokerRevenueSharePercentage;

    @XmlAttribute(name = "brokerRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal brokerRevenue;

    @XmlAttribute(name = "amountForSupplier", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal amountForSupplier;

    @XmlElement(name = "CustomerRevenueShareDetails")
    protected List<CustomerRevenueShareDetails> customerRevenueShareDetails;

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

    public BigDecimal getMarketplaceRevenue() {
        return marketplaceRevenue;
    }

    public void setMarketplaceRevenue(BigDecimal marketplaceRevenue) {
        this.marketplaceRevenue = marketplaceRevenue;
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

    public BigDecimal getResellerRevenue() {
        return resellerRevenue;
    }

    public void setResellerRevenue(BigDecimal resellerRevenue) {
        this.resellerRevenue = resellerRevenue;
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

    public BigDecimal getAmountForSupplier() {
        return amountForSupplier;
    }

    public void addCustomerRevenueShareDetails(CustomerRevenueShareDetails crs) {
        boolean isEmptyOrNew = true;
        if (customerRevenueShareDetails != null) {
            for (CustomerRevenueShareDetails cus : customerRevenueShareDetails) {
                if (cus.getCustomerId().equals(crs.getCustomerId())) {
                    crs.updateServiceRevenue(cus,
                            marketplaceRevenueSharePercentage,
                            operatorRevenueSharePercentage,
                            brokerRevenueSharePercentage,
                            resellerRevenueSharePercentage);
                    List<CustomerRevenueShareDetails> customerRSD = new LinkedList<CustomerRevenueShareDetails>(
                            customerRevenueShareDetails);
                    if (customerRSD.remove(cus)) {
                        customerRSD.add(crs);
                        customerRevenueShareDetails = customerRSD;
                        isEmptyOrNew = false;
                    }
                }
            }
        }
        if (isEmptyOrNew) {
            getCustomerRevenueShareDetails().add(crs);
        }
    }

    public List<CustomerRevenueShareDetails> getCustomerRevenueShareDetails() {
        if (customerRevenueShareDetails == null) {
            customerRevenueShareDetails = new ArrayList<CustomerRevenueShareDetails>();
        }
        return customerRevenueShareDetails;
    }

    public void calculate(Seller sellerType) {
        marketplaceRevenue = BigDecimals.calculatePercent(
                marketplaceRevenueSharePercentage, serviceRevenue);
        operatorRevenue = BigDecimals.calculatePercent(
                operatorRevenueSharePercentage, serviceRevenue);

        amountForSupplier = serviceRevenue.subtract(marketplaceRevenue)
                .subtract(operatorRevenue);

        switch (sellerType) {
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
}
