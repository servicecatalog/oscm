/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: kulle                                                   
 *                                                                              
 *  Creation Date: 13.12.2011                                                      
 *                                                                              
 *  Completion Time: 13.12.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.TSXMLForWebService;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.converter.api.VOConverter;
import org.oscm.intf.AccountService;
import org.oscm.intf.EventService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.intf.SubscriptionService;
import org.oscm.types.enumtypes.PaymentInfoType;
import org.oscm.types.exceptions.DuplicateEventException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOGatheredEvent;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationPaymentConfiguration;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTechnicalService;
import org.oscm.vo.VOUserDetails;

/**
 * Tests the EventService web-service.
 * 
 * @author kulle
 */
@Ignore
public class EventServiceWSTest {

    private static ServiceProvisioningService serviceProvisioningService;
    private static MarketplaceService srvMarketplace;
    private static SubscriptionService subscriptionService;
    private static AccountService accountService;
    private static EventService eventService;
    private static VOFactory factory = new VOFactory();
    private static VOOrganization supplier;
    private static VOTechnicalService techProduct;
    private static VOService service;
    private static VOMarketplace marketplace;
    private static String providerAdminKey;
    private static String supplierAdminKey;
    private static String instanceId;
    private static long subscriptionKey;

    public static final String TOO_LONG_STRING = "desription_123456789_123456789_123456789_123456789"
            + "_123456789_123456789_123456789_123456789"
            + "_123456789_123456789_123456789_123456789"
            + "_123456789_123456789_123456789_123456789"
            + "_123456789_123456789_123456789_123456789"
            + "_123456789_123456789_123456789_123456789" + "_12345";

    @BeforeClass
    public static void setup() throws Exception {
        WebserviceTestBase.getMailReader().deleteMails();
        initTestData();
    }

    @Before
    public void before() throws Exception {
        eventService = ServiceFactory.getDefault().getEventService(
                providerAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);
    }

    /**
     * Creates a technology provider and imports a technical product. Also a
     * supplier is created for which a service is created and published.
     */
    private static void initTestData() throws Exception {
        // OPERATOR:
        WebserviceTestBase.getOperator().addCurrency(
                WebserviceTestBase.CURRENCY_EUR);
        registerProvider();
        registerSupplier();
        WebserviceTestBase.savePaymentInfoToSupplier(supplier,
                PaymentInfoType.INVOICE);

        // TECHNCIAL PROVIDER:
        initProviderServices();
        importTechnicalService(serviceProvisioningService);
        accountService.addSuppliersForTechnicalService(techProduct,
                Collections.singletonList(supplier.getOrganizationId()));

        // SUPPLIER:
        initSupplierServices();
        marketplace = WebserviceTestBase.registerMarketplace(
                supplier.getOrganizationId(), "mp");
        enablePaymentType(accountService);
        registerMarketableService(serviceProvisioningService);
        WebserviceTestBase.publishToMarketplace(service, true, srvMarketplace,
                marketplace);
        service = serviceProvisioningService.activateService(service);
        VOSubscription subscription = WebserviceTestBase.createSubscription(
                accountService, subscriptionService, "subscrname", service);
        subscriptionKey = subscription.getKey();
        instanceId = subscription.getServiceInstanceId();
    }

    private static void enablePaymentType(AccountService accountService)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OperationPendingException {
        Set<VOPaymentType> defaultPaymentTypes = accountService
                .getDefaultPaymentConfiguration();
        VOPaymentType voPaymentType = WebserviceTestBase.getPaymentTypeVO(
                accountService.getAvailablePaymentTypesForOrganization(),
                PaymentInfoType.INVOICE);
        defaultPaymentTypes.add(voPaymentType);
        VOOrganizationPaymentConfiguration c = new VOOrganizationPaymentConfiguration();
        c.setEnabledPaymentTypes(defaultPaymentTypes);
        c.setOrganization(supplier);
        accountService.savePaymentConfiguration(defaultPaymentTypes,
                Collections.singletonList(c), defaultPaymentTypes, null);
    }

