/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.*;

import javax.persistence.*;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.de.GermanStemFilterFactory;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Parameter;
import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.bridge.ProductClassBridge;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.interceptor.DateFactory;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * What is offered on the SaaS platform to the market is called a "product". A
 * product consists of one or more technical products and a pricing model. One
 * technical product can be offered in several (market) products, with different
 * pricing models and different license conditions and capability
 * characteristics (Leistungsmerkmalen). In most cases a product will contain
 * just one technical product. However, the platform should also allow the
 * creation of product bundles consisting of multiple technical products, but
 * with one pricing model.
 * 
 * @author schmid
 */
@Entity
@Indexed
@ClassBridge(impl = ProductClassBridge.class)
@AnalyzerDefs({
        @AnalyzerDef(name = "en", tokenizer = @TokenizerDef(factory = WhitespaceTokenizerFactory.class),
            filters
            = {
                @TokenFilterDef(factory = WordDelimiterFilterFactory.class, params = {
                        @Parameter(name = "preserveOriginal", value = "1"),
                        @Parameter(name = "catenateAll", value = "1") }),
                @TokenFilterDef(factory = LowerCaseFilterFactory.class)}),
        @AnalyzerDef(name = "de", tokenizer = @TokenizerDef(factory = WhitespaceTokenizerFactory.class), filters = {
                @TokenFilterDef(factory = WordDelimiterFilterFactory.class, params = {
                        @Parameter(name = "preserveOriginal", value = "1"),
                        @Parameter(name = "catenateAll", value = "1") }),
                @TokenFilterDef(factory = LowerCaseFilterFactory.class)}), })
@AnalyzerDiscriminator(impl = ProductClassBridge.class)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "productId",
        "vendorKey" }))
