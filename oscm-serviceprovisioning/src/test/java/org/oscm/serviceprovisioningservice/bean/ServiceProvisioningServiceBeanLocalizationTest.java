/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPriceModelLocalization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOServiceLocalization;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogCollector;
import org.oscm.serviceprovisioningservice.auditlog.ServiceAuditLogCollector;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;

public class ServiceProvisioningServiceBeanLocalizationTest {

    private static long tkey = 0;
    private ServiceProvisioningServiceBean sps;
    private ServiceProvisioningServiceLocalizationBean spsLocalizer;
    private LocalizerServiceLocal localizer;
    private ServiceAuditLogCollector audit;
    private PriceModelAuditLogCollector priceModelAudit;
    private SubscriptionAuditLogCollector subscriptionAudit;
    private DataService ds;

    @Before
    public void setup() throws Exception {
        ds = mock(DataService.class);
        audit = mock(ServiceAuditLogCollector.class);
        priceModelAudit = mock(PriceModelAuditLogCollector.class);
        subscriptionAudit = mock(SubscriptionAuditLogCollector.class);
        sps = spy(new ServiceProvisioningServiceBean());
        sps.dm = ds;

        localizer = mock(LocalizerServiceLocal.class);
        doNothing().when(localizer).setLocalizedValues(anyLong(),
                any(LocalizedObjectTypes.class),
                anyListOf(VOLocalizedText.class));
        sps.localizer = localizer;

        spsLocalizer = spy(new ServiceProvisioningServiceLocalizationBean());
        spsLocalizer.ds = ds;
        spsLocalizer.localizer = localizer;
        spsLocalizer.serviceAudit = audit;
        spsLocalizer.priceModelAudit = priceModelAudit;
        spsLocalizer.subscriptionAudit = subscriptionAudit;
        sps.spsLocalizer = spsLocalizer;
    }

    @Test
    public void checkIsAllowedForLocalizingService_resellerOtherService()
            throws Exception {
        // given
        Organization o = givenResellerOrganization();
        givenCurrentUserForOrganization(o);
        Organization otherOrganization = givenOtherOrganization();
        otherOrganization.setKey(o.getKey() + 100);
        Product givenProduct = givenProduct(otherOrganization);

        // when
        boolean result = spsLocalizer
                .checkIsAllowedForLocalizingService(givenProduct.getKey());

        // then
        assertFalse(result);
    }

    @Test
    public void checkIsAllowedForLocalizingService_resellerOwnsService()
            throws Exception {
        // given
        Organization o = givenResellerOrganization();
        Product givenProduct = givenProduct(o);
        givenCurrentUserForOrganization(o);

        // when
        boolean result = spsLocalizer
                .checkIsAllowedForLocalizingService(givenProduct.getKey());

        // then
        assertTrue(result);
    }

    @Test
    public void checkIsAllowedForLocalizingService_brokerOtherService()
            throws Exception {
        // given
        Organization o = givenBrokerOrganization();
        givenCurrentUserForOrganization(o);
        Organization otherOrganization = givenOtherOrganization();
        givenProduct(otherOrganization);

        // when
        boolean result = spsLocalizer.checkIsAllowedForLocalizingService(1L);

        // then
        assertFalse(result);
    }

    @Test
    public void checkIsAllowedForLocalizingService_customer_owningSubscription()
            throws Exception {
        // given
        Organization customerOrganization = givenCustomerOrganization();
        givenCurrentUserForOrganization(customerOrganization);
        givenSubscriptionForProduct(givenProduct(givenSupplierOrganization()),
                customerOrganization);

        // when
        boolean result = spsLocalizer.checkIsAllowedForLocalizingService(1L);

        // then
        assertTrue(result);
    }

    @Test
    public void checkIsAllowedForLocalizingService_customer_notOwningSubscription()
            throws Exception {
        // given
        Organization customerOrganization = givenCustomerOrganization();
        givenCurrentUserForOrganization(customerOrganization);
        Organization supplierOrganization = givenSupplierOrganization();
        givenSubscriptionForProduct(givenProduct(supplierOrganization),
                supplierOrganization);

        // when
        boolean result = spsLocalizer.checkIsAllowedForLocalizingService(1L);

        // then
        assertFalse(result);
    }

