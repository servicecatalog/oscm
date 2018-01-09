/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 19.11.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.vo.VOService;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

/**
 * @author weiser
 * 
 */
public class TriggerProcessParameterDataTest {

    private TriggerProcessParameterData tppd;
    private VOService svc;

    @Before
    public void setup() {
        AESEncrypter.generateKey();
        tppd = new TriggerProcessParameterData();
        tppd.setName(TriggerProcessParameterName.PRODUCT);

        svc = new VOService();
    }

    @Test
    public void setValue_Bug9711() {
        svc.setDescription("test" + "\u000b" + "hallo");
        svc.setBaseURL("http://localhost:8080/app");

        tppd.setValue(svc);

        VOService result = tppd.getValue(VOService.class);
        assertEquals("testhallo", result.getDescription());
        assertEquals("http://localhost:8080/app", result.getBaseURL());
    }

    @Test
    public void setValue_JapaneseUnicodeCharsPlusInvalid() {
        svc.setDescription(
                "\u30C0\u30A4\u30EC\u30AF\u30C8\u30A2\u30AF\u30BB\u30B9"
                        + "\uDBBF\uDFFE");

        tppd.setValue(svc);

        VOService result = tppd.getValue(VOService.class);
        assertEquals("\u30C0\u30A4\u30EC\u30AF\u30C8\u30A2\u30AF\u30BB\u30B9",
                result.getDescription());
    }

    @Test
    public void setValue_JapaneseCharsPlusInvalid() {
        svc.setDescription("サービスポータル運用管理部門" + "\u0084");

        tppd.setValue(svc);

        VOService result = tppd.getValue(VOService.class);
        assertEquals("サービスポータル運用管理部門", result.getDescription());
    }

    @Test
    public void setSerializedValue() {
        String value = "<xml>xyz</xml>";
        tppd.setSerializedValue(value);

        String s = tppd.getValue(String.class);
        assertEquals(value, s);
    }

    @Test
    public void setValue() {
        String value = "<xml>xyz</xml>";
        tppd.setValue(value);

        String s = tppd.getValue(String.class);
        assertEquals(value, s);
    }
}
