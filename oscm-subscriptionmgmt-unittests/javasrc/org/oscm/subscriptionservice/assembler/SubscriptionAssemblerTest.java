/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: weiser                                                 
 *                                                                              
 *  Creation Date: 18.08.2010                                                      
 *                                                                              
 *  Completion Time: 18.08.2010                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.assembler;

import static org.junit.Assert.assertNull;
import static org.oscm.test.Numbers.L_TIMESTAMP;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTechnicalServiceOperation;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserSubscription;
import org.oscm.test.stubs.LocalizerServiceStub;

/**
 * @author weiser
 * 
 */
@SuppressWarnings("boxing")
public class SubscriptionAssemblerTest {

    private final LocalizerFacade facade = new LocalizerFacade(
            new LocalizerServiceStub() {

                @Override
                public String getLocalizedTextFromDatabase(String localeString,
                        long objectKey, LocalizedObjectTypes objectType) {
                    return objectType.name();
                }
            }, "en");

    private Subscription subscription;
    private Product product;
    private Organization supplier;
    private TechnicalProduct technicalProduct;
    private TechnicalProductOperation operation;
    private PlatformUser platformUser;
    private Organization organization;
    private UsageLicense license;

    @Before
    public void setup() throws Exception {
        technicalProduct = new TechnicalProduct();
        technicalProduct.setTechnicalProductId("TP_ID");
        technicalProduct.setAccessType(ServiceAccessType.LOGIN);
        technicalProduct.setBaseURL("baseURL");
        technicalProduct.setLoginPath("loginPath");

        operation = new TechnicalProductOperation();
        operation.setTechnicalProduct(technicalProduct);
        operation.setOperationId("OP_ID");

        technicalProduct.getTechnicalProductOperations().add(operation);

        supplier = new Organization();
        supplier.setOrganizationId("SUPPLIER_ID");
        supplier.setName("SupplierName");

        product = new Product();
        product.setTechnicalProduct(technicalProduct);
        product.setProductId("PROD_ID");
        product.setVendor(supplier);
        product.setAutoAssignUserEnabled(false);

        organization = new Organization();
        organization.setOrganizationId("org123");

        UserGroup unit = new UserGroup();
        unit.setName("UNIT_NAME");
        unit.setKey(10L);
        subscription = new Subscription();
        subscription.bindToProduct(product);
        subscription.setSubscriptionId("SUB_ID");
        subscription.setCreationDate(L_TIMESTAMP);
        subscription.setActivationDate(L_TIMESTAMP);
        subscription.setDeactivationDate(L_TIMESTAMP);
        subscription.setTimeoutMailSent(false);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setAccessInfo("accessInfo");
        subscription.setBaseURL("baseURL");
        subscription.setLoginPath("loginPath");
        subscription.setProductInstanceId("productInstanceId");
        subscription.setPurchaseOrderNumber("purchaseOrderNumber");
        PlatformUser owner = new PlatformUser();
        owner.setUserId("OWNER_USER_ID");
        subscription.setOwner(owner);
        subscription.setUserGroup(unit);
        subscription.setOrganization(organization);

        organization = new Organization();
        organization.setOrganizationId("ORG_ID");

        platformUser = new PlatformUser();
        platformUser.setOrganization(organization);
        platformUser.setUserId("USER_ID");
        platformUser.setStatus(UserAccountStatus.ACTIVE);

        license = subscription.addUser(platformUser, null);
        license.setApplicationUserId("applicationUserId");
    }

    @Test
    public void testToVOSubscription() {
        VOSubscription sub = SubscriptionAssembler
                .toVOSubscription(subscription, facade);
        validateVOSubscription(sub);

    }

    @Test
    public void testToVOSubscription_MultipleUsers() throws Exception {
        PlatformUser pu = new PlatformUser();
        pu.setOrganization(organization);
        pu.setUserId("USER_ID1");
        pu.setStatus(UserAccountStatus.ACTIVE);
        subscription.addUser(pu, null);

        pu = new PlatformUser();
        pu.setOrganization(organization);
        pu.setUserId("USER_ID2");
        pu.setStatus(UserAccountStatus.ACTIVE);
        subscription.addUser(pu, null);

        VOSubscription sub = SubscriptionAssembler
                .toVOSubscription(subscription, facade);
        validateVOSubscription(sub);
    }

