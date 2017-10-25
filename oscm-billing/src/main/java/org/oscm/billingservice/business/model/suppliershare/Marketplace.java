/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.oscm.converter.PriceConverter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "marketplaceOwner", "service",
        "revenuePerMarketplace" })
@XmlRootElement(name = "Marketplace")
public class Marketplace {

    @XmlElement(name = "MarketplaceOwner", required = true)
    protected MarketplaceOwner marketplaceOwner;

    @XmlElement(name = "Service", required = true)
    protected List<Service> service;

    @XmlElement(name = "RevenuePerMarketplace", required = true)
    protected RevenuePerMarketplace revenuePerMarketplace = new RevenuePerMarketplace();

    @XmlAttribute(name = "id", required = true)
    protected String id;

    @XmlAttribute(name = "key", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger key;

    private transient BigDecimal revenueSharePercentage;

    transient BigDecimal serviceRevenue = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    transient BigDecimal marketplaceRevenue = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    transient BigDecimal operatorRevenue = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    transient BigDecimal resellerRevenue = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    transient BigDecimal brokerRevenue = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

    public Marketplace() {
    }

    public MarketplaceOwner getMarketplaceOwner() {
        return marketplaceOwner;
    }

    public void setMarketplaceOwner(MarketplaceOwner value) {
        this.marketplaceOwner = value;
    }

    public List<Service> getService() {
        if (service == null) {
            service = new ArrayList<Service>();
        }
        return this.service;
    }

    public Service getServiceByKey(long productKey) {
        for (Service service : getService()) {
            if (service.getKey().longValue() == productKey) {
                return service;
            }
        }
        return null;
    }

    public void addService(Service service) {
        getService().add(service);
    }

    public RevenuePerMarketplace getRevenuePerMarketplace() {
        return revenuePerMarketplace;
    }

    public void setRevenuePerMarketplace(RevenuePerMarketplace value) {
        this.revenuePerMarketplace = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public BigInteger getKey() {
        return key;
    }

    public void setKey(BigInteger value) {
        this.key = value;
    }

    public BigDecimal getRevenueSharePercentage() {
        return revenueSharePercentage;
    }

    public void setRevenueSharePercentage(BigDecimal revenueSharePercentage) {
        this.revenueSharePercentage = revenueSharePercentage;
    }

    public void calculate() {
        // calculation for services has to be done first
        for (Service service : getService()) {
            service.calculate();
            updateRevenueSums(service);
        }

        // after service calculation compute marketplace shares
        updateOverallCosts();
    }

    private void updateRevenueSums(Service service) {
        marketplaceRevenue = marketplaceRevenue.add(service
                .getRevenueShareDetails().getMarketplaceRevenue());
        operatorRevenue = operatorRevenue.add(service.getRevenueShareDetails()
                .getOperatorRevenue());
        serviceRevenue = serviceRevenue.add(service.getRevenueShareDetails()
                .getServiceRevenue());
        if (service.getBroker() != null) {
            brokerRevenue = brokerRevenue.add(service.getRevenueShareDetails()
                    .getBrokerRevenue());
        } else if (service.getReseller() != null) {
            resellerRevenue = resellerRevenue.add(service
                    .getRevenueShareDetails().getResellerRevenue());
        }
    }

    private void updateOverallCosts() {
        revenuePerMarketplace.setBrokerRevenue(brokerRevenue);
        revenuePerMarketplace.setResellerRevenue(resellerRevenue);
        revenuePerMarketplace.setServiceRevenue(serviceRevenue);
        revenuePerMarketplace.setMarketplaceRevenue(marketplaceRevenue);
        revenuePerMarketplace.setOperatorRevenue(operatorRevenue);
        revenuePerMarketplace.setOverallRevenue(serviceRevenue
                .subtract(marketplaceRevenue).subtract(resellerRevenue)
                .subtract(brokerRevenue).subtract(operatorRevenue));
    }
}
