/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                         
 *                                                                              
 *  Creation Date: 26.05.2010                                                      
 *                                                                              
 *  Completion Time: 26.05.2010                                    
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
 * Discount value validator for UI.
 * 
 * @author Aleh Khomich.
 * 
 */
public class DiscountValueValidator implements Validator {

    public static final int MAXIMUM_FRACTION_DIGIT = 2;

    /**
     * Validate discount value.
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
            // this is a case, when there are more then two decimal digits -
            // it is not allowed, only two are allowed
            // this is a case, when there are more then two decimal digits -
            // it is not allowed, only two are allowed
            FacesMessage facesMessage = JSFUtils.getFacesMessage(uiComponent,
                    facesContext,
                    BaseBean.ERROR_DISCOUNT_INVALID_FRACTIONAL_PART);
            throw new ValidatorException(facesMessage);
        }

        // test > 100%
        if (valueBigDecimal.compareTo(maxValue) == 1) {
            FacesMessage facesMessage = JSFUtils.getFacesMessage(uiComponent,
                    facesContext, BaseBean.ERROR_DISCOUNT_VALUE);
            throw new ValidatorException(facesMessage);
        }
        // test 0 and negative values
        if (valueBigDecimal.compareTo(minValue) == -1
                || valueBigDecimal.compareTo(minValue) == 0) {
            FacesMessage facesMessage = JSFUtils.getFacesMessage(uiComponent,
                    facesContext, BaseBean.ERROR_DISCOUNT_VALUE);
            throw new ValidatorException(facesMessage);
        }
    }
}