    @Test
    public void testToVOSubscription_TPValuesUsed() {
        subscription.setAccessInfo(null);
        subscription.setBaseURL(null);
        subscription.setLoginPath(null);
        VOSubscription sub = SubscriptionAssembler
                .toVOSubscription(subscription, facade);
        Assert.assertEquals(technicalProduct.getBaseURL(),
                sub.getServiceBaseURL());
        Assert.assertEquals(technicalProduct.getLoginPath(),
                sub.getServiceLoginPath());
        Assert.assertEquals(
                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC.name(),
                sub.getServiceAccessInfo());
    }

    @Test
    public void testToVOSubscription_ProductWithTemplate() {
        Organization supplier2 = new Organization();
        supplier2.setOrganizationId("SUPPLIER_ID2");
        supplier2.setName("SupplierName2");

        Product template = new Product();
        template.setProductId("templateId");
        template.setVendor(supplier2);
        template.setType(ServiceType.TEMPLATE);

        product.setTemplate(template);
        product.setType(ServiceType.SUBSCRIPTION);
        VOSubscription sub = SubscriptionAssembler
                .toVOSubscription(subscription, facade);
        Assert.assertEquals(template.getProductId(), sub.getServiceId());
        Assert.assertEquals(supplier2.getName(), sub.getSellerName());
    }

    @Test
    public void testToVOSubscription_NullUserGroup() {
        // given
        subscription.setUserGroup(null);

        // when
        VOSubscription sub = SubscriptionAssembler
                .toVOSubscription(subscription, facade);

        // then
        Assert.assertEquals(0, sub.getUnitKey());
        Assert.assertNull(sub.getUnitName());
    }

    @Test
    public void testToVOUserSubscription() {
        VOUserSubscription sub = SubscriptionAssembler
                .toVOUserSubscription(subscription, platformUser, facade);
        validateVOSubscription(sub);
        VOUsageLicense lic = sub.getLicense();
        validateVOUsageLicense(lic);
    }

    @Test
    public void testToVOUsageLicense() {
        VOUsageLicense lic = SubscriptionAssembler.toVOUsageLicense(license,
                facade);
        validateVOUsageLicense(lic);
    }

