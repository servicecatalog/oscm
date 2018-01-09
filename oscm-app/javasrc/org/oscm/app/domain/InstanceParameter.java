/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 12.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.domain;

import java.security.GeneralSecurityException;

import javax.persistence.*;

import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.encrypter.AESEncrypter;

/**
 * Represents a parameter setting for a service instance.
 * 
 * @author Mike J&auml;ger
 * 
 */
@NamedQueries({
        @NamedQuery(name = "InstanceParameter.getAllForInstanceId", query = "SELECT ip FROM InstanceParameter ip WHERE ip.serviceInstance.instanceId = :sid and ip.serviceInstance.controllerId = :cid") })
@Entity
public class InstanceParameter {

    /**
     * The parameter key prefix to be used for those parameters that are only
     * available for the proxy.
     */
    public static final String APP_PARAM_KEY_PREFIX = "APP_";

    /**
     * The parameter key for the user name used to call web services in BES.
     */
    public static final String BSS_USER = APP_PARAM_KEY_PREFIX + "BSS_USER";

    /**
     * The parameter key for the user's password.
     */
    public static final String BSS_USER_PWD = APP_PARAM_KEY_PREFIX
            + "BSS_USER_PWD";

    /**
     * The parameter key for the user's password.
     */
    public static final String CONTROLLER_ID = APP_PARAM_KEY_PREFIX
            + "CONTROLLER_ID";

    /**
     * The parameter key for the user name used to call the provisioning service
     * of the service..
     */
    public static final String SERVICE_USER = APP_PARAM_KEY_PREFIX
            + "SERVICE_USER";

    /**
     * The parameter key for the user's password used to call the provisioning
     * service of the service..
     */
    public static final String SERVICE_USER_PWD = APP_PARAM_KEY_PREFIX
            + "SERVICE_USER_PWD";
    /**
     * The parameter key for the relative path to the service's provisioning
     * service.
     */
    public static final String SERVICE_RELATIVE_PROVSERV_WSDL = APP_PARAM_KEY_PREFIX
            + "SERVICE_RELATIVE_PROVSERV_WSDL";

    /**
     * The parameter key for the protocol to be used to access the service's
     * provisioning service.
     */
    public static final String SERVICE_RELATIVE_PROVSERV_PROTOCOL = APP_PARAM_KEY_PREFIX
            + "PROVSERV_PROTOCOL";

    /**
     * The parameter key for the port to be used to access the service's
     * provisioning service.
     */
    public static final String SERVICE_RELATIVE_PROVSERV_PORT = APP_PARAM_KEY_PREFIX
            + "PROVSERV_PORT";

    /**
     * The parameter key for the public IP of the system.
     */
    public static final String PUBLIC_IP = APP_PARAM_KEY_PREFIX + "PUBLIC_IP";

    /**
     * The technical key of the entity.
     */
    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "do_seq")
    @TableGenerator(table = "hibernate_sequences",  name = "do_seq", allocationSize = 1000, valueColumnName = "sequence_next_hi_value")
    private long tkey;

    /**
     * Reference to the service instance containing this parameter.
     */
    @ManyToOne(optional = false)
    private ServiceInstance serviceInstance;

    /**
     * The value of the instance parameter.
     */
    @Column(nullable = false)
    private String parameterValue;

    /**
     * The key of the parameter.
     */
    private String parameterKey;

    /**
     * The indicator if the value is encrypted.
     */
    private boolean encrypted;

    public long getTkey() {
        return tkey;
    }

    public void setTkey(long tkey) {
        this.tkey = tkey;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public String getDecryptedValue() throws BadResultException {
        try {
            return isEncrypted() ? AESEncrypter.decrypt(parameterValue)
                    : parameterValue;
        } catch (GeneralSecurityException e) {
            throw new BadResultException(String.format(
                    "Parameter for key '%s' could not be decrypted",
                    getParameterKey()));
        }
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    public void setDecryptedValue(String parameterValue)
            throws BadResultException {
        try {
            this.parameterValue = isEncrypted()
                    ? AESEncrypter.encrypt(parameterValue) : parameterValue;
        } catch (GeneralSecurityException e) {
            throw new BadResultException(String.format(
                    "Parameter for key '%s' could not be encrypted",
                    getParameterKey()));
        }
    }

    public String getParameterKey() {
        return parameterKey;
    }

    public void setParameterKey(String parameterKey) {
        this.parameterKey = parameterKey;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }
}
