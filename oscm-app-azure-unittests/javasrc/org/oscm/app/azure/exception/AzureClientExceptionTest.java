/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *******************************************************************************/
package org.oscm.app.azure.exception;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.app.azure.AzureCommunication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by PLGrubskiM on 2017-03-24.
 */
public class AzureClientExceptionTest {

    private static final String MESSAGE = "msg";

    AzureClientException ex;
    AzureCommunication azureComm;

    @Before
    public void setUp() {
        azureComm = mock(AzureCommunication.class);
    }

    @Test
    public void testAzureClientException_message() {
        // given
        ex = new AzureClientException(MESSAGE);
        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (Exception ex) {
            // then
            Assert.assertTrue(ex instanceof RuntimeException);
            Assert.assertTrue(ex.getMessage().equals(MESSAGE));
        }
    }

    @Test
    public void testAzureClientException_cause() {
        // given
        ex = new AzureClientException(new Exception());
        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (Exception ex) {
            // then
            Assert.assertTrue(ex instanceof RuntimeException);
            Assert.assertTrue(!ex.getMessage().isEmpty());
            Assert.assertTrue(ex.getCause() instanceof Exception);
        }
    }

    @Test
    public void testAzureClientException_cause_msg() {
        // given
        ex = new AzureClientException(MESSAGE, new Exception());
        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (Exception ex) {
            // then
            Assert.assertTrue(ex instanceof RuntimeException);
            Assert.assertTrue(ex.getMessage().equals(MESSAGE));
            Assert.assertTrue(ex.getCause() instanceof Exception);
        }
    }
}
