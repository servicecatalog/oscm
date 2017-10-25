/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 23.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.net.URL;
import java.net.URLEncoder;

import javax.xml.bind.JAXBElement;

import org.w3c.dom.Document;

import org.oscm.converter.XMLConverter;
import org.oscm.internal.types.exception.SAMLRedirectURLException;

/**
 * @author roderus
 * 
 */
public class RedirectSamlURLBuilder<T> {

    private URL redirectEndpoint = null;
    private JAXBElement<T> samlRequest = null;
    private String relayState = null;

    public RedirectSamlURLBuilder<T> addRedirectEndpoint(URL redirectEndpoint) {
        this.redirectEndpoint = redirectEndpoint;
        return this;
    }

    public RedirectSamlURLBuilder<T> addSamlRequest(JAXBElement<T> samlRequest) {
        this.samlRequest = samlRequest;
        return this;
    }

    public RedirectSamlURLBuilder<T> addRelayState(String relayState) {
        this.relayState = relayState;
        return this;
    }

    public URL getURL() throws Exception {

        if (redirectEndpoint == null) {
            throw new SAMLRedirectURLException(
                    "SAML redirect endpoint URL not set",
                    SAMLRedirectURLException.ReasonEnum.MISSING_ENDPOINTURL);
        }
        if (samlRequest == null) {
            throw new SAMLRedirectURLException("SAML request not set",
                    SAMLRedirectURLException.ReasonEnum.MISSING_AUTHNREQUEST);
        }

        String samlRequest = getSamlRequestAsString();

        RedirectEncoder coding = new RedirectEncoder();
        String samlRequest_encoded = coding
                .encodeForRedirectBinding(samlRequest);

        StringBuilder finalUrl = new StringBuilder(
                redirectEndpoint.toExternalForm());
        if (redirectEndpoint.getQuery() != null) {
            finalUrl.append("&");
        } else {
            finalUrl.append("?");
        }

        finalUrl.append("SAMLRequest=").append(samlRequest_encoded);
        if (relayState != null) {
            finalUrl.append("&RelayState=").append(URLEncoder.encode(relayState, "UTF-8"));
        }

        return new URL(finalUrl.toString());
    }

    private String getSamlRequestAsString() throws Exception {
        Marshalling<T> marshaller = new Marshalling<>();
        Document samlRequestDoc = marshaller.marshallElement(samlRequest);
        return XMLConverter.convertToString(samlRequestDoc, false);
    }
}
