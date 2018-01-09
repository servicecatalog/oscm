/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;

import javax.persistence.*;

import org.oscm.domobjects.converters.ProvTConverter;
import org.oscm.domobjects.converters.ServiceAccessTypeConverter;
import org.oscm.domobjects.converters.TimerTConverter;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;

/**
 * DataContainer for domain object TechnicalProduct
 * 
 * @see TechnicalProduct
 * 
 * @author Mike J&auml;ger
 */
@Embeddable
public class TechnicalProductData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = -652830386165518647L;

    /**
     * The identifier for the technical product.
     */
    @Column(nullable = false)
    private String technicalProductId;

    /**
     * The build ID for the technical product.
     */
    private String technicalProductBuildId;

    /**
     * The provisioning type for the product instance.
     * 
     * @see ProvisioningType
     */
    @Enumerated(EnumType.STRING)
    private ProvisioningType provisioningType = ProvisioningType.SYNCHRONOUS;

    /**
     * The URL of the provisioning web service
     */
    @Column(nullable = false)
    private String provisioningURL;

    /**
     * The Version of the provisioning web service
     */
    private String provisioningVersion;

    /**
     * The timeout for the asynchronous provisioning
     */
    private Long provisioningTimeout;

    /**
     * The username for the provisioning web service
     */
    private String provisioningUsername;

    /**
     * The password for the provisioning web service
     */
    private String provisioningPassword;

    /**
     * The default base URL to communicate with an instance of the technical
     * product.
     */
    private String baseURL;

    /**
     * The default path to the login wrapper of the technical product (can be
     * overwritten from create instance)
     */
    private String loginPath;

    /**
     * The access integration type (direct access or access via platform with
     * reverse proxy)
     */
    @Enumerated(EnumType.STRING)
    private ServiceAccessType accessType = ServiceAccessType.LOGIN;

    /**
     * Only one subscription is allowed of this technical product.
     */
    private boolean onlyOneSubscriptionAllowed;

    /**
     * Indicates that an organization requires an on-behalf-of reference to
     * allow a paas organization to make api calls for a third party for
     * instances of this technical service.
     */
    private boolean allowingOnBehalfActing;

    /**
     * Identifier of adapter for external billing system, if null System default
     * will be used *
     */
    @Column(nullable = false)
    private String billingIdentifier;

    public boolean isAllowingOnBehalfActing() {
        return allowingOnBehalfActing;
    }

    public void setAllowingOnBehalfActing(boolean allowsOnBehalfActing) {
        this.allowingOnBehalfActing = allowsOnBehalfActing;
    }

    public String getTechnicalProductId() {
        return technicalProductId;
    }

    public void setTechnicalProductId(String technicalProductId) {
        this.technicalProductId = technicalProductId;
    }

    public String getTechnicalProductBuildId() {
        return technicalProductBuildId;
    }

    public void setTechnicalProductBuildId(String technicalProductBuildId) {
        this.technicalProductBuildId = technicalProductBuildId;
    }

    public ProvisioningType getProvisioningType() {
        return provisioningType;
    }

    public void setProvisioningType(ProvisioningType provisioningType) {
        this.provisioningType = provisioningType;
    }

    public String getProvisioningURL() {
        return provisioningURL;
    }

    public void setProvisioningURL(String provisioningURL) {
        this.provisioningURL = provisioningURL;
    }

    public String getProvisioningVersion() {
        return provisioningVersion;
    }

    public void setProvisioningVersion(String provisioningVersion) {
        this.provisioningVersion = provisioningVersion;
    }

    public Long getProvisioningTimeout() {
        return provisioningTimeout;
    }

    public void setProvisioningTimeout(Long provisioningTimeout) {
        this.provisioningTimeout = provisioningTimeout;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    public ServiceAccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(ServiceAccessType accessType) {
        this.accessType = accessType;
    }

    public String getProvisioningUsername() {
        return provisioningUsername;
    }

    public void setProvisioningUsername(String provisioningUsername) {
        this.provisioningUsername = provisioningUsername;
    }

    public String getProvisioningPassword() {
        return provisioningPassword;
    }

    public void setProvisioningPassword(String provisioningPassword) {
        this.provisioningPassword = provisioningPassword;
    }

    public boolean isOnlyOneSubscriptionAllowed() {
        return onlyOneSubscriptionAllowed;
    }

    public void setOnlyOneSubscriptionAllowed(boolean onlyOneSubscriptionAllowed) {
        this.onlyOneSubscriptionAllowed = onlyOneSubscriptionAllowed;
    }

    public String getBillingIdentifier() {
        return billingIdentifier;
    }

    public void setBillingIdentifier(String billingIdentifier) {
        this.billingIdentifier = billingIdentifier;
    }
}
