/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 17.06.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import org.oscm.types.enumtypes.TriggerProcessParameterType;

/**
 * Represents a parameter of a trigger process.
 * <p>
 * The value of a trigger process parameter is the serialized form of a specific
 * value object in XML format. The value object is specified by the type of the
 * trigger process parameter.
 * 
 */
public class VOTriggerProcessParameter extends BaseVO {

    private static final long serialVersionUID = -7271737602848430658L;

    /**
     * The type of the trigger process parameter.
     */
    private TriggerProcessParameterType type;

    /**
     * The value of the trigger process parameter in XML format.
     */
    private String value;

    /**
     * The numeric key of the trigger process to which the trigger process
     * parameter belongs.
     */
    private Long triggerProcessKey;

    /**
     * Returns the type of the trigger process parameter.
     * 
     * @return a <code>TriggerProcessParameterType</code> object specifying the
     *         type
     */
    public TriggerProcessParameterType getType() {
        return type;
    }

    /**
     * Sets the type of the trigger process parameter.
     * 
     * @param type
     *            a <code>TriggerProcessParameterType</code> object specifying
     *            the type
     */
    public void setType(TriggerProcessParameterType type) {
        this.type = type;
    }

    /**
     * Returns the value of the trigger process parameter, which is the
     * serialized form of a specific value object in XML format.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the trigger process parameter, which is the serialized
     * form of a specific value object in XML format.
     * 
     * @param value
     *            the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Retrieves the numeric key of the trigger process to which the trigger
     * process parameter belongs.
     * 
     * @return the key
     * 
     */
    public Long getTriggerProcessKey() {
        return triggerProcessKey;
    }

    /**
     * Sets the numeric key of the trigger process to which trigger process
     * parameter belongs.
     * 
     * @param triggerProcessKey
     *            the key
     */
    public void setTriggerProcessKey(Long triggerProcessKey) {
        this.triggerProcessKey = triggerProcessKey;
    }
}
