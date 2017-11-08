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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.domobjects.PSP;
import org.oscm.domobjects.PSPAccount;
import org.oscm.domobjects.PSPSetting;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPAccount;
import org.oscm.internal.vo.VOPSPSetting;

public class PSPAccountAssemblerTest {

    private PSPAccount pspAccount;
    private VOPSPAccount vopspAccount;

    @Before
    public void setup() {
        pspAccount = new PSPAccount();
        pspAccount.setPspIdentifier("pspIdentifier");
        vopspAccount = new VOPSPAccount();
        vopspAccount.setPspIdentifier("voPspIdentifier");
    }

    @Test(expected = SaaSSystemException.class)
    public void updatePSPAccount_DifferentKeys() throws Exception {
        vopspAccount.setKey(1);
        PSPAccountAssembler.updatePSPAccount(vopspAccount, pspAccount);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void updatePSPAccount_DifferentVersion() throws Exception {
        vopspAccount.setVersion(-1);
        PSPAccountAssembler.updatePSPAccount(vopspAccount, pspAccount);
    }

    @Test(expected = ValidationException.class)
    public void updatePSPAccount_NullId() throws Exception {
        vopspAccount.setPspIdentifier(null);
        PSPAccountAssembler.updatePSPAccount(vopspAccount, pspAccount);
    }

    @Test(expected = ValidationException.class)
    public void updatePSPAccount_EmptyId() throws Exception {
        vopspAccount.setPspIdentifier("");
        PSPAccountAssembler.updatePSPAccount(vopspAccount, pspAccount);
    }

    @Test(expected = ValidationException.class)
    public void updatePSPAccount_TooLongId() throws Exception {
        vopspAccount.setPspIdentifier(getString(256, 'c'));
        PSPAccountAssembler.updatePSPAccount(vopspAccount, pspAccount);
    }

    @Test
    public void updatePSPAccount_MaxLengthId() throws Exception {
        vopspAccount.setPspIdentifier(getString(255, 'c'));
        PSPAccount result = PSPAccountAssembler.updatePSPAccount(vopspAccount,
                pspAccount);
        assertEquals(
                "ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc",
                result.getPspIdentifier());
    }

    @Test
    public void toVo_WithPSPAndSettings() throws Exception {
        pspAccount.setKey(5);
        PSP psp = new PSP();
        psp.setIdentifier("psp");
        PSPSetting pspSetting = new PSPSetting();
        pspSetting.setSettingKey("key");
        pspSetting.setSettingValue("value");
        psp.addPSPSetting(pspSetting);
        pspAccount.setPsp(psp);
        VOPSPAccount result = PSPAccountAssembler.toVo(pspAccount,
                Mockito.mock(LocalizerFacade.class));
        assertEquals(5, result.getKey());
        assertEquals(pspAccount.getPspIdentifier(), result.getPspIdentifier());
        VOPSP voPsp = result.getPsp();
        assertNotNull(voPsp);
        assertEquals("psp", voPsp.getId());
        List<VOPSPSetting> pspSettings = voPsp.getPspSettings();
        assertFalse(pspSettings.isEmpty());
        VOPSPSetting setting = pspSettings.get(0);
        assertEquals("key", setting.getSettingKey());
        assertEquals("value", setting.getSettingValue());
    }

    private String getString(int length, char c) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

}
