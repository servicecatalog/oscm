/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 23.07.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.TransactionAttributeType;

import org.oscm.billingservice.service.model.BillingRun;
import org.oscm.domobjects.BillingResult;
import org.oscm.types.exceptions.BillingRunFailed;

/**
 * 
 * Local interface for the billing service component.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Local
public interface BillingServiceLocal {

    /**
     * Gathers the billing information for the last month and stores them in the
     * database.
     * 
     * @param currentTime
     *            The time the billing job was triggered.
     * @return <code>true</code> in case the billing run passed without any
     *         problems (including psp invocation), <code>false</code>
     *         otherwise.
     */
    public boolean startBillingRun(long currentTime);

    /**
     * Gathers the billing information for the organization in the given
     * timeframe. The functionality is provided as separate method to allow
     * running in a separate transaction.
     * 
     * <p>
     * <b>NOTE:</b> The transaction modifier for this method is
     * {@link TransactionAttributeType#REQUIRES_NEW}
     * </p>
     * 
     * @param startOfPeriod
     *            The start date of the period in milliseconds.
     * @param endOfPeriod
     *            The end date of the period in milliseconds.
     * @param organizationKey
     *            The technical key of the organization.
     * @return The billing result objects generated for the organization, one
     *         for every currency. The list will be empty in case the parameter
     *         storeResultXML is set to <code>false</code> and no costs were
     *         caused for the period. <code>null</code> will be returned in case
     *         an exception occurred at runtime.
     * @throws BillingRunFailed
     *             Thrown in case the billing run failed because of the lacking
     *             of history data
     */
    public List<BillingResult> generateBillingForAnyPeriod(long startOfPeriod,
            long endOfPeriod, long organizationKey) throws BillingRunFailed;

    /**
     * Calculates the billing results for a customer payment preview from
     * current date time until the last invoiced subscriptions.
     * 
     * @param organizationKey
     *            - the key of organization (customer)
     * @return a billing run, containing list of billing results, representing
     *         the not yet invoiced subscriptions as well as the period the
     *         billing results are for.
     * @throws BillingRunFailed
     *             Thrown in case the billing run failed because of the lacking
     *             of history data
     */
    public BillingRun generatePaymentPreviewReport(long organizationKey)
            throws BillingRunFailed;

    /**
     * Calculates the billing results for a customer payment preview from
     * current date time until the last invoiced subscriptions. Only the billing
     * results for subscriptions which belong to the specified units are
     * calculated.
     * 
     * @param organizationKey
     *            - the key of organization (customer)
     * @param unlitKeys
     *            - the keys of organizational units the calling user is allowed
     *            to administrate
     * @return a billing run, containing list of billing results, representing
     *         the not yet invoiced subscriptions as well as the period the
     *         billing results are for.
     * @throws BillingRunFailed
     *             Thrown in case the billing run failed because of the lacking
     *             of history data
     */
    public BillingRun generatePaymentPreviewReport(long organizationKey,
            List<Long> unitKeys) throws BillingRunFailed;
}