    @Test
    public void toVOSubscriptionDetails() {
        BillingContact bc = new BillingContact();
        bc.setBillingContactId("billingContactId");

        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PaymentType.CREDIT_CARD);
        pt.setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);

        PaymentInfo pi = new PaymentInfo();
        pi.setPaymentType(pt);
        pi.setPaymentInfoId("paymentInfoId");

        subscription.setBillingContact(bc);
        subscription.setPaymentInfo(pi);

        VOSubscriptionDetails details = SubscriptionAssembler
                .toVOSubscriptionDetails(subscription, facade);

        validateVOSubscription(details);
        VOBillingContact billingContact = details.getBillingContact();
        Assert.assertNotNull(billingContact);
        Assert.assertEquals(bc.getBillingContactId(), billingContact.getId());

        VOPaymentInfo paymentInfo = details.getPaymentInfo();
        Assert.assertNotNull(paymentInfo);
        Assert.assertEquals(pi.getPaymentInfoId(), paymentInfo.getId());

        VOPaymentType paymentType = paymentInfo.getPaymentType();
        Assert.assertNotNull(paymentType);
        Assert.assertEquals(pt.getPaymentTypeId(),
                paymentType.getPaymentTypeId());
        Assert.assertEquals(pt.getCollectionType(),
                paymentType.getCollectionType());
    }

    @Test
    public void testToVOUserSubscription_NullSubscription() {
        VOUserSubscription sub = SubscriptionAssembler.toVOUserSubscription(
                null, platformUser, facade, PerformanceHint.ALL_FIELDS);
        assertNull(sub);
    }

    @Test
    public void testToVOSubscription_NullSubscription() {
        VOSubscription sub = SubscriptionAssembler.toVOSubscription(null,
                facade, PerformanceHint.ALL_FIELDS);
        assertNull(sub);
    }

    @Test
    public void testToVOUserSubscription_WithPerformanceHints() {
        VOUserSubscription sub = SubscriptionAssembler.toVOUserSubscription(
                subscription, platformUser, facade,
                PerformanceHint.ONLY_IDENTIFYING_FIELDS);
        validateVOSubscription(sub, PerformanceHint.ONLY_IDENTIFYING_FIELDS);

        sub = SubscriptionAssembler.toVOUserSubscription(subscription,
                platformUser, facade, PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
        validateVOSubscription(sub, PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
    }

    @Test
    public void testToVOSubscription_WithPerformanceHints() {
        VOSubscription sub = SubscriptionAssembler.toVOSubscription(
                subscription, facade, PerformanceHint.ONLY_IDENTIFYING_FIELDS);
        validateVOSubscription(sub, PerformanceHint.ONLY_IDENTIFYING_FIELDS);

        sub = SubscriptionAssembler.toVOSubscription(subscription, facade,
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
        validateVOSubscription(sub, PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
    }

    private void validateVOUsageLicense(VOUsageLicense lic) {
        Assert.assertNotNull(lic);
        Assert.assertEquals(license.getApplicationUserId(),
                lic.getApplicationUserId());
        Assert.assertNull(lic.getRoleDefinition());

        VOUser user = lic.getUser();
        Assert.assertNotNull(user);
        Assert.assertEquals(organization.getOrganizationId(),
                user.getOrganizationId());
        Assert.assertEquals(platformUser.getUserId(), user.getUserId());
        Assert.assertEquals(platformUser.getStatus(), user.getStatus());
    }

    private void validateVOSubscription(VOSubscription sub) {
        validateVOSubscription(sub, PerformanceHint.ALL_FIELDS);
    }

    private void validateVOSubscription(VOSubscription sub,
            PerformanceHint perfHint) {

        // in all cases
        Assert.assertEquals(subscription.getSubscriptionId(),
                sub.getSubscriptionId());

        switch (perfHint) {
        case ONLY_IDENTIFYING_FIELDS:
            break;
        case ONLY_FIELDS_FOR_LISTINGS:
            Assert.assertEquals(subscription.getCreationDate(),
                    sub.getCreationDate());
            Assert.assertEquals(subscription.getActivationDate(),
                    sub.getActivationDate());
            Assert.assertEquals(subscription.getDeactivationDate(),
                    sub.getDeactivationDate());
            Assert.assertEquals(subscription.isTimeoutMailSent(),
                    sub.isTimeoutMailSent());
            Assert.assertEquals(subscription.getStatus(), sub.getStatus());
            Assert.assertEquals(subscription.getUserGroup().getName(),
                    sub.getUnitName());
            Assert.assertEquals(subscription.getUserGroup().getKey(),
                    sub.getUnitKey());
            break;
        case ALL_FIELDS:
            Assert.assertEquals(subscription.getCreationDate(),
                    sub.getCreationDate());
            Assert.assertEquals(subscription.getActivationDate(),
                    sub.getActivationDate());
            Assert.assertEquals(subscription.getDeactivationDate(),
                    sub.getDeactivationDate());
            Assert.assertEquals(subscription.isTimeoutMailSent(),
                    sub.isTimeoutMailSent());
            Assert.assertEquals(subscription.getStatus(), sub.getStatus());
            Assert.assertEquals(technicalProduct.getAccessType(),
                    sub.getServiceAccessType());
            Assert.assertEquals(product.getProductId(), sub.getServiceId());
            Assert.assertEquals(supplier.getName(), sub.getSellerName());
            Assert.assertEquals(subscription.getAccessInfo(),
                    sub.getServiceAccessInfo());
            Assert.assertEquals(subscription.getBaseURL(),
                    sub.getServiceBaseURL());
            Assert.assertEquals(subscription.getLoginPath(),
                    sub.getServiceLoginPath());
            Assert.assertEquals(subscription.getProductInstanceId(),
                    sub.getServiceInstanceId());
            Assert.assertEquals(subscription.getPurchaseOrderNumber(),
                    sub.getPurchaseOrderNumber());
            Assert.assertEquals(subscription.getUsageLicenses().size(),
                    sub.getNumberOfAssignedUsers());
            Assert.assertEquals("OWNER_USER_ID", sub.getOwnerId());
            Assert.assertEquals(subscription.getUserGroup().getName(),
                    sub.getUnitName());
            Assert.assertEquals(subscription.getUserGroup().getKey(),
                    sub.getUnitKey());

            List<VOTechnicalServiceOperation> operations = sub
                    .getTechnicalServiceOperations();
            Assert.assertNotNull(operations);
            Assert.assertEquals(1, operations.size());
            VOTechnicalServiceOperation op = operations.get(0);
            Assert.assertEquals(operation.getOperationId(),
                    op.getOperationId());
            Assert.assertEquals(
                    LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_DESCRIPTION
                            .name(),
                    op.getOperationDescription());
            Assert.assertEquals(
                    LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_NAME
                            .name(),
                    op.getOperationName());
            break;
        default:
            Assert.fail("The performance hint is NOK!");
        }
    }
}
