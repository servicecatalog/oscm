/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter;

import org.junit.Assert;
import org.junit.Test;

public class ParameterEncoderTest {

    private final String testStringDecoded_part1 = "username";
    private final String testStringDecoded_part2 = "orgid";
    private final String testStringDecoded_part3 = "serviceid";

    private final String testStringEncoded = "dXNlcm5hbWUmb3JnaWQmc2VydmljZWlkJg==";

    /**
     * Simple test to check the encoding of parameters.
     */
    @Test
    public void testEncodeParam_success() {
        String[] testParam = new String[] { testStringDecoded_part1,
                testStringDecoded_part2, testStringDecoded_part3 };
        String result = ParameterEncoder.encodeParameters(testParam);

        Assert.assertEquals(testStringEncoded, result);
    }

    /**
     * Simple test to check the decoding of parameters.
     */
    @Test
    public void testDecodeParam_success() {
        String[] result = ParameterEncoder.decodeParameters(testStringEncoded);
        Assert.assertEquals(3, result.length);

        Assert.assertEquals(testStringDecoded_part1, result[0]);
        Assert.assertEquals(testStringDecoded_part2, result[1]);
        Assert.assertEquals(testStringDecoded_part3, result[2]);
    }

    /**
     * Check the behavior when passing null as argument.
     */
    @Test
    public void testEncodeParam_nullParamList() {
        String result = ParameterEncoder.encodeParameters(null);
        Assert.assertEquals(null, result);
    }

    /**
     * Check the behavior when one of the arguments in the list is null;
     */
    @Test
    public void testEncodeParam_nullParam() {
        String[] testParam = new String[] { testStringDecoded_part1, null,
                testStringDecoded_part3 };

        String result = ParameterEncoder.encodeParameters(testParam);

        // The null will be ignored when doing the convention.
        Assert.assertEquals("dXNlcm5hbWUmc2VydmljZWlkJg==", result);
    }

    /**
     * Check the behavior when passing null as argument.
     */
    @Test
    public void testDecodeParam_nullParam() {
        String[] result = ParameterEncoder.decodeParameters(null);
        Assert.assertNull(result);
    }

}
