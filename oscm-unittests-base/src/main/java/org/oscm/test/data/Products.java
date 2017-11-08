/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.data;

import static org.oscm.test.Numbers.TIMESTAMP;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.QueryBasedObjectFactory;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

public class Products {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static Product findProduct(DataService mgr,
            Organization organization, String id) {
        Product prod = new Product();
        prod.setVendor(organization);
        prod.setProductId(id);
        return (Product) mgr.find(prod);
    }

    public static void addPlatformParameter(Product product,
            String parameterId, boolean configurable, Long value,
            DataService mgr) throws NonUniqueBusinessKeyException {
        Query query = mgr
                .createNamedQuery("ParameterDefinition.getAllPlatformParameterDefinitions");
        query.setParameter("parameterType", ParameterType.PLATFORM_PARAMETER);
        List<ParameterDefinition> result = ParameterizedTypes.list(
                query.getResultList(), ParameterDefinition.class);

        ParameterDefinition paramDefNamedUser = null;
        ParameterDefinition paramDefConcurrentUser = null;
        ParameterDefinition paramDefPeriod = null;
        for (ParameterDefinition paramDef : result) {
            if (PlatformParameterIdentifiers.NAMED_USER.equals(paramDef
                    .getParameterId())) {
                paramDefNamedUser = paramDef;
            } else if (PlatformParameterIdentifiers.CONCURRENT_USER
                    .equals(paramDef.getParameterId())) {
                paramDefConcurrentUser = paramDef;
            } else if (PlatformParameterIdentifiers.PERIOD.equals(paramDef
                    .getParameterId())) {
                paramDefPeriod = paramDef;
            }
        }
        if (paramDefNamedUser == null) {
            paramDefNamedUser = createProductParameterDefinition(
                    PlatformParameterIdentifiers.NAMED_USER,
                    ParameterValueType.LONG, mgr);
        }
        if (paramDefConcurrentUser == null) {
            paramDefConcurrentUser = createProductParameterDefinition(
                    PlatformParameterIdentifiers.CONCURRENT_USER,
                    ParameterValueType.LONG, mgr);
        }
        if (paramDefPeriod == null) {
            paramDefPeriod = createProductParameterDefinition(
                    PlatformParameterIdentifiers.PERIOD,
                    ParameterValueType.DURATION, mgr);
        }

        Parameter param = new Parameter();
        param.setParameterSet(product.getParameterSet());
        if (value != null) {
            param.setValue(String.valueOf(value));
        }
        param.setConfigurable(configurable);
        if (PlatformParameterIdentifiers.NAMED_USER.equals(parameterId)) {
            param.setParameterDefinition(paramDefNamedUser);
        } else if (PlatformParameterIdentifiers.CONCURRENT_USER
                .equals(parameterId)) {
            param.setParameterDefinition(paramDefConcurrentUser);
        } else if (PlatformParameterIdentifiers.PERIOD.equals(parameterId)) {
            param.setParameterDefinition(paramDefPeriod);
        }
        if (param.getParameterDefinition() != null) {
            product.getParameterSet().getParameters().add(param);
            mgr.persist(param);
        }
    }

    public static Product createProduct(Organization supplier,
            TechnicalProduct tProd, boolean chargeable, String productId,
            String priceModelId, DataService mgr)
            throws NonUniqueBusinessKeyException {
        return createProduct(supplier, tProd, chargeable, productId,
                priceModelId, null, mgr);

    }

    public static Product createProduct(Organization supplier,
            String techProdId, boolean chargeable, String productId,
            String priceModelId, DataService mgr)
            throws NonUniqueBusinessKeyException {
        TechnicalProduct tProd = TechnicalProducts
                .findOrCreateTechnicalProduct(mgr, supplier, techProdId,
                        ServiceAccessType.LOGIN);
        return createProduct(supplier, tProd, chargeable, productId,
                priceModelId, null, mgr);
    }

    public static Product createProduct(Organization supplier,
            TechnicalProduct tProd, boolean chargeable, String productId,
            String priceModelId, Marketplace marketplace, DataService mgr)
            throws NonUniqueBusinessKeyException {

        return createProduct(supplier, tProd, chargeable, productId,
                priceModelId, marketplace, mgr, false);
    }

