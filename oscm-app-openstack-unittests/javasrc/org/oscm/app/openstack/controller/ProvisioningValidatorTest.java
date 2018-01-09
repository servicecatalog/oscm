/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Sep 27, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.controller;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.v2_0.exceptions.APPlatformException;

/**
 * Unit test for ProvisioningValidator
 * 
 * @author miethaner
 */
public class ProvisioningValidatorTest extends ProvisioningValidator {

    @Test
    public void testValidationPositive() throws Exception {

        PropertyHandler ph = Mockito.mock(PropertyHandler.class);
        Mockito.when(ph.getStackName()).thenReturn("Test123");
        Mockito.when(ph.getStackNamePattern()).thenReturn("Test123");

        validateStackName(ph);
    }

    @Test
    public void testValidationNegativeName() throws Exception {

        PropertyHandler ph = Mockito.mock(PropertyHandler.class);
        Mockito.when(ph.getStackName()).thenReturn("");

        try {
            validateStackName(ph);
            fail();
        } catch (APPlatformException e) {
        }
    }

    @Test
    public void testValidationNegativeNativePattern() throws Exception {

        PropertyHandler ph = Mockito.mock(PropertyHandler.class);
        Mockito.when(ph.getStackName()).thenReturn("$$Test123");

        try {
            validateStackName(ph);
            fail();
        } catch (APPlatformException e) {
        }
    }

    @Test
    public void testValidationNegativeCustomPattern() throws Exception {

        PropertyHandler ph = Mockito.mock(PropertyHandler.class);
        Mockito.when(ph.getStackName()).thenReturn("Test123");
        Mockito.when(ph.getStackNamePattern()).thenReturn("Test");

        try {
            validateStackName(ph);
            fail();
        } catch (APPlatformException e) {
        }
    }
}
