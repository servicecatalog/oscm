/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 23.07.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import javax.ejb.Local;

import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.BillingResult;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Calculates the revenue for subscription based costs. Revenue shares for other
 * organizations like reseller are calculated at a later step.
 * 
 * @author cheld
 * 
 */
@Local
public interface RevenueCalculatorLocal {

    /**
     * Perform billing run for subscription. For each run a separate transaction
     * is executed.
     */
    BillingResult performBillingRunForSubscription(BillingInput billingInput)
            throws ObjectNotFoundException, BillingRunFailed,
            NonUniqueBusinessKeyException;
}
