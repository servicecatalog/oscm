/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import org.oscm.logging.Log4jLogger;
import org.oscm.saml2.api.model.protocol.AuthnRequestType;
import org.oscm.internal.types.exception.SAMLRedirectURLException;

/**
 * @author roderus
 * 
 */
public class RedirectSamlURLBuilderTest {

    private JAXBElement<AuthnRequestType> authnRequest;
    private RedirectSamlURLBuilder<AuthnRequestType> redirectSamlURLBuilder;
    private Log4jLogger logger;

    @Before
    public void setup() throws DatatypeConfigurationException {
        authnRequest = new AuthnRequestGenerator("Issuer Name", Boolean.TRUE)
                .generateAuthnRequest();
        redirectSamlURLBuilder = spy(new RedirectSamlURLBuilder<AuthnRequestType>());
        logger = mock(Log4jLogger.class);
    }

    @Test
    public void getURL_authnRequest() throws Exception {
        // given
        // when
        URL actualURL = redirectSamlURLBuilder
                .addSamlRequest(authnRequest)
                .addRedirectEndpoint(
                        new URL(
                                "http://idp.something.de:8080/path/to/idp/redirect/endpoint"))
                .getURL();

        // then
        String actualAuthnRequest = extractDecodedSamlRequestFromUrl(actualURL);
        assertTrue(actualAuthnRequest.contains("samlp:AuthnRequest"));
    }

    @Test
    public void getURL_withExistingQueryParameter() throws Exception {
        // given
        // when
        URL actualURL = redirectSamlURLBuilder
                .addSamlRequest(authnRequest)
                .addRedirectEndpoint(
                        new URL(
                                "http://idp.something.de:5050/path/to/redirect/endpoint?Parameter=Value"))
                .addRelayState("some_token").getURL();

        // then
        assertEquals("idp.something.de", actualURL.getHost());
        assertEquals(5050, actualURL.getPort());
        assertEquals("/path/to/redirect/endpoint", actualURL.getPath());
        assertTrue(actualURL.getQuery().contains("Parameter=Value"));
        assertTrue(actualURL.getQuery().contains("RelayState=some_token"));
    }

    @Test
    public void getURL_relayState() throws Exception {
        // given
        // when
        URL actualURL = redirectSamlURLBuilder
                .addSamlRequest(authnRequest)
                .addRedirectEndpoint(
                        new URL(
                                "http://idp.something.de:8080/path/to/idp/redirect/endpoint"))
                .addRelayState(
                        "http://sp.something.de/path/to/requested/resource.html")
                .getURL();

        // then
        String actualRelayState = extractQueryValue(actualURL, "RelayState");
        actualRelayState = URLDecoder.decode(actualRelayState, "UTF-8");
        assertEquals("http://sp.something.de/path/to/requested/resource.html",
                actualRelayState);
    }

    @Test
    public void getURL_noRedirectEndpointSet() throws Exception {
        // given

        try {
            // when
            redirectSamlURLBuilder.addSamlRequest(authnRequest).getURL();
            fail("Expected SAMLRedirectURLException");
        } catch (SAMLRedirectURLException e) {
            // then
            assertTrue(e.getMessage().contains(
                    "SAML redirect endpoint URL not set"));
        }
    }

    @Test
    public void getURL_noAuthnRequestSet() throws Exception {
        // given

        try {
            // when
            redirectSamlURLBuilder
                    .addRedirectEndpoint(
                            new URL(
                                    "http://idp.something.de:8080/path/to/idp/redirect/endpoint"))
                    .getURL();
            fail("Expected SAMLRedirectURLException");
        } catch (SAMLRedirectURLException e) {
            // then
            assertTrue(e.getMessage().contains("SAML request not set"));
        }
    }

    private String extractDecodedSamlRequestFromUrl(URL redirectURL)
            throws UnsupportedEncodingException, DataFormatException {
        String encodedRequest = extractQueryValue(redirectURL, "SAMLRequest");
        return decodeURLBase64DeflateString(encodedRequest);
    }

    private String extractQueryValue(URL url, String field) {
        String query = url.getQuery();
        int beginIndex = query.indexOf(field) + field.length() + 1;
        int endIndex = Math.max(query.indexOf("&", beginIndex), query.length());
        return query.substring(beginIndex, endIndex);
    }

    private String decodeURLBase64DeflateString(final String input)
            throws UnsupportedEncodingException, DataFormatException {
        String urlDecoded = URLDecoder.decode(input, "UTF-8");
        byte[] base64Decoded = Base64.decodeBase64(urlDecoded);

        Inflater decompresser = new Inflater(true);
        decompresser.setInput(base64Decoded);
        StringBuilder result = new StringBuilder();

        while (!decompresser.finished()) {
            byte[] outputFraction = new byte[base64Decoded.length];
            int resultLength = decompresser.inflate(outputFraction);
            result.append(new String(outputFraction, 0, resultLength, "UTF-8"));
        }

        return result.toString();
    }
}
