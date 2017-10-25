/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import org.oscm.reportingservice.business.model.RDO;

/**
 * Report Data Object for stepped prices.
 */
public class RDOSteppedPrice extends RDO {

    private static final long serialVersionUID = -6301171048572975214L;

    /** the step name, e.g. 'up to 2' */
    private String id;

    /** base price used to calculate the price */
    private String basePrice;

    /** final step price */
    private String price;

    /** the limit for this step */
    private String limit;

    /**
     * quantity used to calculate the final price. not mandatory but factor must
     * instead be set.
     */
    private String quantity = "";

    /**
     * factor used to calculate the final price. not mandatory but quantity must
     * instead be set.
     */
    private String factor = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String value) {
        this.factor = value;
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

}
