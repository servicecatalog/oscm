/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.bridge.SubscriptionClassBridge;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.interceptor.DateFactory;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.types.exceptions.UserAlreadyAssignedException;
import org.oscm.types.exceptions.UserNotAssignedException;

/**
 * The Subscription is the representation for the usage of a product by a
 * organization. In order to be able to use a product, a organization must
 * subscribe to it. When the organization does so, logically an own data
 * container is created and made available exclusively to this organization.
 * This means that this organization's product-specific data are kept strictly
 * separate from those of all other organizations.
 * 
 * @author schmid
 */
@Entity
@ClassBridge(impl = SubscriptionClassBridge.class)
@AnalyzerDef(name = "customanalyzer", tokenizer = @TokenizerDef(factory = WhitespaceTokenizerFactory.class), filters = {
        @TokenFilterDef(factory = WordDelimiterFilterFactory.class, params = {
                @org.hibernate.search.annotations.Parameter(name = "preserveOriginal", value = "1"),
                @org.hibernate.search.annotations.Parameter(name = "catenateAll", value = "1") }),
        @TokenFilterDef(factory = LowerCaseFilterFactory.class) })
@Analyzer(definition = "customanalyzer")
@Indexed
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "subscriptionId",
        "organizationKey" }))
@NamedQueries({
        @NamedQuery(name = "Subscription.hasSubscriptionsBasedOnOnBehalfServicesForTp", query = "SELECT COUNT(su) FROM Subscription su, TechnicalProduct tp, Product p WHERE tp.organizationKey=:tpOrgKey and tp.dataContainer.allowingOnBehalfActing=true and su.dataContainer.status=org.oscm.internal.types.enumtypes.SubscriptionStatus.ACTIVE and su.product.key=p.key and p.technicalProduct.key=tp.key)"),
        @NamedQuery(name = "Subscription.findByBusinessKey", query = "select obj from Subscription obj where obj.dataContainer.subscriptionId=:subscriptionId and obj.organizationKey=:organizationKey"),
        @NamedQuery(name = "Subscription.getByStatus", query = "select obj from Subscription obj where obj.dataContainer.status = :status"),
        @NamedQuery(name = "Subscription.getByUUID", query = "select obj from Subscription obj where obj.uuid = :uuid"),
        @NamedQuery(name = "Subscription.getForProduct", query = "SELECT s FROM Subscription s WHERE s.dataContainer.status IN (:status)"
                + " AND (s.product.template = :product OR s.product.template.template = :product)"),
        @NamedQuery(name = "Subscription.getByInstanceIdOfTechProd", query = "select s from Subscription s, Product p, TechnicalProduct tp"
                + " where s.dataContainer.productInstanceId = :productInstanceId"
                + " and s.product = p and p.technicalProduct = :technicalProduct"),
        @NamedQuery(name = "Subscription.getSubscriptionIdsForMyCustomers", query = "SELECT DISTINCT s.dataContainer.subscriptionId FROM Subscription s WHERE s.product.vendor = :offerer AND s.dataContainer.status IN (:states) ORDER BY s.dataContainer.subscriptionId ASC"),
        @NamedQuery(name = "Subscription.hasCurrentUserSubscriptions", query = "SELECT COUNT(*) FROM Subscription sub WHERE sub.dataContainer.status IN (:status) AND EXISTS (SELECT lic FROM UsageLicense lic WHERE lic.user.key = :userKey AND lic.subscription = sub)"),
        @NamedQuery(name = "Subscription.organizationsWithMoreThanOneVisibleSubscription", query = "SELECT COUNT(organization) FROM Organization organization WHERE organization.key=(SELECT sub.organizationKey FROM Subscription sub WHERE sub.product.technicalProduct.key=:productKey AND sub.dataContainer.status<>org.oscm.internal.types.enumtypes.SubscriptionStatus.INVALID AND sub.dataContainer.status<>org.oscm.internal.types.enumtypes.SubscriptionStatus.DEACTIVATED GROUP BY sub.organizationKey HAVING COUNT(sub)>1)"),
        @NamedQuery(name = "Subscription.numberOfSubscriptionsForProduct", query = "SELECT COUNT(*) FROM Subscription sub WHERE sub.product=:product"),
        @NamedQuery(name = "Subscription.numberOfVisibleSubscriptionsForTechnicalProduct", query = "SELECT COUNT(*) FROM Subscription sub WHERE sub.product.technicalProduct.key=:productKey AND sub.dataContainer.status<>org.oscm.internal.types.enumtypes.SubscriptionStatus.INVALID AND sub.dataContainer.status<>org.oscm.internal.types.enumtypes.SubscriptionStatus.DEACTIVATED"),
        @NamedQuery(name = "Subscription.getCurrentUserSubscriptions", query = "SELECT sub FROM Subscription sub WHERE sub.dataContainer.status IN (:status) AND EXISTS (SELECT lic FROM UsageLicense lic WHERE lic.user.key = :userKey AND lic.subscription = sub)"),
        @NamedQuery(name = "Subscription.getCurrentUserSubscriptionsByKeys", query = ""
                + "SELECT sub FROM Subscription sub "
                + "WHERE sub.dataContainer.status IN (:status) " + "AND EXISTS "
                + "(SELECT lic FROM UsageLicense lic WHERE lic.user.key = :userKey AND lic.subscription = sub) "
                + "AND sub.key in :keys"),
        @NamedQuery(name = "Subscription.numberOfVisibleSubscriptions", query = "SELECT count(sub) FROM Subscription sub WHERE sub.product.technicalProduct.key=:productKey AND sub.organizationKey=:orgKey AND sub.dataContainer.status<>org.oscm.internal.types.enumtypes.SubscriptionStatus.INVALID AND sub.dataContainer.status<>org.oscm.internal.types.enumtypes.SubscriptionStatus.DEACTIVATED"),
        @NamedQuery(name = "Subscription.getForMarketplace", query = "SELECT sub FROM Subscription sub WHERE sub.marketplace=:marketplace"),
        @NamedQuery(name = "Subscription.getUsableSubscriptionsForMplAndOrg", query = "SELECT sub FROM Subscription sub WHERE sub.organization=:organization AND sub.marketplace=:marketplace AND sub.dataContainer.status <> org.oscm.internal.types.enumtypes.SubscriptionStatus.SUSPENDED"),
        @NamedQuery(name = "Subscription.instanceIdsForSuppliers", query = "SELECT sub.dataContainer.productInstanceId FROM Subscription sub, Product p, TechnicalProduct tp, Organization sup WHERE sup.dataContainer.organizationId IN (:supplierIds) AND tp.organizationKey=:providerKey AND sub.product.key=p.key AND sub.dataContainer.status IN (:status) AND p.technicalProduct.key=tp.key AND p.vendor.key = sup.key"),
        @NamedQuery(name = "Subscription.getForOrgFetchRoles", query = "SELECT DISTINCT sub, role FROM Subscription sub, Product prod, TechnicalProduct tp LEFT JOIN tp.roleDefinitions role WHERE sub.product = prod AND prod.technicalProduct = tp AND sub.dataContainer.status IN (:status) AND sub.organizationKey =:orgKey ORDER by sub.key ASC"),
        @NamedQuery(name = "Subscription.getSubRoles", query = "SELECT DISTINCT role FROM Subscription sub, Product prod, TechnicalProduct tp LEFT JOIN tp.roleDefinitions role WHERE sub.product = prod AND prod.technicalProduct = tp AND sub.organizationKey =:orgKey AND sub.dataContainer.subscriptionId=:subId ORDER by role.dataContainer.roleId ASC"),
        @NamedQuery(name = "Subscription.getForOwner", query = "SELECT sub FROM Subscription sub WHERE sub.owner.key=:ownerKey"),
        @NamedQuery(name = "Subscription.findUsageLicense", query = "SELECT lic FROM UsageLicense lic WHERE lic.user.dataContainer.userId = :userId AND lic.subscription.key = :subscriptionKey"),
        @NamedQuery(name = "Subscription.getSubscriptionsForMyCustomers", query = "SELECT DISTINCT s FROM Subscription s WHERE s.product.vendor = :offerer AND s.dataContainer.status IN (:states) ORDER BY s.dataContainer.subscriptionId ASC"),
        @NamedQuery(name = "Subscription.getSubscriptionsForMyBrokerCustomers", query = "SELECT DISTINCT sub FROM Subscription sub, Product prod, Product prodTemplate, Product resaleCopyTemplate, Organization organization, OrganizationToRole otr, OrganizationRole orgRole"
                + " WHERE sub.dataContainer.status IN (org.oscm.internal.types.enumtypes.SubscriptionStatus.ACTIVE, org.oscm.internal.types.enumtypes.SubscriptionStatus.PENDING)"
                + " AND sub.product.key =  prod.key"
                + " AND prod.dataContainer.type = org.oscm.internal.types.enumtypes.ServiceType.PARTNER_SUBSCRIPTION"
                + " AND prod.template = resaleCopyTemplate"
                + " AND resaleCopyTemplate.template = prodTemplate"
                + " AND prodTemplate.vendor = :offerer"
                + " AND prod.vendor.key= organization.key"
                + " AND otr.organization.key = organization.key"
                + " AND otr.organizationRole.key= orgRole.key"
                + " AND orgRole.dataContainer.roleName= org.oscm.internal.types.enumtypes.OrganizationRoleType.BROKER"
                + " ORDER BY sub.dataContainer.subscriptionId ASC"),
        @NamedQuery(name = "Subscription.numberOfUsableSubscriptionsForUser", query = "SELECT count(*) FROM Subscription sub"
                + " WHERE sub.dataContainer.status IN (:status)"
                + " AND sub.product.template =  :prodTemplate"
                + " AND (EXISTS (SELECT lic FROM UsageLicense lic WHERE sub.key=lic.subscription.key AND lic.user.key = :userKey) OR EXISTS(SELECT pu FROM PlatformUser pu, RoleAssignment ra WHERE pu.key=:userKey and ra.user.key=pu.key and ra.userRole.dataContainer.roleName=org.oscm.internal.types.enumtypes.UserRoleType.ORGANIZATION_ADMIN))"),
        @NamedQuery(name = "Subscription.isNotTerminatedSubscriptionAssignedToUnit", query = "SELECT sub FROM Subscription sub WHERE sub.userGroup.key = :unitKey AND sub.dataContainer.status != (:subscriptionStatus)") })
