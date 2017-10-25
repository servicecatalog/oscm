/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 5, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oscm.domobjects.BillingResult;

/**
 * Contains a list of {@link BillingResult} elements and period information of
 * the billing run.
 * 
 * @author muenz
 */
public class BillingRun {

    /**
     * Status of billing run
     */
    private boolean successful;

    /**
     * Start of billing run frame in millis.
     */
    private long start;

    /**
     * End of billing run frame in millis.
     */
    private long end;

    /**
     * Billing results within the start and end datetime.
     */
    private List<BillingResult> billingResultList;

    public BillingRun(long start, long end) {
        this.successful = true;
        this.billingResultList = new ArrayList<>();
        this.start = start;
        this.end = end;
    }

    /**
     * @return the successful
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * @param successful
     *            the successful to set
     */
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    /**
     * @return the start
     */
    public long getStart() {
        return start;
    }

    /**
     * @param start
     *            the start to set
     */
    public void setStart(long start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public long getEnd() {
        return end;
    }

    /**
     * @param end
     *            the end to set
     */
    public void setEnd(long end) {
        this.end = end;
    }

    /**
     * @return the billingResults
     */
    public List<BillingResult> getBillingResultList() {
        return billingResultList;
    }

    /**
     * @param billingResult
     *            the billingResults to set
     */
    public void addBillingResult(BillingResult... billingResult) {
        this.billingResultList.addAll(Arrays.asList(billingResult));
    }

    /**
     * Clear the billing result list
     */
    public void clearBillingResults() {
        billingResultList.clear();
    }

}
