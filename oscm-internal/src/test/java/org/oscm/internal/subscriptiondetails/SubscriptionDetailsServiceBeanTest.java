/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 12.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptiondetails;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.DiscountService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.ServiceProvisioningServiceInternal;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.intf.SubscriptionServiceInternal;
import org.oscm.internal.partnerservice.PartnerService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOServiceEntry;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author weiser
 * 
 */
public class SubscriptionDetailsServiceBeanTest {

    private static final long CURRENT_ORG_KEY = 4711;
    private static final String SUB_ID = "subid";

    private SubscriptionDetailsServiceBean bean;

    @Before
    public void setup() {
        bean = new SubscriptionDetailsServiceBean();

        bean.accountService = mock(AccountService.class);
        bean.discountService = mock(DiscountService.class);
        bean.ds = mock(DataService.class);
        bean.identityService = mock(IdentityService.class);
        bean.partnerService = mock(PartnerService.class);
        bean.serviceProvisioningService = mock(ServiceProvisioningService.class);
        bean.serviceProvisioningServiceInternal = mock(ServiceProvisioningServiceInternal.class);
        bean.sessionCtx = mock(SessionContext.class);
        bean.sessionService = mock(SessionService.class);
        bean.subscriptionService = mock(SubscriptionService.class);
        bean.subscriptionServiceInternal = mock(SubscriptionServiceInternal.class);

        PlatformUser pu = new PlatformUser();

        pu.setOrganization(new Organization());
        pu.getOrganization().setKey(CURRENT_ORG_KEY);

        when(bean.ds.getCurrentUser()).thenReturn(pu);
    }

    @Test
    public void getServiceForSubscription_Broker() throws Exception {
        long serviceKey = givenVendorAndService(OrganizationRoleType.BROKER)
                .getKey();

        bean.getServiceForSubscription(serviceKey, "en");

        verifyUdaAccessForService(OrganizationRoleType.SUPPLIER.name());
    }

    @Test
    public void getServiceForSubscription_Reseller() throws Exception {
        long serviceKey = givenVendorAndService(OrganizationRoleType.RESELLER)
                .getKey();

        bean.getServiceForSubscription(serviceKey, "en");

        verifyUdaAccessForService(OrganizationRoleType.RESELLER.name());
    }

    @Test
    public void getServiceForSubscription_Supplier() throws Exception {
        long serviceKey = givenVendorAndService(OrganizationRoleType.SUPPLIER)
                .getKey();

        bean.getServiceForSubscription(serviceKey, "en");

        verifyUdaAccessForService(OrganizationRoleType.SUPPLIER.name());
    }

