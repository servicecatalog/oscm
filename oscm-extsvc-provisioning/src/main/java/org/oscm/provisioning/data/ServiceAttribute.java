/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-07                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.provisioning.data;

/**
 * Provides information on a service attribute. An application can evaluate the
 * information and perform the corresponding actions, for example, overwrite the
 * credentials of the controller.
 */
public class ServiceAttribute {

    /**
     * The identifier of the attribute.
     * 
     */
    private String attributeId;

    /**
     * The value of the attribute.
     * 
     */
    private String value;

    /**
     * The indicator if the value must be encrypted.
     */
    private boolean encrypted;

    /**
     * The controller the attribute is meant for.
     */
    private String controllerId;

    /**
     * Retrieves the identifier of the attribute.
     * 
     * @return the parameter ID
     */
    public String getAttributeId() {
        return attributeId;
    }

    /**
     * Sets the identifier of the attribute.
     * 
     * @param attributeId
     *            the attribute ID
     */
    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    /**
     * Retrieves the value of the attribute.
     * 
     * @return the attribute value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the attribute.
     * 
     * @param value
     *            the attribute value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns true if the value must be encrypted.
     * 
     * @return the encrypted
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Sets the indicator if the value must be encrypted.
     * 
     * @param encrypted
     *            the encrypted to set
     */
    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * Retrieves the controller the attribute is meant for.
     * 
     * @return the controllerId
     */
    public String getControllerId() {
        return controllerId;
    }

    /**
     * Sets the controller the attribute is meant for.
     * 
     * @param controllerId
     *            the controllerId to set
     */
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

}
