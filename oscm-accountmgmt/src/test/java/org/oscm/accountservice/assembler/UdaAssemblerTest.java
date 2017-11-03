/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 13.10.2010                                                      
 *                                                                              
 *  Completion Time: 13.10.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.assembler;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.data.Udas;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author weiser
 * 
 */
public class UdaAssemblerTest {

    @Test
    public void testToVOUdaDefinition_Null() throws Exception {
        Assert.assertNull(UdaAssembler.toVOUdaDefinition(null, null));
    }

    @Test
    public void testToVOUdaDefinition() throws Exception {
        UdaDefinition def = createUdaDefinition();
        LocalizerFacade facade = mock(LocalizerFacade.class);
        when(facade.getText(anyLong(), any(LocalizedObjectTypes.class))).thenReturn("localizatgion");
        VOUdaDefinition voDef = UdaAssembler.toVOUdaDefinition(def, facade);
        verifyDefinition(def, voDef);
    }

    @Test
    public void testToUdaDefinition_Null() throws Exception {
        Assert.assertNull(UdaAssembler.toUdaDefinition(null));
    }

    @Test
    public void testToUdaDefinition() throws Exception {
        VOUdaDefinition voDef = Udas.createVOUdaDefinition("CUSTOMER", "udaId",
                "defaultValue", UdaConfigurationType.SUPPLIER);
        UdaDefinition def = UdaAssembler.toUdaDefinition(voDef);
        Assert.assertNotNull(def);
        Assert.assertEquals(voDef.getDefaultValue(), def.getDefaultValue());
        Assert.assertEquals(0, def.getKey());
        Assert.assertEquals(voDef.getUdaId(), def.getUdaId());
        Assert.assertEquals(voDef.getTargetType(), def.getTargetType().name());
        Assert.assertEquals(voDef.getConfigurationType(),
                def.getConfigurationType());
    }

    @Test(expected = ValidationException.class)
    public void testToUdaDefinition_NullId() throws Exception {
        VOUdaDefinition def = Udas.createVOUdaDefinition(
                UdaTargetType.CUSTOMER_SUBSCRIPTION.name(), null, null,
                UdaConfigurationType.SUPPLIER);
        UdaAssembler.toUdaDefinition(def);
    }

    @Test(expected = ValidationException.class)
    public void testToUdaDefinition_EmptyId() throws Exception {
        VOUdaDefinition def = Udas.createVOUdaDefinition(
                UdaTargetType.CUSTOMER_SUBSCRIPTION.name(), "   ", null,
                UdaConfigurationType.SUPPLIER);
        UdaAssembler.toUdaDefinition(def);
    }

    @Test(expected = ValidationException.class)
    public void testToUdaDefinition_ToLongId() throws Exception {
        VOUdaDefinition def = Udas.createVOUdaDefinition(
                UdaTargetType.CUSTOMER_SUBSCRIPTION.name(),
                BaseAdmUmTest.TOO_LONG_ID, null, UdaConfigurationType.SUPPLIER);
        UdaAssembler.toUdaDefinition(def);
    }

    @Test(expected = ValidationException.class)
    public void testToUdaDefinition_NullTargetType() throws Exception {
        VOUdaDefinition def = Udas.createVOUdaDefinition(null, "UDA", null,
                UdaConfigurationType.SUPPLIER);
        UdaAssembler.toUdaDefinition(def);
    }

    @Test(expected = ValidationException.class)
    public void testToUdaDefinition_EmptyTargetType() throws Exception {
        VOUdaDefinition def = Udas.createVOUdaDefinition("   ", "UDA", null,
                UdaConfigurationType.SUPPLIER);
        UdaAssembler.toUdaDefinition(def);
    }

    @Test(expected = ValidationException.class)
    public void testToUdaDefinition_InvalidTargetType() throws Exception {
        VOUdaDefinition def = Udas.createVOUdaDefinition("invalid", "UDA",
                null, UdaConfigurationType.SUPPLIER);
        UdaAssembler.toUdaDefinition(def);
    }

    @Test(expected = ValidationException.class)
    public void testToUdaDefinition_NullConfigurationType() throws Exception {
        VOUdaDefinition def = Udas.createVOUdaDefinition(
                UdaTargetType.CUSTOMER_SUBSCRIPTION.name(), "UDA", null, null);
        UdaAssembler.toUdaDefinition(def);
    }

