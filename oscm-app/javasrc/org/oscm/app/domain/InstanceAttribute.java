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
 * Represents a attributes setting for a service instance.
 * 
 * @author miethaner
 * 
 */
@NamedQueries({
        @NamedQuery(name = "InstanceAttribute.getAllForInstanceId", query = "SELECT ia FROM InstanceAttribute ia WHERE ia.serviceInstance.instanceId = :sid and ia.serviceInstance.controllerId = :cid and ia.controllerId = :cid") })
@Entity
public class InstanceAttribute {

    /**
     * The technical key of the entity.
     */
    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "do_seq")
    @TableGenerator(table = "hibernate_sequences", name = "do_seq", allocationSize = 1000, valueColumnName = "sequence_next_hi_value")
    private long tkey;
//glassfish upgrade. SequenceGenerator updated to TableGenerator
    /**
     * Reference to the service instance containing this parameter.
     */
    @ManyToOne(optional = false)
    private ServiceInstance serviceInstance;

    /**
     * The value of the instance parameter.
     */
    private String attributeValue;

    /**
     * The key of the parameter.
     */
    private String attributeKey;

    /**
     * The indicator if the value is encrypted.
     */
    private boolean encrypted;

    /**
     * The controller this attribute is meant for.
     */
    private String controllerId;

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

    public String getAttributeValue() {
        return attributeValue;
    }

    public String getDecryptedValue() throws BadResultException {
        try {
            return isEncrypted() ? AESEncrypter.decrypt(attributeValue)
                    : attributeValue;
        } catch (GeneralSecurityException e) {
            throw new BadResultException(String.format(
                    "Attribute for key '%s' could not be decrypted",
                    getAttributeKey()));
        }
    }

    public void setAttributeValue(String parameterValue) {
        this.attributeValue = parameterValue;
    }

    public void setDecryptedValue(String parameterValue)
            throws BadResultException {
        try {
            this.attributeValue = isEncrypted()
                    ? AESEncrypter.encrypt(parameterValue) : parameterValue;
        } catch (GeneralSecurityException e) {
            throw new BadResultException(String.format(
                    "Attribute for key '%s' could not be encrypted",
                    getAttributeKey()));
        }
    }

    public String getAttributeKey() {
        return attributeKey;
    }

    public void setAttributeKey(String parameterKey) {
        this.attributeKey = parameterKey;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }
}
