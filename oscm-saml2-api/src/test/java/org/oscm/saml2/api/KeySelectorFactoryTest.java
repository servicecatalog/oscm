/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dsig.XMLSignature;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.oscm.converter.XMLConverter;
import org.oscm.string.Strings;
import org.oscm.internal.types.exception.DigitalSignatureValidationException;

/**
 * @author kulle
 * 
 */
public class KeySelectorFactoryTest {

    private final String FILE_OPENAM_RESPONSE = "javares/openamResponse.xml";
    private KeySelectorFactory factory;

    @Before
    public void setup() {
        factory = new KeySelectorFactory(null);
    }

    @Test
    public void newKeySelector_keyinfoEmpty() throws Exception {
        // given
        String response = Strings
                .textFileToString("javares/openamResponse.xml");
        response = response.replaceAll(System.lineSeparator(), "").replaceAll(
                "<ds:KeyInfo>.*</ds:KeyInfo>", "<ds:KeyInfo></ds:KeyInfo>");
        Document document = XMLConverter.convertToDocument(response, true);
        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS,
                "Signature");

        // when
        try {
            factory.newKeySelector(nl.item(0));
            fail();
        } catch (DigitalSignatureValidationException e) {
            assertTrue(e.getMessage().contains(
                    "Only RSA/DSA KeyValue and are X509Data supported"));
        }
    }

    @Test
    public void newKeySelector_keyinfoMissing() throws Exception {
        // given
        String response = Strings
                .textFileToString("javares/openamResponse.xml");
        response = response.replaceAll(System.lineSeparator(), "").replaceAll(
                "<ds:KeyInfo>.*</ds:KeyInfo>", "");
        Document document = XMLConverter.convertToDocument(response, true);
        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS,
                "Signature");

        try {
            // when
            factory.newKeySelector(nl.item(0));
            fail();
        } catch (DigitalSignatureValidationException e) {
            // then
            assertTrue(e.getMessage().contains(
                    "No KeyInfo element found in SAML assertion"));
        }
    }

    @Test
    public void newKeySelector_x509() throws Exception {
        // given
        FileInputStream in = null;
        Document document = null;
        try {
            in = new FileInputStream(FILE_OPENAM_RESPONSE);
            document = XMLConverter.convertToDocument(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS,
                "Signature");

        // when
        KeySelector keySelector = factory.newKeySelector(nl.item(0));

        // then
        assertTrue(keySelector instanceof X509KeySelector);
    }

    @Test
    public void newKeySelector_keyValue() throws Exception {
        // given
        String response = Strings
                .textFileToString("javares/openamResponse.xml");
        Document document = XMLConverter.convertToDocument(
                replaceX509WithKeyValueData(response), true);
        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS,
                "Signature");

        // when
        KeySelector keySelector = factory.newKeySelector(nl.item(0));

        // then
        assertTrue(keySelector instanceof KeyValueKeySelector);
    }

    private String replaceX509WithKeyValueData(String response) {
        final String KEYVALUE = "<ds:KeyInfo><KeyValue><RSAKeyValue><Modulus>...</Modulus><Exponent>AQAB</Exponent></RSAKeyValue></KeyValue></ds:KeyInfo>";
        response = response.replaceAll(System.lineSeparator(), "").replaceAll(
                "<ds:KeyInfo>.*</ds:KeyInfo>", KEYVALUE);
        return response;
    }

    @Test
    public void newKeySelector_firstFound() throws Exception {
        // given
        String response = Strings
                .textFileToString("javares/openamResponse.xml");
        Document document = XMLConverter.convertToDocument(
                addKeyValueAfterX509Data(response), true);
        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS,
                "Signature");

        // when
        KeySelector keySelector = factory.newKeySelector(nl.item(0));

        // then
        assertTrue(keySelector instanceof X509KeySelector);
    }

    private String addKeyValueAfterX509Data(String response) {
        final String KEYVALUE = "<KeyValue><RSAKeyValue><Modulus>...</Modulus><Exponent>AQAB</Exponent></RSAKeyValue></KeyValue>";
        String result = response.replaceAll(System.lineSeparator(), "")
                .replaceAll("(<ds:KeyInfo>.*)</ds:KeyInfo>",
                        "$1" + KEYVALUE + "</ds:KeyInfo>");
        return result;
    }
}
