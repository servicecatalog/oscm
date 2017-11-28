/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 11.04.2011                                                      
 *                                                                              
 *  Completion Time: 11.04.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.ProductReview;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Tag;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductTag;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCompatibleService;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.test.stubs.LocalizerServiceStub;

/**
 * @author weiser
 * 
 */
public class ProductAssemblerTest {

    private LocalizerFacade facade;
    private TechnicalProduct technicalProduct;
    private Product product;
    private Product customerProduct;
    private Product template;
    private Product partnerTemplate;
    private Organization supplier;
    private Organization supplier2;
    private PlatformUser user;
    private VOService voService;
    private VOService voCustomerService;
    @Captor
    ArgumentCaptor<List<Long>> objectKeyCaptor;

    @Before
    public void setup() throws Exception {

        facade = new LocalizerFacade(new LocalizerServiceStub() {

            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {

                return objectType.name();
            }
        }, "en");

        MockitoAnnotations.initMocks(this);

        technicalProduct = new TechnicalProduct();
        technicalProduct.setAccessType(ServiceAccessType.DIRECT);
        technicalProduct.setBaseURL("baseURL");
        technicalProduct.setKey(1234);
        technicalProduct.setLoginPath("loginPath");
        technicalProduct.setTechnicalProductId("technicalProductId");
        technicalProduct.setBillingIdentifier("some billing id");

        // Store tags on product
        List<String> values = new ArrayList<String>();
        values.add("Enterprise");
        values.add("Mega Store");
        values.add("Windows 7");
        List<TechnicalProductTag> tags = new ArrayList<TechnicalProductTag>(1);
        for (Tag tag : TagAssembler.toTags(values, "en")) {
            TechnicalProductTag productTag = new TechnicalProductTag();
            productTag.setTag(tag);
            tags.add(productTag);
        }
        technicalProduct.setTags(tags);
        supplier = new Organization();
        supplier.setOrganizationId("organizationId");
        supplier.setName("name");

        supplier2 = new Organization();
        supplier2.setOrganizationId("organizationId");

        product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setVendor(supplier);
        product.setTechnicalProduct(technicalProduct);
        product.setKey(4321);
        product.setProductId("productId");
        product.setStatus(ServiceStatus.ACTIVE);
        product.setParameterSet(createParameterSet());
        product.setPriceModel(createPriceModel(product));
        product.setAutoAssignUserEnabled(Boolean.FALSE);

        template = new Product();
        template.setType(ServiceType.TEMPLATE);
        template.setVendor(supplier);
        template.setTechnicalProduct(technicalProduct);
        template.setKey(4322);
        template.setProductId("templateId");
        template.setStatus(ServiceStatus.ACTIVE);
        template.setConfiguratorUrl("http://www.conf.de");
        template.setAutoAssignUserEnabled(Boolean.FALSE);

        customerProduct = new Product();
        customerProduct.setType(ServiceType.CUSTOMER_TEMPLATE);
        customerProduct.setVendor(supplier);
        customerProduct.setTechnicalProduct(technicalProduct);
        customerProduct.setKey(4323);
        customerProduct
                .setProductId("customerTemplateId" + "#" + UUID.randomUUID());
        customerProduct.setStatus(ServiceStatus.ACTIVE);
        customerProduct.setParameterSet(createParameterSet());
        customerProduct.setPriceModel(createPriceModel(product));
        customerProduct.setAutoAssignUserEnabled(Boolean.FALSE);

        ProductFeedback feedback = new ProductFeedback();
        feedback.setProduct(product);
        feedback.setAverageRating(new BigDecimal("2.5"));
        ProductReview review = new ProductReview();
        review.setProductFeedback(feedback);
        feedback.getProductReviews().add(review);
        product.setProductFeedback(feedback);

        user = new PlatformUser();
        user.setOrganization(supplier);
        review.setPlatformUser(user);

        voService = new VOService();
        voService.setServiceId("service identifier");
        voService.setVersion(product.getVersion());
        voService.setKey(product.getKey());
        voService.setConfiguratorUrl("https://some.configurator.url.de");
        voService.setAutoAssignUserEnabled(Boolean.FALSE);

        voCustomerService = new VOService();
        voCustomerService.setServiceId("service identifier");
        voCustomerService.setVersion(customerProduct.getVersion());
        voCustomerService.setKey(customerProduct.getKey());
        voCustomerService
                .setConfiguratorUrl("https://some.configurator.url.de");
        voCustomerService.setAutoAssignUserEnabled(Boolean.FALSE);

        Organization reseller = new Organization();
        reseller.setOrganizationId("theReseller");
        reseller.setName("TheReseller");
        addRole(reseller, OrganizationRoleType.RESELLER);
        partnerTemplate = product.copyForResale(reseller);
        partnerTemplate.setKey(8813);
    }

