/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.auditlog.ServiceAuditLogCollector;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceOperationException.Reason;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOLocalizedText;

public class ServiceProvisioningPartnerServiceLocalBeanGrantResalePermissionTest {

    private static final String SUPPLIER_ID = "MySupplier";
    private static final String GRANTEE_ID = "MyGrantee";
    private static final String MARKETPLACE_ID = "1234567";
    private static final String TP_ID = "testTechnicalProduct";
    private static final String PRODUCT_ID = "testProduct";
    private static final long PRICEMODEL_KEY = 4711L;
    private static final BigDecimal GRANTEE_REVENUE_SHARE = new BigDecimal(
            "25.5");

    private DataService ds;
    private ServiceProvisioningServiceLocal spsl;
    private ImageResourceServiceLocal irsl;
    private LocalizerServiceLocal lsl;
    private ServiceProvisioningPartnerServiceLocalBean sppslBean;
    private ServiceAuditLogCollector audit;

    private Organization supplier;
    private Organization grantee;
    private Product productTemplate;
    private ImageResource imgRes;
    private Product resaleCopy = null;
    private List<VOLocalizedText> productMarketingDescrs;
    private List<VOLocalizedText> priceModelLicenses;

    @Before
    public void setup() {
        ds = mock(DataService.class);

        spsl = mock(ServiceProvisioningServiceLocal.class);
        lsl = mock(LocalizerServiceLocal.class);
        irsl = mock(ImageResourceServiceLocal.class);
        audit = mock(ServiceAuditLogCollector.class);

        sppslBean = spy(new ServiceProvisioningPartnerServiceLocalBean());
        sppslBean.dm = ds;
        sppslBean.spsl = spsl;
        sppslBean.localizer = lsl;
        sppslBean.imgrsl = irsl;
        sppslBean.audit = audit;
    }

    private void setup(OfferingType resaleType, boolean revShareAtCatalogEntry,
            boolean createResaleCopy) throws ObjectNotFoundException {
        createDomainObjects(resaleType, revShareAtCatalogEntry);

        doAnswer(new Answer<DomainObject<?>>() {
            @Override
            public DomainObject<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                Object[] args = invocation.getArguments();
                if (args[0] instanceof Organization) {
                    Organization org = (Organization) args[0];
                    if (org.getOrganizationId().equals(
                            grantee.getOrganizationId())) {
                        return grantee;
                    } else {
                        return supplier;
                    }
                } else if (args[0] instanceof Product) {
                    return productTemplate;
                } else {
                    return null;
                }
            }
        }).when(ds).getReferenceByBusinessKey(any(DomainObject.class));

        createLocalizedResources();

        imgRes = new ImageResource();
        imgRes.setBuffer(new byte[] { 'T', 'E', 'S', 'T' });
        when(irsl.read(productTemplate.getKey(), ImageType.SERVICE_IMAGE))
                .thenReturn(imgRes);

