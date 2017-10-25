/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.resellershare;

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
@XmlType(name = "", propOrder = { "subscription", "serviceRevenue" })
@XmlRootElement(name = "Service")
public class Service {

    @XmlElement(name = "Subscription", required = true)
    protected List<Subscription> subscription;

    @XmlElement(name = "ServiceRevenue", required = true)
    protected ServiceRevenue serviceRevenue = new ServiceRevenue();

    @XmlAttribute(name = "id", required = true)
    protected String id;

    @XmlAttribute(name = "key", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger key;

    @XmlAttribute(name = "templateKey", required = true)
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

    public ServiceRevenue getServiceRevenue() {
        return serviceRevenue;
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

    public BigInteger getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(BigInteger value) {
        this.templateKey = value;
    }

    public void calculate() {
        BigDecimal totalSubscriptionRevenue = BigDecimal.ZERO;
        for (Subscription subscription : getSubscription()) {
            BigDecimal subscriptionRevenue = subscription.getRevenue();
            subscriptionRevenue = subscriptionRevenue.setScale(
                    PriceConverter.NORMALIZED_PRICE_SCALING,
                    PriceConverter.ROUNDING_MODE);

            totalSubscriptionRevenue = totalSubscriptionRevenue
                    .add(subscriptionRevenue);
        }
        serviceRevenue.setTotalAmount(totalSubscriptionRevenue);
        serviceRevenue.calculate();
    }
}
