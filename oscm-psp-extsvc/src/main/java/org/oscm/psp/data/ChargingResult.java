/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-10-06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.psp.data;

import java.io.Serializable;

/**
 * Provides the result for a charging request to a payment service provider
 * (PSP).
 * 
 */
public class ChargingResult implements Serializable {

    private static final long serialVersionUID = 7713973721314956356L;

    /**
     * The PSP response to the charging request.
     */
    private String processingResult;

    /**
     * The time the charging request was processed.
     */
    private long processingTime;

    /**
     * The stack of exceptions that occurred in the processing.
     */
    private String processingException;

    /**
     * The status of the processing.
     */
    private PaymentProcessingStatus processingStatus;

    /**
     * Returns the PSP's response to the charging request.
     * 
     * @return the result
     */
    public String getProcessingResult() {
        return processingResult;
    }

    /**
     * Sets the PSP's response to the charging request.
     * 
     * @param processingResult
     *            the result
     */
    public void setProcessingResult(String processingResult) {
        this.processingResult = processingResult;
    }

    /**
     * Returns the time the charging request was processed at the PSP.
     * 
     * @return the processing time
     */
    public long getProcessingTime() {
        return processingTime;
    }

    /**
     * Sets the time the charging request was processed at the PSP.
     * 
     * @param processingTime
     *            the processing time
     */
    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * Returns the stack of exceptions that occurred in the processing of the
     * charging request.
     * 
     * @return the exception stack
     */
    public String getProcessingException() {
        return processingException;
    }

    /**
     * Sets the stack of exceptions that occurred in the processing of the
     * charging request.
     * 
     * @param processingException
     *            the exception stack
     */
    public void setProcessingException(String processingException) {
        this.processingException = processingException;
    }

    /**
     * Returns the status of the request processing.
     * 
     * @return the status
     */
    public PaymentProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    /**
     * Sets the status of the request processing.
     * 
     * @param processingStatus
     *            the status
     */
    public void setProcessingStatus(PaymentProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

}