        if (createResaleCopy) {
            createResaleCopy();
        }
        doReturn(resaleCopy).when(sppslBean).loadProductCopyForVendor(
                any(Organization.class), any(Product.class));
    }

    private void createDomainObjects(OfferingType resaleType,
            boolean revShareAtCatalogEntry) {
        OrganizationRoleType granteeType = null;
        RevenueShareModel brokerRevenueShareModel = null;
        RevenueShareModel resellerRevenueShareModel = null;

        if (resaleType.equals(OfferingType.BROKER)) {
            granteeType = OrganizationRoleType.BROKER;
            brokerRevenueShareModel = createRevenueShareModel(
                    RevenueShareModelType.BROKER_REVENUE_SHARE,
                    GRANTEE_REVENUE_SHARE);
            resellerRevenueShareModel = createRevenueShareModel(
                    RevenueShareModelType.RESELLER_REVENUE_SHARE,
                    BigDecimal.ZERO);
        } else if (resaleType.equals(OfferingType.RESELLER)) {
            granteeType = OrganizationRoleType.RESELLER;
            brokerRevenueShareModel = createRevenueShareModel(
                    RevenueShareModelType.BROKER_REVENUE_SHARE, BigDecimal.ZERO);
            resellerRevenueShareModel = createRevenueShareModel(
                    RevenueShareModelType.RESELLER_REVENUE_SHARE,
                    GRANTEE_REVENUE_SHARE);
        }

        supplier = new Organization();
        supplier.setOrganizationId(SUPPLIER_ID);
        grantee = new Organization();
        grantee.setOrganizationId(GRANTEE_ID);
        addRole(grantee, granteeType);

        PlatformUser supplierManager = new PlatformUser();
        supplierManager.setOrganization(supplier);
        addRole(supplier, OrganizationRoleType.SUPPLIER);
        doReturn(supplierManager).when(ds).getCurrentUser();

        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setTechnicalProductId(TP_ID);
        productTemplate = new Product();
        productTemplate.setProductId(PRODUCT_ID);
        productTemplate.setStatus(ServiceStatus.ACTIVE);
        productTemplate.setVendor(supplier);
        PriceModel pm = new PriceModel();
        pm.setKey(PRICEMODEL_KEY);
        pm.setProduct(productTemplate);
        productTemplate.setPriceModel(pm);

        CatalogEntry ce = new CatalogEntry();
        Marketplace mp = new Marketplace(MARKETPLACE_ID);
        ce.setProduct(productTemplate);
        ce.setMarketplace(mp);
        productTemplate.setCatalogEntries(Arrays
                .asList(new CatalogEntry[] { ce }));

        if (revShareAtCatalogEntry) {
            ce.setBrokerPriceModel(brokerRevenueShareModel);
            ce.setResellerPriceModel(resellerRevenueShareModel);
            mp.setBrokerPriceModel(createRevenueShareModel(
                    RevenueShareModelType.BROKER_REVENUE_SHARE, BigDecimal.ZERO));
            mp.setResellerPriceModel(createRevenueShareModel(
                    RevenueShareModelType.RESELLER_REVENUE_SHARE,
                    BigDecimal.ZERO));
        } else {
            mp.setBrokerPriceModel(brokerRevenueShareModel);
            mp.setResellerPriceModel(resellerRevenueShareModel);
        }
    }

    private RevenueShareModel createRevenueShareModel(
            RevenueShareModelType type, BigDecimal revenueShare) {
        RevenueShareModel model = new RevenueShareModel();
        model.setRevenueShareModelType(type);
        model.setRevenueShare(revenueShare);
        return model;
    }

    private void createLocalizedResources() {
        productMarketingDescrs = Arrays
                .asList(new VOLocalizedText[] { new VOLocalizedText("en",
                        "A product marketing description") });
        priceModelLicenses = Arrays
                .asList(new VOLocalizedText[] { new VOLocalizedText("en",
                        "A price model license") });

        when(
                lsl.getLocalizedValues(productTemplate.getKey(),
                        LocalizedObjectTypes.PRODUCT_MARKETING_DESC))
                .thenReturn(productMarketingDescrs);

        when(
                lsl.getLocalizedValues(
                        productTemplate.getPriceModel().getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE)).thenReturn(
                priceModelLicenses);
    }

    private void addRole(Organization org, OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganizationRole(role);
        org.setGrantedRoles(Collections.singleton(otr));
    }

    private void createResaleCopy() {
        resaleCopy = productTemplate.copyForResale(grantee);
        resaleCopy.setStatus(ServiceStatus.DELETED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void grantResalePermission_NullArguments() throws Exception {
        // when
        resaleCopy = sppslBean.grantResalePermission(null, null, null, null);
    }

    @Test
    public void grantResalePermission_Broker() throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, true, false);

        final List<DomainObject<?>> persistedDomainObjects = new ArrayList<DomainObject<?>>();
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                persistedDomainObjects.add((DomainObject<?>) args[0]);
                return null;
            }
        }).when(ds).persist(any(DomainObject.class));

        // when
        resaleCopy = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId(), resaleType);

        // then
        verifyResaleCopy();

        assertEquals(
                "A new Product, Broker price model and CatalogEntry should be persisted",
                3, persistedDomainObjects.size());
        assertSame("Resale Product Copy was not persisted", resaleCopy,
                persistedDomainObjects.get(0));
        CatalogEntry createdCatalogEntry = (CatalogEntry) persistedDomainObjects
                .get(2);
        assertNotNull(createdCatalogEntry);
        assertSame("Broker price model was not persisted",
                createdCatalogEntry.getBrokerPriceModel(),
                persistedDomainObjects.get(1));

        verifyCreatedCatalogEntry(createdCatalogEntry, resaleType);
        verifyImageResource();
    }

    @Test
    public void grantResalePermission_BrokerRevShareAtMP() throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, false, false);

        // when
        resaleCopy = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId(), resaleType);

        // then
        verifyResaleCopy();

        InOrder inOrder = inOrder(ds);
        inOrder.verify(ds).persist(resaleCopy);
        ArgumentCaptor<CatalogEntry> argCatEntry = ArgumentCaptor
                .forClass(CatalogEntry.class);
        inOrder.verify(ds, new Times(2)).persist(argCatEntry.capture());
        verifyCreatedCatalogEntry(argCatEntry.getValue(), resaleType);

        verifyImageResource();
    }

    @Test
    public void grantResalePermission_BrokerResaleCopyExists() throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, true, true);

        // when
        Product result = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId(), resaleType);

        // then
        assertTrue("Wrong result", result == resaleCopy);
        assertTrue("Wrong resale copy status",
                resaleCopy.getStatus() == ServiceStatus.INACTIVE);
    }

    @Test
    public void grantResalePermission_InactiveBrokerResaleCopyExists()
            throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, true, true);
        resaleCopy.setStatus(ServiceStatus.INACTIVE);

        // when
        Product result = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId(), resaleType);

        // then
        assertTrue("Wrong result", result == resaleCopy);
        assertTrue("Wrong resale copy status",
                resaleCopy.getStatus() == ServiceStatus.INACTIVE);
    }

    @Test
    public void grantResalePermission_Reseller() throws Exception {
        // given
        OfferingType resaleType = OfferingType.RESELLER;
        setup(resaleType, true, false);

        final List<DomainObject<?>> persistedDomainObjects = new ArrayList<DomainObject<?>>();
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                persistedDomainObjects.add((DomainObject<?>) args[0]);
                return null;
            }
        }).when(ds).persist(any(DomainObject.class));

        // when
        resaleCopy = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId(), resaleType);

        // then
        verifyResaleCopy();

        assertEquals(
                "A new Product, Reseller price model and CatalogEntry should be persisted",
                3, persistedDomainObjects.size());
        assertSame("Resale Product Copy was not persisted", resaleCopy,
                persistedDomainObjects.get(0));
        CatalogEntry createdCatalogEntry = (CatalogEntry) persistedDomainObjects
                .get(2);
        assertNotNull(createdCatalogEntry);
        assertSame("Reseller price model was not persisted",
                createdCatalogEntry.getResellerPriceModel(),
                persistedDomainObjects.get(1));

        verifyCreatedCatalogEntry(createdCatalogEntry, resaleType);
        verifyImageResource();

        verify(lsl).setLocalizedValues(resaleCopy.getKey(),
                LocalizedObjectTypes.RESELLER_PRICEMODEL_LICENSE,
                priceModelLicenses);

        verify(spsl).copyDefaultPaymentEnablement(resaleCopy, grantee);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void grantResalePermission_CalledByReseller() throws Exception {
        // given
        OfferingType resaleType = OfferingType.RESELLER;
        setup(resaleType, true, false);
        addRole(supplier, OrganizationRoleType.RESELLER);

        // when
        resaleCopy = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId(), resaleType);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void grantResalePermission_CalledByBroker() throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, true, false);
        addRole(supplier, OrganizationRoleType.BROKER);

        // when
        resaleCopy = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId(), resaleType);
    }

    @Test
    public void grantResalePermission_ResellerRevShareAtMP() throws Exception {
        // given
        OfferingType resaleType = OfferingType.RESELLER;
        setup(resaleType, false, false);

        // when
        resaleCopy = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId(), resaleType);

        // then
        verifyResaleCopy();

        InOrder inOrder = inOrder(ds);
        inOrder.verify(ds).persist(resaleCopy);
        ArgumentCaptor<CatalogEntry> argCatEntry = ArgumentCaptor
                .forClass(CatalogEntry.class);
        inOrder.verify(ds, new Times(2)).persist(argCatEntry.capture());
        verifyCreatedCatalogEntry(argCatEntry.getValue(), resaleType);

        verifyImageResource();

        verify(lsl).setLocalizedValues(resaleCopy.getKey(),
                LocalizedObjectTypes.RESELLER_PRICEMODEL_LICENSE,
                priceModelLicenses);

        verify(spsl).copyDefaultPaymentEnablement(resaleCopy, grantee);
    }

    @Test(expected = OrganizationAuthorityException.class)
    public void grantResalePermission_InvalidGrantee() throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, true, false);

        // when
        resaleCopy = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                supplier.getOrganizationId(), resaleType);
    }

    @Test(expected = ValidationException.class)
    public void grantResalePermission_InvalidResaleType() throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, true, false);

        // when
        resaleCopy = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId(), OfferingType.DIRECT);
    }

    @Test(expected = ServiceStateException.class)
    public void grantResalePermission_InvalidTemplateStatus() throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, true, false);
        productTemplate.setStatus(ServiceStatus.DELETED);

        // when
        resaleCopy = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId(), resaleType);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void grantResalePermission_InvalidTemplateOwner() throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, true, false);
        productTemplate.setVendor(grantee);

        // when
        resaleCopy = sppslBean.grantResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId(), resaleType);
    }

    @Test
    public void grantResalePermission_NoTemplate() throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, true, false);
        productTemplate.setTemplate(new Product());

        // when
        try {
            resaleCopy = sppslBean.grantResalePermission(
                    productTemplate.getProductId(),
                    supplier.getOrganizationId(), grantee.getOrganizationId(),
                    resaleType);
            fail("ServiceOperationException expected because passed service is not a template");
        } catch (ServiceOperationException soe) {
            // then
            soe.getMessageKey().contains(
                    Reason.SERVICE_IS_NOT_A_TEMPLATE.toString());
        }
    }

    @Test
    public void grantResalePermission_InvalidTemplateMarketplace()
            throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, true, false);
        productTemplate.getCatalogEntries().get(0).setMarketplace(null);

        // when
        try {
            resaleCopy = sppslBean.grantResalePermission(
                    productTemplate.getProductId(),
                    supplier.getOrganizationId(), grantee.getOrganizationId(),
                    resaleType);
            fail("ServiceOperationException expected because passed service is not assigned to a marketplace");
        } catch (ServiceOperationException soe) {
            // then
            soe.getMessageKey().contains(
                    Reason.SERVICE_NOT_ASSIGNED_TO_MARKETPLACE.toString());
        }
    }

    @Test
    public void grantResalePermission_InvalidTemplatePriceModel()
            throws Exception {
        // given
        OfferingType resaleType = OfferingType.BROKER;
        setup(resaleType, true, false);
        productTemplate.setPriceModel(null);

        // when
        try {
            resaleCopy = sppslBean.grantResalePermission(
                    productTemplate.getProductId(),
                    supplier.getOrganizationId(), grantee.getOrganizationId(),
                    resaleType);
            fail("ServiceOperationException expected because price model for passed service template is missing");
        } catch (ServiceOperationException soe) {
            // then
            soe.getMessageKey().contains(
                    Reason.MISSING_PRICE_MODEL_FOR_TEMPLATE.toString());
        }
    }

    private void verifyResaleCopy() {
        assertTrue(
                "Wrong resale copy id",
                resaleCopy.getProductId().contains(
                        productTemplate.getProductId()));
        assertTrue("Wrong resale copy vendor",
                resaleCopy.getVendor() == grantee);
        assertTrue("Wrong resale copy status",
                resaleCopy.getStatus() == ServiceStatus.INACTIVE);
        assertTrue("Wrong resale copy technical product",
                resaleCopy.getTechnicalProduct() == productTemplate
                        .getTechnicalProduct());
        assertTrue("Wrong resale copy template",
                resaleCopy.getTemplate() == productTemplate);
        assertNull("Price model at resale copy must not exist",
                resaleCopy.getPriceModel());
    }

    private void verifyCreatedCatalogEntry(CatalogEntry resaleCatEntry,
            OfferingType resaleType) throws Exception {
        assertTrue("Wrong product in created catalog entry",
                resaleCatEntry.getProduct() == resaleCopy);
        if (resaleType.equals(OfferingType.BROKER)) {
            assertTrue(
                    "Wrong broker revenue share model type in created catalog entry",
                    resaleCatEntry.getBrokerPriceModel()
                            .getRevenueShareModelType() == RevenueShareModelType.BROKER_REVENUE_SHARE);
            assertTrue("Wrong broker revenue share in created catalog entry",
                    resaleCatEntry.getBrokerPriceModel().getRevenueShare()
                            .equals(GRANTEE_REVENUE_SHARE));
            assertTrue(
                    "Broker revenue share model type in created catalog entry should be a copy",
                    resaleCatEntry.getBrokerPriceModel() != productTemplate
                            .getCatalogEntries().get(0).getBrokerPriceModel());
            assertNull("Reseller revenue share model should not exist",
                    resaleCatEntry.getResellerPriceModel());
            assertNull("Operator revenue share model should not exist",
                    resaleCatEntry.getOperatorPriceModel());
        } else {
            assertTrue(
                    "Wrong reseller revenue share model type in created catalog entry",
                    resaleCatEntry.getResellerPriceModel()
                            .getRevenueShareModelType() == RevenueShareModelType.RESELLER_REVENUE_SHARE);
            assertTrue("Wrong reseller revenue share in created catalog entry",
                    resaleCatEntry.getResellerPriceModel().getRevenueShare()
                            .equals(GRANTEE_REVENUE_SHARE));
            assertTrue(
                    "Reseller revenue share model type in created catalog entry should be a copy",
                    resaleCatEntry.getResellerPriceModel() != productTemplate
                            .getCatalogEntries().get(0).getResellerPriceModel());
            assertNull("Broker revenue share model should not exist",
                    resaleCatEntry.getBrokerPriceModel());
            assertNull("Operator revenue share model should not exist",
                    resaleCatEntry.getOperatorPriceModel());
        }
        assertTrue(resaleCatEntry.isAnonymousVisible());
        assertTrue(resaleCatEntry.isVisibleInCatalog());
    }

    private void verifyImageResource() {
        ArgumentCaptor<ImageResource> imgResArg = ArgumentCaptor
                .forClass(ImageResource.class);
        verify(irsl, never()).save(imgResArg.capture());
    }

}