    @Test
    public void checkIsAllowedForLocalizingService_customer_targetCustomer()
            throws Exception {
        // given
        Organization customerOrganization = givenCustomerOrganization();
        givenCurrentUserForOrganization(customerOrganization);
        Product givenProduct = givenProduct(givenSupplierOrganization());
        givenProduct.setTargetCustomer(customerOrganization);

        // when
        boolean result = spsLocalizer.checkIsAllowedForLocalizingService(1L);

        // then
        assertTrue(result);
    }

    @Test
    public void checkIsAllowedForLocalizingService_customer_notTargetCustomer()
            throws Exception {
        // given
        Organization supplierOrganization = givenSupplierOrganization();
        Organization customerOrganization = givenCustomerOrganization();
        givenCurrentUserForOrganization(customerOrganization);
        givenProduct(supplierOrganization);

        // when
        boolean result = spsLocalizer.checkIsAllowedForLocalizingService(1L);

        // then
        assertFalse(result);
    }

    private Subscription givenSubscriptionForProduct(Product givenProduct,
            Organization organization) {
        Subscription s = new Subscription();
        s.setKey(1L);
        s.setOrganization(organization);
        givenProduct.setOwningSubscription(s);
        return s;
    }

    private Organization givenBrokerOrganization() {
        Organization o = givenOrganization();
        grantRole(o, OrganizationRoleType.BROKER);
        return o;
    }

    private Organization givenResellerOrganization() {
        Organization o = givenOrganization();
        grantRole(o, OrganizationRoleType.RESELLER);
        return o;
    }

    private Organization givenSupplierOrganization() {
        Organization o = givenOrganization();
        grantRole(o, OrganizationRoleType.SUPPLIER);
        return o;
    }

    private Organization givenCustomerOrganization() {
        Organization o = givenOrganization();
        grantRole(o, OrganizationRoleType.CUSTOMER);
        return o;
    }

    private Organization givenOtherOrganization() {
        Organization o = givenOrganization();
        o.setKey(tkey++);
        return o;
    }

    private void grantRole(Organization o, OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganization(o);
        otr.setOrganizationRole(role);

        Set<OrganizationToRole> grantedRoles = new HashSet<OrganizationToRole>();
        grantedRoles.add(otr);
        o.setGrantedRoles(grantedRoles);
    }

    private Organization givenOrganization() {
        Organization o = new Organization();
        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        o.setKey(tkey++);
        o.setGrantedRoles(roles);
        return o;
    }

    private PlatformUser givenCurrentUserForOrganization(
            Organization organization) {
        PlatformUser u = new PlatformUser();
        u.setKey(1L);
        u.setOrganization(organization);
        for (UserRoleType role : OrganizationRoleType
                .correspondingUserRoles(organization.getGrantedRoleTypes())) {
            RoleAssignment ra = new RoleAssignment();
            ra.setRole(new UserRole(role));
            u.getAssignedRoles().add(ra);
        }
        when(ds.getCurrentUser()).thenReturn(u);
        return u;
    }

    private Product givenProduct(Organization o) throws ObjectNotFoundException {
        Product p = new Product();
        p.setKey(1L);
        p.setVendor(o);

        when(ds.getReference(Product.class, 1L)).thenReturn(p);
        return p;
    }

    @Test
    public void saveServiceLocalization_serviceDescription_asReseller()
            throws Exception {
        // given
        VOServiceLocalization sl = givenServiceLocalization("localizedDescription");
        VOService service = givenVoService();
        doReturn(Boolean.TRUE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());
        givenCurrentUserForOrganization(givenResellerOrganization());
        doReturn(sl).when(spsLocalizer).getServiceLocalization(
                any(Product.class));
        // when
        sps.saveServiceLocalization(service, sl);

        // then
        verify(localizer, never()).storeLocalizedResource(
                sl.getDescriptions().get(0).getLocale(), 1L,
                LocalizedObjectTypes.PRODUCT_MARKETING_DESC,
                sl.getDescriptions().get(0).getText());
        verify(localizer, never()).setLocalizedValues(1L,
                LocalizedObjectTypes.PRODUCT_MARKETING_DESC,
                Arrays.asList(sl.getDescriptions().get(0)));
        verify(localizer, never()).setLocalizedValues(1L,
                LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                Arrays.asList(sl.getNames().get(0)));
        verify(localizer, never()).setLocalizedValues(1L,
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION,
                Arrays.asList(sl.getShortDescriptions().get(0)));
    }

    private VOService givenVoService() {
        VOService service = new VOService();
        service.setKey(1L);
        service.setVersion(0);
        return service;
    }