@BusinessKey(attributes = { "subscriptionId", "organizationKey" })
public class Subscription extends DomainObjectWithHistory<SubscriptionData> {

    private static final long serialVersionUID = -8733921553124979655L;

    public static final List<SubscriptionStatus> VISIBLE_SUBSCRIPTION_STATUS = Arrays
            .asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED,
                    SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED,
                    SubscriptionStatus.PENDING_UPD,
                    SubscriptionStatus.SUSPENDED_UPD);
    public static final Set<SubscriptionStatus> ASSIGNABLE_SUBSCRIPTION_STATUS = Collections
            .unmodifiableSet(EnumSet.of(SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED));

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays.asList(
                    LocalizedObjectTypes.SUBSCRIPTION_PROVISIONING_PROGRESS));

    public Subscription() {
        super();
        dataContainer = new SubscriptionData();
    }

    @Column(name = "uuid", unique = true, nullable = false)
    @Type(type = "pg-uuid")
    private UUID uuid;

    @Column(name = "eventPublished", nullable = false)
    private boolean eventPublished;

    /**
     * In order to form a complete business key the Organization key is needed
     * as explicit field inside this class. This field is also used as
     * JoinColumn for the n:1 relation to Organization.
     */
    @Column(name = "organizationKey", insertable = false, updatable = false, nullable = false)
    private long organizationKey;

    /**
     * n:1 relation to the organization the user belongs to. Has to be set, as
     * each user must belong to exactly one organization.<br>
     * CascadeType: NONE
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organizationKey")
    private Organization organization;

    /**
     * 1:n relation to the usage licenses of this subscription. The usage
     * licenses serve as association class from subscription to platform user,
     * which has a n:m multiplicity.<br>
     * CascadeType: ALL
     */
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy
    private List<UsageLicense> usageLicenses = new ArrayList<UsageLicense>();

    /**
     * 1:1 relation to the product this subscription is for. A subscription is
     * for exactly one product. An organization may have several subscriptions
     * of the same product.<br>
     * CascadeType: NONE
     */
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    /**
     * 1:1 relation to the product which a copy of the current one of the
     * subscription or the product selected for upgrade, when asynchronously
     * modify/upgrade subscription.<br>
     * CascadeType: NONE
     */
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "asyncTempProductKey")
    private Product asyncTempProduct;

    /**
     * n:1 relation to the marketplace the product has been subscribed over. In
     * case the marketplace does no longer exist, the relation is canceled.<br>
     * CascadeType: NONE
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Marketplace marketplace;

    /**
     * n:1 relation to the payment information record. CascadeType: NONE
     * Reflects that the payment information is stored in the context of a
     * subscription. The connection will be created at the creation time of a
     * subscription. One payment information record can be used by different
     * Subscriptions. A payment information record is not mandatory for a
     * subscription since it can be free of charge.
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private PaymentInfo paymentInfo;

    /**
     * n:1 relation to the billing contact. CascadeType: NONE Reflects that the
     * billing contact is stored in the context of a subscription. The
     * connection will be created at the creation time of a subscription. One
     * billing contact can be used by different Subscriptions. A billing contact
     * is not mandatory for a subscription since it can be free of charge.
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private BillingContact billingContact;

    /**
     * n:1 relation to the PlatformUser which is the owner of the subscription.
     * In case the owner does no longer exist, the relation is canceled.<br>
     * CascadeType: NONE
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private PlatformUser owner;

    /**
     * n:1 relation to the UserGroup which is the owner of the subscription. In
     * case the owner does no longer exist, the relation is canceled.<br>
     * CascadeType: NONE
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private UserGroup userGroup;

    /**
     * 1:n relation to the operation status Record of this subscription.
     * CascadeType: REMOVE
     */
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<OperationRecord> operationRecord = new ArrayList<OperationRecord>();

    /**
     * Adds a user to the subscription assigning the provided role or standard
     * authorities if no role is provided.
     * 
     * @param user
     *            the user to add
     * @param role
     *            the service role to set or <code>null</code> if default
     *            authorities should be used
     * @return the created usage license or null if the provided user is null
     * @throws UserAlreadyAssignedException
     *             Thrown in case the user is already assigned to the
     *             subscription
     */
    public UsageLicense addUser(PlatformUser user, RoleDefinition role)
            throws UserAlreadyAssignedException {
        if (user == null) {
            return null;
        }
        // check if already active or assigned license exists
        for (UsageLicense u : getUsageLicenses()) {
            if (user.equals(u.getUser())) {
                throw new UserAlreadyAssignedException(this.getSubscriptionId(),
                        user.getUserId());
            }
        }
        UsageLicense license = new UsageLicense();
        license.setRoleDefinition(role);
        license.setAssignmentDate(
                DateFactory.getInstance().getTransactionTime());
        license.setSubscription(this);
        license.setUser(user);
        usageLicenses.add(license);
        return license;
    }

    /**
     * Modify the roles of the provided user
     * 
     * @param usr
     *            the user whose roles should be modified
     * @param role
     *            the service role to set
     * @return the affected UsageLicense
     * @throws UserNotAssignedException
     *             if the user is not assigned to the subscription
     */
    public UsageLicense changeRole(PlatformUser usr, RoleDefinition role)
            throws UserNotAssignedException {
        if (usr == null)
            return null;
        for (UsageLicense u : getUsageLicenses()) {
            if (usr.equals(u.getUser())) {
                u.setRoleDefinition(role);
                return u;
            }
        }
        throw new UserNotAssignedException(this.getSubscriptionId(),
                usr.getUserId());
    }

    /**
     * Takes user out of the list of licensed users. The UsageLicense is not
     * deleted, as there is no reference to a data manager instance. The caller
     * has to to invoke the deletion explicitly.
     * 
     * @param usr
     * @return the affected UsageLicense
     */
    public UsageLicense revokeUser(PlatformUser usr) {
        if (usr == null)
            return null;

        // check if user is assigned or active
        Iterator<UsageLicense> licenses = getUsageLicenses().iterator();
        while (licenses.hasNext()) {
            UsageLicense u = licenses.next();
            if (usr.equals(u.getUser())) {
                licenses.remove();
                return u;
            }
        }
        return null;
    }

    /**
     * Returns the list of users currently is status ACTIVE or PENDING
     * 
     * @return
     */
    public List<PlatformUser> getActiveUsers() {
        ArrayList<PlatformUser> result = new ArrayList<PlatformUser>();
        for (UsageLicense lic : getUsageLicenses()) {
            result.add(lic.getUser());
        }
        return result;
    }

    /**
     * Binds the current subscription to a given Product. The corresponding
     * PriceModel and ParameterSet are not copied, so every subscription uses
     * the same pricing and parameters. If a dedicated pricing or parameter has
     * to be used, the required functionality must be added.
     * 
     * @param product
     *            the Product
     */
    public void bindToProduct(Product product) {
        setProduct(product);
    }

    /**
     * Returns true if a given user is assigned to this subscription and is not
     * in status REVOKED
     * 
     * @param usr
     * @return
     */
    public boolean hasUser(PlatformUser usr) {
        for (UsageLicense lic : getUsageLicenses()) {
            if (usr.equals(lic.getUser())) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------
    // Start of generated getters/setters/delegates

    public void setOrganization(Organization organization) {
        this.organization = organization;
        if (organization != null)
            setOrganizationKey(organization.getKey());
    }

    public Organization getOrganization() {
        return organization;
    }

    /**
     * Refer to {@link SubscriptionData#creationDate}
     */
    public Long getCreationDate() {
        return dataContainer.getCreationDate();
    }

    /**
     * Refer to {@link SubscriptionData#creationDate}
     */
    public void setCreationDate(Long creationDate) {
        dataContainer.setCreationDate(creationDate);
    }

    /**
     * Refer to {@link SubscriptionData#status}
     */
    public SubscriptionStatus getStatus() {
        return dataContainer.getStatus();
    }

    /**
     * Refer to {@link SubscriptionData#status}
     */
    public void setStatus(SubscriptionStatus status) {
        dataContainer.setStatus(status);
    }

    /**
     * Refer to {@link SubscriptionData#subscriptionId}
     */
    public String getSubscriptionId() {
        return dataContainer.getSubscriptionId();
    }

    /**
     * Refer to {@link SubscriptionData#subscriptionId}
     */
    public void setSubscriptionId(String subscriptionId) {
        dataContainer.setSubscriptionId(subscriptionId);
    }

    /**
     * Refer to {@link SubscriptionData#activationDate}
     */
    public Long getActivationDate() {
        return dataContainer.getActivationDate();
    }

    /**
     * Refer to {@link SubscriptionData#activationDate}
     */
    public void setActivationDate(Long activationDate) {
        dataContainer.setActivationDate(activationDate);
    }

    /**
     * Refer to {@link SubscriptionData#deactivationDate}
     */
    public Long getDeactivationDate() {
        return dataContainer.getDeactivationDate();
    }

    /**
     * Refer to {@link SubscriptionData#deactivationDate}
     */
    public void setDeactivationDate(Long deactivationDate) {
        dataContainer.setDeactivationDate(deactivationDate);
    }

    public void setUsageLicenses(List<UsageLicense> users) {
        this.usageLicenses = users;
    }

    public List<UsageLicense> getUsageLicenses() {
        return usageLicenses;
    }

    public List<OperationRecord> getOperationRecord() {
        return operationRecord;
    }

    public void setOperationRecord(List<OperationRecord> operationRecord) {
        this.operationRecord = operationRecord;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    public Product getAsyncTempProduct() {
        return asyncTempProduct;
    }

    public void setAsyncTempProduct(Product asyncTempProduct) {
        this.asyncTempProduct = asyncTempProduct;
    }

    public void setOrganizationKey(long organizationKey) {
        this.organizationKey = organizationKey;
    }

    public long getOrganizationKey() {
        return organizationKey;
    }

    public String getProductInstanceId() {
        return dataContainer.getProductInstanceId();
    }

    public void setProductInstanceId(String productInstanceId) {
        dataContainer.setProductInstanceId(productInstanceId);
    }

    public void setTimeoutMailSent(boolean timeoutMailSent) {
        dataContainer.setTimeoutMailSent(timeoutMailSent);
    }

    public boolean isTimeoutMailSent() {
        return dataContainer.isTimeoutMailSent();
    }

    /**
     * Determines the parameter set valid for this subscription via the product.
     * 
     * @return The parameter set valid for this subscription.
     */
    public ParameterSet getParameterSet() {
        return product.getParameterSet();
    }

    /**
     * Determines the price model valid for this subscription via the product.
     * 
     * @return The price model valid for this subscription.
     */
    public PriceModel getPriceModel() {
        return product.getPriceModel();
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        dataContainer.setPurchaseOrderNumber(purchaseOrderNumber);
    }

    public String getPurchaseOrderNumber() {
        return dataContainer.getPurchaseOrderNumber();
    }

    public void setAccessInfo(String accessInfo) {
        dataContainer.setAccessInfo(accessInfo);
    }

    public String getAccessInfo() {
        return dataContainer.getAccessInfo();
    }

    public void setBaseURL(String baseURL) {
        dataContainer.setBaseURL(baseURL);
    }

    public String getBaseURL() {
        return dataContainer.getBaseURL();
    }

    public void setLoginPath(String loginPath) {
        dataContainer.setLoginPath(loginPath);
    }

    public String getLoginPath() {
        return dataContainer.getLoginPath();
    }

    /**
     * Sets the marketplace this subscription has been made over.
     * 
     * @param marketplace
     *            the marketplace
     */
    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    /**
     * Returns the (optional) marketplace this subscription has been made over.
     * 
     * @return the marketplace
     */
    public Marketplace getMarketplace() {
        return marketplace;
    }

    /**
     * Returns if this subscription can be deleted.
     * 
     * @return true if and only if the status of the subscription is
     *         SubscriptionStatus.DEACTIVATED or SubscriptionStatus.EXPIRED or
     *         SubscriptionStatus.INVALID
     */
    public boolean isDeletable() {
        final SubscriptionStatus status = getStatus();
        return status == SubscriptionStatus.DEACTIVATED
                || status == SubscriptionStatus.EXPIRED
                || status == SubscriptionStatus.INVALID;
    }

    /**
     * Returns true if this subscription has a state that allows users to
     * subscribe.
     * 
     * @return boolean
     */
    public boolean isUsable() {
        return !isDeletable();
    }

    public UsageLicense getUsageLicenseForUser(PlatformUser user) {
        UsageLicense result = null;
        for (UsageLicense license : getUsageLicenses()) {
            if (license.getUser().getKey() == user.getKey()) {
                result = license;
            }
        }
        return result;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo param) {
        this.paymentInfo = param;
    }

    public BillingContact getBillingContact() {
        return billingContact;
    }

    public void setBillingContact(BillingContact billingContact) {
        this.billingContact = billingContact;
    }

    public void setOwner(PlatformUser owner) {
        this.owner = owner;
    }

    public PlatformUser getOwner() {
        return owner;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

    public void setCutOffDay(int cutoffDay) {
        dataContainer.setCutOffDay(cutoffDay);
    }

    public int getCutOffDay() {
        return dataContainer.getCutOffDay();
    }

    public void setSuccessMessage(String successMessage) {
        dataContainer.setSuccessMessage(successMessage);
    }

    public String getSuccessMessage() {
        return dataContainer.getSuccessMessage();
    }

    public void setExternal(boolean external) {
        dataContainer.setExternal(external);
    }

    public boolean isExternal() {
        return dataContainer.isExternal();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean getEventPublished() {
        return this.eventPublished;
    }

    public void setEventPublished(boolean eventPublished) {
        this.eventPublished = eventPublished;
    }

}
