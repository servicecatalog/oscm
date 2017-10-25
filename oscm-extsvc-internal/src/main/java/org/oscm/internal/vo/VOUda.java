/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-10-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * Represents a custom attribute with a value for a specific entity. For
 * example, a custom attribute, <code>Customer ID</code>, can take on a
 * different value for each customer.
 * 
 */
public class VOUda extends BaseVO {

    private static final long serialVersionUID = 2479362769410811330L;

    private VOUdaDefinition udaDefinition;

    private String udaValue;
    private long targetObjectKey;

    /**
     * Retrieves the definition of the custom attribute.
     * 
     * @return the attribute definition
     * 
     */
    public VOUdaDefinition getUdaDefinition() {
        return udaDefinition;
    }

    /**
     * Sets the definition of the custom attribute.
     * 
     * @param udaDefinition
     *            the attribute definition
     * 
     */
    public void setUdaDefinition(VOUdaDefinition udaDefinition) {
        this.udaDefinition = udaDefinition;
    }

    /**
     * Retrieves the value of the custom attribute.
     * 
     * @return the attribute value
     * 
     */
    public String getUdaValue() {
        return udaValue;
    }

    /**
     * Sets the value of the custom attribute.
     * 
     * @param udaValue
     *            the attribute value
     * 
     */
    public void setUdaValue(String udaValue) {
        this.udaValue = udaValue;
    }

    /**
     * Retrieves the numeric key of the entity to which the custom attribute is
     * assigned. The entity is, for example, a customer organization or a
     * subscription.
     * 
     * @return the key
     */
    public long getTargetObjectKey() {
        return targetObjectKey;
    }

    /**
     * Sets the numeric key of the entity to which the custom attribute is
     * assigned. The entity is, for example, a customer organization or a
     * subscription.
     * 
     * @param targetObjectKey
     *            the key
     */
    public void setTargetObjectKey(long targetObjectKey) {
        this.targetObjectKey = targetObjectKey;
    }
}
