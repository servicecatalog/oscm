/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 27.01.2011                                                      
 *                                                                              
 *  Completion Time: <date>                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.operator;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.DateConverter;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.validator.DateFromToValidator;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;

/**
 * Backing bean for operator functionality for exporting billing data, starting
 * billing, retry of failed payment processes and start for SOP data
 * handling/billing.
 * 
 * @author weiser
 * 
 */
@ViewScoped
@ManagedBean(name="billingBean")
public class BillingBean extends BaseOperatorBean implements Serializable {

    private static final long serialVersionUID = 8433375406019219535L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(BillingBean.class);

    private Date fromDate;
    private Date toDate;
    
    @ManagedProperty(value="#{operatorSelectOrgBean}")
    private OperatorSelectOrgBean operatorSelectOrgBean;
    
    private DateFromToValidator validator = new DateFromToValidator();

    private byte[] billingData;

    public OperatorSelectOrgBean getOperatorSelectOrgBean() {
        return operatorSelectOrgBean;
    }

    public void setOperatorSelectOrgBean(
            OperatorSelectOrgBean operatorSelectOrgBean) {
        this.operatorSelectOrgBean = operatorSelectOrgBean;
    }


    public void setValidator(DateFromToValidator validator) {
        this.validator = validator;
    }

    /**
     * Executes a billing run.
     * 
     * @return the logical outcome.
     * @throws OrganizationAuthoritiesException
     */
    public String startBillingRun() throws OrganizationAuthoritiesException {

        boolean result = getOperatorService().startBillingRun();
        
        return getOutcome(result);
    }

    Log4jLogger getLogger() {
        return logger;
    }

    /**
     * Retries the failed payment processes.
     * 
     * @return the logical outcome.
     * @throws OrganizationAuthoritiesException
     */
    public String retryFailedPaymentProcesses()
            throws OrganizationAuthoritiesException {
        
        boolean result = getOperatorService().retryFailedPaymentProcesses();
        
        return getOutcome(result);
    }

    /**
     * Starts the payment processing for the not yet processed billing results
     * that must be handled by the PSP.
     * 
     * @return a generic success or error message
     * @throws OrganizationAuthoritiesException
     *             if the user calling this method belongs to an organization
     *             which has no authorities to start the payment process.
     */
    public String startPaymentProcessing()
            throws OrganizationAuthoritiesException {
        getLogger().logInfo(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.INFO_USER_PAYMENT_RUN_STARTED);
        
        boolean result = getOperatorService().startPaymentProcessing();
        getLogger().logInfo(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.INFO_USER_PAYMENT_RUN_FINISHED);
        return getOutcome(result);
    }

    /**
     * Exports the billing data for the specified start and end date and the
     * supplier id. The read data will be written to the response as type
     * text/xml.
     * 
     * @return the logical outcome.
     * @throws ObjectNotFoundException
     * @throws OrganizationAuthoritiesException
     */
    public String getBillingData() throws ObjectNotFoundException,
            OrganizationAuthoritiesException {
        
        String orgId = operatorSelectOrgBean.getOrganizationId();
        if (fromDate == null || toDate == null || orgId == null
                || orgId.trim().length() == 0) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_EXPORT_BILLING_DATA);
            logger.logError(
                    LogMessageIdentifier.ERROR_GET_BILLING_DATA_FAILED_WITH_WRONG_PARAMETER,
                    String.valueOf(fromDate), String.valueOf(toDate), orgId);
            
            return OUTCOME_ERROR;
        }

        long from = DateConverter.getBeginningOfDayInCurrentTimeZone(fromDate
                .getTime());
        long to = DateConverter.getBeginningOfNextDayInCurrentTimeZone(toDate
                .getTime());

        billingData = getOperatorService().getOrganizationBillingData(from, to,
                orgId);
        if (billingData == null) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_SHOW_BILLING_DATA);
            logger.logError(LogMessageIdentifier.ERROR_GET_ORGANIZATION_BILLING_DATA_RETURN_NULL);
            
            return OUTCOME_ERROR;
        }
        
        return OUTCOME_SUCCESS;
    }

    public String showBillingData() throws IOException {
        
        if (billingData == null) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_SHOW_BILLING_DATA);
            logger.logError(LogMessageIdentifier.ERROR_EXECUTE_SHOW_BILLING_DATA_WITH_NULL_DATA);
            
            return OUTCOME_ERROR;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        String filename = sdf.format(Calendar.getInstance().getTime())
                + "_BillingData.xml";
        String contentType = "text/xml";
        writeContentToResponse(billingData, filename, contentType);
        billingData = null;
        
        return OUTCOME_SUCCESS;
    }

    public void validateFromAndToDate(final FacesContext context,
            final UIComponent toValidate, final Object value) {
        validator.setToDate(toDate);
        validator.setFromDate(fromDate);
        try {
            validator.validate(context, toValidate, value);
        } catch (ValidatorException ex) {
            context.addMessage(
                    toValidate.getClientId(context),
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
                            .getLocalizedMessage(), null));
        }
    }

    public boolean isBillingDataButtonDisabled() {
        boolean retVal = true;
        if (fromDate != null && toDate != null) {
            retVal = fromDate.after(toDate);
        }
        return retVal;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getToDate() {
        return toDate;
    }

    /**
     * Checks if billing data has been read.
     * 
     * @return <code>true</code> if billing data is available otherwise
     *         <code>false</code>.
     */
    public boolean isBillingDataAvailable() {
        return billingData != null;
    }
}
