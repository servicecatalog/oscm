/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter;

import java.io.IOException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * This class is responsible to offer the functionality to encode and decode
 * parameters which are attached to an URL. The format used to do the encoding
 * is base64.
 */
public class ParameterEncoder {

    private static BASE64Encoder base64Encoder = new BASE64Encoder();
    private static BASE64Decoder base64Decoder = new BASE64Decoder();

    // Same separator as for parameters in an URL
    private static final String PARAMETER_SEPARATOR = "&";

    /**
     * Converts a string list to a single string and encodes this string. <br>
     * If one parameter in the list is <code>null</code> it'll be ignored.
     * 
     * @param paramList
     *            List of parameters to encode.
     * @return A base64 encoded string which contains all parameters of the
     *         passed list. I fit was not possible to encode the passed
     *         parameter list (e.g. it was null) <code>null</code> will be
     *         returned.
     */
    public static String encodeParameters(String[] paramList) {
        if (paramList == null)
            return null;

        String encodedString;
        StringBuilder sb = new StringBuilder();
        for (String parameter : paramList) {
            if (parameter == null)
                continue;
            sb.append(parameter);
            sb.append(PARAMETER_SEPARATOR);
        }
        encodedString = base64Encoder.encode(sb.toString().getBytes());

        return encodedString;
    }

    /**
     * Decodes the passed string and splits it to a list of single parameters.
     * 
     * @param encodedString
     *            A base64 encoded string which is a concatenation of single
     *            parameters.
     * @return A list of the single parameters. If it was not possible to decode
     *         the passed string (e.g. it was null) <code>null</code> will be
     *         returned.
     */
    public static String[] decodeParameters(String encodedString) {
        if (encodedString == null)
            return null;

        String[] parameters;
        try {
            String decodedString = new String(base64Decoder
                    .decodeBuffer(encodedString));
            parameters = decodedString.split(PARAMETER_SEPARATOR);
        } catch (IOException e) {
            return null;
        }

        return parameters;
    }
}
