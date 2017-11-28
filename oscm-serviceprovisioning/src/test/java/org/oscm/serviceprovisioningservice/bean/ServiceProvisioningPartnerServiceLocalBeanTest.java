/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.SessionContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.PublicLandingpage;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;

public class ServiceProvisioningPartnerServiceLocalBeanTest {

    private ServiceProvisioningPartnerServiceLocalBean partnerBean;
    private DataService ds;
    private LandingpageServiceLocal lpService;

    @Captor
	ArgumentCaptor<Marketplace> lpCaptor;

    @Captor
    ArgumentCaptor<Product> productCaptor;

    static boolean STATUS_CHECK_NEEDED = true;
    static boolean STATUS_CHECK_NOT_NEEDED = false;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        partnerBean = spy(new ServiceProvisioningPartnerServiceLocalBean());

        ds = mock(DataService.class);
        lpService = mock(LandingpageServiceLocal.class);
        partnerBean.dm = ds;
        partnerBean.sessionCtx = mock(SessionContext.class);
        partnerBean.landingpageService = lpService;
        doNothing().when(lpService).removeProductFromLandingpage(
				any(Marketplace.class), any(Product.class));
    }

    @Test(expected = ServiceStateException.class)
    public void loadProduct_StatusCheck_DeletedService() throws Exception {
        // given
        Product prod = new Product();
        prod.setStatus(ServiceStatus.DELETED);
        doReturn(prod).when(ds).getReference(eq(Product.class),
                eq(prod.getKey()));

        // when
        Product loadedProd = partnerBean.loadProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);

        // then
        assertEquals(prod, loadedProd);
    }

    @Test(expected = ServiceStateException.class)
    public void loadProduct_StatusCheck_ObsoleteService() throws Exception {
        // given
        Product prod = new Product();
        prod.setStatus(ServiceStatus.OBSOLETE);
        doReturn(prod).when(ds).getReference(eq(Product.class),
                eq(prod.getKey()));

        // when
        Product loadedProd = partnerBean.loadProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);

        // then
        assertEquals(prod, loadedProd);
    }

    @Test
    public void loadProduct_StatusCheck_SuspendedService() throws Exception {
        // given
        Product prod = new Product();
        prod.setStatus(ServiceStatus.SUSPENDED);
        doReturn(prod).when(ds).getReference(eq(Product.class),
                eq(prod.getKey()));

        // when
        Product loadedProd = partnerBean.loadProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);

        // then
        assertEquals(prod, loadedProd);
    }

    @Test
    public void loadProduct_StatusCheck_InactiveService() throws Exception {
        // given
        Product prod = new Product();
        prod.setStatus(ServiceStatus.INACTIVE);
        doReturn(prod).when(ds).getReference(eq(Product.class),
                eq(prod.getKey()));

        // when
        Product loadedProd = partnerBean.loadProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);

        // then
        assertEquals(prod, loadedProd);
    }

    @Test
    public void loadProduct_StatusCheck_ActiveService() throws Exception {
        // given
        Product prod = new Product();
        prod.setStatus(ServiceStatus.ACTIVE);
        doReturn(prod).when(ds).getReference(eq(Product.class),
                eq(prod.getKey()));

        // when
        Product loadedProd = partnerBean.loadProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);

        // then
        assertEquals(prod, loadedProd);
    }

    public void loadProduct_NoStatusCheck_DeletedService() throws Exception {
        // given
        Product prod = new Product();
        prod.setStatus(ServiceStatus.DELETED);
        doReturn(prod).when(ds).getReference(eq(Product.class),
                eq(prod.getKey()));

        // when
        Product loadedProd = partnerBean.loadProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);

        // then
        assertEquals(prod, loadedProd);
    }

    public void loadProduct_NoStatusCheck_ObsoleteService() throws Exception {
        // given
        Product prod = new Product();
        prod.setStatus(ServiceStatus.OBSOLETE);
        doReturn(prod).when(ds).getReference(eq(Product.class),
                eq(prod.getKey()));

        // when
        Product loadedProd = partnerBean.loadProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);

        // then
        assertEquals(prod, loadedProd);
    }

    @Test
    public void loadProduct_NoStatusCheck_SuspendedService() throws Exception {
        // given
        Product prod = new Product();
        prod.setStatus(ServiceStatus.SUSPENDED);
        doReturn(prod).when(ds).getReference(eq(Product.class),
                eq(prod.getKey()));

        // when
        Product loadedProd = partnerBean.loadProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);

        // then
        assertEquals(prod, loadedProd);
    }

    @Test
    public void loadProduct_NoStatusCheck_InactiveService() throws Exception {
        // given
        Product prod = new Product();
        prod.setStatus(ServiceStatus.INACTIVE);
        doReturn(prod).when(ds).getReference(eq(Product.class),
                eq(prod.getKey()));

        // when
        Product loadedProd = partnerBean.loadProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);

        // then
        assertEquals(prod, loadedProd);
    }

    @Test
    public void loadProduct_NoStatusCheck_ActiveService() throws Exception {
        // given
        Product prod = new Product();
        prod.setStatus(ServiceStatus.ACTIVE);
        doReturn(prod).when(ds).getReference(eq(Product.class),
                eq(prod.getKey()));

        // when
        Product loadedProd = partnerBean.loadProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);

        // then
        assertEquals(prod, loadedProd);
    }

    @Test
    public void verifyOwningPermission_AsPlatformOperator() throws Exception {
        // given
        Organization org = new Organization();
        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(createOrgToRole(OrganizationRoleType.PLATFORM_OPERATOR));
        roles.add(createOrgToRole(OrganizationRoleType.SUPPLIER));
        roles.add(createOrgToRole(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        org.setGrantedRoles(roles);
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        doReturn(user).when(ds).getCurrentUser();

        // when
        partnerBean.verifyOwningPermission(new Product());

        // then no check is done
    }

    @Test
    public void verifyOwningPermission_AsMarketplaceOwner() throws Exception {
        // given
        Organization org = new Organization();
        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(createOrgToRole(OrganizationRoleType.MARKETPLACE_OWNER));
        roles.add(createOrgToRole(OrganizationRoleType.SUPPLIER));
        roles.add(createOrgToRole(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        org.setGrantedRoles(roles);
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        doReturn(user).when(ds).getCurrentUser();

        // when
        partnerBean.verifyOwningPermission(new Product());

        // then no check is done
    }

    @Test(expected = OperationNotPermittedException.class)
    public void verifyOwningPermission_AsSupTp() throws Exception {
        // given
        Organization org = new Organization();

        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(createOrgToRole(OrganizationRoleType.SUPPLIER));
        roles.add(createOrgToRole(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        roles.add(createOrgToRole(OrganizationRoleType.CUSTOMER));
        org.setGrantedRoles(roles);
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        doReturn(user).when(ds).getCurrentUser();

        // when
        partnerBean.verifyOwningPermission(new Product());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void verifyOwningPermission_AsBroker() throws Exception {
        // given
        Organization org = new Organization();

        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(createOrgToRole(OrganizationRoleType.BROKER));
        roles.add(createOrgToRole(OrganizationRoleType.CUSTOMER));
        org.setGrantedRoles(roles);
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        doReturn(user).when(ds).getCurrentUser();

        // when
        partnerBean.verifyOwningPermission(new Product());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void verifyOwningPermission_AsReseller() throws Exception {
        // given
        Organization org = new Organization();

        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(createOrgToRole(OrganizationRoleType.RESELLER));
        roles.add(createOrgToRole(OrganizationRoleType.CUSTOMER));
        org.setGrantedRoles(roles);
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        doReturn(user).when(ds).getCurrentUser();

        // when
        partnerBean.verifyOwningPermission(new Product());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void verifyOwningPermission_AsCustomer() throws Exception {
        // given
        Organization org = new Organization();

        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(createOrgToRole(OrganizationRoleType.CUSTOMER));
        org.setGrantedRoles(roles);
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        doReturn(user).when(ds).getCurrentUser();

        // when
        partnerBean.verifyOwningPermission(new Product());
    }

    @Test
    public void verifyOwningPermission_WithOwnProduct() throws Exception {
        // given
        Organization org = new Organization();

        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(createOrgToRole(OrganizationRoleType.SUPPLIER));
        roles.add(createOrgToRole(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        roles.add(createOrgToRole(OrganizationRoleType.CUSTOMER));
        org.setGrantedRoles(roles);
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        doReturn(user).when(ds).getCurrentUser();

        Product prod = new Product();
        prod.setVendor(org);

        // when
        partnerBean.verifyOwningPermission(prod);

        // then no exception occurs
    }

    private OrganizationToRole createOrgToRole(OrganizationRoleType role) {
        OrganizationToRole orgToRole = new OrganizationToRole();
        OrganizationRole orgRole = new OrganizationRole();
        orgRole.setRoleName(role);
        orgToRole.setOrganizationRole(orgRole);
        return orgToRole;
    }

    @Test
    public void checkTemplateOrPartnerSpecificCopy_PartnerCopiesAllowed()
            throws Exception {
        // given a non subscription-specific copy
        Product prod = new Product();
        prod.setTemplate(new Product());

        // when
        partnerBean.checkTemplateOrPartnerSpecificCopy(prod);
    }

    @Test(expected = ServiceOperationException.class)
    public void checkTemplateOrPartnerSpecificCopy_SubscriptionCopiesNotAllowed()
            throws Exception {
        // given a subscription-specific copy
        Product prod = new Product();
        prod.setTemplate(new Product());
        prod.setOwningSubscription(new Subscription());

        // when
        partnerBean.checkTemplateOrPartnerSpecificCopy(prod);
    }

    @Test(expected = ServiceOperationException.class)
    public void checkTemplateOrPartnerSpecificCopy_CustomerSpecificCopiesNotAllowed()
            throws Exception {
        // given a subscription-specific copy
        Product prod = new Product();
        prod.setTemplate(new Product());
        prod.setTargetCustomer(new Organization());

        // when
        partnerBean.checkTemplateOrPartnerSpecificCopy(prod);
    }

    @Test(expected = SaaSSystemException.class)
    public void validateRevenueShareOfProductCopy_NoPriceModel() {
        // given a catalog entry with a product which is a partner-specific copy
        // where the price model for the catalog entry is null
        Product prod = new Product();
        prod.setTemplate(new Product());
        CatalogEntry entry = new CatalogEntry();
        entry.setProduct(prod);

        // when
        partnerBean.validateRevenueShareOfProductCopy(entry);
    }

    @Test
    public void validateRevenueShareOfProductCopy() {
        // given a catalog entry with a product which is a partner-specific copy
        // where the price model for the catalog entry is null
        Product prod = new Product();
        prod.setTemplate(new Product());
        CatalogEntry entry = new CatalogEntry();
        entry.setProduct(prod);
        entry.setBrokerPriceModel(createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE));
        entry.setResellerPriceModel(createRevenueModel(RevenueShareModelType.RESELLER_REVENUE_SHARE));

        // when
        partnerBean.validateRevenueShareOfProductCopy(entry);
    }

    @Test(expected = NullPointerException.class)
    public void getPriceModelsForEntry_NoBrokerPriceModelForMarketplace() {
        // given a catalog entry whose marketplace does not have a broker price
        // model
        CatalogEntry ce = new CatalogEntry();
        ce.setMarketplace(new Marketplace("mId"));

        // when
        partnerBean.getPriceModelsForEntry(ce);
    }

    @Test(expected = NullPointerException.class)
    public void getPriceModelsForEntry_NoResellerPriceModelForMarketplace() {
        // given a catalog entry whose marketplace does not have a reseller
        // price model
        CatalogEntry ce = new CatalogEntry();
        Marketplace mp = new Marketplace("mId");
        mp.setBrokerPriceModel(createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE));
        ce.setMarketplace(mp);

        // when
        partnerBean.getPriceModelsForEntry(ce);
    }

    @Test
    public void getPriceModelsForEntry_NoBrokerPriceModelForCatalogEntry() {
        // given a catalog entry with no broker price model defined.
        CatalogEntry ce = new CatalogEntry();
        Marketplace mp = new Marketplace("mId");
        mp.setBrokerPriceModel(createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE));
        mp.setResellerPriceModel(createRevenueModel(RevenueShareModelType.RESELLER_REVENUE_SHARE));
        ce.setMarketplace(mp);

        // when
        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = partnerBean
                .getPriceModelsForEntry(ce);

        // then
        assertNotNull(revenueShareModels);

        RevenueShareModel brokerPriceModel = revenueShareModels
                .get(RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerPriceModel = revenueShareModels
                .get(RevenueShareModelType.RESELLER_REVENUE_SHARE);
        assertNotNull(brokerPriceModel);
        assertNotNull(resellerPriceModel);

        assertEquals(BigDecimal.ZERO, brokerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.BROKER_REVENUE_SHARE,
                brokerPriceModel.getRevenueShareModelType());

        assertEquals(BigDecimal.ZERO, resellerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.RESELLER_REVENUE_SHARE,
                resellerPriceModel.getRevenueShareModelType());

    }

    @Test
    public void getPriceModelsForEntry_NoResellerPriceModelForCatalogEntry() {
        // given a catalog entry with no reseller price model defined.
        CatalogEntry ce = new CatalogEntry();
        Marketplace mp = new Marketplace("mId");
        mp.setBrokerPriceModel(createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE));
        mp.setResellerPriceModel(createRevenueModel(RevenueShareModelType.RESELLER_REVENUE_SHARE));

        ce.setBrokerPriceModel(createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE));
        ce.setMarketplace(mp);

        // when
        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = partnerBean
                .getPriceModelsForEntry(ce);

        // then
        assertNotNull(revenueShareModels);

        RevenueShareModel brokerPriceModel = revenueShareModels
                .get(RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerPriceModel = revenueShareModels
                .get(RevenueShareModelType.RESELLER_REVENUE_SHARE);
        assertNotNull(brokerPriceModel);
        assertNotNull(resellerPriceModel);

        assertEquals(BigDecimal.ZERO, brokerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.BROKER_REVENUE_SHARE,
                brokerPriceModel.getRevenueShareModelType());

        assertEquals(BigDecimal.ZERO, resellerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.RESELLER_REVENUE_SHARE,
                resellerPriceModel.getRevenueShareModelType());

    }

    @Test(expected = IllegalArgumentException.class)
    public void saveOperatorRevenueShare_IllegalArgumentException()
            throws Exception {
        // when
        partnerBean.saveOperatorRevenueShare(101L, null, 1);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void saveOperatorRevenueShare_ObjectNotFound() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(partnerBean.dm)
                .getReference(eq(Product.class), eq(101L));
        // when
        partnerBean.saveOperatorRevenueShare(101L, new RevenueShareModel(), 0);
    }

    @Test(expected = ServiceOperationException.class)
    public void saveOperatorRevenueShare_ServiceOperationException()
            throws Exception {
        // given
        Product template = givenProduct(100L);
        Product product = givenProduct(101L);
        product.setTemplate(template);
        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(101L));
        doNothing().when(partnerBean).verifyOwningPermission(eq(product));

        // when
        partnerBean.saveOperatorRevenueShare(101L, new RevenueShareModel(), 0);
    }

    @Test
    public void saveOperatorRevenueShare_MandatoryForTemplates()
            throws Exception {
        // given
        Product product = givenProduct(101L);
        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(101L));
        doNothing().when(partnerBean).verifyOwningPermission(eq(product));
        doThrow(
                new SaaSSystemException(
                        "Template without operator revenue share")).when(
                partnerBean).validateOperatorRevenueShare(eq(product));

        // when
        try {
            partnerBean.saveOperatorRevenueShare(101L, new RevenueShareModel(),
                    0);
            fail();
        } catch (SaaSSystemException e) {
            assertTrue(e.getMessage().contains(
                    "Template without operator revenue share"));
        }
    }

    @Test
    public void saveOperatorRevenueShare_ValidationException() throws Exception {
        // given
        RevenueShareModel oldRevenue = givenRevenueShareModel(1L,
                RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        oldRevenue.setRevenueShare(BigDecimal.valueOf(10L));
        Product product = givenProduct(1L);
        product.setType(ServiceType.TEMPLATE);
        product.getCatalogEntries().get(0).setOperatorPriceModel(oldRevenue);

        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(1L));
        doNothing().when(partnerBean).verifyOwningPermission(eq(product));

        RevenueShareModel newRevenue = givenRevenueShareModel(2L,
                RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        newRevenue.setRevenueShare(BigDecimal.valueOf(101L));

        try {
            // when
            partnerBean.saveOperatorRevenueShare(1L, newRevenue, 0);
            fail();
        } catch (ValidationException e) {
            // then
            assertEquals(ReasonEnum.VALUE_NOT_IN_RANGE, e.getReason());
            assertEquals(
                    ServiceProvisioningPartnerServiceLocalBean.FIELD_REVENUE_SHARE
                            + " for "
                            + RevenueShareModelType.OPERATOR_REVENUE_SHARE,
                    e.getMember());
            verify(partnerBean.sessionCtx, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void saveOperatorRevenueShare() throws Exception {
        // given
        RevenueShareModel revenueToBeUpdated = givenRevenueShareModel(1L,
                RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        revenueToBeUpdated.setRevenueShare(BigDecimal.valueOf(10L));
        Product product = givenProduct(1L);
        product.setType(ServiceType.TEMPLATE);
        product.getCatalogEntries().get(0)
                .setOperatorPriceModel(revenueToBeUpdated);

        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(1L));
        doNothing().when(partnerBean).verifyOwningPermission(eq(product));

        RevenueShareModel newRevenue = givenRevenueShareModel(1L,
                RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        newRevenue.setRevenueShare(BigDecimal.valueOf(20L));

        // when
        partnerBean.saveOperatorRevenueShare(1L, newRevenue, 0);

        // then
        assertEquals(BigDecimal.valueOf(20L),
                revenueToBeUpdated.getRevenueShare());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getOperatorRevenueShare_ObjectNotFound() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(partnerBean.dm)
                .getReference(eq(Product.class), eq(101L));

        // when
        partnerBean.getOperatorRevenueShare(101L);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getOperatorRevenueShare_OperationNotPermittedException()
            throws Exception {
        // given
        Product product = givenProduct(101L);
        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(101L));
        doThrow(new OperationNotPermittedException()).when(partnerBean)
                .verifyOwningPermission(eq(product));

        // when
        partnerBean.getOperatorRevenueShare(101L);
    }

    @Test
    public void getOperatorRevenueShare_MandatoryForTemplates()
            throws Exception {
        // given
        Product product = givenProduct(101L);
        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(101L));
        doNothing().when(partnerBean).verifyOwningPermission(eq(product));
        doThrow(
                new SaaSSystemException(
                        "Template without operator revenue share")).when(
                partnerBean).validateOperatorRevenueShare(eq(product));

        // when
        try {
            partnerBean.getOperatorRevenueShare(101L);
            fail();
        } catch (SaaSSystemException e) {
            assertTrue(e.getMessage().contains(
                    "Template without operator revenue share"));
        }
    }

    @Test
    public void getOperatorRevenueShare() throws Exception {
        // given
        Product product = givenProduct(101L);
        product.setType(ServiceType.TEMPLATE);
        RevenueShareModel revenue = new RevenueShareModel();
        revenue.setRevenueShare(BigDecimal.TEN);
        product.getCatalogEntries().get(0).setOperatorPriceModel(revenue);

        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(101L));
        doNothing().when(partnerBean).verifyOwningPermission(eq(product));

        // when
        RevenueShareModel result = partnerBean.getOperatorRevenueShare(101L);

        // then
        assertEquals(BigDecimal.TEN, result.getRevenueShare());
    }

    @Test
    public void getOperatorRevenueShare_ServiceCopy() throws Exception {
        // given
        Product product = givenProduct(101L);
        product.setType(ServiceType.PARTNER_TEMPLATE);

        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(101L));
        doNothing().when(partnerBean).verifyOwningPermission(eq(product));

        // when
        RevenueShareModel result = partnerBean.getOperatorRevenueShare(101L);

        // then
        assertNull(result);
    }

    @Test
    public void validateOperatorRevenueShare() {
        // given
        Product product = givenProduct(101L);
        product.setType(ServiceType.TEMPLATE);
        RevenueShareModel revenue = new RevenueShareModel();
        revenue.setRevenueShare(BigDecimal.TEN);
        product.getCatalogEntries().get(0).setOperatorPriceModel(revenue);

        // when
        partnerBean.validateOperatorRevenueShare(product);
    }

    @Test
    public void validateOperatorRevenueShare_TemplateWithoutRevenue() {
        // given
        Product product = givenProduct(101L);
        product.setType(ServiceType.TEMPLATE);

        // when
        try {
            partnerBean.validateOperatorRevenueShare(product);
            fail();
        } catch (SaaSSystemException e) {
            assertTrue(e
                    .getMessage()
                    .contains(
                            "The catalog entry for the service template 101 does not have an operator price model."));
        }
    }

    @Test
    public void validateOperatorRevenueShare_Copy() {
        // given
        Product product = givenProduct(101L);
        product.setType(ServiceType.PARTNER_TEMPLATE);

        partnerBean.validateOperatorRevenueShare(product);
    }

    @Test
    public void validateOperatorRevenueShare_CopyWithRevenue() {
        // given
        Product product = givenProduct(101L);
        product.setType(ServiceType.PARTNER_TEMPLATE);
        RevenueShareModel revenue = new RevenueShareModel();
        revenue.setRevenueShare(BigDecimal.TEN);
        product.getCatalogEntries().get(0).setOperatorPriceModel(revenue);

        // when
        try {
            partnerBean.validateOperatorRevenueShare(product);
            fail();
        } catch (SaaSSystemException e) {
            assertTrue(e
                    .getMessage()
                    .contains(
                            "The catalog entry for the service copy 101 has an operator price model."));
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getDefaultOperatorRevenueShare_ObjectNotFound()
            throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(partnerBean.dm)
                .getReference(eq(Product.class), eq(101L));

        // when
        partnerBean.getDefaultOperatorRevenueShare(101L);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getDefaultOperatorRevenueShare_OperationNotPermittedException()
            throws Exception {
        // given
        Product product = givenProduct(101L);
        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(101L));
        doThrow(new OperationNotPermittedException()).when(partnerBean)
                .verifyOwningPermission(eq(product));

        // when
        partnerBean.getDefaultOperatorRevenueShare(101L);
    }

    @Test
    public void getDefaultOperatorRevenueShare_SaaSSystemException()
            throws Exception {
        // given
        Product product = givenProduct(101L);
        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(101L));
        doNothing().when(partnerBean).verifyOwningPermission(eq(product));
        doThrow(
                new SaaSSystemException(
                        "Template without operator revenue share")).when(
                partnerBean).validateDefaultOperatorRevenueShare(eq(product));

        // when
        try {
            partnerBean.getDefaultOperatorRevenueShare(101L);
            fail();
        } catch (SaaSSystemException e) {
            assertTrue(e.getMessage().contains(
                    "Template without operator revenue share"));
        }
    }

    @Test
    public void getDefaultOperatorRevenueShare() throws Exception {
        // given
        Product product = givenProduct(101L);
        RevenueShareModel revenue = new RevenueShareModel();
        revenue.setRevenueShare(BigDecimal.TEN);
        product.setVendor(givenOrganization("oId",
                OrganizationRoleType.SUPPLIER));
        product.getVendor().setOperatorPriceModel(revenue);

        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(101L));
        doNothing().when(partnerBean).verifyOwningPermission(eq(product));

        // when
        RevenueShareModel result = partnerBean
                .getDefaultOperatorRevenueShare(101L);

        // then
        assertEquals(BigDecimal.TEN, result.getRevenueShare());
    }

    @Test
    public void getDefaultOperatorRevenueShare_ServiceCopy() throws Exception {
        // given
        Product product = givenProduct(101L);
        Organization supplier = new Organization();
        product.setVendor(supplier);

        doReturn(product).when(partnerBean.dm).getReference(eq(Product.class),
                eq(101L));
        doNothing().when(partnerBean).verifyOwningPermission(eq(product));

        // when
        RevenueShareModel result = partnerBean
                .getDefaultOperatorRevenueShare(101L);

        // then
        assertNull(result);
    }

    @Test
    public void validateDefaultOperatorRevenueShare() {
        // given
        Product product = givenProduct(101L);
        RevenueShareModel revenue = new RevenueShareModel();
        revenue.setRevenueShare(BigDecimal.TEN);
        product.setVendor(givenOrganization("oId",
                OrganizationRoleType.SUPPLIER));
        product.getVendor().setOperatorPriceModel(revenue);

        // when
        partnerBean.validateDefaultOperatorRevenueShare(product);
    }

    @Test
    public void validateDefaultOperatorRevenueShare_TemplateWithoutRevenue() {
        // given
        Product product = givenProduct(101L);
        product.setVendor(givenOrganization("oId",
                OrganizationRoleType.SUPPLIER));

        // when
        try {
            partnerBean.validateDefaultOperatorRevenueShare(product);
            fail();
        } catch (SaaSSystemException e) {
            assertTrue(e
                    .getMessage()
                    .contains(
                            "The supplier organization oId does not have an operator price model."));
        }
    }

    @Test
    public void validateDefaultOperatorRevenueShare_Copy() {
        // given
        Product product = givenProduct(101L);
        product.setVendor(givenOrganization("oId",
                OrganizationRoleType.TECHNOLOGY_PROVIDER));

        partnerBean.validateDefaultOperatorRevenueShare(product);
    }

    @Test
    public void validateDefaultOperatorRevenueShare_CopyWithRevenue() {
        // given
        Product product = givenProduct(101L);
        RevenueShareModel revenue = new RevenueShareModel();
        revenue.setRevenueShare(BigDecimal.TEN);
        product.setVendor(givenOrganization("oId",
                OrganizationRoleType.TECHNOLOGY_PROVIDER));
        product.getVendor().setOperatorPriceModel(revenue);

        // when
        try {
            partnerBean.validateDefaultOperatorRevenueShare(product);
            fail();
        } catch (SaaSSystemException e) {
            assertTrue(e
                    .getMessage()
                    .contains(
                            "The non supplier organization oId has an operator price model."));
        }
    }

    @Test
    public void getRevenueShareModelsForProduct() throws Exception {
        // given a product whose catalog entry has revenue share models
        Product prod = new Product();
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        ce.setProduct(prod);
        entries.add(ce);
        prod.setCatalogEntries(entries);

        ce.setBrokerPriceModel(createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE));
        ce.setResellerPriceModel(createRevenueModel(RevenueShareModelType.RESELLER_REVENUE_SHARE));

        prod.setTemplate(new Product());

        ServiceProvisioningPartnerServiceLocalBean mockBean = spy(new ServiceProvisioningPartnerServiceLocalBean());
        doReturn(prod).when(mockBean).loadProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);
        doNothing().when(mockBean).verifyOwningPermission(prod);

        // when
        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = mockBean
                .getRevenueShareModelsForProduct(prod.getKey(),
                        STATUS_CHECK_NEEDED);

        RevenueShareModel brokerPriceModel = revenueShareModels
                .get(RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerPriceModel = revenueShareModels
                .get(RevenueShareModelType.RESELLER_REVENUE_SHARE);

        // then
        assertNotNull(revenueShareModels);
        assertNotNull(brokerPriceModel);
        assertNotNull(resellerPriceModel);

        assertEquals(BigDecimal.ZERO, brokerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.BROKER_REVENUE_SHARE,
                brokerPriceModel.getRevenueShareModelType());

        assertEquals(BigDecimal.ZERO, resellerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.RESELLER_REVENUE_SHARE,
                resellerPriceModel.getRevenueShareModelType());
    }

    @Test(expected = SaaSSystemException.class)
    public void getRevenueShareModelsForProduct_NoPriceModelForCopy()
            throws Exception {
        // given a product which is a partner-specific copy
        // where the price model for the catalog entry is null
        Product prod = new Product();
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        ce.setProduct(prod);
        entries.add(ce);
        prod.setCatalogEntries(entries);
        prod.setTemplate(new Product());
        doReturn(prod).when(partnerBean).loadProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);
        doNothing().when(partnerBean).verifyOwningPermission(prod);

        // when
        partnerBean.getRevenueShareModelsForProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);
    }

    @Test
    public void getPartnerRevenueShareForService_NoPriceModelAndMarketplaceForTemplate()
            throws Exception {
        // given a product which is a template where the price model of the
        // catalog entry is null and where the marketplace of the catalog entry
        // is null
        Product prod = new Product();
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        ce.setProduct(prod);
        entries.add(ce);
        prod.setCatalogEntries(entries);
        prod.setTemplate(prod);
        doReturn(prod).when(partnerBean).loadProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);
        doNothing().when(partnerBean).verifyOwningPermission(prod);

        // when
        partnerBean.getRevenueShareModelsForProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);
    }

    @Test(expected = NullPointerException.class)
    public void getRevenueShareModelsForProduct_NoBrokerPriceModelForMarketplace()
            throws Exception {
        // given
        Product prod = new Product();
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        ce.setMarketplace(new Marketplace("mId"));
        entries.add(ce);
        prod.setCatalogEntries(entries);
        prod.setTemplate(prod);
        doReturn(prod).when(partnerBean).loadProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);
        doNothing().when(partnerBean).verifyOwningPermission(prod);

        // when
        partnerBean.getRevenueShareModelsForProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);
    }

    @Test(expected = NullPointerException.class)
    public void getRevenueShareModelsForProduct_NoResellerPriceModelForMarketplace()
            throws Exception {
        // given
        Product prod = new Product();
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        Marketplace mp = new Marketplace("mId");
        mp.setBrokerPriceModel(createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE));
        ce.setMarketplace(mp);
        entries.add(ce);
        prod.setCatalogEntries(entries);
        prod.setTemplate(prod);
        doReturn(prod).when(partnerBean).loadProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);
        doNothing().when(partnerBean).verifyOwningPermission(prod);

        // when
        partnerBean.getRevenueShareModelsForProduct(prod.getKey(),
                STATUS_CHECK_NEEDED);
    }

    @Test(expected = NullPointerException.class)
    public void getRevenueShareModelsForProduct_AllStates_NoBrokerPriceModelForMarketplace()
            throws Exception {
        // given
        Product prod = new Product();
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        ce.setMarketplace(new Marketplace("mId"));
        entries.add(ce);
        prod.setCatalogEntries(entries);
        prod.setTemplate(prod);
        doReturn(prod).when(partnerBean).loadProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);
        doNothing().when(partnerBean).verifyOwningPermission(prod);

        // when
        partnerBean.getRevenueShareModelsForProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);
    }

    @Test(expected = SaaSSystemException.class)
    public void getRevenueShareModelsForProduct_AllStates_NoPriceModelForCopy()
            throws Exception {
        // given a product which is a partner-specific copy
        // where the price model for the catalog entry is null
        Product prod = new Product();
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        ce.setProduct(prod);
        entries.add(ce);
        prod.setCatalogEntries(entries);
        prod.setTemplate(new Product());
        doReturn(prod).when(partnerBean).loadProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);
        doNothing().when(partnerBean).verifyOwningPermission(prod);

        // when
        partnerBean.getRevenueShareModelsForProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);
    }

    @Test(expected = NullPointerException.class)
    public void getRevenueShareModelsForProduct_AllStates_NoResellerPriceModelForMarketplace()
            throws Exception {
        // given
        Product prod = new Product();
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        Marketplace mp = new Marketplace("mId");
        mp.setBrokerPriceModel(createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE));
        ce.setMarketplace(mp);
        entries.add(ce);
        prod.setCatalogEntries(entries);
        prod.setTemplate(prod);
        doReturn(prod).when(partnerBean).loadProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);
        doNothing().when(partnerBean).verifyOwningPermission(prod);

        // when
        partnerBean.getRevenueShareModelsForProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);
    }

    @Test
    public void getRevenueShareModelsForProduct_AllStates() throws Exception {
        // given a product whose catalog entry has revenue share models
        Product prod = new Product();
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        ce.setProduct(prod);
        entries.add(ce);
        prod.setCatalogEntries(entries);

        ce.setBrokerPriceModel(createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE));
        ce.setResellerPriceModel(createRevenueModel(RevenueShareModelType.RESELLER_REVENUE_SHARE));

        prod.setTemplate(new Product());

        ServiceProvisioningPartnerServiceLocalBean mockBean = spy(new ServiceProvisioningPartnerServiceLocalBean());
        doReturn(prod).when(mockBean).loadProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);
        doNothing().when(mockBean).verifyOwningPermission(prod);

        // when
        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = mockBean
                .getRevenueShareModelsForProduct(prod.getKey(),
                        STATUS_CHECK_NOT_NEEDED);

        RevenueShareModel brokerPriceModel = revenueShareModels
                .get(RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerPriceModel = revenueShareModels
                .get(RevenueShareModelType.RESELLER_REVENUE_SHARE);

        // then
        assertNotNull(revenueShareModels);
        assertNotNull(brokerPriceModel);
        assertNotNull(resellerPriceModel);

        assertEquals(BigDecimal.ZERO, brokerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.BROKER_REVENUE_SHARE,
                brokerPriceModel.getRevenueShareModelType());

        assertEquals(BigDecimal.ZERO, resellerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.RESELLER_REVENUE_SHARE,
                resellerPriceModel.getRevenueShareModelType());
    }

    @Test
    public void getRevenueShareModelsForProduct_AllStates_DELETEDStatus()
            throws Exception {
        // given a product whose catalog entry has revenue share models
        Product prod = new Product();
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        ce.setProduct(prod);
        entries.add(ce);
        prod.setCatalogEntries(entries);
        prod.setStatus(ServiceStatus.DELETED);

        ce.setBrokerPriceModel(createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE));
        ce.setResellerPriceModel(createRevenueModel(RevenueShareModelType.RESELLER_REVENUE_SHARE));

        prod.setTemplate(new Product());

        ServiceProvisioningPartnerServiceLocalBean mockBean = spy(new ServiceProvisioningPartnerServiceLocalBean());
        doReturn(prod).when(mockBean).loadProduct(prod.getKey(),
                STATUS_CHECK_NOT_NEEDED);
        doNothing().when(mockBean).verifyOwningPermission(prod);

        // when
        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = mockBean
                .getRevenueShareModelsForProduct(prod.getKey(),
                        STATUS_CHECK_NOT_NEEDED);

        RevenueShareModel brokerPriceModel = revenueShareModels
                .get(RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerPriceModel = revenueShareModels
                .get(RevenueShareModelType.RESELLER_REVENUE_SHARE);

        // then
        assertNotNull(revenueShareModels);
        assertNotNull(brokerPriceModel);
        assertNotNull(resellerPriceModel);

        assertEquals(BigDecimal.ZERO, brokerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.BROKER_REVENUE_SHARE,
                brokerPriceModel.getRevenueShareModelType());

        assertEquals(BigDecimal.ZERO, resellerPriceModel.getRevenueShare());
        assertEquals(RevenueShareModelType.RESELLER_REVENUE_SHARE,
                resellerPriceModel.getRevenueShareModelType());
    }

    @Test
    public void getCatalogEntryForProduct() throws Exception {
        // given
        long serviceKey = 11111;
        Product prod = new Product();
        prod.setKey(serviceKey);
        long catalogEntryKey = 22222;
        CatalogEntry ce = new CatalogEntry();
        ce.setKey(catalogEntryKey);
        List<CatalogEntry> catalogEntries = new ArrayList<CatalogEntry>();
        catalogEntries.add(ce);
        prod.setCatalogEntries(catalogEntries);
        doReturn(prod).when(partnerBean.dm).getReference(Product.class,
                prod.getKey());

        // when
        CatalogEntry result = partnerBean.getCatalogEntryForProduct(serviceKey);

        // then
        assertNotNull(result);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getPartnerProductsForTemplate_ObjectNotFoundExecption()
            throws Exception {
        // given
        when(ds.getReference(eq(Product.class), anyLong())).thenThrow(
                new ObjectNotFoundException());

        // when
        partnerBean.getPartnerProductsForTemplate(0);

        // then
    }

    @Test(expected = ServiceOperationException.class)
    public void getPartnerProductsForTemplate_NotTemplateError()
            throws Exception {
        // given
        Product product = new Product();
        product.setTemplate(new Product());
        when(ds.getReference(eq(Product.class), anyLong())).thenReturn(product);

        // when
        partnerBean.getPartnerProductsForTemplate(1);

        // then
    }

    private static RevenueShareModel createRevenueModel(
            RevenueShareModelType type) {
        RevenueShareModel m = new RevenueShareModel();
        m.setRevenueShare(BigDecimal.ZERO);
        m.setRevenueShareModelType(type);
        return m;
    }

    @Test
    public void executeQueryLoadTemplateServices() {
        // given
        Query q = mock(Query.class);
        when(ds.createNamedQuery(anyString())).thenReturn(q);
        doReturn(new ArrayList<Product>()).when(q).getResultList();
        Organization vendor = new Organization();
        vendor.setKey(1L);

        // when
        partnerBean.executeQueryLoadTemplateServices(
                EnumSet.of(ServiceType.TEMPLATE), vendor);

        // then
        verify(q).setParameter("vendorKey", Long.valueOf(vendor.getKey()));
        verify(q)
                .setParameter("productTypes", EnumSet.of(ServiceType.TEMPLATE));
        verify(q).setParameter("filterOutWithStatus",
                EnumSet.of(ServiceStatus.OBSOLETE, ServiceStatus.DELETED));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadSuppliedTemplateServices() {
        // given
        Organization o = new Organization();
        PlatformUser u = new PlatformUser();
        u.setOrganization(o);
        when(ds.getCurrentUser()).thenReturn(u);
        doReturn(new ArrayList<Product>()).when(partnerBean)
                .executeQueryLoadTemplateServices(any(EnumSet.class),
                        any(Organization.class));

        // when
        partnerBean.loadSuppliedTemplateServices();

        // then
        verify(partnerBean).executeQueryLoadTemplateServices(
                any(EnumSet.class), eq(o));
    }

    @Test
    public void removeProductFromLandingpage_Ok() throws Exception {
        // given
        Product product = givenProduct(1111);
        // when
        partnerBean.removeProductFromLandingpage(product);
        // then
        verify(lpService, times(1)).removeProductFromLandingpage(
                lpCaptor.capture(), productCaptor.capture());
		assertEquals("mp", lpCaptor.getValue().getMarketplaceId());
        assertEquals(1111, productCaptor.getValue().getKey());
    }

    private Product givenProduct(long key) {
        Product prod = new Product();
        prod.setKey(key);
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        CatalogEntry ce = new CatalogEntry();
        ce.setProduct(prod);
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId("mp");
        PublicLandingpage landingPage = new PublicLandingpage();
        landingPage.setMarketplace(mp);
        mp.setPublicLandingpage(landingPage);
        ce.setMarketplace(mp);
        entries.add(ce);
        prod.setCatalogEntries(entries);

        return prod;
    }

    private Organization givenOrganization(String oId,
            OrganizationRoleType... roles) {
        Organization org = new Organization();
        org.setOrganizationId(oId);
        Set<OrganizationToRole> otrs = new HashSet<OrganizationToRole>();
        for (OrganizationRoleType role : roles) {
            otrs.add(createOrgToRole(role));
        }
        org.setGrantedRoles(otrs);
        return org;
    }

    private RevenueShareModel givenRevenueShareModel(long key,
            RevenueShareModelType type) {
        RevenueShareModel revenue = new RevenueShareModel();
        revenue.setKey(key);
        revenue.setRevenueShareModelType(type);
        revenue.setRevenueShare(BigDecimal.ZERO);
        return revenue;
    }

}
