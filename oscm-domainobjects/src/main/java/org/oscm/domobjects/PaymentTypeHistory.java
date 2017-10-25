/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 12.10.2011                                                      
 *                                                                              
 *  Completion Time: 12.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

/**
 * History object for the payment type.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@NamedQuery(name = "PaymentTypeHistory.findByObject", query = "SELECT c FROM PaymentTypeHistory c WHERE c.objKey=:objKey ORDER BY objversion")
public class PaymentTypeHistory extends DomainHistoryObject<PaymentTypeData> {

    private static final long serialVersionUID = -2212509608168111043L;

    @Column(nullable = false)
    private long pspObjKey;

    public PaymentTypeHistory() {
        dataContainer = new PaymentTypeData();
    }

    /**
     * Constructs PSPSettingHistory from a PSPSetting domain object
     * 
     * @param c
     *            - the payment type
     */
    public PaymentTypeHistory(PaymentType c) {
        super(c);
        if (c.getPsp() != null) {
            setPspObjKey(c.getPsp().getKey());
        }
    }

    public void setPspObjKey(long pspObjKey) {
        this.pspObjKey = pspObjKey;
    }

    public long getPspObjKey() {
        return pspObjKey;
    }

}