    public static Product createProduct(Organization supplier,
            TechnicalProduct tProd, boolean chargeable, String productId,
            String priceModelId, Marketplace marketplace, DataService mgr,
            boolean isPartnerPriceModelCopied)
            throws NonUniqueBusinessKeyException {
        return createProduct(supplier, tProd, chargeable, productId,
                priceModelId, marketplace, mgr, isPartnerPriceModelCopied,
                false);
    }

    public static Product createProduct(Organization supplier,
            TechnicalProduct tProd, boolean chargeable, String productId,
            String priceModelId, Marketplace marketplace, DataService mgr,
            boolean isPartnerPriceModelCopied, boolean isSubscriptionCopy)
            throws NonUniqueBusinessKeyException {

        supplier = (Organization) mgr.find(supplier);
        Product prod = createProductWithoutPriceModel(supplier, tProd,
                productId);
        prod.setStatus(ServiceStatus.ACTIVE);

        PriceModel pm = new PriceModel();
        if(tProd.isExternalBilling()){
            pm.setType(PriceModelType.UNKNOWN);
            pm.setExternal(true);
        }
        else if (chargeable) {
            pm.setType(PriceModelType.PRO_RATA);
            pm.setPeriod(PricingPeriod.DAY);
            pm.setPricePerPeriod(new BigDecimal(1));
            SupportedCurrency sc = new SupportedCurrency();
            sc.setCurrency(Currency.getInstance("EUR"));
            sc = (SupportedCurrency) mgr.find(sc);
            pm.setCurrency(sc);
            prod.setPriceModel(pm);
        } else {
            pm.setType(PriceModelType.FREE_OF_CHARGE);
        }
        prod.setPriceModel(pm);

        if (isPartnerPriceModelCopied) {
            prod.setType(ServiceType.PARTNER_TEMPLATE);
        } else if (isSubscriptionCopy) {
            prod.setType(ServiceType.SUBSCRIPTION);
        } else {
            prod.setType(ServiceType.TEMPLATE);
            prod.setConfiguratorUrl("http://www.configUrl.de");
        }

        prod.setAutoAssignUserEnabled(isPartnerPriceModelCopied ? null
                : Boolean.FALSE);

        mgr.persist(prod);

        if (!isSubscriptionCopy) {
            CatalogEntry entry = QueryBasedObjectFactory.createCatalogEntry(
                    prod, marketplace);
            if (marketplace != null) {
                entry.setVisibleInCatalog(true);
                entry.setAnonymousVisible(true);
                if (isPartnerPriceModelCopied) {
                    entry.setBrokerPriceModel(createCopyOfRevenueShare(mgr,
                            marketplace.getBrokerPriceModel()));
                    entry.setResellerPriceModel(createCopyOfRevenueShare(mgr,
                            marketplace.getResellerPriceModel()));
                }
            }

            if (prod.getType().equals(ServiceType.TEMPLATE)
                    && supplier.getOperatorPriceModel() != null) {
                RevenueShareModel operatorPriceModelCopy = supplier
                        .getOperatorPriceModel().copy();
                entry.setOperatorPriceModel(operatorPriceModelCopy);
                mgr.persist(operatorPriceModelCopy);
            }

            mgr.persist(entry);
        }

        if (priceModelId != null) {
            LocalizedResource res2 = new LocalizedResource();
            res2.setLocale("en");
            res2.setObjectKey(pm.getKey());
            res2.setObjectType(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
            res2.setValue(priceModelId);
            mgr.persist(res2);
        }
        mgr.flush();

        try {
            prod = mgr.getReference(Product.class, prod.getKey());
        } catch (ObjectNotFoundException ex) {
            // must not happen
            ex.printStackTrace();
        }

        return prod;
    }

    public static Product setStatusForProduct(DataService mgr, Product product,
            ServiceStatus status) throws ObjectNotFoundException,
            NonUniqueBusinessKeyException {
        Product prod = mgr.getReference(Product.class, product.getKey());
        prod.setStatus(status);
        if (status == ServiceStatus.DELETED) {
            prod.setProductId(prod.getProductId().concat("#"));
        }
        mgr.persist(prod);
        return prod;
    }

    private static RevenueShareModel createCopyOfRevenueShare(DataService mgr,
            RevenueShareModel revenueShare)
            throws NonUniqueBusinessKeyException {
        if (revenueShare == null) {
            return null;
        }
        RevenueShareModel model = revenueShare.copy();
        mgr.persist(model);
        return model;
    }

    public static Product createProductWithoutPriceModel(Organization supplier,
            TechnicalProduct tProd, String productId) {
        Product prod = new Product();
        prod.setVendor(supplier);
        prod.setProductId(productId);
        prod.setTechnicalProduct(tProd);
        prod.setProvisioningDate(TIMESTAMP);
        prod.setStatus(ServiceStatus.INACTIVE);
        prod.setType(supplier.hasRole(OrganizationRoleType.SUPPLIER) ? ServiceType.TEMPLATE
                : ServiceType.PARTNER_TEMPLATE);
        prod.setAutoAssignUserEnabled(supplier
                .hasRole(OrganizationRoleType.SUPPLIER) ? Boolean.FALSE : null);
        ParameterSet paramSet = new ParameterSet();
        prod.setParameterSet(paramSet);

        return prod;
    }

    public static Product createProduct(String organizationId,
            String productId, String techProductId, DataService mgr)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        return createProduct(organizationId, productId, techProductId, mgr,
                ServiceAccessType.LOGIN);
    }

