/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2012-08-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.intf;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.ejb.Remote;

import org.oscm.app.data.PasswordAuthentication;
import org.oscm.app.data.ProvisioningSettings;
import org.oscm.app.data.User;
import org.oscm.app.exceptions.APPlatformException;
import org.oscm.app.exceptions.AuthenticationException;
import org.oscm.app.exceptions.ConfigurationException;

/**
 * Interface providing methods by which service controllers implemented in APP
 * can access common APP utilities.
 */
@Remote
public interface APPlatformService {

    /**
     * The JNDI name with which the APP service is registered in the container.
     */
    public static final String JNDI_NAME = "bss/app/PlatformService";

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
    public void sendMail(List<String> mailAddresses, String subject, String text)
            throws APPlatformException;

    /**
     * Returns the basic URL of the APP notification handler. Requests to the
     * notification handler are forwarded to the appropriate service controller.
     * 
     * @return the notification handler URL
     * @throws ConfigurationException
     *             if the notification handler is not configured correctly
     */
    public String getEventServiceUrl() throws ConfigurationException;

    /**
     * Returns the base URL of the WSDL files of the OSCM platform services in
     * the following format:
     * <p>
     * <code>http(s)://&lt;host&gt;:&lt;port&gt;/{SERVICE}/v1.6/BASIC?wsdl</code>
     * <p>
     * Replace <code>{SERVICE}</code> by the name of the service you want to
     * instantiate (e.g. <code>SubscriptionService</code>).
     * 
     * @return the platform service URL
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     */
    public String getBSSWebServiceUrl() throws ConfigurationException;

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
    public boolean lockServiceInstance(String controllerId, String instanceId,
            PasswordAuthentication authentication)
            throws AuthenticationException, APPlatformException;

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
    public void unlockServiceInstance(String controllerId, String instanceId,
            PasswordAuthentication authentication)
            throws AuthenticationException, APPlatformException;

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
    public boolean exists(String controllerId, String instanceId);

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
    public HashMap<String, String> getControllerSettings(String controllerId,
            PasswordAuthentication authentication)
            throws AuthenticationException, ConfigurationException,
            APPlatformException;

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
    public void storeControllerSettings(String controllerId,
            HashMap<String, String> controllerSettings,
            PasswordAuthentication authentication)
            throws AuthenticationException, ConfigurationException,
            APPlatformException;

    /**
     * Authenticates the user specified by the given authentication object as a
     * technology manager in the organization which is responsible for the
     * service controller specified by the given ID.
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param authentication
     *            a <code>PasswordAuthentication</code> object identifying the
     *            technology manager to be authenticated in the organization
     *            responsible for the controller
     * @return an <code>User</code> object holding details about the
     *         authenticated user
     * @throws AuthenticationException
     *             if the authentication of the user fails
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     * @throws APPlatformException
     *             if a general problem occurs in accessing APP
     */
    public User authenticate(String controllerId,
            PasswordAuthentication authentication)
            throws AuthenticationException, ConfigurationException,
            APPlatformException;

    /**
     * @param controllerId
     *            the ID of the service controller
     * @throws ConfigurationException
     *             if the configuration settings cannot be loaded
     * @throws APPlatformException
     *             if a general problem occurs in accessing APP
     */
    public void requestControllerSettings(String controllerId)
            throws ConfigurationException, APPlatformException;

    /**
     * Returns a collection of the IDs of all service instances that are managed
     * by APP in the context of the controller specified by the given ID.
     * 
     * @param controllerId
     *            the ID of the service controller
     * @param authentication
     *            a <code>PasswordAuthentication</code> object identifying a
     *            technology manager registered in the organization 
     *            responsible for the controller
     * @return a list of service instances
     *     
     * @throws AuthenticationException
     *            if the authentication of the user fails
     * @throws ConfigurationException
     *            if the configuration settings cannot be loaded 
     * @throws APPlatformException
     *            if a general problem occurs in accessing APP
     */
    public Collection<String> listServiceInstances(String controllerId,
            PasswordAuthentication authentication)
            throws AuthenticationException, ConfigurationException,
            APPlatformException;

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
     *            technology manager registered in the organization
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
    public ProvisioningSettings getServiceInstanceDetails(String controllerId,
            String instanceId, PasswordAuthentication authentication)
            throws AuthenticationException, ConfigurationException,
            APPlatformException;

}
