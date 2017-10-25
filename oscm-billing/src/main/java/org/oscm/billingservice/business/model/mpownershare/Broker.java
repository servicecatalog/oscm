/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.mpownershare;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "organizationData" })
@XmlRootElement(name = "Broker")
public class Broker {

    @XmlElement(name = "OrganizationData", required = true)
    protected OrganizationData organizationData;

    public OrganizationData getOrganizationData() {
        return organizationData;
    }

    public void setOrganizationData(OrganizationData value) {
        this.organizationData = value;
    }
}
