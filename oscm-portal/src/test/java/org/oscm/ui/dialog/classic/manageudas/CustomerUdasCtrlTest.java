/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-6-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import org.oscm.ui.beans.UdaBean;
import org.oscm.ui.dialog.classic.manageudas.CustomerUdas;
import org.oscm.ui.dialog.classic.manageudas.CustomerUdasCtrl;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * @author yuyin
 * 
 */
public class CustomerUdasCtrlTest {
    private CustomerUdasCtrl customerUdasCtrl;
    private AccountService delegate;
    private List<VOUdaDefinition> voDefinitions;
    private List<VOUda> subVoUdas;
    private List<VOUda> cusVoUdas;
    @Captor
    ArgumentCaptor<String> spID;
    @Captor
    ArgumentCaptor<String> type;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        delegate = mock(AccountService.class);
        subVoUdas = new ArrayList<VOUda>();
        cusVoUdas = new ArrayList<VOUda>();

        customerUdasCtrl = spy(new CustomerUdasCtrl(delegate));
        voDefinitions = new ArrayList<VOUdaDefinition>();
        doReturn(voDefinitions).when(delegate).getUdaDefinitionsForCustomer(
                anyString());
        doReturn(cusVoUdas).when(delegate).getUdasForCustomer(
                eq(UdaBean.CUSTOMER), anyLong(), anyString());
        doReturn(subVoUdas).when(delegate).getUdasForCustomer(
                eq(UdaBean.CUSTOMER_SUBSCRIPTION), anyLong(), anyString());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUdasForNewSubscription_InValidID() throws Exception {
        // given
        String supplierId = "no such SP ID";
        doThrow(new ObjectNotFoundException()).when(delegate)
                .getUdaDefinitionsForCustomer(eq(supplierId));
        // when
        customerUdasCtrl.getUdasForNewSubscription(supplierId, 0);
        // then
    }

    @Test
    public void getUdasForNewSubscription_NoOrganizationUDADefinition()
            throws Exception {
        // given
        String supplierId = "SP";

        VOUdaDefinition cusUda = ManageUdaDefinitionCtrlTest
                .createVoDefinition(UdaConfigurationType.USER_OPTION_OPTIONAL,
                        "defaultValue", 123, UdaBean.CUSTOMER_SUBSCRIPTION,
                        "UDAID", 1);
        voDefinitions.add(cusUda);
        VOUda voUda = new VOUda();
        voUda.setUdaDefinition(cusUda);
        voUda.setKey(12345);
        voUda.setTargetObjectKey(123);
        cusVoUdas.add(voUda);
        // when
        CustomerUdas result = customerUdasCtrl.getUdasForNewSubscription(
                supplierId, 123);
        // then
        assertEquals(0, result.getOrganizationUdaRows().size());
        assertEquals(1, result.getSubscriptionUdaRows().size());
        assertEquals(UdaBean.CUSTOMER_SUBSCRIPTION, result
                .getSubscriptionUdaRows().get(0).getUdaDefinition()
                .getTargetType());
    }