    private void addRole(Organization org, OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganizationRole(role);
        org.setGrantedRoles(Collections.singleton(otr));
    }

    private ParameterSet createParameterSet() {
        ParameterSet parameterSet = new ParameterSet();

        ParameterDefinition parDef1 = new ParameterDefinition();
        parDef1.setKey(4711);
        Parameter par1 = new Parameter();
        par1.setParameterSet(parameterSet);
        par1.setParameterDefinition(parDef1);
        par1.setValue("Value1");

        ParameterDefinition parDef2 = new ParameterDefinition();
        parDef2.setKey(4712);
        Parameter par2 = new Parameter();
        par2.setParameterSet(parameterSet);
        par2.setParameterDefinition(parDef2);
        par2.setValue("Value2");

        parameterSet
                .setParameters(Arrays.asList(new Parameter[] { par1, par2 }));
        return parameterSet;
    }

    private PriceModel createPriceModel(Product product) {
        PriceModel pm = new PriceModel();
        pm.setProduct(product);

        List<Parameter> parameters = product.getParameterSet().getParameters();
        List<PricedParameter> selectedParameters = new ArrayList<PricedParameter>();

        PricedParameter pricedPar1 = new PricedParameter();
        pricedPar1.setParameter(parameters.get(0));
        pricedPar1.setPricePerUser(new BigDecimal(1));
        pricedPar1.setPricePerSubscription(new BigDecimal(2));
        selectedParameters.add(pricedPar1);

        PricedParameter pricedPar2 = new PricedParameter();
        pricedPar2.setParameter(parameters.get(1));
        pricedPar2.setPricePerUser(new BigDecimal(3));
        pricedPar2.setPricePerSubscription(new BigDecimal(4));
        selectedParameters.add(pricedPar2);

        pm.setSelectedParameters(selectedParameters);

        return pm;
    }

    @Test
    public void testToVOProduct() throws Exception {
        VOService service = ProductAssembler.toVOProduct(product, facade);
        verifyBaseProperties(service, product, supplier, technicalProduct);
    }

    @Test
    public void testToVOProduct_PartnerTemplate() throws Exception {
        // when
        VOService service = ProductAssembler.toVOProduct(partnerTemplate,
                facade);

        // then
        verifyBasePropertiesForPartnerService(service, partnerTemplate, product,
                technicalProduct);
        verifyParametersForPartnerService(service, product);
    }

    @Test
    public void testToVOProduct_PartnerSubscription() throws Exception {
        // given
        Organization reseller = new Organization();
        reseller.setOrganizationId("theReseller");
        reseller.setName("TheReseller");
        addRole(reseller, OrganizationRoleType.RESELLER);

        Product partnerTemplate = product.copyForResale(reseller);
        Product partnerSubscrCopy = partnerTemplate
                .copyForSubscription(new Organization(), new Subscription());
        partnerSubscrCopy.setKey(8888);
        partnerSubscrCopy.setType(ServiceType.PARTNER_SUBSCRIPTION);

        // when
        VOService service = ProductAssembler.toVOProduct(partnerSubscrCopy,
                facade);

        // then
        verifyBasePropertiesForPartnerService(service, partnerSubscrCopy,
                product, technicalProduct);
    }

    @Test
    public void testToVOProduct_ServiceIdToDisplay() throws Exception {
        // given
        Organization reseller = new Organization();
        reseller.setOrganizationId("theReseller");
        reseller.setName("TheReseller");
        addRole(reseller, OrganizationRoleType.RESELLER);

        Product partnerTemplate = product.copyForResale(reseller);

        // when
        VOService service = ProductAssembler.toVOProduct(partnerTemplate,
                facade);

        // then
        assertEquals("productId (organizationId)",
                service.getServiceIdToDisplay());
    }

