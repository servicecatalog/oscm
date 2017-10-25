/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.oscm.billingservice.business.BigDecimalAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "SubscriptionsRevenue")
public class SubscriptionsRevenue {

    @XmlAttribute(name = "amount", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal amount = BigDecimal.ZERO;

    public BigDecimal getAmount() {
        return amount;
    }

    public void sumUp(BigDecimal valueToAdd) {
        amount = amount.add(valueToAdd);
    }
}
