/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.oscm.converter.XMLConverter;
import org.oscm.security.Keystores;
import org.oscm.test.security.XMLSignatureBuilder;
import org.oscm.internal.types.exception.DigitalSignatureValidationException;

/**
 * @author kulle
 * 
 */
public class DigitalSignatureValidatorTest {

    private final String FILE_OPENAM_RESPONSE = "javares/openamResponse.xml";
    private final String FILE_UNSIGNED_ASSERTION = "javares/unsignedAssertion.xml";
    private final String FILE_KEYSTORE_OPENAM = "javares/openam.jks";

    private DigitalSignatureValidator validator;

    @Before
    public void setup() throws Exception {
        KeyStore keystore = Keystores.initializeKeyStore(FILE_KEYSTORE_OPENAM,
                "changeit");
        validator = spy(new DigitalSignatureValidator(keystore));
    }

    private KeyPair generateKeyPair(String algorithm)
            throws NoSuchAlgorithmException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(algorithm);
        keyGenerator.initialize(512);
        KeyPair keyPair = keyGenerator.generateKeyPair();
        return keyPair;
    }

    @Test(expected = DigitalSignatureValidationException.class)
    public void validate_error() throws Exception {
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
        doThrow(new XMLSignatureException("")).when(validator)
                .workaroundOpenamBug(any(XMLSignature.class),
                        any(DOMValidateContext.class), anyBoolean());

        // when
        validator.validate(nl.item(0));

        // then exception expected
    }

    @Test
    public void validate_x509_openamResponse() throws Exception {
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
        Element assertion = (Element) document.getElementsByTagNameNS(
                "urn:oasis:names:tc:SAML:2.0:assertion", "Assertion").item(0);
        assertion.setIdAttribute("ID", true);
        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS,
                "Signature");

        // when
        boolean valid = validator.validate(nl.item(0));

        // then
        assertTrue(valid);
    }

    @Test
    public void validate_rsa_publicKey() throws Exception {
        // given
        XMLSignatureBuilder builder = new XMLSignatureBuilder();
        KeyPair keyPair = generateKeyPair("RSA");

        FileInputStream in = null;
        Document document = null;
        try {
            in = new FileInputStream(FILE_UNSIGNED_ASSERTION);
            document = builder.sign(in, keyPair);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS,
                "Signature");

        // when
        boolean valid = validator.validate(nl.item(0), keyPair.getPublic());

        // then
        assertTrue(valid);
    }

    @Test
    public void validate_rsa_keySelector() throws Exception {
        // given
        XMLSignatureBuilder builder = new XMLSignatureBuilder();
        KeyPair keyPair = generateKeyPair("RSA");
        FileInputStream in = null;
        Document document = null;
        try {
            in = new FileInputStream(FILE_UNSIGNED_ASSERTION);
            document = builder.sign(in, keyPair);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS,
                "Signature");

        // when
        boolean valid = validator.validate(nl.item(0));

        // then
        assertTrue(valid);
    }

    @Test
    public void validate_dsa_publicKey() throws Exception {
        // given
        XMLSignatureBuilder builder = new XMLSignatureBuilder(
                SignatureMethod.DSA_SHA1);
        KeyPair keyPair = generateKeyPair("DSA");
        FileInputStream in = null;
        Document document = null;
        try {
            in = new FileInputStream(FILE_UNSIGNED_ASSERTION);
            document = builder.sign(in, keyPair);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS,
                "Signature");

        // when
        boolean valid = validator.validate(nl.item(0), keyPair.getPublic());

        // then
        assertTrue(valid);
    }

    @Test
    public void validate_dsa_keySelector() throws Exception {
        // given
        XMLSignatureBuilder builder = new XMLSignatureBuilder(
                SignatureMethod.DSA_SHA1);
        KeyPair keyPair = generateKeyPair("DSA");
        FileInputStream in = null;
        Document document = null;
        try {
            in = new FileInputStream(FILE_UNSIGNED_ASSERTION);
            document = builder.sign(in, keyPair);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS,
                "Signature");

        // when
        boolean valid = validator.validate(nl.item(0));

        // then
        assertTrue(valid);
    }

}