@NamedQueries({
        @NamedQuery(name = "Product.findByBusinessKey", query = "select p from Product p where p.dataContainer.productId=:productId and p.vendorKey=:vendorKey"),
        @NamedQuery(name = "Product.getCustomerSpecificCopiesForTemplate", query = "SELECT p FROM Product p WHERE p.template =:template AND p.dataContainer.type IN (:serviceType) AND p.targetCustomer IS NOT NULL ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getProductsForVendor", query = "SELECT p FROM Product p WHERE p.vendorKey=:vendorKey AND p.dataContainer.status NOT IN (:filterOutWithStatus) ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getProductCopyForVendor", query = "SELECT p FROM Product p WHERE p.vendorKey=:vendorKey AND p.template=:product AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) AND p.targetCustomer IS NULL ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getPartnerSpecificCopiesForVendor", query = "SELECT p FROM Product p WHERE p.vendorKey=:vendorKey AND p.dataContainer.status IN (:status) AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getPartnerCopiesForTemplate", query = "SELECT p FROM Product p WHERE p.template =:template AND p.targetCustomer IS NULL AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getPartnerCopiesForTemplateNotInState", query = "SELECT p FROM Product p WHERE p.template =:template AND p.dataContainer.status NOT IN (:statusToIgnore) AND p.targetCustomer IS NULL AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getProductTemplatesForVendor", query = "SELECT p FROM Product p WHERE p.vendorKey=:vendorKey AND p.dataContainer.type IN (:productTypes) AND p.dataContainer.status NOT IN (:filterOutWithStatus) ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getProductsForVendorPaymentConfiguration", query = "SELECT p FROM Product p JOIN p.technicalProduct tp WHERE p.vendorKey=:vendorKey AND p.dataContainer.type IN (org.oscm.internal.types.enumtypes.ServiceType.TEMPLATE, org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE) AND p.dataContainer.status NOT IN (:statusToIgnore) AND tp.dataContainer.billingIdentifier='NATIVE_BILLING' ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getProductsForVendorOnMarketplace", query = "SELECT p FROM Product p LEFT JOIN p.catalogEntries ce WHERE p.vendorKey=:vendorKey AND p.dataContainer.type IN (org.oscm.internal.types.enumtypes.ServiceType.TEMPLATE, org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE) AND EXISTS (SELECT m FROM Marketplace m WHERE m.dataContainer.marketplaceId = :marketplaceId AND m = ce.marketplace) AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getProductsForCustomerOnMarketplace", query = "SELECT p FROM Product p LEFT JOIN p.catalogEntries ce "
                + " WHERE (p.dataContainer.type IN (org.oscm.internal.types.enumtypes.ServiceType.TEMPLATE, org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE) AND EXISTS(SELECT m FROM Marketplace m WHERE m.dataContainer.marketplaceId = :marketplaceId AND m = ce.marketplace)) "
                + "    OR (p.dataContainer.type = org.oscm.internal.types.enumtypes.ServiceType.CUSTOMER_TEMPLATE AND p.targetCustomer = :customer AND EXISTS (SELECT template FROM Product template LEFT JOIN template.catalogEntries ce2 WHERE template.key = p.template.key AND EXISTS(SELECT m FROM Marketplace m WHERE m.dataContainer.marketplaceId = :marketplaceId AND m = ce2.marketplace)) ) "
                + "ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getProductsForTemplateIndexUpdate", query = "SELECT p FROM Product p WHERE p.template =:template AND p.dataContainer.type IN (:type) AND p.dataContainer.status IN (:state) AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getTemplatesInAllStates", query = "SELECT p FROM Product p WHERE p.template IS NULL ORDER BY p.dataContainer.productId ASC"),
        @NamedQuery(name = "Product.getTemplatesForMarketplace", query = "SELECT p FROM Product p LEFT JOIN p.catalogEntries ce WHERE p.template IS NULL AND EXISTS (SELECT m FROM Marketplace m WHERE m.dataContainer.marketplaceId = :marketplaceId AND m = ce.marketplace)) AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getRelatedProductsForMarketplace", query = "SELECT p FROM Product p LEFT JOIN p.catalogEntries ce WHERE (p.targetCustomer = :customer OR (p.template IS NULL AND EXISTS (SELECT m FROM Marketplace m WHERE m.dataContainer.marketplaceId = :marketplaceId AND m = ce.marketplace))) AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) AND p.technicalProduct=:technicalProduct AND p.vendor=:vendor ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getRelatedPublicProductsForMarketplace", query = "SELECT p FROM Product p LEFT JOIN p.catalogEntries ce WHERE (p.template IS NULL AND EXISTS (SELECT m FROM Marketplace m WHERE m.dataContainer.marketplaceId = :marketplaceId AND m = ce.marketplace)) AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) AND p.technicalProduct=:technicalProduct AND ce.dataContainer.anonymousVisible=TRUE AND p.vendor=:vendor ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getPublicProductsForMarketplace", query = "SELECT p FROM Product p, CatalogEntry c WHERE p.template IS NULL AND p.dataContainer.status = org.oscm.internal.types.enumtypes.ServiceStatus.ACTIVE AND c.marketplace = :marketplace AND c.product = p AND c.dataContainer.anonymousVisible=TRUE ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getActiveProductsForMarketplace", query = "SELECT ce FROM Product p INNER JOIN p.catalogEntries ce WHERE p.targetCustomer IS NULL AND (SELECT s from Subscription s WHERE p = s.product) IS NULL AND p.dataContainer.status = org.oscm.internal.types.enumtypes.ServiceStatus.ACTIVE AND p.technicalProduct=:technicalProduct AND p.vendorKey=:vendorKey"),
        @NamedQuery(name = "Product.getCustomerProductsForVendor", query = "select p from Product p where p.vendorKey=:vendorKey AND p.targetCustomer IS NOT NULL AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getCustomerSpecificProducts", query = "select p from Product p where p.vendorKey=:vendorKey AND p.template IS NOT NULL AND p.targetCustomer = :customer AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getSpecificCustomerProduct", query = "SELECT p FROM Product p WHERE p.template = :template AND p.targetCustomer = :customer AND p.dataContainer.type=org.oscm.internal.types.enumtypes.ServiceType.CUSTOMER_TEMPLATE ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getForCustomerAndTemplate", query = "SELECT prod FROM Product prod WHERE prod.targetCustomer = :customer AND prod.template = :template AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = prod) ORDER BY prod.key ASC"),
        @NamedQuery(name = "Product.getForTemplate", query = "SELECT prod FROM Product prod WHERE prod.template = :template AND EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = prod) ORDER BY prod.key ASC"),
        @NamedQuery(name = "Product.getForCustomerOnly", query = "select p from Product p where p.vendorKey=:vendorKey AND p.targetCustomer = :customer AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = p) ORDER BY p.key ASC"),
        @NamedQuery(name = "Product.getForCustomerAndSubId", query = "SELECT prod FROM Product prod, Subscription sub WHERE prod = sub.product AND sub.dataContainer.subscriptionId = :subscriptionId AND sub.organization = :customer"),
        @NamedQuery(name = "Product.getCustomerCopies", query = "SELECT prod FROM Product prod WHERE prod.template = :template AND prod.targetCustomer IS NOT NULL AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = prod) ORDER BY prod.key ASC"),
        @NamedQuery(name = "Product.countCustomerCopiesForTemplateInState", query = "SELECT COUNT(*) FROM Product prod WHERE prod.template = :template AND prod.dataContainer.status IN (:status) AND prod.targetCustomer IS NOT NULL AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = prod)"),
        @NamedQuery(name = "Product.countAllReferences", query = "SELECT COUNT(*) FROM ProductReference pr WHERE (pr.sourceProduct.key=:productKey AND pr.targetProduct.dataContainer.status NOT IN (:status)) OR (pr.targetProduct.key=:productKey AND pr.sourceProduct.dataContainer.status NOT IN (:status))"),
        @NamedQuery(name = "Product.getCopyForCustomer", query = "SELECT prod FROM Product prod WHERE prod.template = :template AND prod.targetCustomer = :customer AND NOT EXISTS (SELECT sub FROM Subscription sub WHERE sub.product = prod)"),
        @NamedQuery(name = "Product.getPotentialCompatibleForProduct", query = "SELECT p FROM Product p LEFT JOIN p.catalogEntries ce WHERE p.vendorKey=:vendorKey AND p.template IS NULL AND p.technicalProduct=:tp AND p.dataContainer.status NOT IN (:status) AND ce.marketplace IN (:marketplaces)"),
        @NamedQuery(name = "Product.getPublishedProductTemplates", query = "SELECT p FROM Product p INNER JOIN p.catalogEntries ce WHERE p.dataContainer.status NOT IN (:filterOutWithStatus) AND p.dataContainer.type IN (org.oscm.internal.types.enumtypes.ServiceType.TEMPLATE, org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE) AND ce.marketplace = :marketplace AND NOT EXISTS (SELECT lp FROM PublicLandingpage lp INNER JOIN lp.marketplace mp INNER JOIN lp.landingpageProducts lpp WHERE mp = :marketplace AND lpp.product = p)"),
        @NamedQuery(name = "Product.getActivePublishedProductTemplates", query = "SELECT p FROM Product p INNER JOIN p.catalogEntries ce"
                + " WHERE p.dataContainer.status = org.oscm.internal.types.enumtypes.ServiceStatus.ACTIVE"
                + " AND ce.dataContainer.visibleInCatalog=TRUE"
                + " AND ce.dataContainer.anonymousVisible=TRUE"
                + " AND p.dataContainer.type IN (org.oscm.internal.types.enumtypes.ServiceType.TEMPLATE, org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE)"
                + " AND ce.marketplace.dataContainer.marketplaceId = :marketplaceId"
                + " AND NOT EXISTS (SELECT lp FROM LandingpageProduct lp WHERE lp.landingpage.marketplace.dataContainer.marketplaceId = :marketplaceId AND lp.product.key = p.key)"
                + " ORDER BY p.dataContainer.provisioningDate DESC"),
        @NamedQuery(name = "Product.getActivePublishedProducts", query = "SELECT p FROM Product p LEFT JOIN p.catalogEntries ce"
                + " WHERE NOT EXISTS (SELECT lp FROM LandingpageProduct lp WHERE lp.landingpage.marketplace.dataContainer.marketplaceId = :marketplaceId AND ((p.targetCustomer IS NULL AND lp.product = p) OR (p.targetCustomer = :customer AND lp.product = p.template)))"
                + " AND p.dataContainer.type IN (org.oscm.internal.types.enumtypes.ServiceType.TEMPLATE, org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE, org.oscm.internal.types.enumtypes.ServiceType.CUSTOMER_TEMPLATE)"
                + " AND ((EXISTS (SELECT m FROM Marketplace m WHERE m.dataContainer.marketplaceId = :marketplaceId AND m = ce.marketplace AND ce.dataContainer.visibleInCatalog=TRUE))"
                + "  OR (p.dataContainer.type = org.oscm.internal.types.enumtypes.ServiceType.CUSTOMER_TEMPLATE AND p.targetCustomer = :customer AND EXISTS (SELECT ce1 FROM CatalogEntry ce1 WHERE ce1.marketplace.dataContainer.marketplaceId = :marketplaceId AND ce1.product = p.template AND ce1.dataContainer.visibleInCatalog=TRUE)))"
                + " AND NOT EXISTS (SELECT pr FROM Product pr WHERE pr.dataContainer.type = org.oscm.internal.types.enumtypes.ServiceType.CUSTOMER_TEMPLATE AND pr.template = p AND pr.targetCustomer = :customer)"
                + " AND p.dataContainer.status = org.oscm.internal.types.enumtypes.ServiceStatus.ACTIVE"
                + " ORDER BY p.dataContainer.provisioningDate DESC") })
