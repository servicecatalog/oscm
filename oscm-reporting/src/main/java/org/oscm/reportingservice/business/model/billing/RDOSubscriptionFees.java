/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: baumann                                                      
 *                                                                              
 *  Creation Date: 10.08.2011                                                      
 *                                                                              
 *  Completion Time: 10.08.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import java.util.ArrayList;
import java.util.List;

import org.oscm.reportingservice.business.model.RDO;

/**
 * @author kulle
 * 
 */
public class RDOSubscriptionFees extends RDO implements RDOBilling {

    private static final long serialVersionUID = 1713985298700495499L;

    private String basePeriod;

    private String calculationMode;

    private String serverTimeZone;

    /** pro rata factor */
    private String factor = "";

    /** price per period */
    private String basePrice;

    /** resulting subscription price */
    private String price;

    /** Sum of all subscription fees */
    private String subtotalAmount;

    private boolean hideRecurringCharge = false;

    private boolean hideSubscriptionFees = false;

    private List<RDOParameter> parameters = new ArrayList<RDOParameter>();

    public String getBasePeriod() {
        return basePeriod;
    }

    public void setBasePeriod(String basePeriod) {
        this.basePeriod = basePeriod;
    }

    public String getCalculationMode() {
        return calculationMode;
    }

    public void setCalculationMode(String calculationMode) {
        this.calculationMode = calculationMode;
    }

    public String getServerTimeZone() {
        return serverTimeZone;
    }

    public void setServerTimeZone(String serverTimeZone) {
        this.serverTimeZone = serverTimeZone;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(String subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public List<RDOParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<RDOParameter> parameters) {
        this.parameters = parameters;
    }

    public boolean isHideRecurringCharge() {
        return hideRecurringCharge;
    }

    public void setHideRecurringCharge(boolean hideRecurringCharge) {
        this.hideRecurringCharge = hideRecurringCharge;
    }

    public boolean isHideSubscriptionFees() {
        return hideSubscriptionFees;
    }

    public void setHideSubscriptionFees(boolean hideSubscriptionFees) {
        this.hideSubscriptionFees = hideSubscriptionFees;
    }

}