    private static void initProviderServices() throws Exception {
        accountService = ServiceFactory.getDefault().getAccountService(
                providerAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);
        serviceProvisioningService = ServiceFactory.getDefault()
                .getServiceProvisioningService(providerAdminKey,
                        WebserviceTestBase.DEFAULT_PASSWORD);
    }

    private static void initSupplierServices() throws Exception {
        srvMarketplace = ServiceFactory.getDefault().getMarketPlaceService(
                supplierAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);
        serviceProvisioningService = ServiceFactory.getDefault()
                .getServiceProvisioningService(supplierAdminKey,
                        WebserviceTestBase.DEFAULT_PASSWORD);
        subscriptionService = ServiceFactory.getDefault()
                .getSubscriptionService(supplierAdminKey,
                        WebserviceTestBase.DEFAULT_PASSWORD);
        accountService = ServiceFactory.getDefault().getAccountService(
                supplierAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);
    }

    public static void registerProvider() throws Exception {
        VOUserDetails adminUser = factory.createUserVO("providerAdmin"
                + System.currentTimeMillis());
        VOOrganization organization = factory.createOrganizationVO();
        WebserviceTestBase
                .getOperator()
                .registerOrganization(
                        VOConverter.convertToUp(organization),
                        null,
                        VOConverter.convertToUp(adminUser),
                        null,
                        null,
                        org.oscm.internal.types.enumtypes.OrganizationRoleType.TECHNOLOGY_PROVIDER);
        providerAdminKey = WebserviceTestBase
                .readLastMailAndSetCommonPassword();
    }

    public static void registerSupplier() throws Exception {
        VOUserDetails adminUser = factory.createUserVO("supplierAdmin"
                + System.currentTimeMillis());
        VOOrganization organization = factory.createOrganizationVO();
        org.oscm.internal.vo.VOOrganization internalVOOrg = VOConverter
                .convertToUp(organization);
        internalVOOrg.setOperatorRevenueShare(BigDecimal.ZERO);

        supplier = VOConverter
                .convertToApi(WebserviceTestBase
                        .getOperator()
                        .registerOrganization(
                                internalVOOrg,
                                null,
                                VOConverter.convertToUp(adminUser),
                                null,
                                null,
                                org.oscm.internal.types.enumtypes.OrganizationRoleType.SUPPLIER));
        supplierAdminKey = WebserviceTestBase
                .readLastMailAndSetCommonPassword();
    }

    private static void importTechnicalService(
            ServiceProvisioningService serviceProvisioningService)
            throws Exception {
        String tsxml = TSXMLForWebService
                .createTSXMLWithAllowingOnBehalfActingConnectable(Boolean.FALSE
                        .toString());
        techProduct = WebserviceTestBase.createTechnicalService(tsxml,
                serviceProvisioningService);
    }

