/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 24.11.2010                                                      
 *                                                                              
 *  Completion Time: 25.11.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Ignore;
import org.junit.Test;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.vo.*;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Categories;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PaymentTypes;
import org.oscm.test.data.Scenario;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.*;

import javax.ejb.EJBException;
import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author weiser
 * 
 */
@Ignore
public class ServiceProvisioningServiceBeanCopyIT extends EJBTestBase {

    private static final String[] ENABLED_PAYMENTTYPES = new String[] {
            PaymentType.CREDIT_CARD, PaymentType.INVOICE };

    private static final String NEW_SERVICEID = "newserviceid";

    private ServiceProvisioningService provisioningService;
    private LocalizerServiceLocal localizerService;
    private DataService dataManager;

    protected PlatformUser providerUser;
    protected PlatformUser supplierUser;
    protected PlatformUser customerUser;
    protected PlatformUser otherSupplierUser;

    private VOServiceDetails serviceToCopy;
    private CatalogEntry catalogEntry;

    @Override
    protected void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);

        container.login("1");
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(new ServiceProvisioningServiceLocalizationBean());
        container.addBean(mock(ImageResourceServiceLocal.class));
        container.addBean(new ApplicationServiceStub() {
            @Override
            public void validateCommunication(TechnicalProduct techProduct)
                    throws TechnicalServiceNotAliveException {
            }
        });
        container.addBean(new SessionServiceStub());
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LdapAccessServiceStub());
        container.addBean(new TaskQueueServiceStub());
        container.addBean(mock(IdentityServiceLocal.class));
        container.addBean(new PaymentServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(mock(AccountServiceLocal.class));
        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new TagServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());

        provisioningService = container.get(ServiceProvisioningService.class);
        dataManager = container.get(DataService.class);
        localizerService = container.get(LocalizerServiceLocal.class);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Scenario.setup(container, true);
                Organization po = Organizations
                        .createPlatformOperator(dataManager);
                PaymentTypes.enableForSupplier(po, Scenario.getSupplier(),
                        dataManager, true, true, ENABLED_PAYMENTTYPES);
                Organization provider = Organizations.createOrganization(
                        dataManager, OrganizationRoleType.TECHNOLOGY_PROVIDER);
                providerUser = Organizations.createUserForOrg(dataManager,
                        provider, true, "admin");
                Organization supplier = Organizations.createOrganization(
                        dataManager, OrganizationRoleType.SUPPLIER);
                otherSupplierUser = Organizations.createUserForOrg(dataManager,
                        supplier, true, "admin");
                supplierUser = Scenario.getSupplierAdminUser();
                customerUser = Scenario.getCustomerAdminUser();
                return null;
            }
        });

        VOService service = runTX(new Callable<VOService>() {

            @Override
            public VOService call() throws Exception {
                Product product = dataManager.getReference(Product.class,
                        Scenario.getProduct().getKey());
                catalogEntry = product.getCatalogEntries().get(0);
                Marketplace mp = dataManager.getReference(Marketplace.class,
                        Scenario.getLocalMarketplaceSupplier().getKey());
                for (int i = 0; i < 3; i++) {
                    Category cat = Categories
                            .create(dataManager, "Cat" + i, mp);
                    Categories.assignToCatalogEntry(dataManager, cat,
                            catalogEntry);
                }
                catalogEntry.setAnonymousVisible(true);
                catalogEntry.setVisibleInCatalog(true);
                dataManager.persist(catalogEntry);
                return ProductAssembler.toVOProduct(product,
                        new LocalizerFacade(localizerService, "en"));
            }
        });

        OrganizationReference orgRef = createOrgRef(supplierUser
                .getOrganization().getKey());
        createMarketingPermission(Scenario.getTechnicalProduct().getKey(),
                orgRef.getKey());

        container.login(supplierUser.getKey(), new String[] {
                ROLE_ORGANIZATION_ADMIN, ROLE_SERVICE_MANAGER });
        serviceToCopy = provisioningService.getServiceDetails(service);

    }

    private void createMarketingPermission(final long tpKey,
            final long orgRefKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct technicalProduct = dataManager.find(
                        TechnicalProduct.class, tpKey);
                OrganizationReference reference = dataManager.find(
                        OrganizationReference.class, orgRefKey);

                MarketingPermission permission = new MarketingPermission();
                permission.setOrganizationReference(reference);
                permission.setTechnicalProduct(technicalProduct);
                dataManager.persist(permission);
                return null;
            }
        });
    }

    private OrganizationReference createOrgRef(final long orgKey)
            throws Exception {
        return runTX(new Callable<OrganizationReference>() {
            @Override
            public OrganizationReference call() throws Exception {
                Organization organization = dataManager.find(
                        Organization.class, orgKey);
                OrganizationReference orgRef = new OrganizationReference(
                        organization,
                        organization,
                        OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
                dataManager.persist(orgRef);
                return orgRef;
            }
        });
    }

    @Test
    public void testCopy() throws Exception {
        serviceToCopy = saveLocalization(serviceToCopy);
        List<VOService> servicesBefore = provisioningService
                .getSuppliedServices();
        VOServiceDetails copy = provisioningService.copyService(serviceToCopy,
                NEW_SERVICEID);
        assertNotNull(copy);
        List<VOService> servicesAfter = provisioningService
                .getSuppliedServices();
        assertEquals(servicesBefore.size() + 1, servicesAfter.size());
        validate(serviceToCopy, copy);
        validateCatalogEntry(serviceToCopy, copy);
        validateCategories(serviceToCopy, copy);
        validate(provisioningService.loadImage(Long.valueOf(serviceToCopy
                .getKey())), provisioningService.loadImage(Long.valueOf(copy
                .getKey())));
        validateCopiedProductPaymentConfiguration(copy.getKey(),
                ENABLED_PAYMENTTYPES);
    }

    @Test
    public void testCopy_ModifyCatalogEntries() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                catalogEntry.setVisibleInCatalog(!catalogEntry
                        .isVisibleInCatalog());
                catalogEntry.setAnonymousVisible(!catalogEntry
                        .isAnonymousVisible());
                return null;
            }
        });

        VOServiceDetails copy = provisioningService.copyService(serviceToCopy,
                NEW_SERVICEID);
        assertNotNull(copy);
        validateCatalogEntry(serviceToCopy, copy);
    }

    @Test
    public void testCopy_WithoutCatalogEntry() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product p = dataManager.getReference(Product.class,
                        serviceToCopy.getKey());
                CatalogEntry ce = p.getCatalogEntries().get(0);
                dataManager.remove(ce);
                p.getCatalogEntries().clear();
                // increase version as product was just updated
                serviceToCopy.setVersion(serviceToCopy.getVersion() + 1);
                return null;
            }
        });
        serviceToCopy = saveLocalization(serviceToCopy);
        List<VOService> servicesBefore = provisioningService
                .getSuppliedServices();
        final VOServiceDetails copy = provisioningService.copyService(
                serviceToCopy, NEW_SERVICEID);
        assertNotNull(copy);
        List<VOService> servicesAfter = provisioningService
                .getSuppliedServices();
        assertEquals(servicesBefore.size() + 1, servicesAfter.size());
        validate(serviceToCopy, copy);
        validate(provisioningService.loadImage(Long.valueOf(serviceToCopy
                .getKey())), provisioningService.loadImage(Long.valueOf(copy
                .getKey())));
        validateCopiedProductPaymentConfiguration(copy.getKey(),
                ENABLED_PAYMENTTYPES);
        validateNoCategories(copy);
    }

    @Test
    public void testCopy_WithoutMarketplace() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product p = dataManager.getReference(Product.class,
                        serviceToCopy.getKey());
                CatalogEntry ce = p.getCatalogEntries().get(0);
                ce.setMarketplace(null);
                for (CategoryToCatalogEntry entry : ce
                        .getCategoryToCatalogEntry()) {
                    dataManager.remove(entry);
                }
                ce.getCategoryToCatalogEntry().clear();
                return null;
            }
        });
        serviceToCopy = saveLocalization(serviceToCopy);
        List<VOService> servicesBefore = provisioningService
                .getSuppliedServices();
        final VOServiceDetails copy = provisioningService.copyService(
                serviceToCopy, NEW_SERVICEID);
        assertNotNull(copy);
        List<VOService> servicesAfter = provisioningService
                .getSuppliedServices();
        assertEquals(servicesBefore.size() + 1, servicesAfter.size());
        validate(serviceToCopy, copy);
        validate(provisioningService.loadImage(Long.valueOf(serviceToCopy
                .getKey())), provisioningService.loadImage(Long.valueOf(copy
                .getKey())));
        validateCopiedProductPaymentConfiguration(copy.getKey(),
                ENABLED_PAYMENTTYPES);
        validateNoCategories(copy);
    }

    @Test
    public void testCopyWithLicenses() throws Exception {
        serviceToCopy = saveLocalization(serviceToCopy);
        final String licenseDescEn = "LICENSE_DESCRIPTION_EN";
        final String licenseDescDe = "LICENSE_DESCRIPTION_DE";
        final LocalizedObjectTypes type = LocalizedObjectTypes.PRICEMODEL_LICENSE;
        final long pmKey = serviceToCopy.getPriceModel().getKey();
        setLocalizedResource(licenseDescEn, "en", type, pmKey);
        setLocalizedResource(licenseDescDe, "de", type, pmKey);
        final VOServiceDetails copy = provisioningService.copyService(
                serviceToCopy, NEW_SERVICEID);
        assertNotNull(copy);
        validateCopiedProductPaymentConfiguration(copy.getKey(),
                ENABLED_PAYMENTTYPES);

        String license1 = getLocalizedResource("en", type, pmKey);
        String license2 = getLocalizedResource("en", type, copy.getPriceModel()
                .getKey());
        assertEquals("Wrong license description EN locale.", licenseDescEn,
                license1);
        assertEquals("Wrong license description EN locale.", license1, license2);

        license1 = getLocalizedResource("de", type, pmKey);
        license2 = getLocalizedResource("de", type, copy.getPriceModel()
                .getKey());
        assertEquals("Wrong license description for DE locale.", licenseDescDe,
                license1);
        assertEquals("Wrong license description for DE locale.", license1,
                license2);

    }

    @Test
    public void testCopyWithShortDescription() throws Exception {
        serviceToCopy = saveLocalization(serviceToCopy);
        final String shortDescEn = "SHORT_DESCRIPTION_EN";
        final String shortDescDe = "SHORT_DESCRIPTION_DE";

        final LocalizedObjectTypes type = LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION;
        final long svcKey = serviceToCopy.getKey();
        setLocalizedResource(shortDescEn, "en", type, svcKey);
        setLocalizedResource(shortDescDe, "de", type, svcKey);

        final VOServiceDetails copy = provisioningService.copyService(
                serviceToCopy, NEW_SERVICEID);
        assertNotNull(copy);
        validateCopiedProductPaymentConfiguration(copy.getKey(),
                ENABLED_PAYMENTTYPES);

        String short1 = getLocalizedResource("en", type, svcKey);
        String short2 = getLocalizedResource("en", type, copy.getKey());
        assertEquals("Wrong short description EN locale.", shortDescEn, short1);
        assertEquals("Wrong short description EN locale.", short1, short2);

        short1 = getLocalizedResource("de", type, svcKey);
        short2 = getLocalizedResource("de", type, copy.getKey());
        assertEquals("Wrong short description for DE locale.", shortDescDe,
                short1);
        assertEquals("Wrong short description for DE locale.", short1, short2);
    }

    @Test
    public void testCopy_WithoutResources() throws Exception {
        List<VOService> servicesBefore = provisioningService
                .getSuppliedServices();
        VOServiceDetails copy = provisioningService.copyService(serviceToCopy,
                NEW_SERVICEID);
        assertNotNull(copy);
        List<VOService> servicesAfter = provisioningService
                .getSuppliedServices();
        assertEquals(servicesBefore.size() + 1, servicesAfter.size());
        validate(serviceToCopy, copy);
        validateCategories(serviceToCopy, copy);
        assertNull(provisioningService.loadImage(Long.valueOf(serviceToCopy
                .getKey())));
        assertNull(provisioningService.loadImage(Long.valueOf(copy.getKey())));
    }

    @Test(expected = EJBException.class)
    public void testCopy_AsProvider() throws Exception {
        container.login(providerUser.getKey(), ROLE_TECHNOLOGY_MANAGER);
        provisioningService.copyService(serviceToCopy, NEW_SERVICEID);
    }

    @Test(expected = EJBException.class)
    public void testCopy_AsCustomer() throws Exception {
        container.login(customerUser.getKey());
        provisioningService.copyService(serviceToCopy, NEW_SERVICEID);
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testCopy_NonUniqueId() throws Exception {
        provisioningService.copyService(serviceToCopy,
                serviceToCopy.getServiceId());
    }

    @Test(expected = ValidationException.class)
    public void testCopy_NullId() throws Exception {
        provisioningService.copyService(serviceToCopy, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCopy_NullService() throws Exception {
        try {
            provisioningService.copyService(null, NEW_SERVICEID);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testCopy_NotExistingService() throws Exception {
        provisioningService.copyService(new VOService(), NEW_SERVICEID);
    }

    @Test(expected = ValidationException.class)
    public void testCopy_InvalidId() throws Exception {
        provisioningService.copyService(serviceToCopy, "  some id   ");
    }

    @Test(expected = ValidationException.class)
    public void testCopy_ToLongId() throws Exception {
        provisioningService.copyService(serviceToCopy, TOO_LONG_ID);
    }

    @Test(expected = EJBException.class)
    public void testCopy_NotOwner() throws Exception {
        container.login(otherSupplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        provisioningService.copyService(serviceToCopy, NEW_SERVICEID);
    }

    @Test(expected = EJBException.class)
    public void testCopy_CustomerSpecific() throws Exception {
        container.login(otherSupplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        provisioningService.copyService(getCustomerSpecificService(),
                NEW_SERVICEID);
    }

    @Test(expected = EJBException.class)
    public void testCopy_SubscriptionSpecific() throws Exception {
        container.login(otherSupplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        provisioningService.copyService(getSubscriptionSpecificService(),
                NEW_SERVICEID);
    }

    @Test(expected = ServiceStateException.class)
    public void testCopy_DeletedState() throws Exception {
        setProductStatus(ServiceStatus.DELETED);
        provisioningService.copyService(serviceToCopy, NEW_SERVICEID);
    }

    @Test(expected = ServiceStateException.class)
    public void testCopy_ObsoleteState() throws Exception {
        setProductStatus(ServiceStatus.OBSOLETE);
        provisioningService.copyService(serviceToCopy, NEW_SERVICEID);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testCopy_ConcurrentChange() throws Exception {
        setProductStatus(ServiceStatus.ACTIVE);
        provisioningService.copyService(serviceToCopy, NEW_SERVICEID);
    }

    private void setProductStatus(final ServiceStatus state) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product product = Scenario.getProduct();
                product = dataManager.getReference(Product.class,
                        product.getKey());
                product.setStatus(state);
                return null;
            }
        });
    }

    private void validateCatalogEntry(final VOServiceDetails template,
            final VOServiceDetails copy) throws Exception {
        try {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Product c1 = dataManager.getReference(Product.class,
                            template.getKey());
                    Product c2 = dataManager.getReference(Product.class,
                            copy.getKey());
                    assertEquals(1, c1.getCatalogEntries().size());
                    assertEquals(1, c2.getCatalogEntries().size());
                    CatalogEntry cCe1 = c1.getCatalogEntries().get(0);
                    CatalogEntry cCe2 = c2.getCatalogEntries().get(0);
                    assertTrue(cCe1.isAnonymousVisible() == cCe2
                            .isAnonymousVisible());
                    assertTrue(cCe1.isVisibleInCatalog() == cCe2
                            .isVisibleInCatalog());
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    private static void validate(VOServiceDetails template,
            VOServiceDetails copy) {
        assertEquals(ServiceStatus.INACTIVE, copy.getStatus());
        assertEquals(template.isAutoAssignUserEnabled(),
                copy.isAutoAssignUserEnabled());
        assertTrue(template.getKey() != copy.getKey());
        assertEquals(template.getDescription(), copy.getDescription());
        assertEquals(template.getFeatureURL(), copy.getFeatureURL());
        assertEquals(template.getPriceModel().getLicense(), copy
                .getPriceModel().getLicense());
        assertEquals(template.getName(), copy.getName());
        assertEquals(template.getConfiguratorUrl(), copy.getConfiguratorUrl());

        int size = template.getParameters().size();
        assertEquals(size, copy.getParameters().size());
        for (int index = 0; index < size; index++) {
            validate(template.getParameters().get(index), copy.getParameters()
                    .get(index));
        }
        validate(template.getPriceModel(), copy.getPriceModel());

    }

    private static void validate(VOPriceModel template, VOPriceModel copy) {
        assertTrue(template.getKey() != copy.getKey());
        assertEquals(template.getDescription(), copy.getDescription());
        assertEquals(template.getCurrency(), copy.getCurrency());
        assertEquals(template.getCurrencyISOCode(), copy.getCurrencyISOCode());
        assertEquals(template.getOneTimeFee(), copy.getOneTimeFee());
        assertEquals(template.getPricePerPeriod(), copy.getPricePerPeriod());
        assertEquals(template.getPricePerUserAssignment(),
                copy.getPricePerUserAssignment());
        assertEquals(template.getPeriod(), copy.getPeriod());

        int size = template.getConsideredEvents().size();
        assertEquals(size, copy.getConsideredEvents().size());
        for (int index = 0; index < size; index++) {
            validate(template.getConsideredEvents().get(index), copy
                    .getConsideredEvents().get(index));
        }
        size = template.getRoleSpecificUserPrices().size();
        assertEquals(size, copy.getRoleSpecificUserPrices().size());
        for (int index = 0; index < size; index++) {
            validate(template.getRoleSpecificUserPrices().get(index), copy
                    .getRoleSpecificUserPrices().get(index));
        }
        size = template.getSelectedParameters().size();
        assertEquals(size, copy.getSelectedParameters().size());
        for (int index = 0; index < size; index++) {
            validate(template.getSelectedParameters().get(index), copy
                    .getSelectedParameters().get(index));
        }
        size = template.getSteppedPrices().size();
        assertEquals(size, copy.getSteppedPrices().size());
        for (int index = 0; index < size; index++) {
            validate(template.getSteppedPrices().get(index), copy
                    .getSteppedPrices().get(index));
        }

    }

    private static void validate(VOSteppedPrice template, VOSteppedPrice copy) {
        assertTrue(template.getKey() != copy.getKey());
        assertEquals(template.getPrice(), copy.getPrice());
        assertEquals(template.getLimit(), copy.getLimit());
    }

    private static void validate(VOPricedParameter template,
            VOPricedParameter copy) {
        assertTrue(template.getKey() != copy.getKey());
        assertTrue(template.getParameterKey() != copy.getParameterKey());
        assertEquals(template.getPricePerSubscription(),
                copy.getPricePerSubscription());
        assertEquals(template.getPricePerUser(), copy.getPricePerUser());

        int size = template.getSteppedPrices().size();
        assertEquals(size, copy.getSteppedPrices().size());
        for (int index = 0; index < size; index++) {
            validate(template.getSteppedPrices().get(index), copy
                    .getSteppedPrices().get(index));
        }
        size = template.getRoleSpecificUserPrices().size();
        assertEquals(size, copy.getRoleSpecificUserPrices().size());
        for (int index = 0; index < size; index++) {
            validate(template.getRoleSpecificUserPrices().get(index), copy
                    .getRoleSpecificUserPrices().get(index));
        }

        size = template.getPricedOptions().size();
        assertEquals(size, copy.getPricedOptions().size());
        for (int index = 0; index < size; index++) {
            validate(template.getPricedOptions().get(index), copy
                    .getPricedOptions().get(index));
        }

    }

    private static void validate(VOPricedOption template, VOPricedOption copy) {
        assertTrue(template.getKey() != copy.getKey());
        assertEquals(template.getParameterOptionKey(),
                copy.getParameterOptionKey());
        assertEquals(template.getPricePerSubscription(),
                copy.getPricePerSubscription());
        assertEquals(template.getPricePerUser(), copy.getPricePerUser());

        int size = template.getRoleSpecificUserPrices().size();
        assertEquals(size, copy.getRoleSpecificUserPrices().size());
        for (int index = 0; index < size; index++) {
            validate(template.getRoleSpecificUserPrices().get(index), copy
                    .getRoleSpecificUserPrices().get(index));
        }
    }

    private static void validate(VOPricedRole template, VOPricedRole copy) {
        assertTrue(template.getKey() != copy.getKey());
        assertEquals(template.getPricePerUser(), copy.getPricePerUser());
        assertEquals(template.getRole().getKey(), copy.getRole().getKey());
    }

    private static void validate(VOPricedEvent template, VOPricedEvent copy) {
        assertTrue(template.getKey() != copy.getKey());
        assertEquals(template.getEventPrice(), copy.getEventPrice());
        assertEquals(template.getEventDefinition().getKey(), copy
                .getEventDefinition().getKey());

        int size = template.getSteppedPrices().size();
        assertEquals(size, copy.getSteppedPrices().size());
        for (int index = 0; index < size; index++) {
            validate(template.getSteppedPrices().get(index), copy
                    .getSteppedPrices().get(index));
        }
    }

    private static void validate(VOParameter template, VOParameter copy) {
        assertTrue(template.getKey() != copy.getKey());
        assertEquals(template.getValue(), copy.getValue());
        assertEquals(template.getParameterDefinition().getKey(), copy
                .getParameterDefinition().getKey());
    }

    private static void validate(VOImageResource template, VOImageResource copy) {
        assertEquals(template.getContentType(), copy.getContentType());
        assertArrayEquals(template.getBuffer(), copy.getBuffer());
        assertEquals(template.getImageType(), copy.getImageType());
    }

    private VOServiceDetails saveLocalization(VOServiceDetails service)
            throws Exception {
        VOServiceLocalization serviceLoc = new VOServiceLocalization();
        ArrayList<VOLocalizedText> names = new ArrayList<VOLocalizedText>();
        names.add(new VOLocalizedText("en", "english service name", 0));
        names.add(new VOLocalizedText("de", "deutscher service name"));
        serviceLoc.setNames(names);

        ArrayList<VOLocalizedText> descriptions = new ArrayList<VOLocalizedText>();
        descriptions.add(new VOLocalizedText("en",
                "english service description", 0));
        descriptions.add(new VOLocalizedText("de",
                "deutsche service beschreibung"));
        serviceLoc.setDescriptions(descriptions);
        provisioningService.saveServiceLocalization(service, serviceLoc);

        VOPriceModelLocalization priceModelLoc = new VOPriceModelLocalization();
        descriptions = new ArrayList<VOLocalizedText>();
        descriptions.add(new VOLocalizedText("en", "english pm description"));
        descriptions.add(new VOLocalizedText("de", "deutsche pm beschreibung"));
        priceModelLoc.setDescriptions(new ArrayList<VOLocalizedText>());

        descriptions = new ArrayList<VOLocalizedText>();
        descriptions
                .add(new VOLocalizedText("en", "english short description"));
        descriptions
                .add(new VOLocalizedText("de", "deutsche kurzbeschreibung"));
        provisioningService.savePriceModelLocalization(service.getPriceModel(),
                priceModelLoc);

        VOImageResource ir = new VOImageResource();
        ir.setBuffer(BaseAdmUmTest.getFileAsByteArray(
                ServiceProvisioningServiceBeanIT.class, "icon1.png"));
        ir.setContentType("image/jpg");
        ir.setImageType(ImageType.SERVICE_IMAGE);
        provisioningService.updateService(service, ir);

        return provisioningService.getServiceDetails(service);
    }

    private VOService getSubscriptionSpecificService() throws Exception {
        VOService service = runTX(new Callable<VOService>() {

            @Override
            public VOService call() throws Exception {
                Subscription sub = Scenario.getSubscription();
                sub = dataManager
                        .getReference(Subscription.class, sub.getKey());
                return ProductAssembler.toVOProduct(sub.getProduct(),
                        new LocalizerFacade(localizerService, "en"));
            }
        });
        return service;
    }

    private VOService getCustomerSpecificService() throws Exception {
        VOOrganization cust = Scenario.getVoCustomer();
        return provisioningService.getServiceForCustomer(cust, serviceToCopy);
    }

    private void validateCopiedProductPaymentConfiguration(final long key,
            String... payments) throws Exception {
        final Set<String> ptIds = new HashSet<String>(Arrays.asList(payments));
        try {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Product prod = dataManager.getReference(Product.class, key);
                    List<ProductToPaymentType> types = prod.getPaymentTypes();
                    for (ProductToPaymentType t : types) {
                        assertTrue(ptIds.remove(t.getPaymentType()
                                .getPaymentTypeId()));
                    }
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
        assertTrue(ptIds.isEmpty());
    }

    private void setLocalizedResource(final String value, final String locale,
            final LocalizedObjectTypes type, final long objKey)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizerService.storeLocalizedResource(locale, objKey, type,
                        value);
                return null;
            }
        });
    }

    private String getLocalizedResource(final String locale,
            final LocalizedObjectTypes type, final long objKey)
            throws Exception {
        return runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizerService.getLocalizedTextFromDatabase(locale,
                        objKey, type);
            }
        });
    }

    private void validateCategories(final VOService template,
            final VOService copy) throws Exception {
        try {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Product t = dataManager.getReference(Product.class,
                            template.getKey());
                    Product c = dataManager.getReference(Product.class,
                            copy.getKey());
                    assertEquals(1, t.getCatalogEntries().size());
                    assertEquals(1, c.getCatalogEntries().size());
                    CatalogEntry tCe = t.getCatalogEntries().get(0);
                    CatalogEntry cCe = c.getCatalogEntries().get(0);
                    assertEquals(tCe.getMarketplace(), cCe.getMarketplace());
                    List<CategoryToCatalogEntry> tCats = tCe
                            .getCategoryToCatalogEntry();
                    List<CategoryToCatalogEntry> cCats = cCe
                            .getCategoryToCatalogEntry();
                    assertEquals(tCats.size(), cCats.size());
                    Set<Category> tmp = new HashSet<Category>();
                    for (CategoryToCatalogEntry entry : tCats) {
                        tmp.add(entry.getCategory());
                    }
                    for (CategoryToCatalogEntry entry : cCats) {
                        assertTrue(tmp.remove(entry.getCategory()));
                    }
                    assertTrue(tmp.isEmpty());
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    private void validateNoCategories(final VOServiceDetails copy)
            throws Exception {
        try {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Product c = dataManager.getReference(Product.class,
                            copy.getKey());
                    assertEquals(1, c.getCatalogEntries().size());
                    CatalogEntry cCe = c.getCatalogEntries().get(0);
                    assertTrue(cCe.getCategoryToCatalogEntry().isEmpty());
                    assertNull(cCe.getMarketplace());
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

}
