/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Dec 13, 2011                                                      
 *                                                                              
 *  Completion Time: Dec 14, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//import com.sun.xml.ws.client.ClientTransportException;
//import com.sun.xml.ws.fault.ServerSOAPFaultException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.intf.OrganizationalUnitService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.types.enumtypes.OperationParameterType;
import org.oscm.types.enumtypes.SubscriptionStatus;
import org.oscm.types.enumtypes.TriggerType;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.types.enumtypes.UnitRoleType;
import org.oscm.types.exceptions.DomainObjectException.ClassEnum;
import org.oscm.types.exceptions.beans.ApplicationExceptionBean;
import org.oscm.vo.VOOrganizationalUnit;
import org.oscm.vo.VORoleDefinition;
import org.oscm.vo.VOServiceOperationParameterValues;
import org.oscm.vo.VOSubscriptionIdAndOrganizations;
import org.oscm.vo.VOTechnicalServiceOperation;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserSubscription;
import org.oscm.ws.base.PaymentTypeFactory;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;
import org.oscm.intf.AccountService;
import org.oscm.intf.IdentityService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.SubscriptionService;
import org.oscm.types.enumtypes.PriceModelType;
import org.oscm.types.enumtypes.Salutation;
import org.oscm.types.enumtypes.UdaConfigurationType;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.MandatoryUdaMissingException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.PaymentInformationException;
import org.oscm.types.exceptions.PriceModelException;
import org.oscm.types.exceptions.ServiceChangedException;
import org.oscm.types.exceptions.ServiceParameterException;
import org.oscm.types.exceptions.SubscriptionAlreadyExistsException;
import org.oscm.types.exceptions.SubscriptionStateException;
import org.oscm.types.exceptions.TechnicalServiceNotAliveException;
import org.oscm.types.exceptions.TechnicalServiceOperationException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOBillingContact;
import org.oscm.vo.VOInstanceInfo;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOParameterDefinition;
import org.oscm.vo.VOPaymentInfo;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOSubscriptionDetails;
import org.oscm.vo.VOTechnicalService;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUdaDefinition;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUserDetails;


public class SubscriptionServiceWSTest {

    private static final String SUPPLIER_ORG_NAME = "Supplier";
    private static final String RESELLER_ORG_NAME = "Reseller";
    private static final String MAIL_SUBJECT_SERVICE_TICKET_EN = "[SaaS incident] Test ReportIssue";

    private static WebserviceTestSetup setup;
    private static VOOrganization supplier;
    private static VOOrganization reseller;
    private static VOService freeService;
    private static VOService chargeableService;
    private static VOService asynFreeService;
    private static VOService asynFreeService2;
    private static VOUserDetails customerUser;
    private static VOOrganization customerOrg;
    private static VOPaymentInfo customerPaymentInfo;
    private static VOBillingContact customerBillingContact;
    private static SubscriptionService subscrServiceForCustomer;
    private static SubscriptionService subscrServiceForSupplier;
    private static SubscriptionService subscrServiceForReseller;
    private static SubscriptionService subscrServiceForSubManager;
    private static SubscriptionService service4UnitAdminSubManager;
    private static SubscriptionService service4UnitAdminOnly;
    private static SubscriptionService service4SubManagerOnly;
    private static SubscriptionService service4notUnitAdminNotSubManager;
    private static AccountService accountService;
    private static AccountService accountServiceForCustomer;
    private static IdentityService identityService;
    private static List<VOUsageLicense> usageLicences;
    private static VOUserDetails orgAdmin;
    private static SubscriptionService subscrServiceForOrgAdmin;
    private static OrganizationalUnitService organizationalUnitService;

    private String mailContentReceived = null;
    private String mailSubjectToSend;
    private String mailContentToSend;
    private String subscriptionID = null;
    private VOSubscription createdSubscription = null;
    private static VOService serviceWithParameter;
    private static VOMarketplace mpLocal;
    private VOTechnicalService technicalServiceWithParameter;
    private final String paraValue = "201";
    private final String newParaValue = "202";
    private final String standardDefinitionID = "StandardDef";
    private final String onetimeDefinitionID = "OneTimeDef";
    private final String SUBSCRIBE_SERVICE = "30000";
    private static VOFactory factory = new VOFactory();
    private static VOUserDetails subscriptionManager;
    private static VOUserDetails user;
    private static VOUserDetails unitAdminSubManager;
    private static VOUserDetails unitAdminOnly;
    private static VOUserDetails subManagerOnly;
    private static VOUserDetails notUnitAdminNotSubManager;
    private static VOService svcWithOp;
    private static VOTechnicalService tpWithOp;
    private static VOOrganizationalUnit orgUnit;
    private static VOOrganizationalUnit unitBug12379;

    @BeforeClass
    public static void setUpOnce() throws Exception {

        WebserviceTestBase.getMailReader().deleteMails();
        WebserviceTestBase.getOperator().addCurrency("EUR");
        PaymentTypeFactory.preparePaymentType();
        setup = new WebserviceTestSetup();
        supplier = setup.createSupplier(SUPPLIER_ORG_NAME);
        setup.createTechnicalService();
        setup.createAsynTechnicalService("asynTechnicalProduct");
        tpWithOp = setup
                .createTechnicalServiceWithOperationsAndOperationParameters(
                        "TP with OP",
                        setup.getServiceProvisioningSrvAsSupplier());

        MarketplaceService mpSrvOperator = ServiceFactory.getDefault()
                .getMarketPlaceService(
                        WebserviceTestBase.getPlatformOperatorKey(),
                        WebserviceTestBase.getPlatformOperatorPassword());

        mpLocal = mpSrvOperator.createMarketplace(factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "Local Marketplace"));

        chargeableService = setup.createAndActivateService("Service", mpLocal);

        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        freeService = setup.createAndActivateService("Service", mpLocal,
                priceModel);

        asynFreeService = setup.createAndActivateAsynService("Service",
                mpLocal, priceModel);
        asynFreeService2 = setup.createAndActivateAsynService("Service2",
                mpLocal, priceModel);

        svcWithOp = setup.createAndActivateService("Service OP", tpWithOp,
                mpLocal, new LinkedList<VOParameter>());

        customerOrg = setup.createCustomer("Customer");
        customerUser = setup.getCustomerUser();
        customerPaymentInfo = setup.getCustomerPaymentInfo();
        customerBillingContact = setup.getCustomerBillingContact();

        usageLicences = new ArrayList<VOUsageLicense>();
        usageLicences.add(factory.createUsageLicenceVO(customerUser));