    @Test(expected = ValidationException.class)
    public void testToUdaDefinition_ToLongDefaultValue() throws Exception {
        VOUdaDefinition def = Udas.createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "UDA",
                BaseAdmUmTest.TOO_LONG_DESCRIPTION,
                UdaConfigurationType.SUPPLIER);
        UdaAssembler.toUdaDefinition(def);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testUpdateUdaDefinition_DifferentVersion() throws Exception {
        UdaDefinition def = createUdaDefinition();
        LocalizerFacade facade = mock(LocalizerFacade.class);
        when(facade.getText(anyLong(), any(LocalizedObjectTypes.class))).thenReturn("localizatgion");
        VOUdaDefinition voDef = UdaAssembler.toVOUdaDefinition(def, facade);
        voDef.setVersion(voDef.getVersion() - 1);
        UdaAssembler.updateUdaDefinition(def, voDef);
    }

    @Test(expected = SaaSSystemException.class)
    public void testUpdateUdaDefinition_DifferentKey() throws Exception {
        UdaDefinition def = createUdaDefinition();
        LocalizerFacade facade = mock(LocalizerFacade.class);
        when(facade.getText(anyLong(), any(LocalizedObjectTypes.class))).thenReturn("localizatgion");
        VOUdaDefinition voDef = UdaAssembler.toVOUdaDefinition(def, facade);
        voDef.setKey(voDef.getKey() - 1);
        UdaAssembler.updateUdaDefinition(def, voDef);
    }

    @Test
    public void testUpdateUdaDefinition() throws Exception {
        UdaDefinition def = createUdaDefinition();
        LocalizerFacade facade = mock(LocalizerFacade.class);
        when(facade.getText(anyLong(), any(LocalizedObjectTypes.class))).thenReturn("localizatgion");
        VOUdaDefinition voDef = UdaAssembler.toVOUdaDefinition(def, facade);
        voDef.setDefaultValue("newDefaultValue");
        voDef.setTargetType(UdaTargetType.CUSTOMER_SUBSCRIPTION.name());
        voDef.setUdaId("newUdaId");
        voDef.setConfigurationType(UdaConfigurationType.USER_OPTION_MANDATORY);
        def = UdaAssembler.updateUdaDefinition(def, voDef);
        Assert.assertNotNull(def);
        Assert.assertEquals(voDef.getDefaultValue(), def.getDefaultValue());
        Assert.assertEquals(voDef.getKey(), def.getKey());
        Assert.assertEquals(voDef.getUdaId(), def.getUdaId());
        Assert.assertEquals(voDef.getTargetType(), def.getTargetType().name());
        Assert.assertEquals(voDef.getVersion(), def.getVersion());
        Assert.assertEquals(voDef.getConfigurationType(),
                def.getConfigurationType());
    }

    @Test
    public void testToVOUda() throws Exception {
        UdaDefinition def = createUdaDefinition();
        Uda uda = new Uda();
        uda.setKey(1234);
        uda.setTargetObjectKey(2345);
        uda.setUdaDefinition(def);
        uda.setUdaValue("42");

        LocalizerFacade facade = mock(LocalizerFacade.class);
        when(facade.getText(anyLong(), any(LocalizedObjectTypes.class))).thenReturn("localizatgion");
        VOUda voUda = UdaAssembler.toVOUda(uda, facade);
        Assert.assertNotNull(voUda);
        Assert.assertEquals(uda.getKey(), voUda.getKey());
        Assert.assertEquals(uda.getTargetObjectKey(),
                voUda.getTargetObjectKey());
        Assert.assertEquals(uda.getUdaValue(), voUda.getUdaValue());
        Assert.assertEquals(uda.getVersion(), voUda.getVersion());
        verifyDefinition(def, voUda.getUdaDefinition());
    }

    @Test
    public void testToVOUda_Null() throws Exception {
        Assert.assertNull(UdaAssembler.toVOUda(null, null));
    }

