/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-08                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.types.enumtypes.ServiceAccessType;

/**
 * Represents a technical service.
 * 
 */
public class VOTechnicalService extends BaseVO {

    private static final long serialVersionUID = -657931182595357932L;

    /**
     * The definitions of the events supported by the technical service.
     */
    private List<VOEventDefinition> eventDefinitions = new ArrayList<VOEventDefinition>();

    /**
     * The identifier of the technical service.
     */
    private String technicalServiceId;

    /**
     * The build number of the application.
     */
    private String technicalServiceBuildId;

    /**
     * The access type defined for the technical service.
     */
    private ServiceAccessType accessType;

    /**
     * The description of the technical service.
     */
    private String technicalServiceDescription;

    /**
     * The URL of the application's remote interface.
     */
    private String baseUrl;

    /**
     * The URL of the application's provisioning service.
     */
    private String provisioningUrl;

    /**
     * The path to the application's token handler for login requests.
     */
    private String loginPath;

    /**
     * The version of the application's provisioning service.
     */
    private String provisioningVersion;

    /**
     * The definitions of the parameters supported by the technical service.
     */
    private List<VOParameterDefinition> parameterDefinitions = new ArrayList<VOParameterDefinition>();

    /**
     * The definitions of the service roles supported by the technical service.
     */
    private List<VORoleDefinition> roleDefinitions = new ArrayList<VORoleDefinition>();

    /**
     * The tags defined for the technical service in the current locale.
     */
    private List<String> tags = new ArrayList<String>();

    /**
     * The license agreement for the technical service.
     */
    private String license;

    /**
     * The textual description of how to access the underlying application.
     */
    private String accessInfo;

    /**
     * Identifier to distinguish between billing systems
     */
    private String billingIdentifier;

    /**
     * The technical service operations.
     */
    private List<VOTechnicalServiceOperation> technicalServiceOperations = new ArrayList<VOTechnicalServiceOperation>();

    /**
     * Flag indicating weather service is using external billing system
     */
    private boolean externalBilling;

    /**
     * Retrieves the identifier of the technical service.
     * 
     * @return the service ID
     */
    public String getTechnicalServiceId() {
        return technicalServiceId;
    }

    /**
     * Sets the identifier of the technical service.
     * 
     * @param name
     *            the service ID
     */
    public void setTechnicalServiceId(String name) {
        this.technicalServiceId = name;
    }

    /**
     * Retrieves the build number of the underlying application set for the
     * technical service.
     * 
     * @return the build number
     */
    public String getTechnicalServiceBuildId() {
        return technicalServiceBuildId;
    }

    /**
     * Sets the build number of the underlying application for the technical
     * service.
     * 
     * @param technicalServiceBuildId
     *            the build number
     */
    public void setTechnicalServiceBuildId(String technicalServiceBuildId) {
        this.technicalServiceBuildId = technicalServiceBuildId;
    }

    /**
     * Retrieves the access type defined for the technical service. The access
     * type specifies how users access the underlying application.
     * 
     * @return the service access type
     */
    public ServiceAccessType getAccessType() {
        return accessType;
    }

    /**
     * Sets the access type for the technical service. The access type specifies
     * how users access the underlying application.
     * 
     * @param accessType
     *            the service access type
     */
    public void setAccessType(ServiceAccessType accessType) {
        this.accessType = accessType;
    }

    /**
     * Retrieves the definitions of the parameters supported by the technical
     * service.
     * 
     * @return the list of parameter definitions
     */
    public List<VOParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    /**
     * Sets the definitions of the parameters supported by the technical
     * service.
     * 
     * @param parameters
     *            the list of parameter definitions
     */
    public void setParameterDefinitions(
            List<VOParameterDefinition> parameters) {
        this.parameterDefinitions = parameters;
    }

    /**
     * Retrieves the text describing the technical service.
     * 
     * @return the service description
     */
    public String getTechnicalServiceDescription() {
        return technicalServiceDescription;
    }

    /**
     * Sets the text describing the technical service.
     * 
     * @param technicalServiceDescription
     *            the service description
     */
    public void setTechnicalServiceDescription(
            String technicalServiceDescription) {
        this.technicalServiceDescription = technicalServiceDescription;
    }

    /**
     * Retrieves the definitions of the events supported by the technical
     * service.
     * 
     * @return the list of event definitions
     */
    public List<VOEventDefinition> getEventDefinitions() {
        return eventDefinitions;
    }

    /**
     * Sets the definitions of the events supported by the technical service.
     * 
     * @param eventDefinitions
     *            the list of event definitions
     */
    public void setEventDefinitions(List<VOEventDefinition> eventDefinitions) {
        this.eventDefinitions = eventDefinitions;
    }