        subscrServiceForCustomer = ServiceFactory.getDefault()
                .getSubscriptionService(String.valueOf(customerUser.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        subscrServiceForSupplier = ServiceFactory.getDefault()
                .getSubscriptionService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        // reseller is created AFTER the customer, so the customerUser is not
        // assigned to him
        reseller = setup.createReseller(RESELLER_ORG_NAME);
        subscrServiceForReseller = ServiceFactory.getDefault()
                .getSubscriptionService(setup.getResellerUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        accountService = ServiceFactory.getDefault()
                .getAccountService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        accountServiceForCustomer = ServiceFactory.getDefault()
                .getAccountService(String.valueOf(customerUser.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        identityService = ServiceFactory.getDefault().getIdentityService(
                String.valueOf(customerUser.getKey()),
                WebserviceTestBase.DEFAULT_PASSWORD);

        subscriptionManager = createVOUser();
        subscriptionManager = identityService.createUser(subscriptionManager,
                Arrays.asList(UserRoleType.SUBSCRIPTION_MANAGER),
                mpLocal.getMarketplaceId());
        subscriptionManager.setKey(Long.parseLong(WebserviceTestBase
                .readLastMailAndSetCommonPassword(subscriptionManager.getUserId())));

        subscrServiceForSubManager = ServiceFactory.getDefault()
                .getSubscriptionService(
                        String.valueOf(subscriptionManager.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        user = createVOUser();
        user = identityService.createUser(user, null,
                mpLocal.getMarketplaceId());

        orgAdmin = createVOUser();
        orgAdmin = identityService.createUser(orgAdmin,
                Arrays.asList(UserRoleType.ORGANIZATION_ADMIN),
                mpLocal.getMarketplaceId());
        orgAdmin.setKey(Long.parseLong(WebserviceTestBase
                .readLastMailAndSetCommonPassword(orgAdmin.getUserId())));
        subscrServiceForOrgAdmin = ServiceFactory.getDefault()
                .getSubscriptionService(
                        String.valueOf(orgAdmin.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);
    }

    @Before
    public void setup() throws Exception {
        mailSubjectToSend = "Test ReportIssue";
        mailContentToSend = "Test ReportIssue Content.";

        unitAdminSubManager = createVOUser();
        unitAdminSubManager = identityService.createUser(unitAdminSubManager,
                Arrays.asList(UserRoleType.SUBSCRIPTION_MANAGER),
                mpLocal.getMarketplaceId());
        unitAdminSubManager.setKey(Long.parseLong(WebserviceTestBase
                .readLastMailAndSetCommonPassword(unitAdminSubManager.getUserId())));

        organizationalUnitService = ServiceFactory.getDefault()
                .getOrganizationalUnitService(String.valueOf(orgAdmin.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        unitBug12379 = organizationalUnitService.createUnit("testUnit" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()),
                "testDescription", new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        identityService.addRevokeUserUnitAssignment(unitBug12379.getName(), Arrays.<VOUser>asList(unitAdminSubManager), Collections.<VOUser>emptyList());

        organizationalUnitService.revokeUserRoles(unitAdminSubManager, Arrays.asList(UnitRoleType.USER), unitBug12379);
        organizationalUnitService.grantUserRoles(unitAdminSubManager, Arrays.asList(org.oscm.types.enumtypes.UnitRoleType.ADMINISTRATOR), unitBug12379);
        service4UnitAdminSubManager = ServiceFactory.getDefault()
                .getSubscriptionService(
                        String.valueOf(subscriptionManager.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        unitAdminOnly = createVOUser();
        unitAdminOnly = identityService.createUser(unitAdminOnly,
                Collections.<UserRoleType>emptyList(),
                mpLocal.getMarketplaceId());
        unitAdminOnly.setKey(Long.parseLong(WebserviceTestBase
                .readLastMailAndSetCommonPassword(unitAdminOnly.getUserId())));
        identityService.addRevokeUserUnitAssignment(unitBug12379.getName(), Arrays.<VOUser>asList(unitAdminOnly), Collections.<VOUser>emptyList());

        service4UnitAdminOnly = ServiceFactory.getDefault()
                .getSubscriptionService(
                        String.valueOf(unitAdminOnly.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        subManagerOnly = createVOUser();
        subManagerOnly = identityService.createUser(subManagerOnly, Arrays.asList(UserRoleType.SUBSCRIPTION_MANAGER), mpLocal.getMarketplaceId());
        subManagerOnly.setKey(Long.parseLong(WebserviceTestBase
                .readLastMailAndSetCommonPassword(subManagerOnly.getUserId())));

        service4SubManagerOnly = ServiceFactory.getDefault().getSubscriptionService(String.valueOf(subManagerOnly.getKey()), WebserviceTestBase.DEFAULT_PASSWORD);


        notUnitAdminNotSubManager = createVOUser();
        notUnitAdminNotSubManager = identityService.createUser(notUnitAdminNotSubManager, Collections.<UserRoleType>emptyList(), mpLocal.getMarketplaceId());
        notUnitAdminNotSubManager.setKey(Long.parseLong(WebserviceTestBase
                .readLastMailAndSetCommonPassword(notUnitAdminNotSubManager.getUserId())));

        service4notUnitAdminNotSubManager = ServiceFactory.getDefault().getSubscriptionService(String.valueOf(notUnitAdminNotSubManager.getKey()), WebserviceTestBase.DEFAULT_PASSWORD);
    }

    @After
    public void cleanup() throws Exception {
        if (createdSubscription != null) {
            try {
                subscrServiceForCustomer
                        .unsubscribeFromService(createdSubscription
                                .getSubscriptionId());
                subscrServiceForSubManager
                        .unsubscribeFromService(createdSubscription
                                .getSubscriptionId());
                subscrServiceForOrgAdmin
                        .unsubscribeFromService(createdSubscription.getSubscriptionId());
            } catch (ObjectNotFoundException e) {
                // for unsubscribe test, it is already done
            }
        }
        accountService.saveUdaDefinitions(new ArrayList<VOUdaDefinition>(),
                accountService.getUdaDefinitions());
        setup.deleteTriggersForUser();
    }

    private void terminateAsyncSubscription() throws Exception {
        if (createdSubscription != null) {
            subscrServiceForSupplier.terminateSubscription(createdSubscription,
                    "reason");
        }
    }

    private VOSubscription subscribe() throws ObjectNotFoundException,
            NonUniqueBusinessKeyException, ValidationException,
            PaymentInformationException, ServiceParameterException,
            ServiceChangedException, PriceModelException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            SubscriptionAlreadyExistsException, OperationPendingException,
            MandatoryUdaMissingException, ConcurrentModificationException,
            SubscriptionStateException {
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        subscriptionID = createdSubscription.getSubscriptionId();
        return createdSubscription;
    }

    @Test
    public void bug12379_subscriptionToServiceWithUnitAssignedByApi_unitAdminSubManager() throws Exception {
        //given
        List<VOUda> udasToSave = prepareUdasForSave();
        VOSubscription subscription = createSubscription();
        subscription.setUnitKey(unitBug12379.getKey());
        subscription.setUnitName(unitBug12379.getName());

        organizationalUnitService.revokeUserRoles(unitAdminSubManager, Arrays.asList(UnitRoleType.USER), unitBug12379);
        organizationalUnitService.grantUserRoles(unitAdminSubManager, Arrays.asList(org.oscm.types.enumtypes.UnitRoleType.ADMINISTRATOR), unitBug12379);
        //when
        service4UnitAdminSubManager = ServiceFactory.getDefault()
                .getSubscriptionService(
                        String.valueOf(unitAdminSubManager.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        VOSubscription voSubscription = service4UnitAdminSubManager.subscribeToService(subscription, freeService, usageLicences, null, null, udasToSave);
        //then
        assertEquals(voSubscription.getUnitKey(), unitBug12379.getKey());
        assertEquals(voSubscription.getUnitName(), unitBug12379.getName());
        service4UnitAdminSubManager.unsubscribeFromService(voSubscription.getSubscriptionId());
    }

    @Test
    public void bug12379_subscriptionToServiceWithoutUnitAssignedByApi_unitAdminSubManager() throws Exception {
        //given
        List<VOUda> udasToSave = prepareUdasForSave();
        VOSubscription subscription = createSubscription();

        organizationalUnitService.revokeUserRoles(unitAdminSubManager, Arrays.asList(UnitRoleType.USER), unitBug12379);
        organizationalUnitService.grantUserRoles(unitAdminSubManager, Arrays.asList(org.oscm.types.enumtypes.UnitRoleType.ADMINISTRATOR), unitBug12379);
        //when
        VOSubscription voSubscription = service4UnitAdminSubManager.subscribeToService(subscription, freeService, usageLicences, null, null, udasToSave);
        waitForLogStored();
        //then
        assertTrue(voSubscription.getUnitKey() == 0);
        assertNull(voSubscription.getUnitName());
        service4UnitAdminSubManager.unsubscribeFromService(voSubscription.getSubscriptionId());
    }

    @Test
    public void bug12379_subscriptionToServiceWithUnitAssignedByApi_unitAdminOnly() throws Exception {
        //given
        List<VOUda> udasToSave = prepareUdasForSave();
        VOSubscription subscription = createSubscription();
        subscription.setUnitKey(unitBug12379.getKey());
        subscription.setUnitName(unitBug12379.getName());
        organizationalUnitService.revokeUserRoles(unitAdminOnly, Arrays.asList(UnitRoleType.USER), unitBug12379);
        organizationalUnitService.grantUserRoles(unitAdminOnly, Arrays.asList(org.oscm.types.enumtypes.UnitRoleType.ADMINISTRATOR), unitBug12379);
        service4UnitAdminOnly = ServiceFactory.getDefault()
                .getSubscriptionService(
                        String.valueOf(unitAdminOnly.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        //when
        VOSubscription voSubscription = service4UnitAdminOnly.subscribeToService(subscription, freeService, usageLicences, null, null, udasToSave);
        waitForLogStored();
        //then
        assertEquals(voSubscription.getUnitKey(), unitBug12379.getKey());
        assertEquals(voSubscription.getUnitName(), unitBug12379.getName());
        service4UnitAdminOnly.unsubscribeFromService(voSubscription.getSubscriptionId());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void bug12379_subscriptionToServiceWithoutUnitAssignedByApi_unitAdminOnly() throws Exception {
        //given
        List<VOUda> udasToSave = prepareUdasForSave();
        VOSubscription subscription = createSubscription();
        organizationalUnitService.revokeUserRoles(unitAdminOnly, Arrays.asList(UnitRoleType.USER), unitBug12379);
        organizationalUnitService.grantUserRoles(unitAdminOnly, Arrays.asList(org.oscm.types.enumtypes.UnitRoleType.ADMINISTRATOR), unitBug12379);

        //when
        VOSubscription voSubscription = service4UnitAdminOnly.subscribeToService(subscription, freeService, usageLicences, null, null, udasToSave);
        waitForLogStored();
        //then
        assertTrue(voSubscription.getUnitKey()== 0);
        assertNull(voSubscription.getUnitName());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void bug12379_subscriptionToServiceWithUnitAssignedByApi_subManagerOnly() throws Exception {
        //given
        List<VOUda> udasToSave = prepareUdasForSave();
        VOSubscription subscription = createSubscription();
        subscription.setUnitKey(unitBug12379.getKey());
        subscription.setUnitName(unitBug12379.getName());
        service4SubManagerOnly = ServiceFactory.getDefault()
                .getSubscriptionService(
                        String.valueOf(subManagerOnly.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        //when
        VOSubscription voSubscription = service4SubManagerOnly.subscribeToService(subscription, freeService, usageLicences, null, null, udasToSave);
        //then
        assertEquals(unitBug12379.getKey(), voSubscription.getUnitKey());
        assertEquals(unitBug12379.getName(), voSubscription.getUnitName());
    }
    @Test
    public void bug12379_subscriptionToServiceWithoutUnitAssignedByApi_subManagerOnly() throws Exception {
        //given
        List<VOUda> udasToSave = prepareUdasForSave();
        VOSubscription subscription = createSubscription();
        service4SubManagerOnly = ServiceFactory.getDefault()
                .getSubscriptionService(
                        String.valueOf(subManagerOnly.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        //when
        VOSubscription voSubscription = service4SubManagerOnly.subscribeToService(subscription, freeService, usageLicences, null, null, udasToSave);
        //then
        assertTrue(voSubscription.getUnitKey() == 0);
        assertNull(voSubscription.getUnitName());
        service4SubManagerOnly.unsubscribeFromService(voSubscription.getSubscriptionId());
    }

    @Test
    public void bug12379_subscriptionToServiceWithoutUnitAssignedByApi_notUnitAdminNotSubManager() throws Exception{
//        //given
//        List<VOUda> udasToSave = prepareUdasForSave();
//        VOSubscription subscription = createSubscription();
//        //when
//        try {
//            service4notUnitAdminNotSubManager.subscribeToService(subscription, freeService, usageLicences, null, null, udasToSave);
//            fail();
//        } catch (ServerSOAPFaultException e) {
//            //then
//            assertTrue(e.getMessage().contains("Unauthorized"));
//        }
    }
    @Test
    public void bug12379_subscriptionToServiceWithUnitAssignedByApi_notUnitAdminNotSubManager() throws Exception {
//        //given
//        List<VOUda> udasToSave = prepareUdasForSave();
//        VOSubscription subscription = createSubscription();
//        subscription.setUnitKey(unitBug12379.getKey());
//        subscription.setUnitName(unitBug12379.getName());
//        service4notUnitAdminNotSubManager = ServiceFactory.getDefault()
//                .getSubscriptionService(
//                        String.valueOf(notUnitAdminNotSubManager.getKey()),
//                        WebserviceTestBase.DEFAULT_PASSWORD);
//        //when
//        try {
//            service4notUnitAdminNotSubManager.subscribeToService(subscription, freeService, usageLicences, null, null, udasToSave);
//        } catch (ServerSOAPFaultException e) {
//            //then
//            assertTrue(e.getMessage().contains("Unauthorized"));
//        }
    }


    @Test
    public void subscribeToFreeService() throws Exception {
        // given
        List<String> operations = new ArrayList<String>();
        operations.add(SUBSCRIBE_SERVICE);
        long fromDate = new Date().getTime();
        OperatorService operatorService = WebserviceTestBase.getOperator();

        createdSubscription = new VOSubscription();
        createdSubscription.setSubscriptionId(Long.toHexString(System
                .currentTimeMillis()));
        createdSubscription.setUnitName("default");

        WebserviceTestBase.storeGlobalConfigurationSetting(
                ConfigurationKey.AUDIT_LOG_ENABLED, true);
        try {
            // when
            createdSubscription = subscrServiceForCustomer.subscribeToService(
                    createdSubscription, freeService, null, null, null,
                    new ArrayList<VOUda>());
            waitForLogStored();
            // then
            byte[] auditLogs = operatorService.getUserOperationLog(operations,
                    fromDate, fromDate + 86400000L);
            verifySubscriptionAndAuditLogs(createdSubscription, freeService,
                    auditLogs);
            assertEquals(customerUser.getUserId(),
                    createdSubscription.getOwnerId());
            assertEquals("default", createdSubscription.getUnitName());
        } finally {
            WebserviceTestBase.storeGlobalConfigurationSetting(
                    ConfigurationKey.AUDIT_LOG_ENABLED, false);
        }
    }

    /**
     * The log method will be invoked asynchronously, so some time is needed
     * before retrieving log entries.
     *
     *
     * @throws InterruptedException
     */
    private void waitForLogStored() throws InterruptedException {
        Thread.sleep(5000);
    }

    @Test
    public void subscribeToService_WithUdas() throws Exception {
        List<VOUda> udasToSave = prepareUdasForSave();
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                udasToSave);
        assertEquals(customerUser.getUserId(), createdSubscription.getOwnerId());
        verifyUdas(udasToSave);
    }

    @Test
    public void modifySubscription_WithUdas() throws Exception {
        // given
        List<VOUda> udasToSave = prepareUdasForSave();
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String newSubscriptionId = "NewSubscrId"
                + Long.toHexString(System.currentTimeMillis());
        createdSubscription.setSubscriptionId(newSubscriptionId);

        createdSubscription.setOwnerId(subscriptionManager.getUserId());
        // when
        VOSubscriptionDetails modifiedSubscription = subscrServiceForCustomer
                .modifySubscription(createdSubscription, null, udasToSave);

        // then
        assertEquals(subscriptionManager.getUserId(),
                modifiedSubscription.getOwnerId());
        verifyUdas(udasToSave);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void modifySubscription_AddOwnerWithoutRole() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        createdSubscription.setOwnerId(user.getUserId());
        // when
        try {
            subscrServiceForCustomer.modifySubscription(createdSubscription,
                    null, null);
            // then
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage()
                    .contains("Add subscription owner failed."));
            throw e;
        }
    }

    @Test
    public void modifySubscriptionBug11736() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription.setOwnerId(subscriptionManager.getUserId());
        createdSubscription = subscrServiceForSubManager.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        assertEquals(createdSubscription.getOwnerId(), subscriptionManager.getUserId());

        // when
        createdSubscription.setOwnerId(customerUser.getUserId());
        VOSubscriptionDetails modified = subscrServiceForSubManager.modifySubscription(createdSubscription,
                null, null);
        // then
        String ownerId = modified.getOwnerId();
        assertNotSame(customerUser.getUserId(), ownerId);
        assertEquals(subscriptionManager.getUserId(), ownerId);
    }

    @Test
    public void modifySubscriptionBug11736Admin() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription.setOwnerId(subscriptionManager.getUserId());
        createdSubscription = subscrServiceForSubManager.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        assertEquals(createdSubscription.getOwnerId(), subscriptionManager.getUserId());

        // when
        createdSubscription.setOwnerId(customerUser.getUserId());
        VOSubscriptionDetails modified = subscrServiceForOrgAdmin.modifySubscription(createdSubscription,
                null, null);
        // then
        String ownerId = modified.getOwnerId();
        assertEquals(customerUser.getUserId(), ownerId);
        assertNotSame(subscriptionManager.getUserId(), ownerId);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void modifySubscription_AddNoneExsitUserAsOwner() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        createdSubscription.setOwnerId("TestId");
        // when
        try {
            subscrServiceForCustomer.modifySubscription(createdSubscription,
                    null, null);
            // then
            fail();
        } catch (Exception e) {
            assertTrue(e
                    .getMessage()
                    .contains(
                            "Could not find object of type 'USER' with business key 'TestId'"));
            throw e;
        }
    }

    @Test
    public void modifySubscription_RemoveOwner() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        createdSubscription.setOwnerId(null);
        // when
        VOSubscription modifiedSubscription = subscrServiceForCustomer
                .modifySubscription(createdSubscription, null, null);
        // then
        assertNull(modifiedSubscription.getOwnerId());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void modifySubscription_SubscriptionManagerNoPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        createdSubscription.setOwnerId(null);

        // when
        subscrServiceForSubManager.modifySubscription(createdSubscription,
                null, null);
    }

    @Test
    public void modifySubscription_SubscriptionManagerPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForSubManager.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        createdSubscription.setPurchaseOrderNumber("newOrderNumber");

        // when
        subscrServiceForSubManager.modifySubscription(createdSubscription,
                null, null);

        // then
        List<VOSubscription> subList = subscrServiceForSubManager
                .getSubscriptionsForOrganization();
        VOSubscription subscription = getSubscriptionInList(
                createdSubscription, subList);
        assertNotNull(subscription);
        assertEquals("newOrderNumber", subscription.getPurchaseOrderNumber());
    }

    private VOSubscription getSubscriptionInList(VOSubscription subToGet,
            List<VOSubscription> subList) {
        VOSubscription subscription = null;
        for (VOSubscription sub : subList) {
            if (sub.getKey() == subToGet.getKey()) {
                subscription = sub;
            }
        }
        return subscription;
    }

    @Test
    public void modifySubscription_UpdateParameterOK() throws Exception {
        createAndSubscribeToServiceWithParameter(paraValue);
        List<VOParameter> paramsForUpdate = getParametersForModifySubscription(newParaValue);
        setNewNameForSubscription();

        createdSubscription.setOwnerId(subscriptionManager.getUserId());
        VOSubscriptionDetails subDetails = subscrServiceForCustomer
                .modifySubscription(createdSubscription, paramsForUpdate, null);

        assertEquals(subscriptionManager.getUserId(), subDetails.getOwnerId());
        assertEquals(createdSubscription.getSubscriptionId(),
                subDetails.getSubscriptionId());
        assertEquals(newParaValue, subDetails.getSubscribedService()
                .getParameters().get(0).getValue());
    }

    @Test
    public void asynModifySubscription() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, asynFreeService, usageLicences, null,
                null, new ArrayList<VOUda>());

        VOInstanceInfo instance = new VOInstanceInfo();
        instance.setAccessInfo("PLATFORM");
        instance.setBaseUrl(asynFreeService.getBaseURL());
        instance.setInstanceId(createdSubscription.getSubscriptionId());
        instance.setLoginPath("/login");
        subscrServiceForSupplier.completeAsyncSubscription(
                createdSubscription.getSubscriptionId(),
                customerOrg.getOrganizationId(), instance);
        refreshSubscriptionDetails();
        String newSubscriptionId = "NewSubscrId"
                + Long.toHexString(System.currentTimeMillis());
        createdSubscription.setSubscriptionId(newSubscriptionId);
        // when
        createdSubscription = subscrServiceForCustomer.modifySubscription(
                createdSubscription, null, new ArrayList<VOUda>());
        subscrServiceForSupplier.completeAsyncModifySubscription(
                createdSubscription.getSubscriptionId(),
                customerOrg.getOrganizationId(), instance);

        // then
        assertEquals(SubscriptionStatus.PENDING_UPD,
                createdSubscription.getStatus());
        createdSubscription = subscrServiceForCustomer
                .getSubscriptionDetails(newSubscriptionId);
        terminateAsyncSubscription();
    }

    @Test
    public void asynModifySubscription_PendingUpdate() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, asynFreeService, usageLicences, null,
                null, new ArrayList<VOUda>());

        VOInstanceInfo instance = new VOInstanceInfo();
        instance.setAccessInfo("PLATFORM");
        instance.setBaseUrl(asynFreeService.getBaseURL());
        instance.setInstanceId(createdSubscription.getSubscriptionId());
        instance.setLoginPath("/login");
        subscrServiceForSupplier.completeAsyncSubscription(
                createdSubscription.getSubscriptionId(),
                customerOrg.getOrganizationId(), instance);
        refreshSubscriptionDetails();

        String newSubscriptionId = "NewSubscrId"
                + Long.toHexString(System.currentTimeMillis());
        createdSubscription.setSubscriptionId(newSubscriptionId);
        createdSubscription = subscrServiceForCustomer.modifySubscription(
                createdSubscription, null, new ArrayList<VOUda>());
        String oldSubscriptionId = createdSubscription.getSubscriptionId();
        refreshSubscriptionDetails();
        newSubscriptionId = "NewSubscrId"
                + Long.toHexString(System.currentTimeMillis());
        createdSubscription.setSubscriptionId(newSubscriptionId);
        // when
        try {
            createdSubscription = subscrServiceForCustomer.modifySubscription(
                    createdSubscription, null, new ArrayList<VOUda>());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
        subscrServiceForSupplier.abortAsyncModifySubscription(
                oldSubscriptionId, customerOrg.getOrganizationId(), null);

        // then
        assertEquals(SubscriptionStatus.PENDING_UPD,
                createdSubscription.getStatus());
        createdSubscription = subscrServiceForCustomer
                .getSubscriptionDetails(oldSubscriptionId);
        assertEquals(SubscriptionStatus.ACTIVE, createdSubscription.getStatus());
        terminateAsyncSubscription();
    }

    @Test
    public void abortAsynModifySubscription() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, asynFreeService, usageLicences, null,
                null, new ArrayList<VOUda>());

        VOInstanceInfo instance = new VOInstanceInfo();
        instance.setAccessInfo("PLATFORM");
        instance.setBaseUrl(asynFreeService.getBaseURL());
        instance.setInstanceId(createdSubscription.getSubscriptionId());
        instance.setLoginPath("/login");
        subscrServiceForSupplier.completeAsyncSubscription(
                createdSubscription.getSubscriptionId(),
                customerOrg.getOrganizationId(), instance);
        refreshSubscriptionDetails();
        String newSubscriptionId = "NewSubscrId"
                + Long.toHexString(System.currentTimeMillis());
        createdSubscription.setSubscriptionId(newSubscriptionId);
        createdSubscription = subscrServiceForCustomer.modifySubscription(
                createdSubscription, null, new ArrayList<VOUda>());
        // when
        subscrServiceForSupplier.abortAsyncModifySubscription(
                createdSubscription.getSubscriptionId(),
                customerOrg.getOrganizationId(), null);
        // then
        refreshSubscriptionDetails();
        assertEquals(SubscriptionStatus.ACTIVE, createdSubscription.getStatus());
        terminateAsyncSubscription();
    }

    @Test
    public void completeAsynUpgradeSubscription() throws Exception {
        // given
        asynFreeService = setup.deactivateService(asynFreeService);
        ServiceProvisioningService spsSvc = ServiceFactory.getDefault()
                .getServiceProvisioningService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        spsSvc.setCompatibleServices(asynFreeService,
                Collections.singletonList(asynFreeService2));
        asynFreeService = setup.activateService(asynFreeService);
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, asynFreeService, null, null, null,
                new ArrayList<VOUda>());
        VOInstanceInfo instance = new VOInstanceInfo();
        instance.setAccessInfo("PLATFORM");
        instance.setBaseUrl(asynFreeService.getBaseURL());
        instance.setInstanceId(createdSubscription.getSubscriptionId());
        instance.setLoginPath("/login");
        subscrServiceForSupplier.completeAsyncSubscription(
                createdSubscription.getSubscriptionId(),
                customerOrg.getOrganizationId(), instance);
        refreshSubscriptionDetails();
        // when
        subscrServiceForCustomer.upgradeSubscription(createdSubscription,
                asynFreeService2, customerPaymentInfo, customerBillingContact,
                new ArrayList<VOUda>());
        refreshSubscriptionDetails();
        subscrServiceForSupplier.completeAsyncUpgradeSubscription(
                createdSubscription.getSubscriptionId(),
                customerOrg.getOrganizationId(), instance);

        // then
        assertEquals(SubscriptionStatus.PENDING_UPD,
                createdSubscription.getStatus());
        refreshSubscriptionDetails();
        assertEquals(SubscriptionStatus.ACTIVE, createdSubscription.getStatus());
        terminateAsyncSubscription();
    }

    @Test
    public void abortAsynUpgradeSubscription() throws Exception {
        // given
        asynFreeService = setup.deactivateService(asynFreeService);
        ServiceProvisioningService spsSvc = ServiceFactory.getDefault()
                .getServiceProvisioningService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        spsSvc.setCompatibleServices(asynFreeService,
                Collections.singletonList(asynFreeService2));
        asynFreeService = setup.activateService(asynFreeService);
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, asynFreeService, null, null, null,
                new ArrayList<VOUda>());
        VOInstanceInfo instance = new VOInstanceInfo();
        instance.setAccessInfo("PLATFORM");
        instance.setBaseUrl(asynFreeService.getBaseURL());
        instance.setInstanceId(createdSubscription.getSubscriptionId());
        instance.setLoginPath("/login");
        subscrServiceForSupplier.completeAsyncSubscription(
                createdSubscription.getSubscriptionId(),
                customerOrg.getOrganizationId(), instance);
        refreshSubscriptionDetails();
        // when
        subscrServiceForCustomer.upgradeSubscription(createdSubscription,
                asynFreeService2, customerPaymentInfo, customerBillingContact,
                new ArrayList<VOUda>());
        refreshSubscriptionDetails();
        subscrServiceForSupplier.abortAsyncUpgradeSubscription(
                createdSubscription.getSubscriptionId(),
                customerOrg.getOrganizationId(), null);
        // then
        assertEquals(SubscriptionStatus.PENDING_UPD,
                createdSubscription.getStatus());
        refreshSubscriptionDetails();
        assertEquals(SubscriptionStatus.ACTIVE, createdSubscription.getStatus());
        terminateAsyncSubscription();
    }

    @Test
    public void upgradeSubscription_WithUdas() throws Exception {
        // given
        List<VOUda> udasToSave = prepareUdasForSave();
        freeService = setup.deactivateService(freeService);
        ServiceProvisioningService spsSvc = ServiceFactory.getDefault()
                .getServiceProvisioningService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        spsSvc.setCompatibleServices(freeService,
                Collections.singletonList(chargeableService));
        freeService = setup.activateService(freeService);
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, null, null, null,
                new ArrayList<VOUda>());

        // when
        subscrServiceForCustomer.upgradeSubscription(createdSubscription,
                chargeableService, customerPaymentInfo, customerBillingContact,
                udasToSave);

        // then
        verifyUdas(udasToSave);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void upgradeSubscription_SubscriptionManagerNoPermissions()
            throws Exception {
        // given
        freeService = setup.deactivateService(freeService);
        ServiceProvisioningService spsSvc = ServiceFactory.getDefault()
                .getServiceProvisioningService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        spsSvc.setCompatibleServices(freeService,
                Collections.singletonList(chargeableService));
        freeService = setup.activateService(freeService);
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, null, null, null,
                new ArrayList<VOUda>());

        // when
        subscrServiceForSubManager.upgradeSubscription(createdSubscription,
                chargeableService, customerPaymentInfo, customerBillingContact,
                new ArrayList<VOUda>());
    }

    @Test
    public void upgradeSubscription_SubscriptionManagerPermissions()
            throws Exception {
        // given
        List<VOUda> udasToSave = prepareUdasForSave();
        freeService = setup.deactivateService(freeService);
        ServiceProvisioningService spsSvc = ServiceFactory.getDefault()
                .getServiceProvisioningService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        spsSvc.setCompatibleServices(freeService,
                Collections.singletonList(chargeableService));
        freeService = setup.activateService(freeService);
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForSubManager.subscribeToService(
                createdSubscription, freeService, null, null, null,
                new ArrayList<VOUda>());

        // when
        subscrServiceForSubManager.upgradeSubscription(createdSubscription,
                chargeableService, customerPaymentInfo, customerBillingContact,
                udasToSave);

        // then
        verifyUdas(udasToSave);
    }

    @Test
    public void subscribeToChargeableService_NoPayment() throws Exception {
        SubscriptionService subSvc = ServiceFactory.getDefault()
                .getSubscriptionService();
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(Long.toHexString(System
                .currentTimeMillis()));
        PaymentInformationException expected = null;

        try {
            subSvc.subscribeToService(subscription, chargeableService, null,
                    null, customerBillingContact, new ArrayList<VOUda>());
        } catch (PaymentInformationException e) {
            expected = e;
            ApplicationExceptionBean faultInfo = expected.getFaultInfo();
            assertNotNull(faultInfo);
            assertEquals("ex.PaymentInformationException",
                    faultInfo.getMessageKey());
        }
        assertNotNull("Exception expected", expected);

    }

    @Test
    public void subscribeToChargeableService_NoBillingContact()
            throws Exception {
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(Long.toHexString(System
                .currentTimeMillis()));
        PaymentInformationException expected = null;

        try {
            subscrServiceForCustomer.subscribeToService(subscription,
                    chargeableService, null, customerPaymentInfo, null,
                    new ArrayList<VOUda>());
        } catch (PaymentInformationException e) {
            expected = e;
            ApplicationExceptionBean faultInfo = expected.getFaultInfo();
            assertNotNull(faultInfo);
            assertEquals("ex.PaymentInformationException",
                    faultInfo.getMessageKey());
        }
        assertNotNull("Exception expected", expected);
    }

    @Test
    public void subscribeToChargeableService() throws Exception {
        createdSubscription = new VOSubscription();
        createdSubscription.setSubscriptionId(Long.toHexString(System
                .currentTimeMillis()));
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, chargeableService, null,
                customerPaymentInfo, customerBillingContact,
                new ArrayList<VOUda>());
        assertEquals(customerUser.getUserId(), createdSubscription.getOwnerId());
    }

    @Test
    public void subscribeToChargeableService_WrongUser() throws Exception {
        SubscriptionService subSvc = ServiceFactory.getDefault()
                .getSubscriptionService();
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(Long.toHexString(System
                .currentTimeMillis()));
        OperationNotPermittedException expected = null;

        try {
            subSvc.subscribeToService(subscription, chargeableService, null,
                    customerPaymentInfo, customerBillingContact,
                    new ArrayList<VOUda>());
        } catch (OperationNotPermittedException e) {
            expected = e;
            ApplicationExceptionBean faultInfo = expected.getFaultInfo();
            assertNotNull(faultInfo);
            assertEquals("ex.OperationNotPermittedException",
                    faultInfo.getMessageKey());
        }

        assertNotNull("Exception expected", expected);

    }

    @Test
    public void subscribeToChargeableService_WrongService() throws Exception {
        SubscriptionService subSvc = ServiceFactory.getDefault()
                .getSubscriptionService();
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(Long.toHexString(System
                .currentTimeMillis()));
        VOService dummy = new VOService();
        ObjectNotFoundException expected = null;

        try {
            subSvc.subscribeToService(subscription, dummy, null,
                    customerPaymentInfo, customerBillingContact,
                    new ArrayList<VOUda>());
        } catch (ObjectNotFoundException e) {
            expected = e;
            ApplicationExceptionBean faultInfo = expected.getFaultInfo();
            assertNotNull(faultInfo);
            assertEquals("ex.ObjectNotFoundException.SERVICE",
                    faultInfo.getMessageKey());
            assertNotNull(faultInfo.getMessageParams());
            assertEquals(1, faultInfo.getMessageParams().length);
            assertEquals("0", faultInfo.getMessageParams()[0]);
            assertEquals(ClassEnum.SERVICE, expected.getDomainObjectClassEnum());
        }

        assertNotNull("Exception expected", expected);

    }

    @Test
    public void subscribeToChargeableService_DuplicateID() throws Exception {
        VOSubscription subscription = new VOSubscription();
        String subscriptionId = Long.toHexString(System.currentTimeMillis());
        subscription.setSubscriptionId(subscriptionId);

        createdSubscription = subscrServiceForCustomer.subscribeToService(
                subscription, chargeableService, null, customerPaymentInfo,
                customerBillingContact, new ArrayList<VOUda>());

        NonUniqueBusinessKeyException expected = null;
        try {
            subscrServiceForCustomer.subscribeToService(subscription,
                    chargeableService, null, customerPaymentInfo,
                    customerBillingContact, new ArrayList<VOUda>());
        } catch (NonUniqueBusinessKeyException e) {
            expected = e;
            ApplicationExceptionBean faultInfo = expected.getFaultInfo();
            assertNotNull(faultInfo);
            assertEquals("ex.NonUniqueBusinessKeyException.SUBSCRIPTION",
                    faultInfo.getMessageKey());
            assertNotNull(faultInfo.getMessageParams());
            assertEquals(1, faultInfo.getMessageParams().length);
            assertEquals(createdSubscription.getSubscriptionId(),
                    faultInfo.getMessageParams()[0]);
            assertEquals(ClassEnum.SUBSCRIPTION,
                    expected.getDomainObjectClassEnum());
        }

        assertNotNull("Exception expected", expected);

    }

    @Test
    public void getCustomersForSubscriptionId() throws Exception {
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        // Check the customers
        List<VOOrganization> customers = subscrServiceForSupplier
                .getCustomersForSubscriptionId(createdSubscription
                        .getSubscriptionId());
        assertEquals("Wrong numer of customers", 1, customers.size());
        assertEquals("Wrong customer organization ID",
                customerOrg.getOrganizationId(), customers.get(0)
                        .getOrganizationId());
    }

    @Test
    public void getCustomersSubscriptionForSupplier() throws Exception {
        createdSubscription = createSubscription();
        subscrServiceForCustomer.subscribeToService(createdSubscription,
                freeService, usageLicences, null, null, new ArrayList<VOUda>());

        List<VOSubscriptionIdAndOrganizations> customerSubscriptions = subscrServiceForSupplier
                .getCustomerSubscriptions();

        assertEquals("Wrong numer of customer subscriptions", 1,
                customerSubscriptions.size());
        assertEquals("Wrong subscriptionId",
                createdSubscription.getSubscriptionId(), customerSubscriptions
                        .get(0).getSubscriptionId());
        assertEquals("Wrong customer organization ID",
                customerOrg.getOrganizationId(), customerSubscriptions.get(0)
                        .getOrganizations().get(0).getOrganizationId());
    }

    @Test
    public void getSubscriptionForCustomer() throws Exception {
        createdSubscription = createSubscription();
        subscrServiceForCustomer.subscribeToService(createdSubscription,
                freeService, usageLicences, null, null, new ArrayList<VOUda>());
        VOSubscriptionDetails subscriptionDetails = subscrServiceForSupplier
                .getSubscriptionForCustomer(customerOrg.getOrganizationId(),
                        createdSubscription.getSubscriptionId());
        assertNotNull(subscriptionDetails);
        List<VOUsageLicense> usageLicences = subscriptionDetails
                .getUsageLicenses();
        assertEquals("Wrong number of usage licenses", 1, usageLicences.size());
        assertEquals("Wrong userId", customerUser.getUserId(), usageLicences
                .get(0).getUser().getUserId());
    }

    @Test
    public void getSubscriptionForCustomerUnauthorizedOrg() throws Exception {
        // given
        createdSubscription = createSubscription();
        subscrServiceForCustomer.subscribeToService(createdSubscription,
                freeService, usageLicences, null, null, new ArrayList<VOUda>());
        String customerOrgId = customerOrg.getOrganizationId();

        try {
            // when: get subscription details
            subscrServiceForReseller.getSubscriptionForCustomer(customerOrgId,
                    createdSubscription.getSubscriptionId());
            fail("OperationNotPermittedException expected");
        } catch (OperationNotPermittedException e) {
            // then
            assertTrue(
                    "Wrong Exception message",
                    e.getMessage().contains(
                            "Organization '" + reseller.getOrganizationId()
                                    + "' is not reseller of customer '"
                                    + customerOrgId + "'"));
        }
    }

    @Test
    public void getSubscriptionForCustomerWrongOrgId() throws Exception {
        try {
            subscrServiceForSupplier.getSubscriptionForCustomer("WRONG_ORG_ID",
                    "4711");
            fail("ObjectNotFoundException expected");
        } catch (ObjectNotFoundException e) {
            assertTrue(
                    "Wrong Exception message",
                    e.getMessage()
                            .contains(
                                    "Could not find object of type 'ORGANIZATION' with business key 'WRONG_ORG_ID'"));
        }
    }

    @Test
    public void getSubscriptionForCustomerWrongSubscriptionId()
            throws Exception {
        try {
            subscrServiceForSupplier.getSubscriptionForCustomer(
                    customerOrg.getOrganizationId(), "WRONG_SUBSCRIPTION_ID");
            fail("ObjectNotFoundException expected");
        } catch (ObjectNotFoundException e) {
            assertTrue(
                    "Wrong Exception message",
                    e.getMessage()
                            .contains(
                                    "Could not find object of type 'SUBSCRIPTION' with business key 'WRONG_SUBSCRIPTION_ID'"));
        }
    }

    @Test
    public void getSubscriptionIdentifiersForSupplier() throws Exception {
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        // Check the subscription id's
        List<String> subscriptions = subscrServiceForSupplier
                .getSubscriptionIdentifiers();
        assertEquals("Wrong numer of subscriptions", 1, subscriptions.size());
        assertEquals("Wrong subscription ID",
                createdSubscription.getSubscriptionId(), subscriptions.get(0));
    }

    @Test
    public void getSubscriptionsForCurrentUser() throws Exception {
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        List<VOUserSubscription> subscriptions = subscrServiceForCustomer
                .getSubscriptionsForCurrentUser();
        assertEquals("Wrong numer of subscriptions", 1, subscriptions.size());
        assertEquals("Wrong subscription id",
                createdSubscription.getSubscriptionId(), subscriptions.get(0)
                        .getSubscriptionId());
        assertEquals("Wrong supplier org name", SUPPLIER_ORG_NAME,
                subscriptions.get(0).getSellerName());
        assertEquals("Wrong user id", customerUser.getUserId(), subscriptions
                .get(0).getLicense().getUser().getUserId());
        assertEquals("Wrong user org id", customerOrg.getOrganizationId(),
                subscriptions.get(0).getLicense().getUser().getOrganizationId());

        // The supplier itself has no subscription
        subscriptions = subscrServiceForSupplier
                .getSubscriptionsForCurrentUser();
        assertEquals("Wrong numer of subscriptions", 0, subscriptions.size());
    }

    @Test
    public void getSubscriptionsForUser() throws Exception {
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        List<VOUserSubscription> subscriptions = subscrServiceForCustomer
                .getSubscriptionsForUser(customerUser);
        assertEquals("Wrong numer of subscriptions", 1, subscriptions.size());
        assertEquals("Wrong subscription id",
                createdSubscription.getSubscriptionId(), subscriptions.get(0)
                        .getSubscriptionId());
        assertEquals("Wrong supplier org name", SUPPLIER_ORG_NAME,
                subscriptions.get(0).getSellerName());
        assertEquals("Wrong user id", customerUser.getUserId(), subscriptions
                .get(0).getLicense().getUser().getUserId());
        assertEquals("Wrong user org id", customerOrg.getOrganizationId(),
                subscriptions.get(0).getLicense().getUser().getOrganizationId());
    }

    @Test
    public void getSubscriptionsForUserDifferentOrganization() throws Exception {
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        // Get the subscription with a user, that belongs to a different
        // organization
        try {
            subscrServiceForSupplier.getSubscriptionsForUser(customerUser);
            fail("OperationNotPermittedException expected");
        } catch (OperationNotPermittedException e) {
            String exMsg = String.format(
                    "User '%s' does not belong to organization '%s'",
                    customerUser.getUserId(), supplier.getOrganizationId());
            assertTrue("Wrong exception message", e.getMessage()
                    .contains(exMsg));
        }
    }

    @Test
    public void getSubscriptionsForOrganization() throws Exception {
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        List<VOSubscription> subscriptions = subscrServiceForCustomer
                .getSubscriptionsForOrganization();

        int activeSubscriptions = 0;
        VOSubscription activeSubscription = null;
        for (VOSubscription subscr : subscriptions) {
            if (subscr.getStatus().equals(SubscriptionStatus.ACTIVE)) {
                activeSubscriptions++;
                activeSubscription = subscr;
            }
        }

        assertEquals("Wrong numer of active subscriptions", 1,
                activeSubscriptions);
        if (activeSubscription != null) {
            assertEquals("Wrong subscription id",
                    createdSubscription.getSubscriptionId(),
                    activeSubscription.getSubscriptionId());
            assertEquals("Wrong supplier org name", SUPPLIER_ORG_NAME,
                    activeSubscription.getSellerName());
            assertEquals("Wrong number of users", 1,
                    activeSubscription.getNumberOfAssignedUsers());
        } else {
            fail("No active subscription");
        }

        // The supplier org itself has no active subscription
        subscriptions = subscrServiceForSupplier
                .getSubscriptionsForOrganization();

        activeSubscriptions = 0;
        for (VOSubscription subscr : subscriptions) {
            if (subscr.getStatus().equals(SubscriptionStatus.ACTIVE)) {
                activeSubscriptions++;
            }
        }

        assertEquals("Wrong numer of active subscriptions", 0,
                activeSubscriptions);
    }

    @Test
    public void getSubscriptionsForOrganizationWithFilter() throws Exception {
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        Set<SubscriptionStatus> requiredStatus = new HashSet<SubscriptionStatus>();
        requiredStatus.add(SubscriptionStatus.SUSPENDED);
        requiredStatus.add(SubscriptionStatus.EXPIRED);
        requiredStatus.add(SubscriptionStatus.PENDING);
        List<VOSubscription> subscriptions = subscrServiceForCustomer
                .getSubscriptionsForOrganizationWithFilter(requiredStatus);
        assertEquals("Wrong numer of subscriptions", 0, subscriptions.size());

        requiredStatus.add(SubscriptionStatus.ACTIVE);
        subscriptions = subscrServiceForCustomer
                .getSubscriptionsForOrganizationWithFilter(requiredStatus);
        assertEquals("Wrong numer of subscriptions", 1, subscriptions.size());
        assertEquals("Wrong subscription id",
                createdSubscription.getSubscriptionId(), subscriptions.get(0)
                        .getSubscriptionId());
        assertEquals("Wrong supplier org name", SUPPLIER_ORG_NAME,
                subscriptions.get(0).getSellerName());
        assertEquals("Wrong number of users", 1, subscriptions.get(0)
                .getNumberOfAssignedUsers());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getServiceRolesForSubscription_SubscriptionManagerNoPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subId = createdSubscription.getSubscriptionId();

        // when
        subscrServiceForSubManager.getServiceRolesForSubscription(subId);
    }

    @Test
    public void getServiceRolesForSubscription_SubscriptionManagerPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForSubManager.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subId = createdSubscription.getSubscriptionId();

        // when
        List<VORoleDefinition> roleDefList = subscrServiceForSubManager
                .getServiceRolesForSubscription(subId);

        // then
        assertEquals(0, roleDefList.size());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getSubscriptionDetails_SubscriptionManagerNoPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subId = createdSubscription.getSubscriptionId();

        // when
        subscrServiceForSubManager.getSubscriptionDetails(subId);
    }

    @Test
    public void getSubscriptionDetails_SubscriptionManagerPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForSubManager.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subId = createdSubscription.getSubscriptionId();

        // when
        VOSubscription subDetails = subscrServiceForSubManager
                .getSubscriptionDetails(subId);

        // then
        assertEquals(createdSubscription.getSubscriptionId(),
                subDetails.getSubscriptionId());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getUpgradeOptions_SubscriptionManagerNoPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subId = createdSubscription.getSubscriptionId();

        // when
        subscrServiceForSubManager.getUpgradeOptions(subId);
    }

    @Test
    public void getUpgradeOptions_SubscriptionManagerPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForSubManager.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subId = createdSubscription.getSubscriptionId();

        // when
        List<VOService> serviceListSubManager = subscrServiceForSubManager
                .getUpgradeOptions(subId);

        // then
        List<VOService> serviceListAdmin = subscrServiceForCustomer
                .getUpgradeOptions(subId);
        assertEquals(serviceListAdmin.size(), serviceListSubManager.size());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void modifySubscriptionPaymentData_SubscriptionManagerNoPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        // when
        subscrServiceForSubManager.modifySubscriptionPaymentData(
                createdSubscription, new VOBillingContact(),
                new VOPaymentInfo());
    }

    @Test
    public void modifySubscriptionPaymentData_SubscriptionManagerPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();

        createdSubscription = subscrServiceForSubManager.subscribeToService(
                createdSubscription, chargeableService, usageLicences,
                customerPaymentInfo, customerBillingContact,
                new ArrayList<VOUda>());
        VOBillingContact newBillingContact = factory.createBillingContactVO();
        newBillingContact = accountServiceForCustomer
                .saveBillingContact(newBillingContact);

        // when
        VOSubscriptionDetails voSubDetails = subscrServiceForSubManager
                .modifySubscriptionPaymentData(createdSubscription,
                        newBillingContact, customerPaymentInfo);

        // then
        assertEquals(newBillingContact.getId(), voSubDetails
                .getBillingContact().getId());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void addRevokeUser_SubscriptionManagerNoPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subId = createdSubscription.getSubscriptionId();

        // when
        subscrServiceForSubManager.addRevokeUser(subId,
                new ArrayList<VOUsageLicense>(), new ArrayList<VOUser>());
    }

    @Test
    public void addRevokeUser_SubscriptionManagerPermissions() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForSubManager.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subId = createdSubscription.getSubscriptionId();
        factory.createUsageLicenceVO(customerUser);

        // when
        subscrServiceForSubManager.addRevokeUser(subId,
                new ArrayList<VOUsageLicense>(),
                Arrays.asList((VOUser) customerUser));

        // then
        List<VOUserSubscription> subList = subscrServiceForCustomer
                .getSubscriptionsForUser(customerUser);
        assertEquals(0, subList.size());
    }

    @Test
    public void hasCurrentUserSubscriptions() throws Exception {
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        assertTrue("Current user has no subscriptions",
                subscrServiceForCustomer.hasCurrentUserSubscriptions());

        assertFalse("Supplier has subscriptions",
                subscrServiceForSupplier.hasCurrentUserSubscriptions());
    }

    @Test
    public void modifySubscription() throws Exception {
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        String newSubscriptionId = "NewSubscrId"
                + Long.toHexString(System.currentTimeMillis());
        createdSubscription.setSubscriptionId(newSubscriptionId);
        createdSubscription.setOwnerId(subscriptionManager.getUserId());
        VOSubscription modifiedSubscription = subscrServiceForCustomer
                .modifySubscription(createdSubscription, null,
                        new ArrayList<VOUda>());

        assertEquals(subscriptionManager.getUserId(),
                modifiedSubscription.getOwnerId());
        List<VOUserSubscription> subscriptions = subscrServiceForCustomer
                .getSubscriptionsForCurrentUser();

        int activeSubscriptions = 0;
        VOUserSubscription activeSubscription = null;
        for (VOUserSubscription subscr : subscriptions) {
            if (subscr.getStatus().equals(SubscriptionStatus.ACTIVE)) {
                activeSubscriptions++;
                activeSubscription = subscr;
            }
        }

        assertEquals("Wrong numer of subscriptions", 1, activeSubscriptions);
        if (activeSubscription != null) {
            assertEquals("Wrong subscription id", newSubscriptionId,
                    activeSubscription.getSubscriptionId());
        } else {
            fail("No active subscription");
        }
    }

    @Test
    public void modifyUnknownSubscription() throws Exception {

        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId("UNKNOWN_SUBSCRIPTION");

        try {
            subscrServiceForCustomer.modifySubscription(subscription, null,
                    new ArrayList<VOUda>());
            fail("ObjectNotFoundException expected");
        } catch (ObjectNotFoundException e) {
            assertTrue(
                    "Wrong exception message",
                    e.getMessage()
                            .contains(
                                    "Could not find object of type 'SUBSCRIPTION' with business key 'UNKNOWN_SUBSCRIPTION'"));
        }
    }

    @Test(expected = OperationPendingException.class)
    public void subscribeToService_TriggerProcessPending() throws Exception {
        VOTriggerDefinition triggerDef = WebserviceTestBase
                .createTriggerDefinition();
        triggerDef.setType(TriggerType.SUBSCRIBE_TO_SERVICE);
        setup.createTriggerDefinition(triggerDef);

        SubscriptionService subSvc = ServiceFactory.getDefault()
                .getSubscriptionService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        VOSubscription subscription = new VOSubscription();
        String subscriptionId = Long.toHexString(System.currentTimeMillis());
        subscription.setSubscriptionId(subscriptionId);
        subscription = subSvc.subscribeToService(subscription, freeService,
                null, null, null, new ArrayList<VOUda>());

        VOSubscription subscription2 = new VOSubscription();
        subscription2.setSubscriptionId(subscriptionId);

        subSvc.subscribeToService(subscription2, freeService, null, null, null,
                new ArrayList<VOUda>());
    }

    @Test
    public void unsubscribeFromService_TriggerProcessPending() throws Exception {
        // given
        VOTriggerDefinition triggerDef = WebserviceTestBase
                .createTriggerDefinition();
        triggerDef.setType(TriggerType.UNSUBSCRIBE_FROM_SERVICE);
        setup.createTriggerDefinition(triggerDef);

        SubscriptionService subSvc = ServiceFactory.getDefault()
                .getSubscriptionService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        VOSubscription subscription = new VOSubscription();
        String subscriptionId = Long.toHexString(System.currentTimeMillis());
        subscription.setSubscriptionId(subscriptionId);
        subscription = subSvc.subscribeToService(subscription, freeService,
                null, null, null, new ArrayList<VOUda>());
        subSvc.unsubscribeFromService(subscriptionId);

        try {
            // when: try to delete again the same subscription
            subSvc.unsubscribeFromService(subscriptionId);
            fail();
        } catch (OperationPendingException e) {
            // then
            assertNotNull("Exception expected", e);
        } finally {
            setup.deleteTriggersForUser();
            subSvc.unsubscribeFromService(subscriptionId);
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void unsubscribeFromService_SubscriptionManagerNoPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subId = createdSubscription.getSubscriptionId();

        // when
        subscrServiceForSubManager.unsubscribeFromService(subId);
    }

    @Test
    public void unsubscribeFromService_SubscriptionManagerPermissions()
            throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForSubManager.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subId = createdSubscription.getSubscriptionId();

        // when
        subscrServiceForSubManager.unsubscribeFromService(subId);

        // then
        Set<SubscriptionStatus> requiredStatus = new HashSet<SubscriptionStatus>();
        requiredStatus.add(SubscriptionStatus.DEACTIVATED);
        List<VOSubscription> subList = subscrServiceForSubManager
                .getSubscriptionsForOrganizationWithFilter(requiredStatus);
        assertNotNull(getSubscriptionInList(createdSubscription, subList));
    }

    @Test
    public void modifySubscription_TriggerProcessPending() throws Exception {
        // given
        VOTriggerDefinition triggerDef = WebserviceTestBase
                .createTriggerDefinition();
        triggerDef.setType(TriggerType.MODIFY_SUBSCRIPTION);
        setup.createTriggerDefinition(triggerDef);

        // add a pending subscription modification process
        SubscriptionService subSvc = ServiceFactory.getDefault()
                .getSubscriptionService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        VOSubscription subscription = new VOSubscription();
        String subscriptionId = Long.toHexString(System.currentTimeMillis());
        subscription.setSubscriptionId(subscriptionId);
        subscription = subSvc.subscribeToService(subscription, freeService,
                null, null, null, new ArrayList<VOUda>());
        subSvc.modifySubscription(subscription, new ArrayList<VOParameter>(),
                new ArrayList<VOUda>());

        try {
            // when: try to modify again the same subscription
            subSvc.modifySubscription(subscription,
                    new ArrayList<VOParameter>(), new ArrayList<VOUda>());
            fail();
        } catch (OperationPendingException e) {
            // then
            assertNotNull("Exception expected", e);
        } finally {
            setup.deleteTriggersForUser();
            subSvc.unsubscribeFromService(subscriptionId);
        }
    }

    @Test
    public void upgradeSubscription_TriggerProcessPending() throws Exception {
        // given
        freeService = setup.deactivateService(freeService);
        ServiceProvisioningService spsSvc = ServiceFactory.getDefault()
                .getServiceProvisioningService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        spsSvc.setCompatibleServices(freeService,
                Collections.singletonList(chargeableService));
        freeService = setup.activateService(freeService);

        // create a trigger definition for customer
        VOTriggerDefinition triggerDef = WebserviceTestBase
                .createTriggerDefinition();
        triggerDef.setType(TriggerType.UPGRADE_SUBSCRIPTION);
        setup.createTriggerDefinition(triggerDef,
                String.valueOf(customerUser.getKey()));

        // customer subscribes to service
        VOSubscription subscription = new VOSubscription();
        String subscriptionId = Long.toHexString(System.currentTimeMillis());
        subscription.setSubscriptionId(subscriptionId);
        subscription = subscrServiceForCustomer.subscribeToService(
                subscription, freeService, null, null, null,
                new ArrayList<VOUda>());

        // add a pending subscription upgrade process
        subscrServiceForCustomer.upgradeSubscription(subscription,
                chargeableService, customerPaymentInfo, customerBillingContact,
                new ArrayList<VOUda>());

        try {
            // when: try to upgrade again the same subscription
            subscrServiceForCustomer.upgradeSubscription(subscription,
                    chargeableService, customerPaymentInfo,
                    customerBillingContact, new ArrayList<VOUda>());
            fail();
        } catch (OperationPendingException e) {
            // then
            assertNotNull("Exception expected", e);
        } finally {
            setup.deleteTriggersForUser(String.valueOf(customerUser.getKey()));
            subscrServiceForCustomer.unsubscribeFromService(subscriptionId);
        }
    }

    @Test
    public void checkReceivedMailContent_CustomerLine() throws Exception {
        // given
        WebserviceTestBase.getMailReader().deleteMails();
        createdSubscription = subscribe();

        // when: call the reportIssue method
        subscrServiceForCustomer.reportIssue(subscriptionID, mailSubjectToSend,
                mailContentToSend);

        // then
        mailContentReceived = readContentFromServiceTicketMail();
        assertNotNull(mailContentReceived);
        checkTag("[Customer]", customerOrg.getName(),
                customerOrg.getOrganizationId());
    }

    @Test
    public void checkReceivedMailContent_SubscriptionLine() throws Exception {
        WebserviceTestBase.getMailReader().deleteMails();
        createdSubscription = subscribe();
        // call the reportIssue method
        subscrServiceForCustomer.reportIssue(subscriptionID, mailSubjectToSend,
                mailContentToSend);

        mailContentReceived = readContentFromServiceTicketMail();
        assertNotNull(mailContentReceived);
        checkTag("[Subscription]", createdSubscription.getSubscriptionId(),
                createdSubscription.getServiceInstanceId());
    }

    @Test
    public void checkReceivedMailContent_ServiceLine() throws Exception {
        WebserviceTestBase.getMailReader().deleteMails();
        createdSubscription = subscribe();
        // call the reportIssue method
        subscrServiceForCustomer.reportIssue(subscriptionID, mailSubjectToSend,
                mailContentToSend);

        mailContentReceived = readContentFromServiceTicketMail();
        assertNotNull(mailContentReceived);
        checkTag("[Marketable Service]", freeService.getName(),
                freeService.getServiceId());
    }

    @Test
    public void checkReceivedMailContent_TechnicalServicLine() throws Exception {
        WebserviceTestBase.getMailReader().deleteMails();
        createdSubscription = subscribe();
        // call the reportIssue method
        subscrServiceForCustomer.reportIssue(subscriptionID, mailSubjectToSend,
                mailContentToSend);

        mailContentReceived = readContentFromServiceTicketMail();
        assertNotNull(mailContentReceived);
        checkTag("[Technical Service]", freeService.getTechnicalId(), setup
                .getVoTechnicalService().getTechnicalServiceId(), setup
                .getVoTechnicalService().getTechnicalServiceBuildId());
    }

    @Test
    public void checkReceivedMailContent_ContentLine() throws Exception {
        WebserviceTestBase.getMailReader().deleteMails();
        createdSubscription = subscribe();
        // call the reportIssue method
        subscrServiceForCustomer.reportIssue(subscriptionID, mailSubjectToSend,
                mailContentToSend);

        mailContentReceived = readContentFromServiceTicketMail();
        assertNotNull(mailContentReceived);
        assertTrue(mailContentReceived.contains(mailContentToSend));
    }

    @Test
    public void reportIssueNoEmail() throws Exception {
        WebserviceTestBase.getMailReader().deleteMails();
        createdSubscription = subscribe();

        // set the supportMail of supplier is ""
        OperatorService operatorService = WebserviceTestBase.getOperator();
        VOOperatorOrganization operatorOrg = operatorService
                .getOrganization(supplier.getOrganizationId());
        operatorOrg.setSupportEmail("");
        operatorService.updateOrganization(operatorOrg, null);

        // call the reportIssue method
        subscrServiceForCustomer.reportIssue(subscriptionID, mailSubjectToSend,
                mailContentToSend);

        // read mail from email address and check the content
        String mailContent = readContentFromServiceTicketMail();
        assertNotNull(mailContent);
    }

    @Test
    public void reportIssueChangeEmail() throws Exception {
        WebserviceTestBase.getMailReader().deleteMails();
        createdSubscription = subscribe();

        // change the supportMail address to another one
        OperatorService operatorService = WebserviceTestBase.getOperator();
        VOOperatorOrganization operatorOrg = operatorService
                .getOrganization(supplier.getOrganizationId());
        operatorOrg.setSupportEmail(supplier.getEmail());
        operatorService.updateOrganization(operatorOrg, null);

        // call the reportIssue method
        subscrServiceForCustomer.reportIssue(subscriptionID, mailSubjectToSend,
                mailContentToSend);

        // read mail from new email address and check the content
        String mailContent = readContentFromServiceTicketMail();
        assertNotNull(mailContent);

    }

//    @Test(expected = ClientTransportException.class)
//    public void reportIssueForUserNotAdmin() throws Exception {
//        WebserviceTestBase.getMailReader().deleteMails();
//        // create new user with no ORGANIZATION_ADMIN role
//        IdentityService identityService = ServiceFactory.getDefault()
//                .getIdentityService();
//        VOUserDetails voUser = factory.createUserVO(WebserviceTestBase
//                .createUniqueKey());
//        voUser.setOrganizationId(customerOrg.getOrganizationId());
//        List<UserRoleType> emptyList = new ArrayList<UserRoleType>();
//        emptyList.add(UserRoleType.MARKETPLACE_OWNER);
//        identityService.createUser(voUser, emptyList, null);
//
//        // get subscriptionService for the new user
//        SubscriptionService subscrService = ServiceFactory.getDefault()
//                .getSubscriptionService(String.valueOf(voUser.getKey()),
//                        WebserviceTestBase.DEFAULT_PASSWORD);
//        VOSubscription subscription = createSubscription();
//
//        // call the reportIssue method
//        subscrService.reportIssue(subscription.getSubscriptionId(),
//                mailSubjectToSend, mailContentToSend);
//    }

    @Test(expected = ObjectNotFoundException.class)
    public void reportIssueForUserNotCustomer() throws Exception {

        WebserviceTestBase.getMailReader().deleteMails();
        // create subscription
        VOSubscription subscription = createSubscription();
        String subscriptionID = subscription.getSubscriptionId();

        // create new user of supplier
        IdentityService identityService = ServiceFactory.getDefault()
                .getIdentityService();
        VOUserDetails voUser = factory.createUserVO(WebserviceTestBase
                .createUniqueKey());
        voUser.setOrganizationId(supplier.getOrganizationId());
        List<UserRoleType> userRoles = new ArrayList<UserRoleType>();
        userRoles.add(UserRoleType.ORGANIZATION_ADMIN);
        userRoles.add(UserRoleType.MARKETPLACE_OWNER);
        identityService.createUser(voUser, userRoles, null);
        voUser = identityService.getUserDetails(voUser);

        // get the subscriptionService for new user
        SubscriptionService subscrService = ServiceFactory.getDefault()
                .getSubscriptionService(
                        String.valueOf(WebserviceTestBase
                                .readLastMailAndSetCommonPassword(voUser.getUserId())),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        // User does not have a subscription with this given id
        // --> ObjectNotFoundException
        subscrService.reportIssue(subscriptionID, mailSubjectToSend,
                mailContentToSend);
    }

    @Test(expected = ValidationException.class)
    public void reportIssueTooLongContent() throws Exception {
        // create subscription
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subscriptionID = createdSubscription.getSubscriptionId();

        // a very long content with more than 2000 a
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 2001; i++) {
            sb.append("a");
        }
        String longContent = sb.toString();

        // call the reportIssue method
        subscrServiceForCustomer.reportIssue(subscriptionID, mailSubjectToSend,
                longContent);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void reportIssueSubscriptionNotFound() throws Exception {
        // call the reportIssue method with "AnyID" as subscriptionID
        subscrServiceForCustomer.reportIssue("AnyID", mailSubjectToSend,
                mailContentToSend);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void reportIssue_SubsciptionManagerNoPermissions() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subscriptionID = createdSubscription.getSubscriptionId();

        // when
        subscrServiceForSubManager.reportIssue(subscriptionID,
                mailSubjectToSend, "someText");
    }

    @Test
    public void reportIssue_SubsciptionManagerPermissions() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForSubManager.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());
        String subscriptionID = createdSubscription.getSubscriptionId();

        // when
        subscrServiceForSubManager.reportIssue(subscriptionID,
                mailSubjectToSend, "someText");
    }

    @Test
    public void getServiceOperationParameterValues() throws Exception {
        VOTechnicalServiceOperation operation = tpWithOp
                .getTechnicalServiceOperations().get(0);
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, svcWithOp, usageLicences, null, null,
                new ArrayList<VOUda>());

        List<VOServiceOperationParameterValues> values = subscrServiceForCustomer
                .getServiceOperationParameterValues(createdSubscription,
                        operation);

        assertEquals(1, values.size());
        VOServiceOperationParameterValues op = values.get(0);
        assertEquals("SERVER", op.getParameterId());
        assertEquals(OperationParameterType.REQUEST_SELECT, op.getType());
        assertTrue(op.isMandatory());
        assertEquals(Arrays.asList("Server 1", "Server 2", "Server 3"),
                op.getValues());
    }

    @Test
    public void executeServiceSubscription() throws Exception {
        // given
        VOTechnicalServiceOperation operation = tpWithOp
                .getTechnicalServiceOperations().get(0);
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, svcWithOp, usageLicences, null, null,
                new ArrayList<VOUda>());

        operation.getOperationParameters().get(0).setParameterValue("Server 1");
        operation.getOperationParameters().get(1).setParameterValue("Comment");

        // when
        subscrServiceForCustomer.executeServiceOperation(createdSubscription,
                operation);

    }

