/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 03.06.2013
 *
 *******************************************************************************/

package org.oscm.saml2.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64;

import org.oscm.internal.types.enumtypes.SigningAlgorithmType;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * @author mgrubski
 */
public class LogoutRequestGenerator {

    private static final String UTF_8 = "UTF-8";
    private static final String FORMAT = "http://schemas.xmlsoap.org/claims/UPN";

    /**
     * Creates SAML LogoutRequest as per SAML 2.0 protocol specification.
     *
     * @param idpSessionIndex - session index of the identity provider (receiver of the request)
     * @param nameID - unique identifier of the user registered with the identity provider.
     *
     * @param keystorePass
     * @return
     * @throws SaaSApplicationException
     */
    public String generateLogoutRequest(String idpSessionIndex,
                                        String nameID,
                                        String logoutURL,
                                        String keystorePath,
                                        String issuer,
                                        String keyAlias,
                                        String keystorePass,
                                        SigningAlgorithmType signingAlgorithm) throws SaaSApplicationException {
        try {
            return getRequest(logoutURL, nameID, FORMAT, idpSessionIndex,
                    keystorePath, issuer, keyAlias, keystorePass, signingAlgorithm);
        } catch (XMLStreamException | IOException | GeneralSecurityException e) {
            throw new SaaSApplicationException("Exception during SAML logout URL generation.", e);
        }
    }

    private String getRequest(String logoutUrl,
                              String nameID,
                              String format,
                              String sessionIndex,
                              String keyPath,
                              String issuer,
                              String keyAlias,
                              String keystorePass,
                              SigningAlgorithmType algorithmType) throws XMLStreamException, IOException, GeneralSecurityException, SaaSApplicationException {

        String issueInstant = getIssueDate();

        ByteArrayOutputStream xmlLogoutRequest = producePureXMLLogoutRequest(logoutUrl, nameID, format, sessionIndex, issuer, issueInstant);
        ByteArrayOutputStream deflatedXmlLogoutRequest = deflateBytes(xmlLogoutRequest);
        String base64AndURLEncoded = encodeBase64AndURL(deflatedXmlLogoutRequest);
        String finalSignatureValue = "";

        //If a keyPath was provided, sign it!
        finalSignatureValue = signRequestIfNeeded(keyPath, keyAlias, keystorePass, base64AndURLEncoded, finalSignatureValue, algorithmType);

        String appender = getAppender(logoutUrl);

        return concatenateFullLogoutURL(logoutUrl, base64AndURLEncoded, finalSignatureValue, appender);
    }

    private String concatenateFullLogoutURL(String logoutUrl, String base64AndURLEncoded, String finalSignatureValue, String appender) {
        String fullLogoutURL = logoutUrl + appender + "SAMLRequest=" + getRidOfCRLF(base64AndURLEncoded) + finalSignatureValue;

        return fullLogoutURL;
    }

    private String getAppender(String logoutUrl) {
        String appender = "?";

        if (logoutUrl.indexOf("?") >= 0) {
            appender = "&";
        }
        return appender;
    }

    private String signRequestIfNeeded(String keyPath,
                                       String keyAlias,
                                       String keystorePass,
                                       String base64AndURLEncoded,
                                       String finalSignatureValue,
                                       SigningAlgorithmType type) throws NoSuchAlgorithmException, InvalidKeyException, SaaSApplicationException, IOException, KeyStoreException, CertificateException, UnrecoverableKeyException, SignatureException {
        if (keyPath != null && !"".equals(keyPath)) {

            String encodedSigAlg = URLEncoder.encode(type.getUri(), UTF_8);

            Signature signature = Signature.getInstance(type.getAlgorithm());


            String strSignature = "SAMLRequest=" + getRidOfCRLF(base64AndURLEncoded) + "&SigAlg=" + encodedSigAlg;


            signature.initSign(SamlKeyLoader.loadPrivateKeyFromStore(keyPath, keystorePass, keyAlias));
            signature.update(strSignature.getBytes(UTF_8));

            String encodedSignature = URLEncoder.encode(Base64.encodeBase64String(signature.sign()), UTF_8);

            finalSignatureValue = "&SigAlg=" + encodedSigAlg + "&Signature=" + encodedSignature;
        }
        return finalSignatureValue;
    }

    private ByteArrayOutputStream producePureXMLLogoutRequest(String logoutUrl, String nameID, String format, String sessionIndex, String issuer, String issueInstant) throws XMLStreamException, UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(baos);

        writer.writeStartElement("saml2p", "LogoutRequest", "urn:oasis:names:tc:SAML:2.0:protocol");
        writer.writeNamespace("saml2p", "urn:oasis:names:tc:SAML:2.0:protocol");
        writer.writeAttribute("ID", "_" + UUID.randomUUID().toString());
        writer.writeAttribute("Version", "2.0");
        writer.writeAttribute("Destination", logoutUrl);
        writer.writeAttribute("IssueInstant", issueInstant + "Z");

        writer.writeStartElement("saml2", "Issuer", "urn:oasis:names:tc:SAML:2.0:assertion");
        writer.writeNamespace("saml2", "urn:oasis:names:tc:SAML:2.0:assertion");
        writer.writeCharacters(issuer);
        writer.writeEndElement();

        writer.writeStartElement("saml", "NameID", "urn:oasis:names:tc:SAML:2.0:assertion");
        writer.writeNamespace("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        writer.writeAttribute("Format", format);
        writer.writeCharacters(nameID);
        writer.writeEndElement();

        writer.writeStartElement("saml2p", "SessionIndex", "urn:oasis:names:tc:SAML:2.0:protocol");
        writer.writeCharacters(sessionIndex);
        writer.writeEndElement();

        writer.writeEndElement();
        writer.flush();

        return baos;
    }

    private String encodeBase64AndURL(ByteArrayOutputStream deflatedBytes) throws UnsupportedEncodingException {
        // Base64 Encode the bytes
        byte[] encoded = Base64.encodeBase64Chunked(deflatedBytes.toByteArray());

        // URL Encode the bytes
        return URLEncoder.encode(new String(encoded, Charset.forName(UTF_8)), UTF_8);
    }

    private ByteArrayOutputStream deflateBytes(ByteArrayOutputStream baos) throws IOException {
        ByteArrayOutputStream deflatedBytes = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        DeflaterOutputStream deflaterStream = new DeflaterOutputStream(deflatedBytes, deflater);
        deflaterStream.write(baos.toByteArray());
        deflaterStream.finish();
        return deflatedBytes;
    }

    private String getIssueDate() {
        SimpleDateFormat simpleDf = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss");
        return simpleDf.format(new Date());
    }

    private String getRidOfCRLF(String input) {
        String lf = "%0D";
        String cr = "%0A";
        String now = lf;

        int index = input.indexOf(now);
        StringBuffer r = new StringBuffer();

        while (index != -1) {
            r.append(input.substring(0, index));
            input = input.substring(index + 3, input.length());

            if (now.equals(lf)) {
                now = cr;
            } else {
                now = lf;
            }

            index = input.indexOf(now);
        }
        return r.toString();
    }
}
