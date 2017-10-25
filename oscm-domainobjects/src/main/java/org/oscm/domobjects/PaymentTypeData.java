/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.PaymentCollectionTypeConverter;
import org.oscm.domobjects.converters.UserAccountStatusConverter;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;

/**
 * @author weiser
 * 
 */
@Embeddable
public class PaymentTypeData extends DomainDataContainer {

    private static final long serialVersionUID = -191548137602022633L;

    /**
     * The name of the payment type
     */
    @Column(nullable = false, unique = true)
    private String paymentTypeId;

    /**
     * The collection type for this payment type
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentCollectionType collectionType;

    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public void setCollectionType(PaymentCollectionType collectionType) {
        this.collectionType = collectionType;
    }

    public PaymentCollectionType getCollectionType() {
        return collectionType;
    }

}
