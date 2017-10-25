/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 31.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;

/**
 * @author kulle
 * 
 */
class X509KeySelector extends SamlKeySelector {

    private KeyStore keystore;

    public X509KeySelector(KeyStore keystore) {
        this.keystore = keystore;
    }

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
            if (xmlStructure instanceof X509Data) {
                X509Data x509Data = (X509Data) xmlStructure;
                @SuppressWarnings("rawtypes")
                List content = x509Data.getContent();
                for (int i = 0; i < content.size(); i++) {
                    Object x509Content = content.get(i);
                    if (x509Content instanceof X509Certificate) {
                        X509Certificate certificate = (X509Certificate) x509Content;
                        try {
                            return getPublicKeyFromKeystore(certificate,
                                    (SignatureMethod) algorithmMethod);
                        } catch (KeyStoreException e) {
                            throw new KeySelectorException(e);
                        }
                    }
                }
            }
        }

        throw new KeySelectorException("No X509Data element found.");
    }

    KeySelectorResult getPublicKeyFromKeystore(X509Certificate certificate,
            SignatureMethod signatureMethod) throws KeyStoreException,
            KeySelectorException {

        isSigningCertificate(certificate);
        return searchInKeystore(certificate, signatureMethod);
    }

    KeySelectorResult searchInKeystore(X509Certificate certificate,
            SignatureMethod signatureMethod) throws KeyStoreException,
            KeySelectorException {
        String alias = keystore.getCertificateAlias(certificate);
        if (alias != null) {
            PublicKey pk = keystore.getCertificate(alias).getPublicKey();
            if (algorithmCompatibleWithMethod(signatureMethod.getAlgorithm(),
                    pk.getAlgorithm())) {
                return new SimpleKeySelectorResult(pk);
            }
        }
        throw new KeySelectorException(
                "X509 content is not a signing certificate");
    }

    private void isSigningCertificate(X509Certificate certificate)
            throws KeySelectorException {
        boolean[] keyUsage = certificate.getKeyUsage();
        if (keyUsage != null && keyUsage[0] == false) {
            throw new KeySelectorException(
                    "X509 content is not a signing certificate");
        }
    }

}
