/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: G&uuml;nther Schmid                                                      
 *                                                                              
 *  Creation Date: 24.02.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.domobjects.Event;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.interceptor.DateFactory;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCompatibleService;
import org.oscm.internal.vo.VOCustomerService;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOServiceEntry;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;

/**
 * @author G&uuml;nther Schmid
 * 
 */
public class ProductAssembler extends BaseAssembler {

    public static final String FIELD_NAME_NAME = "name";

    public static final String FIELD_NAME_SERVICE_ID = "serviceId";
    public static final String FIELD_NAME_CONFIGURATOR_URL = "configuratorUrl";
    public static final String FIELD_NAME_CUSTOM_TAB_URL = "customTabUrl";

    /**
     * Sets the key and identifier in the transfer object
     */
    private static void fillIdentifyingFields(final Product product,
            final VOService voProduct) {
        voProduct.setServiceId(getProductId(product));
    }

    /**
     * Returns the identifier of the template, if the product is just a copy
     * 
     * @param product
     *            the product to get the identifier for
     * @return the identifier
     */
    public static String getProductId(final Product product) {
        Product p = product;
        if (ServiceType.isSubscription(p.getType())) {
            p = p.getTemplate();
        }
        if (p.getType() == ServiceType.PARTNER_TEMPLATE
                || p.getType() == ServiceType.CUSTOMER_TEMPLATE) {
            p = p.getTemplate();
        }
        return p.getProductId();

    }

    /**
     * Sets the most important attributes in the transfer object
     * <code>voProduct</code>. Usually, these fields are required for listings.
     */
    private static void fillBaseFields(final Product product,
            final LocalizerFacade facade, final VOService voProduct,
            PerformanceHint scope) {
        fillIdentifyingFields(product, voProduct);
        voProduct.setAutoAssignUserEnabled(product.isAutoAssignUserEnabled());
        voProduct.setStatus(product.getStatus());
        voProduct.setSellerKey(product.getVendorKey());

        // The product feedback is always stored for the original product
        // template, not for a customer specific-, partner- or subscription
        // product copy!
        ProductFeedback feedback = product.getProductTemplate()
                .getProductFeedback();
        if (feedback != null) {
            voProduct.setAverageRating(feedback.getAverageRating());
            voProduct.setNumberOfReviews(feedback.getProductReviews().size());
        } else {
            voProduct.setAverageRating(BigDecimal.ZERO);
        }

        final Organization supplier = getSupplier(product);
        if (supplier != null) {
            if (supplier.getGrantedRoleTypes().contains(
                    OrganizationRoleType.SUPPLIER)) {
                voProduct.setOfferingType(OfferingType.DIRECT);
            } else if (supplier.getGrantedRoleTypes().contains(
                    OrganizationRoleType.BROKER)) {
                voProduct.setOfferingType(OfferingType.BROKER);
            } else if (supplier.getGrantedRoleTypes().contains(
                    OrganizationRoleType.RESELLER)) {
                voProduct.setOfferingType(OfferingType.RESELLER);
            }
            voProduct.setSellerId(supplier.getOrganizationId());
            voProduct.setSellerName(supplier.getName() != null
                    && supplier.getName().trim().length() > 0 ? supplier
                    .getName() : supplier.getOrganizationId());
        }

        long key = getKeyForLocalizedResource(product);
        String name = getServiceName(product, facade);
        voProduct.setName(name);

        String shortDescription = facade.getText(key,
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION);
        voProduct.setShortDescription(shortDescription);

        PriceModel pm;
        if (ServiceType.isTemplate(product.getType())
                && supplier != null
                && (supplier.getGrantedRoleTypes().contains(
                        OrganizationRoleType.RESELLER) || supplier
                        .getGrantedRoleTypes().contains(
                                OrganizationRoleType.BROKER))
                && product.getTemplate() != null) {
            pm = product.getTemplate().getPriceModel();
        } else {
            pm = product.getPriceModel();
        }

        VOPriceModel voPM = PriceModelAssembler.toVOPriceModel(pm, facade,
                scope);
        if (ServiceType.isTemplate(product.getType())
                && voPM != null
                && supplier != null
                && supplier.getGrantedRoleTypes().contains(
                        OrganizationRoleType.RESELLER)) {
            final String license = facade.getText(product.getKey(),
                    LocalizedObjectTypes.RESELLER_PRICEMODEL_LICENSE);
            voPM.setLicense(license);
        }
        voProduct.setPriceModel(voPM);

        TechnicalProduct technicalProduct = product.getTechnicalProduct();
        voProduct.setTechnicalId(technicalProduct.getTechnicalProductId());
        voProduct.setTags(TagAssembler.toStrings(technicalProduct.getTags(),
                facade.getLocale()));

        // Bug #8488 this information is required in lists to allow redirect to
        // EXTERNAL ACCESS services
        voProduct.setAccessType(technicalProduct.getAccessType());
        voProduct.setBaseURL(technicalProduct.getBaseURL());
        voProduct.setBillingIdentifier(technicalProduct.getBillingIdentifier());
    }

