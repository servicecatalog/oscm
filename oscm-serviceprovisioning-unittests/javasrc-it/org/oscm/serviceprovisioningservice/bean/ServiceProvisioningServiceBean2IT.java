/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: kulle                                         
 *                                                                              
 *  Creation Date: 06.12.2011                                                      
 *                                                                              
 *  Completion Time: 06.12.2011                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.CategoryToCatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductReference;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PriceModelException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ServiceCompatibilityException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOTechnicalService;

@SuppressWarnings("boxing")
public class ServiceProvisioningServiceBean2IT
        extends ServiceProvisioningServiceTestBase {

    /**
     * Test the export of technical service where the subscription restriction
     * is set to true. This test checks if the attribute
     * "onlyOneSubscriptionPerUser", defined at the import of the technical
     * service xml, is also exported with the correct value "true".
     * 
     * @throws Exception
     */
    @Test
    public void testExportTechnicalProducts_OneSubscription() throws Exception {
        boolean subscriptionRestriction = true;
        createTechnicalProductWithSubscriptionRestriction(svcProv,
                subscriptionRestriction);
        List<VOTechnicalService> list2 = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        List<VOTechnicalService> list = Collections
                .singletonList(list2.get(list2.size() - 1));
        byte[] content = svcProv.exportTechnicalServices(list);
        Document document = XMLConverter
                .convertToDocument(new ByteArrayInputStream(content));
        Assert.assertEquals(1, document.getChildNodes().getLength());
        Element root = document.getDocumentElement();
        Assert.assertEquals("tns:TechnicalServices", root.getNodeName());
        List<Node> productNodes = XMLConverter.getNodeList(root.getChildNodes(),
                "tns:TechnicalService");
        Assert.assertEquals(1, productNodes.size());
        compareSubscriptionRestriction(productNodes.get(0),
                subscriptionRestriction);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelWithParameterFromDifferentTP()
            throws Exception {
        VOServiceDetails product1 = runTX(new Callable<VOServiceDetails>() {
            @Override
            public VOServiceDetails call() throws Exception {
                return createProductWithParameters("test1");
            }
        });
        VOTechnicalService tp = svcProv
                .getTechnicalServices(OrganizationRoleType.SUPPLIER).get(1);
        Assert.assertFalse(product1.getTechnicalService()
                .getTechnicalServiceId().equals(tp.getTechnicalServiceId()));
        List<VOParameterDefinition> parameterDefinitions = tp
                .getParameterDefinitions();
        List<VOParameter> params = new ArrayList<VOParameter>();
        for (VOParameterDefinition def : parameterDefinitions) {
            VOParameter param = new VOParameter(def);
            param.setValue("");
            params.add(param);
        }
        VOServiceDetails product2 = new VOServiceDetails();
        product2.setServiceId("test2");
        product2.setParameters(params);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        product2 = svcProv.createService(tp, product2, null);
        VOPriceModel priceModel = createChargeablePriceModel();
        List<VOParameter> parameters = product1.getParameters();
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
        svcProv.savePriceModel(product2, priceModel);
    }

    @Test
    public void testImportTechnicalProduct() throws Exception {
        String rc = svcProv.importTechnicalServices(
                readBytesFromFile("TechnicalServices.xml"));
        Assert.assertEquals("", rc);
        final List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals(list.get(3).getAccessType(),
                ServiceAccessType.LOGIN);
        Assert.assertEquals(list.get(4).getAccessType(),
                ServiceAccessType.EXTERNAL);
        Assert.assertEquals(list.get(5).getAccessType(),
                ServiceAccessType.USER);
    }

    @Test
    public void testGetTechnicalServicesCheckParamDefHandling()
            throws Exception {
        initTechnicalProductAndProductForParamDefTesting();
        List<VOTechnicalService> technicalServices = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertNotNull(technicalServices);
        Assert.assertEquals(4, technicalServices.size());
        Assert.assertEquals(7,
                technicalServices.get(3).getParameterDefinitions().size());

        // now login as supplier, and verify that the non configurable
        // parameters are not returned
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        technicalServices = svcProv
                .getTechnicalServices(OrganizationRoleType.SUPPLIER);
        Assert.assertNotNull(technicalServices);
        Assert.assertEquals(4, technicalServices.size());
        Assert.assertEquals(10,
                technicalServices.get(0).getParameterDefinitions().size());
    }

    @Test
    public void testGetServiceDetailsCheckParamDefHandling() throws Exception {
        initTechnicalProductAndProductForParamDefTesting();
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        List<VOService> result = svcProv.getSuppliedServices();
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        VOService voService = result.get(0);

        // as supplier
        VOServiceDetails serviceDetails = svcProv.getServiceDetails(voService);
        List<VOParameterDefinition> parameterDefinitions = serviceDetails
                .getTechnicalService().getParameterDefinitions();
        Assert.assertEquals(5, parameterDefinitions.size());
    }

    @Test
    public void testGetServiceForCustomerCheckParamDefHandling()
            throws Exception {
        container.login(supplierUserKey, ROLE_TECHNOLOGY_MANAGER,
                ROLE_SERVICE_MANAGER);
        final Product prod = initTechnicalProductAndProductForParamDefTesting();
        List<VOService> result = svcProv.getSuppliedServices();
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        VOService voService = result.get(0);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = mgr.getReference(Product.class,
                        prod.getKey());
                product.setTargetCustomer(customer);
                product.setTemplate(product);
                return null;
            }
        });

        VOOrganization customer = new VOOrganization();
        customer.setKey(this.customer.getKey());
        VOServiceDetails serviceDetails = svcProv
                .getServiceForCustomer(customer, voService);
        Assert.assertNotNull(serviceDetails);
        List<VOParameterDefinition> parameterDefinitions = serviceDetails
                .getTechnicalService().getParameterDefinitions();
        Assert.assertEquals(5, parameterDefinitions.size());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testUpdateServiceForParamDefHandling() throws Exception {
        initTechnicalProductAndProductForParamDefTesting();
        List<VOTechnicalService> services = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        VOTechnicalService tp = services.get(services.size() - 1);
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
        VOServiceDetails serviceDetails = svcProv.createService(tp,
                serviceToCreate, null);

        services = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        tp = services.get(0);
        parameters = serviceDetails.getParameters();
        for (VOParameterDefinition paramDef : tp.getParameterDefinitions()) {
            if (!paramDef.isConfigurable()) {
                VOParameter param = new VOParameter();
                param.setConfigurable(true);
                param.setValue("123");
                param.setParameterDefinition(paramDef);
                parameters.add(param);
            }
        }
        svcProv.updateService(serviceDetails, null);
    }

    @Test
    public void testCreateService_CatalogEntry_ServicesOnSameMarketplace()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("testCatalogEntry1");
        product.setName("CatalogTest 1");
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails svc1 = svcProv.createService(tp, product, null);
        publishToLocalMarketplaceSupplier(svc1, mpSupplier);

        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        tp = createTechnicalProduct(svcProv);
        product = new VOServiceDetails();
        product.setServiceId("testCatalogEntry2");
        product.setName("CatalogTest 2");
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails svc2 = svcProv.createService(tp, product, null);
        publishToLocalMarketplaceSupplier(svc2, mpSupplier);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Organization organization = Organizations.findOrganization(mgr,
                        supplierOrgId);
                assertNotNull("Cannot find providerOrg", organization);
                Product product1 = Products.findProduct(mgr, organization,
                        "testCatalogEntry1");
                assertNotNull("Test product 1 not found", product1);
                CatalogEntry ce1 = product1.getCatalogEntries().get(0);
                assertNotNull("Test product 1 has no catalog entry", ce1);

                Product product2 = Products.findProduct(mgr, organization,
                        "testCatalogEntry2");
                assertNotNull("Test product 2 not found", product2);
                CatalogEntry ce2 = product2.getCatalogEntries().get(0);
                assertNotNull("Test product 2 has no catalog entry", ce2);

                return null;
            }
        });

    }

    @Test
    public void testCreateService_CatalogEntry_TwoServicesNullMarketplace()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = new VOServiceDetails();
        product.setServiceId("testCatalogEntry1");
        product.setName("CatalogTest 1");
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.createService(tp, product, null);

        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        tp = createTechnicalProduct(svcProv);
        product = new VOServiceDetails();
        product.setServiceId("testCatalogEntry2");
        product.setName("CatalogTest 2");
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.createService(tp, product, null);

        // main objective of this test is to prove that creating two services
        // with null marketplace is ok (and does not result in exception); thus
        // if we get here without exception, everything's fine

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Organization organization = Organizations.findOrganization(mgr,
                        supplierOrgId);
                assertNotNull("Cannot find providerOrg", organization);
                Product product1 = Products.findProduct(mgr, organization,
                        "testCatalogEntry1");
                assertNotNull("Test product 1 not found", product1);
                CatalogEntry ce1 = product1.getCatalogEntries().get(0);
                assertNotNull("Test product 1 has no catalog entry", ce1);

                Product product2 = Products.findProduct(mgr, organization,
                        "testCatalogEntry2");
                assertNotNull("Test product 2 not found", product2);
                CatalogEntry ce2 = product2.getCatalogEntries().get(0);
                assertNotNull("Test product 2 has no catalog entry", ce2);

                return null;
            }
        });

    }

    @Test
    public void testUpdateTechnicalProductTags() throws Exception {
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

        // validate service tags
        List<String> svcTags1 = svc1.getTags();
        checkTags(tagsvo1, svcTags1);
        List<String> svcTags2 = svc2.getTags();
        checkTags(tagsvo2, svcTags2);

        // now check also all existing tags of the datastore
        List<String> allTags = getAllTagsByLocale("en", "%");
        assertEquals(5, allTags.size());
        assertTrue(allTags.contains("tag1"));
        assertTrue(allTags.contains("tag2"));
        assertTrue(allTags.contains("tag3"));
        assertTrue(allTags.contains("tag4"));

        // now we change the service definition of tp2
        final String[] tagsvo2_new = new String[] { "tag2", "tag5" };
        svc2.setTags(Arrays.asList(tagsvo2_new));
        deleteEmptyTp_keyRecord(svc2);
        svcProv.saveTechnicalServiceLocalization(svc2);

        // and re-check
        // reread services
        list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals(5, list.size());
        svc1 = list.get(3);
        svc2 = list.get(4);
        Assert.assertEquals(vo1.getTechnicalServiceId(),
                svc1.getTechnicalServiceId());
        Assert.assertEquals(vo2.getTechnicalServiceId(),
                svc2.getTechnicalServiceId());

        // validate service tags
        svcTags1 = svc1.getTags();
        checkTags(tagsvo1, svcTags1);
        svcTags2 = svc2.getTags();
        checkTags(tagsvo2_new, svcTags2);

        // now check also all existing tags of the datastore
        allTags = getAllTagsByLocale("en", "%");
        assertEquals(5, allTags.size());
        assertTrue(allTags.contains("tag1"));
        assertTrue(allTags.contains("tag2"));
        assertTrue(allTags.contains("tag3"));
        assertTrue(allTags.contains("tag5"));

        // now we change the service definition of tp1
        final String[] tagsvo1_new = new String[] { "tag1" };
        svc1.setTags(Arrays.asList(tagsvo1_new));
        deleteEmptyTp_keyRecord(svc1);
        svcProv.saveTechnicalServiceLocalization(svc1);

        // and re-check
        // reread services
        list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Assert.assertEquals(5, list.size());
        svc1 = list.get(3);
        svc2 = list.get(4);
        Assert.assertEquals(vo1.getTechnicalServiceId(),
                svc1.getTechnicalServiceId());
        Assert.assertEquals(vo2.getTechnicalServiceId(),
                svc2.getTechnicalServiceId());

        // validate service tags
        svcTags1 = svc1.getTags();
        checkTags(tagsvo1_new, svcTags1);
        svcTags2 = svc2.getTags();
        checkTags(tagsvo2_new, svcTags2);

        // now check also all existing tags of the datastore
        allTags = getAllTagsByLocale("en", "%");
        assertEquals(4, allTags.size());
        assertTrue(allTags.contains("tag1"));
        assertTrue(allTags.contains("tag2"));
        assertTrue(allTags.contains("tag5"));
    }

    @Test(expected = ServiceStateException.class)
    public void testSetCompatibleProducts_Active() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        product = publishToLocalMarketplaceSupplier(product, mpSupplier);

        svcProv.activateService(product);
        VOServiceDetails voDetails = svcProv.getServiceDetails(product);
        Assert.assertEquals(ServiceStatus.ACTIVE, voDetails.getStatus());
        List<VOService> list = emptyList();
        svcProv.setCompatibleServices(voDetails, list);
    }

    @Test(expected = ServiceCompatibilityException.class)
    public void testSetCompatibleProducts_incompatible() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);

        VOPriceModel priceModel1 = prepareVOPriceModel(product1, USD);
        VOPriceModel priceModel2 = prepareVOPriceModel(product2, EUR);
        product1 = svcProv.savePriceModel(product1, priceModel1);
        product2 = svcProv.savePriceModel(product2, priceModel2);

        try {
            svcProv.setCompatibleServices(product1,
                    Collections.singletonList((VOService) product2));
        } catch (ServiceCompatibilityException e) {
            assertTrue(e.getMessageKey().endsWith(
                    ServiceCompatibilityException.Reason.CURRENCY.name()));
            throw e;
        }
    }

    @Test
    public void testSetCompatibleProducts_compatibleFreeOfCharge()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                return null;
            }
        });
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);

        VOPriceModel priceModel1 = new VOPriceModel();
        priceModel1.setCurrencyISOCode(USD);
        priceModel1.setPeriod(PricingPeriod.MONTH);

        VOPriceModel priceModel2 = createChargeablePriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel1);
        product2 = svcProv.savePriceModel(product2, priceModel2);

        svcProv.setCompatibleServices(product1,
                Collections.singletonList((VOService) product2));
    }

    @Test
    public void testSetCompatibleProducts_compatibleFreeOfCharge1()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                return null;
            }
        });
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);

        VOPriceModel priceModel1 = createChargeablePriceModel();
        priceModel1.setCurrencyISOCode(USD);

        VOPriceModel priceModel2 = createChargeablePriceModel();
        priceModel2.setType(PriceModelType.FREE_OF_CHARGE);
        product1 = svcProv.savePriceModel(product1, priceModel1);
        product2 = svcProv.savePriceModel(product2, priceModel2);

        svcProv.setCompatibleServices(product1,
                Collections.singletonList((VOService) product2));
    }

    @Test
    public void testSetCompatibleProducts_compatibleFreeOfCharge3()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                return null;
            }
        });
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);

        VOPriceModel priceModel1 = createChargeablePriceModel();
        priceModel1.setCurrencyISOCode(USD);
        priceModel1.setType(PriceModelType.FREE_OF_CHARGE);

        VOPriceModel priceModel2 = createChargeablePriceModel();
        priceModel2.setType(PriceModelType.FREE_OF_CHARGE);
        product1 = svcProv.savePriceModel(product1, priceModel1);
        product2 = svcProv.savePriceModel(product2, priceModel2);

        svcProv.setCompatibleServices(product1,
                Collections.singletonList((VOService) product2));
    }

    @Test
    public void testSetCompatibleProducts_bothFreeOfCharge() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);

        VOPriceModel priceModel1 = createPriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel1);
        product2 = svcProv.savePriceModel(product2, priceModel1);

        svcProv.setCompatibleServices(product1,
                Collections.singletonList((VOService) product2));
    }

    @Test
    public void testSetCompatibleProducts_compatible() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);

        VOPriceModel priceModel1 = createChargeablePriceModel();
        VOPriceModel priceModel2 = createChargeablePriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel1);
        product2 = svcProv.savePriceModel(product2, priceModel2);

        svcProv.setCompatibleServices(product1,
                Collections.singletonList((VOService) product2));
    }

    @Test
    public void testSet3CompatibleProducts_compatible() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);
        VOServiceDetails product3 = createProduct(techProduct, "product3",
                svcProv);
        publishToLocalMarketplaceSupplier(product3, mpSupplier);

        VOPriceModel priceModel1 = createChargeablePriceModel();
        VOPriceModel priceModel2 = createChargeablePriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel2);
        product2 = svcProv.savePriceModel(product2, priceModel1);
        product3 = svcProv.savePriceModel(product3, priceModel2);

        svcProv.setCompatibleServices(product1,
                Arrays.asList((VOService) product2, product3));
    }

    @Test(expected = ServiceCompatibilityException.class)
    public void testSet3CompatibleProducts_incompatible() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                return null;
            }
        });
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);
        VOServiceDetails product3 = createProduct(techProduct, "product3",
                svcProv);
        publishToLocalMarketplaceSupplier(product3, mpSupplier);

        VOPriceModel priceModel1 = createChargeablePriceModel();
        priceModel1.setCurrencyISOCode(USD);
        VOPriceModel priceModel2 = createChargeablePriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel2);
        product2 = svcProv.savePriceModel(product2, priceModel1);
        product3 = svcProv.savePriceModel(product3, priceModel2);
        try {
            svcProv.setCompatibleServices(product1,
                    Arrays.asList((VOService) product2, product3));
        } catch (ServiceCompatibilityException e) {
            assertTrue(e.getMessageKey().endsWith(
                    ServiceCompatibilityException.Reason.CURRENCY.name()));
            throw e;
        }
    }

    @Test
    public void testSet3CompatibleProducts_2compatible1FreeOfCharge()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                return null;
            }
        });
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);
        VOServiceDetails product3 = createProduct(techProduct, "product3",
                svcProv);
        publishToLocalMarketplaceSupplier(product3, mpSupplier);

        VOPriceModel priceModel1 = createChargeablePriceModel();
        priceModel1.setCurrencyISOCode(USD);
        priceModel1.setType(PriceModelType.FREE_OF_CHARGE);
        VOPriceModel priceModel2 = createChargeablePriceModel();
        priceModel2.setCurrencyISOCode(USD);
        VOPriceModel priceModel3 = createChargeablePriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel1);
        product2 = svcProv.savePriceModel(product2, priceModel2);
        product3 = svcProv.savePriceModel(product3, priceModel3);

        svcProv.setCompatibleServices(product1,
                Arrays.asList((VOService) product2, product3));
    }

    @Test(expected = ServiceCompatibilityException.class)
    public void testSet3CompatibleProducts_2incompatibleFreeOfCharge()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                return null;
            }
        });
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);
        VOServiceDetails product3 = createProduct(techProduct, "product3",
                svcProv);
        publishToLocalMarketplaceSupplier(product3, mpSupplier);

        VOPriceModel priceModel1 = createChargeablePriceModel();
        priceModel1.setCurrencyISOCode(USD);
        priceModel1.setType(PriceModelType.FREE_OF_CHARGE);
        VOPriceModel priceModel2 = createChargeablePriceModel();
        priceModel2.setCurrencyISOCode(USD);
        VOPriceModel priceModel3 = createChargeablePriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel1);
        product2 = svcProv.savePriceModel(product2, priceModel2);
        product3 = svcProv.savePriceModel(product3, priceModel3);
        try {
            svcProv.setCompatibleServices(product2,
                    Arrays.asList((VOService) product1, product3));
        } catch (ServiceCompatibilityException e) {
            assertTrue(e.getMessageKey().endsWith(
                    ServiceCompatibilityException.Reason.CURRENCY.name()));
            throw e;
        }
    }

    @Test
    public void testSet3CompatibleProducts_compatibleFreeOfCharge()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                return null;
            }
        });
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);
        VOServiceDetails product3 = createProduct(techProduct, "product3",
                svcProv);
        publishToLocalMarketplaceSupplier(product3, mpSupplier);

        VOPriceModel priceModel1 = createChargeablePriceModel();
        priceModel1.setCurrencyISOCode(USD);
        priceModel1.setType(PriceModelType.FREE_OF_CHARGE);
        VOPriceModel priceModel2 = createChargeablePriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel2);
        product2 = svcProv.savePriceModel(product2, priceModel1);
        product3 = svcProv.savePriceModel(product3, priceModel2);

        svcProv.setCompatibleServices(product1,
                Arrays.asList((VOService) product2, product3));
    }

    @Test
    public void testSet3CompatibleProducts_compatible2FreeOfCharge()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                return null;
            }
        });
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);
        VOServiceDetails product3 = createProduct(techProduct, "product3",
                svcProv);
        publishToLocalMarketplaceSupplier(product3, mpSupplier);

        VOPriceModel priceModel1 = createChargeablePriceModel();
        priceModel1.setCurrencyISOCode(USD);
        VOPriceModel priceModel2 = createChargeablePriceModel();
        priceModel2.setType(PriceModelType.FREE_OF_CHARGE);
        product1 = svcProv.savePriceModel(product1, priceModel2);
        product2 = svcProv.savePriceModel(product2, priceModel1);
        product3 = svcProv.savePriceModel(product3, priceModel2);

        svcProv.setCompatibleServices(product1,
                Arrays.asList((VOService) product2, product3));
    }

    @Test
    public void testSetCompatibleProducts_Inactive() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);

        VOPriceModel priceModel = createPriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel);
        product2 = svcProv.savePriceModel(product2, priceModel);

        svcProv.setCompatibleServices(product1,
                Collections.singletonList((VOService) product2));

        List<VOService> list = svcProv.getCompatibleServices(product1);
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(product2.getKey(), list.get(0).getKey());
    }

    @Test(expected = ServiceStateException.class)
    public void testSetCompatibleProducts_Deleted() throws Exception {
        VOServiceDetails[] details = prepareProductsForSetCompatibleProducts(
                ServiceStatus.DELETED);
        svcProv.setCompatibleServices(details[0],
                Collections.singletonList((VOService) details[1]));
    }

    @Test(expected = ServiceStateException.class)
    public void testSetCompatibleProducts_Obsolete() throws Exception {
        VOServiceDetails[] details = prepareProductsForSetCompatibleProducts(
                ServiceStatus.OBSOLETE);
        svcProv.setCompatibleServices(details[0],
                Collections.singletonList((VOService) details[1]));
    }

    @Test
    public void testSetCompatibleProducts_Suspended() throws Exception {
        VOServiceDetails[] details = prepareProductsForSetCompatibleProducts(
                ServiceStatus.SUSPENDED);
        svcProv.setCompatibleServices(details[0],
                Collections.singletonList((VOService) details[1]));
        List<VOService> list = svcProv.getCompatibleServices(details[0]);
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(details[1].getKey(), list.get(0).getKey());
    }

    @Test
    public void testSetCompatibleProductsUnset() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);

        VOPriceModel priceModel = createPriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel);
        product2 = svcProv.savePriceModel(product2, priceModel);

        svcProv.setCompatibleServices(product1,
                Collections.singletonList((VOService) product2));
        product1 = svcProv.getServiceDetails(product1);

        List<VOService> list = svcProv.getCompatibleServices(product1);
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(product2.getKey(), list.get(0).getKey());

        List<VOService> emptyList = emptyList();
        svcProv.setCompatibleServices(product1, emptyList);

        list = svcProv.getCompatibleServices(product1);
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());
    }

    @Test(expected = ServiceStateException.class)
    public void testSavePriceModel_Active() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        product = publishToLocalMarketplaceSupplier(product, mpSupplier);
        svcProv.activateService(product);
        VOServiceDetails voDetails = svcProv.getServiceDetails(product);
        Assert.assertEquals(ServiceStatus.ACTIVE, voDetails.getStatus());
        svcProv.savePriceModel(voDetails, new VOPriceModel());
    }

    @Test(expected = ServiceStateException.class)
    public void testSavePriceModel_Deleted() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);
        setProductStatus(ServiceStatus.DELETED, product.getKey());
        product.setVersion(product.getVersion() + 1);
        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(product, priceModel);

    }

    @Test(expected = ServiceStateException.class)
    public void testSavePriceModel_Obsolete() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);
        setProductStatus(ServiceStatus.OBSOLETE, product.getKey());
        product.setVersion(product.getVersion() + 1);
        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(product, priceModel);

    }

    @Test(expected = ServiceStateException.class)
    public void testSavePriceModelForCustomerActive() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(techProduct, "product1",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        voProduct1 = svcProv.savePriceModel(voProduct1, priceModel);
        voProduct1 = publishToLocalMarketplaceSupplier(voProduct1, mpSupplier);

        svcProv.activateService(voProduct1);
        VOServiceDetails voDetails = svcProv.getServiceDetails(voProduct1);
        Assert.assertEquals(ServiceStatus.ACTIVE, voDetails.getStatus());
        // the first time must work because a copy is created from the inactive
        // template
        VOOrganization org = getOrganizationForOrgId(customerOrgId);
        VOPriceModel pm = createPriceModel();
        VOServiceDetails forCustomer = svcProv
                .savePriceModelForCustomer(voDetails, pm, org);
        svcProv.activateService(forCustomer);
        // the second time an exception is thrown because a copy already exists
        svcProv.savePriceModelForCustomer(forCustomer, new VOPriceModel(), org);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForCustomerWithConfigurableParamsDuplicateParam()
            throws Exception {
        VOServiceDetails service = prepareService();
        VOPriceModel priceModel = service.getPriceModel();
        List<VOPricedParameter> selectedParameters = priceModel
                .getSelectedParameters();
        selectedParameters = priceModel.getSelectedParameters();
        VOPricedParameter pricedParameter = selectedParameters.get(0);
        pricedParameter.setPricePerUser(BigDecimal.valueOf(5555L));
        selectedParameters.add(pricedParameter);
        priceModel.setSelectedParameters(selectedParameters);
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        VOOrganization localCustomer = getOrganizationForOrgId(customerOrgId);
        svcProv.savePriceModelForCustomer(service, priceModel, localCustomer);
    }

    @Test
    public void testSavePriceModelForCustomerModifyPricedParameter()
            throws Exception {
        VOServiceDetails service = prepareService();
        VOPriceModel priceModel = service.getPriceModel();
        List<VOPricedParameter> selectedParameters = priceModel
                .getSelectedParameters();
        VOPricedParameter pricedParameter = selectedParameters.get(0);
        pricedParameter.setPricePerUser(BigDecimal.valueOf(7777L));
        priceModel.setSelectedParameters(selectedParameters);
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        VOServiceDetails customerProduct = svcProv
                .savePriceModelForCustomer(service, priceModel, secondCustomer);

        service = svcProv.getServiceDetails(service);
        VOPriceModel templatePriceModel = service.getPriceModel();
        Assert.assertNotNull(templatePriceModel);
        Assert.assertEquals(1,
                templatePriceModel.getSelectedParameters().size());
        Assert.assertEquals(BigDecimal.valueOf(0), templatePriceModel
                .getSelectedParameters().get(0).getPricePerUser());

        VOPriceModel copyPriceModel = customerProduct.getPriceModel();
        Assert.assertNotNull(copyPriceModel);
        Assert.assertEquals(1, copyPriceModel.getSelectedParameters().size());
        Assert.assertEquals(BigDecimal.valueOf(7777L), copyPriceModel
                .getSelectedParameters().get(0).getPricePerUser());
    }

    @Test
    public void testSavePriceModelForCustomerUnsavedDescriptionNotChargeable()
            throws Exception {
        VOServiceDetails service = prepareService();
        VOPriceModel priceModel = service.getPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        List<VOPricedParameter> selectedParameters = priceModel
                .getSelectedParameters();
        VOPricedParameter pricedParameter = selectedParameters.get(0);
        pricedParameter.setPricePerUser(BigDecimal.valueOf(7777L));
        priceModel.setSelectedParameters(selectedParameters);
        priceModel.setCurrencyISOCode("EUR");
        VOServiceDetails customerProduct = svcProv
                .savePriceModelForCustomer(service, priceModel, secondCustomer);

        service = svcProv.getServiceDetails(service);
        VOPriceModel templatePriceModel = service.getPriceModel();
        Assert.assertNotNull(templatePriceModel);
        Assert.assertEquals("", templatePriceModel.getDescription());

        VOPriceModel copyPriceModel = customerProduct.getPriceModel();
        Assert.assertNotNull(copyPriceModel);
        Assert.assertEquals("", copyPriceModel.getDescription());

    }

    @Test
    public void testSavePriceModel_ChangeDescriptionInactive()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = new VOPriceModel();
        String description1 = "Description1";
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setDescription(description1);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(PricingPeriod.MONTH);
        product = svcProv.savePriceModel(product, priceModel);
        Assert.assertEquals(description1,
                product.getPriceModel().getDescription());
        String description2 = "description2";
        product.getPriceModel().setDescription(description2);
        product = svcProv.savePriceModel(product, product.getPriceModel());
        Assert.assertEquals(description2,
                product.getPriceModel().getDescription());
    }

    @Test
    public void testSavePriceModel_ChangeDescriptionSuspended()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = new VOPriceModel();
        String description1 = "Description1";
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setDescription(description1);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setType(PriceModelType.PRO_RATA);
        product = svcProv.savePriceModel(product, priceModel);
        Assert.assertEquals(description1,
                product.getPriceModel().getDescription());
        setProductStatus(ServiceStatus.SUSPENDED, product.getKey());
        product.setVersion(product.getVersion() + 1);
        String description2 = "description2";
        product.getPriceModel().setDescription(description2);
        product = svcProv.savePriceModel(product, product.getPriceModel());
        Assert.assertEquals(description2,
                product.getPriceModel().getDescription());
    }

    @Test
    public void testSavePriceModel_UnsavedDescriptionNotChargeable()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = new VOPriceModel();
        String description1 = "Description1";
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        priceModel.setDescription(description1);
        product = svcProv.savePriceModel(product, priceModel);
        Assert.assertEquals("", product.getPriceModel().getDescription());
    }

    @Test
    public void testSavePriceModel_DescriptionRemovedNotChargeable()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setDescription("description");
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setType(PriceModelType.PRO_RATA);
        product = svcProv.savePriceModel(product, priceModel);
        Assert.assertEquals("description",
                product.getPriceModel().getDescription());
        priceModel = product.getPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        product = svcProv.savePriceModel(product, priceModel);
        Assert.assertEquals("description",
                product.getPriceModel().getDescription());
    }

    @Test
    public void testSavePriceModelRemoveEvents() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setCurrencyISOCode("EUR");
        List<VOEventDefinition> defs = tp.getEventDefinitions();
        List<VOPricedEvent> events = new ArrayList<VOPricedEvent>();
        for (VOEventDefinition def : defs) {
            VOPricedEvent event = new VOPricedEvent(def);
            event.setEventPrice(BigDecimal.valueOf(50));
            events.add(event);
        }
        priceModel.setConsideredEvents(events);
        product = svcProv.savePriceModel(product, priceModel);
        Assert.assertEquals(events.size(),
                product.getPriceModel().getConsideredEvents().size());
        List<VOPricedEvent> emptyList = emptyList();
        product.getPriceModel().setConsideredEvents(emptyList);
        product = svcProv.savePriceModel(product, product.getPriceModel());
        Assert.assertEquals(0,
                product.getPriceModel().getConsideredEvents().size());
    }

    @Test
    public void testSavePriceModelRemoveSomeEvents() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setType(PriceModelType.PRO_RATA);
        List<VOEventDefinition> defs = tp.getEventDefinitions();
        List<VOPricedEvent> events = new ArrayList<VOPricedEvent>();
        for (VOEventDefinition def : defs) {
            VOPricedEvent event = new VOPricedEvent(def);
            event.setEventPrice(BigDecimal.valueOf(50));
            events.add(event);
        }
        priceModel.setConsideredEvents(events);
        product = svcProv.savePriceModel(product, priceModel);
        Assert.assertEquals(events.size(),
                product.getPriceModel().getConsideredEvents().size());
        events = product.getPriceModel().getConsideredEvents();
        events.remove(0);
        events.remove(0);
        product = svcProv.savePriceModel(product, product.getPriceModel());
        Assert.assertEquals(events.size(),
                product.getPriceModel().getConsideredEvents().size());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelEventsOfDifferentTP() throws Exception {
        List<VOTechnicalService> list = createTechnicalProducts(svcProv);
        VOServiceDetails product = createProduct(list.get(1), "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setCurrencyISOCode("EUR");
        List<VOEventDefinition> defs = list.get(0).getEventDefinitions();
        List<VOPricedEvent> events = new ArrayList<VOPricedEvent>();
        for (VOEventDefinition def : defs) {
            VOPricedEvent event = new VOPricedEvent(def);
            event.setEventPrice(BigDecimal.valueOf(50));
            events.add(event);
        }
        priceModel.setConsideredEvents(events);
        product = svcProv.savePriceModel(product, priceModel);
    }

    @Test
    public void testSavePriceModelForSubscriptionWithEvents() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setType(PriceModelType.PRO_RATA);
        svcProv.savePriceModel(product, priceModel);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        Assert.assertNotNull(subId);
        VOServiceDetails forSubscription = svcProv
                .getServiceForSubscription(customer, subId);
        Assert.assertNotNull(forSubscription);
        priceModel = forSubscription.getPriceModel();
        String description = "forSubscription";
        priceModel.setDescription(description);
        List<VOEventDefinition> defs = tp.getEventDefinitions();
        List<VOPricedEvent> events = new ArrayList<VOPricedEvent>();
        for (VOEventDefinition def : defs) {
            VOPricedEvent event = new VOPricedEvent(def);
            event.setEventPrice(BigDecimal.valueOf(50));
            events.add(event);
        }
        priceModel.setConsideredEvents(events);
        forSubscription = svcProv.savePriceModelForSubscription(forSubscription,
                priceModel);
        Assert.assertNotNull(forSubscription);
        Assert.assertNotNull(forSubscription.getPriceModel());
        Assert.assertEquals(description,
                forSubscription.getPriceModel().getDescription());
        Assert.assertEquals(true,
                forSubscription.getPriceModel().isChargeable());
        Assert.assertEquals(PricingPeriod.MONTH,
                forSubscription.getPriceModel().getPeriod());
        Assert.assertEquals("EUR",
                forSubscription.getPriceModel().getCurrencyISOCode());
        Assert.assertEquals(events.size(),
                forSubscription.getPriceModel().getConsideredEvents().size());
    }

    @Test
    public void testUnsubscribeAndSavePriceModel() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);

        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(product, priceModel);

        final VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        final String subId = createSubscription(customer,
                SubscriptionStatus.ACTIVE, product, "testSub", null);
        Assert.assertNotNull(subId);

        VOServiceDetails forSubscription = svcProv
                .getServiceForSubscription(customer, subId);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription s = new Subscription();
                s.setSubscriptionId(subId);
                s.setOrganizationKey(customer.getKey());

                Subscription s1 = (Subscription) mgr.find(s);
                s1.setStatus(SubscriptionStatus.DEACTIVATED);

                return null;
            }
        });

        try {
            forSubscription = svcProv
                    .savePriceModelForSubscription(forSubscription, priceModel);

        } catch (SubscriptionStateException sse) {
            assertEquals(sse.getReason(),
                    SubscriptionStateException.Reason.SUBSCRIPTION_STATE_CHANGED);
            assertEquals(sse.getMessageParams()[0],
                    SubscriptionStatus.DEACTIVATED.name());
        }
    }

    @Test
    public void testExpireSubscriptionAndSavePriceModel() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);

        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(product, priceModel);

        final VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        final String subId = createSubscription(customer,
                SubscriptionStatus.ACTIVE, product, "testSub", null);
        Assert.assertNotNull(subId);

        VOServiceDetails forSubscription = svcProv
                .getServiceForSubscription(customer, subId);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription s = new Subscription();
                s.setSubscriptionId(subId);
                s.setOrganizationKey(customer.getKey());

                Subscription s1 = (Subscription) mgr.find(s);
                s1.setStatus(SubscriptionStatus.EXPIRED);

                return null;
            }
        });

        try {
            forSubscription = svcProv
                    .savePriceModelForSubscription(forSubscription, priceModel);

        } catch (SubscriptionStateException sse) {
            assertEquals(sse.getReason(),
                    SubscriptionStateException.Reason.SUBSCRIPTION_STATE_CHANGED);

            assertEquals(sse.getMessageParams()[0],
                    SubscriptionStatus.EXPIRED.name());
        }
    }

    @Test
    public void testInvalidSubscriptionAndSavePriceModel() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);

        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(product, priceModel);

        final VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        final String subId = createSubscription(customer,
                SubscriptionStatus.ACTIVE, product, "testSub", null);
        Assert.assertNotNull(subId);

        VOServiceDetails forSubscription = svcProv
                .getServiceForSubscription(customer, subId);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription s = new Subscription();
                s.setSubscriptionId(subId);
                s.setOrganizationKey(customer.getKey());

                Subscription s1 = (Subscription) mgr.find(s);
                s1.setStatus(SubscriptionStatus.INVALID);

                return null;
            }
        });

        try {
            forSubscription = svcProv
                    .savePriceModelForSubscription(forSubscription, priceModel);

        } catch (SubscriptionStateException sse) {
            assertEquals(sse.getReason(),
                    SubscriptionStateException.Reason.SUBSCRIPTION_STATE_CHANGED);

            assertEquals(sse.getMessageParams()[0],
                    SubscriptionStatus.INVALID.name());
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForSubscriptionChangeOneTimeFee()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setType(PriceModelType.PRO_RATA);
        svcProv.savePriceModel(product, priceModel);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        Assert.assertNotNull(subId);
        VOServiceDetails forSubscription = svcProv
                .getServiceForSubscription(customer, subId);
        Assert.assertNotNull(forSubscription);
        priceModel = forSubscription.getPriceModel();
        forSubscription = svcProv.savePriceModelForSubscription(forSubscription,
                priceModel);
        priceModel = forSubscription.getPriceModel();
        priceModel.setOneTimeFee(BigDecimal.valueOf(600L));
        svcProv.savePriceModelForSubscription(forSubscription, priceModel);
    }

    @Test(expected = PriceModelException.class)
    public void testSavePriceModelForSubscriptionChangeToNotChargeable()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);

        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setType(PriceModelType.PRO_RATA);
        svcProv.savePriceModel(product, priceModel);

        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        Assert.assertNotNull(subId);
        VOServiceDetails forSubscription = svcProv
                .getServiceForSubscription(customer, subId);
        Assert.assertNotNull(forSubscription);

        priceModel = forSubscription.getPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        forSubscription = svcProv.savePriceModelForSubscription(forSubscription,
                priceModel);
    }

    @Test(expected = PriceModelException.class)
    public void testSavePriceModelForSubscriptionChangeToChargeable()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);

        final VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        svcProv.savePriceModel(product, priceModel);

        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        Assert.assertNotNull(subId);
        VOServiceDetails forSubscription = svcProv
                .getServiceForSubscription(customer, subId);
        Assert.assertNotNull(forSubscription);

        priceModel = forSubscription.getPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(PricingPeriod.MONTH);
        forSubscription = svcProv.savePriceModelForSubscription(forSubscription,
                priceModel);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelForSubscriptionProductNotOfSubscription()
            throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        Assert.assertNotNull(subId);
        svcProv.savePriceModelForSubscription(product, priceModel);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetCompatibleProductsProductNotOwned() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.getCompatibleServices(product);
    }

    @Test(expected = ServiceCompatibilityException.class)
    public void testSetCompatibleProductsNotTheSameTPs() throws Exception {
        List<VOTechnicalService> list = createTechnicalProducts(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOService product1 = createProduct(list.get(0), "1", svcProv);
        VOService product2 = createProduct(list.get(1), "2", svcProv);
        svcProv.setCompatibleServices(product1,
                Collections.singletonList(product2));
    }

    @Test(expected = SaaSSystemException.class)
    public void testSetCompatibleProductsSourceIsCopy() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product1 = createProduct(tp, "1", svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOService product2 = createProduct(tp, "2", svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);
        VOPriceModel priceModel = createPriceModel();
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        product1 = svcProv.savePriceModelForCustomer(product1, priceModel,
                customer);
        try {
            svcProv.setCompatibleServices(product1,
                    Collections.singletonList(product2));
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = SaaSSystemException.class)
    public void testSetCompatibleProductsTargetIsCopy() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOService product1 = createProduct(tp, "1", svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(tp, "2", svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);
        VOPriceModel priceModel = createPriceModel();
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        product2 = svcProv.savePriceModelForCustomer(product2, priceModel,
                customer);
        try {
            svcProv.setCompatibleServices(product1,
                    Collections.singletonList((VOService) product2));
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testGetCompatibleProducts_Active() throws Exception {
        VOService voProduct = setupCompatibleProducts(ServiceStatus.ACTIVE);
        Assert.assertNotNull(voProduct);
        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct);
        Assert.assertEquals(1, compatibleProducts.size());
        Assert.assertEquals(domObjects.get(1).getKey(),
                compatibleProducts.get(0).getKey());
    }

    @Test
    public void testGetCompatibleProducts_PriceModelCompatible()
            throws Exception {
        VOService voProduct = setupCompatibleProducts(ServiceStatus.ACTIVE);
        Assert.assertNotNull(voProduct);
        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct);
        Assert.assertEquals(1, compatibleProducts.size());
        Assert.assertEquals(domObjects.get(1).getKey(),
                compatibleProducts.get(0).getKey());
        VOPriceModel priceModel = compatibleProducts.get(0).getPriceModel();
        Assert.assertTrue(priceModel.isChargeable());
        Assert.assertEquals(EUR, priceModel.getCurrencyISOCode());
    }

    @Test(expected = PriceModelException.class)
    public void testGetCompatibleProducts_SavePriceModelSource_USD()
            throws Exception {
        final List<ProductReference> references = new ArrayList<ProductReference>();
        final List<String> currencies = Arrays.asList(EUR, USD);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addProducts(4, currencies.get(0), true);
                return null;
            }
        });
        final long referenceProductKey = domObjects.get(0).getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createProductReferences(references, referenceProductKey, 4);
                return null;
            }

        });

        VOService voProduct = getVOProduct(referenceProductKey);
        Assert.assertNotNull(voProduct);
        // check reference list
        Assert.assertEquals(3, references.size());
        for (ProductReference productReference : references) {
            Assert.assertEquals(voProduct.getKey(),
                    productReference.getSourceProduct().getKey());
        }
        // get compatible services
        List<VOService> compatibleServices = svcProv
                .getCompatibleServices(voProduct);

        Assert.assertEquals(3, compatibleServices.size());

        VOService voService = compatibleServices.get(0);
        Assert.assertEquals(domObjects.get(1).getKey(), voService.getKey());
        VOPriceModel priceModel = voService.getPriceModel();
        Assert.assertTrue(priceModel.isChargeable());
        Assert.assertEquals(EUR, priceModel.getCurrencyISOCode());
        // set state to inactive

        svcProv.deactivateService(voProduct);
        priceModel.setCurrencyISOCode(USD);

        // get compatible services
        compatibleServices = svcProv.getCompatibleServices(voProduct);

        Assert.assertEquals(3, compatibleServices.size());

        VOServiceDetails serviceDetails = svcProv.getServiceDetails(voProduct);
        svcProv.savePriceModel(serviceDetails, priceModel);
    }

    @Test(expected = PriceModelException.class)
    public void testGetCompatibleProducts_SavePriceModelTarget_USD()
            throws Exception {
        final List<ProductReference> references = new ArrayList<ProductReference>();
        final List<String> currencies = Arrays.asList(EUR, USD);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addProducts(4, currencies.get(0), true);
                return null;
            }
        });
        final long referenceProductKey = domObjects.get(0).getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createProductReferences(references, referenceProductKey, 4);
                return null;
            }

        });

        VOService voProduct = getVOProduct(referenceProductKey);
        Assert.assertNotNull(voProduct);
        // check reference list
        Assert.assertEquals(3, references.size());
        for (ProductReference productReference : references) {
            Assert.assertEquals(voProduct.getKey(),
                    productReference.getSourceProduct().getKey());
        }
        // get compatible services
        List<VOService> compatibleServices = svcProv
                .getCompatibleServices(voProduct);

        Assert.assertEquals(3, compatibleServices.size());

        VOService voService = compatibleServices.get(1);
        Assert.assertEquals(domObjects.get(2).getKey(), voService.getKey());
        VOPriceModel priceModel = voService.getPriceModel();
        Assert.assertTrue(priceModel.isChargeable());
        Assert.assertEquals(EUR, priceModel.getCurrencyISOCode());
        // set state to inactive

        svcProv.deactivateService(voProduct);
        priceModel.setCurrencyISOCode(USD);

        VOServiceDetails serviceDetails = svcProv.getServiceDetails(voProduct);
        svcProv.savePriceModel(serviceDetails, priceModel);
    }

    @Test(expected = PriceModelException.class)
    public void testGetCompatibleProducts_SavePriceModelInCompatible()
            throws Exception {
        final List<ProductReference> references = new ArrayList<ProductReference>();
        final List<String> currencies = Arrays.asList(EUR, USD);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addProducts(4, currencies.get(0), true);
                return null;
            }
        });
        final long referenceProductKey = domObjects.get(0).getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createProductReferences(references, referenceProductKey, 4);
                return null;
            }

        });
        VOService voProduct = getVOProduct(referenceProductKey);
        Assert.assertNotNull(voProduct);
        // check reference list
        Assert.assertEquals(3, references.size());
        for (ProductReference productReference : references) {
            Assert.assertEquals(voProduct.getKey(),
                    productReference.getSourceProduct().getKey());
        }
        // get compatible services
        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct);

        Assert.assertEquals(3, compatibleProducts.size());

        VOService voService = compatibleProducts.get(0);
        VOServiceDetails serviceDetails = svcProv.getServiceDetails(voService);
        Assert.assertEquals(domObjects.get(1).getKey(), voService.getKey());
        VOPriceModel priceModel = voService.getPriceModel();
        Assert.assertTrue(priceModel.isChargeable());
        Assert.assertEquals(EUR, priceModel.getCurrencyISOCode());
        // set state to inactive

        voService.setStatus(ServiceStatus.INACTIVE);
        priceModel.setCurrencyISOCode(USD);
        svcProv.savePriceModel(serviceDetails, priceModel);
    }

    @Test
    public void testGetCompatibleProducts_PriceModelNotCompatible()
            throws Exception {
        final List<ProductReference> references = new ArrayList<ProductReference>();
        final List<String> currencies = Arrays.asList(EUR, USD);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addProducts(2, currencies.get(0), true);
                addProducts(2, currencies.get(1), false);
                return null;
            }
        });
        final long referenceProductKey = domObjects.get(0).getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createProductReferences(references, referenceProductKey, 4);
                return null;
            }

        });

        VOService voProduct = getVOProduct(referenceProductKey);
        Assert.assertNotNull(voProduct);
        // check reference list
        Assert.assertEquals(3, references.size());
        for (ProductReference productReference : references) {
            Assert.assertEquals(voProduct.getKey(),
                    productReference.getSourceProduct().getKey());
        }
        // get compatible services
        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct);

        Assert.assertEquals(1, compatibleProducts.size());
        Assert.assertEquals(domObjects.get(1).getKey(),
                compatibleProducts.get(0).getKey());
        VOPriceModel priceModel = compatibleProducts.get(0).getPriceModel();
        Assert.assertTrue(priceModel.isChargeable());
        Assert.assertEquals(EUR, priceModel.getCurrencyISOCode());
    }

    @Test
    public void testGetCompatibleProducts_PriceModelEmptyCompatible1()
            throws Exception {
        final List<ProductReference> references = new ArrayList<ProductReference>();
        final List<String> currencies = Arrays.asList(EUR, USD);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addProducts(1, currencies.get(0), true);
                addProducts(2, currencies.get(1), false);
                return null;
            }
        });
        final long referenceProductKey = domObjects.get(0).getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createProductReferences(references, referenceProductKey, 3);
                return null;
            }

        });

        VOService voProduct = getVOProduct(referenceProductKey);
        Assert.assertNotNull(voProduct);
        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct);
        Assert.assertTrue(compatibleProducts.isEmpty());
    }

    @Test
    public void testGetCompatibleProducts_PriceModelEmptyCompatible()
            throws Exception {
        final List<ProductReference> references = new ArrayList<ProductReference>();
        final List<String> currencies = Arrays.asList(EUR, USD);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addProducts(1, currencies.get(0), true);
                addProducts(2, currencies.get(1), false);
                return null;
            }
        });
        final long referenceProductKey = domObjects.get(0).getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createProductReferences(references, referenceProductKey, 3);
                return null;
            }

        });

        VOService voProduct = getVOProduct(referenceProductKey);
        Assert.assertNotNull(voProduct);
        // check reference list
        Assert.assertEquals(2, references.size());
        for (ProductReference productReference : references) {
            Assert.assertEquals(voProduct.getKey(),
                    productReference.getSourceProduct().getKey());
        }
        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct);
        Assert.assertTrue(compatibleProducts.isEmpty());
    }

    @Test
    public void testGetCompatibleProducts_Suspended() throws Exception {
        VOService voProduct = setupCompatibleProducts(ServiceStatus.SUSPENDED);
        Assert.assertNotNull(voProduct);
        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct);
        Assert.assertEquals(1, compatibleProducts.size());
        Assert.assertEquals(domObjects.get(1).getKey(),
                compatibleProducts.get(0).getKey());
    }

    @Test
    public void testGetCompatibleProducts_Inactive() throws Exception {
        VOService voProduct = setupCompatibleProducts(ServiceStatus.INACTIVE);
        Assert.assertNotNull(voProduct);
        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct);
        Assert.assertEquals(1, compatibleProducts.size());
        Assert.assertEquals(domObjects.get(1).getKey(),
                compatibleProducts.get(0).getKey());
    }

    @Test
    public void testGetCompatibleProducts_Obsolete() throws Exception {
        VOService voProduct = setupCompatibleProducts(ServiceStatus.OBSOLETE);
        Assert.assertNotNull(voProduct);
        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct);
        Assert.assertEquals(0, compatibleProducts.size());
    }

    @Test
    public void testGetCompatibleProducts_Deleted() throws Exception {
        VOService voProduct = setupCompatibleProducts(ServiceStatus.DELETED);
        Assert.assertNotNull(voProduct);
        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct);
        Assert.assertEquals(0, compatibleProducts.size());
    }

    @Test
    public void testGetCompatibleProductsThroughTemplate() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        createProduct(techProduct, "template1", svcProv);
        VOService voTemplate2 = createProduct(techProduct, "template2",
                svcProv);
        VOService voProduct1 = createProduct(techProduct, "product1", svcProv);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product template1 = Products.findProduct(mgr,
                        Organizations.findOrganization(mgr, supplierOrgId),
                        "template1");
                Product template2 = Products.findProduct(mgr,
                        Organizations.findOrganization(mgr, supplierOrgId),
                        "template2");
                mgr.persist(new ProductReference(template1, template2));

                Product product1 = Products.findProduct(mgr,
                        Organizations.findOrganization(mgr, supplierOrgId),
                        "product1");
                product1.setTemplate(template1);
                return null;
            }
        });

        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct1);
        Assert.assertEquals(1, compatibleProducts.size());
        Assert.assertEquals(voTemplate2.getKey(),
                compatibleProducts.get(0).getKey());
    }

    @Test
    public void testGetCompatibleProductsThroughTemplateToCustom()
            throws Exception {

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        createProduct(techProduct, "template1", svcProv);
        VOService voTemplate2 = createProduct(techProduct, "template2",
                svcProv);
        createProduct(techProduct, "product1", svcProv);
        VOService voSubscriptionProduct = createProduct(techProduct,
                "subscriptionProduct", svcProv);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product template1 = Products.findProduct(mgr,
                        Organizations.findOrganization(mgr, supplierOrgId),
                        "template1");
                Product template2 = Products.findProduct(mgr,
                        Organizations.findOrganization(mgr, supplierOrgId),
                        "template2");
                mgr.persist(new ProductReference(template1, template2));

                Product product1 = Products.findProduct(mgr,
                        Organizations.findOrganization(mgr, supplierOrgId),
                        "product1");
                product1.setTemplate(template1);

                Product subscriptionProduct = Products.findProduct(mgr,
                        Organizations.findOrganization(mgr, supplierOrgId),
                        "subscriptionProduct");
                subscriptionProduct.setTemplate(product1);
                return null;
            }
        });

        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voSubscriptionProduct);
        Assert.assertEquals(1, compatibleProducts.size());
        Assert.assertEquals(voTemplate2.getKey(),
                compatibleProducts.get(0).getKey());
    }

    @Test
    public void testIsPartOfUpgradePath_Status() throws Exception {
        OrganizationReference ref = createOrgRef(provider.getKey());

        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        createMarketingPermission(techProduct.getKey(), ref.getKey());

        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpProvider);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpProvider);
        VOServiceDetails active = createProduct(techProduct, "active", svcProv);
        publishToLocalMarketplaceSupplier(active, mpProvider);
        VOServiceDetails inactive = createProduct(techProduct, "inactive",
                svcProv);
        publishToLocalMarketplaceSupplier(inactive, mpProvider);
        VOServiceDetails deleted = createProduct(techProduct, "deleted",
                svcProv);
        publishToLocalMarketplaceSupplier(deleted, mpProvider);
        VOServiceDetails obsolete = createProduct(techProduct, "obsolete",
                svcProv);
        publishToLocalMarketplaceSupplier(obsolete, mpProvider);
        VOServiceDetails suspended = createProduct(techProduct, "suspended",
                svcProv);
        publishToLocalMarketplaceSupplier(suspended, mpProvider);

        VOPriceModel priceModel = createPriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel);
        product2 = svcProv.savePriceModel(product2, priceModel);
        active = svcProv.savePriceModel(active, priceModel);
        inactive = svcProv.savePriceModel(inactive, priceModel);
        deleted = svcProv.savePriceModel(deleted, priceModel);
        obsolete = svcProv.savePriceModel(obsolete, priceModel);
        suspended = svcProv.savePriceModel(suspended, priceModel);

        List<VOService> origin = new ArrayList<VOService>();
        origin.add(product1);
        List<VOService> target = new ArrayList<VOService>();
        target.add(product2);

        List<VOService> notConsidered = new ArrayList<VOService>();
        notConsidered.add(deleted);
        notConsidered.add(obsolete);

        makeCompatible(origin, notConsidered);
        makeCompatible(notConsidered, target);

        assertTrue(svcProv.isPartOfUpgradePath(product1));
        assertTrue(svcProv.isPartOfUpgradePath(product2));

        setProductStatus(ServiceStatus.DELETED, deleted.getKey());
        setProductStatus(ServiceStatus.OBSOLETE, obsolete.getKey());

        assertFalse(svcProv.isPartOfUpgradePath(product1));
        assertFalse(svcProv.isPartOfUpgradePath(product2));

        List<VOService> single = new ArrayList<VOService>();
        single.add(inactive);
        product1 = svcProv.getServiceDetails(product1); // reload
        origin.clear();
        origin.add(product1);
        makeCompatible(origin, single);
        makeCompatible(single, target);

        assertTrue(svcProv.isPartOfUpgradePath(product1));
        assertTrue(svcProv.isPartOfUpgradePath(product2));

        setProductStatus(ServiceStatus.INACTIVE, inactive.getKey());

        assertTrue(svcProv.isPartOfUpgradePath(product1));
        assertTrue(svcProv.isPartOfUpgradePath(product2));

        single.clear();
        single.add(suspended);
        product1 = svcProv.getServiceDetails(product1); // reload
        origin.clear();
        origin.add(product1);
        makeCompatible(origin, single);
        makeCompatible(single, target);

        assertTrue(svcProv.isPartOfUpgradePath(product1));
        assertTrue(svcProv.isPartOfUpgradePath(product2));

        setProductStatus(ServiceStatus.SUSPENDED, suspended.getKey());

        assertTrue(svcProv.isPartOfUpgradePath(product1));
        assertTrue(svcProv.isPartOfUpgradePath(product2));
    }

    @Test
    public void testIsPartOfUpgradePath_Neg() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        OrganizationReference ref = createOrgRef(provider.getKey());
        createMarketingPermission(techProduct.getKey(), ref.getKey());
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        product1 = svcProv.savePriceModel(product1, priceModel);
        product2 = svcProv.savePriceModel(product2, priceModel);
        assertFalse(svcProv.isPartOfUpgradePath(product1));
        assertFalse(svcProv.isPartOfUpgradePath(product2));
    }

    @Test
    public void testIsPartOfUpgradePath() throws Exception {
        VOService voProduct = setupCompatibleProducts(ServiceStatus.ACTIVE);
        assertNotNull(voProduct);
        List<VOService> compatibleProducts = svcProv
                .getCompatibleServices(voProduct);
        assertEquals(1, compatibleProducts.size());
        assertTrue(svcProv.isPartOfUpgradePath(voProduct));
        assertTrue(svcProv.isPartOfUpgradePath(compatibleProducts.get(0)));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testIsPartOfUpgradePath_NotFound() throws Exception {
        VOService vo = new VOService();
        vo.setKey(7);
        svcProv.getCompatibleServices(vo);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testIsPartOfUpgradePath_NotOwned() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.isPartOfUpgradePath(product);
    }

    @Test(expected = ServiceCompatibilityException.class)
    public void setCompatibleProducts_NoMarketplaceToMarketplace()
            throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mpSupplier);

        try {
            svcProv.setCompatibleServices(product1,
                    Collections.singletonList((VOService) product2));
        } catch (ServiceCompatibilityException e) {
            assertTrue(e.getMessageKey().endsWith(
                    ServiceCompatibilityException.Reason.MARKETPLACE.name()));
            throw e;
        }
    }

    @Test(expected = ServiceCompatibilityException.class)
    public void setCompatibleProducts_MarketplaceToNoMarketplace()
            throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);

        try {
            svcProv.setCompatibleServices(product1,
                    Collections.singletonList((VOService) product2));
        } catch (ServiceCompatibilityException e) {
            assertTrue(e.getMessageKey().endsWith(
                    ServiceCompatibilityException.Reason.MARKETPLACE.name()));
            throw e;
        }
    }

    @Test(expected = ServiceCompatibilityException.class)
    public void setCompatibleProducts_MarketplaceToDifferentMarketplace()
            throws Exception {
        Marketplace mp = runTX(new Callable<Marketplace>() {

            @Override
            public Marketplace call() throws Exception {
                Organization s = mgr.getReference(Organization.class,
                        supplier.getKey());
                Marketplace mp = Marketplaces.createGlobalMarketplace(s,
                        "global", mgr);
                Marketplaces.grantPublishing(s, mp, mgr, false);
                return mp;
            }
        });
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails product1 = createProduct(techProduct, "product1",
                svcProv);
        publishToLocalMarketplaceSupplier(product1, mpSupplier);
        VOServiceDetails product2 = createProduct(techProduct, "product2",
                svcProv);
        publishToLocalMarketplaceSupplier(product2, mp);

        try {
            svcProv.setCompatibleServices(product1,
                    Collections.singletonList((VOService) product2));
        } catch (ServiceCompatibilityException e) {
            assertTrue(e.getMessageKey().endsWith(
                    ServiceCompatibilityException.Reason.MARKETPLACE.name()));
            throw e;
        }
    }

    @Test
    public void testGetCompatibleProducts_SavePriceModelCompatible()
            throws Exception {
        final List<ProductReference> references = new ArrayList<ProductReference>();
        final List<String> currencies = Arrays.asList(EUR, USD);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addProducts(4, currencies.get(0), true);
                return null;
            }
        });
        final long referenceProductKey = domObjects.get(0).getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createProductReferences(references, referenceProductKey, 4);
                return null;
            }

        });

        VOService voProduct = getVOProduct(referenceProductKey);
        Assert.assertNotNull(voProduct);
        // check reference list
        Assert.assertEquals(3, references.size());
        for (ProductReference productReference : references) {
            Assert.assertEquals(voProduct.getKey(),
                    productReference.getSourceProduct().getKey());
        }
        // get compatible services
        List<VOService> compatibleServices = svcProv
                .getCompatibleServices(voProduct);

        Assert.assertEquals(3, compatibleServices.size());

        VOService voService = compatibleServices.get(0);
        Assert.assertEquals(domObjects.get(1).getKey(), voService.getKey());
        VOPriceModel priceModel = voService.getPriceModel();
        Assert.assertTrue(priceModel.isChargeable());
        Assert.assertEquals(EUR, priceModel.getCurrencyISOCode());

        // set state to inactive
        svcProv.deactivateService(voProduct);

        // get compatible services
        compatibleServices = svcProv.getCompatibleServices(voProduct);

        Assert.assertEquals(3, compatibleServices.size());

        VOServiceDetails serviceDetails = svcProv.getServiceDetails(voProduct);
        priceModel = serviceDetails.getPriceModel();
        priceModel.setCurrencyISOCode(EUR);
        svcProv.savePriceModel(serviceDetails, priceModel);
    }

    @Test(expected = PriceModelException.class)
    public void testGetCompatibleProducts_SavePriceModelSource_USD_freeOfCharge()
            throws Exception {
        final List<ProductReference> references = new ArrayList<ProductReference>();
        final List<String> currencies = Arrays.asList(EUR, USD);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addProducts(4, currencies.get(0), true);
                return null;
            }
        });
        final long referenceProductKey = domObjects.get(0).getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createProductReferences(references, referenceProductKey, 4);
                return null;
            }

        });

        VOService voProduct = getVOProduct(referenceProductKey);
        Assert.assertNotNull(voProduct);
        // check reference list
        Assert.assertEquals(3, references.size());
        for (ProductReference productReference : references) {
            Assert.assertEquals(voProduct.getKey(),
                    productReference.getSourceProduct().getKey());
        }
        // get compatible services
        List<VOService> compatibleServices = svcProv
                .getCompatibleServices(voProduct);

        Assert.assertEquals(3, compatibleServices.size());

        VOService voService = compatibleServices.get(0);
        Assert.assertEquals(domObjects.get(1).getKey(), voService.getKey());
        VOPriceModel priceModel = voService.getPriceModel();
        Assert.assertTrue(priceModel.isChargeable());
        Assert.assertEquals(EUR, priceModel.getCurrencyISOCode());
        // set state to inactive

        svcProv.deactivateService(voProduct);
        VOServiceDetails serviceDetails = svcProv.getServiceDetails(voProduct);
        priceModel = serviceDetails.getPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        priceModel.setCurrencyISOCode(USD);

        try {
            svcProv.savePriceModel(serviceDetails, priceModel);
        } catch (Exception e) {
            Assert.fail(
                    "Save priceModel must not fail 'free of charge' is true");
        }

        serviceDetails = svcProv.getServiceDetails(voProduct);
        priceModel = serviceDetails.getPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        svcProv.savePriceModel(serviceDetails, priceModel);
    }

    @Test(expected = PriceModelException.class)
    public void testGetCompatibleProducts_SavePriceModelTarget_USD_freeOfCharge()
            throws Exception {
        final List<ProductReference> references = new ArrayList<ProductReference>();
        final List<String> currencies = Arrays.asList(EUR, USD);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addProducts(4, currencies.get(0), true);
                return null;
            }
        });
        final long referenceProductKey = domObjects.get(0).getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createProductReferences(references, referenceProductKey, 4);
                return null;
            }
        });

        VOService voProduct = getVOProduct(referenceProductKey);
        Assert.assertNotNull(voProduct);
        // check reference list
        Assert.assertEquals(3, references.size());
        for (ProductReference productReference : references) {
            Assert.assertEquals(voProduct.getKey(),
                    productReference.getSourceProduct().getKey());
        }
        // get compatible services
        List<VOService> compatibleServices = svcProv
                .getCompatibleServices(voProduct);

        Assert.assertEquals(3, compatibleServices.size());

        VOService voService = compatibleServices.get(1);
        Assert.assertEquals(domObjects.get(2).getKey(), voService.getKey());
        VOPriceModel priceModel = voService.getPriceModel();
        Assert.assertTrue(priceModel.isChargeable());
        Assert.assertEquals(EUR, priceModel.getCurrencyISOCode());
        // set state to inactive

        svcProv.deactivateService(voProduct);
        VOServiceDetails serviceDetails = svcProv.getServiceDetails(voProduct);
        priceModel = serviceDetails.getPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        priceModel.setCurrencyISOCode(USD);

        try {
            svcProv.savePriceModel(serviceDetails, priceModel);
        } catch (Exception e) {
            Assert.fail(
                    "Save priceModel must not fail 'free of charge' is true");
        }

        serviceDetails = svcProv.getServiceDetails(voProduct);
        priceModel = serviceDetails.getPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        svcProv.savePriceModel(serviceDetails, priceModel);
    }

    @Test
    public void testCreateServiceAssignCategories() throws Exception {
        final int numberOfCategories = 3;
        // create technical product
        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService tp = createTechnicalProduct(svcProv);

        // create categories for marketplace "mpSupplier"
        container.login(supplierUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name(),
                ROLE_SERVICE_MANAGER);
        final List<VOCategory> categories_en = createCategories(mpSupplier,
                numberOfCategories, "en");

        // create product and assign categories
        VOServiceDetails svc = new VOServiceDetails();
        svc.setServiceId("serviceWithCategoriesId");
        svc.setName("serviceWithCategoriesName");
        svc = svcProv.createService(tp, svc, null);

        // publish service to marketplace "mpSupplier"

        svc = publishToLocalMarketplaceSupplier(svc, mpSupplier, categories_en);

        // check DB content
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = Products.findProduct(mgr, supplier,
                        "serviceWithCategoriesId");

                assertNotNull(product);
                assertNotNull(product.getCatalogEntries());
                assertEquals(1, product.getCatalogEntries().size());
                assertEquals(mpSupplier.getMarketplaceId(),
                        product.getCatalogEntries().get(0).getMarketplace()
                                .getMarketplaceId());

                assertEquals(numberOfCategories, product.getCatalogEntries()
                        .get(0).getCategoryToCatalogEntry().size());
                List<String> categoryIdsOut = new ArrayList<String>();
                for (int i = 0; i < numberOfCategories; i++) {
                    categoryIdsOut.add(product.getCatalogEntries().get(0)
                            .getCategoryToCatalogEntry().get(i).getCategory()
                            .getCategoryId());
                }

                for (int i = 0; i < numberOfCategories; i++) {
                    assertEquals(true, categoryIdsOut
                            .contains(categories_en.get(i).getCategoryId()));
                }
                return null;
            }
        });
    }

    @Test
    public void testUpdateServiceDeassignCategories() throws Exception {
        final int numberOfCategories = 3;
        // create technical product
        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService tp = createTechnicalProduct(svcProv);

        // create categories for marketplace "mpSupplier"
        container.login(supplierUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name(),
                ROLE_SERVICE_MANAGER);
        final List<VOCategory> categories_en = createCategories(mpSupplier,
                numberOfCategories, "en");

        // create product and assign categories
        VOServiceDetails svc = new VOServiceDetails();
        svc.setServiceId("serviceWithCategoriesId");
        svc.setName("serviceWithCategoriesName");
        svc = svcProv.createService(tp, svc, null);

        // publish service to marketplace "mpSupplier"

        svc = publishToLocalMarketplaceSupplier(svc, mpSupplier, categories_en);

        // update categories_en
        categories_en.remove(2);
        categories_en.remove(0);

        // publish service to marketplace "mpSupplier"

        publishToLocalMarketplaceSupplier(svc, mpSupplier, categories_en);

        // check DB content
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = Products.findProduct(mgr, supplier,
                        "serviceWithCategoriesId");

                assertNotNull(product);
                assertNotNull(product.getCatalogEntries());
                assertEquals(1, product.getCatalogEntries().size());
                assertEquals(mpSupplier.getMarketplaceId(),
                        product.getCatalogEntries().get(0).getMarketplace()
                                .getMarketplaceId());

                assertEquals(1, product.getCatalogEntries().get(0)
                        .getCategoryToCatalogEntry().size());

                assertEquals(categories_en.get(0).getCategoryId(),
                        product.getCatalogEntries().get(0)
                                .getCategoryToCatalogEntry().get(0)
                                .getCategory().getCategoryId());
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateServiceSwitchMarketplaceWrongCategories()
            throws Exception {
        final int numberOfCategories = 3;

        // create technical product
        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService tp = createTechnicalProduct(svcProv);

        // create categories for marketplace "mpSupplier"
        container.login(supplierUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name(),
                ROLE_SERVICE_MANAGER);
        final List<VOCategory> categories_en = createCategories(mpSupplier,
                numberOfCategories, "en");

        // create product and assign categories
        VOServiceDetails svc = new VOServiceDetails();
        svc.setServiceId("serviceWithCategoriesId");
        svc.setName("serviceWithCategoriesName");
        svc = svcProv.createService(tp, svc, null);

        // publish service to marketplace "mpSupplier"
        svc = publishToLocalMarketplaceSupplier(svc, mpSupplier, categories_en);

        final Marketplace mpSupplier2 = createMarketplace(supplierOrgId,
                "mp2Id");
        try {
            // publish service to marketplace "mpSupplier2"
            svc = publishToLocalMarketplaceSupplier(svc, mpSupplier2,
                    categories_en);
        } catch (EJBException e) {
            if (e.getCausedByException() instanceof EJBTransactionRolledbackException) {
                EJBTransactionRolledbackException ex = (EJBTransactionRolledbackException) e
                        .getCausedByException();
                while (ex
                        .getCausedByException() instanceof EJBTransactionRolledbackException) {
                    ex = (EJBTransactionRolledbackException) ex
                            .getCausedByException();
                }
                throw ex.getCausedByException();
            } else {
                throw e.getCausedByException();
            }
        }
    }

    @Test
    public void testUpdateServiceSwitchMarketplaceNoCategories()
            throws Exception {
        final int numberOfCategories = 3;

        // create technical product
        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService tp = createTechnicalProduct(svcProv);

        // create categories for marketplace "mpSupplier"
        container.login(supplierUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name(),
                ROLE_SERVICE_MANAGER);
        final List<VOCategory> categories_en = createCategories(mpSupplier,
                numberOfCategories, "en");

        // create product and assign categories
        VOServiceDetails svc = new VOServiceDetails();
        svc.setServiceId("serviceWithCategoriesId");
        svc.setName("serviceWithCategoriesName");
        svc = svcProv.createService(tp, svc, null);

        // publish service to marketplace "mpSupplier"

        svc = publishToLocalMarketplaceSupplier(svc, mpSupplier, categories_en);

        final List<CategoryToCatalogEntry> ctcList = getCTCsOfProduct(
                "serviceWithCategoriesId", 3);
        final Marketplace mpSupplier2 = createMarketplace(supplierOrgId,
                "mp2Id");

        // publish service to marketplace "mpSupplier2"

        svc = publishToLocalMarketplaceSupplier(svc, mpSupplier2);

        // check DB content
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = Products.findProduct(mgr, supplier,
                        "serviceWithCategoriesId");

                assertNotNull(product);
                assertNotNull(product.getCatalogEntries());
                assertEquals(1, product.getCatalogEntries().size());
                assertEquals(mpSupplier2.getMarketplaceId(),
                        product.getCatalogEntries().get(0).getMarketplace()
                                .getMarketplaceId());

                assertEquals(0, product.getCatalogEntries().get(0)
                        .getCategoryToCatalogEntry().size());

                for (CategoryToCatalogEntry iter : ctcList) {
                    try {
                        mgr.getReference(CategoryToCatalogEntry.class,
                                iter.getKey());
                        Assert.fail("object must not exist");
                    } catch (ObjectNotFoundException ex) {
                        // expected: object does not exist
                    }
                }
                return null;
            }
        });
    }

    private List<CategoryToCatalogEntry> getCTCsOfProduct(
            final String serviceId, final int numberCTCs) throws Exception {
        // get list of CategoryToCatalogEntry for check after un-publish
        final List<CategoryToCatalogEntry> ctcList = runTX(
                new Callable<List<CategoryToCatalogEntry>>() {
                    @Override
                    public List<CategoryToCatalogEntry> call()
                            throws Exception {
                        Product product = Products.findProduct(mgr, supplier,
                                serviceId);
                        assertNotNull(product);
                        assertNotNull(product.getCatalogEntries());
                        assertEquals(numberCTCs, product.getCatalogEntries()
                                .get(0).getCategoryToCatalogEntry().size());

                        List<CategoryToCatalogEntry> ctcList = product
                                .getCatalogEntries().get(0)
                                .getCategoryToCatalogEntry();

                        return ctcList;
                    }
                });
        return ctcList;
    }

    @Test
    public void testUpdateServiceSwitchMarketplaceWithCategories()
            throws Exception {
        final int numberOfCategories = 3;
        final int numberOfCategories2 = 2;

        // create technical product
        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService tp = createTechnicalProduct(svcProv);

        // create categories for marketplace "mpSupplier"
        container.login(supplierUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name(),
                ROLE_SERVICE_MANAGER);
        final List<VOCategory> categories_en = createCategories(mpSupplier,
                numberOfCategories, "en");

        // create product and assign categories
        VOServiceDetails svc = new VOServiceDetails();
        svc.setServiceId("serviceWithCategoriesId");
        svc.setName("serviceWithCategoriesName");
        svc = svcProv.createService(tp, svc, null);

        // publish service to marketplace "mpSupplier"

        svc = publishToLocalMarketplaceSupplier(svc, mpSupplier, categories_en);

        final Marketplace mpSupplier2 = createMarketplace(supplierOrgId,
                "mp2Id");
        final List<VOCategory> categories_en2 = createCategories(mpSupplier2,
                numberOfCategories2, "en2");

        // publish service to marketplace "mpSupplier2"

        svc = publishToLocalMarketplaceSupplier(svc, mpSupplier2,
                categories_en2);

        // check DB content
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = Products.findProduct(mgr, supplier,
                        "serviceWithCategoriesId");

                assertNotNull(product);
                assertNotNull(product.getCatalogEntries());
                assertEquals(1, product.getCatalogEntries().size());
                assertEquals(mpSupplier2.getMarketplaceId(),
                        product.getCatalogEntries().get(0).getMarketplace()
                                .getMarketplaceId());

                assertEquals(numberOfCategories2, product.getCatalogEntries()
                        .get(0).getCategoryToCatalogEntry().size());
                List<String> categoryIdsOut = new ArrayList<String>();
                for (int i = 0; i < numberOfCategories2; i++) {
                    categoryIdsOut.add(product.getCatalogEntries().get(0)
                            .getCategoryToCatalogEntry().get(i).getCategory()
                            .getCategoryId());
                }

                for (int i = 0; i < numberOfCategories2; i++) {
                    assertEquals(true, categoryIdsOut
                            .contains(categories_en2.get(i).getCategoryId()));
                }
                return null;
            }
        });
    }

    private Marketplace createMarketplace(final String orgId,
            final String marketplaceId) throws Exception {
        final Marketplace marketplace = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Organization org = Organizations.findOrganization(mgr, orgId);
                Marketplace mp = Marketplaces.createGlobalMarketplace(org,
                        marketplaceId, mgr);
                Marketplaces.grantPublishing(org, mp, mgr, false);
                return mp;
            }
        });
        return marketplace;
    }

    @Test
    public void testUnpublishServiceWithCategories() throws Exception {
        final int numberOfCategories = 3;
        // create technical product
        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService tp = createTechnicalProduct(svcProv);

        // create categories for marketplace "mpSupplier"
        container.login(supplierUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name(),
                ROLE_SERVICE_MANAGER);
        final List<VOCategory> categories_en = createCategories(mpSupplier,
                numberOfCategories, "en");

        // create product and assign categories
        VOServiceDetails svc = new VOServiceDetails();
        svc.setServiceId("serviceWithCategoriesId");
        svc.setName("serviceWithCategoriesName");
        svc = svcProv.createService(tp, svc, null);

        // publish service to marketplace "mpSupplier"

        svc = publishToLocalMarketplaceSupplier(svc, mpSupplier, categories_en);

        final List<CategoryToCatalogEntry> ctcList = getCTCsOfProduct(
                "serviceWithCategoriesId", 3);

        // un-publish service to marketplace "mpSupplier"
        VOCatalogEntry voCatalogEntry = new VOCatalogEntry();
        voCatalogEntry.setMarketplace(null);
        voCatalogEntry.setCategories(null);
        voCatalogEntry.setAnonymousVisible(false);
        voCatalogEntry.setVisibleInCatalog(false);
        voCatalogEntry.setService(svc);
        svc = mpSvc.publishService(svc, Arrays.asList(voCatalogEntry));

        // check DB content
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = Products.findProduct(mgr, supplier,
                        "serviceWithCategoriesId");

                assertNotNull(product);
                assertNotNull(product.getCatalogEntries());
                assertEquals(1, product.getCatalogEntries().size());
                assertEquals(null,
                        product.getCatalogEntries().get(0).getMarketplace());

                assertEquals(0, product.getCatalogEntries().get(0)
                        .getCategoryToCatalogEntry().size());

                for (CategoryToCatalogEntry iter : ctcList) {
                    try {
                        mgr.getReference(CategoryToCatalogEntry.class,
                                iter.getKey());
                        Assert.fail("object must not exist");
                    } catch (ObjectNotFoundException ex) {
                        // expected: object does not exist
                    }
                }
                return null;
            }
        });
    }

    @Test
    public void testUnpublishServiceWithCatalogEntrySet() throws Exception {
        final int numberOfCategories = 3;
        // create technical product
        container.login(providerUserKey, ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService tp = createTechnicalProduct(svcProv);

        // create categories for marketplace "mpSupplier"
        container.login(supplierUserKey, ROLE_MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.name(),
                ROLE_SERVICE_MANAGER);
        final List<VOCategory> categories_en = createCategories(mpSupplier,
                numberOfCategories, "en");

        // create product and assign categories
        VOServiceDetails svc = new VOServiceDetails();
        svc.setServiceId("serviceWithCategoriesId");
        svc.setName("serviceWithCategoriesName");
        svc = svcProv.createService(tp, svc, null);

        // publish service to marketplace "mpSupplier"
        svc = publishToLocalMarketplaceSupplier(svc, mpSupplier, categories_en);

        final List<CategoryToCatalogEntry> ctcList = getCTCsOfProduct(
                "serviceWithCategoriesId", 3);

        // un-publish service to marketplace "mpSupplier"
        VOCatalogEntry voCatalogEntry = new VOCatalogEntry();
        voCatalogEntry.setMarketplace(null);
        voCatalogEntry.setCategories(categories_en);
        voCatalogEntry.setAnonymousVisible(false);
        voCatalogEntry.setVisibleInCatalog(false);
        voCatalogEntry.setService(svc);
        svc = mpSvc.publishService(svc, Arrays.asList(voCatalogEntry));

        // check DB content
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = Products.findProduct(mgr, supplier,
                        "serviceWithCategoriesId");

                assertNotNull(product);
                assertNotNull(product.getCatalogEntries());
                assertEquals(1, product.getCatalogEntries().size());
                assertEquals(null,
                        product.getCatalogEntries().get(0).getMarketplace());

                assertEquals(0, product.getCatalogEntries().get(0)
                        .getCategoryToCatalogEntry().size());

                for (CategoryToCatalogEntry iter : ctcList) {
                    try {
                        mgr.getReference(CategoryToCatalogEntry.class,
                                iter.getKey());
                        Assert.fail("object must not exist");
                    } catch (ObjectNotFoundException ex) {
                        // expected: object does not exist
                    }
                }
                return null;
            }
        });
    }

}
