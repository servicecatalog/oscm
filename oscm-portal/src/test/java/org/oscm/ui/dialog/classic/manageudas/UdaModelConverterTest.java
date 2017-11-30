/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-6-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.oscm.ui.beans.UdaBean;
import org.oscm.ui.dialog.classic.manageudas.UdaDefinitionDetails;
import org.oscm.ui.dialog.classic.manageudas.UdaDefinitionRowModel;
import org.oscm.ui.dialog.classic.manageudas.UdaModelConverter;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * @author yuyin
 * 
 */
public class UdaModelConverterTest {

    @Test(expected = IllegalArgumentException.class)
    public void convertVoUdaDefinitionToRowModel_NullVo() throws Exception {
        // give
        VOUdaDefinition voUdaDefinition = null;
        // when
        UdaModelConverter.convertVoUdaDefinitionToRowModel(voUdaDefinition);
        // then
    }

    @Test
    public void convertVoUdaDefinitionToRowModel_USER_OPTION_MANDATORY()
            throws Exception {
        UdaDefinitionRowModel model = testConvertVoUdaDefinitionToRowModel(UdaConfigurationType.USER_OPTION_MANDATORY);
        assertTrue(model.isMandatory());
        assertTrue(model.isUserOption());
    }

    @Test
    public void convertVoUdaDefinitionToRowModel_SUPPLIER() throws Exception {
        UdaDefinitionRowModel model = testConvertVoUdaDefinitionToRowModel(UdaConfigurationType.SUPPLIER);
        assertFalse(model.isMandatory());
        assertFalse(model.isUserOption());
    }

    @Test
    public void convertVoUdaDefinitionToRowModel_USER_OPTION_OPTIONAL()
            throws Exception {
        UdaDefinitionRowModel model = testConvertVoUdaDefinitionToRowModel(UdaConfigurationType.USER_OPTION_OPTIONAL);
        assertFalse(model.isMandatory());
        assertTrue(model.isUserOption());
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertUdaDefDetailsToUdaDefinitionRow_NullModel()
            throws Exception {
        // give
        UdaDefinitionDetails model = null;
        // when
        UdaModelConverter.convertUdaDefDetailsToVoUdaDefinition(model);
        // then
    }

    @Test
    public void convertUdaDefDetailsToUdaDefinitionRow_USER_OPTION_OPTIONAL()
            throws Exception {

        VOUdaDefinition voUdaDefinition = testConvertUdaDefDetailsToUdaDefinitionRow(
                false, true);
        assertEquals(UdaConfigurationType.USER_OPTION_OPTIONAL,
                voUdaDefinition.getConfigurationType());
    }

    @Test
    public void convertUdaDefDetailsToUdaDefinitionRow_SUPPLIER()
            throws Exception {

        VOUdaDefinition voUdaDefinition = testConvertUdaDefDetailsToUdaDefinitionRow(
                false, false);
        assertEquals(UdaConfigurationType.SUPPLIER,
                voUdaDefinition.getConfigurationType());
    }

    @Test
    public void convertUdaDefDetailsToUdaDefinitionRow_USER_OPTION_MANDATORY()
            throws Exception {

        VOUdaDefinition voUdaDefinition = testConvertUdaDefDetailsToUdaDefinitionRow(
                true, true);
        assertEquals(UdaConfigurationType.USER_OPTION_MANDATORY,
                voUdaDefinition.getConfigurationType());
    }

    private VOUdaDefinition testConvertUdaDefDetailsToUdaDefinitionRow(
            boolean mandatory, boolean userOption) throws Exception {
        // give
        UdaDefinitionDetails model = ManageUdaDefinitionCtrlTest
                .createUdaDefinitionDetails("123", 1111, "1234", 1, mandatory,
                        userOption);
        // when
        VOUdaDefinition voUdaDefinition = UdaModelConverter
                .convertUdaDefDetailsToVoUdaDefinition(model);
        // then
        assertEquals(1, voUdaDefinition.getVersion());
        assertEquals("123", voUdaDefinition.getDefaultValue());
        assertEquals(1111, voUdaDefinition.getKey());
        assertEquals("1234", voUdaDefinition.getUdaId());
        return voUdaDefinition;
    }

    private UdaDefinitionRowModel testConvertVoUdaDefinitionToRowModel(
            UdaConfigurationType type) {
        // give
        VOUdaDefinition voUdaDefinition = ManageUdaDefinitionCtrlTest
                .createVoDefinition(type, "123", 1111,
                        UdaBean.CUSTOMER_SUBSCRIPTION, "1234", 1);

        // when
        UdaDefinitionRowModel model = UdaModelConverter
                .convertVoUdaDefinitionToRowModel(voUdaDefinition);
        // then
        assertEquals("123", model.getDefaultValue());
        assertEquals(1111, model.getKey());
        assertEquals("1234", model.getUdaId());
        assertEquals(1, model.getVersion());
        return model;
    }
}
