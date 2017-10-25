/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                 
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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.types.enumtypes.ProvisioningType;

/**
 * This object represents a technical product that is provided via the platform
 * as service.
 * 
 * @author Mike J&auml;ger
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "technicalProductId", "organizationKey" }) )
@NamedQueries({
        @NamedQuery(name = "TechnicalProduct.findByBusinessKey", query = "SELECT obj FROM TechnicalProduct obj WHERE obj.dataContainer.technicalProductId = :technicalProductId AND obj.organizationKey = :organizationKey"),
        @NamedQuery(name = "TechnicalProduct.getTechnicalProductsById", query = "SELECT p FROM TechnicalProduct p WHERE p.dataContainer.technicalProductId = :technicalProductId") })
@BusinessKey(attributes = { "technicalProductId", "organizationKey" })
public class TechnicalProduct
        extends DomainObjectWithHistory<TechnicalProductData> {

    private static final long serialVersionUID = -3908268076011224319L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays.asList(
                    LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC,
                    LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC,
                    LocalizedObjectTypes.PRODUCT_LICENSE_DESC));

    public TechnicalProduct() {
        super();
        dataContainer = new TechnicalProductData();
    }

    /**
     * In order to form a complete business key the Organization key is needed
     * as explicit field inside this class. This field is also used as
     * JoinColumn for the n:1 relation to Organization.
     */
    @Column(name = "organizationKey", insertable = false, updatable = false, nullable = false)
    private long organizationKey;

    /**
     * n:1 relation to the organization the technical product belongs to. Has to
     * be set, as each technical product must belong to exactly one
     * organization.<br>
     * CascadeType: NONE
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organizationKey")
    private Organization organization;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "technicalProduct", fetch = FetchType.LAZY)
    @OrderBy
    private List<Event> events = new ArrayList<Event>();

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "technicalProduct", fetch = FetchType.LAZY)
    @OrderBy
    private List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "technicalProduct", fetch = FetchType.LAZY)
    @OrderBy
    private List<TechnicalProductTag> tags = new ArrayList<TechnicalProductTag>();

    @OneToMany(cascade = {}, mappedBy = "technicalProduct", fetch = FetchType.LAZY)
    @OrderBy
    private List<Product> products = new ArrayList<Product>();

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "technicalProduct", fetch = FetchType.LAZY)
    @OrderBy
    private List<RoleDefinition> roleDefinitions = new ArrayList<RoleDefinition>();

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "technicalProduct", fetch = FetchType.LAZY)
    @OrderBy
    private List<TechnicalProductOperation> technicalProductOperations = new ArrayList<TechnicalProductOperation>();

    public boolean isAllowingOnBehalfActing() {
        return dataContainer.isAllowingOnBehalfActing();
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganizationKey(long organizationKey) {
        this.organizationKey = organizationKey;
    }

    public long getOrganizationKey() {
        return organizationKey;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<ParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    public void setParameterDefinitions(
            List<ParameterDefinition> parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setRoleDefinitions(List<RoleDefinition> roleDefinitions) {
        this.roleDefinitions = roleDefinitions;
    }

    public List<RoleDefinition> getRoleDefinitions() {
        return roleDefinitions;
    }

    public List<TechnicalProductTag> getTags() {
        return tags;
    }

    public void setTags(List<TechnicalProductTag> tags) {
        this.tags = tags;
    }

    /*
     * Delegate Methods
     */

    public ServiceAccessType getAccessType() {
        return dataContainer.getAccessType();
    }

    public String getBaseURL() {
        return dataContainer.getBaseURL();
    }

    public String getLoginPath() {
        return dataContainer.getLoginPath();
    }

    public String getBillingIdentifier() {
        return dataContainer.getBillingIdentifier();
    }

    public boolean isExternalBilling() {
        String billingIdentifier = getBillingIdentifier();
        return (!BillingAdapterIdentifier.NATIVE_BILLING.toString()
                .equals(billingIdentifier));
    }

    public Long getProvisioningTimeout() {
        return dataContainer.getProvisioningTimeout();
    }

    public ProvisioningType getProvisioningType() {
        return dataContainer.getProvisioningType();
    }

    public String getProvisioningURL() {
        return dataContainer.getProvisioningURL();
    }

    public String getProvisioningVersion() {
        return dataContainer.getProvisioningVersion();
    }

    public String getTechnicalProductId() {
        return dataContainer.getTechnicalProductId();
    }

    public String getTechnicalProductBuildId() {
        return dataContainer.getTechnicalProductBuildId();
    }

    public void setAccessType(ServiceAccessType serviceAccessType) {
        dataContainer.setAccessType(serviceAccessType);
    }

    public void setBaseURL(String baseURL) {
        dataContainer.setBaseURL(baseURL);
    }

    public void setBillingIdentifier(String billingIdentifier) {
        dataContainer.setBillingIdentifier(billingIdentifier);
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
        if (organization != null)
            setOrganizationKey(organization.getKey());
    }

    public void setAllowingOnBehalfActing(boolean requiresOnBehalfOf) {
        dataContainer.setAllowingOnBehalfActing(requiresOnBehalfOf);
    }

    public void setLoginPath(String loginPath) {
        dataContainer.setLoginPath(loginPath);
    }

    public void setProvisioningTimeout(Long provisioningTimeout) {
        dataContainer.setProvisioningTimeout(provisioningTimeout);
    }

    public void setProvisioningType(ProvisioningType provisioningType) {
        dataContainer.setProvisioningType(provisioningType);
    }

    public void setProvisioningURL(String provisioningURL) {
        dataContainer.setProvisioningURL(provisioningURL);
    }

    public void setProvisioningVersion(String provisioningVersion) {
        dataContainer.setProvisioningVersion(provisioningVersion);
    }

    public void setTechnicalProductId(String technicalProductId) {
        dataContainer.setTechnicalProductId(technicalProductId);
    }

    public void setTechnicalProductBuildId(String technicalProductBuildId) {
        dataContainer.setTechnicalProductBuildId(technicalProductBuildId);
    }

    public String getProvisioningUsername() {
        return dataContainer.getProvisioningUsername();
    }

    public void setProvisioningUsername(String provisioningUsername) {
        dataContainer.setProvisioningUsername(provisioningUsername);
    }

    public String getProvisioningPassword() {
        return dataContainer.getProvisioningPassword();
    }

    public void setProvisioningPassword(String provisioningPassword) {
        dataContainer.setProvisioningPassword(provisioningPassword);
    }

    public void setTechnicalProductOperations(
            List<TechnicalProductOperation> technicalProductOperations) {
        this.technicalProductOperations = technicalProductOperations;
    }

    public List<TechnicalProductOperation> getTechnicalProductOperations() {
        return technicalProductOperations;
    }

    public boolean isOnlyOneSubscriptionAllowed() {
        return dataContainer.isOnlyOneSubscriptionAllowed();
    }

    public void setOnlyOneSubscriptionAllowed(
            boolean onlyOneSubscriptionAllowed) {
        dataContainer.setOnlyOneSubscriptionAllowed(onlyOneSubscriptionAllowed);
    }

    /**
     * @param products
     *            the products to set
     */
    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Override
    String toStringAttributes() {
        return String.format(
                ", technicalProductId='%s', organizationKey='%s', %nevents='%s', %nparameterDefinitions='%s'",
                getTechnicalProductId(), Long.valueOf(getOrganizationKey()),
                getEvents(), getParameterDefinitions());
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }
}
