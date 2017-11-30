/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-6-11                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.ui.beans.UdaBean;

/**
 * @author yuyin
 * 
 */
public class ManageUdaDefinitionCtrlTest {

    private ManageUdaDefinitionCtrl mngUdaDefinitionCtrl;
    private AccountService delegate;
    private final String defaultValue = "defaultValue";
    private final String udaID = "UDAID";
    private final long udaKey = 1234;
    private List<VOUdaDefinition> voDefinitions;
    @Captor
    ArgumentCaptor<List<VOUdaDefinition>> toSave;
    @Captor
    ArgumentCaptor<List<VOUdaDefinition>> toDelete;

    @Before
    public void setup() {
        delegate = mock(AccountService.class);
        voDefinitions = new ArrayList<VOUdaDefinition>();
        MockitoAnnotations.initMocks(this);

        mngUdaDefinitionCtrl = spy(new ManageUdaDefinitionCtrl(delegate));
        doReturn(voDefinitions).when(delegate).getUdaDefinitions();
    }

    @Test
    public void getModel_NoModel() throws Exception {
        // when
        ManageUdaDefinitionPage pageReslut = mngUdaDefinitionCtrl.getModel();
        // then
        assertNotNull(pageReslut);
        assertEquals(0, pageReslut.getCustomerUdas().size());
        assertEquals(0, pageReslut.getSubscriptionUdas().size());
    }

    @Test
    public void getModel_InitialModel() throws Exception {
        // give
        VOUdaDefinition voUdaDefinition = createVoDefinition(
                UdaConfigurationType.USER_OPTION_MANDATORY, "defaultValue",
                123, UdaBean.CUSTOMER, udaID, 1);
        voDefinitions.add(voUdaDefinition);
        voUdaDefinition = createVoDefinition(
                UdaConfigurationType.USER_OPTION_OPTIONAL, "defaultValue",
                udaKey, UdaBean.CUSTOMER_SUBSCRIPTION, udaID, 1);
        voDefinitions.add(voUdaDefinition);

        // when
        ManageUdaDefinitionPage pageReslut = mngUdaDefinitionCtrl.getModel();
        // then
        assertEquals(1, pageReslut.getCustomerUdas().size());
        assertEquals(123, pageReslut.getCustomerUdas().get(0).getKey());
        assertTrue(pageReslut.getCustomerUdas().get(0).isMandatory());
        assertTrue(pageReslut.getCustomerUdas().get(0).isUserOption());
        assertEquals(1, pageReslut.getSubscriptionUdas().size());
        assertEquals(udaKey, pageReslut.getSubscriptionUdas().get(0).getKey());
        assertFalse(pageReslut.getSubscriptionUdas().get(0).isMandatory());
        assertTrue(pageReslut.getSubscriptionUdas().get(0).isUserOption());
    }

    @Test
    public void getModel_SuccessfulReceiveModel() throws Exception {
        // give
        UdaDefinitionRowModel cusVoUdaDefinitionRow = createVoDefinitionRow(
                "defaultValue", 123, udaID, 1, false, true);
        UdaDefinitionRowModel subVoUdaDefinitionRow = createVoDefinitionRow(
                "defaultValue", udaKey, udaID, 1, true, true);
        ManageUdaDefinitionPage page = new ManageUdaDefinitionPage();
        List<UdaDefinitionRowModel> pageCustomer = new ArrayList<UdaDefinitionRowModel>();
        pageCustomer.add(cusVoUdaDefinitionRow);
        page.setCustomerUdas(pageCustomer);

        List<UdaDefinitionRowModel> pageSubscription = new ArrayList<UdaDefinitionRowModel>();
        pageSubscription.add(subVoUdaDefinitionRow);
        page.setSubscriptionUdas(pageSubscription);

        mngUdaDefinitionCtrl.setModel(page);
        // when
        ManageUdaDefinitionPage pageReslut = mngUdaDefinitionCtrl.getModel();
        // then
        assertEquals(1, pageReslut.getCustomerUdas().size());
        assertEquals(123, pageReslut.getCustomerUdas().get(0).getKey());
        assertEquals(1, pageReslut.getSubscriptionUdas().size());
        assertEquals(udaKey, pageReslut.getSubscriptionUdas().get(0).getKey());
        verify(delegate, never()).getUdaDefinitions();
    }

