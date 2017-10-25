/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 31.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.security.KeyException;
import java.security.PublicKey;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

/**
 * @author kulle
 * 
 */
class KeyValueKeySelector extends SamlKeySelector {

    @Override
    public KeySelectorResult select(KeyInfo keyInfo,
            KeySelector.Purpose purpose, AlgorithmMethod algorithmMethod,
            XMLCryptoContext context) throws KeySelectorException {

        if (keyInfo == null) {
            throw new KeySelectorException("Null KeyInfo object!");
        }

        @SuppressWarnings("unchecked")
        List<XMLStructure> list = keyInfo.getContent();
        for (XMLStructure xmlStructure : list) {
            if (xmlStructure instanceof KeyValue) {
                PublicKey publicKey = null;
                try {
                    publicKey = ((KeyValue) xmlStructure).getPublicKey();
                } catch (KeyException ke) {
                    throw new KeySelectorException(ke);
                }
                if (algorithmCompatibleWithMethod(
                        algorithmMethod.getAlgorithm(),
                        publicKey.getAlgorithm())) {
                    return new SimpleKeySelectorResult(publicKey);
                }
            }
        }

        throw new KeySelectorException("No RSA/DSA KeyValue element found");
    }

}
