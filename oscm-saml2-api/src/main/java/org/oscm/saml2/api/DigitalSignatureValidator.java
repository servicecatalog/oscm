/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 16.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.security.KeyStore;
import java.security.PublicKey;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import org.w3c.dom.Node;

import org.oscm.internal.types.exception.DigitalSignatureValidationException;

/**
 * @author kulle
 * 
 */
class DigitalSignatureValidator {

    // private final static String CHARACTER_ENCODING = "UTF-8";
    private final static String XML_MECHANISM_TYPE = "DOM";

    private KeySelectorFactory factory;

    public DigitalSignatureValidator(KeyStore keystore) {
        factory = new KeySelectorFactory(keystore);
    }

    /**
     * Steps:
     * <ul>
     * <li>create validation context
     * <li>unmarshal the XMLSignature
     * <li>validate
     * </ul>
     * 
     * @param signature
     *            to be validated
     * @throws DigitalSignatureValidationException
     * @return true if the signature is valid, false otherwise
     */
    public boolean validate(Node nodeSignature)
            throws DigitalSignatureValidationException {
        DOMValidateContext validationContext = new DOMValidateContext(
                factory.newKeySelector(nodeSignature), nodeSignature);
        return validate(validationContext);
    }

    /**
     * Steps:
     * <ul>
     * <li>create validation context
     * <li>unmarshal the XMLSignature
     * <li>validate
     * </ul>
     * 
     * @param signature
     *            to be validated
     * @throws DigitalSignatureValidationException
     * @return true if the signature is valid, false otherwise
     */
    public boolean validate(Node nodeSignature, PublicKey publicKey)
            throws DigitalSignatureValidationException {
        DOMValidateContext validationContext = new DOMValidateContext(
                publicKey, nodeSignature);
        return validate(validationContext);
    }

    private boolean validate(final DOMValidateContext validationContext)
            throws DigitalSignatureValidationException {

        try {
            // if (getLogger().isDebugLoggingEnabled()) {
            // enableReferenceCaching(validationContext);
            // }

            XMLSignatureFactory factory = XMLSignatureFactory
                    .getInstance(XML_MECHANISM_TYPE);
            XMLSignature signature = factory
                    .unmarshalXMLSignature(validationContext);
            boolean validationResult = signature.validate(validationContext);

            validationResult = workaroundOpenamBug(signature,
                    validationContext, validationResult);

            // if (getLogger().isDebugLoggingEnabled()) {
            // debugLogReferences(signature, validationContext);
            // }
            return validationResult;
        } catch (XMLSignatureException | MarshalException exception) {
            throw new DigitalSignatureValidationException(
                    "Error occurred during digital signature validation process",
                    DigitalSignatureValidationException.ReasonEnum.EXCEPTION_OCCURRED,
                    exception);
        }
    }

    /**
     * The overall signature validation consists of two steps, one is the
     * validation of the signature itself and the other the validation of the
     * references digest values. Because of a canonicalization bug in openam,
     * which is not yet registered, the second verification cannot be done.
     * 
     * @return true if the signature validation has not failed, even if the
     *         reference validation failed.
     */
    boolean workaroundOpenamBug(XMLSignature signature,
            DOMValidateContext validationContext, boolean validationResult)
            throws XMLSignatureException {
        if (!validationResult) {
            if (signature.getSignatureValue().validate(validationContext)) {
                return true;
            }
        }
        return validationResult;
    }

    // TODO stavreva get logger
    // private void enableReferenceCaching(
    // final DOMValidateContext validationContext) {
    // validationContext.setProperty("javax.xml.crypto.dsig.cacheReference",
    // Boolean.TRUE);
    // }
    //
    // private void debugLogReferences(final XMLSignature signature,
    // final DOMValidateContext validationContext)
    // throws XMLSignatureException {
    //
    // StringBuilder sb = new StringBuilder();
    // for (Object ref : signature.getSignedInfo().getReferences()) {
    // Reference reference = (Reference) ref;
    // if (!reference.validate(validationContext)) {
    // sb.append("reference id:\t" + reference.getId() + "\n");
    // sb.append("reference uri:\t" + reference.getURI() + "\n");
    // sb.append("pre-digested input:\n");
    // InputStream is = reference.getDigestInputStream();
    // try {
    // BufferedReader bufferedReader = new BufferedReader(
    // new InputStreamReader(is, CHARACTER_ENCODING));
    // String line = bufferedReader.readLine();
    // while (line != null) {
    // sb.append(line);
    // sb.append('\n');
    // line = bufferedReader.readLine();
    // }
    // } catch (IOException e) {
    // // ignore
    // }
    // sb.append("\n\n");
    // }
    // }
    // }

}