    /**
     * Retrieves the URL of the underlying application's remote interface.
     * 
     * @return the URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the URL of the underlying application's remote interface.
     * 
     * @param baseUrl
     *            the URL of the application<br>
     *            Be aware that internet domain names must follow the following
     *            rules: <br>
     *            They must start with a letter and end with a letter or number.
     *            <br>
     *            They may contain letters, numbers, or hyphens only. Special
     *            characters are not allowed.<br>
     *            They may consist of a maximum of 63 characters.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Retrieves the URL of the WSDL document defining the interface of the
     * provisioning service of the underlying application.
     * 
     * @return the provisioning service URL
     */
    public String getProvisioningUrl() {
        return provisioningUrl;
    }

    /**
     * Sets the URL of the WSDL document defining the interface of the
     * provisioning service of the underlying application.
     * 
     * @param provisioningUrl
     *            the provisioning service URL
     */
    public void setProvisioningUrl(String provisioningUrl) {
        this.provisioningUrl = provisioningUrl;
    }

    /**
     * Retrieves the path to the application's token handler for login requests
     * with user tokens.
     * 
     * @return the login path
     */
    public String getLoginPath() {
        return loginPath;
    }

    /**
     * Sets the path to the application's token handler for login requests with
     * user tokens.
     * 
     * @param loginPath
     *            the login path
     */
    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    /**
     * Retrieves the version of the application's provisioning service.
     * 
     * @return the version number
     */
    public String getProvisioningVersion() {
        return provisioningVersion;
    }

    /**
     * Sets the version of the application's provisioning service.
     * 
     * @param provisioningVersion
     *            the version number
     */
    public void setProvisioningVersion(String provisioningVersion) {
        this.provisioningVersion = provisioningVersion;
    }

    /**
     * Sets the license information for the technical service as it will be
     * shown to customers.
     * 
     * @param license
     *            the license agreement
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * Retrieves the license information defined for the technical service.
     * 
     * @return the license agreement
     */
    public String getLicense() {
        return license;
    }

    /**
     * Sets the text describing how to access the underlying application. The
     * text may consist of a maximum of 4096 bytes.
     * <p>
     * The access information is required with the following access types:
     * <code>DIRECT</code>, <code>USER</code>. It is retrieved when a user
     * subscribes to a service with synchronous provisioning or when an
     * asynchronous provisioning completes. First, the text stored for the
     * calling user's current locale is searched for; if this is not found, the
     * text of the platform's default locale (English) is used. If none of these
     * texts is available, a <code>TechnicalServiceOperationException</code> is
     * thrown.
     * <p>
     * This means that you should always make sure to store the access
     * information at least in the platform's default locale (English). Change
     * your locale before calling this method, if required, because the method
     * stores the information in the current locale of the calling user.
     * 
     * @param accessInfo
     *            the access information
     */
    public void setAccessInfo(String accessInfo) {
        this.accessInfo = accessInfo;
    }

    /**
     * Retrieves the text describing how to access the underlying application.
     * 
     * @return the access information
     */
    public String getAccessInfo() {
        return accessInfo;
    }

    /**
     * Sets the definitions of the service roles supported by the technical
     * service.
     * 
     * @param roleDefinitions
     *            the list of role definitions
     */
    public void setRoleDefinitions(List<VORoleDefinition> roleDefinitions) {
        this.roleDefinitions = roleDefinitions;
    }

    /**
     * Retrieves the definitions of the service roles supported by the technical
     * service.
     * 
     * @return the list of role definitions
     */
    public List<VORoleDefinition> getRoleDefinitions() {
        return roleDefinitions;
    }

    /**
     * Sets the operations for the technical service.
     * 
     * @param technicalServiceOperations
     *            the list of technical service operations
     */
    public void setTechnicalServiceOperations(
            List<VOTechnicalServiceOperation> technicalServiceOperations) {
        this.technicalServiceOperations = technicalServiceOperations;
    }

    /**
     * Retrieves the operations defined for the technical service.
     * 
     * @return the list of technical service operations
     */
    public List<VOTechnicalServiceOperation> getTechnicalServiceOperations() {
        return technicalServiceOperations;
    }

    /**
     * Sets the tags for the technical service in the current locale.
     * 
     * @param tags
     *            the list of tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Retrieves the tags defined for the technical service in the current
     * locale.
     * 
     * @return the list of tags
     */
    public List<String> getTags() {
        return tags;
    }

    public void setBillingIdentifier(String billingId) {
        this.billingIdentifier = billingId;
    }

    public String getBillingIdentifier() {
        return billingIdentifier;
    }

    public boolean isExternalBilling() {
        return externalBilling;
    }

    public void setExternalBilling(boolean externalBilling) {
        this.externalBilling = externalBilling;
    }

}
