/* 
 *  Copyright FUJITSU LIMITED 2017
 **
 * 
 */
package org.oscm.ui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;

/**
 * @author Administrator
 * 
 */
public class ParameterRowTest {
    private ParameterRow parameterRow;
    private VOParameter parameter;
    private VOParameterOption parameterOption;
    private VOParameterDefinition parameterDefinition;
    private List<VOParameterOption> parameterOptions;

    @Before
    public void setup() {
        parameterOption = mock(VOParameterOption.class);
        parameterDefinition = mock(VOParameterDefinition.class);
        parameterOptions = new ArrayList<VOParameterOption>();
        parameter = spy(new VOParameter());

        parameter.setParameterDefinition(parameterDefinition);

        doReturn(parameterOptions).when(parameterDefinition)
                .getParameterOptions();

        ParameterRow row = new ParameterRow(parameterDefinition,
                parameterOption);
        parameterRow = spy(row);

    }

    @Test
    public void isOneTimeParameter_onetime() {
        // given
        when(parameterDefinition.getModificationType()).thenReturn(
                ParameterModificationType.ONE_TIME);
        assertTrue(parameterRow.isOneTimeParameter());
    }

    @Test
    public void isOneTimeParameter_standard() {
        // given
        when(parameterDefinition.getModificationType()).thenReturn(
                ParameterModificationType.STANDARD);
        assertFalse(parameterRow.isOneTimeParameter());
    }

    @Test
    public void isNonConfigurableOneTimeParameter() {
        // given
        when(parameterDefinition.getModificationType()).thenReturn(
                ParameterModificationType.ONE_TIME);
        when(parameterRow.getParameter()).thenReturn(parameter);
        parameter.setConfigurable(false);

        // when
        boolean result = parameterRow.isNonConfigurableOneTimeParameter();

        // then
        assertTrue(result);
        verify(parameterRow.getParameter()).isConfigurable();
        verify(parameterDefinition).getModificationType();
    }

    @Test
    public void getEnumerateOption_notENUMERATION() {

        doReturn(ParameterValueType.STRING).when(parameterDefinition)
                .getValueType();
        assertNull(parameterRow.getSelectedEnumerateOption());
    }

    @Test
    public void getEnumerateOption() {
        doReturn(ParameterValueType.ENUMERATION).when(parameterDefinition)
                .getValueType();

        for (int i = 0; i < 3; i++) {
            parameterOptions.add(new VOParameterOption("option" + i,
                    "desc" + i, "def" + i));
        }

        doReturn("option2").when(parameter).getValue();
        parameterRow = new ParameterRow(parameter, parameterOption, true);
        VOParameterOption option = parameterRow.getSelectedEnumerateOption();
        assertNotNull(option);
        assertEquals("option2", option.getOptionId());
        assertEquals("desc2", option.getOptionDescription());
        assertEquals("def2", option.getParamDefId());
    }
    
    @Test
    public void isPasswordTypeParameter() {
        // given
        when(parameterDefinition.getParameterId()).thenReturn("WINDOWS_PWD");

        // when
        boolean passwordType = parameterRow.isPasswordType();

        // then
        assertTrue(passwordType);
    }

}
