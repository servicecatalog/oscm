/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 30.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.*;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.*;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.Mockito.mock;

/**
 * Tests for the service provisiong that test the handling of role related
 * prices.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ServiceProvisioningRolePricingIT extends EJBTestBase {

    private DataService dm;
    private LocalizerServiceLocal localizer;
    private ServiceProvisioningService svcProv;

    private Organization supplierAndProvider;
    private Organization customer;
    private TechnicalProduct techProd;
    private Product product;
    private Subscription sub;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new SessionServiceStub());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new ApplicationServiceStub());
        container.addBean(mock(LocalizerServiceLocal.class));
        localizer = container.get(LocalizerServiceLocal.class);
        container.addBean(new ImageResourceServiceStub() {
            ImageResource saved;

            @Override
            public ImageResource read(long objectKey, ImageType imageType) {
                return saved;
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
        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new TagServiceBean());
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());

        dm = container.get(DataService.class);
        svcProv = container.get(ServiceProvisioningService.class);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                SupportedCountries.createSomeSupportedCountries(dm);
                return null;
            }
        });
        PlatformUser user = init();
        container.login(String.valueOf(user.getKey()), ROLE_SERVICE_MANAGER);
    }

    /**
     * Creates the required test scenario and assigns created objects to the
     * appropriate global fields.
     * 
     * @throws Exception
     */
    private PlatformUser init() throws Exception {
        return runTX(new Callable<PlatformUser>() {

            private ParameterDefinition paramDef;
            private ParameterDefinition optionParamDef;

            @Override
            public PlatformUser call() throws Exception {
                // create roles and currency
                createOrganizationRoles(dm);
                createSupportedCurrencies(dm);
                createPaymentTypes(dm);

                // create organization
                supplierAndProvider = Organizations.createOrganization(dm,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(dm,
                        supplierAndProvider, true, "admin");

                customer = Organizations.createCustomer(dm,
                        supplierAndProvider);
                // create technical product with roles};
                techProd = TechnicalProducts.createTechnicalProduct(dm,
                        supplierAndProvider, "prodId", false,
                        ServiceAccessType.LOGIN);

                RoleDefinition rd = new RoleDefinition();
                rd.setTechnicalProduct(techProd);
                rd.setRoleId("role1");
                RoleDefinition rd2 = new RoleDefinition();
                rd2.setTechnicalProduct(techProd);
                rd2.setRoleId("role2");
                dm.persist(rd);
                dm.persist(rd2);
                ArrayList<RoleDefinition> roles = new ArrayList<>();
                techProd.setRoleDefinitions(roles);

                paramDef = TechnicalProducts.addParameterDefinition(
                        ParameterValueType.INTEGER, "intParam",
                        ParameterType.SERVICE_PARAMETER, techProd, dm, null,
                        null, true);

                optionParamDef = TechnicalProducts.addParameterDefinition(
                        ParameterValueType.ENUMERATION, "enumParam",
                        ParameterType.SERVICE_PARAMETER, techProd, dm, null,
                        null, true);

                ParameterOption po = new ParameterOption();
                po.setOptionId("optionId");
                po.setParameterDefinition(optionParamDef);
                optionParamDef.setOptionList(Collections.singletonList(po));
                dm.persist(po);

                // create a product for the technical product
                product = Products.createProduct(supplierAndProvider, techProd,
                        true, "product", null, dm);
                product.setStatus(ServiceStatus.INACTIVE);

                Parameter param = new Parameter();
                param.setValue("123");
                param.setParameterSet(product.getParameterSet());
                param.setParameterDefinition(paramDef);
                param.setConfigurable(true);
                dm.persist(param);

                Parameter param2 = new Parameter();
                param2.setParameterSet(product.getParameterSet());
                param2.setParameterDefinition(optionParamDef);
                param2.setConfigurable(true);
                param2.setValue("optionId");
                dm.persist(param2);

                // set payment information for the customer
                PaymentType paymentType = findPaymentType(INVOICE, dm);
                PaymentInfo pi = PaymentInfos.createPaymentInfo(customer, dm,
                        paymentType);
                // create sub
                sub = Subscriptions.createSubscription(dm,
                        customer.getOrganizationId(), "product", "subId",
                        supplierAndProvider);
                sub.setPaymentInfo(pi);
                BillingContact bc = PaymentInfos.createBillingContact(dm,
                        customer);
                sub.setBillingContact(bc);
                return user;
            }
        });
    }

    /**
     * Asserts that the created product and price model are correct.
     * 
     * @param roleDefinitions
     *            The role definition a price has been stored for.
     * @param savedProduct
     *            The product to validate.
     * @param expectedRoleDefCount
     *            The number of role definitions the saved product should have.
     * @param isCustomerSpecific
     *            Indicates whether the created product should be validated to
     *            be customer specific or not.
     * @param isSubscriptionSpecific
     *            Indicates whether the created product should be validated to
     *            be subscription specific or not.
     * @param expectedBasePrice
     *            The base price to check for.
     * @param isUpdate
     *            Indicates whether the test performed an update. If so, there
     *            will be more than only one history entry...
     * @param createsNewEntry
     *            Indicates whether a new entry has been created so that this
     *            must be checked accordingly.
     * @param removedPricedProductRole
     *            A priced product role that was removed. History has to be
     *            checked accordingly.
     */
    private void assertCreatedProductSettings(
            List<VORoleDefinition> roleDefinitions,
            final VOServiceDetails savedProduct, final int expectedRoleDefCount,
            final boolean isCustomerSpecific,
            final boolean isSubscriptionSpecific,
            final BigDecimal expectedBasePrice, final boolean isUpdate,
            final VOPricedRole removedPricedProductRole) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (int i = 0; i < expectedRoleDefCount; i++) {
                    Product createdProduct = dm.getReference(Product.class,
                            savedProduct.getKey());
                    List<PricedProductRole> roleSpecificUserPrices = createdProduct
                            .getPriceModel().getRoleSpecificUserPrices();
                    Assert.assertEquals(expectedRoleDefCount,
                            roleSpecificUserPrices.size());
                    PricedProductRole pricedProductRole = roleSpecificUserPrices
                            .get(i);
                    Assert.assertEquals(createdProduct.getPriceModel().getKey(),
                            pricedProductRole.getPriceModel().getKey());
                    Assert.assertNull(pricedProductRole.getPricedParameter());
                    Assert.assertNull(pricedProductRole.getPricedOption());
                    Assert.assertEquals(
                            expectedBasePrice.multiply(new BigDecimal(i + 1)),
                            pricedProductRole.getPricePerUser());
                    RoleDefinition role = pricedProductRole.getRoleDefinition();

                    List<DomainHistoryObject<?>> findHistory = dm
                            .findHistory(pricedProductRole);
                    {
                        Assert.assertEquals(createdProduct.getTechnicalProduct()
                                .getRoleDefinitions().get(i), role);

                        PricedProductRoleHistory history = (PricedProductRoleHistory) findHistory
                                .get(0);
                        Assert.assertEquals(ModificationType.ADD,
                                history.getModtype());
                        Assert.assertNull(history.getPricedOptionObjKey());
                        Assert.assertNull(history.getPricedParameterObjKey());
                        Assert.assertEquals(
                                createdProduct.getPriceModel().getKey(),
                                history.getPriceModelObjKey().longValue());
                        Assert.assertEquals(
                                createdProduct.getTechnicalProduct()
                                        .getRoleDefinitions().get(i).getKey(),
                                history.getRoleDefinitionObjKey());
                    }

                    if (!isCustomerSpecific && !isSubscriptionSpecific) {
                        Assert.assertNull(createdProduct.getTemplate());
                        Assert.assertNull(
                                createdProduct.getOwningSubscription());
                    }

                    if (isCustomerSpecific) {
                        Assert.assertNotNull(createdProduct.getTemplate());
                        Assert.assertEquals(customer.getKey(),
                                createdProduct.getTargetCustomer().getKey());
                    }

                    if (isSubscriptionSpecific) {
                        Assert.assertNotNull(createdProduct.getTemplate());
                        Assert.assertEquals(sub.getKey(), createdProduct
                                .getOwningSubscription().getKey());
                    }
                }
                if (removedPricedProductRole != null) {
                    PricedProductRole ppr = new PricedProductRole();
                    ppr.setKey(removedPricedProductRole.getKey());
                    List<PricedProductRoleHistory> findHistory = ParameterizedTypes
                            .list(dm.findHistory(ppr),
                                    PricedProductRoleHistory.class);
                    Assert.assertEquals(2, findHistory.size());
                    Assert.assertEquals(ModificationType.DELETE,
                            findHistory.get(1).getModtype());
                }

                return null;
            }
        });

        List<VOPricedRole> roleSpecificUserPrices = savedProduct.getPriceModel()
                .getRoleSpecificUserPrices();
        Assert.assertEquals(expectedRoleDefCount,
                roleSpecificUserPrices.size());
        Assert.assertEquals(expectedBasePrice,
                roleSpecificUserPrices.get(0).getPricePerUser());
        if (isUpdate) {
            Assert.assertEquals(roleDefinitions.get(0).getKey(),
                    roleSpecificUserPrices.get(0).getRole().getKey());
        } else {
            Assert.assertEquals(roleDefinitions.get(0).getKey(),
                    roleSpecificUserPrices.get(0).getRole().getKey());
        }

        if (expectedRoleDefCount == 2) {
            VOPricedRole voPricedProductRole = roleSpecificUserPrices.get(1);
            Assert.assertEquals(expectedBasePrice.multiply(new BigDecimal(2)),
                    voPricedProductRole.getPricePerUser());
            Assert.assertEquals(roleDefinitions.get(1).getKey(),
                    roleSpecificUserPrices.get(1).getRole().getKey());
        }
    }

    /**
     * Initializes a price model object.
     * 
     * @param ppr
     *            The priced product role to be set.
     * @return The price model that was initialized.
     */
    private VOPriceModel createPriceModelDefinition(List<VOPricedRole> ppr,
            VOServiceDetails productDetails) {
        VOPriceModel pm = productDetails.getPriceModel();
        pm.setType(PriceModelType.PRO_RATA);
        pm.setConsideredEvents(new ArrayList<VOPricedEvent>());
        pm.setCurrencyISOCode("EUR");
        pm.setDescription("desc");
        pm.setOneTimeFee(BigDecimal.ZERO);
        pm.setPeriod(PricingPeriod.MONTH);
        pm.setPricePerPeriod(BigDecimal.valueOf(10L));
        pm.setPricePerUserAssignment(BigDecimal.valueOf(22L));
        pm.setSelectedParameters(new ArrayList<VOPricedParameter>());
        pm.setRoleSpecificUserPrices(ppr);

        return pm;
    }

    /**
     * Returns the product for a vo product.
     * 
     * @param prod
     *            The product as value object.
     * @return The product as domain object.
     * @throws Exception
     */
    private Product getProduct(final VOService prod) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product doProduct = dm.find(Product.class, prod.getKey());
                List<PricedProductRole> roleSpecificUserPrices = doProduct.getPriceModel().getRoleSpecificUserPrices();
                List<PricedProductRole> ppr = new ArrayList<>();
                for (PricedProductRole roleSpecificUserPrice : roleSpecificUserPrices) {
                    roleSpecificUserPrice.setRoleDefinition(unproxyEntity(roleSpecificUserPrice.getRoleDefinition()));
                    ppr.add(unproxyEntity(roleSpecificUserPrice));
                }
                doProduct.getPriceModel().setRoleSpecificUserPrices(ppr);
                return doProduct;
            }
        });
    }

    @Test(expected = OperationNotPermittedException.class)
    public void savePriceModelInvalidRolePriceData() throws Exception {
        List<VOService> products = svcProv.getSuppliedServices();
        VOService product = products.get(0);
        VOServiceDetails productDetails = svcProv.getServiceDetails(product);
        VORoleDefinition roleDefinition = new VORoleDefinition();
        roleDefinition.setKey(Long.MAX_VALUE);

        VOPricedRole ppr = new VOPricedRole();
        ppr.setRole(roleDefinition);
        ppr.setPricePerUser(BigDecimal.valueOf(33L));

        VOPriceModel pm = createPriceModelDefinition(
                Collections.singletonList(ppr), productDetails);

        svcProv.savePriceModel(productDetails, pm);
    }

    @Test(expected = ValidationException.class)
    public void savePriceModelInvalidRolePriceDataNegativePrice()
            throws Exception {
        List<VOService> products = svcProv.getSuppliedServices();
        VOService product = products.get(0);
        VOServiceDetails productDetails = svcProv.getServiceDetails(product);
        List<VORoleDefinition> roleDefinitions = productDetails
                .getTechnicalService().getRoleDefinitions();
        Assert.assertEquals(2, roleDefinitions.size());
        VORoleDefinition roleDefinition = roleDefinitions.get(0);

        VOPricedRole ppr = new VOPricedRole();
        ppr.setRole(roleDefinition);
        ppr.setPricePerUser(BigDecimal.valueOf(-33L));

        VOPriceModel pm = createPriceModelDefinition(
                Collections.singletonList(ppr), productDetails);

        svcProv.savePriceModel(productDetails, pm);
    }

    @Test
    public void testSavePriceModelOneRolePrice() throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        List<VORoleDefinition> roleDefinitions = productDetails
                .getTechnicalService().getRoleDefinitions();
        Assert.assertEquals(2, roleDefinitions.size());
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 1);
        VOPricedRole ppr = new VOPricedRole();
        ppr.setRole(roleDefs.get(0));
        ppr.setPricePerUser(BigDecimal.valueOf(33L));

        VOPriceModel pm = createPriceModelDefinition(
                Collections.singletonList(ppr), productDetails);

        final VOServiceDetails savedProduct = svcProv
                .savePriceModel(productDetails, pm);

        // and also assert domain object integrity and history object existence
        assertCreatedProductSettings(roleDefs, savedProduct, 1, false, false,
                new BigDecimal(33), false, null);
    }

    @Test
    public void testSavePriceModelOneRolePriceAndUpdateToUpdatePriceValue()
            throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        List<VORoleDefinition> roleDefinitions = productDetails
                .getTechnicalService().getRoleDefinitions();
        Assert.assertEquals(2, roleDefinitions.size());
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 1);
        VOPricedRole ppr = new VOPricedRole();
        ppr.setRole(roleDefs.get(0));
        ppr.setPricePerUser(BigDecimal.valueOf(33L));

        VOPriceModel pm = createPriceModelDefinition(
                Collections.singletonList(ppr), productDetails);

        VOServiceDetails savedProduct = svcProv.savePriceModel(productDetails,
                pm);

        VOPricedRole voPricedProductRole = savedProduct.getPriceModel()
                .getRoleSpecificUserPrices().get(0);
        voPricedProductRole.setPricePerUser(BigDecimal.valueOf(123L));

        savedProduct = svcProv.savePriceModel(
                svcProv.getServiceDetails(savedProduct),
                savedProduct.getPriceModel());

        // and also assert domain object integrity and history object existence
        assertCreatedProductSettings(roleDefs, savedProduct, 1, false, false,
                new BigDecimal(123), true, null);
    }

    // see bug 5586
    @Test
    public void testSavePriceModelOneRolePriceAndUpdateToUpdatePriceValueCustomerSpecific()
            throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        List<VORoleDefinition> roleDefinitions = productDetails
                .getTechnicalService().getRoleDefinitions();
        Assert.assertEquals(2, roleDefinitions.size());
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 1);
        VOPricedRole ppr = new VOPricedRole();
        ppr.setRole(productDetails.getTechnicalService().getRoleDefinitions()
                .get(1));
        ppr.setPricePerUser(BigDecimal.valueOf(33L));

        VOPriceModel pm = createPriceModelDefinition(
                Collections.singletonList(ppr), productDetails);
        VOServiceDetails savedProduct = svcProv.savePriceModel(productDetails,
                pm);

        VOPricedRole voPricedProductRole = savedProduct.getPriceModel()
                .getRoleSpecificUserPrices().get(0);
        voPricedProductRole.setPricePerUser(BigDecimal.valueOf(123L));

        List<VOPricedRole> currentPricedRoles = savedProduct.getPriceModel()
                .getRoleSpecificUserPrices();
        VORoleDefinition newRole = productDetails.getTechnicalService()
                .getRoleDefinitions().get(0);
        VOPricedRole voPricedProductRole2 = new VOPricedRole();
        voPricedProductRole2.setRole(newRole);
        voPricedProductRole2.setPricePerUser(BigDecimal.valueOf(246L));
        currentPricedRoles.add(voPricedProductRole2);
        roleDefs.add(0, newRole);

        VOOrganization organization = runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                return OrganizationAssembler.toVOOrganization(customer, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });
        savedProduct = svcProv.savePriceModelForCustomer(
                svcProv.getServiceDetails(savedProduct),
                savedProduct.getPriceModel(), organization);

        // and also assert domain object integrity and history object existence
        // for product
        Assert.assertEquals(2,
                savedProduct.getTechnicalService().getRoleDefinitions().size());
        Product storedProduct = getProduct(savedProduct);
        Assert.assertNotNull(storedProduct.getTemplate());

        // priced roles
        List<PricedProductRole> roleSpecificUserPrices = storedProduct
                .getPriceModel().getRoleSpecificUserPrices();
        for (PricedProductRole pricedProductRole : roleSpecificUserPrices) {
            if (pricedProductRole.getRoleDefinition().getKey() == savedProduct
                    .getTechnicalService().getRoleDefinitions().get(0)
                    .getKey()) {
                Assert.assertEquals(0, pricedProductRole.getVersion());
                Assert.assertEquals(new BigDecimal(246L),
                        pricedProductRole.getPricePerUser());
            } else {
                Assert.assertEquals(1, pricedProductRole.getVersion());
                Assert.assertEquals(new BigDecimal(123L),
                        pricedProductRole.getPricePerUser());
            }
        }
    }

    @Test
    public void testSavePriceModelOneRolePriceRemoveAllCustomerSpecific()
            throws Exception {
        final VOServiceDetails productDetails = getProductDetails();
        List<VORoleDefinition> roleDefinitions = productDetails
                .getTechnicalService().getRoleDefinitions();
        Assert.assertEquals(2, roleDefinitions.size());
        VOPricedRole ppr = new VOPricedRole();
        ppr.setRole(productDetails.getTechnicalService().getRoleDefinitions()
                .get(1));
        ppr.setPricePerUser(BigDecimal.valueOf(33L));

        final VOPriceModel pm = createPriceModelDefinition(
                Collections.singletonList(ppr), productDetails);

        VOServiceDetails savedProduct = runTX(new Callable<VOServiceDetails>() {
            @Override
            public VOServiceDetails call() throws Exception {
                return svcProv.savePriceModel(productDetails, pm);
            }
        });

        savedProduct.getPriceModel().getRoleSpecificUserPrices().clear();

        final VOOrganization orga = runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                return OrganizationAssembler.toVOOrganization(customer, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });

        savedProduct = svcProv.savePriceModelForCustomer(
                svcProv.getServiceDetails(savedProduct),
                savedProduct.getPriceModel(), orga);

        // and also assert domain object integrity and history object existence
        // for product
        final VOPriceModel savedPriceModel = savedProduct.getPriceModel();
        Assert.assertEquals(0,
                savedPriceModel.getRoleSpecificUserPrices().size());
        Product storedProduct = getProduct(savedProduct);
        Assert.assertNotNull(storedProduct.getTemplate());

        // priced roles - assert that history count is 2 for the removed object
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = dm.createQuery(
                        "SELECT ph FROM PricedProductRoleHistory ph WHERE ph.priceModelObjKey = "
                                + savedPriceModel.getKey()
                                + " ORDER BY ph.key");
                List<PricedProductRoleHistory> list = ParameterizedTypes.list(
                        query.getResultList(), PricedProductRoleHistory.class);
                Assert.assertEquals(2, list.size());
                Assert.assertEquals(ModificationType.DELETE,
                        list.get(1).getModtype());
                return null;
            }
        });
    }

    @Test
    public void testSavePriceModelOneRolePriceAndUpdateToCreateValue()
            throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        List<VORoleDefinition> roleDefinitions = productDetails
                .getTechnicalService().getRoleDefinitions();
        Assert.assertEquals(2, roleDefinitions.size());
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 1);
        VOPricedRole ppr = new VOPricedRole();
        ppr.setRole(roleDefs.get(0));
        ppr.setPricePerUser(BigDecimal.valueOf(33L));

        VOPriceModel pm = createPriceModelDefinition(
                Collections.singletonList(ppr), productDetails);

        VOServiceDetails savedProduct = svcProv.savePriceModel(productDetails,
                pm);

        VOPricedRole newRolePrice = new VOPricedRole();
        newRolePrice.setPricePerUser(BigDecimal.valueOf(66L));
        newRolePrice.setRole(productDetails.getTechnicalService()
                .getRoleDefinitions().get(1));

        savedProduct.getPriceModel().getRoleSpecificUserPrices()
                .add(newRolePrice);

        savedProduct = svcProv.savePriceModel(
                svcProv.getServiceDetails(savedProduct),
                savedProduct.getPriceModel());

        roleDefs.add(newRolePrice.getRole());

        // and also assert domain object integrity and history object existence
        assertCreatedProductSettings(roleDefs, savedProduct, 2, false, false,
                new BigDecimal(33), false, null);
    }

    @Test
    public void testSavePriceModelTwoRolePrices() throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        List<VORoleDefinition> roleDefinitions = productDetails
                .getTechnicalService().getRoleDefinitions();
        Assert.assertEquals(2, roleDefinitions.size());
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        ArrayList<VOPricedRole> pricedRoles = definePricedProductRoles(
                roleDefs);

        VOPriceModel pm = createPriceModelDefinition(pricedRoles,
                productDetails);

        final VOServiceDetails savedProduct = svcProv
                .savePriceModel(productDetails, pm);

        // and also assert domain object integrity and history object existence
        assertCreatedProductSettings(roleDefs, savedProduct, 2, false, false,
                new BigDecimal(33), false, null);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelTwoRolePricesForOneRole() throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        List<VORoleDefinition> roleDefinitions = productDetails
                .getTechnicalService().getRoleDefinitions();
        Assert.assertEquals(2, roleDefinitions.size());
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        ArrayList<VOPricedRole> pricedRoles = definePricedProductRoles(
                roleDefs);
        pricedRoles.get(1).setRole(pricedRoles.get(0).getRole());

        VOPriceModel pm = createPriceModelDefinition(pricedRoles,
                productDetails);

        svcProv.savePriceModel(productDetails, pm);
    }

    @Test
    public void testSavePriceModelOneRolePriceAndUpdateToRemoveValue()
            throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        List<VORoleDefinition> roleDefinitions = productDetails
                .getTechnicalService().getRoleDefinitions();
        Assert.assertEquals(2, roleDefinitions.size());
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        ArrayList<VOPricedRole> pricedRoles = definePricedProductRoles(
                roleDefs);

        VOPriceModel pm = createPriceModelDefinition(pricedRoles,
                productDetails);

        VOServiceDetails savedProduct = svcProv.savePriceModel(productDetails,
                pm);

        VOPricedRole removedPricedRole = savedProduct.getPriceModel()
                .getRoleSpecificUserPrices().remove(1);
        productDetails = svcProv.getServiceDetails(savedProduct);

        savedProduct = svcProv.savePriceModel(productDetails,
                savedProduct.getPriceModel());
        assertCreatedProductSettings(roleDefs, savedProduct, 1, false, false,
                new BigDecimal(33), true, removedPricedRole);

    }

    @Test
    public void testSavePriceModelForCustomer() throws Exception {
        final VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = runTX(
                new Callable<ArrayList<VORoleDefinition>>() {
                    @Override
                    public ArrayList<VORoleDefinition> call() throws Exception {
                        return defineRoleDefinitionsForPM(productDetails, 2);
                    }
                });

        ArrayList<VOPricedRole> pricedRoles = definePricedProductRoles(
                roleDefs);

        final VOPriceModel pm = createPriceModelDefinition(pricedRoles,
                productDetails);

        final VOOrganization orga = runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                return OrganizationAssembler.toVOOrganization(customer, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });

        VOServiceDetails savedProduct = runTX(new Callable<VOServiceDetails>() {
            @Override
            public VOServiceDetails call() throws Exception {
                return svcProv.savePriceModelForCustomer(productDetails, pm,
                        orga);
            }
        });

        // and also assert domain object integrity and history object existence
        assertCreatedProductSettings(roleDefs, savedProduct, 2, true, false,
                new BigDecimal(33), false, null);
    }

    @Test
    public void testSavePriceModelForSubscription() throws Exception {
        VOService template = new VOService();
        template.setKey(sub.getProduct().getKey());
        VOServiceDetails productDetails = svcProv.getServiceDetails(template);
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        ArrayList<VOPricedRole> pricedRoles = definePricedProductRoles(
                roleDefs);

        VOPriceModel pm = createPriceModelDefinition(pricedRoles,
                productDetails);
        pm.setPeriod(PricingPeriod.DAY);

        final VOServiceDetails savedProduct = svcProv
                .savePriceModelForSubscription(productDetails, pm);

        // and also assert domain object integrity and history object existence
        assertCreatedProductSettings(roleDefs, savedProduct, 2, false, true,
                new BigDecimal(33), false, null);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelPricedParameterNegativePrice()
            throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        VOPriceModel pm = createPriceModelForParameter(productDetails, roleDefs,
                new BigDecimal(-12), null);

        svcProv.savePriceModel(productDetails, pm);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelPricedParameterInvalidRole()
            throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        VOPriceModel pm = createPriceModelForParameter(productDetails, roleDefs,
                null, Long.valueOf(500L));

        svcProv.savePriceModel(productDetails, pm);
    }

    @Test
    public void testSavePriceModelPricedParameter() throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        VOPriceModel pm = createPriceModelForParameter(productDetails, roleDefs,
                null, null);

        final VOServiceDetails savedProduct = svcProv
                .savePriceModel(productDetails, pm);

        // check that the priced product roles have been created
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product storedProduct = dm.getReference(Product.class,
                        savedProduct.getKey());
                List<PricedProductRole> storedRolePricesForParam = storedProduct
                        .getPriceModel().getSelectedParameters().get(0)
                        .getRoleSpecificUserPrices();
                Assert.assertEquals(2, storedRolePricesForParam.size());
                PricedProductRole ppr = storedRolePricesForParam.get(0);
                Assert.assertEquals(new BigDecimal(33), ppr.getPricePerUser());
                Assert.assertEquals(storedProduct.getTechnicalProduct()
                        .getRoleDefinitions().get(0), ppr.getRoleDefinition());
                Assert.assertNull(ppr.getPriceModel());
                Assert.assertNull(ppr.getPricedOption());
                Assert.assertNotNull(ppr.getPricedParameter());

                List<DomainHistoryObject<?>> hist = dm.findHistory(ppr);
                Assert.assertEquals(1, hist.size());
                Assert.assertEquals(ModificationType.ADD,
                        hist.get(0).getModtype());
                return null;
            }
        });

        // also validate correctness of the value object
        List<VOPricedRole> roleSpecificUserPrices = savedProduct.getPriceModel()
                .getSelectedParameters().get(0).getRoleSpecificUserPrices();
        Assert.assertNotNull(roleSpecificUserPrices);
        Assert.assertEquals(2, roleSpecificUserPrices.size());
        VOPricedRole voPricedProductRole = roleSpecificUserPrices.get(0);
        Assert.assertEquals(BigDecimal.valueOf(33),
                voPricedProductRole.getPricePerUser());
        Assert.assertEquals(savedProduct.getTechnicalService()
                .getRoleDefinitions().get(0).getKey(),
                voPricedProductRole.getRole().getKey());
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelPricedParameterUpdateDuplicateRoleDefinitionReference()
            throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        VOPriceModel pm = createPriceModelForParameter(productDetails, roleDefs,
                null, null);

        VOServiceDetails savedProduct = svcProv.savePriceModel(productDetails,
                pm);

        // update the parameter list and save it again
        VOPriceModel updatedPM = savedProduct.getPriceModel();
        VOPricedRole voPricedProductRole = updatedPM.getSelectedParameters()
                .get(0).getRoleSpecificUserPrices().get(0);
        voPricedProductRole.setKey(0);
        voPricedProductRole.setPricePerUser(BigDecimal.valueOf(111L));
        updatedPM.getSelectedParameters().get(0).getRoleSpecificUserPrices()
                .add(voPricedProductRole);

        savedProduct = svcProv.savePriceModel(productDetails, updatedPM);
    }

    @Test
    public void testSavePriceModelPricedParameterUpdateRoleDefinitionReference()
            throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        VOPriceModel pm = createPriceModelForParameter(productDetails, roleDefs,
                null, null);

        VOServiceDetails savedProduct = svcProv.savePriceModel(productDetails,
                pm);

        // update the parameter list and save it again
        VOPriceModel updatedPM = savedProduct.getPriceModel();
        List<VOPricedRole> roleSpecificUserPrices = updatedPM
                .getSelectedParameters().get(0).getRoleSpecificUserPrices();
        VOPricedRole voPricedProductRole = roleSpecificUserPrices.get(0);
        voPricedProductRole.setPricePerUser(BigDecimal.valueOf(111L));
        roleSpecificUserPrices.set(0, voPricedProductRole);

        savedProduct = svcProv.savePriceModel(productDetails, updatedPM);

        // ensure that only one priced product role is stored for this role
        // definition (the old one must have been deleted)
        Assert.assertEquals(2, roleSpecificUserPrices.size());
        VOPricedRole rolePricing = savedProduct.getPriceModel()
                .getSelectedParameters().get(0).getRoleSpecificUserPrices()
                .get(0);
        Assert.assertEquals(BigDecimal.valueOf(111L),
                rolePricing.getPricePerUser());
        Assert.assertEquals(1, rolePricing.getVersion());
    }

    @Test
    public void testSavePriceModelPricedParameterUpdateRoleDefinitionReferenceRemoveOldAddNew()
            throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        VOPriceModel pm = createPriceModelForParameter(productDetails, roleDefs,
                null, null);

        VOServiceDetails savedProduct = svcProv.savePriceModel(productDetails,
                pm);

        // update the parameter list and save it again
        VOPriceModel updatedPM = savedProduct.getPriceModel();
        List<VOPricedRole> roleSpecificUserPrices = updatedPM
                .getSelectedParameters().get(0).getRoleSpecificUserPrices();
        VOPricedRole voPricedProductRole = roleSpecificUserPrices.remove(0);

        savedProduct = svcProv.savePriceModel(savedProduct, updatedPM);

        updatedPM = savedProduct.getPriceModel();
        roleSpecificUserPrices = updatedPM.getSelectedParameters().get(0)
                .getRoleSpecificUserPrices();
        Assert.assertEquals(1, roleSpecificUserPrices.size());

        voPricedProductRole.setKey(0);
        voPricedProductRole.setPricePerUser(BigDecimal.valueOf(111L));
        roleSpecificUserPrices.add(voPricedProductRole);

        savedProduct = svcProv.savePriceModel(savedProduct, updatedPM);

        voPricedProductRole = savedProduct.getPriceModel()
                .getSelectedParameters().get(0).getRoleSpecificUserPrices()
                .get(1);
        Assert.assertNotNull(roleSpecificUserPrices);
        Assert.assertEquals(2, roleSpecificUserPrices.size());
        Assert.assertEquals(BigDecimal.valueOf(111L),
                voPricedProductRole.getPricePerUser());
        Assert.assertEquals(savedProduct.getTechnicalService()
                .getRoleDefinitions().get(0).getKey(),
                voPricedProductRole.getRole().getKey());
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelPricedOptionNegativePrice() throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        VOPriceModel pm = setupPricedOptions(productDetails, roleDefs,
                new BigDecimal(-123), null);
        svcProv.savePriceModel(productDetails, pm);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSavePriceModelPricedOptionInvalidRole() throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        VOPriceModel pm = setupPricedOptions(productDetails, roleDefs, null,
                Long.valueOf(500L));
        svcProv.savePriceModel(productDetails, pm);
    }

    @Test
    public void testSavePriceModelPricedOption() throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        VOPriceModel pm = setupPricedOptions(productDetails, roleDefs, null,
                null);
        final VOServiceDetails savedProduct = svcProv
                .savePriceModel(productDetails, pm);

        // read and verify the priced option role settings
        // check that the priced product roles have been created
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product storedProduct = dm.getReference(Product.class,
                        savedProduct.getKey());
                List<PricedProductRole> storedRolePricesForOption = storedProduct
                        .getPriceModel().getSelectedParameters().get(0)
                        .getPricedOptionList().get(0)
                        .getRoleSpecificUserPrices();
                Assert.assertEquals(2, storedRolePricesForOption.size());
                PricedProductRole ppr = storedRolePricesForOption.get(0);
                Assert.assertEquals(new BigDecimal(33), ppr.getPricePerUser());
                Assert.assertEquals(storedProduct.getTechnicalProduct()
                        .getRoleDefinitions().get(0), ppr.getRoleDefinition());
                Assert.assertNull(ppr.getPriceModel());
                Assert.assertNotNull(ppr.getPricedOption());
                Assert.assertNull(ppr.getPricedParameter());

                List<DomainHistoryObject<?>> hist = dm.findHistory(ppr);
                Assert.assertEquals(1, hist.size());
                Assert.assertEquals(ModificationType.ADD,
                        hist.get(0).getModtype());
                return null;
            }
        });

        // and also check the value object representation
        List<VOPricedRole> roleSpecificUserPrices = savedProduct.getPriceModel()
                .getSelectedParameters().get(0).getPricedOptions().get(0)
                .getRoleSpecificUserPrices();
        Assert.assertNotNull(roleSpecificUserPrices);
        Assert.assertEquals(2, roleSpecificUserPrices.size());
        VOPricedRole voPricedProductRole = roleSpecificUserPrices.get(0);
        Assert.assertEquals(BigDecimal.valueOf(33L),
                voPricedProductRole.getPricePerUser());
        Assert.assertEquals(savedProduct.getTechnicalService()
                .getRoleDefinitions().get(0).getKey(),
                voPricedProductRole.getRole().getKey());
    }

    @Test
    public void testSavePriceModelPricedOptionUpdateCreateNewRemoveOld()
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
        supplierAndProvider = Scenario.getSupplier();
        String productId = Scenario.getProduct().getProductId();
        List<VOService> services = svcProv.getSuppliedServices();
        VOServiceDetails serviceDetails = null;
        for (VOService voService : services) {
            if (voService.getServiceId().equals(productId)) {
                serviceDetails = svcProv.getServiceDetails(voService);
            }
        }
        if (serviceDetails == null) {
            Assert.fail("Service not found.");
            return;
        }

        VOPriceModel priceModel = serviceDetails.getPriceModel();
        List<VOPricedRole> roleSpecificUserPrices = priceModel
                .getSelectedParameters().get(2).getPricedOptions().get(0)
                .getRoleSpecificUserPrices();
        roleSpecificUserPrices.remove(0);

        priceModel = serviceDetails.getPriceModel();
        roleSpecificUserPrices = priceModel.getSelectedParameters().get(2)
                .getPricedOptions().get(0).getRoleSpecificUserPrices();

        VOPricedRole pricedRole = new VOPricedRole();
        pricedRole.setPricePerUser(BigDecimal.valueOf(11111L));
        VORoleDefinition role = serviceDetails.getTechnicalService()
                .getRoleDefinitions().get(1);
        pricedRole.setRole(role);
        roleSpecificUserPrices.add(pricedRole);

        VOServiceDetails savedProduct = svcProv.savePriceModel(serviceDetails,
                priceModel);

        roleSpecificUserPrices = priceModel.getSelectedParameters().get(2)
                .getPricedOptions().get(0).getRoleSpecificUserPrices();
        Assert.assertEquals(1, roleSpecificUserPrices.size());

        final VOPricedRole voPricedProductRole2 = savedProduct.getPriceModel()
                .getSelectedParameters().get(2).getPricedOptions().get(0)
                .getRoleSpecificUserPrices().get(0);
        // new entry must have version 0 with new price
        Assert.assertEquals(0, voPricedProductRole2.getVersion());
        Assert.assertEquals(BigDecimal.valueOf(11111L),
                voPricedProductRole2.getPricePerUser());
        Assert.assertEquals(savedProduct.getTechnicalService()
                .getRoleDefinitions().get(1).getKey(),
                voPricedProductRole2.getRole().getKey());

        // and assert that the latest history entry for that priced role also
        // has the correct pricing
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PricedProductRole role = new PricedProductRole();
                role.setKey(voPricedProductRole2.getKey());
                List<PricedProductRoleHistory> list = ParameterizedTypes.list(
                        dm.findHistory(role), PricedProductRoleHistory.class);
                Assert.assertEquals(1, list.size());
                Assert.assertEquals(new BigDecimal(11111L),
                        list.get(0).getPricePerUser());
                Assert.assertEquals(ModificationType.ADD,
                        list.get(0).getModtype());
                return null;
            }
        });
    }

    @Test
    public void testSavePriceModelPricedOptionUpdateExistingEntry()
            throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        VOPriceModel pm = setupPricedOptions(productDetails, roleDefs, null,
                null);
        VOServiceDetails savedProduct = svcProv.savePriceModel(productDetails,
                pm);

        pm = savedProduct.getPriceModel();
        VOPricedRole ppr = pm.getSelectedParameters().get(0).getPricedOptions()
                .get(0).getRoleSpecificUserPrices().get(0);
        ppr.setPricePerUser(BigDecimal.valueOf(11111L));

        savedProduct = svcProv.savePriceModel(savedProduct, pm);

        VOPricedRole voPricedProductRole = savedProduct.getPriceModel()
                .getSelectedParameters().get(0).getPricedOptions().get(0)
                .getRoleSpecificUserPrices().get(0);
        // modified entry must have version 1 with new price
        Assert.assertEquals(1, voPricedProductRole.getVersion());
        Assert.assertEquals(BigDecimal.valueOf(11111L),
                voPricedProductRole.getPricePerUser());
        Assert.assertEquals(savedProduct.getTechnicalService()
                .getRoleDefinitions().get(0).getKey(),
                voPricedProductRole.getRole().getKey());

        VOPricedRole voPricedProductRole1 = savedProduct.getPriceModel()
                .getSelectedParameters().get(0).getPricedOptions().get(0)
                .getRoleSpecificUserPrices().get(1);
        // assert remaining one is unchanged in version 0 with old pricing 66
        Assert.assertEquals(BigDecimal.valueOf(66L),
                voPricedProductRole1.getPricePerUser());
        Assert.assertEquals(0, voPricedProductRole1.getVersion());
        Assert.assertEquals(savedProduct.getTechnicalService()
                .getRoleDefinitions().get(1).getKey(),
                voPricedProductRole1.getRole().getKey());

    }

    /**
     * Configures a price model with priced product roles for a priced
     * parameter.
     * 
     * @param productDetails
     *            The details of the affected product.
     * @param roleDefs
     *            The role definitions to use in the priced roles.
     * @param rolePrice
     *            The price to be set for the priced role.
     * @param roleKey
     *            The key to be set for the role definition.
     * @return A price model containing priced roles for a priced parameter.
     */
    private VOPriceModel createPriceModelForParameter(
            VOServiceDetails productDetails,
            ArrayList<VORoleDefinition> roleDefs, BigDecimal rolePrice,
            Long roleKey) {
        ArrayList<VOPricedRole> pricedRoles = definePricedProductRoles(
                roleDefs);
        if (rolePrice != null) {
            pricedRoles.get(0).setPricePerUser(rolePrice);
        }
        if (roleKey != null) {
            pricedRoles.get(0).getRole().setKey(roleKey.longValue());
        }
        VOPriceModel pm = createPriceModelDefinition(pricedRoles,
                productDetails);
        pm.setRoleSpecificUserPrices(new ArrayList<VOPricedRole>());

        VOPricedParameter pricedParameter = new VOPricedParameter();
        pricedParameter.setPricePerSubscription(BigDecimal.valueOf(12L));
        pricedParameter.setPricePerUser(BigDecimal.valueOf(999L));
        pricedParameter.setVoParameterDef(
                productDetails.getParameters().get(0).getParameterDefinition());
        pricedParameter.setRoleSpecificUserPrices(pricedRoles);

        pm.setSelectedParameters(Collections.singletonList(pricedParameter));
        return pm;
    }

    /**
     * Configures a price model to use a priced option with priced product
     * roles.
     * 
     * @param productDetails
     *            The product details for the affected product.
     * @param roleDefs
     *            The role definitions for the price model.
     * @param priceForRole
     *            The option price per user.
     * @param roleKey
     *            The key to be set for the role definition.
     * @return The price model containing the priced option.
     */
    private VOPriceModel setupPricedOptions(VOServiceDetails productDetails,
            ArrayList<VORoleDefinition> roleDefs, BigDecimal priceForRole,
            Long roleKey) {
        ArrayList<VOPricedRole> pricedRoles = definePricedProductRoles(
                roleDefs);
        if (priceForRole != null) {
            pricedRoles.get(0).setPricePerUser(priceForRole);
        }
        if (roleKey != null) {
            pricedRoles.get(0).getRole().setKey(roleKey.longValue());
        }
        VOPriceModel pm = createPriceModelDefinition(pricedRoles,
                productDetails);
        pm.setRoleSpecificUserPrices(new ArrayList<VOPricedRole>());

        VOPricedParameter pricedParameter = new VOPricedParameter();
        pricedParameter.setPricePerSubscription(BigDecimal.valueOf(12L));
        pricedParameter.setPricePerUser(BigDecimal.valueOf(999L));

        List<VOParameter> parameters = productDetails.getParameters();
        for (VOParameter voParameter : parameters) {
            if (voParameter.getParameterDefinition()
                    .getValueType() == ParameterValueType.ENUMERATION) {
                pricedParameter.setVoParameterDef(
                        voParameter.getParameterDefinition());
            }
        }

        VOPricedOption vpo = new VOPricedOption();
        vpo.setParameterOptionKey(
                productDetails.getTechnicalService().getParameterDefinitions()
                        .get(1).getParameterOptions().get(0).getKey());
        vpo.setPricePerUser(BigDecimal.valueOf(33L));
        vpo.setPricePerSubscription(BigDecimal.ZERO);
        pricedParameter.setPricedOptions(Collections.singletonList(vpo));

        pm.setSelectedParameters(Collections.singletonList(pricedParameter));

        vpo.setRoleSpecificUserPrices(pricedRoles);
        return pm;
    }

    /**
     * Retrieves the product details.
     * 
     * @return The product details.
     */
    private VOServiceDetails getProductDetails() throws Exception {
        List<VOService> products = svcProv.getSuppliedServices();
        Assert.assertEquals(1, products.size());
        VOService product = products.get(0);
        VOServiceDetails productDetails = svcProv.getServiceDetails(product);
        return productDetails;
    }

    /**
     * Defines role definitions.
     * 
     * @param productDetails
     *            The product details to retrieve the available role definitions
     *            from.
     * @param amount
     *            The number of definitions to be created, can be 1 or 2.
     * @return A list of role definitions to be used for the price model
     *         definition.
     */
    private ArrayList<VORoleDefinition> defineRoleDefinitionsForPM(
            VOServiceDetails productDetails, int amount) {
        List<VORoleDefinition> roleDefinitions = productDetails
                .getTechnicalService().getRoleDefinitions();
        Assert.assertEquals(2, roleDefinitions.size());
        VORoleDefinition roleDefinition1 = roleDefinitions.get(0);
        VORoleDefinition roleDefinition2 = roleDefinitions.get(1);

        ArrayList<VORoleDefinition> roleDefs = new ArrayList<>();
        roleDefs.add(roleDefinition1);
        if (amount == 2) {
            roleDefs.add(roleDefinition2);
        }
        return roleDefs;
    }

    /**
     * Defines the priced product roles definitions to be used for the test.
     * 
     * @param roleDefs
     *            The role definitions serving as base.
     * @return The priced product roles.
     */
    private ArrayList<VOPricedRole> definePricedProductRoles(
            ArrayList<VORoleDefinition> roleDefs) {
        VOPricedRole ppr = new VOPricedRole();
        ppr.setRole(roleDefs.get(0));
        ppr.setPricePerUser(BigDecimal.valueOf(33L));

        VOPricedRole ppr2 = new VOPricedRole();
        ppr2.setRole(roleDefs.get(1));
        ppr2.setPricePerUser(BigDecimal.valueOf(66L));

        ArrayList<VOPricedRole> pricedRoles = new ArrayList<>();
        pricedRoles.add(ppr);
        pricedRoles.add(ppr2);
        return pricedRoles;
    }

    @Test
    public void testModifyPricedParam() throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);

        VOPriceModel pm = createPriceModelForParameter(productDetails, roleDefs,
                null, null);

        VOServiceDetails savedProduct = svcProv.savePriceModel(productDetails,
                pm);

        VOPriceModel storedPM = savedProduct.getPriceModel();
        VOPricedParameter voPricedParameter = storedPM.getSelectedParameters()
                .get(0);
        Assert.assertEquals(BigDecimal.valueOf(999),
                voPricedParameter.getPricePerUser());

        voPricedParameter.setPricePerUser(BigDecimal.ONE);

        savedProduct = svcProv.savePriceModel(productDetails, storedPM);

        storedPM = savedProduct.getPriceModel();
        voPricedParameter = storedPM.getSelectedParameters().get(0);
        Assert.assertEquals(BigDecimal.ONE,
                voPricedParameter.getPricePerUser());
    }

    @Test
    public void testModifyPricedParamOption() throws Exception {
        VOServiceDetails productDetails = getProductDetails();
        ArrayList<VORoleDefinition> roleDefs = defineRoleDefinitionsForPM(
                productDetails, 2);
        VOPriceModel pm = setupPricedOptions(productDetails, roleDefs, null,
                null);

        VOServiceDetails savedProduct = svcProv.savePriceModel(productDetails,
                pm);

        VOPriceModel storedPM = savedProduct.getPriceModel();
        VOPricedOption voPricedOption = storedPM.getSelectedParameters().get(0)
                .getPricedOptions().get(0);
        Assert.assertEquals(BigDecimal.valueOf(33),
                voPricedOption.getPricePerUser());

        voPricedOption.setPricePerUser(BigDecimal.valueOf(1));

        savedProduct = svcProv.savePriceModel(savedProduct, storedPM);

        storedPM = savedProduct.getPriceModel();
        voPricedOption = storedPM.getSelectedParameters().get(0)
                .getPricedOptions().get(0);
        Assert.assertEquals(BigDecimal.valueOf(1),
                voPricedOption.getPricePerUser());
    }
}
