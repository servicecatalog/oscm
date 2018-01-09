/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: barzu                                    
 *                                                                              
 *  Creation Date: Apr 19, 2012                                                      
 *                                                                              
 *  Completion Time: Apr 19, 2012                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.subscriptionservice.dao.OrganizationDao;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * Unit testing for the email sending functionality of
 * {@link SubscriptionServiceBean}.
 * 
 * @author barzu
 */
public class SubscriptionServiceBeanEmailTest {

    private SubscriptionServiceBean bean;
    private TerminateSubscriptionBean terminateBean;
    private ManageSubscriptionBean manageBean;
    private ModifyAndUpgradeSubscriptionBean modifyAndUpgradeBean;

    private Subscription subscription;
    private PlatformUser user;
    private TechnicalProduct technicalProduct;
    private Product subscriptionCopy;
    private Product template;
    private OrganizationDao orgDao = mock(OrganizationDao.class);
    private List<PlatformUser> givenUsers = new ArrayList<PlatformUser>();

    private final ArgumentCaptor<String> stringArg = ArgumentCaptor
            .forClass(String.class);
    private final ArgumentCaptor<String[]> arrayArg = ArgumentCaptor
            .forClass(String[].class);

    private static String[] expectedSupportMailParams;
    private static String[] expectedSupportMailParamsWithoutOrgName;
    final static String CUSTOMER_ID = "Customer 123";
    final static String CUSTOMER_NAME = "Customer Ltd.";
    final static String SUPPLIER_ID = "Supplier 123";
    final static String SUPPLIER_SUPPORT_EMAIL = "supplier-support@test.de";
    final static String BROKER_ID = "Broker 123";
    final static String BROKER_SUPPORT_EMAIL = "broker-support@test.de";
    final static String RESELLER_ID = "Reseller 123";
    final static String RESELLER_SUPPORT_EMAIL = "reseller-support@test.de";

    private final String SUBSCRIPTION_ID = "SubscriptionName";
    private final String SUBSCRIPTION_PRODINST = "SubscriptionProdInstanceID";
    private final String TECHNICAL_PRODUCT_BUILD = "Build 2.1.0";
    private final String TECHNICAL_PRODUCT_VERSION = "1.0";
    private final String MAIL_SUBJECT = "Bug";
    private final String MAIL_TEXT = "A bug occurred.";
    private static final String ORGANIZATION_ID = "orgId";
    private List<VOLocalizedText> reason;

