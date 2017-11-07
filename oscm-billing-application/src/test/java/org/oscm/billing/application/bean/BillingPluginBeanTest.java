/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.application.bean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.billing.external.billing.service.BillingPluginService;
import org.oscm.billing.external.exception.BillingException;
import org.oscm.domobjects.BillingAdapter;
import org.oscm.internal.types.exception.BillingAdapterConnectionException;
import org.oscm.internal.types.exception.BillingApplicationException;

public class BillingPluginBeanTest {

    private static final String BILLING_ID = "FILE_BILLING";

    private BillingPluginBean billingApplServLocal;
    BillingPluginProxy billingPluginProxy;
    private BillingPluginService billingPluginMock;

    @Before
    public void setup() throws Exception {

        billingPluginProxy = spy(new BillingPluginProxy(null));
        billingPluginMock = mock(BillingPluginService.class);
        doReturn(billingPluginMock).when(billingPluginProxy)
                .locateBillingPluginService();

        billingApplServLocal = spy(new BillingPluginBean());

        doReturn(billingPluginProxy).when(billingApplServLocal)
                .newBillingPluginProxy(BILLING_ID);
        doReturn(billingPluginProxy).when(billingApplServLocal)
                .newBillingPluginProxy(any(BillingAdapter.class));
    }

    @Test
    public void testConnection() throws Exception {
        // given
        doNothing().when(billingPluginMock).testConnection();

        // when
        billingApplServLocal.testConnection(BILLING_ID);
    }

    @Test
    public void testConnection_NoConnectionToBillingApplication()
            throws Exception {
        // given
        final BillingException billingException = new BillingException(
                "No connection to billing application.");

        doThrow(billingException).when(billingPluginMock).testConnection();

        // when
        try {
            billingApplServLocal.testConnection(BILLING_ID);
            fail("BillingApplicationException expected");
        } catch (BillingApplicationException e) {
            // then
            assertTrue("Wrong exception message",
                    e.getMessage().contains(billingException.getMessage()));
        }
    }

    @Test
    public void testConnection_AdapterTimeout() throws Exception {
        // given
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                try {
                    TimeUnit.SECONDS
                            .sleep(BasicBillingProxy.ADAPTER_TIMEOUT_IN_SECONDS + 3);
                } catch (InterruptedException e) {
                }

                return null;
            }
        }).when(billingPluginMock).testConnection();

        // when
        try {
            billingApplServLocal.testConnection(BILLING_ID);
            fail("BillingApplicationException expected");
        } catch (BillingApplicationException e) {
            assertTrue("Wrong exception cause",
                    e.getCause() instanceof BillingAdapterConnectionException);
        }
    }

    @Test
    public void testConnection_adapter() throws Exception {
        // given
        doNothing().when(billingPluginMock).testConnection();

        // when
        billingApplServLocal.testConnection(new BillingAdapter());
    }
}
