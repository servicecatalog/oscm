/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.app.azure.exception;

import java.io.IOException;

import com.microsoft.windowsazure.exception.CloudError;
import com.microsoft.windowsazure.exception.ServiceException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.app.azure.AzureCommunication;
import org.oscm.app.v1_0.exceptions.AbortException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by PLGrubskiM on 2017-03-24.
 */
public class AzureServiceExceptionTest {

    private static final String MESSAGE = "msg";
    private static final String ERROR_CODE = "errorCode";

    AzureServiceException ex;
    AzureCommunication azureComm;

    @Before
    public void setUp() {
        azureComm = mock(AzureCommunication.class);
    }

    @Test
    public void testAzureServiceException_message() throws AbortException {
        // given
        ex = new AzureServiceException(MESSAGE);
        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().equals(MESSAGE));
        }
        // then
    }

    @Test
    public void testAzureServiceException_message_cause() throws AbortException {
        // given
        ex = new AzureServiceException(MESSAGE, new Exception());
        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (Exception e) {
            // then
            Assert.assertTrue(e.getMessage().equals(MESSAGE));
            Assert.assertTrue(e instanceof Exception);
        }
    }

    @Test
    public void testAzureServiceException_errorCodeAndMessage() throws AbortException {
        // given

        ServiceException serviceExMock = mock(ServiceException.class);
        CloudError error = mock(CloudError.class);
        when(error.getCode()).thenReturn(ERROR_CODE);
        when(error.getMessage()).thenReturn(MESSAGE);
        when(serviceExMock.getError()).thenReturn(error);
        ex = new AzureServiceException(serviceExMock);

        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (AzureServiceException e) {
            // then
            Assert.assertTrue(e.getErrorMessage().equals(MESSAGE));
            Assert.assertTrue(e.getErrorCode().equals(ERROR_CODE));
            Assert.assertTrue(e instanceof Exception);
        }
    }

    @Test
    public void testAzureServiceException_serviceException_message() throws AbortException {
        // given
        ServiceException serviceExceptionMock = mock(ServiceException.class);
        when(serviceExceptionMock.getMessage()).thenReturn(MESSAGE);
        ex = new AzureServiceException(serviceExceptionMock){
            @Override
            public JsonNode readTree(ObjectMapper mapper, ServiceException ex) throws IOException {
                JsonNode responseDoc = mock(JsonNode.class);
                JsonNode jsonNodeValue = mock(JsonNode.class);
                when(jsonNodeValue.getTextValue()).thenReturn(MESSAGE);
                when(responseDoc.get("Message")).thenReturn(jsonNodeValue);
                return responseDoc;
            }
        };
        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (AzureServiceException e) {
            // then
            Assert.assertTrue(e.getCause() instanceof ServiceException);
            Assert.assertTrue(e.getErrorMessage().equals(MESSAGE));
        }
    }

    @Test
    public void testAzureServiceException_serviceException_message_lowercase() throws AbortException {
        // given
        ServiceException serviceExceptionMock = mock(ServiceException.class);
        when(serviceExceptionMock.getMessage()).thenReturn(MESSAGE);
        ex = new AzureServiceException(serviceExceptionMock){
            @Override
            public JsonNode readTree(ObjectMapper mapper, ServiceException ex) throws IOException {
                JsonNode responseDoc = mock(JsonNode.class);
                JsonNode jsonNodeValue = mock(JsonNode.class);
                when(jsonNodeValue.getTextValue()).thenReturn(MESSAGE);
                when(responseDoc.get("message")).thenReturn(jsonNodeValue);
                return responseDoc;
            }
        };
        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (AzureServiceException e) {
            // then
            Assert.assertTrue(e.getCause() instanceof ServiceException);
            Assert.assertTrue(e.getErrorMessage().equals(MESSAGE));
        }
    }

    @Test
    public void testAzureServiceException_serviceException_noErrorCodeOrMessage() throws AbortException {
        // given
        ServiceException serviceExceptionMock = mock(ServiceException.class);
        when(serviceExceptionMock.getMessage()).thenReturn(MESSAGE);
        ex = new AzureServiceException(serviceExceptionMock){
            @Override
            public JsonNode readTree(ObjectMapper mapper, ServiceException ex) throws IOException {
                JsonNode responseDoc = mock(JsonNode.class);
                JsonNode jsonNodeValue = mock(JsonNode.class);
                when(jsonNodeValue.getTextValue()).thenReturn(ERROR_CODE);
                when(responseDoc.get("Error")).thenReturn(jsonNodeValue);
                return responseDoc;
            }
        };
        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (AzureServiceException e) {
            // then
            Assert.assertTrue(e.getCause() instanceof ServiceException);
            Assert.assertTrue(e.getErrorMessage().isEmpty());
            Assert.assertTrue(e.getErrorCode().isEmpty());
        }
    }

    @Test
    public void testAzureServiceException_serviceException_errorCode() throws AbortException {
        // given
        ServiceException serviceExceptionMock = mock(ServiceException.class);
        when(serviceExceptionMock.getMessage()).thenReturn(MESSAGE);
        ex = new AzureServiceException(serviceExceptionMock){
            @Override
            public JsonNode readTree(ObjectMapper mapper, ServiceException ex) throws IOException {
                JsonNode responseDoc = mock(JsonNode.class);
                JsonNode jsonNodeValue = mock(JsonNode.class);
                when(jsonNodeValue.getTextValue()).thenReturn(ERROR_CODE);
                when(responseDoc.get("Code")).thenReturn(jsonNodeValue);
                return responseDoc;
            }
        };
        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (AzureServiceException e) {
            // then
            Assert.assertTrue(e.getCause() instanceof ServiceException);
            Assert.assertTrue(e.getErrorCode().equals(ERROR_CODE));
        }
    }

    @Test
    public void testAzureServiceException_serviceException_errorCode_lowercase() throws AbortException {
        // given
        ServiceException serviceExceptionMock = mock(ServiceException.class);
        when(serviceExceptionMock.getMessage()).thenReturn(MESSAGE);
        ex = new AzureServiceException(serviceExceptionMock){
            @Override
            public JsonNode readTree(ObjectMapper mapper, ServiceException ex) throws IOException {
                JsonNode responseDoc = mock(JsonNode.class);
                JsonNode jsonNodeValue = mock(JsonNode.class);
                when(jsonNodeValue.getTextValue()).thenReturn(ERROR_CODE);
                when(responseDoc.get("code")).thenReturn(jsonNodeValue);
                return responseDoc;
            }
        };
        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (AzureServiceException e) {
            // then
            Assert.assertTrue(e.getCause() instanceof ServiceException);
            Assert.assertTrue(e.getErrorCode().equals(ERROR_CODE));
        }
    }

    @Test
    public void testAzureServiceException_serviceException_exception() throws AbortException {
        // given
        ServiceException serviceExceptionMock = mock(ServiceException.class);
        when(serviceExceptionMock.getMessage()).thenReturn(MESSAGE);
        ex = new AzureServiceException(serviceExceptionMock){
            @Override
            public JsonNode readTree(ObjectMapper mapper, ServiceException ex) throws IOException {
                throw new IOException();
            }
        };
        when(azureComm.getAvailableRegions()).thenThrow(ex);
        // when
        try {
            azureComm.getAvailableRegions();
        } catch (AzureServiceException e) {
            // then
            Assert.assertTrue(e.getCause() instanceof ServiceException);
        }
    }

    @Test
    public void readTreeTest() throws IOException {
        // given
        ex = new AzureServiceException(MESSAGE);
        // when
        ObjectMapper mapperMock = mock(ObjectMapper.class);
        ServiceException srvExMock = mock(ServiceException.class);
        ex.readTree(mapperMock, srvExMock);
        // then
        // no exceptions
    }

}
