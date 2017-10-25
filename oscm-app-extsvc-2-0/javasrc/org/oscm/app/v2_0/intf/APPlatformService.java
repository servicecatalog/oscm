/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2012-08-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.intf;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.ejb.Remote;

import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.data.User;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.exceptions.ObjectNotFoundException;

/**
 * Interface providing methods by which service controllers implemented in APP
 * can access common APP utilities.
 */
public interface APPlatformService {

    /**
     * The JNDI name with which the APP service is registered in the container.
     */
    String JNDI_NAME = "java:global/oscm-app/oscm-app/org.oscm.app.v2_0.intf.APPlatformService";

    /**
     * Sends a mail with the specified subject and body to the given recipients.
     * 
     * @param mailAddresses
     *            a list of the mail recipients
     * @param subject
     *            the subject of the mail
     * @param text
     *            the body of the mail
     * @throws APPlatformException
     */
    void sendMail(List<String> mailAddresses, String subject,
            String text) throws APPlatformException;

    /**
     * Returns the basic URL of the APP notification handler. Requests to the
     * notification handler are forwarded to the appropriate service controller.
     * 
     * @return the notification handler URL
     * @throws ConfigurationException
     *             if the notification handler is not configured correctly
     */
    String getEventServiceUrl() throws ConfigurationException;

    /**
     * Returns the base URL of the OSCM platform services in the following
     * format:
     * <p>
     * <code>http(s)://&lt;host&gt;:&lt;port&gt;/{SERVICE}/BASIC</code>
     * <p>
     * Replace <code>{SERVICE}</code> by the name of the service you want to
     * instantiate (e.g. <code>SubscriptionService</code>).
     * 
     * @return the platform service URL
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     */
    String getBSSWebServiceUrl() throws ConfigurationException;

    /**
     * Returns the base URL of the WSDL files of the OSCM platform services in
     * the following format:
     * <p>
     * <code>http(s)://&lt;host&gt;:&lt;port&gt;/{SERVICE}/BASIC?wsdl</code>
     * <p>
     * Replace <code>{SERVICE}</code> by the name of the service you want to
     * instantiate (e.g. <code>SubscriptionService</code>).
     * 
     * @return the platform service URL
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     */
    String getBSSWebServiceWSDLUrl() throws ConfigurationException;

    /**
     * Provides the specified controller with a semaphore that prohibits access
     * to any application instance other than the given one. As a prerequisite,
     * no such lock must exist for another application instance.
     * <p>
     * Locking is useful, for example, to avoid conflicts in the application
     * that may be caused by parallel work on several instances.
     * <p>
     * In order to execute this method, you must specify the credentials of a
     * technology manager registered in the organization which is responsible
     * for the controller.
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param instanceId
     *            the ID of the application instance for which the lock is to be
     *            set
     * @param authentication
     *            a <code>PasswordAuthentication</code> object identifying a
     *            technology manager registered in the organization which is
     *            responsible for the controller
     * @return <code>true</code> if the lock can be set, <code>false</code> if a
     *         lock is already set for another instance
     * @throws AuthenticationException
     *             if the authentication of the user fails
     * @throws APPlatformException
     *             if a general problem occurs in accessing APP
     */
    boolean lockServiceInstance(String controllerId, String instanceId,
            PasswordAuthentication authentication)
            throws APPlatformException;

    /**
     * Removes the lock (semaphore) for the specified application instance and
     * service controller.
     * <p>
     * In order to execute this method, you must specify the credentials of a
     * technology manager registered in the organization which is responsible
     * for the controller.
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param instanceId
     *            the ID of the application instance
     * @param authentication
     *            a <code>PasswordAuthentication</code> object identifying a
     *            technology manager registered in the organization which is
     *            responsible for the controller
     * @throws AuthenticationException
     *             if the authentication of the user fails
     * @throws APPlatformException
     *             if a general problem occurs in accessing APP
     */
    void unlockServiceInstance(String controllerId, String instanceId,
            PasswordAuthentication authentication)
            throws APPlatformException;

    /**
     * Checks whether an application instance with the specified ID exists for
     * the given service controller.
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param instanceId
     *            the ID of the application instance to look for
     * @return <code>true</code> if the instance exists, <code>false</code>
     *         otherwise
     */
    boolean exists(String controllerId, String instanceId);

    /**
     * Returns the configuration settings for the given service controller.
     * <p>
     * In order to execute this method, you must specify the credentials of a
     * technology manager registered in the organization which is responsible
     * for the controller.
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param authentication
     *            a <code>PasswordAuthentication</code> object identifying a
     *            technology manager registered in the organization which is
     *            responsible for the controller
     * @return the configuration settings, consisting of a key and a value each
     * @throws AuthenticationException
     *             if the authentication of the user fails
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     * @throws APPlatformException
     *             if a general problem occurs in accessing APP
     */
    HashMap<String, Setting> getControllerSettings(String controllerId,
            PasswordAuthentication authentication)
            throws APPlatformException;

