/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Peter Pock                                             
 *                                                                              
 *  Creation Date: 14.06.2010                                                      
 *                                                                              
 *  Completion Time: 15.06.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.converter.XMLSerializer;
import org.oscm.domobjects.converters.TPPNConverter;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SessionType;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

/**
 * JPA managed entity representing the trigger process parameter data.
 * 
 * @author pock
 * 
 */
@Embeddable
public class TriggerProcessParameterData extends DomainDataContainer {

    private static final long serialVersionUID = 8283145624102989170L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(TriggerProcessParameterData.class);

    /**
     * If all enumeration classes used by the serialized objects (only necessary
     * for XML serialization)
     */
    private static Class<?> enumArray[] = { EventType.class,
            OrganizationRoleType.class, ParameterType.class,
            ParameterValueType.class, PaymentType.class,
            PaymentCollectionType.class, PricingPeriod.class,
            ServiceAccessType.class, ServiceStatus.class, Salutation.class,
            SessionType.class, SettingType.class, SubscriptionStatus.class,
            TriggerType.class, TriggerProcessStatus.class,
            TriggerProcessParameterName.class, UserAccountStatus.class,
            UserRoleType.class, UdaConfigurationType.class,
            ParameterModificationType.class, OfferingType.class };

    /**
     * Creates an object from the xml string.
     * 
     * @param str
     *            the xml string representing the object to create.
     * @return the created object.
     */
    private static Object fromXml(String str) {
        return XMLSerializer.toObject(str);
    }

    /**
     * Serializes an object to a string.
     * 
     * @param source
     *            the object to serialize.
     * @return the string representing the object.
     */
    public static String getVOSerialization(Object source) {
        return toXml(source);
    }

    private static String toXml(Object source) {
        String xml = null;
        try {
            xml = XMLSerializer.toXml(source,
                    TriggerProcessParameterData.enumArray);
        } catch (Exception e) {
            SaaSSystemException se = new SaaSSystemException(
                    "Object encoding failed", e);
            TriggerProcessParameterData.logger.logError(Log4jLogger.SYSTEM_LOG,
                    se, LogMessageIdentifier.ERROR_OBJECT_ENCODING_FAILED);
            throw se;
        }
        return xml;
    }

    /**
     * Creates an object from the given string.
     * 
     * @param clazz
     *            the class of the object to create.
     * @param str
     *            the string representing the object to create.
     * @return the created object.
     */
    public static <T> T getVOFromSerialization(Class<T> clazz, String str) {
        final Object obj;
        obj = fromXml(str);
        return clazz.cast(obj);
    }

    /**
     * The name of the parameter.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerProcessParameterName name;

    /**
     * The serialized parameter value.
     */
    private String serializedValue;
    private boolean serialized = false;

    public TriggerProcessParameterName getName() {
        return name;
    }

    public void setName(TriggerProcessParameterName name) {
        this.name = name;
    }

    public <T> T getValue(Class<T> clazz) {
        if (serializedValue != null) {
            if (serialized) {
                return clazz.cast(fromXml(serializedValue));
            } else {
                return clazz.cast(serializedValue);
            }

        }
        return null;
    }

    public void setValue(Object value) {
        if (value == null) {
            this.serializedValue = null;
        } else {
            this.serializedValue = toXml(value);
            serialized = true;
        }
    }

    protected String getSerializedValue() {
        return serializedValue;
    }

    protected void setSerializedValue(String serializedValue) {
        this.serializedValue = serializedValue;
        serialized = false;
    }

}
