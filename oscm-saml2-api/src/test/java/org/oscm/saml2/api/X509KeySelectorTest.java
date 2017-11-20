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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.X509Data;

import org.junit.Before;
import org.junit.Test;

/**
 * @author kulle
 * 
 */
public class X509KeySelectorTest {

    private X509KeySelector selector;
    private KeyStore keystore;

    @Before
    public void setup() {
        keystore = mock(KeyStore.class);
        selector = new X509KeySelector(keystore);
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
            assertTrue(e.getMessage().contains("No X509Data element found."));
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
            assertTrue(e.getMessage().contains("No X509Data element found."));
        }
    }

    @Test()
    public void select_x509Data_empty() throws Exception {
        // given
        KeyInfo keyinfo = mock(KeyInfo.class);
        ArrayList<XMLStructure> list = new ArrayList<XMLStructure>();
        X509Data x509Data = mock(X509Data.class);
        list.add(x509Data);
        doReturn(list).when(keyinfo).getContent();
        doReturn(new ArrayList<Object>()).when(x509Data).getContent();

        // when
        try {
            selector.select(keyinfo, null, null, null);
            fail();
        } catch (KeySelectorException e) {
            assertTrue(e.getMessage().contains("No X509Data element found."));
        }
    }

    @Test()
    public void select_x509Data_noCertificate() throws Exception {
        // given
        KeyInfo keyinfo = mock(KeyInfo.class);
        ArrayList<XMLStructure> list = new ArrayList<XMLStructure>();
        X509Data x509Data = mock(X509Data.class);
        list.add(x509Data);
        doReturn(list).when(keyinfo).getContent();
        ArrayList<Object> x509DataContent = new ArrayList<Object>();
        x509DataContent.add(new String());
        doReturn(x509DataContent).when(x509Data).getContent();

        // when
        try {
            selector.select(keyinfo, null, null, null);
            fail();
        } catch (KeySelectorException e) {
            assertTrue(e.getMessage().contains("No X509Data element found."));
        }
    }

    @Test()
    public void select_publicKey_exception() throws Exception {
        // given
        selector = spy(new X509KeySelector(keystore));
        KeyInfo keyinfo = mock(KeyInfo.class);
        ArrayList<XMLStructure> list = new ArrayList<XMLStructure>();
        X509Data x509Data = mock(X509Data.class);
        list.add(x509Data);
        doReturn(list).when(keyinfo).getContent();
        ArrayList<Object> x509DataContent = new ArrayList<Object>();
        x509DataContent.add(mock(X509Certificate.class));
        doReturn(x509DataContent).when(x509Data).getContent();
        doThrow(new KeyStoreException("key exception")).when(selector)
                .getPublicKeyFromKeystore(any(X509Certificate.class),
                        any(SignatureMethod.class));

        // when
        try {
            selector.select(keyinfo, null, null, null);
            fail();
        } catch (KeySelectorException e) {
            assertTrue(e.getCause().getMessage().contains("key exception"));
        }
    }

}
