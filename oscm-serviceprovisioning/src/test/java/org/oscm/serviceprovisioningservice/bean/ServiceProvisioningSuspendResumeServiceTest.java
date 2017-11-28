/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 08.08.2011                                                      
 *                                                                              
 *  Completion Time: 08.08.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.util.ArrayList;
import java.util.Collections;

import javax.persistence.Query;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UserRole;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.vo.VOService;

/**
 * @author weiser
 * 
 */
public class ServiceProvisioningSuspendResumeServiceTest {

    private static final String MAIL = "mail@caller.com";

    private ServiceProvisioningServiceBean bean;
    private DataService dataServiceMock;
    private CommunicationServiceLocal commServiceMock;

    private Product product;
    private Product custSpec;
    private Product partnerProduct;
    private Marketplace marketplace;
    private Organization supplier;

    private VOService voService;
    private VOService voCustSpec;
    private VOService voPartnerService;

    @Before
    public void setup() throws Exception {

        PlatformUser user = new PlatformUser();
        user.setOrganization(new Organization());
        user.getOrganization().getPlatformUsers().add(user);
        user.setEmail(MAIL);

        // supplier with one administrator
        supplier = new Organization();
        PlatformUser pu = new PlatformUser();
        RoleAssignment ra = new RoleAssignment();
        ra.setRole(new UserRole(UserRoleType.ORGANIZATION_ADMIN));
        ra.setUser(pu);
        pu.getAssignedRoles().add(ra);
        supplier.addPlatformUser(pu);

        product = new Product();
        product.setStatus(ServiceStatus.ACTIVE);
        product.setKey(123);
        product.setTechnicalProduct(new TechnicalProduct());
        product.setVendor(supplier);
        product.setProductId("productId");
        product.setType(ServiceType.TEMPLATE);
        product.setAutoAssignUserEnabled(Boolean.FALSE);

        custSpec = new Product();
        custSpec.setStatus(ServiceStatus.ACTIVE);
        custSpec.setKey(321);
        custSpec.setTechnicalProduct(product.getTechnicalProduct());
        custSpec.setVendor(supplier);
        custSpec.setProductId("custSpecId");
        custSpec.setTemplate(product);
        custSpec.setType(ServiceType.CUSTOMER_TEMPLATE);
        custSpec.setAutoAssignUserEnabled(Boolean.FALSE);

        partnerProduct = new Product();
        partnerProduct.setStatus(ServiceStatus.ACTIVE);
        partnerProduct.setKey(322);
        partnerProduct.setTechnicalProduct(product.getTechnicalProduct());
        partnerProduct.setVendor(supplier);
        partnerProduct.setProductId("partnerProductId");
        partnerProduct.setTemplate(product);
        partnerProduct.setType(ServiceType.PARTNER_TEMPLATE);
        partnerProduct.setAutoAssignUserEnabled(Boolean.FALSE);

        marketplace = new Marketplace();
        marketplace.setOrganization(user.getOrganization());

        CatalogEntry ce = new CatalogEntry();
        ce.setMarketplace(marketplace);
        ce.setProduct(product);
        product.getCatalogEntries().add(ce);

        CatalogEntry brokerCe = new CatalogEntry();
        brokerCe.setMarketplace(marketplace);
        brokerCe.setProduct(product);
        partnerProduct.getCatalogEntries().add(brokerCe);

        bean = new ServiceProvisioningServiceBean();
        dataServiceMock = Mockito.mock(DataService.class);

        // the logged in user
        Mockito.when(dataServiceMock.getCurrentUser()).thenReturn(user);

        // the product to find
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(Product.class),
                        Matchers.eq(product.getKey()))).thenReturn(product);
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(Product.class),
                        Matchers.eq(custSpec.getKey()))).thenReturn(custSpec);

        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(Product.class),
                        Matchers.eq(partnerProduct.getKey()))).thenReturn(
                partnerProduct);

        Query query = Mockito.mock(Query.class);
        Mockito.when(query.getResultList()).thenReturn(
                Collections.singletonList(custSpec));
        Mockito.when(
                dataServiceMock.createNamedQuery(Matchers
                        .eq("Product.getCustomerCopies"))).thenReturn(query);

        commServiceMock = Mockito.mock(CommunicationServiceLocal.class);

        bean.dm = dataServiceMock;
        bean.commService = commServiceMock;
        bean.localizer = Mockito.mock(LocalizerServiceLocal.class);
        LocalizerFacade localizerMock = Mockito.mock(LocalizerFacade.class);
        voService = ProductAssembler.toVOProduct(product, localizerMock);
        voCustSpec = ProductAssembler.toVOProduct(custSpec, localizerMock);
        voPartnerService = ProductAssembler.toVOProduct(partnerProduct,
                localizerMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void suspendService_EmptyReason() throws Exception {
        try {
            bean.suspendService(voService, "");
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void suspendService_NotFound() throws Exception {
        try {
            Mockito.when(
                    dataServiceMock.getReference(Matchers.eq(Product.class),
                            Matchers.anyLong())).thenThrow(
                    new ObjectNotFoundException());
            bean.suspendService(voService, "some reason");
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void suspendService_CatalogEntriesNull() throws Exception {
        try {
            product.setCatalogEntries(null);
            bean.suspendService(voService, "some reason");
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void suspendService_CatalogEntriesEmpty() throws Exception {
        try {
            product.setCatalogEntries(new ArrayList<CatalogEntry>());
            bean.suspendService(voService, "some reason");
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void suspendService_NotMarketplaceOwner() throws Exception {
        try {
            Assert.assertNotNull(
                    "Marketplace expected to be set for catalog entry", product
                            .getCatalogEntries().get(0).getMarketplace());
            product.getCatalogEntries().get(0).getMarketplace()
                    .setOrganization(new Organization());
            bean.suspendService(voService, "some reason");
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void suspendService_SubscriptionProduct() throws Exception {
        try {
            product.setOwningSubscription(new Subscription());
            bean.suspendService(voService, "some reason");
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = ServiceStateException.class)
    public void suspendService_Deleted() throws Exception {
        try {
            product.setStatus(ServiceStatus.DELETED);
            bean.suspendService(voService, "some reason");
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = ServiceStateException.class)
    public void suspendService_Inactive() throws Exception {
        try {
            product.setStatus(ServiceStatus.INACTIVE);
            bean.suspendService(voService, "some reason");
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = ServiceStateException.class)
    public void suspendService_Obsolete() throws Exception {
        try {
            product.setStatus(ServiceStatus.OBSOLETE);
            bean.suspendService(voService, "some reason");
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = ServiceStateException.class)
    public void suspendService_Suspended() throws Exception {
        try {
            product.setStatus(ServiceStatus.SUSPENDED);
            bean.suspendService(voService, "some reason");
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test
    public void suspendService_PartnerTemplate() throws Exception {
        ArgumentCaptor<PlatformUser> user = ArgumentCaptor
                .forClass(PlatformUser.class);
        ArgumentCaptor<EmailType> mail = ArgumentCaptor
                .forClass(EmailType.class);
        ArgumentCaptor<Object[]> param = ArgumentCaptor
                .forClass(Object[].class);
        ArgumentCaptor<Marketplace> mp = ArgumentCaptor
                .forClass(Marketplace.class);

        // when
        String reason = "some reason";
        VOService svcUpdated = bean.suspendService(voPartnerService, reason);

        // then verify that the service has been suspended
        Assert.assertEquals(ServiceStatus.SUSPENDED, partnerProduct.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, product.getStatus());

        Mockito.verify(commServiceMock).sendMail(user.capture(),
                mail.capture(), param.capture(), mp.capture());
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();

        Assert.assertEquals(partnerProduct.getKey(), svcUpdated.getKey());
        Assert.assertEquals(product.getProductId(), svcUpdated.getServiceId());
        Assert.assertEquals(ServiceStatus.SUSPENDED, svcUpdated.getStatus());

        Assert.assertEquals(supplier.getPlatformUsers().get(0), user.getValue());
        Assert.assertEquals(EmailType.SERVICE_SUSPENDED, mail.getValue());
        Assert.assertEquals(marketplace, mp.getValue());
        Assert.assertEquals(partnerProduct.getProductId(), param.getValue()[0]);
        Assert.assertEquals(reason, param.getValue()[1]);
        Assert.assertEquals(MAIL, param.getValue()[2]);
    }

    @Test
    public void suspendService() throws Exception {
        ArgumentCaptor<PlatformUser> user = ArgumentCaptor
                .forClass(PlatformUser.class);
        ArgumentCaptor<EmailType> mail = ArgumentCaptor
                .forClass(EmailType.class);
        ArgumentCaptor<Object[]> param = ArgumentCaptor
                .forClass(Object[].class);
        ArgumentCaptor<Marketplace> mp = ArgumentCaptor
                .forClass(Marketplace.class);

        String reason = "some reason";
        VOService svcUpdated = bean.suspendService(voService, reason);
        Assert.assertEquals(ServiceStatus.SUSPENDED, product.getStatus());
        Assert.assertEquals(ServiceStatus.SUSPENDED, custSpec.getStatus());
        Mockito.verify(commServiceMock).sendMail(user.capture(),
                mail.capture(), param.capture(), mp.capture());
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();

        Assert.assertEquals(product.getKey(), svcUpdated.getKey());
        Assert.assertEquals(product.getProductId(), svcUpdated.getServiceId());
        Assert.assertEquals(ServiceStatus.SUSPENDED, svcUpdated.getStatus());

        Assert.assertEquals(supplier.getPlatformUsers().get(0), user.getValue());
        Assert.assertEquals(EmailType.SERVICE_SUSPENDED, mail.getValue());
        Assert.assertEquals(marketplace, mp.getValue());
        Assert.assertEquals(product.getProductId(), param.getValue()[0]);
        Assert.assertEquals(reason, param.getValue()[1]);
        Assert.assertEquals(MAIL, param.getValue()[2]);
    }

    @Test
    public void suspendService_PassCustSpec() throws Exception {
        ArgumentCaptor<PlatformUser> user = ArgumentCaptor
                .forClass(PlatformUser.class);
        ArgumentCaptor<EmailType> mail = ArgumentCaptor
                .forClass(EmailType.class);
        ArgumentCaptor<Object[]> param = ArgumentCaptor
                .forClass(Object[].class);
        ArgumentCaptor<Marketplace> mp = ArgumentCaptor
                .forClass(Marketplace.class);

        String reason = "some reason";
        VOService svcUpdated = bean.suspendService(voCustSpec, reason);
        Assert.assertEquals(ServiceStatus.SUSPENDED, product.getStatus());
        Assert.assertEquals(ServiceStatus.SUSPENDED, custSpec.getStatus());
        Mockito.verify(commServiceMock).sendMail(user.capture(),
                mail.capture(), param.capture(), mp.capture());
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();

        Assert.assertEquals(custSpec.getKey(), svcUpdated.getKey());
        Assert.assertEquals(product.getProductId(), svcUpdated.getServiceId());
        Assert.assertEquals(ServiceStatus.SUSPENDED, svcUpdated.getStatus());

        Assert.assertEquals(supplier.getPlatformUsers().get(0), user.getValue());
        Assert.assertEquals(EmailType.SERVICE_SUSPENDED, mail.getValue());
        Assert.assertEquals(marketplace, mp.getValue());
        Assert.assertEquals(product.getProductId(), param.getValue()[0]);
        Assert.assertEquals(reason, param.getValue()[1]);
        Assert.assertEquals(MAIL, param.getValue()[2]);
    }

    @Test
    public void suspendService_CustSpecInactive() throws Exception {
        custSpec.setStatus(ServiceStatus.INACTIVE);
        String reason = "some reason";
        bean.suspendService(voService, reason);
        Assert.assertEquals(ServiceStatus.SUSPENDED, product.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, custSpec.getStatus());
    }

    @Test
    public void suspendService_MultipleSupplierUsers() throws Exception {
        // a service manager
        PlatformUser pu = new PlatformUser();
        RoleAssignment ra = new RoleAssignment();
        ra.setRole(new UserRole(UserRoleType.SERVICE_MANAGER));
        ra.setUser(pu);
        pu.getAssignedRoles().add(ra);
        supplier.addPlatformUser(pu);

        // a standard user
        supplier.addPlatformUser(new PlatformUser());

        String reason = "some reason";
        VOService svcUpdated = bean.suspendService(voService, reason);
        Assert.assertEquals(ServiceStatus.SUSPENDED, product.getStatus());
        Assert.assertEquals(ServiceStatus.SUSPENDED, custSpec.getStatus());
        Object[] expected = new Object[] { product.getProductId(), reason, MAIL };

        Mockito.verify(commServiceMock, Mockito.times(2)).sendMail(
                Matchers.any(PlatformUser.class),
                Matchers.eq(EmailType.SERVICE_SUSPENDED),
                Matchers.eq(expected), Matchers.eq(marketplace));
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();

        Assert.assertEquals(product.getKey(), svcUpdated.getKey());
        Assert.assertEquals(product.getProductId(), svcUpdated.getServiceId());
        Assert.assertEquals(ServiceStatus.SUSPENDED, svcUpdated.getStatus());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void resumeService_NotFound() throws Exception {
        try {
            Mockito.when(
                    dataServiceMock.getReference(Matchers.eq(Product.class),
                            Matchers.anyLong())).thenThrow(
                    new ObjectNotFoundException());
            bean.resumeService(voService);
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void resumeService_CatalogEntriesNull() throws Exception {
        try {
            product.setCatalogEntries(null);
            bean.resumeService(voService);
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void resumeService_CatalogEntriesEmpty() throws Exception {
        try {
            product.setCatalogEntries(new ArrayList<CatalogEntry>());
            bean.resumeService(voService);
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void resumeService_NotMarketplaceOwner() throws Exception {
        try {
            Assert.assertNotNull(
                    "Marketplace expected to be set for catalog entry", product
                            .getCatalogEntries().get(0).getMarketplace());
            product.getCatalogEntries().get(0).getMarketplace()
                    .setOrganization(new Organization());
            bean.resumeService(voService);
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void resumeService_SubscriptionProduct() throws Exception {
        try {
            product.setOwningSubscription(new Subscription());
            bean.resumeService(voService);
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = ServiceStateException.class)
    public void resumeService_Deleted() throws Exception {
        try {
            product.setStatus(ServiceStatus.DELETED);
            bean.resumeService(voService);
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = ServiceStateException.class)
    public void resumeService_Inactive() throws Exception {
        try {
            product.setStatus(ServiceStatus.INACTIVE);
            bean.resumeService(voService);
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = ServiceStateException.class)
    public void resumeService_Obsolete() throws Exception {
        try {
            product.setStatus(ServiceStatus.OBSOLETE);
            bean.resumeService(voService);
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test(expected = ServiceStateException.class)
    public void resumeService_Active() throws Exception {
        try {
            bean.resumeService(voService);
        } finally {
            Mockito.verify(dataServiceMock, Mockito.never()).flush();
        }
    }

    @Test
    public void resumeService_PartnerTemplate() throws Exception {
        // given a suspended partner service
        partnerProduct.setStatus(ServiceStatus.SUSPENDED);

        // when
        VOService svcUpdated = bean.resumeService(voPartnerService);

        // then that the service is active again
        Assert.assertEquals(ServiceStatus.ACTIVE, partnerProduct.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, product.getStatus());
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Assert.assertEquals(voPartnerService.getKey(), svcUpdated.getKey());
        Assert.assertEquals(voPartnerService.getServiceId(),
                svcUpdated.getServiceId());
        Assert.assertEquals(ServiceStatus.ACTIVE, svcUpdated.getStatus());
    }

    @Test
    public void resumeService() throws Exception {
        product.setStatus(ServiceStatus.SUSPENDED);
        custSpec.setStatus(ServiceStatus.SUSPENDED);
        VOService svcUpdated = bean.resumeService(voService);
        Assert.assertEquals(ServiceStatus.ACTIVE, product.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, custSpec.getStatus());
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Assert.assertEquals(voService.getKey(), svcUpdated.getKey());
        Assert.assertEquals(voService.getServiceId(), svcUpdated.getServiceId());
        Assert.assertEquals(ServiceStatus.ACTIVE, svcUpdated.getStatus());
    }

    @Test
    public void resumeService_PassCustSpec() throws Exception {
        product.setStatus(ServiceStatus.SUSPENDED);
        custSpec.setStatus(ServiceStatus.SUSPENDED);
        VOService svcUpdated = bean.resumeService(voCustSpec);
        Assert.assertEquals(ServiceStatus.ACTIVE, product.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, custSpec.getStatus());
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Assert.assertEquals(voCustSpec.getKey(), svcUpdated.getKey());
        Assert.assertEquals(voCustSpec.getServiceId(),
                svcUpdated.getServiceId());
        Assert.assertEquals(ServiceStatus.ACTIVE, svcUpdated.getStatus());
    }

    @Test
    public void resumeService_CustSpecInactive() throws Exception {
        product.setStatus(ServiceStatus.SUSPENDED);
        custSpec.setStatus(ServiceStatus.INACTIVE);
        VOService svcUpdated = bean.resumeService(voService);
        Assert.assertEquals(ServiceStatus.ACTIVE, product.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, custSpec.getStatus());
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Assert.assertEquals(voService.getKey(), svcUpdated.getKey());
        Assert.assertEquals(voService.getServiceId(), svcUpdated.getServiceId());
        Assert.assertEquals(ServiceStatus.ACTIVE, svcUpdated.getStatus());
    }
}
