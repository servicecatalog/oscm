/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.mpownershare;

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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "service", "revenuesPerMarketplace" })
@XmlRootElement(name = "Marketplace")
public class Marketplace {

    @XmlElement(name = "Service", required = true)
    protected List<Service> service;

    @XmlElement(name = "RevenuesPerMarketplace", required = true)
    protected RevenuesPerMarketplace revenuesPerMarketplace = new RevenuesPerMarketplace();

    @XmlAttribute(name = "id", required = true)
    protected String id;

    @XmlAttribute(name = "key", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger key;

    private transient BigDecimal revenueSharePercentage;

    public Marketplace() {
    }

    public Marketplace(String marketplaceId) {
        setId(marketplaceId);
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

    public RevenuesPerMarketplace getRevenuesPerMarketplace() {
        return revenuesPerMarketplace;
    }

    public void setRevenuesPerMarketplace(
            RevenuesPerMarketplace revenuesPerMarketplace) {
        this.revenuesPerMarketplace = revenuesPerMarketplace;
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
        for (Service service : getService()) {
            service.calculate();
        }
        revenuesPerMarketplace.calculate();
    }
}