@BusinessKey(attributes = { "productId", "vendorKey" })
public class Product extends DomainObjectWithHistory<ProductData> {

    private static final long serialVersionUID = 4336379997821816252L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays.asList(
                    LocalizedObjectTypes.PRODUCT_MARKETING_DESC,
                    LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                    LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION,
                    LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME));

    private static final transient Log4jLogger logger = LoggerFactory
            .getLogger(Product.class);

    public Product() {
        super();
        dataContainer = new ProductData();
    }

    /**
     * In order to form a complete business key the Organization key is needed
     * as explicit field inside this class. This field is also used as
     * JoinColumn for the n:1 relation to Organization.
     */
    @Column(name = "vendorKey", insertable = false, updatable = false, nullable = false)
    private long vendorKey;

    /**
     * n:1 relation to the organization the product belongs to, that is the
     * organization it was supplied by or a broker/reseller organization. Has to
     * be set, as each product must belong to exactly one organization.<br>
     * CascadeType: NONE
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vendorKey")
    private Organization vendor;

    /**
     * 1:1 relation to the product's price model. Currently each product is
     * assigned to a fixed price model. <br>
     * CascadeType: ALL
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PriceModel priceModel;

    /**
     * 1:1 relation to the product's parameter set. CascadeType: ALL
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ParameterSet parameterSet;

    /**
     * Reference to the technical product this marketing product is based on.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TechnicalProduct technicalProduct;

    /**
     * References a relation to other products indicating supporting migration
     * paths for the product. The cascade type is set to REMOVE, to prevent
     * existence of orphan entries, and to PERSIST, so that the copying works.
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, mappedBy = "sourceProduct", fetch = FetchType.LAZY)
    @OrderBy
    private List<ProductReference> compatibleProducts = new ArrayList<ProductReference>();

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "product", fetch = FetchType.LAZY)
    private List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = new ArrayList<UserGroupToInvisibleProduct>();

    /**
     * References a relation to other products indicating supporting migration
     * paths for the product. The cascade type is set to REMOVE, to prevent
     * existence of orphan entries, and to PERSIST, so that the copying works.
     * The member is used only for cascade delete on product deleting.
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, mappedBy = "targetProduct", fetch = FetchType.LAZY)
    @OrderBy
    private List<ProductReference> compatibleProductsTarget = new ArrayList<ProductReference>();

    /**
     * Reference to the parent product object, the current product is a copy of.
     * The copy is created to support subscription- or organization specific
     * settings in e.g. a price model.
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Product template;

    /**
     * Reference to the subscription this (marketing) product has been created
     * for. When this field is set, the field {@link #template} has to be set as
     * well.
     */
    @OneToOne(optional = true, mappedBy = "product", fetch = FetchType.LAZY)
    private Subscription owningSubscription;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Organization targetCustomer;

    /**
     * References to the catalog entries. A catalog entry defines how a product
     * is shown in the catalog.
     */
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "product", fetch = FetchType.LAZY)
    private List<CatalogEntry> catalogEntries = new ArrayList<CatalogEntry>();

    /**
     * Reference to the user reviews for this product and to the average rating.
     * Should be <code>null</code> in case this product is a customer specific
     * product, because only a template product may receive reviews.
     */
    @OneToOne(cascade = CascadeType.REMOVE, optional = true, mappedBy = "product", fetch = FetchType.LAZY)
    private ProductFeedback productFeedback;

    /**
     * Reference to ProductToPaymentType entries.
     */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ProductToPaymentType> paymentTypes = new ArrayList<ProductToPaymentType>();

    public List<ProductToPaymentType> getPaymentTypes() {
        return paymentTypes;
    }

    public void setPaymentTypes(List<ProductToPaymentType> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

    public void setVendor(Organization organization) {
        vendor = organization;
        if (organization != null) {
            setVendorKey(organization.getKey());
        }
    }

    public Organization getVendor() {
        return vendor;
    }

    public void setVendorKey(long organizationKey) {
        vendorKey = organizationKey;
    }

    public long getVendorKey() {
        return vendorKey;
    }

    /**
     * Sets the price model for this product and also updates the price model
     * parameter by setting its product reference to this product.
     * 
     * @param priceModel
     *            The price model a reference to which has to be set.
     */
    public void setPriceModel(PriceModel priceModel) {
        this.priceModel = priceModel;
        if (priceModel != null) {
            priceModel.setProduct(this);
        }
    }

    public PriceModel getPriceModel() {
        return priceModel;
    }

    public void setParameterSet(ParameterSet parameterSet) {
        this.parameterSet = parameterSet;
        if (parameterSet != null) {
            parameterSet.setProduct(this);
        }
    }

    public ParameterSet getParameterSet() {
        return parameterSet;
    }

    /**
     * Refer to {@link ProductData#deprovisioningDate}
     */
    public Long getDeprovisioningDate() {
        return dataContainer.getDeprovisioningDate();
    }

    /**
     * Refer to {@link ProductData#deprovisioningDate}
     */
    public void setDeprovisioningDate(Long deprovisioningDate) {
        dataContainer.setDeprovisioningDate(deprovisioningDate);
    }

    /**
     * Refer to {@link ProductData#productId}
     */
    public String getProductId() {
        return dataContainer.getProductId();
    }

    /**
     * Refer to {@link ProductData#productId}
     */
    public void setProductId(String productId) {
        dataContainer.setProductId(productId);
    }

    /**
     * Refer to {@link ProductData#provisioningDate}
     */
    public long getProvisioningDate() {
        return dataContainer.getProvisioningDate();
    }

    /**
     * Refer to {@link ProductData#provisioningDate}
     */
    public void setProvisioningDate(long provisioningDate) {
        dataContainer.setProvisioningDate(provisioningDate);
    }

    /**
     * Refer to {@link ProductData#status}
     */
    public ServiceStatus getStatus() {
        return dataContainer.getStatus();
    }

    /**
     * Refer to {@link ProductData#status}
     */
    public void setStatus(ServiceStatus status) {
        dataContainer.setStatus(status);
    }

    public ServiceType getType() {
        return dataContainer.getType();
    }

    public void setType(ServiceType type) {
        dataContainer.setType(type);
    }

    public TechnicalProduct getTechnicalProduct() {
        return technicalProduct;
    }

    public void setTechnicalProduct(TechnicalProduct technicalProduct) {
        this.technicalProduct = technicalProduct;
    }

    public Boolean isAutoAssignUserEnabled() {
        if (this.getType() == org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE) {
            return getTemplate().getDataContainer().isAutoAssignUserEnabled();
        } else {
            return this.getDataContainer().isAutoAssignUserEnabled();
        }
    }

    public void setAutoAssignUserEnabled(Boolean autoAssignUserEnabled) {
        dataContainer.setAutoAssignUserEnabled(autoAssignUserEnabled);
    }

    public String getConfiguratorUrl() {
        return dataContainer.getConfiguratorUrl();
    }

    public void setConfiguratorUrl(String configuratorUrl) {
        dataContainer.setConfiguratorUrl(configuratorUrl);
    }

    public String getCustomTabUrl() {
        return dataContainer.getCustomTabUrl();
    }

    public void setCustomTabUrl(String customTabUrl) {
        dataContainer.setCustomTabUrl(customTabUrl);
    }

    public List<Product> getCompatibleProductsList() {
        Product templ = getType() == org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE ? null
                : getTemplate();
        if (templ != null) {
            return templ.getCompatibleProductsList();
        }
        List<Product> result = new ArrayList<Product>(compatibleProducts.size());
        for (ProductReference ref : compatibleProducts) {
            result.add(ref.getTargetProduct());
        }
        return result;
    }

    /**
     * Returns all product references for the current product. It does also
     * return all product references related to the template, in case the
     * product is a copy itself.
     * 
     * @return The list of products the current product can be migrated to.
     */
    public List<ProductReference> getAllCompatibleProducts() {
        Product templ = getType() == org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE ? null
                : getTemplate();
        if (templ != null) {
            return templ.getAllCompatibleProducts();
        }
        return compatibleProducts;
    }

    /**
     * Returns all product references for the current product. It does not
     * consider references stored for the template.
     * 
     * @return The list of compatible products stored for this product.
     */
    public List<ProductReference> getCompatibleProducts() {
        return compatibleProducts;
    }

    public Product getTemplate() {
        return template;
    }

    public void setTemplate(Product template) {
        this.template = template;
    }

    public Subscription getOwningSubscription() {
        return owningSubscription;
    }

    public void setOwningSubscription(Subscription owningSubscription) {
        this.owningSubscription = owningSubscription;
    }

    public Organization getTargetCustomer() {
        return targetCustomer;
    }

    public void setTargetCustomer(Organization targetOrganization) {
        targetCustomer = targetOrganization;
    }

    /**
     * Returns a copy of this product keeping all references except the
     * <ul>
     * <li>PriceModel reference</li>
     * <li>ParameterSet reference</li>
     * </ul>
     * Those objects are copied themselves, so that a new marketing product can
     * be based on the returned copy.
     * 
     * <p>
     * Furthermore the deprovisioning date will be set to null, the provisioning
     * date to the max value of the current time and the provisioning date of
     * this product.
     * </p>
     * 
     * @param targetCustomer
     *            The customer this copy is created and will be used for.
     * @param owningSubscription
     *            The subscription the copy is made and will be used for.
     * 
     * @return A copy of the current product.
     */
    public Product copyForSubscription(Organization targetCustomer,
            Subscription owningSubscription) {

        Product copy = new Product();
        copy.setDataContainer(new ProductData());

        setSubscriptionProductType(copy);
        copy.setStatus(this.getStatus());
        copy.setTemplate(getTemplateIfSubscriptionOrSelf());
        copy.setTargetCustomer(targetCustomer);
        copy.setOwningSubscription(owningSubscription);
        copy.setVendor(this.getVendor());
        copy.setTechnicalProduct(this.getTechnicalProduct());

        setDatacontainerValues(copy, copy.getType());

        setParamatersAndPriceModel(copy);

        return copy;
    }

    void setParamatersAndPriceModel(Product copy) {
        ParameterSet parameterSet = getParameterSet();
        if (parameterSet == null && getType() == org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE) {
            parameterSet = getTemplate().getParameterSet();
        }

        if (parameterSet != null) {
            copy.setParameterSet(parameterSet.copy());
        }

        PriceModel pm = getPriceModel();
        if (pm == null && getType() == org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE) {
            pm = getTemplate().getPriceModel();
        }

        if (pm != null) {
            PriceModel pmCopy = pm.copy(copy.getParameterSet());
            copy.setPriceModel(pmCopy);
        }
    }

    void setSubscriptionProductType(Product subscriptionCopy) {
        if (this.getType() == null) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, new Exception(
                    "setSubscriptionProductType, type==null"),
                    LogMessageIdentifier.DEBUG);
            return;
        }
        switch (this.getType()) {
        case TEMPLATE:
            subscriptionCopy.setType(org.oscm.internal.types.enumtypes.ServiceType.SUBSCRIPTION);
            break;
        case CUSTOMER_TEMPLATE:
            subscriptionCopy.setType(org.oscm.internal.types.enumtypes.ServiceType.CUSTOMER_SUBSCRIPTION);
            break;
        case PARTNER_TEMPLATE:
            subscriptionCopy.setType(org.oscm.internal.types.enumtypes.ServiceType.PARTNER_SUBSCRIPTION);
            break;
        case SUBSCRIPTION:
            subscriptionCopy.setType(org.oscm.internal.types.enumtypes.ServiceType.SUBSCRIPTION);
            break;
        case CUSTOMER_SUBSCRIPTION:
            subscriptionCopy.setType(org.oscm.internal.types.enumtypes.ServiceType.CUSTOMER_SUBSCRIPTION);
            break;
        case PARTNER_SUBSCRIPTION:
            subscriptionCopy.setType(org.oscm.internal.types.enumtypes.ServiceType.PARTNER_SUBSCRIPTION);
            break;
        }
    }

    /**
     * Returns a template copy of this product keeping all references except the
     * <ul>
     * <li>PriceModel reference</li>
     * <li>ParameterSet reference</li>
     * </ul>
     * Those objects are copied themselves, so that a new marketing product can
     * be based on the returned copy.
     * 
     * <p>
     * Furthermore the deprovisioning date will be set to null, the provisioning
     * date to the max value of the current time and the provisioning date of
     * this product.
     * </p>
     * 
     * @param productId
     *            The productId for the copy.
     * 
     * @return A copy of the current product.
     */
    public Product copyTemplate(String productId) {

        Product copy = new Product();
        copy.setDataContainer(new ProductData());

        copy.setType(org.oscm.internal.types.enumtypes.ServiceType.TEMPLATE);
        copy.setStatus(ServiceStatus.INACTIVE);
        copy.setTemplate(null);
        copy.setTargetCustomer(null);
        copy.setOwningSubscription(null);
        copy.setTechnicalProduct(this.getTechnicalProduct());
        copy.setVendor(this.getVendor());
        copy.setProductFeedback(null);

        setDatacontainerValues(copy, org.oscm.internal.types.enumtypes.ServiceType.TEMPLATE);

        // overwrite
        copy.setProductId(productId);
        setParamatersAndPriceModel(copy);

        return copy;
    }

    /**
     * Returns a copy of this product keeping all references except the
     * <ul>
     * <li>PriceModel reference</li>
     * <li>ParameterSet reference</li>
     * </ul>
     * Those objects are copied themselves, so that a new marketing product can
     * be based on the returned copy.
     * 
     * <p>
     * Furthermore the deprovisioning date will be set to null, the provisioning
     * date to the max value of the current time and the provisioning date of
     * this product.
     * </p>
     * 
     * @param targetCustomer
     *            The customer this copy is created and will be used for.
     * @return A copy of the current product.
     */
    public Product copyForCustomer(Organization targetCustomer) {

        Product copy = new Product();
        copy.setDataContainer(new ProductData());

        copy.setType(org.oscm.internal.types.enumtypes.ServiceType.CUSTOMER_TEMPLATE);
        copy.setStatus(ServiceStatus.INACTIVE);
        copy.setOwningSubscription(null);
        copy.setTargetCustomer(targetCustomer);
        copy.setVendor(this.getVendor());
        copy.setTechnicalProduct(this.getTechnicalProduct());
        copy.setTemplate(getTemplateOrSelf());

        setDatacontainerValues(copy, org.oscm.internal.types.enumtypes.ServiceType.CUSTOMER_TEMPLATE);

        setParamatersAndPriceModel(copy);

        return copy;
    }

    void setDatacontainerValues(Product copy, ServiceType serviceType) {
        copy.setDeprovisioningDate(null);
        copy.setProvisioningDate(Math.max(DateFactory.getInstance()
                .getTransactionTime(), this.getProvisioningDate()));
        copy.setProductId(getTemplateOrSelf().getProductId() + "#"
                + UUID.randomUUID());
        copy.setAutoAssignUserEnabled(this.isAutoAssignUserEnabled());
        if (serviceType == org.oscm.internal.types.enumtypes.ServiceType.TEMPLATE) {
            copy.setConfiguratorUrl(this.getConfiguratorUrl());
        } else {
            copy.setConfiguratorUrl(null);
        }
        copy.setCustomTabUrl(this.getCustomTabUrl());

    }

    /**
     * Copies a product for resale. All references are kept except the
     * <ul>
     * <li>PriceModel reference</li>
     * <li>ParameterSet reference</li>
     * </ul>
     * The PriceModel reference is set to null. The ParameterSet is copied, so
     * that a new marketing product can be based on the returned copy.
     * 
     * <p>
     * Furthermore the deprovisioning date will be set to null, the provisioning
     * date to the max value of the current time and the provisioning date of
     * this product.
     * </p>
     * 
     * @param vendor
     *            The vendor (broker or reseller) this copy is created and will
     *            be used for.
     * 
     * @return A copy of the current product.
     */
    public Product copyForResale(Organization vendor) {

        Product copy = new Product();
        copy.setDataContainer(new ProductData());

        copy.setType(org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE);
        copy.setStatus(this.getStatus());
        copy.setVendor(vendor);
        copy.setTechnicalProduct(this.getTechnicalProduct());

        // Price model and parameters can only be modified by the supplier.
        // Thus they are not stored in the resale copy but in the template.
        copy.setPriceModel(null);
        copy.setParameterSet(null);
        copy.setConfiguratorUrl(null);
        copy.setCustomTabUrl(null);

        setDatacontainerValues(copy, org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE);
        copy.setAutoAssignUserEnabled(null);
        // to set the template, ensure that this object is not a copy itself. If
        // it is, set it's parent!
        copy.setTemplate(getTemplateOrSelf());

        return copy;
    }

    /**
     * 
     * @return true if and only if the status of the product is
     *         ServiceStatus.DELETED or ServiceStatus.OBSOLETE
     */
    public boolean isDeleted() {
        return getStatus() == ServiceStatus.DELETED
                || getStatus() == ServiceStatus.OBSOLETE;
    }

    /**
     * Setter for compatibleProductsTarget. protected for not using. The member
     * is used only for cascade delete on product deleting.
     * 
     * @param compatibleProductsTarget
     *            List of compatible products.
     */
    protected void setCompatibleProductsTarget(
            List<ProductReference> compatibleProductsTarget) {
        this.compatibleProductsTarget = compatibleProductsTarget;
    }

    /**
     * Getter for compatibleProductsTarget. The member is used only for cascade
     * delete on product deleting.
     * 
     * @return List of compatible products.
     */
    public List<ProductReference> getCompatibleProductsTarget() {
        return compatibleProductsTarget;
    }

    /**
     * Sets the catalog entries which are representing this product.
     * 
     * @param catalogEntries
     *            the catalog entries
     */
    public void setCatalogEntries(List<CatalogEntry> catalogEntries) {
        this.catalogEntries = catalogEntries;
    }

    /**
     * Returns the catalog entries which are representing this product.
     * 
     * @return the catalog entries
     */
    public List<CatalogEntry> getCatalogEntries() {
        return catalogEntries;
    }

    public List<UserGroupToInvisibleProduct> getUserGroupToInvisibleProducts() {
        return userGroupToInvisibleProducts;
    }

    public void setUserGroupToInvisibleProducts(
            List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts) {
        this.userGroupToInvisibleProducts = userGroupToInvisibleProducts;
    }

    /**
     * Returns the catalog entry for the marketplace identified by the passed
     * marketplace object. Returns null if no entry exists.
     * 
     * @param marketplace
     * @return the catalog entry
     */
    public CatalogEntry getCatalogEntryForMarketplace(Marketplace marketplace) {
        if (marketplace == null) {
            return null;
        }

        CatalogEntry catalogEntry = null;
        List<CatalogEntry> cEntries = getCatalogEntries();
        if (cEntries != null) {
            for (CatalogEntry ce : cEntries) {
                Marketplace mp = ce.getMarketplace();
                if ((mp != null)
                        && (marketplace.getMarketplaceId().equals(mp
                                .getMarketplaceId()))) {
                    catalogEntry = ce;
                    break;
                }
            }
        }
        return catalogEntry;
    }

    public ProductFeedback getProductFeedback() {
        return productFeedback;
    }

    public void setProductFeedback(ProductFeedback productFeedback) {
        this.productFeedback = productFeedback;
    }

    /**
     * Returns the template or if no template exists it returns itself.
     * 
     * @return Product
     */
    public Product getTemplateOrSelf() {
        if (isCopy()) {
            return getTemplate();
        }
        return this;
    }

    public Product getTemplateOrSelfForReview() {
        if (this.getType().name().equals(ServiceType.PARTNER_TEMPLATE.name())) {
            return this;
        }
        if (isCopy()) {
            return getTemplate();
        }
        return this;
    }

    /**
     * Returns itself in case this is a template product or a re-sale copy,
     * otherwise the template of this product is returned.
     * 
     * @return Product
     */
    public Product getTemplateIfSubscriptionOrSelf() {
        if (targetCustomer == null && owningSubscription == null) {
            return this;
        }
        return getTemplate();
    }

    /**
     * Determine the charging organization of this product. The charging
     * organization is a organization of type SUPPLIER or RESELLER.
     * 
     * @return Organization
     */
    public Organization determineChargingOrganization() {
        Organization vendor = getVendor();
        if (vendor.getGrantedRoleTypes()
                .contains(OrganizationRoleType.SUPPLIER)
                || vendor.getGrantedRoleTypes().contains(
                        OrganizationRoleType.RESELLER)) {
            return vendor;
        } else if (vendor.getGrantedRoleTypes().contains(
                OrganizationRoleType.BROKER)) {
            return getProductTemplate().getVendor();
        } else {
            return null;
        }
    }

    /**
     * Returns the product template. If the product is a partner template, the
     * original product template is returned.
     * 
     * @return
     */
    public Product getProductTemplate() {
        if (isCopy()) {
            return getTemplate().getProductTemplate();
        } else {
            return this;
        }
    }

    /**
     * Returns true if this product is a copy of a template product.
     * 
     * @return boolean
     */
    public boolean isCopy() {
        if (getTemplate() != null && !getTemplate().equals(this)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the given user is allowed to either create a new review or
     * update is own review.
     * 
     * @return boolean
     */
    public boolean isAllowedToWriteReview(PlatformUser user) {
        return isAllowedToCreateReview(user)
                || isAllowedToUpdateOwnReview(user);
    }

    /**
     * The given user is allowed to review the product only in one of the two
     * cases:<br>
     * 1. Its technical product has 'EXTERNAL' access type, or<br>
     * 2. The organization the user belongs to has at least one subscription and
     * the current user has a usage license or is administrator
     */
    public boolean isAllowedToCreateReview(PlatformUser user) {
        if (isExtenalService()) {
            return true;
        }

        Organization org = user.getOrganization();
        List<Subscription> subscriptions = org
                .getUsableSubscriptionsForProduct(this);
        if (subscriptions.isEmpty()) {
            return false;
        }
        if (user.isOrganizationAdmin()) {
            return true;
        }
        for (Subscription subscription : subscriptions) {
            if (subscription.getUsageLicenseForUser(user) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Its technical product has 'EXTERNAL' access type or not
     */
    public boolean isExtenalService() {
        if (ServiceAccessType.EXTERNAL.equals(getTechnicalProduct()
                .getAccessType())) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the given user has written a review. In this case he is allowed
     * to update his own review.
     * 
     * @param user
     * @return boolean
     */
    public boolean isAllowedToUpdateOwnReview(PlatformUser user) {
        if (getProductFeedback() == null) {
            return false;
        }
        return getProductFeedback().hasReview(user);
    }

    /**
     * If the product id contains a '#' the part before will be returned.
     */
    public String getCleanProductId() {
        return dataContainer.getCleanProductId();
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

    /**
     * In case this is a customer specific or subscription copy, its template
     * or, if the template is a broker copy, its template's template will be
     * returned.
     * 
     * @return the supplier or reseller template
     */
    public Product getSupplierOrResellerTemplate() {
        Product prod = this;
        if (prod.getType() == org.oscm.internal.types.enumtypes.ServiceType.CUSTOMER_TEMPLATE
                || org.oscm.internal.types.enumtypes.ServiceType.isSubscription(prod.getType())) {
            prod = prod.getTemplate();
        }
        if (prod.getType() == org.oscm.internal.types.enumtypes.ServiceType.PARTNER_TEMPLATE
                && prod.getVendor().getGrantedRoleTypes()
                        .contains(OrganizationRoleType.BROKER)) {
            prod = prod.getTemplate();
        }
        return prod;
    }
}
