/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import org.oscm.string.Strings;

/**
 * @author roderus
 * 
 */
public class RedirectEncoderTest {

    private final String FILE_METADATA_LINEBREAKS = "javares/metadata_linebreaks.xml";

    private String metadataLinebreaks;
    private RedirectEncoder encodingUtils;

    @Before
    public void setup() throws Exception {
        metadataLinebreaks = Strings.textFileToString(FILE_METADATA_LINEBREAKS);
        encodingUtils = new RedirectEncoder();
    }

    @Test
    public void deflateAndBase64Encode_authnRequestInput() throws Exception {
        // given
        // when
        String encodedString = encodingUtils
                .encodeForRedirectBinding(metadataLinebreaks);

        // then
        String decodedString = decodeURLBase64DeflateString(encodedString);
        assertEquals(metadataLinebreaks, decodedString);
    }

    @Test
    public void deflateAndBase64Encode_emptyInput() throws Exception {
        // given
        // when
        String encodedString = encodingUtils.encodeForRedirectBinding("");

        // then
        assertEquals("", encodedString);
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