    @Test
    public void getUdasForNewSubscription_Successful() throws Exception {
        // given
        String supplierId = "SP";
        VOUdaDefinition subUda = ManageUdaDefinitionCtrlTest
                .createVoDefinition(UdaConfigurationType.USER_OPTION_MANDATORY,
                        "defaultValue", 123, UdaBean.CUSTOMER_SUBSCRIPTION,
                        "UDAID", 1);
        voDefinitions.add(subUda);

        VOUdaDefinition cusUda = ManageUdaDefinitionCtrlTest
                .createVoDefinition(UdaConfigurationType.USER_OPTION_OPTIONAL,
                        "defaultValue", 123, UdaBean.CUSTOMER, "UDAID", 1);
        voDefinitions.add(cusUda);
        VOUda voUda = new VOUda();
        voUda.setUdaDefinition(cusUda);
        voUda.setKey(12345);
        voUda.setTargetObjectKey(123);
        cusVoUdas.add(voUda);
        // when
        CustomerUdas result = customerUdasCtrl.getUdasForNewSubscription(
                supplierId, 123);
        // then
        assertEquals(1, result.getOrganizationUdaRows().size());
        assertEquals(1, result.getSubscriptionUdaRows().size());
        assertEquals(voDefinitions.get(1),
                result.getOrganizationUdaRows().get(0).getUdaDefinition());
        assertEquals(voDefinitions.get(0),
                result.getSubscriptionUdaRows().get(0).getUdaDefinition());
        verify(delegate, times(1)).getUdaDefinitionsForCustomer(spID.capture());
        assertEquals(supplierId, spID.getValue());
        verify(delegate, times(1)).getUdasForCustomer(type.capture(),
                anyLong(), spID.capture());
        assertEquals(supplierId, spID.getValue());
        assertEquals(UdaBean.CUSTOMER, type.getValue());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUdasForExistingSubscription_InValidID() throws Exception {
        // given
        String supplierId = "no such SP ID";
        doThrow(new ObjectNotFoundException()).when(delegate)
                .getUdaDefinitionsForCustomer(eq(supplierId));
        // when
        customerUdasCtrl.getUdasForExistingSubscription(supplierId, 0, 0);
        // then
    }

    @Test
    public void getUdasForExistingSubscription_NoOragnizationUDADefinition()
            throws Exception {
        // given
        String supplierId = "SP";

        VOUdaDefinition cusUda = ManageUdaDefinitionCtrlTest
                .createVoDefinition(UdaConfigurationType.USER_OPTION_OPTIONAL,
                        "defaultValue", 123, UdaBean.CUSTOMER_SUBSCRIPTION,
                        "UDAID", 1);
        voDefinitions.add(cusUda);
        VOUda voUda = new VOUda();
        voUda.setUdaDefinition(cusUda);
        voUda.setKey(12345);
        voUda.setTargetObjectKey(123);
        voUda.setUdaValue("default");
        subVoUdas.add(voUda);
        // when
        CustomerUdas result = customerUdasCtrl.getUdasForExistingSubscription(
                supplierId, 123, 123);
        // then
        assertEquals(0, result.getOrganizationUdaRows().size());
        assertEquals(1, result.getSubscriptionUdaRows().size());
        assertEquals("default", result.getSubscriptionUdaRows().get(0).getUda()
                .getUdaValue());
        assertEquals(UdaBean.CUSTOMER_SUBSCRIPTION, result
                .getSubscriptionUdaRows().get(0).getUdaDefinition()
                .getTargetType());

    }

    @Test
    public void getUdasForExistingSubscription_Successful() throws Exception {
        // given
        String supplierId = "SP";
        VOUdaDefinition subUda = ManageUdaDefinitionCtrlTest
                .createVoDefinition(UdaConfigurationType.USER_OPTION_OPTIONAL,
                        "defaultValue", 123, UdaBean.CUSTOMER_SUBSCRIPTION,
                        "UDAID", 1);
        voDefinitions.add(subUda);
        VOUda voUda = new VOUda();
        voUda.setUdaDefinition(subUda);
        voUda.setKey(12345);
        voUda.setTargetObjectKey(123);
        voUda.setUdaValue("default");
        subVoUdas.add(voUda);

        VOUdaDefinition cusUda = ManageUdaDefinitionCtrlTest
                .createVoDefinition(UdaConfigurationType.USER_OPTION_OPTIONAL,
                        "defaultValue", 123, UdaBean.CUSTOMER, "UDAID", 1);
        voDefinitions.add(cusUda);
        voUda.setUdaDefinition(cusUda);
        cusVoUdas.add(voUda);
        // when
        CustomerUdas result = customerUdasCtrl.getUdasForExistingSubscription(
                supplierId, 123, 123);
        // then
        assertEquals(1, result.getOrganizationUdaRows().size());
        assertEquals(1, result.getSubscriptionUdaRows().size());
        assertEquals(voDefinitions.get(0),
                result.getSubscriptionUdaRows().get(0).getUdaDefinition());
        assertEquals(voDefinitions.get(1),
                result.getOrganizationUdaRows().get(0).getUdaDefinition());
        verify(delegate, times(1)).getUdaDefinitionsForCustomer(spID.capture());
        assertEquals(supplierId, spID.getValue());
        verify(delegate, times(2)).getUdasForCustomer(type.capture(),
                anyLong(), spID.capture());
    }
}