    @Test
    public void setServiceIdToDisplay() throws Exception {
        // given
        Organization reseller = new Organization();
        reseller.setOrganizationId("theReseller");
        reseller.setName("TheReseller");
        addRole(reseller, OrganizationRoleType.RESELLER);

        Product partnerTemplate = product.copyForResale(reseller);
        VOService service = new VOService();
        service.setServiceId("service");

        // when
        ProductAssembler.setServiceIdToDisplay(service, partnerTemplate);

        // then
        assertEquals("service (organizationId)",
                service.getServiceIdToDisplay());
    }

    @Test
    public void setServiceIdToDisplay_Supplier() throws Exception {
        // given
        VOService service = new VOService();
        service.setServiceId("service");

        // when
        ProductAssembler.setServiceIdToDisplay(service, product);

        // then
        assertEquals("service", service.getServiceIdToDisplay());
    }

    @Test
    public void testToVOProductDetails() throws Exception {
        VOServiceDetails service = ProductAssembler.toVOProductDetails(product,
                new ArrayList<ParameterDefinition>(), new ArrayList<Event>(),
                true, facade);
        verifyBaseProperties(service, product, supplier, technicalProduct);
        assertTrue(service.isImageDefined());
    }

    @Test
    public void testSupplierName() throws Exception {
        try {
            product.setVendor(supplier);
            VOServiceDetails service = ProductAssembler.toVOProductDetails(
                    product, new ArrayList<ParameterDefinition>(),
                    new ArrayList<Event>(), true, facade);
            assertEquals(supplier.getName(), service.getSellerName());

            product.setVendor(supplier2);

            VOServiceDetails service2 = ProductAssembler.toVOProductDetails(
                    product, new ArrayList<ParameterDefinition>(),
                    new ArrayList<Event>(), true, facade);
            assertEquals(supplier2.getOrganizationId(),
                    service2.getSellerName());

        } finally {
            product.setVendor(supplier);
        }
    }

    @Test
    public void testProductId_PartnerSubscription() throws Exception {
        try {
            product.setProductId("xxx#123");
            product.setType(ServiceType.PARTNER_SUBSCRIPTION);
            Product p1 = new Product();
            p1.setType(ServiceType.PARTNER_TEMPLATE);
            p1.setProductId("xxx#123456");
            product.setTemplate(p1);
            Product p2 = new Product();
            p2.setProductId("xxx");
            p2.setType(ServiceType.TEMPLATE);
            p1.setTemplate(p2);

            VOServiceDetails service2 = ProductAssembler.toVOProductDetails(
                    product, new ArrayList<ParameterDefinition>(),
                    new ArrayList<Event>(), true, facade);
            assertEquals(p2.getProductId(), service2.getServiceId());

        } finally {
            product.setVendor(supplier);
        }
    }

    @Test
    public void testProductId_Subscription() throws Exception {
        try {
            product.setProductId("xxx#123");
            product.setType(ServiceType.PARTNER_SUBSCRIPTION);
            Product p1 = new Product();
            p1.setType(ServiceType.TEMPLATE);
            p1.setProductId("xxx");
            product.setTemplate(p1);

            VOServiceDetails service2 = ProductAssembler.toVOProductDetails(
                    product, new ArrayList<ParameterDefinition>(),
                    new ArrayList<Event>(), true, facade);
            assertEquals(p1.getProductId(), service2.getServiceId());

        } finally {
            product.setVendor(supplier);
        }
    }

    /**
     * Convert product without ratings
     * 
     * @throws Exception
     */
    @Test
    public void testToVOProductDetails_noRatings() throws Exception {

        // given product without feedback
        product.setProductFeedback(null);

        // when converting
        VOServiceDetails service = ProductAssembler.toVOProductDetails(product,
                new ArrayList<ParameterDefinition>(), new ArrayList<Event>(),
                true, facade);

        // then VOServiceFeedback must be provided with default values
        assertEquals(new BigDecimal(0), service.getAverageRating());
    }