    private VOServiceLocalization givenServiceLocalization(
            String localizedDescription) {
        VOServiceLocalization sl = new VOServiceLocalization();
        sl.setDescriptions(Arrays
                .asList(givenLocalizedText(localizedDescription)));
        sl.setNames(Arrays.asList(givenLocalizedText("name")));
        sl.setShortDescriptions(Arrays
                .asList(givenLocalizedText("shortDescription")));
        sl.setCustomTabNames(Arrays.asList(givenLocalizedText("customTabName")));
        return sl;
    }

    private VOLocalizedText givenLocalizedText(String localizedDescription) {
        VOLocalizedText lt = new VOLocalizedText();
        lt.setLocale("en");
        lt.setText(localizedDescription);
        lt.setVersion(0);
        return lt;
    }

    private VOPriceModelLocalization givenPriceModelLocalization(
            String description, String license) {
        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        localization.setDescriptions(Arrays
                .asList(givenLocalizedText(description)));
        localization.setLicenses(Arrays.asList(givenLocalizedText(license)));
        return localization;
    }

    private VOPriceModel givenPriceModel(boolean chargeable)
            throws ObjectNotFoundException {
        VOPriceModel pm = new VOPriceModel();
        pm.setKey(1L);
        if (chargeable) {
            pm.setType(PriceModelType.PRO_RATA);
        } else {
            pm.setType(PriceModelType.FREE_OF_CHARGE);
        }

        PriceModel p = new PriceModel();
        p.setKey(1L);
        if (chargeable) {
            p.setType(PriceModelType.PRO_RATA);
        } else {
            p.setType(PriceModelType.FREE_OF_CHARGE);
        }

        when(ds.getReference(PriceModel.class, pm.getKey())).thenReturn(p);
        return pm;
    }

    @Test
    public void savePriceModelLocalization_asSupplier() throws Exception {
        // given
        givenCurrentUserForOrganization(givenSupplierOrganization());
        VOPriceModel priceModel = givenPriceModel(true);
        VOPriceModelLocalization localization = givenPriceModelLocalization(
                "desc", "license");
        doReturn(Boolean.TRUE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());
        ds.getReference(PriceModel.class, priceModel.getKey()).setProduct(
                givenProduct(givenSupplierOrganization()));
        doReturn(localization).when(spsLocalizer).getPriceModelLocalization(
                anyLong());
        // when
        sps.savePriceModelLocalization(priceModel, localization);

        // then
        verify(localizer, times(1)).setLocalizedValues(priceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                localization.getDescriptions());
        verify(localizer, times(1)).setLocalizedValues(priceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_LICENSE,
                localization.getLicenses());
    }

