/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 10.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import static org.oscm.test.Numbers.L2000;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameterDefinition;

public class ParameterDefinitionAssemblerTest {

    private LocalizerFacade facade;

    @Before
    public void setUp() throws Exception {
        LocalizerServiceStub localizer = new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "";
            }

            @Override
            public String getLocalizedTextFromBundle(
                    LocalizedObjectTypes objectType, Marketplace shop,
                    String localeString, String key) {
                return "";
            }
        };
        facade = new LocalizerFacade(localizer, "en");
    }

    @Test
    public void testToVOParameterDefinitionNullInput() throws Exception {
        ParameterDefinitionAssembler.toVOParameterDefinition(null, facade);
    }

    @Test
    public void testToVOParameterDefinition() throws Exception {
        ParameterDefinition pd = initParameterDefinition();

        VOParameterDefinition voParameterDefinition = ParameterDefinitionAssembler
                .toVOParameterDefinition(pd, facade);

        validateParameterDefinition(voParameterDefinition);
    }

    @Test
    public void testToVOParameterDefinitionsNullInput() throws Exception {
        List<VOParameterDefinition> result = ParameterDefinitionAssembler
                .toVOParameterDefinitions(null, null, false, facade);

        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testToVOParameterDefinitionsNoPlatformParamDefs()
            throws Exception {
        List<VOParameterDefinition> result = ParameterDefinitionAssembler
                .toVOParameterDefinitions(null,
                        Collections.singletonList(initParameterDefinition()),
                        false, facade);

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        VOParameterDefinition voParameterDefinition = result.get(0);
        validateParameterDefinition(voParameterDefinition);
    }

    @Test
    public void testToVOParameterDefinitions() throws Exception {
        List<VOParameterDefinition> result = ParameterDefinitionAssembler
                .toVOParameterDefinitions(
                        Collections.singletonList(initParameterDefinition()),
                        Collections.singletonList(initParameterDefinition()),
                        false, facade);

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        VOParameterDefinition voParameterDefinition = result.get(0);
        validateParameterDefinition(voParameterDefinition);
        voParameterDefinition = result.get(1);
        validateParameterDefinition(voParameterDefinition);
    }

    /**
     * Validates the value object parameter definition to match the expected
     * values.
     * 
     * @param voParameterDefinition
     *            The value object to validate.
     */
    private void validateParameterDefinition(
            VOParameterDefinition voParameterDefinition) {
        Assert.assertTrue(voParameterDefinition.isConfigurable());
        Assert.assertEquals("bla", voParameterDefinition.getDefaultValue());
        Assert.assertEquals(123, voParameterDefinition.getKey());
        Assert.assertTrue(voParameterDefinition.isMandatory());
        Assert.assertEquals(L2000, voParameterDefinition.getMaxValue());
        Assert.assertNull(voParameterDefinition.getMinValue());
        Assert.assertEquals("diskSpace", voParameterDefinition.getParameterId());
        Assert.assertEquals(ParameterType.SERVICE_PARAMETER,
                voParameterDefinition.getParameterType());
        Assert.assertEquals(ParameterModificationType.STANDARD,
                voParameterDefinition.getModificationType());
        Assert.assertEquals(ParameterValueType.INTEGER,
                voParameterDefinition.getValueType());
        Assert.assertEquals(0, voParameterDefinition.getVersion());
        Assert.assertNotNull(voParameterDefinition.getParameterOptions());
        Assert.assertEquals(0, voParameterDefinition.getParameterOptions()
                .size());
    }

    @Test
    public void testToVOParameterDefinitionsNonConfigurableInServiceParamDefs() {
        ParameterDefinition pd = initParameterDefinition();
        pd.setConfigurable(false);
        List<ParameterDefinition> list = Collections.singletonList(pd);
        List<VOParameterDefinition> result = ParameterDefinitionAssembler
                .toVOParameterDefinitions(null, list, true, facade);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testToVOParameterDefinitionsNonConfigurableInPlatformParamDefs() {
        ParameterDefinition pd = initParameterDefinition();
        pd.setConfigurable(false);
        List<ParameterDefinition> list = Collections.singletonList(pd);
        List<VOParameterDefinition> result = ParameterDefinitionAssembler
                .toVOParameterDefinitions(list, null, true, facade);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testToVOParameterDefinitionsNonConfigurableInBothParamDefs() {
        ParameterDefinition pd = initParameterDefinition();
        pd.setConfigurable(false);
        ParameterDefinition pd2 = initParameterDefinition();
        List<ParameterDefinition> list = new ArrayList<ParameterDefinition>();
        list.add(pd);
        list.add(pd2);
        List<VOParameterDefinition> result = ParameterDefinitionAssembler
                .toVOParameterDefinitions(list, list, true, facade);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        validateParameterDefinition(result.get(0));
        validateParameterDefinition(result.get(1));
    }

    /**
     * Creates a parameter definition object.
     * 
     * @return The created parameter definition.
     */
    private ParameterDefinition initParameterDefinition() {
        ParameterDefinition pd = new ParameterDefinition();
        pd.setConfigurable(true);
        pd.setDefaultValue("bla");
        pd.setKey(123);
        pd.setMandatory(true);
        pd.setMaximumValue(L2000);
        pd.setMinimumValue(null);
        pd.setParameterId("diskSpace");
        pd.setParameterType(ParameterType.SERVICE_PARAMETER);
        pd.setModificationType(ParameterModificationType.STANDARD);
        pd.setValueType(ParameterValueType.INTEGER);
        List<ParameterOption> optionList = Collections.emptyList();
        pd.setOptionList(optionList);
        return pd;
    }
}
