/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.10.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.vo.VOOrganization;

/**
 * Auxiliary class to set up test data.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class Scenario {

    private static Organization supplier;
    private static PlatformUser supplierAdminUser;
    private static Organization customer;
    private static PlatformUser customerAdminUser;
    private static Organization secondCustomer;
    private static PlatformUser secondCustomerAdminUser;
    private static VOOrganization voCustomer;
    private static VOOrganization voSecondCustomer;
    private static TechnicalProduct tProd;
    private static RoleDefinition roleDef1;
    private static RoleDefinition roleDef2;
    private static Event event1;
    private static Event event2;
    private static ParameterDefinition paramDefLong;
    private static ParameterDefinition paramDefBool;
    private static ParameterDefinition paramDefString;
    private static ParameterDefinition paramDefEnum;
    private static ParameterDefinition paramDefInt;
    private static Product product;
    private static PriceModel priceModel;
    private static Subscription subscription;
    private static Uda customerUda1;
    private static Uda customerUda2;
    private static Uda subUda1;
    private static Uda subUda2;
    private static UdaDefinition udaDefSub1;
    private static UdaDefinition udaDefSub2;
    private static UdaDefinition udaDefCust1;
    private static UdaDefinition udaDefCust2;
    private static Marketplace mp;

    /**
     * Performs the setup. Must be run in a transaction.
     * 
     * @param container
     *            The test container.
     * @throws Exception
     */
    public static void setup(TestContainer container, boolean basicSetupRequired)
            throws Exception {
        setup(container, basicSetupRequired, false);
    }

    /**
     * Performs the setup. Must be run in a transaction.
     * 
     * @param container
     *            The test container.
     * @param basicSetupRequired
     *            if <code>true</code>, roles, payment types and some supported
     *            countries will be created.
     * @param withSteppedPrices
     *            if <code>true</code>, stepped prices will be defined; base
     *            prices otherwise
     * @throws Exception
     */
    public static void setup(TestContainer container,
            boolean basicSetupRequired, boolean withSteppedPrices)
            throws Exception {
        container.login("admin");
        DataService dm = container.get(DataService.class);

        if (basicSetupRequired) {
            EJBTestBase.createOrganizationRoles(dm);
            EJBTestBase.createPaymentTypes(dm);
            SupportedCountries.createSomeSupportedCountries(dm);
        }
        LocalizerFacade lf = new LocalizerFacade(
                container.get(LocalizerServiceLocal.class), "en");

        SupportedCurrencies.createOneSupportedCurrency(dm);

        supplier = Organizations.createOrganization(dm,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        mp = Marketplaces.ensureMarketplace(supplier,
                supplier.getOrganizationId(), dm);
        LocalizedResources.localizeMarketPlace(dm, mp.getKey());

        udaDefCust1 = Udas.createUdaDefinition(dm, supplier,
                UdaTargetType.CUSTOMER, "UdaDefCust1", "UdaDefCust1_Default",
                UdaConfigurationType.SUPPLIER);
        udaDefCust2 = Udas.createUdaDefinition(dm, supplier,
                UdaTargetType.CUSTOMER, "UdaDefCust2", "UdaDefCust2_Default",
                UdaConfigurationType.SUPPLIER);
        udaDefSub1 = Udas.createUdaDefinition(dm, supplier,
                UdaTargetType.CUSTOMER_SUBSCRIPTION, "UdaDefSub1",
                "UdaDefSub1_Default", UdaConfigurationType.SUPPLIER);
        udaDefSub2 = Udas.createUdaDefinition(dm, supplier,
                UdaTargetType.CUSTOMER_SUBSCRIPTION, "UdaDefSub2",
                "UdaDefSub2_Default", UdaConfigurationType.SUPPLIER);
        supplierAdminUser = Organizations.createUserForOrg(dm, supplier, true,
                "admin");
        PlatformUsers.grantRoles(dm, supplierAdminUser,
                UserRoleType.SERVICE_MANAGER, UserRoleType.TECHNOLOGY_MANAGER);

        customer = Organizations.createCustomer(dm, supplier);
        customerUda1 = Udas.createUda(dm, customer, udaDefCust1,
                "UdaCust1_Value");
        customerUda2 = Udas.createUda(dm, customer, udaDefCust2,
                "UdaCust2_Value");
        customerAdminUser = Organizations.createUserForOrg(dm, customer, true,
                "admin");

        secondCustomer = Organizations.createCustomer(dm, supplier);
        secondCustomerAdminUser = Organizations.createUserForOrg(dm,
                secondCustomer, true, "admin");

        createTechnicalService(dm, "techProd");

        // create marketable product based on it, initially published on local
        // mp, with price model, having prices for all created events, params...
        product = Products.createProduct(supplier, tProd, true, "productId",
                "priceModelId", mp, dm);
        LocalizedResources.localizeProduct(dm, product.getKey());

        product.setStatus(ServiceStatus.INACTIVE);
        Products.createParameter(paramDefLong, product, dm);
        Products.createParameter(paramDefBool, product, dm);
        Products.createParameter(paramDefString, product, dm);
        Parameter enumParam = Products.createParameter(paramDefEnum, product,
                dm);
        enumParam.setValue(paramDefEnum.getOptionList().get(0).getOptionId());
        Products.createParameter(paramDefInt, product, dm);

        priceModel = product.getPriceModel();
        priceModel.setPeriod(PricingPeriod.HOUR);
        SupportedCurrency template = new SupportedCurrency();
        template.setCurrency(Currency.getInstance("EUR"));
        template = (SupportedCurrency) dm.getReferenceByBusinessKey(template);
        priceModel.setCurrency(template);
        priceModel.setOneTimeFee(new BigDecimal("12345.00"));
        priceModel.setPricePerPeriod(new BigDecimal("67890.00"));
        priceModel.setPricePerUserAssignment(new BigDecimal("34567.00"));

        // eventprices
        PricedEvent pricedEvent1 = Products.createPricedEvent(dm, event1,
                new BigDecimal("1111.00"), priceModel);
        Products.createPricedEvent(dm, event2, new BigDecimal("2222.00"),
                priceModel);
        // parameterprices & optionprices
        for (Parameter param : product.getParameterSet().getParameters()) {
            if (param.getParameterDefinition().getValueType() != ParameterValueType.STRING) {
                PricedParameter pricedParameter = Products
                        .createPricedParameter(new BigDecimal("111.00"),
                                new BigDecimal("111.00"), priceModel, param, dm);
                if (param.getParameterDefinition().getValueType() == ParameterValueType.ENUMERATION) {
                    for (ParameterOption option : param
                            .getParameterDefinition().getOptionList()) {
                        Products.createPricedOption(new BigDecimal("111.00"),
                                new BigDecimal("111.00"), pricedParameter,
                                option.getOptionId(), param
                                        .getParameterDefinition().getKey(), dm);
                    }
                }
            }
        }
        PricedParameter pricedParameter = priceModel.getSelectedParameters()
                .get(0);

        if (withSteppedPrices) {
            addSteppedPrices(priceModel, pricedEvent1, pricedParameter);
        }

        // roleprices for base user price
        PricedProductRole ppr = new PricedProductRole();
        ppr.setPriceModel(priceModel);
        ppr.setPricePerUser(new BigDecimal(1L));
        ppr.setRoleDefinition(roleDef1);
        dm.persist(ppr);
        priceModel.setRoleSpecificUserPrices(Collections.singletonList(ppr));

        // roleprice for parameter
        PricedProductRole paramPPR = new PricedProductRole();
        paramPPR.setPricedParameter(pricedParameter);
        paramPPR.setPricePerUser(new BigDecimal("2.00"));
        paramPPR.setRoleDefinition(roleDef2);
        dm.persist(paramPPR);
        pricedParameter.setRoleSpecificUserPrices(Collections
                .singletonList(paramPPR));

        // roleprice for parameter option
        PricedProductRole optionPPR = new PricedProductRole();
        PricedOption pricedOption = priceModel.getSelectedParameters().get(2)
                .getPricedOptionList().get(0);
        optionPPR.setPricedOption(pricedOption);
        optionPPR.setPricePerUser(new BigDecimal("3.00"));
        optionPPR.setRoleDefinition(roleDef1);
        dm.persist(optionPPR);
        pricedOption.setRoleSpecificUserPrices(Collections
                .singletonList(optionPPR));

        // finally subscribe to it.
        product.setStatus(ServiceStatus.ACTIVE);
        subscription = Subscriptions.createSubscription(dm,
                customer.getOrganizationId(), product.getProductId(), "subId",
                System.currentTimeMillis(), System.currentTimeMillis(),
                supplier, 1);

        Subscriptions.createUsageLicense(dm, customerAdminUser, subscription);

        subUda1 = Udas.createUda(dm, subscription, udaDefSub1, "UdaSub_Value1");
        subUda2 = Udas.createUda(dm, subscription, udaDefSub2, "UdaSub_Value2");

        product.setStatus(ServiceStatus.INACTIVE);
        voCustomer = OrganizationAssembler
                .toVOOrganization(customer, false, lf);
        voSecondCustomer = OrganizationAssembler.toVOOrganization(
                secondCustomer, false, lf);

        // create customer specific copy of the service
        String productId = product.getProductId();
        product.setProductId(productId + "CC");
        Product customerCopy = product.copyForCustomer(customer);
        dm.persist(customerCopy);
        product.setProductId(productId);
    }

    public static void addSteppedPrices(PriceModel priceModel,
            PricedEvent pricedEvent1, PricedParameter pricedParameter) {
        // stepped price for base price
        List<SteppedPrice> steppedPrices = new ArrayList<SteppedPrice>();
        SteppedPrice step1 = new SteppedPrice();
        step1.setLimit(new Long(50));
        step1.setPrice(new BigDecimal("50.00"));
        step1.setPriceModel(priceModel);
        steppedPrices.add(step1);
        SteppedPrice step2 = new SteppedPrice();
        step2.setLimit(new Long(450));
        step2.setPrice(new BigDecimal("60.00"));
        step2.setAdditionalPrice(new BigDecimal("2500.00"));
        step2.setPriceModel(priceModel);
        steppedPrices.add(step2);
        priceModel.setSteppedPrices(steppedPrices);
        priceModel.setPricePerUserAssignment(BigDecimal.ZERO);

        // stepped price for event
        List<SteppedPrice> steppedPricesForEvent = new ArrayList<SteppedPrice>();
        SteppedPrice evtStep1 = new SteppedPrice();
        evtStep1.setLimit(new Long(50));
        evtStep1.setPrice(new BigDecimal("51.00"));
        evtStep1.setPricedEvent(pricedEvent1);
        steppedPricesForEvent.add(evtStep1);
        SteppedPrice evtStep2 = new SteppedPrice();
        evtStep2.setLimit(new Long(450));
        evtStep2.setPrice(new BigDecimal("61.00"));
        evtStep2.setAdditionalPrice(new BigDecimal("2550.00"));
        evtStep2.setPricedEvent(pricedEvent1);
        steppedPricesForEvent.add(evtStep2);
        pricedEvent1.setSteppedPrices(steppedPricesForEvent);
        pricedEvent1.setEventPrice(BigDecimal.ZERO);

        // stepped price for numerical parameters
        List<SteppedPrice> steppedPricesForParameter = new ArrayList<SteppedPrice>();
        SteppedPrice paramStep1 = new SteppedPrice();
        paramStep1.setLimit(new Long(50));
        paramStep1.setPrice(new BigDecimal("52.00"));
        paramStep1.setPricedParameter(pricedParameter);
        steppedPricesForParameter.add(paramStep1);
        SteppedPrice paramStep2 = new SteppedPrice();
        paramStep2.setLimit(new Long(450));
        paramStep2.setPrice(new BigDecimal("62.00"));
        paramStep2.setAdditionalPrice(new BigDecimal("2600.00"));
        paramStep2.setPricedParameter(pricedParameter);
        steppedPricesForParameter.add(paramStep2);
        pricedParameter.setSteppedPrices(steppedPricesForParameter);
        pricedParameter.setPricePerSubscription(BigDecimal.ZERO);
    }

    /**
     * Creates a technical service with events, roledefinitions, parameter
     * definitions. The global variables will be updated accordingly, further
     * calls to their getters return the updated value.
     * 
     * @param dm
     *            The data manager reference.
     * @param tProdId
     *            The identifier of the technical service.
     * @return The created technical service.
     * @throws NonUniqueBusinessKeyException
     */
    public static TechnicalProduct createTechnicalService(DataService dm,
            String tProdId) throws NonUniqueBusinessKeyException {
        // create technical product with events, parameters, also
        // options and roles
        tProd = TechnicalProducts.createTechnicalProduct(dm, supplier, tProdId,
                false, ServiceAccessType.LOGIN);

        roleDef1 = TechnicalProducts.addRoleDefinition("Role1", tProd, dm);
        LocalizedResources.localizeRoleDefinition(dm, roleDef1.getKey());
        roleDef2 = TechnicalProducts.addRoleDefinition("Role2", tProd, dm);
        LocalizedResources.localizeRoleDefinition(dm, roleDef2.getKey());
        event1 = TechnicalProducts.addEvent("Event1", EventType.SERVICE_EVENT,
                tProd, dm);
        LocalizedResources.localizeEvent(dm, event1.getKey());
        event2 = TechnicalProducts.addEvent("Event2", EventType.SERVICE_EVENT,
                tProd, dm);
        LocalizedResources.localizeEvent(dm, event2.getKey());
        paramDefLong = TechnicalProducts.addParameterDefinition(
                ParameterValueType.LONG, "ParamLong",
                ParameterType.SERVICE_PARAMETER, tProd, dm, Long.valueOf(500),
                Long.valueOf(0), true);
        LocalizedResources.localizeParameterDef(dm, paramDefLong.getKey());
        paramDefBool = TechnicalProducts.addParameterDefinition(
                ParameterValueType.BOOLEAN, "ParamBool",
                ParameterType.SERVICE_PARAMETER, tProd, dm, null, null, true);
        LocalizedResources.localizeParameterDef(dm, paramDefBool.getKey());
        paramDefString = TechnicalProducts.addParameterDefinition(
                ParameterValueType.STRING, "ParamString",
                ParameterType.SERVICE_PARAMETER, tProd, dm, null, null, true);
        LocalizedResources.localizeParameterDef(dm, paramDefString.getKey());
        paramDefEnum = TechnicalProducts.addParameterDefinition(
                ParameterValueType.ENUMERATION, "ParamEnum",
                ParameterType.SERVICE_PARAMETER, tProd, dm, null, null, true);
        LocalizedResources.localizeParameterDef(dm, paramDefEnum.getKey());
        ParameterOption opt1 = TechnicalProducts.addParameterOption(
                paramDefEnum, "ParamOption1", dm);
        LocalizedResources.localizeParameterDefOption(dm, opt1.getKey());
        ParameterOption opt2 = TechnicalProducts.addParameterOption(
                paramDefEnum, "ParamOption2", dm);
        LocalizedResources.localizeParameterDefOption(dm, opt2.getKey());
        paramDefInt = TechnicalProducts.addParameterDefinition(
                ParameterValueType.INTEGER, "ParamInt",
                ParameterType.SERVICE_PARAMETER, tProd, dm, null, null, true);
        LocalizedResources.localizeParameterDef(dm, paramDefInt.getKey());

        TechnicalProducts.addTechnicalProductOperation(dm, tProd, "operation1",
                "http://actionhost.actionDomain/action");

        return tProd;
    }

    public static Organization getSupplier() {
        return supplier;
    }

    public static PlatformUser getSupplierAdminUser() {
        return supplierAdminUser;
    }

    public static Organization getCustomer() {
        return customer;
    }

    public static VOOrganization getVoCustomer() {
        return voCustomer;
    }

    public static TechnicalProduct getTechnicalProduct() {
        return tProd;
    }

    public static RoleDefinition getRoleDef1() {
        return roleDef1;
    }

    public static RoleDefinition getRoleDef2() {
        return roleDef2;
    }

    public static Event getEvent1() {
        return event1;
    }

    public static Event getEvent2() {
        return event2;
    }

    public static ParameterDefinition getParamDefLong() {
        return paramDefLong;
    }

    public static ParameterDefinition getParamDefBool() {
        return paramDefBool;
    }

    public static ParameterDefinition getParamDefString() {
        return paramDefString;
    }

    public static ParameterDefinition getParamDefEnum() {
        return paramDefEnum;
    }

    public static Product getProduct() {
        return product;
    }

    public static PriceModel getPriceModel() {
        return priceModel;
    }

    public static Subscription getSubscription() {
        return subscription;
    }

    public static Organization getSecondCustomer() {
        return secondCustomer;
    }

    public static VOOrganization getVoSecondCustomer() {
        return voSecondCustomer;
    }

    public static PlatformUser getCustomerAdminUser() {
        return customerAdminUser;
    }

    public static ParameterDefinition getParamDefInt() {
        return paramDefInt;
    }

    public static Uda getCustomerUda1() {
        return customerUda1;
    }

    public static Uda getCustomerUda2() {
        return customerUda2;
    }

    public static Uda getSubUda1() {
        return subUda1;
    }

    public static Uda getSubUda2() {
        return subUda2;
    }

    public static UdaDefinition getUdaDefSub1() {
        return udaDefSub1;
    }

    public static UdaDefinition getUdaDefSub2() {
        return udaDefSub2;
    }

    public static UdaDefinition getUdaDefCust1() {
        return udaDefCust1;
    }

    public static UdaDefinition getUdaDefCust2() {
        return udaDefCust2;
    }

    public static PlatformUser getSecondCustomerAdminUser() {
        return secondCustomerAdminUser;
    }

    public static Marketplace getLocalMarketplaceSupplier() {
        return mp;
    }

}
