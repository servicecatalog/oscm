/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                                  
 *                                                                              
 *  Creation Date: 07.12.2011                                                      
 *                                                                              
 *  Completion Time: 07.12.2011                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.BigDecimalComparator;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException.Reason;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.*;
import org.oscm.serviceprovisioningservice.auditlog.ServiceAuditLogCollector;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.setup.ProductImportParser;
import org.oscm.test.stubs.*;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;

import javax.ejb.EJBException;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.oscm.test.Numbers.BD100;

/**
 * @author kulle
 */
@SuppressWarnings("deprecation")
public class ServiceProvisioningServiceBean3IT extends EJBTestBase {

    private static final String[] ENABLED_PAYMENTTYPES = new String[] {
            PaymentType.CREDIT_CARD, PaymentType.INVOICE };
    private static final String EXAMPLE_ENTERPRISE = "EXAMPLE Enterprise";
    private static final String EXAMPLE_PROFESSIONAL = "EXAMPLE Professional";
    private static final String EXAMPLE_STARTER = "EXAMPLE Starter";
    private static final String EXAMPLE_TRIAL = "EXAMPLE Trial";
    private static final String ANY_MARKETPLACE_ID = "anyMarketplaceId";

    private ServiceProvisioningService svcProv;
    private DataService mgr;
    private LocalizerServiceLocal localizer;
    private IdentityService is;

    private final Map<String, VOService> COMPARE_VALUES = new HashMap<String, VOService>();

    private String providerOrgId;
    private String supplierOrgId;
    private String customerOrgId;
    private long providerUserKey;
    private long supplierUserKey;
    protected boolean appNotAlive;
    private Organization provider;
    private Organization supplier;
    private Organization customer;
    private Organization orgToReturnForTriggerProcessing;
    private static final String EUR = "EUR";
    private OrganizationReference orgRef;
    private final Random random = new Random();

    protected List<TechnicalProduct> marketingPermServ_getTechnicalProducts = new ArrayList<TechnicalProduct>();
    private MarketingPermissionServiceLocal marketingPermissionSvcMock;
    private ServiceAuditLogCollector audit;

    @Override
    public void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(new ApplicationServiceStub() {
            @Override
            public void validateCommunication(TechnicalProduct techProduct)
                    throws TechnicalServiceNotAliveException {
                if (appNotAlive) {
                    throw new TechnicalServiceNotAliveException(
                            Reason.CONNECTION_REFUSED);
                }
            }
        });
        container.addBean(new ImageResourceServiceStub() {
            ImageResource saved;

            @Override
            public ImageResource read(long objectKey, ImageType imageType) {
                if (saved != null && saved.getObjectKey() == objectKey
                        && saved.getImageType() == imageType) {
                    return saved;
                }
                return null;
            }

            @Override
            public void save(ImageResource imageResource) {
                saved = imageResource;
            }

            @Override
            public void delete(long objectKey, ImageType imageType) {
                saved = null;
            }
        });
        container.addBean(new CommunicationServiceStub());
        container.addBean(mock(IdentityServiceLocal.class));
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                TriggerProcess tp = new TriggerProcess();
                PlatformUser user = new PlatformUser();
                user.setOrganization(orgToReturnForTriggerProcessing);
                tp.setUser(user);