    @Test
    public void savePriceModelLocalization_asSupplier_notChargeable()
            throws Exception {
        // given
        givenCurrentUserForOrganization(givenSupplierOrganization());
        givenProduct(givenSupplierOrganization());
        VOPriceModel priceModel = givenPriceModel(false);
        Product product = givenProduct(givenSupplierOrganization());
        VOPriceModelLocalization localization = givenPriceModelLocalization(
                "desc", "license");
        doReturn(Boolean.TRUE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());
        ds.getReference(PriceModel.class, priceModel.getKey()).setProduct(
                product);
        doReturn(localization).when(spsLocalizer).getPriceModelLocalization(
                anyLong());

        // when
        sps.savePriceModelLocalization(priceModel, localization);

        // then
        verify(localizer, times(1)).removeLocalizedValues(priceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
        verify(localizer, times(1)).setLocalizedValues(priceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_LICENSE,
                localization.getLicenses());
    }

    @Test
    public void savePriceModelLocalization_asSupplier_localizedDescriptionRemovedInNonChargeable()
            throws Exception {
        // given
        givenCurrentUserForOrganization(givenSupplierOrganization());
        VOPriceModel priceModel = givenPriceModel(false);
        VOPriceModelLocalization localization = givenPriceModelLocalization(
                "desc", "license");
        doReturn(Boolean.TRUE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());
        ds.getReference(PriceModel.class, priceModel.getKey()).setProduct(
                givenProduct(givenSupplierOrganization()));
        doReturn(localization).when(spsLocalizer).getPriceModelLocalization(
                anyLong());

        // when
        sps.savePriceModelLocalization(priceModel, localization);

        // then
        verify(localizer, times(1)).removeLocalizedValues(priceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
    }

    @Test
    public void saveServiceLocalization_asSupplier() throws Exception {
        // given
        givenCurrentUserForOrganization(givenSupplierOrganization());

        VOServiceLocalization sl = givenServiceLocalization("localized");
        VOService service = givenVoService();
        doReturn(Boolean.TRUE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());
        doReturn(sl).when(spsLocalizer).getServiceLocalization(
                any(Product.class));
        // when
        sps.saveServiceLocalization(service, sl);

        // then
        verify(localizer, times(1)).setLocalizedValues(1L,
                LocalizedObjectTypes.PRODUCT_MARKETING_DESC,
                Arrays.asList(sl.getDescriptions().get(0)));
        verify(localizer, times(1)).setLocalizedValues(1L,
                LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                Arrays.asList(sl.getNames().get(0)));
        verify(localizer, times(1)).setLocalizedValues(1L,
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION,
                Arrays.asList(sl.getShortDescriptions().get(0)));
        verify(localizer, times(1)).setLocalizedValues(1L,
                LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME,
                Arrays.asList(sl.getCustomTabNames().get(0)));
    }

    @Test
    public void getServiceLocalization_asCustomer() throws Exception {
        // given
        givenCurrentUserForOrganization(givenCustomerOrganization());
        givenProduct(givenSupplierOrganization());
        VOService service = givenVoService();
        doReturn(Boolean.TRUE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());

        // when
        sps.getServiceLocalization(service);

        // then
        verify(localizer, times(1)).getLocalizedValues(service.getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_NAME);
        verify(localizer, times(1)).getLocalizedValues(service.getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_DESC);
        verify(localizer, times(1)).getLocalizedValues(service.getKey(),
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getServiceLocalization_noProduct() throws Exception {
        // given
        givenProduct(givenSupplierOrganization());
        doReturn(Boolean.FALSE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());

        // when
        sps.getServiceLocalization(givenVoService());
    }

    @Test
    public void getPriceModelLocalization() throws Exception {
        // given
        givenCurrentUserForOrganization(givenSupplierOrganization());
        VOPriceModel givenPriceModel = givenPriceModel(true);
        doReturn(Boolean.TRUE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());
        Product p = givenProduct(givenSupplierOrganization());
        ds.getReference(PriceModel.class, givenPriceModel.getKey()).setProduct(
                p);
        ds.getReference(Product.class, p.getKey()).setPriceModel(
                ds.getReference(PriceModel.class, givenPriceModel.getKey()));

        // when
        sps.getPriceModelLocalization(givenPriceModel);

        // then
        verify(localizer, times(1)).getLocalizedValues(
                givenPriceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
        verify(localizer, times(1)).getLocalizedValues(
                givenPriceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_LICENSE);
    }

    @Test
    public void getPriceModelLocalization_asSupplierOrBroker() throws Exception {
        // given
        Product product = new Product();
        PriceModel priceModel = new PriceModel();
        priceModel.setKey(675L);
        product.setKey(321L);
        product.setTemplate(product);
        product.setPriceModel(priceModel);
        Organization vendor = new Organization();
        product.setVendor(vendor);
        when(ds.getReference(Product.class, 321L)).thenReturn(product);

        // when
        spsLocalizer.getPriceModelLocalizationForSupplierOrBroker(product,
                vendor);
        // then
        verify(localizer, times(1)).getLocalizedValues(priceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
        verify(localizer, times(1)).getLocalizedValues(priceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_LICENSE);
    }

    @Test
    public void getPriceModelLocalization_asReseller() throws Exception {
        // given
        Product product = new Product();
        PriceModel priceModel = new PriceModel();
        priceModel.setKey(887L);
        product.setTemplate(product);
        product.setPriceModel(priceModel);
        long serviceKey = 989L;

        // when
        spsLocalizer.getPriceModelLocalizationForReseller(product, serviceKey);
        // then
        verify(localizer, times(1)).getLocalizedValues(priceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
        verify(localizer, times(1)).getLocalizedValues(serviceKey,
                LocalizedObjectTypes.RESELLER_PRICEMODEL_LICENSE);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getPriceModelLicenseTemplateLocalization_notFound()
            throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());

        // when
        sps.getPriceModelLicenseTemplateLocalization(new VOServiceDetails());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getPriceModelLicenseTemplateLocalization_notPermitted()
            throws Exception {
        // given
        doReturn(Boolean.FALSE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());

        // when
        sps.getPriceModelLicenseTemplateLocalization(new VOServiceDetails());
    }

    @Test
    public void getPriceModelLicenseTemplateLocalization() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());
        VOServiceDetails givenService = givenVoServiceDetails();

        // when
        sps.getPriceModelLicenseTemplateLocalization(givenService);

        // then
        verify(localizer).getLocalizedValues(
                givenService.getTechnicalService().getKey(),
                LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
    }

    private VOServiceDetails givenVoServiceDetails() {
        VOTechnicalService technicalService = new VOTechnicalService();
        technicalService.setKey(2L);
        VOServiceDetails givenService = new VOServiceDetails();
        givenService.setKey(1L);
        givenService.setTechnicalService(technicalService);
        return givenService;
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getPriceModelLocalization_OperationNotPermitted()
            throws Exception {
        // given
        PlatformUser u = new PlatformUser();
        u.setOrganization(givenOtherOrganization());
        doReturn(u).when(ds).getCurrentUser();

        // when
        sps.getPriceModelLocalization(new VOPriceModel());
    }

    @Test
    public void getServiceLocalization_asReseller() throws Exception {
        // given
        Organization resellerOrganization = givenResellerOrganization();
        givenProduct(resellerOrganization);
        doReturn(Boolean.TRUE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());
        givenCurrentUserForOrganization(resellerOrganization);
        VOService voService = givenVoService();

        // when
        sps.getServiceLocalization(voService);

        // then
        verify(localizer).getLocalizedValues(voService.getKey(),
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION);
        verify(localizer).getLocalizedValues(voService.getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_NAME);
        verify(localizer).getLocalizedValues(voService.getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_DESC);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getPriceModelLocalizationAsReseller_OperationNotPermitted()
            throws Exception {
        // given
        Organization resellerOrganization = givenResellerOrganization();
        givenProduct(resellerOrganization);
        doReturn(Boolean.TRUE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());
        givenCurrentUserForOrganization(resellerOrganization);
        VOPriceModel priceModel = givenPriceModel(false);

        // when
        sps.getPriceModelLocalization(priceModel);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void savePriceModelLocalizationForReseller() throws Exception {
        // given
        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        givenProduct(new Organization());
        doReturn(Boolean.FALSE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());

        // when
        spsLocalizer.savePriceModelLocalizationForReseller(1L, true,
                localization);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void savePriceModelLocalizationForSupplier_noProduct()
            throws Exception {
        // given
        PriceModel model = new PriceModel();
        model.setProduct(null);
        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        when(ds.getReference(PriceModel.class, 22L)).thenReturn(model);

        // when
        spsLocalizer.savePriceModelLocalizationForSupplier(22L, true,
                localization);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void savePriceModelLocalizationForSupplier_notAllowed()
            throws Exception {
        // given
        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        givenProduct(new Organization());
        doReturn(Boolean.FALSE).when(spsLocalizer)
                .checkIsAllowedForLocalizingService(anyLong());
        PriceModel model = new PriceModel();
        model.setProduct(new Product());
        when(ds.getReference(PriceModel.class, 22L)).thenReturn(model);

        // when
        spsLocalizer.savePriceModelLocalizationForSupplier(22L, true,
                localization);
    }

    @Test
    public void saveLicenseInformationForPriceModel_oldLicensesEmpty() {
        // given
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setLicense("license");
        List<VOLocalizedText> oldLicenses = new ArrayList<VOLocalizedText>();
        long productKey = 1L;
        long priceModelKey = 1L;
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("en");
        doReturn(oldLicenses).when(localizer).getLocalizedValues(
                eq(productKey), eq(LocalizedObjectTypes.PRODUCT_LICENSE_DESC));

        // when
        boolean result = sps.saveLicenseInformationForPriceModel(productKey,
                priceModelKey, priceModel, currentUser, true);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
        verify(localizer, times(1)).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(productKey),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE),
                eq(priceModel.getLicense()));
    }

    @Test
    public void saveLicenseInformationForPriceModel_oldLicensesNull() {
        // given
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setLicense("license");
        long productKey = 1L;
        long priceModelKey = 1L;
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("en");
        doReturn(Collections.emptyList()).when(localizer).getLocalizedValues(eq(productKey),
                eq(LocalizedObjectTypes.PRODUCT_LICENSE_DESC));

        // when
        boolean result = sps.saveLicenseInformationForPriceModel(productKey,
                priceModelKey, priceModel, currentUser, true);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
        verify(localizer, times(1)).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(productKey),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE),
                eq(priceModel.getLicense()));
    }

    @Test
    public void saveLicenseInformationForPriceModel_oldLicensesEmptyAndNewLicenseEmpty() {
        // given
        final String EMPTY = "";
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setLicense(null);
        List<VOLocalizedText> oldLicenses = new ArrayList<VOLocalizedText>();
        long productKey = 1L;
        long priceModelKey = 1L;
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("en");
        doReturn(oldLicenses).when(localizer).getLocalizedValues(
                eq(productKey), eq(LocalizedObjectTypes.PRODUCT_LICENSE_DESC));

        // when
        boolean result = sps.saveLicenseInformationForPriceModel(productKey,
                priceModelKey, priceModel, currentUser, true);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
        verify(localizer, times(1)).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(productKey),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE), eq(EMPTY));
    }

    @Test
    public void saveLicenseInformationForPriceModel_oldLicensesExists() {
        // given
        final String NEW_LICENSE_DESCRIPTION = "license";
        final String OLD_LICENSE_DESCRIPTION = "description";
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setLicense(NEW_LICENSE_DESCRIPTION);
        priceModel.setKey(0);
        List<VOLocalizedText> oldLicenses = new ArrayList<VOLocalizedText>();
        VOLocalizedText enText = createVOLocalizedText("en",
                OLD_LICENSE_DESCRIPTION, 0);
        oldLicenses.add(enText);
        VOLocalizedText deText = createVOLocalizedText("de",
                OLD_LICENSE_DESCRIPTION, 0);
        oldLicenses.add(deText);
        long productKey = 1L;
        long priceModelKey = 1L;
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("jp");
        doReturn(oldLicenses).when(localizer).getLocalizedValues(
                eq(priceModel.getKey()),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE));

        // when
        boolean result = sps.saveLicenseInformationForPriceModel(productKey,
                priceModelKey, priceModel, currentUser, false);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
        verify(localizer, times(1)).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(productKey),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE),
                eq(NEW_LICENSE_DESCRIPTION));
        verify(localizer, times(1)).storeLocalizedResource(eq("en"),
                eq(productKey), eq(LocalizedObjectTypes.PRICEMODEL_LICENSE),
                eq(OLD_LICENSE_DESCRIPTION));
        verify(localizer, times(1)).storeLocalizedResource(eq("de"),
                eq(productKey), eq(LocalizedObjectTypes.PRICEMODEL_LICENSE),
                eq(OLD_LICENSE_DESCRIPTION));
    }

    /**
     * @return
     */
    private VOLocalizedText createVOLocalizedText(String locale, String text,
            int version) {
        VOLocalizedText enText = new VOLocalizedText();
        enText.setLocale(locale);
        enText.setText(text);
        enText.setVersion(version);
        return enText;
    }

    @Test
    public void saveLicenseInformationForPriceModel_oldLicensesForUserLocaleExists() {
        // given
        final String NEW_LICENSE_DESCRIPTION = "license";
        final String OLD_LICENSE_DESCRIPTION = "description";
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setLicense(NEW_LICENSE_DESCRIPTION);
        priceModel.setKey(0);
        List<VOLocalizedText> oldLicenses = new ArrayList<VOLocalizedText>();
        VOLocalizedText jpText = createVOLocalizedText("jp",
                OLD_LICENSE_DESCRIPTION, 0);
        oldLicenses.add(jpText);
        long productKey = 1L;
        long priceModelKey = 1L;
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("jp");
        doReturn(oldLicenses).when(localizer).getLocalizedValues(
                eq(priceModel.getKey()),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE));

        // when
        boolean result = sps.saveLicenseInformationForPriceModel(productKey,
                priceModelKey, priceModel, currentUser, false);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
        verify(localizer, times(1)).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(productKey),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE),
                eq(NEW_LICENSE_DESCRIPTION));
    }

    @Test
    public void saveLicenseInformationForPriceModel_oldLicensesForUserLocaleExistsSameNewLicense() {
        // given
        final String UNCHANGED_LICENSE_DESCRIPTION = "description";
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setLicense(UNCHANGED_LICENSE_DESCRIPTION);
        priceModel.setKey(0);
        List<VOLocalizedText> oldLicenses = new ArrayList<VOLocalizedText>();
        VOLocalizedText jpText = createVOLocalizedText("jp",
                UNCHANGED_LICENSE_DESCRIPTION, 0);
        oldLicenses.add(jpText);
        long productKey = 1L;
        long priceModelKey = 1L;
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("jp");
        doReturn(oldLicenses).when(localizer).getLocalizedValues(
                eq(priceModel.getKey()),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE));

        // when
        boolean result = sps.saveLicenseInformationForPriceModel(productKey,
                priceModelKey, priceModel, currentUser, false);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
        verify(localizer, times(1)).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(productKey),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE),
                eq(UNCHANGED_LICENSE_DESCRIPTION));
    }

    @Test
    public void saveLicenseInformationForPriceModel_oldLicensesForUserLocaleNotExistsNewLicenseEmpty() {
        // given
        final String EMPTY_LICENSE_DESCRIPTION = "";
        final String OLD_LICENSE_DESCRIPTION = "description";
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setLicense(EMPTY_LICENSE_DESCRIPTION);
        priceModel.setKey(0);
        List<VOLocalizedText> oldLicenses = new ArrayList<VOLocalizedText>();
        VOLocalizedText jpText = createVOLocalizedText("jp",
                OLD_LICENSE_DESCRIPTION, 0);
        oldLicenses.add(jpText);
        long productKey = 1L;
        long priceModelKey = 1L;
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("de");
        doReturn(oldLicenses).when(localizer).getLocalizedValues(
                eq(priceModel.getKey()),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE));

        // when
        boolean result = sps.saveLicenseInformationForPriceModel(productKey,
                priceModelKey, priceModel, currentUser, false);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
        verify(localizer, times(1)).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(productKey),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE),
                eq(EMPTY_LICENSE_DESCRIPTION));
        verify(localizer, times(1)).storeLocalizedResource(eq("jp"),
                eq(productKey), eq(LocalizedObjectTypes.PRICEMODEL_LICENSE),
                eq(OLD_LICENSE_DESCRIPTION));
    }

    @Test
    public void localizePriceModel_ChargeableAndDescriptionIsSame() {
        // given
        VOPriceModel priceModel = new VOPriceModel();
        PriceModel priceModelToStore = new PriceModel();
        long priceModelKey = 1L;
        priceModel.setKey(priceModelKey);
        priceModel.setType(PriceModelType.PER_UNIT);
        priceModelToStore.setType(PriceModelType.PER_UNIT);
        long priceModelToStoreKey = 2L;
        priceModelToStore.setKey(priceModelToStoreKey);
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("en");
        String currentDescription = "CURRENTDESCRIPTION";
        priceModel.setDescription(currentDescription);
        doReturn(currentDescription).when(localizer)
                .getLocalizedTextFromDatabase(currentUser.getLocale(),
                        priceModelKey,
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);

        // when
        boolean result = sps.localizePriceModel(priceModel, currentUser,
                priceModelToStore);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
        verify(localizer, never()).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(priceModelToStoreKey),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE),
                eq(priceModel.getDescription()));
        verify(localizer, never()).removeLocalizedValues(eq(priceModelKey),
                eq(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION));
    }

    @Test
    public void localizePriceModel_ChargeableAndDescriptionIsNotSame() {
        // given
        VOPriceModel priceModel = new VOPriceModel();
        PriceModel priceModelToStore = new PriceModel();
        long priceModelKey = 1L;
        priceModel.setKey(priceModelKey);
        priceModel.setType(PriceModelType.PER_UNIT);
        priceModelToStore.setType(PriceModelType.PER_UNIT);
        long priceModelToStoreKey = 2L;
        priceModelToStore.setKey(priceModelToStoreKey);
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("en");
        String currentDescription = "CURRENTDESCRIPTION";
        priceModel.setDescription("DESCRIPTION");
        doReturn(currentDescription).when(localizer)
                .getLocalizedTextFromDatabase(currentUser.getLocale(),
                        priceModelKey,
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);

        // when
        boolean result = sps.localizePriceModel(priceModel, currentUser,
                priceModelToStore);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
        verify(localizer, times(1)).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(priceModelToStoreKey),
                eq(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION),
                eq(priceModel.getDescription()));
        verify(localizer, never()).removeLocalizedValues(eq(priceModelKey),
                eq(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION));
    }

    @Test
    public void localizePriceModel_FreeOfChargeToChargeable() {
        // given
        VOPriceModel priceModel = new VOPriceModel();
        PriceModel priceModelToStore = new PriceModel();
        long priceModelKey = 1L;
        priceModel.setKey(priceModelKey);
        priceModel.setType(PriceModelType.PER_UNIT);
        priceModelToStore.setType(PriceModelType.FREE_OF_CHARGE);
        long priceModelToStoreKey = 2L;
        priceModelToStore.setKey(priceModelToStoreKey);
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("en");
        String currentDescription = "";
        priceModel.setDescription("DESCRIPTION");
        doReturn(currentDescription).when(localizer)
                .getLocalizedTextFromDatabase(currentUser.getLocale(),
                        priceModelKey,
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);

        // when
        boolean result = sps.localizePriceModel(priceModel, currentUser,
                priceModelToStore);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
        verify(localizer, times(1)).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(priceModelToStoreKey),
                eq(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION),
                eq(priceModel.getDescription()));
        verify(localizer, never()).removeLocalizedValues(eq(priceModelKey),
                eq(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION));
    }

    @Test
    public void localizePriceModel_FreeOfCharge() {
        // given
        VOPriceModel priceModel = new VOPriceModel();
        PriceModel priceModelToStore = new PriceModel();
        long priceModelKey = 1L;
        priceModel.setKey(priceModelKey);
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        priceModelToStore.setType(PriceModelType.FREE_OF_CHARGE);
        long priceModelToStoreKey = 2L;
        priceModelToStore.setKey(priceModelToStoreKey);
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("en");
        String currentDescription = "";
        priceModel.setDescription("");
        doReturn(currentDescription).when(localizer)
                .getLocalizedTextFromDatabase(currentUser.getLocale(),
                        priceModelKey,
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);

        // when
        boolean result = sps.localizePriceModel(priceModel, currentUser,
                priceModelToStore);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
        verify(localizer, never()).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(priceModelToStoreKey),
                eq(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION),
                eq(priceModel.getDescription()));
        verify(localizer, never()).removeLocalizedValues(eq(priceModelKey),
                eq(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION));
    }

    @Test
    public void localizePriceModel_DescriptionIsNull() {
        // given
        VOPriceModel priceModel = new VOPriceModel();
        PriceModel priceModelToStore = new PriceModel();
        long priceModelKey = 1L;
        priceModel.setKey(priceModelKey);
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        priceModelToStore.setType(PriceModelType.FREE_OF_CHARGE);
        long priceModelToStoreKey = 2L;
        priceModelToStore.setKey(priceModelToStoreKey);
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("en");
        String currentDescription = "";
        priceModel.setDescription(null);
        doReturn(currentDescription).when(localizer)
                .getLocalizedTextFromDatabase(currentUser.getLocale(),
                        priceModelKey,
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);

        // when
        boolean result = sps.localizePriceModel(priceModel, currentUser,
                priceModelToStore);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
        verify(localizer, never()).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(priceModelToStoreKey),
                eq(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION),
                eq(priceModel.getDescription()));
        verify(localizer, never()).removeLocalizedValues(eq(priceModelKey),
                eq(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION));
    }

    @Test
    public void savePriceModelLocalizationAsReseller_bug11573() {
        // given
        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        List<VOLocalizedText> licenses = new ArrayList<VOLocalizedText>();
        VOLocalizedText text = new VOLocalizedText();
        text.setText(null);
        text.setLocale("en");
        licenses.add(text);
        localization.setLicenses(licenses);
        // when
        spsLocalizer.savePriceModelLocalizationAsReseller(1L, localization);
        // then
        verify(localizer, times(1)).storeLocalizedResource(eq("en"), eq(1L),
                eq(LocalizedObjectTypes.RESELLER_PRICEMODEL_LICENSE), eq(""));
    }

    @Test
    public void saveLicenseInformationForPriceModelOldLicensesAndNewOne() {
        // given
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setLicense("license");
        List<VOLocalizedText> oldLicenses = Arrays.asList(new VOLocalizedText[]{givenLocalizedText("engText")});
        long productKey = 1L;
        long priceModelKey = 1L;
        PlatformUser currentUser = new PlatformUser();
        currentUser.setLocale("de");
        doReturn(oldLicenses).when(localizer).getLocalizedValues(
                eq(productKey), eq(LocalizedObjectTypes.PRODUCT_LICENSE_DESC));

        // when
        boolean result = sps.saveLicenseInformationForPriceModel(productKey,
                priceModelKey, priceModel, currentUser, true);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
        verify(localizer, times(1)).storeLocalizedResource(
                eq(currentUser.getLocale()), eq(productKey),
                eq(LocalizedObjectTypes.PRICEMODEL_LICENSE),
                eq(priceModel.getLicense()));
    }
}
