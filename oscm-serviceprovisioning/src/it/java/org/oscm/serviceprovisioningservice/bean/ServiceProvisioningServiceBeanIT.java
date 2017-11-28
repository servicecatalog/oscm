/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.*;
import org.oscm.serviceprovisioningservice.assembler.ParameterDefinitionAssembler;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.data.*;
import org.oscm.types.enumtypes.PlatformEventIdentifier;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.persistence.Query;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.oscm.test.Numbers.BD100;

@SuppressWarnings("boxing")
public class ServiceProvisioningServiceBeanIT extends
        ServiceProvisioningServiceTestBase {

    /*
     * Test of saveProductLocalization method. Case is: input value is
     * VOProduct. Current organization has no OrganizationRoleType.SUPPLIER, but
     * CUSTOMER. calling supplier organization and owner of VOProduct is not the
     * same.
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testSaveProductLocalizationNotPermitted() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        // create product for the technical product
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOService productVO = createProduct(techProduct, PRODUCT_ID1,
                svcProv);
        container.login(providerUserKey, ROLE_SERVICE_MANAGER);
        svcProv.saveServiceLocalization(productVO, new VOServiceLocalization());
    }

    @Test(expected = EJBAccessException.class)
    public void testSaveProductLocalizationWrongUserRole() throws Exception {
        try {
            // create product for the technical product
            container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
            final VOService productVO = createProduct(techProduct, PRODUCT_ID1,
                    svcProv);
            container.login(providerUserKey, ROLE_MARKETPLACE_OWNER);
            svcProv.saveServiceLocalization(productVO,
                    new VOServiceLocalization());
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    /*
     * Test of saveProductLocalization method. Case is: input VOProduct is null,
     * will be NotFoundException situation.
     */
    @Test(expected = ObjectNotFoundException.class)
    public void testSaveProductLocalizationNotFound() throws Exception {
        svcProv.saveServiceLocalization(new VOService(),
                new VOServiceLocalization());
    }

    /*
     * Test of savePriceModelLocalization method. Case is: input VOProduct is
     * null, will be NotFoundException situation.
     */
    @Test(expected = ObjectNotFoundException.class)
    public void testSavePriceModelLocalizationNotFound() throws Exception {
        svcProv.savePriceModelLocalization(new VOPriceModel(),
                new VOPriceModelLocalization());
    }

    /*
     * Test of savePriceModelLocalization method. Case is: input value is
     * VOPriceModel without correct product.
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelLocalizationNotPermitted() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        // create product for the technical product
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOService productVO = createProduct(techProduct, PRODUCT_ID1,
                svcProv);
        VOPriceModel priceModel = prepareVOPriceModel(productVO, EUR);
        container.login(providerUserKey, ROLE_SERVICE_MANAGER);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                PlatformUsers.grantRoles(mgr, mgr.getCurrentUser(),
                        UserRoleType.SERVICE_MANAGER);
                return null;
            }
        });
        svcProv.savePriceModelLocalization(priceModel,
                new VOPriceModelLocalization());
    }

    @Test(expected = EJBAccessException.class)
    public void testSavePriceModelLocalizationWrongRole() throws Exception {
        try {

            // create product for the technical product
            container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
            final VOService productVO = createProduct(techProduct, PRODUCT_ID1,
                    svcProv);
            VOPriceModel priceModel = prepareVOPriceModel(productVO, EUR);
            container.login(providerUserKey, ROLE_MARKETPLACE_OWNER);
            svcProv.savePriceModelLocalization(priceModel,
                    new VOPriceModelLocalization());
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ValidationException.class)
    public void testSaveProductLocalization_ToLongName() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOService productVO = createProduct(techProduct, PRODUCT_ID1,
                svcProv);
        final VOServiceLocalization loc = new VOServiceLocalization();
        loc.setNames(createLocalizedTexts(TOO_LONG_NAME));
        loc.setDescriptions(createLocalizedTexts("desc"));
        svcProv.saveServiceLocalization(productVO, loc);
    }

    /*
     * Test of getPriceModelLocalization method. Case is: Unknown product
     */
    @Test(expected = ObjectNotFoundException.class)
    public void testGetProductLocalizationNotFound() throws Exception {
        svcProv.getServiceLocalization(new VOService());
    }

    /*
     * Test of getPriceModelLocalization method. Case is: Unknown price model
     */
    @Test(expected = ObjectNotFoundException.class)
    public void testGetPriceModelLocalizationNotFound() throws Exception {
        svcProv.getPriceModelLocalization(new VOPriceModel());
    }

    /*
     * Test of getProductLocalization method. Case is: current organization is
     * OrganizationRoleType.SUPPLIER and is supplier of the tested product.
     */
    @Test
    public void testGetProductLocalization() throws Exception {
        // current organization is a supplier for the product
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOService productVO = createProduct(techProduct, PRODUCT_ID1,
                svcProv);
        final VOServiceLocalization tmp = svcProv
                .getServiceLocalization(productVO);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                localizer.setLocalizedValues(productVO.getKey(),
                        LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                        createLocalizedTexts("name"));
                localizer.setLocalizedValues(productVO.getKey(),
                        LocalizedObjectTypes.PRODUCT_MARKETING_DESC,
                        createLocalizedTexts("desc", tmp.getDescriptions()));
                localizer.setLocalizedValues(productVO.getKey(),
                        LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION,
                        createLocalizedTexts("shortdesc"));
                localizer.setLocalizedValues(productVO.getKey(),
                        LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME,
                        createLocalizedTexts("customtabname"));
                return null;
            }
        });

        final VOServiceLocalization loc = svcProv
                .getServiceLocalization(productVO);
        assertLocalizedTexts("name", loc.getNames());
        assertLocalizedTexts("desc", loc.getDescriptions());
        assertLocalizedTexts("shortdesc", loc.getShortDescriptions());
        assertLocalizedTexts("customtabname", loc.getCustomTabNames());
    }

    /*
     * Test of getProductLocalization method. Case is: input value is VOProduct.
     * Current organization is customer of the supplier that owns the product.
     */
    @Test
    public void testGetDescriptionsVOProductCustomerRole() throws Exception {
        final VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        // create product for the technical product
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOService productVO = createProduct(techProduct, PRODUCT_ID1,
                svcProv);
        final VOServiceLocalization temp = svcProv
                .getServiceLocalization(productVO);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                localizer.setLocalizedValues(productVO.getKey(),
                        LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                        createLocalizedTexts("name"));
                localizer.setLocalizedValues(productVO.getKey(),
                        LocalizedObjectTypes.PRODUCT_MARKETING_DESC,
                        createLocalizedTexts("desc", temp.getDescriptions()));
                localizer.setLocalizedValues(productVO.getKey(),
                        LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION,
                        createLocalizedTexts("shortdesc"));
                localizer.setLocalizedValues(productVO.getKey(),
                        LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME,
                        createLocalizedTexts("customtabname"));
                return null;
            }

        });
        container.login(customerUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceLocalization loc = svcProv
                .getServiceLocalization(productVO);
        assertLocalizedTexts("name", loc.getNames());
        assertLocalizedTexts("desc", loc.getDescriptions());
        assertLocalizedTexts("shortdesc", loc.getShortDescriptions());
        assertLocalizedTexts("customtabname", loc.getCustomTabNames());
    }

    @Test
    /*
     * Simple test of deleting technical product.
     */
    public void testDeleteTechnicalProduct() throws Exception {
        List<VOTechnicalService> technicalProducts = createTechnicalProducts(svcProv);
        for (VOTechnicalService techProduct : technicalProducts) {

            verifyLocalizationTechnicalProduct(techProduct, false);
            svcProv.deleteTechnicalService(techProduct);
            verifyLocalizationTechnicalProduct(techProduct, true);

        }
        List<VOTechnicalService> techProducts = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals("Wrong number of technical products", 0,
                techProducts.size());
    }

    private void verifyLocalizationTechnicalProduct(
            VOTechnicalService techProduct, boolean deleted) throws Exception {
        String localText;
        String empty = "";
        String techDescr = empty;
        String loginAccessDesc = empty;
        String license = empty;

        if (deleted == false) {
            techDescr = techProduct.getTechnicalServiceDescription();
            loginAccessDesc = techProduct.getAccessInfo();
            license = techProduct.getLicense();
        }
        final long key = techProduct.getKey();

        final String locale = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return mgr.getCurrentUser().getLocale();
            }
        });

        localText = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase(locale, key,
                        LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC);
            }
        });
        assertEquals(techDescr, localText);

        localText = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase(locale, key,
                        LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC);
            }
        });
        assertEquals(loginAccessDesc, localText);

        localText = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase(locale, key,
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
            }
        });
        assertEquals(license, localText);
    }

    @Test
    /*
     * Test of deleting technical product and correct inserting referenced
     * objects to history tables. Deleting is OK.
     */
    public void testDeleteTechnicalProductAndAllReferencedObjects()
            throws Exception {
        final List<VOTechnicalService> voTechnicalProducts = createTechnicalProducts(svcProv);
        for (final VOTechnicalService voTechnicalProduct : voTechnicalProducts) {
            final List<ParameterDefinition> parameterDefinitionsResult = new ArrayList<ParameterDefinition>();
            final List<List<ParameterOption>> parameterOptionsResult = new ArrayList<List<ParameterOption>>();
            final List<Event> eventsResult = new ArrayList<Event>();
            // reading has to be done in transaction. start transaction
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    final TechnicalProduct technicalProduct = mgr
                            .find(TechnicalProduct.class,
                                    voTechnicalProduct.getKey());
                    // get list of ParameterDefinition objects
                    parameterDefinitionsResult.addAll(technicalProduct
                            .getParameterDefinitions());
                    // get list of ParameterOptions
                    for (ParameterDefinition parameterDefinition : parameterDefinitionsResult) {
                        final List<ParameterOption> parameterOptions = parameterDefinition
                                .getOptionList();
                        parameterOptionsResult.add(parameterOptions);
                    }
                    // get list of events
                    eventsResult.addAll(technicalProduct.getEvents());
                    // technical product deleting
                    svcProv.deleteTechnicalService(voTechnicalProduct);
                    return null;
                }
            });
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    // test correct inserting to history tables after referenced
                    // objects deleting
                    // test deleting of ParameterDefinition
                    for (ParameterDefinition parameterDefinition : parameterDefinitionsResult) {
                        final ParameterDefinition parameterDefinitionTemplate = new ParameterDefinition();
                        parameterDefinitionTemplate.setKey(parameterDefinition
                                .getKey());
                        final List<DomainHistoryObject<?>> histParDef = mgr
                                .findHistory(parameterDefinitionTemplate);
                        checkHistory(histParDef);
                    }

                    // test deleting of parameter options
                    for (List<ParameterOption> tmpList : parameterOptionsResult) {
                        for (ParameterOption option : tmpList) {
                            final ParameterOption parameterOptionTemplate = new ParameterOption();
                            parameterOptionTemplate.setKey(option.getKey());
                            final List<DomainHistoryObject<?>> histParamOption = mgr
                                    .findHistory(parameterOptionTemplate);
                            checkHistory(histParamOption);
                        }
                    }
                    // test deleting of events
                    for (Event event : eventsResult) {
                        final Event eventTemplate = new Event();
                        eventTemplate.setKey(event.getKey());
                        final List<DomainHistoryObject<?>> histEvent = mgr
                                .findHistory(eventTemplate);
                        checkHistory(histEvent);
                    }
                    return null;
                }
            });
        }
        List<VOTechnicalService> techProducts = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        // test deleting technical product
        Assert.assertEquals("Wrong number of technical products", 0,
                techProducts.size());
    }

    @Test
    /*
     * Test of deleting technical product with deleted market product and
     * references between products. Deleting is OK.
     */
    public void testDeleteTechnicalProductWithReferencedProducts()
            throws Exception {
        List<VOTechnicalService> technicalProducts = createTechnicalProducts(svcProv);
        for (VOTechnicalService techProduct : technicalProducts) {
            // create product for the technical product
            container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
            final VOService voProductSource = createProduct(techProduct,
                    PRODUCT_ID1, svcProv);
            final VOService voProductTarget = createProduct(techProduct,
                    PRODUCT_ID2, svcProv);
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    final Product sourceProduct = mgr.getReference(
                            Product.class, voProductSource.getKey());
                    final Product targetProduct = mgr.getReference(
                            Product.class, voProductTarget.getKey());
                    // make two reference between products with different
                    // direction
                    ProductReference productReference = new ProductReference(
                            sourceProduct, targetProduct);
                    mgr.persist(productReference);
                    ProductReference productReference2 = new ProductReference(
                            targetProduct, sourceProduct);
                    mgr.persist(productReference2);
                    sourceProduct.setStatus(ServiceStatus.DELETED);
                    targetProduct.setStatus(ServiceStatus.DELETED);
                    return null;
                }
            });

            container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
            svcProv.deleteTechnicalService(techProduct);
        }
        List<VOTechnicalService> techProducts = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals("Wrong number of technical products", 0,
                techProducts.size());
    }

    /*
     * Test of deleting technical product by user with not allowed role.
     * Exception is expected.
     */
    @Test(expected = OrganizationAuthoritiesException.class)
    public void testDeleteTechnicalProductWrongOrganizationRole()
            throws Exception {
        container.login(customerUserKey, ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product = new VOServiceDetails();
        product.setTechnicalService(new VOTechnicalService());
        svcProv.deleteTechnicalService(techProduct);
    }

    /*
     * Test of deleting technical product by user with not allowed role.
     * Exception is expected.
     */
    @Test(expected = EJBAccessException.class)
    public void testDeleteTechnicalProductWrongUserRole() throws Exception {
        try {
            container.login(providerUserKey, ROLE_SERVICE_MANAGER);
            VOServiceDetails product = new VOServiceDetails();
            product.setTechnicalService(new VOTechnicalService());
            svcProv.deleteTechnicalService(techProduct);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = DeletionConstraintException.class)
    /*
     * Test of deleting technical product with active market product. Deleting
     * is not allowed. Exception is expected.
     */
    public void testDeleteTechnicalProductWithProduct() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        createProduct(techProduct, PRODUCT_ID1, svcProv);

        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        svcProv.deleteTechnicalService(techProduct);
    }

    @Test
    /*
     * Test of deleting technical product with deleted market product. Deleting
     * is OK.
     */
    public void testDeleteTechnicalProductWithDeletedProduct() throws Exception {
        List<VOTechnicalService> technicalProducts = createTechnicalProducts(svcProv);
        for (VOTechnicalService techProduct : technicalProducts) {

            container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
            final VOService voProduct = createProduct(techProduct, PRODUCT_ID1,
                    svcProv);

            // update status of market product
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    final Product product = mgr.getReference(Product.class,
                            voProduct.getKey());
                    product.setStatus(ServiceStatus.DELETED);
                    return null;
                }
            });

            container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
            verifyLocalizationTechnicalProduct(techProduct, false);
            svcProv.deleteTechnicalService(techProduct);
            verifyLocalizationTechnicalProduct(techProduct, true);
        }
        List<VOTechnicalService> techProducts = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals("Wrong number of technical products", 0,
                techProducts.size());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeleteTechnicalProductWithDeletedProduct_Localization()
            throws Exception {
        List<VOTechnicalService> technicalProducts = createTechnicalProducts(svcProv);
        VOTechnicalService techProduct = technicalProducts.get(0);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOService voProduct = createProduct(techProduct, PRODUCT_ID1,
                svcProv);

        // update status of market product
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                final Product product = mgr.getReference(Product.class,
                        voProduct.getKey());
                product.setStatus(ServiceStatus.DELETED);
                return null;
            }
        });

        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        verifyLocalizationTechnicalProduct(techProduct, false);
        svcProv.deleteTechnicalService(techProduct);
        verifyLocalizationTechnicalProduct(techProduct, true);
        svcProv.getServiceLocalization(voProduct);
    }

    @Test(expected = DeletionConstraintException.class)
    /*
     * Test of deleting technical product with deleted market product and active
     * subscription. Deleting is not allowed. Exception is expected.
     */
    public void testDeleteTechnicalProductWithDeletedProductAndSubscription()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        // create product for the technical product
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOService voProduct = createProduct(techProduct, PRODUCT_ID1,
                svcProv);
        // create subscription for the market product
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Subscription subscription = Subscriptions.createSubscription(
                        mgr, customerOrgId, PRODUCT_ID1, SUBSCRIPTION_ID,
                        supplier);
                // update product copy
                final Product product = subscription.getProduct();
                product.setStatus(ServiceStatus.DELETED);
                // update of market product status for deleting
                final Product origProduct = mgr.getReference(Product.class,
                        voProduct.getKey());
                origProduct.setStatus(ServiceStatus.DELETED);
                return null;
            }
        });
        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        svcProv.deleteTechnicalService(techProduct);
    }

    @Test
    /*
     * Test of deleting technical product with deleted market product and
     * deleted subscription. Deleting is OK.
     */
    public void testDeleteTechnicalProductWithDeletedProductAndDeletedSubscription()
            throws Exception {
        List<VOTechnicalService> technicalProducts = createTechnicalProducts(svcProv);
        for (VOTechnicalService techProduct : technicalProducts) {
            // create product for the technical product
            container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
            final VOService voProduct = createProduct(techProduct, PRODUCT_ID1,
                    svcProv);
            // create subscription for the market product
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Subscription subscription = Subscriptions
                            .createSubscription(mgr, customerOrgId,
                                    PRODUCT_ID1, SUBSCRIPTION_ID, supplier);
                    subscription.setStatus(SubscriptionStatus.DEACTIVATED);
                    // update product copy
                    final Product product = subscription.getProduct();
                    product.setStatus(ServiceStatus.DELETED);
                    // update of market product status for deleting
                    final Product origProduct = mgr.getReference(Product.class,
                            voProduct.getKey());
                    origProduct.setStatus(ServiceStatus.DELETED);
                    return null;
                }
            });
            container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
            svcProv.deleteTechnicalService(techProduct);
        }
        List<VOTechnicalService> techProducts = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals("Wrong number of technical products", 0,
                techProducts.size());
    }

    @Test
    public void testGetServicesOfSupplier() throws Exception {
        createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        String productXml = "<?xml version='1.0' encoding='UTF-8'?>"
                + "<TechnicalProduct orgId=\""
                + providerOrgId
                + "\" id=\"example\" version=\"1.00\">"

                + String.format(PRODUCT_FREE_XML_TEMPLATE, EXAMPLE_TRIAL)
                + String.format(PRODUCT_FREE_XML_TEMPLATE, EXAMPLE_STARTER)
                + String.format(Locale.US, PRODUCT_CHARGEABLE_XML_TEMPLATE,
                        EXAMPLE_PROFESSIONAL, PricingPeriod.MONTH,
                        BigDecimal.ZERO, BD100, BD100, BD100, BD100, BD100)
                + String.format(Locale.US, PRODUCT_CHARGEABLE_XML_TEMPLATE,
                        EXAMPLE_ENTERPRISE, PricingPeriod.MONTH, BD100,
                        BigDecimal.ZERO, BD100, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO)

                + "</TechnicalProduct>";

        String importProduct = importProduct(productXml, mgr);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Query query = mgr
                        .createQuery("update Product p set p.dataContainer.status = :status");
                query.setParameter("status", ServiceStatus.ACTIVE);
                query.executeUpdate();

                long supplierKey = mgr.getCurrentUser().getOrganization()
                        .getKey();

                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(mgr.getCurrentUser().getOrganization()
                        .getOrganizationId());
                mp = (Marketplace) mgr.getReferenceByBusinessKey(mp);

                Product p = new Product();
                p.setProductId(EXAMPLE_STARTER);
                p.setVendorKey(supplierKey);
                p = (Product) mgr.getReferenceByBusinessKey(p);
                p.setAutoAssignUserEnabled(false);

                CatalogEntry ce = new CatalogEntry();
                ce.setProduct(p);
                ce.setAnonymousVisible(true);
                ce.setMarketplace(mp);
                mgr.persist(ce);

                p = new Product();
                p.setProductId(EXAMPLE_ENTERPRISE);
                p.setVendorKey(supplierKey);
                p = (Product) mgr.getReferenceByBusinessKey(p);
                p.setAutoAssignUserEnabled(false);

                ce = new CatalogEntry();
                ce.setProduct(p);
                ce.setAnonymousVisible(true);
                ce.setMarketplace(mp);
                mgr.persist(ce);

                p = new Product();
                p.setProductId(EXAMPLE_TRIAL);
                p.setVendorKey(supplierKey);
                p = (Product) mgr.getReferenceByBusinessKey(p);
                p.setAutoAssignUserEnabled(false);

                ce = new CatalogEntry();
                ce.setProduct(p);
                ce.setAnonymousVisible(false);
                ce.setMarketplace(mp);
                mgr.persist(ce);

                // create a global marketplace and add a catalog entry

                Marketplace global = new Marketplace();
                global.setMarketplaceId(GLOBAL_MARKETPLACE_NAME);
                try {
                    global = (Marketplace) mgr
                            .getReferenceByBusinessKey(global);
                } catch (ObjectNotFoundException e) {
                    global = Marketplaces.createGlobalMarketplace(mgr
                            .getCurrentUser().getOrganization(),
                            GLOBAL_MARKETPLACE_NAME, mgr);
                }

                p = new Product();
                p.setProductId(EXAMPLE_PROFESSIONAL);
                p.setVendorKey(supplierKey);
                p = (Product) mgr.getReferenceByBusinessKey(p);
                p.setAutoAssignUserEnabled(false);

                ce = new CatalogEntry();
                ce.setProduct(p);
                ce.setAnonymousVisible(true);
                ce.setMarketplace(global);
                mgr.persist(ce);

                return null;
            }
        });

        ArrayList<String> expectedOrder = new ArrayList<String>(
                Arrays.asList(new String[] { EXAMPLE_TRIAL, EXAMPLE_STARTER,
                        EXAMPLE_ENTERPRISE }));
        Assert.assertEquals(null, importProduct);
        List<VOService> products = svcProv
                .getServicesForMarketplace(supplierOrgId);

        Assert.assertEquals(3, products.size());
        for (int i = 0; i < 3; i++) {
            VOService product = products.get(i);
            String productId = product.getServiceId();
            Assert.assertEquals(expectedOrder.get(i), productId);
            VOService expected = COMPARE_VALUES.get(productId);
            Assert.assertNotNull(expected);
            Assert.assertEquals("", product.getName());
            Assert.assertEquals("", product.getDescription());
            VOPriceModel priceModel = expected.getPriceModel();
            Assert.assertEquals(priceModel.getClass(), product.getPriceModel()
                    .getClass());
            Assert.assertEquals("", product.getPriceModel().getDescription());
            Assert.assertEquals(priceModel.isChargeable(), product
                    .getPriceModel().isChargeable());
            if (priceModel.isChargeable()) {
                compareChargeablePriceModels(priceModel,
                        product.getPriceModel());
                Assert.assertEquals("Wrong currency stored", EUR, product
                        .getPriceModel().getCurrencyISOCode());
            }
        }
    }

    @Test
    public void testGetProducts() throws Exception {
        importProducts();
        container.login(customerUserKey, ROLE_SERVICE_MANAGER);
        List<VOService> products = getServicesForLocalMarketplace(supplier);
        Assert.assertEquals(0, products.size());
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        products = svcProv.getSuppliedServices();
        Assert.assertEquals(4, products.size());
        for (VOService product : products) {
            String productId = product.getServiceId();
            VOService expected = COMPARE_VALUES.get(productId);
            Assert.assertNotNull(expected);
            Assert.assertEquals("", product.getName());
            Assert.assertEquals("", product.getDescription());
            Assert.assertEquals(ServiceStatus.INACTIVE, product.getStatus());
            VOPriceModel priceModel = expected.getPriceModel();
            Assert.assertEquals(priceModel.getClass(), product.getPriceModel()
                    .getClass());
            Assert.assertEquals("", product.getPriceModel().getDescription());
            Assert.assertEquals(priceModel.isChargeable(), product
                    .getPriceModel().isChargeable());
            if (priceModel.isChargeable()) {
                compareChargeablePriceModels(priceModel,
                        product.getPriceModel());
                Assert.assertEquals("Wrong currency stored", EUR, product
                        .getPriceModel().getCurrencyISOCode());
            }
        }
    }

    @Test
    public void testGetProducts_Obsolete() throws Exception {
        importProducts();
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        supplier.getKey());
                List<Product> products = org.getProducts();
                for (Product product : products) {
                    product.setStatus(ServiceStatus.OBSOLETE);
                }
                return null;
            }
        });
        List<VOService> products = svcProv.getSuppliedServices();
        Assert.assertEquals(0, products.size());
    }

    @Test
    public void testGetProducts_Deleted() throws Exception {
        importProducts();
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        supplier.getKey());
                List<Product> products = org.getProducts();
                for (Product product : products) {
                    product.setStatus(ServiceStatus.DELETED);
                }
                return null;
            }
        });
        List<VOService> products = svcProv.getSuppliedServices();
        Assert.assertEquals(0, products.size());
    }

    @Test
    public void testGetProducts_Suspended() throws Exception {
        importProducts();
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        supplier.getKey());
                List<Product> products = org.getProducts();
                for (Product product : products) {
                    product.setStatus(ServiceStatus.SUSPENDED);
                }
                return null;
            }
        });
        List<VOService> products = svcProv.getSuppliedServices();
        Assert.assertEquals(4, products.size());
    }

    @Test
    public void testGetProducts_Active() throws Exception {
        importProducts();
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        supplier.getKey());
                List<Product> products = org.getProducts();
                for (Product product : products) {
                    product.setStatus(ServiceStatus.ACTIVE);
                }
                return null;
            }
        });
        List<VOService> products = svcProv.getSuppliedServices();
        Assert.assertEquals(4, products.size());
    }

    /**
     * Create a service that allows only one subscription. Subscribe to it and
     * list all services from the marketplace. The subscribed service must not
     * be on the list, because it cannot be subscribed again.
     * 
     * @throws Exception
     */
    @Test
    public void testGetProducts_filterAlreadySubscribed() throws Exception {

        // given a service that only allows one subscription
        VOServiceDetails voProduct1 = createServiceThatAllowsOnlyOneSubscription();
        createSubscription(voProduct1.getServiceId(), "sub1", customerOrgId);

        // when listing services in marketplace
        container.login(customerUserKey, ROLE_SERVICE_MANAGER);
        List<VOService> products = getServicesForLocalMarketplace(supplier);

        // then subscribed service must be filtered out
        assertEquals(1, products.size());
    }

    /**
     * Create a service that allows only one subscription. Subscribe to it, but
     * log in with different organization. The subscribed service must not be
     * removed from the result list of all services.
     * 
     * @throws Exception
     */
    @Test
    public void testGetProducts_noFilterWhenSubscribedByOtherOrg()
            throws Exception {

        // given a service that only allows one subscription
        VOServiceDetails voProduct1 = createServiceThatAllowsOnlyOneSubscription();
        createSubscription(voProduct1.getServiceId(), "sub1", customerOrgId);

        // when listing services in marketplace as different organization
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        List<VOService> products = getServicesForLocalMarketplace(supplier);

        // then subscribed service must not be filtered out
        assertEquals(1, products.size());
    }

    @Test
    public void testGetTechnicalProductsCheckDirectAccessParamsAndEvents()
            throws Exception {
        checkParamsAndEvents(
                "ssh",
                null,
                new HashSet<String>(Arrays.asList(new String[] {
                        PlatformParameterIdentifiers.NAMED_USER,
                        PlatformParameterIdentifiers.CONCURRENT_USER })),
                null,
                new HashSet<String>(Arrays.asList(new String[] {
                        PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                        PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE })));
    }

    @Test
    public void testGetTechnicalProductsCheckSamlAccessParamsAndEvents()
            throws Exception {
        checkParamsAndEvents(
                "saml",
                new HashSet<String>(
                        Arrays.asList(new String[] { PlatformParameterIdentifiers.NAMED_USER })),
                new HashSet<String>(
                        Arrays.asList(new String[] { PlatformParameterIdentifiers.CONCURRENT_USER })),
                null,
                new HashSet<String>(Arrays.asList(new String[] {
                        PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                        PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE })));
    }

    @Test(expected = EJBException.class)
    public void testUpdateMarketingProductNoSupplier() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp,
                "modifiedProductIdAddParam", svcProv);
        container.login(customerUserKey);
        svcProv.updateService(product, null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testUpdateMarketingProductNonExistingProduct() throws Exception {
        VOServiceDetails product = new VOServiceDetails();
        product.setTechnicalService(new VOTechnicalService());
        svcProv.updateService(product, null);
    }

    @Test
    public void testUpdateMarketingProductChangeId() throws Exception {
        final String customerOrgId = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                OrganizationReference ref = new OrganizationReference(
                        Organizations.findOrganization(mgr, supplierOrgId),
                        org, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                mgr.persist(ref);
                return org.getOrganizationId();
            }
        });

        container.login(providerUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService tp = createTechnicalProduct(svcProv);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOService createdProduct = createProduct(tp, "initialProductId",
                svcProv);

        // Create a customer specific version
        VOServiceDetails createdCustomerProductDetails = svcProv
                .getServiceDetails(createdProduct);
        VOOrganization voCustomerOrg = getOrganizationForOrgId(customerOrgId);
        VOPriceModel pm = createPriceModel();
        createdCustomerProductDetails = svcProv.savePriceModelForCustomer(
                createdCustomerProductDetails, pm, voCustomerOrg);

        VOServiceDetails productDetails = svcProv
                .getServiceDetails(createdProduct);
        productDetails.setServiceId("modifiedProductId");
        VOServiceDetails updateMarketingProduct = svcProv.updateService(
                productDetails, null);

        createdCustomerProductDetails = svcProv
                .getServiceDetails(createdCustomerProductDetails);

        Assert.assertEquals("Wrong product id", "modifiedProductId",
                updateMarketingProduct.getServiceId());
        Assert.assertEquals("Wrong product id for customer specific copy",
                "modifiedProductId",
                createdCustomerProductDetails.getServiceId());
    }

    @Test
    public void testUpdateMarketingProductProvideSameIdDifferentMarketingDesc()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService product = new VOService();
        product.setDescription("marketing desc");
        product.setServiceId("modifiedProductIdIdentical");

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOService createdProduct = svcProv.createService(tp, product, null);

        VOServiceDetails productDetails = svcProv
                .getServiceDetails(createdProduct);
        productDetails.setServiceId("modifiedProductIdIdentical");
        productDetails.setDescription("new marketing description");
        VOServiceDetails updateMarketingProduct = svcProv.updateService(
                productDetails, null);

        Assert.assertEquals("Wrong product id", "modifiedProductIdIdentical",
                updateMarketingProduct.getServiceId());
        Assert.assertEquals("Wrong marketing description",
                "new marketing description",
                updateMarketingProduct.getDescription());
    }

    @Test
    public void testUpdateMarketingProductAddParameters() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp,
                "modifiedProductIdAddParam", svcProv);

        List<VOParameterDefinition> parameterDefinitions = tp
                .getParameterDefinitions();
        Assert.assertTrue(
                "No parameter definitions found for technical product",
                0 < parameterDefinitions.size());
        // product has no parameters so far, so add one
        VOParameterDefinition parameterDefinition = parameterDefinitions.get(0);
        VOParameter parameter = new VOParameter(parameterDefinition);
        parameter.setValue("1223");
        parameter.setConfigurable(true);

        List<VOParameter> parameters = new ArrayList<VOParameter>();
        parameters.add(parameter);
        product.setParameters(parameters);
        product = svcProv.updateService(product, null);

        Assert.assertEquals("Parameter has not been persisted", 1, product
                .getParameters().size());
        Assert.assertEquals("Wrong parameter value", "1223", product
                .getParameters().get(0).getValue());
        Assert.assertEquals("Wrong configuration value for the parameter",
                true, product.getParameters().get(0).isConfigurable());
    }

    @Test
    public void testUpdateMarketingProductAddParametersToExistingOnes()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService product = new VOService();
        product.setDescription("marketing desc");
        product.setServiceId("modifiedProductIdAddParamToExisting");
        List<VOParameter> parameters = new ArrayList<VOParameter>();
        // product has no parameters so far, so add one
        List<VOParameterDefinition> parameterDefinitions = tp
                .getParameterDefinitions();
        VOParameterDefinition parameterDefinition = parameterDefinitions.get(0);
        VOParameter parameter = new VOParameter(parameterDefinition);
        parameter.setValue("1223");
        parameter.setConfigurable(true);
        parameters.add(parameter);
        product.setParameters(parameters);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails createdProduct = svcProv.createService(tp, product,
                null);

        final VOParameter originalParameter = createdProduct.getParameters()
                .get(0);

        parameters = createdProduct.getParameters();
        VOParameter newParam = new VOParameter(parameterDefinitions.get(1));
        newParam.setValue("4321");
        parameter.setConfigurable(false);
        parameters.add(newParam);

        // add a second new parameter
        parameters = createdProduct.getParameters();
        newParam = new VOParameter(parameterDefinitions.get(2));
        newParam.setValue("86400000");
        parameter.setConfigurable(false);
        parameters.add(newParam);

        // and change the value of the existing parameter
        parameters.get(0).setValue("1233");

        createdProduct = svcProv.updateService(createdProduct, null);

        assertEquals("Parameter has not been persisted", 3, createdProduct
                .getParameters().size());
        assertEquals("Wrong parameter value", "1233", createdProduct
                .getParameters().get(0).getValue());
        assertEquals("Wrong parameter value", true, createdProduct
                .getParameters().get(0).isConfigurable());
        assertEquals("Wrong key", originalParameter.getKey(), createdProduct
                .getParameters().get(0).getKey());

        assertEquals("Wrong parameter value", "4321", createdProduct
                .getParameters().get(1).getValue());
        assertEquals("Wrong parameter value", false, createdProduct
                .getParameters().get(1).isConfigurable());

        assertEquals("Wrong parameter value", "86400000", createdProduct
                .getParameters().get(2).getValue());
        assertEquals("Wrong parameter value", false, createdProduct
                .getParameters().get(2).isConfigurable());

    }

    @Test
    public void testUpdateMarketingProductModifyParameter() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp,
                "modifiedProductIdAddParam", svcProv);

        List<VOParameterDefinition> parameterDefinitions = tp
                .getParameterDefinitions();
        assertTrue("No parameter definitions found for technical product",
                0 < parameterDefinitions.size());
        // product has no parameters so far, so add one
        VOParameterDefinition parameterDefinition = parameterDefinitions.get(0);
        VOParameter parameter = new VOParameter(parameterDefinition);
        parameter.setValue("1223");
        parameter.setConfigurable(true);

        List<VOParameter> parameters = new ArrayList<VOParameter>();
        parameters.add(parameter);
        product.setParameters(parameters);
        product = svcProv.updateService(product, null);

        final VOParameter paramBefore = product.getParameters().get(0);

        paramBefore.setValue("5555");

        product = svcProv.updateService(product, null);

        final VOParameter paramAfter = product.getParameters().get(0);

        assertEquals("5555", paramAfter.getValue());

        // But the key must still be the same:
        assertEquals(paramBefore.getKey(), paramAfter.getKey());
    }

    @Test
    public void testUpdateMarketingProductRemoveParameter() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOParameterDefinition> parameterDefinitions = techProduct
                .getParameterDefinitions();

        VOService voProduct1 = new VOService();
        voProduct1.setServiceId("product1");
        List<VOParameter> params = new ArrayList<VOParameter>();
        VOParameter p1 = new VOParameter(parameterDefinitions.get(0));
        p1.setValue("3");
        p1.setConfigurable(true);
        params.add(p1);
        VOParameter p2 = new VOParameter(parameterDefinitions.get(1));
        p2.setValue("5");
        p2.setConfigurable(true);
        params.add(p2);
        voProduct1.setParameters(params);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);

        voProduct1 = svcProv.createService(techProduct, voProduct1, null);
        VOServiceDetails productDetails = svcProv.getServiceDetails(voProduct1);

        // Create a Price Model
        final VOPriceModel priceModel = createChargeablePriceModel();
        VOPricedParameter pp1 = new VOPricedParameter(
                parameterDefinitions.get(0));
        pp1.setPricePerUser(BigDecimal.valueOf(123));
        priceModel.getSelectedParameters().add(pp1);
        VOPricedParameter pp2 = new VOPricedParameter(
                parameterDefinitions.get(1));
        pp2.setPricePerUser(BigDecimal.valueOf(456));
        priceModel.getSelectedParameters().add(pp2);

        productDetails = svcProv.savePriceModel(productDetails, priceModel);

        // remove a parameter and set the others value to null and make it
        // non-configurable, then update. Both entries must disappear
        VOParameter parameter = productDetails.getParameters().get(0);
        parameter.setValue(null);
        parameter.setConfigurable(false);
        productDetails.setParameters(Arrays
                .asList(new VOParameter[] { parameter }));

        productDetails = svcProv.updateService(productDetails, null);

        assertEquals("Parameter for removed parameters have not been removed",
                0, productDetails.getParameters().size());
        assertEquals(
                "PricedParameter for removed parameters have not been removed",
                0, productDetails.getPriceModel().getSelectedParameters()
                        .size());
    }

    @Test
    public void testUpdateMarketingProductRemoveParameterWithPricedParameter()
            throws Exception {
        // Create a Technical Service
        VOTechnicalService techService = createTechnicalProduct(svcProv);
        final VOParameterDefinition paramDef = techService
                .getParameterDefinitions().get(0);

        // Create a Service
        VOService voService = new VOService();
        voService.setServiceId("product1");
        VOParameter p1 = new VOParameter(paramDef);
        p1.setValue("3");
        p1.setConfigurable(true);
        voService.getParameters().add(p1);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);

        voService = svcProv.createService(techService, voService, null);
        VOServiceDetails serviceDetails = svcProv.getServiceDetails(voService);

        // Create a Price Model
        final VOPriceModel priceModel = createChargeablePriceModel();
        final VOPricedParameter pricedParameter = new VOPricedParameter(
                paramDef);
        pricedParameter.setPricePerUser(BigDecimal.valueOf(123));
        priceModel.getSelectedParameters().add(pricedParameter);
        serviceDetails = svcProv.savePriceModel(serviceDetails, priceModel);

        serviceDetails.setParameters(new ArrayList<VOParameter>());
        serviceDetails = svcProv.updateService(serviceDetails, null);

        assertEquals("Parameter for removed parameters have not been removed",
                0, serviceDetails.getParameters().size());
        assertEquals(
                "PricedParameter for removed parameters have not been removed",
                0, serviceDetails.getPriceModel().getSelectedParameters()
                        .size());
    }

    @Test
    public void testUpdateMarketingProductParameterWithPricedParameter()
            throws Exception {
        // Create a Technical Service
        VOTechnicalService techService = createTechnicalProduct(svcProv);
        final VOParameterDefinition paramDef = techService
                .getParameterDefinitions().get(0);

        // Create a Service
        VOService voService = new VOService();
        voService.setServiceId("product1");
        VOParameter p1 = new VOParameter(paramDef);
        p1.setValue("3");
        p1.setConfigurable(true);
        voService.getParameters().add(p1);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);

        voService = svcProv.createService(techService, voService, null);
        VOServiceDetails serviceDetails = svcProv.getServiceDetails(voService);

        // Create a Price Model
        final VOPriceModel priceModel = createChargeablePriceModel();
        final VOPricedParameter pricedParameter = new VOPricedParameter(
                paramDef);
        pricedParameter.setPricePerUser(BigDecimal.valueOf(123));
        priceModel.getSelectedParameters().add(pricedParameter);
        serviceDetails = svcProv.savePriceModel(serviceDetails, priceModel);

        // as prices for non-configurable parameters will not be shown, priced
        // parameters must be removed for parameters made non-configurable
        p1 = serviceDetails.getParameters().get(0);
        p1.setConfigurable(false);
        serviceDetails.setParameters(Arrays.asList(new VOParameter[] { p1 }));
        serviceDetails = svcProv.updateService(serviceDetails, null);

        assertEquals("Parameter must still exist", 1, serviceDetails
                .getParameters().size());
        assertEquals(
                "PricedParameter for non-configurable parameter has not been removed",
                0, serviceDetails.getPriceModel().getSelectedParameters()
                        .size());
    }

    @Test
    public void testUpdateMarketingProductSetInvalidParameter()
            throws Exception {
        final VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOParameterDefinition> parameterDefinitions = techProduct
                .getParameterDefinitions();

        VOService voProduct1 = new VOService();
        voProduct1.setServiceId("product1");
        List<VOParameter> voParams = new ArrayList<VOParameter>();
        VOParameter p1 = new VOParameter(parameterDefinitions.get(0));
        p1.setValue("3");
        voParams.add(p1);
        VOParameter p2 = new VOParameter(parameterDefinitions.get(1));
        p2.setValue("5");
        voParams.add(p2);
        voProduct1.setParameters(voParams);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);

        voProduct1 = svcProv.createService(techProduct, voProduct1, null);

        // use the product from the previous test case
        VOServiceDetails productDetails = svcProv.getServiceDetails(voProduct1);

        VOParameterDefinition foreignParamDef = runTX(new Callable<VOParameterDefinition>() {

            @Override
            public VOParameterDefinition call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrgId);

                TechnicalProduct otherProduct = new TechnicalProduct();
                otherProduct.setTechnicalProductId("otherId");
                otherProduct.setOrganization(org);
                otherProduct.setProvisioningURL("http://fujitsu.com/");
                otherProduct.setProvisioningVersion("1.0");
                otherProduct
                        .setBillingIdentifier(BillingAdapterIdentifier.NATIVE_BILLING
                                .toString());
                mgr.persist(otherProduct);

                ParameterDefinition paramDef = new ParameterDefinition();
                paramDef.setParameterId("bla");
                paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
                paramDef.setValueType(ParameterValueType.STRING);
                paramDef.setTechnicalProduct(otherProduct);
                mgr.persist(paramDef);
                LocalizerFacade facade = new LocalizerFacade(localizer, "en");
                return ParameterDefinitionAssembler.toVOParameterDefinition(
                        paramDef, facade);
            }
        });

        VOParameter param = new VOParameter(foreignParamDef);
        param.setValue("13");
        List<VOParameter> params = new ArrayList<VOParameter>();
        params.add(param);

        productDetails.setParameters(params);

        try {
            svcProv.updateService(productDetails, null);
            Assert.fail("Operation must fail, as the specified parameter is not compatible to the current product.");
        } catch (OperationNotPermittedException e) {
        }
    }

    /**
     * Tries to create a customer specific price model, although the specified
     * organization intended as customer is not authorized as customer. The
     * operation is expected to fail.
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForCustomerNonAuthorizedCustomer()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);

        Organization org = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                Organization createdOrganization = Organizations
                        .createOrganization(mgr);
                return createdOrganization;
            }
        });

        svcProv.savePriceModelForCustomer(product, new VOPriceModel(),
                getOrganizationForOrgId(org.getOrganizationId()));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForCustomerCustomerWithoutSupplier()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);

        Organization org = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                Organization createdOrganization = Organizations
                        .createOrganization(mgr, OrganizationRoleType.CUSTOMER);
                return createdOrganization;
            }
        });

        svcProv.savePriceModelForCustomer(product, new VOPriceModel(),
                getOrganizationForOrgId(org.getOrganizationId()));
    }

    @Test(expected = ServiceStateException.class)
    public void testUpdateMarketingProduct_Active() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);

        publishToLocalMarketplaceSupplier(product, mpSupplier);
        svcProv.activateService(product);
        VOServiceDetails voDetails = svcProv.getServiceDetails(product);
        Assert.assertEquals(ServiceStatus.ACTIVE, voDetails.getStatus());

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        svcProv.updateService(voDetails, null);
    }

    @Test(expected = ServiceStateException.class)
    public void testUpdateMarketingProduct_Deleted() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        setProductStatus(ServiceStatus.DELETED, product.getKey());
        product.setVersion(product.getVersion() + 1);
        svcProv.updateService(product, null);
    }

    @Test(expected = ServiceStateException.class)
    public void testUpdateMarketingProduct_Obsolete() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        setProductStatus(ServiceStatus.OBSOLETE, product.getKey());
        product.setVersion(product.getVersion() + 1);
        svcProv.updateService(product, null);
    }

    @Test
    public void testSavePriceModelForCustomerDescriptionRemovedNotChargeable()
            throws Exception {
        VOServiceDetails service = prepareService();
        VOPriceModel priceModel = service.getPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setDescription("description");
        priceModel.setCurrencyISOCode("EUR");
        List<VOPricedParameter> selectedParameters = priceModel
                .getSelectedParameters();
        VOPricedParameter pricedParameter = selectedParameters.get(0);
        pricedParameter.setPricePerUser(BigDecimal.valueOf(7777L));
        priceModel.setSelectedParameters(selectedParameters);
        VOServiceDetails customerProduct = svcProv.savePriceModelForCustomer(
                service, priceModel, secondCustomer);

        service = svcProv.getServiceDetails(service);
        VOPriceModel copyPriceModel = customerProduct.getPriceModel();
        Assert.assertNotNull(copyPriceModel);
        Assert.assertEquals("description", copyPriceModel.getDescription());

        copyPriceModel.setType(PriceModelType.FREE_OF_CHARGE);

        VOServiceDetails customerProduct2 = svcProv.savePriceModelForCustomer(
                customerProduct, copyPriceModel, secondCustomer);
        VOPriceModel copyPriceModel2 = customerProduct2.getPriceModel();
        Assert.assertNotNull(copyPriceModel);
        Assert.assertEquals("description", copyPriceModel2.getDescription());
    }

    @Test
    public void testGetProductsForCustomerWithInactiveTemplate()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(techProduct, "product1",
                svcProv);
        VOServiceDetails voProduct2 = createProduct(techProduct, "product2",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(voProduct1, priceModel);
        svcProv.savePriceModel(voProduct2, priceModel);

        voProduct1 = publishToLocalMarketplaceSupplier(voProduct1, mpSupplier);
        svcProv.activateService(voProduct1);

        container.login(customerUserKey, ROLE_SERVICE_MANAGER);
        List<VOService> products = getServicesForLocalMarketplace(supplier);
        Assert.assertEquals(1, products.size());
        Assert.assertEquals(voProduct1.getServiceId(), products.get(0)
                .getServiceId());
        Assert.assertEquals(ServiceStatus.ACTIVE, products.get(0).getStatus());
    }

    @Test
    public void testGetProductsForCustomerWithInactiveTemplateAndInactiveCustomerSpecific()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(techProduct, "product1",
                svcProv);
        VOServiceDetails voProduct2 = createProduct(techProduct, "product2",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        voProduct1 = svcProv.savePriceModel(voProduct1, priceModel);
        voProduct2 = svcProv.savePriceModel(voProduct2, priceModel);

        // the first time must work because a copy is created from the inactive
        // template
        VOOrganization org = getOrganizationForOrgId(customerOrgId);
        VOPriceModel pm = createPriceModel();
        svcProv.savePriceModelForCustomer(voProduct1, pm, org);

        publishToLocalMarketplaceSupplier(voProduct2, mpSupplier);
        svcProv.activateService(voProduct2);

        container.login(customerUserKey, ROLE_SERVICE_MANAGER);
        List<VOService> products = getServicesForLocalMarketplace(supplier);
        Assert.assertEquals(1, products.size());
        Assert.assertEquals(voProduct2.getServiceId(), products.get(0)
                .getServiceId());
        Assert.assertEquals(ServiceStatus.ACTIVE, products.get(0).getStatus());
    }

    @Test
    public void testGetProductsForCustomerWithActiveTemplateAndInactiveCustomerSpecific()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(techProduct, "product1",
                svcProv);
        VOServiceDetails voProduct2 = createProduct(techProduct, "product2",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        voProduct1 = svcProv.savePriceModel(voProduct1, priceModel);
        voProduct2 = svcProv.savePriceModel(voProduct2, priceModel);

        publishToLocalMarketplaceSupplier(voProduct1, mpSupplier);
        publishToLocalMarketplaceSupplier(voProduct2, mpSupplier);
        svcProv.activateService(voProduct1);
        svcProv.activateService(voProduct2);

        VOServiceDetails voDetails = svcProv.getServiceDetails(voProduct1);
        // the first time must work because a copy is created from the inactive
        // template
        VOOrganization org = getOrganizationForOrgId(customerOrgId);
        VOPriceModel pm = createPriceModel();
        svcProv.savePriceModelForCustomer(voDetails, pm, org).getKey();

        container.login(customerUserKey, ROLE_SERVICE_MANAGER);
        List<VOService> services = getServicesForLocalMarketplace(supplier);
        Assert.assertEquals(1, services.size());
        Assert.assertEquals(ServiceStatus.ACTIVE, services.get(0).getStatus());
        Assert.assertEquals(voProduct2.getKey(), services.get(0).getKey());
    }

    @Test
    public void testGetProductsForCustomerWithInactiveTemplateAndActiveCustomerSpecific()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(techProduct, "product1",
                svcProv);
        VOServiceDetails voProduct2 = createProduct(techProduct, "product2",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        voProduct1 = svcProv.savePriceModel(voProduct1, priceModel);
        voProduct2 = svcProv.savePriceModel(voProduct2, priceModel);
        publishToLocalMarketplaceSupplier(voProduct1, mpSupplier);
        publishToLocalMarketplaceSupplier(voProduct2, mpSupplier);

        svcProv.activateService(voProduct2);
        long key = voProduct1.getKey();

        VOOrganization org = getOrganizationForOrgId(customerOrgId);
        VOPriceModel pm = createPriceModel();
        VOServiceDetails voProductCust = svcProv.savePriceModelForCustomer(
                voProduct1, pm, org);
        svcProv.activateService(voProductCust);

        container.login(customerUserKey, ROLE_SERVICE_MANAGER);
        List<VOService> products = getServicesForLocalMarketplace(supplier);
        Assert.assertEquals(2, products.size());
        Assert.assertEquals(ServiceStatus.ACTIVE, products.get(0).getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, products.get(1).getStatus());
        Assert.assertFalse(products.get(0).getKey() == key);
        Assert.assertFalse(products.get(1).getKey() == key);
    }

    @Test(expected = ServiceOperationException.class)
    public void testSavePriceModelForCustomerCreateSecond() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(techProduct, "product1",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        voProduct1 = svcProv.savePriceModel(voProduct1, priceModel);

        VOOrganization org = getOrganizationForOrgId(customerOrgId);

        svcProv.savePriceModelForCustomer(voProduct1, priceModel, org);
        svcProv.savePriceModelForCustomer(voProduct1, priceModel, org);
    }

    @Test
    public void testLoadParameterOptionsAfterReImport() throws Exception {
        final LocalizerFacade facade = new LocalizerFacade(localizer, "en");
        final VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        runTX(new Callable<DomainObject<?>>() {

            @Override
            public DomainObject<?> call() throws Exception {
                TechnicalProduct find = mgr.find(TechnicalProduct.class,
                        techProduct.getKey());

                List<ParameterDefinition> parameterDefinitions = find
                        .getParameterDefinitions();
                Assert.assertEquals("wrong number of parameter found", 7,
                        parameterDefinitions.size());

                ParameterDefinition pramDef = parameterDefinitions.get(0);
                Assert.assertEquals("MAX_FILE_NUMBER", pramDef.getParameterId());
                Assert.assertEquals(ParameterValueType.INTEGER,
                        pramDef.getValueType());
                Assert.assertEquals(1l, pramDef.getMinimumValue().longValue());
                Assert.assertEquals(10l, pramDef.getMaximumValue().longValue());
                Assert.assertEquals("5", pramDef.getDefaultValue());
                Assert.assertTrue(pramDef.isConfigurable());
                Assert.assertFalse(pramDef.isMandatory());
                Assert.assertTrue(pramDef.getOptionList().isEmpty());

                pramDef = parameterDefinitions.get(1);
                Assert.assertEquals("HAS_OPTIONS", pramDef.getParameterId());
                Assert.assertEquals(ParameterValueType.ENUMERATION,
                        pramDef.getValueType());
                Assert.assertEquals("2", pramDef.getDefaultValue());
                Assert.assertTrue(pramDef.isConfigurable());
                Assert.assertFalse(pramDef.isMandatory());

                List<ParameterOption> optionsList = pramDef.getOptionList();
                Assert.assertEquals("wrong number of parameter options found",
                        3, optionsList.size());
                ParameterOption option = optionsList.get(0);
                Assert.assertEquals("1", option.getOptionId());
                Assert.assertEquals("Minimum Storage", facade.getText(
                        option.getKey(),
                        LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC));
                option = optionsList.get(1);
                Assert.assertEquals("2", option.getOptionId());
                Assert.assertEquals("Optimum storage", facade.getText(
                        option.getKey(),
                        LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC));

                option = optionsList.get(2);
                Assert.assertEquals("3", option.getOptionId());
                Assert.assertEquals("Maximum storage", facade.getText(
                        option.getKey(),
                        LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC));
                return find;
            }
        });
    }

    @Test
    public void testSaveTechnicalProduct() throws Exception {
        VOTechnicalService techProd = createTechnicalProduct(svcProv);
        Assert.assertNotNull(techProd);
        final String valueToSet = "someValue";
        String license = techProd.getLicense();
        techProd.setAccessInfo(valueToSet);
        techProd.setTechnicalServiceDescription(valueToSet);
        List<VOEventDefinition> eventDefinitions = techProd
                .getEventDefinitions();
        for (VOEventDefinition event : eventDefinitions) {
            event.setEventDescription(valueToSet);
        }
        List<VOParameterDefinition> parameterDefinitions = techProd
                .getParameterDefinitions();
        for (VOParameterDefinition parameter : parameterDefinitions) {
            parameter.setDescription(valueToSet);
            List<VOParameterOption> parameterOptions = parameter
                    .getParameterOptions();
            for (VOParameterOption option : parameterOptions) {
                option.setOptionDescription(valueToSet);
            }
        }
        List<VORoleDefinition> roleDefinitions = techProd.getRoleDefinitions();
        for (VORoleDefinition role : roleDefinitions) {
            role.setDescription(valueToSet);
            role.setName(valueToSet);
        }
        List<VOTechnicalServiceOperation> ops = techProd
                .getTechnicalServiceOperations();
        for (VOTechnicalServiceOperation op : ops) {
            op.setOperationDescription(valueToSet);
            op.setOperationName(valueToSet);
            List<VOServiceOperationParameter> operationParameters = op
                    .getOperationParameters();
            for (VOServiceOperationParameter p : operationParameters) {
                p.setParameterName(valueToSet);
            }
        }
        List<String> tags = new ArrayList<String>();
        tags.add("tag1");
        tags.add("tag2");
        techProd.setTags(tags);

        deleteEmptyTp_keyRecord(techProd);
        svcProv.saveTechnicalServiceLocalization(techProd);
        techProd = svcProv.getTechnicalServices(
                OrganizationRoleType.TECHNOLOGY_PROVIDER).get(0);
        Assert.assertEquals(valueToSet, techProd.getAccessInfo());
        Assert.assertEquals(valueToSet,
                techProd.getTechnicalServiceDescription());
        Assert.assertEquals(license, techProd.getLicense());
        eventDefinitions = techProd.getEventDefinitions();
        for (VOEventDefinition event : eventDefinitions) {
            if (event.getEventType() != EventType.PLATFORM_EVENT) {
                Assert.assertEquals(valueToSet, event.getEventDescription());
            }
        }
        parameterDefinitions = techProd.getParameterDefinitions();
        for (VOParameterDefinition parameter : parameterDefinitions) {
            if (parameter.getParameterType() != ParameterType.PLATFORM_PARAMETER) {
                Assert.assertEquals(valueToSet, parameter.getDescription());
                List<VOParameterOption> parameterOptions = parameter
                        .getParameterOptions();
                for (VOParameterOption option : parameterOptions) {
                    Assert.assertEquals(valueToSet,
                            option.getOptionDescription());
                }
            }
        }
        roleDefinitions = techProd.getRoleDefinitions();
        for (VORoleDefinition role : roleDefinitions) {
            Assert.assertEquals(valueToSet, role.getDescription());
            Assert.assertEquals(valueToSet, role.getName());
        }
        ops = techProd.getTechnicalServiceOperations();
        for (VOTechnicalServiceOperation op : ops) {
            Assert.assertEquals(valueToSet, op.getOperationDescription());
            Assert.assertEquals(valueToSet, op.getOperationName());
            List<VOServiceOperationParameter> operationParameters = op
                    .getOperationParameters();
            for (VOServiceOperationParameter p : operationParameters) {
                assertEquals(valueToSet, p.getParameterName());
            }

        }
        tags = techProd.getTags();
        assertEquals(2, tags.size());
        assertTrue(tags.contains("tag1"));
        assertTrue(tags.contains("tag2"));
    }

    @Test
    public void testSaveTechnicalProductWithEmptyDesc() throws Exception {
        VOTechnicalService techProd = createTechnicalProduct(svcProv);
        Assert.assertNotNull(techProd);
        final String valueToSet = "";
        techProd.setLicense("");
        String license = "";
        techProd.setAccessInfo(valueToSet);
        techProd.setTechnicalServiceDescription(valueToSet);
        List<VOEventDefinition> eventDefinitions = techProd
                .getEventDefinitions();
        for (VOEventDefinition event : eventDefinitions) {
            event.setEventDescription(valueToSet);
        }
        List<VOParameterDefinition> parameterDefinitions = techProd
                .getParameterDefinitions();
        for (VOParameterDefinition parameter : parameterDefinitions) {
            parameter.setDescription(valueToSet);
            List<VOParameterOption> parameterOptions = parameter
                    .getParameterOptions();
            for (VOParameterOption option : parameterOptions) {
                option.setOptionDescription(valueToSet);
            }
        }
        List<VORoleDefinition> roleDefinitions = techProd.getRoleDefinitions();
        for (VORoleDefinition role : roleDefinitions) {
            role.setDescription(valueToSet);
            role.setName(valueToSet);
        }
        List<VOTechnicalServiceOperation> ops = techProd
                .getTechnicalServiceOperations();
        for (VOTechnicalServiceOperation op : ops) {
            op.setOperationDescription(valueToSet);
            op.setOperationName(valueToSet);
            List<VOServiceOperationParameter> operationParameters = op
                    .getOperationParameters();
            for (VOServiceOperationParameter p : operationParameters) {
                p.setParameterName(valueToSet);
            }
        }
        List<String> tags = new ArrayList<String>();
        tags.add("tag1");
        tags.add("tag2");
        techProd.setTags(tags);

        deleteEmptyTp_keyRecord(techProd);
        svcProv.saveTechnicalServiceLocalization(techProd);
        techProd = svcProv.getTechnicalServices(
                OrganizationRoleType.TECHNOLOGY_PROVIDER).get(0);
        Assert.assertEquals(valueToSet, techProd.getAccessInfo());
        Assert.assertEquals(valueToSet,
                techProd.getTechnicalServiceDescription());
        Assert.assertEquals(license, techProd.getLicense());
        eventDefinitions = techProd.getEventDefinitions();
        for (VOEventDefinition event : eventDefinitions) {
            if (event.getEventType() != EventType.PLATFORM_EVENT) {
                Assert.assertEquals(valueToSet, event.getEventDescription());
            }
        }
        parameterDefinitions = techProd.getParameterDefinitions();
        for (VOParameterDefinition parameter : parameterDefinitions) {
            if (parameter.getParameterType() != ParameterType.PLATFORM_PARAMETER) {
                Assert.assertEquals(valueToSet, parameter.getDescription());
                List<VOParameterOption> parameterOptions = parameter
                        .getParameterOptions();
                for (VOParameterOption option : parameterOptions) {
                    Assert.assertEquals(valueToSet,
                            option.getOptionDescription());
                }
            }
        }
        roleDefinitions = techProd.getRoleDefinitions();
        for (VORoleDefinition role : roleDefinitions) {
            Assert.assertEquals(valueToSet, role.getDescription());
            Assert.assertEquals(valueToSet, role.getName());
        }
        ops = techProd.getTechnicalServiceOperations();
        for (VOTechnicalServiceOperation op : ops) {
            Assert.assertEquals(valueToSet, op.getOperationDescription());
            Assert.assertEquals(valueToSet, op.getOperationName());
            List<VOServiceOperationParameter> operationParameters = op
                    .getOperationParameters();
            for (VOServiceOperationParameter p : operationParameters) {
                assertEquals(valueToSet, p.getParameterName());
            }

        }
        tags = techProd.getTags();
        assertEquals(2, tags.size());
        assertTrue(techProd.getLicense().equals(""));
        assertTrue(techProd.getTechnicalServiceDescription().equals(""));
    }

    @Test(expected = ValidationException.class)
    public void testSaveTechnicalProduct_TooLongRoleName() throws Exception {
        VOTechnicalService techProd = createTechnicalProduct(svcProv);
        Assert.assertNotNull(techProd);
        List<VORoleDefinition> roleDefinitions = techProd.getRoleDefinitions();
        for (VORoleDefinition role : roleDefinitions) {
            role.setName(TOO_LONG_NAME);
        }
        svcProv.saveTechnicalServiceLocalization(techProd);
    }

    @Test(expected = ValidationException.class)
    public void testSaveTechnicalProduct_TooLongOperationName()
            throws Exception {
        VOTechnicalService techProd = createTechnicalProduct(svcProv);
        Assert.assertNotNull(techProd);
        List<VOTechnicalServiceOperation> ops = techProd
                .getTechnicalServiceOperations();
        for (VOTechnicalServiceOperation op : ops) {
            op.setOperationName(TOO_LONG_NAME);
        }
        svcProv.saveTechnicalServiceLocalization(techProd);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSaveTechnicalProductNotExisting() throws Exception {
        VOTechnicalService toSave = new VOTechnicalService();
        svcProv.saveTechnicalServiceLocalization(toSave);
    }

    @Test
    public void testSaveTechnicalProductInUseLicenseChanged() throws Exception {
        VOTechnicalService techProd = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        createProduct(techProd, "product1", svcProv);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Subscriptions.createSubscription(mgr, customerOrgId,
                        "product1", "sub", supplier);
                return null;
            }
        });

        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        techProd.setLicense("some new license");
        deleteEmptyTp_keyRecord(techProd);
        svcProv.saveTechnicalServiceLocalization(techProd);
    }

    @Test
    public void testSaveTechnicalProductInUseLicenseNull() throws Exception {
        VOTechnicalService techProd = createTechnicalProduct(svcProv);
        String license = techProd.getLicense();
        techProd.setLicense(null);
        deleteEmptyTp_keyRecord(techProd);
        svcProv.saveTechnicalServiceLocalization(techProd);
        techProd = svcProv.getTechnicalServices(
                OrganizationRoleType.TECHNOLOGY_PROVIDER).get(0);
        Assert.assertEquals(license, techProd.getLicense());
    }

    @Test
    /*
     * Test with saving new formatted license.
     */
    public void testSaveTechnicalProductNewFormattedLicense() throws Exception {
        VOTechnicalService techProd = createTechnicalProduct(svcProv);
        Assert.assertNotNull(techProd);
        String oldLicense = techProd.getLicense();
        StringBuffer modifiedLicense = new StringBuffer(oldLicense);
        modifiedLicense.insert(0, "<strong>"); // just format text, not content
        modifiedLicense.append("</strong>"); // just format text, not content
        techProd.setLicense(modifiedLicense.toString());
        // new formatted license have to be saved
        deleteEmptyTp_keyRecord(techProd);
        svcProv.saveTechnicalServiceLocalization(techProd);
        techProd = svcProv.getTechnicalServices(
                OrganizationRoleType.TECHNOLOGY_PROVIDER).get(0);
        String newLicense = techProd.getLicense();
        Assert.assertNotSame("New license has to be changed.", oldLicense,
                newLicense);
        Assert.assertEquals(
                "New license has to be changed accroding new formatting.",
                modifiedLicense.toString(), newLicense);
    }

    @Test
    public void testGetProductsWithEnumDataTypes() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                addProductsHavingEnumerationDataType(2);
                return null;
            }
        });
    }

    @Test
    public void testUpdateMarketingProductAddEnumParametersToExistingOnes()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService product = new VOService();
        product.setDescription("marketing desc");
        product.setServiceId("modifiedProductIdAddEnumParamToExisting");
        List<VOParameter> parameters = new ArrayList<VOParameter>();
        // product has no parameters so far, so add one
        List<VOParameterDefinition> parameterDefinitions = tp
                .getParameterDefinitions();

        VOParameterDefinition enumParamDefi = null;
        for (VOParameterDefinition parameterDef : parameterDefinitions) {
            if (parameterDef.getValueType() == ParameterValueType.ENUMERATION) {
                enumParamDefi = parameterDef;
                break;
            }
        }
        if (enumParamDefi != null) {
            VOParameter parameter = new VOParameter(enumParamDefi);
            parameter.setConfigurable(false);
            parameter.setValue("1");
            parameters.add(parameter);
            product.setParameters(parameters);
            container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                    ROLE_TECHNOLOGY_MANAGER);
            VOServiceDetails createdProduct = svcProv.createService(tp,
                    product, null);
            parameters = createdProduct.getParameters();

            VOParameter newParam = new VOParameter(parameterDefinitions.get(1));
            newParam.setValue("5555");
            parameter.setConfigurable(false);
            parameters.add(newParam);

            // add a second new parameter
            parameters = createdProduct.getParameters();
            newParam = new VOParameter(parameterDefinitions.get(2));
            newParam.setValue("86400000");
            parameter.setConfigurable(false);
            parameters.add(newParam);

            // and change the value of the Enumeration
            VOParameter parameterWithEnumValues = parameters.get(0);
            parameterWithEnumValues.setValue("3");

            createdProduct = svcProv.updateService(createdProduct, null);

            Assert.assertEquals("Parameter has not been persisted", 3,
                    createdProduct.getParameters().size());
            Assert.assertEquals("Wrong parameter value", "3", createdProduct
                    .getParameters().get(0).getValue());
            Assert.assertEquals("Wrong parameter value", false, createdProduct
                    .getParameters().get(0).isConfigurable());

            Assert.assertEquals("Wrong parameter value", "5555", createdProduct
                    .getParameters().get(1).getValue());
            Assert.assertEquals("Wrong parameter value", false, createdProduct
                    .getParameters().get(1).isConfigurable());

            Assert.assertEquals("Wrong parameter value", "86400000",
                    createdProduct.getParameters().get(2).getValue());
            Assert.assertEquals("Wrong parameter value", false, createdProduct
                    .getParameters().get(2).isConfigurable());

        }
    }

    @Test
    public void testUpdateMarketingProductWithEmptyDesc() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOService product = new VOService();
        product.setDescription("");
        product.setServiceId("modifiedProductWithEmptyDesc");
        List<VOParameter> parameters = new ArrayList<VOParameter>();
        // product has no parameters so far, so add one
        List<VOParameterDefinition> parameterDefinitions = tp
                .getParameterDefinitions();

        VOParameterDefinition enumParamDefi = null;
        for (VOParameterDefinition parameterDef : parameterDefinitions) {
            if (parameterDef.getValueType() == ParameterValueType.ENUMERATION) {
                enumParamDefi = parameterDef;
                break;
            }
        }
        if (enumParamDefi != null) {
            VOParameter parameter = new VOParameter(enumParamDefi);
            parameter.setConfigurable(false);
            parameter.setValue("1");
            parameters.add(parameter);
            product.setParameters(parameters);
            container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                    ROLE_TECHNOLOGY_MANAGER);
            VOServiceDetails createdProduct = svcProv.createService(tp,
                    product, null);
            parameters = createdProduct.getParameters();

            VOParameter newParam = new VOParameter(parameterDefinitions.get(1));
            newParam.setValue("5555");
            parameter.setConfigurable(false);
            parameters.add(newParam);

            // add a second new parameter
            parameters = createdProduct.getParameters();
            newParam = new VOParameter(parameterDefinitions.get(2));
            newParam.setValue("86400000");
            parameter.setConfigurable(false);
            parameters.add(newParam);

            // and change the value of the Enumeration
            VOParameter parameterWithEnumValues = parameters.get(0);
            parameterWithEnumValues.setValue("3");

            createdProduct = svcProv.updateService(createdProduct, null);

            Assert.assertEquals(product.getDescription(), "");

        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testExportTechnicalProductNotExisting() throws Exception {
        VOTechnicalService product = new VOTechnicalService();
        List<VOTechnicalService> list = Collections.singletonList(product);
        svcProv.exportTechnicalServices(list);
    }

    @Test
    public void testExportTechnicalProducts() throws Exception {
        VOTechnicalService techProd = createTechnicalProduct(svcProv);
        List<VOTechnicalService> list = Collections.singletonList(techProd);
        byte[] content = svcProv.exportTechnicalServices(list);
        List<Node> productNodes = verifyExportedTechnicalService(content);
        compareTP(techProd, productNodes.get(0));
    }

    private List<Node> verifyExportedTechnicalService(byte[] content)
            throws ParserConfigurationException, SAXException, IOException {
        Document document = XMLConverter
                .convertToDocument(new ByteArrayInputStream(content));
        Assert.assertEquals(1, document.getChildNodes().getLength());
        Element root = document.getDocumentElement();
        Assert.assertEquals("tns:TechnicalServices", root.getNodeName());
        List<Node> productNodes = XMLConverter.getNodeList(
                root.getChildNodes(), "tns:TechnicalService");
        Assert.assertEquals(1, productNodes.size());
        return productNodes;
    }

    /**
     * Test the export of a technical service where the subscription restriction
     * flag is set to false, as in the above test case.
     * 
     * @throws Exception
     */
    @Test
    public void testExportTechnicalProducts_OneSubscriptionFlagFalse()
            throws Exception {

        boolean subscriptionRestriction = false;
        VOTechnicalService techProd = createTechnicalProductWithSubscriptionRestriction(
                svcProv, subscriptionRestriction);

        List<VOTechnicalService> list = Collections.singletonList(techProd);
        byte[] content = svcProv.exportTechnicalServices(list);
        List<Node> productNodes = verifyExportedTechnicalService(content);
        compareSubscriptionRestriction(productNodes.get(0),
                subscriptionRestriction);
    }

    /**
     * Test the export of technical service where the subscription restriction
     * flag is not set. In this case the flag "onlyOneSubscriptionPerUser" is
     * exported and has the value "false".
     * 
     * @throws Exception
     */
    @Test
    public void testExportTechnicalProducts_OneSubscriptionFlagNotSet()
            throws Exception {

        VOTechnicalService techProd = createTechnicalProduct(svcProv);

        List<VOTechnicalService> list = Collections.singletonList(techProd);
        byte[] content = svcProv.exportTechnicalServices(list);
        List<Node> productNodes = verifyExportedTechnicalService(content);
        compareSubscriptionRestriction(productNodes.get(0), false);
    }

    @Test
    public void testExportTechnicalProductAndImport() throws Exception {
        VOTechnicalService techProd = createTechnicalProduct(svcProv);
        List<VOTechnicalService> singleton = Collections
                .singletonList(techProd);
        byte[] content = svcProv.exportTechnicalServices(singleton);
        svcProv.importTechnicalServices(content);
    }

    @Test
    public void testGetCustomerSpecificProductsOneInactive() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(techProduct, "product1",
                svcProv);
        VOServiceDetails voProduct2 = createProduct(techProduct, "product2",
                svcProv);
        VOPriceModel priceModel = createPriceModel();
        voProduct1 = svcProv.savePriceModel(voProduct1, priceModel);
        voProduct2 = svcProv.savePriceModel(voProduct2, priceModel);

        publishToLocalMarketplaceSupplier(voProduct1, mpSupplier);
        publishToLocalMarketplaceSupplier(voProduct2, mpSupplier);
        svcProv.activateService(voProduct1);
        svcProv.activateService(voProduct2);

        VOServiceDetails voDetails = svcProv.getServiceDetails(voProduct1);
        VOOrganization org = getOrganizationForOrgId(customerOrgId);
        VOServiceDetails forCustomer = svcProv.savePriceModelForCustomer(
                voDetails, priceModel, org);

        List<VOCustomerService> products = svcProv
                .getAllCustomerSpecificServices();
        Assert.assertEquals(1, products.size());
        Assert.assertEquals(forCustomer.getKey(), products.get(0).getKey());
        Assert.assertEquals(forCustomer.getServiceId(), products.get(0)
                .getServiceId());
        Assert.assertEquals(ServiceStatus.INACTIVE, products.get(0).getStatus());
        Assert.assertEquals(forCustomer.getPriceModel().getKey(),
                products.get(0).getPriceModel().getKey());
        Assert.assertEquals(org.getKey(), products.get(0).getOrganizationKey()
                .longValue());
        Assert.assertEquals(org.getName(), products.get(0)
                .getOrganizationName());
        Assert.assertEquals(org.getOrganizationId(), products.get(0)
                .getOrganizationId());
    }

    @Test
    public void testGetCustomerSpecificProductsOneSuspended() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(techProduct, "product1",
                svcProv);
        VOServiceDetails voProduct2 = createProduct(techProduct, "product2",
                svcProv);
        VOPriceModel priceModel = createPriceModel();
        voProduct1 = svcProv.savePriceModel(voProduct1, priceModel);
        voProduct2 = svcProv.savePriceModel(voProduct2, priceModel);
        publishToLocalMarketplaceSupplier(voProduct1, mpSupplier);
        publishToLocalMarketplaceSupplier(voProduct2, mpSupplier);
        svcProv.activateService(voProduct1);
        svcProv.activateService(voProduct2);

        VOServiceDetails voDetails = svcProv.getServiceDetails(voProduct1);
        VOOrganization org = getOrganizationForOrgId(customerOrgId);
        final VOServiceDetails forCustomer = svcProv.savePriceModelForCustomer(
                voDetails, priceModel, org);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product p = mgr.getReference(Product.class,
                        forCustomer.getKey());
                p.setStatus(ServiceStatus.SUSPENDED);
                return null;
            }

        });

        List<VOCustomerService> products = svcProv
                .getAllCustomerSpecificServices();
        Assert.assertEquals(1, products.size());
        Assert.assertEquals(forCustomer.getKey(), products.get(0).getKey());
        Assert.assertEquals(forCustomer.getServiceId(), products.get(0)
                .getServiceId());
        Assert.assertEquals(ServiceStatus.SUSPENDED, products.get(0)
                .getStatus());
        Assert.assertEquals(forCustomer.getPriceModel().getKey(),
                products.get(0).getPriceModel().getKey());
        Assert.assertEquals(org.getKey(), products.get(0).getOrganizationKey()
                .longValue());
        Assert.assertEquals(org.getName(), products.get(0)
                .getOrganizationName());
        Assert.assertEquals(org.getOrganizationId(), products.get(0)
                .getOrganizationId());
    }

    @Test
    public void testGetCustomerSpecificProductsOneInactiveOneActive()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(techProduct, "product1",
                svcProv);
        VOServiceDetails voProduct2 = createProduct(techProduct, "product2",
                svcProv);
        VOPriceModel priceModel = createPriceModel();
        voProduct1 = svcProv.savePriceModel(voProduct1, priceModel);
        voProduct2 = svcProv.savePriceModel(voProduct2, priceModel);
        publishToLocalMarketplaceSupplier(voProduct1, mpSupplier);
        publishToLocalMarketplaceSupplier(voProduct2, mpSupplier);
        svcProv.activateService(voProduct1);
        svcProv.activateService(voProduct2);

        voProduct1 = svcProv.getServiceDetails(voProduct1);
        voProduct2 = svcProv.getServiceDetails(voProduct2);
        VOOrganization org = getOrganizationForOrgId(customerOrgId);
        VOServiceDetails forCustomer1 = svcProv.savePriceModelForCustomer(
                voProduct1, priceModel, org);
        VOServiceDetails forCustomer2 = svcProv.savePriceModelForCustomer(
                voProduct2, priceModel, org);
        publishToLocalMarketplaceSupplier(forCustomer2, mpSupplier);
        svcProv.activateService(forCustomer2);

        List<VOCustomerService> products = svcProv
                .getAllCustomerSpecificServices();
        Assert.assertEquals(2, products.size());
        Assert.assertEquals(forCustomer1.getKey(), products.get(0).getKey());
        Assert.assertEquals(forCustomer1.getServiceId(), products.get(0)
                .getServiceId());
        Assert.assertEquals(ServiceStatus.INACTIVE, products.get(0).getStatus());
        Assert.assertEquals(forCustomer1.getPriceModel().getKey(), products
                .get(0).getPriceModel().getKey());

        Assert.assertEquals(forCustomer2.getKey(), products.get(1).getKey());
        Assert.assertEquals(forCustomer2.getServiceId(), products.get(1)
                .getServiceId());
        Assert.assertEquals(ServiceStatus.ACTIVE, products.get(1).getStatus());
        Assert.assertEquals(forCustomer2.getPriceModel().getKey(), products
                .get(1).getPriceModel().getKey());
    }

    @Test
    public void testGetCustomerSpecificProductsOneActiveOneDeleted()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(techProduct, "product1",
                svcProv);
        VOServiceDetails voProduct2 = createProduct(techProduct, "product2",
                svcProv);
        VOPriceModel priceModel = createPriceModel();
        voProduct1 = svcProv.savePriceModel(voProduct1, priceModel);
        voProduct2 = svcProv.savePriceModel(voProduct2, priceModel);
        publishToLocalMarketplaceSupplier(voProduct1, mpSupplier);
        publishToLocalMarketplaceSupplier(voProduct2, mpSupplier);
        svcProv.activateService(voProduct1);
        svcProv.activateService(voProduct2);

        voProduct1 = svcProv.getServiceDetails(voProduct1);
        voProduct2 = svcProv.getServiceDetails(voProduct2);
        VOOrganization org = getOrganizationForOrgId(customerOrgId);
        VOServiceDetails forCustomer1 = svcProv.savePriceModelForCustomer(
                voProduct1, priceModel, org);
        VOServiceDetails forCustomer2 = svcProv.savePriceModelForCustomer(
                voProduct2, priceModel, org);
        publishToLocalMarketplaceSupplier(forCustomer1, mpSupplier);
        svcProv.activateService(forCustomer1);
        svcProv.deleteService(forCustomer2);

        List<VOCustomerService> products = svcProv
                .getAllCustomerSpecificServices();
        Assert.assertEquals(1, products.size());
        Assert.assertEquals(forCustomer1.getKey(), products.get(0).getKey());
        Assert.assertEquals(forCustomer1.getServiceId(), products.get(0)
                .getServiceId());
        Assert.assertEquals(ServiceStatus.ACTIVE, products.get(0).getStatus());
        Assert.assertEquals(forCustomer1.getPriceModel().getKey(), products
                .get(0).getPriceModel().getKey());
    }

    @Test
    public void testValidateTechnicalProductCommunication() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        svcProv.validateTechnicalServiceCommunication(tp);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void testValidateTechnicalProductCommunicationNotAlive()
            throws Exception {
        appNotAlive = true;
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        svcProv.validateTechnicalServiceCommunication(tp);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testValidateTechnicalProductCommunicationNotExisting()
            throws Exception {
        VOTechnicalService tp = new VOTechnicalService();
        svcProv.validateTechnicalServiceCommunication(tp);
    }

    @Test
    public void testValidateTechnicalProductCommunicationAsSupplier()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.validateTechnicalServiceCommunication(tp);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void testValidateTechnicalProductCommunicationNotAliveAsSupplier()
            throws Exception {
        appNotAlive = true;
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.validateTechnicalServiceCommunication(tp);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testValidateTechnicalProductCommunicationAsNonSupplierOfProvider()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        String userKey = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        organization, true, "admin");
                return String.valueOf(user.getKey());
            }
        });
        container.login(userKey, ROLE_SERVICE_MANAGER);
        svcProv.validateTechnicalServiceCommunication(tp);
    }

    @Test
    public void testCreateTechnicalProduct() throws Exception {
        final VOTechnicalService vo = createTechnicalProduct(
                ServiceAccessType.LOGIN, "",
                BillingAdapterIdentifier.NATIVE_BILLING.name());
        svcProv.deleteTechnicalService(vo);
        createTechnicalProduct(ServiceAccessType.EXTERNAL, "",
                BillingAdapterIdentifier.NATIVE_BILLING.name());
    }

    @Test
    public void testCreateTechnicalProductLoginPathRelative() throws Exception {
        final VOTechnicalService vo = createTechnicalProduct(
                ServiceAccessType.LOGIN, "/xy/123",
                BillingAdapterIdentifier.NATIVE_BILLING.name());
        svcProv.deleteTechnicalService(vo);
    }

    @Test(expected = ValidationException.class)
    public void testCreateTechnicalProductLoginPathNotRelative()
            throws Exception {
        createTechnicalProduct(ServiceAccessType.LOGIN, "xy/123",
                BillingAdapterIdentifier.NATIVE_BILLING.name());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testCreateMarketingProductAsNonSupplierOfProvider()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        String userKey = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        organization, true, "admin");
                return String.valueOf(user.getKey());
            }
        });
        container.login(userKey, ROLE_SERVICE_MANAGER);
        VOService product = new VOService();
        product.setServiceId("test");
        svcProv.createService(tp, product, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testUpdateMarketingProductAsNonSupplierOfProvider()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        product = svcProv.createService(tp, product, null);
        String userKey = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        organization, true, "admin");
                return String.valueOf(user.getKey());
            }
        });
        container.login(userKey, ROLE_SERVICE_MANAGER);
        svcProv.updateService(product, null);
    }

    @Test
    public void testCreateMarketingProductWithParameters() throws Exception {
        VOServiceDetails product = createProductWithParameters("test");
        List<VOParameter> params = product.getParameters();
        // the 1st parameter must not be saved because its empty and not
        // configurable
        // the 2nd parameter must be saved because a value is set
        // the 3rd parameter must be saved because its configurable
        // the null at 4th position will be ignored
        Assert.assertEquals(3, params.size());
    }

    @Test
    public void testCreateMarketingProductWithParameter_NotMandatoryForSupplier1()
            throws Exception {
        createMarketingProductWithParameter("", false, false);
    }

    @Test
    public void testCreateMarketingProductWithParameter_NotMandatoryForSupplier2()
            throws Exception {
        createMarketingProductWithParameter("", true, false);
    }

    @Test
    public void testCreateMarketingProductWithParameter_NotMandatoryForSupplier3()
            throws Exception {
        createMarketingProductWithParameter("", true, true);
    }

    @Test(expected = ValidationException.class)
    public void testCreateMarketingProductWithParameter_MandatoryForSupplier1()
            throws Exception {
        createMarketingProductWithParameter("", false, true);
    }

    @Test
    public void testCreateMarketingProductWithParameter_MandatoryForSupplier2()
            throws Exception {
        createMarketingProductWithParameter("20", false, true);
    }

    @Test
    public void testUpdateMarketingProductWithParameter_NotMandatoryForSupplier1()
            throws Exception {
        updateMarketingProductWithParameter("", false, false);
    }

    @Test
    public void testUpdateMarketingProductWithParameter_NotMandatoryForSupplier2()
            throws Exception {
        updateMarketingProductWithParameter("", true, false);
    }

    @Test
    public void testUpdateMarketingProductWithParameter_NotMandatoryForSupplier3()
            throws Exception {
        updateMarketingProductWithParameter("", true, true);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateMarketingProductWithParameter_MandatoryForSupplier1()
            throws Exception {
        updateMarketingProductWithParameter("", false, true);
    }

    @Test
    public void testUpdateMarketingProductWithParameter_MandatoryForSupplier2()
            throws Exception {
        updateMarketingProductWithParameter("20", false, true);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testCreateMarketingProductWithNotExistingParameter()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        List<VOParameter> params = new ArrayList<VOParameter>();
        List<VOParameterOption> empty = emptyList();
        VOParameterDefinition def = new VOParameterDefinition(
                ParameterType.SERVICE_PARAMETER, "Test", "test",
                ParameterValueType.STRING, null, null, null, false, true, empty);
        VOParameter param = new VOParameter(def);
        param.setValue("");
        param.setConfigurable(false);
        params.add(param);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        product.setParameters(params);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.createService(tp, product, null);
    }

    @Test
    public void testUpdateMarketingProductRemoveParameters() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        List<VOParameter> params = new ArrayList<VOParameter>();
        VOParameter param = new VOParameter(getParamDefinition(
                "MAX_FOLDER_NUMBER", tp.getParameterDefinitions()));
        param.setValue("");
        param.setConfigurable(true);
        params.add(param);
        param = new VOParameter(getParamDefinition("HAS_OPTIONS",
                tp.getParameterDefinitions()));
        param.setValue("1");
        param.setConfigurable(false);
        params.add(param);
        param = new VOParameter(getParamDefinition("BOOLEAN_PARAMETER",
                tp.getParameterDefinitions()));
        param.setValue("");
        param.setConfigurable(true);
        params.add(param);
        params.add(null);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        product.setParameters(params);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        product = svcProv.createService(tp, product, null);
        params = product.getParameters();

        Assert.assertEquals(3, params.size());
        VOParameter paramToRemove = null;
        for (VOParameter voParameter : params) {
            if (voParameter.isConfigurable()) {
                voParameter.setConfigurable(false);
            } else {
                paramToRemove = voParameter;
            }
        }
        Assert.assertNotNull(paramToRemove);
        params.remove(paramToRemove);
        product = svcProv.updateService(product, null);
        params = product.getParameters();
        Assert.assertEquals(0, params.size());
    }

    @Test
    public void testUpdateMarketingProductRemoveParametersWithNullList()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        List<VOParameter> params = new ArrayList<VOParameter>();
        VOParameter param = new VOParameter(getParamDefinition(
                "MAX_FOLDER_NUMBER", tp.getParameterDefinitions()));
        param.setValue("");
        param.setConfigurable(true);
        params.add(param);
        param = new VOParameter(getParamDefinition("HAS_OPTIONS",
                tp.getParameterDefinitions()));
        param.setValue("1");
        param.setConfigurable(false);
        params.add(param);
        param = new VOParameter(getParamDefinition("BOOLEAN_PARAMETER",
                tp.getParameterDefinitions()));
        param.setValue("");
        param.setConfigurable(true);
        params.add(param);
        params.add(null);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        product.setParameters(params);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        product = svcProv.createService(tp, product, null);
        params = product.getParameters();
        Assert.assertEquals(3, params.size());
        product.setParameters(null);
        product = svcProv.updateService(product, null);
        params = product.getParameters();
        Assert.assertEquals(0, params.size());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetProductForCustomerNotSupplierOfProduct()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        svcProv.getServiceForCustomer(customer, product);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetProductForCustomerNotSupplierOfCustomer()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOOrganization provider = getOrganizationForOrgId(providerOrgId);
        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModelForCustomer(product, priceModel, provider);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        svcProv.getServiceForCustomer(customer, product);
    }

    @Test
    public void testGetProductForCustomer() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModelForCustomer(product, priceModel, customer);
        final VOServiceDetails customerProduct = svcProv.getServiceForCustomer(
                customer, product);
        Assert.assertNotNull(customerProduct);
        Assert.assertEquals("test", customerProduct.getServiceId());
        Assert.assertEquals("example", customerProduct.getTechnicalId());

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                // assert that both the template and the customer specific copy
                // have a catalog entry and that the copy appears after the
                // template
                Product productTemplate = mgr.getReference(Product.class,
                        product.getKey());
                assertNotNull("Persisted product template missing",
                        productTemplate);
                assertNotNull("Product template has no catalog entry",
                        productTemplate.getCatalogEntries().get(0));
                Product product = mgr.getReference(Product.class,
                        customerProduct.getKey());
                assertNotNull("Persisted product copy missing", product);
                assertNotNull(
                        "Target customer not persisted in copy of product",
                        product.getTargetCustomer());
                assertNotNull("Template not persisted in copy of product",
                        product.getTemplate());
                assertTrue("Catalog entry set in customer copy of product",
                        product.getCatalogEntries().isEmpty());
                return null;
            }
        });
    }

    @Test
    public void testGetProductForCustomerSubscription() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(product, priceModel);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        Assert.assertNotNull(subId);
        VOServiceDetails forCustomer = svcProv.getServiceForSubscription(
                customer, subId);
        Assert.assertNotNull(forCustomer);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetProductForCustomerSubscriptionNotExistingCustomer()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(product, priceModel);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        Assert.assertNotNull(subId);
        svcProv.getServiceForSubscription(new VOOrganization(), subId);
    }

    @Test(expected = EJBException.class)
    public void testGetProductForCustomerSubscriptionAsCustomer()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(product, priceModel);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        Assert.assertNotNull(subId);
        container.login(customerUserKey);
        svcProv.getServiceForSubscription(customer, subId);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetProductDetailsNotOwner() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.getServiceDetails(product);
    }

    @Test
    public void testGetProductDetailsVerifyRoleDefinitions() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        runTX(new Callable<Product>() {

            @Override
            public Product call() throws Exception {

                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider, "techProd1", false,
                        ServiceAccessType.LOGIN);

                RoleDefinition rd1 = new RoleDefinition();
                rd1.setRoleId("Role1");
                rd1.setTechnicalProduct(tp);
                mgr.persist(rd1);

                RoleDefinition rd2 = new RoleDefinition();
                rd2.setRoleId("Role2");
                rd2.setTechnicalProduct(tp);
                mgr.persist(rd2);

                Product prod = Products.createProduct(supplier, tp, true,
                        "prodId", null, mgr);

                return prod;
            }
        });

        List<VOService> products = svcProv.getSuppliedServices();
        Assert.assertNotNull(products);
        Assert.assertEquals(1, products.size());
        VOServiceDetails productDetails = svcProv.getServiceDetails(products
                .get(0));
        Assert.assertNotNull(productDetails);
        Assert.assertTrue(productDetails.getKey() > 0);
        VOTechnicalService technicalProduct = productDetails
                .getTechnicalService();
        Assert.assertNotNull(technicalProduct);
        Assert.assertTrue(technicalProduct.getKey() > 0);

        List<VORoleDefinition> roleDefinitions = technicalProduct
                .getRoleDefinitions();
        Assert.assertNotNull(roleDefinitions);
        Assert.assertEquals(2, roleDefinitions.size());
        VORoleDefinition role1 = roleDefinitions.get(0);
        Assert.assertEquals("Role1", role1.getRoleId());
        VORoleDefinition role2 = roleDefinitions.get(1);
        Assert.assertEquals("Role2", role2.getRoleId());
    }

    @Test(expected = CurrencyException.class)
    public void testSavePriceModelChargeableWithoutCurrency() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setPricePerPeriod(BigDecimal.valueOf(500L));
        svcProv.savePriceModel(product, priceModel);
    }

    @Test
    public void testSavePriceModelForSubscriptionWithLicense() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        final VOPriceModel priceModel_init = createPriceModel();
        svcProv.savePriceModel(product, priceModel_init);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        Assert.assertNotNull(subId);
        VOServiceDetails forSubscription = svcProv.getServiceForSubscription(
                customer, subId);
        Assert.assertNotNull(forSubscription);
        final VOPriceModel priceModel = forSubscription.getPriceModel();

        final String priceModelLicenseEnOld = "price model license OLD EN";
        String priceModelLicenseEnNEW = "price model license NEW EN";

        priceModel.setLicense(priceModelLicenseEnNEW);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("en", priceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE,
                        priceModelLicenseEnOld);
                return null;
            }
        });
        forSubscription = svcProv.savePriceModelForSubscription(
                forSubscription, priceModel);

        String priceModelLicenseActualEn = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("en",
                        priceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE);
            }
        });

        assertEquals("License is wrong. En", priceModelLicenseEnOld,
                priceModelLicenseActualEn);

    }

    @Test
    public void testValidateSubscriptionWithoutException() throws Exception {
        // given

        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "testProd", svcProv);
        final VOPriceModel priceModel_init = createPriceModel();
        svcProv.savePriceModel(product, priceModel_init);
        VOOrganization customer = getOrganizationForOrgId(supplierOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSubscriptionToValidate", null);

        VOServiceDetails forSubscription = svcProv.getServiceForSubscription(
                customer, subId);
        // when
        svcProv.validateSubscription(forSubscription);

        // then
        Assert.assertNotNull(subId);
    }

    @Test(expected = SubscriptionStateException.class)
    public void testValidateSubscriptionInvalidState() throws Exception {
        // given

        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "testProdInvalid",
                svcProv);
        final VOPriceModel priceModel_init = createPriceModel();
        svcProv.savePriceModel(product, priceModel_init);
        VOOrganization customer = getOrganizationForOrgId(supplierOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.INVALID,
                product, "testSubscriptionInvalid", null);

        VOServiceDetails forSubscription = svcProv.getServiceForSubscription(
                customer, subId);
        // when
        svcProv.validateSubscription(forSubscription);
    }

    @Test(expected = SubscriptionStateException.class)
    public void testValidateSubscriptionExpiredState() throws Exception {
        // given

        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "testProdExpired",
                svcProv);
        final VOPriceModel priceModel_init = createPriceModel();
        svcProv.savePriceModel(product, priceModel_init);
        VOOrganization customer = getOrganizationForOrgId(supplierOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.EXPIRED,
                product, "testSubscriptionExpired", null);

        VOServiceDetails forSubscription = svcProv.getServiceForSubscription(
                customer, subId);
        // when
        svcProv.validateSubscription(forSubscription);
    }

    @Test(expected = SubscriptionStateException.class)
    public void testValidateSubscriptionDeactivatedState() throws Exception {
        // given

        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp,
                "testProdDeactivated", svcProv);
        final VOPriceModel priceModel_init = createPriceModel();
        svcProv.savePriceModel(product, priceModel_init);
        VOOrganization customer = getOrganizationForOrgId(supplierOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.EXPIRED,
                product, "testSubscriptionDeactivated", null);

        VOServiceDetails forSubscription = svcProv.getServiceForSubscription(
                customer, subId);
        // when
        svcProv.validateSubscription(forSubscription);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelNotOwned() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.savePriceModel(product, priceModel);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSetCompatibleProductsSourceNotOwned() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product1 = createProduct(tp, "1", svcProv);
        VOService product2 = createProduct(tp, "2", svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.setCompatibleServices(product1,
                Collections.singletonList(product2));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSetCompatibleProductsTargetNotOwned() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product1 = createProduct(tp, "1", svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOService product2 = createProduct(tp, "2", svcProv);
        svcProv.setCompatibleServices(product1,
                Collections.singletonList(product2));
    }

    @Test
    public void testGetCustomerSpecificProductsForCustomer() throws Exception {
        List<VOTechnicalService> list = createTechnicalProducts(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product1 = createProduct(list.get(0), "1", svcProv);
        VOServiceDetails product2 = createProduct(list.get(1), "2", svcProv);
        VOPriceModel priceModel = createPriceModel();
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        product1 = svcProv.savePriceModelForCustomer(product1, priceModel,
                customer);
        product2 = svcProv.savePriceModelForCustomer(product2, priceModel,
                customer);
        List<VOService> products = svcProv.getServicesForCustomer(customer);
        Assert.assertNotNull(products);
        Assert.assertEquals(2, products.size());
        Assert.assertEquals(product1.getServiceId(), products.get(0)
                .getServiceId());
        Assert.assertEquals(product2.getServiceId(), products.get(1)
                .getServiceId());
    }

    @Test(expected = EJBException.class)
    public void testGetCustomerSpecificProductsForCustomerAsNonSupplier()
            throws Exception {
        container.login(customerUserKey);
        VOOrganization organization = getOrganizationForOrgId(providerOrgId);
        svcProv.getServicesForCustomer(organization);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetCustomerSpecificProductsForCustomerAsNonSupplierOfCustomer()
            throws Exception {
        VOOrganization organization = getOrganizationForOrgId(customerOrgId);
        svcProv.getServicesForCustomer(organization);
    }

    @Test
    public void testGetSupportedCurrencies() throws Exception {
        List<String> currencies = svcProv.getSupportedCurrencies();
        List<String> expected = Collections.singletonList(EUR);
        Assert.assertEquals(expected, currencies);
    }

    @Test
    public void testLoadImageNotExistingProduct() throws Exception {
        VOImageResource image = svcProv.loadImage(new Long(-6));
        Assert.assertNull(image);
    }

    @Test
    public void testLoadImage() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        VOImageResource imageResource = new VOImageResource();
        byte[] content = BaseAdmUmTest.getFileAsByteArray(
                ServiceProvisioningServiceBeanIT.class, "icon1.png");
        imageResource.setBuffer(content);
        imageResource.setContentType("image/png");
        imageResource.setImageType(ImageType.SERVICE_IMAGE);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        product = svcProv.createService(tp, product, imageResource);
        VOImageResource loadImage = svcProv.loadImage(Long.valueOf(product
                .getKey()));
        Assert.assertNotNull(loadImage);
        Assert.assertEquals(imageResource.getContentType(),
                loadImage.getContentType());
        Assert.assertEquals(content, loadImage.getBuffer());
        Assert.assertEquals(imageResource.getImageType(),
                loadImage.getImageType());
    }

    /**
     * Test of looking for image for public service catalog of the supplier
     * without customer login.
     * 
     * @throws Exception
     */
    @Test
    public void testLoadImageForSupplier() throws Exception {

        // given
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        VOImageResource imageResource = new VOImageResource();
        byte[] content = BaseAdmUmTest.getFileAsByteArray(
                ServiceProvisioningServiceBeanIT.class, "icon1.png");
        imageResource.setBuffer(content);
        imageResource.setContentType("image/png");
        imageResource.setImageType(ImageType.SERVICE_IMAGE);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        product = svcProv.createService(tp, product, imageResource);

        // execute
        VOImageResource loadImage = svcProv.loadImageForSupplier(
                product.getServiceId(), supplierOrgId);

        // assert
        Assert.assertNotNull(loadImage);
        Assert.assertEquals(imageResource.getContentType(),
                loadImage.getContentType());
        Assert.assertEquals(content, loadImage.getBuffer());
        Assert.assertEquals(imageResource.getImageType(),
                loadImage.getImageType());
    }

    /**
     * Negative test of looking for image for public service catalog of the
     * supplier without customer login. ID is not existed supplier.
     * 
     * @throws Exception
     */
    @Test(expected = ObjectNotFoundException.class)
    public void testLoadImageForSupplierNotFound() throws Exception {

        // given
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        VOImageResource imageResource = new VOImageResource();
        byte[] content = BaseAdmUmTest.getFileAsByteArray(
                ServiceProvisioningServiceBeanIT.class, "icon1.png");
        imageResource.setBuffer(content);
        imageResource.setContentType("image/png");
        imageResource.setImageType(ImageType.SERVICE_IMAGE);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        product = svcProv.createService(tp, product, imageResource);

        // execute
        svcProv.loadImageForSupplier(product.getServiceId(), "NotExisteId");
    }

    @Test
    public void testLoadImageAsCustomer() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        VOImageResource imageResource = new VOImageResource();
        byte[] content = BaseAdmUmTest.getFileAsByteArray(
                ServiceProvisioningServiceBeanIT.class, "icon1.png");
        imageResource.setBuffer(content);
        imageResource.setContentType("image/png");
        imageResource.setImageType(ImageType.SERVICE_IMAGE);
        product = svcProv.createService(tp, product, imageResource);
        container.login(customerUserKey);
        VOImageResource loadImage = svcProv.loadImage(Long.valueOf(product
                .getKey()));
        Assert.assertNotNull("Image resource expected", loadImage);
        Assert.assertEquals("Wrong content type - ",
                imageResource.getContentType(), loadImage.getContentType());
        Assert.assertEquals("Wrong image type - ",
                imageResource.getImageType(), loadImage.getImageType());
        Assert.assertEquals("Wrong content - ", content, loadImage.getBuffer());
    }

    @Test
    public void testLoadImageForInactiveService() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails serviceDetails = new VOServiceDetails();
        serviceDetails.setServiceId("test");

        VOImageResource imageResource = new VOImageResource();
        byte[] content = BaseAdmUmTest.getFileAsByteArray(
                ServiceProvisioningServiceBeanIT.class, "icon1.png");
        imageResource.setBuffer(content);
        imageResource.setContentType("image/png");
        imageResource.setImageType(ImageType.SERVICE_IMAGE);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        serviceDetails = svcProv.createService(tp, serviceDetails,
                imageResource);

        VOPriceModel priceModel = createPriceModel();
        serviceDetails = svcProv.savePriceModel(serviceDetails, priceModel);
        publishToLocalMarketplaceSupplier(serviceDetails, mpSupplier);

        VOService service = svcProv.activateService(serviceDetails);
        serviceDetails = svcProv.getServiceDetails(service);
        VOImageResource loadImage = svcProv.loadImage(Long.valueOf(service
                .getKey()));
        Assert.assertNotNull(loadImage);

        service = svcProv.deactivateService(serviceDetails);
        loadImage = svcProv.loadImage(Long.valueOf(service.getKey()));
        Assert.assertNotNull(loadImage);

        // as customer
        container.login(customerUserKey);
        loadImage = svcProv.loadImage(Long.valueOf(service.getKey()));
        Assert.assertNotNull(loadImage);

    }

    @Test(expected = SaaSSystemException.class)
    public void testCreateMarketingProductImageTypeInvalid() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        VOImageResource imageResource = new VOImageResource();
        byte[] content = BaseAdmUmTest.getFileAsByteArray(
                ServiceProvisioningServiceBeanIT.class, "icon1.png");
        imageResource.setBuffer(content);
        imageResource.setContentType("image/png");
        imageResource.setImageType(ImageType.SHOP_LOGO_LEFT);
        try {
            container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
            svcProv.createService(tp, product, imageResource);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ValidationException.class)
    public void testCreateService_ToLongName() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        product.setName(TOO_LONG_NAME);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.createService(tp, product, null);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateService_ToLongName() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails service = svcProv.createService(tp, product, null);
        service.setName(TOO_LONG_NAME);
        svcProv.updateService(service, null);
    }

    @Test
    public void testCreatePriceModelForExternalProductChargeable()
            throws Exception {
        VOTechnicalService vo = createTechnicalProduct(ServiceAccessType.LOGIN,
                "", BillingAdapterIdentifier.NATIVE_BILLING.name());
        svcProv.deleteTechnicalService(vo);
        vo = createTechnicalProduct(ServiceAccessType.EXTERNAL, "",
                BillingAdapterIdentifier.NATIVE_BILLING.name());
        createMarketingPermission(vo.getTechnicalServiceId());
        VOServiceDetails sd = createProduct(vo, "ExternalProductChargeable",
                svcProv);
        VOPriceModel pm = new VOPriceModel();
        pm.setType(PriceModelType.PRO_RATA);
        try {
            svcProv.savePriceModel(sd, pm);
        } catch (ValidationException ex) {
            assertEquals(ex.getReason(),
                    ReasonEnum.EXTERNAL_SERVICE_MUST_BE_FREE_OF_CHARGE);
        }
    }

    @Test(expected = SaaSSystemException.class)
    public void testCreateMarketingProductImageTypeNull() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        VOImageResource imageResource = new VOImageResource();
        byte[] content = BaseAdmUmTest.getFileAsByteArray(
                ServiceProvisioningServiceBeanIT.class, "icon1.png");
        imageResource.setBuffer(content);
        imageResource.setContentType("image/png");
        try {
            container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
            svcProv.createService(tp, product, imageResource);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testUpdateMarketingProduct_DeleteImageInactive()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        VOImageResource imageResource = new VOImageResource();
        byte[] content = BaseAdmUmTest.getFileAsByteArray(
                ServiceProvisioningServiceBeanIT.class, "icon1.png");
        imageResource.setBuffer(content);
        imageResource.setContentType("image/png");
        imageResource.setImageType(ImageType.SERVICE_IMAGE);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        product = svcProv.createService(tp, product, imageResource);
        VOImageResource loadImage = svcProv.loadImage(Long.valueOf(product
                .getKey()));
        Assert.assertNotNull(loadImage);
        loadImage.setBuffer(null);
        product = svcProv.updateService(product, loadImage);
        loadImage = svcProv.loadImage(Long.valueOf(product.getKey()));
        Assert.assertNull(loadImage);
    }

    @Test
    public void testUpdateMarketingProduct_DeleteImageSuspended()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("test");
        VOImageResource imageResource = new VOImageResource();
        byte[] content = BaseAdmUmTest.getFileAsByteArray(
                ServiceProvisioningServiceBeanIT.class, "icon1.png");
        imageResource.setBuffer(content);
        imageResource.setContentType("image/png");
        imageResource.setImageType(ImageType.SERVICE_IMAGE);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        product = svcProv.createService(tp, product, imageResource);
        VOImageResource loadImage = svcProv.loadImage(Long.valueOf(product
                .getKey()));
        Assert.assertNotNull(loadImage);
        setProductStatus(ServiceStatus.SUSPENDED, product.getKey());
        product.setVersion(product.getVersion() + 1);
        loadImage.setBuffer(null);
        product = svcProv.updateService(product, loadImage);
        loadImage = svcProv.loadImage(Long.valueOf(product.getKey()));
        Assert.assertNull(loadImage);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testExportTechnicalProductsNotOwned() throws Exception {
        List<VOTechnicalService> products = createTechnicalProducts(svcProv);
        String userKey = createTechnologyProvider();
        container.login(userKey, ROLE_TECHNOLOGY_MANAGER);
        svcProv.exportTechnicalServices(products);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSaveTechnicalProductNotOwned() throws Exception {
        VOTechnicalService product = createTechnicalProduct(svcProv);
        String userKey = createTechnologyProvider();
        container.login(userKey, ROLE_TECHNOLOGY_MANAGER);
        svcProv.saveTechnicalServiceLocalization(product);
    }

    @Test
    public void testSavePriceModelWithParametersRemoveAllOnUpdate()
            throws Exception {
        VOServiceDetails product = createProductWithParameters("test");
        VOPriceModel priceModel = createChargeablePriceModel();
        List<VOParameter> parameters = product.getParameters();
        List<VOPricedParameter> pricedParams = new ArrayList<VOPricedParameter>();
        for (VOParameter p : parameters) {
            if (p.isConfigurable()) {
                VOPricedParameter pp = new VOPricedParameter(
                        p.getParameterDefinition());
                pp.setParameterKey(p.getKey());
                pp.setPricePerSubscription(BigDecimal.valueOf(50));
                pricedParams.add(pp);
            }
        }
        priceModel.setSelectedParameters(pricedParams);
        product = svcProv.savePriceModel(product, priceModel);
        priceModel = product.getPriceModel();
        Assert.assertEquals(2, priceModel.getSelectedParameters().size());
        pricedParams.clear();
        priceModel.setSelectedParameters(pricedParams);
        product = svcProv.savePriceModel(product, priceModel);
        Assert.assertEquals(0, product.getPriceModel().getSelectedParameters()
                .size());
    }

    @Test
    public void testSavePriceModelWithParametersRemoveOneOnUpdate()
            throws Exception {
        VOServiceDetails product = createProductWithParameters("test");
        VOPriceModel priceModel = createChargeablePriceModel();
        List<VOParameter> parameters = product.getParameters();
        List<VOPricedParameter> pricedParams = new ArrayList<VOPricedParameter>();
        for (VOParameter p : parameters) {
            if (p.isConfigurable()) {
                VOPricedParameter pp = new VOPricedParameter(
                        p.getParameterDefinition());
                pp.setParameterKey(p.getKey());
                pp.setPricePerSubscription(BigDecimal.valueOf(50));
                pricedParams.add(pp);
            }
        }
        priceModel.setSelectedParameters(pricedParams);
        product = svcProv.savePriceModel(product, priceModel);
        priceModel = product.getPriceModel();
        pricedParams = priceModel.getSelectedParameters();
        Assert.assertEquals(2, pricedParams.size());
        pricedParams.remove(0);
        product = svcProv.savePriceModel(product, priceModel);
        priceModel = product.getPriceModel();
        Assert.assertEquals(1, priceModel.getSelectedParameters().size());
        Assert.assertEquals(pricedParams.get(0).getPricePerSubscription(),
                priceModel.getSelectedParameters().get(0)
                        .getPricePerSubscription());
        Assert.assertEquals(pricedParams.get(0).getParameterKey(), priceModel
                .getSelectedParameters().get(0).getParameterKey());
    }

    @Test
    public void testSavePriceModelWithParametersAddOnUpdate() throws Exception {
        VOServiceDetails product = createProductWithParameters("test");
        VOPriceModel priceModel = createChargeablePriceModel();
        List<VOParameter> parameters = product.getParameters();
        List<VOPricedParameter> pricedParams = new ArrayList<VOPricedParameter>();
        VOPricedParameter pricedParam = new VOPricedParameter(parameters.get(0)
                .getParameterDefinition());
        pricedParam.setParameterKey(parameters.get(0).getKey());
        pricedParam.setPricePerSubscription(BigDecimal.valueOf(50));
        VOPricedOption pricedOption = new VOPricedOption();
        pricedOption
                .setParameterOptionKey(parameters.get(0)
                        .getParameterDefinition().getParameterOptions().get(0)
                        .getKey());
        pricedOption.setPricePerSubscription(BigDecimal.valueOf(50));
        pricedParam.getPricedOptions().add(pricedOption);
        pricedParams.add(pricedParam);
        priceModel.setSelectedParameters(pricedParams);
        product = svcProv.savePriceModel(product, priceModel);

        priceModel = product.getPriceModel();
        Assert.assertEquals(1, priceModel.getSelectedParameters().size());
        pricedParams = priceModel.getSelectedParameters();
        pricedParam = new VOPricedParameter(parameters.get(1)
                .getParameterDefinition());
        pricedParam.setParameterKey(parameters.get(1).getKey());
        pricedParam.setPricePerSubscription(BigDecimal.valueOf(45));
        pricedParams.add(pricedParam);
        product = svcProv.savePriceModel(product, priceModel);
        priceModel = product.getPriceModel();
        Assert.assertEquals(2, priceModel.getSelectedParameters().size());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelWithParameterNonConfigurablePricedParameter()
            throws Exception {
        VOServiceDetails product = createProductWithParameters("test1");
        VOPriceModel priceModel = createChargeablePriceModel();
        List<VOParameter> parameters = product.getParameters();
        List<VOPricedParameter> pricedParams = new ArrayList<VOPricedParameter>();
        for (VOParameter p : parameters) {
            if (!p.isConfigurable()) {
                VOPricedParameter pp = new VOPricedParameter(
                        p.getParameterDefinition());
                pp.setParameterKey(p.getKey());
                pp.setPricePerSubscription(BigDecimal.valueOf(50));
                pricedParams.add(pp);
            }
        }
        priceModel.setSelectedParameters(pricedParams);
        svcProv.savePriceModel(product, priceModel);
    }

    // refers to bug 5345
    @Test
    public void testSaveTechnicalProductNoLocalizedLicenseInUsersLocale()
            throws Exception {
        // create technology provider and user, both with german locale
        final Organization tp = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                Organization tp = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser user = Organizations.createUserForOrg(mgr, tp,
                        true, "admin");
                user.setLocale("de");
                return tp;
            }
        });

        // create technical product and add license in english locale
        TechnicalProduct technicalProduct = runTX(new Callable<TechnicalProduct>() {

            @Override
            public TechnicalProduct call() throws Exception {
                TechnicalProduct technicalProduct = TechnicalProducts
                        .createTechnicalProduct(mgr, tp, "tpId", false,
                                ServiceAccessType.DIRECT);
                localizer.storeLocalizedResource("en",
                        technicalProduct.getKey(),
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                        "the license text in english language");
                return technicalProduct;
            }
        });

        // read the technical product
        Organization storedTP = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                Organization result = mgr.getReference(Organization.class,
                        tp.getKey());
                result.getPlatformUsers().size();
                return result;
            }
        });
        container.login(
                String.valueOf(storedTP.getPlatformUsers().get(0).getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        List<VOTechnicalService> technicalProducts = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertNotNull("No technical product found", technicalProducts);
        Assert.assertEquals("wrong number of technical products found", 1,
                technicalProducts.size());
        VOTechnicalService voTechnicalProduct = technicalProducts.get(0);
        Assert.assertEquals("Wrong technical product found",
                technicalProduct.getKey(), voTechnicalProduct.getKey());

        // save the technical product with new license
        voTechnicalProduct.setLicense("the new license text");
        deleteEmptyTp_keyRecord(voTechnicalProduct);
        svcProv.saveTechnicalServiceLocalization(voTechnicalProduct);

        // ensure that technical product is updated with new license
        technicalProducts = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        voTechnicalProduct = technicalProducts.get(0);
        Assert.assertEquals("wrong license text", "the new license text",
                voTechnicalProduct.getLicense());
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalProduct_DirectAndRoles() throws Exception {
        String toImport = TECHNICAL_SERVICES_XML
                .replaceAll(ServiceAccessType.LOGIN.name(),
                        ServiceAccessType.DIRECT.name());
        svcProv.importTechnicalServices(toImport.getBytes("UTF-8"));
    }

    @Test(expected = UpdateConstraintException.class)
    public void testImportTechnicalProduct_RemoveAssignedRoles()
            throws Exception {
        String rc = svcProv.importTechnicalServices(getTSWithRoles(true));
        Assert.assertEquals("", rc);
        final List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        VOTechnicalService tp = list.get(0);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(product, priceModel);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", tp.getRoleDefinitions().get(0));
        Assert.assertNotNull(subId);
        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        svcProv.importTechnicalServices(getTSWithRoles(false));
    }

    @Test(expected = EJBAccessException.class)
    public void testImportTechnicalProduct_WrongRole() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        try {
            svcProv.importTechnicalServices(readBytesFromFile("TechnicalServices.xml"));
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testGetServicesCheckParamDefHandling() throws Exception {
        initTechnicalProductAndProductForParamDefTesting();

        // as supplier
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        List<VOService> result = svcProv.getSuppliedServices();
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        VOService voService = result.get(0);
        Assert.assertEquals(2, voService.getParameters().size());
    }

    @Test
    public void testGetServiceForSubscriptionCheckParamDefHandling()
            throws Exception {
        final Product prod = initTechnicalProductAndProductForParamDefTesting();
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        List<VOService> result = svcProv.getSuppliedServices();
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        String subscriptionId = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                Product product = mgr
                        .getReference(Product.class, prod.getKey());
                product.setTemplate(product);
                Subscription sub = Subscriptions.createSubscription(mgr,
                        customerOrgId, prod.getProductId(), "subId", supplier);
                return sub.getSubscriptionId();
            }
        });

        VOOrganization customer = new VOOrganization();
        customer.setKey(this.customer.getKey());
        VOServiceDetails serviceDetails = svcProv.getServiceForSubscription(
                customer, subscriptionId);
        Assert.assertNotNull(serviceDetails);
        List<VOParameterDefinition> parameterDefinitions = serviceDetails
                .getTechnicalService().getParameterDefinitions();
        Assert.assertEquals(5, parameterDefinitions.size());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testCreateServiceForParamDefHandlingDefineInvalid()
            throws Exception {
        initTechnicalProductAndProductForParamDefTesting();
        // tp contains two parameter definitions that are not configurable. By
        // retrieving the tp as technology provider, those are also returned.
        // Hence we created parameters based on them, what must cause the
        // expected exception.
        List<VOTechnicalService> services = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
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
        svcProv.createService(tp, serviceToCreate, null);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelWithStringBasedPricedParam() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Scenario.setup(container, false);
                return null;
            }
        });
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_SERVICE_MANAGER);
        String productId = Scenario.getProduct().getProductId();
        List<VOService> services = svcProv.getSuppliedServices();
        VOServiceDetails currentService = null;
        for (VOService voService : services) {
            if (voService.getServiceId().equals(productId)) {
                currentService = svcProv.getServiceDetails(voService);
            }
        }
        if (currentService != null) {
            List<VOParameter> parameters = currentService.getParameters();
            for (VOParameter voParameter : parameters) {
                if (voParameter.getParameterDefinition().getValueType() == ParameterValueType.STRING) {
                    VOPricedParameter pp = new VOPricedParameter(
                            voParameter.getParameterDefinition());
                    pp.setParameterKey(voParameter.getKey());
                    pp.setPricePerUser(BigDecimal.valueOf(12L));
                    currentService.getPriceModel().getSelectedParameters()
                            .add(pp);
                }
            }
            svcProv.savePriceModel(currentService,
                    currentService.getPriceModel());
        }
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelWithPricedParamWithoutParamDef()
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Scenario.setup(container, false);
                return null;
            }
        });
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_SERVICE_MANAGER);
        String productId = Scenario.getProduct().getProductId();

        List<VOService> services = svcProv.getSuppliedServices();
        VOServiceDetails currentService = null;
        for (VOService voService : services) {
            if (voService.getServiceId().equals(productId)) {
                currentService = svcProv.getServiceDetails(voService);
            }
        }
        if (currentService != null) {
            List<VOParameter> parameters = currentService.getParameters();
            for (VOParameter voParameter : parameters) {
                if (voParameter.getParameterDefinition().getValueType() == ParameterValueType.STRING) {
                    VOPricedParameter pp = new VOPricedParameter();
                    pp.setParameterKey(voParameter.getKey());
                    pp.setPricePerUser(BigDecimal.valueOf(12L));
                    currentService.getPriceModel().getSelectedParameters()
                            .add(pp);
                }
            }
            svcProv.savePriceModel(currentService,
                    currentService.getPriceModel());
        }
    }

    @Test
    public void testCreateTechnicalProductWithTags() throws Exception {
        final String[] tagsvo = new String[] { "storage", "free",
                "stürmisch kalt" };
        VOTechnicalService vo = createTechnicalServiceWithTags("tp1", tagsvo,
                BillingAdapterIdentifier.NATIVE_BILLING.name());

        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals(4, list.size());
        VOTechnicalService svc = list.get(3);
        Assert.assertEquals(vo.getTechnicalServiceId(),
                svc.getTechnicalServiceId());

        // validate tags
        List<String> svcTags = svc.getTags();
        checkTags(tagsvo, svcTags);
    }

    @Test
    /*
     * Test of deleting technical product.checking the tags
     */
    public void testDeleteTechnicalProductWithTags() throws Exception {
        // create two services
        final String[] tagsvo1 = new String[] { "tag1", "tag2", "tag3" };
        VOTechnicalService vo1 = createTechnicalServiceWithTags("tp1", tagsvo1,
                BillingAdapterIdentifier.NATIVE_BILLING.name());

        final String[] tagsvo2 = new String[] { "tag2", "tag3", "tag4" };
        VOTechnicalService vo2 = createTechnicalServiceWithTags("tp2", tagsvo2,
                BillingAdapterIdentifier.NATIVE_BILLING.name());

        // reread services
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals(5, list.size());
        VOTechnicalService svc1 = list.get(3);
        VOTechnicalService svc2 = list.get(4);
        Assert.assertEquals(vo1.getTechnicalServiceId(),
                svc1.getTechnicalServiceId());
        Assert.assertEquals(vo2.getTechnicalServiceId(),
                svc2.getTechnicalServiceId());

        // now we delete service 1
        svcProv.deleteTechnicalService(svc1);

        // validate service tags
        list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals(4, list.size());
        svc2 = list.get(3);
        Assert.assertEquals(vo2.getTechnicalServiceId(),
                svc2.getTechnicalServiceId());

        List<String> svcTags2 = svc2.getTags();
        checkTags(tagsvo2, svcTags2);

        // now check also all existing tags of the datastore
        List<String> allTags = getAllTagsByLocale("en", "%");
        assertEquals(4, allTags.size());
        assertTrue(allTags.contains("tag2"));
        assertTrue(allTags.contains("tag3"));
        assertTrue(allTags.contains("tag4"));

        // now delete service2 as well
        svcProv.deleteTechnicalService(svc2);

        // and re-check
        // reread services
        list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals(3, list.size());

        // now check also all existing tags of the datastore (no more exists!)
        allTags = getAllTagsByLocale("en", "%");
        assertEquals(1, allTags.size());
    }

    @Test(expected = ValidationException.class)
    public void testCreateTechnicalProductWithTooManyTags() throws Exception {
        final String[] tagsvo = new String[] { "tag1", "tag2", "tag3", "tag4",
                "tag5", "tag6" };
        try {
            createTechnicalServiceWithTags("tp1", tagsvo,
                    BillingAdapterIdentifier.NATIVE_BILLING.name());

        } catch (ValidationException ve) {
            assertEquals(ReasonEnum.TAGS_MAX_COUNT, ve.getReason());
            throw ve;
        }
    }

    @Test(expected = ValidationException.class)
    public void testUpdateTechnicalProductWithTooManyTags() throws Exception {
        // create service
        final String[] tagsvo1 = new String[] { "tag1", "tag2", "tag3" };
        VOTechnicalService vo1 = createTechnicalServiceWithTags("tp1", tagsvo1,
                BillingAdapterIdentifier.NATIVE_BILLING.name());

        // reread service
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals(4, list.size());
        VOTechnicalService svc1 = list.get(3);
        Assert.assertEquals(vo1.getTechnicalServiceId(),
                svc1.getTechnicalServiceId());

        // validate service tags
        List<String> svcTags1 = svc1.getTags();
        checkTags(tagsvo1, svcTags1);
        try {
            // now we change the service definition wrongly
            final String[] tagsvo1_new = new String[] { "tag1", "tag2", "tag5",
                    "tag6", "tag7", "tag8" };
            svc1.setTags(Arrays.asList(tagsvo1_new));
            deleteEmptyTp_keyRecord(svc1);
            svcProv.saveTechnicalServiceLocalization(svc1);

        } catch (ValidationException ve) {
            assertEquals(ReasonEnum.TAGS_MAX_COUNT, ve.getReason());
            throw ve;
        }
    }

    /**
     * Check the execution of the resume service method with a not allowed user
     * role.
     */
    @Test(expected = EJBException.class)
    public void testResumeService_invalidUserRole() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.resumeService(null);
    }

    /**
     * Check the execution of the suspend service method with a not allowed user
     * role.
     */
    @Test(expected = EJBException.class)
    public void testSuspendService_invalidUserRole() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.suspendService(null, null);
    }

    /**
     * Check the execution of the resume service method with a allowed user
     * role. (For reasons of simplification we pass null and expect a illegal
     * argument exception)
     */
    @Test(expected = IllegalArgumentException.class)
    public void testResumeService_validUserRole() throws Exception {
        container.login(supplierUserKey, ROLE_MARKETPLACE_OWNER);
        try {
            svcProv.resumeService(null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    /**
     * Check the execution of the suspend service method with a allowed user
     * role. (For reasons of simplification we pass null and expect a illegal
     * argument exception)
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSuspendService_validUserRole() throws Exception {
        container.login(supplierUserKey, ROLE_MARKETPLACE_OWNER);
        try {
            svcProv.suspendService(null, null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testStatusAllowsDeletionWrongOwner() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        VOServiceDetails template = createProduct(techProduct, "product",
                svcProv);
        VOOrganization customer = runTX(new Callable<VOOrganization>() {

            @Override
            public VOOrganization call() throws Exception {
                Organization result = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                Organization currentOrg = new Organization();
                currentOrg.setOrganizationId(customerOrgId);
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
        VOServiceDetails custSpec = svcProv.savePriceModelForCustomer(template,
                priceModel, customer);
        svcProv.activateService(custSpec);
        svcProv.statusAllowsDeletion(template);
        Assert.fail(" should throw OperationNotPermittedException");

    }

    @Test
    public void updateMarketingAndCustomerSpecificProduct_IdAndParametersDeleteAndAdd()
            throws Exception {
        final String customerOrgId = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                OrganizationReference ref = new OrganizationReference(
                        Organizations.findOrganization(mgr, supplierOrgId),
                        org, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                mgr.persist(ref);
                return org.getOrganizationId();
            }
        });

        container.login(providerUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService tp = createTechnicalProduct(svcProv);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product = createProduct(tp,
                "modifiedProductIdAddParam", svcProv);

        // Create a customer specific version
        VOServiceDetails createdCustomerProductDetails = svcProv
                .getServiceDetails(product);
        VOOrganization voCustomerOrg = getOrganizationForOrgId(customerOrgId);
        VOPriceModel pm = createPriceModel();
        createdCustomerProductDetails = svcProv.savePriceModelForCustomer(
                createdCustomerProductDetails, pm, voCustomerOrg);

        List<VOParameterDefinition> parameterDefinitions = tp
                .getParameterDefinitions();
        Assert.assertTrue(
                "No parameter definitions found for technical product",
                0 < parameterDefinitions.size());
        // product has no parameters so far, so add two
        VOParameterDefinition parameterDefinition1 = parameterDefinitions
                .get(0);
        VOParameterDefinition parameterDefinition2 = parameterDefinitions
                .get(1);
        VOParameter parameter1 = new VOParameter(parameterDefinition1);
        parameter1.setValue("1223");
        parameter1.setConfigurable(true);
        VOParameter parameter2 = new VOParameter(parameterDefinition2);
        parameter2.setValue("2234");
        parameter2.setConfigurable(true);

        List<VOParameter> parameters = new ArrayList<VOParameter>();
        parameters.add(parameter1);
        product.setParameters(parameters);
        VOServiceDetails updateMarketingProduct = svcProv.updateService(
                product, null);

        // remove a parameter and set the others value to null and make it
        // non-configurable, then update. Both entries must disappear
        parameter1 = updateMarketingProduct.getParameters().get(0);
        parameter1.setValue(null);
        parameter1.setConfigurable(false);
        updateMarketingProduct.setParameters(Arrays.asList(new VOParameter[] {
                parameter1, parameter2 }));

        updateMarketingProduct = svcProv.updateService(updateMarketingProduct,
                null);

        createdCustomerProductDetails = svcProv
                .getServiceDetails(createdCustomerProductDetails);

        Assert.assertEquals("Parameters size", updateMarketingProduct
                .getParameters().size(), createdCustomerProductDetails
                .getParameters().size());
        Assert.assertEquals("Parameters add", updateMarketingProduct
                .getParameters().get(0).getParameterDefinition().getKey(),
                createdCustomerProductDetails.getParameters().get(0)
                        .getParameterDefinition().getKey());
    }

    private VOParameterDefinition findConfigurableParam(
            List<VOParameterDefinition> parameterDefinitions, boolean mandatory) {
        VOParameterDefinition parameterDefinition = null;
        for (VOParameterDefinition voParamDef : parameterDefinitions) {
            if ((voParamDef.isMandatory() == mandatory)
                    && voParamDef.isConfigurable())
                parameterDefinition = voParamDef;
        }
        return parameterDefinition;
    }

    private void createMarketingProductWithParameter(String value,
            boolean configurable, boolean mandatory) throws Exception {
        VOTechnicalService tp = createTechnicalProductWithMandatoryParameter(svcProv);
        List<VOParameter> params = new ArrayList<VOParameter>();
        List<VOParameterDefinition> paramDefinitions = tp
                .getParameterDefinitions();
        VOParameterDefinition paramDef = findConfigurableParam(
                paramDefinitions, mandatory);
        assertNotNull(paramDef);
        VOParameter param = new VOParameter(paramDef);
        param.setValue(value);
        param.setConfigurable(configurable);
        params.add(param);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("testproduct");
        product.setParameters(params);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        product = svcProv.createService(tp, product, null);
    }

    private void updateMarketingProductWithParameter(String value,
            boolean configurable, boolean mandatory) throws Exception {
        VOTechnicalService tp = createTechnicalProductWithMandatoryParameter(svcProv);
        List<VOParameterDefinition> parameterDefinitions = tp
                .getParameterDefinitions();
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp,
                "modifiedProductIdAddParam", svcProv);
        assertTrue("No parameter definitions found for technical product",
                0 < parameterDefinitions.size());
        VOParameterDefinition parameterDefinition = findConfigurableParam(
                parameterDefinitions, mandatory);
        assertNotNull(parameterDefinition);
        VOParameter parameter = new VOParameter(parameterDefinition);
        parameter.setValue(value);
        parameter.setConfigurable(configurable);
        List<VOParameter> parameters = new ArrayList<VOParameter>();
        parameters.add(parameter);
        product.setParameters(parameters);
        product = svcProv.updateService(product, null);
    }

}