    public static Product createProduct(String organizationId,
            String technicalProviderId, String productId, String techProductId,
            DataService mgr, ServiceAccessType accessType)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {

        Organization supplier = Organizations.findOrganization(mgr,
                organizationId);
        if (supplier == null) {
            supplier = Organizations.createOrganization(mgr,
                    OrganizationRoleType.TECHNOLOGY_PROVIDER,
                    OrganizationRoleType.SUPPLIER);
            supplier.setOrganizationId(organizationId);
            mgr.flush();
        }

        Organization provider = Organizations.findOrganization(mgr,
                technicalProviderId);
        if (provider == null) {
            provider = Organizations.createOrganization(mgr,
                    OrganizationRoleType.TECHNOLOGY_PROVIDER,
                    OrganizationRoleType.SUPPLIER);
            provider.setOrganizationId(technicalProviderId);
            mgr.flush();
        }

        TechnicalProduct tp = TechnicalProducts.findTechnicalProduct(mgr,
                provider, techProductId);
        if (tp == null) {
            tp = TechnicalProducts.createTechnicalProduct(mgr, supplier,
                    techProductId, false, accessType);
        }

        Product product = Products.createProductWithoutPriceModel(supplier, tp,
                productId);
        mgr.persist(product);
        return product;
    }

    public static Product createProduct(String organizationId,
            String productId, String techProductId, DataService mgr,
            ServiceAccessType accessType) throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {

        Organization supplier = Organizations.findOrganization(mgr,
                organizationId);
        if (supplier == null) {
            supplier = Organizations.createOrganization(mgr,
                    OrganizationRoleType.TECHNOLOGY_PROVIDER,
                    OrganizationRoleType.SUPPLIER);
            supplier.setOrganizationId(organizationId);
            mgr.flush();
        }
        TechnicalProduct tp = TechnicalProducts.findTechnicalProduct(mgr,
                supplier, techProductId);
        if (tp == null) {
            tp = TechnicalProducts.createTechnicalProduct(mgr, supplier,
                    techProductId, false, accessType);
        }
        Product product = Products.createProductWithoutPriceModel(supplier, tp,
                productId);
        product.setType(ServiceType.TEMPLATE);
        mgr.persist(product);
        return product;
    }

    /**
     * Create a resale copy of a product and link it to the given vendor. A
     * corresponding CatalogEntry is NOT created.
     * 
     * @param productTemplate
     * @param vendor
     * @param mgr
     * @return
     * @throws NonUniqueBusinessKeyException
     */
    public static Product createProductResaleCopy(Product productTemplate,
            Organization vendor, DataService mgr)
            throws NonUniqueBusinessKeyException {
        Product resaleCopy = createProductResaleCopy(productTemplate, vendor,
                null, mgr);
        return resaleCopy;
    }

