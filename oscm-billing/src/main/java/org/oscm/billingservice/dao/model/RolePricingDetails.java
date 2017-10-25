/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich.                                                      
 *                                                                              
 *  Creation Date: 09.07.2010.                                                      
 *                                                                              
 *  Completion Time: 09.07.2010.                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

import java.math.BigDecimal;

import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.PricedProductRoleHistory;

/**
 * Contains the role prices, role costs and object for billing report xml file.
 * a price model.
 * 
 * @author Aleh Khomich.
 * 
 */
public class RolePricingDetails {

    private final RolePricingData parent;
    private PricedProductRoleHistory pricedProductRoleHistory;
    private BigDecimal cost = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private ParameterRolePricingData xmlReportData;
    private String roleId;
    private double factor;

    public RolePricingDetails() {
        this.parent = null;
    }

    public RolePricingDetails(RolePricingData parent) {
        this.parent = parent;
        // class members like costs etc will be calculated later
        xmlReportData = new ParameterRolePricingData();
    }

    public RolePricingDetails(PricedProductRoleHistory pricedProductRoleHistory) {
        this.pricedProductRoleHistory = pricedProductRoleHistory;
        xmlReportData = new ParameterRolePricingData();
        this.parent = null;
    }

    public void setPricedProductRoleHistory(
            PricedProductRoleHistory pricedProductRoleHistory) {
        this.pricedProductRoleHistory = pricedProductRoleHistory;
    }

    public PricedProductRoleHistory getPricedProductRoleHistory() {
        return pricedProductRoleHistory;
    }

    public BigDecimal getPricePerUser() {
        BigDecimal result = null;
        if (pricedProductRoleHistory != null) {
            result = pricedProductRoleHistory.getPricePerUser();
        }
        return result;
    }

    public BigDecimal getCost() {
        if (cost == null) {
            return BigDecimal.ZERO;
        }
        return cost;
    }

    public void setXmlReportData(ParameterRolePricingData xmlReportData) {
        this.xmlReportData = xmlReportData;
    }

    public ParameterRolePricingData getXmlReportData() {
        return xmlReportData;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void addCosts(BigDecimal costs) {
        if (parent != null) {
            parent.addCosts(costs);
        }
        this.cost = this.cost.add(costs);
    }

}
