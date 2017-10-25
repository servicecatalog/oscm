/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 26.01.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

import org.oscm.types.enumtypes.PaymentProcessingStatus;

/**
 * Domain object representing the result data on the payment processing for a
 * certain billing result object.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@NamedQuery(name = "PaymentResult.getAllByStatus", query = "SELECT pr FROM PaymentResult pr WHERE pr.dataContainer.processingStatus = :status")
public class PaymentResult extends
        DomainObjectWithVersioning<PaymentResultData> {

    private static final long serialVersionUID = 478112019440984839L;

    public PaymentResult() {
        super();
        dataContainer = new PaymentResultData();
    }

    /**
     * The billing result object the payment operation refered to.
     */
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private BillingResult billingResult;

    public String getProcessingResult() {
        return dataContainer.getProcessingResult();
    }

    public long getProcessingTime() {
        return dataContainer.getProcessingTime();
    }

    public String getProcessingException() {
        return dataContainer.getProcessingException();
    }

    public PaymentProcessingStatus getProcessingStatus() {
        return dataContainer.getProcessingStatus();
    }

    public void setProcessingResult(String processingResult) {
        dataContainer.setProcessingResult(processingResult);
    }

    public void setProcessingTime(long processingTime) {
        dataContainer.setProcessingTime(processingTime);
    }

    public void setProcessingException(String processingException) {
        dataContainer.setProcessingException(processingException);
    }

    public void setProcessingStatus(PaymentProcessingStatus processingStatus) {
        dataContainer.setProcessingStatus(processingStatus);
    }

    public BillingResult getBillingResult() {
        return billingResult;
    }

    public void setBillingResult(BillingResult billingResult) {
        this.billingResult = billingResult;
    }

    /**
     * Writes the stack trace of the given throwable to the exception field.
     * 
     * @param e
     *            The throwable to record the stack trace of.
     */
    public void setProcessingException(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        dataContainer.setProcessingException(sw.toString());
    }
}