    /**
     * Create a resale copy of a product and link it to the given vendor. A
     * corresponding CatalogEntry is created only if marketplace given.
     * 
     * @param productTemplate
     * @param vendor
     * @param marketplace
     * @param mgr
     * @return
     * @throws NonUniqueBusinessKeyException
     */
    public static Product createProductResaleCopy(Product productTemplate,
            Organization vendor, Marketplace marketplace, DataService mgr)
            throws NonUniqueBusinessKeyException {
        Product resaleCopy = productTemplate.copyForResale(vendor);
        mgr.persist(resaleCopy);

        CatalogEntry resaleCatalogEntry = QueryBasedObjectFactory
                .createCatalogEntry(resaleCopy, marketplace);

        if (marketplace != null) {
            resaleCatalogEntry.setVisibleInCatalog(true);
            resaleCatalogEntry.setAnonymousVisible(true);
        }

        mgr.persist(resaleCatalogEntry);

        return resaleCopy;
    }

    public static ParameterDefinition createProductParameterDefinition(
            String parameterId, ParameterValueType parameterValueType,
            DataService mgr) throws NonUniqueBusinessKeyException {
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setParameterType(ParameterType.PLATFORM_PARAMETER);
        paramDef.setParameterId(parameterId);
        paramDef.setConfigurable(false);
        paramDef.setMandatory(false);
        paramDef.setValueType(parameterValueType);
        mgr.persist(paramDef);
        return paramDef;
    }

    /**
     * Create parameter option - definition of possible parameter option.
     * 
     * @param optionId
     * @param parameterDefinition
     * @param dataManager
     * @return
     * @throws NonUniqueBusinessKeyException
     */
    public static ParameterOption createParameterOption(String optionId,
            ParameterDefinition parameterDefinition, DataService dataManager)
            throws NonUniqueBusinessKeyException {
        ParameterOption parameterOption = new ParameterOption();

        parameterOption.setOptionId(optionId);
        parameterOption.setParameterDefinition(parameterDefinition);
        dataManager.persist(parameterOption);
        dataManager.flush();

        return parameterOption;
    }

    /**
     * Create priced parameter.
     * 
     * @param pricePerUser
     * @param pricePerSubscription
     * @param priceModel
     * @param parameter
     * @param dataManager
     * @return
     * @throws NonUniqueBusinessKeyException
     */
    public static PricedParameter createPricedParameter(
            final BigDecimal pricePerUser,
            final BigDecimal pricePerSubscription, final PriceModel priceModel,
            final Parameter parameter, DataService dataManager)
            throws NonUniqueBusinessKeyException {

        if (parameter.getParameterDefinition().getValueType() == ParameterValueType.STRING) {
            throw new RuntimeException(
                    "No priced parameter must be based on a type string parameter.");
        }
        PricedParameter pricedParameter = new PricedParameter();
        pricedParameter.setPricePerUser(pricePerUser);
        pricedParameter.setPricePerSubscription(pricePerSubscription);
        pricedParameter.setPriceModel(priceModel);
        pricedParameter.setParameter(parameter);

        dataManager.persist(pricedParameter);
        dataManager.flush();
        priceModel.getSelectedParameters().add(pricedParameter);

        return pricedParameter;
    }

    public static PricedEvent createPricedEvent(DataService dm, Event event,
            BigDecimal eventPrice, PriceModel priceModel)
            throws NonUniqueBusinessKeyException {
        PricedEvent pEvent = new PricedEvent();
        pEvent.setEvent(event);
        pEvent.setEventPrice(eventPrice);
        pEvent.setPriceModel(priceModel);

        dm.persist(pEvent);
        priceModel.getConsideredEvents().add(pEvent);

        return pEvent;
    }