    @Test(expected = ValidationException.class)
    public void executeServiceSubscription_MandatoryValueMissing()
            throws Exception {
        // given
        VOTechnicalServiceOperation operation = tpWithOp
                .getTechnicalServiceOperations().get(0);
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, svcWithOp, usageLicences, null, null,
                new ArrayList<VOUda>());
        operation.getOperationParameters().get(0).setParameterValue("");

        // when
        subscrServiceForCustomer.executeServiceOperation(createdSubscription,
                operation);

    }

    @Test(expected = ValidationException.class)
    public void executeServiceSubscription_MandatoryValueIsNull()
            throws Exception {
        // given
        VOTechnicalServiceOperation operation = tpWithOp
                .getTechnicalServiceOperations().get(0);
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, svcWithOp, usageLicences, null, null,
                new ArrayList<VOUda>());
        operation.getOperationParameters().get(0).setParameterValue(null);

        // when
        subscrServiceForCustomer.executeServiceOperation(createdSubscription,
                operation);

    }

    @Test
    public void updateAccessInfo() throws Exception {
        // given
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                new ArrayList<VOUda>());

        VOInstanceInfo instanceInfo = givenInstanceInfo();

        // when
        subscrServiceForSupplier.updateAccessInformation(
                createdSubscription.getSubscriptionId(),
                customerOrg.getOrganizationId(), instanceInfo);

        // then
        VOSubscription subDetails = subscrServiceForCustomer
                .getSubscriptionDetails(createdSubscription.getSubscriptionId());

        assertInstanceInfoSet(instanceInfo, subDetails);
    }

    /**
     * Read the content from the last service ticket mail from the server.
     * 
     * @throws Exception
     */
    String readContentFromServiceTicketMail() throws Exception {
        return WebserviceTestBase.getMailReader()
                .getLastMailContentWithSubject(MAIL_SUBJECT_SERVICE_TICKET_EN);
    }

    /**
     * Read the tag from the received mail content and check for the given
     * values.
     */
    void checkTag(String tag, String... values) {
        int idx = mailContentReceived.indexOf(tag);
        if (idx < 0)
            fail(mailContentReceived + " " + " does not contain required tag"
                    + tag + ".");

        String line = mailContentReceived.substring(idx);
        if (line.indexOf('\n') > 0) {
            line = line.substring(0, line.indexOf('\n'));
        }
        for (String value : values) {
            assertTrue(line + " " + " does not contain required value " + value
                    + ".", line.contains(value));
        }
    }

    private void verifyUdas(List<VOUda> udas) throws Exception {
        // get the udas has been saved
        List<VOUda> resultUdas = accountService.getUdas(
                UdaTargetType.CUSTOMER.name(), customerOrg.getKey());
        // compare the uda's value with the created value
        assertEquals(udas.get(0).getTargetObjectKey(), resultUdas.get(0)
                .getTargetObjectKey());
        assertEquals(udas.get(0).getUdaValue(), resultUdas.get(0).getUdaValue());
        assertEquals(udas.get(0).getVersion(), resultUdas.get(0).getVersion());
    }

    private void verifySubscriptionAndAuditLogs(VOSubscription subscription,
            VOService freeService, byte[] auditLog) throws Exception {
        String auditLogs = new String(auditLog, "UTF-8");
        assertNotNull(auditLogs);
        assertNotNull(subscription);
        assertEquals(
                Boolean.TRUE,
                new Boolean(
                        auditLogs.contains(subscription.getSubscriptionId())));
        assertEquals(Boolean.TRUE,
                new Boolean(auditLogs.contains(freeService.getServiceId())));
        assertEquals(Boolean.TRUE,
                new Boolean(auditLogs.contains(freeService.getName())));
    }

    private List<VOUda> prepareUdasForSave() throws Exception {
        // create udaDefinition and save it
        List<VOUdaDefinition> udaDefinitions = new ArrayList<VOUdaDefinition>();
        VOUdaDefinition udaDef = createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(),
                "Uda1" + Long.toHexString(System.currentTimeMillis()),
                "Value1", UdaConfigurationType.USER_OPTION_OPTIONAL);
        udaDefinitions.add(udaDef);
        accountService.saveUdaDefinitions(udaDefinitions,
                new ArrayList<VOUdaDefinition>());
        // get the saved udaDefinition
        udaDefinitions = accountService.getUdaDefinitions();
        // create uda object
        VOUda uda = createVOUda(udaDefinitions.get(0), "UdaValue1",
                customerOrg.getKey());
        List<VOUda> udasToSave = new ArrayList<VOUda>();
        udasToSave.add(uda);
        return udasToSave;
    }

    private VOUdaDefinition createVOUdaDefinition(String targetType,
            String udaId, String defaultValue,
            UdaConfigurationType configurationType) {
        VOUdaDefinition def = new VOUdaDefinition();
        def.setDefaultValue(defaultValue);
        def.setTargetType(targetType);
        def.setUdaId(udaId);
        def.setConfigurationType(configurationType);
        return def;
    }

    private VOUda createVOUda(VOUdaDefinition def, String value,
            long targetObjectKey) {
        VOUda uda = new VOUda();
        uda.setTargetObjectKey(targetObjectKey);
        uda.setUdaDefinition(def);
        uda.setUdaValue(value);
        return uda;
    }

    private VOSubscription createSubscription() {
        // Subscribe to service
        VOSubscription subscription = new VOSubscription();
        String subscriptionId = Long.toHexString(System.currentTimeMillis());
        subscription.setSubscriptionId(subscriptionId);

        return subscription;
    }

    /**
     * get the parameter definition by name from VOTechnicalService object
     * 
     * @param ts
     * @param name
     * @return
     */
    private VOParameterDefinition getParameterDefinitionByName(
            VOTechnicalService ts, String name) {
        List<VOParameterDefinition> paramDefinitions = ts
                .getParameterDefinitions();
        VOParameterDefinition parameterDefinition = new VOParameterDefinition();
        for (VOParameterDefinition voParamDef : paramDefinitions) {
            if (voParamDef.getParameterId().equals(name))
                parameterDefinition = voParamDef;
        }
        return parameterDefinition;
    }

    /**
     * create the technical service,create parameters,create marketplace service
     * and subscribe to it
     * 
     * @param paraValue
     * @return
     * @throws Exception
     */
    private VOSubscription createAndSubscribeToServiceWithParameter(
            String paraValue) throws Exception {
        technicalServiceWithParameter = createTechnicalService("tp1");
        List<VOParameter> params = createParametersForTechnicalService(
                technicalServiceWithParameter, paraValue);
        serviceWithParameter = createServiceWithParameter(params);
        createdSubscription = createSubscription();
        // subscription to a service and then modify it
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, serviceWithParameter, usageLicences, null,
                null, new ArrayList<VOUda>());
        return createdSubscription;
    }

    /**
     * create marketplace service with parameters
     * 
     * @param params
     * @return
     * @throws Exception
     */
    private VOService createServiceWithParameter(List<VOParameter> params)
            throws Exception {
        return setup.createAndActivateService("marketPlace",
                technicalServiceWithParameter, mpLocal, params);
    }

    /**
     * create the parameters for technical service
     * 
     * @param voTechnicalService
     * @param value
     * @return
     */
    private List<VOParameter> createParametersForTechnicalService(
            VOTechnicalService voTechnicalService, String value) {
        // create parameters and set the value
        VOParameter standardParam = createParameterForTechnicalService(
                voTechnicalService, value, standardDefinitionID);
        VOParameter onetimeParam = createParameterForTechnicalService(
                voTechnicalService, value, onetimeDefinitionID);
        // return these parameters
        List<VOParameter> params = new ArrayList<VOParameter>();
        params.add(standardParam);
        params.add(onetimeParam);
        return params;
    }

    /**
     * create the parameters for technical service
     * 
     * @param voTechnicalService
     * @param value
     * @return
     */
    private VOParameter createParameterForTechnicalService(
            VOTechnicalService voTechnicalService, String value, String defID) {
        // get the parameter definition with definition id
        VOParameterDefinition standardDef = getParameterDefinitionByName(
                voTechnicalService, defID);
        // create parameter and set the value
        VOParameter standardParam = new VOParameter(standardDef);
        standardParam.setConfigurable(true);
        standardParam.setValue(value);
        return standardParam;
    }

    /**
     * create a technical service with the service id
     * 
     * @param serviceID
     * @return
     * @throws Exception
     */
    private VOTechnicalService createTechnicalService(String serviceID)
            throws Exception {
        return setup.createTechnicalServiceWithParameterDefinition(serviceID);
    }

    /**
     * set new name for Subscription to modify
     */
    private void setNewNameForSubscription() {
        String newSubscriptionId = "NewSubscrId"
                + Long.toHexString(System.currentTimeMillis());
        createdSubscription.setSubscriptionId(newSubscriptionId);
    }

    /**
     * get the parameter from subscription,set new value and return them
     * 
     * @param value
     * @return
     * @throws Exception
     */
    private List<VOParameter> getParametersForModifySubscription(String value)
            throws Exception {
        return getParametersForModifySubscription(0, value, createdSubscription);
    }

    /**
     * get the parameter from subscription,set new value and return them
     * 
     * @param index
     * @param value
     * @return
     * @throws Exception
     */
    private List<VOParameter> getParametersForModifySubscription(int index,
            String value, VOSubscription subscription) throws Exception {
        VOParameter param = getParameterFromSubscription(index, subscription);
        param.setValue(value);
        List<VOParameter> paramsForUpdate = new ArrayList<VOParameter>();
        paramsForUpdate.add(param);
        return paramsForUpdate;
    }

    /**
     * get the parameter from subscription
     * 
     * @param index
     * @param subscription
     * @return
     * @throws Exception
     */
    private VOParameter getParameterFromSubscription(int index,
            VOSubscription subscription) throws Exception {
        VOSubscriptionDetails voSubscriptionDetails = subscrServiceForCustomer
                .getSubscriptionDetails(subscription.getSubscriptionId());
        VOParameter param = voSubscriptionDetails.getSubscribedService()
                .getParameters().get(index);
        return param;
    }

    /**
     * create VOUserDetail object
     */
    private static VOUserDetails createVOUser() throws Exception {
        VOUserDetails voUser = factory.createUserVO(Long.toHexString(System
                .currentTimeMillis()));
        voUser.setOrganizationId(customerOrg.getOrganizationId());
        voUser.setAdditionalName("additionalName");
        voUser.setAddress("address");
        voUser.setFirstName("firstName");
        voUser.setLastName("lastName");
        voUser.setPhone("08154711");
        voUser.setSalutation(Salutation.MR);
        return voUser;
    }

    private void refreshSubscriptionDetails() throws ObjectNotFoundException,
            OperationNotPermittedException {
        createdSubscription = subscrServiceForCustomer
                .getSubscriptionDetails(createdSubscription.getSubscriptionId());
    }

    private VOInstanceInfo givenInstanceInfo() {
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setAccessInfo("Access info");
        instanceInfo.setBaseUrl("https://esttest:8181/int1");
        instanceInfo.setInstanceId("instanceId");
        instanceInfo.setLoginPath("/login");
        return instanceInfo;
    }

    private void assertInstanceInfoSet(VOInstanceInfo info, VOSubscription sub) {
        assertEquals(info.getBaseUrl(), sub.getServiceBaseURL());
        assertEquals(info.getAccessInfo(), sub.getServiceAccessInfo());
        assertEquals(info.getInstanceId(), sub.getServiceInstanceId());
        assertEquals(info.getLoginPath(), sub.getServiceLoginPath());
    }

}
