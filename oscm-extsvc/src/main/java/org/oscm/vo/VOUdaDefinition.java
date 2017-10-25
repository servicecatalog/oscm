/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-10-12                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import org.oscm.types.enumtypes.UdaConfigurationType;

/**
 * Represents the definition of a custom attribute.
 */
public class VOUdaDefinition extends BaseVO {

    private static final long serialVersionUID = 805264595187439089L;

    /**
     * The identifier of the custom attribute; must be unique for the target
     * type.
     */
    private String udaId;

    /**
     * The type of entity for which the custom attribute is defined. May be
     * <code>CUSTOMER</code> or <code>CUSTOMER_SUBSCRIPTION</code>.
     */
    private String targetType;

    /**
     * The default value of the custom attribute.
     */
    private String defaultValue;

    /**
     * The configuration type of the custom attribute.
     */
    private UdaConfigurationType configurationType;

    /**
     * Retrieves the identifier of the custom attribute.
     * 
     * @return the attribute ID
     */
    public String getUdaId() {
        return udaId;
    }

    /**
     * Sets the identifier of the custom attribute. The ID must be unique for
     * the given target type.
     * 
     * @param udaId
     *            the attribute ID
     */
    public void setUdaId(String udaId) {
        this.udaId = udaId;
    }

    /**
     * Retrieves the type of entity for which the custom attribute is defined.
     * This may be <code>CUSTOMER</code> or <code>CUSTOMER_SUBSCRIPTION</code>.
     * 
     * @return the entity type
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * Sets the type of entity for which the custom attribute is defined. This
     * may be <code>CUSTOMER</code> or <code>CUSTOMER_SUBSCRIPTION</code>.
     * 
     * @param targetType
     *            the entity type
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * Retrieves the default value defined for the custom attribute.
     * 
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value for the custom attribute.
     * 
     * @param defaultValue
     *            the default value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Sets the configuration type of the custom attribute, specifying to whom
     * the attribute is visible and whether a value is mandatory or optional.
     * 
     * @param configurationType
     *            the configuration type
     */
    public void setConfigurationType(UdaConfigurationType configurationType) {
        this.configurationType = configurationType;
    }

    /**
     * Retrieves the configuration type defined for the custom attribute.
     * 
     * @return the configuration type
     */
    public UdaConfigurationType getConfigurationType() {
        return configurationType;
    }

}
