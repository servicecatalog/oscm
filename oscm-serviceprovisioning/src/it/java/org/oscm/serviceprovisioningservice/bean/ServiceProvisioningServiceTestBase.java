/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 06.12.2011                                                      
 *
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.categorizationService.local.CategorizationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.intf.*;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException.Reason;
import org.oscm.internal.vo.*;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.serviceprovisioningservice.assembler.PriceModelAssembler;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.*;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.setup.ProductImportParser;
import org.oscm.test.stubs.*;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.oscm.test.Numbers.BD100;
import static org.oscm.test.Numbers.TIMESTAMP;

@SuppressWarnings("deprecation")
public class ServiceProvisioningServiceTestBase extends EJBTestBase {

    protected static final String[] ENABLED_PAYMENTTYPES = new String[]{
        PaymentType.CREDIT_CARD, PaymentType.INVOICE};
    protected static final String EXAMPLE_ENTERPRISE = "EXAMPLE Enterprise";
    protected static final String EXAMPLE_PROFESSIONAL = "EXAMPLE Professional";
    protected static final String EXAMPLE_STARTER = "EXAMPLE Starter";
    protected static final String EXAMPLE_TRIAL = "EXAMPLE Trial";
    protected static final String EUR = "EUR";
    protected static final String USD = "USD";
    protected final String PRODUCT_ID1 = "PRODUCT_ID1";
    protected final String PRODUCT_ID2 = "PRODUCT_ID2";
    protected ServiceProvisioningService svcProv;
    protected ServiceProvisioningServiceLocal svcProvLocal;
    protected DataService mgr;
    protected LocalizerServiceLocal localizer;
    protected AccountService accMgmt;
    protected IdentityService is;
    protected MarketplaceService mpSvc;
    protected CategorizationService categorizationService;
    protected Map<String, VOService> COMPARE_VALUES = new HashMap<>();
    protected String providerOrgId;
    protected String supplierOrgId;
    protected String customerOrgId;
    protected long providerUserKey;
    protected long supplierUserKey;
    protected long customerUserKey;
    protected List<DomainObject<?>> domObjects = new ArrayList<>();
    protected String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    protected boolean appNotAlive;
    protected Organization provider;
    protected Organization supplier;
    protected Organization customer;
    protected Organization orgToReturnForTriggerProcessing;
    protected VOOrganization secondCustomer;
    protected VOTechnicalService techProduct;
    protected OrganizationReference orgRef;
    protected List<TechnicalProduct> marketingPermServ_getTechnicalProducts = new ArrayList<>();
    protected Marketplace mpSupplier;
    protected Marketplace mpProvider;
    protected ServiceDefinition serviceDefinition = new ServiceDefinition();
    private MarketingPermissionServiceLocal marketingPermissionSvcMock;

    public static final <T> List<T> emptyList() {
        return new ArrayList<>();
    }

    protected static byte[] getTSWithRoles(boolean withRoles) throws Exception {
        String tp = "<tns:TechnicalServices xmlns:tns=\"oscm.serviceprovisioning/1.9/TechnicalService.xsd\">"
            + "<tns:TechnicalService id=\"example\" \n"
            + " accessType=\"LOGIN\"\n"
            + " baseUrl=\"http://estadmue:8089/example-dev/\"\n"
            + " provisioningType=\"SYNCHRONOUS\"\n"
            + " provisioningUrl=\"http://estadmue:8089/example-dev/services/ProvisioningService?wsdl\"\n"
            + " provisioningVersion=\"1.0\"\n" + " loginPath=\"\\login/\"\n"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "<LocalizedDescription locale=\"en\">LocalizedDescription</LocalizedDescription>"
            + "<LocalizedLicense locale=\"en\">LocalizedLicense</LocalizedLicense>";
        if (withRoles) {
            tp += "<Role id=\"ADMIN\">"
                + "<LocalizedName locale=\"en\">Administrator</LocalizedName>"
                + "<LocalizedName locale=\"de\">Administrator</LocalizedName>"
                + "<LocalizedDescription locale=\"en\">Administrators have full access to all data entities and can execute administartive tasks such as role assignments and user creation.</LocalizedDescription>"
                + "<LocalizedDescription locale=\"de\">Administratoren haben vollen Datenzugriff und können administartive Aufgaben erledigen wie Rollen zuweisen oder Benutzer anlegen.</LocalizedDescription>"
                + "</Role><Role id=\"USER\">"
                + "<LocalizedName locale=\"en\">User</LocalizedName>"
                + "<LocalizedName locale=\"de\">Benutzer</LocalizedName>"
                + "<LocalizedDescription locale=\"en\">Users have full access to all data entities but cannot execute adminstartive tasks.</LocalizedDescription>"
                + "<LocalizedDescription locale=\"de\">Benutzer haben vollen Datenzugriff aber können keine administrativen Aufgaben erledigen.</LocalizedDescription>"
                + "</Role><Role id=\"GUEST\">"
                + "<LocalizedName locale=\"en\">Guest</LocalizedName>"
                + "<LocalizedName locale=\"de\">Gast</LocalizedName>"
                + "<LocalizedDescription locale=\"en\">Gustes only have limited read access.</LocalizedDescription>"
                + "<LocalizedDescription locale=\"de\">Gäste haben nur eingeschränkten Lesezugriff.</LocalizedDescription>"
                + "</Role>";
        }
        tp += " </tns:TechnicalService></tns:TechnicalServices>";
        return tp.getBytes("UTF-8");
    }

    protected static VOServiceDetails createProduct(
        VOTechnicalService technicalProduct, String id,
        ServiceProvisioningService serviceProvisioning) throws Exception {
        VOService product = new VOService();
        product.setServiceId(id);
        return serviceProvisioning.createService(technicalProduct, product,
            null);
    }

