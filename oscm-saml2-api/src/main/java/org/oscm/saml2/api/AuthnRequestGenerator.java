/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.util.Random;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;

import org.oscm.calendar.GregorianCalendars;
import org.oscm.converter.XMLConverter;
import org.oscm.saml2.api.model.assertion.NameIDType;
import org.oscm.saml2.api.model.protocol.AuthnRequestType;
import org.oscm.internal.types.exception.SAML2AuthnRequestException;

/**
 * @author roderus
 * 
 */
public class AuthnRequestGenerator {

    private final static Integer HTTPS_INDEX = Integer.valueOf(0);
    private final static Integer HTTP_INDEX = Integer.valueOf(1);

    private Random prng = new Random();
    private String issuer;
    private Boolean isHttps;
    private String requestId;

    public AuthnRequestGenerator(String issuer, Boolean isHttps) {
        this.issuer = issuer;
        this.isHttps = isHttps;
        this.requestId = generate160BitID();
    }

    /**
     * @return BASE64-encoded SAML Authentication Request
     * @throws SAML2AuthnRequestException
     */
    public String getEncodedAuthnRequest() throws SAML2AuthnRequestException {
        String authnRequest = null;
        try {
            authnRequest = marshal(generateAuthnRequest());
        } catch (SAML2AuthnRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new SAML2AuthnRequestException(
                    e.getMessage(),
                    SAML2AuthnRequestException.ReasonEnum.XML_TRANSFORMATION_ERROR);
        }
        return encodeBase64(authnRequest);
    }

    public String getRequestId() {
        return requestId;
    }

    public JAXBElement<AuthnRequestType> generateAuthnRequest()
            throws DatatypeConfigurationException {

        org.oscm.saml2.api.model.protocol.ObjectFactory protocolObjFactory;
        protocolObjFactory = new org.oscm.saml2.api.model.protocol.ObjectFactory();
        org.oscm.saml2.api.model.assertion.ObjectFactory assertionObjFactory;
        assertionObjFactory = new org.oscm.saml2.api.model.assertion.ObjectFactory();

        NameIDType issuer = assertionObjFactory.createNameIDType();
        issuer.setValue(this.issuer);

        AuthnRequestType authnRequest = protocolObjFactory
                .createAuthnRequestType();
        authnRequest.setID(requestId);
        authnRequest.setVersion("2.0");
        authnRequest.setIssueInstant(GregorianCalendars
                .newXMLGregorianCalendarSystemTime());
        Integer acsIndex = isHttps.booleanValue() ? HTTPS_INDEX : HTTP_INDEX;
        authnRequest.setAssertionConsumerServiceIndex(acsIndex);
        authnRequest.setIssuer(issuer);

        JAXBElement<AuthnRequestType> authnRequestJAXB = protocolObjFactory
                .createAuthnRequest(authnRequest);

        return authnRequestJAXB;
    }

    String marshal(JAXBElement<AuthnRequestType> authnRequest) throws Exception {
        Marshalling<AuthnRequestType> marshaller = new Marshalling<AuthnRequestType>();
        Document samlRequestDoc = marshaller.marshallElement(authnRequest);
        String authnRequestString = XMLConverter.convertToString(
                samlRequestDoc, false);
        return XMLConverter.removeEOLCharsFromXML(authnRequestString);
    }

    private String generate160BitID() {
        byte[] randomBytes = new byte[20];
        prng.nextBytes(randomBytes);

        // the XML-Schema standard requires an ID to start with [a-zA-Z_:]
        StringBuilder sb = new StringBuilder("ID_");
        // the rest can also contain numbers
        for (byte b : randomBytes) {
            sb.append(String.format("%02x", Byte.valueOf(b)));
        }

        return sb.toString();
    }

    private String encodeBase64(String authnRequest) {
        String encodedAuthnRequest = Base64.encodeBase64String(authnRequest
                .getBytes());
        return XMLConverter.removeEOLCharsFromXML(encodedAuthnRequest);
    }
}
