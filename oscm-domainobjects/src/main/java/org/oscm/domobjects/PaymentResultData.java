/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 26.01.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.PaymentCollectionTypeConverter;
import org.oscm.domobjects.converters.PaymentProcessingStatusConverter;
import org.oscm.types.enumtypes.PaymentProcessingStatus;

/**
 * The data container for the payment result domain object.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Embeddable
public class PaymentResultData extends DomainDataContainer {

    private static final long serialVersionUID = 5231620699224252865L;

    /**
     * The PSP response for the debit operation based on the billing details of
     * this billing result object.
     */
    private String processingResult;

    /**
     * The time the payment was processed at.
     */
    @Column(nullable = false)
    private long processingTime;

    /**
     * The stack of the exception retrieved during processing.
     */
    private String processingException;

    /**
     * The current status of the payment operation.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProcessingStatus processingStatus;

    public String getProcessingResult() {
        return processingResult;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public String getProcessingException() {
        return processingException;
    }

    public PaymentProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingResult(String processingResult) {
        this.processingResult = processingResult;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public void setProcessingException(String processingException) {
        this.processingException = processingException;
    }

    public void setProcessingStatus(PaymentProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

}
