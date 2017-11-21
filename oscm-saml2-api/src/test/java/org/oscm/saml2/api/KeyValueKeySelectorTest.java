/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.security.KeyException;
import java.util.ArrayList;

import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author kulle
 * 
 */
public class KeyValueKeySelectorTest {

    private KeyValueKeySelector selector;

    @Before
    public void setup() {
        selector = new KeyValueKeySelector();
    }

    @Test()
    public void select_keyInfo_null() throws Exception {
        // given

        // when
        try {
            selector.select(null, null, null, null);
            fail();
        } catch (KeySelectorException e) {
            assertTrue(e.getMessage().contains("Null KeyInfo object!"));
        }
    }

    @Test()
    public void select_xmlStruct_empty() throws Exception {
        // given
        KeyInfo keyinfo = mock(KeyInfo.class);
        doReturn(new ArrayList<XMLStructure>()).when(keyinfo).getContent();

        // when
        try {
            selector.select(keyinfo, null, null, null);
            fail();
        } catch (KeySelectorException e) {
            assertTrue(e.getMessage().contains(
                    "No RSA/DSA KeyValue element found"));
        }
    }

    @Test()
    public void select_wrong_structType() throws Exception {
        // given
        KeyInfo keyinfo = mock(KeyInfo.class);
        ArrayList<XMLStructure> list = new ArrayList<XMLStructure>();
        KeyName struct = mock(KeyName.class);
        list.add(struct);
        doReturn(list).when(keyinfo).getContent();

        // when
        try {
            selector.select(keyinfo, null, null, null);
            fail();
        } catch (KeySelectorException e) {
            assertTrue(e.getMessage().contains(
                    "No RSA/DSA KeyValue element found"));
        }
    }

    @Test()
    public void select_publicKey_exception() throws Exception {
        // given
        KeyInfo keyinfo = mock(KeyInfo.class);
        ArrayList<XMLStructure> list = new ArrayList<XMLStructure>();
        KeyValue struct = mock(KeyValue.class);
        list.add(struct);
        doReturn(list).when(keyinfo).getContent();
        doThrow(new KeyException("test")).when(struct).getPublicKey();

        // when
        try {
            selector.select(keyinfo, null, null, null);
            fail();
        } catch (KeySelectorException e) {
            assertTrue(e.getCause().getMessage().contains("test"));
        }
    }

}