    /**
     * Create parameter.
     * 
     * @param platformParameterIdentifier
     * @param parameterValue
     * @param dataManager
     * @return
     * @throws NonUniqueBusinessKeyException
     */
    public static Parameter createParameter(
            final String platformParameterIdentifier,
            final String parameterValue, DataService dataManager)
            throws NonUniqueBusinessKeyException {

        String tmpPlatformParameterIdentifier = platformParameterIdentifier;
        String tmpParameterValue = parameterValue;

        Query query = dataManager
                .createQuery("select c from ParameterDefinition c where c.dataContainer.parameterId=:parameterId");
        query.setParameter("parameterId", tmpPlatformParameterIdentifier);

        final List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();

        Iterator<ParameterDefinition> parameterDefinitionIterator = ParameterizedTypes
                .iterator(query.getResultList(), ParameterDefinition.class);

        while (parameterDefinitionIterator.hasNext()) {
            parameterDefinitions.add(parameterDefinitionIterator.next());
        }

        Parameter parameter = new Parameter();
        parameter.setParameterDefinition(parameterDefinitions.get(0));
        parameter.setValue(tmpParameterValue);

        // parameter set only for creating billing tests, not needed for
        // parameter further
        query = dataManager.createQuery("select c from ParameterSet c");

        final List<ParameterSet> parameterSetArray = new ArrayList<ParameterSet>();
        Iterator<ParameterSet> parameterSetIterator = ParameterizedTypes
                .iterator(query.getResultList(), ParameterSet.class);
        while (parameterSetIterator.hasNext()) {
            parameterSetArray.add(parameterSetIterator.next());
        }

        parameter.setParameterSet(parameterSetArray.get(0));
        dataManager.persist(parameter);
        dataManager.flush();

        return parameter;
    }

    /**
     * Create priced parameter.
     * 
     * @param pricePerUser
     * @param pricePerSubscription
     * @param pricedParameter
     * @return
     * @throws NonUniqueBusinessKeyException
     */
    public static PricedOption createPricedOption(BigDecimal pricePerUser,
            BigDecimal pricePerSubscription,
            final PricedParameter pricedParameter, String optionId,
            long parameterdefinition_tkey, DataService dataManager)
            throws NonUniqueBusinessKeyException {

        PricedOption pricedOption = new PricedOption();

        pricedOption.setPricedParameter(pricedParameter);

        Query query = dataManager
                .createQuery("select c from ParameterOption c where optionId=:optionId and parameterdefinition_tkey=:parameterdefinition_tkey");
        query.setParameter("optionId", optionId);
        query.setParameter("parameterdefinition_tkey",
                Long.valueOf(parameterdefinition_tkey));

        final List<ParameterOption> parameterOptionArray = new ArrayList<ParameterOption>();
        Iterator<ParameterOption> parameterOptionIterator = ParameterizedTypes
                .iterator(query.getResultList(), ParameterOption.class);
        while (parameterOptionIterator.hasNext()) {
            parameterOptionArray.add(parameterOptionIterator.next());
        }
        final long parameterOptionKey = parameterOptionArray.get(0).getKey();
        pricedOption.setParameterOptionKey(parameterOptionKey);

        pricedOption.setPricePerUser(pricePerUser);
        pricedOption.setPricePerSubscription(pricePerSubscription);
        dataManager.persist(pricedOption);
        dataManager.flush();
        pricedParameter.getPricedOptionList().add(pricedOption);

        return pricedOption;
    }

    public static Parameter createParameter(ParameterDefinition paramDef,
            Product product, DataService dm) throws Exception {
        String value = null;
        if (paramDef.getValueType() == ParameterValueType.BOOLEAN) {
            value = "false";
        } else if (paramDef.getValueType() != ParameterValueType.ENUMERATION) {
            value = "123";
        }
        return createParameter(paramDef, product, dm, value);
    }

    public static Parameter createParameter(ParameterDefinition paramDef,
            Product product, DataService dm, String value) throws Exception {
        Parameter param = new Parameter();
        param.setValue(value);
        param.setParameterSet(product.getParameterSet());
        if (!paramDef.isConfigurable()) {
            throw new Exception(
                    "Must not create a parameter for a non configurable parameter definition!");
        }
        param.setParameterDefinition(paramDef);
        param.setConfigurable(true);
        dm.persist(param);
        dm.flush();
        product.getParameterSet().getParameters().add(param);
        return param;
    }

