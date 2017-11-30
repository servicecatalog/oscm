/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                     
 *                                                                              
 *  Creation Date: 14.07.2010                                                      
 *                                                                              
 *  Completion Time: <date>                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.*;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.oscm.test.Numbers.*;

/**
 * @author weiser
 * 
 */
@Ignore
public class ServiceProvisioningSteppedPricingIT extends EJBTestBase {

    private DataService dm;
    private ServiceProvisioningService svcProv;
    private LocalizerServiceLocal localizer;

    private Organization supplierAndProvider;
    private Organization customer;
    private TechnicalProduct techProd;
    private Product product;
    private PlatformUser user;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.login("1");

        container.addBean(new DataServiceBean());
        container.addBean(new SessionServiceStub());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new ApplicationServiceStub() {

            @Override
            public void validateCommunication(TechnicalProduct techProduct)
                    throws TechnicalServiceNotAliveException {

            }

        });
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(new ImageResourceServiceStub() {

            @Override
            public ImageResource read(long objectKey, ImageType imageType) {
                return null;
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
        localizer = container.get(LocalizerServiceLocal.class);

        init();
        container.login(String.valueOf(user.getKey()), ROLE_SERVICE_MANAGER);
    }

    private void init() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                // create roles and currency
                createOrganizationRoles(dm);
                createSupportedCurrencies(dm);
                createPaymentTypes(dm);
                SupportedCountries.createSomeSupportedCountries(dm);
                // create organization
                supplierAndProvider = Organizations.createOrganization(dm,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                Marketplace mpLocal = Marketplaces.ensureMarketplace(
                        supplierAndProvider,
                        supplierAndProvider.getOrganizationId(), dm);
                user = Organizations.createUserForOrg(dm, supplierAndProvider,
                        true, "admin");

                // create technical product with roles};
                techProd = TechnicalProducts.createTechnicalProduct(dm,
                        supplierAndProvider, "prodId", false,
                        ServiceAccessType.LOGIN);

                TechnicalProducts.addEvent("event", EventType.SERVICE_EVENT,
                        techProd, dm);

                ParameterDefinition paramDefInteger = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.INTEGER,
                                "intParam", ParameterType.SERVICE_PARAMETER,
                                techProd, dm, new Long(5), null, true);
                ParameterDefinition paramDefLong = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.LONG,
                                "longParam", ParameterType.SERVICE_PARAMETER,
                                techProd, dm, new Long(10000), null, true);
                ParameterDefinition paramDefDuration = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.DURATION,
                                "durationParam",
                                ParameterType.SERVICE_PARAMETER, techProd, dm,
                                null, new Long(40), true);
                ParameterDefinition paramDefString = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.STRING,
                                "stringParam", ParameterType.SERVICE_PARAMETER,
                                techProd, dm, null, null, true);
                ParameterDefinition paramDefBoolean = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.BOOLEAN,
                                "booleanParam",
                                ParameterType.SERVICE_PARAMETER, techProd, dm,
                                null, null, true);
                ParameterDefinition paramDefEnum = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.ENUMERATION,
                                "enumParam", ParameterType.SERVICE_PARAMETER,
                                techProd, dm, null, null, true);

                ParameterOption po = new ParameterOption();
                po.setOptionId("optionId");
                po.setParameterDefinition(paramDefEnum);
                paramDefEnum.setOptionList(Collections.singletonList(po));
                dm.persist(po);

                // create a product for the technical product
                product = Products.createProduct(supplierAndProvider, techProd,
                        true, "product", null, mpLocal, dm);
                // publish created product to local marketplace
                product.setStatus(ServiceStatus.INACTIVE);

                Parameter paramInt = new Parameter();
                paramInt.setValue("123");
                paramInt.setParameterSet(product.getParameterSet());
                paramInt.setParameterDefinition(paramDefInteger);
                paramInt.setConfigurable(true);
                dm.persist(paramInt);

                Parameter paramLong = new Parameter();
                paramLong.setValue("234");
                paramLong.setParameterSet(product.getParameterSet());
                paramLong.setParameterDefinition(paramDefLong);
                paramLong.setConfigurable(true);
                dm.persist(paramLong);

                Parameter paramDuration = new Parameter();
                paramDuration.setValue("100000");
                paramDuration.setParameterSet(product.getParameterSet());
                paramDuration.setParameterDefinition(paramDefDuration);
                paramDuration.setConfigurable(true);
                dm.persist(paramDuration);

                Parameter paramString = new Parameter();
                paramString.setValue("some string");
                paramString.setParameterSet(product.getParameterSet());
                paramString.setParameterDefinition(paramDefString);
                paramString.setConfigurable(true);
                dm.persist(paramString);

                Parameter paramBooelan = new Parameter();
                paramBooelan.setValue("true");
                paramBooelan.setParameterSet(product.getParameterSet());
                paramBooelan.setParameterDefinition(paramDefBoolean);
                paramBooelan.setConfigurable(true);
                dm.persist(paramBooelan);

                Parameter paramEnum = new Parameter();
                paramEnum.setParameterSet(product.getParameterSet());
                paramEnum.setParameterDefinition(paramDefEnum);
                paramEnum.setConfigurable(true);
                paramEnum.setValue("optionId");
                dm.persist(paramEnum);

                customer = Organizations
                        .createCustomer(dm, supplierAndProvider);
                return null;
            }
        });
    }

    private static VOPriceModel createBasicPriceModelForSteppedPricing(
            VOServiceDetails prod) throws Exception {
        VOPriceModel pm = createBasicPriceModel(prod);
        pm.setPricePerUserAssignment(BigDecimal.ZERO);
        List<VOPricedEvent> events = pm.getConsideredEvents();
        for (VOPricedEvent e : events) {
            e.setEventPrice(BigDecimal.ZERO);
        }
        List<VOPricedParameter> params = pm.getSelectedParameters();
        for (VOPricedParameter p : params) {
            p.setPricePerSubscription(BigDecimal.ZERO);
        }
        return pm;
    }

    private static VOPriceModel createBasicPriceModel(VOServiceDetails prod)
            throws Exception {
        VOPriceModel pm = prod.getPriceModel();
        if (pm == null) {
            pm = new VOPriceModel();
        }
        pm.setType(PriceModelType.PRO_RATA);
        pm.setCurrencyISOCode("EUR");
        pm.setPeriod(PricingPeriod.MONTH);
        pm.setPricePerPeriod(BigDecimal.valueOf(5L));
        pm.setPricePerUserAssignment(BigDecimal.valueOf(5L));

        List<VOParameter> parameters = prod.getParameters();
        List<VOPricedParameter> pricedParameters = new ArrayList<VOPricedParameter>();
        for (VOParameter p : parameters) {
            VOParameterDefinition pd = p.getParameterDefinition();
            // only create priced parameters for non string based parameters
            if (pd.getValueType() == ParameterValueType.STRING) {
                continue;
            }
            VOPricedParameter pp = new VOPricedParameter(pd);
            pp.setParameterKey(p.getKey());
            pp.setPricePerSubscription(BigDecimal.ONE);
            pp.setPricePerUser(BigDecimal.ONE);
            if (pd.getValueType() == ParameterValueType.ENUMERATION) {
                List<VOParameterOption> options = pd.getParameterOptions();
                List<VOPricedOption> pricedOptions = new ArrayList<VOPricedOption>();
                for (VOParameterOption o : options) {
                    VOPricedOption po = new VOPricedOption();
                    po.setParameterOptionKey(o.getKey());
                    po.setPricePerSubscription(BigDecimal.valueOf(1));
                    po.setPricePerUser(BigDecimal.valueOf(1));
                    pricedOptions.add(po);
                }
                pp.setPricedOptions(pricedOptions);
            }
            pricedParameters.add(pp);
        }
        pm.setSelectedParameters(pricedParameters);

        List<VOEventDefinition> events = prod.getTechnicalService()
                .getEventDefinitions();
        List<VOPricedEvent> pricedEvents = new ArrayList<VOPricedEvent>();
        for (VOEventDefinition e : events) {
            VOPricedEvent pe = new VOPricedEvent(e);
            pe.setEventPrice(BigDecimal.valueOf(1));
            pricedEvents.add(pe);
        }
        pm.setConsideredEvents(pricedEvents);
        return pm;
    }

    private VOServiceDetails getProductDetails() throws Exception {
        List<VOService> products = svcProv.getSuppliedServices();
        Assert.assertNotNull(products);
        Assert.assertEquals(1, products.size());
        VOServiceDetails prod = svcProv.getServiceDetails(products.get(0));
        return prod;
    }

    private static List<VOSteppedPrice> createSteppedPricesList() {
        List<VOSteppedPrice> prices = new ArrayList<VOSteppedPrice>();
        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L5);
        sp.setPrice(BigDecimal.valueOf(10));
        prices.add(sp);

        sp = new VOSteppedPrice();
        sp.setLimit(L10);
        sp.setPrice(BigDecimal.valueOf(5));
        prices.add(sp);

        sp = new VOSteppedPrice();
        sp.setLimit(null);
        sp.setPrice(BigDecimal.valueOf(2));
        prices.add(sp);

        return prices;
    }

    private static void validateSteppedPrices(
            List<VOSteppedPrice> steppedPrices, int size, Long[] limits,
            long[] prices) {
        Assert.assertNotNull(steppedPrices);
        Assert.assertEquals(size, steppedPrices.size());
        for (int i = 0; i < size; i++) {
            Assert.assertEquals(limits[i], steppedPrices.get(i).getLimit());
            Assert.assertEquals(BigDecimal.valueOf(prices[i]), steppedPrices
                    .get(i).getPrice());
        }
    }

    private static VOPricedParameter getParameterWithType(
            ParameterValueType type, VOPriceModel pm) {
        List<VOPricedParameter> params = pm.getSelectedParameters();
        for (VOPricedParameter pp : params) {
            if (pp.getVoParameterDef().getValueType() == type) {
                return pp;
            }
        }
        return null;
    }

    private VOOrganization getCustomer() throws Exception {
        return runTX(new Callable<VOOrganization>() {

            @Override
            public VOOrganization call() throws Exception {
                Organization org = dm.getReference(Organization.class,
                        customer.getKey());
                return OrganizationAssembler.toVOOrganization(org, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });
    }

    private VOServiceDetails subscribe() throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModel(prod);
        final VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        svcProv.activateService(saved);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                // set payment information for the customer
                PaymentType paymentType = findPaymentType(INVOICE, dm);
                PaymentInfo pi = PaymentInfos.createPaymentInfo(customer, dm,
                        paymentType);

                Subscription sub = Subscriptions.createSubscription(dm,
                        customer.getOrganizationId(), saved.getServiceId(),
                        "sub", supplierAndProvider);
                sub.setPaymentInfo(pi);
                BillingContact bc = PaymentInfos.createBillingContact(dm,
                        customer);
                sub.setBillingContact(bc);

                return null;
            }
        });
        return svcProv.getServiceForSubscription(getCustomer(), "sub");
    }

    /**
     * Try to save a price model with stepped pricing on a parameter of the
     * provided value type.
     * 
     * @param type
     *            the parameter type
     * @param method
     *            1 = save; 2 = saveForCustomer; 3 = saveForSubscription
     * @throws Exception
     */
    private void savePriceModel(ParameterValueType type, int method)
            throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModel(prod);
        VOPricedParameter pp = getParameterWithType(type, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        switch (method) {
        case 1:
            svcProv.savePriceModel(prod, pm);
            break;
        case 2:
            svcProv.savePriceModelForCustomer(prod, pm, getCustomer());
            break;
        case 3:
            VOServiceDetails details = subscribe();
            pm = details.getPriceModel();
            pp = getParameterWithType(type, details.getPriceModel());
            prices = createSteppedPricesList();
            pp.setSteppedPrices(prices);
            svcProv.savePriceModelForSubscription(details, pm);
            break;
        default:
            Assert.fail("Unknown method: " + method);
            break;
        }
        svcProv.savePriceModel(prod, pm);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModel_InvalidParameter_Boolean() throws Exception {
        savePriceModel(ParameterValueType.BOOLEAN, 1);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModel_InvalidParameter_Enumeration()
            throws Exception {
        savePriceModel(ParameterValueType.ENUMERATION, 1);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModel_LimitBelowMin() throws Exception {
        savePriceModel(ParameterValueType.DURATION, 1);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModel_LimitAboveMax() throws Exception {
        savePriceModel(ParameterValueType.INTEGER, 1);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelForCustomer_InvalidParameter_Boolean()
            throws Exception {
        savePriceModel(ParameterValueType.BOOLEAN, 2);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelForCustomer_InvalidParameter_Enumeration()
            throws Exception {
        savePriceModel(ParameterValueType.ENUMERATION, 2);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelForCustomer_LimitBelowMin() throws Exception {
        savePriceModel(ParameterValueType.DURATION, 2);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelForCustomer_LimitAboveMax() throws Exception {
        savePriceModel(ParameterValueType.INTEGER, 2);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelForSubscription_InvalidParameter_Boolean()
            throws Exception {
        savePriceModel(ParameterValueType.BOOLEAN, 3);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelForSubscription_InvalidParameter_Enumeration()
            throws Exception {
        savePriceModel(ParameterValueType.ENUMERATION, 3);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelForSubscription_LimitBelowMin()
            throws Exception {
        savePriceModel(ParameterValueType.DURATION, 3);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModelForSubscription_LimitAboveMax()
            throws Exception {
        savePriceModel(ParameterValueType.INTEGER, 3);
    }

    @Test
    public void testSavePriceModel_PriceModelStepped() throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pm.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3, new Long[] { L5, L10, null },
                new long[] { 10, 5, 2 });
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModel_PriceModelSteppedAndBasePrice()
            throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModel(prod);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pm.setSteppedPrices(prices);
        try {
            svcProv.savePriceModel(prod, pm);
        } catch (ValidationException e) {
            assertEquals(ValidationException.ReasonEnum.STEPPED_USER_PRICING,
                    e.getReason());
            throw e;
        }
    }

    @Test
    public void testSavePriceModel_PriceModelStepped_RemoveStep()
            throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pm.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getSteppedPrices();
        steppedPrices.remove(0);
        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        steppedPrices = saved.getPriceModel().getSteppedPrices();

        validateSteppedPrices(steppedPrices, 2, new Long[] { L10, null },
                new long[] { 5, 2 });
    }

    @Test
    public void testSavePriceModel_PriceModelStepped_AddStep() throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pm.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getSteppedPrices();
        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L100);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);
        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        steppedPrices = saved.getPriceModel().getSteppedPrices();

        validateSteppedPrices(steppedPrices, 4, new Long[] { L5, L10, null,
                L100 }, new long[] { 10, 5, 2, 1 });
    }

    @Test
    public void testSavePriceModel_PriceModelStepped_ModifyStep()
            throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pm.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getSteppedPrices();
        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.ONE);
        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        steppedPrices = saved.getPriceModel().getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3, new Long[] { L100, L10, null },
                new long[] { 1, 5, 2 });
    }

    @Test
    public void testSavePriceModel_PriceModelStepped_AddRemoveModifyStep()
            throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pm.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getSteppedPrices();

        steppedPrices.remove(0);

        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.valueOf(3));

        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L200);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);

        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        steppedPrices = saved.getPriceModel().getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3,
                new Long[] { L100, null, L200 }, new long[] { 3, 2, 1 });
    }

    @Test
    public void testSavePriceModel_EventStepped() throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();

        event.setSteppedPrices(prices);

        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getConsideredEvents().get(0).getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3, new Long[] { L5, L10, null },
                new long[] { 10, 5, 2 });
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModel_EventSteppedAndBasePrice() throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModel(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        event.setSteppedPrices(prices);
        try {
            svcProv.savePriceModel(prod, pm);
        } catch (ValidationException e) {
            assertEquals(ValidationException.ReasonEnum.STEPPED_EVENT_PRICING,
                    e.getReason());
            assertEquals(event.getEventDefinition().getEventId(),
                    e.getMessageParams()[0]);
            throw e;
        }
    }

    @Test
    public void testSavePriceModel_EventStepped_RemoveStep() throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        event.setSteppedPrices(prices);

        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        event = saved.getPriceModel().getConsideredEvents().get(0);
        List<VOSteppedPrice> steppedPrices = event.getSteppedPrices();
        steppedPrices.remove(0);
        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        steppedPrices = saved.getPriceModel().getConsideredEvents().get(0)
                .getSteppedPrices();

        validateSteppedPrices(steppedPrices, 2, new Long[] { L10, null },
                new long[] { 5, 2 });
    }

    @Test
    public void testSavePriceModel_EventStepped_AddStep() throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        event.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        event = saved.getPriceModel().getConsideredEvents().get(0);
        List<VOSteppedPrice> steppedPrices = event.getSteppedPrices();
        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L100);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);
        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        steppedPrices = saved.getPriceModel().getConsideredEvents().get(0)
                .getSteppedPrices();

        validateSteppedPrices(steppedPrices, 4, new Long[] { L5, L10, null,
                L100 }, new long[] { 10, 5, 2, 1 });
    }

    @Test
    public void testSavePriceModel_EventStepped_ModifyStep() throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        event.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        event = saved.getPriceModel().getConsideredEvents().get(0);
        List<VOSteppedPrice> steppedPrices = event.getSteppedPrices();

        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.ONE);

        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        steppedPrices = saved.getPriceModel().getConsideredEvents().get(0)
                .getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3, new Long[] { L100, L10, null },
                new long[] { 1, 5, 2 });
    }

    @Test
    public void testSavePriceModel_EventStepped_AddRemoveModifyStep()
            throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        event.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        event = saved.getPriceModel().getConsideredEvents().get(0);
        List<VOSteppedPrice> steppedPrices = event.getSteppedPrices();

        steppedPrices.remove(0);

        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.valueOf(3));

        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L200);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);

        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        steppedPrices = saved.getPriceModel().getConsideredEvents().get(0)
                .getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3,
                new Long[] { L100, null, L200 }, new long[] { 3, 2, 1 });
    }

    @Test
    public void testSavePriceModel_ParameterStepped() throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);

        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());
        validateSteppedPrices(pp.getSteppedPrices(), 3, new Long[] { L5, L10,
                null }, new long[] { 10, 5, 2 });
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModel_ParameterSteppedAndBasePrice()
            throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModel(prod);
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        try {
            svcProv.savePriceModel(prod, pm);
        } catch (ValidationException e) {
            assertEquals(
                    ValidationException.ReasonEnum.STEPPED_PARAMETER_PRICING,
                    e.getReason());
            assertEquals(pp.getVoParameterDef().getParameterId(),
                    e.getMessageParams()[0]);
            throw e;
        }
    }

    @Test
    public void testSavePriceModel_ParameterStepped_RemoveStep()
            throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());
        List<VOSteppedPrice> steppedPrices = pp.getSteppedPrices();
        steppedPrices.remove(0);
        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());

        validateSteppedPrices(pp.getSteppedPrices(), 2,
                new Long[] { L10, null }, new long[] { 5, 2 });
    }

    @Test
    public void testSavePriceModel_ParameterStepped_AddStep() throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());
        List<VOSteppedPrice> steppedPrices = pp.getSteppedPrices();
        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L100);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);
        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());

        validateSteppedPrices(pp.getSteppedPrices(), 4, new Long[] { L5, L10,
                null, L100 }, new long[] { 10, 5, 2, 1 });
    }

    @Test
    public void testSavePriceModel_ParameterStepped_ModifyStep()
            throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());
        List<VOSteppedPrice> steppedPrices = pp.getSteppedPrices();
        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.ONE);
        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());

        validateSteppedPrices(pp.getSteppedPrices(), 3, new Long[] { L100, L10,
                null }, new long[] { 1, 5, 2 });
    }

    @Test
    public void testSavePriceModel_ParameterStepped_AddRemoveModifyStep()
            throws Exception {
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());
        List<VOSteppedPrice> steppedPrices = pp.getSteppedPrices();

        steppedPrices.remove(0);

        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.valueOf(3));

        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L200);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);

        saved = svcProv.savePriceModel(prod, saved.getPriceModel());
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());

        validateSteppedPrices(pp.getSteppedPrices(), 3, new Long[] { L100,
                null, L200 }, new long[] { 3, 2, 1 });
    }

    @Test
    public void testSavePriceModelForCustomer_PriceModelStepped()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pm.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3, new Long[] { L5, L10, null },
                new long[] { 10, 5, 2 });
    }

    @Test
    public void testSavePriceModelForCustomer_PriceModelStepped_RemoveStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pm.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getSteppedPrices();
        steppedPrices.remove(0);
        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        steppedPrices = saved.getPriceModel().getSteppedPrices();

        validateSteppedPrices(steppedPrices, 2, new Long[] { L10, null },
                new long[] { 5, 2 });
    }

    @Test
    public void testSavePriceModelForCustomer_PriceModelStepped_AddStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pm.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getSteppedPrices();
        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L100);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);
        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        steppedPrices = saved.getPriceModel().getSteppedPrices();

        validateSteppedPrices(steppedPrices, 4, new Long[] { L5, L10, null,
                L100 }, new long[] { 10, 5, 2, 1 });
    }

    @Test
    public void testSavePriceModelForCustomer_PriceModelStepped_ModifyStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pm.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getSteppedPrices();
        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.ONE);
        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        steppedPrices = saved.getPriceModel().getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3, new Long[] { L100, L10, null },
                new long[] { 1, 5, 2 });
    }

    @Test
    public void testSavePriceModelForCustomer_PriceModelStepped_AddRemoveModifyStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pm.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getSteppedPrices();

        steppedPrices.remove(0);

        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.valueOf(3));

        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L200);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);

        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        steppedPrices = saved.getPriceModel().getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3,
                new Long[] { L100, null, L200 }, new long[] { 3, 2, 1 });
    }

    @Test
    public void testSavePriceModelForCustomer_EventStepped() throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();

        event.setSteppedPrices(prices);

        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getConsideredEvents().get(0).getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3, new Long[] { L5, L10, null },
                new long[] { 10, 5, 2 });
    }

    @Test
    public void testSavePriceModelForCustomer_EventStepped_RemoveStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        event.setSteppedPrices(prices);

        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        event = saved.getPriceModel().getConsideredEvents().get(0);
        List<VOSteppedPrice> steppedPrices = event.getSteppedPrices();
        steppedPrices.remove(0);
        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        steppedPrices = saved.getPriceModel().getConsideredEvents().get(0)
                .getSteppedPrices();

        validateSteppedPrices(steppedPrices, 2, new Long[] { L10, null },
                new long[] { 5, 2 });
    }

    @Test
    public void testSavePriceModelForCustomer_EventStepped_AddStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        event.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        event = saved.getPriceModel().getConsideredEvents().get(0);
        List<VOSteppedPrice> steppedPrices = event.getSteppedPrices();
        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L100);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);
        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        steppedPrices = saved.getPriceModel().getConsideredEvents().get(0)
                .getSteppedPrices();

        validateSteppedPrices(steppedPrices, 4, new Long[] { L5, L10, null,
                L100 }, new long[] { 10, 5, 2, 1 });
    }

    @Test
    public void testSavePriceModelForCustomer_EventStepped_ModifyStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        event.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        event = saved.getPriceModel().getConsideredEvents().get(0);
        List<VOSteppedPrice> steppedPrices = event.getSteppedPrices();

        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.ONE);

        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        steppedPrices = saved.getPriceModel().getConsideredEvents().get(0)
                .getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3, new Long[] { L100, L10, null },
                new long[] { 1, 5, 2 });
    }

    @Test
    public void testSavePriceModelForCustomer_EventStepped_AddRemoveModifyStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        event.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        event = saved.getPriceModel().getConsideredEvents().get(0);
        List<VOSteppedPrice> steppedPrices = event.getSteppedPrices();

        steppedPrices.remove(0);

        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.valueOf(3));

        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L200);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);

        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        steppedPrices = saved.getPriceModel().getConsideredEvents().get(0)
                .getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3,
                new Long[] { L100, null, L200 }, new long[] { 3, 2, 1 });
    }

    @Test
    public void testSavePriceModelForCustomer_ParameterStepped()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);

        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());
        validateSteppedPrices(pp.getSteppedPrices(), 3, new Long[] { L5, L10,
                null }, new long[] { 10, 5, 2 });
    }

    @Test
    public void testSavePriceModelForCustomer_ParameterStepped_RemoveStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());
        List<VOSteppedPrice> steppedPrices = pp.getSteppedPrices();
        steppedPrices.remove(0);
        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());

        validateSteppedPrices(pp.getSteppedPrices(), 2,
                new Long[] { L10, null }, new long[] { 5, 2 });
    }

    @Test
    public void testSavePriceModelForCustomer_ParameterStepped_AddStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());
        List<VOSteppedPrice> steppedPrices = pp.getSteppedPrices();
        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L100);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);
        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());

        validateSteppedPrices(pp.getSteppedPrices(), 4, new Long[] { L5, L10,
                null, L100 }, new long[] { 10, 5, 2, 1 });
    }

    @Test
    public void testSavePriceModelForCustomer_ParameterStepped_ModifyStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());
        List<VOSteppedPrice> steppedPrices = pp.getSteppedPrices();
        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.ONE);
        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());

        validateSteppedPrices(pp.getSteppedPrices(), 3, new Long[] { L100, L10,
                null }, new long[] { 1, 5, 2 });
    }

    @Test
    public void testSavePriceModelForCustomer_ParameterStepped_AddRemoveModifyStep()
            throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG, pm);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        VOServiceDetails saved = svcProv.savePriceModelForCustomer(prod, pm,
                org);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());
        List<VOSteppedPrice> steppedPrices = pp.getSteppedPrices();

        steppedPrices.remove(0);

        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.valueOf(3));

        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L200);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);

        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());

        validateSteppedPrices(pp.getSteppedPrices(), 3, new Long[] { L100,
                null, L200 }, new long[] { 3, 2, 1 });
    }

    @Test
    public void testSavePriceModelForSubscription_PriceModelStepped_AddRemoveModifyStep()
            throws Exception {
        VOServiceDetails prod = subscribe();
        List<VOSteppedPrice> prices = createSteppedPricesList();
        prod.getPriceModel().setSteppedPrices(prices);
        prod.getPriceModel().setPricePerUserAssignment(BigDecimal.ZERO);
        VOServiceDetails saved = svcProv.savePriceModelForSubscription(prod,
                prod.getPriceModel());
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getSteppedPrices();

        steppedPrices.remove(0);

        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.valueOf(3));

        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L200);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);

        saved = svcProv.savePriceModelForSubscription(saved,
                saved.getPriceModel());
        steppedPrices = saved.getPriceModel().getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3,
                new Long[] { L100, null, L200 }, new long[] { 3, 2, 1 });
    }

    @Test
    public void testSavePriceModelForSubscription_EventStepped_AddRemoveModifyStep()
            throws Exception {
        VOServiceDetails prod = subscribe();
        VOPricedEvent event = prod.getPriceModel().getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();
        event.setSteppedPrices(prices);
        event.setEventPrice(BigDecimal.ZERO);

        VOServiceDetails saved = svcProv.savePriceModelForSubscription(prod,
                prod.getPriceModel());
        event = saved.getPriceModel().getConsideredEvents().get(0);
        List<VOSteppedPrice> steppedPrices = event.getSteppedPrices();

        steppedPrices.remove(0);

        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.valueOf(3));

        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L200);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);

        saved = svcProv.savePriceModelForSubscription(saved,
                saved.getPriceModel());
        steppedPrices = saved.getPriceModel().getConsideredEvents().get(0)
                .getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3,
                new Long[] { L100, null, L200 }, new long[] { 3, 2, 1 });
    }

    @Test
    public void testSavePriceModelForSubscription_ParameterStepped_AddRemoveModifyStep()
            throws Exception {
        VOServiceDetails details = subscribe();
        VOPricedParameter pp = getParameterWithType(ParameterValueType.LONG,
                details.getPriceModel());
        List<VOSteppedPrice> prices = createSteppedPricesList();
        pp.setSteppedPrices(prices);
        pp.setPricePerSubscription(BigDecimal.ZERO);
        VOServiceDetails saved = svcProv.savePriceModelForSubscription(details,
                details.getPriceModel());

        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());
        List<VOSteppedPrice> steppedPrices = pp.getSteppedPrices();

        steppedPrices.remove(0);

        steppedPrices.get(0).setLimit(L100);
        steppedPrices.get(0).setPrice(BigDecimal.valueOf(3));

        VOSteppedPrice sp = new VOSteppedPrice();
        sp.setLimit(L200);
        sp.setPrice(BigDecimal.ONE);
        steppedPrices.add(sp);
        svcProv.savePriceModelForSubscription(saved, saved.getPriceModel());
        pp = getParameterWithType(ParameterValueType.LONG,
                saved.getPriceModel());

        validateSteppedPrices(pp.getSteppedPrices(), 3, new Long[] { L100,
                null, L200 }, new long[] { 3, 2, 1 });
    }

    @Test
    public void testSavePriceModel_AndSameForCustomer() throws Exception {
        VOOrganization org = getCustomer();
        VOServiceDetails prod = getProductDetails();
        VOPriceModel pm = createBasicPriceModelForSteppedPricing(prod);
        VOPricedEvent event = pm.getConsideredEvents().get(0);
        List<VOSteppedPrice> prices = createSteppedPricesList();

        event.setSteppedPrices(prices);

        VOServiceDetails saved = svcProv.savePriceModel(prod, pm);

        saved = svcProv.savePriceModelForCustomer(saved, saved.getPriceModel(),
                org);
        List<VOSteppedPrice> steppedPrices = saved.getPriceModel()
                .getConsideredEvents().get(0).getSteppedPrices();

        validateSteppedPrices(steppedPrices, 3, new Long[] { L5, L10, null },
                new long[] { 10, 5, 2 });
    }

    // refers to bug 5686
    @Test
    public void testSavePriceModelVerifyDescription() throws Exception {
        final String productId = "productBug5686";
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        dm, supplierAndProvider, "techProd", false,
                        ServiceAccessType.LOGIN);
                Product prod = Products.createProductWithoutPriceModel(
                        supplierAndProvider, tp, productId);
                dm.persist(prod);
                return null;
            }
        });

        VOService currentService = null;
        List<VOService> services = svcProv.getSuppliedServices();
        for (VOService voService : services) {
            if (voService.getServiceId().equals(productId)) {
                currentService = voService;
                break;
            }
        }
        assertNotNull(currentService);

        VOServiceDetails serviceDetails = svcProv
                .getServiceDetails(currentService);
        VOPriceModel pm = new VOPriceModel();
        pm.setDescription("description");
        pm.setType(PriceModelType.PRO_RATA);
        pm.setCurrencyISOCode("EUR");
        pm.setPeriod(PricingPeriod.MONTH);

        serviceDetails = svcProv.savePriceModel(serviceDetails, pm);
        VOPriceModel priceModel = serviceDetails.getPriceModel();
        assertEquals("description", priceModel.getDescription());

        serviceDetails = svcProv.getServiceDetails(currentService);
        priceModel = serviceDetails.getPriceModel();
        assertEquals("description", priceModel.getDescription());
    }

}