    @Before
    public void setup() throws Exception {
        bean = spy(new SubscriptionServiceBean());
        DataService dsMock = mock(DataService.class);
        CommunicationServiceLocal commServiceMock = mock(CommunicationServiceLocal.class);
        bean.commService = commServiceMock;
        bean.dataManager = dsMock;
        bean.audit = mock(SubscriptionAuditLogCollector.class);

        terminateBean = spy(new TerminateSubscriptionBean());
        terminateBean.dataManager = bean.dataManager;
        terminateBean.commService = bean.commService;
        terminateBean.audit = bean.audit;
        bean.terminateBean = terminateBean;

        manageBean = spy(new ManageSubscriptionBean());
        manageBean.dataManager = bean.dataManager;
        manageBean.audit = bean.audit;
        manageBean.commService = bean.commService;
        bean.manageBean = manageBean;

        modifyAndUpgradeBean = spy(new ModifyAndUpgradeSubscriptionBean());
        modifyAndUpgradeBean.dataManager = bean.dataManager;
        modifyAndUpgradeBean.commService = bean.commService;
        modifyAndUpgradeBean.localizer = mock(LocalizerServiceLocal.class);
        bean.modUpgBean = modifyAndUpgradeBean;

        final String PRODUCT_ID = "ProductID";
        final String TECHNICAL_PRODUCT_ID = "TechnicalProductID";
        final String PRODUCT_TEMPLATE_ID = "ProductTemplate_ID";

        // Product
        technicalProduct = new TechnicalProduct();
        technicalProduct.setTechnicalProductId(TECHNICAL_PRODUCT_ID);
        technicalProduct.setTechnicalProductBuildId(TECHNICAL_PRODUCT_BUILD);
        technicalProduct.setProvisioningVersion(TECHNICAL_PRODUCT_VERSION);
        template = new Product();
        template.setKey(1L);
        template.setProductId(PRODUCT_TEMPLATE_ID);
        template.setTechnicalProduct(technicalProduct);
        template.setType(ServiceType.TEMPLATE);
        subscriptionCopy = new Product();
        subscriptionCopy.setProductId(PRODUCT_ID);
        subscriptionCopy.setTechnicalProduct(technicalProduct);
        subscriptionCopy.setTemplate(template);
        subscriptionCopy.setType(ServiceType.SUBSCRIPTION);

        // Subscription
        subscription = new Subscription();
        subscription.setSubscriptionId(SUBSCRIPTION_ID);
        subscription.setProductInstanceId(SUBSCRIPTION_PRODINST);
        subscription.bindToProduct(subscriptionCopy);

        expectedSupportMailParams = new String[] { MAIL_SUBJECT, CUSTOMER_NAME,
                CUSTOMER_ID, SUBSCRIPTION_ID, "%s", "", "%s",
                TECHNICAL_PRODUCT_ID, "%s", "%s", MAIL_TEXT };
        expectedSupportMailParamsWithoutOrgName = new String[] { MAIL_SUBJECT,
                "-", CUSTOMER_ID, SUBSCRIPTION_ID, "%s", "",
                PRODUCT_TEMPLATE_ID, TECHNICAL_PRODUCT_ID, "%s", "%s",
                MAIL_TEXT };

        // Current user
        user = new PlatformUser();
        bean.localizer = mock(LocalizerServiceLocal.class);
        manageBean.localizer = bean.localizer;
        user.setLocale("en");
        doReturn(user).when(bean.dataManager).getCurrentUser();
        user.setAssignedRoles(newRoleAssignment(user,
                UserRoleType.ORGANIZATION_ADMIN));
        reason = new ArrayList<VOLocalizedText>();

        subscription.setOwner(user);

        bean.manageBean.userGroupService = mock(UserGroupServiceLocalBean.class);
        doReturn(Collections.EMPTY_LIST).when(bean.manageBean.userGroupService)
                .getUserGroupsForUserWithRole(anyLong(), anyLong());
    }

    @Test
    public void getAccessInfo_LOGIN() throws Exception {
        technicalProduct.setAccessType(ServiceAccessType.LOGIN);
        doReturn("URL").when(bean).getSubscriptionUrl(eq(subscription));

        String info = bean.getAccessInfo(subscription, user);
        Assert.assertEquals("URL", info);
        verify(bean, times(1)).getSubscriptionUrl(eq(subscription));
    }

    @Test
    public void getAccessInfo_DIRECT_fromSubscription() throws Exception {
        technicalProduct.setAccessType(ServiceAccessType.DIRECT);
        subscription.setAccessInfo("URL");

        String info = bean.getAccessInfo(subscription, user);
        Assert.assertEquals("URL", info);
        verify(bean, times(0)).getSubscriptionUrl(eq(subscription));
    }

