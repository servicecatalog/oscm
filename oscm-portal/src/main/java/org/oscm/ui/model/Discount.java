/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import java.io.Serializable;

import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.converter.DiscountDateConverter;
import org.oscm.internal.vo.VODiscount;

/**
 * Wrapper Class for <code>VODiscount</code>
 * 
 * @author tokoda
 * 
 */
public class Discount implements Serializable {

    private static final long serialVersionUID = 1L;
    private VODiscount voDiscount;

    public Discount(VODiscount voDiscount) {
        this.voDiscount = voDiscount;
    }

    public VODiscount getVO() {
        return voDiscount;
    }

    /**
     * Returns the localized discount information
     * 
     * @return the localized discount information
     */
    public String getDiscountToDisplay() {
        if (voDiscount == null || voDiscount.getValue() == null
                || voDiscount.getStartTime() == null) {
            return null;
        }

        String discountLabel = JSFUtils.getText(
                "priceModel.text.discountLabel", null);

        return JSFUtils.getText("priceModel.text.discountInformation",
                new Object[] { discountLabel, getValueToDisplay(),
                        getStartTimeToDisplay(), getEndTimeToDisplay() });

    }

    private String getValueToDisplay() {
        if (voDiscount == null || voDiscount.getValue() == null) {
            return "";
        }
        return JSFUtils.getText("priceModel.text.discountValue",
                new Object[] { voDiscount.getValue() });
    }

    private String getStartTimeToDisplay() {
        if (voDiscount == null || voDiscount.getStartTime() == null) {
            return "";
        }
        return JSFUtils.getText("priceModel.text.discountStart",
                new Object[] { DiscountDateConverter
                        .convertToDateFormat(voDiscount.getStartTime()) });
    }

    private String getEndTimeToDisplay() {
        if (voDiscount == null || voDiscount.getEndTime() == null) {
            return "";
        }
        return JSFUtils.getText("priceModel.text.discountEnd",
                new Object[] { DiscountDateConverter
                        .convertToDateFormat(voDiscount.getEndTime()) });
    }
}