    private static Organization getSupplier(final Product product) {
        final Organization supplier;
        if (ServiceType.isSubscription(product.getType())) {
            supplier = product.getTemplate().getVendor();
        } else {
            supplier = product.getVendor();
        }
        return supplier;
    }

    private static long getKeyForLocalizedResource(Product product) {
        long key = product.getKey();
        Organization supplier = getSupplier(product);
        // TODO: customer specific copies should also use the localized
        // resources of the template
        if (supplier != null) {
            Set<OrganizationRoleType> roles = supplier.getGrantedRoleTypes();
            if (roles.contains(OrganizationRoleType.BROKER)
                    || roles.contains(OrganizationRoleType.RESELLER)) {
                key = product.getTemplate().getKey();
            }
        }
        return key;
    }

    public static String getServiceName(Product product, LocalizerFacade facade) {
        String name;
        if (ServiceType.isPartnerSubscription(product.getType())
                && product.getTemplate() != null) {
            name = facade.getText(product.getTemplate().getKey(),
                    LocalizedObjectTypes.PRODUCT_MARKETING_NAME);
        } else {
            name = facade.getText(getKeyForLocalizedResource(product),
                    LocalizedObjectTypes.PRODUCT_MARKETING_NAME);
        }
        return name;
    }

    /**
     * Sets all attributes in the transfer object. Warning: a lot of data must
     * be loaded from the database. This will result in slow performance, if
     * called for multiple products.
     */
    protected static void fillAllFields(final Product product,
            final LocalizerFacade facade, final VOService voProduct,
            PerformanceHint scope) {
        fillBaseFields(product, facade, voProduct, scope);

        voProduct.setConfiguratorUrl(product.getProductTemplate()
                .getConfiguratorUrl());
        voProduct.setCustomTabUrl(product.getProductTemplate().getCustomTabUrl());

        long key = product.getKey();

        final Organization vendor = getSupplier(product);
        if (vendor != null
                && !vendor.getGrantedRoleTypes().contains(
                        OrganizationRoleType.SUPPLIER)) {
            key = product.getTemplateOrSelf().getKey();
        }

        String marketingDescription = facade.getText(key,
                LocalizedObjectTypes.PRODUCT_MARKETING_DESC);
        voProduct.setDescription(marketingDescription);

        String customTabName = facade.getText(key,
                LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME);
        voProduct.setCustomTabName(customTabName);

        final ParameterSet parameterSet;
        if (product.getType() == ServiceType.PARTNER_TEMPLATE) {
            parameterSet = product.getTemplate().getParameterSet();
        } else {
            parameterSet = product.getParameterSet();
        }
        voProduct.setParameters(ParameterAssembler.toVOParameters(parameterSet,
                facade));

        TechnicalProduct technicalProduct = product.getTechnicalProduct();
        if (voProduct.getPriceModel() == null) {
            VOPriceModel voPM = new VOPriceModel();
            String licenseDescription = facade.getText(
                    technicalProduct.getKey(),
                    LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
            voPM.setLicense(licenseDescription);
            voProduct.setPriceModel(voPM);
        }
        voProduct.setServiceType(product.getType());
    }

    /**
     * Preloads all data required to construct a transfer object. Preloading
     * increases performance, by replacing many small SQL requests with one
     * large request. Subsequent DB request will be avoided because of the
     * internal caches in Hibernate and LocalizerFacade.
     */
    public static void prefetchData(List<Product> products,
            LocalizerFacade facade, PerformanceHint scope) {
        if (scope == PerformanceHint.ONLY_FIELDS_FOR_LISTINGS) {
            List<Long> objectKeys = new ArrayList<Long>();
            for (Product product : products) {
                objectKeys.add(Long.valueOf(product.getTemplateOrSelf()
                        .getKey()));
            }
            facade.prefetch(
                    objectKeys,
                    Arrays.asList(new LocalizedObjectTypes[] {
                            LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                            LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION }));
        }
    }

    /**
     * Copies all attributes in Product object according to the values specified
     * in the value object. Sets the necessary attributes to create a new
     * template product.
     * 
     * @param voProduct
     *            The value object containing the values to be set.
     * @param tProd
     *            Technical product on which this service is based on.
     * @param organization
     *            Organization which this service belongs to.
     * @return The newly created product object.
     */
    public static Product toNewTemplateProduct(final VOService voProduct,
            final TechnicalProduct tProd, final Organization organization)
            throws ValidationException {
        Product product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setStatus(ServiceStatus.INACTIVE);
        product.setProvisioningDate(DateFactory.getInstance()
                .getTransactionTime());
        product.setVendor(organization);
        product.setTechnicalProduct(tProd);
        if (tProd.getAccessType() == ServiceAccessType.EXTERNAL) {
            final PriceModel pm = new PriceModel();
            pm.setType(PriceModelType.FREE_OF_CHARGE);
            product.setPriceModel(pm);
        }
        copyAttributes(product, voProduct);
        return product;
    }

    public static VOService toVOProduct(final Product product,
            final LocalizerFacade facade) {
        return toVOProduct(product, facade, PerformanceHint.ALL_FIELDS);
    }

    public static VOService toVOProduct(final Product product,
            final LocalizerFacade facade, PerformanceHint scope) {

        VOService voProduct = new VOService();
        switch (scope) {
        case ONLY_IDENTIFYING_FIELDS:
            fillIdentifyingFields(product, voProduct);
            break;
        case ONLY_FIELDS_FOR_LISTINGS:
            fillBaseFields(product, facade, voProduct, scope);
            break;
        default:
            fillAllFields(product, facade, voProduct, scope);
        }
        // Bug #10179,add supplierId after serviceId to display
        setServiceIdToDisplay(voProduct, product);
        updateValueObject(voProduct, product);
        return voProduct;
    }

    /**
     * Add supplierId after serviceId to display[serviceId (supplierId)]
     * 
     * @param service
     *            the service to set service Id to display
     * @param product
     *            the product to get the supplierId
     */
    static void setServiceIdToDisplay(VOService service, Product product) {
        StringBuffer buf = new StringBuffer();
        buf.append(service.getServiceId());
        String supplierId = (product.getTemplate() == null ? null : product
                .getTemplate().getVendor().getOrganizationId());
        if (supplierId != null && supplierId.length() > 0) {
            buf.append(" (");
            buf.append(supplierId);
            buf.append(")");
        }
        service.setServiceIdToDisplay(buf.toString());
    }

    public static VOServiceEntry toVOServiceEntry(final Product product,
            final LocalizerFacade facade, boolean subscriptionLimitReached) {
        return toVOServiceEntry(product, facade, subscriptionLimitReached,
                PerformanceHint.ALL_FIELDS);
    }

    public static VOServiceEntry toVOServiceEntry(final Product product,
            final LocalizerFacade facade, boolean subscriptionLimitReached,
            PerformanceHint scope) {
        VOServiceEntry voServiceEntry = new VOServiceEntry();
        switch (scope) {
        case ONLY_IDENTIFYING_FIELDS:
            fillIdentifyingFields(product, voServiceEntry);
            break;
        case ONLY_FIELDS_FOR_LISTINGS:
            fillBaseFields(product, facade, voServiceEntry, scope);
            break;
        default:
            fillAllFields(product, facade, voServiceEntry, scope);
        }
        voServiceEntry.setSubscriptionLimitReached(subscriptionLimitReached);
        updateValueObject(voServiceEntry, product);
        return voServiceEntry;
    }

    public static VOCustomerService toVOCustomerProduct(final Product product,
            final LocalizerFacade facade) {
        VOCustomerService voProduct = new VOCustomerService();
        fillAllFields(product, facade, voProduct, PerformanceHint.ALL_FIELDS);
        updateValueObject(voProduct, product);
        Organization customer = product.getTargetCustomer();
        if (customer != null) {
            voProduct.setOrganizationId(customer.getOrganizationId());
            voProduct.setOrganizationKey(Long.valueOf(customer.getKey()));
            voProduct.setOrganizationName(customer.getName());
        }
        return voProduct;
    }

    /**
     * Returns a value object representation of the given product domain object.
     * Non-configurable parameter definitions will not be attached to the
     * technical product in the result.
     * 
     * @param product
     *            The domain object to take the information from.
     * @param platformParamterDefinitions
     *            The platform parameters to be considered in the output.
     * @param platformEvents
     *            The platform events supported by the system.
     * @param imageDefined
     *            Indicates whether an image is defined for the product or not.
     * @param facade
     *            The localizer facade object.
     * @return A value object reflecting the settings of the domain object.
     */
    public static VOServiceDetails toVOProductDetails(final Product product,
            final List<ParameterDefinition> platformParamterDefinitions,
            final List<Event> platformEvents, boolean imageDefined,
            final LocalizerFacade facade) {
        VOServiceDetails result = new VOServiceDetails();
        updateValueObject(result, product);
        fillAllFields(product, facade, result, PerformanceHint.ALL_FIELDS);
        fillVOServiceDetails(product, platformParamterDefinitions,
                platformEvents, imageDefined, facade, result);
        return result;
    }

    static void fillVOServiceDetails(final Product product,
            final List<ParameterDefinition> platformParamterDefinitions,
            final List<Event> platformEvents, boolean imageDefined,
            final LocalizerFacade facade, VOServiceDetails result) {

        // image
        result.setImageDefined(imageDefined);

        // set the technical product information in the value object
        VOTechnicalService technicalProduct = TechnicalProductAssembler
                .toVOTechnicalProduct(product.getTechnicalProduct(),
                        platformParamterDefinitions, platformEvents, facade,
                        true);
        result.setTechnicalService(technicalProduct);
    }

    /**
     * Updates an already existing product domain object with the settings of a
     * given value object.
     * 
     * @param productToUpdate
     *            The domain object to be updated.
     * @param product
     *            The product to get the recent data from.
     * @return The updated domain object.
     * @throws ValidationException
     *             Thrown in case the identifier length exceeds 40 characters.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public static Product updateProduct(final Product productToUpdate,
            final VOService product) throws ValidationException,
            ConcurrentModificationException {
        verifyVersionAndKey(productToUpdate, product);
        copyAttributes(productToUpdate, product);
        return productToUpdate;
    }

    public static Product updateCustomerTemplateProduct(
            final Product productToUpdate, final VOService product)
            throws ValidationException {
        copyAttributesForCustomerTemplate(productToUpdate, product);
        return productToUpdate;
    }

    static void copyAttributesForCustomerTemplate(Product product,
            final VOService template) throws ValidationException {
        BLValidator.isId(FIELD_NAME_SERVICE_ID, template.getServiceId(), true);
        // Handle the specific product Id of customer template product
        product.setProductId(template.getServiceId()
                + product.getProductId().substring(
                        product.getProductId().indexOf("#")));
        product.setAutoAssignUserEnabled(template.isAutoAssignUserEnabled());
        product.setConfiguratorUrl(template.getConfiguratorUrl());
        product.setCustomTabUrl(template.getCustomTabUrl());
    }

    static void copyAttributes(Product product, final VOService template)
            throws ValidationException {
        BLValidator.isId(FIELD_NAME_SERVICE_ID, template.getServiceId(), true);

        product.setProductId(template.getServiceId());
        product.setAutoAssignUserEnabled(template.isAutoAssignUserEnabled());
        if (product.getType() == ServiceType.TEMPLATE) {
            BLValidator.isUrl(FIELD_NAME_CONFIGURATOR_URL,
                    template.getConfiguratorUrl(), false);

            product.setConfiguratorUrl(template.getConfiguratorUrl());

            BLValidator.isUrl(FIELD_NAME_CUSTOM_TAB_URL,
                    template.getCustomTabUrl(), false);
            product.setCustomTabUrl(template.getCustomTabUrl());

        } else {
            product.setConfiguratorUrl(null);
            product.setCustomTabUrl(null);
        }
    }

    /**
     * @param product
     *            the potential compatible products
     * @param targetKeys
     *            the keys of the products already defined as target
     * @param facade
     *            the {@link LocalizerFacade} to use
     * @return the list of {@link VOCompatibleService}s
     */
    public static VOCompatibleService toVOCompatibleService(Product product,
            Set<Long> targetKeys, LocalizerFacade facade) {
        if (product == null) {
            return null;
        }
        if (targetKeys == null) {
            targetKeys = new HashSet<Long>();
        }
        VOCompatibleService s = new VOCompatibleService();
        fillBaseFields(product, facade, s,
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
        updateValueObject(s, product);
        s.setCompatible(targetKeys.contains(Long.valueOf(s.getKey())));
        return s;
    }
}
