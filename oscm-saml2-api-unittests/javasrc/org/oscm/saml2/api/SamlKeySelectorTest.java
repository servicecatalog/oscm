/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 04.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import javax.xml.crypto.dsig.SignatureMethod;

import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.internal.types.enumtypes.SigningAlgorithmType;

/**
 * @author kulle
 * 
 */
public class SamlKeySelectorTest {

    @Test
    public void algorithmCompatibleWithMethod_dsa() {
        // given
        SamlKeySelector keySelector = mock(SamlKeySelector.class,
                Mockito.CALLS_REAL_METHODS);

        // when
        boolean result = keySelector.algorithmCompatibleWithMethod(
                SignatureMethod.DSA_SHA1, keySelector.ALGORITHM_DSA);

        // then
        assertTrue(result);
    }

    @Test
    public void algorithmCompatibleWithMethod_dsa_wrongSignatureMethod() {
        // given
        SamlKeySelector keySelector = mock(SamlKeySelector.class,
                Mockito.CALLS_REAL_METHODS);

        // when
        boolean result = keySelector.algorithmCompatibleWithMethod(
                "wrong method", keySelector.ALGORITHM_DSA);

        // then
        assertFalse(result);
    }

    @Test
    public void algorithmCompatibleWithMethod_rsa() {
        // given
        SamlKeySelector keySelector = mock(SamlKeySelector.class,
                Mockito.CALLS_REAL_METHODS);

        // when
        boolean result = keySelector.algorithmCompatibleWithMethod(
                SignatureMethod.RSA_SHA1, keySelector.ALGORITHM_RSA);

        // then
        assertTrue(result);
    }

    @Test
    public void algorithmCompatibleWithMethod_rsa256() {
        // given
        SamlKeySelector keySelector = mock(SamlKeySelector.class,
                Mockito.CALLS_REAL_METHODS);

        // when
        boolean result = keySelector.algorithmCompatibleWithMethod(
                SigningAlgorithmType.SHA256.getUri(), keySelector.ALGORITHM_RSA);

        // then
        assertTrue(result);
    }

    @Test
    public void algorithmCompatibleWithMethod_rsa_wrongSignatureMethod() {
        // given
        SamlKeySelector keySelector = mock(SamlKeySelector.class,
                Mockito.CALLS_REAL_METHODS);

        // when
        boolean result = keySelector.algorithmCompatibleWithMethod(
                "wrong method", keySelector.ALGORITHM_RSA);

        // then
        assertFalse(result);
    }

    @Test
    public void algorithmCompatibleWithMethod_wrongAlgorithm() {
        // given
        SamlKeySelector keySelector = mock(SamlKeySelector.class,
                Mockito.CALLS_REAL_METHODS);

        // when
        boolean result = keySelector.algorithmCompatibleWithMethod(
                SignatureMethod.RSA_SHA1, "wrong algorithm");

        // then
        assertFalse(result);
    }

}
