/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 03.06.2013
 *
 *******************************************************************************/

package org.oscm.saml2.api;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.SigningAlgorithmType;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * Added by @chojnackid
 */
public class LogoutRequestGeneratorTest {

    LogoutRequestGenerator classUnderStress;

    @Before
    public void setup() throws SaaSApplicationException {
        classUnderStress = new LogoutRequestGenerator();
    }

    @Test
    public void generateLogoutRequestTest() throws SaaSApplicationException {


        String result = classUnderStress.generateLogoutRequest("", "", "", "", "", "", "", SigningAlgorithmType.SHA1);

        assertTrue(result.length() != 0);
    }

    @Test(expected = SaaSApplicationException.class)
    public void generateLogoutRequestWithoutKeystoreTest() throws SaaSApplicationException {
        classUnderStress.generateLogoutRequest("", "", "", "aaa", "", "", "", SigningAlgorithmType.SHA1);
    }
}
