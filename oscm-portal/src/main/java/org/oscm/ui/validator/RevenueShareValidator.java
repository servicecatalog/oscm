/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 09.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import java.math.BigDecimal;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;

/**
 * Revenue share validator for UI.
 * 
 * @author tokoda
 * 
 */
public class RevenueShareValidator implements Validator {

    public static final int MAXIMUM_FRACTION_DIGIT = 2;

    /**
     * Validate revenue share value.
     */
    public void validate(FacesContext facesContext, UIComponent uiComponent,
            Object value) throws ValidatorException {

        if (value == null) {
            return;
        }

        BigDecimal valueBigDecimal = (BigDecimal) value;
        BigDecimal maxValue = new BigDecimal("100.00");
        BigDecimal minValue = new BigDecimal("0");

        // not allowed more then 2 decimal digits
        if (valueBigDecimal.scale() > MAXIMUM_FRACTION_DIGIT) {
            FacesMessage facesMessage = JSFUtils.getFacesMessage(uiComponent,
                    facesContext,
                    BaseBean.ERROR_REVENUESHARE_INVALID_FRACTIONAL_PART);
            throw new ValidatorException(facesMessage);
        }

        // test > 100%
        if (valueBigDecimal.compareTo(maxValue) == 1) {
            FacesMessage facesMessage = JSFUtils.getFacesMessage(uiComponent,
                    facesContext, BaseBean.ERROR_REVENUESHARE_VALUE);
            throw new ValidatorException(facesMessage);
        }
        // test negative values
        if (valueBigDecimal.compareTo(minValue) == -1) {
            FacesMessage facesMessage = JSFUtils.getFacesMessage(uiComponent,
                    facesContext, BaseBean.ERROR_REVENUESHARE_VALUE);
            throw new ValidatorException(facesMessage);
        }
    }
}
