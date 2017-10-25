/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.brokershare;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "serviceRevenue" })
@XmlRootElement(name = "Service")
public class Service {

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

    public ServiceRevenue getServiceRevenue() {
        return serviceRevenue;
    }

    public void setServiceRevenue(ServiceRevenue value) {
        this.serviceRevenue = value;
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
}