    private static void registerMarketableService(
            ServiceProvisioningService serviceProvisioningService)
            throws Exception {
        VOServiceDetails serviceDetails = serviceProvisioningService
                .createService(
                        techProduct,
                        factory.createMarketableServiceVO("service" + "_"
                                + WebserviceTestBase.createUniqueKey()), null);

        VOPriceModel priceModel = factory
                .createPriceModelVO(WebserviceTestBase.CURRENCY_EUR);
        priceModel.setOneTimeFee(BigDecimal.valueOf(100));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(40));
        service = serviceProvisioningService.savePriceModel(serviceDetails,
                priceModel);
    }

    @Test(expected = SOAPFaultException.class)
    public void recordEventForInstance_AuthError() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        eventService = ServiceFactory.getDefault().getEventService(
                supplierAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);
        eventService.recordEventForInstance(
                techProduct.getTechnicalServiceId(), instanceId, event);
    }

    @Test
    public void recordEventForInstance() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        eventService.recordEventForInstance(
                techProduct.getTechnicalServiceId(), instanceId, event);
    }

    @Test(expected = SOAPFaultException.class)
    public void recordEventForInstance_NullServiceId() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        eventService.recordEventForInstance(null, instanceId, event);
    }

    @Test(expected = SOAPFaultException.class)
    public void recordEventForInstance_NullInstanceId() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        eventService.recordEventForInstance(
                techProduct.getTechnicalServiceId(), null, event);
    }

    @Test(expected = SOAPFaultException.class)
    public void recordEventForInstance_NullEvent() throws Exception {
        eventService.recordEventForInstance(
                techProduct.getTechnicalServiceId(), instanceId, null);
    }

    @Test(expected = DuplicateEventException.class)
    public void recordEventForInstance_Twice() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        eventService.recordEventForInstance(
                techProduct.getTechnicalServiceId(), instanceId, event);
        eventService.recordEventForInstance(
                techProduct.getTechnicalServiceId(), instanceId, event);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void recordEventForInstance_WrongEventId() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST123");
        eventService.recordEventForInstance(
                techProduct.getTechnicalServiceId(), instanceId, event);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void recordEventForInstance_WrongInstanceId() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST123");
        eventService.recordEventForInstance(
                techProduct.getTechnicalServiceId(), instanceId + "123", event);
    }

    @Test(expected = ValidationException.class)
    public void recordEventForInstance_ToLongUniqueId() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        event.setUniqueId(TOO_LONG_STRING + System.currentTimeMillis());
        eventService.recordEventForInstance(
                techProduct.getTechnicalServiceId(), instanceId, event);
    }

    @Test(expected = ValidationException.class)
    public void recordEventForInstance_ToLongActor() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        event.setActor(TOO_LONG_STRING + System.currentTimeMillis());
        eventService.recordEventForInstance(
                techProduct.getTechnicalServiceId(), instanceId, event);
    }

    @Test
    public void recordEventForSubscription() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        eventService = ServiceFactory.getDefault().getEventService(
                providerAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);
        eventService.recordEventForSubscription(subscriptionKey, event);
    }

    @Test(expected = SOAPFaultException.class)
    public void recordEventForSubscription_AuthError() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        eventService = ServiceFactory.getDefault().getEventService(
                supplierAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);
        try {
            eventService.recordEventForSubscription(subscriptionKey, event);
        } catch (SOAPFaultException e) {
            String stackTrace = WebserviceTestBase.convertStacktrace(e);
            assertTrue(stackTrace.contains("Unauthorized"));
            throw e;
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void recordEventForSubscription_WrongSubscriptionKey()
            throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        eventService.recordEventForSubscription(-1L, event);
    }

    @Test(expected = SOAPFaultException.class)
    public void recordEventForSubscription_NullEvent() throws Exception {
        eventService.recordEventForSubscription(subscriptionKey, null);
    }

    @Test(expected = DuplicateEventException.class)
    public void recordEventForSubscription_Twice() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        eventService.recordEventForSubscription(subscriptionKey, event);
        eventService.recordEventForSubscription(subscriptionKey, event);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void recordEventForSubscription_WrongEventId() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST123");
        eventService.recordEventForSubscription(subscriptionKey, event);
    }

    @Test(expected = ValidationException.class)
    public void recordEventForSubscription_ToLongUniqueId() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        event.setUniqueId(TOO_LONG_STRING + System.currentTimeMillis());
        eventService.recordEventForSubscription(subscriptionKey, event);
    }

    @Test(expected = ValidationException.class)
    public void recordEventForSubscription_ToLongActor() throws Exception {
        VOGatheredEvent event = factory.createVOGatheredEvent(
                System.currentTimeMillis(), "TEST");
        event.setActor(TOO_LONG_STRING + System.currentTimeMillis());
        eventService.recordEventForSubscription(subscriptionKey, event);
    }

}
