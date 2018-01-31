/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2012-08-06                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Provides information on the current status of an application instance with
 * respect to actions controlled by APP.
 */
public class InstanceStatus implements Serializable {

    private static final long serialVersionUID = 8686130971941381037L;

    private boolean isReady;
    private boolean runWithTimer = true;
    private boolean instanceProvisioningRequested = false;
    private List<LocalizedText> description;
    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> attributes;

    private String baseUrl;
    private String accessInfo;
    private String loginPath;

    /**
     * Returns whether the application instance has completed the most recent
     * sequence of operations requested by APP (e.g. the application instance
     * was created).
     * 
     * @return <code>true</code> if the instance is ready, <code>false</code>
     *         otherwise
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * Sets information on whether the application instance has completed the
     * most recent sequence of operations requested by APP (e.g. the application
     * instance was created).
     * <p>
     * This can be used to divide long-running provisioning operations into
     * several steps. For example, when creating an application instance, the
     * controller could immediately return the instance ID, report the instance
     * status as "not ready", and request APP to poll the status at regular
     * intervals. Only after the instance has actually been set up at the
     * application side, the controller would report its status as "ready" upon
     * the next polling by APP.
     * 
     * @param isReady
     *            <code>true</code> if the instance is ready, <code>false</code>
     *            otherwise
     */
    public void setIsReady(boolean isReady) {
        this.isReady = isReady;
    }

    /**
     * Returns whether the application-specific controller wants APP to poll the
     * instance status at certain intervals.
     * 
     * @return <code>true</code> if the instance status is to be polled,
     *         <code>false</code> otherwise
     */
    public boolean getRunWithTimer() {
        return runWithTimer;
    }

    /**
     * Sets information on whether the application-specific controller wants APP
     * to poll the instance status at certain intervals.
     * <p>
     * This can be used to divide long-running provisioning operations into
     * several steps. For example, when creating an application instance, the
     * controller could immediately return the instance ID, report the instance
     * status as "not ready", and request APP to poll the status at regular
     * intervals. Only after the instance has actually been set up at the
     * application side, the controller would report its status as "ready" upon
     * the next polling by APP.
     * <p>
     * On the other hand, <code>setRunWithTimer</code> should be set to
     * <code>false</code> for processing steps which actively report their
     * completion via the APP notification handler. The controller is
     * responsible for providing the correct URL of the handler in this case.
     * The URL has the following format:<br>
     * <code><i>base_url</i>?sid=<i>instance_id</i>&cid=<i>controller_id</i>[&<i>options</i>]</code>
     * <br>
     * <code><i>base_url</i></code> is the basic URL of the APP notification
     * handler as provided by the <code>getEventServiceUrl</code> method of
     * <code>APPlatformService</code>.<br>
     * <code><i>instance_id</i></code> is the ID of the relevant application
     * instance.<br>
     * <code><i>controller_id</i></code> is the ID of the service controller.<br>
     * <code><i>options</i></code> are optional commands or parameters to be
     * passed to the controller.<br>
     * Example:<br>
     * <code>127.0.0.1:8080/oscm-app/notify?sid=vm2041&cid=ess.vmware&command=finish</code>
     * 
     * @param runWithTimer
     *            <code>true</code> if the instance status is to be polled,
     *            <code>false</code> otherwise
     */
    public void setRunWithTimer(boolean runWithTimer) {
        this.runWithTimer = runWithTimer;
    }

    /**
     * Returns whether specific or additional steps are to be executed for the
     * current provisioning operation by means of the application instance's own
     * provisioning service.
     * 
     * @return <code>true</code> if an instance-specific provisioning service is
     *         to be called, <code>false</code> otherwise
     */
    public boolean isInstanceProvisioningRequested() {
        return instanceProvisioningRequested;
    }

    /**
     * Specifies whether specific or additional steps are to be executed for the
     * current provisioning operation by means of the application instance's own
     * provisioning service.
     * <p>
     * If the application-specific service controller sets this flag to
     * <code>true</code> for a provisioning operation, APP issues a
     * corresponding call to the provisioning service of the application
     * instance immediately after the controller's return. The provisioning
     * service of the instance must implement the standard
     * <code>ProvisioningService</code> interface and work in synchronous mode.
     * <p>
     * This feature is useful if not all provisioning operations can be executed
     * by the service controller via the application's general remote interface.
     * For example, the general remote interface of a virtual machine server
     * allows you to provision virtual machines, but you usually cannot create
     * or manage users within these virtual machines. To do this, you have to
     * address the virtual machine itself.
     * 
     * @param executeInstanceProvisioning
     *            <code>true</code> if an instance-specific provisioning service
     *            is to be called, <code>false</code> otherwise
     */
    public void setInstanceProvisioningRequired(
            boolean executeInstanceProvisioning) {
        this.instanceProvisioningRequested = executeInstanceProvisioning;
    }

    /**
     * Sets a description of the current status.
     * 
     * @param list
     *            the localized texts to set as the description
     */
    public void setDescription(List<LocalizedText> list) {
        this.description = list;
    }

    /**
     * Returns the description of the current status (if available).
     * 
     * @return the localized description
     */
    public List<LocalizedText> getDescription() {
        return this.description;
    }

    /**
     * Returns the parameters and settings of the application instance that are
     * to be persisted as instance information by APP.
     * 
     * @return the parameters and settings to store, consisting of a key and a
     *         value each
     */
    public HashMap<String, Setting> getChangedParameters() {
        return parameters;
    }

    /**
     * Sets the parameters and settings of the application instance that are to
     * be persisted as instance information by APP.
     * 
     * @param parameters
     *            the parameters and settings to store, consisting of a key and
     *            a value each
     */
    public void setChangedParameters(HashMap<String, Setting> parameters) {
        if (parameters != null) {
            for (Setting setting : parameters.values()) {
                if (setting != null && setting.getValue() == null) {
                    setting.setValue("");
                }
            }
        }
        this.parameters = parameters;
    }

    /**
     * Returns the attributes of the application instance that are to be
     * persisted as instance information by APP.
     * 
     * @return the attributes to store, consisting of a key and a value each
     */
    public HashMap<String, Setting> getChangedAttributes() {
        return attributes;
    }

    /**
     * Sets the attributes of the application instance that are to be persisted
     * as instance information by APP.
     * 
     * @param attributes
     *            the attributes to store, consisting of a key and a value each
     */
    public void setChangedAttributes(HashMap<String, Setting> attributes) {
        this.attributes = attributes;
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
     * the <code>LOGIN</code> access type.
     * 
     * @param baseUrl
     *            the URL of the application<br>
     *            Be aware that internet domain names must follow the following
     *            rules: <br>
     *            They must start with a letter and end with a letter or number.<br>
     *            They may contain letters, numbers, or hyphens only. Special
     *            characters are not allowed.<br>
     *            They may consist of a maximum of 63 characters.
     * 
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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
     * 
     * @param accessInfo
     *            the access information
     */
    public void setAccessInfo(String accessInfo) {
        this.accessInfo = accessInfo;
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
     * 
     * @param loginPath
     *            the login path
     */
    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }
}
