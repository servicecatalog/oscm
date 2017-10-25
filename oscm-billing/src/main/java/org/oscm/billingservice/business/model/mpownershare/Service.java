/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.mpownershare;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "supplier", "broker", "reseller",
        "revenueShareDetails" })
@XmlRootElement(name = "Service")
public class Service {

    @XmlElement(name = "Supplier", required = true)
    protected Supplier supplier;

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
    protected OfferingType model;

    @XmlAttribute(name = "templateKey")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger templateKey;

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier value) {
        this.supplier = value;
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

    public OfferingType getModel() {
        return model;
    }

    public void setModel(OfferingType value) {
        this.model = value;
    }

    public BigInteger getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(BigInteger value) {
        this.templateKey = value;
    }

    public void calculate() {
        revenueShareDetails.calculate(model);
    }
}
