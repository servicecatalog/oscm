/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 15.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.oscm.converter.XMLConverter;
import org.oscm.saml2.api.model.assertion.AssertionType;
import org.oscm.security.Keystores;
import org.oscm.internal.types.exception.AssertionValidationException;
import org.oscm.internal.types.exception.DigitalSignatureValidationException;

/**
 * @author kulle
 * 
 */
public class AssertionConsumerService {

    private final String acsUrl;
    private final String acsUrlHttps;
    private AssertionContentVerifier verifier;
    private final String truststorePath;
    private final String truststorePassword;

    public AssertionConsumerService(String acsUrl, String acsUrlHttps,
            String truststorePath, String truststorePassword) {
        this.acsUrl = acsUrl;
        this.acsUrlHttps = acsUrlHttps;
        this.truststorePath = truststorePath;
        this.truststorePassword = truststorePassword;
    }

    /**
     * Validates unencrypted assertions of the IdP response message.
     * 
     * @param idpResponse
     *            xml response message of an IdP
     * @param requestId
     *            id of the authentication request send to the IdP
     * @param tenantID
     * @throws DigitalSignatureValidationException
     *             thrown if the digital signature could not be validated
     * @throws AssertionValidationException
     *             thrown if the content of the assertion is not valid, e.g. the
     *             expiration time is reached
     * @throws ParserConfigurationException
     *             Thrown in case the conversion cannot be initiated.
     * @throws IOException
     *             Thrown in case the reading of the string fails.
     * @throws SAXException
     *             Thrown in case the string cannot be parsed.
     */
    public void validateResponse(String idpResponse, String requestId, String tenantID)
            throws DigitalSignatureValidationException,
            AssertionValidationException, ParserConfigurationException,
            SAXException, IOException {

        VerifierConfiguration config = new VerifierConfiguration(requestId,
                acsUrl, acsUrlHttps, Calendar.getInstance(), tenantID);
        verifier = new AssertionContentVerifier(config);
        Document document = XMLConverter.convertToDocument(idpResponse, true);
        List<Element> assertions = XMLConverter.getElementsByTagNameNS(
                document, AssertionType.XMLNS, SamlXmlTags.NODE_ASSERTION);
        for (Element assertion : assertions) {
            validateDigitalSignature(assertion);
            validateAssertionContent(assertion);
        }
    }

    private void validateDigitalSignature(Element assertion)
            throws DigitalSignatureValidationException {
        assertion.setIdAttribute("ID", true);
        Node signature = retrieveSignature(assertion);
        KeyStore keystore = loadIdpKeystore();
        DigitalSignatureValidator validator = new DigitalSignatureValidator(
                keystore);
        boolean validity = validator.validate(signature);
        if (!validity) {
            DigitalSignatureValidationException exception = new DigitalSignatureValidationException(
                    "Signature is not valid",
                    DigitalSignatureValidationException.ReasonEnum.NOT_VALID);
            throw exception;
        }
    }

    private Node retrieveSignature(Element nodeAssertion)
            throws DigitalSignatureValidationException {
        try {
            return XMLConverter.getNodeByXPath(nodeAssertion,
                    "//*[local-name()='Signature']");
        } catch (XPathExpressionException e) {
            throw new DigitalSignatureValidationException(
                    "Signature is not valid",
                    DigitalSignatureValidationException.ReasonEnum.EXCEPTION_OCCURRED,
                    e);
        }

    }

    private KeyStore loadIdpKeystore()
            throws DigitalSignatureValidationException {
        KeyStore keystore;
        try {
            keystore = Keystores.initializeKeyStore(truststorePath,
                    truststorePassword);
        } catch (KeyStoreException | NoSuchAlgorithmException
                | CertificateException | IOException e) {
            throw new DigitalSignatureValidationException(
                    "IdP truststore could not be loaded",
                    DigitalSignatureValidationException.ReasonEnum.EXCEPTION_KEYSTORE,
                    e);
        }
        return keystore;
    }

    private void validateAssertionContent(Node nodeAssertion)
            throws AssertionValidationException {
        verifier.verifyAssertionContent(nodeAssertion);
    }

}
