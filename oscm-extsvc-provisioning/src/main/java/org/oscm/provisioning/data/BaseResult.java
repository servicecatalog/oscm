/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-08                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.provisioning.data;

/**
 * Provides the basic data returned to the platform upon calls to the
 * provisioning service of an application. These basic data consists in a return
 * code and a status message.
 */
public class BaseResult {

    /**
     * The return code of an operation. A value of 0 indicates that the
     * operation was successful. A value greater than 0 indicates an error.
     */
    private int rc;
    /**
     * The status message.
     */
    private String desc;

    /**
     * Retrieves the return code for an operation.
     * 
     * @return the return code. A value of 0 indicates that the operation was
     *         successful. A value greater than 0 indicates an error.
     */
    public int getRc() {
        return rc;
    }

    /**
     * Sets the return code for an operation.
     * 
     * @param returnCode
     *            the return code. A value of 0 indicates that the operation was
     *            successful. A value greater than 0 indicates an error.
     */
    public void setRc(int returnCode) {
        this.rc = returnCode;
    }

    /**
     * Retrieves the text displayed as the status message for an operation.
     * 
     * @return the message text
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets the text to be displayed as the status message for an operation.
     * 
     * @param text
     *            the message text
     */
    public void setDesc(String text) {
        this.desc = text;
    }

}
