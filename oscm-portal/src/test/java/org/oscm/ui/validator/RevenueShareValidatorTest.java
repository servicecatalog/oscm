/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 09.08.2012                                                      
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
 * Tests for RevenueShareValidator class.
 * 
 * @author tokoda
 * 
 */
public class RevenueShareValidatorTest {

    private RevenueShareValidator validator;
    private FacesContextStub context;
    private UIComponentStub component;

    @Before
    public void setup() {
        validator = new RevenueShareValidator();
        context = new FacesContextStub(Locale.ENGLISH);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        component = new UIComponentStub(map);
    }
    
    @Test
    public void validate_NullValue() {
        // given
        // when
        validator.validate(context, component, null);
        // then no error happen
    }

    @Test(expected = ValidatorException.class)
    public void validate_WrongScale() throws ConverterException {
        // given
        BigDecimal value = new BigDecimal("1.0001");
        // when
        validator.validate(context, component, value);
        // then no ValidatorException happen
    }

    @Test(expected = ValidatorException.class)
    public void validate_NegativeValue() {
        // given
        BigDecimal value = new BigDecimal("-0.01");
        // when
        validator.validate(context, component, value);
        // then no ValidatorException happen
    }

    @Test(expected = ValidatorException.class)
    public void validate_OverMaxValue() {
        // given
        BigDecimal value = new BigDecimal("100.01");
        // when
        validator.validate(context, component, value);
        // then no ValidatorException happen
    }

    @Test
    public void validate_CorrectValue() {
        // given
        BigDecimal value = new BigDecimal("2.55");
        // when
        validator.validate(context, component, value);
        // then no error happen
    }

    @Test
    public void validate_ZeroValue() {
        // given
        BigDecimal value = new BigDecimal("0");
        // when
        validator.validate(context, component, value);
        // then no error happen
    }

    @Test
    public void validate_MaxValue() {
        // given
        BigDecimal value = new BigDecimal("100.00");
        // when
        validator.validate(context, component, value);
        // then no error happen
    }
}
