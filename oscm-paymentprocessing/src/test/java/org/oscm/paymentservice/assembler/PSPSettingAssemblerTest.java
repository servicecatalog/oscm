/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *  Completion Time: 10.10.2011                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.assembler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.PSPSetting;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPSPSetting;

public class PSPSettingAssemblerTest {

    private VOPSPSetting voPspSetting;
    private PSPSetting pspSetting;

    private final static String TEXT_256 = "texttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttexttext";

    @Before
    public void setup() {
        voPspSetting = new VOPSPSetting();
        voPspSetting.setSettingKey("key");
        voPspSetting.setSettingValue("value");
        pspSetting = new PSPSetting();
        pspSetting.setSettingKey("anotherKey");
        pspSetting.setSettingValue("anotherValue");
    }

    @Test(expected = SaaSSystemException.class)
    public void updatePSPSetting_DifferentKeys() throws Exception {
        voPspSetting.setKey(1);
        PSPSettingAssembler.updatePSPSetting(voPspSetting, pspSetting);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void updatePSPSetting_DifferentVersions() throws Exception {
        voPspSetting.setVersion(-1);
        PSPSettingAssembler.updatePSPSetting(voPspSetting, pspSetting);
    }

    @Test(expected = ValidationException.class)
    public void updatePSPSetting_EmptyKey() throws Exception {
        voPspSetting.setSettingKey("");
        PSPSettingAssembler.updatePSPSetting(voPspSetting, pspSetting);
    }

    @Test
    public void updatePSPSetting_EmptyValue() throws Exception {
        voPspSetting.setSettingValue("");
        PSPSettingAssembler.updatePSPSetting(voPspSetting, pspSetting);
    }

    @Test(expected = ValidationException.class)
    public void updatePSPSetting_TooLongKey() throws Exception {
        voPspSetting.setSettingKey(TEXT_256);
        PSPSettingAssembler.updatePSPSetting(voPspSetting, pspSetting);
    }

    @Test(expected = ValidationException.class)
    public void updatePSPSetting_TooLongValue() throws Exception {
        voPspSetting.setSettingValue(TEXT_256);
        PSPSettingAssembler.updatePSPSetting(voPspSetting, pspSetting);
    }

    @Test
    public void updatePSPSetting() throws Exception {
        PSPSetting result = PSPSettingAssembler.updatePSPSetting(voPspSetting,
                pspSetting);
        assertEquals(voPspSetting.getSettingKey(), result.getSettingKey());
        assertEquals(voPspSetting.getSettingValue(), result.getSettingValue());
    }

    @Test
    public void toVoPspSetting() throws Exception {
        pspSetting.setSettingKey("domainKey");
        pspSetting.setSettingValue("domainValue");
        pspSetting.setKey(20);
        VOPSPSetting result = PSPSettingAssembler.toVoPspSetting(pspSetting);
        assertEquals("domainKey", result.getSettingKey());
        assertEquals("domainValue", result.getSettingValue());
        assertEquals(20, result.getKey());
    }

    @Test
    public void toVoPspSettings() throws Exception {
        PSPSetting localSetting = new PSPSetting();
        localSetting.setSettingKey("domainKey");
        localSetting.setSettingValue("domainValue");
        localSetting.setKey(1);
        List<VOPSPSetting> result = PSPSettingAssembler.toVoPspSettings(Arrays
                .asList(localSetting, pspSetting));
        VOPSPSetting entry = result.get(0);
        assertEquals("domainKey", entry.getSettingKey());
        assertEquals("domainValue", entry.getSettingValue());
        assertEquals(1, entry.getKey());
        entry = result.get(1);
        assertEquals(pspSetting.getSettingKey(), entry.getSettingKey());
        assertEquals(pspSetting.getSettingValue(), entry.getSettingValue());
        assertEquals(0, entry.getKey());
    }
}
