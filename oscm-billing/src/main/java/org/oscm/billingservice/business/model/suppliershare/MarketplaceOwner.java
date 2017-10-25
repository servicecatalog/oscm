/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "organizationData" })
@XmlRootElement(name = "MarketplaceOwner")
public class MarketplaceOwner {

    @XmlElement(name = "OrganizationData", required = true)
    protected OrganizationData organizationData;

    public OrganizationData getOrganizationData() {
        return organizationData;
    }

    public void setOrganizationData(OrganizationData value) {
        this.organizationData = value;
    }
}
