/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.app.azure.controller;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import org.oscm.app.azure.AzureCommunication;
import org.oscm.app.v1_0.exceptions.APPlatformException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * Created by PLGrubskiM on 2017-03-22.
 */
public class ProvisioningValidatorTest {

    private static final String RESOURCE_GROUP_NAME_OK = "res1_2-test";
    private static final String RESOURCE_GROUP_NAME_INVALID = "res1_2-test?";

    PropertyHandler ph;
    AzureCommunication azureCommMock;

    @Before
    public void setUp() {
        ph = mock(PropertyHandler.class);
        azureCommMock = mock(AzureCommunication.class);
        ArrayList<String> regions = new ArrayList<>();
        regions.add("region1");
        regions.add("region2");
        when(azureCommMock.getAvailableRegions()).thenReturn(regions);
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersTest_noResourceGroup() throws APPlatformException {
        // given
        // when
        ProvisioningValidator.validateParameters(ph, azureCommMock);
        // then
        // expect exception
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersTest_invalidResourceGroupName() throws APPlatformException {
        // given
        when(ph.getResourceGroupName()).thenReturn(RESOURCE_GROUP_NAME_INVALID);
        // when
        ProvisioningValidator.validateParameters(ph, azureCommMock);
        // then
        // expect exception
    }

    @Test(expected = APPlatformException.class)
    public void validateParametersTest_invalidRegion() throws APPlatformException {
        // given
        when(ph.getResourceGroupName()).thenReturn(RESOURCE_GROUP_NAME_OK);
        when(ph.getRegion()).thenReturn("region3");
        // when
        ProvisioningValidator.validateParameters(ph, azureCommMock);
        // then
        // expect exception
    }

    @Test
    public void validateParametersTest() throws APPlatformException {
        // given
        when(ph.getResourceGroupName()).thenReturn(RESOURCE_GROUP_NAME_OK);
        when(ph.getRegion()).thenReturn("region1");
        // when
        ProvisioningValidator.validateParameters(ph, azureCommMock);
        // then
        // no exceptions
    }

    @Test(expected = RuntimeException.class)
    public void validateTimeoutTest_noStartTime() throws APPlatformException {
        // given
        // when
        ProvisioningValidator.validateTimeout(ph);
        // then
        // no exceptions
    }

    @Test
    public void validateTimeoutTest_invalidStartTime() throws APPlatformException {
        // given
        when(ph.getStartTime()).thenReturn("this is wrong");
        // when
        ProvisioningValidator.validateTimeout(ph);
        // then
        verify(ph, times(0)).setStartTime(any(String.class));
    }

    @Test
    public void validateTimeoutTest() throws APPlatformException {
        // given
        when(ph.getStartTime()).thenReturn("suspended");
        // when
        ProvisioningValidator.validateTimeout(ph);
        // then
        verify(ph, times(1)).setStartTime(any(String.class));
    }
}
