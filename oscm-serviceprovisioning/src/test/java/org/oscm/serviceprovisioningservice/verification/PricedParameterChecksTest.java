/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 14.10.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.verification;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOPricedParameter;

/**
 * Tests for the priced parameter verifications.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PricedParameterChecksTest {

    private ParameterDefinition numericParamDef;
    private ParameterDefinition stringParamDef;
    private VOPricedParameter voPricedParameter;

    @Before
    public void setUp() {
        numericParamDef = new ParameterDefinition();
        numericParamDef.setValueType(ParameterValueType.INTEGER);

        stringParamDef = new ParameterDefinition();
        stringParamDef.setValueType(ParameterValueType.STRING);

        voPricedParameter = new VOPricedParameter();
    }

    @Test
    public void testIsValidBaseParamPositive() throws Exception {
        Parameter param = new Parameter();
        param.setParameterDefinition(numericParamDef);
        PricedParameterChecks.isValidBaseParam(param, voPricedParameter);
    }

    @Test(expected = ValidationException.class)
    public void testIsValidBaseParamNegative() throws Exception {
        Parameter param = new Parameter();
        param.setParameterDefinition(stringParamDef);
        PricedParameterChecks.isValidBaseParam(param, voPricedParameter);
    }

    @Test(expected = ValidationException.class)
    public void testValidateParamDefSetNoneSet() throws Exception {
        PricedParameterChecks.validateParamDefSet(voPricedParameter);
    }

    @Test
    public void testValidateParamDefSet() throws Exception {
        voPricedParameter = new VOPricedParameter(new VOParameterDefinition());
        PricedParameterChecks.validateParamDefSet(voPricedParameter);
    }

}
