/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import java.util.ArrayList;
import java.util.List;

import org.oscm.reportingservice.business.model.RDO;

/**
 * Report data object for priced parameters.
 * 
 * @author kulle
 */
public class RDOParameter extends RDO implements RDOBilling {

    private static final long serialVersionUID = 5469265373076309013L;

    /** the parameter's name, e.g. DISK_SPACE */
    private String id;

    /** the parameter's value, e.g. 2 */
    private String value;

    /**
     * value of number based parameter types like integer or long, equal to the
     * value field. not set for other types like enumeration based parameters
     */
    private String valueFactor;

    /** pro rata factor used to calculate the final parameter price */
    private String factor = "";

    /**
     * calculated! base price for the parameter
     */
    private String basePrice;

    /** final parameter price (price=factor*basePrice*valueFactor) */
    private String price;

    /** list of stepped prices */
    private List<RDOSteppedPrice> steppedPrices;

    /** list of parameter options */
    private List<RDOOption> options;

    /**
     * Default constructor initializing fields.
     * <p>
     * In some cases only parameter options are displayed in the report. In such
     * cases all the fields must be initializing properly to avoid null values
     * and resulting conflicts from such values.
     */
    public RDOParameter() {
        id = "";
        value = "";
        valueFactor = "";
        basePrice = "";
        price = "";
        steppedPrices = new ArrayList<RDOSteppedPrice>();
        options = new ArrayList<RDOOption>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueFactor() {
        return valueFactor;
    }

    public void setValueFactor(String valueFactor) {
        this.valueFactor = valueFactor;
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

    public List<RDOSteppedPrice> getSteppedPrices() {
        return steppedPrices;
    }

    public void setSteppedPrices(List<RDOSteppedPrice> steppedPrices) {
        this.steppedPrices = steppedPrices;
    }

    public List<RDOOption> getOptions() {
        return options;
    }

    public void setOptions(List<RDOOption> options) {
        this.options = options;
    }

}
