/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: kulle                                         
 *                                                                              
 *  Creation Date: 25.10.2011                                                      
 *                                                                              
 *  Completion Time: 25.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ws.concurrency;

import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.TSXMLForWebService;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.converter.api.VOConverter;
import org.oscm.intf.AccountService;
import org.oscm.intf.IdentityService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.intf.SubscriptionService;
import org.oscm.intf.TriggerDefinitionService;
import org.oscm.types.enumtypes.PaymentInfoType;
import org.oscm.types.enumtypes.TriggerTargetType;
import org.oscm.types.enumtypes.TriggerType;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.vo.VOBillingContact;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationPaymentConfiguration;
import org.oscm.vo.VOPaymentInfo;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTechnicalService;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUserDetails;

/**
 * The test creates x subscriptions. For each of the subscription a thread is
 * created which will call the add/revoke user action.
 * 
 * @author kulle
 * 
 */
public class JmsConnectionsTest {

    // thread stuff
    private volatile boolean showDebug = false;
    private static volatile int threadCounter = 0;
    private static final int NUMBER_OF_THREADS = 20;
    private static final int NUMBER_OF_THREAD_EXECUTIONS = 5;
    private final List<VOSubscription> subscriptionList = new CopyOnWriteArrayList<VOSubscription>();
    private List<Thread> threads = new ArrayList<Thread>();

    // business stuff
    private static final String CURRENCY_EUR = "EUR";
    private static final String SUPPLIER = "supplier_"
            + System.currentTimeMillis();
    private static final String MARKETPLACE = "marketplace_"
            + System.currentTimeMillis();
    private VOFactory factory = new VOFactory();
    private VOOrganization supplierOrganization;
    private String supplierOrganizationId;
    private String supplierAdminKey;
    private VOService service;
    private VOMarketplace marketplace;

    /**
     * Use to get uncaught exceptions
     * 
     * @author kulle
     */
    private class AddRevokeUserThreadGroup extends ThreadGroup {
        private Map<Thread, Throwable> uncaughtExceptions = Collections
                .synchronizedMap(new HashMap<Thread, Throwable>());

        public AddRevokeUserThreadGroup(String name) {
            super(name);
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            super.uncaughtException(t, e);
            uncaughtExceptions.put(t, e);
        }

        public Map<Thread, Throwable> getUncaughtExceptions() {
            return uncaughtExceptions;
        }
    }

    /**
     * @author kulle
     */
    private class AddRevokeUserThread extends Thread {
        private int executionTimes;
        private int threadNumber;
        private List<VOUserDetails> users = new ArrayList<VOUserDetails>();

        public AddRevokeUserThread(ThreadGroup threadGroup, int executionTimes)
                throws Exception {
            super(threadGroup, "AddRevokeUserTHREAD_" + (threadCounter));
            this.threadNumber = threadCounter++;
            this.executionTimes = executionTimes;
        }

