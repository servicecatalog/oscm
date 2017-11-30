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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.convert.ConverterException;
import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.Constants;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIComponentStub;

/**
 * JUnit tests for DiscountValueValidator class. No assert for test methods,
 * cause on error exception will be thrown.
 * 
 * @author Aleh Khomich.
 * 
 */
public class DiscountValueValidatorTest {

    private DiscountValueValidator validator;
    private FacesContextStub context;
    private UIComponentStub component;

    /**
     * Setup method.
     */
    @Before
    public void setup() {
        validator = new DiscountValueValidator();
        context = new FacesContextStub(Locale.ENGLISH);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        component = new UIComponentStub(map);
    }

    /**
     * Test for correct value.
     */
    @Test
    public void testValidate() {
        BigDecimal value = new BigDecimal("2.55");

        validator.validate(context, component, value);
    }

    /**
     * Test for correct value.
     */
    @Test
    public void testValidateMaxValue() {
        BigDecimal value = new BigDecimal("100.00");

        validator.validate(context, component, value);
    }

    /**
     * Test for not correct value.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateNegative() {
        BigDecimal value = new BigDecimal("-1");
        validator.validate(context, component, value);
    }

    /**
     * Test for not correct value.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateZero() {
        BigDecimal value = new BigDecimal("0");
        validator.validate(context, component, value);
    }

    /**
     * Test for not correct value.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateTooBig() {
        BigDecimal value = new BigDecimal("100.01");
        validator.validate(context, component, value);
    }

    /**
     * Test for not correct value.
     */
    @Test(expected = ValidatorException.class)
    public void testWrongScale() throws ConverterException {
        BigDecimal value = new BigDecimal("1.0001");
        validator.validate(context, component, value);
    }

}
