/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 17.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author kulle
 * 
 */
public class XMLSignatureBuilder {

    String signatureMethod = SignatureMethod.RSA_SHA1;
    String digestMethod = DigestMethod.SHA1;

    public XMLSignatureBuilder() {

    }

    public XMLSignatureBuilder(String signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    public Document sign(FileInputStream fileStream, KeyPair keyPair)
            throws ParserConfigurationException, SAXException, IOException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            KeyException, MarshalException, XMLSignatureException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(fileStream);

        DOMSignContext signContext = new DOMSignContext(keyPair.getPrivate(),
                document.getDocumentElement());
        XMLSignatureFactory signFactory = XMLSignatureFactory
                .getInstance("DOM");
        Reference ref = signFactory.newReference("", signFactory
                .newDigestMethod(digestMethod, null), Collections
                .singletonList(signFactory.newTransform(Transform.ENVELOPED,
                        (TransformParameterSpec) null)), null, null);
        SignedInfo si = signFactory.newSignedInfo(signFactory
                .newCanonicalizationMethod(
                        CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                        (C14NMethodParameterSpec) null), signFactory
                .newSignatureMethod(signatureMethod, null), Collections
                .singletonList(ref));

        KeyInfoFactory kif = signFactory.getKeyInfoFactory();
        KeyValue kv = kif.newKeyValue(keyPair.getPublic());
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

        XMLSignature signature = signFactory.newXMLSignature(si, ki);
        signature.sign(signContext);

        return document;
    }

}
