/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.resellershare;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Period")
public class Period {

    @XmlAttribute(name = "startDate")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger startDate;

    @XmlAttribute(name = "startDateIsoFormat")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDateIsoFormat;

    @XmlAttribute(name = "endDate")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger endDate;

    @XmlAttribute(name = "endDateIsoFormat")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar endDateIsoFormat;

    public BigInteger getStartDate() {
        return startDate;
    }

    public void setStartDate(BigInteger value) {
        this.startDate = value;
    }

    public XMLGregorianCalendar getStartDateIsoFormat() {
        return startDateIsoFormat;
    }

    public void setStartDateIsoFormat(XMLGregorianCalendar value) {
        this.startDateIsoFormat = value;
    }

    public BigInteger getEndDate() {
        return endDate;
    }

    public void setEndDate(BigInteger value) {
        this.endDate = value;
    }

    public XMLGregorianCalendar getEndDateIsoFormat() {
        return endDateIsoFormat;
    }

    public void setEndDateIsoFormat(XMLGregorianCalendar value) {
        this.endDateIsoFormat = value;
    }
}
