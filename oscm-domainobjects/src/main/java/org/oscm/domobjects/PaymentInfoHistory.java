/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * History object for PaymentInfos
 * 
 * @author schmid
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "PaymentInfoHistory.findPSPAccount", query = "SELECT pah FROM PSPAccountHistory pah WHERE pah.pspObjKey = :pspKey AND pah.organizationObjKey = :organizationKey AND pah.objVersion = (SELECT MAX(ipah.objVersion) FROM PSPAccountHistory ipah WHERE pah.objKey = ipah.objKey AND ipah.pspObjKey IS NOT NULL)"),
        @NamedQuery(name = "PaymentInfoHistory.findByObject", query = "SELECT c FROM PaymentInfoHistory c WHERE c.objKey=:objKey ORDER BY objVersion"),
        @NamedQuery(name = "PaymentInfoHistory.findForSubscriptionKeyDescVersion", query = "SELECT"
                + " ph, pth, psph, prodh "
                + "FROM"
                + " PaymentInfoHistory ph, SubscriptionHistory sh, PaymentTypeHistory pth, ProductHistory prodh, PSPHistory psph "
                + "WHERE"
                + " ph.objKey = sh.paymentInfoObjKey"
                + " AND sh.objKey = :subscriptionKey"
                + " AND ph.paymentTypeObjKey = pth.objKey"
                + " AND pth.pspObjKey = psph.objKey"
                + " AND prodh.objKey = sh.productObjKey"
                + " AND sh.objVersion = (SELECT MAX(ish.objVersion) FROM SubscriptionHistory ish WHERE sh.objKey = ish.objKey AND ish.paymentInfoObjKey IS NOT NULL)"
                + " AND ph.objVersion = (SELECT MAX(iph.objVersion) FROM PaymentInfoHistory iph WHERE ph.objKey = iph.objKey AND iph.paymentTypeObjKey IS NOT NULL)"
                + " AND pth.objVersion = (SELECT MAX(ipth.objVersion) FROM PaymentTypeHistory ipth WHERE pth.objKey = ipth.objKey AND ipth.pspObjKey IS NOT NULL)"
                + " AND psph.objVersion = (SELECT MAX(ipsph.objVersion) FROM PSPHistory ipsph WHERE psph.objKey = ipsph.objKey)"
                + " AND prodh.objVersion = (SELECT MAX(iprodh.objVersion) FROM ProductHistory iprodh WHERE prodh.objKey = iprodh.objKey AND iprodh.vendorObjKey IS NOT NULL)"
                + "ORDER BY" + " ph.objVersion DESC, ph.modDate DESC") })
public class PaymentInfoHistory extends DomainHistoryObject<PaymentInfoData> {

    private static final long serialVersionUID = -4456481503791516536L;

    /**
     * Reference to the payment type.
     */
    private long paymentTypeObjKey;

    /**
     * Reference to the payment type.
     */
    private long organizationObjKey;

    public PaymentInfoHistory() {
        dataContainer = new PaymentInfoData();
    }

    public PaymentInfoHistory(PaymentInfo domObj) {
        super(domObj);
        if (domObj.getPaymentType() != null) {
            setPaymentTypeObjKey(domObj.getPaymentType().getKey());
        }
        if (domObj.getOrganization() != null) {
            setOrganizationObjKey(domObj.getOrganization().getKey());
        }
    }

    public String getExternalIdentifier() {
        return dataContainer.getExternalIdentifier();
    }

    public long getPaymentTypeObjKey() {
        return paymentTypeObjKey;
    }

    public void setPaymentTypeObjKey(long paymentTypeObjKey) {
        this.paymentTypeObjKey = paymentTypeObjKey;
    }

    public void setOrganizationObjKey(long organizationObjKey) {
        this.organizationObjKey = organizationObjKey;
    }

    public long getOrganizationObjKey() {
        return organizationObjKey;
    }

    public String getPaymentInfoId() {
        return dataContainer.getPaymentInfoId();
    }

    public String getProviderName() {
        return dataContainer.getProviderName();
    }

    public String getAccountNumber() {
        return dataContainer.getAccountNumber();
    }

}
