/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-07                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.provisioning.data;

/**
 * Provides information on a service parameter. An application can evaluate the
 * information and perform the corresponding actions, for example, activate or
 * deactivate a specific feature.
 */
public class ServiceParameter {

    /**
     * The identifier of the parameter.
     * 
     */
    private String parameterId;

    /**
     * The value of the parameter.
     * 
     */
    private String value;

    /**
     * The indicator if the value must be encrypted.
     */
    private boolean encrypted;

    /**
     * Retrieves the identifier of the parameter.
     * 
     * @return the parameter ID
     */
    public String getParameterId() {
        return parameterId;
    }

    /**
     * Sets the identifier of the parameter.
     * 
     * @param parameterId
     *            the parameter ID
     */
    public void setParameterId(String parameterId) {
        this.parameterId = parameterId;
    }

    /**
     * Retrieves the value of the parameter.
     * 
     * @return the parameter value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the parameter.
     * 
     * @param value
     *            the parameter value
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

}
