/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 31.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import org.oscm.calendar.GregorianCalendars;
import org.oscm.converter.XMLConverter;
import org.oscm.saml2.api.model.assertion.NameIDType;
import org.oscm.saml2.api.model.protocol.AuthnRequestType;

/**
 * @author roderus
 * 
 */
public class MarshallingTest {

    private Marshalling<AuthnRequestType> marshaller;
    private org.oscm.saml2.api.model.protocol.ObjectFactory protocolObjFactory;
    private org.oscm.saml2.api.model.assertion.ObjectFactory assertionObjFactory;

    @Before
    public void setup() {
        marshaller = new Marshalling<AuthnRequestType>();
        protocolObjFactory = new org.oscm.saml2.api.model.protocol.ObjectFactory();
        assertionObjFactory = new org.oscm.saml2.api.model.assertion.ObjectFactory();
    }

    @Test
    public void marshallElement_authnRequest() throws Exception {
        // given
        XMLGregorianCalendar gregCalendar = GregorianCalendars
                .newXMLGregorianCalendarSystemTime();
        JAXBElement<AuthnRequestType> authnRequestJAXB = createAuthnRequest(gregCalendar);

        // when
        Document authnRequestDoc = marshaller.marshallElement(authnRequestJAXB);

        // then
        String issuerValueResult = XMLConverter.getNodeTextContentByXPath(
                authnRequestDoc, "/samlp:AuthnRequest/saml:Issuer");
        assertEquals("Issuer Test Name", issuerValueResult);

        String versionResult = XMLConverter.getNodeTextContentByXPath(
                authnRequestDoc, "/samlp:AuthnRequest/@Version");
        assertEquals("2.0", versionResult);

        String idResult = XMLConverter.getNodeTextContentByXPath(
                authnRequestDoc, "/samlp:AuthnRequest/@ID");
        assertEquals("4040406c-1530-11e0-e869-0110283f4jj6", idResult);

        String issueInstantResult = XMLConverter.getNodeTextContentByXPath(
                authnRequestDoc, "/samlp:AuthnRequest/@IssueInstant");
        assertEquals(gregCalendar.toString(), issueInstantResult);

        String ACSResult = XMLConverter.getNodeTextContentByXPath(
                authnRequestDoc,
                "/samlp:AuthnRequest/@AssertionConsumerServiceIndex");
        assertEquals("0", ACSResult);
    }

    private JAXBElement<AuthnRequestType> createAuthnRequest(
            XMLGregorianCalendar gregCalendar) {
        NameIDType issuer = assertionObjFactory.createNameIDType();
        AuthnRequestType authnRequest = protocolObjFactory
                .createAuthnRequestType();

        issuer.setValue("Issuer Test Name");
        authnRequest.setIssuer(issuer);
        authnRequest.setID("4040406c-1530-11e0-e869-0110283f4jj6");
        authnRequest.setVersion("2.0");
        authnRequest.setIssueInstant(gregCalendar);
        authnRequest.setAssertionConsumerServiceIndex(Integer.valueOf(0));

        return protocolObjFactory.createAuthnRequest(authnRequest);
    }
}
