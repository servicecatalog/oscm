/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import javax.ejb.Local;

/**
 * local interface for invocation of revenue shares calculation methods
 */
@Local
public interface SharesCalculatorLocal {

    /**
     * Calculates all broker revenue shares for the given period and persist
     * them as BillingSharesResults.
     */
    public boolean performBrokerSharesCalculationRun(long startDate,
            long endDate);

    public void performBrokerShareCalculationRun(long startOfLastMonth,
            long endOfLastMonth, Long brokerKey) throws Exception;

    public boolean performMarketplacesSharesCalculationRun(long startDate,
            long endDate);

    public void performMpOwnerSharesCalculationRun(long startOfLastMonth,
            long endOfLastMonth, Long mpOwnerKey) throws Exception;

    public boolean performResellerSharesCalculationRun(long startDate,
            long endDate);

    public void performResellerSharesCalculationRun(long startOfLastMonth,
            long endOfLastMonth, Long resellerKey) throws Exception;

    public boolean performSupplierSharesCalculationRun(long startDate,
            long endDate);

    public void performSupplierSharesCalculationRun(long startOfLastMonth,
            long endOfLastMonth, Long supplierKey) throws Exception;

}