        @Override
        public void run() {
            try {
                IdentityService identityService = ServiceFactory.getDefault()
                        .getIdentityService(supplierAdminKey,
                                WebserviceTestBase.DEFAULT_PASSWORD);
                SubscriptionService subscriptionService = ServiceFactory
                        .getDefault().getSubscriptionService(
                                String.valueOf(supplierAdminKey),
                                WebserviceTestBase.DEFAULT_PASSWORD);

                // get subscription
                VOSubscription voSubscription = subscriptionList
                        .get(threadNumber);

                // register new user
                for (int i = 0; i < executionTimes; i++) {
                    String name = this.getName() + "_U"
                            + System.currentTimeMillis();
                    VOUserDetails userDetails = registerUser(identityService,
                            name);
                    users.add(userDetails);
                    List<VOUsageLicense> usageLicenses = new ArrayList<VOUsageLicense>();
                    usageLicenses.add(createUsageLicense(userDetails));
                    printMessage("registered user " + name);

                    subscriptionService.addRevokeUser(
                            voSubscription.getSubscriptionId(), usageLicenses,
                            null);
                    printMessage("added user " + name + " to subscription");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Before
    public void setup() throws MessagingException, Exception {
        WebserviceTestBase.getMailReader().deleteMails();

        // add currency
        WebserviceTestBase.getOperator().addCurrency(CURRENCY_EUR);

        // OPERATOR: create supplier
        VOUserDetails supplierAdmin = factory.createUserVO("admin"
                + System.currentTimeMillis());
        supplierOrganization = factory.createOrganizationVO();
        supplierOrganization.setName(SUPPLIER);

        org.oscm.internal.vo.VOOrganization internalVOOrg = VOConverter
                .convertToUp(supplierOrganization);
        internalVOOrg.setOperatorRevenueShare(BigDecimal.ZERO);

        supplierOrganization = VOConverter
                .convertToApi(WebserviceTestBase
                        .getOperator()
                        .registerOrganization(
                                internalVOOrg,
                                null,
                                VOConverter.convertToUp(supplierAdmin),
                                null,
                                null,
                                org.oscm.internal.types.enumtypes.OrganizationRoleType.TECHNOLOGY_PROVIDER,
                                org.oscm.internal.types.enumtypes.OrganizationRoleType.SUPPLIER));

        supplierAdminKey = WebserviceTestBase
                .readLastMailAndSetCommonPassword();
        supplierOrganizationId = supplierOrganization.getOrganizationId();
        supplierAdmin.setKey(Long.parseLong(supplierAdminKey));

        createTriggerDefinition();

        // OPERATOR: enable payment type invoice for suppliers
        WebserviceTestBase.savePaymentInfoToSupplier(supplierOrganization,
                PaymentInfoType.INVOICE);

        // SUPPLIER: enable payment type invoice
        AccountService accountService = ServiceFactory.getDefault()
                .getAccountService(supplierAdminKey,
                        WebserviceTestBase.DEFAULT_PASSWORD);
        Set<VOPaymentType> defaultPaymentTypes = accountService
                .getDefaultPaymentConfiguration();
        VOPaymentType voPaymentType = WebserviceTestBase.getPaymentTypeVO(
                accountService.getAvailablePaymentTypesForOrganization(),
                PaymentInfoType.INVOICE);
        defaultPaymentTypes.add(voPaymentType);
        VOOrganizationPaymentConfiguration c = new VOOrganizationPaymentConfiguration();
        c.setEnabledPaymentTypes(defaultPaymentTypes);
        c.setOrganization(supplierOrganization);
        accountService.savePaymentConfiguration(defaultPaymentTypes,
                Collections.singletonList(c), defaultPaymentTypes, null);

        // OPERATOR: create "Marketplace"
        MarketplaceService srvMarketplace = ServiceFactory.getDefault()
                .getMarketPlaceService();
        marketplace = srvMarketplace.createMarketplace(factory
                .createMarketplaceVO(supplierOrganization.getOrganizationId(),
                        false, MARKETPLACE));

        // SUPPLIER: create technical service
        ServiceProvisioningService serviceProvisioningService = ServiceFactory
                .getDefault().getServiceProvisioningService(supplierAdminKey,
                        WebserviceTestBase.DEFAULT_PASSWORD);
        String tsxml = TSXMLForWebService
                .createTSXMLWithAllowingOnBehalfActingConnectable(Boolean.FALSE
                        .toString());
        VOTechnicalService techSrv = WebserviceTestBase.createTechnicalService(
                tsxml, serviceProvisioningService);

        // SUPPLIER: create marketable service
        VOServiceDetails serviceDetails = serviceProvisioningService
                .createService(
                        techSrv,
                        factory.createMarketableServiceVO("service" + "_"
                                + WebserviceTestBase.createUniqueKey()), null);
        VOPriceModel priceModel = factory.createPriceModelVO(CURRENCY_EUR);
        priceModel.setOneTimeFee(BigDecimal.valueOf(100));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(40));
        serviceDetails = serviceProvisioningService.savePriceModel(
                serviceDetails, priceModel);

        // SUPPLIER: publish service & activate
        srvMarketplace = ServiceFactory.getDefault().getMarketPlaceService(
                supplierAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);
        WebserviceTestBase.publishToMarketplace(serviceDetails, true,
                srvMarketplace, marketplace);
        service = serviceProvisioningService.activateService(serviceDetails);

        // create subscriptions
        SubscriptionService subscriptionService = ServiceFactory.getDefault()
                .getSubscriptionService(String.valueOf(supplierAdminKey),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            subscriptionList.add(createSubscription(accountService,
                    subscriptionService, "subscription" + i));
        }
    }

    private VOSubscription createSubscription(AccountService accountService,
            SubscriptionService subscriptionService, String name)
            throws Exception {
        // billing contact
        List<VOPaymentInfo> paymentInfos = accountService.getPaymentInfos();
        VOPaymentInfo voPaymentInfo = paymentInfos.get(0);
        VOBillingContact voBillingContact = accountService
                .saveBillingContact(factory.createBillingContactVO());

        // create subscription
        VOSubscription s = factory.createSubscriptionVO(name);
        List<VOUsageLicense> users = new ArrayList<VOUsageLicense>();
        return subscriptionService.subscribeToService(s, service, users,
                voPaymentInfo, voBillingContact, new ArrayList<VOUda>());

    }

    private void createTriggerDefinition() throws ObjectNotFoundException,
            OrganizationAuthoritiesException, Exception {

        VOTriggerDefinition triggerDefinition = new VOTriggerDefinition();
        triggerDefinition.setType(TriggerType.ADD_REVOKE_USER);
        triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDefinition
                .setTarget("http://estbesdev2:8680/oscm-integrationtests-mockproduct/NotificationService?wsdl");
        triggerDefinition.setSuspendProcess(true);

        TriggerDefinitionService service = ServiceFactory.getDefault()
                .getTriggerDefinitionService(supplierAdminKey,
                        WebserviceTestBase.DEFAULT_PASSWORD);
        service.createTriggerDefinition(triggerDefinition);

    }

    private VOUserDetails registerUser(IdentityService identityService,
            String name) throws Exception {
        VOUserDetails voUser = factory.createUserVO(name);
        voUser.setOrganizationId(supplierOrganizationId);
        identityService.createUser(voUser,
                Arrays.asList(UserRoleType.TECHNOLOGY_MANAGER), null);
        voUser = identityService.getUserDetails(voUser);
        return voUser;
    }

    private VOUsageLicense createUsageLicense(VOUserDetails userDetails) {
        VOUsageLicense lic = new VOUsageLicense();
        lic.setUser(userDetails);
        // lic.setRoleDefinition(role);
        return lic;
    }

    @Test
    @Ignore
    public void testBug7654() throws Exception {
        // create add-revoke-user-threads
        AddRevokeUserThreadGroup threadGroup = new AddRevokeUserThreadGroup(
                "CreationThreads");
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            AddRevokeUserThread thread = new AddRevokeUserThread(threadGroup,
                    NUMBER_OF_THREAD_EXECUTIONS);
            threads.add(thread);
        }

        // execute all threads
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads.get(i).start();
        }

        // wait for threads to die (interrupted or reached number of execution
        // times)
        try {
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                threads.get(i).join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // check thread group for uncaught exceptions
        if (!threadGroup.getUncaughtExceptions().isEmpty()) {
            final StringBuffer errMsg = new StringBuffer();
            for (Thread t : threadGroup.getUncaughtExceptions().keySet()) {
                Throwable e = threadGroup.getUncaughtExceptions().get(t);
                errMsg.append("EXCEPTION FOR THREAD :");
                errMsg.append(t.getName());
                errMsg.append("\n");
                errMsg.append(WebserviceTestBase.convertStacktrace(e));
                errMsg.append("\n");
            }
            fail(errMsg.toString());
        }

    }

    private void printMessage(String message) {
        if (showDebug) {
            System.out.println(message);
        }

    }

}
