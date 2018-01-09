/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Nov 4, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 4, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage.Severity;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.*;
import org.oscm.internal.partnerservice.PartnerService;
import org.oscm.internal.subscriptions.POSubscriptionAndCustomer;
import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.vo.*;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceInternalBean;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalCustomerPriceModelCtrl;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalServicePriceModelCtrl;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalSubscriptionPriceModelCtrl;
import org.oscm.ui.model.BPLazyDataModel;
import org.oscm.ui.model.Organization;
import org.oscm.ui.model.Service;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.HttpServletRequestStub;

/**
 * Unit tests for the PriceModelBean.
 */
public class PriceModelBeanTest {

    private static final String SUB_ID = "subId";
    private static final String CUST_ID = "custId";
    private static final String CUST_NAME = "custName";
    private static final String SERVICE_ID = "serviceId";
    private static final String SUB_ID_1 = "subId1";
    private static final String SUB_ID_2 = "subId2";
    private static final String COLUMN_CUSTOMER_NAME = "customerName";
    private static final String COLUMN_CUSTOMER_ID = "customerId";
    private static final String COLUMN_SUBSCRIPTION_ID = "subscriptionId";
    private static final String COLUMN_ACTIVATION = "activation";
    private static final String COLUMN_SERVICE_ID = "serviceId";
    private static final String COLUMN_SERVICE_NAME = "serviceName";
    private static final String COLUMN_TKEY = "tkey";
    private final static int PRICEMODEL_FOR_SUBSCRIPTION = 3;
    private static long ACTIVATION_DATE = 1383844091182L;
    private static ServiceProvisioningServiceInternal provisioningServiceInternal;
    private static ServiceProvisioningService provisioningService;
    private Response response;
    private SubscriptionsService subscriptionsService;
    private PriceModelBean bean;
    private SessionBean sessionBean;
    private List<Service> selectedServiceList;
    private VOServiceDetails vOServiceDetails;
    private Organization mockedCustomer;
    private ApplicationBean appBean;
    private BPLazyDataModel model;
    private PartnerService parterService;
    private UiDelegate uiMock;
    private AccountService accountService;
    private ExternalServicePriceModelCtrl externalServicePriceModelCtrl;
    private ExternalCustomerPriceModelCtrl externalCustomerPriceModelCtrl;
    private ExternalSubscriptionPriceModelCtrl externalSubscriptionPriceModelCtrl;

    private List<POSubscriptionAndCustomer> subscriptionAndCustomers = new ArrayList<>();
    private VOPriceModel voPriceModel;