    private static void verifyBaseProperties(VOService service, Product product,
            Organization supplier, TechnicalProduct technicalProduct) {
        Assert.assertEquals(LocalizedObjectTypes.PRODUCT_MARKETING_DESC.name(),
                service.getDescription());
        Assert.assertEquals(
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION.name(),
                service.getShortDescription());
        Assert.assertEquals(LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME.name(), service.getCustomTabName());
        Assert.assertEquals(product.getStatus(), service.getStatus());
        Assert.assertEquals(product.isAutoAssignUserEnabled(),
                service.isAutoAssignUserEnabled());
        Assert.assertEquals(product.getKey(), service.getKey());
        Assert.assertEquals(product.getProductId(), service.getServiceId());
        Assert.assertEquals(LocalizedObjectTypes.PRODUCT_MARKETING_NAME.name(),
                service.getName());

        Assert.assertEquals(supplier.getOrganizationId(),
                service.getSellerId());
        Assert.assertEquals(supplier.getName(), service.getSellerName());

        Assert.assertEquals(technicalProduct.getTechnicalProductId(),
                service.getTechnicalId());
        Assert.assertEquals(technicalProduct.getAccessType(),
                service.getAccessType());
        Assert.assertTrue(service.getTags().size() > 0);
        Assert.assertEquals(TagAssembler
                .toStrings(product.getTechnicalProduct().getTags(), "en"),
                service.getTags());
        Assert.assertEquals(product.getProductFeedback().getAverageRating(),
                service.getAverageRating());
        Assert.assertEquals(
                product.getProductFeedback().getProductReviews().size(),
                service.getNumberOfReviews());
        Assert.assertEquals(
                product.getTechnicalProduct().getBillingIdentifier(),
                service.getBillingIdentifier());
    }

    @SuppressWarnings("boxing")
    private static void verifyBasePropertiesForPartnerService(VOService service,
            Product partnerProduct, Product template,
            TechnicalProduct technicalProduct) {
        Assert.assertEquals(
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION.name(),
                service.getShortDescription());
        Assert.assertEquals(
                LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME.name(),
                service.getCustomTabName());
        Assert.assertEquals(partnerProduct.getStatus(), service.getStatus());
        Assert.assertEquals(
                Boolean.valueOf(partnerProduct.isAutoAssignUserEnabled()),
                Boolean.valueOf(service.isAutoAssignUserEnabled()));
        Assert.assertEquals(partnerProduct.getKey(), service.getKey());
        Assert.assertEquals(template.getProductId(), service.getServiceId());
        Assert.assertEquals(LocalizedObjectTypes.PRODUCT_MARKETING_NAME.name(),
                service.getName());

        Assert.assertEquals(partnerProduct.getVendor().getOrganizationId(),
                service.getSellerId());
        Assert.assertEquals(partnerProduct.getVendor().getName(),
                service.getSellerName());

        Assert.assertEquals(technicalProduct.getTechnicalProductId(),
                service.getTechnicalId());
        Assert.assertEquals(technicalProduct.getAccessType(),
                service.getAccessType());
        Assert.assertTrue(service.getTags().size() > 0);
        Assert.assertEquals(
                TagAssembler.toStrings(
                        partnerProduct.getTechnicalProduct().getTags(), "en"),
                service.getTags());
        Assert.assertEquals(template.getProductFeedback().getAverageRating(),
                service.getAverageRating());
        Assert.assertEquals(
                template.getProductFeedback().getProductReviews().size(),
                service.getNumberOfReviews());
    }

    private void verifyParametersForPartnerService(VOService service,
            Product template) {
        List<VOParameter> voPars = service.getParameters();
        List<Parameter> pars = template.getParameterSet().getParameters();
        assertEquals("Parameter lists must have the same size", voPars.size(),
                pars.size());

        for (int i = 0; i < voPars.size(); i++) {
            VOParameter voPar = voPars.get(i);
            Parameter par = pars.get(i);
            assertEquals("Wrong parameter definition",
                    voPar.getParameterDefinition().getKey(),
                    par.getParameterDefinition().getKey());
            assertEquals("Wrong parameter value", voPar.getValue(),
                    par.getValue());
        }

        List<VOPricedParameter> voPricedPars = service.getPriceModel()
                .getSelectedParameters();
        List<PricedParameter> pricedPars = template.getPriceModel()
                .getSelectedParameters();
        assertEquals("Priced parameter lists must have the same size",
                voPricedPars.size(), pricedPars.size());

        for (int i = 0; i < voPricedPars.size(); i++) {
            VOPricedParameter voPricedPar = voPricedPars.get(i);
            PricedParameter pricedPar = pricedPars.get(i);
            assertEquals("Wrong parameter definition",
                    voPricedPar.getVoParameterDef().getKey(),
                    pricedPar.getParameter().getParameterDefinition().getKey());
            assertEquals("Wrong user price", voPricedPar.getPricePerUser(),
                    pricedPar.getPricePerUser());
            assertEquals("Wrong subscription price",
                    voPricedPar.getPricePerSubscription(),
                    pricedPar.getPricePerSubscription());
        }
    }

