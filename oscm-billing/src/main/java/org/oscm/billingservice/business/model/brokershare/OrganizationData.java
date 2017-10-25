/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.brokershare;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrganizationData", propOrder = { "email", "name", "address",
        "countryIsoCode" })
public class OrganizationData {

    @XmlElement(name = "Email", required = true)
    protected String email;

    @XmlElement(name = "Name", required = true)
    protected String name;

    @XmlElement(name = "Address", required = true)
    protected String address;

    @XmlElement(name = "CountryIsoCode")
    protected String countryIsoCode;

    @XmlAttribute(name = "id", required = true)
    protected String id;

    @XmlAttribute(name = "key", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger key;

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        this.address = value;
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

    public String getCountryIsoCode() {
        return countryIsoCode;
    }

    public void setCountryIsoCode(String countryIsoCode) {
        this.countryIsoCode = countryIsoCode;
    }
}
