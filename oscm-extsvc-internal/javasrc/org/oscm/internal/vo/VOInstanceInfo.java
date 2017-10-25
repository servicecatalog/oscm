/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-05-22                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.io.Serializable;

/**
 * Represents an application instance and provides the data required to access
 * the instance. An instance is the set of items that an application provisions
 * for a subscription.
 */
public class VOInstanceInfo implements Serializable {

    /**
     * Identifier of the instance defined when the instance is created.
     */
    private String instanceId;

    /**
     * Text describing how to access the application. The text may consist of a
     * maximum of 4096 characters. When set to <code>null</code>, the
     * description is retrieved from the technical service definition. First,
     * the text stored for the calling user's current locale is searched for; if
     * this is not found, the text of the default locale (English) is used. If
     * none of these is available at the technical service definition, a
     * <code>TechnicalServiceOperationException</code> is thrown when a user
     * subscribes with synchronous provisioning or when an asynchronous
     * provisioning completes.
     */
    private String accessInfo;

    /**
     * The URL of the application's remote interface. If <code>null</code>, the
     * URL is retrieved from the technical service definition.
     */
    private String baseUrl;

    /**
     * Path to the token handler of the instance. This is the module that
     * handles login requests with user tokens. If <code>null</code>, the value
     * is retrieved from the technical service definition.
     */
    private String loginPath;

    /**
     * Number of provisioned VMs
     */
    private int vmsNumber;

    /**
     * Retrieves the ID of the application instance as defined when the instance
     * is created.
     * 
     * @return the instance ID
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Sets the ID of the application instance.
     * 
     * @param instanceId
     *            the instance ID
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Retrieves the text describing how to access the application.
     * 
     * @return the access information
     */
    public String getAccessInfo() {
        return accessInfo;
    }

    /**
     * Sets the text describing how to access the application. This is required
     * with the following access types: <code>DIRECT</code>, <code>USER</code>.
     * The text may consist of a maximum of 4096 bytes.
     * <p>
     * When set to <code>null</code>, the description is retrieved from the
     * technical service definition. First, the text stored for the calling
     * user's current locale is searched for; if this is not found, the text of
     * the default locale (English) is used. If none of these is available at
     * the technical service definition, a
     * <code>TechnicalServiceOperationException</code> is thrown when a user
     * subscribes with synchronous provisioning or when an asynchronous
     * provisioning completes.
     * 
     * @param accessInfo
     *            the access information
     */
    public void setAccessInfo(String accessInfo) {
        this.accessInfo = accessInfo;
    }

    /**
     * Retrieves the URL of the application's remote interface.
     * 
     * @return the URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the URL of the application's remote interface. This is required with
     * the <code>LOGIN</code> access type. When set to <code>null</code>, the
     * URL is retrieved from the technical service definition.
     * 
     * @param baseUrl
     *            the URL of the application<br>
     *            Be aware that internet domain names must follow the following
     *            rules: <br>
     *            They must start with a letter and end with a letter or number.<br>
     *            They may contain letters, numbers, or hyphens only. Special
     *            characters are not allowed.<br>
     *            They may consist of a maximum of 63 characters.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Retrieves the path of the application's token handler for login requests
     * with user tokens.
     * 
     * @return the login path
     */
    public String getLoginPath() {
        return loginPath;
    }

    /**
     * Sets the path of the application's token handler for login requests with
     * user tokens. This is required with the <code>LOGIN</code> access type.
     * When set to <code>null</code>, the path is retrieved from the technical
     * service definition.
     * 
     * @param loginPath
     *            the login path
     */
    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    public int getVmsNumber() {
        return vmsNumber;
    }

    public void setVmsNumber(int vmsNumber) {
        this.vmsNumber = vmsNumber;
    }
}
