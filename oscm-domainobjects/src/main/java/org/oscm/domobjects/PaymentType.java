/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;

/**
 * @author weiser
 * 
 */
@Entity
@BusinessKey(attributes = "paymentTypeId")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "paymentTypeId" }))
@NamedQueries({
        @NamedQuery(name = "PaymentType.findByBusinessKey", query = "SELECT pt FROM PaymentType pt WHERE pt.dataContainer.paymentTypeId=:paymentTypeId"),
        @NamedQuery(name = "PaymentType.getAllExceptInvoice", query = "SELECT pt FROM PaymentType pt WHERE pt.dataContainer.paymentTypeId <> 'INVOICE'"),
        @NamedQuery(name = "PaymentType.getAll", query = "SELECT pt FROM PaymentType pt") })
public class PaymentType extends DomainObjectWithHistory<PaymentTypeData> {

    public static final String INVOICE = "INVOICE";
    public static final String DIRECT_DEBIT = "DIRECT_DEBIT";
    public static final String CREDIT_CARD = "CREDIT_CARD";

    private static final long serialVersionUID = 1970088786295929276L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays
                    .asList(LocalizedObjectTypes.PAYMENT_TYPE_NAME));

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PSP psp;

    public PaymentType() {
        super();
        dataContainer = new PaymentTypeData();
    }

    public String getPaymentTypeId() {
        return dataContainer.getPaymentTypeId();
    }

    public void setPaymentTypeId(String paymentTypeId) {
        dataContainer.setPaymentTypeId(paymentTypeId);
    }

    public PaymentCollectionType getCollectionType() {
        return dataContainer.getCollectionType();
    }

    public void setCollectionType(PaymentCollectionType collectionType) {
        dataContainer.setCollectionType(collectionType);
    }

    public void setPsp(PSP psp) {
        this.psp = psp;
    }

    public PSP getPsp() {
        return psp;
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }
}
