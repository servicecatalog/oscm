/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 23.03.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import java.util.List;

import org.oscm.reportingservice.business.model.RDO;

/**
 * Data container for report related representation of billing results.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class RDODetailedBilling extends RDO {

    private static final long serialVersionUID = -4018015402650960456L;

    private List<RDOSummary> summaries;
    private List<RDOSubscription> subscriptions;

    public List<RDOSummary> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<RDOSummary> summaries) {
        this.summaries = summaries;
    }

    public List<RDOSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<RDOSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
