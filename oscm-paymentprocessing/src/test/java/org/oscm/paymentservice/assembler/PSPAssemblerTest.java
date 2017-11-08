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
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.domobjects.PSP;
import org.oscm.domobjects.PSPSetting;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPSetting;

public class PSPAssemblerTest {

    private PSP psp;
    private VOPSP vopsp;

    private LocalizerFacade lf;

    @Before
    public void setup() {
        psp = new PSP();
        psp.setIdentifier("identifier");
        psp.setWsdlUrl("http://www.google.de");
        psp.setDistinguishedName("DOdistinguishedName");
        vopsp = new VOPSP();
        vopsp.setId("voIdentifier");
        vopsp.setWsdlUrl("http://www.voogle.de");
        vopsp.setDistinguishedName("distinguishedName");
        lf = Mockito.mock(LocalizerFacade.class);
    }

    @Test(expected = SaaSSystemException.class)
    public void updatePSP_differentKeys() throws Exception {
        vopsp.setKey(1);
        PSPAssembler.updatePSP(vopsp, psp);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void updatePSP_differentVersions() throws Exception {
        vopsp.setVersion(-1);
        PSPAssembler.updatePSP(vopsp, psp);
    }

    @Test(expected = ValidationException.class)
    public void updatePSP_nullId() throws Exception {
        vopsp.setId(null);
        PSPAssembler.updatePSP(vopsp, psp);
    }

    @Test(expected = ValidationException.class)
    public void updatePSP_TooLongId() throws Exception {
        vopsp.setId("charcharcharcharcharcharcharcharcharchar1");
        PSPAssembler.updatePSP(vopsp, psp);
    }

    @Test
    public void updatePSP_MaxLengthId() throws Exception {
        vopsp.setId("charcharcharcharcharcharcharcharcharchar");
        PSPAssembler.updatePSP(vopsp, psp);
    }

    @Test(expected = ValidationException.class)
    public void updatePSP_nullUrl() throws Exception {
        vopsp.setWsdlUrl(null);
        PSPAssembler.updatePSP(vopsp, psp);
    }

    @Test(expected = ValidationException.class)
    public void updatePSP_invalidUrl() throws Exception {
        vopsp.setWsdlUrl("xx");
        PSPAssembler.updatePSP(vopsp, psp);
    }

    @Test(expected = ValidationException.class)
    public void updatePSP_TooLongDN() throws Exception {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 4097; i++) {
            sb.append("a");
        }
        vopsp.setDistinguishedName(sb.toString());
        PSPAssembler.updatePSP(vopsp, psp);
    }

    @Test
    public void updatePSP() throws Exception {
        PSP result = PSPAssembler.updatePSP(vopsp, psp);
        assertEquals(vopsp.getId(), result.getIdentifier());
        assertEquals(vopsp.getWsdlUrl(), result.getWsdlUrl());
        assertEquals(vopsp.getDistinguishedName(),
                result.getDistinguishedName());
    }

    @Test
    public void toVO() throws Exception {
        psp.setKey(5);
        VOPSP result = PSPAssembler.toVo(psp, lf);
        assertEquals(psp.getIdentifier(), result.getId());
        assertEquals(psp.getWsdlUrl(), result.getWsdlUrl());
        assertEquals(psp.getDistinguishedName(), result.getDistinguishedName());
        assertEquals(5, result.getKey());
    }

    @Test
    public void toVOs() throws Exception {
        psp.setKey(5);
        List<VOPSP> result = PSPAssembler.toVos(Arrays.asList(psp, psp), lf);
        assertNotNull(result);
        assertEquals(2, result.size());
        for (VOPSP currentEntry : result) {
            assertEquals(psp.getIdentifier(), currentEntry.getId());
            assertEquals(psp.getWsdlUrl(), currentEntry.getWsdlUrl());
            assertEquals(5, currentEntry.getKey());
        }
    }

    @Test
    public void toVO_WithSettings() throws Exception {
        psp.setKey(5);
        PSPSetting setting = new PSPSetting();
        setting.setSettingKey("key");
        setting.setSettingValue("value");
        setting.setKey(11);
        psp.addPSPSetting(setting);
        VOPSP result = PSPAssembler.toVo(psp, lf);
        List<VOPSPSetting> pspSettings = result.getPspSettings();
        assertEquals(1, pspSettings.size());
        assertEquals("key", pspSettings.get(0).getSettingKey());
        assertEquals("value", pspSettings.get(0).getSettingValue());
    }

}