    public static Product createCustomerSpecifcProduct(DataService mgr,
            Organization org, Product product, ServiceStatus status)
            throws NonUniqueBusinessKeyException {
        Product find = mgr.find(Product.class, product.getKey());
        Product copy = find.copyForCustomer(org);
        copy.setStatus(status);
        mgr.persist(copy);
        return copy;
    }

    public static List<Product> createTestData(DataService mgr,
            Organization org, int numberOfProducts) throws Exception {
        List<Product> result = new ArrayList<Product>();
        org = (Organization) mgr.find(org);
        SupportedCurrencies.createOneSupportedCurrency(mgr);
        List<TechnicalProduct> technicalProducts = TechnicalProducts
                .createTestData(mgr, org, numberOfProducts);
        for (int i = 0; i < numberOfProducts; i++) {
            TechnicalProduct tProd = technicalProducts.get(i);
            Product product = Products.createProduct(org, tProd, true,
                    "productId_" + i, "priceModelId", mgr);
            LocalizedResources.localizeProduct(mgr, product.getKey());

            List<ParameterDefinition> paramDefinitions = tProd
                    .getParameterDefinitions();
            for (ParameterDefinition parameterDefinition : paramDefinitions) {
                Products.createParameter(parameterDefinition, product, mgr);
            }
            PriceModel priceModel = product.getPriceModel();
            priceModel.setPeriod(PricingPeriod.HOUR);
            SupportedCurrency template = new SupportedCurrency();
            template.setCurrency(Currency.getInstance("EUR"));
            template = (SupportedCurrency) mgr
                    .getReferenceByBusinessKey(template);
            priceModel.setCurrency(template);
            priceModel.setOneTimeFee(new BigDecimal("12345.00"));
            priceModel.setPricePerPeriod(new BigDecimal("67890.00"));
            priceModel.setPricePerUserAssignment(new BigDecimal("34567.00"));

            // eventprices
            Event event1 = tProd.getEvents().get(0);
            PricedEvent pricedEvent1 = Products.createPricedEvent(mgr, event1,
                    new BigDecimal("1111.00"), priceModel);
            Event event2 = tProd.getEvents().get(1);
            Products.createPricedEvent(mgr, event2, new BigDecimal("2222.00"),
                    priceModel);

            // parameterprices & optionprices
            for (Parameter param : product.getParameterSet().getParameters()) {
                if (param.getParameterDefinition().getValueType() != ParameterValueType.STRING) {
                    PricedParameter pricedParameter = Products
                            .createPricedParameter(new BigDecimal("111.00"),
                                    new BigDecimal("111.00"), priceModel,
                                    param, mgr);
                    if (param.getParameterDefinition().getValueType() == ParameterValueType.ENUMERATION) {
                        for (ParameterOption option : param
                                .getParameterDefinition().getOptionList()) {
                            Products.createPricedOption(
                                    new BigDecimal("111.00"), new BigDecimal(
                                            "111.00"), pricedParameter, option
                                            .getOptionId(), param
                                            .getParameterDefinition().getKey(),
                                    mgr);
                        }
                    }
                }
            }

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

            // stepped price for numerical parameters
            List<SteppedPrice> steppedPricesForParameter = new ArrayList<SteppedPrice>();
            PricedParameter pricedParameter = priceModel
                    .getSelectedParameters().get(0);
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

            // roleprices for base user price
            RoleDefinition roleDef1 = tProd.getRoleDefinitions().get(0);
            PricedProductRole ppr = new PricedProductRole();
            ppr.setPriceModel(priceModel);
            ppr.setPricePerUser(new BigDecimal(1L));
            ppr.setRoleDefinition(roleDef1);
            mgr.persist(ppr);
            priceModel
                    .setRoleSpecificUserPrices(Collections.singletonList(ppr));

            // roleprice for parameter
            RoleDefinition roleDef2 = tProd.getRoleDefinitions().get(1);
            PricedProductRole paramPPR = new PricedProductRole();
            paramPPR.setPricedParameter(pricedParameter);
            paramPPR.setPricePerUser(new BigDecimal("2.00"));
            paramPPR.setRoleDefinition(roleDef2);
            mgr.persist(paramPPR);
            pricedParameter.setRoleSpecificUserPrices(Collections
                    .singletonList(paramPPR));

            // roleprice for parameter option
            PricedProductRole optionPPR = new PricedProductRole();
            PricedOption pricedOption = priceModel.getSelectedParameters()
                    .get(2).getPricedOptionList().get(0);
            optionPPR.setPricedOption(pricedOption);
            optionPPR.setPricePerUser(new BigDecimal("3.00"));
            optionPPR.setRoleDefinition(roleDef1);
            mgr.persist(optionPPR);
            pricedOption.setRoleSpecificUserPrices(Collections
                    .singletonList(optionPPR));

            product.setStatus(ServiceStatus.ACTIVE);
            result.add(product);
        }
        return result;
    }