    @Test
    public void createUdaDefinition_ForSubscription() throws Exception {
        // give
        UdaDefinitionDetails defDetails = createUdaDefinitionDetails(
                defaultValue, udaKey, udaID, 1, false, true);
        ManageUdaDefinitionPage page = new ManageUdaDefinitionPage();
        page.setUdaType(UdaBean.CUSTOMER_SUBSCRIPTION);
        page.setNewUdaDefinition(defDetails);
        mngUdaDefinitionCtrl.setModel(page);
        // when
        mngUdaDefinitionCtrl.createUdaDefinition();
        // then
        verify(delegate, times(1)).saveUdaDefinitions(toSave.capture(),
                toDelete.capture());
        VOUdaDefinition voUdaDef = toSave.getValue().get(0);
        verifyUdaDefinition(voUdaDef, UdaBean.CUSTOMER_SUBSCRIPTION);
    }

    @Test
    public void createUdaDefinition_ForCustomer() throws Exception {
        // give
        UdaDefinitionDetails defDetails = createUdaDefinitionDetails(
                defaultValue, udaKey, udaID, 1, false, true);
        ManageUdaDefinitionPage page = new ManageUdaDefinitionPage();
        page.setUdaType(UdaBean.CUSTOMER);
        page.setNewUdaDefinition(defDetails);
        mngUdaDefinitionCtrl.setModel(page);
        // when
        mngUdaDefinitionCtrl.createUdaDefinition();
        // then
        verify(delegate, times(1)).saveUdaDefinitions(toSave.capture(),
                toDelete.capture());
        VOUdaDefinition voUdaDef = toSave.getValue().get(0);
        verifyUdaDefinition(voUdaDef, UdaBean.CUSTOMER);
    }

    @Test
    public void updateUdaDefinition_ForSubscription() throws Exception {
        // give
        prepareUdaDefinitionForSubscription();
        // when
        mngUdaDefinitionCtrl.updateUdaDefinition();
        // then
        verify(delegate, times(1)).saveUdaDefinitions(toSave.capture(),
                toDelete.capture());
        VOUdaDefinition voUdaDef = toSave.getValue().get(0);
        verifyUdaDefinition(voUdaDef, UdaBean.CUSTOMER_SUBSCRIPTION);
        assertEquals(1, mngUdaDefinitionCtrl.getModel().getSubscriptionUdas()
                .size());
    }

    @Test
    public void updateUdaDefinition_ForCustomer() throws Exception {
        // give
        prepareUdaDefinitionForCustomer();
        // when
        mngUdaDefinitionCtrl.updateUdaDefinition();
        // then
        verify(delegate, times(1)).saveUdaDefinitions(toSave.capture(),
                toDelete.capture());
        VOUdaDefinition voUdaDef = toSave.getValue().get(0);
        verifyUdaDefinition(voUdaDef, UdaBean.CUSTOMER);
        assertEquals(1, mngUdaDefinitionCtrl.getModel().getCustomerUdas()
                .size());
    }

    @Test
    public void deleteUdaDefinition_ForSubscription() throws Exception {
        // give
        prepareUdaDefinitionForSubscription();
        // when
        mngUdaDefinitionCtrl.deleteUdaDefinition();
        // then
        verify(delegate, times(1)).saveUdaDefinitions(toSave.capture(),
                toDelete.capture());
        VOUdaDefinition voUdaDef = toDelete.getValue().get(0);
        verifyUdaDefinition(voUdaDef, UdaBean.CUSTOMER_SUBSCRIPTION);
        assertEquals(1, mngUdaDefinitionCtrl.getModel().getSubscriptionUdas()
                .size());

    }