    protected static VOService getEnterprise() {
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

    protected static VOService getProfessional() {
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

    protected static VOService getFreeProduct(String id) {
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
     * Compares the localized description of the technical service with the ones
     * form the service except in the locale that has to be ignored.
     *
     * @param expected
     * @param localization
     * @param localeToIgnore
     */
    protected static void compareDescriptions(List<VOLocalizedText> expected,
                                              VOServiceLocalization localization, String localeToIgnore) {
        Map<String, String> localeToDesc = new HashMap<>();
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

    @Override
    protected void setup(TestContainer container) throws Exception {
        setupTestContainer();
        setupTestData();
    }

    private void setupTestContainer() throws Exception {
        AESEncrypter.generateKey();
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(new ServiceProvisioningServiceLocalizationBean());
        container.addBean(mock(LandingpageServiceLocal.class));
        container.addBean(mock(MarketplaceServiceLocal.class));
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
        marketingPermissionSvcMock = mock(
            MarketingPermissionServiceLocal.class);
        container.addBean(marketingPermissionSvcMock);
        container.addBean(mock(AccountServiceLocal.class));
        container.addBean(new TagServiceBean());
        container.addBean(mock(CategorizationServiceLocal.class));

        // Subclasses may override
        addBean(container, new ServiceProvisioningServiceBean());
        container.addBean(mock(MarketplaceService.class));
        is = container.get(IdentityService.class);
        mpSvc = container.get(MarketplaceService.class);
        categorizationService = container.get(CategorizationService.class);

        setUpDirServerStub(container.get(ConfigurationServiceLocal.class));
        svcProv = container.get(ServiceProvisioningService.class);

        svcProvLocal = container.get(ServiceProvisioningServiceLocal.class);

        mgr = container.get(DataService.class);
        localizer = container.get(LocalizerServiceLocal.class);
        accMgmt = container.get(AccountService.class);

        COMPARE_VALUES.put(EXAMPLE_TRIAL, getFreeProduct(EXAMPLE_TRIAL));
        COMPARE_VALUES.put(EXAMPLE_STARTER, getFreeProduct(EXAMPLE_STARTER));
        COMPARE_VALUES.put(EXAMPLE_PROFESSIONAL, getProfessional());
        COMPARE_VALUES.put(EXAMPLE_ENTERPRISE, getEnterprise());
    }

    private void setupTestData() throws Exception {

        // EUR, INVOICE, DIRECT DEBIT, CREDIT CARD
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance(EUR));
                try {
                    mgr.persist(sc);
                    createPaymentTypes(mgr);
                    createOrganizationRoles(mgr);
                    SupportedCountries.createSomeSupportedCountries(mgr);
                    BillingAdapters.createBillingAdapter(mgr,
                        BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                        true);
                } catch (NonUniqueBusinessKeyException e) {
                    // ignore
                }
                return null;
            }
        });

        // CREATE TP
        provider = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization po = Organizations.createPlatformOperator(mgr);
                Organization organization = Organizations.createOrganization(
                    mgr, OrganizationRoleType.TECHNOLOGY_PROVIDER,
                    OrganizationRoleType.SUPPLIER);
                mpProvider = Marketplaces.ensureMarketplace(organization,
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

        // CREATE TECHNICAL PRODUCT
        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                techProduct = createTechnicalProduct(svcProv);
                return null;
            }
        });
        container.login("1");

        // CREATE SUPPLIER
        supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations
                    .createOrganization(mgr, OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                    organization, true, "admin");
                supplierUserKey = user.getKey();
                Organization provider = Organizations.findOrganization(mgr,
                    providerOrgId);
                TechnicalProduct tp = mgr.find(TechnicalProduct.class,
                    techProduct.getKey());
                orgRef = Organizations.addSupplierForTechnicalProduct(mgr,
                    provider, organization, tp);
                return organization;
            }
        });
        supplierOrgId = supplier.getOrganizationId();

        // CREATE MARKETPLACE FOR SUPPLIER
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization sup = Organizations.findOrganization(mgr,
                    supplierOrgId);
                mpSupplier = Marketplaces.createGlobalMarketplace(sup,
                    sup.getOrganizationId(), mgr);
                Marketplaces.grantPublishing(sup, mpSupplier, mgr, false);
                return null;
            }
        });

        // CREATE CUSTOMER
        customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier = Organizations.findOrganization(mgr,
                    supplierOrgId);
                Organization organization = Organizations.createCustomer(mgr,
                    supplier);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                    organization, true, "admin");
                customerUserKey = user.getKey();
                return organization;
            }
        });

        customerOrgId = customer.getOrganizationId();
        container.login(providerUserKey, ROLE_SERVICE_MANAGER,
            ROLE_TECHNOLOGY_MANAGER);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation)
                throws Throwable {
                runTX(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        Query query = mgr.createQuery(
                            "DELETE FROM MarketingPermission mp WHERE mp.technicalProduct = :tpKey");
                        query.setParameter("tpKey",
                            invocation.getArguments()[0]);
                        query.executeUpdate();
                        return null;
                    }
                });
                return null;
            }
        }).when(marketingPermissionSvcMock).removeMarketingPermissions(
            Matchers.any(TechnicalProduct.class));

        doAnswer(new Answer<List<TechnicalProduct>>() {
            @Override
            public List<TechnicalProduct> answer(InvocationOnMock invocation)
                throws Throwable {
                return marketingPermServ_getTechnicalProducts;
            }
        }).when(marketingPermissionSvcMock).getTechnicalServicesForSupplier(
            Matchers.any(Organization.class));

    }

    protected VOService setupCompatibleProducts(final ServiceStatus status)
        throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addProducts(4, EUR, true);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Product prod1 = mgr.getReference(Product.class,
                    domObjects.get(0).getKey());
                final Product prod2 = mgr.getReference(Product.class,
                    domObjects.get(1).getKey());
                prod2.setStatus(status);
                ProductReference reference = new ProductReference(prod1, prod2);
                mgr.persist(reference);
                return null;
            }
        });

        VOService voProduct = getVOProduct(domObjects.get(0).getKey());
        return voProduct;
    }

    protected VOService prepareServiceForDeletion(final ServiceStatus status)
        throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOService refProduct = createProduct(techProduct, "product",
            svcProv);
        setProductStatus(status, refProduct.getKey());
        refProduct.setVersion(refProduct.getVersion() + 1);
        return refProduct;
    }

    protected VOServiceDetails[] prepareProductsForSetCompatibleProducts(
        final ServiceStatus status) throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
            ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails[] details = new VOServiceDetails[2];
        details[0] = createProduct(techProduct, "product1", svcProv);
        publishToLocalMarketplaceSupplier(details[0], mpSupplier);
        details[1] = createProduct(techProduct, "product2", svcProv);
        publishToLocalMarketplaceSupplier(details[1], mpSupplier);

        VOPriceModel priceModel = createPriceModel();
        details[0] = svcProv.savePriceModel(details[0], priceModel);
        details[1] = svcProv.savePriceModel(details[1], priceModel);

        setProductStatus(status, details[0].getKey());
        details[0].setVersion(details[0].getVersion() + 1);
        return details;
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

    protected void addProducts(int numOfProducts, String usedCurrency,
                               boolean clear) throws Exception {
        // Enter new Products
        if (clear) {
            domObjects.clear();
        }
        Organization organization = Organizations.findOrganization(mgr,
            providerOrgId);
        String technicalSProductId = "TP_ID";
        TechnicalProduct tProd = TechnicalProducts.findTechnicalProduct(mgr,
            organization, technicalSProductId);
        if (tProd == null) {
            tProd = TechnicalProducts.createTechnicalProduct(mgr, organization,
                technicalSProductId, false, ServiceAccessType.LOGIN);
        }
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setParameterId("param" + UUID.randomUUID());
        paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
        paramDef.setValueType(ParameterValueType.STRING);
        paramDef.setTechnicalProduct(tProd);
        mgr.persist(paramDef);

        SupportedCurrency sc = new SupportedCurrency();
        sc.setCurrency(Currency.getInstance(usedCurrency));
        sc = (SupportedCurrency) mgr.find(sc);
        if (sc == null) {
            sc = new SupportedCurrency();
            sc.setCurrency(Currency.getInstance(usedCurrency));
            try {
                mgr.persist(sc);
            } catch (NonUniqueBusinessKeyException e) {
                fail("Error creating PriceModel for test.");
            }
        }

        Product prod;
        for (int i = 1; i <= numOfProducts; i++) {
            prod = new Product();
            prod.setVendor(organization);
            prod.setProductId("Product" + i + usedCurrency);
            prod.setTechnicalProduct(tProd);
            prod.setProvisioningDate(TIMESTAMP);
            prod.setStatus(ServiceStatus.ACTIVE);
            prod.setAutoAssignUserEnabled(Boolean.FALSE);
            ParameterSet ps = new ParameterSet();
            prod.setParameterSet(ps);
            prod.setType(ServiceType.TEMPLATE);
            mgr.persist(prod);

            Parameter param = new Parameter();
            param.setParameterSet(ps);
            param.setValue("someValue");
            param.setConfigurable(true);
            param.setParameterDefinition(paramDef);
            param.setParameterSet(ps);
            mgr.persist(param);

            PriceModel pi = createChargeablePriceModel(sc);
            prod.setPriceModel(pi);
            domObjects.add((Product) ReflectiveClone.clone(prod));
        }
    }

    protected void createProductReferences(
        final List<ProductReference> references,
        final long referenceProductKey, int numberOfProducts)
        throws ObjectNotFoundException, NonUniqueBusinessKeyException {
        Product prod1 = mgr.getReference(Product.class, referenceProductKey);
        for (int i = 1; i < numberOfProducts; i++) {
            Product prod2 = mgr.getReference(Product.class,
                domObjects.get(i).getKey());
            prod2.setStatus(ServiceStatus.INACTIVE);
            ProductReference reference = new ProductReference(prod1, prod2);
            mgr.persist(reference);
            references.add(reference);
        }
    }

    protected VOPriceModel createPriceModel() {
        VOPriceModel priceModel = new VOPriceModel();
        return priceModel;
    }

    protected VOPriceModel createChargeablePriceModel() {
        return createChargeablePriceModel(EUR);
    }

    protected PriceModel createChargeablePriceModel(
        SupportedCurrency usedCurrency) {
        PriceModel priceModel = new PriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrency(usedCurrency);
        priceModel.setPeriod(PricingPeriod.MONTH);
        return priceModel;
    }

    protected VOPriceModel createChargeablePriceModel(String usedCurrency) {
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode(usedCurrency);
        priceModel.setPeriod(PricingPeriod.MONTH);
        return priceModel;
    }

    protected <T extends DomainObject<?>> T getDOFromServer(
        final Class<T> objTypeIndicator, final long key) throws Exception {
        return runTX(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return mgr.find(objTypeIndicator, key);
            }
        });
    }

    protected DomainObject<?> refresh(final DomainObject<?> obj)
        throws Exception {
        return runTX(new Callable<DomainObject<?>>() {
            @Override
            public DomainObject<?> call() throws Exception {
                return mgr.find(obj.getClass(), obj.getKey());
            }
        });
    }

    protected VOOrganization getOrganizationForOrgId(
        final String organizationId) throws Exception {
        return runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                Organization result = new Organization();
                result.setOrganizationId(organizationId);
                result = (Organization) mgr.find(result);
                return OrganizationAssembler.toVOOrganization(result, false,
                    new LocalizerFacade(localizer, "en"));
            }
        });
    }

    void createPublicCatalogEntry(final String productId) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product p = Products.findProduct(mgr,
                    mgr.getCurrentUser().getOrganization(), productId);

                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(mgr.getCurrentUser().getOrganization()
                    .getOrganizationId());
                mp = (Marketplace) mgr.getReferenceByBusinessKey(mp);

                CatalogEntry ce = new CatalogEntry();
                ce.setProduct(p);
                ce.setAnonymousVisible(true);
                ce.setMarketplace(mp);
                mgr.persist(ce);
                return null;
            }
        });
    }

    protected VOServiceDetails createServiceThatAllowsOnlyOneSubscription()
        throws Exception, OrganizationAuthoritiesException,
        ObjectNotFoundException, OperationNotPermittedException,
        CurrencyException, ValidationException, ServiceStateException,
        ConcurrentModificationException, ServiceOperationException,
        TechnicalServiceNotAliveException {

        VOTechnicalService techProduct1 = runTX(
            new Callable<VOTechnicalService>() {
                @Override
                public VOTechnicalService call() throws Exception {
                    return createTechnicalProduct(svcProv,
                        TSXML.createTSXMLWithSubscriptionRestriction(
                            "true"));
                }
            });

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(techProduct1, "product1",
            svcProv);
        VOPriceModel priceModel = createPriceModel();
        voProduct1 = svcProv.savePriceModel(voProduct1, priceModel);
        publishToLocalMarketplaceSupplier(voProduct1, mpSupplier);
        svcProv.activateService(voProduct1);
        return voProduct1;
    }

    protected VOServiceDetails publishToLocalMarketplaceSupplier(
        VOService voProduct, Marketplace mp)
        throws ObjectNotFoundException, ValidationException,
        NonUniqueBusinessKeyException, OperationNotPermittedException {
        VOCatalogEntry entry = new VOCatalogEntry();
        VOMarketplace voMarketplace = new VOMarketplace();
        voMarketplace.setMarketplaceId(mp.getMarketplaceId());
        entry.setMarketplace(voMarketplace);
        return mpSvc.publishService(voProduct, Arrays.asList(entry));
    }

    protected VOServiceDetails publishToLocalMarketplaceSupplier(
        VOService voProduct, Marketplace mp, List<VOCategory> categories)
        throws ObjectNotFoundException, ValidationException,
        NonUniqueBusinessKeyException, OperationNotPermittedException {
        VOCatalogEntry entry = new VOCatalogEntry();
        VOMarketplace voMarketplace = new VOMarketplace();
        voMarketplace.setMarketplaceId(mp.getMarketplaceId());
        entry.setMarketplace(voMarketplace);
        entry.setCategories(categories);
        return mpSvc.publishService(voProduct, Arrays.asList(entry));
    }

    protected Subscription createSubscription(final String productId,
                                              final String subscriptionId, final String orgId) throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                String marketplaceId = supplier.getOrganizationId();
                Subscription sub = Subscriptions.createSubscription(mgr, orgId,
                    productId, subscriptionId, marketplaceId, supplier, 1);
                return sub;
            }
        });
    }

    protected String importProduct(final String xml, final DataService mgr)
        throws Exception {
        return runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                ProductImportParser parser = new ProductImportParser(mgr,
                    mgr.getCurrentUser().getOrganization());
                parser.parse(xml.getBytes("UTF-8"));
                return null;
            }
        });
    }

    /**
     * Helper method for create situation, when NotPermittedOperation will be
     * thrown.
     *
     * @return The prepared price model.
     * @throws Exception
     */
    protected VOPriceModel prepareVOPriceModel(final VOService productVO,
                                               final String usedCurrency) throws Exception {
        final LocalizerFacade facade = new LocalizerFacade(localizer, "en");
        VOPriceModel priceModel = runTX(new Callable<VOPriceModel>() {
            @Override
            public VOPriceModel call() {
                PriceModel priceModel = new PriceModel();
                priceModel.setType(PriceModelType.PRO_RATA);
                priceModel.setPeriod(PricingPeriod.MONTH);

                Product product = null;
                if (productVO != null) {
                    try {
                        product = mgr.getReference(Product.class,
                            productVO.getKey());
                    } catch (ObjectNotFoundException e1) {
                        fail("Can not get product reference.");
                    }
                } else {
                    fail("VOProduct has not to be null.");
                }

                priceModel.setProduct(product);

                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance(usedCurrency));
                sc = (SupportedCurrency) mgr.find(sc);
                if (sc == null) {
                    sc = new SupportedCurrency();
                    sc.setCurrency(Currency.getInstance(usedCurrency));
                    try {
                        mgr.persist(sc);
                    } catch (NonUniqueBusinessKeyException e) {
                        fail("Error creating PriceModel for test.");
                    }
                }
                priceModel.setCurrency(sc);
                try {
                    mgr.persist(priceModel);
                } catch (NonUniqueBusinessKeyException e) {
                    fail("Error creating PriceModel for test.");
                }
                final VOPriceModel priceModelVO = PriceModelAssembler
                    .toVOPriceModel(priceModel, facade);
                return priceModelVO;
            }
        });
        return priceModel;
    }

    /**
     * Initializes a service for testing.
     *
     * @return The vo representation of the created service.
     * @throws Exception
     */
    protected VOServiceDetails prepareService() throws Exception {
        secondCustomer = runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                // create technical product with parameters
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                    mgr, provider, "tp", false, ServiceAccessType.LOGIN);
                List<ParameterDefinition> paramDefs = new ArrayList<>();
                ParameterDefinition paramDef = new ParameterDefinition();
                paramDef.setConfigurable(true);
                paramDef.setParameterId("Test_Param");
                paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
                paramDef.setValueType(ParameterValueType.LONG);
                paramDef.setTechnicalProduct(tp);
                mgr.persist(paramDef);
                paramDefs.add(paramDef);
                tp.setParameterDefinitions(paramDefs);

                MarketingPermission mp = new MarketingPermission();
                OrganizationReference reference = mgr
                    .find(OrganizationReference.class, orgRef.getKey());
                mp.setOrganizationReference(reference);
                mp.setTechnicalProduct(tp);
                mgr.persist(mp);

                Organization cust = Organizations.createCustomer(mgr, mgr
                    .getReference(Organization.class, supplier.getKey()));
                marketingPermServ_getTechnicalProducts.add(tp);

                return OrganizationAssembler.toVOOrganization(cust, false,
                    new LocalizerFacade(localizer, "en"));
            }
        });

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
            ROLE_TECHNOLOGY_MANAGER);
        List<VOTechnicalService> tps = svcProv
            .getTechnicalServices(OrganizationRoleType.SUPPLIER);
        VOServiceDetails productToCreate = new VOServiceDetails();
        productToCreate.setServiceId("productId");
        VOTechnicalService technicalProduct = tps.get(tps.size() - 1);
        List<VOParameterDefinition> parameterDefinitions = technicalProduct
            .getParameterDefinitions();
        VOParameterDefinition parameterDefinition = parameterDefinitions.get(0);

        List<VOParameter> parameters = new ArrayList<>();
        VOParameter parameter = new VOParameter(parameterDefinition);
        parameter.setConfigurable(true);
        parameter.setValue("1");
        parameters.add(parameter);
        productToCreate.setParameters(parameters);

        // define price model
        VOPriceModel priceModel = new VOPriceModel();
        List<VOPricedParameter> selectedParameters = new ArrayList<>();
        VOPricedParameter pricedParam = new VOPricedParameter(
            parameterDefinition);
        selectedParameters.add(pricedParam);
        priceModel.setSelectedParameters(selectedParameters);
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(PricingPeriod.MONTH);

        VOServiceDetails mp = svcProv.createService(technicalProduct,
            productToCreate, null);
        mp = svcProv.savePriceModel(mp, priceModel);
        return mp;
    }

    /**
     * Helper method for creating a technical service with the given tags.
     */
    protected VOTechnicalService createTechnicalServiceWithTags(String id,
                                                                String[] tags, String billingId) throws Exception {
        VOTechnicalService vo = new VOTechnicalService();
        vo.setTechnicalServiceId(id);
        vo.setAccessType(ServiceAccessType.LOGIN);
        vo.setBaseUrl("http://localhost");
        vo.setProvisioningUrl("");
        vo.setProvisioningVersion("1.0");
        List<String> tstags = Arrays.asList(tags);
        vo.setTags(tstags);
        vo.setBillingIdentifier(billingId);
        svcProv.createTechnicalService(vo);
        return vo;
    }

    /**
     * Helper method for retrieveing all currently defined tags of the
     * datastore.
     */
    protected List<String> getAllTagsByLocale(final String locale,
                                              final String pattern) throws Exception {
        List<String> rc = runTX(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                Query query = mgr
                    .createNamedQuery("Tag.getAllOfLocaleFiltered");
                query.setParameter("locale", locale);
                query.setParameter("value", pattern);
                List<Tag> tagList = ParameterizedTypes
                    .list(query.getResultList(), Tag.class);
                List<String> result = new ArrayList<>();
                for (Tag def : tagList) {
                    result.add(def.getValue());
                }
                return result;
            }
        });

        return rc;

    }

    /**
     * Helper method for checking last entry of history table for status
     * DELETED.
     *
     * @param historyList List of history records.
     */
    protected void checkHistory(
        final List<DomainHistoryObject<?>> historyList) {
        // get last record from history list
        final DomainHistoryObject<?> history = historyList
            .get(historyList.size() - 1);
        ModificationType modificationType = history.getModtype();
        if (!modificationType.equals(ModificationType.DELETE)) {
            fail("The last version of history object is not DELETED type. Object was not correctly deleted.");
        }
    }

    protected String createSubscription(final VOOrganization cust,
                                        final SubscriptionStatus status, final VOService product,
                                        final String subId, final VORoleDefinition role) throws Exception {
        return runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                Organization customer = Organizations.findOrganization(mgr,
                    cust.getOrganizationId());

                // set payment information for the customer
                PaymentType paymentType = findPaymentType(INVOICE, mgr);
                PaymentInfo pi = PaymentInfos.createPaymentInfo(customer, mgr,
                    paymentType);

                Subscription subscription = Subscriptions.createSubscription(
                    mgr, customer.getOrganizationId(),
                    product.getServiceId(), subId, supplier);
                subscription.setStatus(status);
                subscription.setPaymentInfo(pi);
                BillingContact bc = PaymentInfos.createBillingContact(mgr,
                    customer);
                subscription.setBillingContact(bc);

                if (role != null) {
                    RoleDefinition r = mgr.getReference(RoleDefinition.class,
                        role.getKey());
                    List<PlatformUser> users = customer.getPlatformUsers();
                    for (PlatformUser u : users) {
                        subscription.addUser(u, r);
                    }
                }
                return subscription.getSubscriptionId();
            }
        });
    }

    protected String createTechnologyProvider() throws Exception {
        return runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Organization organization = Organizations.createOrganization(
                    mgr, OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                    organization, true, "admin");
                return String.valueOf(user.getKey());
            }
        });
    }

    protected VOServiceDetails createProductWithParameters(String prodId)
        throws Exception, OrganizationAuthoritiesException,
        ObjectNotFoundException, OperationNotPermittedException,
        ValidationException, NonUniqueBusinessKeyException {

        VOTechnicalService tp = createTechnicalProduct(svcProv);

        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId(prodId);
        product.setName(prodId);
        product.setShortDescription("shortDesc_" + prodId);
        product.setDescription("desc_" + prodId);
        product.setParameters(createParameters(tp));

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
            ROLE_TECHNOLOGY_MANAGER);
        product = svcProv.createService(tp, product, null);
        return product;
    }

    protected List<VOParameter> createParameters(VOTechnicalService tp) {
        List<VOParameter> parameters = new ArrayList<>();

        VOParameter param = new VOParameter(getParamDefinition(
            "MAX_FOLDER_NUMBER", tp.getParameterDefinitions()));
        param.setValue("");
        param.setConfigurable(false);
        parameters.add(param);

        param = new VOParameter(getParamDefinition("HAS_OPTIONS",
            tp.getParameterDefinitions()));
        param.setValue("1");
        param.setConfigurable(true);
        parameters.add(param);

        param = new VOParameter(getParamDefinition("BOOLEAN_PARAMETER",
            tp.getParameterDefinitions()));
        param.setValue("");
        param.setConfigurable(true);
        parameters.add(param);

        param = new VOParameter(getParamDefinition("STRING_PARAMETER",
            tp.getParameterDefinitions()));
        param.setValue("xyz");
        param.setConfigurable(false);
        parameters.add(param);

        parameters.add(null);

        return parameters;
    }

    protected VOServiceDetails createProduct(String prodId)
        throws Exception, OrganizationAuthoritiesException,
        ObjectNotFoundException, OperationNotPermittedException,
        ValidationException, NonUniqueBusinessKeyException {

        VOTechnicalService tp = createTechnicalProduct(svcProv);
        List<VOParameter> params = new ArrayList<>();
        VOParameter param = new VOParameter(getParamDefinition(
            "MAX_FOLDER_NUMBER", tp.getParameterDefinitions()));
        param.setValue("");
        param.setConfigurable(false);
        params.add(param);
        param = new VOParameter(getParamDefinition("HAS_OPTIONS",
            tp.getParameterDefinitions()));
        param.setValue("1");
        param.setConfigurable(true);
        params.add(param);
        param = new VOParameter(getParamDefinition("BOOLEAN_PARAMETER",
            tp.getParameterDefinitions()));
        param.setValue("");
        param.setConfigurable(true);
        params.add(param);
        param = new VOParameter(getParamDefinition("STRING_PARAMETER",
            tp.getParameterDefinitions()));
        param.setValue("xyz");
        param.setConfigurable(false);
        params.add(param);
        params.add(null);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId(prodId);
        product.setParameters(params);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
            ROLE_TECHNOLOGY_MANAGER);
        product = svcProv.createService(tp, product, null);
        return product;
    }

    // === Test Data Creation ===
    protected VOParameterDefinition getParamDefinition(String parameterId,
                                                       List<VOParameterDefinition> parameterDefinitions) {
        for (VOParameterDefinition def : parameterDefinitions) {
            if (def.getParameterId().equals(parameterId)) {
                return def;
            }
        }
        return null;
    }

    protected VOTechnicalService createTechnicalProduct(
        final ServiceProvisioningService serviceProvisioning)
        throws Exception {
        return runTX(new Callable<VOTechnicalService>() {
            @Override
            public VOTechnicalService call() throws Exception {
                return createTechnicalProduct(serviceProvisioning,
                    TECHNICAL_SERVICES_XML);
            }
        });
    }

    protected VOTechnicalService createTechnicalProductWithMandatoryParameter(
        final ServiceProvisioningService serviceProvisioning)
        throws Exception {
        return runTX(new Callable<VOTechnicalService>() {
            @Override
            public VOTechnicalService call() throws Exception {
                return createTechnicalProduct(serviceProvisioning,
                    TECHNICAL_SERVICES_WITH_MANDATORY_PARAM_XML);
            }
        });
    }

    protected VOTechnicalService createTechnicalProductWithSubscriptionRestriction(
        final ServiceProvisioningService serviceProvisioning,
        boolean substrictionRestriction) throws Exception {

        final String tsxml = TSXML.createTSXMLWithSubscriptionRestriction(
            String.valueOf(substrictionRestriction));
        return runTX(new Callable<VOTechnicalService>() {
            @Override
            public VOTechnicalService call() throws Exception {
                return createTechnicalProduct(serviceProvisioning, tsxml);
            }
        });
    }

    protected VOTechnicalService createTechnicalProduct(
        ServiceProvisioningService serviceProvisioning, String xml)
        throws Exception {
        String rc = serviceProvisioning
            .importTechnicalServices(xml.getBytes("UTF-8"));
        assertEquals("", rc);
        List<VOTechnicalService> technicalServices = serviceProvisioning
            .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        final VOTechnicalService result = technicalServices.get(0);
        for (VOTechnicalService service : technicalServices) {
            marketingPermServ_getTechnicalProducts.add(
                mgr.getReference(TechnicalProduct.class, service.getKey()));
        }
        return result;
    }

    protected List<VOTechnicalService> createTechnicalProducts(
        ServiceProvisioningService serviceProvisioning) throws Exception {

        String rc = serviceProvisioning.importTechnicalServices(
            TECHNICAL_SERVICES_XML.getBytes("UTF-8"));
        assertEquals("", rc);

        final List<VOTechnicalService> after = serviceProvisioning
            .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                OrganizationReference reference = mgr
                    .find(OrganizationReference.class, orgRef.getKey());
                for (VOTechnicalService s : after) {
                    try {
                        MarketingPermission mp = new MarketingPermission();
                        mp.setOrganizationReference(reference);
                        mp.setTechnicalProduct(
                            mgr.find(TechnicalProduct.class, s.getKey()));
                        mgr.persist(mp);
                    } catch (NonUniqueBusinessKeyException e) {
                        // ignore
                    }
                }
                return null;
            }
        });

        final List<VOTechnicalService> technicalProducts = serviceProvisioning
            .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        return technicalProducts;
    }

    /**
     * Helper method for checking whether a tag list is correct.
     */
    protected void checkTags(String[] expected, List<String> result) {
        assertEquals(expected.length, result.size());
        for (int i = 0; i < expected.length; i++) {
            if (!result.contains(expected[i])) {
                fail(String.format("No tag with value '%s' found!",
                    expected[i]));
            }
        }
    }

    /**
     * Initializes a technical product with four parameter definitions, two of
     * type {@link ParameterType#PLATFORM_PARAMETER} and two of type
     * {@link ParameterType#SERVICE_PARAMETER}. For each category, one is
     * configurable and one is not.
     *
     * @return The created product based on the created technical product.
     * @throws Exception
     */
    protected Product initTechnicalProductAndProductForParamDefTesting()
        throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                    mgr, provider, "tpId", false, ServiceAccessType.LOGIN);
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
                        "serviceCount", ParameterType.SERVICE_PARAMETER,
                        tp, mgr, null, null, true);
                TechnicalProducts.addParameterDefinition(
                    ParameterValueType.LONG, "serviceCountNC",
                    ParameterType.SERVICE_PARAMETER, tp, mgr, null, null,
                    false);

                Product prod = Products.createProduct(supplier, tp, false,
                    "prodId", null, mpSupplier, mgr);
                prod.setAutoAssignUserEnabled(Boolean.FALSE);
                Products.createParameter(pd1, prod, mgr);
                Products.createParameter(pd3, prod, mgr);

                OrganizationReference reference = mgr
                    .find(OrganizationReference.class, orgRef.getKey());
                MarketingPermission mp = new MarketingPermission();
                mp.setOrganizationReference(reference);
                mp.setTechnicalProduct(tp);
                mgr.persist(mp);

                marketingPermServ_getTechnicalProducts.add(tp);
                return prod;
            }
        });
    }

    protected void importProducts() throws Exception {
        createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        String productXml = "<?xml version='1.0' encoding='UTF-8'?>"
            + "<TechnicalProduct orgId=\"" + providerOrgId
            + "\" id=\"example\" version=\"1.00\">"

            + String.format(PRODUCT_FREE_XML_TEMPLATE, EXAMPLE_TRIAL)
            + String.format(PRODUCT_FREE_XML_TEMPLATE, EXAMPLE_STARTER)
            + String.format(Locale.US, PRODUCT_CHARGEABLE_XML_TEMPLATE,
            EXAMPLE_PROFESSIONAL, PricingPeriod.MONTH,
            BigDecimal.ZERO, BD100, BD100, BD100, BD100, BD100)
            + String.format(Locale.US, PRODUCT_CHARGEABLE_XML_TEMPLATE,
            EXAMPLE_ENTERPRISE, PricingPeriod.MONTH, BD100,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO)

            + "</TechnicalProduct>";

        String importProduct = importProduct(productXml, mgr);
        assertEquals(null, importProduct);
    }

    /**
     * Checks if the value of the attribute "onlyOneSubscriptionPerUser" is
     * equal to the value of the boolean "subscriptionRectriction".
     *
     * @param tpNode                  the technical product node from which to fetch the
     *                                "onlyOneSubscriptionPerUser" attribute.
     * @param subscriptionRestriction the boolean with which to compare the value of the attribute.
     */
    protected void compareSubscriptionRestriction(Node tpNode,
                                                  boolean subscriptionRestriction) {
        assertNotNull(tpNode);
        assertEquals(String.valueOf(subscriptionRestriction),
            tpNode.getAttributes()
                .getNamedItem("onlyOneSubscriptionPerUser")
                .getNodeValue());
    }

    protected void compareTP(VOTechnicalService tp, Node tpNode) {
        assertNotNull(tpNode);
        assertEquals(tp.getProvisioningUrl(), tpNode.getAttributes()
            .getNamedItem("provisioningUrl").getNodeValue());
        assertEquals(tp.getProvisioningVersion(), tpNode.getAttributes()
            .getNamedItem("provisioningVersion").getNodeValue());
        assertEquals("",
            tpNode.getAttributes().getNamedItem("build").getNodeValue());
        assertEquals(tp.getTechnicalServiceId(),
            tpNode.getAttributes().getNamedItem("id").getNodeValue());
        List<VOParameterDefinition> paramDefs = getOnlyProductParamters(tp);
        List<Node> parameterNodes = getNodes(tpNode.getChildNodes(),
            "ParameterDefinition");
        assertEquals(paramDefs.size(), parameterNodes.size());
        for (VOParameterDefinition def : paramDefs) {
            compareParam(def,
                getNodeWithId(parameterNodes, def.getParameterId()));
        }
        List<VOEventDefinition> eventDefs = getOnlyProductEvents(tp);
        List<Node> eventNodes = getNodes(tpNode.getChildNodes(), "Event");
        assertEquals(eventDefs.size(), eventNodes.size());
        for (VOEventDefinition def : eventDefs) {
            compareEvent(def, getNodeWithId(eventNodes, def.getEventId()));
        }
        List<VORoleDefinition> roleDefinitions = tp.getRoleDefinitions();
        List<Node> roleNodes = getNodes(tpNode.getChildNodes(), "Role");
        assertEquals(roleDefinitions.size(), roleNodes.size());
        for (VORoleDefinition def : roleDefinitions) {
            compareRole(def, getNodeWithId(roleNodes, def.getRoleId()));
        }
        List<String> tags = tp.getTags();
        List<Node> tagNodes = getNodes(tpNode.getChildNodes(), "LocalizedTag");
        assertEquals(tags.size(), tagNodes.size());
        for (String tag : tags) {
            assertNotNull(getNodeWithTag(roleNodes, "en", tag));
        }
    }

    protected void compareRole(VORoleDefinition def, Node roleNode) {
        assertNotNull(roleNode);
        checkLocalizedValues(def.getDescription(), roleNode.getChildNodes(),
            "LocalizedDescription");
        checkLocalizedValues(def.getName(), roleNode.getChildNodes(),
            "LocalizedName");
    }

    protected List<VOEventDefinition> getOnlyProductEvents(
        VOTechnicalService tp) {
        List<VOEventDefinition> result = new ArrayList<>();
        for (VOEventDefinition e : tp.getEventDefinitions()) {
            if (e.getEventType() != EventType.PLATFORM_EVENT) {
                result.add(e);
            }
        }
        return result;
    }

    protected void compareEvent(VOEventDefinition def, Node eventNode) {
        assertNotNull(eventNode);
        checkLocalizedValues(def.getEventDescription(),
            eventNode.getChildNodes(), "LocalizedDescription");
    }

    protected void compareParam(VOParameterDefinition def, Node paramNode) {
        assertNotNull(paramNode);
        String defaultValue = def.getDefaultValue();
        Node defaultValueAttribute = paramNode.getAttributes()
            .getNamedItem("default");
        if (defaultValue == null) {
            assertNull(defaultValueAttribute);
        } else {
            assertEquals(defaultValue, defaultValueAttribute.getNodeValue());
        }
        Long max = def.getMaxValue();
        Node maxAttribute = paramNode.getAttributes().getNamedItem("maxValue");
        if (max == null) {
            assertNull(maxAttribute);
        } else {
            assertEquals(String.valueOf(max.longValue()),
                maxAttribute.getNodeValue());
        }
        Long min = def.getMinValue();
        Node minAttribute = paramNode.getAttributes().getNamedItem("minValue");
        if (min == null) {
            assertNull(minAttribute);
        } else {
            assertEquals(String.valueOf(min.longValue()),
                minAttribute.getNodeValue());
        }
        assertEquals(def.getValueType().toString(), paramNode.getAttributes()
            .getNamedItem("valueType").getNodeValue());
        assertEquals(String.valueOf(def.isMandatory()), paramNode
            .getAttributes().getNamedItem("mandatory").getNodeValue());
        assertEquals(String.valueOf(def.isConfigurable()), paramNode
            .getAttributes().getNamedItem("configurable").getNodeValue());
        checkLocalizedValues(def.getDescription(), paramNode.getChildNodes(),
            "LocalizedDescription");
        List<VOParameterOption> options = def.getParameterOptions();
        List<Node> optionsNodes = getNodes(paramNode.getChildNodes(),
            "Options");
        if (options.isEmpty()) {
            assertTrue(optionsNodes.isEmpty());
        } else {
            assertEquals(1, optionsNodes.size());
            List<Node> optionNodes = getNodes(
                optionsNodes.get(0).getChildNodes(), "Option");
            assertEquals(options.size(), optionNodes.size());
            for (VOParameterOption option : options) {
                Node optionNode = getNodeWithId(optionNodes,
                    option.getOptionId());
                assertNotNull(optionNode);
                checkLocalizedValues(option.getOptionDescription(),
                    optionNode.getChildNodes(), "LocalizedOption");
            }
        }
    }

    protected void checkLocalizedValues(String value, NodeList childNodes,
                                        String nodeName) {
        for (int index = 0; index < childNodes.getLength(); index++) {
            Node item = childNodes.item(index);
            String nodeValue = item.getTextContent();
            if (item.getNodeName().equals(nodeName) && nodeValue != null
                && nodeValue.equals(value)) {
                return;
            }
        }
        fail(String.format("No '%s' with value '%s' found!", nodeName, value));
    }

    protected List<Node> getNodes(NodeList childNodes, String nodeName) {
        List<Node> result = new ArrayList<>();
        for (int index = 0; index < childNodes.getLength(); index++) {
            if (childNodes.item(index).getNodeName().equals(nodeName)) {
                result.add(childNodes.item(index));
            }
        }
        return result;
    }

    protected Node getNodeWithId(List<Node> list, String id) {
        for (Node node : list) {
            if (node.getAttributes().getNamedItem("id").getNodeValue()
                .equals(id)) {
                return node;
            }
        }
        return null;
    }

    protected Node getNodeWithTag(List<Node> list, String locale, String tag) {
        for (Node node : list) {
            if (node.getAttributes().getNamedItem("locale").getNodeValue()
                .equals(locale)
                && node.getAttributes().getNamedItem("value").getNodeValue()
                .equals(tag)) {
                return node;
            }
        }
        return null;
    }

    protected List<VOParameterDefinition> getOnlyProductParamters(
        VOTechnicalService tp) {
        List<VOParameterDefinition> result = new ArrayList<>();
        for (VOParameterDefinition p : tp.getParameterDefinitions()) {
            if (p.getParameterType() != ParameterType.PLATFORM_PARAMETER) {
                result.add(p);
            }
        }
        return result;
    }

    protected VOTechnicalService createTechnicalProduct(
        ServiceAccessType serviceAccessType, String loginPath,
        String billingId) throws Exception {
        VOTechnicalService vo = new VOTechnicalService();
        vo.setTechnicalServiceId("technicalProductId" + serviceAccessType);
        vo.setAccessType(serviceAccessType);
        vo.setBaseUrl("http://localhost");
        vo.setProvisioningUrl("");
        vo.setProvisioningVersion("1.0");
        vo.setLoginPath(loginPath);
        vo.setBillingIdentifier(billingId);

        VOTechnicalService voNew = svcProv.createTechnicalService(vo);

        List<VOTechnicalService> list = svcProv
            .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(vo.getTechnicalServiceId(),
            list.get(list.size() - 1).getTechnicalServiceId());
        assertEquals(vo.getAccessType(),
            list.get(list.size() - 1).getAccessType());
        return voNew;
    }

    protected List<VOLocalizedText> createLocalizedTexts(String text) {
        return createLocalizedTexts(text, new ArrayList<VOLocalizedText>());
    }

    protected List<VOLocalizedText> createLocalizedTexts(String text,
                                                         List<VOLocalizedText> existing) {
        Map<String, String> temp = new HashMap<>();
        temp.put("de", text + "-de");
        temp.put("en", text + "-en");
        temp.put("jp", text + "-jp");
        final List<VOLocalizedText> list = new ArrayList<>();
        for (VOLocalizedText txt : existing) {
            String remove = temp.remove(txt.getLocale());
            if (remove != null) {
                txt.setText(remove);
            }
            list.add(txt);
        }
        for (String locale : temp.keySet()) {
            String txt = temp.get(locale);
            list.add(new VOLocalizedText(locale, txt));
        }
        return list;
    }

    protected void assertLocalizedTexts(String expected,
                                        List<VOLocalizedText> actual) {
        assertEquals(toMap(createLocalizedTexts(expected)), toMap(actual));
    }

    protected Map<String, String> toMap(List<VOLocalizedText> texts) {
        Map<String, String> map = new HashMap<>();
        for (VOLocalizedText t : texts) {
            assertNull("duplicate locale " + t.getLocale(),
                map.put(t.getLocale(), t.getText()));
        }
        return map;
    }

    protected VOService getVOProduct(long key) {

        List<VOService> products = svcProv.getSuppliedServices();
        for (VOService product : products) {
            if (product.getKey() == key) {
                return product;
            }
        }
        return null;
    }

    protected void compareChargeablePriceModels(VOPriceModel expected,
                                                VOPriceModel actual) {
        assertEquals(expected.getPeriod(), actual.getPeriod());
        assertEquals(expected.getPricePerPeriod(), actual.getPricePerPeriod());

    }

    protected List<VOService> getServicesForLocalMarketplace(
        Organization supplier) {
        assertNotNull(supplier);
        // assumption for now
        String marketplaceId = supplier.getOrganizationId();
        return svcProv.getServicesForMarketplace(marketplaceId);
    }

    protected void checkParamsAndEvents(String technicalServiceId,
                                        Set<String> validParams, Set<String> invalidParams,
                                        Set<String> validEvents, Set<String> invalidEvents)
        throws Exception {
        List<VOTechnicalService> technicalProducts = createTechnicalProducts(
            svcProv);
        VOTechnicalService voTP = null;
        for (VOTechnicalService tp : technicalProducts) {
            if (technicalServiceId.equals(tp.getTechnicalServiceId())) {
                voTP = tp;
            }
        }
        if (voTP == null) {
            fail("technical product '" + technicalServiceId + "' not found");
        } else {
            // check params
            List<VOParameterDefinition> pDefs = voTP.getParameterDefinitions();
            Set<String> ids = new HashSet<>();
            for (VOParameterDefinition pDef : pDefs) {
                ids.add(pDef.getParameterId());
                if (invalidParams != null) {
                    assertFalse(invalidParams.contains(pDef.getParameterId()));
                }
            }
            if (validParams != null) {
                assertTrue(ids.containsAll(validParams));
            }
            // check events
            List<VOEventDefinition> eDefs = voTP.getEventDefinitions();
            ids = new HashSet<>();
            for (VOEventDefinition eDef : eDefs) {
                ids.add(eDef.getEventId());
                if (invalidEvents != null) {
                    assertFalse(invalidEvents.contains(eDef.getEventId()));
                }
            }
            if (validEvents != null) {
                assertTrue(ids.containsAll(validEvents));
            }
        }
    }

    protected void addProductsHavingEnumerationDataType(int numOfProducts)
        throws NonUniqueBusinessKeyException {
        // Enter new Products
        domObjects.clear();
        Organization organization = Organizations.findOrganization(mgr,
            providerOrgId);
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
            organization, "TP_WITH_ENUM_ID", false,
            ServiceAccessType.LOGIN);
        Product prod;
        for (int i = 1; i < numOfProducts; i++) {
            prod = new Product();
            prod.setVendor(organization);
            prod.setProductId("ProductWithEnum" + i);
            prod.setTechnicalProduct(tProd);
            prod.setProvisioningDate(TIMESTAMP);
            prod.setStatus(ServiceStatus.ACTIVE);
            prod.setType(ServiceType.TEMPLATE);
            prod.setAutoAssignUserEnabled(Boolean.FALSE);
            ParameterSet ps = new ParameterSet();
            prod.setParameterSet(ps);
            mgr.persist(prod);

            ParameterDefinition paramDef = new ParameterDefinition();
            paramDef.setParameterId("enumParam1");
            paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
            paramDef.setValueType(ParameterValueType.ENUMERATION);
            addParameterOptions(i, paramDef);
            paramDef.setTechnicalProduct(tProd);
            mgr.persist(paramDef);

            Parameter param = new Parameter();
            param.setParameterSet(ps);
            param.setValue("someValue");
            param.setConfigurable(true);
            param.setParameterDefinition(paramDef);
            param.setParameterSet(ps);
            mgr.persist(param);

            PriceModel pi = new PriceModel();
            prod.setPriceModel(pi);
            domObjects.add((Product) ReflectiveClone.clone(prod));
        }
    }

    protected void addParameterOptions(int optionNumber,
                                       ParameterDefinition paramDef) {
        List<ParameterOption> optionLists = new ArrayList<>();
        for (int ii = 0; ii < optionNumber; ii++) {
            ParameterOption po = new ParameterOption();
            po.setOptionId("" + ii);
            po.setParameterDefinition(paramDef);
            optionLists.add(po);
        }
        paramDef.setOptionList(optionLists);
    }

    protected void makeCompatible(Collection<VOService> sources,
                                  List<VOService> targets)
        throws ObjectNotFoundException, OrganizationAuthoritiesException,
        OperationNotPermittedException, ServiceCompatibilityException,
        ServiceStateException, ConcurrentModificationException {
        if (sources != null && targets != null) {
            for (VOService src : sources) {
                svcProv.setCompatibleServices(src, targets);
            }
        }
    }

    protected OrganizationReference createOrgRef(final long orgKey)
        throws Exception {
        return runTX(new Callable<OrganizationReference>() {
            @Override
            public OrganizationReference call() throws Exception {
                Organization organization = mgr.find(Organization.class,
                    orgKey);
                OrganizationReference orgRef = new OrganizationReference(
                    organization, organization,
                    OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
                mgr.persist(orgRef);
                return orgRef;
            }
        });
    }

    protected void createMarketingPermission(final String technicalServiceId)
        throws Exception {
        createMarketingPermission(technicalServiceId, null);
    }

    protected void createMarketingPermission(final String technicalServiceId,
                                             final Organization supplierOrg) throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                OrganizationReference ref;
                if (supplierOrg == null) {
                    ref = createOrgRef(
                        mgr.getCurrentUser().getOrganization().getKey());
                } else
                    ref = createOrgRef(supplierOrg.getKey());
                final long orgRefKey = ref.getKey();
                List<TechnicalProduct> technicalProducts = ParameterizedTypes
                    .list(mgr
                        .createNamedQuery(
                            "TechnicalProduct.getTechnicalProductsById")
                        .setParameter("technicalProductId",
                            technicalServiceId)
                        .getResultList(), TechnicalProduct.class);

                for (TechnicalProduct technicalProduct : technicalProducts) {
                    OrganizationReference reference = mgr
                        .find(OrganizationReference.class, orgRefKey);

                    MarketingPermission permission = new MarketingPermission();
                    permission.setOrganizationReference(reference);
                    permission.setTechnicalProduct(technicalProduct);
                    mgr.persist(permission);
                    mgr.flush();
                }
                return null;
            }
        });
    }

    protected void createMarketingPermission(final long tpKey,
                                             final long orgRefKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct technicalProduct = mgr
                    .find(TechnicalProduct.class, tpKey);
                OrganizationReference reference = mgr
                    .find(OrganizationReference.class, orgRefKey);

                MarketingPermission permission = new MarketingPermission();
                permission.setOrganizationReference(reference);
                permission.setTechnicalProduct(technicalProduct);
                mgr.persist(permission);
                return null;
            }
        });
    }

    /**
     * Add given been to container.
     */
    protected void addBean(TestContainer container, Object bean)
        throws Exception {
        container.addBean(bean);
    }

    protected List<VOCategory> createCategories(Marketplace mp, int number,
                                                String locale) throws Exception {

        // create categories
        List<VOCategory> categories = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            VOCategory category = new VOCategory();
            category.setCategoryId("categoryId_" + i + "_" + locale);
            category.setName("categoryName_" + i + "_" + locale);
            category.setMarketplaceId(mp.getMarketplaceId());
            categories.add(category);
        }

        // save categories
        categorizationService.saveCategories(categories, null, locale);

        // check categories
        categories = categorizationService.getCategories(mp.getMarketplaceId(),
            locale);
        assertEquals(number, categories.size());
        return categories;
    }

    protected void deleteEmptyTp_keyRecord(VOTechnicalService techProd)
        throws Exception {
        List<VOEventDefinition> events = techProd.getEventDefinitions();
        for (Iterator<VOEventDefinition> eventIterator = events
            .iterator(); eventIterator.hasNext(); ) {
            final VOEventDefinition voEventDef = eventIterator.next();

            Event e = runTX(new Callable<Event>() {
                @Override
                public Event call() throws Exception {
                    return mgr.find(Event.class, voEventDef.getKey());
                }
            });
            if (null == e.getTechnicalProduct_tkey()) {
                eventIterator.remove();
            }
        }
        List<VOParameterDefinition> paramDefinitions = techProd
            .getParameterDefinitions();
        for (Iterator<VOParameterDefinition> paramDefIterator = paramDefinitions
            .iterator(); paramDefIterator.hasNext(); ) {
            final VOParameterDefinition voParamDef = paramDefIterator.next();

            ParameterDefinition pdf = runTX(
                new Callable<ParameterDefinition>() {
                    @Override
                    public ParameterDefinition call() throws Exception {
                        return mgr.find(ParameterDefinition.class,
                            voParamDef.getKey());
                    }
                });
            if (null == pdf.getTechnicalProduct_tkey()) {
                paramDefIterator.remove();
            }
        }
    }
}
