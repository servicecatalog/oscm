/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import java.util.ArrayList;
import java.util.List;

import org.oscm.reportingservice.business.model.RDO;

public class RDOEvent extends RDO {

    private static final long serialVersionUID = -3672653180802180556L;

    private String id;
    private String basePrice;
    private String numberOfOccurences;
    private String price;
    private List<RDOSteppedPrice> steppedPrices = new ArrayList<RDOSteppedPrice>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    public String getNumberOfOccurences() {
        return numberOfOccurences;
    }

    public void setNumberOfOccurences(String value) {
        this.numberOfOccurences = value;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public List<RDOSteppedPrice> getSteppedPrices() {
        return steppedPrices;
    }

    public void setSteppedPrices(List<RDOSteppedPrice> steppedPrices) {
        this.steppedPrices = steppedPrices;
    }

}
