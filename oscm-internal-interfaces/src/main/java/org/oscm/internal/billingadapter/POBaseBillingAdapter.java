/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.billingadapter;

import org.oscm.internal.base.BasePO;

/**
 * Presentation object for the billing adapter with minimum info: identifier and
 * name.
 * 
 * @author stavreva
 * 
 */
public class POBaseBillingAdapter extends BasePO {

    private static final long serialVersionUID = -5404084805451715389L;

    private String billingIdentifier;
    private String name;

    public String getBillingIdentifier() {
        return billingIdentifier;
    }

    public void setBillingIdentifier(String billingIdentifier) {
        this.billingIdentifier = billingIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
