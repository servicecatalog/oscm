/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                                      
 *                                                                              
 *  Creation Date: 04.08.2011                                                      
 *                                                                              
 *  Completion Time: 04.08.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import java.util.ArrayList;
import java.util.List;

import org.oscm.reportingservice.business.model.RDO;

/**
 * Report Data Object for the customer payment preview report.
 * 
 * @author kulle
 */
public class RDOCustomerPaymentPreview extends RDO {

    private static final long serialVersionUID = 6894884586788159641L;

    private String startDate;
    private String endDate;
    private List<RDOPaymentPreviewSummary> summaries;
    private List<RDOSubscription> subscriptions;

    /**
     * Default constructor.
     */
    public RDOCustomerPaymentPreview() {
        startDate = "";
        endDate = "";
        summaries = new ArrayList<>();
        subscriptions = new ArrayList<>();
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<RDOPaymentPreviewSummary> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<RDOPaymentPreviewSummary> summaries) {
        this.summaries = summaries;
    }

    public List<RDOSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<RDOSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

}