    @Test
    public void toVOCompatibleServices_Compatible() throws Exception {
        HashSet<Long> targetKeys = new HashSet<Long>(
                Arrays.asList(Long.valueOf(product.getKey())));
        VOCompatibleService s = ProductAssembler.toVOCompatibleService(product,
                targetKeys, facade);
        Assert.assertNotNull(s);
        Assert.assertTrue(s.isCompatible());
    }

    @Test
    public void toVOCompatibleServices_NotCompatible() throws Exception {
        HashSet<Long> targetKeys = new HashSet<Long>();
        VOCompatibleService s = ProductAssembler.toVOCompatibleService(product,
                targetKeys, facade);
        Assert.assertNotNull(s);
        Assert.assertFalse(s.isCompatible());
    }

    @Test
    public void toVOCompatibleServices_TargetsNull() throws Exception {
        VOCompatibleService s = ProductAssembler.toVOCompatibleService(product,
                null, facade);
        Assert.assertNotNull(s);
        Assert.assertFalse(s.isCompatible());
    }

    @Test
    public void toVOCompatibleServices_ProductNull() throws Exception {
        HashSet<Long> targetKeys = new HashSet<Long>();
        VOCompatibleService s = ProductAssembler.toVOCompatibleService(null,
                targetKeys, facade);
        Assert.assertNull(s);
    }

    @Test
    public void toNewTemplateProduct_externalAccess() throws Exception {
        TechnicalProduct tp = new TechnicalProduct();
        tp.setAccessType(ServiceAccessType.EXTERNAL);
        Organization org = new Organization();
        Product prod = ProductAssembler.toNewTemplateProduct(voService, tp,
                org);
        assertEquals(ServiceType.TEMPLATE, prod.getType());
        assertEquals(ServiceStatus.INACTIVE, prod.getStatus());
        assertEquals(tp, prod.getTechnicalProduct());
        assertEquals(org, prod.getVendor());
        assertEquals(voService.getServiceId(), prod.getProductId());
        assertEquals(voService.getServiceId(), prod.getProductId());
        assertEquals(voService.getConfiguratorUrl(), prod.getConfiguratorUrl());
        assertEquals(PriceModelType.FREE_OF_CHARGE,
                prod.getPriceModel().getType());
    }

    @Test
    public void toNewTemplateProduct_otherAccess() throws Exception {
        TechnicalProduct tp = new TechnicalProduct();
        Organization org = new Organization();
        Product prod = ProductAssembler.toNewTemplateProduct(voService, tp,
                org);
        assertEquals(ServiceType.TEMPLATE, prod.getType());
        assertEquals(ServiceStatus.INACTIVE, prod.getStatus());
        assertEquals(tp, prod.getTechnicalProduct());
        assertEquals(org, prod.getVendor());
        assertEquals(voService.getServiceId(), prod.getProductId());
        assertEquals(voService.getServiceId(), prod.getProductId());
        assertEquals(voService.getConfiguratorUrl(), prod.getConfiguratorUrl());
        assertNull(prod.getPriceModel());
    }

    @Test
    public void updateProduct() throws Exception {
        Product prod = ProductAssembler.updateProduct(product, voService);
        assertEquals(voService.getServiceId(), prod.getProductId());
        assertEquals(voService.getConfiguratorUrl(), prod.getConfiguratorUrl());
    }

