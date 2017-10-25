/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle
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
public class RDOUserFees extends RDO implements RDOBilling {

    private static final long serialVersionUID = 1651286834015362888L;

    /** pro rata factor used in the final price calculation */
    private String factor = "";

    /** final price for recurring user fees */
    private String price;

    /** base price, calculated in case of stepped prices */
    private String basePrice;

    /** the period, e.g. HOUR or MONTH */
    private String basePeriod;

    /** total costs of user fees */
    private String subtotalAmount;

    /** hide, if contains zero factors and no stepped prices */
    private boolean hideRecurringCharge = false;

    /** hide, if no rows at all are present */
    private boolean hideUserFees = false;

    private String numberOfUsersTotal;

    private List<RDOSteppedPrice> steppedPrices = new ArrayList<RDOSteppedPrice>();
    private List<RDORole> roles = new ArrayList<RDORole>();
    private List<RDOParameter> parameters = new ArrayList<RDOParameter>();

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    public String getBasePeriod() {
        return basePeriod;
    }

    public void setBasePeriod(String basePeriod) {
        this.basePeriod = basePeriod;
    }

    public String getNumberOfUsersTotal() {
        return numberOfUsersTotal;
    }

    public void setNumberOfUsersTotal(String numberOfUsersTotal) {
        this.numberOfUsersTotal = numberOfUsersTotal;
    }

    public String getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(String subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public List<RDOSteppedPrice> getSteppedPrices() {
        return steppedPrices;
    }

    public void setSteppedPrices(List<RDOSteppedPrice> steppedPrices) {
        this.steppedPrices = steppedPrices;
    }

    public List<RDORole> getRoles() {
        return roles;
    }

    public void setRoles(List<RDORole> roles) {
        this.roles = roles;
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

    public void setHideRecurringCharge(boolean recurringCharge) {
        this.hideRecurringCharge = recurringCharge;
    }

    public boolean isHideUserFees() {
        return hideUserFees;
    }

    public void setHideUserFees(boolean hideUserFees) {
        this.hideUserFees = hideUserFees;
    }

    /**
     * Returns the role for the given roleId
     * 
     * @param roleId
     *            of the searched role
     * @return RDORole
     */
    public RDORole getRole(String roleId) {
        for (RDORole role : roles) {
            if (role.getRoleId().equals(roleId)) {
                return role;
            }
        }
        return null;
    }

}
