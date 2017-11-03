/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 13.01.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductToPaymentType;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.ImageResourceServiceBean;
import org.oscm.identityservice.bean.IdentityServiceBean;
import org.oscm.identityservice.bean.LdapAccessStub;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.reviewservice.bean.ReviewServiceLocalBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ApplicationServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.PaymentServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.test.stubs.TaskQueueServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.types.enumtypes.EmailType;

/**
 * @author pock
 * 
 */
public class AccountServiceBeanRegisterCustomerIT extends EJBTestBase {

    private DataService mgr;
    private AccountService accountMgmt;
    private String supplierId;
    private String brokerId;
    private String resellerId;
    private Object[] mailParams;
    private String marketplaceId = "FUJITSU";
    private static final String PAYMENT_INFO_NAME = "Rechnung";

    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();

        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                if (objectType == LocalizedObjectTypes.PAYMENT_TYPE_NAME) {
                    return PAYMENT_INFO_NAME;
                }
                return "";
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
        });
        container.addBean(mock(CommunicationServiceLocal.class));
        container.addBean(mock(ReviewServiceLocalBean.class));
        container.addBean(new ImageResourceServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new ApplicationServiceStub());
        container.addBean(new SessionServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new CommunicationServiceStub() {
            @Override
            public void sendMail(PlatformUser recipient, EmailType type,
                    Object[] params, Marketplace marketplace) {
                mailParams = params;
            }

            @Override
            public String getMarketplaceUrl(String marketplaceId)
                    throws MailOperationException {
                return "";
            }
        });
        container.addBean(new LdapAccessStub());
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new TaskQueueServiceStub());
        container.addBean(new IdentityServiceBean());
        container.addBean(new PaymentServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(new AccountServiceBean());

        setUpDirServerStub(container.get(ConfigurationServiceLocal.class));

        mgr = container.get(DataService.class);
        accountMgmt = container.get(AccountService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOrganizationRoles(mgr);
                createPaymentTypes(mgr);
                createUserRoles(mgr);
                SupportedCountries.setupAllCountries(mgr);
                return null;
            }
        });

        PlatformUser tmp = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization organization = Organizations
                        .createOrganization(mgr, OrganizationRoleType.SUPPLIER);
                supplierId = organization.getOrganizationId();
                Organization brokerOrg = Organizations.createOrganization(mgr,
                        OrganizationRoleType.BROKER);
                brokerId = brokerOrg.getOrganizationId();
                Organization resellerOrg = Organizations.createOrganization(mgr,
                        OrganizationRoleType.RESELLER);
                resellerId = resellerOrg.getOrganizationId();
                Marketplaces.createMarketplace(organization, marketplaceId,
                        false, mgr);
                Organizations.createUserForOrg(mgr, organization, true,
                        "admin");
                Organizations.supportAllCountries(mgr, organization);

                return Organizations.createUserForOrg(mgr, organization, true,
                        "TempUser");
            }
        });

        container.login(String.valueOf(tmp.getKey()), ROLE_ORGANIZATION_ADMIN);
    }

    @Test
    public void testRegisterCustomerForNonSupplierOrganization()
            throws Exception {
        Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                return org;
            }
        });

        VOUserDetails admin = new VOUserDetails();
        admin.setLocale("de");
        admin.setEMail("testuser@test.de");
        admin.setUserId("admin");
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setLocale("de");
        voOrganization.setDomicileCountry("DE");

        try {
            accountMgmt.registerCustomer(voOrganization, admin, "secret", null,
                    null, org.getOrganizationId());
            Assert.fail(
                    "Call must not pass, as the target organization is not a supplier");
        } catch (RegistrationException e) {
            Assert.assertEquals("Wrong reason in exception for operation",
                    RegistrationException.Reason.TARGET_ORG_INVALID,
                    e.getReason());
        }
    }

    /**
     * 
     * No exception should be thrown by registering customer without supplier
     * 
     * @throws Exception
     */
    @Test
    public void testRegisterCustomerForNullSupplierReference()
            throws Exception {
        VOUserDetails admin = new VOUserDetails();
        admin.setLocale("de");
        admin.setEMail("testuser@test.de");
        admin.setUserId("admin");
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setLocale("de");
        voOrganization.setDomicileCountry("DE");

        final VOOrganization createdCustomer = accountMgmt.registerCustomer(
                voOrganization, admin, "secret", null, marketplaceId, null);
        checkRegisteredCustomer(createdCustomer, false, null);
    }

    private void checkRegisteredCustomer(final VOOrganization createdCustomer,
            final boolean refExists, final OrganizationReferenceType refType)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        createdCustomer.getKey());
                Assert.assertNotNull(org);
                assertTrue(org.getTargets().isEmpty());
                if (!refExists) {
                    assertTrue(org.getSources().isEmpty());
                } else {
                    List<OrganizationReference> sources = org.getSources();
                    assertEquals(1, sources.size());
                    OrganizationReference ref = sources.get(0);
                    assertEquals(refType, ref.getReferenceType());
                }
                List<PaymentInfo> infos = org.getPaymentInfos();
                Assert.assertNotNull(infos);
                Assert.assertEquals(1, infos.size());
                Assert.assertEquals(PaymentType.INVOICE,
                        infos.get(0).getPaymentType().getPaymentTypeId());
                Assert.assertEquals(PAYMENT_INFO_NAME,
                        infos.get(0).getPaymentInfoId());
                return null;
            }
        });
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testRegisterCustomerForNonExistingSupplierReference()
            throws Exception {
        VOUserDetails admin = new VOUserDetails();
        admin.setLocale("de");
        admin.setEMail("testuser@test.de");
        admin.setUserId("admin");
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setLocale("de");
        voOrganization.setDomicileCountry("DE");

        accountMgmt.registerCustomer(voOrganization, admin, "secret", null,
                marketplaceId, "someId");
    }

    private Long[] prepareProducts(final ServiceStatus status,
            Organization supplier) throws Exception {
        final Organization supplier1 = supplier;

        Long[] productKeys = runTX(new Callable<Long[]>() {
            @Override
            public Long[] call() throws Exception {

                Organization provider = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);

                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider, "testTechProd", false,
                        ServiceAccessType.LOGIN);
                Product product1 = Products.createProduct(supplier1, tp, true,
                        "testProd1", null, mgr);

                PaymentType pt1 = findPaymentType(CREDIT_CARD, mgr);
                ProductToPaymentType prodToPt1 = new ProductToPaymentType(
                        product1, pt1);
                mgr.persist(prodToPt1);

                Product product2 = Products.createProduct(supplier1, tp, false,
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

    @Test
    public void testRegisterCustomerWithEmptyDefaultPayments()
            throws Exception {
        final Organization supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        supplierId);
                org.getGrantedRoleTypes();
                return org;
            }
        });

        addPaymentTypesToOrganizationRef(supplierId,
                OrganizationRoleType.SUPPLIER);

        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerCustomer(org, user,
                null, null, marketplaceId, supplierId);

        checkRegisteredCustomer(customer, true,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);

        String customerUserKey = getUserKeyForOrg(customer.getOrganizationId());

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE, supplier);

        container.login(customerUserKey, "OrganizationAdmin");
        Set<VOPaymentType> expected = Collections.emptySet();
        Set<VOPaymentType> actual = accountMgmt
                .getAvailablePaymentTypesFromOrganization(productKeys[0]);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRegisterCustomerWithDefaultPayments() throws Exception {
        final Organization supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        supplierId);
                org.getGrantedRoleTypes();
                return org;
            }
        });

        addPaymentTypesToOrganizationRef(supplierId,
                OrganizationRoleType.SUPPLIER);
        Set<VOPaymentType> orgPt = new HashSet<>();
        VOPaymentType pt;
        pt = new VOPaymentType();
        pt.setPaymentTypeId(INVOICE);
        orgPt.add(pt);
        pt = new VOPaymentType();
        pt.setPaymentTypeId(CREDIT_CARD);
        orgPt.add(pt);

        Set<VOPaymentType> expected = new HashSet<>();
        pt = new VOPaymentType();
        pt.setPaymentTypeId(CREDIT_CARD);
        expected.add(pt);

        List<VOOrganizationPaymentConfiguration> empty = new ArrayList<>();

        container.login(getUserKeyForOrg(supplierId), ROLE_SERVICE_MANAGER);
        accountMgmt.savePaymentConfiguration(orgPt, empty, orgPt, null);
        VOOrganization org = new VOOrganization();
        org.setLocale(Locale.ENGLISH.toString());
        org.setDomicileCountry(Locale.GERMANY.getCountry());
        VOUserDetails user = new VOUserDetails();
        user.setEMail(TEST_MAIL_ADDRESS);
        user.setLocale(org.getLocale());
        user.setUserId("initialUser");
        final VOOrganization customer = accountMgmt.registerCustomer(org, user,
                null, null, marketplaceId, supplierId);

        checkRegisteredCustomer(customer, true,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);

        Long[] productKeys = prepareProducts(ServiceStatus.ACTIVE, supplier);

        String customerUserKey = getUserKeyForOrg(customer.getOrganizationId());
        container.login(customerUserKey, "OrganizationAdmin");
        Set<VOPaymentType> actual = accountMgmt
                .getAvailablePaymentTypesFromOrganization(productKeys[0]);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRegisterCustomerWithServiceId() throws Exception {
        VOOrganization organization = new VOOrganization();
        organization.setLocale("de");
        organization.setDomicileCountry("DE");

        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setEMail(TEST_MAIL_ADDRESS);
        userDetails.setUserId("admin");
        userDetails.setLocale("de");

        final VOOrganization createdCustomer = accountMgmt.registerCustomer(
                organization, userDetails, "admin", Long.valueOf(12345),
                marketplaceId, supplierId);
        checkRegisteredCustomer(createdCustomer, true,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);

        Assert.assertEquals(1, mailParams.length);
    }

    @Test
    public void testRegisterCustomerWithBrokerId() throws Exception {
        VOOrganization organization = new VOOrganization();
        organization.setLocale("de");
        organization.setDomicileCountry("DE");

        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setEMail(TEST_MAIL_ADDRESS);
        userDetails.setUserId("admin");
        userDetails.setLocale("de");

        final VOOrganization createdCustomer = accountMgmt.registerCustomer(
                organization, userDetails, "admin", Long.valueOf(12345),
                marketplaceId, brokerId);
        checkRegisteredCustomer(createdCustomer, true,
                OrganizationReferenceType.BROKER_TO_CUSTOMER);

        Assert.assertEquals(1, mailParams.length);
    }

    @Test
    public void testRegisterCustomerWithResellerId() throws Exception {
        VOOrganization organization = new VOOrganization();
        organization.setLocale("de");
        organization.setDomicileCountry("DE");

        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setEMail(TEST_MAIL_ADDRESS);
        userDetails.setUserId("admin");
        userDetails.setLocale("de");

        final VOOrganization createdCustomer = accountMgmt.registerCustomer(
                organization, userDetails, "admin", Long.valueOf(12345),
                marketplaceId, resellerId);
        checkRegisteredCustomer(createdCustomer, true,
                OrganizationReferenceType.RESELLER_TO_CUSTOMER);

        Assert.assertEquals(1, mailParams.length);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerInvalidOrgMail() throws Exception {
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setLocale("en");
        voOrganization.setEmail("some_invalid_email");
        accountMgmt.registerCustomer(voOrganization, new VOUserDetails(),
                "password", null, marketplaceId, null);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerInvalidOrgAdress() throws Exception {
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setLocale("en");
        voOrganization.setAddress(TOO_LONG_DESCRIPTION);
        accountMgmt.registerCustomer(voOrganization, new VOUserDetails(),
                "password", null, marketplaceId, null);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerInvalidOrgName() throws Exception {
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setLocale("en");
        voOrganization.setName(TOO_LONG_NAME);
        accountMgmt.registerCustomer(voOrganization, new VOUserDetails(),
                "password", null, marketplaceId, null);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerInvalidOrgPhone() throws Exception {
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setLocale("en");
        voOrganization.setPhone(TOO_LONG_NAME);
        accountMgmt.registerCustomer(voOrganization, new VOUserDetails(),
                "password", null, marketplaceId, null);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterCustomerInvalidOrgLocale() throws Exception {
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setLocale("some_invalid_locale");
        accountMgmt.registerCustomer(voOrganization, new VOUserDetails(),
                "password", null, marketplaceId, null);
    }

    private String getUserKeyForOrg(final String organizationId)
            throws Exception {
        return runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Organization organization = Organizations.findOrganization(mgr,
                        organizationId);
                return String.valueOf(
                        organization.getPlatformUsers().get(0).getKey());
            }
        });
    }

    private void addPaymentTypesToOrganizationRef(final String orgId,
            final OrganizationRoleType roleType) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organizations.addPaymentTypesToOrganizationRef(mgr, orgId,
                        roleType);
                return null;
            }
        });
    }
}