    @Test
    public void testToUda() throws Exception {
        VOUdaDefinition voDef = Udas.createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "UDA", null,
                UdaConfigurationType.SUPPLIER);
        VOUda voUda = Udas.createVOUda(voDef, "42", 1234);
        voUda.setKey(1234);
        Uda uda = UdaAssembler.toUda(voUda);
        Assert.assertNotNull(uda);
        Assert.assertEquals(0, uda.getKey());
        Assert.assertEquals(voUda.getTargetObjectKey(),
                uda.getTargetObjectKey());
        Assert.assertEquals(voUda.getUdaValue(), uda.getUdaValue());
        Assert.assertNull(uda.getUdaDefinition());
    }

    @Test(expected = ValidationException.class)
    public void testToUda_NullDefinition() throws Exception {
        VOUda voUda = Udas.createVOUda(null, "42", 1234);
        UdaAssembler.toUda(voUda);
    }

    @Test(expected = ValidationException.class)
    public void testToUda_InvalidTargetObjectKey() throws Exception {
        VOUdaDefinition voDef = Udas.createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "UDA", null,
                UdaConfigurationType.SUPPLIER);
        VOUda voUda = Udas.createVOUda(voDef, "42", 0);
        UdaAssembler.toUda(voUda);
    }

    @Test(expected = ValidationException.class)
    public void testToUda_ToLongValue() throws Exception {
        VOUdaDefinition voDef = Udas.createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "UDA", null,
                UdaConfigurationType.SUPPLIER);
        VOUda voUda = Udas.createVOUda(voDef,
                BaseAdmUmTest.TOO_LONG_DESCRIPTION, 1234);
        UdaAssembler.toUda(voUda);
    }

    @Test
    public void testToUda_Null() throws Exception {
        Assert.assertNull(UdaAssembler.toUda(null));
    }

    @Test
    public void testUpdateUda() throws Exception {
        Uda uda = createUda();
        LocalizerFacade facade = mock(LocalizerFacade.class);
        when(facade.getText(anyLong(), any(LocalizedObjectTypes.class))).thenReturn("localizatgion");
        VOUda voUda = UdaAssembler.toVOUda(uda, facade);
        voUda.setTargetObjectKey(5432);
        voUda.setUdaValue("newUdaValue");
        Uda updateUda = UdaAssembler.updateUda(uda, voUda);
        Assert.assertNotNull(updateUda);
        Assert.assertEquals(voUda.getKey(), updateUda.getKey());
        Assert.assertEquals(voUda.getTargetObjectKey(),
                updateUda.getTargetObjectKey());
        Assert.assertEquals(voUda.getUdaValue(), updateUda.getUdaValue());
        Assert.assertNotNull(updateUda.getUdaDefinition());
    }

    @Test(expected = SaaSSystemException.class)
    public void testUpdateUda_DifferentKey() throws Exception {
        Uda uda = createUda();
        LocalizerFacade facade = mock(LocalizerFacade.class);
        when(facade.getText(anyLong(), any(LocalizedObjectTypes.class))).thenReturn("localizatgion");
        VOUda voUda = UdaAssembler.toVOUda(uda, facade);
        voUda.setKey(voUda.getKey() - 1);
        UdaAssembler.updateUda(uda, voUda);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testUpdateUda_DifferentVersion() throws Exception {
        Uda uda = createUda();
        LocalizerFacade facade = mock(LocalizerFacade.class);
        when(facade.getText(anyLong(), any(LocalizedObjectTypes.class))).thenReturn("localizatgion");
        VOUda voUda = UdaAssembler.toVOUda(uda, facade);
        voUda.setVersion(voUda.getVersion() - 1);
        UdaAssembler.updateUda(uda, voUda);
    }

    @Test(expected = ValidationException.class)
    public void testToUdaTargetType_InvalidTargetType() throws Exception {
        UdaAssembler.toUdaTargetType("TEST");
    }

    @Test(expected = ValidationException.class)
    public void testToUdaTargetType_EmptyTargetType() throws Exception {
        UdaAssembler.toUdaTargetType("   ");
    }

    @Test(expected = ValidationException.class)
    public void testToUdaTargetType_NullTargetType() throws Exception {
        UdaAssembler.toUdaTargetType(null);
    }

    private static UdaDefinition createUdaDefinition() {
        UdaDefinition def = new UdaDefinition();
        def.setDefaultValue("defaultValue");
        def.setKey(1234);
        def.setTargetType(UdaTargetType.CUSTOMER);
        def.setUdaId("udaId");
        def.setConfigurationType(UdaConfigurationType.SUPPLIER);
        return def;
    }

    private static Uda createUda() {
        Uda uda = new Uda();
        uda.setKey(1234);
        uda.setTargetObjectKey(2345);
        uda.setUdaDefinition(createUdaDefinition());
        uda.setUdaValue("udaValue");
        return uda;
    }

    private static void verifyDefinition(UdaDefinition def,
            VOUdaDefinition voDef) {
        Assert.assertNotNull(voDef);
        Assert.assertEquals(def.getDefaultValue(), voDef.getDefaultValue());
        Assert.assertEquals(def.getKey(), voDef.getKey());
        Assert.assertEquals(def.getUdaId(), voDef.getUdaId());
        Assert.assertEquals(def.getTargetType().name(), voDef.getTargetType());
        Assert.assertEquals(def.getVersion(), voDef.getVersion());
        Assert.assertEquals(def.getConfigurationType(),
                voDef.getConfigurationType());
    }
}