    public static ProductHistory createProductHistory(final DataService ds,
            final long technicalProductObjKey, final long prdObjKey,
            final long sellerKey, final String modificationDate,
            final int version, final ModificationType modificationType)
            throws Exception {

        return createProductHistory(ds, technicalProductObjKey, prdObjKey, 0,
                sellerKey, modificationDate, version, modificationType);
    }

    public static ProductHistory createProductHistory(final DataService ds,
            final long technicalProductObjKey, final long prdObjKey,
            final long priceModelObjKey, final long sellerKey,
            final String modificationDate, final int version,
            final ModificationType modificationType) throws Exception {
        ProductHistory prdHist = new ProductHistory();

        prdHist.setInvocationDate(new Date());
        prdHist.setObjKey(prdObjKey);
        prdHist.setObjVersion(version);
        prdHist.setModdate(new SimpleDateFormat(DATE_PATTERN)
                .parse(modificationDate));
        prdHist.setModtype(modificationType);
        prdHist.setModuser("moduser");

        prdHist.getDataContainer().setProductId("productId");
        prdHist.getDataContainer().setProvisioningDate(0);
        prdHist.getDataContainer().setStatus(ServiceStatus.ACTIVE);
        prdHist.setVendorObjKey(sellerKey);
        prdHist.setTechnicalProductObjKey(technicalProductObjKey);
        prdHist.setPriceModelObjKey(Long.valueOf(priceModelObjKey));
        prdHist.getDataContainer().setType(ServiceType.SUBSCRIPTION);

        ds.persist(prdHist);
        return prdHist;
    }

    public static ProductHistory createProductHistory(DataService ds,
            long objKey, Long pmObjKey) throws Exception {
        ProductHistory prd = new ProductHistory();
        prd.setObjKey(objKey);
        prd.setInvocationDate(new Date());
        prd.setPriceModelObjKey(pmObjKey);
        prd.getDataContainer().setProductId("productId");
        prd.getDataContainer().setStatus(ServiceStatus.ACTIVE);
        prd.setModdate(new Date());
        prd.setModtype(ModificationType.ADD);
        prd.setModuser("moduser");
        prd.getDataContainer().setType(ServiceType.TEMPLATE);
        ds.persist(prd);
        return prd;
    }

    public static ProductHistory createProductHistory(DataService ds,
            ModificationType modificationType, long modDate, long objKey,
            long objVersion, ServiceType type, String productId,
            Long templateObjKey) throws Exception {
        ProductHistory prd = new ProductHistory();
        prd.setObjKey(objKey);
        prd.setObjVersion(objVersion);
        prd.setInvocationDate(new Date());
        prd.setTemplateObjKey(templateObjKey);
        prd.getDataContainer().setProductId(productId);
        prd.getDataContainer().setStatus(ServiceStatus.ACTIVE);
        prd.setModdate(new Date(modDate));
        prd.setModtype(modificationType);
        prd.setModuser("moduser");
        prd.getDataContainer().setType(type);
        ds.persist(prd);
        return prd;
    }

}
