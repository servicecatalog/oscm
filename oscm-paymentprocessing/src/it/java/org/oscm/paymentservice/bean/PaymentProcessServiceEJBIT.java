/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 17.09.2010                                                                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.bean;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.internal.intf.PaymentService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.vo.VOPaymentData;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.ApplicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class PaymentProcessServiceEJBIT extends EJBTestBase {

    protected DataService mgr;

    private PaymentService paymentMgmt;

    private String supplierId;
    private String providerId;
    private Organization supplier;
    private PlatformUser supplierUser;

    protected boolean instanceActivated;

    @Override
    public void setup(final TestContainer container) throws Exception {
        instanceActivated = false;
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new AccountServiceStub());
        container.addBean(Mockito.mock(BillingDataRetrievalServiceLocal.class));
        container.addBean(new PortLocatorBean());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new ApplicationServiceStub() {

            @Override
            public void activateInstance(Subscription subscription)
                    throws TechnicalServiceNotAliveException,
                    TechnicalServiceOperationException {
                instanceActivated = true;
            }

        });
        container.addBean(new PaymentServiceBean());

        mgr = container.get(DataService.class);
        paymentMgmt = container.get(PaymentService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOrganizationRoles(mgr);
                createPaymentTypes(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                return null;
            }
        });
        Organization organization = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.TECHNOLOGY_PROVIDER);
                Organizations.createUserForOrg(mgr, organization, true,
                        "ProvAdmin");
                return organization;
            }
        });
        providerId = organization.getOrganizationId();

        supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations
                        .createOrganization(mgr, OrganizationRoleType.SUPPLIER);
                supplierUser = Organizations.createUserForOrg(mgr, organization,
                        true, "SuppAdmin");
                return organization;
            }
        });
        supplierId = supplier.getOrganizationId();

    }

    private PaymentInfo getPaymentInfoForOrg(
            final OrganizationRefToPaymentType orgPayType) throws Exception {

        return runTX(new Callable<PaymentInfo>() {
            @Override
            public PaymentInfo call() throws Exception {
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo
                        .setOrganization(orgPayType.getAffectedOrganization());
                paymentInfo.setPaymentType(orgPayType.getPaymentType());
                paymentInfo.setPaymentInfoId("name");
                mgr.persist(paymentInfo);
                return paymentInfo;
            }
        });

    }

    @Test
    public void testSetPaymentIdentificationCreation() throws Exception {
        final String localOrgId = "orgForPSPIdTest2";
        Organization org = initPlainOrgWithId(localOrgId);
        OrganizationRefToPaymentType orgPayType = addPaymentTypeToOrganization(
                org, CREDIT_CARD);
        PaymentInfo pi = getPaymentInfoForOrg(orgPayType);

        String id = "initialValueToSet";
        String provider = "Platin Card";
        String account = "0123456";

        // Create a new PI (Registration)
        VOPaymentData pd = getData(0, id, provider, account, org.getKey(),
                pi.getPaymentType().getKey());
        paymentMgmt.savePaymentIdentificationForOrganization(pd);

        PaymentInfo savedPi = findPaymentInfo(0);
        Assert.assertEquals("Wrong external identifier for payment info stored",
                id, savedPi.getExternalIdentifier());
        Assert.assertEquals("Wrong payment info type stored", CREDIT_CARD,
                savedPi.getPaymentType().getPaymentTypeId());
        Assert.assertEquals(provider, savedPi.getProviderName());
        Assert.assertEquals(account, savedPi.getAccountNumber());

        // Now update an existing PI (Reregistration)
        pd = getData(pi.getKey(), id, provider, account, org.getKey(),
                pi.getPaymentType().getKey());
        pd.setPaymentInfoId(pd.getPaymentInfoId() + "2");
        paymentMgmt.savePaymentIdentificationForOrganization(pd);

        savedPi = findPaymentInfo(pi.getKey());
        Assert.assertEquals("Wrong external identifier for payment info stored",
                id, savedPi.getExternalIdentifier());
        Assert.assertEquals("Wrong payment info type stored", CREDIT_CARD,
                savedPi.getPaymentType().getPaymentTypeId());
        Assert.assertEquals(provider, savedPi.getProviderName());
        Assert.assertEquals(account, savedPi.getAccountNumber());
    }

    @Test
    public void testSetPaymentIdentificationUpdate() throws Exception {
        Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                return org;
            }
        });

        OrganizationRefToPaymentType orgPayType = addPaymentTypeToOrganization(
                org, DIRECT_DEBIT);
        PaymentInfo pi = getPaymentInfoForOrg(orgPayType);

        VOPaymentData pd = getData(pi.getKey(), "initialValueToSet", null, null,
                org.getKey(), pi.getPaymentType().getKey());
        paymentMgmt.savePaymentIdentificationForOrganization(pd);

        PaymentInfo savedPi = findPaymentInfo(pi.getKey());

        Assert.assertEquals("Wrong external identifier for payment info stored",
                "initialValueToSet", savedPi.getExternalIdentifier());
        Assert.assertEquals("Wrong payment info type stored", DIRECT_DEBIT,
                savedPi.getPaymentType().getPaymentTypeId());

        pd = getData(savedPi.getKey(), "initialValueToSetUpdated", null, null,
                org.getKey(), pi.getPaymentType().getKey());
        paymentMgmt.savePaymentIdentificationForOrganization(pd);

        PaymentInfo updatedPi = findPaymentInfo(pi.getKey());

        Assert.assertEquals("Wrong payment info type updated", DIRECT_DEBIT,
                updatedPi.getPaymentType().getPaymentTypeId());

        Assert.assertEquals("Wrong object has been updated", updatedPi.getKey(),
                savedPi.getKey());

    }

    @Test(expected = PaymentDataException.class)
    public void testSetPaymentIdentificationUpdateForbiddenPaymentType()
            throws Exception {
        Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                return org;
            }
        });

        OrganizationRefToPaymentType orgPayType = addPaymentTypeToOrganization(
                org, INVOICE);
        PaymentInfo pi = getPaymentInfoForOrg(orgPayType);

        VOPaymentData pd = getData(pi.getKey(), "externalId", null, null,
                org.getKey(), pi.getPaymentType().getKey());
        paymentMgmt.savePaymentIdentificationForOrganization(pd);

    }

    @Test
    public void testSetPaymentIdentificationForOrganizationValidateActivationOfSubs()
            throws Exception {
        // create org and sub with status suspended
        final Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);

                return org;
            }
        });

        OrganizationRefToPaymentType orgPayType = addPaymentTypeToOrganization(
                org, CREDIT_CARD);
        final PaymentInfo pi = getPaymentInfoForOrg(orgPayType);

        container.login(String.valueOf(supplierUser.getKey()));
        enablePaymentTypes(org.getOrganizationId(),
                OrganizationRoleType.CUSTOMER);
        Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                prepareProducts(null);
                Organization supplier = new Organization();
                supplier.setOrganizationId(supplierId);
                supplier = (Organization) mgr
                        .getReferenceByBusinessKey(supplier);
                Subscription sub = Subscriptions.createSubscription(mgr,
                        org.getOrganizationId(), "testProd1", "subId",
                        supplier);
                sub.setStatus(SubscriptionStatus.SUSPENDED);
                sub.setPaymentInfo(pi);
                return sub;
            }
        });
        final long subKey = sub.getKey();

        VOPaymentData pd = getData(pi.getKey(), "someIdFromPSP", null, null,
                org.getKey(), pi.getPaymentType().getKey());
        paymentMgmt.savePaymentIdentificationForOrganization(pd);

        // validate subscription status is active
        sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return mgr.getReference(Subscription.class, subKey);
            }
        });
        Assert.assertEquals("subscription was not activated",
                SubscriptionStatus.ACTIVE, sub.getStatus());
        Assert.assertTrue(instanceActivated);
    }

    private PaymentInfo findPaymentInfo(final long key) throws Exception {
        return runTX(new Callable<PaymentInfo>() {
            @Override
            public PaymentInfo call() throws Exception {
                PaymentInfo payment;
                if (key > 0) {
                    payment = mgr.getReference(PaymentInfo.class, key);
                } else {
                    final List<?> list = mgr
                            .createQuery(
                                    "select pi from PaymentInfo pi order by pi.key")
                            .getResultList();
                    payment = (PaymentInfo) list.get(list.size() - 1);
                }
                load(payment.getPaymentType());
                return payment;
            }
        });
    }

    private Long[] prepareProducts(final ServiceStatus status)
            throws Exception {
        Long[] productKeys = runTX(new Callable<Long[]>() {

            @Override
            public Long[] call() throws Exception {
                Organization provider = Organizations.findOrganization(mgr,
                        providerId);
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider, "testTechProd", false,
                        ServiceAccessType.LOGIN);
                Organization supplier = Organizations.findOrganization(mgr,
                        supplierId);
                Product product1 = Products.createProduct(supplier, tp, true,
                        "testProd1", null, mgr);
                Product product2 = Products.createProduct(supplier, tp, false,
                        "testProd2", null, mgr);
                if (status != null) {
                    product2.setStatus(status);
                }
                return new Long[] { Long.valueOf(product1.getKey()),
                        Long.valueOf(product2.getKey()) };
            }
        });
        return productKeys;
    }

    /**
     * Creates an organization with only the organization identifier set. The
     * organization will be a supplier.
     * 
     * @param localOrgId
     *            The identifier of the organization.
     * @return The created organization.
     * @throws Exception
     */
    private Organization initPlainOrgWithId(final String localOrgId)
            throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = new Organization();
                org.setOrganizationId(localOrgId);
                org.setCutOffDay(1);
                mgr.persist(org);
                Organizations.addOrganizationToRole(mgr, org,
                        OrganizationRoleType.SUPPLIER);
                return org;
            }
        });
    }

    private void enablePaymentTypes(final String orgId,
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

    private OrganizationRefToPaymentType addPaymentTypeToOrganization(
            final Organization org, final String paymentType) throws Exception {

        return runTX(new Callable<OrganizationRefToPaymentType>() {
            @Override
            public OrganizationRefToPaymentType call() throws Exception {

                Organization storedOrg = (Organization) mgr
                        .getReferenceByBusinessKey(org);

                Organization supplier = new Organization();
                supplier.setOrganizationId(supplierId);
                OrganizationReference ref = new OrganizationReference(
                        (Organization) mgr.getReferenceByBusinessKey(supplier),
                        storedOrg,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);

                mgr.persist(ref);

                OrganizationRefToPaymentType otpt = new OrganizationRefToPaymentType();
                otpt.setOrganizationReference(ref);
                OrganizationRole orgRole = new OrganizationRole();
                orgRole.setRoleName(OrganizationRoleType.CUSTOMER);
                otpt.setOrganizationRole((OrganizationRole) mgr
                        .getReferenceByBusinessKey(orgRole));
                otpt.setPaymentType(findPaymentType(paymentType, mgr));
                mgr.persist(otpt);
                return otpt;
            }
        });

    }

    private static VOPaymentData getData(long key, String id, String provider,
            String number, long organizationKey, long paymentTypeKey) {
        VOPaymentData pd = new VOPaymentData();
        pd.setAccountNumber(number);
        pd.setIdentification(id);
        pd.setPaymentInfoKey(key);
        pd.setPaymentInfoId("12345");
        pd.setProvider(provider);
        pd.setOrganizationKey(organizationKey);
        pd.setPaymentTypeKey(paymentTypeKey);
        return pd;
    }
}