    /**
     * Stores the configuration settings for the given controller.
     * <p>
     * In order to execute this method, you must specify the credentials of a
     * technology manager registered in the organization which is responsible
     * for the controller.
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param controllerSettings
     *            the configuration settings, consisting of a key and a value
     *            each; specify a <code>null</code> value to remove a setting
     * @param authentication
     *            a <code>PasswordAuthentication</code> object identifying a
     *            technology manager registered in the organization which is
     *            responsible for the controller
     * @throws AuthenticationException
     *             if the authentication of the user fails
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     * @throws APPlatformException
     *             if a general problem occurs in accessing APP
     */
    void storeControllerSettings(String controllerId,
            HashMap<String, Setting> controllerSettings,
            PasswordAuthentication authentication)
            throws APPlatformException;

    /**
     * Authenticates the user specified by the given authentication object as a
     * technology manager in the organization which is responsible for the
     * controller specified by the given ID.
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param authentication
     *            a <code>PasswordAuthentication</code> object identifying the
     *            technology manager to be authenticated in the organization
     *            which is responsible for the controller
     * @return an <code>User</code> object holding some details about the
     *         authenticated user
     * @throws AuthenticationException
     *             if the authentication of the user fails
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     * @throws APPlatformException
     *             if a general problem occurs in accessing APP
     */
    User authenticate(String controllerId,
            PasswordAuthentication authentication)
            throws APPlatformException;

    /**
     * @param controllerId
     *            the ID of the service controller
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     * @throws APPlatformException
     *             if a general problem occurs in accessing APP
     */
    void requestControllerSettings(String controllerId)
            throws APPlatformException;

    /**
     * Returns a collection of the IDs of all service instances that are managed
     * by APP in the context of the controller specified by given ID.
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param authentication
     *            a <code>PasswordAuthentication</code> object identifying a
     *            technology manager registered in the organization which is
     *            responsible for the controller
     * @return
     * @throws AuthenticationException
     * @throws ConfigurationException
     * @throws APPlatformException
     */
    Collection<String> listServiceInstances(String controllerId,
            PasswordAuthentication authentication)
            throws APPlatformException;

    /**
     * Returns the complete provisioning settings of the defined service
     * instance.
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param instanceId
     *            the ID of the service instance
     * @param authentication
     *            a <code>PasswordAuthentication</code> object identifying a
     *            technology manager registered in the organization which is
     *            responsible for the controller
     * @return a <code>ProvisioningSettings</code> object specifying the service
     *         parameters and configuration settings
     * @throws AuthenticationException
     *             if the authentication of the user fails
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     * @throws ObjectNotFoundException
     *             if no such service instance exists
     * @throws APPlatformException
     *             if a general problem occurs in accessing APP
     */
    ProvisioningSettings getServiceInstanceDetails(String controllerId,
            String instanceId, PasswordAuthentication authentication)
            throws APPlatformException;

    /**
     * Returns the complete provisioning settings of the defined service
     * instance without authentication
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param instanceId
     *            the ID of the service instance
     * @param subscriptionId
     *            the ID of the subscription
     * @param organizationId
     *            the ID of the organization
     * @return a <code>ProvisioningSettings</code> object specifying the service
     *         parameters and configuration settings
     * @throws AuthenticationException
     *             if the authentication of the user fails
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     * @throws ObjectNotFoundException
     *             if no such service instance exists
     * @throws APPlatformException
     *             if a general problem occurs in accessing APP
     */
    ProvisioningSettings getServiceInstanceDetails(String controllerId,
            String instanceId, String subscriptionId, String organizationId)
            throws APPlatformException;

    /**
     * Stores the service instance settings for the given instance.
     * <p>
     * In order to execute this method, you must specify the credentials of a
     * technology manager registered in the organization which is responsible
     * for the controller.
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param instanceId
     *            the ID of the service instance
     * @param settings
     *            the configuration settings, consisting of a key and a value
     *            each; specify a <code>null</code> value to remove a setting
     * @param authentication
     *            a <code>PasswordAuthentication</code> object identifying a
     *            technology manager registered in the organization which is
     *            responsible for the controller
     * @throws AuthenticationException
     *             if the authentication of the user fails
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     * @throws APPlatformException
     *             if a general problem occurs in accessing APP
     */
    void storeServiceInstanceDetails(String controllerId,
            String instanceId, ProvisioningSettings settings,
            PasswordAuthentication authentication)
            throws APPlatformException;

    /**
     * Checks the the hash from the given token against the decrypted hash from
     * the signature using the BSS public key.
     * 
     * @param token
     *            the token to hash
     * @param signature
     *            the encrypted token hash
     * @return true if token and signature match
     */
    public boolean checkToken(String token, String signature);

}
