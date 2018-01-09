/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.03.2010                                                      
 *                                                                              
 *  Completion Time: 10.03.2010                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.auditlog.bean.AuditLogServiceBean;
import org.oscm.auditlog.dao.AuditLogDao;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.bean.IdManagementStub;
import org.oscm.interceptor.AuditLogDataInterceptor;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.provisioning.data.User;
import org.oscm.sessionservice.bean.SessionManagementStub2;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.operations.SendMailHandler;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.data.UserRoles;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.ApplicationServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.TaskQueueServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.types.enumtypes.EmailType;

/**
 * Test class considering with user handling related scenarios in the context of
 * a subscription.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class SubscriptionServiceBeanUserHandlingIT extends EJBTestBase {

    private SubscriptionService subMgmt;
    private DataService mgr;
    private ConfigurationServiceStub configurationService;
    private PlatformUser currentUser;
    private int mailCounter = 0;
    private Organization supplier;
    private Organization customer;
    private Subscription subscription;
    private List<PlatformUser> users;
    private boolean throwTechnicalProductNotAliveException = false;
    private boolean throwTechnicalProductOperationFailed = false;

    @Override
    public void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        container.enableInterfaceMocking(true);
        container.addBean(new AuditLogDao());
        container.addBean(new AuditLogServiceBean());
        container.addBean(new AuditLogDataInterceptor());
        container.addBean(new DataServiceBean() {
            @Override
            public PlatformUser getCurrentUser() {
                return currentUser;
            }
        });
        container.addBean(new ApplicationServiceStub() {
            @Override
            public User[] createUsers(Subscription subscription,
                    List<UsageLicense> usageLicenses)
                    throws TechnicalServiceNotAliveException,
                    TechnicalServiceOperationException {
                if (throwTechnicalProductNotAliveException) {
                    throw new TechnicalServiceNotAliveException(
                            TechnicalServiceNotAliveException.Reason.CONNECTION_REFUSED);
                }
                if (throwTechnicalProductOperationFailed) {
                    throw new TechnicalServiceOperationException(
                            "User caused exception");
                }
                return null;
            }

            @Override
            public void deleteUsers(Subscription subscription,
                    List<UsageLicense> licenses) {
            }
        });
        container.addBean(new SessionManagementStub2());
        container.addBean(new IdManagementStub());
        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new CommunicationServiceStub() {
            @Override
            public void sendMail(PlatformUser recipient, EmailType type,
                    Object[] params, Marketplace marketplace) {
                mailCounter++;
            }
        });
        container.addBean(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromBundle(
                    LocalizedObjectTypes objectType, Marketplace shop,
                    String localeString, String key) {
                return "";
            }

        });
        container.addBean(new AccountServiceStub());
        container
                .addBean(configurationService = new ConfigurationServiceStub());
        container.addBean(new TaskQueueServiceStub() {
            @Override
            public void sendAllMessages(List<TaskMessage> messages) {
                for (TaskMessage message : messages) {
                    if (message.getHandlerClass() == SendMailHandler.class) {
                        SendMailPayload payload = (SendMailPayload) messages
                                .get(0).getPayload();
                        mailCounter = mailCounter
                                + payload.getMailObjects().size();
                    }
                }
            }
        });
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                TriggerProcessMessageData data = new TriggerProcessMessageData(
                        new TriggerProcess(), null);
                return Collections.singletonList(data);
            }

            @Override
            public void sendAllNonSuspendingMessages(
                    List<TriggerMessage> messageData) {
            }
        });
        container.addBean(mock(SubscriptionListServiceBean.class));
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new TerminateSubscriptionBean());
        container.addBean(new ManageSubscriptionBean());

        mgr = container.get(DataService.class);
        subMgmt = container.get(SubscriptionService.class);

        configurationService.setConfigurationSetting(ConfigurationKey.BASE_URL,
                "http://here");

        initEnvironment();
        container.login(currentUser.getKey());

    }

    /**
     * No mails must be sent in case of exception. Refers to bug 4993. The
     * constraint that at least one user must be subscribed has been removed.
     * The test case has been adapted. Now, the test case tries to remove a
     * non-existing user and verifies that no mail is sent.
     * 
     * @throws Exception
     */
    @Test
    public void testAddRevokeUserExceptionCaseCheckMails() throws Exception {
        // assign both users to the subscription
        container.login(String.valueOf(users.get(0).getKey()),
                ROLE_ORGANIZATION_ADMIN);
        List<VOUsageLicense> usersToBeAdded = addUsersToSubscription();
        final String subId = subscription.getSubscriptionId();
        subMgmt.addRevokeUser(subId, usersToBeAdded, null);
        Assert.assertEquals("Wrong number of mails sent", 2, mailCounter);

        // revoke both of the users
        mailCounter = 0;
        List<VOUser> usersToRemove = new ArrayList<>();
        for (PlatformUser user : users) {
            usersToRemove.add(UserDataAssembler.toVOUser(user));
        }

        // and non-existing user
        VOUser nonExistingUser = new VOUser();
        nonExistingUser.setKey(1234);
        usersToRemove.add(nonExistingUser);

        try {
            subMgmt.addRevokeUser(subId, null, usersToRemove);
            Assert.fail(
                    "Removal of a non existing user from a subscription must not work");
        } catch (ObjectNotFoundException ignore) {
            // expected
        }
        int numOfUsers = getNumberOfSubUsers();
        Assert.assertEquals(
                "Wrong number of users. Must not be changed by a txn that was rolled back",
                2, numOfUsers);

        // check mails
        Assert.assertEquals(
                "Wrong number of mails sent. None must be sent as txn is rolled back.",
                0, mailCounter);
    }

    // refers to bug 5229
    @Test
    public void testAddRevokeUserUnavailableServiceOpFailed() throws Exception {
        container.login(String.valueOf(users.get(0).getKey()),
                ROLE_ORGANIZATION_ADMIN);

        // now add users to the subscription, ensuring that the call to
        // the application fails
        String subId = subscription.getSubscriptionId();
        List<VOUsageLicense> usersToBeAdded = addUsersToSubscription();

        throwTechnicalProductOperationFailed = true;
        try {
            subMgmt.addRevokeUser(subId, usersToBeAdded, null);
            Assert.fail(
                    "Call to service must not succeed, as it should be pretended that it is not available");
        } catch (TechnicalServiceOperationException e) {
            // expected
        }
        verifyUserChangesAndMails(0, 0);
    }

    // refers to bug 5229
    @Test
    public void testAddRevokeUserUnavailableServiceNotAlive() throws Exception {
        container.login(String.valueOf(users.get(0).getKey()),
                ROLE_ORGANIZATION_ADMIN);

        // now add users to the subscription, ensuring that the call to
        // the application fails
        String subId = subscription.getSubscriptionId();
        List<VOUsageLicense> usersToBeAdded = addUsersToSubscription();

        throwTechnicalProductNotAliveException = true;
        try {
            subMgmt.addRevokeUser(subId, usersToBeAdded, null);
            Assert.fail(
                    "Call to service must not succeed, as it should be pretended that it is not available");
        } catch (TechnicalServiceNotAliveException e) {
            verifyUserChangesAndMails(0, 0);
        }

    }

    /**
     * Adds the globally available users to the subscription.
     * 
     * @return The currently assigned users for the subscription.
     */
    private List<VOUsageLicense> addUsersToSubscription() {
        List<VOUsageLicense> list = new ArrayList<>();

        for (PlatformUser user : users) {
            VOUsageLicense lic = new VOUsageLicense();
            lic.setUser(UserDataAssembler.toVOUser(user));
            list.add(lic);
        }
        return list;
    }

    /**
     * Verifies that the number of users and mails matches the given values.
     * 
     * @param expectedNumberOfUsers
     *            The expected number of values assigned to the subscription.
     * @param expectedNumberOfMails
     *            The expected number of mail received.
     */
    private void verifyUserChangesAndMails(int expectedNumberOfUsers,
            int expectedNumberOfMails) throws Exception {
        int numOfUsers = getNumberOfSubUsers();
        Assert.assertEquals("Number of assigned users must not have changed",
                expectedNumberOfUsers, numOfUsers);

        // no mail must have been sent
        Assert.assertEquals(
                "Wrong number of mails sent. None must be sent as txn is rolled back.",
                expectedNumberOfMails, mailCounter);
    }

    /**
     * Creates a technical product, a marketing product, a subscription, an
     * organization and two users for the organization.
     */
    private void initEnvironment() throws Exception {
        // create organization roles
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                return null;
            }
        });

        supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                return supplier;
            }
        });

        customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization customer = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                OrganizationReference ref = new OrganizationReference(supplier,
                        customer,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                mgr.persist(ref);
                return customer;
            }
        });

        // create a technical product
        final TechnicalProduct tp = runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, supplier, "testTP", false,
                        ServiceAccessType.LOGIN);
                return tp;
            }
        });

        // Create a marketing product
        final Product product = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product prod = Products.createProduct(supplier, tp, false,
                        "testMP", null, mgr);
                return prod;
            }
        });

        subscription = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Subscription sub = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), product.getProductId(),
                        "testSub", supplier);
                return sub;
            }
        });

        users = runTX(new Callable<List<PlatformUser>>() {
            @Override
            public List<PlatformUser> call() throws Exception {
                List<PlatformUser> users = new ArrayList<>();
                users.add(Organizations.createUserForOrg(mgr, customer, true,
                        "user1"));
                users.add(Organizations.createUserForOrg(mgr, customer, true,
                        "user2"));

                return users;
            }
        });

        users.get(0).setAssignedRoles(UserRoles.createRoleAssignments(
                users.get(0), UserRoleType.ORGANIZATION_ADMIN));
        currentUser = users.get(0);
    }

    /**
     * Reads the subscription from the database and determines the number of
     * assigned users.
     * 
     * @return The number of assigned users.
     */
    private int getNumberOfSubUsers() throws Exception {
        // check number of users of subscription
        int numOfUsers = runTX(new Callable<Integer>() {
            @Override
            public Integer call() {
                Subscription storedSub = mgr.find(Subscription.class,
                        subscription.getKey());
                return Integer.valueOf(storedSub.getUsageLicenses().size());
            }
        }).intValue();
        return numOfUsers;
    }
}