    @Before
    public void setup() throws Exception {
        VOService voService = new VOService();
        voPriceModel = spy(new VOPriceModel());
        voPriceModel.setKey(10000);
        voService.setPriceModel(voPriceModel);
        Service service = new Service(voService);
        service.setSelected(true);
        selectedServiceList = Collections.singletonList(service);
        vOServiceDetails = prepareVOServiceDetails(voService);

        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setOrganizationId("123");
        mockedCustomer = new Organization(voOrganization);
        mockedCustomer.setName("Name");

        provisioningServiceInternal = spy(
                new ServiceProvisioningServiceInternalBean());
        provisioningService = spy(new ServiceProvisioningServiceBean() {
            @Override
            public void deleteService(VOService product)
                    throws OrganizationAuthoritiesException,
                    ObjectNotFoundException, ServiceOperationException,
                    ServiceStateException {
                // throw exception to simulate concurrent deletion
                throw new ObjectNotFoundException();
            }

            @Override
            public VOServiceDetails savePriceModelForCustomer(
                    VOServiceDetails productDetails, VOPriceModel priceModel,
                    VOOrganization customer)
                    throws OrganizationAuthoritiesException,
                    ObjectNotFoundException, OperationNotPermittedException,
                    CurrencyException, ValidationException,
                    ServiceStateException, ServiceOperationException {
                productDetails.setPriceModel(priceModel);
                return productDetails;
            }

            @Override
            public VOServiceDetails getServiceDetails(VOService product)
                    throws ObjectNotFoundException,
                    org.oscm.internal.types.exception.OperationNotPermittedException {
                if (product.getKey() == vOServiceDetails.getKey()) {
                    return vOServiceDetails;
                }
                return new VOServiceDetails();

            };

            @Override
            public List<VOService> getSuppliedServices(
                    PerformanceHint performanceHint) {
                return null;
            }
        });
        when(((ServiceProvisioningServiceInternalBean) provisioningServiceInternal)
                .getServiceProvisioningServiceBean())
                        .thenReturn(provisioningService);
        bean = spy(new PriceModelBean() {

            private static final long serialVersionUID = -7750160443293826170L;
            private final HttpServletRequest request = new HttpServletRequestStub();

            @Override
            protected void init() {
            }

            @Override
            protected void addMessage(String clientId, Severity severity,
                    String key, Object[] params) {
            }

            @Override
            protected HttpServletRequest getRequest() {
                return request;
            }

            @Override
            protected SubscriptionService getSubscriptionService() {
                SubscriptionService subMock = mock(SubscriptionService.class);
                return subMock;
            }

            @Override
            protected MarketplaceService getMarketplaceService() {
                return new MarketplaceServiceStub() {
                    @Override
                    public List<VOCatalogEntry> getMarketplacesForService(
                            VOService service) throws ObjectNotFoundException,
                            OperationNotPermittedException {
                        return null;
                    }
                };
            }

            @Override
            protected ServiceProvisioningServiceInternal getProvisioningServiceInternal() {
                return PriceModelBeanTest.provisioningServiceInternal;
            }
        });

        subscriptionsService = mock(SubscriptionsService.class);
        subscriptionAndCustomers = givenPOSubscriptionAndCustomersList();
        subscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID));
        response = new Response(subscriptionAndCustomers);
        when(subscriptionsService.getSubscriptionsAndCustomersForManagers())
                .thenReturn(response);

        uiMock = mock(UiDelegate.class);
        bean.ui = uiMock;
        sessionBean = spy(new SessionBean());
        sessionBean.setSelectedCustomerId(CUST_ID);
        sessionBean.setSelectedSubscriptionId(SUB_ID);
        bean.setSessionBean(sessionBean);
        doReturn(provisioningService).when(bean).getProvisioningService();
        doReturn(selectedServiceList).when(bean).getServices();
        doReturn(mockedCustomer).when(bean).getCustomer();
        doReturn(subscriptionsService).when(bean).getSubscriptionsService();
        appBean = spy(new ApplicationBean() {
            @Override
            protected VOUserDetails getUserFromSessionWithoutException() {
                return new VOUserDetails();
            }
        });
        bean.setAppBean(appBean);
        doReturn("GMT").when(appBean).getTimeZoneId();

        model = spy(new BPLazyDataModel());
        bean.setModel(model);

        externalServicePriceModelCtrl = mock(
                ExternalServicePriceModelCtrl.class);
        externalCustomerPriceModelCtrl = mock(
                ExternalCustomerPriceModelCtrl.class);
        externalSubscriptionPriceModelCtrl = mock(
                ExternalSubscriptionPriceModelCtrl.class);
        bean.setExtServiceBean(externalServicePriceModelCtrl);
        bean.setExtCustBean(externalCustomerPriceModelCtrl);
        bean.setExtSubBean(externalSubscriptionPriceModelCtrl);

        parterService = mock(PartnerService.class);
        doReturn(parterService).when(bean).getParterService();
        accountService = mock(AccountService.class);
        doReturn(accountService).when(bean).getAccountingService();
    }

    @Test
    public void isEditDisabledInSubscriptionPage_SubscriptionPage() {
        doReturn(Integer.valueOf(3)).when(bean).getCurrentPMPage();

        boolean result = bean.isEditDisabledInSubscriptionPage();
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isEditDisabledInSubscriptionPage_NotSubscriptionPage() {
        doReturn(Integer.valueOf(2)).when(bean).getCurrentPMPage();

        boolean result = bean.isEditDisabledInSubscriptionPage();
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void testGetServiceNameToDisplay_NoSelectedService() {
        new FacesContextStub(Locale.ENGLISH);

        bean.setSelectedService(null);
        String result = bean.getSelectedServiceNameToDisplay();
        assertNull(result);
    }

    @Test
    public void testGetServiceNameToDisplay_NullName() {
        new FacesContextStub(Locale.ENGLISH);

        VOServiceDetails details = new VOServiceDetails();
        details.setName(null);
        bean.setSelectedService(details);

        String result = bean.getSelectedServiceNameToDisplay();
        assertEquals(JSFUtils.getText("marketplace.name.undefined", null),
                result);
    }

    @Test
    public void testGetServiceNameToDisplay_EmptyName() {
        new FacesContextStub(Locale.ENGLISH);

        VOServiceDetails details = new VOServiceDetails();
        details.setName("");
        bean.setSelectedService(details);

        String result = bean.getSelectedServiceNameToDisplay();
        assertEquals(JSFUtils.getText("marketplace.name.undefined", null),
                result);
    }

    @Test
    public void testGetServiceNameToDisplay_NotEmptyName() {
        new FacesContextStub(Locale.ENGLISH);

        VOServiceDetails details = new VOServiceDetails();
        details.setName("detail1");
        bean.setSelectedService(details);

        String result = bean.getSelectedServiceNameToDisplay();
        assertEquals(details.getName(), result);
    }

    @Test
    public void isEditDisabled() {
        // given
        bean.setSelectedServiceKey(Long.valueOf(-1));

        // when
        bean.updatePriceModel();

        // than
        assertTrue(bean.isEditDisabled());
    }

    @Test
    public void isEditDisabled_ProperService() {
        // given
        bean.setSelectedServiceKey(Long.valueOf(0));
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_SERVICE);

        // when
        bean.updatePriceModel();

        // than
        assertFalse(bean.isEditDisabled());
    }

    @Test
    public void isDisabledSave_NullSubscription() {
        // given
        bean.setCurrentPMPage(PRICEMODEL_FOR_SUBSCRIPTION);

        // when
        bean.isDisableSave();

        // then
        verify(bean, never()).getCustomers();
    }

    @Test
    public void isDisabledSave_SubscriptionsExist() {
        // given
        bean.setCurrentPMPage(PRICEMODEL_FOR_SUBSCRIPTION);

        // when
        bean.isDisableSave();

        // then
        verify(bean, never()).getCustomers();
    }

    @Test
    public void updatePriceModel_Bug10641() {
        // given
        bean.setSelectedServiceKey(Long.valueOf(0));
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_SERVICE);

        // when
        bean.updatePriceModel();
        bean.getParameters().get(0).getPricedParameter()
                .getRoleSpecificUserPrices().add(new VOPricedRole());
        // then
        verify(bean, times(1)).initParametersRolesForNotExistingParameter();
        assertEquals(bean.getParameters().size(),
                bean.getParametersRoles().size());
        assertEquals(1, bean.getParametersRoles().get(0).getPricedParameter()
                .getRoleSpecificUserPrices().size());
    }

    @Test
    public void updateVOServiceDetails_Bug10257() throws Exception {
        // given
        bean.setCustomerID(null);
        bean.setSelectedServiceKey(0L);
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_CUSTOMER);

        // when
        bean.updateVOServiceDetails();

        // then
        assertNotNull(null, bean.getSelectedService().getPriceModel());
    }

    @Test
    public void updateVOServiceDetails_Bug10700() throws Exception {
        // given
        bean.setSelectedServiceKey(Long.valueOf(0));
        doNothing().when(bean.ui)
                .handleException(any(ObjectNotFoundException.class));
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_SERVICE);
        doReturn(null).when(provisioningService)
                .getServiceDetails(any(VOServiceDetails.class));

        // when
        bean.updateVOServiceDetails();

        // then
        verify(bean, times(1)).initServices();
        verify(bean.ui, times(1))
                .handleException(any(SaaSApplicationException.class));

    }

    @Test
    public void save_Bug10521() throws Exception {
        // given
        prepareForSave();
        bean.updatePriceModel();
        bean.setSelectedRole(bean.getRoles().get(0));
        bean.getParametersSelectedRole()[1].getPricedOption()
                .setPricePerUser(BigDecimal.valueOf(1));
        bean.saveRoles();
        // when
        bean.save();
        // then
        assertEquals(bean.getPriceModel().getSelectedParameters().get(0)
                .getPricedOptions().get(0).getRoleSpecificUserPrices().get(0)
                .getPricePerUser(), BigDecimal.valueOf(1));
    }

    @Test
    public void save_Bug12158() throws Exception {
        // given
        bean.setCustomerID(null);
        bean.setSelectedServiceKey(Long.valueOf(0));
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_SERVICE);
        bean.setRoles(null);
        bean.setServices(this.selectedServiceList);
        doReturn(new ArrayList<String>()).when(bean).getSupportedCurrencies();
        doReturn(new VOPriceModelLocalization()).when(bean).getLocalization();
        bean.updatePriceModel();
        bean.setSelectedRole(bean.getRoles().get(0));
        bean.getParametersSelectedRole()[1].getPricedOption()
                .setPricePerUser(BigDecimal.valueOf(1));
        bean.saveRoles();
        bean.setLocalization(mock(VOPriceModelLocalization.class));
        bean.provisioningService = provisioningService;

        doReturn(bean.getSelectedService()).when(provisioningService)
                .savePriceModel(any(VOServiceDetails.class),
                        any(VOPriceModel.class));
        doNothing().when(provisioningService).savePriceModelLocalization(
                any(VOPriceModel.class), any(VOPriceModelLocalization.class));
        // when
        bean.save();
        // then
        verify(provisioningService, atLeastOnce()).savePriceModelLocalization(
                any(VOPriceModel.class), any(VOPriceModelLocalization.class));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void save_service_VOServiceDetails_NULL() throws Exception {
        // given
        doReturn(null).when(bean).getSelectedService();
        // when
        try {
            bean.save();
            fail();
        } catch (ObjectNotFoundException e) {
            assertEquals(ClassEnum.SERVICE, e.getDomainObjectClassEnum());
            throw e;
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void save_subscription_VOServiceDetails_NULL() throws Exception {
        // given
        doReturn(new Integer(PriceModelBean.PRICEMODEL_FOR_SUBSCRIPTION))
                .when(bean).getCurrentPMPage();
        doReturn(null).when(bean).getSelectedService();
        // when
        try {
            bean.save();
            fail();
        } catch (ObjectNotFoundException e) {
            assertEquals(ClassEnum.SUBSCRIPTION, e.getDomainObjectClassEnum());
            throw e;
        }

    }

    @Test(expected = ServiceStateException.class)
    public void save_service_ServiceStateException() throws Exception {
        // given
        prepareForSave();
        bean.setSelectedService(vOServiceDetails);

        doReturn(null).when(bean).getRoleSpecificUserPricesForSaving();
        doNothing().when(bean).addRoleSpecificPriceToParameters();
        doThrow(new ServiceStateException()).when(provisioningService)
                .savePriceModelForCustomer(any(VOServiceDetails.class),
                        any(VOPriceModel.class), any(VOOrganization.class));
        try {
            // when
            bean.save();
            // then
            fail();
        } catch (ServiceStateException e) {
            throw e;
        }

    }

    @Test(expected = SubscriptionStateException.class)
    public void save_subscription_SubscriptionStateException()
            throws Exception {
        // given
        prepareForSave();

        bean.setSelectedService(vOServiceDetails);
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_SUBSCRIPTION);
        bean.setSelectedServiceKey(null);
        bean.setCustomerID(CUST_ID);
        bean.setSubscriptionID(SUB_ID);

        doReturn(null).when(bean).getRoleSpecificUserPricesForSaving();
        doNothing().when(bean).addRoleSpecificPriceToParameters();

        doThrow(new SubscriptionStateException()).when(provisioningService)
                .savePriceModelForSubscription(any(VOServiceDetails.class),
                        any(VOPriceModel.class));
        doReturn(vOServiceDetails).when(provisioningService)
                .getServiceForSubscription(any(VOOrganization.class),
                        anyString());
        // when
        try {
            bean.save();
            fail();
        } catch (SubscriptionStateException e) {
            throw e;
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void save_service_ObjectNotFoundException() throws Exception {
        // given
        prepareForSave();
        ObjectNotFoundException ex = new ObjectNotFoundException(
                ClassEnum.SERVICE, "test");
        doThrow(ex).when(provisioningService).savePriceModelForSubscription(
                any(VOServiceDetails.class), any(VOPriceModel.class));

        // when
        try {
            bean.save();
            // then
            fail();
        } catch (ObjectNotFoundException e) {
            assertEquals(ClassEnum.SERVICE, e.getDomainObjectClassEnum());
            throw e;
        }
    }

    @Test
    public void initSubscriptions() throws Exception {
        POSubscriptionAndCustomer subscriptionAndCustomer = getPOSubscriptionAndCustomer(
                SUB_ID);
        doReturn(subscriptionAndCustomer).when(model)
                .getSelectedSubscriptionAndCustomer();

        // when
        bean.initSubscriptions();

        // then
        verify(bean, times(1)).setSelectedSubscription(subscriptionAndCustomer);
        verify(bean, times(1)).updatePriceModel();
        assertEquals(SUB_ID, sessionBean.getSelectedSubscriptionId());
        assertEquals(CUST_ID, sessionBean.getSelectedCustomerId());
        assertEquals(SUB_ID, bean.getSubscriptionID());
        assertEquals(CUST_ID, bean.getCustomerID());
    }

    @Test
    public void setSelectedSubscriptionAndCustomer() {
        // when
        bean.setSelectedSubscription(subscriptionAndCustomers.get(0));

        // then
        assertEquals(SUB_ID_1, sessionBean.getSelectedSubscriptionId());
        assertEquals(CUST_ID, sessionBean.getSelectedCustomerId());
        assertEquals(SUB_ID_1, bean.getSubscriptionID());
        assertEquals(CUST_ID, bean.getCustomerID());

    }

    @Test
    public void getDataTableHeaders() throws Exception {
        // when
        List<String> result = bean.getDataTableHeaders();
        System.out.println(result);
        // then
        assertEquals(7, result.size());
        assertEquals(COLUMN_CUSTOMER_NAME, result.get(0));
        assertEquals(COLUMN_CUSTOMER_ID, result.get(1));
        assertEquals(COLUMN_SUBSCRIPTION_ID, result.get(2));
        assertEquals(COLUMN_ACTIVATION, result.get(3));
        assertEquals(COLUMN_SERVICE_ID, result.get(4));
        assertEquals(COLUMN_SERVICE_NAME, result.get(5));
        assertEquals(COLUMN_TKEY, result.get(6));

    }

    @Test
    public void selectSubscriptionIdAndCustomerId() throws Exception {
        subscriptionAndCustomers = givenPOSubscriptionAndCustomersList();
        doReturn(subscriptionAndCustomers).when(model).getCachedList();

        bean.setSubscriptionId(SUB_ID_1);
        bean.setCustomerId(CUST_ID);
        bean.selectSubscriptionIdAndCustomerId();

        verify(bean, times(1))
                .setSelectedSubscription(any(POSubscriptionAndCustomer.class));
        verify(bean, times(1)).updatePriceModel();
        assertEquals(Boolean.FALSE, Boolean.valueOf(bean.isDirty()));
    }

    @Test
    public void selectSubscriptionIdAndCustomerId_Null() throws Exception {

        bean.setSubscriptionId(null);
        bean.setCustomerId(null);
        bean.selectSubscriptionIdAndCustomerId();

        verify(bean, never())
                .setSelectedSubscription(any(POSubscriptionAndCustomer.class));
    }

    @Test
    public void getLocalization()
            throws ObjectNotFoundException, OperationNotPermittedException {
        // given
        VOServiceDetails details = new VOServiceDetails();
        details.setName("test");
        bean.setSelectedService(details);

        VOPriceModel voPriceModel = new VOPriceModel();
        voPriceModel.setKey(10000);
        doReturn(voPriceModel).when(bean).getPriceModel();

        Response r = mock(Response.class);
        doReturn(r).when(parterService)
                .getPriceModelLocalization(any(VOServiceDetails.class));

        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        List<VOLocalizedText> voDescrptionLocalizedText = new ArrayList<VOLocalizedText>();
        voDescrptionLocalizedText.add(new VOLocalizedText("en", "descrption"));
        localization.setDescriptions(voDescrptionLocalizedText);
        List<VOLocalizedText> voLicenceLocalizedText = new ArrayList<VOLocalizedText>();
        voLicenceLocalizedText.add(new VOLocalizedText("en", "Licence"));
        localization.setLicenses(voLicenceLocalizedText);
        doReturn(localization).when(r)
                .getResult(VOPriceModelLocalization.class);

        List<Locale> localeList = new ArrayList<Locale>();
        localeList.add(Locale.ENGLISH);
        localeList.add(Locale.GERMAN);
        doReturn(localeList).when(appBean).getSupportedLocaleList();

        // when
        localization = bean.getLocalization();

        // when
        assertEquals(localization.getDescriptions().get(1).getLocale(), "de");
        assertEquals(localization.getLicenses().get(1).getLocale(), "de");
    }

    @Test
    public void reloadPriceModel_Null() throws Exception {
        // given
        ValueChangeEvent event = prepareForReloadPriceModel();
        doReturn(null).when(event).getNewValue();
        doNothing().when(externalCustomerPriceModelCtrl)
                .reloadPriceModel(any(VOServiceDetails.class));
        // when
        bean.reloadPriceModel(event);

        // then
        assertNull(bean.getSelectedServiceKey());
        assertNull(sessionBean.getSelectedServiceKeyForSupplier());
    }

    @Test
    public void reloadPriceModel() throws Exception {
        // given
        ValueChangeEvent event = prepareForReloadPriceModel();
        doNothing().when(externalCustomerPriceModelCtrl)
                .reloadPriceModel(any(VOServiceDetails.class));
        // when
        bean.reloadPriceModel(event);

        // then
        assertEquals(new Long(11000), bean.getSelectedServiceKey());
        assertEquals(new Long(11000),
                sessionBean.getSelectedServiceKeyForSupplier());
    }

    @Test
    public void getCustomers_ok() throws Exception {
        // given
        List<VOOrganization> orgs = new ArrayList<VOOrganization>();
        VOOrganization org = new VOOrganization();
        org.setName("Name");
        org.setOrganizationId("organizationId");
        orgs.add(org);
        doReturn(orgs).when(accountService).getMyCustomersOptimization();
        // when
        List<Organization> result = bean.getCustomers();
        // then
        assertEquals(orgs.size(), result.size());
        assertEquals(orgs.get(0).getName(), result.get(0).getName());
        assertEquals(orgs.get(0).getOrganizationId(),
                result.get(0).getOrganizationId());
    }

    @Test
    public void testValidateSubscription() throws SaaSApplicationException {
        // given
        doReturn(new VOSubscriptionDetails()).when(provisioningService)
                .validateSubscription(any(VOService.class));

        // when
        bean.validateSubscription(any(VOService.class));

        // then
        verify(provisioningService, times(1))
                .validateSubscription(any(VOService.class));
    }

    @Test
    public void testUpload_forServicePage() throws SaaSApplicationException {
        // given
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_SERVICE);

        // when
        bean.upload();

        // then
        verify(externalServicePriceModelCtrl)
                .upload(any(VOServiceDetails.class));
        assertTrue(bean.isDirty());
    }

    @Test
    public void testUpload_forSubscriptionPage()
            throws SaaSApplicationException {
        // given
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_SUBSCRIPTION);

        // when
        bean.upload();

        // then
        verify(externalSubscriptionPriceModelCtrl)
                .upload(any(VOSubscriptionDetails.class));
        assertTrue(bean.isDirty());
    }

    @Test
    public void testUpload_forCustomerPage() throws SaaSApplicationException {
        // given
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_CUSTOMER);

        // when
        bean.upload();

        // then
        verify(externalCustomerPriceModelCtrl)
                .upload(any(VOServiceDetails.class), any(VOOrganization.class));
        assertTrue(bean.isDirty());
    }

    @Test
    public void testDisplay_forServicePage()
            throws SaaSApplicationException, IOException {
        // given
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_SERVICE);

        // when
        bean.display();

        // then
        verify(externalServicePriceModelCtrl).display();
    }

    @Test
    public void testDisplay_forSubscriptionPage()
            throws SaaSApplicationException, IOException {
        // given
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_SUBSCRIPTION);
        // when
        bean.display();

        // then
        assertFalse(bean.isDirty());
    }

    @Test
    public void testDisplay_forCustomerPage()
            throws SaaSApplicationException, IOException {
        // given
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_CUSTOMER);

        // when
        bean.display();

        // then
        verify(externalCustomerPriceModelCtrl).display();
    }

    @Test
    public void testValidateSubscriptionWithExternalPriceMdel_nullSubscription()
            throws SaaSApplicationException {
        // given
        VOSubscriptionDetails subscription = null;
        // when
        VOSubscriptionDetails result = bean.validateSubscription(subscription);
        // then
        assertTrue(result == null);
    }

    @Test
    public void testValidateSubscriptionWithExternalPriceMdel_nullPriceModel()
            throws SaaSApplicationException {
        // given
        VOSubscriptionDetails subscription = new VOSubscriptionDetails();
        subscription.setPriceModel(null);
        // when
        VOSubscriptionDetails result = bean.validateSubscription(subscription);
        // then
        assertTrue(result == null);
    }

    @Test
    public void testValidateSubscriptionWithExternalPriceMdel_notExtPriceModel()
            throws SaaSApplicationException {
        // given
        VOSubscriptionDetails subscription = new VOSubscriptionDetails();
        VOPriceModel voPriceModel = new VOPriceModel();
        voPriceModel.setExternal(false);
        subscription.setPriceModel(voPriceModel);
        // when
        VOSubscriptionDetails result = bean.validateSubscription(subscription);
        // then
        assertTrue(result == null);
    }

    private ValueChangeEvent prepareForReloadPriceModel() {
        doNothing().when(bean).updatePriceModel();
        ValueChangeEvent event = mock(ValueChangeEvent.class);
        doReturn(new Long("11000")).when(event).getNewValue();
        bean.setServices(this.selectedServiceList);
        return event;
    }

    private List<POSubscriptionAndCustomer> givenPOSubscriptionAndCustomersList() {
        List<POSubscriptionAndCustomer> poSubscriptionAndCustomers = new ArrayList<POSubscriptionAndCustomer>();
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID_1));
        poSubscriptionAndCustomers.add(getPOSubscriptionAndCustomer(SUB_ID_2));
        return poSubscriptionAndCustomers;
    }

    private POSubscriptionAndCustomer getPOSubscriptionAndCustomer(
            String subscriptionId) {
        POSubscriptionAndCustomer poSubscriptionAndCustomer = new POSubscriptionAndCustomer();
        poSubscriptionAndCustomer.setCustomerId(CUST_ID);
        poSubscriptionAndCustomer.setCustomerName(CUST_NAME);
        poSubscriptionAndCustomer.setServiceId(SERVICE_ID);
        poSubscriptionAndCustomer.setSubscriptionId(subscriptionId);
        poSubscriptionAndCustomer
                .setActivation(String.valueOf(ACTIVATION_DATE));
        return poSubscriptionAndCustomer;
    }

    private VOServiceDetails prepareVOServiceDetails(VOService product) {
        VOServiceDetails serviceDetails = new VOServiceDetails();
        serviceDetails.setKey(product.getKey());
        serviceDetails.setAccessType(product.getAccessType());
        serviceDetails.setName(product.getName());
        serviceDetails.setTechnicalId(product.getTechnicalId());
        serviceDetails.setParameters(prepareVOParameters());
        serviceDetails.setPriceModel(prepareVOPriceModel());
        serviceDetails.setTechnicalService(prepareVOTechnicalService());
        return serviceDetails;
    }

    private VOTechnicalService prepareVOTechnicalService() {
        VOTechnicalService technicalService = new VOTechnicalService();
        technicalService.setRoleDefinitions(prepareVORoleDefinitions());
        return technicalService;
    }

    private VOPriceModel prepareVOPriceModel() {
        VOPriceModel voPriceModel = new VOPriceModel();
        voPriceModel.setKey(10000);
        voPriceModel.setSelectedParameters(prepareVOPricedParameter());
        voPriceModel.setRoleSpecificUserPrices(prepareRoleSpecificUserPrice());
        return voPriceModel;
    }

    private List<VOPricedOption> prepareVOPricedOptions() {
        List<VOPricedOption> pricedOptions = new ArrayList<VOPricedOption>();
        VOPricedOption vOPricedOption = new VOPricedOption();
        vOPricedOption.setKey(0);
        vOPricedOption.setPricePerUser(BigDecimal.valueOf(0));
        vOPricedOption
                .setRoleSpecificUserPrices(prepareRoleSpecificUserPrice());
        pricedOptions.add(vOPricedOption);
        return pricedOptions;

    }

    private List<VOPricedParameter> prepareVOPricedParameter() {
        List<VOPricedParameter> selectedParameters = new ArrayList<VOPricedParameter>();
        VOPricedParameter vOPricedParameter = new VOPricedParameter();
        vOPricedParameter.setParameterKey(0);
        vOPricedParameter.setPricedOptions(prepareVOPricedOptions());
        selectedParameters.add(vOPricedParameter);
        return selectedParameters;
    }

    private List<VOPricedRole> prepareRoleSpecificUserPrice() {
        List<VOPricedRole> roleSpecificUserPrice = new ArrayList<VOPricedRole>();
        VOPricedRole vOPricedRole = new VOPricedRole();
        vOPricedRole.setRole(prepareVORoleDefinition());
        roleSpecificUserPrice.add(vOPricedRole);
        return roleSpecificUserPrice;

    }

    private List<VORoleDefinition> prepareVORoleDefinitions() {
        List<VORoleDefinition> vORoleDefinitions = new ArrayList<VORoleDefinition>();
        vORoleDefinitions.add(prepareVORoleDefinition());
        return vORoleDefinitions;
    }

    private VORoleDefinition prepareVORoleDefinition() {
        VORoleDefinition vORoleDefinition = new VORoleDefinition();
        vORoleDefinition.setRoleId("ADMIN");
        return vORoleDefinition;
    }

    private List<VOParameter> prepareVOParameters() {
        List<VOParameter> vOParameters = new ArrayList<VOParameter>();
        VOParameter vOParameter = new VOParameter();
        vOParameter.setConfigurable(true);
        vOParameter.setKey(0);
        vOParameter.setParameterDefinition(prepareVOParameterDefinition());
        vOParameters.add(vOParameter);
        return vOParameters;
    }

    private VOParameterDefinition prepareVOParameterDefinition() {
        VOParameterDefinition vOParameterDefinition = new VOParameterDefinition();
        vOParameterDefinition.setValueType(ParameterValueType.ENUMERATION);
        vOParameterDefinition.setDefaultValue("0");
        vOParameterDefinition.setParameterOptions(prepareVOParameterOptions());
        return vOParameterDefinition;
    }

    private List<VOParameterOption> prepareVOParameterOptions() {
        List<VOParameterOption> VOParameterOptions = new ArrayList<VOParameterOption>();
        VOParameterOption vOParameterOption = new VOParameterOption();
        vOParameterOption.setOptionId("0");
        vOParameterOption.setKey(0);
        VOParameterOptions.add(vOParameterOption);
        return VOParameterOptions;
    }

    private void prepareForSave() {
        bean.setCustomerID(null);
        bean.setSelectedServiceKey(Long.valueOf(0));
        bean.setCurrentPMPage(PriceModelBean.PRICEMODEL_FOR_CUSTOMER);
        bean.setRoles(null);
        bean.setServices(this.selectedServiceList);
        doReturn(new ArrayList<String>()).when(bean).getSupportedCurrencies();
        doReturn(new VOPriceModelLocalization()).when(bean).getLocalization();
    }
}