    @Test
    public void getSubscriptionDetails_Broker() throws Exception {
        // given
        long subKey = givenVendorAndSubscription(OrganizationRoleType.BROKER,
                SubscriptionStatus.ACTIVE).getKey();
        // when
        POSubscriptionDetails result = bean
                .getSubscriptionDetails(SUB_ID, "en").getResult(
                        POSubscriptionDetails.class);
        // then
        verifyUdaAccessForSubscription(OrganizationRoleType.SUPPLIER.name(),
                subKey);
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(result.getStatus().isPending()));
    }

    @Test
    public void getSubscriptionDetails_Reseller() throws Exception {
        // given
        long subKey = givenVendorAndSubscription(OrganizationRoleType.RESELLER,
                SubscriptionStatus.ACTIVE).getKey();
        // when
        bean.getSubscriptionDetails(SUB_ID, "en");
        // then
        verifyUdaAccessForSubscription(OrganizationRoleType.RESELLER.name(),
                subKey);
    }

    @Test
    public void getSubscriptionDetails_Supplier() throws Exception {
        // given
        long subKey = givenVendorAndSubscription(OrganizationRoleType.SUPPLIER,
                SubscriptionStatus.ACTIVE).getKey();
        // when
        bean.getSubscriptionDetails(SUB_ID, "en");
        // then
        verifyUdaAccessForSubscription(OrganizationRoleType.SUPPLIER.name(),
                subKey);
    }

    @Test
    public void getSubscriptionDetails_Pending() throws Exception {
        // given
        long subKey = givenVendorAndSubscription(OrganizationRoleType.SUPPLIER,
                SubscriptionStatus.PENDING).getKey();
        // when
        POSubscriptionDetails result = bean
                .getSubscriptionDetails(SUB_ID, "en").getResult(
                        POSubscriptionDetails.class);

        // then
        verifyUdaAccessForSubscription(OrganizationRoleType.SUPPLIER.name(),
                subKey);
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(result.getStatus().isPending()));
    }

    @Test
    public void getSubscriptionDetails_PendingUpd() throws Exception {
        // given
        long subKey = givenVendorAndSubscription(OrganizationRoleType.SUPPLIER,
                SubscriptionStatus.PENDING_UPD).getKey();
        // when
        POSubscriptionDetails result = bean
                .getSubscriptionDetails(SUB_ID, "en").getResult(
                        POSubscriptionDetails.class);

        // then
        verifyUdaAccessForSubscription(OrganizationRoleType.SUPPLIER.name(),
                subKey);
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(result.getStatus().isPendingUpdOrSuspendedUpd()));
    }

    @Test
    public void getSubscriptionDetails_SuspendedUpd() throws Exception {
        // given
        long subKey = givenVendorAndSubscription(OrganizationRoleType.SUPPLIER,
                SubscriptionStatus.SUSPENDED_UPD).getKey();
        // when
        POSubscriptionDetails result = bean
                .getSubscriptionDetails(SUB_ID, "en").getResult(
                        POSubscriptionDetails.class);

        // then
        verifyUdaAccessForSubscription(OrganizationRoleType.SUPPLIER.name(),
                subKey);
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(result.getStatus().isPendingUpdOrSuspendedUpd()));
    }

    private Product givenVendorAndService(OrganizationRoleType role)
            throws Exception {
        Organization org = new Organization();
        Organizations.grantOrganizationRole(org, role);
        org.setOrganizationId(role.name());

        Product p = Products.createProductWithoutPriceModel(org, null, "2");
        p.setKey(1234);
        if (role != OrganizationRoleType.SUPPLIER) {
            Organization supplier = new Organization();
            Organizations.grantOrganizationRole(supplier,
                    OrganizationRoleType.SUPPLIER);
            supplier.setOrganizationId(OrganizationRoleType.SUPPLIER.name());

            Product template = Products.createProductWithoutPriceModel(
                    supplier, null, "1");
            p.setTemplate(template);
        }

        when(bean.ds.getReference(eq(Product.class), eq(p.getKey())))
                .thenReturn(p);
        when(
                bean.partnerService.getServiceForMarketplace(eq(p.getKey()),
                        anyString())).thenReturn(
                new Response(new VOServiceEntry()));
        return p;
    }

    private Subscription givenVendorAndSubscription(OrganizationRoleType type,
            SubscriptionStatus status) throws Exception {
        Product template = givenVendorAndService(type);

        Subscription sub = new Subscription();
        Product p = template.copyForSubscription(bean.ds.getCurrentUser()
                .getOrganization(), sub);
        if (template.getType() == ServiceType.TEMPLATE) {
            p.setType(ServiceType.SUBSCRIPTION);
        } else {
            p.setType(ServiceType.PARTNER_SUBSCRIPTION);
        }
        sub.bindToProduct(p);
        sub.setKey(4321);
        sub.setStatus(status);

        VOSubscriptionDetails sd = new VOSubscriptionDetails();
        sd.setKey(sub.getKey());
        when(bean.subscriptionService.getSubscriptionDetails(eq(SUB_ID)))
                .thenReturn(sd);
        when(bean.ds.getReference(eq(Subscription.class), eq(sub.getKey())))
                .thenReturn(sub);
        VOOrganization org = new VOOrganization();
        org.setKey(CURRENT_ORG_KEY);
        when(bean.accountService.getOrganizationData()).thenReturn(org);
        return sub;
    }

    private void verifyUdaAccessForService(String vendorId) throws Exception {
        verify(bean.accountService).getUdaDefinitionsForCustomer(eq(vendorId));
        verify(bean.accountService).getUdasForCustomer(
                eq(UdaTargetType.CUSTOMER.name()), eq(CURRENT_ORG_KEY),
                eq(vendorId));
    }

    private void verifyUdaAccessForSubscription(String vendorId, long subKey)
            throws Exception {
        verifyUdaAccessForService(vendorId);
        verify(bean.accountService).getUdasForCustomer(
                eq(UdaTargetType.CUSTOMER_SUBSCRIPTION.name()), eq(subKey),
                eq(vendorId));
    }
}
