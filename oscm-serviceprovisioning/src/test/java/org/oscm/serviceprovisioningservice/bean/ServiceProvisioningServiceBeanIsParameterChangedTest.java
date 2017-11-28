/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: zhaohang                                                     
 *                                                                              
 *  Creation Date: 03.06.2013                                                      
 *                                                                              
 *  Completion Time: 04.06.2013                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;

/**
 * Tests if the parameter changed
 * 
 * @author zhaohang
 * 
 */
public class ServiceProvisioningServiceBeanIsParameterChangedTest {

    private ServiceProvisioningServiceBean service;
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";

    @Before
    public void before() {
        service = spy(new ServiceProvisioningServiceBean());
    }

    @Test
    public void isDifferentFromDefaultValue_ParameterNoChanged() {
        // given
        Parameter para = givenParameter(VALUE1, VALUE1, false);

        // when
        boolean result = service.isDifferentFromDefaultValue(para);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isDifferentFromDefaultValue_OnlyValueChanged() {
        // given
        Parameter para = givenParameter(VALUE1, VALUE2, false);

        // when
        boolean result = service.isDifferentFromDefaultValue(para);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isDifferentFromDefaultValue_OnlyUserOptionChanged() {
        // given
        Parameter para = givenParameter(VALUE1, VALUE1, true);

        // when
        boolean result = service.isDifferentFromDefaultValue(para);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isDifferentFromDefaultValue_DefaultValueIsNull() {
        // given
        Parameter para = givenParameter(null, VALUE1, false);

        // when
        boolean result = service.isDifferentFromDefaultValue(para);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isDifferentFromDefaultValue_InputValueIsNull() {
        // given
        Parameter para = givenParameter(VALUE1, null, false);

        // when
        boolean result = service.isDifferentFromDefaultValue(para);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isParameterChangedWhenUpdate_ParameterNoChanged() {
        // given
        Parameter oldPara = givenParameter(VALUE1, VALUE1, false);
        Parameter newPara = givenParameter(VALUE1, VALUE1, false);

        // when
        boolean result = service.isDifferentFromExistingValue(oldPara, newPara);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isParameterChangedWhenUpdate_OnlyValueChanged() {
        // given
        Parameter oldPara = givenParameter(VALUE1, VALUE1, false);
        Parameter newPara = givenParameter(VALUE1, VALUE2, false);

        // when
        boolean result = service.isDifferentFromExistingValue(oldPara, newPara);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isParameterChangedWhenUpdate_OnlyUserOptionChanged() {
        // given
        Parameter oldPara = givenParameter(VALUE1, VALUE1, true);
        Parameter newPara = givenParameter(VALUE1, VALUE1, false);

        // when
        boolean result = service.isDifferentFromExistingValue(oldPara, newPara);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isParameterChangedWhenUpdate_OldValueIsNull() {
        // given
        Parameter oldPara = givenParameter(VALUE1, null, true);
        Parameter newPara = givenParameter(VALUE1, VALUE1, true);

        // when
        boolean result = service.isDifferentFromExistingValue(oldPara, newPara);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isParameterChangedWhenUpdate_InputValueIsNull() {
        // given
        Parameter oldPara = givenParameter(VALUE1, VALUE1, true);
        Parameter newPara = givenParameter(VALUE1, null, true);

        // when
        boolean result = service.isDifferentFromExistingValue(oldPara, newPara);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    private Parameter givenParameter(String defaultValue, String inputValue,
            boolean userOption) {
        Parameter para = new Parameter();
        ParameterDefinition definition = new ParameterDefinition();
        definition.setDefaultValue(defaultValue);
        para.setParameterDefinition(definition);
        para.setValue(inputValue);
        para.setConfigurable(userOption);
        return para;
    }

}
