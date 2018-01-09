/*******************************************************************************
 *                                                                              

 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *  Completion Time: 12.02.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.oscm.test.Numbers.L1;
import static org.oscm.test.Numbers.L100;
import static org.oscm.test.Numbers.L200;
import static org.oscm.test.Numbers.L300;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.assembler.PaymentTypeAssembler;
import org.oscm.accountservice.dao.PaymentTypeDao;
import org.oscm.accountservice.dao.UserLicenseDao;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Discount;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationSetting;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductToPaymentType;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.bean.IdentityServiceBean;
import org.oscm.identityservice.bean.LdapAccessStub;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.interceptor.DateFactory;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.DistinguishedNameException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ImageException;
import org.oscm.internal.types.exception.IncompatibleRolesException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.ServiceParameterException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.LdapProperties;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VODiscount;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServicePaymentConfiguration;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.marketplace.assembler.MarketplaceAssembler;
import org.oscm.marketplace.bean.MarketplaceServiceBean;
import org.oscm.marketplace.bean.MarketplaceServiceLocalBean;
import org.oscm.reviewservice.bean.ReviewServiceLocalBean;
import org.oscm.reviewservice.dao.ProductReviewDao;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.subscriptionservice.bean.SubscriptionListServiceBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.EJBTestBase;
import org.oscm.test.MailDetails;
import org.oscm.test.data.Discounts;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PaymentInfos;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.data.UserRoles;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ApplicationServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.ImageResourceServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.PaymentServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.test.stubs.TaskQueueServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.usergroupservice.auditlog.UserGroupAuditLogCollector;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.usergroupservice.dao.UserGroupDao;
import org.oscm.usergroupservice.dao.UserGroupUsersDao;

@SuppressWarnings("boxing")
public class AccountServiceBeanIT extends EJBTestBase {

    private static final String PROV_ADMIN = "ProvAdmin";
    private static final String ORGANIZATION_EMAIL = "kundenbetreuung@bmw.de";
    private static final String ORGANIZATION_EMAIL_NEW = "info@bmw.de";
    private static final String ORGANIZATION_NAME = "Bayerische Motoren Werke Aktiengesellschaft";
    private static final String ORGANIZATION_ADDRESS = "Petuelring 130, 80788 M??nchen";
    private static final String ORGANIZATION_PHONE = "01802 / 32 42 52";
    private static final String ORGANIZATION_DOMICILE = "DE";
    private static final String ORGANIZATION_LOCALE = "de";
    private static final String ORGANIZATION_DESCRIPTION = "BMW, die Autos aus Muenchen";
    private static final String PAYMENT_INFO_NAME = "Rechnung";

    private DataService mgr;

    private AccountService accountMgmt;
    private AccountServiceLocal accountMgmtLocal;
    private ConfigurationServiceLocal cfg;
    private IdentityService idManagement;
    private LocalizerServiceLocal localizer;
    private ImageResourceServiceLocal imgSrv;

    private String organizationId;

    private final List<String> supplierIds = new ArrayList<>();

    private String providerId;

    private PlatformUser technologyProviderUser;

    private PlatformUser supplier1User;
    private PlatformUser supplier2User;
    private PlatformUser customerUser;

    private SupportedCurrency supportedCurrency;

    private boolean setInvoiceAsDefaultPayment;

    private List<PaymentType> paymentTypes;

    private boolean isTriggerQueueService_sendSuspendingMessageCalled = false;
    private boolean isTriggerQueueService_sendAllNonSuspendingMessageCalled = false;
    private TriggerType usedTriggerType;

    private boolean instanceActivated;
    private boolean instanceDeactivated;

    private int numberLocalizedFields = 0;
    private boolean isNullLocalizedFieldValue;
    private final LocalizedObjectTypes[] passedObjType = new LocalizedObjectTypes[2];
    @SuppressWarnings("unchecked")
    private final List<VOLocalizedText>[] passedLocalizedTexts = (List<VOLocalizedText>[]) Array
            .newInstance(List.class, 2);

    private final List<MailDetails<PlatformUser>> sendedMails = new LinkedList<>();
    private boolean throwMailOperationException = false;
    private MarketingPermissionServiceLocal mpMock;
    private LdapSettingsManagementServiceLocal ldapSettingMmgtMock;
    private SubscriptionAuditLogCollector subscriptionAuditLogCollector;
    private final String marketplaceId = "FUJITSU";

    private long subSupplier, subReseller;

    private final Set<VOPaymentType> pts = createVOPaymentTypes();

    private UserGroupServiceLocalBean userGroupServiceLocal;
    private UserGroupDao userGroupDao;

    @Captor
    ArgumentCaptor<Properties> storedProps;

    @Override
    public void setup(final TestContainer container) throws Exception {
        instanceActivated = false;
        instanceDeactivated = false;
        setInvoiceAsDefaultPayment = false;

        AESEncrypter.generateKey();

        container.login(1L);
        container.enableInterfaceMocking(true);

        container.addBean(new DataServiceBean());
        container.addBean(new PaymentTypeDao());
        container.addBean(mock(CommunicationServiceLocal.class));
        container.addBean(mock(ConfigurationServiceLocal.class));
        container.addBean(mock(UserLicenseDao.class));
        container.addBean(mock(UserLicenseServiceLocalBean.class));
        container.addBean(mock(TriggerQueueServiceLocal.class));
        container.addBean(new LocalizerServiceStub() {

            String value;

            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                if (LocalizedObjectTypes.PAYMENT_TYPE_NAME == objectType) {
                    return PAYMENT_INFO_NAME;
                }
                return value;
            }

            @Override
            public List<VOLocalizedText> getLocalizedValues(long objectKey,
                    LocalizedObjectTypes objectType) {

                List<VOLocalizedText> texts = new ArrayList<>();
                if (objectType == LocalizedObjectTypes.PAYMENT_TYPE_NAME) {
                    texts.add(new VOLocalizedText("en", PAYMENT_INFO_NAME));
                    texts.add(new VOLocalizedText("de", PAYMENT_INFO_NAME));
                    texts.add(new VOLocalizedText("ja", PAYMENT_INFO_NAME));
                }
                return texts;
            }

            @Override
            public void storeLocalizedResources(long objectKey,
                    LocalizedObjectTypes objectType,
                    List<VOLocalizedText> values) {
                passedObjType[numberLocalizedFields] = objectType;
                passedLocalizedTexts[numberLocalizedFields] = values;
                numberLocalizedFields++;
            }

            @Override
            public boolean storeLocalizedResource(String localeString,
                    long objectKey, LocalizedObjectTypes objectType,
                    String value) {
                this.value = value;
                isNullLocalizedFieldValue = (value == null);
                return true;
            }
        });
        container.addBean(new ProductReviewDao());
        container.addBean(new ReviewServiceLocalBean());
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(mock(UserGroupAuditLogCollector.class));
        userGroupDao = mock(UserGroupDao.class);
        container.addBean(userGroupDao);
        userGroupServiceLocal = mock(UserGroupServiceLocalBean.class);
        container.addBean(userGroupServiceLocal);
        container.addBean(new UserGroupUsersDao());
        container.addBean(new MarketplaceServiceLocalBean());
        container.addBean(new MarketplaceServiceBean());

        container.addBean(new ImageResourceServiceStub() {
            ImageResource saved;

            @Override
            public ImageResource read(long objectKey, ImageType imageType) {
                if (saved != null && saved.getObjectKey() == objectKey
                        && saved.getImageType() == imageType) {
                    return saved;
                }
                return null;
            }

            @Override
            public void save(ImageResource imageResource) {
                saved = imageResource;
            }

            @Override
            public void delete(long objectKey, ImageType imageType) {
                saved = null;
            }
        });

        imgSrv = container.get(ImageResourceServiceLocal.class);

        container.addBean(new ConfigurationServiceStub() {
            @Override
            public ConfigurationSetting getConfigurationSetting(
                    ConfigurationKey informationId, String contextId) {
                if (ConfigurationKey.SUPPLIER_SETS_INVOICE_AS_DEFAULT
                        .equals(informationId)) {

                    super.setConfigurationSetting(new ConfigurationSetting(
                            ConfigurationKey.SUPPLIER_SETS_INVOICE_AS_DEFAULT,
                            Configuration.GLOBAL_CONTEXT,
                            Boolean.valueOf(setInvoiceAsDefaultPayment)
                                    .toString()));
                }
                return super.getConfigurationSetting(informationId, contextId);
            }
        });

        container.addBean(new ApplicationServiceStub() {

            @Override
            public void activateInstance(Subscription subscription)
                    throws TechnicalServiceNotAliveException,
                    TechnicalServiceOperationException {
                instanceActivated = true;
            }

            @Override
            public void deactivateInstance(Subscription subscription)
                    throws TechnicalServiceNotAliveException,
                    TechnicalServiceOperationException {
                instanceDeactivated = true;
            }

        });

        container.addBean(new SessionServiceStub() {
            @Override
            public List<Session> getSessionsForUserKey(long platformUserKey) {
                return new ArrayList<>();
            }
        });

        container.addBean(new CommunicationServiceStub() {
            @Override
            public void sendMail(PlatformUser recipient, EmailType type,
                    Object[] params, Marketplace marketplace)
                    throws MailOperationException {
                if (throwMailOperationException) {
                    throw new MailOperationException("Mail could not be sent.");
                } else {
                    sendedMails.add(new MailDetails<>(recipient, type, params));
                }
            }

            @Override
            public SendMailStatus<PlatformUser> sendMail(EmailType type,
                    Object[] params, Marketplace marketplace,
                    PlatformUser... recipients) {
                SendMailStatus<PlatformUser> mailStatus = new SendMailStatus<>();
                for (PlatformUser recipient : recipients) {
                    try {
                        sendMail(recipient, type, params, marketplace);
                        mailStatus.addMailStatus(recipient);
                    } catch (MailOperationException e) {
                        mailStatus.addMailStatus(recipient, e);
                    }
                }
                return mailStatus;
            }

            @Override
            public String getMarketplaceUrl(String marketplaceId)
                    throws MailOperationException {
                return "";
            }
        });

        container.addBean(new LdapAccessStub());
        SubscriptionServiceLocal subService = mock(
                SubscriptionServiceLocal.class);

        doAnswer(new Answer<Subscription>() {
            @Override
            public Subscription answer(InvocationOnMock invocation)
                    throws ObjectNotFoundException {
                long subscriptionKey = (long) invocation.getArguments()[0];

                if (subscriptionKey == 0) {
                    throw new ObjectNotFoundException(ClassEnum.SUBSCRIPTION,
                            "");
                } else if (subscriptionKey > 1) {
                    return mgr.getReference(Subscription.class,
                            subscriptionKey);
                }
                Organization organization = new Organization();
                organization.setOrganizationId("BMW");
                Subscription subscription = new Subscription();
                subscription.setKey(subscriptionKey);
                subscription.setOrganization(organization);
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                return subscription;
            }
        }).when(subService).loadSubscription(anyLong());

        container.addBean(subService);
        container.addBean(new TaskQueueServiceStub() {

            @Override
            public void sendAllMessages(List<TaskMessage> messages) {
            }
        });

        MockitoAnnotations.initMocks(this);
        ldapSettingMmgtMock = mock(LdapSettingsManagementServiceLocal.class);
        container.addBean(ldapSettingMmgtMock);
        container.addBean(new IdentityServiceBean());
        container.addBean(new PaymentServiceStub() {
            @Override
            public void deregisterPaymentInPSPSystem(PaymentInfo payment) {
            }
        });
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public void sendAllNonSuspendingMessages(
                    List<TriggerMessage> messageData) {
                usedTriggerType = messageData.get(0).getTriggerType();
                isTriggerQueueService_sendAllNonSuspendingMessageCalled = true;
                super.sendAllNonSuspendingMessages(messageData);
            }

            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> triggerMessage) {
                if (!triggerMessage.isEmpty()) {
                    usedTriggerType = triggerMessage.get(0).getTriggerType();
                }
                isTriggerQueueService_sendSuspendingMessageCalled = true;
                return super.sendSuspendingMessages(triggerMessage);
            }
        });

        mpMock = mock(MarketingPermissionServiceLocal.class);
        container.addBean(mpMock);

        subscriptionAuditLogCollector = mock(
                SubscriptionAuditLogCollector.class);
        container.addBean(subscriptionAuditLogCollector);
        container.addBean(new AccountServiceBean());

        cfg = container.get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);

        mgr = container.get(DataService.class);
        accountMgmt = container.get(AccountService.class);
        accountMgmtLocal = container.get(AccountServiceLocal.class);
        idManagement = container.get(IdentityService.class);
        localizer = container.get(LocalizerServiceLocal.class);

        setupCountries();
        setupRoles();
        setupSupportedCurrency();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOrganizationRoles(mgr);
                paymentTypes = createPaymentTypes(mgr);
                return null;
            }
        });
        registerSupplier("admin");

        Organization organization = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.TECHNOLOGY_PROVIDER);
                technologyProviderUser = Organizations.createUserForOrg(mgr,
                        organization, true, PROV_ADMIN);
                return organization;
            }
        });
        providerId = organization.getOrganizationId();

        organization = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier1 = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                supplier1User = Organizations.createUserForOrg(mgr, supplier1,
                        true, "SuppAdmin1");
                Organizations.supportAllCountries(mgr, supplier1);

                Organization platformOperator = Organizations
                        .findOrganization(mgr, "PLATFORM_OPERATOR");
                OrganizationReference ref = new OrganizationReference(
                        platformOperator, supplier1,
                        OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);

                mgr.persist(ref);
                return supplier1;
            }
        });
        supplierIds.add(organization.getOrganizationId());

        organization = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier2 = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                supplier2User = Organizations.createUserForOrg(mgr, supplier2,
                        true, "SuppAdmin2");
                return supplier2;
            }
        });
        supplierIds.add(organization.getOrganizationId());

    }

    @Test
    public void testUpdateAccountInformation_DifferentEmail() throws Exception {
        sendedMails.clear();
        try {
            String oldEmail = "admin@organization.com";
            String newEmail = "enes.sejfi@est.fujitsu.com";
            updateAccountInformation(oldEmail, newEmail);

            assertEquals(2, sendedMails.size());
            checkEmail(0, newEmail);
            checkEmail(1, oldEmail);
        } finally {
            sendedMails.clear();
        }
    }

    @Test
    public void testUpdateAccountInformation_SameEmail() throws Exception {
        sendedMails.clear();
        try {
            String oldEmail = "admin@organization.com";
            String newEmail = new String(oldEmail);
            updateAccountInformation(oldEmail, newEmail);

            assertEquals(1, sendedMails.size());
            checkEmail(0, newEmail);
        } finally {
            sendedMails.clear();
        }
    }

    private void updateAccountInformation(String oldEmail, String changedEmail)
            throws Exception {

        supplier1User.setEmail(oldEmail);
        container.login(supplier1User.getKey(), ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setKey(supplier1User.getKey());
        user.setUserId(supplier1User.getUserId());
        user.setEMail(changedEmail);
        user.setOrganizationId(
                supplier1User.getOrganization().getOrganizationId());
        user.setLocale(supplier1User.getLocale());

        VOOrganization organization = new VOOrganization();
        organization.setOrganizationId(
                supplier1User.getOrganization().getOrganizationId());
        organization.setEmail(oldEmail);
        organization.setPhone("123456");
        organization.setUrl("http://www.example.com");
        organization.setName("example");
        organization.setAddress("an address");
        organization.setLocale(supplier1User.getLocale());
        organization.setVersion(supplier1User.getOrganization().getVersion());
        organization.setKey(supplier1User.getOrganization().getKey());
        organization.setDomicileCountry("DE");
        organization.setNameSpace("http://oscm.org/xsd/2.0");

        accountMgmt.updateAccountInformation(organization, user, "FUJITSU",
                null);
    }

    private void checkEmail(int index, String expectedEmail) {
        assertEquals(expectedEmail,
                sendedMails.get(index).getInstance().getEmail());

        assertEquals(EmailType.ORGANIZATION_UPDATED,
                sendedMails.get(index).getEmailType());

        assertNull(sendedMails.get(index).getParams());
    }

    private Organization registerSupplier(String adminUserId) throws Exception {
        // Create supplier for later registration
        final Organization supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization cust = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                Organizations.supportAllCountries(mgr, cust);
                return cust;
            }
        });

        VOOrganization organization = new VOOrganization();
        organization.setAddress(ORGANIZATION_ADDRESS);
        organization.setEmail(ORGANIZATION_EMAIL);
        organization.setName(ORGANIZATION_NAME);
        organization.setPhone(ORGANIZATION_PHONE);
        organization.setLocale(Locale.ENGLISH.toString());
        organization.setDomicileCountry(ORGANIZATION_DOMICILE);
        organization.setDescription(ORGANIZATION_DESCRIPTION);

        // organization.

        VOUserDetails admin = new VOUserDetails();
        admin.setUserId(adminUserId + "_" + supplier.getOrganizationId());
        admin.setEMail("harald.huber@bmw.de");
        admin.setFirstName("Harald");
        admin.setLastName("Huber");
        admin.setLocale(Locale.ENGLISH.toString());

        PlatformUser tmp = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(mgr, supplier, true,
                        "TempUser");
            }
        });
        container.login(String.valueOf(tmp.getKey()), ROLE_ORGANIZATION_ADMIN);

        runTX(new Callable<VOMarketplace>() {
            @Override
            public VOMarketplace call() throws Exception {
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(marketplaceId);
                if (mgr.find(mp) == null) {
                    Marketplaces.createMarketplace(supplier, marketplaceId,
                            false, mgr);
                }
                return MarketplaceAssembler.toVOMarketplace(mp,
                        new LocalizerFacade(localizer, "en"));
            }
        });
        organization = accountMgmt.registerCustomer(organization, admin,
                "admin", null, marketplaceId, supplier.getOrganizationId());
        organizationId = organization.getOrganizationId();

        VOUser user = idManagement.getUser(admin);
        container.login(String.valueOf(user.getKey()), ROLE_ORGANIZATION_ADMIN);

        return supplier;
    }

    /**
     * get the organization domain object directly from the data manager
     * 
     * @param organizationId
     *            the organizationId
     * @return the read organization domain object
     * @throws Exception
     */
    private Organization getOrganization(final String organizationId)
            throws Exception {
        Callable<DomainObject<?>> callable = new Callable<DomainObject<?>>() {
            @Override
            public DomainObject<?> call() {
                Organization custTmpl = new Organization();
                custTmpl.setOrganizationId(organizationId);
                try {
                    Organization org = (Organization) mgr
                            .getReferenceByBusinessKey(custTmpl);
                    org.getDomicileCountryCode(); // prefetch
                    for (OrganizationRoleType role : EnumSet
                            .allOf(OrganizationRoleType.class)) {
                        org.hasRole(role); // prefetch
                    }
                    org.getSources().size();
                    org.getTargets().size();
                    return org;
                } catch (ObjectNotFoundException e) {
                    // ignore
                }
                return null;
            }
        };
        return (Organization) runTX(callable);
    }

    @Test
    public void testLoadOrganizationImage() throws Exception {
        // given
        Organization supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.findOrganization(mgr, supplierIds.get(0));
            }
        });
        ImageResource imageResource = new ImageResource();
        byte[] content = this.getClass().getName().getBytes();
        imageResource.setBuffer(content);
        imageResource.setContentType("image/gif");
        imageResource.setImageType(ImageType.ORGANIZATION_IMAGE);
        imageResource.setObjectKey(supplier.getKey());
        imgSrv.save(imageResource);

        // execute
        VOImageResource loadImage = accountMgmt
                .loadImageOfOrganization(supplier.getKey());

        // assert
        Assert.assertEquals(imageResource.getContentType(),
                loadImage.getContentType());
        Assert.assertEquals(content, loadImage.getBuffer());
        Assert.assertEquals(imageResource.getImageType(),
                loadImage.getImageType());
    }

    @Test
    public void testLoadOrganizationImageNotAvailable() throws Exception {
        // given
        Organization supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.findOrganization(mgr, supplierIds.get(0));
            }
        });

        // execute
        VOImageResource imageResource = accountMgmt
                .loadImageOfOrganization(supplier.getKey());

        // assert
        assertNull(imageResource);
    }

    @Test
    public void testLoadOrganizationWrongImageType() throws Exception {
        // given
        Organization supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.findOrganization(mgr, supplierIds.get(0));
            }
        });
        ImageResource imageResource = new ImageResource();
        byte[] content = this.getClass().getName().getBytes();
        imageResource.setBuffer(content);
        imageResource.setContentType("image/gif");
        imageResource.setImageType(ImageType.SHOP_LOGO_BACKGROUND);
        imageResource.setObjectKey(supplier.getKey());
        imgSrv.save(imageResource);

        // execute
        VOImageResource loadImage = accountMgmt
                .loadImageOfOrganization(supplier.getKey());

        // assert
        assertNull(loadImage);
    }

    /**
     * Test for getting mail type.
     */
    @Test
    public void testGetMailType() {
        AccountServiceBean bean = new AccountServiceBean();
        Discount oldDiscount = new Discount();
        oldDiscount.setValue(new BigDecimal("100"));
        oldDiscount.setStartTime(L100);
        oldDiscount.setEndTime(L200);
        BigDecimal newDiscountValue = new BigDecimal("100");

        EmailType type = bean.getMailType(null, newDiscountValue, L100, L200);
        Assert.assertEquals(null, type);

        type = bean.getMailType(oldDiscount, newDiscountValue, L100, L200);
        Assert.assertEquals(null, type);

        oldDiscount.setValue(new BigDecimal("200"));
        type = bean.getMailType(oldDiscount, newDiscountValue, L100, L200);
        Assert.assertEquals(EmailType.ORGANIZATION_DISCOUNT_UPDATED, type);

        oldDiscount.setValue(new BigDecimal("100"));
        type = bean.getMailType(oldDiscount, newDiscountValue, L200, L200);
        Assert.assertEquals(EmailType.ORGANIZATION_DISCOUNT_UPDATED, type);

        type = bean.getMailType(oldDiscount, newDiscountValue, L100, L300);
        Assert.assertEquals(EmailType.ORGANIZATION_DISCOUNT_UPDATED, type);

        type = bean.getMailType(oldDiscount, newDiscountValue, L200, L300);
        Assert.assertEquals(EmailType.ORGANIZATION_DISCOUNT_UPDATED, type);
    }

    // Test section
    /**
     * Test for getting organization. Empty list is expected. No discount is
     * added yet.
     * 
     */
    @Test
    public void testGetOrganizationForDiscountEndNotificiation()
            throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                final long time = 1L;

                List<OrganizationReference> orgList = accountMgmtLocal
                        .getOrganizationForDiscountEndNotificiation(time);

                Assert.assertEquals(0, orgList.size());

                return null;
            }
        });
    }

    /**
     * Test for getting organization. One organization is expected.
     * 
     */
    @Test
    public void testGetOrganizationForDiscountEndNotificiationWithData()
            throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, 2010);
        calendar.set(Calendar.MONTH, Calendar.MAY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR, 1);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 1);

        final long currentTime = calendar.getTimeInMillis();

        // current time + 7 days
        calendar.add(Calendar.DAY_OF_MONTH, 7);

        final long endDiscountMillis = calendar.getTimeInMillis();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                final BigDecimal value = new BigDecimal("1.00");
                final Long startTime = L1;
                final Long endTime = Long.valueOf(endDiscountMillis);

                Discount discount = new Discount();

                Organization custTmpl = new Organization();
                custTmpl.setOrganizationId(organizationId);
                Organization org = (Organization) mgr
                        .getReferenceByBusinessKey(custTmpl);
                discount.setOrganizationReference(org.getSources().get(0));
                discount.setValue(value);
                discount.setStartTime(startTime);
                discount.setEndTime(endTime);
                mgr.persist(discount);
                mgr.flush();
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<OrganizationReference> orgList = accountMgmtLocal
                        .getOrganizationForDiscountEndNotificiation(
                                currentTime);

                Assert.assertEquals(1, orgList.size());

                return null;
            }
        });

    }

    /**
     * Test for getting organization. Empty list is expected. Too early to
     * inform about discount end
     * 
     */
    @Test
    public void testGetOrganizationForDiscountEndNotificiationWithDataNotInPeriod()
            throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, 2010);
        calendar.set(Calendar.MONTH, Calendar.MAY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR, 1);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 1);

        final long currentTime = calendar.getTimeInMillis();

        // current time + 7 days
        calendar.add(Calendar.DAY_OF_MONTH, 8);

        final long endDiscountMillis = calendar.getTimeInMillis();

        String orgId = organizationId;
        final Organization organization = getOrganization(orgId);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                final BigDecimal value = new BigDecimal("1.00");
                final Long startTime = L1;
                final Long endTime = Long.valueOf(endDiscountMillis);

                Discount discount = new Discount();

                discount.setOrganizationReference(
                        organization.getSources().get(0));
                discount.setValue(value);
                discount.setStartTime(startTime);
                discount.setEndTime(endTime);
                mgr.persist(discount);
                mgr.flush();
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<OrganizationReference> orgList = accountMgmtLocal
                        .getOrganizationForDiscountEndNotificiation(
                                currentTime);

                Assert.assertEquals(0, orgList.size());

                return null;
            }
        });

    }

    /**
     * Test for manage organization discount. Operation is not permitted.
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testUpdateCustomerDiscount_NotPermitted() throws Exception {

        String orgId = organizationId;
        final Organization org = getOrganization(orgId);

        final VOOrganization voOrganization = runTX(
                new Callable<VOOrganization>() {
                    @Override
                    public VOOrganization call() throws Exception {
                        Organization organization = mgr
                                .getReference(Organization.class, org.getKey());
                        return OrganizationAssembler.toVOOrganization(
                                organization, false,
                                new LocalizerFacade(localizer, "en"));
                    }
                });

        // log in with a supplier which is not the supplier of voOrganization
        container.login(supplier1User.getKey(), ROLE_SERVICE_MANAGER);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateCustomerDiscount(voOrganization);
                return null;
            }
        });
    }

    @Test
    public void testUpdateCustomerDiscount_CallerRoles() throws Exception {
        final VOOrganization voCustomer = runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                Organization supplier1 = Organizations.findOrganization(mgr,
                        supplierIds.get(0));
                Organization customer = Organizations.createCustomer(mgr,
                        supplier1);
                return OrganizationAssembler.toVOOrganization(customer, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });

        // login as service manager - update should be allowed
        container.login(supplier1User.getKey(), ROLE_SERVICE_MANAGER);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateCustomerDiscount(voCustomer);
                return null;
            }
        });
        // login as service manager - update should be allowed
        container.login(supplier1User.getKey(), ROLE_ORGANIZATION_ADMIN,
                ROLE_TECHNOLOGY_MANAGER);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    accountMgmt.updateCustomerDiscount(voCustomer);
                    return null;
                }
            });
            fail("Expected EJBAccessException saying that only a user having the SERVICE_MANAGER role can call it");
        } catch (EJBException e) {
            assertTrue(
                    "Expected cause must be an EJBAccessException saying that only a user having the SERVICE_MANAGER role can call it",
                    e.getCause() instanceof EJBAccessException);
        }
    }

    /**
     * Test for manage organization discount.
     */
    @Test
    public void testUpdateCustomerDiscount_CreateDiscount() throws Exception {
        final BigDecimal discountValue = new BigDecimal("1.00");
        Long discountBegin = L100;
        Long discountEnd = L200;

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization supplier1 = Organizations.findOrganization(mgr,
                        supplierIds.get(0));
                Organizations.createCustomer(mgr, supplier1);
                return null;
            }
        });

        // login as supplier
        container.login(supplier1User.getKey(), ROLE_SERVICE_MANAGER);

        List<VOOrganization> list = accountMgmt.getMyCustomers();
        assertNotNull(list);
        // supplier also customer
        assertEquals(2, list.size());
        final VOOrganization voOrganization = list.get(0);
        assertNull(voOrganization.getDiscount());

        VODiscount voDiscount = new VODiscount();
        voDiscount.setValue(discountValue);
        voDiscount.setStartTime(discountBegin);
        voDiscount.setEndTime(discountEnd);
        voOrganization.setDiscount(voDiscount);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateCustomerDiscount(voOrganization);
                return null;
            }
        });

        list = accountMgmt.getMyCustomers();
        assertNotNull(list);
        assertEquals(2, list.size());
        VOOrganization voOrganizationAfter = list.get(0);
        BigDecimal discountAfter = voOrganizationAfter.getDiscount().getValue();
        Long discountBeginAfter = voOrganizationAfter.getDiscount()
                .getStartTime();
        Long discountEndAfter = voOrganizationAfter.getDiscount().getEndTime();

        Assert.assertEquals(discountValue, discountAfter);
        Assert.assertEquals(discountBegin, discountBeginAfter);
        Assert.assertEquals(discountEnd, discountEndAfter);
    }

    /**
     * Test for manage organization discount.
     */
    @Test
    public void testUpdateCustomerDiscount_UpdateDiscount() throws Exception {
        final BigDecimal valueBefore = new BigDecimal("2.00");
        BigDecimal valueAfter = new BigDecimal("1.00");
        Long discountBegin = L100;
        Long discountEnd = L200;

        Organization supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier1 = Organizations.findOrganization(mgr,
                        supplierIds.get(0));
                Organization customer = Organizations.createCustomer(mgr,
                        supplier1);
                Discounts.createDiscount(mgr, customer.getSources().get(0),
                        valueBefore);

                return supplier1;
            }
        });

        assertNotNull(supplier);
        // login as supplier
        container.login(supplier1User.getKey(), ROLE_SERVICE_MANAGER);

        List<VOOrganization> list = accountMgmt.getMyCustomers();
        assertNotNull(list);
        assertEquals(2, list.size());
        VOOrganization initial_voOrganization = list.get(0);
        for (VOOrganization voOrg : list) {
            if (!voOrg.getOrganizationId()
                    .equals(supplier.getOrganizationId())) {
                initial_voOrganization = voOrg;
                break;
            }
        }
        final VOOrganization voOrganization = initial_voOrganization;
        assertNotNull(voOrganization);
        Assert.assertEquals(valueBefore,
                voOrganization.getDiscount().getValue());

        voOrganization.getDiscount().setValue(valueAfter);
        voOrganization.getDiscount().setStartTime(discountBegin);
        voOrganization.getDiscount().setEndTime(discountEnd);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateCustomerDiscount(voOrganization);
                return null;
            }
        });
        list = accountMgmt.getMyCustomers();
        assertNotNull(list);
        assertEquals(2, list.size());
        VOOrganization voOrganizationAfter = list.get(0);

        for (VOOrganization voOrg : list) {
            if (!voOrg.getOrganizationId()
                    .equals(supplier.getOrganizationId())) {
                voOrganizationAfter = voOrg;
                break;
            }
        }

        BigDecimal discountAfter = voOrganizationAfter.getDiscount().getValue();
        Long discountBeginAfter = voOrganizationAfter.getDiscount()
                .getStartTime();
        Long discountEndAfter = voOrganizationAfter.getDiscount().getEndTime();

        Assert.assertEquals(valueAfter, discountAfter);
        Assert.assertEquals(discountBegin, discountBeginAfter);
        Assert.assertEquals(discountEnd, discountEndAfter);
    }

    @Test
    public void testCreateAndChangeDiscount() throws Exception {
        BigDecimal valueBeforeInsert = new BigDecimal("1.00");
        BigDecimal valueAfterInsert = new BigDecimal("2.00");

        // create supplier
        Organization supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.findOrganization(mgr, supplierIds.get(0));
            }
        });
        assertNotNull(supplier);

        // login supplier with role service_manager
        container.login(supplier1User.getKey(), ROLE_SERVICE_MANAGER);

        // get specific customer of supplier
        List<VOOrganization> customerOfSupplier = accountMgmt.getMyCustomers();
        assertNotNull(customerOfSupplier);
        assertEquals(1, customerOfSupplier.size());

        final VOOrganization customer = customerOfSupplier.get(0);
        assertNotNull(customer);

        // set specific discount values
        VODiscount voDiscount = new VODiscount();
        voDiscount.setValue(valueBeforeInsert);
        customer.setDiscount(voDiscount);

        // create discount
        final VOOrganization customerAfterInsert = runTX(
                new Callable<VOOrganization>() {
                    @Override
                    public VOOrganization call() throws Exception {
                        return accountMgmt.updateCustomerDiscount(customer);
                    }
                });

        // check persisted discount values
        assertEquals(valueBeforeInsert,
                customerAfterInsert.getDiscount().getValue());
        assertEquals(0, customerAfterInsert.getDiscount().getVersion());
        assertTrue(customerAfterInsert.getDiscount().getKey() > 0);

        // change existing discount
        customerAfterInsert.getDiscount().setValue(valueAfterInsert);
        VOOrganization customerAfterChange = runTX(
                new Callable<VOOrganization>() {
                    @Override
                    public VOOrganization call() throws Exception {
                        return accountMgmt
                                .updateCustomerDiscount(customerAfterInsert);
                    }
                });

        // check changes discount values
        assertEquals(valueAfterInsert,
                customerAfterChange.getDiscount().getValue());
        assertEquals(1, customerAfterChange.getDiscount().getVersion());
    }

    /**
     * Test for manage organization discount.
     */
    @Test
    public void testUpdateCustomerDiscount_DeleteDiscount() throws Exception {
        final BigDecimal valueBefore = new BigDecimal("2.00");

        Organization supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier1 = Organizations.findOrganization(mgr,
                        supplierIds.get(0));
                Organization customer = Organizations.createCustomer(mgr,
                        supplier1);
                Discounts.createDiscount(mgr, customer.getSources().get(0),
                        valueBefore);

                return supplier1;
            }
        });

        assertNotNull(supplier);
        // login as supplier
        container.login(supplier1User.getKey(), ROLE_SERVICE_MANAGER);

        List<VOOrganization> list = accountMgmt.getMyCustomers();
        assertNotNull(list);
        assertEquals(2, list.size());
        VOOrganization initial_voOrganization = list.get(0);
        for (VOOrganization voOrg : list) {
            if (!voOrg.getOrganizationId()
                    .equals(supplier.getOrganizationId())) {
                initial_voOrganization = voOrg;
                break;
            }
        }
        final VOOrganization voOrganization = initial_voOrganization;
        assertNotNull(voOrganization);
        Assert.assertEquals(valueBefore,
                voOrganization.getDiscount().getValue());

        voOrganization.setDiscount(null);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateCustomerDiscount(voOrganization);
                return null;
            }
        });

        list = accountMgmt.getMyCustomers();
        assertNotNull(list);
        assertEquals(2, list.size());
        VOOrganization voOrganizationAfter = list.get(0);

        for (VOOrganization voOrg : list) {
            if (!voOrg.getOrganizationId()
                    .equals(supplier.getOrganizationId())) {
                voOrganizationAfter = voOrg;
                break;
            }
        }

        Assert.assertNull(voOrganizationAfter.getDiscount());
    }

    /**
     * Test for manage organization discount.
     */
    @Test
    public void testUpdateCustomerDiscount_WithoutDiscount() throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization supplier1 = Organizations.findOrganization(mgr,
                        supplierIds.get(0));
                Organizations.createCustomer(mgr, supplier1);
                return null;
            }
        });

        // login as supplier
        container.login(supplier1User.getKey(), ROLE_SERVICE_MANAGER);

        List<VOOrganization> list = accountMgmt.getMyCustomers();
        assertNotNull(list);
        assertEquals(2, list.size());
        final VOOrganization voCustomerOrganization = list.get(0);
        assertNull(voCustomerOrganization.getDiscount());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateCustomerDiscount(voCustomerOrganization);
                return null;
            }
        });

        VOOrganization voOrganizationAfter = accountMgmt.getMyCustomers()
                .get(0);

        Assert.assertNull(voOrganizationAfter.getDiscount());
    }

    @Test
    public void testRegisterCustomer_Description() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        VOOrganization org = new VOOrganization();
        org.setLocale(ORGANIZATION_LOCALE);
        org.setDomicileCountry(ORGANIZATION_DOMICILE);
        org.setDescription(ORGANIZATION_DESCRIPTION);

        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(Locale.ENGLISH.toString());
        user.setUserId("admin_" + new Random().nextInt());

        org = accountMgmt.registerCustomer(org, user, "admin", null,
                marketplaceId, null);

        assertEquals(ORGANIZATION_DESCRIPTION, org.getDescription());
    }

    @Test
    public void testRegisterOrganization_NullDescription() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        final VOOrganization voOrg = new VOOrganization();
        voOrg.setLocale(ORGANIZATION_LOCALE);
        voOrg.setDomicileCountry(ORGANIZATION_DOMICILE);

        final VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(Locale.ENGLISH.toString());
        user.setUserId("admin_" + new Random().nextInt());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = accountMgmtLocal.registerOrganization(
                        OrganizationAssembler.toCustomer(voOrg), null, user,
                        null, ORGANIZATION_DOMICILE, marketplaceId, null,
                        new OrganizationRoleType[0]);
                assertFalse(
                        "No localized resource must be created when description is null",
                        isNullLocalizedFieldValue);
                Assert.assertEquals(1, org.getPaymentInfos().size());
                PaymentInfo pi = org.getPaymentInfos().get(0);
                Assert.assertEquals(PaymentType.INVOICE,
                        pi.getPaymentType().getPaymentTypeId());
                Assert.assertEquals(PAYMENT_INFO_NAME, pi.getPaymentInfoId());
                return null;
            }
        });
    }

    @Test
    public void testRegisterCustomer_userGroup() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        final int customersBefore = accountMgmt.getMyCustomers().size();
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerCustomer(org, user,
                "admin", null, marketplaceId,
                supplier1User.getOrganization().getOrganizationId());

        Assert.assertNotNull(customer);

        int customersAfter = accountMgmt.getMyCustomers().size();
        Assert.assertEquals(customersBefore + 1, customersAfter);

        List<UserGroup> userGroups = runTX(new Callable<List<UserGroup>>() {
            @Override
            public List<UserGroup> call() throws Exception {
                final Organization organization = Organizations
                        .findOrganization(mgr, customer.getOrganizationId());

                List<UserGroup> userGroups = organization.getUserGroups();
                Assert.assertEquals("Default usergroup has not been stored", 1,
                        userGroups.size());
                return userGroups;
            }
        });

        UserGroup defaultUserGroup = userGroups.get(0);

        Assert.assertEquals("Wrong UserGroup data", customer.getKey(),
                defaultUserGroup.getOrganization_tkey());
        Assert.assertEquals("Wrong UserGroup data", Boolean.TRUE,
                defaultUserGroup.isDefault());
        Assert.assertEquals("Wrong UserGroup data", "default",
                defaultUserGroup.getName());
    }

    @Test
    public void testGetMyCustomersOptimizationRegisterCustomer()
            throws Exception {
        // given
        final String expectedName = "Optimized query testing";
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        final int customersBefore = accountMgmt.getMyCustomersOptimization()
                .size();
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        org.setName(expectedName);
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerCustomer(org, user,
                "admin", null, marketplaceId,
                supplier1User.getOrganization().getOrganizationId());
        // when
        List<VOOrganization> customers = accountMgmt
                .getMyCustomersOptimization();

        // then
        Assert.assertNotNull(customer);
        Assert.assertEquals(customersBefore + 1, customers.size());
        Assert.assertEquals(expectedName, customers.get(1).getName());

    }

    @Test
    public void testGetMyCustomersOptimizationRegisterCustomersOnOtherOrganization()
            throws Exception {
        // given
        final String expectedName = "Optimized query testing";
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        org.setName(expectedName);
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        accountMgmt.registerCustomer(org, user, "admin", null, marketplaceId,
                supplier1User.getOrganization().getOrganizationId());
        container.logout();
        container.login(String.valueOf(supplier2User.getKey()),
                ROLE_SERVICE_MANAGER);

        // when
        List<VOOrganization> customers = accountMgmt
                .getMyCustomersOptimization();

        // then
        Assert.assertEquals(1, customers.size());

    }

    @Test
    public void testGetMyCustomersOptimizationRegisterCustomersOnBothOrganizationResultConfirmedBySup2()
            throws Exception {
        // given
        final String expectedName = "Optimized query testing";
        final String expectedLetter = "B";
        final int newCustomerOf1 = 20;
        final int newCustomerOf2 = 30;
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        for (int i = 0; i < newCustomerOf1; i++) {
            VOOrganization org = new VOOrganization();
            org.setLocale(Locale.ENGLISH.toString());
            org.setDomicileCountry(Locale.GERMANY.getCountry());
            org.setName(expectedName + "A" + i);
            VOUserDetails user = new VOUserDetails();
            user.setEMail(TEST_MAIL_ADDRESS);
            user.setLocale(org.getLocale());
            user.setUserId("initialUserA" + i);
            accountMgmt.registerCustomer(org, user, "admin", null,
                    marketplaceId,
                    supplier1User.getOrganization().getOrganizationId());
        }
        container.logout();
        container.login(String.valueOf(supplier2User.getKey()),
                ROLE_SERVICE_MANAGER);
        for (int i = 0; i < newCustomerOf2; i++) {
            VOOrganization org = new VOOrganization();
            org.setLocale(Locale.ENGLISH.toString());
            org.setDomicileCountry(Locale.GERMANY.getCountry());
            org.setName(expectedName + "B" + i);
            VOUserDetails user = new VOUserDetails();
            user.setEMail(TEST_MAIL_ADDRESS);
            user.setLocale(org.getLocale());
            user.setUserId("initialUserB" + i);
            accountMgmt.registerCustomer(org, user, "admin", null,
                    marketplaceId,
                    supplier2User.getOrganization().getOrganizationId());
        }

        // when
        List<VOOrganization> customers = accountMgmt
                .getMyCustomersOptimization();

        // then
        Assert.assertEquals(newCustomerOf2 + 1, customers.size());
        for (int i = 0; i < newCustomerOf2; i++)
            Assert.assertEquals(expectedName + expectedLetter,
                    customers.get(1 + i).getName().substring(0,
                            expectedName.length() + 1));

    }

    @Test
    public void testGetMyCustomersOptimizationRegisterCustomersOnBothOrganizationResultConfirmedBySup1()
            throws Exception {
        // given
        final String expectedName = "Optimized query testing";
        final String expectedLetter = "A";
        final int newCustomerOf1 = 20;
        final int newCustomerOf2 = 30;
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        for (int i = 0; i < newCustomerOf1; i++) {
            VOOrganization org = new VOOrganization();
            org.setLocale(Locale.ENGLISH.toString());
            org.setDomicileCountry(Locale.GERMANY.getCountry());
            org.setName(expectedName + "A" + i);
            VOUserDetails user = new VOUserDetails();
            user.setEMail(TEST_MAIL_ADDRESS);
            user.setLocale(org.getLocale());
            user.setUserId("initialUserA" + i);
            accountMgmt.registerCustomer(org, user, "admin", null,
                    marketplaceId,
                    supplier1User.getOrganization().getOrganizationId());
        }
        container.logout();
        container.login(String.valueOf(supplier2User.getKey()),
                ROLE_SERVICE_MANAGER);
        for (int i = 0; i < newCustomerOf2; i++) {
            VOOrganization org = new VOOrganization();
            org.setLocale(Locale.ENGLISH.toString());
            org.setDomicileCountry(Locale.GERMANY.getCountry());
            org.setName(expectedName + "B" + i);
            VOUserDetails user = new VOUserDetails();
            user.setEMail(TEST_MAIL_ADDRESS);
            user.setLocale(org.getLocale());
            user.setUserId("initialUserB" + i);
            accountMgmt.registerCustomer(org, user, "admin", null,
                    marketplaceId,
                    supplier2User.getOrganization().getOrganizationId());
        }
        container.logout();
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        // when
        List<VOOrganization> customers = accountMgmt
                .getMyCustomersOptimization();

        // then
        Assert.assertEquals(newCustomerOf1 + 1, customers.size());
        for (int i = 0; i < newCustomerOf1; i++)
            Assert.assertEquals(expectedName + expectedLetter,
                    customers.get(1 + i).getName().substring(0,
                            expectedName.length() + 1));

    }

    @Test
    public void testRegisterOrganization_HistoryDates() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        final VOOrganization voOrg = registerCustomerAsLoggedInSupplier();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // get history entry for the registered organization
                Organization organization = mgr.getReference(Organization.class,
                        voOrg.getKey());
                List<DomainHistoryObject<?>> findHistory = mgr
                        .findHistory(organization);
                Assert.assertTrue(
                        "No history entries found for organization "
                                + organization.getOrganizationId(),
                        findHistory.size() > 0);
                DomainHistoryObject<?> orgHist = findHistory.get(0);
                // get history entry for the registered user
                List<PlatformUser> users = getUsersForOrganization(
                        organization.getOrganizationId());
                Assert.assertEquals(
                        "Expected exactly one user for organization "
                                + organization.getOrganizationId(),
                        1, users.size());
                PlatformUser user = users.get(0);
                findHistory = mgr.findHistory(user);
                Assert.assertEquals(
                        "Expected exactly one history entry for user "
                                + user.getUserId(),
                        1, findHistory.size());
                DomainHistoryObject<?> userHist = findHistory.get(0);

                // history moddate of organization and user must be exactly the
                // same as they were created inside one transaction
                assertEquals(
                        "Organization " + organization.getOrganizationId()
                                + " and user " + user.getUserId()
                                + " must have the same history moddate (created in same transaction)",
                        orgHist.getModdate(), userHist.getModdate());
                return null;
            }
        });
    }

    @Test
    public void testGetOrgainizationData_SubMgr() throws Exception {
        container.login(supplier1User.getKey(),
                UserRoleType.SUBSCRIPTION_MANAGER.toString());

        final VOOrganization voOrganization = accountMgmt.getOrganizationData();

        assertEquals("Subscription Manager can get organization",
                voOrganization.getDescription(), ORGANIZATION_DESCRIPTION);
    }

    @Test
    public void testUpdateCustomerDiscount_VersionNotChanged()
            throws Exception {
        container.login(supplier1User.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString(),
                UserRoleType.SERVICE_MANAGER.toString());

        final VOOrganization voOrganization = accountMgmt.getOrganizationData();
        int oldVersion = voOrganization.getVersion();

        VODiscount voDiscount = new VODiscount();
        voDiscount.setValue(BigDecimal.TEN);
        voOrganization.setDiscount(voDiscount);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateCustomerDiscount(voOrganization);
                return null;
            }
        });
        assertEquals(
                "Because only the discount must be updated, the version of the organization must not change",
                oldVersion, accountMgmt.getOrganizationData().getVersion());
    }

    @Test
    public void testUpdateCustomerDiscount_DataChanged() throws Exception {
        container.login(supplier1User.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString(),
                UserRoleType.SERVICE_MANAGER.toString());

        // no attribute of the organization except discount may be changed

        // Email
        final VOOrganization voOrganization1 = accountMgmt
                .getOrganizationData();
        voOrganization1.setEmail("something@new.com");
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    accountMgmt.updateCustomerDiscount(voOrganization1);
                    return null;
                }
            });

            fail("OperationNotPermittedException must be thrown since email is changed");
        } catch (OperationNotPermittedException e) {
            // OK, since email was changed, which is not permitted
        }
        // Name
        final VOOrganization voOrganization2 = accountMgmt
                .getOrganizationData();
        voOrganization2.setName("Some new name");
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    accountMgmt.updateCustomerDiscount(voOrganization2);
                    return null;
                }
            });
            fail("OperationNotPermittedException must be thrown since name is changed");
        } catch (OperationNotPermittedException e) {
            // OK, since name was changed, which is not permitted
        }
        // Phone
        final VOOrganization voOrganization3 = accountMgmt
                .getOrganizationData();
        voOrganization3.setPhone("some new phone");
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    accountMgmt.updateCustomerDiscount(voOrganization3);
                    return null;
                }
            });
            fail("OperationNotPermittedException must be thrown since phone is changed");
        } catch (OperationNotPermittedException e) {
            // OK, since phone was changed, which is not permitted
        }
        // URL
        final VOOrganization voOrganization4 = accountMgmt
                .getOrganizationData();
        voOrganization4.setUrl("http://some.new.url.com");
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    accountMgmt.updateCustomerDiscount(voOrganization4);
                    return null;
                }
            });
            fail("OperationNotPermittedException must be thrown since URL is changed");
        } catch (OperationNotPermittedException e) {
            // OK, since URL was changed, which is not permitted
        }
        // Address
        final VOOrganization voOrganization5 = accountMgmt
                .getOrganizationData();
        voOrganization5.setAddress("some new address");
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    accountMgmt.updateCustomerDiscount(voOrganization5);
                    return null;
                }
            });
            fail("OperationNotPermittedException must be thrown since address is changed");
        } catch (OperationNotPermittedException e) {
            // OK, since address was changed, which is not permitted
        }
        // Country
        final VOOrganization voOrganization6 = accountMgmt
                .getOrganizationData();
        voOrganization6.setDomicileCountry("GB");
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    accountMgmt.updateCustomerDiscount(voOrganization6);
                    return null;
                }
            });
            fail("OperationNotPermittedException must be thrown since country is changed");
        } catch (OperationNotPermittedException e) {
            // OK, since country was changed, which is not permitted
        }
        // Description
        final VOOrganization voOrganization7 = accountMgmt
                .getOrganizationData();
        voOrganization7.setDescription("Some new description");
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    accountMgmt.updateCustomerDiscount(voOrganization7);
                    return null;
                }
            });
            fail("OperationNotPermittedException must be thrown since description is changed");
        } catch (OperationNotPermittedException e) {
            // OK, since description was changed, which is not permitted
        }
    }

    @Test
    public void testRegisterOrganization() throws Exception {

        // the registration was executed in the setUpBeforeClass() method, we
        // have to verify the results

        Organization organization = getOrganization(organizationId);
        Assert.assertNotNull(organization);

        Assert.assertEquals(ORGANIZATION_ADDRESS, organization.getAddress());
        Assert.assertEquals(ORGANIZATION_EMAIL, organization.getEmail());
        Assert.assertEquals(ORGANIZATION_NAME, organization.getName());
        Assert.assertEquals(ORGANIZATION_PHONE, organization.getPhone());
        Assert.assertEquals(ORGANIZATION_DOMICILE,
                organization.getDomicileCountryCode());
        Assert.assertTrue(organization.getRegistrationDate() > 0);
        Assert.assertNull(organization.getDeregistrationDate());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testUpdateAccountInformation_Email() throws Throwable {

        final VOOrganization voResultOrganization1 = accountMgmt
                .getOrganizationData();

        final VOOrganization voResultOrganization2 = accountMgmt
                .getOrganizationData();

        voResultOrganization2.setEmail(ORGANIZATION_EMAIL_NEW);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(voResultOrganization2,
                        null, null, null);
                return null;
            }
        });
        Assert.assertEquals(accountMgmt.getOrganizationData().getEmail(),
                ORGANIZATION_EMAIL_NEW);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(voResultOrganization1,
                        null, null, null);
                return null;
            }
        });
    }

    /**
     * Client organizations are not allowed to have images
     */
    @Test(expected = ImageException.class)
    public void testUpdateClientWithImage() throws Throwable {

        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        final VOOrganization cust = registerCustomerAsLoggedInSupplier();
        createSubscription(cust, SubscriptionStatus.DEACTIVATED);
        String userKey = getUserKeyForOrg(cust);
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(cust, null, null,
                        new VOImageResource());
                return null;
            }
        });
    }

    @Test
    public void testUpdateAccountInformation_RenameAttempt() throws Exception {

        final VOOrganization voOrganization = accountMgmt.getOrganizationData();
        String orgId = voOrganization.getOrganizationId();
        voOrganization.setOrganizationId("BMW_RENAMED");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(voOrganization, null, null,
                        null);
                return null;
            }
        });
        // changes to the organization id must not be considered
        Organization organization = getOrganization(orgId);
        Assert.assertNotNull(organization);

        organization = getOrganization("BMW_RENAMED");
        Assert.assertNull(organization);
    }

    @Test
    public void testDeregisterOrganization() throws Exception {

        final Organization organization = getOrganization(organizationId);
        Assert.assertNotNull("Organization could not be found", organization);
        accountMgmt.deregisterOrganization();

        try {
            accountMgmt.getOrganizationData();
            Assert.fail(
                    "Operation must not work as the organization does not exist anymore");
        } catch (EJBException e) {

        }

        // now check history entries
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<DomainHistoryObject<?>> findHistory = mgr
                        .findHistory(organization);
                // the last entry - and only the last entry - must display the
                // mod-type DELETE
                int entryCount = findHistory.size();
                Assert.assertTrue("History entries missing for organization",
                        entryCount > 1);
                Assert.assertEquals("Last historized version has wrong modType",
                        ModificationType.DELETE,
                        findHistory.get(entryCount - 1).getModtype());
                Assert.assertFalse("Historized version has wrong modType",
                        ModificationType.DELETE == findHistory
                                .get(entryCount - 2).getModtype());
                return null;
            }
        });

    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetOrganizationIdNonExisting() throws Exception {
        accountMgmt.getOrganizationId(0);
    }

    @Test
    public void testGetOrganizationIdExisting() throws Exception {
        String id = accountMgmt.getOrganizationId(1);
        Assert.assertEquals("BMW", id);
    }

    @Test
    public void testRemoveOverdueOrganizationsRemoveOneUser() throws Exception {
        registerSupplier("admin");
        List<PlatformUser> platformUsers = runTX(
                new Callable<List<PlatformUser>>() {
                    @Override
                    public List<PlatformUser> call() throws Exception {
                        return getUsersForOrganization(organizationId);
                    }
                });
        Assert.assertTrue("No users found for organization, but are required",
                platformUsers.size() > 0);
        final PlatformUser platformUser = platformUsers.get(0);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                long transactionTime = DateFactory.getInstance()
                        .getTransactionTime();
                // now specify a time that simulates waiting long enough so that
                // the organization initial admin is recognized as overdue
                accountMgmtLocal.removeOverdueOrganizations(
                        System.currentTimeMillis() + 2000L);
                assertEquals(Boolean.FALSE,
                        Boolean.valueOf(transactionTime == DateFactory
                                .getInstance().getTransactionTime()));
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization organization = new Organization();
                organization.setOrganizationId(organizationId);
                DomainObject<?> find = mgr.find(organization);
                Assert.assertNull("Overdue organization was not deleted", find);

                List<DomainHistoryObject<?>> hist = mgr
                        .findHistory(platformUser);
                int entryCount = hist.size();
                Assert.assertEquals(
                        "Wrong number of history entries for the user", 2,
                        entryCount);
                Assert.assertEquals(
                        "Deletion is not tracked in the history entries",
                        ModificationType.DELETE,
                        hist.get(entryCount - 1).getModtype());
                Assert.assertFalse(
                        "Deletion must be mentioned only once in the history entries",
                        ModificationType.DELETE == hist.get(entryCount - 2)
                                .getModtype());
                return null;
            }
        });
    }

    private List<PlatformUser> getUsersForOrganization(String organizationId) {
        Organization initialOrganization = new Organization();
        initialOrganization.setOrganizationId(organizationId);
        initialOrganization = (Organization) mgr.find(initialOrganization);
        List<PlatformUser> pUsers = initialOrganization.getPlatformUsers();
        // now perform a pseudo call on the list, otherwise a lazy
        // initialization error will occur
        pUsers.size();
        return pUsers;
    }

    @Test
    public void testRemoveOverdueOrganizationsRemoveTwoUsersOneOpFails()
            throws Exception {
        registerSupplier("admin");
        final String firstCustumerId = organizationId;
        registerSupplier("admin");
        final String secondCustumerId = organizationId;

        // the only way to cause a failure during deletion is that the
        // considered user has an active subscription... so create one for the
        // second user
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createSubForUser(secondCustumerId);
                return null;
            }
        });

        // now specify a time that simulates waiting long enough so that the
        // organization initial admin is recognized as overdue
        final Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean
                        .valueOf(accountMgmtLocal.removeOverdueOrganizations(
                                System.currentTimeMillis() + 2000L));
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization organization = new Organization();
                organization.setOrganizationId(firstCustumerId);
                DomainObject<?> find = mgr.find(organization);
                Assert.assertNull("Overdue organization was not deleted", find);
                organization.setOrganizationId(secondCustumerId);
                find = mgr.find(organization);
                Assert.assertNotNull(
                        "Overdue organization was deleted, although its deletion should fail",
                        find);
                return null;
            }
        });

        Assert.assertFalse("One deletion failed, so the result must be false",
                result.booleanValue());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // internal helper methods

    private void createSubForUser(String organizationId)
            throws NonUniqueBusinessKeyException {
        Subscription sub = new Subscription();

        Organization organization = Organizations.findOrganization(mgr,
                organizationId);

        Product prod;
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP_ID", false, ServiceAccessType.LOGIN);
        prod = new Product();
        prod.setVendor(organization);
        prod.setProductId("Product");
        prod.setTechnicalProduct(tProd);
        prod.setProvisioningDate(System.currentTimeMillis());
        prod.setStatus(ServiceStatus.ACTIVE);
        prod.setType(ServiceType.SUBSCRIPTION);
        prod.setAutoAssignUserEnabled(false);
        PriceModel pm = new PriceModel();
        pm.setType(PriceModelType.PRO_RATA);
        pm.setPeriod(PricingPeriod.DAY);
        pm.setPricePerPeriod(new BigDecimal(1));
        prod.setPriceModel(pm);
        pm.setProduct(prod);
        pm.setCurrency(supportedCurrency);
        ParameterSet emptyPS = new ParameterSet();
        prod.setParameterSet(emptyPS);
        mgr.persist(prod);

        sub.setOrganization(organization);
        sub.setSubscriptionId("testRemoveOverdueCustSub");
        sub.setCreationDate(Long.valueOf(System.currentTimeMillis()));
        sub.setProductInstanceId("prodId");
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setCutOffDay(1);
        sub.bindToProduct(prod);
        sub.setUuid(UUID.randomUUID());

        mgr.persist(sub);
    }

    @Test
    public void testRegisterOrganizationSupplier() throws Exception {
        Organization organization = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization l_organization = new Organization();
                l_organization.setOrganizationId(organizationId);
                VOUserDetails userDetails = new VOUserDetails();
                userDetails.setFirstName("Hans");
                userDetails.setLastName("Meier");
                userDetails.setEMail(TEST_MAIL_ADDRESS);
                userDetails.setUserId("admin");
                userDetails.setSalutation(Salutation.MR);
                userDetails.setPhone("(089) 123 456 78");
                userDetails.setLocale("de");
                l_organization = accountMgmtLocal.registerOrganization(
                        l_organization, null, userDetails, null, "DE", null,
                        null, OrganizationRoleType.SUPPLIER);
                Assert.assertEquals(0,
                        l_organization.getMarketplaceToOrganizations().size());
                load(l_organization);
                return l_organization;
            }
        });

        validateCreatedPaymentInfo(organization);
        organizationId = organization.getOrganizationId();

        // verify result, if a mail has been received cannot be tested, as this
        // test uses a stub for the communication service. This has to be added
        // in the tested in the CTs then
        Assert.assertTrue("Organization has not been stored",
                0 != organization.getKey());
        Assert.assertTrue("Organization id not set",
                null != organization.getOrganizationId());
        Assert.assertTrue("Missing authority for role",
                organization.hasRole(OrganizationRoleType.SUPPLIER));
        assertLocalizedResources();

        final long orgKey = organization.getKey();
        List<PlatformUser> platformUsers = runTX(
                new Callable<List<PlatformUser>>() {
                    @Override
                    public List<PlatformUser> call() throws Exception {
                        Organization org = mgr.getReference(Organization.class,
                                orgKey);
                        List<PlatformUser> users = org.getPlatformUsers();
                        Assert.assertEquals("User has not been stored", 1,
                                users.size());
                        Assert.assertTrue(
                                "This supplier is not customer of itself",
                                org.getCustomersOfSupplier().contains(org));
                        return users;
                    }
                });

        PlatformUser adminUser = platformUsers.get(0);

        Assert.assertTrue("Wrong key for user", 0 != adminUser.getKey());
        Assert.assertEquals("Wrong user data", "Hans",
                adminUser.getFirstName());
        Assert.assertEquals("Wrong user data", "Meier",
                adminUser.getLastName());
        Assert.assertEquals("Wrong user data", "admin", adminUser.getUserId());
        Assert.assertEquals("Wrong user data", Salutation.MR,
                adminUser.getSalutation());
        Assert.assertEquals("Wrong user data", "(089) 123 456 78",
                adminUser.getPhone());
        Assert.assertEquals("Wrong user data", "de", adminUser.getLocale());
        Assert.assertEquals("Wrong mail for user", TEST_MAIL_ADDRESS,
                adminUser.getEmail());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testRegisterOrganizationNonUniqueSupplier() throws Exception {
        // Organization A
        final Organization organization = new Organization();
        organization.setOrganizationId("orgA");

        final VOUserDetails userDetails = new VOUserDetails();
        userDetails.setFirstName("Hans");
        userDetails.setLastName("Meier");
        userDetails.setEMail(TEST_MAIL_ADDRESS);
        userDetails.setUserId("supplier");
        userDetails.setLocale("de");

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmtLocal.registerOrganization(organization, null,
                        userDetails, null, "DE", null, null,
                        OrganizationRoleType.SUPPLIER);
                return null;
            }
        });
        validateCreatedPaymentInfo(organization);
        // Organization B (using same supplier name)
        final Organization organization2 = new Organization();
        organization2.setOrganizationId("orgB");

        final VOUserDetails userDetails2 = new VOUserDetails();
        userDetails2.setFirstName("Hans");
        userDetails2.setLastName("M??ller");
        userDetails2.setEMail(TEST_MAIL_ADDRESS);
        userDetails2.setUserId("supplier");
        userDetails2.setLocale("de");

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmtLocal.registerOrganization(organization2, null,
                        userDetails2, null, "DE", null, null,
                        OrganizationRoleType.SUPPLIER);
                return null;
            }
        });
    }

    @Test
    public void testRegisterOrganizationTechProviderNoMarketplace()
            throws Exception {

        Organization organization = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization l_organization = new Organization();
                l_organization.setOrganizationId(organizationId);

                VOUserDetails userDetails = new VOUserDetails();
                userDetails.setFirstName("Hans");
                userDetails.setLastName("Meier");
                userDetails.setEMail(TEST_MAIL_ADDRESS);
                userDetails.setUserId("admin");
                userDetails.setSalutation(Salutation.MR);
                userDetails.setPhone("(089) 123 456 78");
                userDetails.setLocale("de");
                Organization org = accountMgmtLocal.registerOrganization(
                        l_organization, null, userDetails, null, "DE", null,
                        null, OrganizationRoleType.TECHNOLOGY_PROVIDER);
                Assert.assertTrue(
                        org.getMarketplaceToOrganizations().isEmpty());
                load(org);
                return org;
            }
        });

        validateCreatedPaymentInfo(organization);
        organizationId = organization.getOrganizationId();

        Assert.assertTrue("Organization has not been stored",
                0 != organization.getKey());
        Assert.assertTrue("Organization id not set",
                null != organization.getOrganizationId());
        Assert.assertTrue("Missing authority for role",
                organization.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER));
    }

    // Refers to bug 6551
    @Test(expected = ValidationException.class)
    public void testRegisterOrganizationSupplierInvalidUserDataPhone()
            throws Exception {
        final Organization organization = new Organization();
        organization.setOrganizationId(organizationId);

        final VOUserDetails userDetails = new VOUserDetails();
        userDetails.setFirstName("Hans");
        userDetails.setLastName("Meier");
        userDetails.setEMail(TEST_MAIL_ADDRESS);
        userDetails.setUserId("admin");
        userDetails.setSalutation(Salutation.MR);
        userDetails.setPhone(TOO_LONG_NAME);
        userDetails.setLocale("de");

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmtLocal.registerOrganization(organization, null,
                        userDetails, null, "DE", null, null,
                        OrganizationRoleType.SUPPLIER);
                return null;
            }
        });
    }

    // Refers to bug 6555
    @Test
    public void testRegisterOrganizationSupplierInvalidUserDataPhoneCheckRollback()
            throws Exception {
        int orgCount = getOrganizationCount();
        final Organization organization = new Organization();
        organization.setOrganizationId(organizationId);

        final VOUserDetails userDetails = new VOUserDetails();
        userDetails.setFirstName("Hans");
        userDetails.setLastName("Meier");
        userDetails.setEMail(TEST_MAIL_ADDRESS);
        userDetails.setUserId("admin");
        userDetails.setSalutation(Salutation.MR);
        userDetails.setPhone(TOO_LONG_NAME);
        userDetails.setLocale("de");

        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    accountMgmtLocal.registerOrganization(organization, null,
                            userDetails, null, "DE", null, null,
                            OrganizationRoleType.SUPPLIER);
                    return null;
                }
            });
        } catch (ValidationException e) {

        }
        int orgCountAfter = getOrganizationCount();
        assertEquals(orgCount, orgCountAfter);
    }

    @Test
    public void testRegisterOrganizationAsSupplierAndTP() throws Exception {
        final Organization organization = new Organization();
        organization.setOrganizationId(organizationId);

        final VOUserDetails userDetails = new VOUserDetails();
        userDetails.setFirstName("Hans");
        userDetails.setLastName("Meier");
        userDetails.setEMail(TEST_MAIL_ADDRESS);
        userDetails.setUserId("admin");
        userDetails.setSalutation(Salutation.MR);
        userDetails.setPhone("(089) 123 456 78");
        userDetails.setLocale("de");

        final Organization updatedOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = accountMgmtLocal.registerOrganization(
                        organization, null, userDetails, null, "DE", null, null,
                        new OrganizationRoleType[] {
                                OrganizationRoleType.SUPPLIER,
                                OrganizationRoleType.TECHNOLOGY_PROVIDER });
                Assert.assertEquals(0,
                        org.getMarketplaceToOrganizations().size());
                load(org);
                return org;
            }
        });

        validateCreatedPaymentInfo(updatedOrg);

        Assert.assertTrue("Wrong roles assigned to organization",
                updatedOrg.hasRole(OrganizationRoleType.SUPPLIER));
        Assert.assertTrue("Wrong roles assigned to organization",
                updatedOrg.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        assertLocalizedResources();
        Assert.assertTrue("Supported countries set for supplier",
                updatedOrg.getSupportedCountryCodes().size() == 0);
    }

    @Test
    public void registerOrganization_Broker() throws Exception {
        Organization organization = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization l_organization = new Organization();
                l_organization.setOrganizationId(organizationId);
                VOUserDetails userDetails = new VOUserDetails();
                userDetails.setFirstName("Hans");
                userDetails.setLastName("Meier");
                userDetails.setEMail(TEST_MAIL_ADDRESS);
                userDetails.setUserId("admin");
                userDetails.setSalutation(Salutation.MR);
                userDetails.setPhone("(089) 123 456 78");
                userDetails.setLocale("de");
                l_organization = accountMgmtLocal.registerOrganization(
                        l_organization, null, userDetails, null, "DE", null,
                        null, OrganizationRoleType.BROKER);
                Assert.assertEquals(0,
                        l_organization.getMarketplaceToOrganizations().size());
                load(l_organization);
                return l_organization;
            }
        });

        validateCreatedPaymentInfo(organization);
        organizationId = organization.getOrganizationId();

        // verify result, if a mail has been received cannot be tested, as this
        // test uses a stub for the communication service. This has to be added
        // in the tested in the CTs then
        Assert.assertTrue("Organization has not been stored",
                0 != organization.getKey());
        Assert.assertTrue("Organization id not set",
                null != organization.getOrganizationId());
        Assert.assertTrue("Missing broker role",
                organization.hasRole(OrganizationRoleType.BROKER));
        assertLocalizedResources();

        final long orgKey = organization.getKey();
        PlatformUser adminUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = mgr.getReference(Organization.class, orgKey);
                List<PlatformUser> users = org.getPlatformUsers();
                PlatformUser admin = users.get(0);
                Assert.assertEquals("User has not been stored", 1,
                        users.size());
                Assert.assertTrue("Mising BROKER_MANAGER role for user",
                        admin.hasRole(UserRoleType.BROKER_MANAGER));
                return admin;
            }
        });

        Assert.assertTrue("Wrong key for user", 0 != adminUser.getKey());
        Assert.assertEquals("Wrong user data", "Hans",
                adminUser.getFirstName());
        Assert.assertEquals("Wrong user data", "Meier",
                adminUser.getLastName());
        Assert.assertEquals("Wrong user data", "admin", adminUser.getUserId());
        Assert.assertEquals("Wrong user data", Salutation.MR,
                adminUser.getSalutation());
        Assert.assertEquals("Wrong user data", "(089) 123 456 78",
                adminUser.getPhone());
        Assert.assertEquals("Wrong user data", "de", adminUser.getLocale());
        Assert.assertEquals("Wrong mail for user", TEST_MAIL_ADDRESS,
                adminUser.getEmail());
    }

    @Test
    public void registerOrganization_Reseller() throws Exception {
        Organization organization = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization l_organization = new Organization();
                l_organization.setOrganizationId(organizationId);
                VOUserDetails userDetails = new VOUserDetails();
                userDetails.setFirstName("Hans");
                userDetails.setLastName("Meier");
                userDetails.setEMail(TEST_MAIL_ADDRESS);
                userDetails.setUserId("admin");
                userDetails.setSalutation(Salutation.MR);
                userDetails.setPhone("(089) 123 456 78");
                userDetails.setLocale("de");
                l_organization = accountMgmtLocal.registerOrganization(
                        l_organization, null, userDetails, null, "DE", null,
                        null, OrganizationRoleType.RESELLER);
                Assert.assertEquals(0,
                        l_organization.getMarketplaceToOrganizations().size());
                load(l_organization);
                return l_organization;
            }
        });

        validateCreatedPaymentInfo(organization);
        organizationId = organization.getOrganizationId();

        // verify result, if a mail has been received cannot be tested, as this
        // test uses a stub for the communication service. This has to be added
        // in the tested in the CTs then
        Assert.assertTrue("Organization has not been stored",
                0 != organization.getKey());
        Assert.assertTrue("Organization id not set",
                null != organization.getOrganizationId());
        Assert.assertTrue("Missing broker role",
                organization.hasRole(OrganizationRoleType.RESELLER));
        assertLocalizedResources();

        final long orgKey = organization.getKey();
        PlatformUser adminUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = mgr.getReference(Organization.class, orgKey);
                List<PlatformUser> users = org.getPlatformUsers();
                PlatformUser admin = users.get(0);
                Assert.assertEquals("User has not been stored", 1,
                        users.size());
                Assert.assertTrue("Mising RESELLER_MANAGER role for user",
                        admin.hasRole(UserRoleType.RESELLER_MANAGER));
                return admin;
            }
        });

        Assert.assertTrue("Wrong key for user", 0 != adminUser.getKey());
        Assert.assertEquals("Wrong user data", "Hans",
                adminUser.getFirstName());
        Assert.assertEquals("Wrong user data", "Meier",
                adminUser.getLastName());
        Assert.assertEquals("Wrong user data", "admin", adminUser.getUserId());
        Assert.assertEquals("Wrong user data", Salutation.MR,
                adminUser.getSalutation());
        Assert.assertEquals("Wrong user data", "(089) 123 456 78",
                adminUser.getPhone());
        Assert.assertEquals("Wrong user data", "de", adminUser.getLocale());
        Assert.assertEquals("Wrong mail for user", TEST_MAIL_ADDRESS,
                adminUser.getEmail());
    }

    @Test(expected = IncompatibleRolesException.class)
    public void registerOrganization_BrokerReseller() throws Exception {
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization l_organization = new Organization();
                l_organization.setOrganizationId(organizationId);
                VOUserDetails userDetails = new VOUserDetails();
                userDetails.setFirstName("Hans");
                userDetails.setLastName("Meier");
                userDetails.setEMail(TEST_MAIL_ADDRESS);
                userDetails.setUserId("admin");
                userDetails.setSalutation(Salutation.MR);
                userDetails.setPhone("(089) 123 456 78");
                userDetails.setLocale("de");
                l_organization = accountMgmtLocal.registerOrganization(
                        l_organization, null, userDetails, null, "DE", null,
                        null, OrganizationRoleType.BROKER,
                        OrganizationRoleType.RESELLER);
                Assert.assertEquals(0,
                        l_organization.getMarketplaceToOrganizations().size());
                load(l_organization);
                return l_organization;
            }
        });
    }

    @Test(expected = IncompatibleRolesException.class)
    public void registerOrganization_ResellerTechProvider() throws Exception {
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization l_organization = new Organization();
                l_organization.setOrganizationId(organizationId);
                VOUserDetails userDetails = new VOUserDetails();
                userDetails.setFirstName("Hans");
                userDetails.setLastName("Meier");
                userDetails.setEMail(TEST_MAIL_ADDRESS);
                userDetails.setUserId("admin");
                userDetails.setSalutation(Salutation.MR);
                userDetails.setPhone("(089) 123 456 78");
                userDetails.setLocale("de");
                l_organization = accountMgmtLocal.registerOrganization(
                        l_organization, null, userDetails, null, "DE", null,
                        null, OrganizationRoleType.RESELLER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                Assert.assertEquals(0,
                        l_organization.getMarketplaceToOrganizations().size());
                load(l_organization);
                return l_organization;
            }
        });
    }

    @Test(expected = IncompatibleRolesException.class)
    public void registerOrganization_BrokerSupplier() throws Exception {
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization l_organization = new Organization();
                l_organization.setOrganizationId(organizationId);
                VOUserDetails userDetails = new VOUserDetails();
                userDetails.setFirstName("Hans");
                userDetails.setLastName("Meier");
                userDetails.setEMail(TEST_MAIL_ADDRESS);
                userDetails.setUserId("admin");
                userDetails.setSalutation(Salutation.MR);
                userDetails.setPhone("(089) 123 456 78");
                userDetails.setLocale("de");
                l_organization = accountMgmtLocal.registerOrganization(
                        l_organization, null, userDetails, null, "DE", null,
                        null, OrganizationRoleType.BROKER,
                        OrganizationRoleType.SUPPLIER);
                Assert.assertEquals(0,
                        l_organization.getMarketplaceToOrganizations().size());
                load(l_organization);
                return l_organization;
            }
        });
    }

    @Test
    public void testRegisterOrganization_userGroup() throws Exception {
        Organization organization = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization l_organization = new Organization();
                l_organization.setOrganizationId(organizationId);
                VOUserDetails userDetails = new VOUserDetails();
                userDetails.setFirstName("Hans");
                userDetails.setLastName("Meier");
                userDetails.setEMail(TEST_MAIL_ADDRESS);
                userDetails.setUserId("admin");
                userDetails.setLocale("de");
                l_organization = accountMgmtLocal.registerOrganization(
                        l_organization, null, userDetails, null, "DE", null,
                        null, OrganizationRoleType.SUPPLIER);
                Assert.assertEquals(0,
                        l_organization.getMarketplaceToOrganizations().size());
                load(l_organization);
                return l_organization;
            }
        });

        organizationId = organization.getOrganizationId();
        Assert.assertTrue("Organization has not been stored",
                0 != organization.getKey());
        Assert.assertTrue("Organization id not set",
                null != organization.getOrganizationId());

        final long orgKey = organization.getKey();
        List<UserGroup> userGroups = runTX(new Callable<List<UserGroup>>() {
            @Override
            public List<UserGroup> call() throws Exception {
                Organization org = mgr.getReference(Organization.class, orgKey);
                List<UserGroup> userGroups = org.getUserGroups();
                Assert.assertEquals("Default usergroup has not been stored", 1,
                        userGroups.size());
                return userGroups;
            }
        });

        UserGroup defaultUserGroup = userGroups.get(0);

        Assert.assertEquals("Wrong UserGroup data", orgKey,
                defaultUserGroup.getOrganization_tkey());
        Assert.assertEquals("Wrong UserGroup data", Boolean.TRUE,
                defaultUserGroup.isDefault());
        Assert.assertEquals("Wrong UserGroup data", "default",
                defaultUserGroup.getName());
    }

    @Test
    public void testAddOrganizationToRole() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization organization = mgr.find(Organization.class,
                        supplier1User.getOrganization().getKey());
                long time = System.currentTimeMillis();
                mgr.persist(TechnicalProducts.createTechnicalProduct(mgr,
                        organization, "" + time, false,
                        ServiceAccessType.DIRECT));
                mgr.persist(TechnicalProducts.createTechnicalProduct(mgr,
                        organization, "" + time + 100, false,
                        ServiceAccessType.DIRECT));
                mgr.persist(TechnicalProducts.createTechnicalProduct(mgr,
                        organization, "" + time + 200, false,
                        ServiceAccessType.DIRECT));
                return null;
            }
        });

        final Organization updatedOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = accountMgmtLocal.addOrganizationToRole(
                        supplier1User.getOrganization().getOrganizationId(),
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                load(org);
                return org;
            }
        });

        Assert.assertTrue("New role was not set",
                updatedOrg.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        Assert.assertTrue("Old role was lost",
                updatedOrg.hasRole(OrganizationRoleType.SUPPLIER));

        verify(mpMock, times(3)).addMarketingPermission(
                Matchers.any(Organization.class), Matchers.anyLong(),
                Matchers.anyListOf(String.class));
    }

    @Test
    public void testAddOrganizationToRole_addNonSupplier() throws Exception {
        final Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization updated = accountMgmtLocal.addOrganizationToRole(
                        org.getOrganizationId(),
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                Assert.assertTrue(
                        updated.getMarketplaceToOrganizations().isEmpty());
                return updated;
            }
        });
    }

    @Test
    public void testAddOrganizationToRole_addSupplier() throws Exception {
        final Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization updated = accountMgmtLocal.addOrganizationToRole(
                        org.getOrganizationId(), OrganizationRoleType.SUPPLIER);
                Assert.assertEquals(0,
                        updated.getMarketplaceToOrganizations().size());
                return updated;

            }
        });
        assertLocalizedResources();
    }

    @Test
    public void addOrganizationToRole_Broker() throws Exception {
        final Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization updated = accountMgmtLocal.addOrganizationToRole(
                        org.getOrganizationId(), OrganizationRoleType.BROKER);
                Assert.assertEquals(0,
                        updated.getMarketplaceToOrganizations().size());
                Assert.assertTrue(
                        updated.hasRole(OrganizationRoleType.CUSTOMER));
                Assert.assertTrue(updated.hasRole(OrganizationRoleType.BROKER));
                return updated;

            }
        });
        assertLocalizedResources();
    }

    @Test
    public void addOrganizationToRole_Reseller() throws Exception {
        final Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization updated = accountMgmtLocal.addOrganizationToRole(
                        org.getOrganizationId(), OrganizationRoleType.RESELLER);
                Assert.assertEquals(0,
                        updated.getMarketplaceToOrganizations().size());
                Assert.assertTrue(
                        updated.hasRole(OrganizationRoleType.CUSTOMER));
                Assert.assertTrue(
                        updated.hasRole(OrganizationRoleType.RESELLER));
                return updated;

            }
        });
        assertLocalizedResources();
    }

    @Test(expected = IncompatibleRolesException.class)
    public void addOrganizationToRole_BrokerToSupplier() throws Exception {
        final Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER,
                        OrganizationRoleType.SUPPLIER);
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization updated = accountMgmtLocal.addOrganizationToRole(
                        org.getOrganizationId(), OrganizationRoleType.BROKER);
                Assert.assertEquals(0,
                        updated.getMarketplaceToOrganizations().size());
                return updated;

            }
        });
        assertLocalizedResources();
    }

    @Test(expected = IncompatibleRolesException.class)
    public void addOrganizationToRole_ResellerToBroker() throws Exception {
        final Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER,
                        OrganizationRoleType.BROKER);
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization updated = accountMgmtLocal.addOrganizationToRole(
                        org.getOrganizationId(), OrganizationRoleType.RESELLER);
                Assert.assertEquals(0,
                        updated.getMarketplaceToOrganizations().size());
                return updated;

            }
        });
        assertLocalizedResources();
    }

    @Test(expected = IncompatibleRolesException.class)
    public void addOrganizationToRole_ResellerToTechProvider()
            throws Exception {
        final Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization updated = accountMgmtLocal.addOrganizationToRole(
                        org.getOrganizationId(), OrganizationRoleType.RESELLER);
                Assert.assertEquals(0,
                        updated.getMarketplaceToOrganizations().size());
                return updated;

            }
        });
        assertLocalizedResources();
    }

    @Test
    public void testRegisterCustomerForSupplier() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        int customersBefore = accountMgmt.getMyCustomers().size();
        final VOOrganization customer = registerCustomerAsLoggedInSupplier();
        Assert.assertNotNull(customer);
        int customersAfter = accountMgmt.getMyCustomers().size();
        Assert.assertEquals(customersBefore + 1, customersAfter);
        PlatformUser platformUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization organization = Organizations.findOrganization(mgr,
                        customer.getOrganizationId());
                PlatformUser platformUser = organization.getPlatformUsers()
                        .get(0);
                load(platformUser);
                load(organization);
                return platformUser;
            }
        });
        Assert.assertEquals(TEST_MAIL_ADDRESS, platformUser.getEmail());
        Assert.assertEquals("administrator", platformUser.getUserId());
        Assert.assertTrue(platformUser.isOrganizationAdmin());
        Assert.assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        Assert.assertEquals("Wrong trigger type used",
                TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER, usedTriggerType);
        Assert.assertFalse("Wrong roles assigned to organization",
                platformUser.getOrganization()
                        .hasRole(OrganizationRoleType.MARKETPLACE_OWNER));
    }

    @Test
    public void testRegisterCustomerForSupplierNoDefaultUserId()
            throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        int customersBefore = accountMgmt.getMyCustomers().size();
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);

        Assert.assertNotNull(customer);
        int customersAfter = accountMgmt.getMyCustomers().size();
        Assert.assertEquals(customersBefore + 1, customersAfter);
        PlatformUser platformUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization organization = Organizations.findOrganization(mgr,
                        customer.getOrganizationId());
                PlatformUser platformUser = organization.getPlatformUsers()
                        .get(0);
                load(platformUser);
                return platformUser;
            }
        });
        Assert.assertEquals(user.getEMail(), platformUser.getEmail());
        Assert.assertEquals(user.getUserId(), platformUser.getUserId());
        Assert.assertTrue(platformUser.isOrganizationAdmin());
    }

    @Test
    /**
     * BE07787
     */
    public void testRegisterCustomerForSupplierMailServerNotAvailable()
            throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());

        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("AUser");

        try {
            throwMailOperationException = true;
            int customersBefore = accountMgmt.getMyCustomers().size();

            try {
                accountMgmt.registerKnownCustomer(org, user, null,
                        marketplaceId);
                fail("MailOperationException expected");
            } catch (MailOperationException e) {
                // Check that there was a transaction fallback;
                // the customer musn't be stored in the database
                int customersAfter = accountMgmt.getMyCustomers().size();
                Assert.assertEquals(customersBefore, customersAfter);
            }
        } finally {
            throwMailOperationException = false;
        }
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerForSupplierNoOrganizationLocale()
            throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(Locale.ENGLISH.toString());
        accountMgmt.registerKnownCustomer(org, user, null, marketplaceId);
    }

    @Test
    public void testRegisterCustomerForSupplierUserIdWithBlanks()
            throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry("DE");
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(Locale.ENGLISH.toString());
        user.setUserId("hgj grfegh");
        accountMgmt.registerKnownCustomer(org, user, null, marketplaceId);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerForSupplierNoUserMail() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setLocale(org.getLocale());
        accountMgmt.registerKnownCustomer(org, user, null, marketplaceId);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerForSupplierNoUserLocale() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        accountMgmt.registerKnownCustomer(org, user, null, marketplaceId);
    }

    @Test
    public void testRegisterCustomerForSupplierOrgIdSet() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setOrganizationId("someValidId");
        VOUserDetails user = new VOUserDetails();
        try {
            accountMgmt.registerKnownCustomer(org, user, null, marketplaceId);
            Assert.fail("No ValidationFailed caught");
        } catch (ValidationException e) {
            Assert.assertEquals(ReasonEnum.EMPTY_VALUE, e.getReason());
        }
    }

    private VOUserDetails prepareLdapUser(String userId, String email) {
        if (userId == null) {
            userId = "pock";
        }
        if (email == null) {
            email = "peter.pock@est.fujitsu.com";
        }
        VOUserDetails user = new VOUserDetails();
        user.setLocale(Locale.ENGLISH.toString());
        user.setUserId(userId);
        user.setEMail(email);
        return user;
    }

    private LdapProperties prepareLdapProperties(String url)
            throws ObjectNotFoundException {
        if (url == null) {
            url = "ldap://estinfra1.lan.est.fujitsu.de:389";
        }

        LdapProperties props = new LdapProperties();
        props.setProperty(SettingType.LDAP_CONTEXT_FACTORY.toString(),
                "com.sun.jndi.ldap.LdapCtxFactory");
        props.setProperty(SettingType.LDAP_BASE_DN.toString(),
                "ou=people,dc=est,dc=fujitsu,dc=de");
        props.setProperty(SettingType.LDAP_URL.toString(), url);

        props.setProperty(SettingType.LDAP_ATTR_UID.toString(), "uid");
        props.setProperty(SettingType.LDAP_ATTR_LAST_NAME.toString(), "sn");
        props.setProperty(SettingType.LDAP_ATTR_FIRST_NAME.toString(),
                "givenName");
        props.setProperty(SettingType.LDAP_ATTR_EMAIL.toString(),
                "scalixEmailAddress");

        // props.setProperty("ERROR",
        // "This is a wrong setting key and should be ignored");
        doNothing().when(ldapSettingMmgtMock)
                .setOrganizationSettings(anyString(), storedProps.capture());
        when(ldapSettingMmgtMock
                .getOrganizationSettingsResolved(Mockito.anyString()))
                        .thenReturn(props.asProperties());
        when(ldapSettingMmgtMock
                .getSettingsResolved(Mockito.any(Properties.class)))
                        .thenReturn(props.asProperties());
        return props;
    }

    private VOOrganization registerCustomerForSupplierLdap(LdapProperties props,
            String userId, String email) throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry("DE");
        VOUserDetails user = prepareLdapUser(userId, email);

        return accountMgmt.registerKnownCustomer(org, user, props,
                marketplaceId);
    }

    @Test
    public void testRegisterCustomerForSupplierLdap() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        LdapProperties props = prepareLdapProperties(null);
        final VOOrganization customer = registerCustomerForSupplierLdap(props,
                null, null);
        Assert.assertNotNull(customer);
        verify(ldapSettingMmgtMock, times(1)).setOrganizationSettings(
                Matchers.anyString(), Matchers.any(Properties.class));
        assertEquals(props.asProperties(), storedProps.getValue());
        verify(ldapSettingMmgtMock, times(1))
                .getOrganizationSettingsResolved(Matchers.anyString());
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerForSupplierWithLdap_WrongEmail()
            throws Exception {
        LdapProperties props = prepareLdapProperties(null);
        registerCustomerForSupplierLdap(props, null, "saas@est.fujitsu.com");
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerForSupplierWithLdap_NoConnection()
            throws Exception {
        LdapProperties props = prepareLdapProperties("invalid");
        registerCustomerForSupplierLdap(props, null, null);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerForSupplierWithLdap_UserNotFound()
            throws Exception {
        LdapProperties props = prepareLdapProperties(null);
        registerCustomerForSupplierLdap(props, "xyx", null);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerForSupplierWithLdap_UserNotUnique()
            throws Exception {
        LdapProperties props = prepareLdapProperties(null);
        registerCustomerForSupplierLdap(props, "p*", null);
    }

    @Test(expected = javax.ejb.EJBException.class)
    public void testRegisterCustomerForSupplierNoSupplier() throws Exception {
        container.login(String.valueOf(technologyProviderUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        VOOrganization org = new VOOrganization();
        VOUserDetails user = new VOUserDetails();
        accountMgmt.registerKnownCustomer(org, user, null, marketplaceId);
    }

    @Test
    public void testUpdateAccountInformation_ChangeOrganizationId()
            throws Exception {
        final Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createCustomer(mgr,
                        Organizations.findOrganization(mgr,
                                supplierIds.get(0)));
                return org;
            }
        });

        String orgId = org.getOrganizationId();

        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        true, "usera");
                return user;
            }
        });

        container.login(String.valueOf(user.getKey()), ROLE_ORGANIZATION_ADMIN);

        final VOOrganization orgData = accountMgmt.getOrganizationData();
        orgData.setOrganizationId(orgId + "suffix");

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(orgData, null, null, null);
                return null;
            }
        });

        Organization newOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization foundOrg = mgr.find(Organization.class,
                        orgData.getKey());
                return foundOrg;
            }
        });

        Assert.assertEquals("Organization id must not have been changed", orgId,
                newOrg.getOrganizationId());

    }

    private String getUserKeyForOrg(final VOOrganization org) throws Exception {
        String userKey = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                long key = getUsersForOrganization(org.getOrganizationId())
                        .get(0).getKey();
                return String.valueOf(key);
            }
        });
        return userKey;
    }

    @Test
    public void testRegisterCustomerForSupplier_NullMarketplaceId()
            throws Exception {

        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale("en");

        try {
            accountMgmt.registerKnownCustomer(org, user, null, null);
            fail("should not come here");
        } catch (EJBException e) {
            // expected
        }
    }

    @Test
    public void testRegisterCustomerForSupplierInvoice() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization voOrganization = registerCustomerAsLoggedInSupplier();
        Assert.assertNotNull(voOrganization);
        Assert.assertNotNull(voOrganization.getOrganizationId());
    }

    private VOOrganization registerCustomerAsLoggedInSupplier()
            throws Exception {
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setDomicileCountry("DE");
        voOrganization.setLocale("en");
        VOUserDetails voUser = new VOUserDetails();
        voUser.setEMail(TEST_MAIL_ADDRESS);
        voUser.setLocale("en");
        voUser.setUserId("administrator");

        voOrganization = accountMgmt.registerKnownCustomer(voOrganization,
                voUser, null, marketplaceId);

        final long OrgKey = voOrganization.getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class, OrgKey);
                Assert.assertNotNull(org);
                Organization supp = new Organization();
                supp.setOrganizationId(supplierIds.get(0));
                supp = (Organization) mgr.getReferenceByBusinessKey(supp);
                Assert.assertTrue(org.getSuppliersOfCustomer().size() == 1);
                Assert.assertEquals(supp, org.getSuppliersOfCustomer().get(0));
                return null;
            }
        });

        return voOrganization;
    }

    private void setupCountries() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCountries.setupSomeCountries(mgr);
                return null;
            }
        });
    }

    private void setupRoles() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UserRoles.createSetupRoles(mgr);
                return null;
            }
        });
    }

    private void setupSupportedCurrency() throws Exception {
        supportedCurrency = new SupportedCurrency();
        supportedCurrency.setCurrency(Currency.getInstance("EUR"));
        mgr.persist(supportedCurrency);
    }

    @Test
    public void testGetAvailablePaymentTypes_AsSupplierEmptyList()
            throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        Set<VOPaymentType> set = accountMgmt
                .getAvailablePaymentTypesForOrganization();
        Assert.assertTrue(set.isEmpty());
    }

    @Test
    public void testGetAvailablePaymentTypes_AsCustomer() throws Exception {
        testGetAvailablePaymentTypes(false);
    }

    @Test
    public void testGetAvailablePaymentTypes_AsSupplier() throws Exception {
        testGetAvailablePaymentTypes(true);
    }

    private void testGetAvailablePaymentTypes(boolean testIsSupplier)
            throws Exception {

        final Organization customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier = Organizations.findOrganization(mgr,
                        supplierIds.get(0));
                Organization supplier1 = Organizations.findOrganization(mgr,
                        supplierIds.get(1));

                Organization customer = Organizations.createCustomer(mgr,
                        supplier);
                OrganizationReference organizationReference = new OrganizationReference(
                        supplier1, customer,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                mgr.persist(organizationReference);

                createAvailablePayment(customer, OrganizationRoleType.CUSTOMER);
                return customer;
            }
        });

        final Organization customer1 = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization customer1 = (Organization) mgr
                        .getReferenceByBusinessKey(customer);
                customer1.getSources().size();
                load(customer1.getSources().get(0).getSource());
                return customer1;
            }
        });

        assertEquals(customer1.getSources().size(), 2);

        PlatformUser customer1User = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(mgr, customer1, true,
                        "CustomerAdmin1");
            }
        });

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE);

        if (!testIsSupplier) {
            container.login(String.valueOf(customer1User.getKey()));
        } else {
            container.login(String.valueOf(supplier1User.getKey()));
        }

        Set<VOPaymentType> set = accountMgmt
                .getAvailablePaymentTypesFromOrganization(productKeys[0]);

        if (!testIsSupplier) {
            Assert.assertEquals(1, set.size());
            Set<String> hashSet = new HashSet<>();
            hashSet.add(CREDIT_CARD);

            for (VOPaymentType type : set) {
                Assert.assertTrue(hashSet.contains(type.getPaymentTypeId()));
                hashSet.remove(type.getPaymentTypeId());
            }
            Assert.assertTrue(hashSet.isEmpty());
        } else {
            Assert.assertTrue(set.isEmpty());
        }
    }

    @Test
    public void testGetAvailablePaymentTypes_AsSupplierCCandDD()
            throws Exception {

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                PlatformUser user = mgr.find(PlatformUser.class,
                        supplier1User.getKey());
                Organization org = user.getOrganization();
                createAvailablePayment(org, OrganizationRoleType.SUPPLIER);
                return null;
            }
        });
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        Set<VOPaymentType> set = accountMgmt
                .getAvailablePaymentTypesForOrganization();
        Assert.assertEquals(2, set.size());
        Set<String> hashSet = new HashSet<>();
        hashSet.add(DIRECT_DEBIT);
        hashSet.add(CREDIT_CARD);
        for (VOPaymentType type : set) {
            Assert.assertTrue(hashSet.contains(type.getPaymentTypeId()));
            hashSet.remove(type.getPaymentTypeId());
        }
        Assert.assertTrue(hashSet.isEmpty());
    }

    @Test(expected = javax.ejb.EJBException.class)
    public void testGetAvailablePaymentTypes_AsTechnologyProvider()
            throws Exception {
        container.login(String.valueOf(technologyProviderUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        accountMgmt.getAvailablePaymentTypesForOrganization();
    }

    @Test(expected = javax.ejb.EJBException.class)
    public void testGetAvailablePaymentTypes_AsCustomerNoSupplier()
            throws Exception {
        Long userKey = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Organization customer = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        customer, true, "admin");
                return Long.valueOf(user.getKey());
            }
        });
        container.login(String.valueOf(userKey));
        accountMgmt.getAvailablePaymentTypesForOrganization();
    }

    @Test(expected = ServiceParameterException.class)
    public void testGetOrganizationIdExpired() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        final VOOrganization cust = registerCustomerAsLoggedInSupplier();
        long subKey = createSubscription(cust, SubscriptionStatus.EXPIRED);
        accountMgmt.getOrganizationId(subKey);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetOrganizationIdNotActive() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        final VOOrganization cust = registerCustomerAsLoggedInSupplier();
        long subKey = createSubscription(cust, SubscriptionStatus.DEACTIVATED);
        accountMgmt.getOrganizationId(subKey);
    }

    private long createSubscription(final VOOrganization cust,
            final SubscriptionStatus status) throws Exception {
        Long custUserKey = runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                Organization provider = getOrganization(providerId);
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider, "testTechProd", false,
                        ServiceAccessType.LOGIN);
                Organization supplier = getOrganization(supplierIds.get(0));
                Product product = Products.createProduct(supplier, tp, true,
                        "testProd", null, mgr);
                Organization customer = Organizations.findOrganization(mgr,
                        cust.getOrganizationId());
                Subscription subscription = Subscriptions.createSubscription(
                        mgr, customer.getOrganizationId(),
                        product.getProductId(), "testSub", supplier);
                subscription.setStatus(status);
                return Long.valueOf(subscription.getKey());
            }
        });
        return custUserKey.longValue();
    }

    private Long[] prepareProducts(final ServiceStatus status)
            throws Exception {
        Long[] productKeys = runTX(new Callable<Long[]>() {

            @Override
            public Long[] call() throws Exception {
                Organization provider = getOrganization(providerId);
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider, "testTechProd", false,
                        ServiceAccessType.LOGIN);
                Organization supplier = getOrganization(supplierIds.get(0));
                Product product1 = Products.createProduct(supplier, tp, true,
                        "testProd1", null, mgr);

                PaymentType pt1 = findPaymentType(CREDIT_CARD, mgr);
                ProductToPaymentType prodToPt1 = new ProductToPaymentType(
                        product1, pt1);
                mgr.persist(prodToPt1);

                Product product2 = Products.createProduct(supplier, tp, false,
                        "testProd2", null, mgr);
                if (status != null) {
                    product2.setStatus(status);
                }
                PaymentType pt2 = findPaymentType(INVOICE, mgr);
                ProductToPaymentType prodToPt2 = new ProductToPaymentType(
                        product2, pt2);
                mgr.persist(prodToPt2);

                return new Long[] { Long.valueOf(product1.getKey()),
                        Long.valueOf(product2.getKey()) };
            }
        });
        return productKeys;
    }

    private Set<Product> prepareProduct(final String providerId,
            final String supplierId, final String customerId,
            final ServiceStatus status, final Set<String> paymentTypes,
            final boolean createCustomerSpecific) throws Exception {

        Set<Product> product = runTX(new Callable<Set<Product>>() {

            @Override
            public Set<Product> call() throws Exception {
                Set<Product> result = new HashSet<>();
                Organization provider = getOrganization(providerId);
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider, "TP_" + UUID.randomUUID() + "_" + status,
                        false, ServiceAccessType.LOGIN);

                Organization supplier = getOrganization(supplierId);
                Product product = Products.createProduct(supplier, tp, true,
                        "PR_" + UUID.randomUUID() + "_" + status, null, mgr);
                if (status != null) {
                    product.setStatus(status);
                }

                // add template product
                result.add(product);

                if (createCustomerSpecific) {
                    Organization customer = getOrganization(customerId);
                    Product copyProduct = Products.createCustomerSpecifcProduct(
                            mgr, customer, product, product.getStatus());
                    // add customer-specific product
                    result.add(copyProduct);

                }

                addPaymentTypesToProduct(product, paymentTypes);

                return result;
            }
        });

        return product;
    }

    private Set<Product> prepareProduct(final String providerId,
            final String supplierId, final boolean chargeable,
            final Set<String> paymentTypes, final boolean createPriceModel)
            throws Exception {

        Set<Product> product = runTX(new Callable<Set<Product>>() {

            @Override
            public Set<Product> call() throws Exception {
                Set<Product> result = new HashSet<>();
                Organization provider = getOrganization(providerId);
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider,
                        "TP_" + UUID.randomUUID() + "_" + ServiceStatus.ACTIVE,
                        false, ServiceAccessType.LOGIN);

                Organization supplier = getOrganization(supplierId);
                Product product = Products.createProduct(supplier, tp, true,
                        "PR_" + UUID.randomUUID() + "_" + ServiceStatus.ACTIVE,
                        null, mgr);
                product.setStatus(ServiceStatus.ACTIVE);

                if (createPriceModel) {
                    PriceModel prmodel = new PriceModel();
                    if (chargeable) {
                        prmodel.setType(PriceModelType.PRO_RATA);
                    } else {
                        prmodel.setType(PriceModelType.FREE_OF_CHARGE);
                    }
                    prmodel.setCurrency(supportedCurrency);
                    product.setPriceModel(prmodel);
                } else {
                    product.setPriceModel(null);
                }
                // add template product
                result.add(product);

                addPaymentTypesToProduct(product, paymentTypes);

                return result;
            }
        });

        return product;
    }

    private Set<Product> prepareProductWithExternalPriceModel(
            final String providerId, final String supplierId,
            final boolean chargeable, final Set<String> paymentTypes,
            final boolean createPriceModel) throws Exception {

        Set<Product> product = runTX(new Callable<Set<Product>>() {

            @Override
            public Set<Product> call() throws Exception {
                Set<Product> result = new HashSet<>();
                Organization provider = getOrganization(providerId);
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider,
                        "TP_" + UUID.randomUUID() + "_" + ServiceStatus.ACTIVE,
                        false, ServiceAccessType.LOGIN, false, true);

                Organization supplier = getOrganization(supplierId);
                Product product = Products.createProduct(supplier, tp, true,
                        "PR_" + UUID.randomUUID() + "_" + ServiceStatus.ACTIVE,
                        null, mgr);
                product.setStatus(ServiceStatus.ACTIVE);
                // add template product
                result.add(product);

                return result;
            }
        });

        return product;
    }

    @Test
    public void testDeregisterOrganizationWithInactiveSubscription()
            throws Exception {
        // given
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        final VOOrganization cust = registerCustomerAsLoggedInSupplier();
        createSubscription(cust, SubscriptionStatus.DEACTIVATED);
        String userKey = getUserKeyForOrg(cust);
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        // when
        accountMgmt.deregisterOrganization();

        // then
        long deregistrationInvocationTime = DateFactory.getInstance()
                .getTransactionTime();
        Long deregistrationDate = getDeregistrationDate(cust);
        assertEquals(deregistrationInvocationTime,
                deregistrationDate.longValue());
    }

    @Test(expected = DeletionConstraintException.class)
    public void testDeregisterOrganizationWithActiveSubscription()
            throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        final VOOrganization cust = registerCustomerAsLoggedInSupplier();
        createSubscription(cust, SubscriptionStatus.ACTIVE);
        String userKey = getUserKeyForOrg(cust);
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);
        accountMgmt.deregisterOrganization();
    }

    @Test(expected = DeletionConstraintException.class)
    public void testDeregisterOrganizationWithPendingSubscription()
            throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        final VOOrganization cust = registerCustomerAsLoggedInSupplier();
        createSubscription(cust, SubscriptionStatus.PENDING);
        String userKey = getUserKeyForOrg(cust);
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);
        accountMgmt.deregisterOrganization();
    }

    @Test
    public void testUpdateAccountInformation_SetDistinguishedName()
            throws Exception {
        PlatformUser user = createCustomerWithPaymentAndUser();
        container.login(String.valueOf(user.getKey()), ROLE_ORGANIZATION_ADMIN);
        final VOOrganization org = accountMgmt.getOrganizationData();
        String distinguishedName = "distinguishedName";
        org.setDistinguishedName(distinguishedName);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(org, null, null, null);
                return null;
            }
        });
        Assert.assertEquals(distinguishedName,
                accountMgmt.getOrganizationData().getDistinguishedName());
    }

    @Test
    public void testInvocationDate() throws Exception {
        final PlatformUser user = createCustomerWithPaymentAndUser();
        container.login(String.valueOf(user.getKey()), ROLE_ORGANIZATION_ADMIN);
        final VOOrganization org = accountMgmt.getOrganizationData();
        // a remote interface method changes the invocation date
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(org, null, null, null);
                assertNotNull("The InvocationDateContainer was not invoked",
                        Long.valueOf(DateFactory.getInstance()
                                .getTransactionTime()));
                return null;
            }
        });

        // a local interface method does NOT change the invocation date
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Date invocationDate = DateFactory.getInstance()
                        .getTransactionDate();
                Thread.sleep(100); // ensure different dates
                accountMgmtLocal.updateAccountInformation(
                        user.getOrganization(), null, null);
                assertEquals(
                        "Local methods are not allowed to change the remote interface method invocation date",
                        invocationDate,
                        DateFactory.getInstance().getTransactionDate());
                return null;
            }
        });
    }

    @Test(expected = DistinguishedNameException.class)
    public void testUpdateAccountInformation_SetExistingDistinguishedName()
            throws Exception {
        PlatformUser user = createCustomerWithPaymentAndUser();
        container.login(String.valueOf(user.getKey()), ROLE_ORGANIZATION_ADMIN);
        final VOOrganization org = accountMgmt.getOrganizationData();
        org.setDistinguishedName("distinguishedName");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(org, null, null, null);
                return null;
            }
        });
        user = createCustomerWithPaymentAndUser();
        container.login(String.valueOf(user.getKey()), ROLE_ORGANIZATION_ADMIN);
        final VOOrganization org1 = accountMgmt.getOrganizationData();
        org1.setDistinguishedName("distinguishedName");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(org1, null, null, null);
                return null;
            }
        });
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentUpdateOrganization() throws Exception {
        PlatformUser user = createCustomerWithPaymentAndUser();
        container.login(String.valueOf(user.getKey()), ROLE_ORGANIZATION_ADMIN);
        final VOOrganization org = accountMgmt.getOrganizationData();
        org.setAddress("another address");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(org, null, null, null);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(org, null, null, null);
                return null;
            }
        });
    }

    @Test
    public void testSavePaymentConfigurationDefault() throws Exception {
        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        Set<VOPaymentType> expected = createVOPaymentTypes(INVOICE,
                CREDIT_CARD);
        List<VOOrganizationPaymentConfiguration> empty = new ArrayList<>();
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        accountMgmt.savePaymentConfiguration(expected, empty, expected, null);
        verifyDefaultPaymentConfiguration(expected);
    }

    @Test
    public void testSavePaymentConfigurationDefaultAndModify()
            throws Exception {
        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        List<VOOrganizationPaymentConfiguration> empty = new ArrayList<>();
        Set<VOPaymentType> expected = createVOPaymentTypes(INVOICE,
                CREDIT_CARD);
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        accountMgmt.savePaymentConfiguration(expected, empty, expected, null);
        verifyDefaultPaymentConfiguration(expected);
        expected = createVOPaymentTypes(DIRECT_DEBIT);
        accountMgmt.savePaymentConfiguration(expected, empty, expected, null);
        verifyDefaultPaymentConfiguration(expected);
    }

    @Test
    public void testSavePaymentConfigurationDefaultAndClear() throws Exception {
        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        List<VOOrganizationPaymentConfiguration> empty = new ArrayList<>();
        Set<VOPaymentType> expected = createVOPaymentTypes(INVOICE,
                CREDIT_CARD);
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        accountMgmt.savePaymentConfiguration(expected, empty, expected, null);
        verifyDefaultPaymentConfiguration(expected);
        expected = new HashSet<>();
        boolean executed = accountMgmt.savePaymentConfiguration(expected, empty,
                expected, null);
        Assert.assertTrue(executed);
        verifyDefaultPaymentConfiguration(expected);
    }

    @Test(expected = javax.ejb.EJBException.class)
    public void testSavePaymentConfigurationAsTechnologyProvider()
            throws Exception {
        container.login(String.valueOf(technologyProviderUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        List<VOOrganizationPaymentConfiguration> empty = Collections
                .emptyList();
        Set<VOPaymentType> expected = Collections.emptySet();
        accountMgmt.savePaymentConfiguration(expected, empty, expected, null);
    }

    @Test(expected = javax.ejb.EJBException.class)
    public void testSavePaymentConfigurationAsCustomer() throws Exception {
        PlatformUser custUser = createCustomerWithPaymentAndUser();
        container.login(String.valueOf(custUser.getKey()));
        List<VOOrganizationPaymentConfiguration> empty = Collections
                .emptyList();
        Set<VOPaymentType> expected = Collections.emptySet();
        accountMgmt.savePaymentConfiguration(expected, empty, expected, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSavePaymentConfigurationDefaultNull() throws Exception {
        List<VOOrganizationPaymentConfiguration> empty = Collections
                .emptyList();
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        try {
            accountMgmt.savePaymentConfiguration(null, empty, null, null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testGetDefaultPaymentConfigurationEmpty() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        Set<VOPaymentType> actual = accountMgmt
                .getDefaultPaymentConfiguration();
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetDefaultPaymentConfiguration() throws Exception {
        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        List<VOOrganizationPaymentConfiguration> empty = new ArrayList<>();
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        Set<VOPaymentType> expected = createVOPaymentTypes(INVOICE,
                CREDIT_CARD);
        accountMgmt.savePaymentConfiguration(expected, empty, expected, null);
        Set<VOPaymentType> actual = accountMgmt
                .getDefaultPaymentConfiguration();
        Assert.assertEquals(expected.size(), actual.size());
        Set<String> set = new HashSet<>();
        for (VOPaymentType voPaymentType : actual) {
            set.add(voPaymentType.getPaymentTypeId());
        }
        for (VOPaymentType type : expected) {
            Assert.assertTrue(set.contains(type.getPaymentTypeId()));
            set.remove(type.getPaymentTypeId());
        }
        Assert.assertTrue(set.isEmpty());
    }

    @Test(expected = javax.ejb.EJBException.class)
    public void testSetDefaultPaymentConfigurationAsTechnologyProvider()
            throws Exception {
        container.login(String.valueOf(technologyProviderUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        accountMgmt.getDefaultPaymentConfiguration();
    }

    @Test(expected = javax.ejb.EJBException.class)
    public void testSetDefaultPaymentConfigurationAsCustomer()
            throws Exception {
        PlatformUser custUser = createCustomerWithPaymentAndUser();
        container.login(String.valueOf(custUser.getKey()));
        accountMgmt.getDefaultPaymentConfiguration();
    }

    @Test
    public void testGetBillingContacts_NoneSaved() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry("DE");
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        String customerUserKey = getUserKeyForOrg(customer);
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        assertTrue(accountMgmt.getBillingContacts().isEmpty());
    }

    @Test
    public void testGetBillingContactAsSubMgr() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        String customerUserKey = getUserKeyForOrg(customer);
        container.login(customerUserKey, ROLE_SUBSCRIPTION_MANAGER);
        accountMgmt.getBillingContacts();
    }

    @Test(expected = EJBException.class)
    public void testGetBillingContactAsNonAdmin() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        String customerUserKey = getUserKeyForOrg(customer);
        container.login(customerUserKey);
        accountMgmt.getBillingContacts();
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testSaveBillingContact_CreateDuplicateId() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER, ROLE_ORGANIZATION_ADMIN);
        final VOBillingContact billingContact = createBillingContact(
                "test address", "test company name", "test@mail.de");
        accountMgmt.saveBillingContact(billingContact);
        accountMgmt.saveBillingContact(billingContact);
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testSaveBillingContact_UpdateDuplicateId() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER, ROLE_ORGANIZATION_ADMIN);
        VOBillingContact bc1 = accountMgmt
                .saveBillingContact(createBillingContact("test address",
                        "test company name", "test@mail.de"));
        VOBillingContact bc2 = accountMgmt
                .saveBillingContact(createBillingContact("test address",
                        "test company name", "test@mail.de"));
        bc1.setId(bc2.getId());
        accountMgmt.saveBillingContact(bc1);
    }

    @Test
    public void testSaveBillingContact_Update() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER, ROLE_ORGANIZATION_ADMIN);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        String customerUserKey = getUserKeyForOrg(customer);
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        assertTrue(accountMgmt.getBillingContacts().isEmpty());

        final String address = "test address";
        final String companyName = "test company name";
        final String email = "test@mail.de";
        final VOBillingContact billingContact = createBillingContact(address,
                companyName, email);

        VOBillingContact savedBc = accountMgmt
                .saveBillingContact(billingContact);

        Assert.assertEquals(address, savedBc.getAddress());
        Assert.assertEquals(companyName, savedBc.getCompanyName());
        Assert.assertEquals(email, savedBc.getEmail());
        Assert.assertEquals(false, savedBc.isOrgAddressUsed());

        savedBc.setAddress("new address");
        savedBc = accountMgmt.saveBillingContact(savedBc);
        List<VOBillingContact> savedBillingContacts = accountMgmt
                .getBillingContacts();
        assertEquals(1, savedBillingContacts.size());
        VOBillingContact bc = savedBillingContacts.get(0);
        Assert.assertEquals("new address", bc.getAddress());
        Assert.assertEquals(companyName, bc.getCompanyName());
        Assert.assertEquals(email, bc.getEmail());
        Assert.assertEquals(false, bc.isOrgAddressUsed());
    }

    @Test
    public void testSaveBillingContact_verifySave() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        String customerUserKey = getUserKeyForOrg(customer);
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        assertTrue(accountMgmt.getBillingContacts().isEmpty());

        final String address = "test address";
        final String companyName = "test company name";
        final String email = "test@mail.de";
        final VOBillingContact billingContact = createBillingContact(address,
                companyName, email);
        final VOBillingContact savedBc = accountMgmt
                .saveBillingContact(billingContact);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BillingContact bcRef = mgr.getReference(BillingContact.class,
                        savedBc.getKey());
                Assert.assertEquals(savedBc.getAddress(), bcRef.getAddress());
                Assert.assertEquals(savedBc.getCompanyName(),
                        bcRef.getCompanyName());
                Assert.assertEquals(savedBc.getEmail(), bcRef.getEmail());
                Assert.assertEquals(savedBc.getKey(), bcRef.getKey());
                Assert.assertEquals(savedBc.getVersion(), bcRef.getVersion());
                return null;
            }
        });
        VOBillingContact bc = accountMgmt.getBillingContacts().get(0);
        assertEqualsVOBillingContact(savedBc, bc);

        long versionBefore = savedBc.getVersion();
        savedBc.setAddress("changed");
        final VOBillingContact savedBc2 = accountMgmt
                .saveBillingContact(savedBc);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BillingContact bcRef = mgr.getReference(BillingContact.class,
                        savedBc.getKey());
                Assert.assertEquals("changed", bcRef.getAddress());
                Assert.assertEquals(savedBc.getCompanyName(),
                        bcRef.getCompanyName());
                Assert.assertEquals(savedBc.getEmail(), bcRef.getEmail());
                Assert.assertEquals(savedBc.getKey(), bcRef.getKey());
                Assert.assertEquals(savedBc.getVersion() + 1,
                        bcRef.getVersion());
                return null;
            }
        });
        Assert.assertTrue((versionBefore + 1) == savedBc2.getVersion());
    }

    private static VOBillingContact createBillingContact(final String address,
            final String companyName, final String email) {
        final VOBillingContact billingContact = new VOBillingContact();
        billingContact.setAddress(address);
        billingContact.setCompanyName(companyName);
        billingContact.setEmail(email);
        billingContact.setOrgAddressUsed(false);
        billingContact.setId("bcname");
        return billingContact;
    }

    @Test
    public void testSaveBillingContact_Multiple() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        String customerUserKey = getUserKeyForOrg(customer);
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        assertTrue(accountMgmt.getBillingContacts().isEmpty());

        final String address = "test address";
        final String companyName = "test company name";
        final String email = "test@mail.de";
        VOBillingContact billingContact = new VOBillingContact();
        billingContact.setAddress(address);
        billingContact.setCompanyName(companyName);
        billingContact.setEmail(email);
        billingContact.setOrgAddressUsed(false);
        billingContact.setId("bcname1");
        accountMgmt.saveBillingContact(billingContact);
        billingContact.setId("bcname2");
        accountMgmt.saveBillingContact(billingContact);
        List<VOBillingContact> billingContacts = accountMgmt
                .getBillingContacts();
        assertEquals(2, billingContacts.size());
        for (VOBillingContact bc : billingContacts) {
            assertEquals(address, bc.getAddress());
            assertEquals(companyName, bc.getCompanyName());
            assertEquals(email, bc.getEmail());
            assertFalse(bc.isOrgAddressUsed());
        }
        VOBillingContact contact = billingContacts.get(0);
        contact.setEmail("anothermail@company.com");
        accountMgmt.saveBillingContact(contact);
        billingContacts = accountMgmt.getBillingContacts();
        assertEquals(2, billingContacts.size());
        for (VOBillingContact bc : billingContacts) {
            assertEquals(address, bc.getAddress());
            if (bc.getKey() == contact.getKey()) {
                assertEquals("anothermail@company.com", bc.getEmail());
            } else {
                assertEquals(email, bc.getEmail());
            }
            assertEquals(companyName, bc.getCompanyName());
            assertFalse(bc.isOrgAddressUsed());
        }
    }

    @Test
    public void testRegisterCustomerForSupplierWithDefaultPayments()
            throws Exception {

        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        List<VOOrganizationPaymentConfiguration> empty = new ArrayList<>();
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        Set<VOPaymentType> orgPt = createVOPaymentTypes(INVOICE, CREDIT_CARD);
        Set<VOPaymentType> expected = createVOPaymentTypes(CREDIT_CARD);
        accountMgmt.savePaymentConfiguration(orgPt, empty, orgPt, null);

        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");

        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        String customerUserKey = getUserKeyForOrg(customer);

        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE);

        Set<VOPaymentType> actual = accountMgmt
                .getAvailablePaymentTypesFromOrganization(productKeys[0]);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRegisterCustomerForSupplierWithEmptyDefaultPayments()
            throws Exception {

        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);

        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");

        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        String customerUserKey = getUserKeyForOrg(customer);

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE);

        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        Set<VOPaymentType> expected = Collections.emptySet();
        Set<VOPaymentType> actual = accountMgmt
                .getAvailablePaymentTypesFromOrganization(productKeys[0]);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRegisterCustomerForSupplierWithInvoiceDefaultConfiguration()
            throws Exception {

        setInvoiceAsDefaultPayment = true;

        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);

        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");

        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        String customerUserKey = getUserKeyForOrg(customer);

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE);

        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);

        Set<VOPaymentType> actual = accountMgmt
                .getAvailablePaymentTypesFromOrganization(productKeys[1]);
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals("INVOICE",
                actual.iterator().next().getPaymentTypeId());
    }

    @Test
    public void testSavePaymentConfigurationForCustomer() throws Exception {

        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);

        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        String customerUserKey = getUserKeyForOrg(customer);

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE);

        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        Set<VOPaymentType> expected = new HashSet<>();
        Set<VOPaymentType> actual = accountMgmt
                .getAvailablePaymentTypesFromOrganization(productKeys[0]);
        // ensure that after registration no payment type is available
        Assert.assertEquals(expected, actual);

        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        expected = createVOPaymentTypes(DIRECT_DEBIT, CREDIT_CARD);
        VOOrganizationPaymentConfiguration configuration = new VOOrganizationPaymentConfiguration();
        configuration.setOrganization(customer);
        configuration.setEnabledPaymentTypes(expected);
        List<VOOrganizationPaymentConfiguration> conf = new ArrayList<>();
        conf.add(configuration);
        accountMgmt.savePaymentConfiguration(expected, conf, expected, null);
        actual = runTX(new Callable<Set<VOPaymentType>>() {

            @Override
            public Set<VOPaymentType> call() throws Exception {
                Set<VOPaymentType> set = new HashSet<>();
                Organization org = mgr.getReference(Organization.class,
                        customer.getKey());
                List<OrganizationRefToPaymentType> types = org
                        .getPaymentTypes(supplierIds.get(0));
                for (OrganizationRefToPaymentType orgToPt : types) {
                    set.add(PaymentTypeAssembler.toVOPaymentType(
                            orgToPt.getPaymentType(), new LocalizerFacade(
                                    localizer, supplier1User.getLocale())));
                }
                return set;
            }
        });
        Assert.assertEquals(expected, actual);
        Assert.assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        Assert.assertEquals(TriggerType.SAVE_PAYMENT_CONFIGURATION,
                usedTriggerType);
    }

    @Test
    public void testSavePaymentConfigurationForCustomerRemove()
            throws Exception {

        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        List<VOOrganizationPaymentConfiguration> empty = new ArrayList<>();
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        Set<VOPaymentType> orgPt = createVOPaymentTypes(INVOICE, CREDIT_CARD);
        Set<VOPaymentType> expected = createVOPaymentTypes(CREDIT_CARD);
        accountMgmt.savePaymentConfiguration(orgPt, empty, orgPt, null);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry("DE");
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        String customerUserKey = getUserKeyForOrg(customer);

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE);

        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        Set<VOPaymentType> actual = accountMgmt
                .getAvailablePaymentTypesFromOrganization(productKeys[0]);
        // ensure that after registration no payment type is available
        Assert.assertEquals(expected, actual);

        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        expected = new HashSet<>();
        VOOrganizationPaymentConfiguration configuration = new VOOrganizationPaymentConfiguration();
        configuration.setOrganization(customer);
        configuration.setEnabledPaymentTypes(expected);
        List<VOOrganizationPaymentConfiguration> conf = new ArrayList<>();
        conf.add(configuration);
        accountMgmt.savePaymentConfiguration(expected, conf, expected, null);
        actual = runTX(new Callable<Set<VOPaymentType>>() {

            @Override
            public Set<VOPaymentType> call() throws Exception {
                Set<VOPaymentType> set = new HashSet<>();
                Organization org = mgr.getReference(Organization.class,
                        customer.getKey());
                List<OrganizationRefToPaymentType> types = org
                        .getPaymentTypes(supplierIds.get(0));
                for (OrganizationRefToPaymentType orgToPt : types) {
                    set.add(PaymentTypeAssembler.toVOPaymentType(
                            orgToPt.getPaymentType(), new LocalizerFacade(
                                    localizer, supplier1User.getLocale())));
                }
                return set;
            }
        });
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetAvailablePaymentTypesFromSupplier_NoRelation()
            throws Exception {
        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        Set<VOPaymentType> orgPt = createVOPaymentTypes(INVOICE, CREDIT_CARD);
        Set<VOPaymentType> expected = createVOPaymentTypes(CREDIT_CARD);
        List<VOOrganizationPaymentConfiguration> conf = new ArrayList<>();
        accountMgmt.savePaymentConfiguration(orgPt, conf, expected, null);

        Long userKey = runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                Organization cust = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                PlatformUser user = Organizations.createUserForOrg(mgr, cust,
                        true, "testAdmin");
                return Long.valueOf(user.getKey());
            }
        });

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE);

        container.login(userKey.longValue(), ROLE_ORGANIZATION_ADMIN);
        Set<VOPaymentType> actual = accountMgmt
                .getAvailablePaymentTypesFromOrganization(productKeys[0]);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetAvailablePaymentTypesFromSupplier_WithRelationNoPaymentTypes()
            throws Exception {
        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        Set<VOPaymentType> orgPt = createVOPaymentTypes(INVOICE, CREDIT_CARD);
        List<VOOrganizationPaymentConfiguration> conf = new ArrayList<>();
        accountMgmt.savePaymentConfiguration(orgPt, conf, orgPt, null);

        Long userKey = runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                Organization supplier = Organizations.findOrganization(mgr,
                        supplierIds.get(0));
                Organization cust = Organizations.createCustomer(mgr, supplier);
                PlatformUser user = Organizations.createUserForOrg(mgr, cust,
                        true, "testAdmin");
                return Long.valueOf(user.getKey());
            }
        });

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE);

        container.login(userKey.longValue(), ROLE_ORGANIZATION_ADMIN);
        Set<VOPaymentType> actual = accountMgmt
                .getAvailablePaymentTypesFromOrganization(productKeys[0]);
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetAvailablePaymentTypesFromSupplier_WithRelationNoDefaults()
            throws Exception {
        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry("DE");
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);

        Set<VOPaymentType> orgPt = createVOPaymentTypes(INVOICE, CREDIT_CARD);
        Set<VOPaymentType> expected = createVOPaymentTypes(CREDIT_CARD);
        Set<VOPaymentType> def = new HashSet<>();
        VOOrganizationPaymentConfiguration configuration = new VOOrganizationPaymentConfiguration();
        configuration.setOrganization(customer);
        configuration.setEnabledPaymentTypes(orgPt);
        List<VOOrganizationPaymentConfiguration> conf = new ArrayList<>();
        conf.add(configuration);
        accountMgmt.savePaymentConfiguration(def, conf, def, null);

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE);

        String customerUserKey = getUserKeyForOrg(customer);
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        Set<VOPaymentType> actual = accountMgmt
                .getAvailablePaymentTypesFromOrganization(productKeys[0]);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetCustomerPaymentConfiguration() throws Exception {
        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);

        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        Set<VOPaymentType> expected = createVOPaymentTypes(INVOICE,
                CREDIT_CARD);
        List<VOOrganizationPaymentConfiguration> customerConf = new ArrayList<>();

        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());

        VOUserDetails user1 = new VOUserDetails();
        user1.setEMail(TEST_MAIL_ADDRESS);
        user1.setLocale(org.getLocale());
        user1.setUserId("initialUser1");

        VOUserDetails user2 = new VOUserDetails();
        user2.setEMail(TEST_MAIL_ADDRESS);
        user2.setLocale(org.getLocale());
        user2.setUserId("initialUser2");

        final VOOrganization customer1 = accountMgmt.registerKnownCustomer(org,
                user1, null, marketplaceId);
        final VOOrganization customer2 = accountMgmt.registerKnownCustomer(org,
                user2, null, marketplaceId);

        Set<VOPaymentType> set1 = createVOPaymentTypes(INVOICE, CREDIT_CARD);
        VOOrganizationPaymentConfiguration conf = new VOOrganizationPaymentConfiguration();
        conf.setEnabledPaymentTypes(set1);
        conf.setOrganization(customer1);
        customerConf.add(conf);

        Set<VOPaymentType> set2 = createVOPaymentTypes(DIRECT_DEBIT,
                CREDIT_CARD);
        conf = new VOOrganizationPaymentConfiguration();
        conf.setEnabledPaymentTypes(set2);
        conf.setOrganization(customer2);
        customerConf.add(conf);
        accountMgmt.savePaymentConfiguration(expected, customerConf, expected,
                null);

        customerConf = accountMgmt.getCustomerPaymentConfiguration();
        Assert.assertNotNull(customerConf);

        List<VOOrganizationPaymentConfiguration> result = new LinkedList<>();
        for (VOOrganizationPaymentConfiguration pc : customerConf) {
            if (pc.getOrganization().getKey() == customer1.getKey()
                    || pc.getOrganization().getKey() == customer2.getKey()) {
                result.add(pc);
            }
        }
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(customer1.getOrganizationId(),
                result.get(0).getOrganization().getOrganizationId());
        Assert.assertEquals(customer2.getOrganizationId(),
                result.get(1).getOrganization().getOrganizationId());
        Assert.assertEquals(set1, result.get(0).getEnabledPaymentTypes());
        Assert.assertEquals(set2, result.get(1).getEnabledPaymentTypes());
    }

    @Test
    public void testSavePaymentConfigurationForCustomerRemoveWithActiveSubscription()
            throws Exception {
        prepareProducts(null);
        Set<String> pts = new HashSet<>(
                Arrays.asList(INVOICE, CREDIT_CARD, DIRECT_DEBIT));
        final Product productOfSupplier2 = prepareProduct(
                technologyProviderUser.getOrganization().getOrganizationId(),
                supplierIds.get(1), true, pts, true).iterator().next();

        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        addPaymentTypesToOrganizationRef(supplierIds.get(1),
                OrganizationRoleType.SUPPLIER);
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        Set<VOPaymentType> def = createVOPaymentTypes(INVOICE, CREDIT_CARD,
                DIRECT_DEBIT);
        List<VOOrganizationPaymentConfiguration> customerConf = new ArrayList<>();
        accountMgmt.savePaymentConfiguration(def, customerConf, def, null);

        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);
        final StringBuffer sub2Key = new StringBuffer();
        final long subKey = runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        customer.getKey());
                PaymentInfo paymentInfo = PaymentInfos.createPaymentInfo(org,
                        mgr, paymentTypes.get(0));
                paymentInfo.setExternalIdentifier("test");
                Subscription sub = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), "testProd1", "testSub",
                        Organizations.findOrganization(mgr,
                                supplierIds.get(0)));
                sub.setPaymentInfo(paymentInfo);
                BillingContact bc = PaymentInfos.createBillingContact(mgr, org);
                sub.setBillingContact(bc);
                Subscription sub2 = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(),
                        productOfSupplier2.getProductId(), "testSub2",
                        Organizations.findOrganization(mgr,
                                supplierIds.get(1)));
                sub2.setPaymentInfo(paymentInfo);
                sub2.setBillingContact(bc);
                sub2Key.append(sub2.getKey());
                return Long.valueOf(sub.getKey());
            }
        }).longValue();
        VOOrganizationPaymentConfiguration conf = new VOOrganizationPaymentConfiguration();
        conf.setOrganization(customer);
        def = new HashSet<>();
        conf.setEnabledPaymentTypes(def);
        customerConf.add(conf);
        accountMgmt.savePaymentConfiguration(def, customerConf, def, null);
        SubscriptionStatus status = runTX(new Callable<SubscriptionStatus>() {

            @Override
            public SubscriptionStatus call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return sub.getStatus();
            }
        });
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, status);
        Assert.assertTrue(instanceDeactivated);
        Assert.assertFalse(instanceActivated);
        status = runTX(new Callable<SubscriptionStatus>() {

            @Override
            public SubscriptionStatus call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class,
                        Long.parseLong(sub2Key.toString()));
                return sub.getStatus();
            }
        });
        Assert.assertEquals(SubscriptionStatus.ACTIVE, status);
    }

    @Test
    public void testSavePaymentConfigurationForCustomerAddWithSuspendedSubscription()
            throws Exception {
        prepareProducts(null);
        Set<String> pts = new HashSet<>(
                Arrays.asList(INVOICE, CREDIT_CARD, DIRECT_DEBIT));
        final Product productOfSupplier2 = prepareProduct(
                technologyProviderUser.getOrganization().getOrganizationId(),
                supplierIds.get(1), true, pts, true).iterator().next();

        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        addPaymentTypesToOrganizationRef(supplierIds.get(1),
                OrganizationRoleType.SUPPLIER);
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        Set<VOPaymentType> def = createVOPaymentTypes();
        List<VOOrganizationPaymentConfiguration> customerConf = new ArrayList<>();
        accountMgmt.savePaymentConfiguration(def, customerConf, def, null);

        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry("DE");
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);

        final StringBuffer sub2Key = new StringBuffer();
        final long subKey = runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        customer.getKey());
                PaymentInfo paymentInfo = PaymentInfos.createPaymentInfo(org,
                        mgr, paymentTypes.get(0));
                paymentInfo.setExternalIdentifier("test");
                Subscription sub = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), "testProd1", "testSub",
                        Organizations.findOrganization(mgr,
                                supplierIds.get(0)));
                sub.setStatus(SubscriptionStatus.SUSPENDED);
                BillingContact bc = PaymentInfos.createBillingContact(mgr, org);
                sub.setBillingContact(bc);
                sub.setPaymentInfo(paymentInfo);
                Subscription sub2 = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(),
                        productOfSupplier2.getProductId(), "testSub2",
                        Organizations.findOrganization(mgr,
                                supplierIds.get(1)));
                sub2.setStatus(SubscriptionStatus.SUSPENDED);
                sub2.setPaymentInfo(paymentInfo);
                sub2.setBillingContact(bc);
                sub2Key.append(sub2.getKey());
                mgr.flush();
                return Long.valueOf(sub.getKey());
            }
        }).longValue();
        VOOrganizationPaymentConfiguration conf = new VOOrganizationPaymentConfiguration();
        conf.setOrganization(customer);
        def = createVOPaymentTypes(CREDIT_CARD, DIRECT_DEBIT, INVOICE);
        conf.setEnabledPaymentTypes(def);
        customerConf.add(conf);
        accountMgmt.savePaymentConfiguration(def, customerConf, def, null);
        SubscriptionStatus status = runTX(new Callable<SubscriptionStatus>() {

            @Override
            public SubscriptionStatus call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return sub.getStatus();
            }
        });
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, status);
        Assert.assertFalse(instanceActivated);
        Assert.assertFalse(instanceDeactivated);
        status = runTX(new Callable<SubscriptionStatus>() {

            @Override
            public SubscriptionStatus call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class,
                        Long.parseLong(sub2Key.toString()));
                return sub.getStatus();
            }
        });

        Assert.assertEquals(SubscriptionStatus.SUSPENDED, status);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testUpdateAccountInformationAsNonAdmin() throws Exception {
        final Organization org = createCustomerAndUser(false);
        final VOOrganization data = runTX(new Callable<VOOrganization>() {

            @Override
            public VOOrganization call() throws Exception {
                Organization organization = mgr.getReference(Organization.class,
                        org.getKey());
                return OrganizationAssembler.toVOOrganization(organization,
                        false, new LocalizerFacade(localizer, "en"));
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(data, null, null, null);
                return null;
            }
        });
    }

    @Test
    public void testUpdateAccountInformation() throws Exception {
        createCustomerAndUser(true);
        final String mail = "mail@mail.com";
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                VOOrganization data = accountMgmt.getOrganizationData();
                data.setEmail(mail);
                data.setLocale("de");
                data.setDomicileCountry("DE");
                data.setDescription("es regnet ????????\n");
                accountMgmt.updateAccountInformation(data, null, null, null);
                return null;
            }
        });
        VOOrganization data = accountMgmt.getOrganizationData();
        Assert.assertNotNull(data);
        Assert.assertEquals(mail, data.getEmail());
        Assert.assertEquals("de", data.getLocale());
        Assert.assertEquals("DE", data.getDomicileCountry());
        Assert.assertEquals("es regnet ????????\n", data.getDescription());
    }

    @Test
    public void testUpdateAccountInformationAsSupplier() throws Exception {
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        true, "admin");
                container.login(String.valueOf(user.getKey()),
                        ROLE_ORGANIZATION_ADMIN);
                return null;
            }
        });
        final String mail = "mail@mail.com";
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                VOOrganization data = accountMgmt.getOrganizationData();
                data.setEmail(mail);
                data.setLocale("de");
                data.setDomicileCountry("DE");
                accountMgmt.updateAccountInformation(data, null, null, null);
                return null;
            }
        });
        VOOrganization data = accountMgmt.getOrganizationData();
        Assert.assertNotNull(data);
        Assert.assertEquals(mail, data.getEmail());
        Assert.assertEquals("de", data.getLocale());
        Assert.assertEquals("DE", data.getDomicileCountry());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentUpdateAccountInformation() throws Throwable {
        createCustomerAndUser(true);
        final VOOrganization data = accountMgmt.getOrganizationData();
        data.setEmail("anothermail@mail.com");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(data, null, null, null);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(data, null, null, null);
                return null;
            }
        });
    }

    @Test
    public void testUpdateAccountInformationUserData() throws Exception {
        createCustomerAndUser(true);
        final VOUserDetails user = idManagement.getCurrentUserDetails();
        String mail = "new_" + user.getEMail();
        user.setEMail(mail);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(null, user, null, null);
                return null;
            }
        });
        VOUserDetails user1 = idManagement.getCurrentUserDetails();
        Assert.assertNotNull(user1);
        Assert.assertEquals(mail, user1.getEMail());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentUpdateAccountInformationUserData()
            throws Throwable {
        createCustomerAndUser(true);
        final VOUserDetails user = idManagement.getCurrentUserDetails();
        user.setEMail("new_" + user.getEMail());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(null, user, null, null);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(null, user, null, null);
                return null;
            }
        });
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testUpdateAccountInformationDifferentUsersData()
            throws Exception {
        container.login(String.valueOf(supplier1User.getKey()));
        final VOUserDetails user = idManagement.getCurrentUserDetails();
        createCustomerAndUser(true);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmt.updateAccountInformation(null, user, null, null);
                return null;
            }
        });
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerForSupplierIntValidationFailed()
            throws Exception {
        container.login(String.valueOf(supplier1User.getKey()));
        final TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USER,
                new VOUserDetails());
        tp.addTriggerProcessParameter(TriggerProcessParameterName.ORGANIZATION,
                new VOOrganization());
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.ORGANIZATION_PROPERTIES, null);
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.MARKETPLACE_ID, marketplaceId);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmtLocal.registerKnownCustomerInt(tp);
                return null;
            }
        });
    }

    @Test
    public void testRegisterCustomerForSupplierInt() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization orgToCreate = new VOOrganization();
        orgToCreate.setLocale("en");
        orgToCreate.setDomicileCountry("DE");
        final TriggerProcess tp = new TriggerProcess();
        VOUserDetails user = new VOUserDetails();
        user.setLocale("en");
        user.setEMail("mail@host.de");
        user.setUserId("administrator");
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USER, user);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.ORGANIZATION,
                orgToCreate);
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.ORGANIZATION_PROPERTIES, null);
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.MARKETPLACE_ID, marketplaceId);

        int initialSize = accountMgmt.getMyCustomers().size();
        final VOOrganization org = runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                return accountMgmtLocal.registerKnownCustomerInt(tp);
            }
        });
        int sizeAfterRegistration = accountMgmt.getMyCustomers().size();

        Assert.assertEquals(1, sizeAfterRegistration - initialSize);
        Assert.assertTrue(org.getKey() > 0);
        Assert.assertTrue(
                isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        Assert.assertEquals(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                usedTriggerType);
    }

    @Test(expected = javax.ejb.EJBException.class)
    public void testSavePaymentConfigurationIntNotAuthorized()
            throws Exception {
        TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.DEFAULT_CONFIGURATION,
                new HashSet<VOPaymentType>());
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.CUSTOMER_CONFIGURATION,
                new ArrayList<VOOrganizationPaymentConfiguration>());
        accountMgmtLocal.savePaymentConfigurationInt(tp);
    }

    @Test
    public void testSavePaymentConfigurationInt() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()));
        VOOrganization cust = runTX(new Callable<VOOrganization>() {

            @Override
            public VOOrganization call() throws Exception {
                Organization org = new Organization();
                org.setOrganizationId(organizationId);
                org = (Organization) mgr.getReferenceByBusinessKey(org);

                Organization sup = new Organization();
                sup.setOrganizationId(supplierIds.get(0));
                sup = (Organization) mgr.getReferenceByBusinessKey(sup);
                OrganizationReference ref = new OrganizationReference(sup, org,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                mgr.persist(ref);
                return OrganizationAssembler.toVOOrganization(org, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });
        final TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.DEFAULT_CONFIGURATION,
                new HashSet<VOPaymentType>());
        VOOrganizationPaymentConfiguration paymentCfg = new VOOrganizationPaymentConfiguration();
        paymentCfg.setOrganization(cust);
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.CUSTOMER_CONFIGURATION, paymentCfg);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountMgmtLocal.savePaymentConfigurationInt(tp);
                return null;
            }
        });
        Assert.assertTrue(
                isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        Assert.assertEquals(TriggerType.SAVE_PAYMENT_CONFIGURATION,
                usedTriggerType);
    }

    @Test
    public void testCreateCustomerWithPaymentAndUser() throws Exception {
        final Organization customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization customer = Organizations.createCustomer(mgr,
                        Organizations.findOrganization(mgr,
                                supplierIds.get(0)));

                PaymentType paymentType = findPaymentType(CREDIT_CARD, mgr);
                PaymentInfos.createPaymentInfo(customer, mgr, paymentType);
                PaymentInfos.createBillingContact(mgr, customer);

                return customer;
            }
        });

        Organization customerDb = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization customer1 = mgr.find(Organization.class,
                        customer.getKey());
                load(customer1.getSources().get(0).getSource());
                load(customer1.getPaymentInfos().get(0).getPaymentType());
                return customer1;
            }
        });

        assertNotNull(customerDb);

        Organization supplier = customerDb.getSources().get(0).getSource();
        assertNotNull(supplier);

        List<PaymentInfo> paymentInfos = customerDb.getPaymentInfos();
        assertNotNull(paymentInfos);
        assertEquals(paymentInfos.size(), 1);

        PaymentInfo paymentInfo = paymentInfos.get(0);

        Organization org = paymentInfo.getOrganization();
        assertEquals(customer.getOrganizationId(), org.getOrganizationId());

        assertEquals(paymentInfo.getPaymentType().getPaymentTypeId(),
                CREDIT_CARD);
    }

    /**
     * Creates a customer organization and a user for it.
     * 
     * @param isAdmin
     *            indicating if the created user will have admin rights
     * 
     * @return The created customer organization.
     * @throws Exception
     */
    private Organization createCustomerAndUser(final boolean isAdmin)
            throws Exception {
        Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createCustomer(mgr,
                        Organizations.findOrganization(mgr,
                                supplierIds.get(0)));
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        isAdmin, "admin");
                if (isAdmin) {
                    container.login(String.valueOf(user.getKey()),
                            ROLE_ORGANIZATION_ADMIN);
                } else {
                    container.login(String.valueOf(user.getKey()));
                }
                org.getSources().size();
                return org;
            }
        });
        return org;
    }

    /**
     * Read the default payment configuration from the database and verify
     * against the expected set.
     * 
     * @param expected
     *            the expected set of saved default payment types
     * @throws Exception
     */
    private void verifyDefaultPaymentConfiguration(
            final Set<VOPaymentType> expected) throws Exception {

        Set<String> result = runTX(new Callable<Set<String>>() {

            @Override
            public Set<String> call() throws Exception {
                Organization org = new Organization();
                org.setOrganizationId(supplierIds.get(0));
                org = (Organization) mgr.getReferenceByBusinessKey(org);

                List<OrganizationRefToPaymentType> types = org.getPaymentTypes(
                        true, OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.PLATFORM_OPERATOR.name());
                Set<String> result = new HashSet<>();
                for (OrganizationRefToPaymentType dpt : types) {
                    result.add(dpt.getPaymentType().getPaymentTypeId());
                }
                return result;
            }
        });
        Assert.assertEquals(expected.size(), result.size());
        for (VOPaymentType type : expected) {
            Assert.assertTrue(result.contains(type.getPaymentTypeId()));
            result.remove(type.getPaymentTypeId());
        }
        Assert.assertTrue(result.isEmpty());
    }

    /**
     * Creates an organization (customer), registers a payment information
     * object for it and creates a platform user for that organization as well.
     * Finally returns the platform user.
     * 
     * @return The created user.
     * @throws Exception
     */
    private PlatformUser createCustomerWithPaymentAndUser() throws Exception {
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization customer = Organizations.createCustomer(mgr,
                        Organizations.findOrganization(mgr,
                                supplierIds.get(0)));
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        customer, true, "admin");

                PaymentType paymentType = findPaymentType(CREDIT_CARD, mgr);
                PaymentInfos.createPaymentInfo(customer, mgr, paymentType);
                PaymentInfos.createBillingContact(mgr, customer);

                return user;
            }
        });
        return user;
    }

    private Long getDeregistrationDate(final VOOrganization org)
            throws Exception {
        return runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                Organization organization = mgr.getReference(Organization.class,
                        org.getKey());
                return organization.getDeregistrationDate();
            }
        });
    }

    private void createAvailablePayment(Organization org,
            OrganizationRoleType organizationRoleType)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(organizationRoleType);
        role = (OrganizationRole) mgr.getReferenceByBusinessKey(role);

        OrganizationRefToPaymentType apt = new OrganizationRefToPaymentType();
        OrganizationReferenceType referenceType;
        Organization source;
        if (organizationRoleType == OrganizationRoleType.CUSTOMER) {
            referenceType = OrganizationReferenceType.SUPPLIER_TO_CUSTOMER;
            source = org.getSuppliersOfCustomer().get(0);
        } else {
            referenceType = OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER;
            source = Organizations.findOrganization(mgr, "PLATFORM_OPERATOR");
        }

        OrganizationReference template = new OrganizationReference(source, org,
                referenceType);
        OrganizationReference orgRef = null;
        try {
            orgRef = (OrganizationReference) mgr
                    .getReferenceByBusinessKey(template);
        } catch (Exception e) {
            // not exists, add

            if (organizationRoleType == OrganizationRoleType.SUPPLIER) {
                orgRef = new OrganizationReference(source, org,
                        OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);

            } else {
                orgRef = new OrganizationReference(source, org,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
            }

            mgr.persist(orgRef);
        }

        apt.setOrganizationReference(orgRef);
        PaymentType pt = mgr.getReference(PaymentType.class,
                paymentTypes.get(1).getKey());
        apt.setPaymentType(pt);
        apt.setOrganizationRole(role);
        apt.setUsedAsDefault(false);
        mgr.persist(apt);

        apt = new OrganizationRefToPaymentType();
        apt.setOrganizationReference(orgRef);
        pt = mgr.getReference(PaymentType.class, paymentTypes.get(2).getKey());
        apt.setPaymentType(pt);
        apt.setOrganizationRole(role);
        apt.setUsedAsDefault(false);
        mgr.persist(apt);

    }

    private void addPaymentTypesToOrganizationRef(final String orgId,
            final OrganizationRoleType roleType) throws Exception {
        boolean[] defServicePaymentTypes = { false, false, false };
        boolean[] defPaymentTypes = { false, false, false };
        addPaymentTypesToOrganizationRef(orgId, roleType,
                BaseAdmUmTest.PAYMENT_TYPE_IDS, defServicePaymentTypes,
                defPaymentTypes);
    }

    private void addPaymentTypesToOrganizationRef(final String orgId,
            final OrganizationRoleType roleType, final String[] paymentTypes)
            throws Exception {
        boolean[] defServicePaymentTypes = { false, false, false };
        boolean[] defPaymentTypes = { false, false, false };
        addPaymentTypesToOrganizationRef(orgId, roleType, paymentTypes,
                defPaymentTypes, defServicePaymentTypes);
    }

    private void addPaymentTypesToOrganizationRef(final String orgId,
            final OrganizationRoleType roleType, final String[] paymentTypes,
            final boolean[] usedAsDefaultPaymentType,
            final boolean[] usedAsServiceDefaultPaymentType) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organizations.addPaymentTypesToOrganizationRef(mgr, orgId,
                        roleType, paymentTypes, usedAsDefaultPaymentType,
                        usedAsServiceDefaultPaymentType);
                return null;
            }
        });
    }

    private void addPaymentTypesToProduct(final Product product,
            final Set<String> paymentTypes) throws Exception {

        for (String iter : paymentTypes) {
            PaymentType pt = findPaymentType(iter, mgr);
            ProductToPaymentType prodToPt = new ProductToPaymentType(product,
                    pt);
            mgr.persist(prodToPt);
            product.getPaymentTypes().add(prodToPt);
            mgr.flush();
            mgr.refresh(product);
        }

    }

    private Set<VOPaymentType> createVOPaymentTypes(String... types) {
        Set<VOPaymentType> set = new HashSet<>();
        for (String type : types) {
            VOPaymentType pt = new VOPaymentType();
            pt.setPaymentTypeId(type);
            set.add(pt);
        }
        return set;
    }

    private int getOrganizationCount() throws Exception {
        return runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Query query = mgr
                        .createQuery("SELECT count(org) FROM Organization org");
                Long result = (Long) query.getSingleResult();
                return result;
            }
        }).intValue();
    }

    /**
     * Register organization and set domicile country.
     * 
     * @throws Exception
     */
    @Test
    public void testRegisterOrganizationWithCountry() throws Exception {
        final Organization organization = new Organization();
        organization.setOrganizationId(organizationId);

        final VOUserDetails userDetails = new VOUserDetails();
        userDetails.setFirstName("Hans");
        userDetails.setLastName("Meier");
        userDetails.setEMail(TEST_MAIL_ADDRESS);
        userDetails.setUserId("admin");
        userDetails.setSalutation(Salutation.MR);
        userDetails.setLocale("de");

        final Organization persistedOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return accountMgmtLocal.registerOrganization(organization, null,
                        userDetails, null, "DE", null, null,
                        OrganizationRoleType.SUPPLIER);
            }
        });
        validateCreatedPaymentInfo(persistedOrg);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization reloadedOrg = Organizations.findOrganization(mgr,
                        persistedOrg.getOrganizationId());
                assertEquals("DE", reloadedOrg.getDomicileCountryCode());
                return null;
            }
        });
    }

    @Test
    public void testRegisterOrganizationWithDescription() throws Exception {
        final Organization organization = new Organization();
        organization.setOrganizationId(organizationId);

        final VOUserDetails userDetails = new VOUserDetails();
        userDetails.setFirstName("Hans");
        userDetails.setLastName("Meier");
        userDetails.setEMail(TEST_MAIL_ADDRESS);
        userDetails.setUserId("admin");
        userDetails.setSalutation(Salutation.MR);
        userDetails.setLocale("de");

        final Organization persistedOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return accountMgmtLocal.registerOrganization(organization, null,
                        userDetails, null, "DE", null, "The description",
                        OrganizationRoleType.SUPPLIER);
            }
        });
        validateCreatedPaymentInfo(persistedOrg);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals("The description",
                        localizer.getLocalizedTextFromDatabase("de",
                                persistedOrg.getKey(),
                                LocalizedObjectTypes.ORGANIZATION_DESCRIPTION));
                return null;
            }
        });
    }

    /**
     * Try to register organization with non-existent country code. Transaction
     * is not rolled back because it is a local interface. (Dev rule)
     * 
     * @throws Exception
     */
    @Test
    public void testRegisterOrganizationWithInvalidCountry_checkRollback()
            throws Exception {
        int orgCount = getOrganizationCount();
        final Organization organization = new Organization();
        organization.setOrganizationId(organizationId);

        final VOUserDetails userDetails = new VOUserDetails();
        userDetails.setEMail(TEST_MAIL_ADDRESS);
        userDetails.setUserId("admin");
        userDetails.setSalutation(Salutation.MR);
        userDetails.setLocale("de");

        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    accountMgmtLocal.registerOrganization(organization, null,
                            userDetails, null, "non-existing", null, null,
                            OrganizationRoleType.SUPPLIER);
                    return null;
                }
            });
        } catch (ObjectNotFoundException e) {
        }
        assertEquals(orgCount + 1, getOrganizationCount());
    }

    @Test
    public void testCheckWrongSupplierOfCustomer() throws Exception {
        final Organization customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier = Organizations.findOrganization(mgr,
                        supplierIds.get(0));
                Organization supplier1 = Organizations.findOrganization(mgr,
                        supplierIds.get(1));

                Organization customer = Organizations.createCustomer(mgr,
                        supplier);
                OrganizationReference organizationReference = new OrganizationReference(
                        supplier1, customer,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                mgr.persist(organizationReference);

                createAvailablePayment(customer, OrganizationRoleType.CUSTOMER);
                return customer;
            }
        });

        final Organization customer1 = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization customer1 = (Organization) mgr
                        .getReferenceByBusinessKey(customer);
                customer1.getSources().size();
                return customer1;
            }
        });

        assertEquals(customer1.getSources().size(), 2);

        PlatformUser customer1User = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(mgr, customer1, true,
                        "CustomerAdmin1");
            }
        });

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE);

        container.login(String.valueOf(customer1User.getKey()));
        accountMgmt.getAvailablePaymentTypesFromOrganization(productKeys[0]);

        long wrongServiceKey = 111111111111112L;
        try {
            accountMgmt.getAvailablePaymentTypesFromOrganization(
                    Long.valueOf(wrongServiceKey));
        } catch (ObjectNotFoundException e) {
            return;
        }
        fail("Supplier does not exist.");
    }

    private void assertLocalizedResources() {

        if (numberLocalizedFields == 0) {
            return;
        }
        Assert.assertEquals(1, numberLocalizedFields);

        Assert.assertEquals(LocalizedObjectTypes.MARKETPLACE_NAME,
                passedObjType[0]);
        Assert.assertEquals(3, passedLocalizedTexts[0].size());

        Assert.assertEquals("en", passedLocalizedTexts[0].get(0).getLocale());
    }

    /**
     * Small helper to verify two {@link BillingContact} objects have the same
     * content.
     */
    private static void assertEqualsVOBillingContact(VOBillingContact bc1,
            VOBillingContact bc2) {
        Class<?> bcClass = VOBillingContact.class;
        Method[] declaredMethods = bcClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.getName().startsWith("get")) {
                try {
                    String s1 = (String) method.invoke(bc1, (Object[]) null);
                    String s2 = (String) method.invoke(bc2, (Object[]) null);
                    Assert.assertEquals(s1, s2);
                } catch (Exception e) {
                    fail();
                }
            }
        }

    }

    private static VOPaymentInfo createPaymentInfo(String id) {
        VOPaymentType pt = new VOPaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        VOPaymentInfo paymentInfo = new VOPaymentInfo();
        paymentInfo.setPaymentType(pt);
        paymentInfo.setId(id);
        return paymentInfo;
    }

    @Test
    public void testGetAvailablePaymentTypes_SubMgr() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SUBSCRIPTION_MANAGER);
        Set<VOPaymentType> types = accountMgmt.getAvailablePaymentTypes();
        Assert.assertNotNull(types);
        Assert.assertEquals(2, types.size());
        Set<String> set = new HashSet<>(Arrays.asList(PaymentType.CREDIT_CARD,
                PaymentType.DIRECT_DEBIT));
        for (VOPaymentType pt : types) {
            Assert.assertTrue(set.remove(pt.getPaymentTypeId()));
        }
        Assert.assertTrue(set.isEmpty());
    }

    @Test
    public void testGetAvailablePaymentTypes() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_ORGANIZATION_ADMIN);
        Set<VOPaymentType> types = accountMgmt.getAvailablePaymentTypes();
        Assert.assertNotNull(types);
        Assert.assertEquals(2, types.size());
        Set<String> set = new HashSet<>(Arrays.asList(PaymentType.CREDIT_CARD,
                PaymentType.DIRECT_DEBIT));
        for (VOPaymentType pt : types) {
            Assert.assertTrue(set.remove(pt.getPaymentTypeId()));
        }
        Assert.assertTrue(set.isEmpty());
    }

    @Test
    public void testGetPaymentInfos_SubMgr() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                UserRoleType.SUBSCRIPTION_MANAGER.name());
        try {
            accountMgmt.getPaymentInfos();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void testGetPaymentInfos_NotAuthorized() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()));
        try {
            accountMgmt.getPaymentInfos();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void getPaymentInfosForOrgAdmin() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                UserRoleType.ORGANIZATION_ADMIN.name());
        try {
            accountMgmt.getPaymentInfosForOrgAdmin();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void getPaymentInfosForOrgAdmin_NotAuthorized() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                UserRoleType.SUBSCRIPTION_MANAGER.name());
        try {
            accountMgmt.getPaymentInfosForOrgAdmin();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void testDeletePaymentInfo_NotAuthorized() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()));
        try {
            accountMgmt.deletePaymentInfo(new VOPaymentInfo());
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void testGetBillingContacts_NotAuthorized() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()));
        try {
            accountMgmt.getBillingContacts();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void testDeleteBillingContact_NotAuthorized() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()));
        try {
            accountMgmt.deleteBillingContact(new VOBillingContact());
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void testSaveBillingContact_NotAuthorized() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()));
        try {
            accountMgmt.saveBillingContact(new VOBillingContact());
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void testSavePaymentInfo_NotAuthorized() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()));
        try {
            accountMgmt.savePaymentInfo(new VOPaymentInfo());
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAvailablePaymentTypes_NotAuthorized() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()));
        try {
            accountMgmt.getAvailablePaymentTypes();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testSavePaymentInfo_UpdateDuplicateId2() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER, ROLE_ORGANIZATION_ADMIN);

        long orgKey = supplier1User.getOrganization().getKey();
        storePaymentInfo("pi1", PaymentType.CREDIT_CARD, orgKey);
        storePaymentInfo("pi2", PaymentType.CREDIT_CARD, orgKey);

        VOPaymentInfo pi = accountMgmt.getPaymentInfos().get(0);
        pi.setId(accountMgmt.getPaymentInfos().get(1).getId());

        accountMgmt.savePaymentInfo(pi);
    }

    private PaymentInfo storePaymentInfo(final String id,
            final String paymentTypeId, final long orgKey) throws Exception {
        return runTX(new Callable<PaymentInfo>() {
            @Override
            public PaymentInfo call() throws Exception {
                PaymentType pt = new PaymentType();
                pt.setPaymentTypeId(paymentTypeId);
                DomainObject<?> storedPt = mgr.find(pt);
                if (storedPt == null) {
                    mgr.persist(pt);
                } else {
                    pt = (PaymentType) storedPt;
                }

                PaymentInfo pi = new PaymentInfo();
                pi.setPaymentInfoId(id);
                pi.setOrganization(
                        mgr.getReference(Organization.class, orgKey));
                pi.setPaymentType(pt);
                mgr.persist(pi);
                return pi;
            };
        });
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSavePaymentInfo_Error() throws Exception {
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER, ROLE_ORGANIZATION_ADMIN);
        accountMgmt.savePaymentInfo(createPaymentInfo("test1"));
    }

    private void validateCreatedPaymentInfo(final Organization temp)
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        temp.getKey());
                Assert.assertEquals(1, org.getPaymentInfos().size());
                PaymentInfo pi = org.getPaymentInfos().get(0);
                Assert.assertEquals(PaymentType.INVOICE,
                        pi.getPaymentType().getPaymentTypeId());
                Assert.assertEquals(PAYMENT_INFO_NAME, pi.getPaymentInfoId());
                return null;
            }
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testGetServiceDefaultPaymentConfiguration_NotAuthorized()
            throws Exception {
        container.login(String.valueOf(supplier1User.getKey()));
        try {
            accountMgmt.getDefaultServicePaymentConfiguration();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testGetServiceDefaultPaymentConfiguration_Empty()
            throws Exception {

        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        Set<VOPaymentType> ptSet = accountMgmt
                .getDefaultServicePaymentConfiguration();
        assertTrue(ptSet.isEmpty());
    }

    @Test
    public void testGetServiceDefaultPaymentConfiguration_NotEmpty()
            throws Exception {

        boolean[] usedAsServiceDefPaymentType = { true, false, false };
        boolean[] usedAsDefPaymentType = { false, false, false };

        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER, BaseAdmUmTest.PAYMENT_TYPE_IDS,
                usedAsDefPaymentType, usedAsServiceDefPaymentType);

        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        Set<VOPaymentType> ptSet = accountMgmt
                .getDefaultServicePaymentConfiguration();
        assertTrue(ptSet.size() == 1);
        Iterator<VOPaymentType> iter = ptSet.iterator();
        VOPaymentType voPt = iter.next();
        assertEquals(PaymentType.INVOICE, voPt.getPaymentTypeId());
    }

    @Test
    public void testGetServicePaymentConfiguration() throws Exception {

        // add all payment types to supplier
        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);

        // prepare expected payment types per product
        final Set<String> expectedActive = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS));
        final Set<String> expectedInactive = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_INV_CC));
        final Set<String> expectedSuspended = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_CC_DD));

        // create customer - needed for customer specific products
        Organization customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {

                Organization supplier = Organizations.findOrganization(mgr,
                        supplierIds.get(0));
                Organization customer = Organizations.createCustomer(mgr,
                        supplier);
                return customer;
            }
        });

        // prepare products - each has a customer specific also
        prepareProduct(providerId, supplierIds.get(0),
                customer.getOrganizationId(), ServiceStatus.ACTIVE,
                expectedActive, true);

        prepareProduct(providerId, supplierIds.get(0),
                customer.getOrganizationId(), ServiceStatus.INACTIVE,
                expectedInactive, true);

        prepareProduct(providerId, supplierIds.get(0),
                customer.getOrganizationId(), ServiceStatus.SUSPENDED,
                expectedSuspended, true);

        // login as supplier user
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        // test method
        List<VOServicePaymentConfiguration> result = accountMgmt
                .getServicePaymentConfiguration();

        // verify result - only template products expected
        assertEquals(3, result.size());

        for (VOServicePaymentConfiguration iter : result) {

            VOService voService = iter.getService();
            Set<VOPaymentType> servicePT = iter.getEnabledPaymentTypes();
            Set<String> resultPt = new HashSet<>();
            for (VOPaymentType spt : servicePT) {
                resultPt.add(spt.getPaymentTypeId());
            }

            // check payment types
            switch (voService.getStatus()) {
            case ACTIVE:
                assertEquals(expectedActive, resultPt);
                break;
            case INACTIVE:
                assertEquals(expectedInactive, resultPt);
                break;
            case SUSPENDED:
                assertEquals(expectedSuspended, resultPt);
                break;
            default:
                assertTrue(servicePT.isEmpty());
            }
        }

    }

    @Test
    public void testGetServicePaymentConfiguration_Mixed() throws Exception {

        int numChargeableServices = 3;
        int numFreeServices = 2;
        int numNoPriceModel = 2;

        // add all payment types to supplier
        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER);

        // prepare expected payment types per product
        final Set<String> paymentTypes = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS));

        // prepare chargeable services
        for (int i = 0; i < numChargeableServices; i++) {
            prepareProduct(providerId, supplierIds.get(0), true, paymentTypes,
                    true);
        }

        // prepare free services
        for (int i = 0; i < numFreeServices; i++) {
            prepareProduct(providerId, supplierIds.get(0), false, paymentTypes,
                    true);
        }

        // prepare services without price model
        for (int i = 0; i < numNoPriceModel; i++) {
            prepareProduct(providerId, supplierIds.get(0), false, paymentTypes,
                    false);
        }

        // prepare services with external price model
        for (int i = 0; i < numNoPriceModel; i++) {
            prepareProductWithExternalPriceModel(providerId, supplierIds.get(0),
                    false, paymentTypes, false);
        }

        // login as supplier user
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        // test method
        List<VOServicePaymentConfiguration> result = accountMgmt
                .getServicePaymentConfiguration();

        // verify result - all services expected
        assertEquals(numChargeableServices + numFreeServices + numNoPriceModel,
                result.size());

        int chargeableCount = 0;
        for (VOServicePaymentConfiguration iter : result) {
            VOPriceModel pm = iter.getService().getPriceModel();
            if (pm != null && pm.isChargeable()) {
                Set<VOPaymentType> servicePT = iter.getEnabledPaymentTypes();
                Set<String> resultPt = new HashSet<>();
                for (VOPaymentType spt : servicePT) {
                    resultPt.add(spt.getPaymentTypeId());
                }
                assertEquals(paymentTypes, resultPt);
                chargeableCount++;
            }
        }
        assertEquals(numChargeableServices, chargeableCount);
    }

    @Test
    public void testGetAvailablePaymentTypesFromSupplier_WithRelationNoServicePaymentTypes()
            throws Exception {
        Set<String> prodPt = new HashSet<>();
        Set<String> custPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_INV_CC));
        // intersect prodPt and custPt
        Set<String> expPt = new HashSet<>();
        getAvailablePaymentTypesFromSupplier(prodPt, custPt, true, expPt);
    }

    @Test
    public void testGetAvailablePaymentTypesFromSupplier_WithRelationSomeServicePaymentTypes()
            throws Exception {
        Set<String> prodPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_INV_DD));
        Set<String> custPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_CC_DD));
        // expected: intersect prodPt and custPt
        Set<String> expPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_DD));
        getAvailablePaymentTypesFromSupplier(prodPt, custPt, true, expPt);
    }

    @Test
    public void testGetAvailablePaymentTypesFromSupplier_WithRelationAllServicePaymentTypes()
            throws Exception {
        Set<String> prodPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS));
        Set<String> custPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_INV_CC));
        // expected: intersect prodPt and custPt
        Set<String> expPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_INV_CC));
        getAvailablePaymentTypesFromSupplier(prodPt, custPt, true, expPt);
    }

    @Test
    public void testGetAvailablePaymentTypesFromSupplier_NoRelationNoSupplierDefaults()
            throws Exception {
        Set<String> prodPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_INV));
        Set<String> suppDefPt = new HashSet<>();
        // expected: intersect prodPt and suppDefPt
        Set<String> expPt = new HashSet<>();
        getAvailablePaymentTypesFromSupplier(prodPt, suppDefPt, false, expPt);
    }

    @Test
    public void testGetAvailablePaymentTypesFromSupplier_NoRelationSomeSupplierDefaults()
            throws Exception {
        Set<String> prodPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_INV_DD));
        Set<String> suppDefPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_INV));
        // expected: intersect prodPt and suppDefPt
        Set<String> expPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_INV));
        getAvailablePaymentTypesFromSupplier(prodPt, suppDefPt, false, expPt);
    }

    @Test
    public void testGetAvailablePaymentTypesFromSupplier_NoRelationAllSupplierDefaults()
            throws Exception {
        Set<String> prodPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_INV_DD));
        Set<String> suppDefPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS));
        // expected: intersect prodPt and suppDefPt
        Set<String> expPt = new HashSet<>(
                Arrays.asList(BaseAdmUmTest.PAYMENT_TYPE_IDS_INV_DD));
        getAvailablePaymentTypesFromSupplier(prodPt, suppDefPt, false, expPt);
    }

    private void getAvailablePaymentTypesFromSupplier(
            Set<String> productPaymentTypes, final Set<String> orgPaymentTypes,
            final boolean withRelation, Set<String> expectedPaymentTypes)
            throws Exception {

        // add all payment types to supplier and defaults if specified
        boolean[] usedAsDefPType = { false, false, false };
        boolean[] usedAsServiceDefPType = { false, false, false };
        if (withRelation == false) {
            if (orgPaymentTypes.contains(INVOICE))
                usedAsDefPType[0] = true;
            if (orgPaymentTypes.contains(DIRECT_DEBIT))
                usedAsDefPType[1] = true;
            if (orgPaymentTypes.contains(CREDIT_CARD))
                usedAsDefPType[2] = true;
        } else {
            if (orgPaymentTypes.contains(INVOICE))
                usedAsServiceDefPType[0] = true;
            if (orgPaymentTypes.contains(DIRECT_DEBIT))
                usedAsServiceDefPType[1] = true;
            if (orgPaymentTypes.contains(CREDIT_CARD))
                usedAsServiceDefPType[2] = true;
        }

        addPaymentTypesToOrganizationRef(supplierIds.get(0),
                OrganizationRoleType.SUPPLIER, BaseAdmUmTest.PAYMENT_TYPE_IDS,
                usedAsDefPType, usedAsServiceDefPType);

        // create customer with reference to supplier and payment types
        Organization customer = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {

                Organization supplier = Organizations.findOrganization(mgr,
                        supplierIds.get(0));
                Organization customer;
                if (withRelation) {
                    customer = Organizations.createCustomer(mgr, supplier);
                } else {
                    customer = Organizations.createOrganization(mgr,
                            OrganizationRoleType.CUSTOMER);
                    mgr.persist(customer);
                }

                customerUser = Organizations.createUserForOrg(mgr, customer,
                        true, "CustomerAdmin1");

                return customer;
            }
        });

        // add customer payment types
        if (withRelation) {
            String[] customerPtArr = new String[] {};
            addPaymentTypesToOrganizationRef(customer.getOrganizationId(),
                    OrganizationRoleType.CUSTOMER,
                    orgPaymentTypes.toArray(customerPtArr));
        }

        // prepare products (template and customer-specific) with payment types
        Set<Product> prod = prepareProduct(providerId, supplierIds.get(0),
                customer.getOrganizationId(), ServiceStatus.ACTIVE,
                productPaymentTypes, true);

        // login as customer user
        container.login(String.valueOf(customerUser.getKey()),
                ROLE_SERVICE_MANAGER);

        // test for template and copy product
        for (Product testProduct : prod) {

            // call the method to be tested
            Set<VOPaymentType> voResult = accountMgmt
                    .getAvailablePaymentTypesFromOrganization(
                            new Long(testProduct.getKey()));

            // verify results
            Set<String> result = new HashSet<>();
            for (VOPaymentType iter : voResult) {
                result.add(iter.getPaymentTypeId());
            }

            assertEquals(expectedPaymentTypes, result);
        }
    }

    @Test(expected = OrganizationAuthorityException.class)
    public void registerOrganization_platformOperator() throws Throwable {
        final Organization organization = prepareCustomerOrganization();
        final VOUserDetails userDetails = prepareLdapUser(null, null);
        createCustomerOrganizationWithAdmin(organization, userDetails, null,
                OrganizationRoleType.PLATFORM_OPERATOR);
    }

    @Test
    public void registerOrganization_ldapPropertiesNull() throws Throwable {
        final VOUserDetails userDetails = new VOUserDetails();
        userDetails.setFirstName("Hans");
        userDetails.setLastName("Meier");
        userDetails.setEMail(TEST_MAIL_ADDRESS);
        userDetails.setUserId("admin");
        userDetails.setSalutation(Salutation.MR);
        userDetails.setPhone("123456");
        userDetails.setLocale("de");

        final Organization organization = prepareCustomerOrganization();
        Organization org = createCustomerOrganizationWithAdmin(organization,
                userDetails, null, new OrganizationRoleType[] {});

        // expected: created organization is not LDAP-managed (no organization
        // setting created, no LDAP import triggered)
        verifyCreationCustomerOrganizationWithAdmin(organization.getKey(),
                userDetails, org, OrganizationRoleType.CUSTOMER);
        assertEquals(
                "No organisation setting must have been created because LDAP is not to be used",
                0, getOrganisationSetting(org, null).size());
    }

    @Test
    public void registerOrganization_ldapPropertiesMinimumGiven()
            throws Throwable {
        final Organization organization = prepareCustomerOrganization();
        final VOUserDetails userDetails = prepareLdapUser(null, null);

        final LdapProperties props = new LdapProperties();
        String myLdapUrl = "someUrl";
        String myBaseDn = "ou=people,dc=est,dc=fujitsu,dc=de";
        props.setProperty(SettingType.LDAP_URL.name(), myLdapUrl);
        props.setProperty(SettingType.LDAP_BASE_DN.name(), myBaseDn);
        when(ldapSettingMmgtMock
                .getDefaultValueForSetting(any(SettingType.class)))
                        .thenReturn("someDefault");

        try {
            createCustomerOrganizationWithAdmin(organization, userDetails,
                    props.asProperties(), new OrganizationRoleType[] {});
        } catch (ValidationException e) {
            // make sure the two missing mandatory settings are added
            // automatically
            verify(ldapSettingMmgtMock, times(1)).getDefaultValueForSetting(
                    eq(SettingType.LDAP_CONTEXT_FACTORY));
            verify(ldapSettingMmgtMock, times(1))
                    .getDefaultValueForSetting(eq(SettingType.LDAP_ATTR_UID));
        }
    }

    private Organization prepareCustomerOrganization() {
        final Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        return organization;
    }

    private Organization createCustomerOrganizationWithAdmin(
            final Organization organization, final VOUserDetails user,
            final Properties ldapProperties, final OrganizationRoleType... role)
            throws Exception {
        // the registration of customer organizations, creation of the
        // administrator user
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = accountMgmtLocal.registerOrganization(
                        organization, null, user, ldapProperties, "DE",
                        "FUJITSU", null, role);
                assertEquals(0, org.getMarketplaceToOrganizations().size());
                load(org);
                return org;
            }
        });
    }

    private void verifyCreationCustomerOrganizationWithAdmin(final long orgKey,
            final VOUserDetails user, final Organization createdOrg,
            final OrganizationRoleType expectedRole) throws Exception {
        // verify result, if a mail has been received cannot be tested, as this
        // test uses a stub for the communication service. This has to be added
        // in the tested in the CTs then

        // verify customer organization has been persisted
        assertTrue("Organization has not been stored",
                0 != createdOrg.getKey());
        assertTrue("Organization id not set",
                null != createdOrg.getOrganizationId());
        assertTrue("Missing authority for role",
                createdOrg.hasRole(expectedRole));

        PlatformUser adminUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = mgr.getReference(Organization.class, orgKey);
                List<PlatformUser> users = org.getPlatformUsers();
                PlatformUser admin = users.get(0);
                assertEquals("User has not been stored", 1, users.size());
                assertTrue("Mising ORGANIZATION_ADMIN role for user",
                        admin.hasRole(UserRoleType.ORGANIZATION_ADMIN));
                return admin;
            }
        });

        // verify admin User organization has been persisted
        assertTrue("Wrong key for user", 0 != adminUser.getKey());
        assertEquals("Wrong user data", user.getFirstName(),
                adminUser.getFirstName());
        assertEquals("Wrong user data", user.getLastName(),
                adminUser.getLastName());
        assertEquals("Wrong user data", user.getUserId(),
                adminUser.getUserId());
        assertEquals("Wrong user data", user.getSalutation(),
                adminUser.getSalutation());
        assertEquals("Wrong user data", user.getPhone(), adminUser.getPhone());
        assertEquals("Wrong user data", user.getLocale(),
                adminUser.getLocale());
        assertEquals("Wrong mail for user", user.getEMail(),
                adminUser.getEmail());

    }

    // get all or a specific organisationSetting for a given organization
    private List<OrganizationSetting> getOrganisationSetting(
            final Organization org, final SettingType type) throws Throwable {
        try {
            return runTX(new Callable<List<OrganizationSetting>>() {
                @Override
                @SuppressWarnings("unchecked")
                public List<OrganizationSetting> call() throws Exception {
                    Query query = mgr.createQuery(
                            "SELECT obj FROM OrganizationSetting obj WHERE "
                                    + (type != null
                                            ? "obj.dataContainer.settingType = :settingType and "
                                            : "")
                                    + "obj.organization = :organization");
                    query.setParameter("organization", org);
                    if (type != null) {
                        query.setParameter("settingType", type);
                    }
                    return query.getResultList();
                }
            });
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Test
    public void savePaymentConfiguration_AddCustomerPaymentTypeWithSuspendedSubscriptionOfBroker()
            throws Exception {
        // given
        setupSupplierAndReseller(OrganizationRoleType.BROKER,
                SubscriptionStatus.SUSPENDED, false, true);

        // when
        accountMgmt.savePaymentConfiguration(pts,
                getOrganizationPaymentConfiguration(false), pts, null);

        // then
        assertEquals(SubscriptionStatus.ACTIVE, getSubStatus(subSupplier));
        assertTrue(instanceActivated);
        assertFalse(instanceDeactivated);
        assertEquals(SubscriptionStatus.ACTIVE, getSubStatus(subReseller));
    }

    @Test
    public void savePaymentConfiguration_RemoveCustomerPaymentTypeWithActiveSubscriptionOfBroker()
            throws Exception {
        // given
        setupSupplierAndReseller(OrganizationRoleType.BROKER,
                SubscriptionStatus.ACTIVE, true, true);

        // when
        accountMgmt.savePaymentConfiguration(pts,
                getOrganizationPaymentConfiguration(true), pts, null);

        // then
        assertEquals(SubscriptionStatus.SUSPENDED, getSubStatus(subSupplier));
        assertTrue(instanceDeactivated);
        assertFalse(instanceActivated);
        assertEquals(SubscriptionStatus.SUSPENDED, getSubStatus(subReseller));
    }

    @Test
    public void savePaymentConfiguration_AddCustomerPaymentTypeWithSuspendedSubscriptionOfReseller()
            throws Exception {
        // given
        setupSupplierAndReseller(OrganizationRoleType.RESELLER,
                SubscriptionStatus.SUSPENDED, false, true);

        // when
        accountMgmt.savePaymentConfiguration(pts,
                getOrganizationPaymentConfiguration(false), pts, null);

        // then
        assertEquals(SubscriptionStatus.ACTIVE, getSubStatus(subSupplier));
        assertTrue(instanceActivated);
        assertFalse(instanceDeactivated);
        assertEquals(SubscriptionStatus.SUSPENDED, getSubStatus(subReseller));
    }

    @Test
    public void savePaymentConfiguration_RemoveCustomerPaymentTypeWithActiveSubscriptionOfReseller()
            throws Exception {
        // given
        setupSupplierAndReseller(OrganizationRoleType.RESELLER,
                SubscriptionStatus.ACTIVE, true, true);

        // when
        accountMgmt.savePaymentConfiguration(pts,
                getOrganizationPaymentConfiguration(true), pts, null);

        // then
        assertEquals(SubscriptionStatus.SUSPENDED, getSubStatus(subSupplier));
        assertTrue(instanceDeactivated);
        assertFalse(instanceActivated);
        assertEquals(SubscriptionStatus.ACTIVE, getSubStatus(subReseller));
    }

    @Test
    public void savePaymentConfiguration_AddServicePaymentTypeWithSuspendedSubscriptionOfBroker()
            throws Exception {

        // given
        Product p = setupSupplierAndReseller(OrganizationRoleType.BROKER,
                SubscriptionStatus.SUSPENDED, true, false);

        // when

        accountMgmt.savePaymentConfiguration(pts, null, pts,
                getServicePaymentConfiguration(true, p));

        // then
        assertEquals(SubscriptionStatus.ACTIVE, getSubStatus(subSupplier));
        assertTrue(instanceActivated);
        assertFalse(instanceDeactivated);
        assertEquals(SubscriptionStatus.ACTIVE, getSubStatus(subReseller));

    }

    @Test
    public void savePaymentConfiguration_RemoveServicePaymentTypeWithActiveSubscriptionOfBroker()
            throws Exception {
        // given
        Product p = setupSupplierAndReseller(OrganizationRoleType.BROKER,
                SubscriptionStatus.ACTIVE, true, true);

        // when
        accountMgmt.savePaymentConfiguration(pts, null, pts,
                getServicePaymentConfiguration(false, p));

        // then
        assertEquals(SubscriptionStatus.SUSPENDED, getSubStatus(subSupplier));
        assertFalse(instanceActivated);
        assertTrue(instanceDeactivated);
        assertEquals(SubscriptionStatus.SUSPENDED, getSubStatus(subReseller));
    }

    @Test
    public void savePaymentConfiguration_AddServicePaymentTypeWithSuspendedSubscriptionOfReseller()
            throws Exception {
        // given
        Product p = setupSupplierAndReseller(OrganizationRoleType.RESELLER,
                SubscriptionStatus.SUSPENDED, true, false);

        // when
        accountMgmt.savePaymentConfiguration(pts, null, pts,
                getServicePaymentConfiguration(true, p));

        // then
        assertEquals(SubscriptionStatus.ACTIVE, getSubStatus(subSupplier));
        assertTrue(instanceActivated);
        assertFalse(instanceDeactivated);
        assertEquals(SubscriptionStatus.SUSPENDED, getSubStatus(subReseller));
    }

    @Test
    public void savePaymentConfiguration_RemoveServicePaymentTypeWithActiveSubscriptionOfReseller()
            throws Exception {
        // given
        Product p = setupSupplierAndReseller(OrganizationRoleType.RESELLER,
                SubscriptionStatus.ACTIVE, true, true);

        // when
        accountMgmt.savePaymentConfiguration(pts, null, pts,
                getServicePaymentConfiguration(false, p));

        // then
        assertEquals(SubscriptionStatus.SUSPENDED, getSubStatus(subSupplier));
        assertFalse(instanceActivated);
        assertTrue(instanceDeactivated);
        assertEquals(SubscriptionStatus.ACTIVE, getSubStatus(subReseller));
    }

    @Test
    public void registerKnownCustomerWithRestrictedMarketplace()
            throws Exception {

        final Organization supplier = registerSupplier("admin");
        runTX(new Callable<VOMarketplace>() {
            @Override
            public VOMarketplace call() throws Exception {
                Marketplace mpRestricted = new Marketplace();
                mpRestricted.setMarketplaceId(marketplaceId + "_restricted");
                if (mgr.find(mpRestricted) == null) {

                    Organization platformOperator = Organizations
                            .findOrganization(mgr, "PLATFORM_OPERATOR");
                    mpRestricted = Marketplaces
                            .createMarketplaceWithRestrictedAccessAndAccessibleOrganizations(
                                    platformOperator,
                                    marketplaceId + "_restricted", mgr,
                                    Arrays.asList(supplier));
                }
                return MarketplaceAssembler.toVOMarketplace(mpRestricted,
                        new LocalizerFacade(localizer, "en"));
            }
        });

        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setLocale(org.getLocale());
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setUserId("testuser");
        accountMgmt.registerKnownCustomer(org, user, null,
                marketplaceId + "_restricted");
    }

    private Product setupSupplierAndReseller(final OrganizationRoleType role,
            final SubscriptionStatus status, boolean addPaymentTypesToCustomer,
            boolean addPaymentTypesToService) throws Exception {
        final Organization broker = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization b = Organizations.createOrganization(mgr, role);
                Organizations.createUserForOrg(mgr, b, true, "SuppAdmin1");
                Organizations.supportAllCountries(mgr, b);

                Organization platformOperator = Organizations
                        .findOrganization(mgr, "PLATFORM_OPERATOR");
                OrganizationReference ref = new OrganizationReference(
                        platformOperator, b,
                        OrganizationReferenceType.PLATFORM_OPERATOR_TO_RESELLER);
                mgr.persist(ref);
                mgr.flush();
                return b;
            }
        });

        PlatformUser technologyProviderUser = runTX(
                new Callable<PlatformUser>() {
                    @Override
                    public PlatformUser call() throws Exception {
                        Organization organization = Organizations
                                .createOrganization(mgr,
                                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                        return Organizations.createUserForOrg(mgr, organization,
                                true, "PROV_ADMIN");
                    }
                });
        final Set<String> pts = addPaymentTypesToService
                ? new HashSet<>(
                        Arrays.asList(INVOICE, CREDIT_CARD, DIRECT_DEBIT))
                : new HashSet<String>();
        final Product productOfSupplier = prepareProduct(
                technologyProviderUser.getOrganization().getOrganizationId(),
                supplier1User.getOrganization().getOrganizationId(), true, pts,
                true).iterator().next();
        final Product productOfBroker = prepareProduct(
                technologyProviderUser.getOrganization().getOrganizationId(),
                broker.getOrganizationId(), true, pts, true).iterator().next();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product p = mgr.getReference(Product.class,
                        productOfBroker.getKey());
                Product p2 = new Product();
                p2.setProductId("testProd1");
                p.setTemplate(mgr.getReference(Product.class,
                        productOfSupplier.getKey()));
                p.setType(ServiceType.PARTNER_TEMPLATE);
                mgr.persist(p);
                mgr.flush();
                return null;
            }
        });

        addPaymentTypesToOrganizationRef(
                supplier1User.getOrganization().getOrganizationId(),
                OrganizationRoleType.SUPPLIER);
        container.login(String.valueOf(supplier1User.getKey()),
                ROLE_SERVICE_MANAGER);

        Set<VOPaymentType> defaultPaymentTypes = addPaymentTypesToCustomer
                ? createVOPaymentTypes(CREDIT_CARD, DIRECT_DEBIT, INVOICE)
                : createVOPaymentTypes();
        accountMgmt.savePaymentConfiguration(defaultPaymentTypes,
                new ArrayList<VOOrganizationPaymentConfiguration>(),
                defaultPaymentTypes, null);

        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry("DE");
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerKnownCustomer(org,
                user, null, marketplaceId);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                customerUser = mgr
                        .getReference(Organization.class, customer.getKey())
                        .getOrganizationAdmins().get(0);
                customerUser.getOrganization().getDomicileCountryCode();
                Organization org = mgr.getReference(Organization.class,
                        customer.getKey());
                PaymentInfo paymentInfo = PaymentInfos.createPaymentInfo(org,
                        mgr, paymentTypes.get(0));
                paymentInfo.setExternalIdentifier("test");
                Subscription sub = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(),
                        productOfSupplier.getProductId(), "testSub",
                        Organizations.findOrganization(mgr, supplier1User
                                .getOrganization().getOrganizationId()));
                sub.setStatus(status);
                BillingContact bc = PaymentInfos.createBillingContact(mgr, org);
                sub.setBillingContact(bc);
                sub.setPaymentInfo(paymentInfo);
                Subscription sub2 = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(),
                        productOfBroker.getProductId(), "testSub2",
                        Organizations.findOrganization(mgr,
                                broker.getOrganizationId()));
                sub2.setStatus(status);
                sub2.setPaymentInfo(paymentInfo);
                sub2.setBillingContact(bc);
                OrganizationReference ref = new OrganizationReference(broker,
                        mgr.getReference(Organization.class, customer.getKey()),
                        OrganizationReferenceType.BROKER_TO_CUSTOMER);
                mgr.persist(ref);
                mgr.flush();
                subSupplier = sub.getKey();
                subReseller = sub2.getKey();
                return null;
            }
        });
        return productOfSupplier;
    }

    private List<VOOrganizationPaymentConfiguration> getOrganizationPaymentConfiguration(
            boolean removePaymentTypesToCustomer) {
        List<VOOrganizationPaymentConfiguration> customerPaymentTypeConfiguration = new ArrayList<>();
        VOOrganizationPaymentConfiguration conf = new VOOrganizationPaymentConfiguration();
        conf.setOrganization(OrganizationAssembler.toVOOrganization(
                customerUser.getOrganization(), false,
                new LocalizerFacade(localizer, Locale.ENGLISH.toString())));
        Set<VOPaymentType> defaultPaymentTypes = removePaymentTypesToCustomer
                ? createVOPaymentTypes()
                : createVOPaymentTypes(CREDIT_CARD, DIRECT_DEBIT, INVOICE);
        conf.setEnabledPaymentTypes(defaultPaymentTypes);
        customerPaymentTypeConfiguration.add(conf);
        return customerPaymentTypeConfiguration;
    }

    private List<VOServicePaymentConfiguration> getServicePaymentConfiguration(
            final boolean suspendService, final Product productOfSupplier)
            throws Exception {
        return runTX(new Callable<List<VOServicePaymentConfiguration>>() {

            @Override
            public List<VOServicePaymentConfiguration> call() throws Exception {
                DomainObject<?> domainObject = mgr.find(productOfSupplier);
                List<VOServicePaymentConfiguration> servicePaymentTypeConfiguration = new ArrayList<>();
                VOServicePaymentConfiguration conf2 = new VOServicePaymentConfiguration();
                conf2.setService(
                        ProductAssembler.toVOProduct((Product) domainObject,
                                new LocalizerFacade(localizer, "EN")));
                servicePaymentTypeConfiguration.add(conf2);
                if (suspendService) {
                    Set<VOPaymentType> pts = createVOPaymentTypes(CREDIT_CARD,
                            DIRECT_DEBIT, INVOICE);
                    accountMgmt.savePaymentConfiguration(pts, null, pts,
                            servicePaymentTypeConfiguration);
                }
                conf2.setEnabledPaymentTypes(
                        suspendService
                                ? createVOPaymentTypes(CREDIT_CARD,
                                        DIRECT_DEBIT, INVOICE)
                                : createVOPaymentTypes());
                return servicePaymentTypeConfiguration;
            }

        });
    }

    private SubscriptionStatus getSubStatus(final long key) throws Exception {
        return runTX(new Callable<SubscriptionStatus>() {

            @Override
            public SubscriptionStatus call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class, key);
                return sub.getStatus();
            }
        });
    }
}
