/*******************************************************************************
 *                                                                              
ö *  Copyright FUJITSU LIMITED 2018
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * We call the one who registers at the SaaS portal a organization. We consider
 * this one also as the contracting party in the legal sense. Part of a
 * organization#s data is the payment information to be used for collecting the
 * subscription fees. (Entering the payment information is only mandatory if the
 * organization subscribes a chargeable product.)
 * 
 * @author schmid
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "organizationId" }))
@NamedQueries({
        @NamedQuery(name = "Organization.getAdministrators", query = "SELECT pu FROM PlatformUser pu, RoleAssignment ra WHERE pu.organization.key=:orgkey and ra.user.key=pu.key and ra.userRole.dataContainer.roleName=org.oscm.internal.types.enumtypes.UserRoleType.ORGANIZATION_ADMIN"),
        @NamedQuery(name = "Organization.findByBusinessKey", query = "SELECT c FROM Organization c WHERE c.dataContainer.organizationId=:organizationId"),
        @NamedQuery(name = "Organization.getAllOrganizations", query = "SELECT c FROM Organization c"),
        @NamedQuery(name = "Organization.getAllSuppliers", query = "SELECT c FROM Organization c INNER JOIN c.grantedRoles otr INNER JOIN otr.organizationRole r WHERE r.dataContainer.roleName = org.oscm.internal.types.enumtypes.OrganizationRoleType.SUPPLIER"),
        @NamedQuery(name = "Organization.getForSupplierKey", query = "SELECT c FROM Organization c, OrganizationReference r WHERE r.target=c AND r.sourceKey=:supplierKey AND r.dataContainer.referenceType = :referenceType ORDER BY c.key ASC"),
        @NamedQuery(name = "Organization.getForSupplierKeyAndProduct", query = "SELECT c FROM Organization c, OrganizationReference r, Product p WHERE r.target=c AND r.sourceKey=:supplierKey AND r.dataContainer.referenceType = org.oscm.domobjects.enums.OrganizationReferenceType.SUPPLIER_TO_CUSTOMER AND c = p.targetCustomer AND p.template = :product AND NOT EXISTS (SELECT s.key FROM Subscription s WHERE s.product = p)"),
        @NamedQuery(name = "Organization.getForOffererKeyAndSubscriptionId", query = "SELECT c FROM Organization c, OrganizationReference r, Subscription sub WHERE r.target=c AND r.sourceKey=:offererKey AND r.dataContainer.referenceType IN (org.oscm.domobjects.enums.OrganizationReferenceType.SUPPLIER_TO_CUSTOMER,org.oscm.domobjects.enums.OrganizationReferenceType.BROKER_TO_CUSTOMER,org.oscm.domobjects.enums.OrganizationReferenceType.RESELLER_TO_CUSTOMER) AND sub.organization = c AND sub.dataContainer.subscriptionId = :subscriptionId AND sub.dataContainer.status IN (:states)"),
        @NamedQuery(name = "Organization.findOrganizationsByIdAndRole", query = "SELECT DISTINCT o FROM Organization o, OrganizationToRole o2r, OrganizationRole r where o.dataContainer.organizationId like :organizationId and o = o2r.organization and o2r.organizationRole = r and r.dataContainer.roleName IN (:organizationRoleTypes)"),
        @NamedQuery(name = "Organization.findOrganizationsByGroupId", query = "SELECT DISTINCT o FROM Organization o where o.dataContainer.groupid like :groupId "),
        @NamedQuery(name = "Organization.findOrganizationsByName", query = "SELECT o FROM Organization o where o.dataContainer.name like :name "),
        @NamedQuery(name = "Organization.countOrgsWithSameDN", query = "SELECT COUNT (DISTINCT organization) FROM Organization organization WHERE organization.dataContainer.distinguishedName = :distinguishedName AND organization != :organization"),
        @NamedQuery(name = "Organization.getLdapManagedOrganizations", query = "SELECT organization FROM Organization organization WHERE organization.dataContainer.remoteLdapActive=true"),
        @NamedQuery(name = "Organization.getOrgsForDN", query = "SELECT DISTINCT organization FROM Organization organization WHERE organization.dataContainer.distinguishedName IN (:dn)") })
@BusinessKey(attributes = { "organizationId" })
public class Organization extends DomainObjectWithHistory<OrganizationData> {

    private static final long serialVersionUID = 1L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays
                    .asList(LocalizedObjectTypes.ORGANIZATION_DESCRIPTION));

    public Organization() {
        super();
        dataContainer = new OrganizationData();
    }

    /**
     * The list with all active setting types of the current organization which
     * define a mapping of an LDAP attribute to a user property.
     * 
     * @see OrganizationRoleType#CUSTOMER
     */
    @Transient
    private List<SettingType> ldapUserAttributes;

    /**
     * 1:n relation to the owned marketplaces: CascadeType: REMOVE
     * 
     * @see OrganizationRoleType#SUPPLIER
     */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Marketplace> marketplaces = new ArrayList<>();

    /**
     * 1:n relation to PlatformUser: the users registered for the organization.
     * No cascading option.
     */
    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    @OrderBy
    private List<PlatformUser> platformUsers = new ArrayList<>();

    /**
     * 1:n relation to Subscription: the subscriptions of the organization.
     * CascadeType: REMOVE
     * 
     * @see OrganizationRoleType#CUSTOMER
     */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<Subscription> subscriptions = new ArrayList<>();

    /**
     * 1:n relation to OrganizationReference which references the sources (this
     * organization is the target).
     */
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "target", fetch = FetchType.LAZY)
    @OrderBy
    private List<OrganizationReference> sources = new ArrayList<>();

    /**
     * 1:n relation to OrganizationReference which references the targets (this
     * organization is the source).
     */
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "source", fetch = FetchType.LAZY)
    @OrderBy
    private List<OrganizationReference> targets = new ArrayList<>();

    /**
     * 1:n relation to Product: the organization is the supplier of the
     * marketing product. CascadeType: REMOVE
     * 
     * @see OrganizationRoleType#SUPPLIER
     */
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<Product> products = new ArrayList<>();

    /**
     * 1:n relation to TechnicalProduct: the organization is technology provider
     * of the technical product. CascadeType: REMOVE
     * 
     * @see OrganizationRoleType#TECHNOLOGY_PROVIDER
     */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<TechnicalProduct> technicalProducts = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private Set<OrganizationToRole> grantedRoles = new HashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<OrganizationSetting> organizationSettings = new ArrayList<>();

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    @OrderBy
    private List<TriggerDefinition> triggerDefinitions = new ArrayList<>();

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    @OrderBy
    private List<UdaDefinition> udaDefinitions = new ArrayList<>();

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    @OrderBy
    private List<PSPAccount> pspAccounts = new ArrayList<>();

    /**
     * @see OrganizationRoleType#SUPPLIER
     */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrganizationToCountry> organizationToCountries = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private SupportedCountry domicileCountry;

    /**
     * @see OrganizationRoleType#SUPPLIER
     */
    @OneToMany(mappedBy = "owningOrganization", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<VatRate> definedVatRates = new ArrayList<>();

    /**
     * The billing contact information data stored for the current organization.
     */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<BillingContact> billingContacts = new ArrayList<>();

    /**
     * The payment information stored for the current organization.
     */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<PaymentInfo> paymentInfos = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<MarketplaceToOrganization> marketplaceToOrganizations = new ArrayList<>();

    @OneToOne(optional = true, cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private RevenueShareModel operatorPriceModel;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<UserGroup> userGroups = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<MarketplaceAccess> marketplaceAccesses = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "tenant_tkey")
    private Tenant tenant;

    /**
     * OrganizationID of the platform operator. The platform operator is created
     * by the setup script.
     */
    public static final String PLATFORM_OPERATOR = "PLATFORM_OPERATOR";

    /*
     * Fill registration date (if not already set)
     */
    @PrePersist
    public void fillRegistrationDate() {
        if (getRegistrationDate() == 0) {
            setRegistrationDate(System.currentTimeMillis());
        }
    }

    /**
     * Refer to {@link OrganizationData#address}
     */
    public String getAddress() {
        return dataContainer.getAddress();
    }

    /**
     * Refer to {@link OrganizationData#email}
     */
    public String getEmail() {
        return dataContainer.getEmail();
    }

    /**
     * Refer to {@link OrganizationData#email}
     */
    public void setEmail(String email) {
        dataContainer.setEmail(email);
    }

    /**
     * Refer to {@link OrganizationData#name}
     */
    public String getName() {
        return dataContainer.getName();
    }

    /**
     * Refer to {@link OrganizationData#phone}
     */
    public String getPhone() {
        return dataContainer.getPhone();
    }

    /**
     * Refer to {@link OrganizationData#url}
     */
    public String getUrl() {
        return dataContainer.getUrl();
    }

    /**
     * Refer to {@link OrganizationData#address}
     */
    public void setAddress(String address) {
        dataContainer.setAddress(address);
    }

    /**
     * Refer to {@link OrganizationData#name}
     */
    public void setName(String name) {
        dataContainer.setName(name);
    }

    /**
     * Refer to {@link OrganizationData#phone}
     */
    public void setPhone(String phone) {
        dataContainer.setPhone(phone);
    }

    /**
     * Refer to {@link OrganizationData#url}
     */
    public void setUrl(String url) {
        dataContainer.setUrl(url);
    }

    /**
     * Refer to {@link OrganizationData#organizationId}
     */
    public String getOrganizationId() {
        return dataContainer.getOrganizationId();
    }

    /**
     * Refer to {@link OrganizationData#deregistrationDate}
     */
    public Long getDeregistrationDate() {
        return dataContainer.getDeregistrationDate();
    }

    /**
     * Refer to {@link OrganizationData#registrationDate}
     */
    public long getRegistrationDate() {
        return dataContainer.getRegistrationDate();
    }

    /**
     * Refer to {@link OrganizationData#organizationId}
     */
    public void setOrganizationId(String organizationId) {
        dataContainer.setOrganizationId(organizationId);
    }

    /**
     * Refer to {@link OrganizationData#deregistrationDate}
     */
    public void setDeregistrationDate(Long deregistrationDate) {
        dataContainer.setDeregistrationDate(deregistrationDate);
    }

    /**
     * Refer to {@link OrganizationData#registrationDate}
     */
    public void setRegistrationDate(long registrationDate) {
        dataContainer.setRegistrationDate(registrationDate);
    }

    /**
     * Refer to {@link OrganizationData#locale}
     */
    public String getLocale() {
        return dataContainer.getLocale();
    }

    /**
     * Refer to {@link OrganizationData#locale}
     */
    public void setLocale(String locale) {
        dataContainer.setLocale(locale);
    }

    public void setPlatformUsers(List<PlatformUser> platformUsers) {
        this.platformUsers = platformUsers;
    }

    public void addPlatformUser(PlatformUser platformUser) {
        platformUsers.add(platformUser);
    }
    
    /**
     * Returns a list of all platform users of this organization. The list does
     * not include users that have been created on the behalf of other users.
     * These users should be excluded in the UI.
     * 
     * @return List of platform users
     */
    public List<PlatformUser> getVisiblePlatformUsers() {
        List<PlatformUser> result = new ArrayList<>();
        for (PlatformUser user : getPlatformUsers()) {
            if (!user.isOnBehalfUser()) {
                result.add(user);
            }
        }
        return result;
    }

    /**
     * Returns all platform users of this organization. The result list includes
     * users that have been created on the behalf of other users also. These
     * users should be excluded in the UI.
     * 
     * @return List of platform users
     */
    public List<PlatformUser> getPlatformUsers() {
        return platformUsers;
    }

    /**
     * Returns all administrators of this organization. The result list includes
     * users that have been created on the behalf of other users also. These
     * users should be excluded in the UI.
     * 
     * @return List of platform users
     */
    public List<PlatformUser> getOrganizationAdmins() {
        List<PlatformUser> result = new ArrayList<>();
        for (PlatformUser platformUser : getPlatformUsers()) {
            if (platformUser.isOrganizationAdmin()) {
                result.add(platformUser);
            }
        }
        return result;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    /**
     * Returns a list of all usable subscriptions. A subscription is considered
     * usable if a user can eventually subscribe to it.
     */
    public List<Subscription> getUsableSubscriptions() {
        List<Subscription> usableSubscriptions = new ArrayList<>(subscriptions);
        for (Iterator<Subscription> i = usableSubscriptions.iterator(); i
                .hasNext();) {
            Subscription subscription = i.next();
            if (!subscription.isUsable()) {
                i.remove();
            }
        }
        return usableSubscriptions;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<TechnicalProduct> getTechnicalProducts() {
        return technicalProducts;
    }

    public void setTechnicalProducts(List<TechnicalProduct> technicalProducts) {
        this.technicalProducts = technicalProducts;
    }

    public List<Organization> getTechnologyProviders() {
        List<Organization> result = new ArrayList<>(getSources().size());
        for (OrganizationReference ref : getSources()) {
            if (ref.getReferenceType() == OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER) {
                result.add(ref.getSource());
            }
        }
        return result;
    }

    public List<Organization> getGrantedSuppliers() {
        List<Organization> result = new ArrayList<>(getTargets().size());
        for (OrganizationReference ref : getTargets()) {
            if (ref.getReferenceType() == OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER) {
                result.add(ref.getTarget());
            }
        }
        return result;
    }

    public List<Organization> getCustomersOfSupplier() {
        List<Organization> result = new ArrayList<>(getTargets().size());
        for (OrganizationReference ref : getTargets()) {
            if (ref.getReferenceType() == OrganizationReferenceType.SUPPLIER_TO_CUSTOMER) {
                result.add(ref.getTarget());
            }
        }
        return result;
    }

    public List<Organization> getCustomersOfBroker() {
        List<Organization> result = new ArrayList<>(getTargets().size());
        for (OrganizationReference ref : getTargets()) {
            if (ref.getReferenceType() == OrganizationReferenceType.BROKER_TO_CUSTOMER) {
                result.add(ref.getTarget());
            }
        }
        return result;
    }

    public List<Organization> getCustomersOfReseller() {
        List<Organization> result = new ArrayList<>(getTargets().size());
        for (OrganizationReference ref : getTargets()) {
            if (ref.getReferenceType() == OrganizationReferenceType.RESELLER_TO_CUSTOMER) {
                result.add(ref.getTarget());
            }
        }
        return result;
    }

    public List<Organization> getSuppliersOfCustomer() {
        List<Organization> result = new ArrayList<>(getSources().size());
        for (OrganizationReference ref : getSources()) {
            if (ref.getReferenceType() == OrganizationReferenceType.SUPPLIER_TO_CUSTOMER) {
                result.add(ref.getSource());
            }
        }
        return result;
    }

    public List<Organization> getVendorsOfCustomer() {
        List<Organization> result = new ArrayList<>(getSources().size());
        for (OrganizationReference ref : getSources()) {
            if ((ref.getReferenceType() == OrganizationReferenceType.SUPPLIER_TO_CUSTOMER)
                    || (ref.getReferenceType() == OrganizationReferenceType.BROKER_TO_CUSTOMER)
                    || (ref.getReferenceType() == OrganizationReferenceType.RESELLER_TO_CUSTOMER)) {
                result.add(ref.getSource());
            }
        }
        return result;
    }

    public Set<OrganizationToRole> getGrantedRoles() {
        return grantedRoles;
    }

    public void setGrantedRoles(Set<OrganizationToRole> grantedRoles) {
        this.grantedRoles = grantedRoles;
    }

    public Set<OrganizationRoleType> getGrantedRoleTypes() {
        Set<OrganizationRoleType> result = new HashSet<>();
        for (OrganizationToRole orgToRole : getGrantedRoles()) {
            result.add(orgToRole.getOrganizationRole().getRoleName());
        }
        return result;
    }

    /**
     * Checks if the organization has been granted the given role.
     * 
     * Any organization has a role CUSTOMER
     * 
     * @param roleToCheckFor
     *            The role the organization is supposed to be granted.
     * @return <code>true</code> in case the organization has been granted the
     *         given role, <code>false</code> otherwise.
     */
    public boolean hasRole(OrganizationRoleType roleToCheckFor) {
        for (OrganizationToRole orgToRole : grantedRoles) {
            if (orgToRole.getOrganizationRole()
                    .getRoleName() == roleToCheckFor) {
                return true;
            }
        }
        return false;
    }

    public void setOrganizationSettings(
            List<OrganizationSetting> organizationSettings) {
        ldapUserAttributes = null;
        this.organizationSettings = organizationSettings;
    }

    public List<OrganizationSetting> getOrganizationSettings() {
        return organizationSettings;
    }

    /**
     * Checks if a remote LDAP has been activated for the organization.
     * 
     * @return <code>true</code> in case a remote LDAP has been activated for
     *         the organization, <code>false</code> otherwise.
     */
    public boolean isRemoteLdapActive() {
        return dataContainer.isRemoteLdapActive();
    }

    /**
     * Defines if the organization supports users managed in a remote LDAP
     * system or not.
     * 
     * @param remoteLdapActive
     *            <code>true</code> in case the organization users are managed
     *            in an external LDAP system, <code>false</code> otherwise.
     */
    public void setRemoteLdapActive(boolean remoteLdapActive) {
        dataContainer.setRemoteLdapActive(remoteLdapActive);
    }

    /**
     * @return The list with all active setting types of the current
     *         Organization which define a mapping of an LDAP attribute to a
     *         user property.
     */
    public List<SettingType> getLdapUserAttributes() {
        if (ldapUserAttributes == null) {
            if (getOrganizationSettings() != null) {
                ldapUserAttributes = new ArrayList<>();
                for (OrganizationSetting setting : getOrganizationSettings()) {
                    if (SettingType.LDAP_ATTRIBUTES
                            .contains(setting.getSettingType())) {
                        ldapUserAttributes.add(setting.getSettingType());
                    }
                }
            }
        }
        return ldapUserAttributes;
    }

    /**
     * Returns the organization reference to payment type objects valid in the
     * context of the defining organization with the given identifier.
     * 
     * @param definingOrgId
     *            The identifier of the defining organization.
     * @return The payment references.
     */
    public List<OrganizationRefToPaymentType> getPaymentTypes(
            String definingOrgId) {
        List<OrganizationRefToPaymentType> result = new ArrayList<>();
        for (OrganizationReference defOrgRef : getSources()) {
            String organizationId = defOrgRef.getSource().getOrganizationId();
            if (organizationId != null
                    && organizationId.equals(definingOrgId)) {
                // get org to payment ref and add to list
                result.addAll(defOrgRef.getPaymentTypes());
            }
        }
        return result;
    }

    /**
     * Reads the payment types added for this organization in the context of a
     * certain role
     * 
     * @param usedAsDefault
     *            <code>true</code> if the configured default payment types
     *            should be returned; <code>false</code> if the available
     *            payment types should be returned
     * @param role
     *            the context role - a customer can have available payment types
     *            as well as a supplier
     * @param definingOrgId
     *            The identifier of the defining organization.
     * @return the list of <code>OrganizationToPaymentType</code>
     */
    public List<OrganizationRefToPaymentType> getPaymentTypes(
            boolean usedAsDefault, OrganizationRoleType role,
            String definingOrgId) {
        List<OrganizationRefToPaymentType> types = getPaymentTypes(
                definingOrgId);
        List<OrganizationRefToPaymentType> result = new ArrayList<>();
        for (OrganizationRefToPaymentType ref : types) {
            if ((!usedAsDefault
                    || (usedAsDefault && ref.isUsedAsDefault() == true))
                    && ref.getOrganizationRole().getRoleName() == role) {
                result.add(ref);
            }
        }
        return result;
    }

    /**
     * Reads the service default payment types added for this organization in
     * the context of a certain role.
     * 
     * @return the list of <code>OrganizationRefToPaymentType</code>
     */
    public List<OrganizationRefToPaymentType> getDefaultServicePaymentTypes() {

        List<OrganizationRefToPaymentType> types = getPaymentTypes(
                OrganizationRoleType.PLATFORM_OPERATOR.name());
        List<OrganizationRefToPaymentType> result = new ArrayList<>();
        for (OrganizationRefToPaymentType ref : types) {
            if ((ref.isUsedAsServiceDefault() == true)
                    && (OrganizationRoleType.SUPPLIER
                            .equals(ref.getOrganizationRole().getRoleName())
                            || OrganizationRoleType.RESELLER.equals(
                                    ref.getOrganizationRole().getRoleName()))) {
                result.add(ref);
            }
        }
        return result;
    }

    public void setDistinguishedName(String distinguishedName) {
        dataContainer.setDistinguishedName(distinguishedName);
    }

    public String getDistinguishedName() {
        return dataContainer.getDistinguishedName();
    }

    public String getSupportEmail() {
        return dataContainer.getSupportEmail();
    }

    public void setSupportEmail(String supportEmail) {
        dataContainer.setSupportEmail(supportEmail);
    }

    public List<TriggerDefinition> getTriggerDefinitions() {
        return triggerDefinitions;
    }

    public void setTriggerDefinitions(
            List<TriggerDefinition> triggerDefinitions) {
        this.triggerDefinitions = triggerDefinitions;
    }

    /**
     * Returns the trigger definition stored for the current organization that
     * is configured to suspend the processing of BES. If more than one entry
     * should be there, a system exception will be thrown.
     * 
     * @param type
     *            The type the trigger must have.
     * 
     * @return The suspending trigger definition, <code>null</code> if none
     *         exists.
     */
    public TriggerDefinition getSuspendingTriggerDefinition(TriggerType type) {
        TriggerDefinition result = null;
        for (TriggerDefinition td : this.triggerDefinitions) {
            if (td.getType() == type && td.isSuspendProcess()) {
                if (result != null) {
                    throw new SaaSSystemException(String.format(
                            "More than one suspending trigger definition found for organization '%s'. Data is inconsistent, operation aborted.",
                            String.valueOf(getKey())));
                }
                result = td;
            }
        }
        return result;
    }

    @Override
    String toStringAttributes() {
        return String.format(", organizationId='%s'", getOrganizationId());
    }

    public void setUdaDefinitions(List<UdaDefinition> udaDefinitions) {
        this.udaDefinitions = udaDefinitions;
    }

    public List<UdaDefinition> getUdaDefinitions() {
        return udaDefinitions;
    }

    public Set<Long> getUdaDefinitionKeysWithTargetType(UdaTargetType type) {
        List<UdaDefinition> definitions = getUdaDefinitions();
        Set<Long> result = new HashSet<>();
        for (UdaDefinition def : definitions) {
            if (def.getTargetType() == type) {
                result.add(Long.valueOf(def.getKey()));
            }
        }
        return result;
    }

    /**
     * Sets the domicile country for this organization. A supplier may restrict
     * the list of supported countries this organization can choose from.
     * 
     * @param country
     */
    public void setDomicileCountry(SupportedCountry country) {
        domicileCountry = country;
    }

    /**
     * Returns the domicile of this organization.
     * 
     * @return String The country code in ISO 3166.
     */
    public String getDomicileCountryCode() {
        if (domicileCountry == null) {
            return null;
        }
        return domicileCountry.getCountryISOCode();
    }

    /**
     * Returns the domicile country for this organization.
     * 
     * @return OrganizationToCountry
     */
    public SupportedCountry getDomicileCountry() {
        return domicileCountry;
    }

    /**
     * Returns the list of OrganizationToCountry domain objects.
     * 
     * @return List<OrganizationToCountry>
     */
    public List<OrganizationToCountry> getOrganizationToCountries() {
        return organizationToCountries;
    }

    /**
     * Returns the list of country codes that this organization supports.
     * 
     * @return List<String>
     */
    public List<String> getSupportedCountryCodes() {
        List<String> result = new ArrayList<>();
        for (OrganizationToCountry orgToCountry : organizationToCountries) {
            result.add(orgToCountry.getCode());
        }
        return result;
    }

    /**
     * Adds the given country to the list of supported countries.
     * 
     * @param country
     *            SupportedCountry to be added.
     */
    public void setSupportedCountry(SupportedCountry country) {
        if (!isCountrySupported(country.getCountryISOCode())) {
            OrganizationToCountry orgToCountry = new OrganizationToCountry();
            orgToCountry.setOrganization(this);
            orgToCountry.setSupportedCountry(country);
            organizationToCountries.add(orgToCountry);
        }
    }

    /**
     * Returns true if the given country code is supported by this organization.
     * 
     * @param countryCode
     *            The country code in ISO 3166.
     * @return boolean
     */
    public boolean isCountrySupported(String countryCode) {
        return getOrganizationToCountry(countryCode) != null;
    }

    /**
     * Returns the OrganizationToCountry object for the given country code.
     * 
     * @param countryCode
     *            The country code in ISO 3166.
     * @return OrganizationToCountry
     */
    public OrganizationToCountry getOrganizationToCountry(String countryCode) {
        for (OrganizationToCountry country : organizationToCountries) {
            if (country.getCode().equalsIgnoreCase(countryCode)) {
                return country;
            }
        }
        return null;
    }

    /**
     * Sets the defined VAT rates of the organization.
     * 
     * @param vatRates
     *            The VAT rates to set.
     */
    public void setDefinedVatRates(List<VatRate> vatRates) {
        this.definedVatRates = vatRates;
    }

    /**
     * Returns the list of VAT rates that are defined from the organization.
     * 
     * @return the list of VAT rates that are defined from the organization.
     */
    public List<VatRate> getDefinedVatRates() {
        return definedVatRates;
    }

    public void setSources(List<OrganizationReference> sources) {
        this.sources = sources;
    }

    public List<OrganizationReference> getSources() {
        return sources;
    }

    public void setTargets(List<OrganizationReference> targets) {
        this.targets = targets;
    }

    public List<OrganizationReference> getTargets() {
        return targets;
    }

    public void setMarketplaces(List<Marketplace> marketplaces) {
        this.marketplaces = marketplaces;
    }

    public List<Marketplace> getMarketplaces() {
        return marketplaces;
    }

    /**
     * Returns the referenced source organization for the given referenced type.
     * 
     * @param orgRefType
     *            The reference type requested.
     * @return The organization references.
     */
    public List<OrganizationReference> getSourcesForType(
            OrganizationReferenceType orgRefType) {
        List<OrganizationReference> refsToSource = getSources();
        List<OrganizationReference> result = new ArrayList<>();
        for (OrganizationReference currentRef : refsToSource) {
            if (currentRef.getReferenceType() == orgRefType) {
                result.add(currentRef);
            }
        }
        return result;
    }

    public List<BillingContact> getBillingContacts() {
        return billingContacts;
    }

    public void setBillingContacts(List<BillingContact> billingContacts) {
        this.billingContacts = billingContacts;
    }

    /**
     * Evaluates the organization's subscriptions and returns those, that have
     * the specified status and that use the specified payment type.
     * 
     * @param states
     *            The status set of the subscription.
     * @param paymentType
     *            The payment type that has to be checked for.
     * @return The subscriptions matching the conditions.
     */
    public List<Subscription> getSubscriptionsForStateAndPaymentType(
            Set<SubscriptionStatus> states, String paymentType) {
        List<Subscription> result = new ArrayList<>();
        List<Subscription> subs = getSubscriptions();
        for (Subscription subscription : subs) {
            if (states.contains(subscription.getStatus())
                    && subscription.getPaymentInfo() != null
                    && subscription.getPaymentInfo().getPaymentType()
                            .getPaymentTypeId().equals(paymentType)) {
                result.add(subscription);
            }
        }
        return result;
    }

    /**
     * Returns a list of all usable subscriptions for the given product. A
     * subscription is considered usable if a user can eventually subscribe to
     * it.
     */
    public List<Subscription> getUsableSubscriptionsForProduct(
            Product template) {
        if (template.isCopy()) {
            throw new IllegalArgumentException("Only templates allowed");
        }

        List<Subscription> result = new ArrayList<>();
        for (Subscription subscription : getUsableSubscriptions()) {
            // A product copy for a subscription to a broker/reseller product
            // refers to the resale product copy, not to the product template!
            if (subscription.getProduct().getProductTemplate()
                    .equals(template)) {
                result.add(subscription);
            }
        }

        return result;
    }

    /**
     * Returns TRUE if this organization is acting on behalf of the given
     * organization.
     */
    public boolean isActingOnBehalf(Organization customer) {
        for (OrganizationReference ref : customer.getSourcesForType(
                OrganizationReferenceType.ON_BEHALF_ACTING)) {
            if (ref.getSource().equals(this)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return a list of acting on behalf users of the customer organization.
     */
    public List<PlatformUser> getOnBehalfUsersFor(Organization customer) {
        List<PlatformUser> userList = getOnBehalfUsers(customer);
        filterOnBehalfUserList(userList);
        return userList;
    }

    private List<PlatformUser> getOnBehalfUsers(Organization customer) {
        List<PlatformUser> userList = new ArrayList<>();
        for (PlatformUser user : customer.getPlatformUsers()) {
            if (user.isOnBehalfUser()) {
                userList.add(user);
            }
        }
        return userList;
    }

    /**
     * Filter all user which are not belonging to the customer organization.
     * 
     * @param userList
     *            the user list to filter, could contain user from other
     *            organizations
     */
    private void filterOnBehalfUserList(List<PlatformUser> userList) {
        Iterator<PlatformUser> iterator = userList.iterator();
        while (iterator.hasNext()) {
            PlatformUser onbehalfUser = iterator.next();
            if (!getPlatformUsers()
                    .contains(onbehalfUser.getMaster().getMasterUser())) {
                iterator.remove();
            }

        }
    }

    public void setPaymentInfos(List<PaymentInfo> paymentInfos) {
        this.paymentInfos = paymentInfos;
    }

    public List<PaymentInfo> getPaymentInfos() {
        return paymentInfos;
    }

    /**
     * Determines if the current organization may allow its customers to use the
     * specified payment type.
     * 
     * @param type
     *            The payment type to check for.
     * @return <code>true</code> in case the type is supported,
     *         <code>false</code> otherwise.
     */
    public boolean canPermitPaymentType(String type) {
        for (OrganizationRefToPaymentType ref : getPaymentTypes(false,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.PLATFORM_OPERATOR.name())) {
            if (ref.getPaymentType().getPaymentTypeId().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public void setMarketplaceToOrganizations(
            List<MarketplaceToOrganization> marketplaceToOrganizations) {
        this.marketplaceToOrganizations = marketplaceToOrganizations;
    }

    public List<MarketplaceToOrganization> getMarketplaceToOrganizations() {
        return marketplaceToOrganizations;
    }

    public void setPspAccounts(List<PSPAccount> pspAccounts) {
        this.pspAccounts = pspAccounts;
    }

    public List<PSPAccount> getPspAccounts() {
        return pspAccounts;
    }

    /**
     * Returns the PSP account registered for the organization which refers to
     * the specified PSP.
     * 
     * @param psp
     *            The PSP to find the account for.
     * @return The registered PSP account or <code>null</code> if none exists.
     */
    public PSPAccount getPspAccountForPsp(PSP psp) {
        for (PSPAccount account : pspAccounts) {
            if (account.getPsp().equals(psp)) {
                return account;
            }
        }
        return null;
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

    /**
     * Retrieves the udaDefinitions that will be readable by the specify role
     * 
     * @param role
     * @return list of udaDefinitions
     */
    public List<UdaDefinition> getReadableUdaDefinitions(
            OrganizationRoleType role) {
        // get the list of defintions
        List<UdaDefinition> definitions = getUdaDefinitions();
        // check which definition is readable by passed role (by configuration
        // type)
        List<UdaDefinition> result = new ArrayList<>();
        for (UdaDefinition def : definitions) {
            if (def.getConfigurationType().canRead(role)) {
                result.add(def);
            }
        }
        // return the filtered list
        return result;

    }

    public List<UdaDefinition> getMandatoryUdaDefinitions() {
        // get the list of defintions
        List<UdaDefinition> definitions = getUdaDefinitions();
        // get all the mandatory uda definitions
        List<UdaDefinition> result = new ArrayList<>();
        for (UdaDefinition def : definitions) {
            if (def.getConfigurationType().isMandatory()) {
                result.add(def);
            }
        }
        // return the filtered list
        return result;
    }

    public OrganizationRoleType getVendorRoleForPaymentConfiguration() {
        OrganizationRoleType role = null;
        Set<OrganizationRoleType> types = getGrantedRoleTypes();
        if (types.contains(OrganizationRoleType.SUPPLIER)) {
            role = OrganizationRoleType.SUPPLIER;
        } else if (getGrantedRoleTypes()
                .contains(OrganizationRoleType.RESELLER)) {
            role = OrganizationRoleType.RESELLER;
        }
        return role;
    }

    /**
     * Returns the reference to the passed {@link Organization} of the passed
     * {@link OrganizationReferenceType} or <code>null</code> if it doesn't
     * exist.
     * 
     * @param customer
     *            the customer {@link Organization}
     * @param type
     *            the required {@link OrganizationReferenceType}
     * @return the {@link OrganizationReference} ore <code>null</code>
     */
    public OrganizationReference getCustomerReference(Organization customer,
            OrganizationReferenceType type) {
        List<OrganizationReference> list = getTargets();
        for (OrganizationReference ref : list) {
            if (ref.getReferenceType() == type && ref.getTarget() == customer) {
                return ref;
            }
        }
        return null;
    }

    public void setCutOffDay(int cutOffDay) {
        dataContainer.setCutOffDay(cutOffDay);
    }

    public int getCutOffDay() {
        return dataContainer.getCutOffDay();
    }

    public RevenueShareModel getOperatorPriceModel() {
        return operatorPriceModel;
    }

    public void setOperatorPriceModel(RevenueShareModel operatorPriceModel) {
        this.operatorPriceModel = operatorPriceModel;
    }

    public List<UserGroup> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }

    public boolean hasAtLeastOneRole(OrganizationRoleType... roles) {
        for (OrganizationRoleType role : roles) {
            if (this.hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public List<MarketplaceAccess> getMarketplaceAccesses() {
        return marketplaceAccesses;
    }

    public void setMarketplaceAccesses(
            List<MarketplaceAccess> marketplaceAccesses) {
        this.marketplaceAccesses = marketplaceAccesses;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
    
    public String getGroupId() {
        return dataContainer.getGroupId();
    }

    public void setGroupId(String groupId) {
        dataContainer.setGroupId(groupId);
    }
}
