/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Mar 31, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Representation of a template for a service controller.
 */
public class Template implements Serializable {

    private static final long serialVersionUID = 5630487379829488409L;

    private String fileName;
    private byte[] content;
    private Date lastChange;

    /**
     * Gets the file name of the template including its suffix.
     * 
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name of the template including its suffix
     * 
     * @param fileName
     *            the file name to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the content of the template.
     * 
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets the content of the template.
     * 
     * @param content
     *            the content to set
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Gets the timestamp of the last change to the template.
     * 
     * @return the timestamp
     */
    public Date getLastChange() {
        return lastChange;
    }

    /**
     * Sets the timestamp of the last change to the template.
     * 
     * @param lastChange
     *            the timestamp to set
     */
    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
    }
}