                TriggerProcessMessageData data = new TriggerProcessMessageData(
                        tp, null);
                return Collections.singletonList(data);
            }

        });
        marketingPermissionSvcMock = mock(MarketingPermissionServiceLocal.class);
        container.addBean(marketingPermissionSvcMock);
        audit = mock(ServiceAuditLogCollector.class);
        container.addBean(audit);
        container.addBean(mock(AccountServiceLocal.class));
        container.addBean(new TagServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceLocalizationBean());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(mock(MarketplaceService.class));

        is = container.get(IdentityService.class);
        setUpDirServerStub(container.get(ConfigurationServiceLocal.class));
        svcProv = container.get(ServiceProvisioningService.class);
        mgr = container.get(DataService.class);
        localizer = container.get(LocalizerServiceLocal.class);

        COMPARE_VALUES.put(EXAMPLE_TRIAL, getFreeProduct(EXAMPLE_TRIAL));
        COMPARE_VALUES.put(EXAMPLE_STARTER, getFreeProduct(EXAMPLE_STARTER));
        COMPARE_VALUES.put(EXAMPLE_PROFESSIONAL, getProfessional());
        COMPARE_VALUES.put(EXAMPLE_ENTERPRISE, getEnterprise());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance(EUR));
                mgr.persist(sc);
                createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                BillingAdapters.createBillingAdapter(mgr,
                        BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                        true);
                return null;
            }
        });

        provider = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization po = Organizations.createPlatformOperator(mgr);
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                Marketplaces.ensureMarketplace(organization,
                        organization.getOrganizationId(), mgr);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        organization, true, "admin");
                PaymentTypes.enableForSupplier(po, organization, mgr, true,
                        true, ENABLED_PAYMENTTYPES);
                providerUserKey = user.getKey();
                return organization;
            }
        });
        providerOrgId = provider.getOrganizationId();
        orgToReturnForTriggerProcessing = provider;

        supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.SUPPLIER);
                Marketplaces.ensureMarketplace(organization,
                        organization.getOrganizationId(), mgr);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        organization, true, "admin");
                supplierUserKey = user.getKey();
                Organization provider = Organizations.findOrganization(mgr,
                        providerOrgId);
                orgRef = Organizations
                        .createSupplierToTechnologyProviderReference(mgr,
                                provider, organization);
                return organization;
            }
        });
        supplierOrgId = supplier.getOrganizationId();

        customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier = Organizations.findOrganization(mgr,
                        supplierOrgId);
                Organization organization = Organizations.createCustomer(mgr,
                        supplier);
                Organizations
                        .createUserForOrg(mgr, organization, true, "admin");
                return organization;
            }
        });
        customerOrgId = customer.getOrganizationId();
        container.login(providerUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        doAnswer(new Answer<List<TechnicalProduct>>() {
            @Override
            public List<TechnicalProduct> answer(InvocationOnMock invocation) {
                return marketingPermServ_getTechnicalProducts;
            }
        }).when(marketingPermissionSvcMock).getTechnicalServicesForSupplier(
                Matchers.any(Organization.class));
    }

    protected static VOServiceDetails createProduct(
            VOTechnicalService technicalProduct, String id,
            ServiceProvisioningService serviceProvisioning) throws Exception {
        VOService product = new VOService();
        product.setServiceId(id);
        return serviceProvisioning.createService(technicalProduct, product,
                null);
    }

    private void createMarketingPermission(final long tpKey,
            final long orgRefKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct technicalProduct = mgr.find(
                        TechnicalProduct.class, tpKey);
                OrganizationReference reference = mgr.find(
                        OrganizationReference.class, orgRefKey);

                MarketingPermission permission = new MarketingPermission();
                permission.setOrganizationReference(reference);
                permission.setTechnicalProduct(technicalProduct);
                mgr.persist(permission);
                return null;
            }
        });
    }

    private OrganizationReference createOrgRef(final long orgKey)
            throws Exception {
        return runTX(new Callable<OrganizationReference>() {
            @Override
            public OrganizationReference call() throws Exception {
                Organization organization = mgr
                        .find(Organization.class, orgKey);
                OrganizationReference orgRef = new OrganizationReference(
                        organization,
                        organization,
                        OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
                mgr.persist(orgRef);
                return orgRef;
            }
        });
    }

    private static VOService getEnterprise() {
        VOService product = new VOService();
        product.setServiceId(EXAMPLE_ENTERPRISE);
        product.setName("");
        product.setDescription("");
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setDescription("");
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setPricePerPeriod(BD100);
        product.setPriceModel(priceModel);
        return product;
    }

    private static VOService getProfessional() {
        VOService product = new VOService();
        product.setServiceId(EXAMPLE_PROFESSIONAL);
        product.setName("");
        product.setDescription("");
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setDescription("");
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setPricePerPeriod(BigDecimal.ZERO);
        product.setPriceModel(priceModel);
        return product;
    }

    private VOOrganization getOrganizationForOrgId(final String organizationId)
            throws Exception {
        return runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() {
                Organization result = new Organization();
                result.setOrganizationId(organizationId);
                result = (Organization) mgr.find(result);
                return OrganizationAssembler.toVOOrganization(result, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });
    }

    protected void validateCopiedProductPaymentConfiguration(final long key,
            String... payments) throws Exception {
        final Set<String> ptIds = new HashSet<String>(Arrays.asList(payments));
        try {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Product prod = mgr.getReference(Product.class, key);
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

    private VOTechnicalService createTechnicalProduct(
            ServiceProvisioningService serviceProvisioning) throws Exception {
        return createTechnicalProduct(serviceProvisioning,
                TECHNICAL_SERVICES_XML);
    }

    private VOTechnicalService createTechnicalProduct(
            ServiceProvisioningService serviceProvisioning, String xml)
            throws Exception {
        String rc = serviceProvisioning.importTechnicalServices(xml
                .getBytes("UTF-8"));
        assertEquals("", rc);
        return serviceProvisioning.getTechnicalServices(
                OrganizationRoleType.TECHNOLOGY_PROVIDER).get(0);
    }

    private static VOService getFreeProduct(String id) {
        VOService product = new VOService();
        product.setServiceId(id);
        product.setName("");
        product.setDescription("");
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setDescription("");
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        product.setPriceModel(priceModel);
        return product;
    }

    /**
     * Initializes a technical product with four parameter definitions, two of
     * type {@link ParameterType#PLATFORM_PARAMETER} and two of type
     * {@link ParameterType#SERVICE_PARAMETER}. For each category, one is
     * configurable and one is not.
     * 
     * @return The created product based on the created technical product.
     * 
     * @throws Exception
     */
    private Product initTechnicalProductAndProductForParamDefTesting()
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider, "tpId", false, ServiceAccessType.LOGIN);
                marketingPermServ_getTechnicalProducts.add(tp);
                ParameterDefinition pd1 = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.LONG,
                                "count", ParameterType.PLATFORM_PARAMETER, tp,
                                mgr, null, null, true);
                TechnicalProducts.addParameterDefinition(
                        ParameterValueType.LONG, "countNC",
                        ParameterType.PLATFORM_PARAMETER, tp, mgr, null, null,
                        false);
                ParameterDefinition pd3 = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.LONG,
                                "serviceCount",
                                ParameterType.SERVICE_PARAMETER, tp, mgr, null,
                                null, true);
                TechnicalProducts.addParameterDefinition(
                        ParameterValueType.LONG, "serviceCountNC",
                        ParameterType.SERVICE_PARAMETER, tp, mgr, null, null,
                        false);

                Product prod = Products.createProduct(supplier, tp, false,
                        "prodId", null, mgr);
                Products.createParameter(pd1, prod, mgr);
                Products.createParameter(pd3, prod, mgr);
                return prod;
            }
        });
    }

    /**
     * Compares the localized description of the technical service with the ones
     * form the service except in the locale that has to be ignored.
     * 
     * @param expected
     * @param localization
     * @param localeToIgnore
     */
    private static void compareDescriptions(List<VOLocalizedText> expected,
            VOServiceLocalization localization, String localeToIgnore) {
        Map<String, String> localeToDesc = new HashMap<String, String>();
        for (VOLocalizedText txt : localization.getDescriptions()) {
            localeToDesc.put(txt.getLocale(), txt.getText());
        }
        for (VOLocalizedText txt : expected) {
            String locale = txt.getLocale();
            if (!localeToIgnore.equals(locale)) {
                String text = localeToDesc.get(locale);
                assertNotNull(text);
                assertEquals(txt.getText(), text);
            }
        }
    }

    protected String importProduct(final String xml, final DataService mgr)
            throws Exception {
        return runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                ProductImportParser parser = new ProductImportParser(mgr, mgr
                        .getCurrentUser().getOrganization());
                parser.parse(xml.getBytes("UTF-8"));
                return null;
            }
        });
    }

    private VOPriceModel createChargeablePriceModel() {
        return createChargeablePriceModel(EUR);
    }

    private VOPriceModel createChargeablePriceModel(String usedCurrency) {
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode(usedCurrency);
        priceModel.setPeriod(PricingPeriod.MONTH);
        return priceModel;
    }

    private <T extends DomainObject<?>> T getDOFromServer(
            final Class<T> objTypeIndicator, final long key) throws Exception {
        return runTX(new Callable<T>() {
            @Override
            public T call() {
                return mgr.find(objTypeIndicator, key);
            }
        });
    }

    protected VOPriceModel createPriceModel() {
        VOPriceModel priceModel = new VOPriceModel();
        return priceModel;
    }

    protected void setProductStatus(final ServiceStatus status, final long key)
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product prod = mgr.getReference(Product.class, key);
                prod.setStatus(status);
                return null;
            }
        });
    }

    @Test
    public void createMarketingProductForExternal() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        svcProv.deleteTechnicalService(tp);
        tp.setAccessType(ServiceAccessType.EXTERNAL);
        tp.setKey(0);
        tp = svcProv.createTechnicalService(tp);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());
        try {
            VOServiceDetails svc = svcProv.createService(tp, product, null);
            validateCopiedProductPaymentConfiguration(svc.getKey(),
                    ENABLED_PAYMENTTYPES);

            // is price model created and not chargeable?
            assertFalse(svc.getPriceModel().isChargeable());
            assertTrue(svc.getPriceModel().getKey() > 0L);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void createTechnicalService_Direct() throws Exception {
        // given
        VOTechnicalService tp = new VOTechnicalService();
        tp.setTechnicalServiceId("example");
        tp.setAccessType(ServiceAccessType.DIRECT);
        tp.setAccessInfo("accessInfo");
        tp.setProvisioningUrl("http://estadmue:8089/example-dev/services/ProvisioningService?wsdl");
        tp.setBillingIdentifier("BillingId");
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                mgr.getCurrentUser().setLocale("de");
                return null;
            }

        });
        // when
        final VOTechnicalService technicalService = svcProv
                .createTechnicalService(tp);
        // then
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Query resource = mgr
                        .createNamedQuery("LocalizedResource.findByBusinessKey");
                resource.setParameter("objectKey",
                        Long.valueOf(technicalService.getKey()));
                resource.setParameter("locale", "en");
                resource.setParameter("objectType",
                        LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC);
                Assert.assertEquals(resource.getResultList().size(), 1);
                resource.setParameter("locale", "de");
                Assert.assertEquals(resource.getResultList().size(), 0);
                return null;
            }
        });

    }

    @Test
    public void createServiceForParamDefHandling() throws Exception {
        initTechnicalProductAndProductForParamDefTesting();
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        List<VOTechnicalService> services = svcProv
                .getTechnicalServices(OrganizationRoleType.SUPPLIER);
        container.login(providerUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService tp = services.get(0);
        VOService serviceToCreate = new VOService();
        serviceToCreate.setServiceId("service");
        List<VOParameter> parameters = new ArrayList<VOParameter>();
        for (VOParameterDefinition paramDef : tp.getParameterDefinitions()) {
            VOParameter param = new VOParameter();
            param.setConfigurable(true);
            param.setValue("123");
            param.setParameterDefinition(paramDef);
            parameters.add(param);
        }
        serviceToCreate.setParameters(parameters);
        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());
        VOServiceDetails serviceDetails = svcProv.createService(tp,
                serviceToCreate, null);
        validateCopiedProductPaymentConfiguration(serviceDetails.getKey(),
                ENABLED_PAYMENTTYPES);

        List<VOParameterDefinition> pds = serviceDetails.getTechnicalService()
                .getParameterDefinitions();
        assertNotNull(pds);
        assertEquals(2, pds.size());
    }

    @Test
    public void createAndUpdateMarketingProduct_shortDescription()
            throws Exception {

        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService product = new VOService();
        // Mandatory fields
        product.setDescription("marketingdesc");
        product.setServiceId("modifiedProductIdIdentical");
        product.setShortDescription("Service short description");
        product.setAutoAssignUserEnabled(Boolean.TRUE);
        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        VOService createdProduct = svcProv.createService(tp, product, null);
        validateCopiedProductPaymentConfiguration(createdProduct.getKey(),
                ENABLED_PAYMENTTYPES);

        VOServiceDetails productDetails = svcProv
                .getServiceDetails(createdProduct);

        List<VOLocalizedText> expected = runTX(new Callable<List<VOLocalizedText>>() {

            @Override
            public List<VOLocalizedText> call() {
                List<VOLocalizedText> list = localizer.getLocalizedValues(
                        tp.getKey(),
                        LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC);
                return list;
            }

        });
        VOServiceLocalization localization = svcProv
                .getServiceLocalization(productDetails);
        String localeToIgnore = is.getCurrentUserDetails().getLocale();
        compareDescriptions(expected, localization, localeToIgnore);

        assertEquals("Wrong product id", "modifiedProductIdIdentical",
                productDetails.getServiceId());
        assertEquals("Wrong product description", "marketingdesc",
                productDetails.getDescription());
        assertEquals("Wrong product short description",
                "Service short description",
                productDetails.getShortDescription());

        productDetails.setShortDescription("newShortDesc");

        VOServiceDetails updateMarketingProduct = svcProv.updateService(
                productDetails, null);

        assertEquals("Wrong product id", "modifiedProductIdIdentical",
                updateMarketingProduct.getServiceId());
        assertEquals("Wrong product description", "marketingdesc",
                updateMarketingProduct.getDescription());
        assertEquals("Wrong product short description", "newShortDesc",
                updateMarketingProduct.getShortDescription());
        verify(audit, times(1)).updateService(any(DataService.class),
                any(Product.class), eq(true), eq(false), eq(false), anyString());
    }

    @Test
    public void testCreateAndUpdateMarketingProduct_productName()
            throws Exception {

        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService product = new VOService();
        // Mandatory fields
        product.setDescription("marketingdesc");
        product.setServiceId("modifiedProductIdIdentical");
        product.setShortDescription("Service short description");
        product.setAutoAssignUserEnabled(Boolean.TRUE);
        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        VOService createdProduct = svcProv.createService(tp, product, null);
        validateCopiedProductPaymentConfiguration(createdProduct.getKey(),
                ENABLED_PAYMENTTYPES);

        VOServiceDetails productDetails = svcProv
                .getServiceDetails(createdProduct);

        List<VOLocalizedText> expected = runTX(new Callable<List<VOLocalizedText>>() {

            @Override
            public List<VOLocalizedText> call() {
                List<VOLocalizedText> list = localizer.getLocalizedValues(
                        tp.getKey(),
                        LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC);
                return list;
            }

        });
        VOServiceLocalization localization = svcProv
                .getServiceLocalization(productDetails);
        String localeToIgnore = is.getCurrentUserDetails().getLocale();
        compareDescriptions(expected, localization, localeToIgnore);

        assertEquals("Wrong product id", "modifiedProductIdIdentical",
                productDetails.getServiceId());
        assertEquals("Wrong product description", "marketingdesc",
                productDetails.getDescription());
        assertEquals("Wrong product short description",
                "Service short description",
                productDetails.getShortDescription());

        productDetails.setServiceId("newServiceId");

        VOServiceDetails updateMarketingProduct = svcProv.updateService(
                productDetails, null);

        assertEquals("Wrong product id", "newServiceId",
                updateMarketingProduct.getServiceId());
        assertEquals("Wrong product description", "marketingdesc",
                updateMarketingProduct.getDescription());
        assertEquals("Wrong product short description",
                "Service short description",
                updateMarketingProduct.getShortDescription());
        verify(audit, times(1)).updateService(any(DataService.class),
                any(Product.class), eq(false), eq(false), eq(false), anyString());
    }

    @Test
    public void testCreateAndUpdateMarketingProduct_description()
            throws Exception {

        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService product = new VOService();
        // Mandatory fields
        product.setDescription("marketingdesc");
        product.setServiceId("modifiedProductIdIdentical");
        product.setShortDescription("Service short description");
        product.setAutoAssignUserEnabled(Boolean.TRUE);
        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        VOService createdProduct = svcProv.createService(tp, product, null);
        validateCopiedProductPaymentConfiguration(createdProduct.getKey(),
                ENABLED_PAYMENTTYPES);

        VOServiceDetails productDetails = svcProv
                .getServiceDetails(createdProduct);

        List<VOLocalizedText> expected = runTX(new Callable<List<VOLocalizedText>>() {

            @Override
            public List<VOLocalizedText> call() {
                List<VOLocalizedText> list = localizer.getLocalizedValues(
                        tp.getKey(),
                        LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC);
                return list;
            }

        });
        VOServiceLocalization localization = svcProv
                .getServiceLocalization(productDetails);
        String localeToIgnore = is.getCurrentUserDetails().getLocale();
        compareDescriptions(expected, localization, localeToIgnore);

        assertEquals("Wrong product id", "modifiedProductIdIdentical",
                productDetails.getServiceId());
        assertEquals("Wrong product description", "marketingdesc",
                productDetails.getDescription());
        assertEquals("Wrong product short description",
                "Service short description",
                productDetails.getShortDescription());

        productDetails.setDescription("newDesc");

        VOServiceDetails updateMarketingProduct = svcProv.updateService(
                productDetails, null);

        assertEquals("Wrong product id", "modifiedProductIdIdentical",
                updateMarketingProduct.getServiceId());
        assertEquals("Wrong product description", "newDesc",
                updateMarketingProduct.getDescription());
        assertEquals("Wrong product short description",
                "Service short description",
                productDetails.getShortDescription());
        verify(audit, times(1)).updateService(any(DataService.class),
                any(Product.class), eq(false), eq(true), eq(false), anyString());
    }

    @Test
    public void createAndUpdateMarketingProduct_NoLog() throws Exception {

        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService product = new VOService();
        // Mandatory fields
        product.setDescription("marketingdesc");
        product.setServiceId("modifiedProductIdIdentical");
        product.setShortDescription("Service short description");

        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        VOService createdProduct = svcProv.createService(tp, product, null);
        validateCopiedProductPaymentConfiguration(createdProduct.getKey(),
                ENABLED_PAYMENTTYPES);

        VOServiceDetails productDetails = svcProv
                .getServiceDetails(createdProduct);

        List<VOLocalizedText> expected = runTX(new Callable<List<VOLocalizedText>>() {

            @Override
            public List<VOLocalizedText> call() {
                List<VOLocalizedText> list = localizer.getLocalizedValues(
                        tp.getKey(),
                        LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC);
                return list;
            }

        });
        VOServiceLocalization localization = svcProv
                .getServiceLocalization(productDetails);
        String localeToIgnore = is.getCurrentUserDetails().getLocale();
        compareDescriptions(expected, localization, localeToIgnore);

        assertEquals("Wrong product id", "modifiedProductIdIdentical",
                productDetails.getServiceId());
        assertEquals("Wrong product description", "marketingdesc",
                productDetails.getDescription());
        assertEquals("Wrong product short description",
                "Service short description",
                productDetails.getShortDescription());

        svcProv.updateService(productDetails, null);

        verify(audit, never()).updateService(any(DataService.class),
                any(Product.class), anyBoolean(), anyBoolean(), anyBoolean(), anyString());
    }

    @Test
    public void deleteProductCustomerCopyImplicit() throws Exception {
        VOTechnicalService service = createTechnicalProduct(svcProv);

        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(service.getKey(), ref.getKey());

        String productXml = "<?xml version='1.0' encoding='UTF-8'?>"
                + "<TechnicalProduct orgId=\""
                + providerOrgId
                + "\" id=\"example\" version=\"1.00\">"

                + String.format(Locale.US, PRODUCT_CHARGEABLE_XML_TEMPLATE,
                        EXAMPLE_PROFESSIONAL, PricingPeriod.MONTH,
                        BigDecimal.ZERO, BD100, BD100, BD100, BD100, BD100)

                + "</TechnicalProduct>";
        importProduct(productXml, mgr);

        List<VOService> products = svcProv.getSuppliedServices();
        assertTrue("Insufficient number of products returned",
                0 < products.size());

        final Organization newOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
            }
        });
        VOOrganization customer = runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                Organization result = mgr.find(Organization.class,
                        newOrg.getKey());
                Organization currentOrg = new Organization();
                currentOrg.setOrganizationId(providerOrgId);
                currentOrg = (Organization) mgr.find(currentOrg);
                OrganizationReference ref = new OrganizationReference(
                        currentOrg, result,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                mgr.persist(ref);
                return OrganizationAssembler.toVOOrganization(result, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });

        VOService product = products.get(0);
        VOServiceDetails productDetails = svcProv.getServiceDetails(product);

        List<VOEventDefinition> eventDefinitions = productDetails
                .getTechnicalService().getEventDefinitions();
        assertTrue("No events found", 0 < eventDefinitions.size());
        BigDecimal price = BigDecimal.valueOf(15);
        List<VOPricedEvent> pricedEvents = new ArrayList<VOPricedEvent>();
        for (VOEventDefinition eventDef : eventDefinitions) {
            VOPricedEvent pe = new VOPricedEvent(eventDef);
            pe.setEventPrice(price);
            price = price.multiply(BigDecimal.valueOf(2));
            pricedEvents.add(pe);
        }

        VOPriceModel voPM = createChargeablePriceModel();
        voPM.setConsideredEvents(pricedEvents);

        VOServiceDetails productForCustomer = svcProv
                .savePriceModelForCustomer(productDetails, voPM, customer);
        voPM = productForCustomer.getPriceModel();

        Product storedProduct = getProductFromServer(
                productForCustomer.getKey());
        PriceModel pm;
        ParameterSet ps = storedProduct.getParameterSet();

        svcProv.getServiceForCustomer(customer, productForCustomer);

        // delete the template, so the customer copy must be deleted as well
        svcProv.deleteService(productDetails);

        // verify changes on server side
        storedProduct = getDOFromServer(Product.class,
                productForCustomer.getKey());
        pm = getDOFromServer(PriceModel.class, voPM.getKey());
        ps = getDOFromServer(ParameterSet.class, ps.getKey());

        assertNull("Customer product was not deleted", storedProduct);
        assertNull("Customer product's price model was not deleted", pm);
        assertNull("Customer product's parameter set was not deleted", ps);

        // verify behaviour of client side
        try {
            svcProv.getServiceForCustomer(customer, productForCustomer);
            fail("Product was deleted and must not be found anymore");
        } catch (ObjectNotFoundException e) {

        }
    }

    private Product getProductFromServer(
            final long key) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() {
                Product product = mgr.find(Product.class, key);
                product.setParameterSet(unproxyEntity(product.getParameterSet()));
                return product;
            }
        });
    }

    @Test
    public void savePriceModelForCustomerFreeWithChargeableTemplate()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // create technical product with parameters
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider, "tp", false, ServiceAccessType.LOGIN);
                List<ParameterDefinition> paramDefs = new ArrayList<ParameterDefinition>();
                ParameterDefinition paramDef = new ParameterDefinition();
                paramDef.setConfigurable(true);
                paramDef.setParameterId("Test_Param1");
                paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
                paramDef.setValueType(ParameterValueType.LONG);
                paramDef.setTechnicalProduct(tp);
                mgr.persist(paramDef);
                paramDefs.add(paramDef);

                paramDef = new ParameterDefinition();
                paramDef.setConfigurable(true);
                paramDef.setParameterId("Test_Param2");
                paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
                paramDef.setValueType(ParameterValueType.ENUMERATION);
                paramDef.setTechnicalProduct(tp);
                mgr.persist(paramDef);
                ParameterOption parameterOption = new ParameterOption();
                parameterOption.setOptionId("1");
                parameterOption.setParameterDefinition(paramDef);
                mgr.persist(parameterOption);
                paramDef.setOptionList(Collections
                        .singletonList(parameterOption));
                paramDefs.add(paramDef);

                tp.setParameterDefinitions(paramDefs);
                marketingPermServ_getTechnicalProducts.add(tp);
                return null;
            }
        });
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        List<VOTechnicalService> tps = svcProv
                .getTechnicalServices(OrganizationRoleType.SUPPLIER);
        assertEquals("Wrong number of products returned", 1, tps.size());

        VOService productToCreate = new VOService();
        productToCreate.setServiceId("productId");

        VOTechnicalService technicalProduct = tps.get(0);
        List<VOParameterDefinition> parameterDefinitions = technicalProduct
                .getParameterDefinitions();
        List<VOParameter> parameters = new ArrayList<VOParameter>();
        VOParameter parameter = new VOParameter(parameterDefinitions.get(0));
        parameter.setConfigurable(true);
        parameter.setValue("1");
        parameters.add(parameter);
        parameter = new VOParameter(parameterDefinitions.get(1));
        parameter.setConfigurable(true);
        parameter.setValue("1");
        parameters.add(parameter);
        productToCreate.setParameters(parameters);

        createMarketingPermission(technicalProduct.getKey(), orgRef.getKey());

        VOServiceDetails mp = svcProv.createService(technicalProduct,
                productToCreate, null);

        VOPriceModel priceModel = createChargeablePriceModel();
        priceModel.setOneTimeFee(BigDecimal.valueOf(500L));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(500L));
        priceModel.setPricePerUserAssignment(BigDecimal.valueOf(500L));
        List<VOPricedParameter> selectedParameters = new ArrayList<VOPricedParameter>();
        VOPricedParameter pricedParam = new VOPricedParameter(
                parameterDefinitions.get(0));
        pricedParam.setPricePerSubscription(BigDecimal.valueOf(500));
        pricedParam.setPricePerUser(BigDecimal.valueOf(500));
        selectedParameters.add(pricedParam);

        pricedParam = new VOPricedParameter(parameterDefinitions.get(1));
        VOParameterOption pOpt = parameterDefinitions.get(1)
                .getParameterOptions().get(0);
        VOPricedOption voPricedOption = new VOPricedOption();
        voPricedOption.setPricePerSubscription(BigDecimal.valueOf(50));
        voPricedOption.setPricePerUser(BigDecimal.valueOf(50));
        voPricedOption.setParameterOptionKey(pOpt.getKey());
        pricedParam.setPricedOptions(Collections.singletonList(voPricedOption));
        selectedParameters.add(pricedParam);

        priceModel.setSelectedParameters(selectedParameters);

        List<VOEventDefinition> defs = technicalProduct.getEventDefinitions();
        List<VOPricedEvent> events = new ArrayList<VOPricedEvent>();
        for (VOEventDefinition def : defs) {
            VOPricedEvent event = new VOPricedEvent(def);
            event.setEventPrice(BigDecimal.valueOf(50));
            events.add(event);
        }
        priceModel.setConsideredEvents(events);
        priceModel.setLicense("licenseDescription");

        VOServiceDetails prod = svcProv.savePriceModel(mp, priceModel);
        VOPriceModel pm = prod.getPriceModel();
        assertEquals(BigDecimal.valueOf(500), pm.getOneTimeFee());
        assertEquals(BigDecimal.valueOf(500), pm.getPricePerPeriod());
        assertEquals(BigDecimal.valueOf(500), pm.getPricePerUserAssignment());
        VOPricedParameter pp = pm.getSelectedParameters().get(0);
        assertEquals(BigDecimal.valueOf(500), pp.getPricePerSubscription());
        assertEquals(BigDecimal.valueOf(500), pp.getPricePerUser());
        pp = pm.getSelectedParameters().get(1);
        VOPricedOption po = pp.getPricedOptions().get(0);
        assertEquals(BigDecimal.valueOf(50), po.getPricePerSubscription());
        assertEquals(BigDecimal.valueOf(50), po.getPricePerUser());

        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        VOServiceDetails customerProduct = svcProv.savePriceModelForCustomer(
                prod, priceModel, customer);

        assertFalse("Customer product must be different to general one",
                customerProduct.getKey() == mp.getKey());
        pm = customerProduct.getPriceModel();
        assertTrue(pm.getConsideredEvents().isEmpty());
        assertTrue(BigDecimalComparator.isZero(pm.getOneTimeFee()));
        assertTrue(BigDecimalComparator.isZero(pm.getPricePerPeriod()));
        assertTrue(BigDecimalComparator.isZero(pm.getPricePerUserAssignment()));
    }

    @Test
    public void statusAllowsDeletion() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);

        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(techProduct.getKey(), ref.getKey());

        VOServiceDetails template = createProduct(techProduct, "product",
                svcProv);
        createAndActivateCustSpecService(template, true);
        boolean rc = svcProv.statusAllowsDeletion(template);
        assertFalse("status should not allow deletion", rc);
    }

    /**
     * Bug 9836
     */
    @Test
    public void statusAllowsDeletion_CustSpecSubscription() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);

        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(techProduct.getKey(), ref.getKey());

        VOServiceDetails template = createProduct(techProduct, "product",
                svcProv);
        VOService svc = createAndActivateCustSpecService(template, false);
        createTerminatedSubscription(svc);

        boolean rc = svcProv.statusAllowsDeletion(template);
        assertTrue("status should allow deletion", rc);
    }

    private void createTerminatedSubscription(final VOService svc)
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product p = mgr.getReference(Product.class, svc.getKey());
                Organization o = p.getTargetCustomer();
                Subscription s = Subscriptions.createSubscription(mgr,
                        o.getOrganizationId(), p);
                s.setStatus(SubscriptionStatus.DEACTIVATED);
                s.getProduct().setStatus(ServiceStatus.DELETED);
                return null;
            }
        });

    }

    private VOService createAndActivateCustSpecService(
            VOServiceDetails template, boolean activate) throws Exception {
        VOOrganization customer = runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                Organization result = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                Organization currentOrg = new Organization();
                currentOrg.setOrganizationId(providerOrgId);
                currentOrg = (Organization) mgr.find(currentOrg);
                OrganizationReference ref = new OrganizationReference(
                        currentOrg, result,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                mgr.persist(ref);
                return OrganizationAssembler.toVOOrganization(result, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });
        VOPriceModel priceModel = new VOPriceModel();
        VOService custSpec = svcProv.savePriceModelForCustomer(template,
                priceModel, customer);
        if (activate) {
            return svcProv.activateService(custSpec);
        }
        return custSpec;
    }

    @Test
    public void filterProducts() {
        // given
        ServiceProvisioningServiceBean sps = spy(new ServiceProvisioningServiceBean());
        sps.dm = mock(DataService.class);

        List<Product> givenProducts = new LinkedList<Product>();
        givenProducts.add(createProductWithStatus(ServiceStatus.DELETED));
        givenProducts.add(createProductWithStatus(ServiceStatus.INACTIVE));
        givenProducts.add(createProductWithStatus(ServiceStatus.ACTIVE));
        givenProducts.add(createProductWithStatus(ServiceStatus.OBSOLETE));
        givenProducts.add(createProductWithStatus(ServiceStatus.SUSPENDED));

        // when
        List<Product> filteredProducts = sps.filterProducts(givenProducts,
                ANY_MARKETPLACE_ID);

        // then
        assertEquals(1, filteredProducts.size());
        assertEquals(ServiceStatus.ACTIVE, filteredProducts.get(0).getStatus());
    }

    private Product createProductWithStatus(ServiceStatus status) {
        Product product = new Product();
        product.setKey(random.nextInt());
        product.setStatus(status);
        return product;
    }

    @SuppressWarnings("boxing")
    @Test
    public void filterProducts_notMpOwnerAndSuspended() {
        // given
        ServiceProvisioningServiceBean sps = spy(new ServiceProvisioningServiceBean());
        sps.dm = mock(DataService.class);
        when(
                sps.isOrganizationMarketplaceOwner(any(PlatformUser.class),
                        anyString())).thenReturn(false);

        List<Product> givenProducts = new LinkedList<Product>();
        givenProducts.add(createProductWithStatus(ServiceStatus.SUSPENDED));

        // when
        List<Product> filteredProducts = sps.filterProducts(givenProducts,
                ANY_MARKETPLACE_ID);

        // then
        assertEquals(0, filteredProducts.size());
    }

    @SuppressWarnings("boxing")
    @Test
    public void filterProducts_mpOwnerAndSuspended() {
        // given
        ServiceProvisioningServiceBean sps = spy(new ServiceProvisioningServiceBean());
        sps.dm = mock(DataService.class);
        when(
                sps.isOrganizationMarketplaceOwner(any(PlatformUser.class),
                        anyString())).thenReturn(true);

        List<Product> givenProducts = new LinkedList<Product>();
        givenProducts.add(createProductWithStatus(ServiceStatus.SUSPENDED));

        // when
        List<Product> filteredProducts = sps.filterProducts(givenProducts,
                ANY_MARKETPLACE_ID);

        // then
        assertEquals(1, filteredProducts.size());
        assertEquals(ServiceStatus.SUSPENDED, filteredProducts.get(0)
                .getStatus());
    }

    @Test
    public void filterProducts_customerProductAvailable() {
        // given
        ServiceProvisioningServiceBean sps = spy(new ServiceProvisioningServiceBean());
        sps.dm = mock(DataService.class);

        List<Product> givenProducts = new LinkedList<Product>();
        Product template = createActiveProductWithType(ServiceType.TEMPLATE);
        givenProducts.add(template);

        Product customerProduct = createActiveProductWithType(ServiceType.CUSTOMER_TEMPLATE);
        customerProduct.setTemplate(template);
        givenProducts.add(customerProduct);

        // when
        List<Product> filteredProducts = sps.filterProducts(givenProducts,
                ANY_MARKETPLACE_ID);

        // then
        assertEquals(1, filteredProducts.size());
        assertEquals(ServiceType.CUSTOMER_TEMPLATE, filteredProducts.get(0)
                .getType());
    }

    private Product createActiveProductWithType(ServiceType type) {
        Product product = new Product();
        product.setKey(random.nextInt());
        product.setStatus(ServiceStatus.ACTIVE);
        product.setType(type);
        return product;
    }

    @Test
    public void filterProducts_partnerProductAvailable() {
        // given
        ServiceProvisioningServiceBean sps = spy(new ServiceProvisioningServiceBean());
        sps.dm = mock(DataService.class);

        List<Product> givenProducts = new LinkedList<Product>();
        Product template = createActiveProductWithType(ServiceType.TEMPLATE);
        givenProducts.add(template);

        Product partnerProduct = createActiveProductWithType(ServiceType.PARTNER_TEMPLATE);
        partnerProduct.setTemplate(template);
        givenProducts.add(partnerProduct);

        // when
        List<Product> filteredProducts = sps.filterProducts(givenProducts,
                ANY_MARKETPLACE_ID);

        // then
        assertEquals(2, filteredProducts.size());
        assertEquals(ServiceType.TEMPLATE, filteredProducts.get(0).getType());
        assertEquals(ServiceType.PARTNER_TEMPLATE, filteredProducts.get(1)
                .getType());
    }

    @SuppressWarnings("boxing")
    @Test
    public void filterProducts_subscriptionLimitReached() {
        // given
        ServiceProvisioningServiceBean sps = spy(new ServiceProvisioningServiceBean());
        sps.dm = mock(DataService.class);
        when(sps.dm.getCurrentUserIfPresent()).thenReturn(new PlatformUser());

        List<Product> givenProducts = new LinkedList<Product>();
        Product product = createProductWithStatus(ServiceStatus.ACTIVE);
        givenProducts.add(product);

        doReturn(true).when(sps).isSubscriptionLimitReached(eq(product));

        // when
        List<Product> filteredProducts = sps.filterProducts(givenProducts,
                ANY_MARKETPLACE_ID);

        // then
        assertEquals(0, filteredProducts.size());
    }

    @SuppressWarnings("boxing")
    @Test
    public void filterProducts_subscriptionLimitNotReached() {
        // given
        ServiceProvisioningServiceBean sps = spy(new ServiceProvisioningServiceBean());
        sps.dm = mock(DataService.class);
        when(sps.dm.getCurrentUserIfPresent()).thenReturn(new PlatformUser());

        List<Product> givenProducts = new LinkedList<Product>();
        Product product = createProductWithStatus(ServiceStatus.ACTIVE);
        givenProducts.add(product);

        doReturn(false).when(sps).isSubscriptionLimitReached(eq(product));

        // when
        List<Product> filteredProducts = sps.filterProducts(givenProducts,
                ANY_MARKETPLACE_ID);

        // then
        assertEquals(1, filteredProducts.size());
        assertEquals(ServiceStatus.ACTIVE, filteredProducts.get(0).getStatus());
    }

    @Test
    public void createAndUpdateMarketingProduct_autoAssignEnabled()
            throws Exception {

        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService product = new VOService();
        // Mandatory fields
        product.setDescription("marketingdesc");
        product.setServiceId("modifiedProductIdIdentical");
        product.setShortDescription("Service short description");
        product.setAutoAssignUserEnabled(Boolean.TRUE);
        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        VOService createdProduct = svcProv.createService(tp, product, null);

        VOServiceDetails productDetails = svcProv
                .getServiceDetails(createdProduct);

        assertEquals("modifiedProductIdIdentical",
                productDetails.getServiceId());
        assertEquals(Boolean.TRUE, productDetails.isAutoAssignUserEnabled());

        VOServiceDetails updateMarketingProduct = svcProv.updateService(
                productDetails, null);

        assertEquals("modifiedProductIdIdentical",
                updateMarketingProduct.getServiceId());
        assertEquals(Boolean.TRUE, productDetails.isAutoAssignUserEnabled());

    }

    @Test
    public void createService_configuratorUrl() throws Exception {
        // given
        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService service = new VOService();
        service.setServiceId("serviceId");
        service.setConfiguratorUrl("http://www.configUrl.de");

        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        // when
        VOService createdService = svcProv.createService(tp, service, null);

        // then
        assertEquals(service.getConfiguratorUrl(),
                createdService.getConfiguratorUrl());
    }

    @Test
    public void updateService_configuratorUrl() throws Exception {
        // given
        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService service = new VOService();
        service.setServiceId("serviceId");
        service.setConfiguratorUrl("http://www.configUrl.de");

        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        VOService createdService = svcProv.createService(tp, service, null);
        VOServiceDetails serviceDetails = svcProv
                .getServiceDetails(createdService);
        serviceDetails.setConfiguratorUrl("http://www.newConfigUrl.de");

        // when
        VOServiceDetails updatedService = svcProv.updateService(serviceDetails,
                null);

        // then
        assertEquals(service.getConfiguratorUrl(),
                createdService.getConfiguratorUrl());

        assertEquals(serviceDetails.getConfiguratorUrl(),
                updatedService.getConfiguratorUrl());

    }

    @Test(expected = ValidationException.class)
    public void createService_configuratorUrl_invalid() throws Exception {
        // given
        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService service = new VOService();
        service.setServiceId("serviceId");
        service.setConfiguratorUrl("configUrl");

        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        // when
        svcProv.createService(tp, service, null);
    }

    @Test(expected = ValidationException.class)
    public void updateService_configuratorUrl_invalid() throws Exception {
        // given
        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService service = new VOService();
        service.setServiceId("serviceId");
        service.setConfiguratorUrl("http://www.configUrl.de");

        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        VOService createdService = svcProv.createService(tp, service, null);
        VOServiceDetails serviceDetails = svcProv
                .getServiceDetails(createdService);
        serviceDetails.setConfiguratorUrl("newConfigUrl");

        // when
        svcProv.updateService(serviceDetails, null);
    }

    @Test
    public void updateService_customTabUrl() throws Exception {
        // given
        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService service = new VOService();
        service.setServiceId("serviceId");
        service.setCustomTabUrl("http://www.oldUrl.example");

        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        VOService createdService = svcProv.createService(tp, service, null);
        VOServiceDetails serviceDetails = svcProv
                .getServiceDetails(createdService);
        serviceDetails.setCustomTabUrl("http://www.newUrl.example");

        // when
        VOServiceDetails updatedService = svcProv.updateService(serviceDetails,
                null);

        // then
        assertEquals(service.getCustomTabUrl(),
                createdService.getCustomTabUrl());

        assertEquals(serviceDetails.getCustomTabUrl(),
                updatedService.getCustomTabUrl());

    }

    @Test(expected = ValidationException.class)
    public void createService_customTabUrl_invalid() throws Exception {
        // given
        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService service = new VOService();
        service.setServiceId("serviceId");
        service.setCustomTabUrl("thisIsNotAnUrl");

        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        // when
        svcProv.createService(tp, service, null);
    }

    @Test(expected = ValidationException.class)
    public void updateService_customTabUrl_invalid() throws Exception {
        // given
        final VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService service = new VOService();
        service.setServiceId("serviceId");
        service.setCustomTabUrl("http://www.oldUrl.example");

        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(tp.getKey(), ref.getKey());

        VOService createdService = svcProv.createService(tp, service, null);
        VOServiceDetails serviceDetails = svcProv
                .getServiceDetails(createdService);
        serviceDetails.setCustomTabUrl("thisIsNotAnUrl");

        // when
        svcProv.updateService(serviceDetails, null);
    }

}
