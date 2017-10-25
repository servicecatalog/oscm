/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.billingdataexport;

import org.oscm.internal.types.enumtypes.BillingSharesResultType;

public class PORevenueShareExport extends POBillingExport {

    private static final long serialVersionUID = 6113911296065807008L;

    BillingSharesResultType revenueShareType;

    public BillingSharesResultType getRevenueShareType() {
        return revenueShareType;
    }

    public void setRevenueShareType(
            BillingSharesResultType billingSharesResultType) {
        this.revenueShareType = billingSharesResultType;
    }

}