    @Test
    public void updateCustomerTemplateProduct() throws Exception {
        Product prod = ProductAssembler.updateCustomerTemplateProduct(
                customerProduct, voCustomerService);
        assertEquals(Boolean.TRUE, Boolean.valueOf(prod.getProductId()
                .contains(voCustomerService.getServiceId())));
        assertEquals(voCustomerService.getConfiguratorUrl(),
                prod.getConfiguratorUrl());
    }

    @Test
    public void fillAllFields_confUrl_TEMPLATE() throws Exception {
        // when
        VOService service = new VOService();
        ProductAssembler.fillAllFields(template, facade, service,
                PerformanceHint.ALL_FIELDS);

        // then
        assertEquals(template.getConfiguratorUrl(),
                service.getConfiguratorUrl());
    }

    @Test
    public void fillAllFields_confUrl_PARTNER_TEMPLATE() throws Exception {
        // when
        VOService service = new VOService();
        ProductAssembler.fillAllFields(partnerTemplate, facade, service,
                PerformanceHint.ALL_FIELDS);

        // then
        assertEquals(product.getConfiguratorUrl(),
                service.getConfiguratorUrl());
    }

    @Test
    public void fillAllFields_confUrl_CUSTOMER_TEMPLATE() throws Exception {
        // given
        Product customerTemplate = product.copyForCustomer(supplier);
        VOService service = new VOService();

        // when
        ProductAssembler.fillAllFields(customerTemplate, facade, service,
                PerformanceHint.ALL_FIELDS);

        // then
        assertEquals(product.getConfiguratorUrl(),
                service.getConfiguratorUrl());
    }

    @Test
    public void fillAllFields_confUrl_TEMPLATE_SUBSCRIPTION() throws Exception {
        // given
        Product subscriptionToTemplate = product.copyForSubscription(supplier,
                new Subscription());
        VOService service = new VOService();

        // when
        ProductAssembler.fillAllFields(subscriptionToTemplate, facade, service,
                PerformanceHint.ALL_FIELDS);

        // then
        assertEquals(product.getConfiguratorUrl(),
                service.getConfiguratorUrl());
    }

    @Test
    public void fillAllFields_confUrl_CUSTOMER_SUBSCRIPTION() throws Exception {
        // given
        Product customerTemplate = product.copyForCustomer(supplier);
        Product subscriptionToCustomerTemplate = customerTemplate
                .copyForSubscription(supplier, new Subscription());
        VOService service = new VOService();

        // when
        ProductAssembler.fillAllFields(subscriptionToCustomerTemplate, facade,
                service, PerformanceHint.ALL_FIELDS);

        // then
        assertEquals(product.getConfiguratorUrl(),
                service.getConfiguratorUrl());
    }

    @Test
    public void fillAllFields_confUrl_PARTNER_SUBSCRIPTION() throws Exception {
        // given
        Product subscriptionToCustomerTemplate = partnerTemplate
                .copyForSubscription(supplier, new Subscription());
        VOService service = new VOService();

        // when
        ProductAssembler.fillAllFields(subscriptionToCustomerTemplate, facade,
                service, PerformanceHint.ALL_FIELDS);

        // then
        assertEquals(product.getConfiguratorUrl(),
                service.getConfiguratorUrl());
    }

    @Test
    public void copyAttributes_TEMPLATE() throws Exception {
        // given
        Product prod = new Product();
        prod.setType(ServiceType.TEMPLATE);
        prod.setConfiguratorUrl("some value");

        // when
        ProductAssembler.copyAttributes(prod, voService);

        // then
        verifyCopiedAttributes(prod);
        assertEquals(prod.getConfiguratorUrl(), voService.getConfiguratorUrl());
    }

    @Test
    public void copyAttributes_CUSTOMER_TEMPLATE() throws Exception {
        // given
        Product prod = new Product();
        prod.setType(ServiceType.CUSTOMER_TEMPLATE);
        prod.setConfiguratorUrl("some value");

        // when
        ProductAssembler.copyAttributes(prod, voService);

        // then
        verifyCopiedAttributes(prod);
        assertNull(prod.getConfiguratorUrl());
    }

