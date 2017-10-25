/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 23.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.zip.Deflater;

import org.apache.commons.codec.binary.Base64;

/**
 * @author roderus
 * 
 */
public class RedirectEncoder {

    /**
     * Encodes a SAML request message to be used in the SAML 2.0 Redirect
     * binding. The input message is encoded as follows:
     * 
     * 1. DEFLATE compression; 2. Base64 encoding; 3. URL-save encoding (UTF-8).
     * 
     * The encoded string can be used as query parameter (e.g. as value for
     * "SAMLResponse") in a URL.
     * 
     * @param input
     *            String the SAML message to be encoded.
     * @return String the encoded message.
     * @throws UnsupportedEncodingException
     */
    public String encodeForRedirectBinding(final String input)
            throws UnsupportedEncodingException {

        byte[] deflatedInput = deflate(input);
        String base64String = new String(Base64.encodeBase64(deflatedInput,
                false), "UTF-8");

        String encodedString = URLEncoder.encode(base64String, "UTF-8");

        return encodedString;
    }

    private byte[] deflate(String input) throws UnsupportedEncodingException {
        byte[] inputBytes = input.getBytes("UTF-8");
        byte[] output = new byte[inputBytes.length];
        Deflater compresser = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        compresser.setInput(inputBytes);
        compresser.finish();
        int outputLen = compresser.deflate(output);
        compresser.end();

        byte[] exact = new byte[outputLen];
        System.arraycopy(output, 0, exact, 0, outputLen);

        return exact;
    }

}
