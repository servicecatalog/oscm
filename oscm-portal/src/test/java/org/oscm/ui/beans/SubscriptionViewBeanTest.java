/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.01.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.subscriptions.POSubscriptionAndCustomer;
import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.*;
import org.oscm.paginator.PaginationFullTextFilter;
import org.oscm.ui.model.BPLazyDataModel;
import org.oscm.ui.model.Discount;
import org.oscm.ui.model.UdaRow;
import org.oscm.ui.stubs.FacesContextStub;
import org.richfaces.model.ArrangeableState;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField;

/**
 * @author weiser
 * 
 */
public class SubscriptionViewBeanTest {

    private static final String SUB_ID = "subId";
    private static final String CUST_ID = "custId";
    private static final String CUST_NAME = "custName";
    private static final String SERVICE_ID = "serviceId";
    private static final String SUB_ID_1 = "subId1";
    private static final String SUB_ID_2 = "subId2";
    private static final String SUB_ID_3 = "subId3";

    private static long ACTIVATION_DATE = 1383844091182L;
    private SubscriptionViewBean bean;
    private SessionBean sessionBean;
    private UdaBean udaBean;
    private DiscountBean discountBean;

    private SubscriptionService ss;
    private SubscriptionsService subscriptionsService;
    private VOSubscriptionDetails sd;
    private Response response;
    private final List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();

    private Discount discount;

    private final List<VOUdaDefinition> voUdaDefinitions = new ArrayList<VOUdaDefinition>();
    private final List<VOUda> voUdas = new ArrayList<VOUda>();
    private List<POSubscriptionAndCustomer> poSubscriptionAndCustomers = new ArrayList<POSubscriptionAndCustomer>();
    private BPLazyDataModel model;

    @Captor
    ArgumentCaptor<List<VOUda>> voUdaCaptor;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        new FacesContextStub(Locale.ENGLISH) {
            @Override
            public void addMessage(String arg0, FacesMessage arg1) {
                facesMessages.add(arg1);
            }
        };
        sd = new VOSubscriptionDetails();
        sd.setPriceModel(new VOPriceModel());
        
        VOServiceDetails voServiceDetails = new VOServiceDetails();
        VOPriceModel voPriceModel = new VOPriceModel();
        voPriceModel.setExternal(false);
        voServiceDetails.setPriceModel(voPriceModel);
        
        sd.setSubscribedService(voServiceDetails);

        bean = spy(new SubscriptionViewBean());
        sessionBean = new SessionBean();
        ss = mock(SubscriptionService.class);

