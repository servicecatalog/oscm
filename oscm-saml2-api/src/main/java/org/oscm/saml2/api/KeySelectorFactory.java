/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 31.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.security.KeyStore;

import javax.xml.crypto.KeySelector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.internal.types.exception.DigitalSignatureValidationException;

/**
 * @author kulle
 * 
 */
class KeySelectorFactory {

    private KeyStore keystore;

    public KeySelectorFactory(KeyStore keystore) {
        this.keystore = keystore;
    }

    public KeySelector newKeySelector(Node nodeSignature)
            throws DigitalSignatureValidationException {

        Node nodeKeyinfo = getKeyInfoNode(nodeSignature);
        if (nodeKeyinfo == null) {
            throw new DigitalSignatureValidationException(
                    "No KeyInfo element found in SAML assertion");
        }

        NodeList children = nodeKeyinfo.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (SamlXmlTags.NODE_KEY_VALUE.equals(node.getLocalName())) {
                return new KeyValueKeySelector();
            } else if (SamlXmlTags.NODE_X509DATA.equals(node.getLocalName())) {
                return new X509KeySelector(keystore);
            }
        }

        throw new DigitalSignatureValidationException(
                "Only RSA/DSA KeyValue and are X509Data supported");
    }

    private Node getKeyInfoNode(Node nodeSignature) {
        NodeList childNodes = nodeSignature.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (SamlXmlTags.NODE_KEY_INFO.equals(childNodes.item(i)
                    .getLocalName())) {
                return childNodes.item(i);
            }
        }
        return null;
    }
}