    @Test
    public void copyAttributes_PARTNER_TEMPLATE() throws Exception {
        // given
        Product prod = new Product();
        prod.setType(ServiceType.PARTNER_TEMPLATE);
        prod.setConfiguratorUrl("some value");
        prod.setTemplate(template);

        // when
        ProductAssembler.copyAttributes(prod, voService);

        // then
        verifyCopiedAttributes(prod);
        assertNull(prod.getConfiguratorUrl());
    }

    @Test
    public void copyAttributes_SUBSCRIPTION() throws Exception {
        // given
        Product prod = new Product();
        prod.setType(ServiceType.SUBSCRIPTION);
        prod.setConfiguratorUrl("some value");

        // when
        ProductAssembler.copyAttributes(prod, voService);

        // then
        verifyCopiedAttributes(prod);
        assertNull(prod.getConfiguratorUrl());
    }

    @Test
    public void copyAttributes_CUSTOMER_SUBSCRIPTION() throws Exception {
        // given
        Product prod = new Product();
        prod.setType(ServiceType.CUSTOMER_SUBSCRIPTION);
        prod.setConfiguratorUrl("some value");

        // when
        ProductAssembler.copyAttributes(prod, voService);

        // then
        verifyCopiedAttributes(prod);
        assertNull(prod.getConfiguratorUrl());
    }

    @Test
    public void copyAttributes_PARTNER_SUBSCRIPTION() throws Exception {
        // given
        Product prod = new Product();
        prod.setType(ServiceType.PARTNER_SUBSCRIPTION);
        prod.setConfiguratorUrl("some value");

        // when
        ProductAssembler.copyAttributes(prod, voService);

        // then
        verifyCopiedAttributes(prod);
        assertNull(prod.getConfiguratorUrl());
    }

    @Test(expected = ValidationException.class)
    public void copyAttributes_TEMPLATE_invalidUrl() throws Exception {
        // given
        Product prod = new Product();
        prod.setType(ServiceType.TEMPLATE);
        prod.setConfiguratorUrl("some value");
        voService.setConfiguratorUrl("configUrl");

        // when
        ProductAssembler.copyAttributes(prod, voService);
    }

    @SuppressWarnings("boxing")
    private void verifyCopiedAttributes(Product prod) {
        assertEquals(prod.getProductId(), voService.getServiceId());
        assertEquals(Boolean.valueOf(prod.isAutoAssignUserEnabled()),
                Boolean.valueOf(voService.isAutoAssignUserEnabled()));
    }

    @Test
    public void prefetchData_withoutTemplates_bug12479() {
        // given
        List<Product> products = new ArrayList<Product>();
        products.add(product);
        products.add(customerProduct);
        LocalizerFacade facadeMock = spy(facade);

        // when
        ProductAssembler.prefetchData(products, facadeMock,
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);

        // then
        verify(facadeMock, times(1)).prefetch(objectKeyCaptor.capture(),
                Matchers.anyListOf(LocalizedObjectTypes.class));
        List<Long> objectkeys = objectKeyCaptor.getValue();
        assertEquals(2, objectkeys.size());
        assertEquals(Long.valueOf(product.getKey()), objectkeys.get(0));
        assertEquals(Long.valueOf(customerProduct.getKey()), objectkeys.get(1));
    }

    @Test
    public void prefetchData_withTemplates_bug12479() {
        // given
        List<Product> products = new ArrayList<Product>();
        products.add(product);
        Product anotherProduct = new Product();
        anotherProduct.setType(ServiceType.PARTNER_TEMPLATE);
        anotherProduct.setConfiguratorUrl("some value");
        anotherProduct.setTemplate(template);
        products.add(anotherProduct);
        products.add(customerProduct);
        LocalizerFacade facadeMock = spy(facade);

        // when
        ProductAssembler.prefetchData(products, facadeMock,
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);

        // then
        verify(facadeMock, times(1)).prefetch(objectKeyCaptor.capture(),
                Matchers.anyListOf(LocalizedObjectTypes.class));
        List<Long> objectkeys = objectKeyCaptor.getValue();
        assertEquals(3, objectkeys.size());
        assertEquals(Long.valueOf(product.getKey()), objectkeys.get(0));
        assertEquals(Long.valueOf(template.getKey()), objectkeys.get(1));
        assertEquals(Long.valueOf(customerProduct.getKey()), objectkeys.get(2));
    }
}
