/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import org.oscm.reportingservice.business.model.RDO;

public class RDOPriceModel extends RDO {

    private static final long serialVersionUID = -3904077274887145430L;

    /** database tkey of the price model */
    private String id;

    /** since when the price model is active */
    private String startDate;

    /** until the price model is active */
    private String endDate;

    /** might be different by up/downgrade */
    private String serviceName;

    private String oneTimeFee;

    /** price model costs */
    private String costs; // total price model costs
    private String currency;
    private String netAmountBeforeDiscount;

    private RDOUserFees userFees;
    private RDOSubscriptionFees subscriptionFees;
    private RDOEventFees eventFees;

    /**
     * Default constructor initializing fields.
     */
    public RDOPriceModel() {
        oneTimeFee = ""; // to avoid null string in soap response
        userFees = new RDOUserFees();
        subscriptionFees = new RDOSubscriptionFees();
        eventFees = new RDOEventFees();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOneTimeFee() {
        return oneTimeFee;
    }

    public void setOneTimeFee(String oneTimeFee) {
        this.oneTimeFee = oneTimeFee;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCosts() {
        return costs;
    }

    public void setCosts(String costs) {
        this.costs = costs;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public RDOUserFees getUserFees() {
        return userFees;
    }

    public void setUserFees(RDOUserFees userFees) {
        this.userFees = userFees;
    }

    public RDOSubscriptionFees getSubscriptionFees() {
        return subscriptionFees;
    }

    public void setSubscriptionFees(RDOSubscriptionFees subscriptionFees) {
        this.subscriptionFees = subscriptionFees;
    }

    public RDOEventFees getEventFees() {
        return eventFees;
    }

    public void setEventFees(RDOEventFees eventFees) {
        this.eventFees = eventFees;
    }

    public String getNetAmountBeforeDiscount() {
        return netAmountBeforeDiscount;
    }

    public void setNetAmountBeforeDiscount(String netAmountBeforeDiscount) {
        this.netAmountBeforeDiscount = netAmountBeforeDiscount;
    }

}