    private void mockTechProdAccessInfo(String url) {
        bean.localizer = mock(LocalizerServiceLocal.class);
        doReturn(url).when(bean.localizer).getLocalizedTextFromDatabase(
                anyString(), Matchers.anyLong(),
                eq(LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
    }

    @Test
    public void getAccessInfo_DIRECT_fromTechService() throws Exception {
        technicalProduct.setAccessType(ServiceAccessType.DIRECT);
        mockTechProdAccessInfo("<p><p>URL</p></p>");

        String info = bean.getAccessInfo(subscription, user);
        Assert.assertEquals("\n\nURL\n\n", info); // see bug 9272
        verify(bean, times(0)).getSubscriptionUrl(eq(subscription));
    }

    @Test
    public void getAccessInfo_USER_fromSubscription() throws Exception {
        technicalProduct.setAccessType(ServiceAccessType.USER);
        subscription.setAccessInfo("<p><p>URL</p></p>");

        String info = bean.getAccessInfo(subscription, user);
        Assert.assertEquals("<p><p>URL</p></p>", info); // see bug 9272
        verify(bean, times(0)).getSubscriptionUrl(eq(subscription));
    }

    @Test
    public void getAccessInfo_USER_fromTechService() throws Exception {
        technicalProduct.setAccessType(ServiceAccessType.USER);
        bean.localizer = mock(LocalizerServiceLocal.class);
        doReturn("URL").when(bean.localizer).getLocalizedTextFromDatabase(
                anyString(), Matchers.anyLong(),
                eq(LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));

        String info = bean.getAccessInfo(subscription, user);
        assertEquals("URL", info);
        verify(bean, times(0)).getSubscriptionUrl(eq(subscription));
    }

    private void testGetSubscriptionUrl(ServiceAccessType accessType) {
        technicalProduct.setAccessType(accessType);
        // mock the base URL:
        ConfigurationSetting setting = new ConfigurationSetting();
        setting.setValue("BASE_URL");
        bean.cfgService = mock(ConfigurationServiceLocal.class);
        doReturn(setting).when(bean.cfgService).getConfigurationSetting(
                eq(ConfigurationKey.BASE_URL), anyString());
        doReturn("BASE_URL").when(bean.cfgService).getBaseURL();
        String url = bean.getSubscriptionUrl(subscription);
        assertEquals("BASE_URL/opt/" + Long.toHexString(0) + "/", url);
    }

    private Organization givenSupplier(String supplierEmail, String supportEmail) {
        Organization supplier = new Organization();
        supplier.setEmail(supplierEmail);
        supplier.setSupportEmail(supportEmail);
        supplier.setOrganizationId(SUPPLIER_ID);
        addRole(supplier, OrganizationRoleType.SUPPLIER);
        return supplier;
    }

    private Organization givenCustomer() {
        Organization customer = new Organization();
        customer.setName(CUSTOMER_NAME);
        customer.setOrganizationId(CUSTOMER_ID);
        user.setOrganization(customer);
        return customer;
    }

    private Organization givenCustomerWithoutOrgName() {
        Organization customer = new Organization();
        customer.setName(null);
        customer.setOrganizationId(CUSTOMER_ID);
        user.setOrganization(customer);
        return customer;
    }

    private Organization givenBroker(String brokerEmail, String supportEmail) {
        Organization broker = new Organization();
        broker.setEmail(brokerEmail);
        broker.setSupportEmail(supportEmail);
        broker.setOrganizationId(BROKER_ID);
        addRole(broker, OrganizationRoleType.BROKER);
        return broker;
    }

    private Organization givenReseller(String resellerEmail, String supportEmail) {
        Organization reseller = new Organization();
        reseller.setEmail(resellerEmail);
        reseller.setSupportEmail(supportEmail);
        reseller.setOrganizationId(RESELLER_ID);
        addRole(reseller, OrganizationRoleType.RESELLER);
        return reseller;
    }

    private void addRole(Organization org, OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganizationRole(role);
        org.setGrantedRoles(Collections.singleton(otr));
    }

    @Test
    public void getSubscriptionUrl_PLATFORM() throws Exception {
        testGetSubscriptionUrl(ServiceAccessType.LOGIN);
    }

    @Test
    public void getSubscriptionUrl_LOGIN() throws Exception {
        testGetSubscriptionUrl(ServiceAccessType.LOGIN);
    }

    @Test
    public void getSubscriptionUrl_PROXY() throws Exception {
        testGetSubscriptionUrl(ServiceAccessType.LOGIN);
    }

    @Test
    public void getSubscriptionUrl_DIRECT() throws Exception {
        testGetSubscriptionUrl(ServiceAccessType.DIRECT);
    }

    @Test
    public void getSubscriptionUrl_USER() throws Exception {
        testGetSubscriptionUrl(ServiceAccessType.USER);
    }

    @Test
    public void reportIssue() throws Exception {

        // given supplier with support email
        Organization supplier = givenSupplier("supplier@test.de",
                SUPPLIER_SUPPORT_EMAIL);
        subscriptionCopy.setVendor(supplier);

        Organization customer = givenCustomer();
        subscription.setOrganization(customer);

        doReturn(subscription).when(bean.dataManager)
                .getReferenceByBusinessKey(any(DomainObject.class));

        // when customer reports an issue
        bean.reportIssue(SUBSCRIPTION_ID, MAIL_SUBJECT, MAIL_TEXT);

        // then check mail sent to support contact
        verify(bean.commService).sendMail(stringArg.capture(),
                eq(EmailType.SUPPORT_ISSUE), arrayArg.capture(),
                any(Marketplace.class), anyString());
        assertEquals(SUPPLIER_SUPPORT_EMAIL, stringArg.getValue());

        assertEquals(String.format(Arrays.toString(expectedSupportMailParams),
                SUBSCRIPTION_PRODINST, template.getProductId(),
                TECHNICAL_PRODUCT_VERSION, TECHNICAL_PRODUCT_BUILD),
                Arrays.toString(arrayArg.getValue()));

    }

    @Test
    public void reportIssue_noSupportEmail() throws Exception {
        // given supplier without support contact
        final String supplierEmail = "supplier@test.de";
        Organization supplier = givenSupplier(supplierEmail, "");
        subscriptionCopy.setVendor(supplier);

        Organization customer = givenCustomer();
        subscription.setOrganization(customer);

        doReturn(subscription).when(bean.dataManager)
                .getReferenceByBusinessKey(any(DomainObject.class));

        // when customer reports an issue
        bean.reportIssue(SUBSCRIPTION_ID, MAIL_SUBJECT, MAIL_TEXT);

        // then check mail sent to supplier contact
        verify(bean.commService).sendMail(stringArg.capture(),
                eq(EmailType.SUPPORT_ISSUE), arrayArg.capture(),
                any(Marketplace.class), anyString());

        assertEquals(supplierEmail, stringArg.getValue());
        assertEquals(String.format(Arrays.toString(expectedSupportMailParams),
                SUBSCRIPTION_PRODINST, template.getProductId(),
                TECHNICAL_PRODUCT_VERSION, TECHNICAL_PRODUCT_BUILD),
                Arrays.toString(arrayArg.getValue()));
    }

    @Test
    public void sendMailForAbortAsyncSubscription() throws Exception {
        // given
        Organization org = new Organization();
        subscription.setOrganization(org);
        subscription.setStatus(SubscriptionStatus.PENDING_UPD);
        TechnicalProduct tech = new TechnicalProduct();
        tech.setOrganization(org);
        subscription.getProduct().setTechnicalProduct(tech);
        doNothing().when(manageBean).validateTechnoloyProvider(
                any(Subscription.class));
        doNothing().when(modifyAndUpgradeBean).setStatusForModifyComplete(
                any(Subscription.class));
        doNothing().when(modifyAndUpgradeBean)
                .deleteModifiedEntityForSubscription(any(Subscription.class));
        doReturn(orgDao).when(bean.manageBean).getOrganizationDao();
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());

        // when
        bean.abortAsyncUpgradeOrModifySubscription(subscription,
                ORGANIZATION_ID, reason, true);

        // then
        verify(bean.commService).sendMail(eq(user),
                eq(EmailType.SUBSCRIPTION_PARAMETER_MODIFY_ABORT),
                arrayArg.capture(), eq(subscription.getMarketplace()));
        Object[] value = arrayArg.getValue();
        assertEquals(SUBSCRIPTION_ID, value[0]);
        assertEquals(ORGANIZATION_ID, value[1]);
        assertNull(value[2]);
    }

    @Test
    public void reportIssue_noDetails_Bug9501() throws Exception {
        // given supplier with subscription and no details
        Organization supplier = givenSupplier("supplier@test.de",
                SUPPLIER_SUPPORT_EMAIL);
        subscriptionCopy.setVendor(supplier);

        Organization customer = givenCustomerWithoutOrgName();
        subscription.setOrganization(customer);
        subscription.setProductInstanceId(null);
        technicalProduct.setTechnicalProductBuildId(null);
        technicalProduct.setProvisioningVersion(null);

        doReturn(subscription).when(bean.dataManager)
                .getReferenceByBusinessKey(any(DomainObject.class));

        // when customer reports an issue
        bean.reportIssue(SUBSCRIPTION_ID, MAIL_SUBJECT, MAIL_TEXT);

        // then check mail contains place holders
        verify(bean.commService).sendMail(stringArg.capture(),
                eq(EmailType.SUPPORT_ISSUE), arrayArg.capture(),
                any(Marketplace.class), anyString());

        assertEquals(String.format(
                Arrays.toString(expectedSupportMailParamsWithoutOrgName), "-",
                "", "-"), Arrays.toString(arrayArg.getValue()));
    }

    @Test(expected = ValidationException.class)
    public void reportIssue_noSubject() throws Exception {

        // given supplier subscription
        Organization supplier = givenSupplier("supplier@test.de",
                SUPPLIER_SUPPORT_EMAIL);
        subscriptionCopy.setVendor(supplier);

        Organization customer = givenCustomer();
        subscription.setOrganization(customer);

        doReturn(subscription).when(bean.dataManager)
                .getReferenceByBusinessKey(any(DomainObject.class));

        // when customer reports issue without subject
        bean.reportIssue(subscription.getSubscriptionId(), "", MAIL_TEXT);
    }

    @Test
    public void reportIssue_BrokerSubscription() throws Exception {
        // given
        Organization supplier = givenSupplier("supplier@test.de",
                SUPPLIER_SUPPORT_EMAIL);
        Organization broker = givenBroker("broker@test.de",
                BROKER_SUPPORT_EMAIL);
        Organization customer = givenCustomer();
        template.setVendor(supplier);
        createPartnerSubscription(broker, customer, subscription);

        doReturn(subscription).when(bean.dataManager)
                .getReferenceByBusinessKey(any(DomainObject.class));

        // when customer reports an issue
        bean.reportIssue(SUBSCRIPTION_ID, MAIL_SUBJECT, MAIL_TEXT);

        // then check mail sent to support contact
        verify(bean.commService).sendMail(stringArg.capture(),
                eq(EmailType.SUPPORT_ISSUE), arrayArg.capture(),
                any(Marketplace.class), anyString());
        assertEquals("Supplier email address expected", SUPPLIER_SUPPORT_EMAIL,
                stringArg.getValue());

        assertEquals(String.format(Arrays.toString(expectedSupportMailParams),
                SUBSCRIPTION_PRODINST, subscription.getProduct().getTemplate()
                        .getProductId(), TECHNICAL_PRODUCT_VERSION,
                TECHNICAL_PRODUCT_BUILD), Arrays.toString(arrayArg.getValue()));
    }

    @Test
    public void reportIssue_ResellerSubscription() throws Exception {
        // given
        Organization supplier = givenSupplier("supplier@test.de",
                SUPPLIER_SUPPORT_EMAIL);
        Organization reseller = givenReseller("reseller@test.de",
                RESELLER_SUPPORT_EMAIL);
        Organization customer = givenCustomer();
        template.setVendor(supplier);
        createPartnerSubscription(reseller, customer, subscription);

        doReturn(subscription).when(bean.dataManager)
                .getReferenceByBusinessKey(any(DomainObject.class));

        // when customer reports an issue
        bean.reportIssue(SUBSCRIPTION_ID, MAIL_SUBJECT, MAIL_TEXT);

        // then check mail sent to support contact
        verify(bean.commService).sendMail(stringArg.capture(),
                eq(EmailType.SUPPORT_ISSUE), arrayArg.capture(),
                any(Marketplace.class), anyString());
        assertEquals("Reseller email address expected", RESELLER_SUPPORT_EMAIL,
                stringArg.getValue());

        assertEquals(String.format(Arrays.toString(expectedSupportMailParams),
                SUBSCRIPTION_PRODINST, subscription.getProduct().getTemplate()
                        .getProductId(), TECHNICAL_PRODUCT_VERSION,
                TECHNICAL_PRODUCT_BUILD), Arrays.toString(arrayArg.getValue()));
    }

    private void createPartnerSubscription(Organization partner,
            Organization customer, Subscription subscription) {
        Product partnerCopy = template.copyForResale(partner);
        Product partnerSubscriptionCopy = partnerCopy.copyForSubscription(
                customer, subscription);
        partnerSubscriptionCopy.setType(ServiceType.PARTNER_SUBSCRIPTION);
        subscription.setOrganization(customer);
        subscription.bindToProduct(partnerSubscriptionCopy);
    }

    private Set<RoleAssignment> newRoleAssignment(PlatformUser user,
            UserRoleType roleType) {
        Set<RoleAssignment> roles = new HashSet<RoleAssignment>();
        RoleAssignment ra = new RoleAssignment();
        ra.setKey(1L);
        ra.setUser(user);
        ra.setRole(new UserRole(roleType));
        roles.add(ra);
        return roles;
    }
}
