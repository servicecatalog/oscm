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
@XmlType(name = "", propOrder = { "subscription", "subscriptionsRevenue",
        "broker", "reseller", "revenueShareDetails" })
@XmlRootElement(name = "Service")
public class Service {

    @XmlElement(name = "Subscription")
    protected List<Subscription> subscription;

    @XmlElement(name = "SubscriptionsRevenue")
    protected SubscriptionsRevenue subscriptionsRevenue;

    @XmlElement(name = "Broker")
    protected Broker broker;

    @XmlElement(name = "Reseller")
    protected Reseller reseller;

    @XmlElement(name = "RevenueShareDetails", required = true)
    protected RevenueShareDetails revenueShareDetails = new RevenueShareDetails();

    @XmlAttribute(name = "id", required = true)
    protected String id;

    @XmlAttribute(name = "key", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger key;

    @XmlAttribute(name = "model", required = true)
    protected String model;

    @XmlAttribute(name = "templateKey")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger templateKey;

    public List<Subscription> getSubscription() {
        if (subscription == null) {
            subscription = new ArrayList<Subscription>();
        }
        return this.subscription;
    }

    public Subscription getSubscriptionByKey(long subscriptionKey) {
        for (Subscription subscription : getSubscription()) {
            if (subscription.getKey().longValue() == subscriptionKey) {
                return subscription;
            }
        }
        return null;
    }

    public void addSubscription(Subscription subscription) {
        getSubscription().add(subscription);
    }

    public SubscriptionsRevenue getSubscriptionsRevenue() {
        return subscriptionsRevenue;
    }

    public void setSubscriptionsRevenue(SubscriptionsRevenue value) {
        this.subscriptionsRevenue = value;
    }

    /**
     * Returns the SubscriptionsRevenue. The object will be created if it does
     * not exist yet.
     */
    public SubscriptionsRevenue retrieveSubscriptionsRevenue() {
        if (subscriptionsRevenue == null) {
            subscriptionsRevenue = new SubscriptionsRevenue();
        }
        return subscriptionsRevenue;
    }

    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker value) {
        this.broker = value;
    }

    public Reseller getReseller() {
        return reseller;
    }

    public void setReseller(Reseller value) {
        this.reseller = value;
    }

    public RevenueShareDetails getRevenueShareDetails() {
        return revenueShareDetails;
    }

    public void setRevenueShareDetails(RevenueShareDetails value) {
        this.revenueShareDetails = value;
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

    public String getModel() {
        return model;
    }

    public void setModel(String value) {
        this.model = value;
    }

    public BigInteger getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(BigInteger value) {
        this.templateKey = value;
    }

    public void calculate() {

        if (getBroker() != null) {
            revenueShareDetails.setServiceRevenue(sumupServiceRevenue());
            revenueShareDetails.calculate(Seller.BROKER);
        } else if (getReseller() != null) {
            revenueShareDetails
                    .setServiceRevenue(retrieveSubscriptionsRevenue()
                            .getAmount());
            revenueShareDetails.calculate(Seller.RESELLER);
        } else {
            revenueShareDetails.setServiceRevenue(sumupServiceRevenue());
            revenueShareDetails.calculate(Seller.SUPPLIER);
        }
    }

    private BigDecimal sumupServiceRevenue() {
        BigDecimal srvRevenue = BigDecimal.ZERO
                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
        for (Subscription s : getSubscription()) {
            srvRevenue = srvRevenue.add(s.getRevenue());
        }
        return srvRevenue;
    }
}
