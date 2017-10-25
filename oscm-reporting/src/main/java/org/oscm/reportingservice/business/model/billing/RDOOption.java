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

import org.oscm.reportingservice.business.model.RDO;

/**
 * Report data object for priced options. Priced options for parameters can
 * occur for user fees and period fees.
 * 
 * @author kulle
 */
public class RDOOption extends RDO implements RDOBilling {

    private static final long serialVersionUID = 516674129785133278L;

    /** final option price */
    private String price;

    /** base price used to calculate the final option price */
    private String basePrice;

    /** (pro rata) factor used in the calculation of the final option price */
    private String factor = "";

    /** the option value, i.e. name */
    private String value;

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

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
