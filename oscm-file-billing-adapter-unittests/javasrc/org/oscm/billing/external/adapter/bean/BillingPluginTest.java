/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                                                                                            
 *******************************************************************************/

package org.oscm.billing.external.adapter.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.billing.external.exception.BillingException;

import javax.ws.rs.client.WebTarget;

public class BillingPluginTest {

    private BillingPlugin billingPlugin;
    private ConfigProperties properties;
    private RestDAO restDao;

    @Before
    public void setup() throws Exception {
        billingPlugin = Mockito.spy(new BillingPlugin());
        properties = Mockito.mock(ConfigProperties.class);
        restDao = Mockito.mock(RestDAO.class);

        billingPlugin.properties = properties;
        billingPlugin.restDao = restDao;
    }

    @Test
    public void testConnection() throws Exception {

        // given
        String testConnectionUrl = "http://localhost:8680/oscm-file-billing/rest/ping";
        Mockito.doReturn(testConnectionUrl).when(properties)
                .getConfigProperty(BillingPlugin.TEST_CONNECTION_URL);

        // when
        billingPlugin.testConnection();

        // then
        verify(properties, times(1)).getConfigProperty(anyString());
        verify(restDao, times(1)).createWebResource(testConnectionUrl);
        verify(restDao, times(1)).getTextResponse(any(WebTarget.class));
    }

    @Test
    public void testConnection_failure() throws Exception {
        // given
        BillingException connectionException = new BillingException(
                "Call to File Billing Application failed",
                new RuntimeException("Failed : HTTP error code : 404"));
        Mockito.doReturn("http://localhost:8680/oscm-file-billing/rest/ping")
                .when(properties)
                .getConfigProperty(BillingPlugin.TEST_CONNECTION_URL);
        Mockito.doThrow(connectionException).when(restDao)
                .getTextResponse(any(WebTarget.class));

        // when
        try {
            billingPlugin.testConnection();
            fail("BillingException expected");
        } catch (BillingException e) {
            assertEquals("Wrong exception message",
                    connectionException.getMessage(), e.getMessage());
        }
    }

}