    @Test
    public void deleteUdaDefinition_ForCustomer() throws Exception {
        // give
        prepareUdaDefinitionForCustomer();
        // when
        mngUdaDefinitionCtrl.deleteUdaDefinition();
        // then
        verify(delegate, times(1)).saveUdaDefinitions(toSave.capture(),
                toDelete.capture());
        VOUdaDefinition voUdaDef = toDelete.getValue().get(0);
        verifyUdaDefinition(voUdaDef, UdaBean.CUSTOMER);
        assertEquals(1, mngUdaDefinitionCtrl.getModel().getCustomerUdas()
                .size());
    }

    private void prepareUdaDefinitionForSubscription() {
        UdaDefinitionRowModel udaRow = createVoDefinitionRow(defaultValue,
                udaKey, udaID, 1, false, true);
        ManageUdaDefinitionPage page = new ManageUdaDefinitionPage();
        List<UdaDefinitionRowModel> subscriptionUdas = new ArrayList<UdaDefinitionRowModel>();
        subscriptionUdas.add(udaRow);
        page.setSubscriptionUdas(subscriptionUdas);
        page.setUdaType(UdaBean.CUSTOMER_SUBSCRIPTION);
        page.setCurrentUdaIndex(0);
        mngUdaDefinitionCtrl.setModel(page);
    }

    private void prepareUdaDefinitionForCustomer() {
        UdaDefinitionRowModel udaRow = createVoDefinitionRow(defaultValue,
                udaKey, udaID, 1, false, true);
        ManageUdaDefinitionPage page = new ManageUdaDefinitionPage();
        List<UdaDefinitionRowModel> customerUdas = new ArrayList<UdaDefinitionRowModel>();
        customerUdas.add(udaRow);
        page.setCustomerUdas(customerUdas);
        page.setUdaType(UdaBean.CUSTOMER);
        page.setCurrentUdaIndex(0);
        mngUdaDefinitionCtrl.setModel(page);
    }

    private void verifyUdaDefinition(VOUdaDefinition voUdaDef, String udaType) {
        assertEquals(UdaConfigurationType.USER_OPTION_OPTIONAL,
                voUdaDef.getConfigurationType());
        assertEquals(defaultValue, voUdaDef.getDefaultValue());
        assertEquals(udaKey, voUdaDef.getKey());
        assertEquals(udaID, voUdaDef.getUdaId());
        assertEquals(udaType, voUdaDef.getTargetType());
    }

    public static VOUdaDefinition createVoDefinition(UdaConfigurationType type,
            String defaultValue, long key, String target, String id, int version) {
        VOUdaDefinition voUdaDefinition = new VOUdaDefinition();
        voUdaDefinition.setConfigurationType(type);
        voUdaDefinition.setDefaultValue(defaultValue);
        voUdaDefinition.setKey(key);
        voUdaDefinition.setTargetType(target);
        voUdaDefinition.setUdaId(id);
        voUdaDefinition.setVersion(version);
        return voUdaDefinition;
    }

    private UdaDefinitionRowModel createVoDefinitionRow(String defaultValue,
            long key, String id, int version, boolean mandatory,
            boolean userOption) {
        UdaDefinitionRowModel udaDefinitionRow = new UdaDefinitionRowModel();
        udaDefinitionRow.setDefaultValue(defaultValue);
        udaDefinitionRow.setKey(key);
        udaDefinitionRow.setUdaId(id);
        udaDefinitionRow.setVersion(version);
        udaDefinitionRow.setMandatory(mandatory);
        udaDefinitionRow.setUserOption(userOption);
        return udaDefinitionRow;
    }

    public static UdaDefinitionDetails createUdaDefinitionDetails(
            String defaultValue, long key, String id, int version,
            boolean mandatory, boolean userOption) {
        UdaDefinitionDetails udaDefinitionDetails = new UdaDefinitionDetails();
        udaDefinitionDetails.setDefaultValue(defaultValue);
        udaDefinitionDetails.setKey(key);
        udaDefinitionDetails.setUdaId(id);
        udaDefinitionDetails.setVersion(version);
        udaDefinitionDetails.setMandatory(mandatory);
        udaDefinitionDetails.setUserOption(userOption);
        return udaDefinitionDetails;
    }

}