        when(ss.getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID))).thenReturn(sd);
        subscriptionsService = mock(SubscriptionsService.class);
        poSubscriptionAndCustomers = givenPOSubscriptionAndCustomersList();
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID));
        response = new Response(poSubscriptionAndCustomers);
        when(subscriptionsService.getSubscriptionsAndCustomersForManagers()).thenReturn(response);

        udaBean = mock(UdaBean.class);
        when(udaBean.getSubscriptionUdas(anyLong())).thenReturn(new ArrayList<UdaRow>());
        when(udaBean.getForType(eq(UdaBean.CUSTOMER_SUBSCRIPTION))).thenReturn(new ArrayList<VOUdaDefinition>());

        doReturn(ss).when(bean).getSubscriptionService();

        model = spy(new BPLazyDataModel());
        bean.setModel(model);
        model.setSubscriptionsService(subscriptionsService);
        model.arrange(spy(new FacesContextStub(Locale.ENGLISH)), mock(ArrangeableState.class));


        // doNothing().when(bean.ui).handleException(
        // any(SaaSApplicationException.class));

        VODiscount voDiscount = new VODiscount();
        voDiscount.setValue(BigDecimal.valueOf(1));
        voDiscount.setStartTime(Long.valueOf(1));
        discount = new Discount(voDiscount);
        discountBean = mock(DiscountBean.class);
        when(discountBean.getDiscountForCustomer(CUST_ID)).thenReturn(discount);

        bean.setSessionBean(sessionBean);
        bean.setUdaBean(udaBean);
        bean.setDiscountBean(discountBean);

    }

    @Test
    public void getInitialize_withoutPreSelection() throws Exception {
        // given
        sessionBean.setSelectedSubscriptionId(null);
        sessionBean.setSelectedCustomerId(null);
        when(model.getCachedList()).thenReturn(poSubscriptionAndCustomers);

        // when
        bean.getInitialize();

        // then

        verify(ss, times(0)).getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID));
        assertTrue(bean.getSubscriptionsListSize() == 4);
        assertNull(bean.getSelectedSubscription());

    }

    @Test
    public void selectSubscriptionIdAndCustomerId_successful() throws Exception {

        // given
        bean.getInitialize();
        when(model.getSubscriptions()).thenReturn(poSubscriptionAndCustomers);

        // when
        bean.setCustomerId(CUST_ID);
        bean.setSubscriptionId(SUB_ID);
        bean.selectSubscriptionIdAndCustomerId();
        VOSubscriptionDetails sub = bean.getSelectedSubscription();

        // then
        verify(ss, times(1)).getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID));
        assertEquals(sub, sd);
        assertEquals(sessionBean.getSelectedCustomerId(), CUST_ID);
        assertEquals(sessionBean.getSelectedSubscriptionId(), SUB_ID);
        verifyNoMoreInteractions(ss);

    }

    // @Test
    // public void getInitialize_withPreSelection() throws Exception {
    // // given
    // sessionBean.setSelectedSubscriptionId(SUB_ID);
    // sessionBean.setSelectedCustomerId(CUST_ID);
    // bean.getInitialize();
    // when(model.getCachedList()).thenReturn(poSubscriptionAndCustomers);
    //
    // // when
    // VOSubscriptionDetails sub = bean.getSelectedSubscription();
    //
    // // then
    // assertTrue(bean.getSubscriptionsListSize() == 4);
    // assertEquals(sd, sub);
    // }

    @Test
    public void getInitialize_SelectionBeforeViewLost() throws Exception {
        // given
        sessionBean.setSelectedSubscriptionId(SUB_ID_1);
        sessionBean.setSelectedCustomerId(CUST_ID);
        model.setSubscriptionId(SUB_ID);
        model.setCustomerId(CUST_ID);

        // when
        poSubscriptionAndCustomers = givenPOSubscriptionWithoutSUB1();
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID));
        response = new Response(poSubscriptionAndCustomers);
        when(subscriptionsService.getSubscriptionsAndCustomersForManagers()).thenReturn(response);
        when(model.getCachedList()).thenReturn(poSubscriptionAndCustomers);
        bean.getInitialize();

        // then
        verify(ss, times(0)).getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID_1));
        assertTrue(bean.getSubscriptionsListSize() == 3);
        // checkReset();
    }

    @Test
    public void selectSubscriptionIdAndCustomerId_SelectionBeforeTerminatedBug10694()
            throws Exception {
        // given

        poSubscriptionAndCustomers = givenPOSubscriptionAndCustomersList();
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID));
        response = new Response(poSubscriptionAndCustomers);
        when(model.getCachedList()).thenReturn(poSubscriptionAndCustomers);
        when(subscriptionsService.getSubscriptionsAndCustomersForManagers()).thenReturn(response);
        sessionBean.setSelectedSubscriptionId(null);
        sessionBean.setSelectedCustomerId(null);
        bean.getInitialize();
        sd.setStatus(SubscriptionStatus.DEACTIVATED);
        when(ss.getSubscriptionForCustomer(CUST_ID, SUB_ID)).thenReturn(sd);

        // when
        bean.setCustomerId(CUST_ID);
        bean.setSubscriptionId(SUB_ID);
        bean.selectSubscriptionIdAndCustomerId();
        bean.getInitialize();

        // then
        verify(ss, times(1)).getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID));
        assertEquals(3, bean.getSubscriptionsListSize());
        assertEquals(model.getSubscriptions().size(), 3);
        // verify(bean.ui, times(1)).handleError(anyString(),
        // eq(BaseBean.ERROR_SUBSCRIPTION_NOT_ACCESSIBLE), eq(SUB_ID));
        // checkReset();

    }

    @Test
    public void selectSubscriptionIdAndCustomerId_SelectionBeforeSelectingLost()
            throws Exception {
        // given
        sessionBean.setSelectedSubscriptionId(null);
        sessionBean.setSelectedCustomerId(null);
        bean.getInitialize();

        when(ss.getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID))).thenThrow(new ObjectNotFoundException());
        bean.setCustomerId(CUST_ID);
        bean.setSubscriptionId(SUB_ID);
        when(model.getCachedList()).thenReturn(poSubscriptionAndCustomers);

        // when
        bean.selectSubscriptionIdAndCustomerId();

        // then
        verify(ss, times(1)).getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID));
        assertEquals(3, bean.getSubscriptionsListSize());
        verifyNoMoreInteractions(ss);
        // verify(bean.ui, times(1)).handleError(anyString(),
        // eq(BaseBean.ERROR_SUBSCRIPTION_NOT_ACCESSIBLE), eq(SUB_ID));
        // checkReset();
    }

    @Test
    public void terminateSubscription_successful() throws Exception {
        // given
        poSubscriptionAndCustomers = givenPOSubscriptionAndCustomersList();
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID));
        response = new Response(poSubscriptionAndCustomers);
        when(model.getCachedList()).thenReturn(poSubscriptionAndCustomers);
        when(subscriptionsService.getSubscriptionsAndCustomersForManagers()).thenReturn(response);
        bean.getInitialize();

        // then
        sd.setSubscriptionId(SUB_ID);
        when(ss.getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID))).thenReturn(sd);

        bean.setCustomerId(CUST_ID);
        bean.setSubscriptionId(SUB_ID);
        bean.selectSubscriptionIdAndCustomerId();
        bean.setTerminationReason("TEST");
        bean.terminateSubscription();

        // then
        verify(ss, times(1)).getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID));
        verify(ss, times(1)).terminateSubscription(any(VOSubscription.class), eq("TEST"));

        // verify(bean.ui, times(1)).showInfoMessage(anyString(),
        // eq(BaseBean.INFO_SUBSCRIPTION_TERMINATED), eq(SUB_ID));
        assertEquals(3, bean.getSubscriptionsListSize());
        verifyNoMoreInteractions(ss);

        // checkReset();
    }

    @Test
    public void terminateSubscription_failed() throws Exception {
        // given
        sessionBean.setSelectedSubscriptionId(null);
        sessionBean.setSelectedCustomerId(null);
        bean.getInitialize();
        when(model.getCachedList()).thenReturn(poSubscriptionAndCustomers);

        // when
        doThrow(new ObjectNotFoundException()).when(ss).terminateSubscription(any(VOSubscription.class), eq("TEST"));

        bean.setCustomerId(CUST_ID);
        bean.setSubscriptionId(SUB_ID);
        bean.selectSubscriptionIdAndCustomerId();
        bean.setTerminationReason("TEST");
        bean.terminateSubscription();

        // then
        verify(ss, times(1)).getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID));
        verify(ss, times(1)).terminateSubscription(any(VOSubscription.class), eq("TEST"));
        verifyNoMoreInteractions(ss);

    }

    @Test
    public void updateSubscription_FilterSubscriptionUdas() throws Exception {
        // given
        poSubscriptionAndCustomers = givenPOSubscriptionAndCustomersList();
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID));
        when(model.getSubscriptions()).thenReturn(poSubscriptionAndCustomers);
        response = new Response(poSubscriptionAndCustomers);
        when(subscriptionsService.getSubscriptionsAndCustomersForManagers()).thenReturn(response);
        // create three different type of Udas
        createUdaWithConfigurationType(UdaConfigurationType.SUPPLIER, 123);
        createUdaWithConfigurationType(UdaConfigurationType.USER_OPTION_MANDATORY, 1234);
        createUdaWithConfigurationType(UdaConfigurationType.USER_OPTION_OPTIONAL, 12345);
        List<UdaRow> subscriptionUdaRows = UdaRow.getUdaRows(voUdaDefinitions, voUdas);
        when(udaBean.getSubscriptionUdas(anyLong())).thenReturn(subscriptionUdaRows);

        bean.getInitialize();

        sd.setSubscriptionId(SUB_ID);
        when(ss.getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID))).thenReturn(sd);

        bean.setCustomerId(CUST_ID);
        bean.setSubscriptionId(SUB_ID);
        
        bean.selectSubscriptionIdAndCustomerId();

        AccountService as = mock(AccountService.class);
        bean.setAccountingService(as);
        bean.getSubscriptionUdas();

        // when
        String result = bean.updateSubscription();

        // then
        verify(as, times(1)).saveUdas(voUdaCaptor.capture());
        assertEquals(1, voUdaCaptor.getValue().size());
        assertEquals(123, voUdaCaptor.getValue().get(0).getKey());
        // verify(bean.ui, times(1)).showInfoMessage(anyString(),
        // eq(BaseBean.INFO_SUBSCRIPTION_SAVED), eq(SUB_ID));
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);

    }

    @Test
    public void initPresetSubscription_NoSubscriptionIdInSession()
            throws Exception {
        sessionBean.setSelectedCustomerId(null);
        sessionBean.setSelectedSubscriptionId(null);
        bean.setSessionBean(sessionBean);

        bean.getInitialize();
        verify(bean, never()).selectSubscriptionIdAndCustomerId();
    }

    @Test
    public void getDiscountForSelectedSubscription_NoSelectedSubscription() {
        when(model.getSelectedSubscription()).thenReturn(null);

        sessionBean.setSelectedCustomerId(null);
        sessionBean.setSelectedSubscriptionId(null);
        Discount result = bean.getDiscountForSelectedSubscription();
        assertNull(result);
    }

    @Test
    public void getDiscountForSelectedSubscription() {

        model.setSubscriptionId(SUB_ID);
        model.setCustomerId(CUST_ID);

        bean.getInitialize();

        when(model.getSelectedSubscription()).thenReturn(new VOSubscriptionDetails());

        POSubscriptionAndCustomer subscriptionAndCustomer = new POSubscriptionAndCustomer();
        subscriptionAndCustomer.setCustomerId(CUST_ID);
        when(model.getSelectedSubscriptionAndCustomer()).thenReturn(subscriptionAndCustomer);

        Discount result = bean.getDiscountForSelectedSubscription();
        assertEquals(discount.getVO().getValue(), result.getVO().getValue());
        assertEquals(discount.getVO().getStartTime(), result.getVO()
                .getStartTime());
    }


    @Test
    public void getSubscriptionsAndCustomers_OrganizationAuthoritiesException() throws Exception {
        // given
        when(subscriptionsService.getSubscriptionsAndCustomersForManagers(any(PaginationFullTextFilter.class))).thenThrow(
                new OrganizationAuthoritiesException());
        poSubscriptionAndCustomers = givenPOSubscriptionAndCustomersList();

        // when
        bean.getInitialize();
        List<FilterField> filters = Collections.emptyList();
        List<SortField> sorters = Collections.emptyList();
        model.getDataList(0, 2, filters, sorters, true);

        // then
        verify(subscriptionsService, times(1)).getSubscriptionsAndCustomersForManagers(any(PaginationFullTextFilter.class));
        assertEquals(0, bean.getSubscriptionsListSize());
    }

    @Test
    public void getSubscriptionsAndCustomers_Success() throws Exception {
        // given
        poSubscriptionAndCustomers = givenPOSubscriptionAndCustomersList();
        response = new Response(poSubscriptionAndCustomers);
        when(subscriptionsService.getSubscriptionsAndCustomersForManagers())
                .thenReturn(response);
        when(model.getCachedList()).thenReturn(poSubscriptionAndCustomers);


        // when
        bean.getInitialize();

        // then
        assertEquals(3, bean.getSubscriptionsListSize());
        assertNotNull(model.getSubscriptions());
        assertEquals(CUST_ID, poSubscriptionAndCustomers.get(0).getCustomerId());
        // assertEquals("", poSubscriptionAndCustomers.get(2).getActivation());
        // assertEquals("2013-11-07 GMT",
        // poSubscriptionAndCustomers.get(1).getActivation());
    }

    @Test
    public void getSelectedCustomerIdAndName() throws Exception {
        // given
        poSubscriptionAndCustomers = givenPOSubscriptionAndCustomersList();
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID));
        response = new Response(poSubscriptionAndCustomers);
        when(model.getSubscriptions()).thenReturn(poSubscriptionAndCustomers);
        when(subscriptionsService.getSubscriptionsAndCustomersForManagers())
                .thenReturn(response);
        bean.getInitialize();

        // when
        when(ss.getSubscriptionForCustomer(CUST_ID, SUB_ID)).thenReturn(sd);
        bean.setCustomerId(CUST_ID);
        bean.setSubscriptionId(SUB_ID);
        bean.selectSubscriptionIdAndCustomerId();
        String customerName = bean.getSelectedCustomerIdAndName();

        // then
        assertEquals(CUST_NAME, customerName);

        // when
        bean.getSelectedSubscriptionAndCustomer().setCustomerName(null);
        customerName = bean.getSelectedCustomerIdAndName();

        // then
        assertEquals("", customerName);
    }

    @Test
    public void getSelectedCustomerIdAndName_NullCustomerId() throws Exception {
        // given
        sessionBean.setSelectedCustomerId(null);

        // when
        String result = bean.getSelectedCustomerIdAndName();

        // then
        assertEquals("", result);

    }

    @Test
     public void getSubscriptionUdas() throws Exception {
        // given
        sessionBean.setSelectedSubscriptionId(null);
        sessionBean.setSelectedCustomerId(null);
        when(model.getSubscriptions()).thenReturn(poSubscriptionAndCustomers);

        // when
        sd.setKey(10);
        when(ss.getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID))).thenReturn(sd);
        bean.setCustomerId(CUST_ID);
        bean.setSubscriptionId(SUB_ID);
        bean.selectSubscriptionIdAndCustomerId();
        bean.getSubscriptionUdas();

        // then
        verify(ss).getSubscriptionForCustomer(eq(CUST_ID), eq(SUB_ID));
        verifyNoMoreInteractions(ss);
        verify(udaBean).getSubscriptionUdas(10);

    }


    @Test
    public void getSelectedCustomerIdAndName_NullSubscriptionsAndCustomers()
            throws Exception {
        // given
        List<POSubscriptionAndCustomer> subscriptionsAndCustomers = new ArrayList<POSubscriptionAndCustomer>();

        when(model.getSubscriptions()).thenReturn(subscriptionsAndCustomers);
        sessionBean.setSelectedCustomerId("customerId");

        // when
        String result = bean.getSelectedCustomerIdAndName();

        // then
        assertEquals("", result);
    }

    private List<POSubscriptionAndCustomer> givenPOSubscriptionAndCustomersList() {
        List<POSubscriptionAndCustomer> poSubscriptionAndCustomers = new ArrayList<POSubscriptionAndCustomer>();
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID_1));
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID_2));
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID_3,
                null));
        return poSubscriptionAndCustomers;
    }

    private List<POSubscriptionAndCustomer> givenPOSubscriptionWithoutSUB1() {
        List<POSubscriptionAndCustomer> poSubscriptionAndCustomers = new ArrayList<POSubscriptionAndCustomer>();
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID_2));
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID_3,
                null));
        return poSubscriptionAndCustomers;
    }

    private POSubscriptionAndCustomer getPOSubscriptionAndCustomer(
            String subscriptionId) {

        return getPOSubscriptionAndCustomer(subscriptionId,
                String.valueOf(ACTIVATION_DATE));
    }

    private POSubscriptionAndCustomer getPOSubscriptionAndCustomer(
            String subscriptionId, String activationDate) {
        POSubscriptionAndCustomer poSubscriptionAndCustomer = new POSubscriptionAndCustomer();
        poSubscriptionAndCustomer.setCustomerId(CUST_ID);
        poSubscriptionAndCustomer.setCustomerName(CUST_NAME);
        poSubscriptionAndCustomer.setServiceId(SERVICE_ID);
        poSubscriptionAndCustomer.setSubscriptionId(subscriptionId);
        poSubscriptionAndCustomer.setActivation(activationDate);
        return poSubscriptionAndCustomer;
    }

    private void createUdaWithConfigurationType(UdaConfigurationType type,
            long key) {
        VOUdaDefinition voUdaDefinition = new VOUdaDefinition();
        VOUda voUda = new VOUda();

        voUdaDefinition.setConfigurationType(type);
        voUdaDefinition.setKey(key);
        voUda.setUdaDefinition(voUdaDefinition);
        voUda.setKey(key);
        voUdas.add(voUda);
        voUdaDefinitions.add(voUdaDefinition);
    }

    /**
     * check if the methode Reset was called
     */
    private void checkReset() {
        assertNull(bean.getCustomerId());
        assertNull(bean.getSubscriptionId());
        assertNull(bean.getSelectedSubscriptionAndCustomer());
        assertNull(bean.getSelectedSubscription());
        assertNull(sessionBean.getSelectedCustomerId());
        assertNull(sessionBean.getSelectedSubscriptionId());
    }
}
